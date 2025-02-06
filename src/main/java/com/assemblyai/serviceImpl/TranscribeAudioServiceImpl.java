package com.assemblyai.serviceImpl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.assemblyai.api.AssemblyAI;
import com.assemblyai.api.resources.lemur.requests.LemurTaskParams;
import com.assemblyai.api.resources.transcripts.types.Transcript;
import com.assemblyai.api.resources.transcripts.types.TranscriptOptionalParams;
import com.assemblyai.api.resources.transcripts.types.TranscriptUtterance;
import com.assemblyai.models.SentimentAudioEntity;
import com.assemblyai.models.TranscribeAudioEntity;
import com.assemblyai.service.SentimentAnalysisService;
import com.assemblyai.service.TranscribeAudioService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;

import java.io.File;
import java.util.List;
import java.util.Properties;
@Slf4j
@Service
public class TranscribeAudioServiceImpl implements TranscribeAudioService {

	@Autowired
	private SentimentAnalysisService sentimentAnalysisService;
	@Value("${AssemblyAI.UPLOAD_API_ENDPOINT}")
	private String UPLOAD_ENDPOINT;

	@Value("${AssemblyAI.TRANSCRIPT_API_ENDPOINT}")
	private String TRANSCRIPT_ENDPOINT;

	@Value("${AssemblyAI.API_KEY}")
	private String API_KEY;

	@Autowired
	private SentimentAudioEntity setAudioEntity;

	public SentimentAudioEntity assemblyAITranscribeAudio(String apiKey, String filePath) throws Exception {
		log.info("Start inside assemblyAITranscribeAudio method in TranscribeAudioServiceImpl class.");

		String audioUrl = assemblyAIUploadAudio(apiKey, filePath);
		String transcriptId = getTranscriptionID(audioUrl, apiKey);

		/*
		 * String response = summarization(transcriptId,apiKey);
		 * log.info("Summary of response  : " + response);
		 */

		String responseBody = null, responseStatus = null;
		try {
			do {
				responseBody = getTranscriptionById(transcriptId, apiKey);
				JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
				responseStatus = jsonObject.get("status").getAsString();
				Thread.sleep(2000);
			} while ("processing".equals(responseStatus));

			setAudioEntity = new Gson().fromJson(responseBody, SentimentAudioEntity.class);
			if (!setAudioEntity.getStatus().equals("processing") && !setAudioEntity.getStatus().equals("error")) {
				setAudioEntity.setStatusCode(HttpStatus.OK);
				setAudioEntity.setMessage("successfully Audio file transcribed");
				if (!setAudioEntity.getSentiment_analysis_results().isEmpty() && setAudioEntity.getSentiment_analysis_results()!=null) {
					setAudioEntity.setSentenceCount(setAudioEntity.getSentiment_analysis_results().isEmpty() ? 0 : setAudioEntity.getSentiment_analysis_results().size());
					setAudioEntity.setSentimentAnalyze(sentimentAnalysisService.analyzeSentiment(setAudioEntity.getSummary()));

					Map<String, Integer> sentimentCount = getCountSentiments(setAudioEntity.getSentiment_analysis_results());

					for (Map.Entry<String, Integer> entry : sentimentCount.entrySet()) {
						switch (entry.getKey()) {
						case "NEUTRAL":
							setAudioEntity.setNeutralCount(entry.getValue());
							break;
						case "POSITIVE":
							setAudioEntity.setPositiveCount(entry.getValue());
							break;
						case "NEGATIVE":
							setAudioEntity.setNegativeCount(entry.getValue());
							break;
						default:
						}
					}
					
//					List<TranscribeAudioEntity> listTranscribe =  new ArrayList<>();
//					for(TranscribeAudioEntity transcribeAudioEntity : setAudioEntity.getSentiment_analysis_results())
//					{
//						transcribeAudioEntity.setVoiceTone(getTone(transcribeAudioEntity.getText()));
//						listTranscribe.add(transcribeAudioEntity);
//						
//					}
//					setAudioEntity.setSentiment_analysis_results(listTranscribe);
				}

			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
		log.info("End of assemblyAITranscribeAudio method in TranscribeAudioServiceImpl classe before return.");
		return setAudioEntity;
	}

	public List<TranscribeAudioEntity> getTranscribeAudiofromURL(String apiKey, String audioUrl) throws Exception {
		AssemblyAI client = AssemblyAI.builder().apiKey(apiKey).build();

		var params = TranscriptOptionalParams.builder().speakerLabels(true).sentimentAnalysis(true).build();

		Transcript transcript = client.transcripts().transcribe(audioUrl, params);
		List<TranscribeAudioEntity> transcribeAudio = null;

		Optional<List<TranscriptUtterance>> utterances = transcript.getUtterances();
		if (utterances.isPresent()) {
			transcribeAudio = new ArrayList<TranscribeAudioEntity>();
			List<TranscriptUtterance> utterancesList = utterances.get();
			TranscribeAudioEntity transcribeAudioEntity;
			for (TranscriptUtterance obj : utterancesList) {
				transcribeAudioEntity = new TranscribeAudioEntity();
				transcribeAudioEntity.setText(obj.getText());
				transcribeAudioEntity.setStart(obj.getStart());
				transcribeAudioEntity.setEnd(obj.getEnd());
				transcribeAudioEntity.setConfidence(obj.getConfidence());
				transcribeAudioEntity.setSpeaker(obj.getSpeaker());
				transcribeAudioEntity.setSentiment(sentimentAnalysisService.analyzeSentiment(obj.getText()));
				transcribeAudio.add(transcribeAudioEntity);
			}
		}
		return transcribeAudio;
	}

	public String transcribeAudiofromURL(String apiKey, String audioUrl) throws Exception {
		AssemblyAI client = AssemblyAI.builder().apiKey(apiKey).build();

		var params = TranscriptOptionalParams.builder().speakerLabels(true).sentimentAnalysis(true).build();

		Transcript transcript = client.transcripts().transcribe(audioUrl, params);
		StringBuilder transcriptBuilder = new StringBuilder();

		transcript.getUtterances().ifPresent(
				utterances -> utterances.forEach(utterance -> transcriptBuilder.append(utterance.getText())));

		return transcriptBuilder.toString();
	}

	private String assemblyAIUploadAudio(String apiKey, String filePath) throws Exception {
		log.info("Start inside assemblyAiUploadAudio method in TranscribeAudioServiceImpl class.");

		OkHttpClient client = new OkHttpClient();
		File audioFile = new File(filePath);

		RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file",
				audioFile.getName(), RequestBody.create(MediaType.parse("application/octet-stream"), audioFile))
				.build();

		Request uploadRequest = new Request.Builder().url(UPLOAD_ENDPOINT).header("Authorization", apiKey)
				.addHeader("Transfer-Encoding", "chunked").addHeader("Content-Type", "application/octet-stream")
				.post(requestBody).build();

		try (Response response = client.newCall(uploadRequest).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected response code: " + response.code());
			}
			String responseBody = response.body().string();
			// Parse the JSON response to extract the upload URL
			String audioUrl = extractUploadUrl(responseBody);
			log.info("End of assemblyAiUploadAudio method in TranscribeAudioServiceImpl class before return.");
			return audioUrl.toString();
		}
	}

	private String getTranscriptionID(String audioUrl, String apiKey) throws IOException {
		log.info("Start inside getTranscriptionID method in TranscribeAudioServiceImpl class.");

		OkHttpClient client = new OkHttpClient();
		String jsonString = createRequestBody(audioUrl);
		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonString);
		// Build the request
		Request request = new Request.Builder().url(TRANSCRIPT_ENDPOINT).header("Authorization", apiKey)
				.addHeader("Content-Type", "application/json").post(requestBody).build();

		// Execute the request
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected response code: " + response.code());
			}
			String responseBody = response.body().string();
			// Parse the JSON response to get the transcript ID
			JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
			// Extract the value of the "Id" field
			String transcriptionId = jsonObject.get("id").getAsString();
			log.info("End of getTranscriptionID method in TranscribeAudioServiceImpl class before return.");
			return transcriptionId.toString();
		}
	}

	private String getTranscriptionById(String transcriptId, String apiKey) throws IOException {
		log.info("Start inside getTranscriptionById method in TranscribeAudioServiceImpl class.");

		OkHttpClient client = new OkHttpClient();
		Request request = new Request.Builder().url(TRANSCRIPT_ENDPOINT + "/" + transcriptId)
				.header("Authorization", apiKey).addHeader("Content-Type", "application/json").build();

		// Execute the request
		try (Response response = client.newCall(request).execute()) {
			if (!response.isSuccessful()) {
				throw new IOException("Unexpected response code: " + response.code());
			}
			String responseBody = response.body().string();
			log.info("End of getTranscriptionById method in TranscribeAudioServiceImpl class before return.");
			return responseBody.toString();
		}
	}

	private static String createRequestBody(String audioUrl) throws IOException {
		Gson gson = new Gson();
		JSONObject jsonBody = new JSONObject();
		jsonBody.put("audio_url", audioUrl);
		jsonBody.put("speaker_labels", true);
		jsonBody.put("sentiment_analysis", true);
		jsonBody.put("summarization", true);
		jsonBody.put("summary_model", "conversational");
		jsonBody.put("summary_type", "paragraph");
		jsonBody.put("language_code", "en_us");
		return gson.toJson(jsonBody);
	}

	private static String extractUploadUrl(String responseBody) throws IOException {
		Gson gson = new Gson();
		UploadResponse uploadResponse = gson.fromJson(responseBody, UploadResponse.class);
		return uploadResponse.upload_url;
	}

	static class UploadResponse {
		String upload_url;
	}

	private static Map<String, Integer> getCountSentiments(List<TranscribeAudioEntity> transcribeAudioList)
			throws Exception {
		log.info("Start inside getCountSentiments method in TranscribeAudioServiceImpl class.");
		Map<String, Integer> sentimentCount = null;
		if (!transcribeAudioList.isEmpty() && transcribeAudioList.size() > 0) {
			sentimentCount = new HashMap<String, Integer>();
			for (TranscribeAudioEntity transcibeAudio : transcribeAudioList) {
				String sentiment = transcibeAudio.getSentiment();
				if (sentimentCount.containsKey(sentiment)) {
					sentimentCount.put(sentiment, sentimentCount.get(sentiment) + 1);
				} else {
					sentimentCount.put(sentiment, 1);
				}
			}
		}
		return sentimentCount;
	}

	@Override
	public String getFileExtension(File file) {
		String fileName = file.getName();
		int lastIndexOf = fileName.lastIndexOf(".");
		if (lastIndexOf == -1 || lastIndexOf == fileName.length() - 1) {
			return ""; // empty extension
		}
		return fileName.substring(lastIndexOf);
	}

	public void convertToMP3(File sourceFile, File targetFile) throws IOException {
		log.info("Start inside convertToMP3 method in TranscribeAudioServiceImpl class.");
		try {
			// Set audio attributes for MP3 encoding
			AudioAttributes audio = new AudioAttributes();
			audio.setCodec("libmp3lame");
			audio.setBitRate(new Integer(128000)); // 128kbps

			// Set encoding attributes
			EncodingAttributes attributes = new EncodingAttributes();
			attributes.setFormat("mp3");
			attributes.setAudioAttributes(audio);
			// Create a MultimediaObject with the source file
			MultimediaObject multimediaObject = new MultimediaObject(sourceFile);
			// Create an Encoder and encode the file
			Encoder encoder = new Encoder();
			encoder.encode(multimediaObject, targetFile, attributes);
			log.info("MP3 File convert in convertToMP3 Method of TranscribeAudioServiceImpl class");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public String summarization(String transcriptId, String apiKey) throws Exception {

		AssemblyAI client = AssemblyAI.builder()
				.apiKey(apiKey)
				.build();
		// Step 2: Define a summarization prompt
		String prompt = "Provide a brief summary of the transcript.";
		// Step 3: Apply LeMUR.
		var params = LemurTaskParams.builder()
					 .prompt(prompt)
				     .transcriptIds(List.of(transcriptId)).build();

		var response = client.lemur().task(params);

		System.out.println(response.getResponse());
		return response.toString();
	}

	
	public String getTone(String text) {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, parse, sentiment");
		StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);

		List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
		
		double totalSentimentScore = 0;
		int sentenceCount = 0;
		for (CoreMap sentence : sentences) {
			
			//String sentiment = sentence.get(SentimentCoreAnnotations.SentimentClass.class);
			double sentimentScore = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class).score();
			totalSentimentScore += sentimentScore;
			sentenceCount++;
		}

		double averageSentimentScore = totalSentimentScore / sentenceCount;
		// Classify tone based on average sentiment score
		String tone;
		if (averageSentimentScore >= 0.2) {
			tone = "Positive";

		} else if (averageSentimentScore <= -0.2) {
			tone = "Negative";
		} else {
			tone = "Neutral";
		}
		return tone;
	}
	
	public String getVoiceTone(String text) throws IOException {
		log.info("Start inside getVoiceTone method in TranscribeAudioServiceImpl class.");

		HttpClient httpClient = HttpClients.createDefault();
		HttpPost httpPost = new HttpPost("https://api.assemblyai.com/v2/tone");

		JSONObject requestBody = new JSONObject();
		requestBody.put("text", text);

		StringEntity entity = new StringEntity(requestBody.toString());
		httpPost.setEntity(entity);
		httpPost.setHeader("authorization", API_KEY);
		httpPost.setHeader("content-type", "application/json");

		HttpResponse response = httpClient.execute(httpPost);
		HttpEntity responseEntity = response.getEntity();
		String responseBody = EntityUtils.toString(responseEntity);

		JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
		return jsonObject.get("tone").getAsString();
	}
}
