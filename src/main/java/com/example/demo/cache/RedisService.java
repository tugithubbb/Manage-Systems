package com.example.demo.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.LinkedHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);

            if (value == null) {
                return null;
            }

            // Fix: Nếu value là LinkedHashMap, convert sang class mong muốn
            if (value instanceof LinkedHashMap<?,?>) {
                return objectMapper.convertValue(value, clazz);
            }

            // Nếu đã đúng type, cast trực tiếp
            return clazz.cast(value);

        } catch (Exception e) {
            log.error("Error getting key: {} with class: {}", key, clazz.getName(), e);
            return null;
        }
    }

    public void set(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
        } catch (Exception e) {
            log.error("Error setting key: {}", key, e);
        }
    }

    public void delete(String key) {
        try {
            redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("Error deleting key: {}", key, e);
        }
    }

    public boolean exists(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("Error checking existence of key: {}", key, e);
            return false;
        }
    }
    public boolean tryLock(String key,Object value, Duration ttl ){
        try {
            Boolean locked = redisTemplate.opsForValue().setIfAbsent(key,value,ttl);
            return Boolean.TRUE.equals(locked);

        }catch (Exception e){
            log.error("Error trying to lock key: {}", key, e);
            return false;
        }
    }

}
