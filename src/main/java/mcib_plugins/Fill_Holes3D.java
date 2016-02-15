package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.processing.FillHoles3D;

public class Fill_Holes3D implements PlugInFilter {
ImagePlus plus;
    
    public void run(String arg) {
        ImagePlus imp = IJ.getImage();
        if (imp == null || imp.getBitDepth() != 8) {
            
            IJ.log("8-bit thresholded image");
            return;
        }

        GenericDialog gd = new GenericDialog("FillHoles3D will override current image. process?");
        gd.showDialog();
        if (gd.wasOKed()) {
        }
    }

    public int setup(String arg, ImagePlus imp) {
       plus=imp;
       
       return PlugInFilter.DOES_8G;
    }

    public void run(ImageProcessor ip) {
        ImageByte im = new ImageByte(plus);
        double max = im.getMax();
        if (max != 255) {
            ij.IJ.log("values must be 0 and 255!");
            return;
        }
        FillHoles3D.process(im, 255, Runtime.getRuntime().availableProcessors(), false);
        plus.updateAndDraw();
    }
}
