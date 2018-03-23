package mcib_plugins;

//import mcib3d.image3d.legacy.IntImage3D;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.measure.Calibration;
import ij.measure.CurveFitter;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3D;
import mcib3d.image3d.ImageHandler;
import mcib3d.utils.ArrayUtil;

/**
 * plugin de filtrage anisotropique 3D
 *
 * @author Thomas BOUDIER
 * @created avril 2003
 */
public class Radial_3D implements PlugInFilter {

    ImagePlus imp;
    int radMax;
    boolean fit = false;
    String mes;

    /**
     * Main processing method for the Median3D_ object
     *
     * @param ip Description of the Parameter
     */
    @Override
    public void run(ImageProcessor ip) {
        radMax = (int) Prefs.get("3Dradial.int", 10);
        ResultsTable rt = ResultsTable.getResultsTable();
        if (rt == null) {
            rt = new ResultsTable();
        }

        if (Dialogue()) {
            Roi roi = imp.getRoi();
            if (roi == null) {
                IJ.error("Point roi required !");
                return;
            }
            int x = roi.getBounds().x;
            int y = roi.getBounds().y;
            int z = imp.getCurrentSlice() - 1;
            Calibration cal = imp.getCalibration();
            ImageHandler ima = ImageHandler.wrap(imp);
            // scaling
            ima.setScale(cal.pixelWidth, cal.pixelDepth, cal.getUnit());
            //ima.setScale(1,1,"pix");
            double[] radidx = new double[2 * (int) radMax + 1];
            for (int i = 0; i < radidx.length; i++) {
                radidx[i] = -radMax + i;
            }

            IJ.log("" + x + " " + y + " " + z);
            // stats in sphere
            ArrayUtil sphere = ima.getNeighborhoodSphere(x, y, z, radMax, radMax, radMax);
            IJ.log("Mean in max sphere : " + sphere.getMean() + " StdDev : " + sphere.getStdDev());

            int me;
            if (mes.equals("Mean")) {
                me = Object3D.MEASURE_INTENSITY_AVG;
            } else if (mes.equals("Median")) {
                me = Object3D.MEASURE_INTENSITY_MEDIAN;
            } else if (mes.equals("Min")) {
                me = Object3D.MEASURE_INTENSITY_MIN;
            } else if (mes.equals("Max")) {
                me = Object3D.MEASURE_INTENSITY_MAX;
            } else {
                me = Object3D.MEASURE_INTENSITY_SD;
            }

            double radtab[] = ima.radialDistribution(x, y, z, radMax, me, null);

            // max local
            ArrayUtil tab = new ArrayUtil(radtab);
            int ml = tab.getFirstLocalMaxima(tab.getSize() / 2 + 1, 0);
            IJ.log("Max local " + radidx[ml]);
            int maxl = tab.getMaximumIndex();
            IJ.log("Max " + -radidx[maxl]);

            radtab[0] = radtab[1];
            Plot plot = new Plot("Radial distribution", "rad", "mean", radidx, radtab);

            if (fit) {
                // gaussian fit
                double params[] = ArrayUtil.fitGaussian(radtab, 2, radMax);

                // plot
                double gauss[] = new double[(2 * radMax + 1)];
                for (int i = 0; i < gauss.length; i++) {
                    gauss[i] = CurveFitter.f(CurveFitter.GAUSSIAN, params, radidx[i]);
                }
                plot.addPoints(radidx, gauss, Plot.CIRCLE);
                plot.show();

                double sig = params[3];
                if (sig < 0) {
                    sig *= -1.0;
                }
                // coeff sig to radMax

                double radius = sig;
                IJ.log("sigma=" + sig + " rad=" + radius);
                if (radius > 10) {
                    //IJ.log("Pb Rayon !!");
                    //radius = 2;
                }
                IJ.log("radius=" + radius);

                //for (int i = 0; i < params.length; i++) {
                //   IJ.write("par " + i + " = " + params[i]);
                //}
                //IJ.write("" + fit.getIterations() + " " + fit.getRestarts());
                if (Math.abs(params[2]) > 2) {
                    IJ.log("TWO OBJECTS ??? " + params[2]);
                }
                // 0.675 --> 50%
                double thresh = CurveFitter.f(CurveFitter.GAUSSIAN, params, 0.675 * params[3]);
                IJ.log("thresh=" + thresh);
                //IJ.write(""+fit.getIterations()+" / "+fit.getMaxIterations());
                if (thresh < 1) {
                    IJ.log("Pb threshold : " + thresh);
                }
                int lcseuil = (int) thresh;
                IJ.log("treshold=" + lcseuil);

                // Results Table
                rt.incrementCounter();
                int row = rt.getCounter();
                rt.setValue("Fit", row - 1, sig);
                rt.show("Results");

            } else {
                plot.show();
            }
        }
        Prefs.set("3Dradial.int", radMax);
    }

    /**
     * Description of the Method
     *
     * @return Description of the Return Value
     */
    private boolean Dialogue() {
        String[] meas = {"Mean", "Median", "Max", "Min", "StdDev"};
        GenericDialog gd = new GenericDialog("3D Radial distribution");
        gd.addNumericField("Radius_max", radMax, 0);
        gd.addChoice("Measure", meas, meas[0]);
        gd.addCheckbox("Fit Gaussian", fit);

        gd.showDialog();
        radMax = (int) gd.getNextNumber();
        fit = gd.getNextBoolean();
        mes = gd.getNextChoice();
        return (!gd.wasCanceled());
    }

    /**
     * Description of the Method
     *
     * @param arg Description of the Parameter
     * @param imp Description of the Parameter
     * @return Description of the Return Value
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_8G + DOES_16 + NO_CHANGES;
    }
}
