package io.github.aquerr.eaglefactions.common.listeners;

import com.flowpowered.math.vector.Vector3d;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.managers.ProtectionResult;
import io.github.aquerr.eaglefactions.common.PluginInfo;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.living.ArmorStand;
import org.spongepowered.api.entity.living.Living;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.entity.InteractEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.event.item.inventory.InteractItemEvent;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Optional;

public class PlayerInteractListener extends AbstractListener
{
    public PlayerInteractListener(EagleFactions plugin)
    {
        super(plugin);
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onItemUse(final InteractItemEvent event, @Root final Player player)
    {
        if (event.getItemStack() == ItemStackSnapshot.NONE)
            return;

        final Vector3d interactionPoint = event.getInteractionPoint().orElse(player.getLocation().getPosition());
        Location<World> location = new Location<>(player.getWorld(), interactionPoint);

        //Handle hitting entities
        boolean hasHitEntity = event.getContext().containsKey(EventContextKeys.ENTITY_HIT);
        if(hasHitEntity)
        {
            final Entity hitEntity = event.getContext().get(EventContextKeys.ENTITY_HIT).get();
            if (hitEntity instanceof Living && !(hitEntity instanceof ArmorStand))
                return;

            location = hitEntity.getLocation();
        }

        final ProtectionResult protectionResult = super.getPlugin().getProtectionManager().canUseItem(location, player, event.getItemStack(), true);
        if (!protectionResult.hasAccess())
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onEntityInteract(final InteractEntityEvent event, @Root final Player player)
    {
        final Entity targetEntity = event.getTargetEntity();
        final Optional<Vector3d> optionalInteractionPoint = event.getInteractionPoint();

        if((targetEntity instanceof Living) && !(targetEntity instanceof ArmorStand))
            return;

        final Vector3d blockPosition = optionalInteractionPoint.orElseGet(() -> targetEntity.getLocation().getPosition());
        final Location<World> location = new Location<>(targetEntity.getWorld(), blockPosition);
        boolean canInteractWithEntity = super.getPlugin().getProtectionManager().canInteractWithBlock(location, player, true).hasAccess();
        if(!canInteractWithEntity)
        {
            event.setCancelled(true);
            return;
        }
    }

    @Listener(order = Order.FIRST, beforeModifications = true)
    public void onBlockInteract(final InteractBlockEvent event, @Root final Player player)
    {
        //If AIR or NONE then return
        if (event.getTargetBlock() == BlockSnapshot.NONE || event.getTargetBlock().getState().getType() == BlockTypes.AIR)
            return;

        final Optional<Location<World>> optionalLocation = event.getTargetBlock().getLocation();
        if (!optionalLocation.isPresent())
            return;

        final Location<World> blockLocation = optionalLocation.get();

        final ProtectionResult protectionResult = super.getPlugin().getProtectionManager().canInteractWithBlock(blockLocation, player, true);
        if (!protectionResult.hasAccess())
        {
            event.setCancelled(true);
            return;
        }
        else
        {
            if(event instanceof InteractBlockEvent.Secondary && protectionResult.isEagleFeather())
                removeEagleFeather(player);
        }
    }

    private void removeEagleFeather(final Player player)
    {
        final ItemStack feather = player.getItemInHand(HandTypes.MAIN_HAND).get();
        feather.setQuantity(feather.getQuantity() - 1);
        player.sendMessage(Text.of(PluginInfo.PLUGIN_PREFIX, TextColors.DARK_PURPLE, "You used Eagle's Feather!"));
    }
}
