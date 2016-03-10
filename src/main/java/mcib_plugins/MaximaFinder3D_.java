/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
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

    @Override
    public int setup(String arg, ImagePlus imp) {
        plus = imp;

        return PlugInFilter.DOES_16;
    }

    @Override
    public void run(ImageProcessor ip) {
        ImageInt img = ImageInt.wrap(plus);
        int noise=(int) IJ.getNumber("Noise ", 100);
        MaximaFinder test = new MaximaFinder(img, noise);
        IJ.log("compute list");
        test.computeMaximaList();
        IJ.log("compute peaks");
        test.drawPeaks();
         IJ.log("show peaks");
        test.getPeaks().show();

    }

}
