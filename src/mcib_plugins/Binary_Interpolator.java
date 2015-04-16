/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 *
 * @author jean
 */
public class Binary_Interpolator implements PlugInFilter {

    ImagePlus plus;

    public int setup(String arg, ImagePlus imp) {
        plus = imp;
        return DOES_16 + DOES_8G + STACK_REQUIRED;
    }

    public void run(ImageProcessor ip) {
        //do the binary interpolation
        ImageStack stack = plus.getStack();
        BinaryInterpolator bi = new BinaryInterpolator();
        bi.run(stack);
        plus.updateAndDraw();
        //ImagePlus binary = new ImagePlus("interpolated", stack);
    }
}
