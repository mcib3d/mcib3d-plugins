/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;

import java.awt.*;
import java.text.NumberFormat;

/**
 * @author thomasb
 */
public class Simple_Dist_Analysis implements PlugInFilter {
    int nBins = 100;
    Color color = null;
    ImagePlus plus;

    @Override
    public int setup(String string, ImagePlus ip) {
        plus = ip;

        return DOES_16 + DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        ImageInt img = ImageInt.wrap(plus);
        Calibration cal = plus.getCalibration();
        if (Dialogue()) {
            if (cal != null) img.setCalibration(cal);
            ImageInt imageInt = img;
            // binary or seg
            if (img.isBinary()) {
                IJ.log("Binary data, labelling ...");
                ImageLabeller imageLabeller = new ImageLabeller();
                imageInt = imageLabeller.getLabels(img);
                imageInt.show("Labelled");
            }
            Objects3DPopulation objPopA = new Objects3DPopulation(imageInt);
            IJ.log("nb=" + objPopA.getNbObjects());
            IJ.log("G analysis, closest point");
            // G
            ArrayUtil dist = objPopA.distancesAllClosestCenter();
            ArrayUtil[] arrayUtils = dist.getHistogram(nBins);
            Plot plotG = new Plot("G function", "distances (" + cal.getUnit() + ") ", "frequency");
            plotG.setColor(color);
            arrayUtils[1].divideAll(dist.getSize());
            plotG.addPoints(arrayUtils[0].getArray(), arrayUtils[1].getArray(), Plot.LINE);
            plotG.show();
            dist.sort();
            ArrayUtil cdf = CDFTools.cdf(dist);
            Plot plotG1 = new Plot("G function cdf", "distances (" + cal.getUnit() + ") ", "frequency");
            plotG1.setColor(color);
            plotG1.addPoints(dist.getArray(), cdf.getArray(), Plot.LINE);
            plotG1.show();
            IJ.log("min=" + nf.format(dist.getMinimum()) + " avg=" + nf.format(dist.getMean()) + " max=" + nf.format(dist.getMaximum()));
            IJ.log("med=" + nf.format(dist.median()) + " sd=" + nf.format(dist.getStdDev()));
            // H
            dist = objPopA.distancesAllCenter();
            arrayUtils = dist.getHistogram(nBins);
            arrayUtils[1].divideAll(dist.getSize());
            Plot plotH = new Plot("H function", "distances (" + cal.getUnit() + ") ", "frequency");
            plotH.setColor(color);
            plotH.addPoints(arrayUtils[0].getArray(), arrayUtils[1].getArray(), Plot.LINE);
            plotH.show();
            dist.sort();
            cdf = CDFTools.cdf(dist);
            Plot plotH1 = new Plot("H function cdf", "distances (" + cal.getUnit() + ") ", "frequency");
            plotH1.setColor(color);
            plotH1.addPoints(dist.getArray(), cdf.getArray(), Plot.LINE);
            plotH1.show();
        }
    }

    private boolean Dialogue() {
        String[] colors = {"Red", "Green", "Blue", "Cyan", "Magenta", "Yellow", "Orange", "Pink", "Black"};
        GenericDialog gd = new GenericDialog("Spatial Statistics");
        gd.addNumericField("Nb bins for histogram", nBins, 0);
        gd.addChoice("Draw_color:", colors, colors[0]);
        gd.showDialog();
        nBins = (int) gd.getNextNumber();

        switch (gd.getNextChoiceIndex()) {
            case 0:
                color = Color.red;
                break;
            case 1:
                color = Color.green;
                break;
            case 2:
                color = Color.blue;
                break;
            case 3:
                color = Color.cyan;
                break;
            case 4:
                color = Color.magenta;
                break;
            case 5:
                color = Color.yellow;
                break;
            case 6:
                color = Color.orange;
                break;
            case 7:
                color = Color.pink;
                break;
            default:
                color = Color.black;
        }

        return (gd.wasOKed());
    }

}
