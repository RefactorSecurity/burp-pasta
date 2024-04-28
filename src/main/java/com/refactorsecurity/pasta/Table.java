package com.refactorsecurity.pasta;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

public class Table extends JTable {
    TableModel model;

    public Table(TableModel model) {
        super(model);
        this.model = model;
    }

    @Override
    public TableCellRenderer getCellRenderer(int row, int column) {
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        if (column == 3) { // Delete
            // Center align the content
            renderer.setHorizontalAlignment(SwingConstants.CENTER);
        } 
        return renderer;
    }
}