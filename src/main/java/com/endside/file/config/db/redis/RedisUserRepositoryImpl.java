package com.endside.file.config.db.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Repository
@Slf4j
public class RedisUserRepositoryImpl {
    private static final String KEY_BLACK_USER = "black:user:%d";
    private static final String KEY_BLACK_TOKEN = "black:token:%s";


    private final RedisTemplate<String, String> userRedisTemplate;

    private final SetOperations<String, String> setOperations;

    public RedisUserRepositoryImpl(@Qualifier("userRedisTemplate") RedisTemplate<String, String> userRedisTemplate) {
        this.userRedisTemplate = userRedisTemplate;
        this.setOperations = userRedisTemplate.opsForSet();
    }

    public boolean setBlackListUser(Long userId, String status) {
        return setValueByKey(String.format(RedisUserRepositoryImpl.KEY_BLACK_USER, userId),status);
    }
    public boolean setBlackListUserWithExpire(Long userId, String status, Date date) {
        return setValueByKeyWithExpire(String.format(RedisUserRepositoryImpl.KEY_BLACK_USER, userId),status, date);
    }


    public List<String> getBlackListUser(Long userId) {
        return getListValueByKey(String.format(RedisUserRepositoryImpl.KEY_BLACK_USER, userId));
    }

    public boolean deleteBlackListUser(Long userId) {
        String key = String.format(RedisUserRepositoryImpl.KEY_BLACK_USER, userId);
        return deleteValueByKey(key);
    }

    public boolean deleteBlackListToken(String issueNo) {
        String key = String.format(RedisUserRepositoryImpl.KEY_BLACK_TOKEN, issueNo);
        return deleteValueByKey(key);
    }

    public List<String> getBlackListToken(String issueNo) {
        return getListValueByKey(String.format(RedisUserRepositoryImpl.KEY_BLACK_TOKEN, issueNo));
    }

    public boolean setBlackListTokenWithExpire(String issueNo, String status, Date date) {
        return setValueByKeyWithExpire(String.format(RedisUserRepositoryImpl.KEY_BLACK_TOKEN, issueNo),status,date);
    }

    private boolean deleteValueByKey(String key) {
        try {
            return Boolean.TRUE.equals(userRedisTemplate.delete(key));
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }

    private List<String> getListValueByKey(String key) {
        try {
            return new ArrayList<>(Objects.requireNonNull(setOperations.members(key)));
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return null;
    }


    private boolean setValueByKey(String key, String value) {
        try {
            setOperations.add(key, value);
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }

    private boolean setValueByKeyWithExpire(String key, String value, Date date ) {
        try {
            setOperations.add(key, value);
            userRedisTemplate.expireAt(key,date);
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }


    private boolean setListValueByKey(String key, List<String> values) {
        try {
            values.forEach(value -> setOperations.add(key, value));
            return true;
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        return false;
    }


}
