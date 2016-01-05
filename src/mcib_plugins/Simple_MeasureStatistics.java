package mcib_plugins;

import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib_plugins.analysis.simpleMeasure;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Simple_MeasureStatistics implements PlugInFilter {

    ImagePlus myPlus;
    int imaSpots;
    int imaSignal;
    String[] keys_s = new String[]{"Value","Average", "StandardDeviation", "Minimum", "Maximum", "IntegratedDensity"};
    boolean debug = false;
    boolean multithread = false;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + PlugInFilter.STACK_REQUIRED;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (Dialogue()) {
            myPlus = WindowManager.getImage(imaSpots);
            String title = myPlus.getTitle();
            ImageInt img = ImageInt.wrap(myPlus);
            ImagePlus seg;
            if (img.isBinary(0)) {
                ImageLabeller label = new ImageLabeller();
                seg = label.getLabels(img).getImagePlus();
                seg.show("Labels");
            } else {
                seg = myPlus;
            }
            simpleMeasure mes = new simpleMeasure(seg);
            ResultsTable rt = ResultsTable.getResultsTable();
            if (rt == null) {
                rt = new ResultsTable();
            }
            ImagePlus plusSignal = WindowManager.getImage(imaSignal);
            title = title.concat(":");
            title = title.concat(plusSignal.getTitle());
            ArrayList<double[]> res = mes.getMeasuresStats(plusSignal);
            int row = rt.getCounter();
            for (Iterator<double[]> it = res.iterator(); it.hasNext();) {
                rt.incrementCounter();
                double[] m = it.next();
                for (int k = 0; k < keys_s.length; k++) {
                    rt.setValue(keys_s[k], row, m[k]);
                }
                rt.setLabel(title, row);
                row++;
            }
            rt.updateResults();
            rt.show("Results");
        }
    }

    private boolean Dialogue() {
        int nbima = WindowManager.getImageCount();
        String[] names = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        imaSpots = 0;
        imaSignal = nbima > 1 ? nbima - 1 : 0;

        GenericDialog dia = new GenericDialog("Statistical measure");
        dia.addChoice("Objects", names, names[imaSpots]);
        dia.addChoice("Signal", names, names[imaSignal]);
        dia.showDialog();
        imaSpots = dia.getNextChoiceIndex() + 1;
        imaSignal = dia.getNextChoiceIndex() + 1;

        return dia.wasOKed();
    }
}
