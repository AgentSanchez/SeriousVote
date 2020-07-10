package net.adamsanchez.seriousvote.integration;

import com.magitechserver.magibridge.MagiBridge;
import com.magitechserver.magibridge.config.FormatType;
import com.magitechserver.magibridge.discord.DiscordMessageBuilder;
import net.adamsanchez.seriousvote.SeriousVote;
import net.dv8tion.jda.api.entities.TextChannel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public class MagiBridgeAPI {

    public static boolean isEnabled;
    public static String channel;

    public static void initialize(PluginContainer instance) {
        if (!isEnabled) {
            instance.getLogger().info("MagiBridge integration disabled!! Skipping...");
            return;
        }
        if (!Sponge.getPluginManager().getPlugin("magibridge").isPresent()) {
            instance.getLogger().warn("MagiBridge not found, support disabled!");
            isEnabled = false;
            return;
        }
        instance.getLogger().info("MagiBridge loaded in! Enabling Seriousvote integration!!");
        isEnabled = true;
    }

    public static void checkValidityChannel() {
        if (channel == null || channel.isEmpty() || !getChannel(channel).isPresent()) {
            channel = MagiBridge.getInstance().getConfig().CHANNELS.MAIN_CHANNEL;
            if (channel == null || channel.isEmpty() || !getChannel(channel).isPresent()) {
                SeriousVote.getInstance().getLogger().warn("Valid channel not found, disabling");
                isEnabled = false;
            }
        }
    }

    public static void makeBroadCast(Text text) {
        checkValidityChannel();
        DiscordMessageBuilder.forChannel(channel)
                .placeholder("message", text.toPlain())
                .format(FormatType.of(() -> "%message%"))
                .useWebhook(false)
                .send();
    }

    public static Optional<TextChannel> getChannel(@NonNull String id) {
        return Optional.ofNullable(
                MagiBridge.getInstance().getJDA().
                        getTextChannelById(id));
    }
}

