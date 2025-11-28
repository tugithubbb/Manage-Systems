package com.example.demo.controller;

import com.example.demo.dto.request.IngredientCreateRequest;
import com.example.demo.dto.request.IngredientUpdateRequest;
import com.example.demo.dto.response.ApiResponse;
import com.example.demo.dto.response.IngredientCreateResponse;
import com.example.demo.dto.response.IngredientResponse;
import com.example.demo.dto.response.IngredientUpdateResponse;
import com.example.demo.services.IngredientService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/admin/ingredient")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientAdminController {
    IngredientService ingredientService;
    @GetMapping("/list")
    public ApiResponse<List<IngredientResponse>> list() {
        List<IngredientResponse> list = ingredientService.getAllIngredients();

        return ApiResponse.<List<IngredientResponse>>builder()
                .code(1000)
                .message("ok")
                .result(list)
                .build();
    }
    @GetMapping("/detail/{id}")
    public ApiResponse<IngredientResponse> detail(@PathVariable String id) {

        return ApiResponse.<IngredientResponse>builder()
                .code(1000)
                .message("Chi tiết nguyên liệu")
                .result(ingredientService.getIngredientById(id))
                .build();
    }
    @PostMapping("/create")
    public ApiResponse<IngredientCreateResponse> create(@RequestBody IngredientCreateRequest request) {

        return ApiResponse.<IngredientCreateResponse>builder()
                .code(1000)
                .message("Tạo nguyên liệu thành công")
                .result(ingredientService.createIngredient(request))
                .build();
    }
    @PutMapping("/update/{id}")
    public ApiResponse<IngredientUpdateResponse> update(
            @PathVariable String id,
            @RequestBody IngredientUpdateRequest request) {


        return ApiResponse.<IngredientUpdateResponse>builder()
                .code(1000)
                .message("Cập nhật nguyên liệu thành công")
                .result(ingredientService.updateIngredient(id,request))
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        ingredientService.deleteIngredient(id);
        return ApiResponse.<Void>builder()
                .code(1000)
                .message("Xóa nguyên liệu thành công")
                .result(null)
                .build();
    }

}
