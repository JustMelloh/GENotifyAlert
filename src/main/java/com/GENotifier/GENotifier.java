
// Import Packages
package com.GENotifier;

// Import Libraries


import com.google.inject.Provides;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.config.ConfigManager;



@PluginDescriptor(
        name = "GENotifier",
        description = "Notifies when an Item you're looking to sell is below a certain price.",
        tags = {"grand", "exchange", "notifier", "price", "alert", "sell", "buy", "trade", "item", "items", "ge", "grandexchange", "grand exchange"}
)
public class GENotifier extends Plugin {
    // Inject the Runelite Client

    @Inject Client client;
    @Inject GENotifierConfig config;

    // Provides the config

    @Provides 
    GENotifierConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(GENotifierConfig.class);
    }

    // On Start

    @Override
    protected void startUp() throws Exception {
        
    }
}
