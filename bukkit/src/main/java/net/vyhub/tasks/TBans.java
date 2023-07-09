package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ABans;
import net.vyhub.abstractClasses.AGroups;
import net.vyhub.abstractClasses.AUser;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

public class TBans extends ABans {
    public TBans(VyHubPlatform platform, AUser aUser, AGroups aGroups) {
        super(platform, aUser, aGroups);
    }

    @Override
    public boolean addMinecraftBan(String playerID, net.vyhub.entity.Ban vyhubBan) {
        Date endDate = null;
        if (vyhubBan.getEnds_on() != null) {
            ZonedDateTime expiresDate = ZonedDateTime.parse(vyhubBan.getEnds_on(), isoDateFormatter);
            endDate = Date.from(expiresDate.toInstant());
        }

        Bukkit.getBanList(BanList.Type.NAME).addBan(playerID, vyhubBan.getReason(), endDate, "VyHub");

        Player bannnedPlayer = Bukkit.getPlayer(UUID.fromString(playerID));
        if (bannnedPlayer != null) {

            getPlatform().executeBlocking(() -> {
                bannnedPlayer.kickPlayer(String.format(getPlatform().getI18n().get("youGotBanned"), vyhubBan.getReason()));
            });
        }

        return true;
    }

    @Override
    public boolean unbanMinecraftBan(String playerID) {
        Bukkit.getBanList(BanList.Type.NAME).pardon(playerID);
        return true;
    }
}
