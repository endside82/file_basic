package com.endside.file.manage.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileDeleteResponse {
    private String path;
    public FileDeleteResponse(String path){
        this.path = path;
    }
}
