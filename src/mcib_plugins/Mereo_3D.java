/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import mcib3d.geom.MereoAnalysis;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Mereo_3D implements PlugIn {

    @Override
    public void run(String arg) {
        int nbima = WindowManager.getImageCount();
        if (nbima < 1) {
            IJ.error("Needs at least one labelled image");
            return;
        }
        String[] namesA = new String[nbima];
        String[] namesB = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            namesA[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesB[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        int idxA = 0;
        int idxB = nbima > 1 ? 1 : 0;
        float radXY = 1;
        float radZ = 1;
        GenericDialog dia = new GenericDialog("MereoTopology3D");
        dia.addChoice("Image_A", namesA, namesA[idxA]);
        dia.addChoice("Image_B", namesB, namesB[idxB]);
        dia.addNumericField("RadiusXY", radXY, 1);
        dia.addNumericField("RadiusZ", radZ, 1);
        dia.addCheckbox("Compute_regularity", false);

        dia.showDialog();
        if (dia.wasOKed()) {
            idxA = dia.getNextChoiceIndex();
            idxB = dia.getNextChoiceIndex();
            radXY = (float) dia.getNextNumber();
            radZ = (float) dia.getNextNumber();
            boolean regularity = dia.getNextBoolean();

            ImagePlus plusA = WindowManager.getImage(idxA + 1);
            ImagePlus plusB = WindowManager.getImage(idxB + 1);

            Objects3DPopulation popA = new Objects3DPopulation(plusA);
            Objects3DPopulation popB = new Objects3DPopulation(plusB);

            // test connexity
            for (int ia = 0; ia < popA.getNbObjects(); ia++) {
                Object3DVoxels vox = (Object3DVoxels) popA.getObject(ia);
                //IJ.log("Testing connexity "+ia+" "+popA.getObject(ia));
                if (!vox.isConnex()) {
                    IJ.log("WARNING Object A" + ia + " : " + popA.getObject(ia) + " is not connex");
                }
            }
            for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                Object3DVoxels vox = (Object3DVoxels) popB.getObject(ib);
                //IJ.log("Testing connexity "+ib+" "+popB.getObject(ib));
                if (!vox.isConnex()) {
                    IJ.log("WARNING Object B" + ib + " : " + popB.getObject(ib) + " is not connex");
                }
            }
//              mereo Analysis
            MereoAnalysis mereo = new MereoAnalysis(popA, popB);
            // radius
            mereo.setRadX(radXY);
            mereo.setRadY(radXY);
            mereo.setRadZ(radZ);
            //mereo.computeSlowRelationships();
            mereo.computeFastRelationships();
            IJ.log("MEREOTOPOLOGY RESULTS (DC if not specified)");
            IJ.log(mereo.getResults(true));
            ResultsTable rt = mereo.getResultsTable(true, true);
            rt.show("Mereo");
            IJ.log("");

            // TEST
            //ImageHandler dup = ImageInt.wrap(plusA).createSameDimensions();
           // Object3D obj = popA.getObject(0);
          //  Object3D closed = obj.getClosedObject(radXY, radXY, radZ, false);

//            closed.draw(dup);
//            dup.show("Closed_obj");
//            obj.getLabelImage().show("obj");
//            closed.getLabelImage().show("closed_image");
//            closed.setLabelImage(null);
//            obj.setLabelImage(null);

           // int a = closed.getColoc(obj);
          //  IJ.log("main "+obj.getVolumePixels() + " " + closed.getVolumePixels() + " " + a);

            if (regularity) {
                for (int ia = 0; ia < popA.getNbObjects(); ia++) {
                    IJ.log("Object A" + ia + " : " + popA.getObject(ia));
                    unchangedMorpho(popA.getObject(ia), (float) radXY, (float) radXY, (float) radZ);
                }
                IJ.log("");
                for (int ib = 0; ib < popB.getNbObjects(); ib++) {
                    IJ.log("Object B" + ib + " : " + popB.getObject(ib));
                    unchangedMorpho(popB.getObject(ib), (float) radXY, (float) radXY, (float) radZ);
                }
            }
            IJ.log("Finished");
        }
    }

    private void unchangedMorpho(Object3D object, float rx, float ry, float rz) {
        //object.setLabelImage(null);
        Object3D objectClosed = object.getClosedObject(rz, ry, rz); 
        if (objectClosed.getVolumePixels() == object.getVolumePixels()) {
            IJ.log("No change after closing radii " + rx + " " + ry + " " + rz);
        } else {
            IJ.log("Change after closing radii " + rx + " " + ry + " " + rz + " of " + (objectClosed.getVolumePixels() - object.getVolumePixels()) + " voxels over " + object.getVolumePixels());
        }
        //object.setLabelImage(null);
        Object3D objectOpened = object.getOpenedObject(rz, ry, rz);
         // test
        //object.setLabelImage(null);
        //objectClosed.setLabelImage(null);
        if (objectOpened.getVolumePixels() == object.getVolumePixels()) {
            IJ.log("No change after opening radii " + rx + " " + ry + " " + rz);
        } else {
            IJ.log("Change after opening radii " + rx + " " + ry + " " + rz + " of " + (object.getVolumePixels() - objectOpened.getVolumePixels()) + " voxels over " + object.getVolumePixels());
        }
    }
}
