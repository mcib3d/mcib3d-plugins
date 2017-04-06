package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
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
public class Segment3D_ implements PlugInFilter {

    ImagePlus myPlus;
    // DB
    boolean debug = false;
    boolean multithread = false;

    @Override
    public int setup(String arg, ImagePlus imp) {
        myPlus = imp;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G + PlugInFilter.STACK_REQUIRED;
    }

    @Override
    public void run(ImageProcessor ip) {
        int low = 128;
        //int high = -1;
        int min = 0;
        int max = -1;
        boolean individual = false;
        GenericDialog gd = new GenericDialog("Segment3D");
        gd.addNumericField("Low_threshold (included)", low, 0);
        //gd.addNumericField("High_threshold (-1 for max)", high, 0);
        gd.addNumericField("Min_size", min, 0);
        gd.addNumericField("Max_size (-1 for infinity)", max, 0);
        gd.addCheckbox("Individual voxels are objects", individual);
        gd.showDialog();
        low = (int) gd.getNextNumber();
        //high = (int) gd.getNextNumber();
        min = (int) gd.getNextNumber();
        max = (int) gd.getNextNumber();
        individual = gd.getNextBoolean();
        if (gd.wasCanceled()) {
            return;
        }
        ImageLabeller labeler = new ImageLabeller();
        if (min > 0) {
            labeler.setMinSize(min);
        }
        if (max > 0) {
            labeler.setMaxsize(max);
        }
        ImageInt img = ImageInt.wrap(myPlus);
        ImageInt bin = img.thresholdAboveInclusive(low);
        bin.setScale(img);
        bin.show("Bin");
        ImageInt res;
        if (individual) res = labeler.getLabelsIndividualVoxels(bin);
        else res = labeler.getLabels(bin);
        res.setScale(img);
        res.show("Seg");
        IJ.log("Nb obj total =" + labeler.getNbObjectsTotal(bin));
        IJ.log("Nb obj in range size =" + labeler.getNbObjectsinSizeRange(bin));
    }
}
