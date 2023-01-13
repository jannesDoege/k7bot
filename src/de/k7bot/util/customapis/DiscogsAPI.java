/**
 *
 */
package de.k7bot.util.customapis;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.apache.commons.text.similarity.LevenshteinDistance;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import de.k7bot.Klassenserver7bbot;
import de.k7bot.music.utilities.SongJson;

/**
 * @author Klassenserver7b
 *
 */
public class DiscogsAPI {

	private final Logger log;

	public DiscogsAPI() {

		log = LoggerFactory.getLogger(this.getClass());

	}

	public SongJson getFilteredSongJson(String searchquery) throws IllegalArgumentException {

		if (!this.isApiEnabled()) {
			return null;
		}

		JsonObject songjson = getSongJson(searchquery);

		if (songjson == null) {
			return null;
		}

		String title = songjson.get("title").getAsString();
		JsonArray artists = songjson.get("artists").getAsJsonArray();
		String year = songjson.get("year").getAsString();
		String url = songjson.get("uri").getAsString();
		String apiurl = songjson.get("resource_url").getAsString();

		int dist = LevenshteinDistance.getDefaultInstance().apply(artists + " " + title, searchquery);
		int rotdist = LevenshteinDistance.getDefaultInstance().apply(title + " " + artists, searchquery);
		int titledist = LevenshteinDistance.getDefaultInstance().apply(title, searchquery);

		if (dist > 10 && rotdist > 10 && titledist > 10) {
			return null;
		}

		return SongJson.of(title, artists, year, url, apiurl);

	}

	private JsonObject getSongJson(String searchquery) {

		assert this.isApiEnabled();

		String res_url = getMasterJson(searchquery);

		if (res_url == null) {
			return null;
		}

		final CloseableHttpClient httpclient = HttpClients.createSystem();
		final HttpGet httpget = new HttpGet(res_url);

		try (final CloseableHttpResponse response = httpclient.execute(httpget)) {

			if (response.getCode() != 200) {
				log.warn("Invalid response from api.dicogs.com");
				return null;
			}

			JsonElement elem = JsonParser.parseString(EntityUtils.toString(response.getEntity()));

			response.close();
			httpclient.close();

			return elem.getAsJsonObject();

		} catch (IOException | JsonSyntaxException | ParseException e) {
			log.error(e.getMessage(), e);

			try {
				httpclient.close();
			} catch (IOException e1) {
				log.error(e1.getMessage(), e1);
			}

		}

		return null;
	}

	private String getMasterJson(String searchquery) {

		assert this.isApiEnabled();

		JsonObject queryresults = getQueryResults(searchquery);

		if (queryresults == null) {
			return null;
		}

		JsonArray results = queryresults.get("results").getAsJsonArray();

		if (results.size() < 1) {
			return null;
		}

		for (int i = 0; i < results.size(); i++) {

			if (results.get(i).getAsJsonObject().get("type").getAsString().equalsIgnoreCase("release")) {
				return results.get(i).getAsJsonObject().get("resource_url").getAsString();
			}

		}

		return null;
	}

	private JsonObject getQueryResults(String searchquery) {

		assert this.isApiEnabled();

		final CloseableHttpClient httpclient = HttpClients.createSystem();

		String token = Klassenserver7bbot.getInstance().getPropertiesManager().getProperty("discogs-token");

		String preparedquery = URLEncoder.encode(searchquery, StandardCharsets.UTF_8);

		final HttpGet httpget = new HttpGet(
				"https://api.discogs.com/database/search?query=" + preparedquery + "&per_page=3&page=1");
		httpget.setHeader(HttpHeaders.AUTHORIZATION, "Discogs token=" + token);

		try (final CloseableHttpResponse response = httpclient.execute(httpget)) {

			if (response.getCode() != 200) {
				log.warn("Invalid response from api.dicogs.com");
				return null;
			}

			JsonElement elem = JsonParser.parseString(EntityUtils.toString(response.getEntity()));

			response.close();
			httpclient.close();

			return elem.getAsJsonObject();

		} catch (IOException | JsonSyntaxException | ParseException e) {
			log.error(e.getMessage(), e);

			try {
				httpclient.close();
			} catch (IOException e1) {
				log.error(e1.getMessage(), e1);
			}

		}

		return null;

	}

	public boolean isApiEnabled() {

		if (!Klassenserver7bbot.getInstance().getPropertiesManager().isApiEnabled("discogs")) {
			log.error("Invalid Discogs Token - API Disabled", new Throwable().fillInStackTrace());
		}

		return Klassenserver7bbot.getInstance().getPropertiesManager().isApiEnabled("discogs");

	}

}
