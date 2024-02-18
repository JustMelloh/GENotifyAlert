package com.GENotifier;


import java.awt.BorderLayout;
import javax.inject.Inject;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import lombok.Getter;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.materialtabs.MaterialTab;
import net.runelite.client.ui.components.materialtabs.MaterialTabGroup;

class GrandExchangePanel extends PluginPanel
{

	// this panel will hold either the ge search panel or the ge offers panel
	private final JPanel display = new JPanel();

	private final MaterialTabGroup tabGroup = new MaterialTabGroup(display);
	private final MaterialTab searchTab;

	@Getter
	private final GESearchPanel searchPanel;
	@Getter
	private final GEOffersPanel alertsPanel;

	@Inject
	private GrandExchangePanel(GESearchPanel searchPanel, GrandExchangeOffersPanel alertsPanel)
	{
		super(false);

		this.searchPanel = searchPanel;
		this.alertsPanel = offersPanel;

		setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);

		MaterialTab alertsTab = new MaterialTab("Alert Items", tabGroup, alertsPanel);
		searchTab = new MaterialTab("Search", tabGroup, searchPanel);

		tabGroup.setBorder(new EmptyBorder(5, 0, 0, 0));
		tabGroup.addTab(alertsTab);
		tabGroup.addTab(searchTab);
		tabGroup.select(alertsTab); // selects the default selected tab

		add(tabGroup, BorderLayout.NORTH);
		add(display, BorderLayout.CENTER);
	}

	void showSearch()
	{
		if (searchPanel.isShowing())
		{
			return;
		}

		tabGroup.select(searchTab);
		revalidate();
	}
}