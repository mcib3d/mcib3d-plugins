package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.PairColocalisation;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

public class Interactions_ implements PlugIn {

    @Override
    public void run(String s) {
        ImagePlus plus = WindowManager.getCurrentImage();
        ImageHandler img = ImageHandler.wrap(plus);

        IJ.log("Loose");
        interactionsLoose(img, 3, 3, 3);

        IJ.log("DamLines");
        interactionsDamLines(img);

        IJ.log("Contours");
        interactionsContours(img);
    }


    private void interactionsLoose(ImageHandler image, int rx, int ry, int rz) {
        Objects3DPopulation population = new Objects3DPopulation(image);
        for (Object3D object3D : population.getObjectsList()) {
            Object3D dilated = object3D.getDilatedObject(rx, ry, rz);
            LinkedList<Voxel3D> contours = dilated.getContours();
            ArrayUtil arrayUtil = new ArrayUtil(contours.size());
            int c = 0;
            for (Voxel3D voxel3D : contours) {
                if (image.contains(voxel3D)) {
                    arrayUtil.putValue(c, image.getPixel(voxel3D));
                    c++;
                }
            }
            arrayUtil.setSize(c);
            ArrayUtil distinctValues = arrayUtil.distinctValues();
            for (int i = 0; i < distinctValues.size(); i++) {
                int other = distinctValues.getValueInt(i);
                if (other == 0) continue;
                IJ.log("Object " + object3D.getValue() + " : " + other + " " + arrayUtil.countValue(other));
            }
            IJ.log("");
        }
    }

    private void interactionsDamLines(ImageHandler image) {
        Objects3DPopulation population = new Objects3DPopulation(image);
        HashMap<String, PairColocalisation> map = new HashMap<>();
        for (int z = 0; z < image.sizeZ; z++) {
            for (int x = 0; x < image.sizeX; x++) {
                for (int y = 0; y < image.sizeY; y++) {
                    if (image.getPixel(x, y, z) == 0) {
                        ArrayUtil util = image.getNeighborhood3x3x3(x, y, z);
                        util = util.distinctValues();
                        int c = 0;
                        for (int i = 0; i < util.size(); i++) {
                            for (int j = i + 1; j < util.size(); j++) {
                                if ((util.getValueInt(i) > 0) && (util.getValueInt(j) > 0)) {
                                    String key = util.getValueInt(i) + "-" + util.getValueInt(j);
                                    if (!map.containsKey(key)) {
                                        PairColocalisation pairColocalisation = new PairColocalisation(population.getObjectByValue(i), population.getObjectByValue(j));
                                        map.put(key, pairColocalisation);
                                    }
                                    map.get(key).incrementVolumeColoc();
                                    c++;
                                }
                            }
                        }
                        if (c > 1) IJ.log("Multiple point " + c + " : " + x + " " + y + " " + z);
                    }
                }
            }
        }
        for (String key : map.keySet()) {
            IJ.log("" + key + " " + map.get(key).getVolumeColoc());
        }
    }

    private void interactionsContours(ImageHandler image) {
        Objects3DPopulation population = new Objects3DPopulation(image);
        HashMap<String, PairColocalisation> map = new HashMap<>();
        for (Object3D object3D : population.getObjectsList()) {
            LinkedList<Voxel3D> list = object3D.getContours();
            for (Voxel3D voxel3D : list) {
                ArrayUtil util = image.getNeighborhood3x3x3(voxel3D.getRoundX(), voxel3D.getRoundY(), voxel3D.getRoundZ());
                util = util.distinctValues();
                int c = 0;
                for (int i = 0; i < util.size(); i++) {
                    for (int j = i + 1; j < util.size(); j++) {
                        if ((util.getValueInt(i) > 0) && (util.getValueInt(j) > 0)) {
                            if ((util.getValueInt(i) == object3D.getValue()) || (util.getValueInt(j) == object3D.getValue())) {
                                String key = util.getValueInt(i) + "-" + util.getValueInt(j);
                                if (!map.containsKey(key)) {
                                    PairColocalisation pairColocalisation = new PairColocalisation(population.getObjectByValue(i), population.getObjectByValue(j));
                                    map.put(key, pairColocalisation);
                                }
                                map.get(key).incrementVolumeColoc();
                                c++;
                            }
                        }
                    }
                }
                if (c > 1)
                    IJ.log("Multiple point " + c + " : " + voxel3D.getRoundX() + " " + voxel3D.getRoundY() + " " + voxel3D.getRoundZ());
            }
        }
        for (String key : map.keySet()) {
            IJ.log("" + key + " " + map.get(key).getVolumeColoc());
        }
    }
}

