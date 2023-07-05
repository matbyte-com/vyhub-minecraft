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
            platform.log(SEVERE, String.format("[API Error] Failed to %s: %s", action, response.message()));
            return false;
        }
        return true;
    }
}
