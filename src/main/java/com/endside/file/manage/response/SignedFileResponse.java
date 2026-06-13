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
    @JsonInclude(NON_NULL)
    private String originName;
}
