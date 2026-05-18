package com.campusmarket.controller;

import com.campusmarket.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RestController
@RequestMapping("/files")
public class FileController {

    @Value("${app.upload.path}")
    private String uploadPath;

    @PostMapping("/upload")
    public ApiResponse<?> upload(@RequestParam("files") MultipartFile[] files) {
        List<String> urls = new ArrayList<>();
        try {
            Path dir = Paths.get(uploadPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                String originalName = file.getOriginalFilename();
                String ext = originalName != null && originalName.contains(".") ?
                        originalName.substring(originalName.lastIndexOf(".")) : ".jpg";
                String newName = UUID.randomUUID().toString() + ext;
                Path target = dir.resolve(newName);
                Files.copy(file.getInputStream(), target);
                urls.add("/api/uploads/" + newName);
            }
            return ApiResponse.success("上传成功", urls);
        } catch (IOException e) {
            return ApiResponse.error("文件上传失败: " + e.getMessage());
        }
    }
}
