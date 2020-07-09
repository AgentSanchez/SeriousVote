package net.adamsanchez.seriousvote.integration;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Token;
import net.adamsanchez.seriousvote.SeriousVote;
import net.adamsanchez.seriousvote.api.SeriousVoteAPI;
import net.adamsanchez.seriousvote.utils.CC;
import net.adamsanchez.seriousvote.utils.U;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

public class MagiBridgeAPI {

    public static boolean isEnabled;
    private static MagiBridge magiBridge;

    public static void initialize(PluginContainer instance) {
        if(!isEnabled) {
            instance.getLogger().info("MagiBridge integration disabled!! Skipping...");
            return;
        }
        try {
            magiBridge = Sponge.getServiceManager().provideUnchecked(MagiBridge.class);
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

