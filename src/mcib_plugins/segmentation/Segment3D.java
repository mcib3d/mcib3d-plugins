package mcib_plugins.segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.measure.Calibration;
import mcib3d.image3d.Segment3DImage;

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
public class Segment3D {

    static int nb_obj;

    public ImagePlus segmentation3D(ImagePlus myImp, float low, float high, int min, int max, boolean show) {
        if (show) {
            IJ.log("Simple segmentation 3D");
        }
        String title = myImp.getTitle();
        Calibration cal = myImp.getCalibration();
        if (high == -1) {
            high = 65536;
        }
        Segment3DImage seg3d = new Segment3DImage(myImp, low, high);
        if (min != -1) {
            seg3d.setMinSizeObject(min);
        }
        if (max != -1) {
            seg3d.setMaxSizeObject(max);
        }
        seg3d.segment();
        ImageStack ob = seg3d.getLabelledObjectsStack();
        ImageStack su = seg3d.getSurfaceObjectsStack();
        nb_obj = seg3d.getNbObj();
        if (show) {
            IJ.log("nb obj=" + nb_obj);
        }

        ImagePlus plusSeg = new ImagePlus(title + "-3Dseg", ob);
        plusSeg.setCalibration(cal);
        ImagePlus plusSurf = new ImagePlus(title + "-3Dsurf", su);
        plusSurf.setCalibration(cal);
        if (show) {
            plusSeg.show();
            plusSurf.show();
        }

        return plusSeg;
    }

    public int getNbObj() {
        return nb_obj;
    }
}
