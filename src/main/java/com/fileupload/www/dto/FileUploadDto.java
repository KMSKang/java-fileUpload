package com.fileupload.www.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FileUploadDto {

    private Long id;

    private byte[] image;

    private String imageContentType;

    private boolean result;

}
