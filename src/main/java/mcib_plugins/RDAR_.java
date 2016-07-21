package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.RDAR;
import mcib3d.image3d.ImageHandler;

/**
 * Created by thomasb on 19/7/16.
 */
public class RDAR_ implements PlugInFilter {
    private ImagePlus imagePlus;

    @Override
    public int setup(String arg, ImagePlus imp) {
        imagePlus = imp;
        return DOES_8G + DOES_16;
    }

    @Override
    public void run(ImageProcessor ip) {
        Object3DVoxels object3DVoxels = new Object3DVoxels(ImageHandler.wrap(imagePlus));
        double r1 = object3DVoxels.getRadiusMoments(2);
        double rad1 = r1;
        double rad2 = Double.NaN;
        if (!Double.isNaN(object3DVoxels.getMainElongation())) {
            rad2 = rad1 / object3DVoxels.getMainElongation();
        }
        double rad3 = Double.NaN;
        if (!Double.isNaN(object3DVoxels.getMedianElongation())) {
            rad3 = rad2 / object3DVoxels.getMedianElongation();
        }
        RDAR rdar = new RDAR(object3DVoxels, (int) rad1, (int) rad2, (int) rad3);
        IJ.log("Nb " + rdar.getPartsInNumber(100) + " " + rdar.getPartsOutNumber(100));

        // drawing
        ImageHandler imageHandler = ImageHandler.wrap(imagePlus.duplicate());
        imageHandler.fill(0);
        //object3DVoxels.draw(imageHandler, 255);
        if (rdar.getPartsIn(100) != null)
            for (Object3DVoxels part : rdar.getPartsIn(100)) part.draw(imageHandler, 200);
        if (rdar.getPartsOut(100) != null)
            for (Object3DVoxels part : rdar.getPartsOut(100)) part.draw(imageHandler, 150);
        imageHandler.show("draw");
    }
}