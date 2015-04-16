/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.BinaryMorpho;
import mcib3d.utils.ThreadUtil;
import mcib3d.utils.exceptionPrinter;

/**
 *
 * @author jean
 */
public class Binary_Close implements PlugInFilter {

    boolean debug;
    // TODO utiliser une methode classique si le rayon est petit    
    float radiusXY, radiusZ;
    ImagePlus plus;

    public ImageInt runPostFilter(ImageInt input) {
        try {
            float radXY = Math.max(radiusXY, 1);
            float radZ = Math.max(radiusZ, 0);
            if (debug) {
                IJ.log("binaryClose: radius XY" + radXY + " radZ:" + radZ);
            }
            return BinaryMorpho.binaryCloseMultilabel(input, radXY, radZ, ThreadUtil.getNbCpus());
        } catch (Exception e) {
            exceptionPrinter.print(e, "", true);
        }
        return null;
    }

    public int setup(String arg, ImagePlus imp) {
        plus = imp;
        return PlugInFilter.DOES_8G + PlugInFilter.DOES_16 + PlugInFilter.STACK_REQUIRED;
    }

    public void run(ImageProcessor ip) {
        IJ.showStatus("binaryClose");
        GenericDialog gd = new GenericDialog("BinaryClose");
        gd.addNumericField("radiusXY (pix):", 5, 1);
        gd.addNumericField("radiusZ (pix):", 3, 1);
        gd.showDialog();
        ImageInt input = ImageInt.wrap(plus);
        //IJ.log("input "+input.getMax());
        if (gd.wasOKed()) {
            radiusXY = (float) gd.getNextNumber();
            radiusZ = (float) gd.getNextNumber();
            ImageHandler res = runPostFilter(input);
            //IJ.log("res "+res.getMax());
            ImagePlus resplus = res.getImagePlus();
            Calibration cal = plus.getCalibration();
            if (cal != null) {
                resplus.setCalibration(cal);
            }
            resplus.setTitle("CloseLabels");
            resplus.show();
        }
    }
}
