package com.example.demo.repository;

import com.example.demo.Entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<Ingredient,String> {
}
