package com.example.peakly.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;


@Configuration
public class RedisConfig {
    /**
     * Create a StringRedisTemplate configured with the provided Redis connection factory.
     *
     * @param cf the RedisConnectionFactory used by the template
     * @return a StringRedisTemplate that uses the given connection factory
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }

    /**
     * Creates and configures a RedisTemplate for String keys and JSON-serialized Object values.
     *
     * @param cf the RedisConnectionFactory used by the template
     * @return a RedisTemplate<String, Object> with string serializers for keys and hash keys and JSON serializers for values and hash values
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory cf) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(cf);

        template.setKeySerializer(RedisSerializer.string());
        template.setValueSerializer(RedisSerializer.json());

        template.setHashKeySerializer(RedisSerializer.string());
        template.setHashValueSerializer(RedisSerializer.json());

        template.afterPropertiesSet();
        return template;
    }
}
