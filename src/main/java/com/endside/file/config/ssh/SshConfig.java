package com.endside.file.config.ssh;

public class SshConfig {
    public static final String KEY_PATH = "keys/dev-an2-key.pem"; // LOCAL pem key path
    public static final String SSH_SERVER = "ec2-{254-254-254-254}.{AWS-REGION-NAME}.compute.amazonaws.com"; // ssh server address. access to db via this server
    public static final int SSH_PORT = 22; // ssh server port
    public static final String SSH_USER = "ec2-user";
    // redis
    public static final int REDIS_L_PORT = 6379;
    public static final String REDIS_L_ADDR = "127.0.0.1";
    public static final int REDIS_R_PORT = 6379;
    public static final String REDIS_R_ADDR = "{REDIS_ADDRESS}";
}
