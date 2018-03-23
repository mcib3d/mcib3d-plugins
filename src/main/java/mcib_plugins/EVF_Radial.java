package mcib_plugins;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

public class EVF_Radial implements PlugIn {
    int nbEVF = 0;
    int nbSig = 1;
    double step = 0.01;
    boolean average = true;
    boolean density = false;

    @Override
    public void run(String arg) {
        if (WindowManager.getImageCount() < 2) {
            IJ.showMessage("Needs two images, one EVF and one signal");
            return;
        }
        if (dialog()) {
            ImageHandler evf = ImageHandler.wrap(WindowManager.getImage(nbEVF + 1));
            ImageHandler raw = ImageHandler.wrap(WindowManager.getImage(nbSig + 1));
            float[] sum = new float[(int) (1.0 / step)];
            float[] count = new float[(int) (1.0 / step)];
            float[] vol = new float[(int) (1.0 / step)];
            float[] idx = new float[(int) (1.0 / step)];
            for (int z = 0; z < evf.sizeZ; z++) {
                for (int y = 0; y < evf.sizeY; y++) {
                    for (int x = 0; x < evf.sizeX; x++) {
                        float evfPix = evf.getPixel(x, y, z);
                        if (evfPix > 0) {
                            int bin = (int) Math.floor(evfPix / step);
                            if (bin >= sum.length) bin = sum.length - 1;
                            float rawPix = raw.getPixel(x, y, z);
                            if (rawPix > 0) {
                                sum[bin] += rawPix;
                                count[bin]++;
                            }
                            vol[bin]++;
                        }
                    }
                }
            }

            for (int i = 0; i < sum.length; i++) {
                sum[i] /= count[i];
                count[i] /= vol[i];
                idx[i] = (float) (i * step);
            }
            if (average) {
                Plot plot = new Plot("Average Intensity", "evf", "avg", idx, sum);
                plot.show();
            }
            if (density) {
                Plot plot1 = new Plot("Density", "evf", "density", idx, count);
                plot1.show();
            }

            Plot plot2 = new Plot("Volume of Layers", "evf", "volume", idx, vol);
            plot2.show();
        }
    }

    private boolean dialog() {
        int nbima = WindowManager.getImageCount();
        String[] names = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        GenericDialog dialog = new GenericDialog("EVF Radial Analysis");
        dialog.addNumericField("Step : ", step, 3);
        dialog.addChoice("EVF : ", names, names[nbEVF]);
        dialog.addChoice("Signal : ", names, names[nbSig]);
        String[] values = {"Average Intensity", "Density", "Both"};
        dialog.addChoice("Values : ", values, values[0]);
        dialog.showDialog();
        step = dialog.getNextNumber();
        nbEVF = dialog.getNextChoiceIndex();
        nbSig = dialog.getNextChoiceIndex();
        int choice = dialog.getNextChoiceIndex();
        if (choice == 0) {
            average = true;
            density = false;
        } else if (choice == 1) {
            average = false;
            density = true;
        } else {
            average = true;
            density = true;
        }

        return dialog.wasOKed();
    }
}
