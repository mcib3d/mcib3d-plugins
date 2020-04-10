package mcib_plugins;

//import fish.FishImage3D;
//import fish.FishObject;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.regionGrowing.Watershed3DVoronoi;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class Watershed_Voronoi3D implements PlugIn {
    // seeds
    ImagePlus seedPlus = null;
    float radMax = 0;
    boolean showLines = false;
    boolean showEDT = false;

    @Override
    public void run(String arg) {
        int nbima = WindowManager.getImageCount();

        if (nbima < 1) {
            IJ.showMessage("No image opened !");
            return;
        }
        seedPlus = WindowManager.getCurrentImage();
        if (Dialog())
            WatershedVoronoi();
    }

    private void WatershedVoronoi() {
        IJ.log("");
        long t = System.currentTimeMillis();
        ImageHandler image = ImageHandler.wrap(seedPlus);
        Watershed3DVoronoi watershed3DVoronoi = new Watershed3DVoronoi(image, radMax);
        //if (image.isBinary()) watershed3DVoronoi.setLabelSeeds(true);// FIXME check binary for ImageHandler
        //else watershed3DVoronoi.setLabelSeeds(false);
        watershed3DVoronoi.getVoronoiZones(showEDT).show("VoronoiZones");
        if (showLines) watershed3DVoronoi.getVoronoiLines(true).show("VoronoiLines");
        IJ.log("Finished in " + (System.currentTimeMillis() - t) + " ms.");
    }

    private boolean Dialog() {
        Calibration calibration = seedPlus.getCalibration();
        String unit = "pixel";
        if (calibration != null) unit = calibration.getUnits();
        GenericDialog genericDialog = new GenericDialog("Voronoi");
        genericDialog.addNumericField("Radius_Max (0 for no max)", radMax, 2, 10, unit);
        genericDialog.addCheckbox("Show_EDT", showEDT);
        genericDialog.addCheckbox("Show_Lines", showLines);
        genericDialog.showDialog();
        radMax = (float) genericDialog.getNextNumber();
        if (radMax == 0) radMax = Float.MAX_VALUE;
        showEDT = genericDialog.getNextBoolean();
        showLines = genericDialog.getNextBoolean();

        return genericDialog.wasOKed();
    }
}
