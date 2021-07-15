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
public class Simple_MeasureGeometrical implements PlugInFilter {

    ImagePlus myPlus;
    String[] keysBase_s1 = new String[]{"Value", "Volume(pix)", "Volume(unit)"};
    String[] keysBase_s2 = new String[]{"Value", "Surface(pix)", "Surface(unit)", "SurfCorrected(pix)", "SurfaceNb"};

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
        ImageInt img = ImageInt.wrap(SimpleMeasure.extractCurrentStack(myPlus));
        ImagePlus seg;
        if (img.isBinary(0)) {
            IJ.log("Labelling image.");
            ImageLabeller label = new ImageLabeller();
            seg = label.getLabels(img).getImagePlus();
            seg.show("Labels");
        } else {
            seg = img.getImagePlus();
        }
        // volumes
        ResultsTable rt = ResultsTable.getResultsTable();
        SimpleMeasure mes = new SimpleMeasure(seg);
        if (rt == null) {
            rt = new ResultsTable();
        }
        IJ.log("Computing volumes");
        List<Double[]> res = mes.getMeasuresVolume();
        int row = rt.getCounter();
        int row0 = row;
        for (Double[] re : res) {
            rt.incrementCounter();
            for (int k = 0; k < keysBase_s1.length; k++) {
                rt.setValue(keysBase_s1[k], row,re[k]);
            }
            rt.setLabel(title, row);
            rt.setValue("Channel", row, channel);
            rt.setValue("Frame", row, frame);
            row++;
        }
        // surfaces
        if (rt == null) {
            rt = new ResultsTable();
        }
        IJ.log("Computing surfaces");
        res = mes.getMeasuresSurface();
        row = row0;
        for (Double[] re : res) {
            for (int k = 0; k < keysBase_s2.length; k++) {
                rt.setValue(keysBase_s2[k], row,re[k]);
            }
            row++;
        }
        rt.sort("Value");
        rt.updateResults();
        rt.show("Results");
    }


}
