package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.Color;
import mcib3d.geom.Point3D;
import mcib_plugins.analysis.spatialAnalysis;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 17 juillet 2006
 */
public class Stats_Spat3D implements PlugInFilter {

    ImagePlus imp;
    Calibration calibration;
    int numPoints = 10000;
    int numRandomSamples = 100;
    double distHardCore = 0;
    double stepKFunction = 0.5d;
    double maxCoeffKFunction = 0.5d;
    int imaspots = 0;
    int imamask = 1;
    String[] names;
    Point3D[] evaluationPoints;
    private double env;
    String functions;
    boolean show;
    boolean save;

    /**
     * Description of the Method
     *
     * @param arg Description of the Parameter
     * @param imp Description of the Parameter
     * @return Description of the Return Value
     */
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL;
    }

    /**
     * Main processing method for the DilateKernel_ object
     *
     * @param ip Description of the Parameter
     */
    @Override
    public void run(ImageProcessor ip) {

        int nbima = WindowManager.getImageCount();
        names = new String[nbima];

        if (nbima < 2) {
            // try to extract mask roi
            ImagePlus plus = IJ.getImage();
            if ((plus != null) && (plus.getStackSize() == 1)) {
                IJ.log("Creating mask");
                createMask(plus);
            }
            nbima = WindowManager.getImageCount();
            names = new String[nbima];
            if (nbima < 2) {
                IJ.showMessage("Needs 2 images");
                return;
            }
        }

        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        if (!Dialogue()) {
            return;
        }
        imp = WindowManager.getImage(imaspots + 1);
        calibration = imp.getCalibration();

        spatialAnalysis spa = new spatialAnalysis(numPoints, numRandomSamples, distHardCore, env);
        spa.setColorsPlot(Color.DARK_GRAY, Color.LIGHT_GRAY, Color.MAGENTA);
        spa.process(imp, WindowManager.getImage(imamask + 1), functions, true, show, save);
        spa.getRandomSample().show("Random Sample");
    }

    private boolean Dialogue() {
        GenericDialog gd = new GenericDialog("Spatial Statistics");
        gd.addMessage("Choose stat functions to evaluate");
        gd.addCheckboxGroup(1, 3, new String[]{"F", "G", "H"}, new boolean[]{true, true, true});
        gd.addNumericField("Nb_points (F function)", Prefs.get("Analysis_F_numPoints.double", numPoints), 0);
        gd.addNumericField("Samples", numRandomSamples, 0);
        gd.addNumericField("Distance hardcore (unit)", distHardCore, 3);
        gd.addNumericField("Error %", 5, 0);
        gd.addChoice("Spots", names, names[0]);
        gd.addChoice("Mask", names, names[1]);
        gd.addCheckbox("Show plots", true);
        gd.addCheckbox("Save plots", true);
        gd.showDialog();
        functions = "";
        if (gd.getNextBoolean()) {
            functions = functions.concat("F");
        }
        if (gd.getNextBoolean()) {
            functions = functions.concat("G");
        }
        if (gd.getNextBoolean()) {
            functions = functions.concat("H");
        }
        if (functions.isEmpty()) {
            IJ.log("Choose at least one function");
            return false;
        }
        numPoints = (int) gd.getNextNumber();
        numRandomSamples = (int) gd.getNextNumber();
        distHardCore = gd.getNextNumber();
        env = gd.getNextNumber() / 100.0;
        imaspots = gd.getNextChoiceIndex();
        imamask = gd.getNextChoiceIndex();
        show = gd.getNextBoolean();
        save = gd.getNextBoolean();
        Prefs.set("Analysis_F_numPoints.double", numPoints);

        return (gd.wasOKed());
    }

    private void createMask(ImagePlus plus) {
        ImageProcessor mask = new ByteProcessor(plus.getWidth(), plus.getHeight());
        Roi roi = plus.getRoi();
        if (roi == null) {
            return;
        }
        ImageProcessor ma = roi.getMask();
        
        mask.insert(ma, roi.getBounds().x, roi.getBounds().y);

//        for (int x = 0; x < plus.getWidth(); x++) {
//            for (int y = 0; y < plus.getHeight(); y++) {
//                if (roi.contains(x, y)) {
//                    mask.putPixel(x, y, 255);
//                }
//            }
//        }
        ImagePlus plusMask = new ImagePlus("mask", mask);
        if (plus.getCalibration() != null) {
            plusMask.setCalibration(plus.getCalibration());
        }
        plusMask.show();
        IJ.log("Mask created");
    }
}
