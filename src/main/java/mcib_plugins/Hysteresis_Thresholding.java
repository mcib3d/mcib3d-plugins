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
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.processing.Flood3D;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Hysteresis_Thresholding implements PlugInFilter {
    ImagePlus plus;

    @Override
    public int setup(String arg, ImagePlus imp) {
        plus = imp;

        return PlugInFilter.DOES_8G + PlugInFilter.DOES_16 + PlugInFilter.DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        GenericDialog gd = new GenericDialog("Hysteresis Thresholding");
        gd.addNumericField("High Threshold:", 128, 1);
        gd.addNumericField("Low Threshold:", 50, 1);
        gd.addCheckbox("Show multi-threshold", false);
        gd.addCheckbox(" Labelling", true);
        gd.showDialog();
        if (gd.wasOKed()) {
            double high = gd.getNextNumber();
            double low = gd.getNextNumber();
            boolean show = gd.getNextBoolean();
            boolean label = gd.getNextBoolean();

            Instant t0 = Instant.now();
            ImagePlus hyst = hysteresis(plus, low, high, show, label);
            Instant t1 = Instant.now();
            hyst.setDisplayRange(0, 255);
            hyst.show();
            IJ.log("Hysteresis took " + Duration.between(t0, t1));
        }
    }


    public ImagePlus hysteresis(ImagePlus image, double lowval, double highval, boolean show, boolean label) {
        int HIGH = 255;
        int LOW = 128;
        // first threshold the image
        ImageHandler img = ImageHandler.wrap(image);
        ImageByte multi = new ImageByte(image.getTitle() + "_Multi", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            for (int xy = 0; xy < img.sizeXY; xy++) {
                if (img.getPixel(xy, z) > highval) {
                    multi.setPixel(xy, z, HIGH);
                } else if (img.getPixel(xy, z) > lowval) {
                    multi.setPixel(xy, z, LOW);
                }
            }
        }
        if (show) multi.show();

        ImageHandler thresholded = multi.thresholdAboveInclusive(LOW);
        ImageLabeller labeller = new ImageLabeller();
        ArrayList<Object3DVoxels> objects = labeller.getObjects(thresholded);

        ImageHandler hyst;
        if(label) hyst= new ImageShort("HystLabel_" + image.getTitle(), multi.sizeX, multi.sizeY, multi.sizeZ);
        else hyst= new ImageByte("HystBin_" + image.getTitle(), multi.sizeX, multi.sizeY, multi.sizeZ);
        hyst.setScale(img);
        int val = 1;
        for (Object3DVoxels object3DVoxels : objects) {
            if (hasOneVoxelValueRange(object3DVoxels, multi, HIGH, HIGH)) {
                if (label)
                    object3DVoxels.draw(hyst, val++);
                else object3DVoxels.draw(hyst, 255);
            }
        }

        return hyst.getImagePlus();
    }

    private boolean hasOneVoxelValueRange(Object3DVoxels object3DVoxels, ImageHandler handler, int t0, int t1) {
        List<Voxel3D> list = object3DVoxels.getVoxels();
        for (Voxel3D vox : list) {
            float pix = handler.getPixel(vox);
            if ((pix >= t0) && (pix <= t1)) return true;
        }

        return false;
    }
}
