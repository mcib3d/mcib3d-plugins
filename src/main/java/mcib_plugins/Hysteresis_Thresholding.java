package mcib_plugins;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.Flood3D;

import java.time.Duration;
import java.time.Instant;

public class Hysteresis_Thresholding implements PlugInFilter {
    ImagePlus plus;

    @Override
    public int setup(String arg, ImagePlus imp) {
        plus = imp;

        return PlugInFilter.DOES_8G + PlugInFilter.DOES_16;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Hysteresis Thresholding");
        gd.addNumericField("High Threshold:", 128, 1);
        gd.addNumericField("Low Threshold:", 50, 1);
        gd.addCheckbox("Show multi-threshold", false);
        gd.showDialog();
        if (gd.wasOKed()) {
            double high = gd.getNextNumber();
            double low = gd.getNextNumber();

            ImagePlus hyst = hysteresis(plus, low, high, gd.getNextBoolean());
            hyst.setDisplayRange(0, 255);
            hyst.show();
        }
    }


    public static ImagePlus hysteresis(ImagePlus image, double lowval, double highval, boolean show) {
        int HIGH = 255;
        int LOW = 128;
        // first threshold the image
        ImageInt img = ImageInt.wrap(image);
        ImageByte temp = new ImageByte(image.getTitle() + "_Multi", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (img.getPixel(xy, z) > highval) {
                    temp.setPixel(xy, z, HIGH);
                } else if (img.getPixel(xy, z) > lowval) {
                    temp.setPixel(xy, z, LOW);
                }
            }
        }
        ImageInt hyst;
        if (show) {
            temp.show();
            hyst = temp.duplicate();
        } else hyst = temp;
        hyst.setTitle(image.getTitle() + "_Hyst");
        // connect 255 to 128
        Instant t0 = Instant.now();
        Flood3D.connect3D(hyst, LOW, HIGH);
        Instant t1 = Instant.now();
        IJ.log("Flooding took " + Duration.between(t0, t1));

        return hyst.getImagePlus();
    }
}
