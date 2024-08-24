package com.endside.file.user.service;

import com.endside.file.config.db.redis.RedisUserRepositoryImpl;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.user.constants.BlackStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

// 로그인
@Slf4j
@Service
public class JwtAuthenticationService {


    private final RedisUserRepositoryImpl redisUserRepository;


    public JwtAuthenticationService( RedisUserRepositoryImpl redisUserRepository) {
        this.redisUserRepository = redisUserRepository;
    }

    // 토큰 블랙 리스트
    public ErrorCode checkBlackListToken(String issueNo) {
        List<String> reasons = redisUserRepository.getBlackListToken(issueNo);
        if (reasons == null || reasons.isEmpty()) {
            return null;
        }
        return checkTokenBlacklistType(reasons);
    }

    // 블랙리스트 유저인지 확인
    public ErrorCode checkBlackListUser(long userId) {
        List<String> reasons = redisUserRepository.getBlackListUser(userId);
        if (reasons == null || reasons.isEmpty()) {
            return null;
        }
        return checkUserBlacklistType(reasons);
    }

    // 블랙리스트 상태값 조회 우선 순위 고려
    private ErrorCode checkTokenBlacklistType(List<String> reasons) {
        if (hasStringInList(reasons, BlackStatus.LOGOUT.getStatus())) {
            // 유저 블랙리스트 상태값 조회 우선 순위 고려
            return ErrorCode.LOGIN_FAILURE_USER_STATUS_LOGOUT;
        }
        return null;
    }

    private ErrorCode checkUserBlacklistType(List<String> reasons) {
        if (hasStringInList(reasons, BlackStatus.EXIT.getStatus())) {
            return ErrorCode.LOGIN_FAILURE_USER_STATUS_EXIT;
        }


        if (hasStringInList(reasons, BlackStatus.BAN.getStatus())) {
            return ErrorCode.LOGIN_FAILURE_USER_STATUS_BAN;
        }
        // note 상기 두개 케이스 외에 발생하는 케이스 없음 (정의 된것은 있으나 케이스 없음)
        return null;
    }

    //
    private boolean hasStringInList(List<String> stringList, String status) {
        return stringList.stream().anyMatch(status::equals);
    }



}

