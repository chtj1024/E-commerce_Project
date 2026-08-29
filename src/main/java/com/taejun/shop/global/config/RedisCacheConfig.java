package com.taejun.shop.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.taejun.shop.domain.product.dto.ProductResponse;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisCacheConfig {

    public static final String PRODUCT_CACHE = "products";

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(
            ObjectMapper objectMapper
    ) {
        Jackson2JsonRedisSerializer<ProductResponse> valueSerializer =
                new Jackson2JsonRedisSerializer<>(
                        objectMapper,
                        ProductResponse.class
                );

        RedisCacheConfiguration productCacheConfiguration =
                RedisCacheConfiguration.defaultCacheConfig()
                        .entryTtl(Duration.ofMinutes(10))
                        .disableCachingNullValues()
                        .serializeValuesWith(
                                RedisSerializationContext.SerializationPair
                                        .fromSerializer(valueSerializer)
                        );

        return builder -> builder.withCacheConfiguration(
                PRODUCT_CACHE,
                productCacheConfiguration
        );
    }
}
