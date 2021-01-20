package mcib_plugins;

import ij.IJ;
import ij.plugin.Duplicator;
import mcib_plugins.analysis.simpleMeasure;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.ArrayList;
import java.util.Iterator;

import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;

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
public class Simple_MeasureCentroid implements PlugInFilter {

    ImagePlus myPlus;
    String[] keysBase_s = new String[]{"Value", "CX(pix)", "CY(unit)", "CZ(pix)"};

    @Override
    public int setup(String arg, ImagePlus imp) {
        myPlus = imp;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        String title = myPlus.getTitle();
        // check dimensions
        int channel = myPlus.getChannel();
        int frame = myPlus.getFrame();
        ImageInt img = ImageInt.wrap(extractCurrentStack(myPlus));
        ImagePlus seg;
        if (img.isBinary(0)) {
            IJ.log("Labelling image.");
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
        ArrayList<double[]> res = mes.getMeasuresCentroid();
        int row = rt.getCounter();
        for (Iterator<double[]> it = res.iterator(); it.hasNext(); ) {
            rt.incrementCounter();
            double[] m = it.next();
            for (int k = 0; k < keysBase_s.length; k++) {
                rt.setValue(keysBase_s[k], row, m[k]);
            }
            rt.setLabel(title, row);
            rt.setValue("Channel", row, channel);
            rt.setValue("Frame", row, frame);
            row++;
        }
        rt.updateResults();
        rt.show("Results");
    }

    private ImagePlus extractCurrentStack(ImagePlus plus) {
        // check dimensions
        int[] dims = plus.getDimensions();//XYCZT
        int channel = plus.getChannel();
        int frame = plus.getFrame();
        ImagePlus stack;
        // crop actual frame
        if ((dims[2] > 1) || (dims[4] > 1)) {
            IJ.log("Hyperstack found, extracting current channel " + channel + " and frame " + frame);
            Duplicator duplicator = new Duplicator();
            stack = duplicator.run(plus, channel, channel, 1, dims[3], frame, frame);
        } else stack = plus.duplicate();

        return stack;
    }
}
