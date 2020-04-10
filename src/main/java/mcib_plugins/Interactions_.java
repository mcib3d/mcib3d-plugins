package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import mcib3d.geom.Object3D;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.PairColocalisation;
import mcib3d.geom.interactions.InteractionsComputeContours;
import mcib3d.geom.interactions.InteractionsComputeDamLines;
import mcib3d.geom.interactions.InteractionsComputeDilate;
import mcib3d.geom.interactions.InteractionsList;
import mcib3d.image3d.ImageHandler;

import java.util.ArrayList;
import java.util.HashMap;

// will use class Objects3DPopulationInteractions in next version
public class Interactions_ implements PlugIn {
    boolean methodLINE = true;
    boolean methodDILATE = false;
    boolean methodTOUCH = false;
    // radii for dilation
    float radxy = 1;
    float radz = 1;
    // process
    private boolean needToComputeInteractions;
    private ImageHandler image;
    private Objects3DPopulation population;
    private HashMap<String, PairColocalisation> interactions;

    @Override
    public void run(String s) {
        ImagePlus plus = WindowManager.getCurrentImage();
        if (plus == null) {
            IJ.error("Open a labelled image to compute interactions");
            return;
        }
        needToComputeInteractions = true;
        image = ImageHandler.wrap(plus);
        population = new Objects3DPopulation(image);

        if (dialog()) {
            if (methodDILATE) {
                InteractionsList list = new InteractionsComputeDilate(radxy, radxy, radz).compute(image);
                interactions = new HashMap<>();
                interactions.putAll(list.getMap());
                getResultsTableOnlyColoc(true).show("InteractionsDilate");
            }
            if (methodLINE) {
                InteractionsList list = new InteractionsComputeDamLines().compute(image);
                interactions = new HashMap<>();
                interactions.putAll(list.getMap());
                getResultsTableOnlyColoc(true).show("InteractionsLines");
            }
            if (methodTOUCH) {
                InteractionsList list = new InteractionsComputeContours().compute(image);
                interactions = new HashMap<>();
                interactions.putAll(list.getMap());
                getResultsTableOnlyColoc(true).show("InteractionsTouch");
            }
        }
    }

    boolean dialog() {
        GenericDialog dialog = new GenericDialog("Interactions");
        dialog.addMessage("Objects are separated by black lines");
        dialog.addCheckbox("Lines", methodLINE);
        dialog.addMessage("Objects are touching");
        dialog.addCheckbox("Touching", methodTOUCH);
        dialog.addMessage("Objects are separated by empty space");
        dialog.addCheckbox("Dilation", methodDILATE);
        dialog.addNumericField("radius_DilateXY", radxy, 2);
        dialog.addNumericField("radius_DilateZ", radz, 2);
        dialog.showDialog();
        methodLINE = dialog.getNextBoolean();
        methodTOUCH = dialog.getNextBoolean();
        methodDILATE = dialog.getNextBoolean();
        radxy = (float) dialog.getNextNumber();
        radz = (float) dialog.getNextNumber();

        return dialog.wasOKed();
    }



    public ResultsTable getResultsTableOnlyColoc(boolean useValueObject) {
        IJ.log("Interactions completed, building results table");
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) rt = new ResultsTable();
        rt.reset();
        for (int ia = 0; ia < population.getNbObjects(); ia++) {
            Object3D object1 = population.getObject(ia);
            rt.incrementCounter();
            if (!useValueObject) {
                rt.setLabel("A" + ia, ia);
            } else {
                rt.setLabel("A" + object1.getValue(), ia);
            }
            ArrayList<PairColocalisation> list1 = getObject1ColocalisationPairs(object1);
            ArrayList<PairColocalisation> list2 = getObject2ColocalisationPairs(object1);
            if ((list1.size() == 0) && (list2.size() == 0)) {
                if (!useValueObject)
                    rt.setValue("O1", ia, 0);
                else
                    rt.setValue("O1", ia, 0);
                rt.setValue("V1", ia, 0);
            }
            for (int c = 0; c < list1.size(); c++) {
                PairColocalisation colocalisation = list1.get(c);
                if (colocalisation.getObject3D1().getValue() != object1.getValue()) IJ.log("Pb object " + object1);
                Object3D object2 = colocalisation.getObject3D2();
                int i2 = population.getIndexOf(object2);
                if (!useValueObject)
                    rt.setValue("O" + (c + 1), ia, i2);
                else
                    rt.setValue("O" + (c + 1), ia, object2.getValue());
                rt.setValue("V" + (c + 1), ia, colocalisation.getVolumeColoc());
            }
            int offset = list1.size();
            for (int c = 0; c < list2.size(); c++) {
                PairColocalisation colocalisation = list2.get(c);
                if (colocalisation.getObject3D1().getValue() != object1.getValue()) IJ.log("Pb object " + object1);
                Object3D object2 = colocalisation.getObject3D2();
                int i2 = population.getIndexOf(object2);
                if (!useValueObject)
                    rt.setValue("O" + (offset + c + 1), ia, i2);
                else
                    rt.setValue("O" + (offset + c + 1), ia, object2.getValue());
                rt.setValue("V" + (offset + c + 1), ia, colocalisation.getVolumeColoc());
            }
        }
        return rt;
    }

    public ArrayList<PairColocalisation> getObject1ColocalisationPairs(Object3D object3D) {
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i1 = object3D.getValue();
        for (String key : interactions.keySet()) {
            if (key.startsWith("" + i1 + "-")) {
                pairColocalisations.add(new PairColocalisation(interactions.get(key).getObject3D1(), interactions.get(key).getObject3D2(), interactions.get(key).getVolumeColoc()));
            }
        }

        return pairColocalisations;
    }

    public ArrayList<PairColocalisation> getObject2ColocalisationPairs(Object3D object3D) {
        ArrayList<PairColocalisation> pairColocalisations = new ArrayList<PairColocalisation>();
        int i1 = object3D.getValue();
        for (String key : interactions.keySet()) {
            if (key.endsWith("-" + i1)) {
                pairColocalisations.add(new PairColocalisation(interactions.get(key).getObject3D2(), interactions.get(key).getObject3D1(), interactions.get(key).getVolumeColoc()));
            }
        }

        return pairColocalisations;
    }
}

