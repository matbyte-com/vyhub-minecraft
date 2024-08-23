package net.vyhub.tasks;

import net.vyhub.VyHubPlatform;
import net.vyhub.abstractClasses.ABans;
import net.vyhub.abstractClasses.AGroups;
import net.vyhub.abstractClasses.AUser;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ban.ProfileBanList;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerProfile;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TBans extends ABans {
    private final Pattern verPattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)");
    private final Matcher verMatcher = verPattern.matcher(Bukkit.getBukkitVersion());
    private final boolean pre120 = !verMatcher.find() || !(Integer.parseInt(verMatcher.group(1)) >= 2 || Integer.parseInt(verMatcher.group(2)) >= 20);

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

        if (pre120) {
            Bukkit.getBanList(BanList.Type.NAME).addBan(playerID, vyhubBan.getReason(), endDate, "VyHub");
        } else {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.fromString(playerID));
            ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE)).addBan(profile, vyhubBan.getReason(), endDate, "VyHub");
        }

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
        if (pre120) {
            Bukkit.getBanList(BanList.Type.NAME).pardon(playerID);
        } else {
            PlayerProfile profile = Bukkit.createPlayerProfile(UUID.fromString(playerID));
            ((ProfileBanList) Bukkit.getBanList(BanList.Type.PROFILE)).pardon(profile);
        }

        return true;
    }

    @Override
    public String getPlayerIdentifier(String playerName) {
        Player sourcePlayer = Bukkit.getPlayer(playerName);
        if (sourcePlayer != null) {
            return sourcePlayer.getUniqueId().toString();
        } else {
            return null;
        }
    }
}
