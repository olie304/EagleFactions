package io.github.aquerr.eaglefactions.logic;

import com.google.common.collect.Sets;
import io.github.aquerr.eaglefactions.config.ConfigAccess;
import io.github.aquerr.eaglefactions.config.IConfig;
import io.github.aquerr.eaglefactions.config.FactionsConfig;
import io.github.aquerr.eaglefactions.entities.Faction;
import ninja.leaping.configurate.ConfigurationNode;
import org.spongepowered.api.entity.living.player.Player;


import java.util.*;
import java.util.function.Function;

/**
 * Created by Aquerr on 2017-07-12.
 */
public class FactionLogic
{
    //TODO:Add other configs
    //private static IConfig mainConfig = MainConfig.getMainConfig();
    private static IConfig factionsConfig = FactionsConfig.getConfig();
    //private static IConfig claimsConfig = ClaimsConfig.getMainConfig();
    //private static IConfig messageConfig = MessageConfig.getMainConfig();

    public static String getFactionName(UUID playerUUID)
    {
        for (Object t : FactionLogic.getFactions ())
        {
            String faction = String.valueOf (t);

            if(faction.equals ("WarZone") || faction.equals ("SafeZone"))
            {
                continue;
            }

            //TODO: If even leader and officers are stored in Members group, checking members is enough.
            if(FactionLogic.getMembers(faction).contains(playerUUID.toString ()))
            {
                return faction;
            }
            else if(FactionLogic.getLeader(faction).equals(playerUUID.toString ()))
            {
                return faction;
            }

            //TODO:Add check for officers.
            else if(FactionLogic.getOfficers(faction).contains(playerUUID.toString ()))
            {
                return faction;
            }
        }
        return null;
    }

    public static Faction getFaction(String factionName)
    {
        ConfigurationNode leaderNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "leader");
        Object leaderUUID = leaderNode.getValue();

        Faction faction = new Faction(factionName, UUID.fromString(leaderUUID.toString()));

        faction.Members = getMembers(factionName);

        //TODO: Load other faction properties here.
        faction.Officers = getOfficers(factionName);
        faction.Enemies = getEnemies(factionName);
        faction.Alliances = getAlliances(factionName);
        //faction.Claims = getClaims(factionName);

        //TODO: Implement power service.
        //faction.Power = PowerService.getFactionPower(faction.Members);

        return faction;
    }

    public static String getLeader(String factionName)
    {
        ConfigurationNode valueNode = ConfigAccess.getConfig(factionsConfig).getNode((Object[]) ("factions." + factionName + ".leader").split("\\."));

        if (valueNode.getValue() != null)
            return valueNode.getString();
        else
            return "";
    }

    public static List<String> getOfficers(String factionName)
    {
        ConfigurationNode officersNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName,"officers");

        if (officersNode.getValue() != null)
        {
            List<String> officersList = officersNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(officersList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static List<String> getMembers(String factionName)
    {
        ConfigurationNode membersNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName,"members");

        if (membersNode.getValue() != null)
        {
            List<String> membersList = membersNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(membersList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static Set<Object> getFactions()
    {
        if(ConfigAccess.getConfig(factionsConfig).getNode ("factions","factions").getValue() != null)
        {
            ConfigAccess.removeChild(factionsConfig, new Object[]{"factions"}, "factions");
        }

        if(ConfigAccess.getConfig(factionsConfig).getNode("factions").getValue() != null)
        {
            return ConfigAccess.getConfig(factionsConfig).getNode("factions").getChildrenMap().keySet();
        }

            return Sets.newHashSet ();
    }

    public static boolean createFaction(String factionName, UUID playerUUID)
    {
        try
        {
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "leader"},(playerUUID.toString()));
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"},(new ArrayList<String>()));
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "home"},"");
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "members"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "enemies"},new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig,new Object[]{"factions", factionName, "alliances"}, new ArrayList<String>());
            ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "friendlyfire"}, false);
        }
        catch (Exception exception)
        {
            return false;
        }

        return true;
    }

    public static void disbandFaction(String factionName)
    {
        ConfigAccess.removeChild(factionsConfig, new Object[]{"factions"},factionName);
    }

    public static void joinFaction(UUID playerUUID, String factionName)
    {
        List<String> memberList = new ArrayList<>(getMembers(factionName));
        memberList.add(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);

    }

    public static void leaveFaction(UUID playerUUID, String factionName)
    {
        List<String> memberList = new ArrayList<>(getMembers(factionName));

        memberList.remove(playerUUID.toString());

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, memberList);
    }

    public static void addAllay(String playerFactionName, String invitedFactionName)
    {
        List<String> playerFactionAllianceList = new ArrayList<>(getAlliances(playerFactionName));
        List<String> invitedFactionAllianceList = new ArrayList<>(getAlliances(invitedFactionName));

        playerFactionAllianceList.add(invitedFactionName);
        invitedFactionAllianceList.add(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "alliances"}, playerFactionAllianceList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", invitedFactionName, "alliances"}, invitedFactionAllianceList);
    }

    public static List<String> getAlliances(String factionName)
    {
        ConfigurationNode allianceNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "alliances");

        if (allianceNode.getValue() != null)
        {
            List<String> alliancesList = allianceNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(alliancesList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    private static Function<Object,String> objectToStringTransformer = input ->
    {
        if (input instanceof String)
        {
            return (String) input;
        }
        else
        {
            return null;
        }
    };

    public static void removeAlliance(String playerFactionName, String removedFaction)
    {
        List<String> playerFactionAllianceList = new ArrayList<>(getAlliances(playerFactionName));
        List<String> removedFactionAllianceList = new ArrayList<>(getAlliances(removedFaction));

        playerFactionAllianceList.remove(removedFaction);
        removedFactionAllianceList.remove(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "alliances"}, playerFactionAllianceList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", removedFaction, "alliances"}, removedFactionAllianceList);
    }

    public static List<String> getEnemies(String factionName)
    {
        ConfigurationNode enemiesNode = ConfigAccess.getConfig(factionsConfig).getNode("factions", factionName, "enemies");

        if (enemiesNode.getValue() != null)
        {
            List<String> enemiesList = enemiesNode.getList(objectToStringTransformer);

            List<String> helpList = new ArrayList<>(enemiesList);

            return helpList;
        }
        else return new ArrayList<String>();
    }

    public static void addEnemy(String playerFactionName, String enemyFactionName)
    {
        List<String> playerFactionEnemiesList = new ArrayList<>(getEnemies(playerFactionName));
        List<String> enemyFactionEnemiesList = new ArrayList<>(getEnemies(enemyFactionName));

        playerFactionEnemiesList.add(enemyFactionName);
        enemyFactionEnemiesList.add(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "enemies"}, playerFactionEnemiesList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", enemyFactionName, "enemies"}, enemyFactionEnemiesList);
    }

    public static void removeEnemy(String playerFactionName, String enemyFactionName)
    {
        List<String> playerFactionEnemiesList = new ArrayList<>(getEnemies(playerFactionName));
        List<String> enemyFactionEnemiesList = new ArrayList<>(getEnemies(enemyFactionName));

        playerFactionEnemiesList.remove(enemyFactionName);
        enemyFactionEnemiesList.remove(playerFactionName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", playerFactionName, "enemies"}, playerFactionEnemiesList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", enemyFactionName, "enemies"}, enemyFactionEnemiesList);
    }

    public static void addOfficer(String newOfficerName, String factionName)
    {
        List<String> officersList = new ArrayList<>(getOfficers(factionName));
        List<String> membersList = new ArrayList<>(getMembers(factionName));

        officersList.add(newOfficerName);
        membersList.remove(newOfficerName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, membersList);
    }

    public static void removeOfficer(String officerName, String factionName)
    {
        List<String> officersList = new ArrayList<>(getOfficers(factionName));
        List<String> membersList = new ArrayList<>(getMembers(factionName));

        officersList.remove(officerName);
        membersList.add(officerName);

        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "officers"}, officersList);
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "members"}, membersList);

    }

    public static boolean getFactionFriendlyFire(String factionName)
    {
        ConfigurationNode friendlyFireNode = ConfigAccess.getConfig(factionsConfig).getNode("eaglefactions", factionName, "friendlyfire");

        Boolean friendlyFire = friendlyFireNode.getBoolean();

        return friendlyFire;
    }

    public static void setFactionFriendlyFire(String factionName, boolean turnOn)
    {
        ConfigAccess.setValueAndSave(factionsConfig, new Object[]{"factions", factionName, "friendlyfire"}, turnOn);
    }
}
