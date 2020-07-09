package net.adamsanchez.seriousvote.integration;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class MagiBridgeAPI {

    public static boolean isEnabled;

    public static void initialize(PluginContainer instance) {
        if(!isEnabled) {
            instance.getLogger().info("MagiBridge integration disabled!! Skipping...");
            return;
        }
        try {
            Sponge.getServiceManager().provideUnchecked(MagiBridge.class);
            instance.getLogger().info("MagiBridge loaded in! Enabling Seriousvote integration!!");
        } catch (NoClassDefFoundError e) {
            instance.getLogger().warn("MagiBridge not found, support disabled!");
            isEnabled = false;
            return;
        }

        isEnabled = true;
    }

    public static void makeBroadCast(Text text){
        String channel = MagiBridge.getInstance().getConfig().CHANNELS.MAIN_CHANNEL;
        FormatType format = FormatType.SERVER_TO_DISCORD_FORMAT;
        DiscordMessageBuilder.forChannel(channel)
                .placeholder("message", TextSerializers.FORMATTING_CODE.serialize(text))
                .format(format)
                .send();
    }
}

