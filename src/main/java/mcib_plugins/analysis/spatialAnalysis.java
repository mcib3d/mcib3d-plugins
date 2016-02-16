package mcib_plugins.analysis;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.measure.Calibration;
import java.awt.Color;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DLabel;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.image3d.ImageShort;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 17 juillet 2006
 */
public class spatialAnalysis {

    //ImagePlus imp;
    //Calibration calibration;
    private final int numEvaluationPoints;
    private final int numRandomSamples;
    private final double distHardCore;
    //int imaspots = 0;
    //int imamask = 1;
    //String[] names;
    //Point3D[] evaluationPoints;
    private final double env;
    // JEAN DB
    private double sdi_F = Double.NaN;
    private double sdi_G = Double.NaN;
    private double sdi_H = Double.NaN;
    //private double sdi_K;
    ImageHandler randomPop;

    int nbBins = 1000;

    public spatialAnalysis(int numPoints, int numRandomSamples, double distHardCore, double env) {
        this.numEvaluationPoints = numPoints;
        this.numRandomSamples = numRandomSamples;
        this.distHardCore = distHardCore;
        this.env = env;
    }

    public void process(ImagePlus plusSpots, ImagePlus plusMask, String functions, boolean verbose, boolean show, boolean save) {
        process(ImageInt.wrap(plusSpots), ImageInt.wrap(plusMask), functions, verbose, show, save);
    }

    public void processAll(ImageHandler plusSpots, ImageHandler plusMask, boolean verbose, boolean show, boolean save) {
        process(plusSpots, plusMask, "FGH", verbose, show, save);
    }

    public void processAll(ImagePlus plusSpots, ImagePlus plusMask, boolean verbose, boolean show, boolean save) {
        process(plusSpots, plusMask, "FGH", verbose, show, save);
    }

    private void processF(Objects3DPopulation pop, Object3D mask, boolean verbose, boolean show, boolean save) {
        Calibration calibration = mask.getCalibration();
        int nbspots = pop.getNbObjects();
        ArrayUtil distances;

        // F
        Point3D[] evaluationPoints;
        ArrayUtil observedDistancesF;
        ArrayUtil[] sampleDistancesF;
        ArrayUtil xEvalsF;
        ArrayUtil observedCDF;
        ArrayUtil averageCDF;

        evaluationPoints = createEvaluationPoints(numEvaluationPoints, pop);
        observedDistancesF = pop.computeDistances(evaluationPoints);
        observedDistancesF.sort();
        observedCDF = CDFTools.cdf(observedDistancesF);
        xEvalsF = new ArrayUtil(numRandomSamples * numEvaluationPoints);
        sampleDistancesF = new ArrayUtil[numRandomSamples];

        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population F " + i);
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            poprandom.createKDTreeCenters();
            distances = poprandom.computeDistances(evaluationPoints);
            distances.sort();
            sampleDistancesF[i] = distances;//           
            xEvalsF.insertValues(i * numEvaluationPoints, distances);
        }
        xEvalsF.sort();
        averageCDF = CDFTools.cdfAverage(sampleDistancesF, xEvalsF);

        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population F " + i);
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            poprandom.createKDTreeCenters();
            distances = poprandom.computeDistances(evaluationPoints);
            distances.sort();
            sampleDistancesF[i] = distances;
        }

        // plot
        Plot plotF = null;
        if (show || save) {
            plotF = createPlot(xEvalsF, sampleDistancesF, observedDistancesF, observedCDF, averageCDF, "F");
        }

        if (show) {
            plotF.draw();
        }
        if (save) {
            PlotWindow plotW = plotF.show();
            if (plotW != null) {
                try {
                    plotW.getResultsTable().saveAs(IJ.getDirectory("home") + "StatsPlot-F.csv");
                } catch (IOException ex) {
                    Logger.getLogger(spatialAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        IJ.log("--- F Function ---");
        sdi_F = CDFTools.SDI(observedDistancesF, sampleDistancesF, averageCDF, xEvalsF);
        IJ.log("SDI F=" + sdi_F);
    }

    private void processG(Objects3DPopulation pop, Object3D mask, boolean verbose, boolean show, boolean save) {
        Calibration calibration = mask.getCalibration();
        int nbspots = pop.getNbObjects();
        ArrayUtil distances;

        // G
        ArrayUtil observedDistancesG;
        ArrayUtil observedCDG;
        observedDistancesG = pop.distancesAllClosestCenter();
        observedDistancesG.sort();
        observedCDG = CDFTools.cdf(observedDistancesG);

        //G 
        ArrayUtil xEvalsG;
        ArrayUtil[] sampleDistancesG;
        ArrayUtil averageCDG;

        xEvalsG = new ArrayUtil(numRandomSamples * nbspots);
        sampleDistancesG = new ArrayUtil[numRandomSamples];
        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population G " + i);
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            //poprandom.createKDTreeCenters();
            distances = poprandom.distancesAllClosestCenter();
            distances.sort();
            sampleDistancesG[i] = distances;
            xEvalsG.insertValues(i * nbspots, distances);
        }
        xEvalsG.sort();
        averageCDG = CDFTools.cdfAverage(sampleDistancesG, xEvalsG);

        // G function
        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population G " + i);
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            poprandom.createKDTreeCenters();
            distances = poprandom.distancesAllClosestCenter();
            distances.sort();
            sampleDistancesG[i] = distances;//           
        }

        // plot
        Plot plotG = null;
        if (show || save) {
            plotG = createPlot(xEvalsG, sampleDistancesG, observedDistancesG, observedCDG, averageCDG, "G");;
        }

        if (show) {
            plotG.draw();
        }
        if (save) {
            PlotWindow plotW = plotG.show();
            if (plotW != null) {
                try {
                    plotW.getResultsTable().saveAs(IJ.getDirectory("home") + "StatsPlot-G.csv");
                } catch (IOException ex) {
                    Logger.getLogger(spatialAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        IJ.log("--- G Function ---");
        sdi_G = CDFTools.SDI(observedDistancesG, sampleDistancesG, averageCDG, xEvalsG);
        IJ.log("SDI G=" + sdi_G);
    }

    private void processH(Objects3DPopulation pop, Object3D mask, boolean verbose, boolean show, boolean save) {
        Calibration calibration = mask.getCalibration();
        int nbspots = pop.getNbObjects();
        ArrayUtil distances;

        // H
        ArrayUtil observedDistancesH;
        ArrayUtil observedCDH;

        observedDistancesH = pop.distancesAllCenter();
        observedDistancesH.sort();
        observedCDH = CDFTools.cdf(observedDistancesH);

        // H
        ArrayUtil xEvalsH;
        ArrayUtil[] sampleDistancesH;
        ArrayUtil averageCDH;

        xEvalsH = new ArrayUtil(numRandomSamples * nbspots);
        sampleDistancesH = new ArrayUtil[numRandomSamples];
        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population H " + i);
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            distances = poprandom.distancesAllCenter();
            distances.sort();
            sampleDistancesH[i] = distances;
            xEvalsH.insertValues(i * nbspots, distances);
        }
        xEvalsH.sort();
        averageCDH = CDFTools.cdfAverage(sampleDistancesH, xEvalsH);

        // H function
        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population H " + i);
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            distances = poprandom.distancesAllCenter();
            distances.sort();
            sampleDistancesH[i] = distances;
        }

        // plot
        Plot plotH = null;
        if (show || save) {
            plotH = createPlot(xEvalsH, sampleDistancesH, observedDistancesH, observedCDH, averageCDH, "H");
        }

        if (show) {
            plotH.draw();
        }
        if (save) {
            PlotWindow plotW = plotH.show();
            if (plotW != null) {
                try {
                    plotW.getResultsTable().saveAs(IJ.getDirectory("home") + "StatsPlot-H.csv");
                } catch (IOException ex) {
                    Logger.getLogger(spatialAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        IJ.log("--- H Function ---");
        sdi_H = CDFTools.SDI(observedDistancesH, sampleDistancesH, averageCDH, xEvalsH);
        IJ.log("SDI H=" + sdi_H);
    }

    private Plot createPlot(ArrayUtil xEvals, ArrayUtil[] sampleDistances, ArrayUtil observedDistances, ArrayUtil observedCD, ArrayUtil averageCD, String function) {
        double plotmaxX = observedDistances.getMaximum();
        double plotmaxY = observedCD.getMaximum();

        // low env      
        double max = xEvals.getMaximum();
        ArrayUtil xEvals0 = new ArrayUtil(nbBins);
        for (int i = 0; i < nbBins; i++) {
            xEvals0.addValue(i, ((double) i) * max / ((double) nbBins));
        }
        // get the values
        ArrayUtil samplespc5 = CDFTools.cdfPercentage(sampleDistances, xEvals0, env / 2.0);
        ArrayUtil samplespc95 = CDFTools.cdfPercentage(sampleDistances, xEvals0, 1.0 - env / 2.0);
        // get the limits        
        if (xEvals0.getMaximum() > plotmaxX) {
            plotmaxX = xEvals0.getMaximum();
        }
        if (samplespc5.getMaximum() > plotmaxY) {
            plotmaxY = samplespc5.getMaximum();
        }
        if (samplespc95.getMaximum() > plotmaxY) {
            plotmaxY = samplespc95.getMaximum();
        }
        if (xEvals.getMaximum() > plotmaxX) {
            plotmaxX = xEvals.getMaximum();
        }
        if (averageCD.getMaximum() > plotmaxY) {
            plotmaxY = averageCD.getMaximum();
        }
        if (observedCD.getMaximum() > plotmaxY) {
            plotmaxY = observedCD.getMaximum();
        }
        if (observedDistances.getMaximum() > plotmaxX) {
            plotmaxX = observedDistances.getMaximum();
        }
        // create the plot
        Plot plot = new Plot(function + "-function", "distance", "cumulated frequency");
        plot.setLimits(0, plotmaxX, 0, plotmaxY);

        // enveloppe  for e.g 10 % at 5 and 95 %
        plot.setColor(Color.green);
        plot.addPoints(xEvals0.getArray(), samplespc5.getArray(), Plot.LINE);

        // high envxEvals.getMaximum
        plot.setColor(Color.green);
        plot.addPoints(xEvals0.getArray(), samplespc95.getArray(), Plot.LINE);

        // average
        plot.setColor(Color.red);
        plot.addPoints(xEvals.getArray(), averageCD.getArray(), Plot.LINE);

        // observed
        plot.setColor(Color.blue);
        plot.addPoints(observedDistances.getArray(), observedCD.getArray(), Plot.LINE);

        return plot;
    }

    /**
     * Main processing method for the DilateKernel_ object
     *
     */
    public void process(ImageHandler plusSpots, ImageHandler plusMask, String functions, boolean verbose, boolean show, boolean save) {
        Calibration calibration = plusSpots.getCalibration();
        if (calibration == null) {
            IJ.log("Image not calibrated");
            calibration = new Calibration();
            calibration.setUnit("pix");
            calibration.pixelWidth = 1;
            calibration.pixelHeight = 1;
            calibration.pixelDepth = 1;
        }

        ImageInt inImage = (ImageInt) plusSpots;
        ImageInt segImage;
        if (inImage.isBinary(0)) {
            if (verbose) {
                IJ.log("Segmenting image...");
            }
            inImage = inImage.threshold(0, false, true);
            ImageLabeller labels = new ImageLabeller(false);
            segImage = labels.getLabels(inImage);
            if (verbose) {
                segImage.show("Labelled Image");
            }
        } else {
            segImage = (ImageInt) inImage.duplicate();
        }
        segImage.setCalibration(calibration);

        int nbspots;

        Objects3DPopulation pop = new Objects3DPopulation();
        ImageInt maskHandler = (ImageInt) plusMask;
        Object3D mask = new Object3DLabel(maskHandler, (int) maskHandler.getMax());
        mask.setCalibration(calibration);
        pop.setMask(mask);
        pop.addImage(segImage, calibration);
        pop.setCalibration(calibration);

        // random sample
        Objects3DPopulation poprandom = new Objects3DPopulation();
        poprandom.setCalibration(calibration);
        poprandom.setMask(mask);
        poprandom.createRandomPopulation(pop.getNbObjects(), distHardCore);
        randomPop = new ImageShort("Random", maskHandler.sizeX,maskHandler.sizeY,maskHandler.sizeZ);
        randomPop.setCalibration(calibration);
        poprandom.draw(randomPop);
        //randomPop.show("random");

        if ((plusMask.getCalibration() == null) || (!plusMask.getCalibration().scaled())) {
            if (verbose) {
                IJ.log("mask not calibrated, calibrating ...");
            }
            plusMask.setCalibration(calibration);
            plusMask.getImagePlus().updateAndRepaintWindow();
        }

        nbspots = pop.getNbObjects();
        if (verbose) {
            IJ.log("Computing spatial statistics, please wait ...");
        }

        if (verbose) {
            IJ.log("Nb Spot=" + nbspots);
            IJ.log("Volume mask=" + mask.getVolumeUnit());
            IJ.log("Density=" + (nbspots / mask.getVolumeUnit()));
        }

        if (functions.contains("F")) {
            processF(pop, mask, verbose, show, save);
        }

        if (functions.contains("G")) {
            processG(pop, mask, verbose, show, save);
        }

        if (functions.contains("H")) {
            processH(pop, mask, verbose, show, save);
        }
    }

    public double getSdi_F() {
        return sdi_F;
    }

    public double getSdi_G() {
        return sdi_G;
    }

    public double getSdi_H() {
        return sdi_H;
    }

    public ImageHandler getRandomSample() {
        return randomPop;
    }

    private Point3D[] createEvaluationPoints(int numPoints, Objects3DPopulation population) {
        Point3D[] evaluationPoints = new Point3D[numPoints];
        for (int i = 0; i < numPoints; ++i) {
            evaluationPoints[i] = population.getRandomPointInMask();
        }

        return evaluationPoints;
    }
}
