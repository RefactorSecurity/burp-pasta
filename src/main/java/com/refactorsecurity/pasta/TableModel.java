package com.refactorsecurity.pasta;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableModel extends AbstractTableModel {
    private final String[] columnNames = { "Name", "Value", "Comment", "Delete" };
    private final Map<String, String[]> data = new HashMap<>();

    @Override
    public int getRowCount() {
        return this.data.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        // Columns 0,1,2 are editable (Name, Value, Comment)
        return column < 3;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        String name = (String) this.data.keySet().toArray()[rowIndex];
        String[] values = this.data.get(name);
        String value = values[0];
        String comment = values[1];

        return switch (columnIndex) {
            case 0 -> name; // Name
            case 1 -> value; // Value
            case 2 -> comment; // Comment
            case 3 -> "❌"; // Delete
            default -> null;
        };
    }

    @Override
    public void setValueAt(Object newValue, int rowIndex, int columnIndex) {
        String currentName = (String) this.data.keySet().toArray()[rowIndex];
        String[] values = this.data.get(currentName);
        String currentValue = values[0];
        String currentComment = values[1];

        // Identify which column was updated
        switch (columnIndex) {
            case 0: // Name
                if (currentName.equals(newValue)) {
                    // Ignore update if no changes were applied to name, otherwise the entry would be deleted
                    break;
                }
                this.addOrUpdateEntry(newValue.toString(), currentValue, currentComment);
                this.deleteEntryByName(currentName);
                break;
            case 1: // Value
                this.addOrUpdateEntry(currentName, newValue.toString(), currentComment);
                break;
            case 2: // Comment
                this.addOrUpdateEntry(currentName, currentValue, newValue.toString());
                break;
        }
        fireTableDataChanged();
    }

    // Add or update entry
    public void addOrUpdateEntry(String name, String value) {
        this.addOrUpdateEntry(name, value, this.getCommentByName(name));
    }

    // Add or update entry
    public void addOrUpdateEntry(String name, String value, String comment) {
        this.data.put(name, new String[] { value, comment });
        fireTableDataChanged(); // Notify listeners that data has changed
    }

    // Add new entry
    public void addNewEntry() {
        addOrUpdateEntry("add new name...", "add new value...", "add new comment...");
    }

    // Retrieve value associated to a name
    public String getValueByName(String name) {
        if (this.data.containsKey(name)) {
            return this.data.get(name)[0];
        } else {
            return "";
        }
    }

    // Retrieve comment associated to a name
    public String getCommentByName(String name) {
        if (this.data.containsKey(name)) {
            return this.data.get(name)[1];
        } else {
            return "";
        }
    }

    // List all names
    public List<String> getNames() {
        return new ArrayList<>(this.data.keySet());
    }

    // Delete entry associated to a name
    public void deleteEntryByName(String name) {
        this.data.remove(name);
        fireTableDataChanged(); // Notify listeners that data has changed
    }

    // Delete all entries
    public void deleteAllEntries() {
        this.data.clear();
        fireTableDataChanged(); // Notify listeners that data has changed
    }

}