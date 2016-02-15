/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.processing;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.processing.BinaryMorpho;
import mcib3d.utils.exceptionPrinter;

/**
 *
 * @author jean
 */
public class Binary_Open implements PlugIn {
    // TODO utiliser une methode classique si le rayon est petit

    boolean debug;
    float radiusXY, radiusZ;

    public ImageInt runPostFilter(ImageInt input) {
        try {
            float radXY = Math.max(radiusXY, 1);
            float radZ = Math.max(radiusZ, 0);
            if (debug) {
                IJ.log("binaryOpen: radius XY" + radXY + " radZ:" + radZ);
            }

            return BinaryMorpho.binaryOpenMultilabel(input, radXY, radZ, 0);
        } catch (Exception e) {
            exceptionPrinter.print(e, "", true);
        }
        return null;
    }

    @Override
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null || imp.getBitDepth() != 8) {
            IJ.log("8-bit thresholded image");
            return;
        }
        IJ.showStatus("binaryOpen");
        GenericDialog gd = new GenericDialog("BinaryOpen");
        gd.addNumericField("radiusXY:", 5, 1);
        gd.addNumericField("radiusZ:", 3, 1);
        gd.showDialog();
        if (gd.wasOKed()) {
            double radXY = gd.getNextNumber();
            double radZ = gd.getNextNumber();
            ImageHandler res = runPostFilter((ImageInt) ImageHandler.wrap(imp));
            res.show(imp.getTitle() + "::open");
        }
    }
}
