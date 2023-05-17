package net.vyhub.server;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import net.vyhub.Entity.Advert;
import net.vyhub.lib.Types;
import net.vyhub.lib.Utility;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class SvAdverts {
    private static Gson gson = new Gson();
    private static List<Advert> adverts = new ArrayList<>();

    private static int currentAdvert = 0;

    public static void loadAdverts() {
        HttpResponse<String> response = Utility.sendRequest(String.format("/advert/?active=true&serverbundle_id=%s", SvServer.serverbundleID), Types.GET);

        if (response == null || response.statusCode() != 200) {
            return;
        }

        adverts = gson.fromJson(response.body(), new TypeToken<ArrayList<Advert>>() {
        }.getType());
    }

    public static Advert nextAdvert() {
        if (adverts.size() < 1) {
            return null;
        }

        if (adverts.get(currentAdvert) == null) {
            currentAdvert = 0;
        }

        net.vyhub.Entity.Advert advert = adverts.get(currentAdvert);
        return advert;
    }
}
