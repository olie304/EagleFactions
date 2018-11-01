package io.github.aquerr.eaglefactions.listeners;import com.flowpowered.math.vector.Vector3i;import io.github.aquerr.eaglefactions.EagleFactions;import io.github.aquerr.eaglefactions.PluginInfo;import io.github.aquerr.eaglefactions.entities.Faction;import io.github.aquerr.eaglefactions.logic.PluginMessages;import org.spongepowered.api.Sponge;import org.spongepowered.api.entity.Transform;import org.spongepowered.api.entity.living.player.Player;import org.spongepowered.api.event.Listener;import org.spongepowered.api.event.Order;import org.spongepowered.api.event.entity.MoveEntityEvent;import org.spongepowered.api.event.filter.cause.Root;import org.spongepowered.api.text.Text;import org.spongepowered.api.text.chat.ChatTypes;import org.spongepowered.api.text.format.TextColors;import org.spongepowered.api.world.Location;import org.spongepowered.api.world.World;import java.util.Optional;public class PlayerMoveListener extends AbstractListener{    public PlayerMoveListener(EagleFactions plugin)    {        super(plugin);    }    @Listener    public void onPlayerMove(MoveEntityEvent event, @Root Player player)    {        Location lastLocation = event.getFromTransform().getLocation();        Location newLocation = event.getToTransform().getLocation();        if (!lastLocation.getChunkPosition().equals(newLocation.getChunkPosition()))        {            World world = player.getWorld();            Vector3i oldChunk = lastLocation.getChunkPosition();            Vector3i newChunk = newLocation.getChunkPosition();            Optional<Faction> optionalOldChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), oldChunk);            Optional<Faction> optionalNewChunkFaction = getPlugin().getFactionLogic().getFactionByChunk(world.getUniqueId(), newChunk);            String oldChunkFactionName;            String newChunkFactionName;            //TODO: Refactor this code.            if (optionalOldChunkFaction.isPresent())            {                oldChunkFactionName = optionalOldChunkFaction.get().getName();            }            else            {                oldChunkFactionName = "Wilderness";            }            if (optionalNewChunkFaction.isPresent())            {                newChunkFactionName = optionalNewChunkFaction.get().getName();            }            else            {                newChunkFactionName = "Wilderness";            }            //Inform a player about entering faction's land.            if (!oldChunkFactionName.equals(newChunkFactionName))            {                if (!newChunkFactionName.equals("SafeZone") && !newChunkFactionName.equals("WarZone") && !newChunkFactionName.equals("Wilderness"))                {                    if (!EagleFactions.AdminList.contains(player.getUniqueId()))                    {                        if (!getPlugin().getFactionLogic().hasOnlinePlayers(optionalNewChunkFaction.get()) && getPlugin().getConfiguration().getConfigFields().getBlockEnteringFactions())                        {                            //Teleport player back if all entering faction's players are offline.                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ENTER_THIS_FACTION + " " + PluginMessages.NONE_OF_THIS_FACTIONS_PLAYERS_ARE_ONLINE));                            event.setToTransform(new Transform<>(world, lastLocation.getPosition(), event.getFromTransform().getRotation(), event.getFromTransform().getScale()));                            return;                        }                    }                }                else if (oldChunkFactionName.equals("WarZone") && newChunkFactionName.equals("SafeZone"))                {                    if (!EagleFactions.AdminList.contains(player.getUniqueId()) && getPlugin().getConfiguration().getConfigFields().shouldBlockEnteringSafezoneFromWarzone())                    {                        if (getPlugin().getPlayerManager().lastDeathAtWarZone(player.getUniqueId()))                        {                            getPlugin().getPlayerManager().setDeathInWarZone(player.getUniqueId(), false);                        }                        else                        {                            //Block player before going to SafeZone from WarZone                            event.setToTransform(new Transform<>(world, lastLocation.getPosition(), event.getFromTransform().getRotation(), event.getFromTransform().getScale()));                            player.sendMessage(Text.of(PluginInfo.ERROR_PREFIX, TextColors.RED, PluginMessages.YOU_CANT_ENTER_SAFEZONE_WHEN_YOU_ARE_IN_WARZONE));                            return;                        }                    }                }                //TODO: Show respective colors for enemy faction, alliance & neutral.                Text information = Text.builder()                        .append(Text.of(PluginMessages.YOU_HAVE_ENTERED_FACTION + " ", TextColors.GOLD, newChunkFactionName))                        .build();                player.sendMessage(ChatTypes.ACTION_BAR, information);            }            //Check if player has tuned on AutoClaim            if (EagleFactions.AutoClaimList.contains(player.getUniqueId()))            {                Sponge.getCommandManager().process(player, "f claim");            }            //Check if player has turned on AutoMap            if (EagleFactions.AutoMapList.contains(player.getUniqueId()))            {                Sponge.getCommandManager().process(player, "f map");            }        }    }}