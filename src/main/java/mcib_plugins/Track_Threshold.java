package mcib_plugins;

import ij.*;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.IterativeThresholding.TrackThreshold;
import mcib3d.image3d.processing.FastFilters3D;

import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
public class Track_Threshold implements PlugInFilter {

    int volMax = (int) Prefs.get("mcib_iterative_volmax.int", 1000);
    int volMin = (int) Prefs.get("mcib_iterative_volmin.int", 100);
    double minTh = (int) Prefs.get("mcib_iterative_thmin.int", 0);
    int minCont = (int) Prefs.get("mcib_iterative_contmin.int", 0);
    boolean filter = false;
    Calibration cal;
    private int step = (int) Prefs.get("mcib_iterative_step.int", 10);
    private int threshold_method = (int) Prefs.get("mcib_iterative_method.int", 0);
    private int crit = (int) Prefs.get("mcib_iterative_criteria.int", 0);
    private int seg = (int) Prefs.get("mcib_iterative_seg.int", 0);
    private boolean start;
    private String[] methods;
    private String[] criteria;
    private String[] segs;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_16 + DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {

        ImagePlus plus = IJ.getImage();
        ArrayList<Point3D> point3Ds = null;
        Calibration calibration = plus.getCalibration();

        ImagePlus seeds = WindowManager.getImage("markers");
        if (seeds == null) ;//IJ.log("No image with name \"markers\" found. Not using markers.");
        else {
            point3Ds = computeMarkers(ImageInt.wrap(seeds));
        }

        if (!dialogue()) {
            return;
        }
        // extract current time 
        Duplicator dup = new Duplicator();
        int[] dim = plus.getDimensions();
        int selectedTime = plus.getFrame();
        ImagePlus timeDuplicate = dup.run(plus, 1, 1, 1, dim[3], selectedTime, selectedTime);
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
            ImageStack res = FastFilters3D.filterIntImageStack(timeDuplicate.getStack(), FastFilters3D.MEDIAN, radX, radX, radZ, 0, true);
            ImagePlus filteredPlus = new ImagePlus("filtered_" + radX, res);
            timeDuplicate.setStack(res);
            filteredPlus.show();
        }
        IJ.log("Threshold method " + methods[threshold_method]);
        IJ.log("Criteria method " + criteria[crit]);
        int thmin = (int) minTh;
        // is starts at mean selected, use mean, maybe remove in new version
        if (start) {
            thmin = (int) ImageHandler.wrap(timeDuplicate).getMean();
            IJ.log("Mean=" + thmin);
        }

        TrackThreshold TT = new TrackThreshold(volMin, volMax, minCont, step, step, thmin);
        TT.setMarkers(point3Ds);

        // markers image and zone image EXPERIMENTAL
        ImagePlus markersPlus = WindowManager.getImage("markers");
        if (markersPlus != null) {
            TT.setImageMarkers(ImageInt.wrap(markersPlus));
            IJ.log("markers image " + markersPlus);
        }
        ImagePlus zonePlus = WindowManager.getImage("zones");
        if (zonePlus != null) {
            TT.setImageZones(ImageInt.wrap(zonePlus));
            IJ.log("zones image " + zonePlus);
        }


        // 8-bits switch to step method
        int tmethod = TrackThreshold.THRESHOLD_METHOD_STEP;
        if (threshold_method == 0) {
            tmethod = TrackThreshold.THRESHOLD_METHOD_STEP;
        } else if (threshold_method == 1) {
            tmethod = TrackThreshold.THRESHOLD_METHOD_KMEANS;
        } else if (threshold_method == 2) {
            tmethod = TrackThreshold.THRESHOLD_METHOD_VOLUME;
        }
        if (timeDuplicate.getBitDepth() == 8) {
            threshold_method = TrackThreshold.THRESHOLD_METHOD_STEP;
        }
        TT.setMethodThreshold(tmethod);
        int cri = TrackThreshold.CRITERIA_METHOD_MIN_ELONGATION;
        switch (crit) {
            case 0:
                cri = TrackThreshold.CRITERIA_METHOD_MIN_ELONGATION;
                break;
            case 1:
                cri = TrackThreshold.CRITERIA_METHOD_MAX_COMPACTNESS;
                break;
            case 2:
                cri = TrackThreshold.CRITERIA_METHOD_MAX_VOLUME;
                break;
            case 3:
                cri = TrackThreshold.CRITERIA_METHOD_MSER;
                break;
            case 4:
                cri = TrackThreshold.CRITERIA_METHOD_MAX_EDGES;
                break;
        }

        TT.setCriteriaMethod(cri);
        ImagePlus res;
        if (seg == 0)
            res = TT.segment(timeDuplicate, true);
        else
            res = TT.segmentBest(timeDuplicate, true);
        if ((res != null) && (calibration != null)) res.setCalibration(calibration);
        if (res != null) res.show();
        else IJ.log("NO OBJECTS FOUND !");
        // test
        //ImagePlus resBest = TT.segmentBest(timeDuplicate, true);
    }

    private boolean dialogue() {
        methods = new String[]{"STEP", "KMEANS", "VOLUME"};
        criteria = new String[]{"ELONGATION", "COMPACTNESS", "VOLUME", "MSER", "EDGES"};
        segs = new String[]{"All", "Best"};
        GenericDialog gd = new GenericDialog("sizes");
        gd.addNumericField("Min_vol_pix", volMin, 0, 10, "");
        gd.addNumericField("Max_vol_pix", volMax, 0, 10, "");
        gd.addNumericField("Min_threshold", minTh, 0, 10, "");
        gd.addNumericField("Min_contrast (exp)", minCont, 0, 10, "");
        gd.addChoice("Criteria_method", criteria, criteria[crit]);
        gd.addChoice("Threshold_method", methods, methods[threshold_method]);
        gd.addChoice("Segment_results", segs, segs[seg]);
        gd.addNumericField("Value_method", step, 1, 10, "");
        gd.addCheckbox("Starts at mean", start);
        gd.addCheckbox("Filtering", filter);
        gd.showDialog();
        volMin = (int) gd.getNextNumber();
        volMax = (int) gd.getNextNumber();
        minTh = (int) gd.getNextNumber();
        minCont = (int) gd.getNextNumber();
        crit = gd.getNextChoiceIndex();
        threshold_method = gd.getNextChoiceIndex();
        seg = gd.getNextChoiceIndex();
        step = (int) gd.getNextNumber();
        start = gd.getNextBoolean();
        filter = gd.getNextBoolean();

        if (volMax < volMin) {
            int vtemp = volMax;
            volMax = volMin;
            volMin = vtemp;
        }

        Prefs.set("mcib_iterative_volmax.int", volMax);
        Prefs.set("mcib_iterative_volmin.int", volMin);
        Prefs.set("mcib_iterative_thmin.int", minTh);
        Prefs.set("mcib_iterative_contmin.int", minCont);
        Prefs.set("mcib_iterative_method.int", threshold_method);
        Prefs.set("mcib_iterative_criteria.int", crit);
        Prefs.set("mcib_iterative_seg.int", seg);
        Prefs.set("mcib_iterative_step.int", step);

        return gd.wasOKed();
    }

    private ArrayList<Point3D> computeMarkers(ImageInt markImage) {
        if (markImage.isBinary()) {
            ImageLabeller labeler = new ImageLabeller();
            markImage = labeler.getLabels(markImage);
        }
        ArrayList<Point3D> point3Ds = new ArrayList<Point3D>();
        Objects3DPopulation objects3DPopulation = new Objects3DPopulation(markImage);
        for (Object3D object3D : objects3DPopulation.getObjectsList()) {
            point3Ds.add(object3D.getCenterAsPoint());
        }
        return point3Ds;
    }
}
