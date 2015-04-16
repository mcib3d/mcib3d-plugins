package mcib_plugins;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import java.awt.Rectangle;
import mcib3d.image3d.ImageInt;

/**
 * plugin de crop 3D
 *
 * @author Thomas BOUDIER @created juin 2007
 */
public class Crop3D_ implements PlugInFilter {

    ImagePlus imp;
    private int radXY = 10;
    private int radZ = 3;

    /**
     * Main processing method for the Crop3D_ object
     *
     * @param ip Description of the Parameter
     */
    @Override
    public void run(ImageProcessor ip) {

        if (Dialogue()) {
            Roi roi = imp.getRoi();
            Rectangle rect = roi.getBounds();
            int la = rect.width;
            int ha = rect.height;
            int x0 = rect.x + la / 2;
            int y0 = rect.y + ha / 2;
            int z0=imp.getSlice();
            ImageInt ima=ImageInt.wrap(imp);
            ImageInt ima2=ima.cropRadius(x0, y0, z0,radXY, radXY, radZ, true, true);
            ima2.getImagePlus().show();
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private boolean Dialogue() {
        GenericDialog gd = new GenericDialog("3D spherical crop");
        gd.addNumericField("Radius_XY (pix)", radXY, 0);
        gd.addNumericField("Radius_Z (pix)", radZ, 0);
        gd.showDialog();
        radXY = (int) gd.getNextNumber();
        radZ = (int) gd.getNextNumber();
        return (!gd.wasCanceled());
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
        return DOES_8G + DOES_16 + ROI_REQUIRED;
    }
}
