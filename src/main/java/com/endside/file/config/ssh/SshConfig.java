package com.endside.file.config.ssh;

/**
 * SSH 터널링 설정
 * 주의: 실제 배포 시에는 환경 변수를 통해 값을 설정하세요.
 * 예: SSH_KEY_PATH, SSH_SERVER, SSH_USER, REDIS_ADDRESS
 */
public class SshConfig {
    // SSH 접속 설정 - 환경 변수로 관리
    public static final String KEY_PATH = System.getenv().getOrDefault("SSH_KEY_PATH", "keys/your-key.pem");
    public static final String SSH_SERVER = System.getenv().getOrDefault("SSH_SERVER", "your-ssh-server.compute.amazonaws.com");
    public static final int SSH_PORT = 22;
    public static final String SSH_USER = System.getenv().getOrDefault("SSH_USER", "ec2-user");

    // Redis 포트 포워딩 설정
    public static final int REDIS_L_PORT = 6379;
    public static final String REDIS_L_ADDR = "127.0.0.1";
    public static final int REDIS_R_PORT = 6379;
    public static final String REDIS_R_ADDR = System.getenv().getOrDefault("REDIS_ADDRESS", "your-redis.cache.amazonaws.com");
}
