package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.text.NumberFormat;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DSurface;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.legacy.IntImage3D;

/**
 * plugin to fit an 3D ellipsoid to a shape
 *
 * @author Thomas BOUDIER
 * @created avril 2003
 */
public class Convex_Hull3D implements PlugInFilter {

    ImagePlus imp;
    float rad;

    /**
     * Main processing method for the Axes3D_ object
     *
     * @param ip Description of the Parameter
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
        //drawing of hulls
        ObjectCreator3D hulls = new ObjectCreator3D(imp.getWidth(), imp.getHeight(), imp.getStackSize());
        hulls.setResolution(resXY, resZ, unit);
        Object3DVoxels obj;
        // all objects from count masks
        IntImage3D ima = new IntImage3D(imp.getStack());
        int valmin = (int) ima.getMinAboveValue(0);
        int valmax = (int) ima.getMaximum();
        for (int val = valmin; val <= valmax; val++) {
            IJ.showStatus("Trying obj " + val);
            obj = new Object3DVoxels(imp, val);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);

            if (obj.getVolumePixels() > 0) {
                IJ.log("\nProcessing obj " + val);
//                Object3DSurface surf = new Object3DSurface(obj.computeMeshSurface(false));
//                surf = surf.getConvexObject();
//                surf.multiThread = true;
//                obj = new Object3DVoxels(surf.getVoxels());
                Object3D objC = obj.getConvexObject(true);
                objC.draw(hulls, val);
                // obj.draw(hulls, val);
            }
        }
        ImagePlus plus = new ImagePlus("Convex_Hulls", hulls.getStack());
        if (cal != null) {
            plus.setCalibration(cal);
        }
        plus.setSlice(plus.getStackSize()/2);
        plus.setDisplayRange(0, valmax);
        plus.show();
    }

    /**
     * Description of the Method
     *
     * @param arg Description of the Parameter
     * @param imp Description of the Parameter
     * @return Description of the Return Value
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G + DOES_16 + NO_CHANGES;
    }
}
