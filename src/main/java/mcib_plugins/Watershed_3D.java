package mcib_plugins;

//import fish.FishImage3D;
//import fish.FishObject;

import ij.*;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.image3d.regionGrowing.Watershed3D;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class Watershed_3D implements PlugIn {
    // seeds

    ImagePlus seedPlus;
    ImageStack seedStack;
    // spots
    ImagePlus spotPlus;
    ImageStack spotStack;
    // res
    double resXY = 0.1328;
    double resZ = 0.2;
    int seeds_threshold = 7000;
    int voxels_threshold = 0;
    int rad = 2;

    boolean anim = false;

    @Override
    public void run(String arg) {
        int nbima = WindowManager.getImageCount();

        if (nbima < 1) {
            IJ.showMessage("No image opened !");
            return;
        }

        String[] namesRaw = new String[nbima];
        String[] namesSeeds = new String[nbima + 1];

        namesSeeds[0] = "Automatic";
        for (int i = 0; i < nbima; i++) {
            namesRaw[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesSeeds[i + 1] = WindowManager.getImage(i + 1).getShortTitle();
        }

        // get Preferences
        seeds_threshold = (int) Prefs.get("Watershed3D_SeedsThreshold.int", seeds_threshold);
        voxels_threshold = (int) Prefs.get("Watershed3D_VoxelsThreshold.int", voxels_threshold);

        int spot = 0;
        int seed = nbima > 1 ? nbima - 1 : 0;

        GenericDialog dia = new GenericDialog("Seeds Watershed");
        dia.addNumericField("Seeds_Threshold", seeds_threshold, 0);
        dia.addNumericField("Image_Threshold", voxels_threshold, 0);
        dia.addChoice("Image", namesRaw, namesRaw[spot]);
        dia.addChoice("Seeds", namesSeeds, namesSeeds[seed]);
        dia.addNumericField("Radius for automatic seeds", rad, 0);
        dia.addCheckbox("Show animation (slow)", anim);

        dia.showDialog();
        if (dia.wasOKed()) {
            seeds_threshold = (int) dia.getNextNumber();
            voxels_threshold = (int) dia.getNextNumber();
            spot = dia.getNextChoiceIndex();
            seed = dia.getNextChoiceIndex();
            rad = (int) dia.getNextNumber();
            anim = dia.getNextBoolean();

            // set Preferences
            Prefs.set("Watershed3D_SeedsThreshold.int", seeds_threshold);
            Prefs.set("Watershed3D_VoxelsThreshold.int", voxels_threshold);

            spotPlus = WindowManager.getImage(spot + 1);
            spotStack = spotPlus.getImageStack();

            if (seed > 0) {
                seedPlus = WindowManager.getImage(seed);
                seedStack = seedPlus.getImageStack();
            } else {
                IJ.log("Computing seeds as local maxima");
                if (spotStack.getBitDepth() < 32) {
                    seedStack = FastFilters3D.filterIntImageStack(spotStack, FastFilters3D.MAXLOCAL, rad, rad, rad, 0, true);
                } else {
                    seedStack = FastFilters3D.filterFloatImageStack(spotStack, FastFilters3D.MAXLOCAL, rad, rad, rad, 0, true);
                }
            }
            this.Watershed();
        }
    }

    private void Watershed() {
        IJ.log("Computing watershed");
        long t = System.currentTimeMillis();
        Watershed3D water = new Watershed3D(spotStack, seedStack, voxels_threshold, seeds_threshold);
        water.setLabelSeeds(true);
        water.setAnim(anim);
        water.getWatershedImage3D().show();
        IJ.log("Finished in " + (System.currentTimeMillis() - t) + " ms.");
    }
}
