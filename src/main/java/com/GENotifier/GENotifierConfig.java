package com.GENotifier;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;


@ConfigGroup("GENotifier")
public interface GENotifierConfig extends Config {
    @ConfigItem(
        keyName = "GENotify",
        name = "GENotify",
        description = "Notifies when an Item you're looking to sell is below a certain price."
    )
    void GENotify();
} 
