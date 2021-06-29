package com.fileupload.www.service;

import com.fileupload.www.dto.FileUploadDto;
import com.fileupload.www.entity.FileUpload;
import com.fileupload.www.repository.FileUploadRepository;
import lombok.RequiredArgsConstructor;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class FileUploadService {

    private final FileUploadRepository repository;

    @Value("${file.upload.location}")
    private String uploadPath; // src/main/resources/files/

    private static String folderPath = LocalDate.now().toString().replaceAll("-", "/"); // 2021/06/25

    /**
     * 업로드 파일에 있는 폴더 모두 삭제
     */
    @PostConstruct
    public void vacate() {
        File folder = new File(uploadPath);
        if(folder.exists()) { // 폴더가 존재하면
            try {
                FileUtils.cleanDirectory(folder);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 리스트
     */
    public List<FileUploadDto> getData() {
        List<FileUploadDto> result = new ArrayList<>();
        List<FileUpload> list = repository.findAll();
        for(FileUpload entity : list) {
            try {
                Path imgPath = Paths.get(entity.getPath() + File.separator, entity.getFileName()); // 파일이 어디 저장되어있는지를 확인하고, 그 경로를 Path 객체로 만듬
                byte[] imgBytes = Files.readAllBytes(imgPath); // Path 객체를 통해 File을 바이트로 읽어온다.
                FileUploadDto dto = new FileUploadDto();
                dto.setId(entity.getId());
                dto.setImage(imgBytes);
                dto.setImageContentType("image/jpeg");
                result.add(dto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 업로드 1 (File)
     */
    @Transactional
    public ResponseEntity uploadFile(MultipartFile multipartFile) {
        /**
         * 폴더 생성
         */
        File folderDate = new File(uploadPath, folderPath); // src/main/resources/files/ , 2021/06/25
        if(folderDate.exists()) { // 동일한 폴더명이 있으면
            System.out.println("이미 동일한 폴더명이 있음");
        } else { // 동일한 폴더명이 없으면
            folderDate.mkdirs(); // 폴더 생성
        }

        /**
         * 테이블 빈 데이터 생성 후 id값 폴더 생성
         */
        FileUpload entity = repository.save(new FileUpload()); // 테이블 빈 데이터 생성
        String id = entity.getId().toString(); // id(pk)

        String folderDatePath = folderDate.getPath(); // 날짜 폴더 경로
        File folderId = new File(folderDatePath, id);
        if(folderId.exists()) { // 동일한 폴더명이 있으면
            System.out.println("이미 동일한 폴더명이 있음");
        } else { // 동일한 폴더명이 없으면
            folderId.mkdir(); // 폴더 생성
        }

        /**
         * 파일 저장
         */
        FileUploadDto dto = new FileUploadDto();
        if (folderDate.exists()) { // 폴더가 있으면
            try {
                String folderIdPath = folderId.getAbsolutePath(); // pk id 폴더 절대경로
                File file = new File(folderIdPath, multipartFile.getOriginalFilename()); // 업로드할 파일명
                multipartFile.transferTo(file); // 파일 생성

                String filePath = folderId.getPath(); // pk id 폴더 상대경로
                String fileName = file.getName(); // 파일명
                String extension = fileName.substring(fileName.lastIndexOf(".")+1); // 확장자
                long length = file.length(); // 파일 크기(바이트)
                entity.update(filePath, fileName, extension, length);

                Path imgPath = Paths.get(folderIdPath, multipartFile.getOriginalFilename()); // 파일이 어디 저장되어있는지를 확인하고, 그 경로를 Path 객체로 만듬
                byte[] imgBytes = Files.readAllBytes(imgPath); // Path 객체를 통해 File을 바이트로 읽어온다.
                dto.setId(entity.getId());
                dto.setImage(imgBytes);
                dto.setImageContentType("image/jpeg");
                dto.setResult(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok().body(dto);
    }

    /**
     * 업로드 2 (Path)
     */
    @Transactional
    public ResponseEntity uploadFile2(MultipartFile multipartFile) {
        /**
         * 폴더 생성
         */
        Path folderDate = Paths.get(uploadPath, folderPath); // src/main/resources/files/ , 2021/06/25
        if (Files.exists(folderDate)) { // 동일한 폴더명이 있으면
            System.out.println("이미 동일한 폴더명이 있음");
        } else { // 동일한 폴더명이 없으면
            new File(folderDate.toAbsolutePath().toString()).mkdirs(); // 폴더 생성
        }

        /**
         * 테이블 빈 데이터 생성 후 id값 폴더 생성
         */
        FileUpload entity = repository.save(new FileUpload()); // 테이블 빈 데이터 생성
        String id = entity.getId().toString(); // id(pk)

        String folderDatePath = folderDate.toAbsolutePath().toString(); // 날짜 폴더 경로
        Path folderId = Paths.get(folderDatePath, id);
        if(Files.exists(folderId)) { // 동일한 폴더명이 있으면
            System.out.println("이미 동일한 폴더명이 있음");
        } else { // 동일한 폴더명이 없으면
            new File(folderId.toAbsolutePath().toString()).mkdir(); // 폴더 생성
        }
        /**
         * 파일 저장
         */
        FileUploadDto dto = new FileUploadDto();
        if (Files.exists(folderDate)) { // 폴더가 있으면
            try {
                String folderIdPath = folderId.toAbsolutePath().toString(); // pk id 폴더 절대경로
                Path file = Paths.get(folderIdPath, multipartFile.getOriginalFilename()); // 업로드할 파일명
                multipartFile.transferTo(file); // 파일 생성

                String filePath = folderIdPath.substring(folderIdPath.indexOf("src")); // pk id 폴더 상대경로
                String fileName = file.getFileName().toString(); // 파일명
                String extension = fileName.substring(fileName.lastIndexOf(".")+1); // 확장자
                long length = file.toFile().length(); // 파일 크기(바이트)
                entity.update(filePath, fileName, extension, length);

                Path imgPath = Paths.get(folderIdPath, multipartFile.getOriginalFilename()); // 파일이 어디 저장되어있는지를 확인하고, 그 경로를 Path 객체로 만듬
                byte[] imgBytes = Files.readAllBytes(imgPath); // Path 객체를 통해 File을 바이트로 읽어온다.
                dto.setId(entity.getId());
                dto.setImage(imgBytes);
                dto.setImageContentType("image/jpeg");
                dto.setResult(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseEntity.ok().body(dto);
    }

    /**
     * 다운로드
     */
    public void download(Long id, HttpServletResponse response) {
        try {
            FileUpload entity = repository.findById(id).orElse(null);
            String fileName = entity.getFileName();
            String folderPath = entity.getPath() + File.separator;
            String filePath = folderPath + fileName;
            File file = new File(filePath);

            if(file.exists()) { // 파일이 있으면
                response.setHeader("Content-Disposition", "attachment; filename=" + entity.getFileName()); // 응답 본문을 브라우저가 어떻게 표시해야 할지 알려주는 헤더입니다. inline인 경우 웹페이지 화면에 표시되고, attachment인 경우 다운로드됩니다.
                response.setHeader("Content-Transfer-Encoding", "binary"); // 전송 데이타의 body를 인코딩한 방법[인코딩 방식]을 표시함

                OutputStream out = response.getOutputStream(); // OutputStream을 상속한 자식 클래스로 파일로 데이터를 출력하는 기능을 수행한다.
                FileInputStream fis = new FileInputStream(filePath); // InputStream을 상속하여 구현한 자식 클래스로 하드 디스크 상에 존재하는 파일로부터 바이트 단위의 입력을 처리하는 클래스
                FileCopyUtils.copy(fis, out); // 스프링 프레임워크에 내장된 파일 다운로드 기능을 사용하는 것인데, 내부 소스를 열어보면 스트림을 열어서 while문으로 카피해주고 flush도 해주고 close까지 싹 다 해줌
                if(fis != null) {
                    fis.close();
                    out.flush();
                    out.close();
                }
            } else { // 파일이 없으면?
                System.out.println("파일 없음");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 삭제
     */
    @Transactional
    public ResponseEntity remove(Long id) {
        FileUpload entity = repository.findById(id).orElse(null);
        String folderPath = entity.getPath(); // 폴더 경로
        File folder = new File(folderPath);
        if(folder.exists()) { // 폴더가 존재하면
            File[] files = folder.listFiles(); // 폴더 안의 파일들
            for(File file: files) {
                file.delete(); // 파일 한개씩 삭제
            }
            folder.delete(); // 폴더 삭제
        }
        return new ResponseEntity("success", HttpStatus.OK);
    }
}

/**
 * 파일 정보
 */
//        File absoluteFile = upload.getAbsoluteFile(); // 파일의 절대 경로
//        String absolutePath = upload.getAbsolutePath(); // 파일의 절대 경로
//        long length = upload.length(); // 파일 크기
//        long l = upload.lastModified();// 마지막 수정일
//        String name = upload.getName(); // 파일명
//        String parent = upload.getParent(); // 부모 경로에 대한 경로
//        File parentFile = upload.getParentFile(); // 부모 경로에 대한 경로
//        String path = upload.getPath(); // 파일의 경로
//        long totalSpace = upload.getTotalSpace(); // 하드디스크의 총 용량
//        long usableSpace = upload.getUsableSpace(); // 하드디스크의 사용 가능한 용량
//        long freeSpace = upload.getFreeSpace(); // 하드디스크의 남은 공간
//        int i = upload.hashCode(); // 해시 코드
//        Path path1 = upload.toPath(); // java.nio.file.Path
//        URI uri = upload.toURI(); // URI 형태로 파일 경로
//        String[] list = upload.list(); // 경로의 파일들과 폴더를 문자열 배열로 반환한다.
//        File[] files = upload.listFiles(); // 해당 경로의 파일들과 폴더의 파일을 배열로 반환한다.
//        boolean delete = upload.delete(); // 파일이나 폴더를 삭제한다. 단 폴더가 비어있지 않ㄴ으면 삭제할 수 없다.
//        upload.deleteOnExit(); // 자바가상머신이 끝날 때 파일을 삭제한다.
//        boolean mkdir = upload.mkdir(); // 해당 경로에 폴더를 만든다.
//        boolean mkdirs = upload.mkdirs(); // 존재하지 않는 부모 폴더까지 포함하여 해당 경로에 폴더를 만든다
//        upload.renameTo(File);
//        boolean exists = upload.exists(); // 파일 존재 여부
//        boolean absolute = upload.isAbsolute(); // 해당 경로가 절대경로인지 여부를 리턴한다.
//        boolean directory = upload.isDirectory();// 해당 경로가 폴더인지 여부를 리턴한다.
//        boolean file1 = upload.isFile();// 해당 경로가 일반 file 인지 여부를 리턴한다.
//        boolean hidden = upload.isHidden();// 해당 경로가 숨긴 file 인지 여부를 리턴한다.
//        boolean b = upload.canExecute();// 파일을 실행할 수 있는지 여부를 리턴한다.
//        boolean b1 = upload.canRead();// 파일을 읽을 수 있는지 여부를 리턴한다.
//        boolean b2 = upload.canWrite();// 파일을 쓸 수 있는지 여부를 리턴한다.
//        boolean b3 = upload.setExecutable(true); // 파일 소유자의 실행 권한을 설정한다.
//        boolean b4 = upload.setExecutable(true, true); // 파일 소유자의 실행 권한을 설정한다.
//        boolean b5 = upload.setReadable(true); // 파일 소유자의 읽기 권한을 설정한다.
//        boolean b6 = upload.setReadable(true, true); // 파일의 실행 권한을 소유자 또는 모두에 대해 설정한다.
//        boolean b7 = upload.setReadOnly();// 파일을 읽기 전용으로 변경한다
//        boolean b8 = upload.setWritable(true);// 파일의 소유자의 쓰기 권한을 설정한다.
//        boolean b9 = upload.setWritable(true, true);// 파일의 쓰기 권한을 소유자 또는 모두에 대해 설정한다.
//
//        try {
//            File canonicalFile = upload.getCanonicalFile();
//            String canonicalPath = upload.getCanonicalPath();
//            boolean newFile = upload.createNewFile();// 주어진 이름의 파일이 없으면 새로 생성한다.
//        } catch (Exception e) {
//            e.printStackTrace();
//        }