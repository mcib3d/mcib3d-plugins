package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.processing.Density3D;

public class Density3D_ implements PlugIn {
    private double sigma = 20;
    private int neighbours = 10;
    private boolean multi = true;

    @Override
    public void run(String arg) {
        // get Preferences
        this.sigma = (int) Prefs.get("mcib_density3d.double", this.sigma);
        this.neighbours = (int) Prefs.get("mcib_density3d.int", this.neighbours);
        if (dialog()) {
            // set Preferences
            Prefs.set("mcib_density3d.double", this.sigma);
            Prefs.set("mcib_density3d.int", this.neighbours);
            ImagePlus plus = WindowManager.getCurrentImage();
            if (plus == null) {
                IJ.error("Open an image with spots to compute their density");
                return;
            }
            ImageInt handler = ImageInt.wrap(plus);
            // test if labelled
            if (handler.isBinary(0)) {
                ImageLabeller labeller = new ImageLabeller();
                handler = labeller.getLabels(handler);
            }
            final ImageInt img = handler;
            Objects3DPopulation population = new Objects3DPopulation(img);
            population.createKDTreeCenters();
            // density
            Density3D density3D = new Density3D(neighbours, sigma);
            ImageHandler res = density3D.computeDensity(img, multi);
            res.show("density3D");
        }
        System.gc();
    }

    private boolean dialog() {

        GenericDialog dialog = new GenericDialog("Density3D");
        dialog.addNumericField("Radius (unit)", sigma, 2);
        dialog.addNumericField("NbNeighbors", neighbours, 0);
        dialog.addCheckbox("MultiThread", multi);
        dialog.showDialog();
        this.sigma = dialog.getNextNumber();
        this.neighbours = (int) dialog.getNextNumber();
        this.multi = dialog.getNextBoolean();

        return dialog.wasOKed();
    }


    private void densityProcess(ImageHandler in, Objects3DPopulation population, ImageHandler out, int zmin, int zmax, int nk, double sigma) {
        zmax = Math.min(zmax, in.sizeZ);
        double coeff = 1.0 / (2.0 * sigma * sigma);
        for (int z = zmin; z < zmax; z++) {
            IJ.showStatus("Density slice " + z);
            for (int x = 0; x < in.sizeX; x++) {
                for (int y = 0; y < in.sizeY; y++) {
                    double[] dists = population.kClosestDistancesSquared(x, y, z, nk);
                    double density = 0;
                    for (int i = 0; i < nk; i++) {
                        density += Math.exp(-dists[i] * coeff);
                    }
                    out.setPixel(x, y, z, (float) density);
                }
            }
        }
    }
}
