package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.Duplicator;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageInt;

/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */

/**
 * *
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 * <p>
 * <p>
 * <p>
 * This file is part of mcib3d
 * <p>
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Numbering_ implements PlugInFilter {

    ImagePlus myPlus;
    int imaSpots;
    int imaSignal;

    @Override
    public int setup(String arg, ImagePlus imp) {
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (Dialogue()) {
            myPlus = WindowManager.getImage(imaSpots);
            int channel = myPlus.getChannel();
            int frame = myPlus.getFrame();
            String title = myPlus.getTitle();

            // objects population
            ImageInt img = ImageInt.wrap(extractCurrentStack(myPlus));
            Objects3DPopulation population = new Objects3DPopulation(img);

            // signal
            myPlus = WindowManager.getImage(imaSignal);
            ImageInt spots = ImageInt.wrap(extractCurrentStack(myPlus));

            ResultsTable rt = ResultsTable.getResultsTable();
            if (rt == null) {
                rt = new ResultsTable();
            }
            int r0 = rt.getCounter();
            for (int r = 0; r < population.getNbObjects(); r++) {
                rt.incrementCounter();
                int row = r0 + r;
                int[] numbers = population.getObject(r).getNumbering(spots);
                rt.setValue("Label", row, population.getObject(r).getValue());
                rt.setValue("NbObjects", row, numbers[0]);
                rt.setValue("VolObjects", row, numbers[1]);
                rt.setValue("Channel", row, channel);
                rt.setValue("Frame", row, frame);
                rt.setValue("Image", row, title);
            }
            rt.updateResults();
            rt.show("Results");
        }
    }

    private boolean Dialogue() {
        int nbima = WindowManager.getImageCount();
        String[] names = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        imaSpots = 0;
        imaSignal = nbima > 1 ? nbima - 1 : 0;

        GenericDialog dia = new GenericDialog("Objects numbering");
        dia.addChoice("Main objects(containing)", names, names[imaSpots]);
        dia.addChoice("Counted objects inside", names, names[imaSignal]);
        dia.showDialog();
        imaSpots = dia.getNextChoiceIndex() + 1;
        imaSignal = dia.getNextChoiceIndex() + 1;

        return dia.wasOKed();
    }

    private ImagePlus extractCurrentStack(ImagePlus plus) {
        // check dimensions
        int[] dims = plus.getDimensions();//XYCZT
        int channel = plus.getChannel();
        int frame = plus.getFrame();
        ImagePlus stack;
        // crop actual frame
        if ((dims[2] > 1) || (dims[4] > 1)) {
            IJ.log("hyperstack found, extracting current channel " + channel + " and frame " + frame);
            Duplicator duplicator = new Duplicator();
            stack = duplicator.run(plus, channel, channel, 1, dims[3], frame, frame);
        } else stack = plus.duplicate();

        return stack;
    }
}
