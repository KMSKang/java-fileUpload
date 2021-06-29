package com.fileupload.www.controller;

import com.fileupload.www.dto.FileUploadDto;
import com.fileupload.www.service.FileUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
public class FileUploadController {

    @Autowired
    private FileUploadService service;

    /**
     * 리스트
     */
    @GetMapping("/data")
    public List<FileUploadDto> data() {
        return service.getData();
    }

    /**
     * 업로드
     */
    @PostMapping("/upload")
    public ResponseEntity upload(MultipartFile uploadFile) {
        return service.uploadFile(uploadFile); // File
//        return service.uploadFile2(uploadFile); // Path
    }

    /**
     * 다운로드
     */
    @GetMapping("/download/{id}")
    public void download(@PathVariable("id") Long id, HttpServletResponse response) {
        service.download(id, response);
    }

    /**
     * 삭제
     */
    @PostMapping("/remove/{id}")
    public ResponseEntity remove(@PathVariable("id") Long id) {
        return service.remove(id);
    }

    /**
     * 폴더 전체 삭제
     */
    @PostMapping("/vacate")
    public void vacate() {
        service.vacate();
    }

}
