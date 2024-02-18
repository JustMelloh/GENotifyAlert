package com.GENotifier;

import lombok.Value;
import net.runelite.client.util.AsyncBufferedImage;

@Value
class GEItems 
{
	private final AsyncBufferedImage icon;
	private final String name;
	private final int itemId;
	private final int gePrice;
	private final int haPrice;
    private final int itemLimit;
}