package com.endside.file.config.ssh;

import java.util.Properties;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SshTunneling {

    private Session session;

    public boolean init() {
        JSch jsch = new JSch();
        try {
            jsch.addIdentity(SshConfig.KEY_PATH);
            session = jsch.getSession(SshConfig.SSH_USER, SshConfig.SSH_SERVER, SshConfig.SSH_PORT);
            // session.setPassword(password);
            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);
            session.connect();
            session.setPortForwardingL(SshConfig.REDIS_L_ADDR, SshConfig.REDIS_L_PORT, SshConfig.REDIS_R_ADDR, SshConfig.REDIS_R_PORT);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void shutdown() throws Exception {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }
}

