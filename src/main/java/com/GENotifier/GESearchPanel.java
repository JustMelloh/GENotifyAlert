package com.GENotifier;

// Import Java Libraries
import com.google.common.base.Strings;
import com.google.inject.Inject;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.ScheduledExecutorService;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.util.ArrayList;
import java.util.List;
import javax.swing.border.EmptyBorder;

import net.runelite.api.ItemComposition;

// Import RuneLite Libraries

import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.grandexchange.GrandExchangePlugin;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.components.IconTextField;
import net.runelite.client.ui.components.PluginErrorPanel;
import net.runelite.client.util.AsyncBufferedImage;
import net.runelite.http.api.item.ItemPrice;
import net.runelite.http.api.item.ItemStats;
import net.runelite.client.util.AsyncBufferedImage;


public class GESearchPanel extends JPanel{
    private static final String ERROR_PANEL = "ERROR_PANEL";
	private static final String RESULTS_PANEL = "RESULTS_PANEL";
	private static final int MAX_SEARCH_ITEMS = 100;

	private final GridBagConstraints constraints = new GridBagConstraints();
	private final CardLayout cardLayout = new CardLayout();

	private final ClientThread clientThread;
	private final ItemManager itemManager;
	private final ScheduledExecutorService executor;
	private final RuneLiteConfig runeLiteConfig;
	private final GrandExchangePlugin GENotifier;

    private final IconTextField searchBar = new IconTextField();

    // Results will hold individual GE item panels

    private final JPanel searchItemsPanel = new JPanel();

    // The center panel holds either the error panel or the results

    private final JPanel centerPanel = new JPanel(cardLayout);

    // The error panel is displayed when there is an error

    private final PluginErrorPanel errorPanel = new PluginErrorPanel();

    private final List<GEItems> itemsList = new ArrayList<>();


    @Inject
    private GESearchPanel(ClientThread clientThread, ItemManager itemManager, ScheduledExecutorService executor, RuneLiteConfig runeLiteConfig, GrandExchangePlugin GENotifier)
    {
        this.clientThread = clientThread;
        this.itemManager = itemManager;
        this.executor = executor;
        this.runeLiteConfig = runeLiteConfig;
        this.GENotifier = GENotifier;

        setLayout(new BorderLayout());
		setBackground(ColorScheme.DARK_GRAY_COLOR);


        // This is the main Container it holds the search bar and center panel.


        JPanel container = new JPanel();
		container.setLayout(new BorderLayout(5, 5));
		container.setBorder(new EmptyBorder(10, 10, 10, 10));
		container.setBackground(ColorScheme.DARK_GRAY_COLOR);

        searchBar.setIcon(IconTextField.Icon.SEARCH);
        searchBar.setPreferredSize(new Dimension(100, 30));
        searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        searchBar.setHoverBackgroundColor(ColorScheme.DARK_GRAY_HOVER_COLOR);
        searchBar.addActionListener(e -> executor.execute(() -> priceLookup(false)));

        searchItemsPanel.setLayout(new GridBagLayout());
        searchItemsPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);

        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;
        constraints.weighty = 0;
        constraints.gridx = 0;
        constraints.gridy = 0;
        /* This panel Wraps the results and guarantees scrolling */

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        wrapper.add(searchItemsPanel, BorderLayout.NORTH);

        /* The results wrapper, this scrolling panel wraps the results container */

        JScrollPane resultsWrapper = new JScrollPane(wrapper);
        resultsWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        resultsWrapper.getVerticalScrollBar().setPreferredSize(new Dimension(12, 0));
        resultsWrapper.getVerticalScrollBar().setBorder(new EmptyBorder(0, 5, 0, 0));
        resultsWrapper.setVisible(false);

        /* This panel wraps the error panel and limits the height */

        JPanel errorWrapper = new JPanel(new BorderLayout());
        errorWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
        errorWrapper.add(errorPanel, BorderLayout.NORTH);

        errorPanel.setContent("GE Notifier Search",
                "Search for an item to be alerted when it reaches a certain price.");
        
        centerPanel.add(resultsWrapper, RESULTS_PANEL);
        centerPanel.add(errorWrapper, ERROR_PANEL);
        
        cardLayout.show(centerPanel, ERROR_PANEL);

        container.add(searchBar, BorderLayout.NORTH);
        container.add(centerPanel, BorderLayout.CENTER);

        add(container, BorderLayout.CENTER);

    }

    void priceLookup (String item) {
        searchBar.setText(item);
        executor.execute(() -> priceLookup(true));
    }

    private boolean updateSearch() {
        String lookup = searchBar.getText();

        if (Strings.isNullOrEmpty(lookup)) {
            searchItemsPanel.removeAll();
            SwingUtilities.invokeLater(searchItemsPanel::updateUI);
            return false;
        }

        // Input is not empty, add searching label
		searchItemsPanel.removeAll();
		searchBar.setBackground(ColorScheme.DARKER_GRAY_COLOR);
		searchBar.setEditable(false);
		searchBar.setIcon(IconTextField.Icon.LOADING);
		return true;
	}

    private void priceLookup(boolean exactMatch){

        if (!updateSearch()) {
            return;
        }

        List<ItemPrice> result = itemManager.search(searchBar.getText());
        if (result.isEmpty()){
            searchBar.setIcon(IconTextField.Icon.ERROR);
            errorPanel.setContent("No results returned.", "No items were found with that name, try again.");
            cardLayout.show(centerPanel, ERROR_PANEL);
            searchBar.setEditable(true);
            return;
        }
        /* Move to Client thread to lookup item compositions */

        clientThread.invokeLater(() -> processResult(result, searchBar.getText(), exactMatch));
    }

    private void processResult(List<ItemPrice> result, String lookup, boolean exactMatch){
        itemsList.clear();

        cardLayout.show(centerPanel, RESULTS_PANEL);

        int count = 0;

        boolean useActivelyTradedPrice = runeLiteConfig.useWikiItemPrices();
        

        for(ItemPrice item : result){
            if (count ++ > MAX_SEARCH_ITEMS){
                break;
            }

            int itemId = item.getId();

            ItemComposition itemComp = itemManager.getItemComposition(itemId);
            ItemStats itemStats = itemManager.getItemStats(itemId, false);

            int itemPrice = useActivelyTradedPrice ? itemManager.getWikiPrice(item) : item.getPrice();
            int itemLimit = itemStats != null ? itemStats.getGeLimit() : 0;
            final int haPrice = itemComp.getHaPrice();
            AsyncBufferedImage itemImage = itemManager.getImage(itemId);

            itemsList.add(new GEItems(itemImage, item.getName(), itemId, itemPrice, haPrice, itemLimit));

            if(exactMatch && item.getName().equalsIgnoreCase(lookup)){
                break;
            }
        }

        SwingUtilities.invokeLater(() -> {
            int index = 0;
            for (GEItems item : itemsList){
                GEItemPanel panel = new GEItemPanel(GENotifier, item.getIcon(), item.getName(), item.getItemId(), item.getGePrice(), item.getHaPrice(), item.getItemLimit());


                /* Add first item directly and wrap the rest in margin */

                if (index++ > 0){
                    JPanel marginWrapper = new JPanel(new BorderLayout());
                    marginWrapper.setBackground(ColorScheme.DARKER_GRAY_COLOR);
                    marginWrapper.setBorder(new EmptyBorder(5, 0, 0, 0));
                    marginWrapper.add(panel, BorderLayout.NORTH);
                    searchItemsPanel.add(marginWrapper, constraints);
                } else {
                    searchItemsPanel.add(panel, constraints);
                }
                constraints.gridy++;
            } 

            /* If exactMatch was set then it came from app */

            if (!exactMatch){
                searchItemsPanel.requestFocusInWindow();
            }
            searchBar.setEditable(true);

            // Remove search label

            if (!itemsList.isEmpty()){
                searchBar.setIcon(IconTextField.Icon.SEARCH);
            }
            });

            
        }
    }



