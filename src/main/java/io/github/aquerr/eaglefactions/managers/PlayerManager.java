package io.github.aquerr.eaglefactions.managers;

import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.entities.FactionMemberType;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.service.user.UserStorageService;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Created by Aquerr on 2017-08-04.
 */
public class PlayerManager
{
    private static Path playersPath;
    private static UserStorageService userStorageService;

    public PlayerManager(Path configDir)
    {
        try
        {
            playersPath = configDir.resolve("players");
            if (!Files.exists(playersPath)) Files.createDirectory(playersPath);

            Optional<UserStorageService> optionalUserStorageService = Sponge.getServiceManager().provide(UserStorageService.class);
            optionalUserStorageService.ifPresent(userStorageService1 -> userStorageService = userStorageService1);

        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static Optional<String> getPlayerName(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return Optional.of(oUser.get().getName());
    }

    public static Optional<Player> getPlayer(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return oUser.get().getPlayer();
    }

    private static Optional<User> getUser(UUID playerUUID)
    {
        Optional<User> oUser = userStorageService.get(playerUUID);

        return oUser;
    }

    public static boolean isPlayerOnline(UUID playerUUID)
    {
        Optional<User> oUser = getUser(playerUUID);

        return oUser.map(User::isOnline).orElse(false);
    }

    public static List<Player> getServerPlayers()
    {
        try
        {
            List<Player> playerList = new ArrayList<>();

            File playerDirectory = new File(playersPath.toUri());
            File[] playerFiles = playerDirectory.listFiles();

            for(File playerFile : playerFiles)
            {
                String uuid = trimFileExtension(playerFile.getName());
                if(uuid.split("-").length == 5)
                {
                    Optional<Player> optionalPlayer = getPlayer(UUID.fromString(uuid));
                    optionalPlayer.ifPresent(playerList::add);
                }
            }

            return playerList;
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }

        return new ArrayList<>();
    }

    public static void setDeathInWarZone(UUID playerUUID, boolean didDieInWarZone)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            playerNode.getNode("death-in-warzone").setValue(didDieInWarZone);

            configLoader.save(playerNode);
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public static boolean lastDeathAtWarZone(UUID playerUUID)
    {
        Path playerFile = Paths.get(playersPath +  "/" + playerUUID.toString() + ".conf");

        try
        {
            ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(playerFile).build();

            CommentedConfigurationNode playerNode = configLoader.load();

            Object value = playerNode.getNode("death-in-warzone").getValue();

            if (value != null)
            {
                return (boolean)value;
            }
            else
            {
                playerNode.getNode("death-in-warzone").setValue(false);
                configLoader.save(playerNode);
                return false;
            }
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return false;
    }

    public static @Nullable FactionMemberType getFactionMemberType(Player factionPlayer, Faction faction)
    {
        if (faction.getLeader().equals(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.LEADER;
        }
        else if(faction.getMembers().contains(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.MEMBER;
        }
        else if (faction.getOfficers().contains(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.OFFICER;
        }
        else if (faction.getRecruits().contains(factionPlayer.getUniqueId()))
        {
            return FactionMemberType.RECRUIT;
        }
        else if (faction.getAlliances().contains(factionPlayer.getUniqueId().toString()))
        {
            return FactionMemberType.ALLY;
        }

        return null;
    }

    private static String trimFileExtension(String str) {
        // Handle null case specially.

        if (str == null) return null;

        // Get position of last '.'.

        int pos = str.lastIndexOf(".");

        // If there wasn't any '.' just return the string as is.

        if (pos == -1) return str;

        // Otherwise return the string, up to the dot.

        return str.substring(0, pos);
    }
}
