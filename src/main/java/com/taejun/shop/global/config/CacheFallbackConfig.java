package com.taejun.shop.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheFallbackConfig implements CachingConfigurer {

    private static final Logger log =
            LoggerFactory.getLogger(CacheFallbackConfig.class);

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new CacheErrorHandler() {

            // 기존은 Redis 조회 과정에서 예외가 발생하면 ProductService의 @Cacheable이 동작하지 않고 프로그램이 멈추게 된다
            // 하지만 예외가 발생했을 때 이 코드가 @Cacheable를 무시 시키고, 기존 메서드를 동작 시킨다. (Mysql 조회)
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn(
                        "Cache GET failed. cache={}, key={}, Falling back to database",
                        cache.getName(),
                        key,
                        exception
                );
            }

            // 기존 DB에서 조회한 것을 Redis 캐시에 저장하다가 실패했을 때면 프로그램을 멈추겠지만
            // 이 코드가 멈추지 않게 이미 DB에서 조회한 결과를 그대로 얻게 한다.
            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn(
                        "Cache PUT faield. cache={}, key={}. Returning database result.",
                        cache.getName(),
                        key,
                        exception
                );
            }

            // Redis 캐시를 삭제하지 못했을 때 예외를 발생시켜 프로그램을 멈추지 않고, 로그 메세지만 출력하는 코드.
            // 실패하고 로그만 남겨도 캐시 삭제는 안되기 때문에
            // 더 나아가 삭제 실패 작업을 MySQL Outbox 테이블에 저장하고 주기적으로 재시도하는 로직을 구성하는 게 좋다.
            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn(
                        "Cache EVICT failed. cache={}, key={}. Database operation continues.",
                        cache.getName(),
                        key,
                        exception
                );
            }

            // 캐시 전체가 삭제 실패 했을 때 예외 처리 시키지 않고 로그만 발생하게 한다.
            // 얘도 별도의 outbox 또는 재시도 시스템이 있어야 한다.
            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn(
                        "Cache CLEAR failed. cache={}",
                        cache.getName(),
                        exception
                );
            }
        };
    }
}
