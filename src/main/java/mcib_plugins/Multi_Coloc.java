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
import mcib3d.geom.*;
import mcib3d.image3d.ImageInt;

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
public class Multi_Coloc implements PlugIn {

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
        GenericDialog dia = new GenericDialog("MultiColoc");
        dia.addChoice("Image_A", namesA, namesA[idxA]);
        dia.addChoice("Image_B", namesB, namesB[idxB]);

        dia.showDialog();
        if (dia.wasOKed()) {
            idxA = dia.getNextChoiceIndex();
            idxB = dia.getNextChoiceIndex();

            ImagePlus plusA = WindowManager.getImage(idxA + 1);
            ImagePlus plusB = WindowManager.getImage(idxB + 1);

            Objects3DPopulation popA = new Objects3DPopulation(ImageInt.wrap(plusA));
            Objects3DPopulation popB = new Objects3DPopulation(ImageInt.wrap(plusB));

            Objects3DPopulationColocalisation colocalisation=new Objects3DPopulationColocalisation(popA,popB);
            colocalisation.getResultsTable(true).show("Coloc");

            

            IJ.log("Finished");
        }
    }
}
