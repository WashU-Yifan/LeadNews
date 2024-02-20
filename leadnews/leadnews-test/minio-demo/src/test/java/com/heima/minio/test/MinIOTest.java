package com.heima.minio.test;

import com.heima.file.service.FileStorageService;
import com.heima.minio.MinIOApplication;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@SpringBootTest(classes = MinIOApplication.class)
@RunWith(SpringRunner.class)
public class MinIOTest {

    @Autowired
    private FileStorageService fileStorageService;

    private static final String FILE_PATH = "/Users/wangyifan/JavaProjects/LeadNews/heima-leadnews/leadnews-test/minio-demo/src/main/resources/list.html";
    @Test
    public void test() throws FileNotFoundException {
        FileInputStream fileInputStream = new FileInputStream(FILE_PATH);
        String path = fileStorageService.uploadHtmlFile("", "list.html", fileInputStream);
        System.out.println(path);
    }
//    public static void main(String[] args) {
//        try {
//
//            FileInputStream fileInputStream = new FileInputStream("/Users/wangyifan/JavaProjects/LeadNews/heima-leadnews/heima-leadnews-test/minio-demo/src/main/resources/list.html");
//            MinioClient minioClient = MinioClient.builder().credentials("minio", "minio123").endpoint("http://127.0.0.1:9000").build();
//
//            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
//                    .object("list.html")
//                    .contentType("test/html")
//                    .bucket("leadnews")
//                    .stream(fileInputStream, fileInputStream.available(), -1).build();
//
//            minioClient.putObject(putObjectArgs);
//            System.out.println("http://127.0.0.1:9000/leadnews/list.html");
//        }
//        catch (FileNotFoundException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
