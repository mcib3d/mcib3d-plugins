package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import java.awt.Color;
import mcib3d.geom.Point3D;
import mcib3d.utils.ThreadUtil;
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
    // double stepKFunction = 0.5d;
    // double maxCoeffKFunction = 0.5d;
    int imaspots = 0;
    int imamask = 1;
    String[] names;
    Point3D[] evaluationPoints;
    private int env = 5;
    String functions;
    boolean show;
    boolean save;
    int indexcol = 0;
    Color colorDraw = null;
    private int nbcpus;

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
        spatialAnalysis spa = new spatialAnalysis(numPoints, numRandomSamples, distHardCore, env / 100.0);
        spa.setMultiThread(nbcpus);
        spa.setColorsPlot(Color.DARK_GRAY, Color.LIGHT_GRAY, colorDraw);
        spa.process(imp, WindowManager.getImage(imamask + 1), functions, true, show, save);
        spa.getRandomSample().show("Random Sample");
    }

    private boolean Dialogue() {
        numPoints = (int) Prefs.get("Analysis_F_numPoints.double", numPoints);
        numRandomSamples = (int) Prefs.get("Analysis_F_numRandom.double", numRandomSamples);
        distHardCore = Prefs.get("Analysis_F_hardCore.double", distHardCore);
        env = (int) Prefs.get("Analysis_F_env.double", env);
        indexcol = (int) Prefs.get("Analysis_F_col.double", indexcol);
        String[] colors = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Orange", "Pink", "Black"};
        GenericDialog gd = new GenericDialog("Spatial Statistics");
        gd.addMessage("Choose stat functions to evaluate");
        gd.addCheckboxGroup(1, 3, new String[]{"F", "G", "H"}, new boolean[]{true, true, false});
        gd.addNumericField("Nb_points (F function)", Prefs.get("Analysis_F_numPoints.double", numPoints), 0);
        gd.addNumericField("Samples", numRandomSamples, 0);
        gd.addNumericField("Distance hardcore (" + imp.getCalibration().getUnits() + ")", distHardCore, 3);
        gd.addNumericField("Error %", env, 0);
        gd.addChoice("Spots", names, names[0]);
        gd.addChoice("Mask", names, names[1]);
        gd.addSlider("MultiThread", 1, ThreadUtil.getNbCpus(), ThreadUtil.getNbCpus());
        gd.addMessage("Plot options");
        gd.addChoice("Draw_color:", colors, colors[indexcol]);
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
        env = (int) gd.getNextNumber();
        imaspots = gd.getNextChoiceIndex();
        imamask = gd.getNextChoiceIndex();
        nbcpus = (int) gd.getNextNumber();
        indexcol = gd.getNextChoiceIndex();
        show = gd.getNextBoolean();
        save = gd.getNextBoolean();

        Prefs.set("Analysis_F_numPoints.double", numPoints);
        Prefs.set("Analysis_F_numRandom.double", numRandomSamples);
        Prefs.set("Analysis_F_hardCore.double", distHardCore);
        Prefs.set("Analysis_F_env.double", env);
        Prefs.set("Analysis_F_col.double", indexcol);

        switch (indexcol) {
            case 0:
                colorDraw = Color.red;
                break;
            case 1:
                colorDraw = Color.green;
                break;
            case 2:
                colorDraw = Color.blue;
                break;
            case 3:
                colorDraw = Color.cyan;
                break;
            case 4:
                colorDraw = Color.magenta;
                break;
            case 5:
                colorDraw = Color.yellow;
                break;
            case 6:
                colorDraw = Color.orange;
                break;
            case 7:
                colorDraw = Color.pink;
                break;
            default:
                colorDraw = Color.black;
        }

        return (gd.wasOKed());
    }

    private void createMask(ImagePlus plus) {
        ImageProcessor mask = new ShortProcessor(plus.getWidth(), plus.getHeight());
        Roi roi = plus.getRoi();
        if (roi == null) {
            return;
        }
        ImageProcessor ma = roi.getMask();

        mask.insert(ma, roi.getBounds().x, roi.getBounds().y);

        ImagePlus plusMask = new ImagePlus("mask", mask);
        if (plus.getCalibration() != null) {
            plusMask.setCalibration(plus.getCalibration());
        }
        plusMask.show();
        IJ.log("Mask created");
    }
}
