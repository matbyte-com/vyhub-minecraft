package net.vyhub;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.vyhub.entity.*;
import okhttp3.*;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.*;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface VyHubAPI {
    static final Gson gson = new GsonBuilder().create();

    public static VyHubAPI create(final String API_URL, final String token) {
        return VyHubAPI.create(API_URL, token, null);
    }

    public static VyHubAPI create(final String API_URL, final String token, OkHttpClient client) {
        OkHttpClient.Builder builder = client != null ? client.newBuilder() : new OkHttpClient.Builder();

        return new Retrofit.Builder()
                .baseUrl(API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder
                        .addInterceptor(new Interceptor() {
                            @Override
                            public okhttp3.Response intercept(Chain chain) throws IOException {
                                Request original = chain.request();

                                Request request = original.newBuilder()
                                        .header("Authorization", "Bearer " + token)
                                        .header("Accept", "application/json")
                                        .method(original.method(), original.body())
                                        .build();

                                return chain.proceed(request);
                            }
                        }).build())
                .build().create(VyHubAPI.class);
    }

    @GET("/advert/?active=true&serverbundle_id={id}")
    public Call<List<Advert>> getAdverts(@Path("id") String id);

    @PATCH("/auth/request/{validation_uuid}")
    public Call<Object> confirmAuth(@Path("validation_uuid") String validation_uuid, @Body RequestBody body);

    @POST("/user/")
    public Call<VyHubUser> createUser(@Body RequestBody body);

    @GET("/user/{id}?type=MINECRAFT")
    public Call<VyHubUser> getUser(@Path("id") String id);

    @GET("/server/{id}")
    public Call<Server> getServer(@Path("id") String id);

    @PATCH("/server/{id}")
    public Call<Server> patchServer(@Path("id") String id, @Body RequestBody body);

    @GET("/user/attribute/definition/playtime")
    public Call<Definition> getPlaytimeDefinition();

    @POST("/user/attribute/definition")
    public Call<Definition> createPlaytimeDefinition(@Body RequestBody body);

    @POST("/user/attribute")
    public Call<Object> sendPlayerTime(@Body RequestBody body);

    @POST("/warning/?morph_user_id={admin_id}")
    public Call<Warn> createWarning(@Path("admin_id") String admin_id, @Body RequestBody body);

    @GET("/server/bundle/{serverbundle_id}/ban?active=true")
    public Call<Map<String, List<Ban>>> getBans(@Path("serverbundle_id") String serverbundle_id);

    @POST("/ban/?morph_user_id={admin_id}")
    public Call<Ban> createBan(@Path("admin_id") String admin_id, @Body RequestBody body);

    @POST("/ban/")
    public Call<Ban> createBanWithoutCreator(@Body RequestBody body);

    @PATCH("/user/{user_id}/ban?serverbundle_id={serverbundle_id}")
    public Call<Ban> unbanUser(@Path("user_id") String user_id, @Path("serverbundle_id") String serverbundle_id);

    @GET("/group/")
    public Call<List<Group>> getGroups();

    @GET("/user/{user_id}/group?serverbundle_id={serverbundle_id}")
    public Call<List<Group>> getUserGroups(@Path("user_id") String user_id, @Path("serverbundle_id") String serverbundle_id);

    @POST("/user/{id}/membership")
    public Call<Membership> createMembership(@Path("id") String id, @Body RequestBody body);

    @DELETE("/user/{user_id}/membership/by-group?group_id={group_id}&serverbundle_id={serverbundle_id}")
    public Call<Membership> deleteMembership(@Path("user_id") String user_id, @Path("group_id") String group_id, @Path("serverbundle_id") String serverbundle_id);

    @DELETE("/user/{user_id}/membership?serverbundle_id={serverbundle_id}")
    public Call<Membership> deleteAllMemberships(@Path("user_id") String user_id, @Path("serverbundle_id") String serverbundle_id);

    @GET("/packet/reward/applied/user?active=true&foreign_ids=true&status=OPEN&serverbundle_id={serverbundle_id}&for_server_id={server_id}&{user_ids}")
    public Call<Map<String, List<AppliedReward>>>  getRewards(@Path("serverbundle_id") String serverbundle_id, @Path("server_id") String server_id, @Path("user_ids") String user_ids);

    @PATCH("/packet/reward/applied/{reward_id}")
    public Call<AppliedReward> sendExecutedRewards(@Path("reward_id") String reward_id, @Body RequestBody body);
}
