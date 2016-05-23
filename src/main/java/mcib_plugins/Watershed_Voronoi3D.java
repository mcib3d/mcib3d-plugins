package mcib_plugins;

//import fish.FishImage3D;
//import fish.FishObject;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.distanceMap3d.EDT;
import mcib3d.image3d.regionGrowing.Watershed3D;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 16 avril 2006
 */
public class Watershed_Voronoi3D implements PlugIn {
    // seeds

    ImagePlus seedPlus = null;

    boolean anim = false;

    @Override
    public void run(String arg) {
        int nbima = WindowManager.getImageCount();

        if (nbima < 1) {
            IJ.showMessage("No image opened !");
            return;
        }
        seedPlus = WindowManager.getCurrentImage();
        WatershedVoronoi();
    }

    private void WatershedVoronoi() {
        IJ.log("");
        long t = System.currentTimeMillis();
        float resXY = 1;
        float resZ = 1;
        Calibration cal = seedPlus.getCalibration();
        if (cal != null) {
            resXY = (float) cal.pixelWidth;
            resZ = (float) cal.pixelDepth;
        }
        IJ.log("Computing EDT");
        ImageInt imgSeeds = ImageInt.wrap(seedPlus);
        ImageFloat edt = EDT.run(imgSeeds, 0, resXY, resZ, true, 0);
        ImageHandler edt16 = edt.convertToShort(true);
        edt16.invert();
        edt16.show("EDT");
        IJ.log("Computing watershed");
        Watershed3D water = new Watershed3D(edt16, imgSeeds, 0, 0);
        water.setAnim(anim);
        ImageInt voronoi = water.getWatershedImage3D();
        voronoi.show("VoronoiZones");
        // lines
        Object3DVoxels zero = new Object3DVoxels(voronoi, 0);
        Objects3DPopulation pop = new Objects3DPopulation(voronoi);
        ObjectCreator3D draw = new ObjectCreator3D(seedPlus.getWidth(), seedPlus.getHeight(), seedPlus.getNSlices());
        draw.getImageHandler().fill(1);
        zero.draw(draw, 0);
        for (int o = 0; o < pop.getNbObjects(); o++) {
            Object3DVoxels obj = (Object3DVoxels) pop.getObject(o);
            obj.computeContours();
            if (seedPlus.getNSlices() > 1)
                obj.drawContours(draw, 0);
            else {
                obj.drawContoursXY(draw, 0, 0);
            }
        }
        draw.getImageHandler().show("VoronoiLines");
        IJ.log("Finished in " + (System.currentTimeMillis() - t) + " ms.");
    }
}
