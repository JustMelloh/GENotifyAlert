package com.GENotifier;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;


public class GENotifierTest {

    public static void main(String[] args) throws Exception
    {
        ExternalPluginManager.loadBuiltin(GENotifier.class);
        RuneLite.main(args);
    }
}
