package com.assemblyai.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.assemblyai.models.SentimentAudioEntity;
import com.assemblyai.models.TranscribeAudioEntity;
import com.assemblyai.models.UploadProperties;
import com.assemblyai.service.TranscribeAudioService;

import jakarta.servlet.annotation.MultipartConfig;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1")
@MultipartConfig(maxFileSize = 1024*1024*1024, maxRequestSize = 1024*1024*1024)
public class AudioSentimentController {

	@Value("${AssemblyAI.API_KEY}")
	private String API_KEY;

	@Autowired
	private UploadProperties uploadProperties;
	
	@Autowired
	private TranscribeAudioService transcribeAudioService;
	
	@Autowired
	private SentimentAudioEntity setAudioEntity;
	
	@Value("${upload.directory}")
	private String uploadDir;
	
	@Value("${target.directory}")
	private String targetDir;
	
	@PostMapping("/upload")
	public ResponseEntity<SentimentAudioEntity> uploadAudio(@RequestParam("file") MultipartFile file) throws Exception {
		if (file.isEmpty()) {
			log.info("Please Select a file to Upload");
			setAudioEntity.setStatusCode(HttpStatus.BAD_REQUEST);
			setAudioEntity.setMessage("Please select a file to upload");
			return ResponseEntity.badRequest().body(setAudioEntity);
		}

		try {
			String fileName = file.getOriginalFilename();
			assert fileName != null;
			
			File outputDir = new File(uploadDir);
			if (!outputDir.exists()) {
				outputDir.mkdirs();
			}

			String filePath = uploadDir + File.separator + fileName;
			File sourceFile = new File(filePath);
			file.transferTo(sourceFile);

			File target = new File(targetDir);
			File createFile = null;
			File uploadAudioFile =null;
			// Check if the uploaded file is in MP3 format
			if (!isMP3File(sourceFile)) 
			{
				if (!target.exists()) {
					target.mkdirs();
					createFile = new File(targetDir + "\\" + "audio.mp3");
					createFile.createNewFile();
				}
				transcribeAudioService.convertToMP3(sourceFile, createFile);
				String convertedFile = targetDir + File.separator + createFile.getName();
				uploadAudioFile = new File(convertedFile);
			}
			else
			{
				String convertedFile = uploadDir + File.separator + file.getOriginalFilename();
				uploadAudioFile = new File(convertedFile);	
			}
			
			String extension = transcribeAudioService.getFileExtension(uploadAudioFile);			
			if (!extension.equalsIgnoreCase(".mp3")) {
				setAudioEntity.setStatusCode(HttpStatus.BAD_REQUEST);
				setAudioEntity.setMessage("Please Select MP3 Extension Audio File.");
				sourceFile.delete();
				uploadAudioFile.delete();			
				target.delete();
				return ResponseEntity.badRequest().body(setAudioEntity);
			}
			Path uploadPath = Paths.get(uploadProperties.getDirectory()).toAbsolutePath().normalize();
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
			}
			
			setAudioEntity = transcribeAudioService.assemblyAITranscribeAudio(API_KEY, uploadAudioFile.toString());
			
			if(!setAudioEntity.getStatus().equals("completed")) {
				setAudioEntity.setStatusCode(HttpStatus.BAD_REQUEST);
				setAudioEntity.setMessage("Please Select Appropriate Audio File.");
				sourceFile.delete();
				uploadAudioFile.delete();			
				target.delete();
				return ResponseEntity.badRequest().body(setAudioEntity);
			}
			log.info("File Upload Successfully. fileName : " + fileName);
			sourceFile.delete();
			uploadAudioFile.delete();			
			target.delete();
			return ResponseEntity.ok().body(setAudioEntity);
		} catch (IOException ex) {
			setAudioEntity.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
			setAudioEntity.setMessage("Failed to upload file" + ex.getMessage());
			return ResponseEntity.badRequest().body(setAudioEntity);
		}
		 
	}
	
	@PostMapping("/url")
	public ResponseEntity<List<TranscribeAudioEntity>> getFromURL(@RequestParam("url") String audioUrl) throws Exception {
		log.info("start inside getFromURL in AudioSentimentController ");
		List<TranscribeAudioEntity> transScript = transcribeAudioService.getTranscribeAudiofromURL(API_KEY, audioUrl);
		return ResponseEntity.ok().body(transScript);
	}
	
	private boolean isMP3File(File file) {
        // Check if the file extension is ".mp3"
        return file.getName().toLowerCase().endsWith(".mp3");
    }
}
