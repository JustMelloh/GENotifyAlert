package com.GENotifier;

//Import Libraries
import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;


public class GEAlerts extends PluginPanel {
    // This panel will hold either the GE Search or the Items that have been selected to be alerted when the price reaches a certain value
    private final JPanel display = new JPanel();

    private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);
    private final MaterialTab searchTab;

    
}
