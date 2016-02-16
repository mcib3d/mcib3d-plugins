package mcib_plugins;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import imagescience.image.Image;
import mcib_plugins.processing.Thresholder;

public class Hysteresis_Thresholding implements PlugInFilter {

    ImagePlus plus = null;

    @Override
    public int setup(String arg, ImagePlus imp) {
        plus = imp;

        return PlugInFilter.DOES_8G + PlugInFilter.DOES_16;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Hysteresis Thresholding (ImageScience)");
        gd.addNumericField("High Threshold:", 128, 1);
        gd.addNumericField("Low Threshold:", 50, 1);
        gd.addCheckbox("Connectivity 4", false);
        gd.showDialog();
        if (gd.wasOKed()) {
            double high = gd.getNextNumber();
            double low = gd.getNextNumber();
            hysteresis(plus , low, high, gd.getNextBoolean());
            plus.setDisplayRange(0, 255);
            plus.updateAndRepaintWindow();
        }
    }
    
    public static void hysteresis(ImagePlus image, double lowval, double highval, boolean lowConnectivity) {
        final Image imp = Image.wrap(image);
        final Thresholder thres = new Thresholder();
        if (lowConnectivity) {
            thres.hysteresisLowConnectivity(imp, lowval, highval);
        } else {
            thres.hysteresis(imp, lowval, highval);
        }        
//        final ImageInt img=ImageInt.wrap(image);
//        ImageHandler.convertToByte(img);
//        image=img.getImagePlus();
    }
}
