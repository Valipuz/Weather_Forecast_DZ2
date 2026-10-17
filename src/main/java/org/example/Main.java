package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Main {
    private static final String LAT = "55.75";
    private static final String LON = "37.62";
    private static final int LIMIT = 3;

    private static final String ACCESS_KEY = "615a5c25-a581-4537-9134-3ce64d3e85c0";

    public static void main(String[] args) {
        String url = String.format("https://api.weather.yandex.ru/v2/forecast?lat=%s&lon=%s&limit=%d", LAT, LON, LIMIT);
        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .header("X-Yandex-Weather-Key", ACCESS_KEY)
                    .build();

            HttpResponse<String> response = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            int statusCode = response.statusCode();
            String responseBody = response.body();

            if (statusCode != 200) {
                System.out.println("Ошибка! Статус код: " + statusCode);
                System.out.println("Ответ: " + responseBody);
                return;
            }

            JsonElement rootElement = JsonParser.parseString(responseBody);
            JsonObject rootObject = rootElement.getAsJsonObject();

            printResponseBody(responseBody);
            printFactTemp(rootObject);
            printAverageTemp(rootObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void printResponseBody(String responseBody) {
        System.out.println("====== ПОЛНЫЙ ОТВЕТ СЕРВИСА ======");
        System.out.println(responseBody);
    }

    private static void printFactTemp(JsonObject jsonObject) {
        System.out.println("====== ТЕКУЩАЯ ТЕМПЕРАТУРА ======");
        String temp = jsonObject.getAsJsonObject("fact").get("temp").getAsString();
        System.out.println("Температура (fact.temp): " + temp);
    }

    private static void printAverageTemp(JsonObject jsonObject) {
        System.out.println("====== СРЕДНЯЯ ТЕМПЕРАТУРА ПО ПРОГНОЗУ ======");
        JsonArray forecasts = jsonObject.getAsJsonArray("forecasts");
        int forecastsSize = forecasts.size();
        System.out.println("Получено дней прогноза (limit=" + LIMIT + "): " + forecastsSize);

        double sumTemp = 0;
        int limiter = Math.min(forecastsSize, LIMIT);
        for (int i = 0; i < limiter; i++) {
            sumTemp += forecasts.get(i).getAsJsonObject()
                    .getAsJsonObject("parts")
                    .getAsJsonObject("day")
                    .get("temp_avg").getAsDouble();
        }

        if (limiter > 0) {
            double avgTemp = sumTemp / limiter;
            System.out.printf("Дней: %d, средняя температура = %.2f", limiter, avgTemp);
            System.out.println();
        } else {
            System.out.println("Нет данных для расчёта средней температуры");
        }
    }
}