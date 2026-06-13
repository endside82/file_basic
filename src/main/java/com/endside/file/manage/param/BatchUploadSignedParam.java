package com.endside.file.manage.param;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class BatchUploadSignedParam {
    private List<UploadSignedParam> files;
}
