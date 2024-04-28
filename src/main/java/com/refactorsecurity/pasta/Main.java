package com.refactorsecurity.pasta;

import burp.api.montoya.BurpExtension;
import burp.api.montoya.MontoyaApi;
import burp.api.montoya.extension.ExtensionUnloadingHandler;
import burp.api.montoya.logging.Logging;
import burp.api.montoya.ui.UserInterface;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.*;

public class Main implements BurpExtension {

    private final static String EXTENSION_NAME = "Pasta";
    private UserInterface userInterface;
    private Table table;
    private Logging logging;

    @Override
    public void initialize(MontoyaApi api) {
        api.extension().setName(EXTENSION_NAME);
        this.userInterface = api.userInterface();

        // Instantiate table and table model
        TableModel tableModel = new TableModel();
        this.table = new Table(tableModel);

        // Add menu items
        this.userInterface
                .registerContextMenuItemsProvider(new MenuItems(tableModel));

        // Load tab
        this.loadTab();

        // Print message to the stdout
        this.logging = api.logging();
        this.logging.logToOutput(EXTENSION_NAME + " was loaded successfully.");
    }

    private void loadTab() {
        // Panel
        JPanel panel = new JPanel(new BorderLayout());

        // Main label
        JLabel label = new JLabel("Find below all saved entries. Double-click on any cell to edit.");
        label.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Table
        Table t = this.table;
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = t.rowAtPoint(e.getPoint());
                int column = t.columnAtPoint(e.getPoint());

                // Delete row
                if (e.getClickCount() == 1 && column == 3) {
                    // Show confirmation dialog
                    int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this entry?",
                            "Confirmation", JOptionPane.YES_NO_OPTION);
                    if (choice == JOptionPane.YES_OPTION) {
                        Object name = t.model.getValueAt(row, 0);
                        if (name != null) {
                            t.model.deleteEntryByName(name.toString());
                        }
                    }
                }
            }
        });
        // Create a TableRowSorter for the table model
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(this.table.model);
        this.table.setRowSorter(sorter);
        this.table.setPreferredScrollableViewportSize(this.table.getPreferredSize());

        // scroll pane
        JScrollPane scrollPane = new JScrollPane(this.table);

        // Add a button to add a new empty row
        JButton addRowButton = new JButton("Add Entry");
        addRowButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Add a new empty row to the table
                t.model.addNewEntry();
            }
        });

        // Add a button to delete all rows
        JButton deleteAllButton = new JButton("Delete All Entries");
        deleteAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int choice = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete all entries?",
                        "Confirmation", JOptionPane.YES_NO_OPTION);
                if (choice == JOptionPane.YES_OPTION) {
                    t.model.deleteAllEntries();
                }
            }
        });

        // Add a button to export entries
        JButton exportButton = new JButton("Export Entries");
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExportImport.exportEntries(t);
            }
        });

        // Add a button to import entries
        JButton importButton = new JButton("Import Entries");
        importButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ExportImport.importEntries(t.model);
            }
        });

        // Add bottom panel
        JPanel bottomPanel = new JPanel(new GridLayout(1, 3));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        bottomPanel.add(addRowButton);
        bottomPanel.add(deleteAllButton);
        bottomPanel.add(importButton);
        bottomPanel.add(exportButton);

        // Calculate preferred width based on the widest button
        int preferredWidth = Math.max(
                Math.max(addRowButton.getPreferredSize().width, deleteAllButton.getPreferredSize().width),
                Math.max(importButton.getPreferredSize().width, exportButton.getPreferredSize().width));

        // Set preferred size for all buttons
        Dimension preferredSize = new Dimension(preferredWidth, addRowButton.getPreferredSize().height);
        addRowButton.setPreferredSize(preferredSize);
        deleteAllButton.setPreferredSize(preferredSize);
        importButton.setPreferredSize(preferredSize);
        exportButton.setPreferredSize(preferredSize);

        // Add all components to panel
        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Add tab
        this.userInterface.registerSuiteTab(EXTENSION_NAME, panel);
    }

    private class ExtensionUnloadHandler implements ExtensionUnloadingHandler {
        @Override
        public void extensionUnloaded() {
            logging.logToOutput(EXTENSION_NAME + " was unloaded successfully.");
        }
    }
}