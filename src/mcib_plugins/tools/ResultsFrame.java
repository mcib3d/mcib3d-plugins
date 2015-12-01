/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.tools;

import ij.IJ;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;

/**
 *
 * @author thomasb
 */
public class ResultsFrame extends JFrame implements ActionListener {

    private final RoiManager3D_2 manager;
    private ResultsTableModel model;
    private JTable tableResults;
    private final String[] columnNames;
    private final Object[][] data;

    public final static int OBJECT_NO = 0;
    public final static int OBJECT_1 = 1;
    public final static int OBJECTS_2 = 2;

    private int type = OBJECT_NO;

    public ResultsFrame(String title, String[] columnNames, Object[][] data, RoiManager3D_2 manager, int type) throws HeadlessException {
        super(title);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        this.columnNames = columnNames;
        this.data = data;
        this.manager = manager;
        this.type = type;

        // add menu
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");
        JMenuItem itemSave = new JMenuItem("Save");
        itemSave.addActionListener(this);
        menu.add(itemSave);
        JMenuItem itemSaveSel = new JMenuItem("Save Selection");
        itemSaveSel.addActionListener(this);
        menu.add(itemSaveSel);
        if (type != OBJECT_NO) {
            menu.add(new JSeparator());
        }
//        JMenuItem itemDel = new JMenuItem("Delete Rows");
//        itemDel.addActionListener(this);
//        menu.add(itemDel);
        if (type == OBJECT_1) {
            JMenuItem itemList = new JMenuItem("Show Objects");
            itemList.addActionListener(this);
            menu.add(itemList);
        }
        if (type == OBJECTS_2) {
            JMenuItem itemList1 = new JMenuItem("Show Objects 1");
            itemList1.addActionListener(this);
            menu.add(itemList1);
            JMenuItem itemList2 = new JMenuItem("Show Objects 2");
            itemList2.addActionListener(this);
            menu.add(itemList2);
            JMenuItem itemList12 = new JMenuItem("Show Objects Pairs");
            itemList12.addActionListener(this);
            menu.add(itemList12);
        }

        menuBar.add(menu);
        setJMenuBar(menuBar);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        // IJ.log("action " + ae + " " + ae.getActionCommand());
        if (ae.getActionCommand().equalsIgnoreCase("save")) {
            JFileChooser fd = new JFileChooser();
            int userSelection = fd.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fd.getSelectedFile();
                model.writeData(file.getAbsolutePath());
            }
        } else if (ae.getActionCommand().equalsIgnoreCase("save selection")) {
            int[] rows = tableResults.getSelectedRows();
            int[] sels = new int[rows.length];
            int c = 0;
            for (int i : rows) {
                sels[c++] = tableResults.convertRowIndexToModel(i);
            }
            JFileChooser fd = new JFileChooser();
            int userSelection = fd.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File file = fd.getSelectedFile();
                model.writeDataSelected(file.getAbsolutePath(), sels);
            }
        } else if (ae.getActionCommand().equalsIgnoreCase("show objects")) {

            int[] sels = tableResults.getSelectedRows();
            int[] rows = new int[sels.length];
            IJ.log("Showing " + sels.length + " selected objects ");
            int c = 0;
            for (int i : sels) {
                int row = tableResults.convertRowIndexToModel(i);
                int nb = Integer.parseInt(model.getValueAt(row, 1).toString()) - 1;
                rows[c++] = nb;
                //IJ.log("Selecting object "+nb);          
            }

            manager.selectByNumbers(rows);
        } else if (ae.getActionCommand().equalsIgnoreCase("show objects 1")) {
            int[] sels = tableResults.getSelectedRows();

            int[] rows = new int[sels.length];
            int c = 0;
            for (int i : sels) {
                int row = tableResults.convertRowIndexToModel(i);
                rows[c++] = Integer.parseInt(model.getValueAt(row, 1).toString()) - 1;
            }
            manager.selectByNumbers(rows);
        } else if (ae.getActionCommand().equalsIgnoreCase("show objects 2")) {
            int[] sels = tableResults.getSelectedRows();

            int[] rows = new int[sels.length];
            int c = 0;
            for (int i : sels) {
                int row = tableResults.convertRowIndexToModel(i);
                rows[c++] = Integer.parseInt(model.getValueAt(row, 2).toString()) - 1;
            }
            manager.selectByNumbers(rows);
        } else if (ae.getActionCommand().equalsIgnoreCase("show objects pairs")) {
            int[] sels = tableResults.getSelectedRows();

            int[] rows = new int[2 * sels.length];
            int c = 0;
            for (int i : sels) {
                int row = tableResults.convertRowIndexToModel(i);
                rows[c++] = Integer.parseInt(model.getValueAt(row, 1).toString()) - 1;
                rows[c++] = Integer.parseInt(model.getValueAt(row, 2).toString()) - 1;
            }
            manager.selectByNumbers(rows);
        }
    }

//    public void setTableResultsMeasure(ResultsTableFrame tableResultsMeasure) {
//        this.tableResultsMeasure = tableResultsMeasure;
//    }
    public void showFrame() {
        JPanel tableResultsMeasure = new JPanel(new GridLayout(1, 0));
        tableResultsMeasure.setOpaque(true);
        model = new ResultsTableModel(columnNames, data);
        tableResults = new JTable(model);
        tableResults.setPreferredScrollableViewportSize(new Dimension(500, 70));
        tableResults.setFillsViewportHeight(true);
        tableResults.setAutoCreateRowSorter(true);
        tableResults.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(tableResults);

        //Add the scroll pane to this panel.
        tableResultsMeasure.add(scrollPane);
        setContentPane(tableResultsMeasure);

        //Display the window.
        pack();
        setVisible(true);
    }

    public ResultsTableModel getModel() {
        return model;
    }

}
