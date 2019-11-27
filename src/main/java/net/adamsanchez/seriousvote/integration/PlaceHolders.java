package net.adamsanchez.seriousvote.integration;

import me.rojo8399.placeholderapi.Placeholder;
import me.rojo8399.placeholderapi.PlaceholderService;
import me.rojo8399.placeholderapi.Source;
import net.adamsanchez.seriousvote.api.SeriousVoteAPI;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.plugin.PluginContainer;

public class PlaceHolders {

    public static PlaceHolders aThis;
    public static boolean apiLoaded;

    public static void initialize(PluginContainer instance){
        if (!Sponge.getServiceManager().isRegistered(PlaceholderService.class)) {
            instance.getLogger().warn("PlaceholderAPI not found, support disabled!");
            return;
        }
        PlaceholderService apiService = Sponge.getServiceManager().provideUnchecked(PlaceholderService.class);
        aThis = new PlaceHolders();
        apiService.loadAll(aThis, instance)
                .stream()
                .map(builder -> builder.author("seriousvote")
                        .plugin(instance)
                        .version("4.8.7")
                )
                .forEach(builder -> {
                    try {
                        builder.buildAndRegister();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
        apiLoaded = true;
    }

    @Placeholder(id = "player-votes")
    public String player_votes(@Source CommandSource sender) {
        if (sender != null){
            String.valueOf(SeriousVoteAPI.getPlayerTotalVotes(sender.getName()));
            return;
        }
        return String.valueOf(0);
    }
}

