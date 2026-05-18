package com.campusmarket.service;

import com.campusmarket.dto.*;
import com.campusmarket.model.*;
import com.campusmarket.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 订单核心业务：下单→支付→收货→退货，以及平台资金托管与结算。
 *
 * 资金流：买家付款 → 系统中间账户托管 → 买家确认收货(或7天自动确认) → 卖家收款(扣除手续费)
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletTransactionRepository walletTransactionRepository;

    @Autowired
    private SystemAccountRepository systemAccountRepository;

    @Autowired
    private ReturnRequestRepository returnRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Value("${app.merchant.auto-confirm-days}")
    private int autoConfirmDays;

    @Value("${app.merchant.return-hours}")
    private int returnHours;

    // 商家等级→平台费率，等级越高费率越高
    private Map<Integer, Double> feeLevels = new HashMap<>();

    @Value("${app.points.rate}")
    private int pointsRate;

    @PostConstruct
    public void init() {
        feeLevels.put(1, 0.001);   // 0.1%
        feeLevels.put(2, 0.002);   // 0.2%
        feeLevels.put(3, 0.005);   // 0.5%
        feeLevels.put(4, 0.0075);  // 0.75%
        feeLevels.put(5, 0.01);    // 1%
    }

    /**
     * 创建订单：校验库存→扣款→资金托管→扣减库存→清空购物车。
     * 一个订单请求可能包含多个商品（购物车一键下单），每个商品生成一条独立订单。
     */
    @Transactional
    public ApiResponse<?> createOrder(Long userId, OrderRequest req) {
        User user = userRepository.findById(userId).orElseThrow();
        Wallet wallet = walletRepository.findByUserId(userId).orElseThrow();

        if (req.getItems() == null || req.getItems().isEmpty()) {
            return ApiResponse.error("订单商品列表为空");
        }

        double totalAmount = 0;
        List<Order> orders = new ArrayList<>();

        for (OrderRequest.CartOrderItem item : req.getItems()) {
            Product product = productRepository.findById(item.getProductId()).orElse(null);
            if (product == null || product.getStatus() != Product.ProductStatus.APPROVED) {
                return ApiResponse.error("商品 " + item.getProductId() + " 不可购买");
            }
            if (product.getQuantity() < item.getQuantity()) {
                return ApiResponse.error(product.getName() + " 库存不足");
            }

            double itemTotal = product.getDiscountPrice() * item.getQuantity();
            totalAmount += itemTotal;

            // 根据商家等级计算平台手续费
            int merchantLevel = product.getMerchant().getMerchantLevel() != null ?
                    product.getMerchant().getMerchantLevel() : 1;
            double feeRate = feeLevels.getOrDefault(merchantLevel, 0.005);
            double platformFee = itemTotal * feeRate;

            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUser(user);
            order.setProduct(product);
            order.setQuantity(item.getQuantity());
            order.setTotalPrice(itemTotal);
            order.setPlatformFee(platformFee);
            order.setStatus(Order.OrderStatus.PAID);
            order.setMeetLocation(req.getMeetLocation());
            order.setMeetTime(req.getMeetTime());
            orders.add(order);
        }

        if (wallet.getBalance() < totalAmount) {
            return ApiResponse.error("余额不足，当前余额: ¥" + String.format("%.2f", wallet.getBalance()));
        }

        // 扣买家余额，同时累积积分（1元=1积分）
        wallet.setBalance(wallet.getBalance() - totalAmount);
        int addPoints = (int) totalAmount;
        wallet.setPoints(wallet.getPoints() + addPoints);
        walletRepository.save(wallet);

        WalletTransaction tx = new WalletTransaction();
        tx.setUser(user);
        tx.setWallet(wallet);
        tx.setAmount(-totalAmount);
        tx.setType(WalletTransaction.TransactionType.PURCHASE);
        tx.setDescription("购买商品，获得积分: " + addPoints);
        walletTransactionRepository.save(tx);

        // 货款暂存系统中间账户，待买家确认收货后转给卖家
        SystemAccount sysAccount = getSystemAccount();
        sysAccount.setBalance(sysAccount.getBalance() + totalAmount);
        systemAccountRepository.save(sysAccount);

        for (Order order : orders) {
            orderRepository.save(order);
            Product product = order.getProduct();
            product.setQuantity(product.getQuantity() - order.getQuantity());
            product.setSalesCount(product.getSalesCount() + order.getQuantity());
            if (product.getQuantity() <= 0) {
                product.setStatus(Product.ProductStatus.SOLD_OUT);
            }
            productRepository.save(product);
        }

        // 下单成功后清空购物车
        cartItemRepository.deleteByUserId(userId);

        return ApiResponse.success("下单成功，共 ¥" + String.format("%.2f", totalAmount), orders);
    }

    /**
     * 买家确认收货：系统中间账户转款给卖家（扣除平台手续费），订单完成。
     */
    @Transactional
    public ApiResponse<?> confirmReceive(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("无权操作");
        }
        if (order.getStatus() != Order.OrderStatus.PAID) {
            return ApiResponse.error("订单状态不允许确认收货");
        }

        order.setStatus(Order.OrderStatus.RECEIVED);
        order.setReceiveTime(LocalDateTime.now());

        SystemAccount sysAccount = getSystemAccount();
        double totalPrice = order.getTotalPrice();
        double fee = order.getPlatformFee() != null ? order.getPlatformFee() : 0;
        double merchantAmount = totalPrice - fee; // 卖家实收=货款-手续费

        sysAccount.setBalance(sysAccount.getBalance() - totalPrice);
        sysAccount.setTotalFee(sysAccount.getTotalFee() + fee);
        systemAccountRepository.save(sysAccount);

        Wallet merchantWallet = walletRepository
                .findByUserId(order.getProduct().getMerchant().getId()).orElseThrow();
        merchantWallet.setBalance(merchantWallet.getBalance() + merchantAmount);
        walletRepository.save(merchantWallet);

        WalletTransaction merchantTx = new WalletTransaction();
        merchantTx.setUser(order.getProduct().getMerchant());
        merchantTx.setWallet(merchantWallet);
        merchantTx.setAmount(merchantAmount);
        merchantTx.setType(WalletTransaction.TransactionType.REFUND);
        merchantTx.setDescription("商品售出: " + order.getProduct().getName()
                + ", 手续费: ¥" + String.format("%.2f", fee));
        walletTransactionRepository.save(merchantTx);

        order.setStatus(Order.OrderStatus.COMPLETED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        return ApiResponse.success("确认收货成功");
    }

    /**
     * 买家申请退货：需在确认收货后24小时内发起，提交后等待商家审核。
     */
    @Transactional
    public ApiResponse<?> requestReturn(Long userId, ReturnRequestDTO req) {
        Order order = orderRepository.findById(req.getOrderId()).orElseThrow();
        if (!order.getUser().getId().equals(userId)) {
            return ApiResponse.error("无权操作");
        }
        if (order.getStatus() != Order.OrderStatus.RECEIVED) {
            return ApiResponse.error("仅已收货的订单可以申请退货");
        }
        if (order.getReceiveTime() != null &&
            LocalDateTime.now().isAfter(order.getReceiveTime().plusHours(returnHours))) {
            return ApiResponse.error("已超过" + returnHours + "小时退货期限");
        }

        ReturnRequest rr = new ReturnRequest();
        rr.setOrder(order);
        rr.setUser(userRepository.findById(userId).orElseThrow());
        rr.setReason(req.getReason());
        rr.setStatus(ReturnRequest.ReturnStatus.PENDING);
        returnRequestRepository.save(rr);

        order.setStatus(Order.OrderStatus.RETURNING);
        orderRepository.save(order);
        return ApiResponse.success("退货申请已提交");
    }

    /**
     * 商家同意退货：退款给买家并扣回积分，恢复商品库存。
     */
    @Transactional
    public ApiResponse<?> approveReturn(Long merchantId, Long returnId) {
        ReturnRequest rr = returnRequestRepository.findById(returnId).orElseThrow();
        Order order = rr.getOrder();
        if (!order.getProduct().getMerchant().getId().equals(merchantId)) {
            return ApiResponse.error("无权操作");
        }

        rr.setStatus(ReturnRequest.ReturnStatus.APPROVED);
        returnRequestRepository.save(rr);

        // 退款
        Wallet buyerWallet = walletRepository.findByUserId(order.getUser().getId()).orElseThrow();
        double refundAmount = order.getTotalPrice();
        buyerWallet.setBalance(buyerWallet.getBalance() + refundAmount);
        int pointsDeduct = order.getTotalPrice().intValue();
        buyerWallet.setPoints(Math.max(0, buyerWallet.getPoints() - pointsDeduct));
        walletRepository.save(buyerWallet);

        WalletTransaction refundTx = new WalletTransaction();
        refundTx.setUser(order.getUser());
        refundTx.setWallet(buyerWallet);
        refundTx.setAmount(refundAmount);
        refundTx.setType(WalletTransaction.TransactionType.REFUND);
        refundTx.setDescription("退货退款: " + order.getProduct().getName());
        refundTx.setRelatedOrderId(order.getId());
        walletTransactionRepository.save(refundTx);

        // 恢复库存
        Product product = order.getProduct();
        product.setQuantity(product.getQuantity() + order.getQuantity());
        if (product.getStatus() == Product.ProductStatus.SOLD_OUT && product.getQuantity() > 0) {
            product.setStatus(Product.ProductStatus.APPROVED);
        }
        productRepository.save(product);

        order.setStatus(Order.OrderStatus.RETURNED);
        orderRepository.save(order);

        return ApiResponse.success("退货已批准");
    }

    @Transactional
    public ApiResponse<?> rejectReturn(Long merchantId, Long returnId, String reply) {
        ReturnRequest rr = returnRequestRepository.findById(returnId).orElseThrow();
        if (!rr.getOrder().getProduct().getMerchant().getId().equals(merchantId)) {
            return ApiResponse.error("无权操作");
        }
        rr.setStatus(ReturnRequest.ReturnStatus.REJECTED);
        rr.setMerchantReply(reply);
        returnRequestRepository.save(rr);

        rr.getOrder().setStatus(Order.OrderStatus.RETURN_REJECTED);
        orderRepository.save(rr.getOrder());
        return ApiResponse.success("退货已拒绝");
    }

    public List<Order> getUserOrders(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Order> getMerchantOrders(Long merchantId) {
        return orderRepository.findByProduct_Merchant_Id(merchantId);
    }

    public List<ReturnRequest> getMerchantReturns(Long merchantId) {
        return returnRequestRepository
                .findByOrder_Product_Merchant_IdAndStatus(merchantId, ReturnRequest.ReturnStatus.PENDING);
    }

    public List<ReturnRequest> getUserReturns(Long userId) {
        return returnRequestRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    // 系统中间账户采用单例模式：全系统只有一个中间账户，不存在则自动创建
    private SystemAccount getSystemAccount() {
        List<SystemAccount> accounts = systemAccountRepository.findAll();
        if (accounts.isEmpty()) {
            SystemAccount acc = new SystemAccount();
            return systemAccountRepository.save(acc);
        }
        return accounts.get(0);
    }

    // 订单号格式：CM + 时间戳 + 4位随机数
    private String generateOrderNo() {
        return "CM" + System.currentTimeMillis() + String.format("%04d", (int)(Math.random() * 10000));
    }
}
