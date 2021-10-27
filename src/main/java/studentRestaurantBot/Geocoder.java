package studentRestaurantBot;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

//Transforms restaurant addresses to coordinates using HERE API 
public class Geocoder {
    private static final String apiAddress = "https://geocode.search.hereapi.com/v1/geocode";
    private static final String api_key = "apikey";

    public ArrayList<Double> geocode(String address) throws IOException, InterruptedException {
        ArrayList<Double> coordinates = parseResponse(getResponse(address));

        return coordinates;

    }

    private HttpResponse getResponse(String address) throws IOException, InterruptedException {

        HttpClient httpClient = HttpClient.newHttpClient();

        String encodedAddress = URLEncoder.encode(address, "UTF-8");
        String requestAddress = apiAddress + "?apiKey=" + api_key + "&q=" + encodedAddress;

        HttpRequest geocodingRequest = HttpRequest.newBuilder().GET().uri(URI.create(requestAddress))
                .timeout(Duration.ofMillis(2000)).build();

        HttpResponse geocodingResponse = httpClient.send(geocodingRequest, HttpResponse.BodyHandlers.ofString());

        return geocodingResponse;
    }

    private ArrayList<Double> parseResponse(HttpResponse response)
            throws JsonMappingException, JsonProcessingException {
        String responseString = response.body().toString();

        ObjectMapper mapper = new ObjectMapper();

        JsonNode responseJsonNode = mapper.readTree(responseString);
        JsonNode items = responseJsonNode.get("items");

        ArrayList<Double> coordinates = new ArrayList<>();
        for (JsonNode item : items) {

            JsonNode position = item.get("position");
            String lat = position.get("lat").asText();
            String lng = position.get("lng").asText();

            coordinates.add(Double.valueOf(lat));
            coordinates.add(Double.valueOf(lng));

        }

        return coordinates;

    }
}
