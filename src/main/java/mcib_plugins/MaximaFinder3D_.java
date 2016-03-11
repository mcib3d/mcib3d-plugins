/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.MaximaFinder;

/**
 *
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

        return PlugInFilter.DOES_16;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (dialog()) {
            ImageInt img = ImageInt.wrap(plus);
            MaximaFinder test = new MaximaFinder(img, noise);
            test.getPeaks().show();
        }
    }

    private boolean dialog() {
        GenericDialog dia = new GenericDialog("Maxima Finder");
        dia.addNumericField("RadiusXY", rxy, 2, 5, "pixel");
        dia.addNumericField("RadiusZ", rz, 2, 5, "pixel");
        dia.addNumericField("Noise", noise, 2);
        dia.showDialog();
        rxy = (float) dia.getNextNumber();
        rz = (float) dia.getNextNumber();
        noise = (float) dia.getNextNumber();

        return dia.wasOKed();

    }
}
