package mcib_plugins;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.Flood3D;

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
        gd.showDialog();
        if (gd.wasOKed()) {
            double high = gd.getNextNumber();
            double low = gd.getNextNumber();
            ImagePlus hyst = hysteresis(plus, low, high);
            hyst.setDisplayRange(0, 255);
            hyst.show();
        }
    }

    public static ImagePlus hysteresis(ImagePlus image, double lowval, double highval) {
        // first threshold the image
        ImageInt img = ImageInt.wrap(image);
        ImageByte temp = new ImageByte("bin", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (img.getPixel(xy, z) > highval) {
                    temp.setPixel(xy, z, 255);
                } else if (img.getPixel(xy, z) > lowval) {
                    temp.setPixel(xy, z, 128);
                }
            }
        }
        // connect 255 to 128
        Flood3D.connect3D(temp, 128, 255);

        return temp.getImagePlus();
    }
}
