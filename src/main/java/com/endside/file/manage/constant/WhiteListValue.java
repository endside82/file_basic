package com.endside.file.manage.constant;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class WhiteListValue {
    public WhiteListValue() {
        String ext = "jpg,jpeg,gif,png,tif,bmp" // 사진
                + "," + "mp4,m4v,avi,asf,wmv,mkv,ts,mpg,mpeg,mov,flv,ogv" // 동영상
                + "," + "wav,flac,tta,tak,aac,wma,ogg,mp3,m4a" // 음성
                + "," + "doc,docx,hwp,txt,rtf,xml,pdf,wks,wps,xps,md,odf,csv,tsv,xls,xlsx,ppt,pptx,pages,key,numbers,tga,psd" // 문서
                + "," + "zip,gz,bz2,rar,7z,lzh,alz"; // 압축
        this.extensions = Arrays.asList(ext.split(","));
        this.fileMaxLimit = 200;
        this.mediaMaxLimit = 200;
    }
    private List<String> extensions;
    @JsonProperty("file_max_limit")
    private int fileMaxLimit;
    @JsonProperty("media_max_limit")
    private int mediaMaxLimit;
}
