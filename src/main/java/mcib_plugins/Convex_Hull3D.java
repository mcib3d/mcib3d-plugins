package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;

import java.text.NumberFormat;

/**
 * plugin to compute the 3D hull, based on quickHull
 *
 * @author Thomas BOUDIER
 * @created avril 2003
 */
public class Convex_Hull3D implements PlugInFilter {

    ImagePlus imp;
    float rad;

    /**
     * Main processing method for the plugin
     *
     * @param ip image to process, must be labelled
     */
    @Override
    public void run(ImageProcessor ip) {
        Calibration cal = imp.getCalibration();
        double resXY = 1.0;
        double resZ = 1.0;
        String unit = "pix";
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.pixelWidth;
                resZ = cal.pixelDepth;
                unit = cal.getUnits();
            }
        }
        // no need calibration for computing hull
        ImageInt ima = ImageInt.wrap(imp.duplicate());
        ima.setScale(1, 1, "pix");
        //drawing of hulls
        ObjectCreator3D hulls = new ObjectCreator3D(ima.sizeX, ima.sizeY, ima.sizeZ);
        hulls.setResolution(resXY, resZ, unit);
        Object3DVoxels obj;
        // all objects from count masks
        int valmin = (int) ima.getMinAboveValue(0);
        int valmax = (int) ima.getMax();
        for (int val = valmin; val <= valmax; val++) {
            IJ.showStatus("Trying obj " + val);
            obj = new Object3DVoxels(ima, val);
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);
            if (obj.getVolumePixels() > 0) {
                IJ.log("Processing obj " + val);
                Object3D objC = obj.getConvexObject();
                objC.draw(hulls, val);
            }
        }
        ImageHandler imageHandler = hulls.getImageHandler();
        if (cal != null) {
            imageHandler.setCalibration(cal);
        }
        ImagePlus plus = imageHandler.getImagePlus();
        plus.setCalibration(cal);
        plus.setSlice(plus.getStackSize() / 2);
        plus.setDisplayRange(0, valmax);
        plus.setTitle("convex_Hull");
        plus.show();
    }

    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G + DOES_16 + NO_CHANGES;
    }
}
