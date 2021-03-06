package io.github.aquerr.eaglefactions.common.listeners;

import com.google.common.collect.ImmutableMap;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.config.PowerConfig;
import io.github.aquerr.eaglefactions.api.config.ProtectionConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import io.github.aquerr.eaglefactions.common.messaging.MessageLoader;
import io.github.aquerr.eaglefactions.common.messaging.Messages;
import io.github.aquerr.eaglefactions.common.messaging.Placeholders;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.entity.DestructEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class PlayerDeathListener extends AbstractListener
{
    private final FactionsConfig factionsConfig;
    private final ProtectionConfig protectionConfig;
    private final PowerConfig powerConfig;

    public PlayerDeathListener(final EagleFactions plugin)
    {
        super(plugin);
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        this.powerConfig = plugin.getConfiguration().getPowerConfig();
        this.protectionConfig = plugin.getConfiguration().getProtectionConfig();
    }

    @Listener(order = Order.POST)
    public void onPlayerDeath(final DestructEntityEvent.Death event, final @Getter("getTargetEntity") Player player)
    {
        CompletableFuture.runAsync(() -> super.getPlugin().getPowerManager().decreasePower(player.getUniqueId()))
                .thenRun(() -> player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, MessageLoader.parseMessage(Messages.YOUR_POWER_HAS_BEEN_DECREASED_BY, TextColors.RESET, ImmutableMap.of(Placeholders.NUMBER, Text.of(TextColors.GOLD, this.powerConfig.getPowerDecrement()))), "\n",
                TextColors.GRAY, Messages.CURRENT_POWER + " ", super.getPlugin().getPowerManager().getPlayerPower(player.getUniqueId()) + "/" + super.getPlugin().getPowerManager().getPlayerMaxPower(player.getUniqueId()))));

        final Optional<Faction> optionalChunkFaction = super.getPlugin().getFactionLogic().getFactionByChunk(player.getWorld().getUniqueId(), player.getLocation().getChunkPosition());
        if (this.protectionConfig.getWarZoneWorldNames().contains(player.getWorld().getName()) || (optionalChunkFaction.isPresent() && optionalChunkFaction.get().isWarZone()))
        {
            super.getPlugin().getPlayerManager().setDeathInWarZone(player.getUniqueId(), true);
        }

        if (this.factionsConfig.shouldBlockHomeAfterDeathInOwnFaction())
        {
            final Optional<Faction> optionalPlayerFaction = super.getPlugin().getFactionLogic().getFactionByPlayerUUID(player.getUniqueId());
            if (optionalChunkFaction.isPresent() && optionalPlayerFaction.isPresent() && optionalChunkFaction.get().getName().equals(optionalPlayerFaction.get().getName()))
            {
                super.getPlugin().getAttackLogic().blockHome(player.getUniqueId());
            }
        }

        if(super.getPlugin().getPVPLogger().isActive() && super.getPlugin().getPVPLogger().isPlayerBlocked(player))
        {
            super.getPlugin().getPVPLogger().removePlayer(player);
        }
    }
}
