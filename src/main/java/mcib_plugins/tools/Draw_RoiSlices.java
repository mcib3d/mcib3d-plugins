/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.tools;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import mcib3d.geom.*;

import java.awt.*;

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
public class Draw_RoiSlices implements PlugIn {
    ImagePlus segPlus;
    ImagePlus rawPlus = null;
    int currentZmin, currentZmax;
    Roi[] arrayRois;
    Objects3DPopulation objects3D;

    public void run(String arg) {
        int nbima = WindowManager.getImageCount();

        if (nbima < 1) {
            IJ.showMessage("No image opened !");
            return;
        }

        String[] namesRaw = new String[nbima + 1];
        String[] namesSeg = new String[nbima];

        namesRaw[0] = "None";
        for (int i = 0; i < nbima; i++) {
            namesRaw[i + 1] = WindowManager.getImage(i + 1).getShortTitle();
            namesSeg[i] = WindowManager.getImage(i + 1).getShortTitle();
        }

        int seg = 0;
        int raw = nbima > 1 ? nbima - 1 : 0;
        boolean label3D = false;
        GenericDialog dia = new GenericDialog("Draw Roi 3D");
        dia.addChoice("Raw", namesRaw, namesRaw[raw]);
        dia.addChoice("Seg", namesSeg, namesSeg[seg]);
        dia.addCheckbox("Display 3D labelled Roi", label3D);
        dia.showDialog();

        if (dia.wasOKed()) {
            raw = dia.getNextChoiceIndex();
            seg = dia.getNextChoiceIndex();
            label3D = dia.getNextBoolean();
            segPlus = WindowManager.getImage(seg + 1);
            if (raw > 0) rawPlus = WindowManager.getImage(raw);
            //IJ.log("Seg : " +segPlus.getShortTitle());
            //if(rawPlus!=null)  IJ.log("Raw : " +rawPlus.getShortTitle());

            objects3D = new Objects3DPopulation(segPlus);

            if (label3D) {
                ObjectCreator3D draw = new ObjectCreator3D(segPlus.getWidth(), segPlus.getHeight(), segPlus.getNSlices());

                for (int o = 0; o < objects3D.getNbObjects(); o++) {
                    Object3DVoxels obj = (Object3DVoxels) objects3D.getObject(o);
                    obj.computeContours();
                    if (segPlus.getNSlices() > 1)
                        obj.drawContours(draw, (o + 1));
                    else {
                        obj.drawContoursXY(draw, 0, o + 1);
                    }
                }
                draw.getImageHandler().show("LabelRoi");
            }

            computeRois();
            if (rawPlus == null) {
                drawRoisNew().show();
            } else {
                drawRois().show();
            }
        }
    }

    private ImagePlus drawRoisNew() {
        ImageStack stack = new ImageStack(segPlus.getWidth(), segPlus.getHeight());
        for (int z = 0; z < currentZmin; z++) {
            ImageProcessor processor = new ColorProcessor(segPlus.getWidth(), segPlus.getHeight());
            stack.addSlice(processor);
        }

        Color col = Toolbar.getForegroundColor();
        for (int z = currentZmin; z <= currentZmax; z++) {
            ImageProcessor processor = new ColorProcessor(segPlus.getWidth(), segPlus.getHeight());
            processor.setColor(col);
            Roi roi = arrayRois[z];
            roi.drawPixels(processor);
            stack.addSlice(processor);
        }

        for (int z = 0; z < currentZmin; z++) {
            ImageProcessor processor = new ColorProcessor(segPlus.getWidth(), segPlus.getHeight());
            stack.addSlice(processor);
        }

        for (int z = currentZmax + 1; z < segPlus.getNSlices(); z++) {
            ImageProcessor processor = new ColorProcessor(segPlus.getWidth(), segPlus.getHeight());
            stack.addSlice(processor);
        }

        return new ImagePlus("drawRoi3D", stack);
    }

    private ImagePlus drawRois() {
        rawPlus.deleteRoi();
        Duplicator duplicator = new Duplicator();
        ImagePlus rawCopy = duplicator.run(rawPlus);
        ImageConverter converter = new ImageConverter(rawCopy);
        converter.convertToRGB();
        Color col = Toolbar.getForegroundColor();
        ImageStack stack = rawCopy.getImageStack();
        for (int z = currentZmin; z <= currentZmax; z++) {
            ImageProcessor processor = stack.getProcessor(z + 1);
            processor.setColor(col);
            Roi roi = arrayRois[z];
            if (roi != null) roi.drawPixels(processor);
        }
        return rawCopy;
    }


    /**
     * Description of the Method
     */
    void computeRois() {
        arrayRois = new Roi[segPlus.getNSlices()];
        // get zmin and zmax
        int zmin = segPlus.getNSlices() + 1;
        int zmax = -1;

        Object3D obj;
        for (int i = 0; i < objects3D.getNbObjects(); i++) {
            obj = objects3D.getObject(i);
            if (obj.getZmin() < zmin) {
                zmin = obj.getZmin();
            }
            if (obj.getZmax() > zmax) {
                zmax = obj.getZmax();
            }
        }
        currentZmin = zmin;
        currentZmax = zmax;

        //IJ.log("Computing rois "+zmin+" "+zmax);
        for (int zz = zmin; zz <= zmax; zz++) {
            //IJ.showStatus("Computing Roi " + zz);
            ByteProcessor mask = new ByteProcessor(segPlus.getWidth(), segPlus.getHeight());
            boolean ok = false;
            for (int i = 0; i < objects3D.getNbObjects(); i++) {
                obj = objects3D.getObject(i);
                ok |= Object3D_IJUtils.draw(obj, mask, zz, 255);
                //ok |= obj.draw(mask, zz, 255);
            }
            if (!ok) {
                arrayRois[zz] = null;
                //IJ.log("No draw for " + zz);
            } else {
                mask.setThreshold(1, 255, ImageProcessor.NO_LUT_UPDATE);
                ImagePlus maskPlus = new ImagePlus("mask " + zz, mask);
                //maskPlus.show("mask_" + zz);
                ThresholdToSelection tts = new ThresholdToSelection();
                tts.setup("", maskPlus);
                tts.run(mask);

                arrayRois[zz] = maskPlus.getRoi();
            }
        }
    }
}

