package ru.popoffvg.sonarqube;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.gson.Gson;

import ru.popoffvg.sonaradapter.SonarAdapterPlugin;

public class SonarQube {
	private final String adress;
	private final String token;
	private volatile int numberOfPages;
	private final HttpClient httpClient;
	private static final int DURATION = 10;
	
	private SonarQube(String adress, String token) {
		super();
		this.adress = adress;
		this.token = token;
		numberOfPages = 0;
		httpClient = HttpClient.newBuilder().build();
	}

	public static SonarQube create(String adress, String token) {
		return new SonarQube(adress, token);
	}
	
	public Stream<Issue> search(HashMap<String, String> params){
		return Stream.iterate(1, i -> i + 1)
			// TODO неправильное изменение numberOfPages
			.takeWhile(this::hasNextPage)
			.flatMap(i -> getIssues(i, params));
	}
	
	public Optional<String> getRawText(String key) {
		Optional<String> text = Optional.empty();
		HashMap<String, String>queryParams = new HashMap<>();
		queryParams.put("key", key);
		try {
			String moduleText = doQuery("/api/sources/raw", queryParams).body();
			text = Optional.of(moduleText.replace("\r", ""));
		} catch (Exception e) {
			SonarAdapterPlugin.logError(e);
		}
		return text;
	}
	
	private Stream<Issue> getIssues(int pageNumber, HashMap<String, String> params){
		HttpResponse<String> responce;
		HashMap<String, String> queryParams = new HashMap<>(params);
		queryParams.put("p", Integer.toString(pageNumber));
		try {
			responce = doQuery("/api/issues/search", queryParams);
			SearchResponce data = new Gson().fromJson(responce.body(), SearchResponce.class);
			if(numberOfPages == 0) {
				setNumberOfPage(data.numberOfPages());
			}
			return data.getIssues().stream();
			
		} catch (Exception e) {
			SonarAdapterPlugin.logError(e);
			numberOfPages = 1;
		}
		return Stream.empty();
	}

	private synchronized int setNumberOfPage(int pages) {
		return numberOfPages = pages;
	}
	
	private HttpResponse<String> doQuery(String path, HashMap<String, String> params) throws IOException, InterruptedException {
		String queryParams = params.isEmpty() ? "" : "?" + paramsQuery(params);
		URI uri = URI.create(adress + path + queryParams);
		HttpRequest request = HttpRequest.newBuilder().GET()
				.timeout(Duration.ofSeconds(DURATION))
				.uri(uri)
				.build();
		return httpClient.send(request, BodyHandlers.ofString());
	}
	
	private String paramsQuery(HashMap<String, String> params) {
		return  params.entrySet().stream()
				.map(this::paramsToString)
				.collect(Collectors.joining("&"));
	}
	
	private String paramsToString(Entry<String, String> entry) {
		try {
			return entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return "";
		}
	}
	
	private boolean hasNextPage(int pageNumber) {
		return numberOfPages == 0 || pageNumber <= numberOfPages;
	}

}
