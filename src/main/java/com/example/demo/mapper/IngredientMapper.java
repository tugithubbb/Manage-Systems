package com.example.demo.mapper;

import com.example.demo.Entity.Ingredient;
import com.example.demo.dto.request.IngredientCreateRequest;
import com.example.demo.dto.request.IngredientUpdateRequest;
import com.example.demo.dto.response.IngredientCreateResponse;
import com.example.demo.dto.response.IngredientResponse;
import com.example.demo.dto.response.IngredientUpdateResponse;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface IngredientMapper {
    Ingredient toEntity(IngredientCreateRequest request);
    IngredientResponse toResponse(Ingredient ingredient);
    IngredientCreateResponse toCreateResponse(Ingredient ingredient);
    IngredientUpdateResponse toUpdateResponse(Ingredient ingredient);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateIngredientFromDTO(IngredientUpdateRequest request, @MappingTarget Ingredient ingredient);

}
