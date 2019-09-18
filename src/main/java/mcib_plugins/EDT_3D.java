package mcib_plugins;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.distanceMap3d.EDT;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Euclidean Distance Map and Eroded Volume Fraction
 * (originally written by Jean Ollion for TANGO)
 */
public class EDT_3D implements PlugIn {

    boolean inverse = false;
    int threshold = 1;

    @Override
    public void run(String arg) {
//        ImagePlus imp = IJ.getImage();
//        String title = imp.getTitle();

        int nbima = WindowManager.getImageCount();
        if (nbima < 1) {
            IJ.showMessage("No image opened !");
            return;
        }
        String[] namesStructure = new String[nbima];
        String[] namesMask = new String[nbima + 1];

        namesMask[0] = "Same";
        for (int i = 0; i < nbima; i++) {
            namesStructure[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesMask[i + 1] = WindowManager.getImage(i + 1).getShortTitle();
        }

        int struct = 0;
        int mask = 0; // use default mask
        //int mask = nbima > 1 ? nbima - 1 : 0;

        GenericDialog dia = new GenericDialog("EDT");
        dia.addChoice("Map", new String[]{"EDT", "EVF", "Both"}, "EDT");
        dia.addChoice("Image", namesStructure, namesStructure[struct]);
        dia.addChoice("Mask (for EVF)", namesMask, namesMask[mask]);
        dia.addNumericField("Threshold", threshold, 0);
        dia.addCheckbox("Inverse", inverse);
        dia.showDialog();
        String map = dia.getNextChoice();
        struct = dia.getNextChoiceIndex();
        mask = dia.getNextChoiceIndex();
        threshold = (int) dia.getNextNumber();
        inverse = dia.getNextBoolean();

        if (dia.wasOKed()) {
            try {
                ImageHandler img = ImageHandler.wrap(WindowManager.getImage(struct + 1));
                IJ.log("Computing Distance Map (EDT) ...");
                ImageFloat r = EDT.run(img, threshold, inverse, 0);
                if (r != null) {
                    r.setTitle("EDT_" + namesStructure[struct]);
                    if (map.compareTo("EVF") != 0) {
                        r.show("EDT");
                    }
                    // EVF 
                    if (map.compareTo("EDT") != 0) {
                        ImageFloat r2 = r.duplicate();
                        ImageHandler imgMask = img.thresholdAboveExclusive(threshold);
                        if (mask > 0) {
                            imgMask = ImageHandler.wrap(WindowManager.getImage(mask));
                        }
                        // check if inverse case
                        if (inverse) { // do not invert if other image
                            if (mask == 0) {
                                imgMask = imgMask.duplicate();
                                imgMask.invert();
                            }
                        }
                        IJ.log("Normalizing Distance Map (EVF) ...");
                        EDT.normalizeDistanceMap(r2, imgMask, true);
                        if (mask > 0) {
                            r2.intersectMask(imgMask);
                        }
                        r2.show("EVF");
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(EDT_3D.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
