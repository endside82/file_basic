package com.endside.file.manage.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignedFileResponse {
    private String url;
    @JsonInclude(NON_NULL)
    private String fileKey;

    /**
     * URL만으로 생성하는 생성자 (기존 코드 호환성 유지)
     */
    public SignedFileResponse(String url) {
        this.url = url;
        this.fileKey = null;
    }
}
