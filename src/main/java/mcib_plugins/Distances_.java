package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import mcib3d.geom.Object3D;
import mcib3d.geom.ObjectDistBB;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.ObjectsPopulationDistances;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;

import java.util.ArrayList;
import java.util.HashMap;

public class Distances_ implements PlugIn {
    private static final int DCENTER = 0;
    private static final int DBORDER = 1;
    private static final int DHAUSDORFF = 2;
    private static boolean CENTER = true;
    private static boolean BORDER = false;
    private static boolean HAUSDORFF = false;
    // distances
    double[][] distances;

    @Override
    public void run(String s) {
        int nbima = WindowManager.getImageCount();
        if (nbima < 1) {
            IJ.error("Needs at least one labelled image");
            return;
        }
        // choice for results table
        String[] tables = new String[]{"Closest_1", "Closest_2", "All"};
        String[] closests = new String[]{"Center", "Border(slow)"};
        int compute;
        int closest;
        String[] namesA = new String[nbima];
        String[] namesB = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            namesA[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesB[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        int idxA = 0;
        int idxB = nbima > 1 ? 1 : 0;
        GenericDialog dia = new GenericDialog("All Distances");
        dia.addChoice("Image_A", namesA, namesA[idxA]);
        dia.addChoice("Image_B", namesB, namesB[idxB]);
        dia.addChoice("Compute", tables, tables[0]);
        dia.addChoice("Closest", closests, closests[0]);
        dia.addMessage("Distances :");
        dia.addCheckbox("Center", true);
        dia.addCheckbox("Border", false);
        dia.addCheckbox("Hausdorff", false);
        dia.showDialog();
        if (dia.wasOKed()) {
            idxA = dia.getNextChoiceIndex();
            idxB = dia.getNextChoiceIndex();
            compute = dia.getNextChoiceIndex();
            closest = dia.getNextChoiceIndex();
            CENTER = dia.getNextBoolean();
            BORDER = dia.getNextBoolean();
            HAUSDORFF = dia.getNextBoolean();
            String closestS;
            if (closest == 0) closestS = "cc";
            else closestS = "bb";

            IJ.log("");
            IJ.log("Building objects population");
            ImagePlus plusA = WindowManager.getImage(idxA + 1);
            ImagePlus plusB = WindowManager.getImage(idxB + 1);
            Objects3DPopulation population1 = new Objects3DPopulation(ImageInt.wrap(plusA));
            Objects3DPopulation population2 = new Objects3DPopulation(ImageInt.wrap(plusB));

            IJ.log("");
            IJ.log("Computing distances, please wait ...");
            ArrayList<Object3D>[] all = null;
            if (compute == 0) all = closestAll(population1, population2, 1, closestS);
            if (compute == 1) all = closestAll(population1, population2, 2, closestS);
            String title;
            if (CENTER) {
                title = "Distances_Center";
                if (compute == 2) {
                    getResultsTableAll(population1, population2, DCENTER).show(title);
                    getStatistics(population1, population2, DCENTER).show(title + "_Statistics");
                }
                if (compute == 0)
                    getResultsTableClosestK(all, population1, population2, 1, DCENTER, closestS).show(title);
                if (compute == 1)
                    getResultsTableClosestK(all, population1, population2, 2, DCENTER, closestS).show(title);
            }
            if (BORDER) {
                title = "Distances_Border";
                if (compute == 2) {
                    getResultsTableAll(population1, population2, DBORDER).show(title);
                    getStatistics(population1, population2, DBORDER).show(title + "_Statistics");
                }
                if (compute == 0)
                    getResultsTableClosestK(all, population1, population2, 1, DBORDER, closestS).show(title);
                if (compute == 1)
                    getResultsTableClosestK(all, population1, population2, 2, DBORDER, closestS).show(title);
            }
            if (HAUSDORFF) {
                title = "Distances_Hausdorff";
                if (compute == 2) {
                    getResultsTableAll(population1, population2, DHAUSDORFF).show(title);
                    getStatistics(population1, population2, DHAUSDORFF).show(title + "_Statistics");
                }
                if (compute == 0)
                    getResultsTableClosestK(all, population1, population2, 1, DHAUSDORFF, closestS).show(title);
                if (compute == 1)
                    getResultsTableClosestK(all, population1, population2, 2, DHAUSDORFF, closestS).show(title);
            }
            IJ.log("Finished");
        }
    }


    private ResultsTable getResultsTableClosestK(ArrayList<Object3D>[] all, Objects3DPopulation population1, Objects3DPopulation population2, int k, int choice, String type) {
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }
        rt.reset();
        //HashMap<String, Integer> colums = new HashMap(population2.getNbObjects());

        for (int ia = 0; ia < population1.getNbObjects(); ia++) {
            IJ.showStatus("Distances " + ia + " / " + population1.getNbObjects());
            //IJ.log("\\Update:Distances " + ia + " / " + population1.getNbObjects()+"              ");
            Object3D obj1 = population1.getObject(ia);
            rt.incrementCounter();
            rt.setLabel("A" + population1.getObject(ia).getValue(), ia);
            ArrayList<Object3D> list = all[ia];
            for (int i = 0; i < list.size(); i++) {
                Object3D object3D = list.get(i);
                double dist = 0;
                if (choice == DCENTER) dist = obj1.distCenterUnit(object3D);
                if (choice == DBORDER) dist = obj1.distBorderUnit(object3D);
                if (choice == DHAUSDORFF) dist = obj1.distHausdorffUnit(object3D);
                rt.setValue("Closest_" + (i + 1), ia, object3D.getValue());
                rt.setValue("Distance_" + (i + 1), ia, dist);
            }

        }
        return rt;
    }

    private ResultsTable getStatistics(Objects3DPopulation population1, Objects3DPopulation population2, int choice) {
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }
        rt.reset();

        ArrayUtil arrayUtil = new ArrayUtil(population1.getNbObjects() * population2.getNbObjects());
        int pos = 0;
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++)
            for (int i2 = 0; i2 < population2.getNbObjects(); i2++)
                arrayUtil.putValue(pos++, distances[i1][i2]);

        rt.setValue("Min", 0, arrayUtil.getMinimum());
        rt.setValue("Mean", 0, arrayUtil.getMean());
        rt.setValue("Max", 0, arrayUtil.getMaximum());
        rt.setValue("SD", 0, arrayUtil.getStdDev());

        return rt;
    }

    private ResultsTable getResultsTableAll(Objects3DPopulation population1, Objects3DPopulation population2, int choice) {
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }
        rt.reset();
        HashMap<String, Integer> colums = new HashMap(population2.getNbObjects());

        computeDistancesAll(population1, population2, choice);
        for (int ia = 0; ia < population1.getNbObjects(); ++ia) {
            IJ.showStatus("Distances " + ia + " / " + population1.getNbObjects());
            rt.incrementCounter();
            rt.setLabel("A" + population1.getObject(ia).getValue(), ia);
            for (int ib = 0; ib < population2.getNbObjects(); ++ib) {
                int v2;
                double dist = distances[ia][ib];
                // first row, headers
                if (ia == 0) {
                    v2 = population2.getObject(ib).getValue();
                    rt.setValue("B" + v2, ia, dist);
                    colums.put("B" + v2, rt.getColumnIndex("B" + v2));
                    // other rows
                } else
                    v2 = population2.getObject(ib).getValue();
                rt.setValue(colums.get("B" + v2), ia, dist);
            }
        }
        return rt;
    }

    private void computeDistancesAll(Objects3DPopulation population1, Objects3DPopulation population2, int choice) {
        distances = new double[population1.getNbObjects()][population2.getNbObjects()];
        for (int i1 = 0; i1 < population1.getNbObjects(); i1++) {
            Object3D obj1 = population1.getObject(i1);
            for (int i2 = 0; i2 < population2.getNbObjects(); i2++) {
                Object3D obj2 = population2.getObject(i2);
                if (choice == DHAUSDORFF) {
                    distances[i1][i2] = obj1.distHausdorffUnit(obj2);
                }
                if (choice == DCENTER) {
                    distances[i1][i2] = obj1.distCenterUnit(obj2);
                }
                if (choice == DBORDER) {
                    distances[i1][i2] = obj1.distBorderUnit(obj2);
                }
            }
        }
    }

    private ArrayList<Object3D> computeClosestK(Object3D obj1, Objects3DPopulation pop2, int k, String type) {
        ArrayList<Object3D> list = new ArrayList<>();

        if (type.equalsIgnoreCase("cc")) {
            Object3D[] closests = pop2.kClosestCentres(obj1.getCenterX(), obj1.getCenterY(), obj1.getCenterZ(), k);
            for (Object3D object3D : closests) list.add(object3D);
        }
        if (type.equalsIgnoreCase("bb")) {
            ObjectsPopulationDistances populationDistances = new ObjectsPopulationDistances(pop2);
            populationDistances.setCurrentObject(obj1);
            ArrayList<ObjectDistBB> distBBS = populationDistances.closestsBorderK(k);
            int k2 = Math.min(k, distBBS.size());
            for (int i = 0; i < k2; i++) {
                ObjectDistBB distBB = distBBS.get(i);
                if (distBB != null)
                    list.add(distBB.getObject3D());
            }
        }

        return list;
    }

    private ArrayList<Object3D>[] closestAll(Objects3DPopulation population1, Objects3DPopulation population2, int k, String type) {
        ArrayList<Object3D>[] all = new ArrayList[population1.getNbObjects()];
        for (int i = 0; i < population1.getNbObjects(); i++) {
            //IJ.showStatus("Closest " + i + " / " + population1.getNbObjects());
            IJ.log("\\Update:Closest " + i + " / " + population1.getNbObjects() + "              ");
            all[i] = computeClosestK(population1.getObject(i), population2, k, type);
        }

        return all;
    }
}
