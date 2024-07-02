package com.endside.file.config.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.exceptions.*;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.endside.file.config.error.ErrorCode;
import com.endside.file.config.error.ResponseConstants;
import com.endside.file.config.security.constants.JwtProperties;
import com.endside.file.user.constants.LoginType;
import com.endside.file.user.model.LoginAddInfo;
import com.endside.file.user.service.JwtAuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static com.auth0.jwt.algorithms.Algorithm.HMAC512;

/**
 * JWT 인증을 위한 필터
 */
@Slf4j
public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private final JwtAuthenticationService jwtAuthenticationService;
    private final ArrayList<String> excludeURL; // jwt를 넣더라도 check 하지 않는 API URL 리스트
    private final JWTVerifier jwtVerifier;


    public JwtAuthorizationFilter(AuthenticationManager authenticationManager, JwtAuthenticationService jwtAuthenticationService, String secret) {
        super(authenticationManager);
        this.jwtAuthenticationService = jwtAuthenticationService;
        excludeURL = new ArrayList<>();
        // excludeURL.add("/some/exclude/url");
        // strict mode JWT Verifier
        jwtVerifier = JWT.require(HMAC512(secret.getBytes())).build();
    }

    private boolean isContainExcludeUrl (String requestUrl) {
        return excludeURL.contains(requestUrl);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        // 권한 헤더(JWT) 취득
        String header = request.getHeader(JwtProperties.HEADER_AUTH);
        String requestUrl = request.getRequestURI();
        // JWT 없거나 JWT 검사할 필요가 없으면 스킵
        if (header == null || isContainExcludeUrl(requestUrl)) {
            chain.doFilter(request, response);
            return;
        }

        // 유저 정보 취득
        Authentication authentication = getUsernamePasswordAuthentication(request, response);
        if(authentication == null) {
            return;
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }

    private Authentication getUsernamePasswordAuthentication(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String token = request.getHeader(JwtProperties.HEADER_AUTH);

        // parse the token and validate it
        DecodedJWT decodedJWT;
        try {
            decodedJWT = jwtVerifier.verify(token); // verification
        } catch (TokenExpiredException e) {
            outErrorMessage(response, ErrorCode.JWT_TOKEN_EXPIRATION.getStatus(), ErrorCode.JWT_TOKEN_EXPIRATION.getCode(), ErrorCode.JWT_TOKEN_EXPIRATION.getMessage());
            return null;
        } catch (SignatureVerificationException | JWTDecodeException | InvalidClaimException e) {
            outErrorMessage(response,ErrorCode.INVALID_AUTH_TOKEN.getStatus(), ErrorCode.INVALID_AUTH_TOKEN.getCode(), ErrorCode.INVALID_AUTH_TOKEN.getMessage());
            return null;
        } catch (JWTVerificationException e) {
            // verification error 처리
            // 필터에서 생성된 에러의 경우 controller advice 에서 핸들링 할 수 없다.
            outErrorMessage(response,ErrorCode.JWT_TOKEN_AUTH_ERROR.getStatus(), ErrorCode.JWT_TOKEN_AUTH_ERROR.getCode(), ErrorCode.JWT_TOKEN_AUTH_ERROR.getMessage());
            return null;
        }
        String converted  = decodedJWT.getSubject();
        String issueNo  = decodedJWT.getId();
        String loginType = decodedJWT.getClaim(JwtProperties.CLAIM_LOGIN_TYPE).asString();
        log.debug("param issueNo  : " + issueNo);
        // 토큰 발행 번호로 블랙리스트 조회
        ErrorCode errorCode = jwtAuthenticationService.checkBlackListToken(issueNo);
        if (errorCode != null) {
            outErrorMessage(response, errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
            return null;
        }

        String subject = new String(DatatypeConverter.parseHexBinary(converted));
        String[] subArray = subject.split(JwtProperties.SPLITTER);

        if (subArray[0] != null) {
            long userId = Long.parseLong(subArray[1]);
            String email = subArray[0];
            // 유저 user_Id로 블랙리스트에 포함 되어 있는지 확인한다.
            errorCode = jwtAuthenticationService.checkBlackListUser(userId);
            if (errorCode != null) {
                outErrorMessage(response, errorCode.getStatus(), errorCode.getCode(), errorCode.getMessage());
            }
            LoginAddInfo loginAddInfo = new LoginAddInfo();
            loginAddInfo.setLoginType(LoginType.getLoginTypeAsType(loginType));
            UserPrincipal principal = UserPrincipal.builder()
                    .userId(userId)
                    .userHex(converted)
                    .loginAddInfo(loginAddInfo)
                    .email(email)
                    .build();
            return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        }
        return null;
    }

    private void outErrorMessage(HttpServletResponse response, int statusCode, int errorCode, String errorMessage) throws IOException {
        // verification error 처리
        // 필터에서 생성된 에러의 경우 controller advice 에서 핸들링 할 수 없다.
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        HashMap<String,Object> resultMap = new HashMap<>();
        resultMap.put(ResponseConstants.ERROR_CODE , errorCode);
        resultMap.put(ResponseConstants.ERROR_MESSAGE , errorMessage);
        resultMap.put(ResponseConstants.ERROR_TIMESTAMP , ResponseConstants.DATE_FORMAT.format(new Date()));
        ObjectMapper mapper = new ObjectMapper();
        PrintWriter out = response.getWriter();
        out.print(mapper.writeValueAsString(resultMap));
        out.flush();
    }
}
