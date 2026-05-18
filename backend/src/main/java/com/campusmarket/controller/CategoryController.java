package com.campusmarket.controller;

import com.campusmarket.dto.*;
import com.campusmarket.model.Category;
import com.campusmarket.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(categoryRepository.findAll());
    }

    @PostMapping
    public ApiResponse<?> create(@RequestBody Category category) {
        categoryRepository.save(category);
        return ApiResponse.success("创建成功");
    }
}
