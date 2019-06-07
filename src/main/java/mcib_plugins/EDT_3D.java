package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;

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

        namesMask[0] = "None";
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
        //dia.addChoice("Mask (for EVF)", namesMask, namesMask[mask]);
        dia.addNumericField("Threshold", threshold, 0);
        dia.addCheckbox("Inverse", inverse);
        dia.showDialog();
        String map = dia.getNextChoice();
        struct = dia.getNextChoiceIndex();
        //mask = dia.getNextChoiceIndex();
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
                        if (inverse) {
                            imgMask = imgMask.duplicate();
                            imgMask.invert();
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

//    public void normalizeDistanceMap(ImageFloat distanceMap, ImageInt mask) {
//        int count = 0;
//        Vox[] idx = new Vox[mask.countMaskVolume()];
//        double volume = idx.length;
//        for (int z = 0; z < distanceMap.sizeZ; z++) {
//            for (int xy = 0; xy < distanceMap.sizeXY; xy++) {
//                if (mask.getPixelInt(xy, z) != 0) {
//                    idx[count] = new Vox(distanceMap.pixels[z][xy], xy, z);
//                    count++;
//                }
//            }
//        }
//        Arrays.sort(idx);
//        for (int i = 0; i < idx.length - 1; i++) {
//            // gestion des repetitions
//            if (idx[i + 1].distance == idx[i].distance) {
//                int j = i + 1;
//                while (j < (idx.length - 1) && idx[i].distance == idx[j].distance) {
//                    j++;
//                }
//                double median = (i + j) / 2d;
//                for (int k = i; k <= j; k++) {
//                    idx[k].index = median;
//                }
//                i = j;
//            } else {
//                idx[i].index = i;
//            }
//        }
//        if (idx[idx.length - 1].index == 0) {
//            idx[idx.length - 1].index = idx.length - 1;
//        }
//        for (Vox idx1 : idx) {
//            distanceMap.pixels[idx1.z][idx1.xy] = (float) (idx1.index / volume);
//        }
//    }

    protected class Vox implements Comparable<Vox> {

        float distance;
        double index;
        int xy, z;

        public Vox(float distance, int xy, int z) {
            this.distance = distance;
            this.xy = xy;
            this.z = z;
        }

        public Vox(float distance, double index, int xy, int z) {
            this.distance = distance;
            this.index = index;
            this.xy = xy;
            this.z = z;
        }

        @Override
        public int compareTo(Vox v) {
            if (distance > v.distance) {
                return 1;
            } else if (distance < v.distance) {
                return -1;
            } else {
                return 0;
            }
        }
    }
}
