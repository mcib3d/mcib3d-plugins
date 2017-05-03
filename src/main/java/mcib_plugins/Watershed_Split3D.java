package mcib_plugins;

//import fish.FishImage3D;
//import fish.FishObject;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.GaussianBlur3D;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.image3d.regionGrowing.Watershed3D;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class Watershed_Split3D implements PlugIn {
    // seeds

    ImagePlus seedPlus = null;
    ImageStack seedStack = null;
    // spots
    ImagePlus binaryMask;
    ImageStack binaryStack;
    float rad = 2;

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

        int mask = 0;
        int seed = nbima > 1 ? nbima - 1 : 0;

        GenericDialog dia = new GenericDialog("Watershed split");
        dia.addChoice("Binary mask", namesRaw, namesRaw[mask]);
        dia.addChoice("Seeds", namesSeeds, namesSeeds[seed]);
        dia.addNumericField("Radius (pixel)", rad, 0);

        dia.showDialog();
        if (dia.wasOKed()) {
            mask = dia.getNextChoiceIndex();
            seed = dia.getNextChoiceIndex();
            rad = (float) dia.getNextNumber();

            binaryMask = WindowManager.getImage(mask + 1);
            binaryStack = binaryMask.getImageStack();

            if (seed > 0) {
                seedPlus = WindowManager.getImage(seed);
                seedStack = seedPlus.getImageStack();
            }
            WatershedSplit();
        }
    }

    private void WatershedSplit() {
        IJ.log("");
        long t = System.currentTimeMillis();
        float resXY = 1;
        float resZ = 1;
        float radXY = rad;
        float radZ = rad;
        Calibration cal = binaryMask.getCalibration();
        if (cal != null) {
            resXY = (float) cal.pixelWidth;
            resZ = (float) cal.pixelDepth;
            radZ = radXY * (resXY / resZ);
        }
        IJ.log("Computing EDT");
        ImageInt imgMask = ImageInt.wrap(binaryMask);
        ImageFloat edt = EDT.run(imgMask, 0, resXY, resZ, false, 0);
        ImageHandler edt16 = edt.convertToShort(true);
        IJ.log("Smoothing EDT");
        ImagePlus edt16Plus = edt16.getImagePlus();
        GaussianBlur3D.blur(edt16Plus, 2.0, 2.0, 2.0);
        edt16 = ImageInt.wrap(edt16Plus);
        edt16.intersectMask(imgMask);
        edt16.show("EDT");
        // seeds
        ImageHandler seedsImg;
        if (seedPlus == null) {
            IJ.log("computing seeds as max local of EDT");
            seedsImg = FastFilters3D.filterImage(edt16, FastFilters3D.MAXLOCAL, radXY, radXY, radZ, 0, false);
        } else {
            seedsImg = ImageInt.wrap(seedPlus);
        }
        IJ.log("Computing watershed");
        Watershed3D water = new Watershed3D(edt16, seedsImg, 0, 0);
        water.setLabelSeeds(true);
        water.setAnim(anim);
        water.getWatershedImage3D().show("Split");
        IJ.log("Finished in " + (System.currentTimeMillis() - t) + " ms.");
    }
}
