package com.endside.file.manage.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.ByteArrayResource;

@Getter
@Setter
public class FileBucket {
    private ByteArrayResource resource;
    private long contentLength;
    private String contentType;
}
