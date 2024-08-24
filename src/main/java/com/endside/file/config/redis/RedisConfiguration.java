package com.endside.file.config.redis;

import com.endside.file.config.ssh.SshTunneling;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.Collections;
import java.util.List;


/**
 * Redis Connection 및 캐시 관리 설정
 */
@Slf4j
@EnableCaching(proxyTargetClass = true)
@Configuration
public class RedisConfiguration {
    @Value("${redis.host:127.0.0.1}")
    private String host;
    @Value("${redis.port:6379}")
    private int port;
    @Value("${redis.authentication.database:4}")
    private int authDatabase;
    @Value("${redis.account.database:0}")
    private int userDatabase;
    @Value("${redis.password:password}")
    private String password;
    @Value("${redis.auth:true}")
    private boolean redisAuth;
    @Value("${redis.type:STANDARD}")
    private String redisType;

    @Value("${ssh.use:false}")
    private Boolean isUse = false;

    private final SshTunneling tunnel;

    private void sshTunnelingInit(){
        if (!tunnel.init()){
            System.exit(0);
        }
    }

    public RedisConfiguration(SshTunneling tunnel) {
        this.tunnel = tunnel;
    }

    @PreDestroy
    public void end() {
        try {
            if(tunnel != null) {
                tunnel.shutdown();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Primary
    @Qualifier("authRedisConnectionFactory")
    @Bean(name="authRedisConnectionFactory")
    LettuceConnectionFactory authRedisConnectionFactory() {
        if(isUse) {
            sshTunnelingInit();
        }
        return getLettuceConnectionFactory(authDatabase);
    }

    @Qualifier("userRedisConnectionFactory")
    @Bean(name="userRedisConnectionFactory")
    public RedisConnectionFactory userRedisConnectionFactory() {
        return getLettuceConnectionFactory(userDatabase);
    }


    private LettuceConnectionFactory getLettuceConnectionFactory(int DBnum) {
        if (redisType.compareTo("CLUSTER") == 0) {
            // clustering 구성 config
            RedisClusterConfiguration redisClusterConfiguration = getRedisClusterConf();
            LettuceConnectionFactory redisConnectionFactory = new LettuceConnectionFactory(redisClusterConfiguration);
            redisConnectionFactory.setDatabase(DBnum);
            return redisConnectionFactory;
        }
        // else Redis is standard
        RedisStandaloneConfiguration redisStandaloneConfiguration = getRedisStandardConf(DBnum);
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    @Qualifier("authRedisTemplate")
    @Bean(name="authRedisTemplate")
    public RedisTemplate<String, Object> authRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(authRedisConnectionFactory());
        return redisTemplate;
    }

    @Qualifier("userRedisTemplate")
    @Bean(name="userRedisTemplate")
    public RedisTemplate<String, String> userRedisTemplate() {
        RedisTemplate<String, String> redisTemplate = new RedisTemplate<>();
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(userRedisConnectionFactory());
        return redisTemplate;
    }

    private RedisStandaloneConfiguration getRedisStandardConf(int dbNum) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(host, port);
        if (redisAuth) {
            config.setPassword(password);
        }
        config.setDatabase(dbNum);
        return config;
    }

    private RedisClusterConfiguration getRedisClusterConf() {
        List<String> nodes = Collections.singletonList(host + ":" + port);
        return new RedisClusterConfiguration(nodes);
    }


    @Bean
    public CacheManager cacheManager() {
        RedisCacheManager.RedisCacheManagerBuilder builder = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(authRedisConnectionFactory());
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .prefixCacheNameWith("cache:")
                .entryTtl(Duration.ofHours(24L));
        builder.cacheDefaults(configuration);
        return builder.build();
    }




}
