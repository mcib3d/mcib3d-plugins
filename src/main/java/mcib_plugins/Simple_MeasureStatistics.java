package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
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
 * *
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 * <p>
 * <p>
 * <p>
 * This file is part of mcib3d
 * <p>
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Simple_MeasureStatistics implements PlugInFilter {

    ImagePlus myPlus;
    int imaSpots;
    int imaSignal;
    String[] keys_s = new String[]{"Value", "Average", "StandardDeviation", "Minimum", "Maximum", "IntegratedDensity"};
    boolean debug = false;
    boolean multithread = false;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (Dialogue()) {
            myPlus = WindowManager.getImage(imaSpots);
            int channel = myPlus.getChannel();
            int frame = myPlus.getFrame();
            String title = myPlus.getTitle();
            ImageInt img = ImageInt.wrap(extractCurrentStack(myPlus));
            ImagePlus seg;
            if (img.isBinary(0)) {
                ImageLabeller label = new ImageLabeller();
                seg = label.getLabels(img).getImagePlus();
                seg.show("Labels");
            } else {
                seg = img.getImagePlus();
            }
            simpleMeasure mes = new simpleMeasure(seg);
            ResultsTable rt = ResultsTable.getResultsTable();
            if (rt == null) {
                rt = new ResultsTable();
            }
            myPlus = WindowManager.getImage(imaSignal);
            ImagePlus plusSignal = extractCurrentStack(myPlus);
            title = title.concat(":");
            title = title.concat(plusSignal.getTitle());
            ArrayList<double[]> res = mes.getMeasuresStats(plusSignal);
            int row = rt.getCounter();
            for (Iterator<double[]> it = res.iterator(); it.hasNext(); ) {
                rt.incrementCounter();
                double[] m = it.next();
                for (int k = 0; k < keys_s.length; k++) {
                    rt.setValue(keys_s[k], row, m[k]);
                }
                rt.setLabel(title, row);
                rt.setValue("Channel", row, channel);
                rt.setValue("Frame", row, frame);
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

    private ImagePlus extractCurrentStack(ImagePlus plus) {
        // check dimensions
        int[] dims = plus.getDimensions();//XYCZT
        int channel = plus.getChannel();
        int frame = plus.getFrame();
        ImagePlus stack;
        // crop actual frame
        if ((dims[2] > 1) || (dims[4] > 1)) {
            IJ.log("hyperstack found, extracting current channel " + channel + " and frame " + frame);
            Duplicator duplicator = new Duplicator();
            stack = duplicator.run(plus, channel, channel, 1, dims[3], frame, frame);
        } else stack = plus.duplicate();

        return stack;
    }
}
