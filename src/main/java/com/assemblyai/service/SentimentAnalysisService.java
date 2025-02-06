package com.assemblyai.service;

import org.springframework.stereotype.Service;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;

@Service
public interface SentimentAnalysisService {
	public  String analyzeSentiment(String text) throws Exception;
	public String getSentiment(String text, StanfordCoreNLP pipeline) throws Exception;
}
