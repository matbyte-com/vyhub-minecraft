package net.vyhub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.vyhub.entity.Advert;

public interface VyHubAPI {
    static final String API_URL = "https://api.vyhub.net";
    static final Gson gson = new GsonBuilder().create();

    @GET("/advert/?active=true&serverbundle_id={id}")
    public Call<Advert> getAdverts(@Path("id") String id);
}
