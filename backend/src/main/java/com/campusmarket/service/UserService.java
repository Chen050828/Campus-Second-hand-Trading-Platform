package com.campusmarket.service;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import com.campusmarket.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    // 验证码暂存内存（生产环境应改用Redis）
    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();

    public String generateCaptcha(String key) {
        String code = String.format("%04d", (int)(Math.random() * 10000));
        captchaStore.put(key, code);
        return code;
    }

    // 验证码校验后立即删除，防止重复使用
    public boolean validateCaptcha(String key, String code) {
        String stored = captchaStore.remove(key);
        return stored != null && stored.equalsIgnoreCase(code);
    }

    /**
     * 用户注册：所有新用户默认状态为PENDING，需管理员审核后才能登录。
     * 商家注册时会额外设置店名和初始等级。
     */
    @Transactional
    public ApiResponse<?> register(RegisterRequest req) {
        if (!validateCaptcha(req.getCaptchaKey(), req.getCaptcha())) {
            return ApiResponse.error("验证码错误");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            return ApiResponse.error("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(passwordEncoder.encode(req.getPassword()));
        user.setName(req.getName());
        user.setPhone(req.getPhone());
        user.setEmail(req.getEmail());
        user.setCity(req.getCity());
        user.setGender(req.getGender());
        user.setBankAccount(req.getBankAccount());
        try {
            user.setRole(User.UserRole.valueOf(req.getRole()));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("无效的用户角色");
        }
        user.setStatus(User.UserStatus.PENDING);
        // 商家注册时保存营业执照和身份证图片
        if (User.UserRole.MERCHANT.equals(user.getRole())) {
            user.setBusinessLicenseImg(req.getBusinessLicenseImg());
            user.setIdCardImg(req.getIdCardImg());
        }
        if (User.UserRole.MERCHANT.equals(user.getRole()) && req.getStoreName() != null) {
            user.setStoreName(req.getStoreName());
            user.setMerchantLevel(1); // 新商家默认等级1
        }
        userRepository.save(user);

        // 注册时自动创建钱包
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        walletRepository.save(wallet);

        return ApiResponse.success("注册成功，等待管理员审核");
    }

    public ApiResponse<?> login(LoginRequest req) {
        if (!validateCaptcha(req.getCaptchaKey(), req.getCaptcha())) {
            return ApiResponse.error("验证码错误");
        }
        Optional<User> opt = userRepository.findByUsername(req.getUsername());
        if (!opt.isPresent()) {
            return ApiResponse.error("用户名或密码错误");
        }
        User user = opt.get();
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            return ApiResponse.error("用户名或密码错误");
        }
        // 只有审核通过的用户才能登录
        if (user.getStatus() != User.UserStatus.APPROVED) {
            return ApiResponse.error("账号未审核通过，请等待管理员审核");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getId(), user.getRole().name());
        return ApiResponse.success("登录成功", Map.of(
            "token", token,
            "userId", user.getId(),
            "username", user.getUsername(),
            "role", user.getRole().name(),
            "name", user.getName()
        ));
    }

    public User getById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public List<User> getPendingUsers() {
        return userRepository.findByStatus(User.UserStatus.PENDING);
    }

    public List<User> getMerchants() {
        return userRepository.findByRole(User.UserRole.MERCHANT);
    }

    @Transactional
    public void approveUser(Long userId) {
        userRepository.findById(userId).ifPresent(user -> {
            user.setStatus(User.UserStatus.APPROVED);
            userRepository.save(user);
        });
    }

    @Transactional
    public void rejectUser(Long userId) {
        // 拒绝后直接删除用户及关联数据，不保留无效记录
        deleteUser(userId);
    }

    @Transactional
    public void updateMerchantLevel(Long userId, int level) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getRole() == User.UserRole.MERCHANT && level >= 1 && level <= 5) {
                user.setMerchantLevel(level);
                userRepository.save(user);
            }
        });
    }

    @Transactional
    public void updateUser(Long userId, User updates) {
        userRepository.findById(userId).ifPresent(user -> {
            if (updates.getName() != null) user.setName(updates.getName());
            if (updates.getPhone() != null) user.setPhone(updates.getPhone());
            if (updates.getEmail() != null) user.setEmail(updates.getEmail());
            if (updates.getCity() != null) user.setCity(updates.getCity());
            userRepository.save(user);
        });
    }

    /**
     * 删除用户前先级联删除关联数据，避免外键约束冲突。
     * 删除顺序：购物车→退货→评价→订单→商品→钱包流水→钱包→用户
     */
    @Transactional
    public void deleteUser(Long userId) {
        cartItemRepository.deleteByUserId(userId);
        returnRequestRepository.deleteByUserId(userId);
        reviewRepository.deleteByUserId(userId);
        orderRepository.deleteByUserId(userId);
        productRepository.deleteByMerchantId(userId);
        walletRepository.findByUserId(userId).ifPresent(w -> {
            walletTransactionRepository.deleteByWalletId(w.getId());
            walletRepository.delete(w);
        });
        userRepository.deleteById(userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
