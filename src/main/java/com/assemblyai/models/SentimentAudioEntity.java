package com.assemblyai.models;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude;

@Service
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SentimentAudioEntity {

	private String id;
	private String language_model;
	private String language_code;
	private String status;
	private String audio_url;
	private String text;
	private Double confidence;
	private boolean format_text;
	private boolean sentiment_analysis;
	private boolean speaker_labels;
	private String summarization;
	private String summary_type;
	private String summary_model;
	private String summary;
	private int sentenceCount;
	private String sentimentAnalyze;
	private HttpStatus statusCode;
	private String message;
	private int neutralCount;
	private int positiveCount;
	private int negativeCount;

	private List<TranscribeAudioEntity> sentiment_analysis_results;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getLanguage_model() {
		return language_model;
	}

	public void setLanguage_model(String language_model) {
		this.language_model = language_model;
	}

	public String getLanguage_code() {
		return language_code;
	}

	public void setLanguage_code(String language_code) {
		this.language_code = language_code;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getAudio_url() {
		return audio_url;
	}

	public void setAudio_url(String audio_url) {
		this.audio_url = audio_url;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}

	public boolean isFormat_text() {
		return format_text;
	}

	public void setFormat_text(boolean format_text) {
		this.format_text = format_text;
	}

	public boolean isSentiment_analysis() {
		return sentiment_analysis;
	}

	public void setSentiment_analysis(boolean sentiment_analysis) {
		this.sentiment_analysis = sentiment_analysis;
	}

	public boolean isSpeaker_labels() {
		return speaker_labels;
	}

	public void setSpeaker_labels(boolean speaker_labels) {
		this.speaker_labels = speaker_labels;
	}

	public String getSummarization() {
		return summarization;
	}

	public void setSummarization(String summarization) {
		this.summarization = summarization;
	}

	public String getSummary_type() {
		return summary_type;
	}

	public void setSummary_type(String summary_type) {
		this.summary_type = summary_type;
	}

	public String getSummary_model() {
		return summary_model;
	}

	public void setSummary_model(String summary_model) {
		this.summary_model = summary_model;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public int getSentenceCount() {
		return sentenceCount;
	}

	public void setSentenceCount(int sentenceCount) {
		this.sentenceCount = sentenceCount;
	}

	public String getSentimentAnalyze() {
		return sentimentAnalyze;
	}

	public void setSentimentAnalyze(String sentimentAnalyze) {
		this.sentimentAnalyze = sentimentAnalyze;
	}

	public List<TranscribeAudioEntity> getSentiment_analysis_results() {
		return sentiment_analysis_results;
	}

	public void setSentiment_analysis_results(List<TranscribeAudioEntity> sentiment_analysis_results) {
		this.sentiment_analysis_results = sentiment_analysis_results;
	}

	public HttpStatus getStatusCode() {
		return statusCode;
	}

	public void setStatusCode(HttpStatus statusCode) {
		this.statusCode = statusCode;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getNeutralCount() {
		return neutralCount;
	}

	public void setNeutralCount(int neutralCount) {
		this.neutralCount = neutralCount;
	}

	public int getPositiveCount() {
		return positiveCount;
	}

	public void setPositiveCount(int positiveCount) {
		this.positiveCount = positiveCount;
	}

	public int getNegativeCount() {
		return negativeCount;
	}

	public void setNegativeCount(int negativeCount) {
		this.negativeCount = negativeCount;
	}

}
