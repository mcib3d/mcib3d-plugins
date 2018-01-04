package mcib_plugins.analysis;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.measure.Calibration;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DLabel;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageLabeller;
import mcib3d.utils.ArrayUtil;
import mcib3d.utils.CDFTools;
import mcib3d.utils.ThreadUtil;

import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    private final double distHardCore; // distance in units
    //int imaspots = 0;
    //int imamask = 1;
    //String[] names;
    //Point3D[] evaluationPoints;
    private final double env;
    //private double sdi_K;
    ImageHandler randomPop;
    int nbBins = 1000;
    int nbCpus = 1;
    // JEAN DB
    private double sdi_F = Double.NaN;
    private double sdi_G = Double.NaN;
    private double sdi_H = Double.NaN;
    private Color ColorAVG = Color.red;
    private Color ColorENV = Color.green;
    private Color ColorOBS = Color.blue;

    public spatialAnalysis(int numPoints, int numRandomSamples, double distHardCore, double env) {
        this.numEvaluationPoints = numPoints;
        this.numRandomSamples = numRandomSamples;
        this.distHardCore = distHardCore;
        this.env = env;
    }

    public static void createMask(ImagePlus plus) {
        WindowManager.setTempCurrentImage(plus);
        ij.plugin.Selection DrawMask = new ij.plugin.Selection();
        DrawMask.run("mask");
    }

    public void setMultiThread(int nb) {
        nbCpus = nb;
    }

    public boolean process(ImagePlus plusSpots, ImagePlus plusMask, String functions, boolean verbose, boolean show, boolean save) {
        return process(ImageInt.wrap(plusSpots), ImageInt.wrap(plusMask), functions, verbose, show, save);
    }

    public void processAll(ImageHandler plusSpots, ImageHandler plusMask, boolean verbose, boolean show, boolean save) {
        process(plusSpots, plusMask, "FGH", verbose, show, save);
    }

    public void processAll(ImagePlus plusSpots, ImagePlus plusMask, boolean verbose, boolean show, boolean save) {
        process(plusSpots, plusMask, "FGH", verbose, show, save);
    }

    @Deprecated
    private void processFMonoThread(Objects3DPopulation pop, Object3D mask, boolean verbose, boolean show, boolean save) {
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
                IJ.showStatus("Random population F " + (i + 1));
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            poprandom.createKDTreeCenters();
            distances = poprandom.computeDistances(evaluationPoints);
            distances.sort();
            sampleDistancesF[i] = distances;
            xEvalsF.insertValues(i * numEvaluationPoints, distances);
        }
        xEvalsF.sort();
        averageCDF = CDFTools.cdfAverage(sampleDistancesF, xEvalsF);

        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population F " + (i + 1));
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
        Plot plotF;
        if (show || save) {
            plotF = createPlot(xEvalsF, sampleDistancesF, observedDistancesF, observedCDF, averageCDF, "F");
            plotF.draw();
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
        }
        IJ.log("--- F Function ---");
        sdi_F = CDFTools.SDI(observedDistancesF, sampleDistancesF, averageCDF, xEvalsF);
        IJ.log("SDI F=" + sdi_F);
    }

    private void processF(Objects3DPopulation pop, Object3D mask, final boolean verbose, boolean show, boolean save) {
        //final Calibration calibration = Object3D_IJUtils.getCalibration(mask);
        final double sxy = mask.getResXY();
        final double sz = mask.getResZ();
        final String unit = mask.getUnits();
        //final Calibration calibration = mask.getCalibration();
        final int nbSpots = pop.getNbObjects();

        // F
        final Point3D[] evaluationPoints;
        ArrayUtil observedDistancesF;
        final ArrayUtil[] sampleDistancesF;
        ArrayUtil xEvalF;
        ArrayUtil observedCDF;
        ArrayUtil averageCDF;

        // observed distances
        evaluationPoints = createEvaluationPoints(numEvaluationPoints, pop);
        observedDistancesF = pop.computeDistances(evaluationPoints);
        observedDistancesF.sort();
        observedCDF = CDFTools.cdf(observedDistancesF);

        xEvalF = new ArrayUtil(numRandomSamples * numEvaluationPoints);
        sampleDistancesF = new ArrayUtil[numRandomSamples];

        // PARALLEL AVERAGE
        final Object3D mask2 = mask;
        final AtomicInteger ai = new AtomicInteger(0);
        final int nCpu = nbCpus == 0 ? ThreadUtil.getNbCpus() : nbCpus;
        Thread[] threads = ThreadUtil.createThreadArray(nCpu);
        final int dec = (int) Math.ceil((double) numRandomSamples / (double) nCpu);
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    ArrayUtil distances2;
                    for (int k = ai.getAndIncrement(); k < nCpu; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < numRandomSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population F " + (i + 1) + " by processor " + (k + 1));
                            }
                            Objects3DPopulation popRandom = new Objects3DPopulation();
                            //popRandom.setCalibration(calibration);
                            popRandom.setCalibration(sxy, sz, unit);
                            popRandom.setMask(mask2);
                            popRandom.createRandomPopulation(nbSpots, distHardCore);
                            popRandom.createKDTreeCenters();
                            distances2 = popRandom.computeDistances(evaluationPoints);
                            distances2.sort();
                            sampleDistancesF[i] = distances2;
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);
        for (int i = 0; i < numRandomSamples; i++) {
            xEvalF.insertValues(i * numEvaluationPoints, sampleDistancesF[i]);
        }
        xEvalF.sort();
        averageCDF = CDFTools.cdfAverage(sampleDistancesF, xEvalF);

        // PARALLEL ENVELOPE
        ai.set(0);
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    ArrayUtil distances2;
                    for (int k = ai.getAndIncrement(); k < nCpu; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < numRandomSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population F " + (i + 1) + " by processor " + (k + 1));
                            }
                            Objects3DPopulation poprandom = new Objects3DPopulation();
                            //poprandom.setCalibration(calibration);
                            poprandom.setCalibration(sxy, sz, unit);
                            poprandom.setMask(mask2);
                            poprandom.createRandomPopulation(nbSpots, distHardCore);
                            poprandom.createKDTreeCenters();
                            distances2 = poprandom.computeDistances(evaluationPoints);
                            distances2.sort();
                            sampleDistancesF[i] = distances2;
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);

        // plot
        Plot plotF;
        if (show || save) {
            plotF = createPlot(xEvalF, sampleDistancesF, observedDistancesF, observedCDF, averageCDF, "F");
            plotF.draw();
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
        }
        IJ.log("--- F Function ---");
        sdi_F = CDFTools.SDI(observedDistancesF, sampleDistancesF, averageCDF, xEvalF);
        IJ.log("SDI F=" + sdi_F);
    }

    @Deprecated
    private void processGMonoThread(Objects3DPopulation pop, Object3D mask, boolean verbose, boolean show, boolean save) {
        Calibration calibration = mask.getCalibration();
        int nbspots = pop.getNbObjects();
        ArrayUtil distances;

        // G
        ArrayUtil observedDistancesG;
        ArrayUtil observedCDG;
        observedDistancesG = pop.distancesAllClosestCenter();
        observedDistancesG.sort();
        observedCDG = CDFTools.cdf(observedDistancesG);

        // G
        ArrayUtil xEvalsG;
        ArrayUtil[] sampleDistancesG;
        ArrayUtil averageCDG;

        xEvalsG = new ArrayUtil(numRandomSamples * nbspots);
        sampleDistancesG = new ArrayUtil[numRandomSamples];
        for (int i = 0; i < numRandomSamples; i++) {
            if (verbose) {
                IJ.showStatus("Random population G " + (i + 1));
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
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
                IJ.showStatus("Random population G " + (i + 1));
            }
            Objects3DPopulation poprandom = new Objects3DPopulation();
            poprandom.setCalibration(calibration);
            poprandom.setMask(mask);
            poprandom.createRandomPopulation(nbspots, distHardCore);
            poprandom.createKDTreeCenters();
            distances = poprandom.distancesAllClosestCenter();
            distances.sort();
            sampleDistancesG[i] = distances;
        }

        // plot
        Plot plotG = null;
        if (show || save) {
            plotG = createPlot(xEvalsG, sampleDistancesG, observedDistancesG, observedCDG, averageCDG, "G");
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

    private void processG(Objects3DPopulation pop, Object3D mask, final boolean verbose, boolean show, boolean save) {
        //final Calibration calibration = Object3D_IJUtils.getCalibration(mask);
        final double sxy = mask.getResXY();
        final double sz = mask.getResZ();
        final String unit = mask.getUnits();
        //final Calibration calibration = mask.getCalibration();
        final int nbSpots = pop.getNbObjects();

        // observed G
        ArrayUtil observedDistancesG;
        ArrayUtil observedCDG;
        observedDistancesG = pop.distancesAllClosestCenter();
        observedDistancesG.sort();
        observedCDG = CDFTools.cdf(observedDistancesG);

        // Average G
        ArrayUtil xEvalG;
        final ArrayUtil[] sampleDistancesG;
        ArrayUtil averageCDG;

        xEvalG = new ArrayUtil(numRandomSamples * nbSpots);
        sampleDistancesG = new ArrayUtil[numRandomSamples];

        // PARALLEL
        final Object3D mask2 = mask;
        final AtomicInteger ai = new AtomicInteger(0);
        final int nCpu = nbCpus == 0 ? ThreadUtil.getNbCpus() : nbCpus;
        Thread[] threads = ThreadUtil.createThreadArray(nCpu);
        final int dec = (int) Math.ceil((double) numRandomSamples / (double) nCpu);
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    ArrayUtil distances2;
                    //image.setShowStatus(show);
                    for (int k = ai.getAndIncrement(); k < nCpu; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < numRandomSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population G " + (i + 1) + " by processor " + (k + 1));
                            }
                            Objects3DPopulation popRandom = new Objects3DPopulation();
                            //popRandom.setCalibration(calibration);
                            popRandom.setCalibration(sxy, sz, unit);
                            popRandom.setMask(mask2);
                            popRandom.createRandomPopulation(nbSpots, distHardCore);
                            distances2 = popRandom.distancesAllClosestCenter();
                            distances2.sort();
                            sampleDistancesG[i] = distances2;
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);
        for (int i = 0; i < numRandomSamples; i++) {
            xEvalG.insertValues(i * nbSpots, sampleDistancesG[i]);
        }
        xEvalG.sort();
        averageCDG = CDFTools.cdfAverage(sampleDistancesG, xEvalG);

        // Envelope G
        ai.set(0);
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    ArrayUtil distances2;
                    //image.setShowStatus(show);
                    for (int k = ai.getAndIncrement(); k < nCpu; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < numRandomSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population G " + (i + 1) + " by processor " + (k + 1));
                            }
                            Objects3DPopulation popRandom = new Objects3DPopulation();
                            //popRandom.setCalibration(calibration);
                            popRandom.setCalibration(sxy, sz, unit);
                            popRandom.setMask(mask2);
                            popRandom.createRandomPopulation(nbSpots, distHardCore);
                            distances2 = popRandom.distancesAllClosestCenter();
                            distances2.sort();
                            sampleDistancesG[i] = distances2;
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);

        // plot
        Plot plotG = null;
        if (show || save) {
            plotG = createPlot(xEvalG, sampleDistancesG, observedDistancesG, observedCDG, averageCDG, "G");
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
        sdi_G = CDFTools.SDI(observedDistancesG, sampleDistancesG, averageCDG, xEvalG);
        IJ.log("SDI G=" + sdi_G);
    }

    @Deprecated
    private void processHMonoThread(Objects3DPopulation pop, Object3D mask, boolean verbose, boolean show, boolean save) {
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
                IJ.showStatus("Random population H " + (i + 1));
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
                IJ.showStatus("Random population H " + (i + 1));
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


    private void processH(Objects3DPopulation pop, Object3D mask, final boolean verbose, boolean show, boolean save) {
        //final Calibration calibration = Object3D_IJUtils.getCalibration(mask);
        final double sxy = mask.getResXY();
        final double sz = mask.getResZ();
        final String unit = mask.getUnits();
        //final Calibration calibration = mask.getCalibration();
        final int nbSpots = pop.getNbObjects();

        // observed H
        ArrayUtil observedDistancesH;
        ArrayUtil observedCDH;
        observedDistancesH = pop.distancesAllCenter();
        observedDistancesH.sort();
        observedCDH = CDFTools.cdf(observedDistancesH);

        // average H
        ArrayUtil xEvalH;
        final ArrayUtil[] sampleDistancesH;
        ArrayUtil averageCDH;

        xEvalH = new ArrayUtil(numRandomSamples * nbSpots);
        sampleDistancesH = new ArrayUtil[numRandomSamples];

        // PARALLEL
        final Object3D mask2 = mask;
        final AtomicInteger ai = new AtomicInteger(0);
        final int nCpu = nbCpus == 0 ? ThreadUtil.getNbCpus() : nbCpus;
        Thread[] threads = ThreadUtil.createThreadArray(nCpu);
        final int dec = (int) Math.ceil((double) numRandomSamples / (double) nCpu);
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    ArrayUtil distances2;
                    //image.setShowStatus(show);
                    for (int k = ai.getAndIncrement(); k < nCpu; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < numRandomSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population H " + (i + 1) + " by processor " + (k + 1));
                            }
                            Objects3DPopulation popRandom = new Objects3DPopulation();
                            //popRandom.setCalibration(calibration);
                            popRandom.setCalibration(sxy, sz, unit);
                            popRandom.setMask(mask2);
                            popRandom.createRandomPopulation(nbSpots, distHardCore);
                            distances2 = popRandom.distancesAllCenter();
                            distances2.sort();
                            sampleDistancesH[i] = distances2;
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);
        for (int i = 0; i < numRandomSamples; i++) {
            xEvalH.insertValues(i * nbSpots, sampleDistancesH[i]);
        }
        xEvalH.sort();
        averageCDH = CDFTools.cdfAverage(sampleDistancesH, xEvalH);

        // envelope
        ai.set(0);
        for (int iThread = 0; iThread < threads.length; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    ArrayUtil distances2;
                    //image.setShowStatus(show);
                    for (int k = ai.getAndIncrement(); k < nCpu; k = ai.getAndIncrement()) {
                        for (int i = dec * k; ((i < (dec * (k + 1))) && (i < numRandomSamples)); i++) {
                            if (verbose) {
                                IJ.showStatus("Random population H " + (i + 1) + " by processor " + (k + 1));
                            }
                            Objects3DPopulation popRandom = new Objects3DPopulation();
                            //popRandom.setCalibration(calibration);
                            popRandom.setCalibration(sxy, sz, unit);
                            popRandom.setMask(mask2);
                            popRandom.createRandomPopulation(nbSpots, distHardCore);
                            distances2 = popRandom.distancesAllCenter();
                            distances2.sort();
                            sampleDistancesH[i] = distances2;
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);

        // plot
        Plot plotH = null;
        if (show || save) {
            plotH = createPlot(xEvalH, sampleDistancesH, observedDistancesH, observedCDH, averageCDH, "H");
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
        sdi_H = CDFTools.SDI(observedDistancesH, sampleDistancesH, averageCDH, xEvalH);
        IJ.log("SDI H=" + sdi_H);
    }

    private Plot createPlot(ArrayUtil xEvals, ArrayUtil[] sampleDistances, ArrayUtil observedDistances, ArrayUtil observedCD, ArrayUtil averageCD, String function) {
        double plotMaxX = observedDistances.getMaximum();
        double plotMaxY = observedCD.getMaximum();

        // low env
        double max = xEvals.getMaximum();
        ArrayUtil xEval0 = new ArrayUtil(nbBins);
        for (int i = 0; i < nbBins; i++) {
            xEval0.addValue(i, ((double) i) * max / ((double) nbBins));
        }
        // get the values
        ArrayUtil samplesPc5 = CDFTools.cdfPercentage(sampleDistances, xEval0, env / 2.0);
        ArrayUtil samplesPc95 = CDFTools.cdfPercentage(sampleDistances, xEval0, 1.0 - env / 2.0);
        // get the limits
        if (xEval0.getMaximum() > plotMaxX) {
            plotMaxX = xEval0.getMaximum();
        }
        if (samplesPc5.getMaximum() > plotMaxY) {
            plotMaxY = samplesPc5.getMaximum();
        }
        if (samplesPc95.getMaximum() > plotMaxY) {
            plotMaxY = samplesPc95.getMaximum();
        }
        if (xEvals.getMaximum() > plotMaxX) {
            plotMaxX = xEvals.getMaximum();
        }
        if (averageCD.getMaximum() > plotMaxY) {
            plotMaxY = averageCD.getMaximum();
        }
        if (observedCD.getMaximum() > plotMaxY) {
            plotMaxY = observedCD.getMaximum();
        }
        if (observedDistances.getMaximum() > plotMaxX) {
            plotMaxX = observedDistances.getMaximum();
        }
        // create the plot
        Plot plot = new Plot(function + "-function", "distance", "cumulated frequency");
        plot.setLimits(0, plotMaxX, 0, plotMaxY);

        // envelope  for e.g 10 % at 5 and 95 %
        plot.setColor(ColorENV);
        plot.addPoints(xEval0.getArray(), samplesPc5.getArray(), Plot.LINE);

        // envelope  for e.g 10 % at 5 and 95 %
        plot.setColor(ColorENV);
        plot.addPoints(xEval0.getArray(), samplesPc95.getArray(), Plot.LINE);

        // average
        plot.setColor(ColorAVG);
        plot.addPoints(xEvals.getArray(), averageCD.getArray(), Plot.LINE);

        // observed
        plot.setColor(ColorOBS);
        plot.addPoints(observedDistances.getArray(), observedCD.getArray(), Plot.LINE);

        return plot;
    }

    /**
     * Main processing method for the DilateKernel_ object
     */
    public boolean process(ImageHandler plusSpots, ImageHandler plusMask, String functions, boolean verbose, boolean show, boolean save) {
        //Calibration calibration = plusSpots.getCalibration();
        /*
        if (calibration == null) {
            IJ.log("Image not calibrated");
            calibration = new Calibration();
            calibration.setUnit("pix");
            calibration.pixelWidth = 1;
            calibration.pixelHeight = 1;
            calibration.pixelDepth = 1;
        }
        */
        double scaleXY = plusMask.getScaleXY();
        double scaleZ = plusMask.getScaleZ();
        String unit = plusMask.getUnit();

        ImageInt inImage = (ImageInt) plusSpots;
        ImageInt segImage;
        if (inImage.isBinary(0)) {
            if (verbose) {
                IJ.log("Segmenting image...");
            }
            inImage = inImage.threshold(0, false, true);
            ImageLabeller labels = new ImageLabeller(false);
            segImage = labels.getLabels(inImage);
            if (labels.getNbObjectsTotal(inImage) < 2) {
                IJ.log("Not enough objects detected. Please check parameters and input images.");
                return false;
            }
            if (verbose) {
                segImage.show("Labelled Image");
            }
        } else {
            segImage = inImage.duplicate();
        }
        segImage.setScale(plusSpots);
        //segImage.setCalibration(calibration);

        int nbSpots;

        Objects3DPopulation pop = new Objects3DPopulation();
        ImageInt maskHandler = (ImageInt) plusMask;
        Object3D mask = new Object3DLabel(maskHandler, (int) maskHandler.getMax());
        //Object3D_IJUtils.setCalibration(mask, calibration);
        mask.setCalibration(scaleXY, scaleZ, unit);
        pop.setMask(mask);
        pop.addImage(segImage, 0);
        //pop.setCalibration(calibration);
        pop.setCalibration(scaleXY, scaleZ, unit);

        /*
        if ((plusMask.getCalibration() == null) || (!plusMask.getCalibration().scaled())) {
            if (verbose) {
                IJ.log("mask not calibrated, calibrating ...");
            }
            //plusMask.setCalibration(calibration);
            plusMask.setScale(plusSpots);
            plusMask.getImagePlus().updateAndRepaintWindow();
        }
        */

        nbSpots = pop.getNbObjects();

        // create one random sample image
        Objects3DPopulation popRandom = new Objects3DPopulation();
        popRandom.setCalibration(scaleXY, scaleZ, unit);
        popRandom.setMask(mask);
        boolean test = popRandom.createRandomPopulation(nbSpots, distHardCore);
        // check if everything was ok
        if (!test) {
            IJ.log("Could not create random population. Aborting.");
            return false;
        } else {
            randomPop = segImage.createSameDimensions();
            popRandom.draw(randomPop);

            if (verbose) {
                IJ.log("Computing spatial statistics, please wait ...");
            }

            if (verbose) {
                IJ.log("Nb Spot=" + nbSpots);
                IJ.log("Volume mask=" + mask.getVolumeUnit());
                IJ.log("Density=" + (nbSpots / mask.getVolumeUnit()));
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

            return true;
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

    public void setColorsPlot(Color avg, Color env, Color obs) {
        ColorAVG = avg;
        ColorENV = env;
        ColorOBS = obs;
    }
}
