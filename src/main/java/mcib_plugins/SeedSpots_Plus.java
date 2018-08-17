package mcib_plugins;

import ij.*;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import java.util.ArrayList;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.Segment3DSpots;
import mcib3d.image3d.processing.FastFilters3D;
import mcib_plugins.tools.RoiManager3D_2;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class SeedSpots_Plus implements PlugIn {
    // seeds

    ImagePlus seedPlus;
    ImageStack seedStack;
    ImageHandler seed3DImage;
    // spots
    ImagePlus spotPlus;
    ImageStack spotStack;
    ImageHandler spot3DImage;
    // segResulrs
    //IntImage3D fishImage;
    ImagePlus segPlus = null;
    ImageStack segStack;
    // res
    Calibration spotCalib = null;
    double resXY = 0.1328;
    double resZ = 0.2;
    double radiusFixed = 0;
    double weight = 0.5;
    int local_method = 0;
    int spot_method = 0;
    int output = 0;
    int seeds_threshold = 15;
    int local_background = 65;
    // local mean
    float rad0 = 2;
    float rad1 = 4;
    float rad2 = 6;
    double we = 0.5;
    // gauss_fit
    int radmax = 10;
    double sdpc = 1.0;
    private boolean watershed = true;
    private int radiusSeeds = 2;
    // volumes (pix)
    int volumeMin = 1;
    int volumeMax = 1000000;
    String[] local_methods = {"Constant", "Diff", "Local Mean", "Gaussian fit"};
    String[] spot_methods = {"Classical", "Maximum", "Block"};
    String[] outputs = {"Label Image", "Roi Manager 3D", "Both"};
    // DB
    private boolean debug = true;
    private boolean bigLabel;
    private int diff = 0;

    @Override
    public void run(String arg) {
        IJ.log("3D spots segmentation");
        int nbima = WindowManager.getImageCount();
        String[] names = new String[nbima + 1];

        int seed, spot;

        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        names[nbima] = "Automatic";

        if (nbima == 0) {
            IJ.log("Image required :");
            return;
        }
        // only spots
        if (nbima < 2) {
            spot = 0;
            seed = 1;
        } else {
            seed = 0;
            spot = 1;
        }

        // get Preferences
        seeds_threshold = (int) Prefs.get("SeedSpots_GlobalBackground.int", seeds_threshold);
        local_background = (int) Prefs.get("SeedSpots_LocalBackground.int", local_background);
        diff = (int) Prefs.get("SeedSpots_Diff.int", diff);
        rad0 = (float) Prefs.get("SeedSpots_Rad0.real", rad0);
        rad1 = (float) Prefs.get("SeedSpots_Rad1.real", rad1);
        rad2 = (float) Prefs.get("SeedSpots_Rad2.real", rad2);
        we = (float) Prefs.get("SeedSpots_Weight.real", we);
        radmax = (int) Prefs.get("SeedSpots_RadMax.int", radmax);
        sdpc = Prefs.get("SeedSpots_SDPC.real", sdpc);
        local_method = (int) Prefs.get("SeedSpots_LocalMethod.int", local_method);
        spot_method = (int) Prefs.get("SeedSpots_SpotMethod.int", spot_method);
        output = (int) Prefs.get("SeedSpots_Output.int", output);
        watershed = Prefs.get("SeedSpots_Watershed.boolean", watershed);
        volumeMin = (int) Prefs.get("SeedSpots_volMin.int", volumeMin);
        volumeMax = (int) Prefs.get("SeedSpots_volMax.int", volumeMax);

        // in case old values was stored
        if (spot_method >= spot_methods.length) {
            spot_method = 0;
        }

        GenericDialog dia = new GenericDialog("Seeds spots");
        //dia.addNumericField("Res_XY (um)", resXY, 4);
        //dia.addNumericField("Res_Z (um)", resZ, 4);
        dia.addNumericField("Seeds_threshold", seeds_threshold, 0);
        dia.addNumericField("Local_Background (0=auto)", local_background, 0);
        dia.addChoice("Local_Threshold method", local_methods, local_methods[local_method]);
        dia.addNumericField("Local_diff", diff, 0);
        dia.addMessage("Local_parameters (local mean)");
        dia.addNumericField("Radius_0", rad0, 2);
        dia.addNumericField("Radius_1", rad1, 2);
        dia.addNumericField("Radius_2", rad2, 2);
        dia.addNumericField("Weigth", we, 2);
        dia.addMessage("Local_parameters (Gauss Fit)");
        dia.addNumericField("Radius_max", radmax, 2);
        dia.addNumericField("sd_value", sdpc, 2);
        dia.addChoice("Seg_spot method", spot_methods, spot_methods[spot_method]);
        dia.addCheckbox("Watershed", watershed);
        dia.addNumericField("Volume_Min (pix)", volumeMin, 0);
        dia.addNumericField("Volume_Max (pix)", volumeMax, 0);
        dia.addChoice("Seeds", names, names[seed]);
        dia.addChoice("Spots", names, names[spot]);
        dia.addNumericField("Radius_for_seeds (automatic)", radiusSeeds, 0);
        dia.addChoice("Output", outputs, outputs[output]);
        dia.addCheckbox("32-bits label", false);
        dia.addCheckbox("Verbose", false);
        dia.showDialog();
        if (dia.wasOKed()) {
            seeds_threshold = (int) dia.getNextNumber();
            local_background = (int) dia.getNextNumber();
            diff = (int) dia.getNextNumber();
            rad0 = (float) dia.getNextNumber();
            rad1 = (float) dia.getNextNumber();
            rad2 = (float) dia.getNextNumber();
            we = dia.getNextNumber();
            radmax = (int) dia.getNextNumber();
            sdpc = dia.getNextNumber();
            local_method = dia.getNextChoiceIndex();
            spot_method = dia.getNextChoiceIndex();
            watershed = dia.getNextBoolean();
            volumeMin = (int) dia.getNextNumber();
            volumeMax = (int) dia.getNextNumber();
            seed = dia.getNextChoiceIndex();
            spot = dia.getNextChoiceIndex();
            radiusSeeds = (int) dia.getNextNumber();
            output = dia.getNextChoiceIndex();
            bigLabel = dia.getNextBoolean();
            debug = dia.getNextBoolean();

            // set Preferences
            Prefs.set("SeedSpots_GlobalBackground.int", seeds_threshold);
            Prefs.set("SeedSpots_LocalBackground.int", local_background);
            Prefs.set("SeedSpots_Diff.int", diff);
            Prefs.set("SeedSpots_Rad0.real", rad0);
            Prefs.set("SeedSpots_Rad1.real", rad1);
            Prefs.set("SeedSpots_Rad2.real", rad2);
            Prefs.set("SeedSpots_Weight.real", we);
            Prefs.set("SeedSpots_RadMax.int", radmax);
            Prefs.set("SeedSpots_SDPC.real", sdpc);
            Prefs.set("SeedSpots_LocalMethod.int", local_method);
            Prefs.set("SeedSpots_SpotMethod.int", spot_method);
            Prefs.set("SeedSpots_Output.int", output);
            Prefs.set("SeedSpots_Watershed.boolean", watershed);
            Prefs.set("SeedSpots_volMin.int", volumeMin);
            Prefs.set("SeedSpots_volMax.int", volumeMax);

            IJ.log("Initial.....");
            spotPlus = WindowManager.getImage(spot + 1);
            spotStack = spotPlus.getImageStack();
            spot3DImage = ImageHandler.wrap(spotPlus);

            if (seed < nbima) {
                seedPlus = WindowManager.getImage(seed + 1);
                seedStack = seedPlus.getImageStack();
                seed3DImage = ImageHandler.wrap(seedPlus);
            } else {
                computeSeeds();
            }
            if (spotPlus.getCalibration() != null) {
                spotCalib = spotPlus.getCalibration();
            }

            IJ.log("Spot segmentation.....");
            this.Segmentation();
            IJ.log("Finished");
            if (segPlus != null) {
                segPlus.show();
            }
            IJ.log("Finished");
        }
    }

    private void computeSeeds() {
        seed3DImage = ImageHandler.wrap(FastFilters3D.filterIntImageStack(spotStack, FastFilters3D.MAXLOCAL, (float) radiusSeeds, (float) radiusSeeds, (float) radiusSeeds, 0, false));
    }

    private void Segmentation() {
        Segment3DSpots seg = new Segment3DSpots(this.spot3DImage, this.seed3DImage);
        seg.show = debug;
        // set parameters
        seg.setSeedsThreshold(this.seeds_threshold);
        seg.setLocalThreshold(local_background);
        seg.setWatershed(watershed);
        seg.setVolumeMin(volumeMin);
        seg.setVolumeMax(volumeMax);
        IJ.log("Spot Image: " + seg.getRawImage().getTitle() + "   Seed Image : " + seg.getSeeds().getTitle());
        IJ.log("Vol min: " + seg.getVolumeMin() + "   Vol max: " + seg.getVolumeMax());
        switch (local_method) {
            case 0:
                seg.setMethodLocal(Segment3DSpots.LOCAL_CONSTANT);
                break;
            case 1:
                seg.setMethodLocal(Segment3DSpots.LOCAL_DIFF);
                seg.setLocalDiff(diff);
                break;
            case 2:
                seg.setMethodLocal(Segment3DSpots.LOCAL_MEAN);
                seg.setRadiusLocalMean(rad0, rad1, rad2, we);
                break;
            case 3:
                seg.setMethodLocal(Segment3DSpots.LOCAL_GAUSS);
                seg.setGaussPc(sdpc);
                seg.setGaussMaxr(radmax);
                break;
        }
        switch (spot_method) {
            case 0:
                seg.setMethodSeg(Segment3DSpots.SEG_CLASSICAL);
                break;
            case 1:
                seg.setMethodSeg(Segment3DSpots.SEG_MAX);
                break;
            case 2:
                seg.setMethodSeg(Segment3DSpots.SEG_BLOCK);
                break;
        }
        // big label (more than 2^16 objects)
        seg.bigLabel = bigLabel;
        seg.segmentAll();
        int size = seg.getObjects().size();
        IJ.log("Number of labelled objects: " + size);
        // output        
        if ((output == 0) || (output == 2)) {
            //segPlus = new ImagePlus("seg", seg.getLabelImage().getImageStack());
            segPlus = new ImagePlus("seg", seg.getLabelImage().getImageStack());
            if (spotCalib != null) {
                segPlus.setCalibration(spotCalib);
            }
        }
        if ((output == 1) || (output == 2)) {
            ArrayList<Object3D> Objects = seg.getObjects();
            RoiManager3D_2 roimanager = new RoiManager3D_2();
            roimanager.addObjects3D(Objects);
        }
    }
}
