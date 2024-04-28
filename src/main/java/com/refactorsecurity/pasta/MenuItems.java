package com.refactorsecurity.pasta;

import burp.api.montoya.core.Range;
import burp.api.montoya.core.ToolType;
import burp.api.montoya.http.message.requests.HttpRequest;
import burp.api.montoya.ui.contextmenu.ContextMenuEvent;
import burp.api.montoya.ui.contextmenu.ContextMenuItemsProvider;
import burp.api.montoya.ui.contextmenu.MessageEditorHttpRequestResponse;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MenuItems implements ContextMenuItemsProvider {
    private final TableModel tableModel;

    public MenuItems(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    @Override
    public List<Component> provideMenuItems(ContextMenuEvent event) {
        List<Component> menuItemList = new ArrayList<>();

        // Retrieve all names stored in table to dynamically generate all options
        List<String> nameList = this.tableModel.getNames();

        // Include "Paste" menu items if applicable
        if (event.isFromTool(ToolType.INTRUDER, ToolType.REPEATER, ToolType.PROXY)) {
            // Skip if tool type is Repeater and right-clicking on the response
            if (! (event.toolType() == ToolType.REPEATER && event.messageEditorRequestResponse().isPresent()
                    && event.messageEditorRequestResponse().get().selectionContext().toString().equals("RESPONSE"))) {
                for (String name : nameList) {
                    // Add "Paste" menu items for current values
                    JMenuItem pasteItem = new JMenuItem("Paste '" + name + "'");
                    pasteItem.addActionListener(l -> clickOnPaste(name, event));
                    menuItemList.add(pasteItem);
                }
            }
        }

        // Include "Save" menu if applicable
        if (event.isFromTool(ToolType.PROXY, ToolType.INTRUDER, ToolType.REPEATER, ToolType.LOGGER)) {
            JMenu copyMenu = new JMenu("Save");
            for (String name : nameList) {
                // Add "Replace" menu items for current values
                JMenuItem copyItem = new JMenuItem("Save as '" + name + "'");
                copyItem.addActionListener(l -> clickOnSave(name, event));
                copyMenu.add(copyItem);
            }

            // Add "Save as..." menu item for new values
            JMenuItem copyItem = new JMenuItem("Save as...");
            copyItem.addActionListener(l -> clickOnSave("NEW_VALUE_TO_BE_SAVED", event));
            copyMenu.add(copyItem);

            menuItemList.add(copyMenu);
        }

        return menuItemList;
    }

    private void clickOnSave(String name, ContextMenuEvent event) {
        if (event.messageEditorRequestResponse().isPresent()) {
            // Retrieve selected text
            MessageEditorHttpRequestResponse editor = event.messageEditorRequestResponse().get();
            Optional<Range> range = editor.selectionOffsets();
            if (range.isEmpty()) {
                JOptionPane.showMessageDialog(null, "No value was selected");
            } else {
                int startIndex = range.get().startIndexInclusive();
                int endIndex = range.get().endIndexExclusive();
                String str;
                if (editor.selectionContext().toString().equals("REQUEST")) {
                    str = editor.requestResponse().request().toString();
                } else {
                    str = editor.requestResponse().response().toString();
                }
                String selectedText = str.substring(startIndex, endIndex);

                // Save the value. If not replacing previous value, ask for a name
                if (name.equals("NEW_VALUE_TO_BE_SAVED")) {
                    String newName = JOptionPane.showInputDialog(null, "Enter a name:");
                    // User does not submit any name
                    while (newName.isEmpty()) {
                        JOptionPane.showMessageDialog(null, "No name was selected. Please select a name");
                        newName = JOptionPane.showInputDialog(null, "Enter a name:");
                    }
                    // User does not cancel
                    if (!newName.equals("null")) {
                        this.tableModel.addOrUpdateEntry(newName, selectedText, "");
                    }
                } else {
                    this.tableModel.addOrUpdateEntry(name, selectedText);
                }
            }
        } else {
            JOptionPane.showMessageDialog(null, "No value was selected");
        }
    }

    private void clickOnPaste(String name, ContextMenuEvent event) {
        if (event.messageEditorRequestResponse().isPresent()) {
            MessageEditorHttpRequestResponse editor = event.messageEditorRequestResponse().get();
            String req = editor.requestResponse().request().toString();
            String valueToPaste = this.tableModel.getValueByName(name);
            String newReq;

            // Replace selected text with stored value
            Optional<Range> range = editor.selectionOffsets();
            if (range.isPresent()) {
                int startIndex = range.get().startIndexInclusive();
                int endIndex = range.get().endIndexExclusive();
                newReq = req.substring(0, startIndex) + valueToPaste + req.substring(endIndex);
            } else {
                // Paste stored value on caret position
                int caretPosition = editor.caretPosition();
                newReq = req.substring(0, caretPosition) + valueToPaste + req.substring(caretPosition);
            }
            editor.setRequest(HttpRequest.httpRequest(newReq));
        }
    }
}