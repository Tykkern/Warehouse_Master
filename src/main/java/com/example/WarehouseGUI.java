package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class WarehouseGUI extends JFrame {
    private final Warehouse warehouse = Warehouse.getInstance();
    private final JTextArea outputArea = new JTextArea(20, 40);
    private final JTextField idField = new JTextField(10);
    private final JTextField nameField = new JTextField(10);
    private final JTextField priceField = new JTextField(10);
    private final JTextField quantityField = new JTextField(10);
    private final JTextField extraField = new JTextField(10); // Для expiration или warranty
    private final JComboBox<String> typeCombo = new JComboBox<>(new String[]{"Food", "Electronics"});
    private final JTextField searchField = new JTextField(10);

    public WarehouseGUI() {
        setTitle("Warehouse Master");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Load data on start
        warehouse.loadData();

        // Buttons
        JButton addButton = new JButton("Add Product");
        addButton.addActionListener(new AddAction());
        JButton removeButton = new JButton("Remove Product");
        removeButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(idField.getText());
                warehouse.removeProduct(id);
                outputArea.append("Product removed: ID " + id + "\n");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Invalid ID format.");
            }
        });

        JButton listButton = new JButton("List Products");
        listButton.addActionListener(e -> {
            outputArea.setText("");
            warehouse.getAllProducts().forEach(p -> outputArea.append(p.toString() + "\n"));
        });

        JButton analyticsButton = new JButton("Analytics");
        analyticsButton.addActionListener(e -> {
            outputArea.setText("Total Value: " + warehouse.getTotalValue() + "\n");
            outputArea.append("Product Types: " + warehouse.getProductTypeCounts() + "\n");
        });

        JButton searchButton = new JButton("Search by Name");
        searchButton.addActionListener(e -> {
            String name = searchField.getText();
            outputArea.setText("");
            warehouse.searchByName(name).forEach(p -> outputArea.append(p.toString() + "\n"));
        });

        // Labels
        add(new JLabel("ID:"));
        add(idField);
        add(new JLabel("Name:"));
        add(nameField);
        add(new JLabel("Price:"));
        add(priceField);
        add(new JLabel("Quantity:"));
        add(quantityField);
        add(new JLabel("Type:"));
        add(typeCombo);
        add(new JLabel("Extra (Date YYYY-MM-DD or Months):"));
        add(extraField);

        add(addButton);
        add(removeButton);
        add(listButton);
        add(analyticsButton);

        add(new JLabel("Search Name:"));
        add(searchField);
        add(searchButton);

        add(new JScrollPane(outputArea));

        // Shutdown hook
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                warehouse.shutdown();
            }
        });

        setVisible(true);
    }

    private class AddAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int id = Integer.parseInt(idField.getText());
                String name = nameField.getText();
                double price = Double.parseDouble(priceField.getText());
                int quantity = Integer.parseInt(quantityField.getText());
                String extra = extraField.getText();

                AbstractProduct product;
                if ("Food".equals(typeCombo.getSelectedItem())) {
                    LocalDate expiration = LocalDate.parse(extra);
                    product = new FoodProduct(id, name, price, quantity, expiration);
                } else {
                    int warranty = Integer.parseInt(extra);
                    product = new ElectronicsProduct(id, name, price, quantity, warranty);
                }

                warehouse.addProduct(product);
                outputArea.append("Added: " + product + "\n");
            } catch (NumberFormatException | DateTimeParseException ex) {
                JOptionPane.showMessageDialog(WarehouseGUI.this, "Invalid input format.");
            }
        }
    }
}