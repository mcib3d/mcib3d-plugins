package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.io.FileSaver;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.RoiManager;
import ij.process.ImageProcessor;
import java.awt.List;
import java.io.File;
import mcib3d.image3d.ImageInt;

/**
 * plugin de crop 3D
 *
 * @author Thomas BOUDIER @created juin 2007
 */
public class Crop3D_All implements PlugInFilter {

    ImagePlus imp;
    int radX = 10, radY = 10, radZ = 5;
    String dir = "embryo";
    String name = "N";
    boolean sp = false;

    /**
     * Main processing method for the Crop3D_ object
     *
     * @param ip Description of the Parameter
     */
    @Override
    public void run(ImageProcessor ip) {
        Calibration cal = imp.getCalibration();

        if (Dialogue()) {
            RoiManager roimanager = RoiManager.getInstance();
            if (roimanager == null) {
                IJ.error("No roiManager opened ");
                return;
            }
            int nb = roimanager.getCount();
            if (nb == 0) {
                IJ.error("RoiManager empty ! ");
                return;
            }
            List list = roimanager.getList();
            Roi[] rois = roimanager.getRoisAsArray();
            ImageInt ima = ImageInt.wrap(imp);
            String s;
            int x, y, slice;
            Roi roi;
            //Image3D res;
            ImagePlus plus;
            FileSaver fs;

            for (int i = 0; i < nb; i++) {
                s = list.getItem(i);
                slice = roimanager.getSliceNumber(s);
                roi = rois[i];
                x = roi.getBounds().x;
                y = roi.getBounds().y;

                File folder = new File(dir);
                folder.mkdir();

                ImageInt res = ima.cropRadius(x, y, slice, radX, radY, radZ, true, true);
                res.setTitle(name + (i + 1));
                plus = res.getImagePlus();
                //res = ima.extract(x, y, slice, radX, radY, radZ, sp);
                //plus = new ImagePlus(, res.getStack());
                if (cal != null) {
                    plus.setCalibration(cal);
                }
                fs = new FileSaver(plus);
                fs.saveAsTiffStack(dir + "/" + name + (i + 1) + ".tif");
            }
        }
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private boolean Dialogue() {
        GenericDialog gd = new GenericDialog("3D Crop All");
        gd.addStringField("Folder", dir, 50);
        gd.addStringField("Name", name);
        gd.addNumericField("Radius_X", radX, 0);
        gd.addNumericField("Radius_Y", radY, 0);
        gd.addNumericField("Radius_Z", radZ, 0);
        gd.addCheckbox("Sphere", sp);
        gd.showDialog();

        dir = gd.getNextString();
        name = gd.getNextString();
        radX = (int) gd.getNextNumber();
        radY = (int) gd.getNextNumber();
        radZ = (int) gd.getNextNumber();
        sp = gd.getNextBoolean();

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
        return DOES_8G + DOES_16;
    }
}
