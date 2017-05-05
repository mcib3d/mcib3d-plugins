/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.NewImage;
import ij.measure.Calibration;
import ij.plugin.filter.EDM;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

/**
 * @author jean
 */
public class Binary_Interpolator implements PlugInFilter {

    ImagePlus plus;

    public int setup(String arg, ImagePlus imp) {
        plus = imp;
        return DOES_8G + STACK_REQUIRED + NO_CHANGES;
    }

    public void run(ImageProcessor ip) {
        GenericDialog genericDialog = new GenericDialog("Interpolate Binary");
        genericDialog.addCheckbox("Make isotropic ?", true);
        genericDialog.showDialog();
        if (genericDialog.wasCanceled()) return;
        boolean isotropic = genericDialog.getNextBoolean();
        isotropic(plus, isotropic).show();
    }

    private ImagePlus isotropic(ImagePlus plus, boolean iso) {
        double ratio = (plus.getCalibration()).getZ(1) / (plus.getCalibration()).getX(1);
        ImageStack stack = plus.getStack();
        int size = stack.getSize();
        if (!iso) ratio = 1;
        int nsize = (int) Math.ceil(size * ratio);
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

        // edm
        EDM edm = new EDM();
        ImagePlus edmImage = NewImage.createFloatImage("edm", stack.getWidth(), stack.getHeight(), nsize, 1);
        ImageStack edmStack = edmImage.getStack();
        for (int s = 1; s <= edmImage.getNSlices(); s++) {
            ImageProcessor ip = imageStack.getProcessor(s).duplicate();
            ip.invert();
            FloatProcessor fp = edm.makeFloatEDM(ip, 0, false);
            edmStack.setProcessor(fp, s);
        }

        int s0 = findNextNonZero(edmStack, 1);
        while (s0 < edmStack.getSize()) {
            int s1 = findNextNonZero(edmStack, s0);
            if ((s1 < edmStack.getSize()) && ((s1 - s0) > 1))
                fillInterpolate(imageStack, edmStack, s0, s1);
            s0 = s1;
        }

        return imagePlus;
    }

    private int findNextNonZero(ImageStack stack, int s0) {
        s0++;
        ImageStatistics imageStatistics = ImageStatistics.getStatistics(stack.getProcessor(s0));
        while ((imageStatistics.min == imageStatistics.max) && (s0 < stack.getSize())) {
            s0++;
            imageStatistics = ImageStatistics.getStatistics(stack.getProcessor(s0));
        }

        return s0;

    }

    private void fillInterpolate(ImageStack stack, ImageStack edm, int s0, int s1) {
        IJ.log("Interpolating between " + s0 + " " + s1);
        // interpolate between s0 and s1
        int delta = s1 - s0;
        if (delta < 2) return;
        ImageProcessor frame0 = edm.getProcessor(s0);
        ImageProcessor frame1 = edm.getProcessor(s1);
        for (int s = 1; s < delta; s++) {
            ImageProcessor ipp = stack.getProcessor(s + s0);
            for (int x = 0; x < stack.getWidth(); x++) {
                for (int y = 0; y < stack.getHeight(); y++) {
                    float value = frame0.getPixelValue(x, y) * (s1 - (s + s0)) + frame1.getPixelValue(x, y) * ((s + s0) - s0);
                    if (value < delta) ipp.putPixelValue(x, y, 255.0);
                }
            }
        }
    }
}
