/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
 * @author jean
 */
public class Binary_Interpolator implements PlugInFilter {

    ImagePlus plus;

    public int setup(String arg, ImagePlus imp) {
        plus = imp;
        return DOES_16 + DOES_8G + STACK_REQUIRED;
    }

    public void run(ImageProcessor ip) {
        GenericDialog genericDialog = new GenericDialog("Interpolate Binary");
        genericDialog.addCheckbox("Make isotropic ?", true);
        genericDialog.showDialog();
        if (genericDialog.wasCanceled()) return;
        boolean isotropic = genericDialog.getNextBoolean();
        //do the binary interpolation
        BinaryInterpolator bi = new BinaryInterpolator();
        if (isotropic) {
            ImagePlus imagePlus = isotropic(plus);
            bi.run(imagePlus.getStack());
            imagePlus.show();
        } else {
            bi.run(plus.getStack());
            plus.updateAndDraw();
        }
    }

    private ImagePlus isotropic(ImagePlus plus) {
        double ratio = (plus.getCalibration()).getZ(1) / (plus.getCalibration()).getX(1);
        ImageStack stack = plus.getStack();
        int size = stack.getSize();
        int nsize = (int) Math.round(size * ratio);
        ImagePlus imagePlus = NewImage.createByteImage("interpolated", stack.getWidth(), stack.getHeight(), nsize, 1);
        Calibration calibration = plus.getCalibration();
        Calibration calibration1 = new Calibration(imagePlus);
        calibration1.pixelWidth = calibration.pixelWidth;
        calibration1.pixelHeight = calibration.pixelHeight;
        calibration1.pixelDepth = calibration.pixelDepth / ratio;
        calibration1.setUnit(calibration.getUnit());
        imagePlus.setCalibration(calibration1);
        ImagePlus plus1 = plus.duplicate();
        ImageStack stack1 = plus1.getImageStack();
        ImageStack imageStack = imagePlus.getImageStack();
        for (int s = 0; s < plus.getNSlices(); s++) {
            imageStack.setProcessor(stack1.getProcessor(s + 1), (int) Math.round(s * ratio) + 1);
        }

        return imagePlus;
    }
}
