package com.assemblyai.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.springframework.stereotype.Service;

import com.assemblyai.models.SentimentAudioEntity;
import com.assemblyai.models.TranscribeAudioEntity;

@Service
public interface TranscribeAudioService {
	public SentimentAudioEntity assemblyAITranscribeAudio(String apiKey, String filePath) throws Exception;

	public List<TranscribeAudioEntity> getTranscribeAudiofromURL(String apiKey, String audioUrl) throws Exception;
	public String transcribeAudiofromURL(String apiKey, String audioUrl) throws Exception;
	public String getFileExtension(File file);
	public void convertToMP3(File Souce, File target) throws IOException;
}
