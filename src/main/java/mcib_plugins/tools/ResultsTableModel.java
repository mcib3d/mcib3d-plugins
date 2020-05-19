/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.tools;

import javax.swing.table.AbstractTableModel;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author thomasb from internet code
 */
public class ResultsTableModel extends AbstractTableModel {

    private final String[] columnNames;
    private final Object[][] data;

    private String delimiter = ",";

    public ResultsTableModel(String[] columnNames, Object[][] data) {
        this.columnNames = columnNames;
        this.data = data;
    }

    @Override
    public int getColumnCount() {
        //IJ.log("nb col=" + columnNames.length + " " + data[0].length);
        return columnNames.length;
    }

    @Override
    public int getRowCount() {
        return data.length;
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        //IJ.log("data " + data.length + " " + data[0].length + " " + data[0][0]);
        // IJ.log("row=" + row + " col=" + col);
        return data[row][col];
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        //Note that the data/cell address is constant,
        //no matter where the cell appears onscreen.
//            if (col < 2) {
//                return false;
//            } else {
//                return true;
//            }
        return false;
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    @Override
    public void setValueAt(Object value, int row, int col) {

        data[row][col] = value;
        // Normally, one should call fireTableCellUpdated() when 
        // a value is changed.  However, doing so in this demo
        // causes a problem with TableSorter.  The tableChanged()
        // call on TableSorter that results from calling
        // fireTableCellUpdated() causes the indices to be regenerated
        // when they shouldn't be.  Ideally, TableSorter should be
        // given a more intelligent tableChanged() implementation,
        // and then the following line can be uncommented.
        // fireTableCellUpdated(row, col);

    }

    public boolean writeData(String fileName) {
        BufferedWriter buf;
        String name = fileName;
        if (!name.contains(".")) {
            name = name.concat(".csv");
        }
        try {
            buf = new BufferedWriter(new FileWriter(name));
            int numRows = getRowCount();
            int numCols = getColumnCount();
            for (String col : columnNames) {
                buf.write(col + delimiter);
            }
            buf.write("\n");
            for (int i = 0; i < numRows; i++) {
                //buf.write("    row " + i + ":");
                for (int j = 0; j < numCols; j++) {
                    buf.write(data[i][j] + delimiter);
                }
                buf.write("\n");
            }
            buf.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultsTableModel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        } catch (IOException ex) {
            Logger.getLogger(ResultsTableModel.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    public void writeDataSelected(String fileName, int[] rows) {
        BufferedWriter buf;
        String name = fileName;
        if (!name.contains(".")) {
            name = name.concat(".csv");
        }
        try {
            buf = new BufferedWriter(new FileWriter(name));
            for (String col : columnNames) {
                buf.write(col + delimiter);
            }
            buf.write("\n");
            int numCols = getColumnCount();
            for (int i : rows) {
                //buf.write("    row " + i + ":");
                for (int j = 0; j < numCols; j++) {
                    buf.write(data[i][j] + delimiter);
                }
                buf.write("\n");
            }
            buf.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ResultsTableModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ResultsTableModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
