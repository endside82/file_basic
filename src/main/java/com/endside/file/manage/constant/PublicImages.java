package com.endside.file.manage.constant;

import org.springframework.stereotype.Component;


@Component
public class PublicImages {
    public boolean checkIsPublicResource(String path) {
        return (path.startsWith("images/resource/bg")
        || path.startsWith("images/resource/public")
        );
    }
}
