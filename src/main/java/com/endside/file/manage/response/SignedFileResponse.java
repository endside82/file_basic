package com.endside.file.manage.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SignedFileResponse {
    private String url;

    public SignedFileResponse(String url){
        this.url = url;
    }
}
