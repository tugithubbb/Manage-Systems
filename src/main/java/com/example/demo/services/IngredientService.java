package com.example.demo.services;

import com.example.demo.Entity.Ingredient;
import com.example.demo.dto.request.IngredientCreateRequest;
import com.example.demo.dto.request.IngredientUpdateRequest;
import com.example.demo.dto.response.IngredientCreateResponse;
import com.example.demo.dto.response.IngredientResponse;
import com.example.demo.dto.response.IngredientUpdateResponse;
import com.example.demo.mapper.IngredientMapper;
import com.example.demo.repository.IngredientRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientService {
    IngredientMapper ingredientMapper;
    IngredientRepository ingredientRepository;

    // List all ingredients
    public List<IngredientResponse> getAllIngredients() {
        return ingredientRepository.findAll()
                .stream()
                .map(ingredientMapper::toResponse)
                .collect(Collectors.toList());
    }
    public IngredientResponse getIngredientById(String id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        return ingredientMapper.toResponse(ingredient);
    }
    // Create new ingredient
    public IngredientCreateResponse createIngredient(IngredientCreateRequest request) {
        Ingredient ingredient = ingredientMapper.toEntity(request);
        ingredientRepository.save(ingredient);
        return ingredientMapper.toCreateResponse(ingredient);
    }
    // Update ingredient
    public IngredientUpdateResponse updateIngredient(String id, IngredientUpdateRequest request) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        ingredientMapper.updateIngredientFromDTO(request, ingredient);
        ingredientRepository.save(ingredient);
        return ingredientMapper.toUpdateResponse(ingredient);
    }
    // Delete ingredient
    public void deleteIngredient(String id) {
        Ingredient ingredient = ingredientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ingredient not found"));
        ingredientRepository.delete(ingredient);
    }
}
