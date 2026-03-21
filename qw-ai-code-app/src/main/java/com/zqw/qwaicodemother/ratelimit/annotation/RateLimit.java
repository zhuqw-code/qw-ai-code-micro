package com.zqw.qwaicodemother.ratelimit.annotation;

import com.zqw.qwaicodemother.ratelimit.enums.RateLimitType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * 存入到redisson中的key
     */
    String key() default "";

    /**
     * 每个时间窗口允许的请求数
     */
    int rate() default 2;

    /**
     * 时间窗口大小
     */
    int rateInterval() default 60;

    /**
     * 限流类型
     */
    RateLimitType limitType() default RateLimitType.USER;

    /**
     * 限流提示信息
     */
    String message() default "请求过于频繁，预警";
}
