package com.assemblyai;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EnableWebMvc
public class AssemblyAiApplication {

	@Value("${AssemblyAI.API_KEY}")
	private static String API_KEY;

	public static void main(String[] args) {
		SpringApplication.run(AssemblyAiApplication.class, args);
		// assemblyrun();
	}
	
	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**").allowedOrigins("http://innovation-573582008.us-east-1.elb.amazonaws.com:4200");
			}
		};
	}
	
	
	

	/*public static void assemblyrun() throws Exception {
		AssemblyAI client = AssemblyAI.builder().apiKey(API_KEY).build();

		String audioUrl = "https://fb-store.s3.ap-south-1.amazonaws.com/Datasets/Speech+datasets/Call+center+speech+datasets/Delivery+and+Logistics/Audio+File/Speech_data_in_English(India)_for_Delivery_and_Logistics_AI.wav";

		var params = TranscriptOptionalParams.builder().speakerLabels(true).sentimentAnalysis(true).build();

		Transcript transcript = client.transcripts().transcribe(audioUrl, params);
		SentimentAnalysisServiceImpl sentimentAnalysisServiceImpl = new SentimentAnalysisServiceImpl();
		StanfordCoreNLP pipeline = new StanfordCoreNLP("application.properties");

		transcript.getUtterances().ifPresent(utterances -> utterances.forEach(utterance -> {
			try {
				System.out.println("Speaker " + utterance.getSpeaker() + ": " + utterance.getText() + " "
						+ sentimentAnalysisServiceImpl.getSentiment(utterance.getText(), pipeline));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}));
	}*/

}
