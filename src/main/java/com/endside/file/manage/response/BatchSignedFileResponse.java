package com.endside.file.manage.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BatchSignedFileResponse {
    private List<SignedFileResponse> items;
}
