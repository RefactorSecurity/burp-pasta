package com.refactorsecurity.pasta;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.exceptions.CsvValidationException;

import javax.swing.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ExportImport {

    // Export entries to a CSV file
    public static void exportEntries(JTable table) {
        try {
            // Open file chooser to let user select a file to save
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setApproveButtonText("Save");

            // Set the default file name
            fileChooser.setSelectedFile(new File("exported_values.csv"));

            // Quit export if user does not approve
            int userSelection = fileChooser.showSaveDialog(null);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            // Check if user has given a file extension, if not append .csv
            File file = fileChooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getParentFile(), file.getName() + ".csv");
            }
            String fileName = file.getName();

            CSVWriter csvWriter = new CSVWriter(new FileWriter(file));
            // Write column names to CSV file
            csvWriter.writeNext(new String[] { "Name", "Value", "Comment" });
            // Write entries to CSV file
            for (int i = 0; i < table.getRowCount(); i++) {
                String[] row = new String[] {
                        table.getValueAt(i, 0).toString(),
                        table.getValueAt(i, 1).toString(),
                        table.getValueAt(i, 2).toString()
                };
                csvWriter.writeNext(row);
            }
            csvWriter.close();
            JOptionPane.showMessageDialog(null, "Entries exported successfully to " + fileName);
        } catch (IOException ex) {
            // ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while exporting entries: " + ex.getMessage());
        }
    }

    // Import entries from a CSV file
    public static void importEntries(TableModel tableModel) {
        try {
            // Open file chooser to let user select a file to save
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select file to import");
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setApproveButtonText("Open");
            int userSelection = fileChooser.showOpenDialog(null);
            if (userSelection != JFileChooser.APPROVE_OPTION) {
                return;
            }

            // Process selected file
            File file = fileChooser.getSelectedFile();
            CSVReader reader = new CSVReader(new FileReader(file));
            // Discard first line with column names
            reader.readNext();
            // Process lines
            String[] nextLine;
            while ((nextLine = reader.readNext()) != null) {
                tableModel.addOrUpdateEntry(nextLine[0], nextLine[1], nextLine[2]);
            }
            JOptionPane.showMessageDialog(null, "Entries were imported successfully.");
        } catch (IOException | CsvValidationException ex) {
            // ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "An error occurred while importing enties.");
        }
    }
}
