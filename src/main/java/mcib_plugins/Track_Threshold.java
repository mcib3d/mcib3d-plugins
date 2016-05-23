package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.IterativeThresholding.TrackThreshold;
import mcib3d.image3d.processing.FastFilters3D;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class Track_Threshold implements PlugInFilter {

    int volMax = 10000;
    int volMin = 100;
    double minTh = 0;
    boolean filter = true;
    Calibration cal;
    private int step = 1;
    private int threshold_method = 0;
    private int crit = 0;
    private boolean start;
    private String[] methods;
    private String[] criteria;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_16 + DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {

        ImagePlus plus = IJ.getImage();
        
        if (plus.getBitDepth() == 8) {
            step = 1;
        } else {
            step = 100;
        }
        if (!dialogue()) {
            return;
        }
        //
//        if (Recorder.record) {
//            Recorder.setCommand(null);
//            String param = "3D Iterative Thresholding\",\"Min_vol_pix=" + volMin + " Max_vol_pix=" + volMax + " Threshold=" + methods[threshold_method]
//                    + " Criteria=" + criteria[crit] + " Value=" + step;
//            if (start) {
//                param = param.concat(" Starts");
//            }
//            if (filter) {
//                param = param.concat(" Filtering");
//            }
//            Recorder.record("run", param);
//        }

        // extract current time 
        Duplicator dup = new Duplicator();
        int[] dim = plus.getDimensions();
        int selectedTime = plus.getFrame();
        ImagePlus timedup = dup.run(plus, 1, 1, 1, dim[3], selectedTime, selectedTime);
        //timedup.show("Frame_" + selectedTime);
        if (filter) {
            int radX = (int) Math.floor(Math.pow((volMin * 3.0) / (4.0 * Math.PI), 1.0 / 3.0));
            if (radX > 10) {
                radX = 10;
            }
            if (radX < 1) {
                radX = 1;
            }
            int radZ = radX; // use calibration ?
            IJ.log("Filtering with radius " + radX);
            ImageStack res = FastFilters3D.filterIntImageStack(timedup.getStack(), FastFilters3D.MEDIAN, radX, radX, radZ, 0, true);
            ImagePlus filteredPlus = new ImagePlus("filtered_" + radX, res);
            timedup.setStack(res);
            filteredPlus.show();
        }
        IJ.log("Threshold method " + methods[threshold_method]);
        IJ.log("Criteria method " + criteria[crit]);
        int thmin = (int) minTh;
        // is starts at mean selected, use mean, maybe remove in new version
        if (start) {
            thmin = (int) ImageHandler.wrap(timedup).getMean();
            IJ.log("Mean=" + thmin);
        }

        TrackThreshold TT = new TrackThreshold(volMin, volMax, step, step, thmin);
        // 8-bits switch to step method
        int tmethod = TrackThreshold.THRESHOLD_METHOD_STEP;
        if (threshold_method == 0) {
            tmethod = TrackThreshold.THRESHOLD_METHOD_STEP;
        } else if (threshold_method == 1) {
            tmethod = TrackThreshold.THRESHOLD_METHOD_KMEANS;
        } else if (threshold_method == 2) {
            tmethod = TrackThreshold.THRESHOLD_METHOD_VOLUME;
        }
        if (timedup.getBitDepth() == 8) {
            threshold_method = TrackThreshold.THRESHOLD_METHOD_STEP;
        }
        TT.setMethodThreshold(tmethod);
        int cri = TrackThreshold.CRITERIA_METHOD_MIN_ELONGATION;
        if (crit == 0) {
            cri = TrackThreshold.CRITERIA_METHOD_MIN_ELONGATION;
        } else if (crit == 1) {
            cri = TrackThreshold.CRITERIA_METHOD_MAX_VOLUME;
        } else if (crit == 2) {
            cri = TrackThreshold.CRITERIA_METHOD_MSER;
        }
        TT.setCriteriaMethod(cri);
        ImagePlus res = TT.segment(timedup, true);
        res.show();
    }

    private boolean dialogue() {
        methods = new String[]{"STEP", "KMEANS", "VOLUME"};
        criteria = new String[]{"ELONGATION", "VOLUME", "MSER"};
        GenericDialog gd = new GenericDialog("sizes");
        gd.addNumericField("Min_vol_pix", volMin, 0, 10, "");
        gd.addNumericField("Max_vol_pix", volMax, 0, 10, "");
        gd.addNumericField("Min_threshold", minTh, 0, 10, "");
        gd.addChoice("Criteria_method", criteria, criteria[crit]);
        gd.addChoice("Threshold_method", methods, methods[threshold_method]);
        gd.addNumericField("Value_method", step, 1, 10, "");
        gd.addCheckbox("Starts at mean", start);
        gd.addCheckbox("Filtering", filter);
        gd.showDialog();
        volMin = (int) gd.getNextNumber();
        volMax = (int) gd.getNextNumber();
        minTh = (int) gd.getNextNumber();
        crit = gd.getNextChoiceIndex();
        threshold_method = gd.getNextChoiceIndex();
        step = (int) gd.getNextNumber();
        start = gd.getNextBoolean();
        filter = gd.getNextBoolean();

        if (volMax < volMin) {
            int vtemp = volMax;
            volMax = volMin;
            volMin = volMax;
        }

        return gd.wasOKed();
    }
}
