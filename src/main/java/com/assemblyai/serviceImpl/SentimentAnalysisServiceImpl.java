package com.assemblyai.serviceImpl;

import java.util.Properties;

import org.springframework.stereotype.Service;

import com.assemblyai.service.SentimentAnalysisService;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SentimentAnalysisServiceImpl implements SentimentAnalysisService {

	public String analyzeSentiment(String text) throws Exception {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		props.setProperty("ssplit.eolonly", "true");
		props.setProperty("tokenize.whitespace", "true");
		
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);

		int overallSentiment = 0;
		int numSentences = 0;

		for (CoreMap sentence : annotation.get(CoreAnnotations.SentencesAnnotation.class)) {
			String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
			switch (sentiment) {
			case "Very negative":
				overallSentiment += 1;
				break;
			case "Negative":
				overallSentiment += 2;
				break;
			case "Neutral":
				overallSentiment += 3;
				break;
			case "Positive":
				overallSentiment += 4;
				break;
			case "Very positive":
				overallSentiment += 5;
				break;
			}
			numSentences++;
		}

		int averageSentiment = overallSentiment / numSentences;
		String sentimentLabel;
		switch (averageSentiment) {
		case 1:
			sentimentLabel = "Very negative";
			break;
		case 2:
			sentimentLabel = "Negative";
			break;
		case 3:
			sentimentLabel = "Neutral";
			break;
		case 4:
			sentimentLabel = "Positive";
			break;
		case 5:
			sentimentLabel = "Very positive";
			break;
		default:
			sentimentLabel = "Unknown";
		}

		return sentimentLabel;
	}

	public String getSentiment(String text, StanfordCoreNLP pipeline) throws Exception {
		// Create an Annotation object with the input text
		Annotation annotation = new Annotation(text);
		// Run all the NLP annotators on the text
		pipeline.annotate(annotation);
		// Extract the sentiment from the annotation
		CoreMap sentence = annotation.get(CoreAnnotations.SentencesAnnotation.class).get(0);
		String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
		log.info("Text: {}, Sentiment: {}", text, sentiment);
		return sentiment;
	}
		 
}
