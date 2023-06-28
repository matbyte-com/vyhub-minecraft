package net.vyhub.abstractClasses;

import net.vyhub.VyHubPlatform;
import net.vyhub.entity.Advert;
import net.vyhub.abstractClasses.AServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public abstract class AAdvert {
    private static List<Advert> adverts = new ArrayList<>();

    private static int currentAdvert = 0;

    private final VyHubPlatform platform;

    public AAdvert(VyHubPlatform platform) {
        this.platform = platform;
    }


    public void loadAdverts() {
        platform.executeAsync(() -> {
            try {
                adverts = platform.getApiClient().getAdverts(AServer.serverbundleID).execute().body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void nextAdvert() {
        if (adverts == null || adverts.size() < 1) {
            return;
        }

        if (adverts.get(currentAdvert) == null) {
            currentAdvert = 0;
        }

        Advert advert = adverts.get(currentAdvert);
        showAdvert(advert);
        if (currentAdvert + 1 == adverts.size()) currentAdvert = 0;
        else currentAdvert++;
    }

    public abstract String replaceColorTags(String text);

    public abstract void showAdvert(Advert advert);
}
