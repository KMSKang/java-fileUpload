package com.fileupload.www.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;

@EntityListeners(AuditingEntityListener.class) // createdAt, modifiedAt
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Entity
public class FileUpload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String fileName; // 파일명

    @Column
    private String path; // 파일 경로

    @Column
    private long length; // 파일 크기

    @Column
    private String extension; // 확장자

    @Column(updatable = false)
    @CreatedDate
    private LocalDate createdAt;

    @LastModifiedDate
    private LocalDate modifiedAt;

    public FileUpload update(String path, String fileName, String extension, long length) {
        this.path = path;
        this.fileName = fileName;
        this.extension = extension;
        this.length = length;
        return this;
    }

    public FileUpload(Long id, String fileName, String path, long length, String extension) {
        this.id = id;
        this.fileName = fileName;
        this.path = path;
        this.length = length;
        this.extension = extension;
    }

}
