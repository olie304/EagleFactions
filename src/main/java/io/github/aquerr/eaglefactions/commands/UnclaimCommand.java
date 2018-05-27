package io.github.aquerr.eaglefactions.commands;

import com.flowpowered.math.vector.Vector3i;
import io.github.aquerr.eaglefactions.EagleFactions;
import io.github.aquerr.eaglefactions.PluginInfo;
import io.github.aquerr.eaglefactions.entities.Faction;
import io.github.aquerr.eaglefactions.logic.FactionLogic;
import io.github.aquerr.eaglefactions.logic.PVPLogger;
import io.github.aquerr.eaglefactions.logic.PluginMessages;
import io.github.aquerr.eaglefactions.managers.FlagManager;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Chunk;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class UnclaimCommand implements CommandExecutor
{
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException
    {
        if(source instanceof Player)
        {
            Player player = (Player)source;

            Optional<Faction> optionalPlayerFaction = FactionLogic.getFactionByPlayerUUID(player.getUniqueId());

            //Check if player has admin mode.
            if(EagleFactions.AdminList.contains(player.getUniqueId()))
            {
                World world = player.getWorld();
                Vector3i chunk = player.getLocation().getChunkPosition();

                Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), chunk);

                if (optionalChunkFaction.isPresent())
                {
                    if (optionalChunkFaction.get().Home != null)
                    {
                        if (world.getUniqueId().equals(optionalChunkFaction.get().Home.WorldUUID))
                        {
                            Location homeLocation = world.getLocation(optionalChunkFaction.get().Home.BlockPosition);

                            if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString())) FactionLogic.setHome(world.getUniqueId(), optionalChunkFaction.get(), null);
                        }
                    }

                    FactionLogic.removeClaim(optionalChunkFaction.get(), world.getUniqueId() ,chunk);

                    player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
                    return CommandResult.success();
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_DOES_NOT_BELOG_TO_ANYONE));
                    return CommandResult.success();
                }
            }

            //Check if player is in the faction.
            if(optionalPlayerFaction.isPresent())
            {
                Faction playerFaction = optionalPlayerFaction.get();

                if (FlagManager.canClaim(player, playerFaction))
                {
                    World world = player.getWorld();
                    Vector3i chunk = player.getLocation().getChunkPosition();

                    Optional<Faction> optionalChunkFaction = FactionLogic.getFactionByChunk(world.getUniqueId(), chunk);

                    if (optionalChunkFaction.isPresent())
                    {
                        Faction chunkFaction = optionalChunkFaction.get();

                        if (chunkFaction.Name.equals(playerFaction.Name))
                        {
                            if (optionalChunkFaction.get().Home != null)
                            {
                                if (world.getUniqueId().equals(optionalChunkFaction.get().Home.WorldUUID))
                                {
                                    Location homeLocation = world.getLocation(optionalChunkFaction.get().Home.BlockPosition);

                                    if(homeLocation.getChunkPosition().toString().equals(player.getLocation().getChunkPosition().toString())) FactionLogic.setHome(world.getUniqueId(), optionalChunkFaction.get(), null);
                                }
                            }

                            FactionLogic.removeClaim(optionalChunkFaction.get(), world.getUniqueId() ,chunk);

                            player.sendMessage(Text.of(PluginInfo.PluginPrefix, PluginMessages.LAND_HAS_BEEN_SUCCESSFULLY + " ", TextColors.GOLD, PluginMessages.UNCLAIMED, TextColors.WHITE, "!"));
                            return CommandResult.success();
                        }
                        else
                        {
                            player.sendMessage(Text.of(PluginInfo.ErrorPrefix, PluginMessages.THIS_PLAYER_IS_NOT_IN_YOUR_FACTION));
                        }
                    }
                    else
                    {
                        source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.THIS_PLACE_IS_ALREADY_CLAIMED));
                    }
                }
                else
                {
                    source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.PLAYERS_WITH_YOUR_RANK_CANT_UNCLAIM_LANDS));
                }
            }
            else
            {
                source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.YOU_MUST_BE_IN_FACTION_IN_ORDER_TO_USE_THIS_COMMAND));
            }
        }
        else
        {
            source.sendMessage(Text.of(PluginInfo.ErrorPrefix, TextColors.RED, PluginMessages.ONLY_IN_GAME_PLAYERS_CAN_USE_THIS_COMMAND));
        }

        return CommandResult.success();
    }
}
