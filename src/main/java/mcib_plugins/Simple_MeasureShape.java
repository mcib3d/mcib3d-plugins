package mcib_plugins;

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
public class Simple_MeasureShape implements PlugInFilter {

    ImagePlus myPlus;
    String[] keysBase_s = new String[]{"Value", "Compactness", "Sphericity", "Elongatio", "Flatness", "Spareness"};

    @Override
    public int setup(String arg, ImagePlus imp) {
        myPlus = imp;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
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
        ArrayList<double[]> res = mes.getMeasuresShape();
        int row = rt.getCounter();
        for (Iterator<double[]> it = res.iterator(); it.hasNext();) {
            rt.incrementCounter();
            double[] m = it.next();
            for (int k = 0; k < keysBase_s.length; k++) {
                rt.setValue(keysBase_s[k], row, m[k]);
            }
            rt.setLabel(title, row);
            row++;
        }
        rt.updateResults();
        rt.show("Results");
    }
}
