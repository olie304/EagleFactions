package io.github.aquerr.eaglefactions.common.messaging;

import com.google.inject.Singleton;
import io.github.aquerr.eaglefactions.api.EagleFactions;
import io.github.aquerr.eaglefactions.api.config.FactionsConfig;
import io.github.aquerr.eaglefactions.api.entities.Faction;
import io.github.aquerr.eaglefactions.common.EagleFactionsPlugin;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.asset.Asset;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Singleton
public class MessageLoader
{
    private final EagleFactions plugin;
    private final FactionsConfig factionsConfig;

    private static MessageLoader instance = null;

    private Locale locale = Locale.getDefault();

    public static MessageLoader getInstance(EagleFactions plugin)
    {
        if (instance == null)
            return new MessageLoader(plugin);
        return instance;
    }

    private MessageLoader(final EagleFactions plugin)
    {
        instance = this;
        this.plugin = plugin;
        this.factionsConfig = plugin.getConfiguration().getFactionsConfig();
        Path configDir = plugin.getConfigDir();
        String messagesFileName = this.factionsConfig.getLanguageFileName();
        Path messagesFilePath = configDir.resolve("messages").resolve(messagesFileName);

        if (Files.notExists(configDir.resolve("messages")))
        {
            try
            {
                Files.createDirectory(configDir.resolve("messages"));
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        Optional<Asset> optionalMessagesFile = Sponge.getAssetManager().getAsset(plugin, "messages/" + messagesFileName);
        if (optionalMessagesFile.isPresent())
        {
            try
            {
                optionalMessagesFile.get().copyToFile(messagesFilePath, false, true);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            optionalMessagesFile = Sponge.getAssetManager().getAsset(plugin, "messages/english.conf");
            optionalMessagesFile.ifPresent(x->
            {
                try
                {
                    x.copyToFile(messagesFilePath, false, true);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            });
        }

        final ConfigurationLoader<CommentedConfigurationNode> configLoader = HoconConfigurationLoader.builder().setPath(messagesFilePath).build();
        ConfigurationNode configNode;

        try
        {
            configNode = configLoader.load(ConfigurationOptions.defaults().setShouldCopyDefaults(true));
            loadPluginMessages(configNode, configLoader);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void loadPluginMessages(ConfigurationNode configNode, ConfigurationLoader<CommentedConfigurationNode> configLoader)
    {
        Field[] messageFields = Messages.class.getFields();
        boolean missingNodes = false;

        for (Field messageField : messageFields)
        {
            Object object = configNode.getNode(messageField.getName()).getString("MISSING_MESSAGE");

            if (object.equals("MISSING_MESSAGE"))
            {
                missingNodes = true;
            }

            String message = object.toString();

            try
            {
                messageField.set(Messages.class.getClass(), message);
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        if (missingNodes)
        {
            try
            {
                configLoader.save(configNode);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    public static String parseMessage(String message, Object supplier)
    {
        String result = message;
        if (supplier instanceof Faction)
        {
            Faction faction = (Faction) supplier;
            result = result.replace(Placeholders.FACTION_NAME.getPlaceholder(), faction.getName());
        }
        else if (supplier instanceof User)
        {
            User user = (User) supplier;
            result = result.replace(Placeholders.PLAYER_NAME.getPlaceholder(), user.getName());
            result = result.replace(Placeholders.POWER.getPlaceholder(), String.valueOf(EagleFactionsPlugin.getPlugin().getPowerManager().getPlayerPower(user.getUniqueId())));
        }
        else if (supplier instanceof String)
        {
            for (final Placeholder placeholder : Placeholders.PLACEHOLDERS)
            {
                result = result.replace(placeholder.getPlaceholder(), (String) supplier);
            }
        }
        else if (supplier instanceof Integer)
        {
            result = result.replace(Placeholders.NUMBER.getPlaceholder(), String.valueOf(supplier));
        }
        return result;
    }

    public static Text parseMessage(final String message, final Map<Placeholder, Text> supplierMap)
    {
        final Text.Builder resultText = Text.builder();
        final String[] splitMessage = message.split(" ");
        for (final String word : splitMessage)
        {
            final Text.Builder textBuilder = Text.builder();
            for (final Map.Entry<Placeholder, Text> mapEntry : supplierMap.entrySet())
            {
                if (word.contains(mapEntry.getKey().getPlaceholder()))
                {
                    final String filledPlaceholder = word.replace(mapEntry.getKey().getPlaceholder(), mapEntry.getValue().toPlain() + " ");
                    resultText.append(TextSerializers.FORMATTING_CODE.deserialize(filledPlaceholder));
                }
                resultText.append(textBuilder.build());
            }

            resultText.append(Text.of(word + " "));
        }
        return resultText.build();
    }
}