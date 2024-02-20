package net.vyhub.lib;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.vyhub.VyHubPlatform;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Cache;
import org.json.JSONObject;
import retrofit2.Response;

import java.io.File;
import java.io.IOException;
import java.net.ProxySelector;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static java.util.logging.Level.SEVERE;

public class Utility {
    public static RequestBody createRequestBody(HashMap<String, Object> values) {
        return RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"),(new JSONObject(values)).toString());
    }

    public static OkHttpClient okhttp(File cacheFolder) {
        return okhttpBuilder().cache(new Cache(cacheFolder, 1024 * 1024 * 10)).build();
    }

    public static OkHttpClient.Builder okhttpBuilder() {
        return new OkHttpClient.Builder()
                .connectTimeout(6, TimeUnit.SECONDS)
                .writeTimeout(7, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .proxySelector(ProxySelector.getDefault());
    }

    public static Boolean checkResponse(VyHubPlatform platform, Response response, String action) {
        if (!response.isSuccessful()) {
            try {
                assert response.errorBody() != null;
                platform.log(SEVERE, String.format("[API Error] Failed to %s: %d, %s", action, response.code(), response.errorBody().string()));
                platform.log(SEVERE, String.format("[API Error] Request: %s", response.raw().request().url(), response.raw().request().body()));
            } catch (IOException | NullPointerException | AssertionError e) {
                platform.log(SEVERE, String.format("[API Error] Failed to %s - Response body contains exception: %s", action, e.getMessage()));
            }
            return false;
        }
        return true;
    }
}
