package com.endside.file.config.ssh;

import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceLoader;
import org.apache.sshd.common.util.net.SshdSocketAddress;
import org.apache.sshd.common.util.security.SecurityUtils;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class SshTunneling {

    private SshClient client;
    private ClientSession session;
    private static final long CONNECT_TIMEOUT_MILLIS = 30000L;

    public boolean init() {
        try {
            // SshClient 초기화 및 시작
            client = SshClient.setUpDefaultClient();
            client.start();

            // SSH 서버 연결
            session = client.connect(
                SshConfig.SSH_USER,
                SshConfig.SSH_SERVER,
                SshConfig.SSH_PORT
            ).verify(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS).getSession();

            // 키 파일 로드
            KeyPairResourceLoader loader = SecurityUtils.getKeyPairResourceParser();
            Collection<KeyPair> keyPairs = loader.loadKeyPairs(
                null,
                Paths.get(SshConfig.KEY_PATH),
                FilePasswordProvider.EMPTY
            );

            // 키 기반 인증 설정
            for (KeyPair keyPair : keyPairs) {
                session.addPublicKeyIdentity(keyPair);
            }

            // StrictHostKeyChecking 비활성화 (기존 동작 유지)
            session.setServerKeyVerifier((clientSession, remoteAddress, serverKey) -> true);

            // 인증 수행
            session.auth().verify(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

            // 로컬 포트 포워딩 설정
            session.startLocalPortForwarding(
                new SshdSocketAddress(SshConfig.REDIS_L_ADDR, SshConfig.REDIS_L_PORT),
                new SshdSocketAddress(SshConfig.REDIS_R_ADDR, SshConfig.REDIS_R_PORT)
            );

            log.info("SSH tunnel initialized successfully: {}:{} -> {}:{}",
                SshConfig.REDIS_L_ADDR, SshConfig.REDIS_L_PORT,
                SshConfig.REDIS_R_ADDR, SshConfig.REDIS_R_PORT);

            return true;
        } catch (IOException | GeneralSecurityException e) {
            log.error("SSH tunnel initialization failed", e);
            shutdown(); // 실패 시 리소스 정리
            return false;
        }
    }

    public void shutdown() {
        try {
            if (session != null && session.isOpen()) {
                session.close(false);
            }
            if (client != null && client.isOpen()) {
                client.stop();
            }
            log.info("SSH tunnel shut down successfully");
        } catch (Exception e) {
            log.error("Error during SSH tunnel shutdown", e);
        }
    }
}
