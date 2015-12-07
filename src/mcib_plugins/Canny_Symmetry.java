/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.processing.CannyEdge3D;
import mcib3d.image3d.processing.SymmetryFilter;

/**
 *
 * @author thomasb
 */
public class Canny_Symmetry implements PlugInFilter {

    ImagePlus plus;

    double alpha = Prefs.get("mcib_symmetry_alpha.double ", 0.5);
    int radius = (int) Prefs.get("mcib_symmetry_radius.int", 10);
    double normalize = Prefs.get("mcib_symmetry_normalize.double", 10);
    double scaling = Prefs.get("mcib_symmetry_scaling.double", 2);
    boolean showEdgesXYZ = false;
    boolean showIntermediate = false;
    boolean improved = true;

    @Override
    public int setup(String string, ImagePlus ip) {
        plus = ip;

        return DOES_16 + DOES_8G + DOES_32;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (!dialog()) {
            return;
        }

        ImageHandler img = ImageHandler.wrap(plus);

        CannyEdge3D edges = new CannyEdge3D(img, alpha);
        ImageHandler[] gg = edges.getGradientsXYZ();

        if (showEdgesXYZ) {
            gg[0].show("EdgeX");
            gg[1].show("EdgeY");
            gg[2].show("EdgeZ");
        }

        ImageHandler ed = edges.getEdge();
        ed.show("Edges");

        SymmetryFilter sy = new SymmetryFilter(gg, radius, improved);
        // optional
        sy.setNormalize(normalize); // default 10
        sy.setScaling(scaling); // default 2
        sy.setImproved(improved); // default true

        if (showIntermediate) {
            sy.getIntermediates()[0].show("Bin");
            sy.getIntermediates()[1].show("BinEdge");
        }

        sy.getSymmetry(false).show("Symmetry_" + radius);
        sy.getSymmetry(true).show("Symmetry_smoothed_" + radius);
    }

    private boolean dialog() {
        GenericDialog gd = new GenericDialog("Edge and Symmetry");
        gd.addMessage("Edge detection Canny Deriche");
        gd.addNumericField("alpha Canny", alpha, 3, 10, "");
        gd.addCheckbox("Show edges in X-Y-Z", showEdgesXYZ);
        gd.addMessage("Symmetry detection");
        gd.addNumericField("Radius", radius, 0, 10, "pix");
        gd.addNumericField("Normalization", normalize, 2, 10, "");
        gd.addNumericField("Scaling", scaling, 2, 10, "");
        gd.addCheckbox("Show intermediates", showIntermediate);
        gd.addCheckbox("Improved seed detection ", improved);
        gd.showDialog();
        alpha = gd.getNextNumber();
        showEdgesXYZ = gd.getNextBoolean();
        radius = (int) gd.getNextNumber();
        normalize = gd.getNextNumber();
        scaling = gd.getNextNumber();
        showIntermediate = gd.getNextBoolean();
        improved = gd.getNextBoolean();

        // Prefs
        Prefs.set("mcib_symmetry_alpha.double ", alpha);
        Prefs.set("mcib_symmetry_radius.int", radius);
        Prefs.set("mcib_symmetry_normalize.double", normalize);
        Prefs.set("mcib_symmetry_scaling.double", scaling);

        return gd.wasOKed();
    }
}
