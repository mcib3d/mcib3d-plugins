/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.MaximaFinder;

import java.util.ArrayList;

/**
 * @author thomasb
 */
public class MaximaFinder3D_ implements PlugInFilter {

    ImagePlus plus;
    float noise = 100;
    float rxy = 1.5f;
    float rz = 1.5f;

    @Override
    public int setup(String arg, ImagePlus imp) {
        plus = imp;

        return PlugInFilter.DOES_16 + PlugInFilter.DOES_32 + PlugInFilter.DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (dialog()) {
            ImageInt img = ImageInt.wrap(plus);
            MaximaFinder test = new MaximaFinder(img, noise);
            test.setRadii(rxy, rz);
            test.getImagePeaks().show();
            // list
            ArrayList<Voxel3D> list = test.getListPeaks();
            ResultsTable rt = ResultsTable.getResultsTable();
            if (rt == null) {
                rt = new ResultsTable();
            }
            rt.reset();
            for (Voxel3D V : list) {
                rt.incrementCounter();
                rt.addValue("X", V.getX());
                rt.addValue("Y", V.getY());
                rt.addValue("Z", V.getZ());
                rt.addValue("V", V.getValue());
            }
            rt.show("Results");
        }
    }

    private boolean dialog() {
        rxy = (float) Prefs.get("mcib3d.maximafinder.radxy.double", rxy);
        rz = (float) Prefs.get("mcib3d.maximafinder.radz.double", rz);
        noise = (float) Prefs.get("mcib3d.maximafinder.noise.double", noise);
        GenericDialog dia = new GenericDialog("Maxima Finder");
        dia.addNumericField("RadiusXY", rxy, 2, 5, "pixel");
        dia.addNumericField("RadiusZ", rz, 2, 5, "pixel");
        dia.addNumericField("Noise", noise, 2);
        dia.showDialog();
        rxy = (float) dia.getNextNumber();
        rz = (float) dia.getNextNumber();
        noise = (float) dia.getNextNumber();

        Prefs.set("mcib3d.maximafinder.radxy.double", rxy);
        Prefs.set("mcib3d.maximafinder.radz.double", rz);
        Prefs.set("mcib3d.maximafinder.noise.double", noise);

        return dia.wasOKed();

    }
}
