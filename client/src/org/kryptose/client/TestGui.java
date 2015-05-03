package org.kryptose.client;
import java.awt.*;

import javax.swing.*;
import javax.swing.table.*;

public class TestGui {
  public static void main(String args[]) {
    JFrame frame = new JFrame("Manage Credentials");
    frame.setDefaultCloseOperation(
      JFrame.EXIT_ON_CLOSE);
    Container contentPane = frame.getContentPane();
    String headers[] = {"Domain", "Username"};
    String data[][] = {
      {"google", "antonio"},
      {"yahoo", "Jonathan"},
      {"google", "Jeff"},
    };
    DefaultTableModel model =
      new DefaultTableModel(data, headers) {
        // Make read-only
        public boolean isCellEditable(int x, int y) {
          return false;
        }
      };

      model.addRow(new Object[]{"v1", "v2"});
      

    JTable table = new JTable(model);
    // Set selection to first row
    ListSelectionModel selectionModel =
      table.getSelectionModel();
    selectionModel.setSelectionInterval(0, 0);
    selectionModel.setSelectionMode(
      ListSelectionModel.SINGLE_SELECTION);
    // Add to screen so scrollable
    JScrollPane scrollPane = new JScrollPane (table);
    contentPane.add(scrollPane, BorderLayout.CENTER);
    
    contentPane.add(new JButton("Edit Credential"), BorderLayout.AFTER_LAST_LINE);
//    contentPane.add(new JButton("New Credential"),BorderLayout.AFTER_LINE_ENDS);
//    contentPane.add(new JButton("Delete Credential"),BorderLayout.AFTER_LAST_LINE);

    frame.setSize(300, 100);
    frame.show();
  }
}