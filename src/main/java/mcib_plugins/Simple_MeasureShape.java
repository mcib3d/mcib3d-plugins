package mcib_plugins;

import ij.IJ;
import mcib_plugins.analysis.SimpleMeasure;
import ij.ImagePlus;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

import java.util.List;

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
public class Simple_MeasureShape implements PlugInFilter {

    ImagePlus myPlus;
    String[] keysBase_s = new String[]{"Value", "Compactness(Pix)", "Compactness(Unit)","CompactCorrected(Pix)","CompactDiscrete(Pix)","Sphericity(Pix)", "Sphericity(Unit)","SpherCorrected(Pix)","SpherDiscrete(Pix)"};

    @Override
    public int setup(String arg, ImagePlus imp) {
        myPlus = imp;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        String title = myPlus.getTitle();
        // check dimensions
        int[] dims = myPlus.getDimensions();//XYCZT
        if (dims[3] == 1) IJ.log("Most shape measurements are not relevant for 2D images");
        int channel = myPlus.getChannel();
        int frame = myPlus.getFrame();
        ImageInt img = ImageInt.wrap(SimpleMeasure.extractCurrentStack(myPlus));
        ImagePlus seg;
        if (img.isBinary(0)) {
            ImageLabeller label = new ImageLabeller();
            seg = label.getLabels(img).getImagePlus();
            seg.show("Labels");
        } else {
            seg = img.getImagePlus();
        }
        SimpleMeasure mes = new SimpleMeasure(seg);
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }
        IJ.log("Computing compactness");
        List<Double[]> res = mes.getMeasuresCompactness();
        int row = rt.getCounter();
        for (Double[] re : res) {
            rt.incrementCounter();
            for (int k = 0; k < keysBase_s.length; k++) {
                rt.setValue(keysBase_s[k], row, re[k]);
            }
            rt.setLabel(title, row);
            rt.setValue("Channel", row, channel);
            rt.setValue("Frame", row, frame);
            row++;
        }
        rt.sort("Value");
        rt.updateResults();
        rt.show("Results");
    }

}
