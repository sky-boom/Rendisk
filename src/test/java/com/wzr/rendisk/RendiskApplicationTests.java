package com.wzr.rendisk;

import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@SpringBootTest
@Slf4j
class RendiskApplicationTests {

	@Test
	public void testUpload() {
		try {
			ClientGlobal.initByProperties("fastdfs-client.properties");
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient1 client = new StorageClient1(trackerServer, storageServer);
			//添加基本的元数据，添加name,date，length
			NameValuePair nvp[] = null;
			//上传到文件系统
			long startTime = System.currentTimeMillis();
			String fileId = client.upload_file1("D:\\BaiduNetdiskDownload\\win10 ra2yuri.7z",
					"bin", nvp);
			long endTime = System.currentTimeMillis();
			log.info("fileid={}",fileId);
			log.info("文件已上传，总耗时: {}s", (endTime - startTime)/1000.0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDownload() {
		try {
			ClientGlobal.initByProperties("fastdfs-client.properties");
			TrackerClient tracker = new TrackerClient();
			TrackerServer trackerServer = tracker.getConnection();
			StorageServer storageServer = null;
			StorageClient1 client = new StorageClient1(trackerServer, storageServer);
			//添加基本的元数据，添加name,date，length
			NameValuePair nvp[] = null;
			//上传到文件系统
			long startTime = System.currentTimeMillis();
			byte[] file = client.download_file1("group1/M00/00/00/wKgMhWSFz2CABCfYEH3HFaZtdqU059.bin");
			log.info(String.valueOf(file.length));
			long endTime = System.currentTimeMillis();
			log.info("文件已下载，总耗时: {}s", (endTime - startTime)/1000.0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpload2() {
		try {
			MinioClient minioClient = MinioClient.builder()
					.endpoint("http://192.168.12.133:9000")
					.credentials("minioadmin", "minioadmin")
					.build();
			//上传到文件系统
			File file = new File("D:\\BaiduNetdiskDownload\\win10 ra2yuri.7z");
			try {
				InputStream inputStream = new FileInputStream(file);
				// 使用 inputStream 进行文件操作，如读取、写入等
				long startTime = System.currentTimeMillis();
				minioClient.putObject(
						PutObjectArgs.builder()
								.bucket("test")
								.object("win10 ra2yuri.7z")
								.contentType("application/octet-stream")
								.stream(inputStream, inputStream.available(), -1)
								.build());
				long endTime = System.currentTimeMillis();
				log.info("文件已上传，总耗时: {}s", (endTime - startTime)/1000.0);
				inputStream.close(); // 记得关闭输入流
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDownload2() {
		try {
			MinioClient minioClient = MinioClient.builder()
					.endpoint("http://192.168.12.133:9000")
					.credentials("minioadmin", "minioadmin")
					.build();
			//上传到文件系统
			try {
				// 使用 inputStream 进行文件操作，如读取、写入等
				long startTime = System.currentTimeMillis();
				InputStream inputStream1 = minioClient.getObject(GetObjectArgs.builder().bucket("test").object("win10 ra2yuri.7z").build());
				long endTime = System.currentTimeMillis();
				log.info("文件长度：{}", inputStream1.available());
				log.info("文件已下载，总耗时: {}s", (endTime - startTime)/1000.0);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
