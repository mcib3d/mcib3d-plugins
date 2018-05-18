package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.Duplicator;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.Recorder;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import ij.process.StackProcessor;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.processing.FastFilters3D;
import mcib3d.utils.CheckInstall;
import mcib3d.utils.ThreadUtil;
import mcib_plugins.Filter3D.Filter3Dmax;
import mcib_plugins.Filter3D.Filter3DmaxLocal;
import mcib_plugins.Filter3D.Filter3Dmean;
import mcib_plugins.Filter3D.Filter3Dmin;

import java.awt.*;
import java.util.Date;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 3D filtering
 *
 * @author Thomas BOUDIER
 * @created feb 2008
 */
@SuppressWarnings("empty-statement")
public class Fast_filters3D implements PlugInFilter, DialogListener {

    int nbcpus;
    ImagePlus imp;
    String filters[] = {"Mean", "Median", "Minimum", "Maximum", "MaximumLocal", "TopHat", "OpenGray", "CloseGray", "Variance", "Sobel", "Adaptive"};
    String algos[] = {"Parallelized", "Isotropic"};
    int filter;
    float voisx = 2;
    float voisy = 2;
    float voisz = 2;
    boolean xy = true;
    Calibration calibration;
    double uvoisx = 0;
    double uvoisy = 0;
    double uvoisz = 0;
    boolean debug = false;
    private int algo = 0;

    /**
     * Main processing method for the Median3D_ object
     *
     * @param ip Image
     */
    @Override
    public void run(ImageProcessor ip) {
        if (!CheckInstall.installComplete()) {
            IJ.log("Not starting Filters 3D");
            return;
        }

        calibration = imp.getCalibration();
        ImageStack stack = imp.getStack();
        int depth = stack.getBitDepth();

        if (Dialogue()) {
            // Macro
            if (Recorder.record) {
                Recorder.setCommand(null);
                Recorder.record("run", "3D Fast Filters\",\"filter=" + filters[filter] + " radius_x_pix=" + voisx + " radius_y_pix=" + voisy + " radius_z_pix=" + voisz + " Nb_cpus=" + nbcpus);
                if (debug) {
                    IJ.log("Performing 3D filter " + filters[filter] + " " + voisx + "x" + voisy + "x" + voisz);
                }
            }

            boolean ff = ((voisx == voisy) && (voisx == voisz) && ((filter == FastFilters3D.MEAN) || (filter == FastFilters3D.MIN) || (filter == FastFilters3D.MAX) || (filter == FastFilters3D.MAXLOCAL) || (filter == FastFilters3D.TOPHAT) || (filter == FastFilters3D.OPENGRAY) || (filter == FastFilters3D.CLOSEGRAY)));

            Date t0 = new Date();

            if ((ff) && (algo == 1)) {
                if (debug) {
                    IJ.log("Using isotropic filtering");
                }
                FastFilter((int) voisx, filters[filter]);
            } else {
                ImageStack res = null;
                if ((depth == 8) || (depth == 16)) {
                    res = FastFilters3D.filterIntImageStack(stack, filter, voisx, voisy, voisz, nbcpus, true);
                } else if (imp.getBitDepth() == 32) {
                    //if ((filter != FastFilters3D.SOBEL)) {
                    res = FastFilters3D.filterFloatImageStack(stack, filter, voisx, voisy, voisz, nbcpus, true);
//                    } else {
//                        if (debug) {
//                            IJ.log("Not implemented for 32-bits images");
//                        }
                    // }
                    // }
                } else {
                    IJ.log("Does not wotk with stack with bitDepth " + depth);
                }
                if (res != null) {
                    ImagePlus plus = new ImagePlus("3D_" + filters[filter], res);
                    plus.setCalibration(calibration);
                    plus.show();
                }

            }
            // time to process
            Date t1 = new Date();
            if (debug) {
                IJ.log("time : " + (t1.getTime() - t0.getTime()) + " ms");
            }
        }
    }

    private void FastFilter(int radius, String selected_filter) {

        //read image
        ImagePlus in_image_j = IJ.getImage();
        ImageStack instack = in_image_j.getStack();
        Duplicator dup = new Duplicator();
        final ImagePlus img = dup.run(in_image_j);
        ImageStack orig = img.getStack();
        ImageShort out3d = new ImageShort("out3d", instack.getWidth(), instack.getHeight(), instack.getSize());
        ImageStack out_image = out3d.getImageStack();
        int rad = radius;
        int de = instack.getSize();

        // Parallelisation DOES NOT WORK !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        final AtomicInteger ai = new AtomicInteger(0);
        //final int n_cpus = nbcpus;
        final int n_cpus = 1;
        final int dec = (int) Math.ceil((double) de / (double) n_cpus);
        //Thread[] threads = ThreadUtil.createThreadArray(n_cpus);

        //process filter
        if (selected_filter.equals("Mean")) {
            Filter3Dmean mean = new Filter3Dmean(instack, out_image, rad);
            mean.filter();
        } else if (selected_filter.equals("Minimum")) {
            Filter3Dmin min = new Filter3Dmin(instack, out_image, rad);
            min.filter();

        } else if (selected_filter.equals("Maximum")) {
            Filter3Dmax max = new Filter3Dmax(instack, out_image, rad);
            max.filter();

        } else if (selected_filter.equals("MaximumLocal")) {
            Filter3DmaxLocal max = new Filter3DmaxLocal(instack, out_image, rad);
            max.filter();
        } else if (selected_filter.equals("TopHat")) {
            Filter3Dmin min = new Filter3Dmin(instack, out_image, rad);
            min.filter();
            // MAXIMUM
            ImageShort out3d2 = new ImageShort("out3d2", instack.getWidth(), instack.getHeight(), instack.getSize());
            ImageStack out_image2 = out3d2.getImageStack();
            Filter3Dmax max = new Filter3Dmax(out_image, out_image2, rad);
            max.filter();

            StackProcessor stackprocess = new StackProcessor(out_image2, null);
            stackprocess.copyBits(orig, 0, 0, Blitter.SUBTRACT);
        }
        ImagePlus out_plus = new ImagePlus(filters[filter], out_image);
        out_plus.setCalibration(calibration);
        out_plus.show();
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, java.awt.AWTEvent e) {
        Vector fields = gd.getNumericFields();
        Vector fieldsb = gd.getCheckboxes();
        xy = ((Checkbox) fieldsb.elementAt(0)).getState();
        //System.out.println("" + voisx + " " + voisy + " " + voisz);
        //NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        //nf.setMaximumFractionDigits(3);


        try {
            if ((e != null) && (!gd.invalidNumber())) {
                switch (fields.indexOf(e.getSource())) {
                    //////// X
                    case 0:
                        double v0 = Double.valueOf(((TextField) fields.elementAt(0)).getText()).doubleValue();
                        if (v0 != uvoisx) {
                            ((TextField) fields.elementAt(1)).setText(Integer.toString((int) Math.round(v0 / calibration.pixelWidth)));
                            uvoisx = v0;
                            voisx = (int) Math.round(v0 / calibration.pixelWidth);
                            if (xy) {
                                uvoisy = uvoisx;
                                voisy = voisx;
                                ((TextField) fields.elementAt(2)).setText("" + uvoisy);
                                ((TextField) fields.elementAt(3)).setText(Integer.toString((int) Math.round(voisy)));
                            }
                        }
                        break;


                    case 1:
                        int v1 = Integer.valueOf(((TextField) fields.elementAt(1)).getText()).intValue();
                        if (v1 != voisx) {
                            ((TextField) fields.elementAt(0)).setText("" + v1 * calibration.pixelWidth);
                            voisx = v1;
                            uvoisx = v1 * calibration.pixelWidth;
                            if (xy) {
                                uvoisy = uvoisx;
                                voisy = voisx;
                                ((TextField) fields.elementAt(2)).setText("" + uvoisy);
                                ((TextField) fields.elementAt(3)).setText(Integer.toString((int) Math.round(voisy)));
                            }
                        }
                        break;
                    //////// Y
                    case 2:
                        double v3 = Double.valueOf(((TextField) fields.elementAt(2)).getText()).doubleValue();
                        if (v3 != uvoisy) {
                            ((TextField) fields.elementAt(3)).setText(Integer.toString((int) Math.round(v3 / calibration.pixelHeight)));
                            uvoisy = v3;
                            voisy = (int) Math.round(v3 / calibration.pixelHeight);
                            if (xy) {
                                uvoisx = uvoisy;
                                voisx = voisy;
                                ((TextField) fields.elementAt(0)).setText("" + uvoisx);
                                ((TextField) fields.elementAt(1)).setText(Integer.toString((int) Math.round(voisx)));
                            }
                        }
                        break;


                    case 3:
                        int v2 = Integer.valueOf(((TextField) fields.elementAt(3)).getText()).intValue();
                        if (v2 != voisy) {
                            ((TextField) fields.elementAt(2)).setText("" + v2 * calibration.pixelHeight);
                            voisy = v2;
                            uvoisy = v2 * calibration.pixelHeight;
                            if (xy) {
                                uvoisx = uvoisy;
                                voisx = voisy;
                                ((TextField) fields.elementAt(0)).setText("" + uvoisx);
                                ((TextField) fields.elementAt(1)).setText(Integer.toString((int) Math.round(voisx)));
                            }
                        }
                        break;
                    //////// Z
                    case 4:
                        double v4 = Double.valueOf(((TextField) fields.elementAt(4)).getText()).doubleValue();
                        if (v4 != uvoisz) {
                            ((TextField) fields.elementAt(5)).setText(Integer.toString((int) Math.round(v4 / calibration.pixelDepth)));
                            uvoisz = v4;
                            voisz = (int) Math.round(v4 / calibration.pixelDepth);
                        }
                        break;
                    case 5:
                        int v5 = Integer.valueOf(((TextField) fields.elementAt(5)).getText()).intValue();
                        if (v5 != voisz) {
                            ((TextField) fields.elementAt(4)).setText("" + v5 * calibration.pixelDepth);
                            voisz = v5;
                            uvoisz = v5 * calibration.pixelDepth;
                        }
                        break;
                    default:
                        break;
                }
            }
            if (!gd.invalidNumber()) ;
        } catch (NumberFormatException nfe) {
            IJ.log(nfe.getMessage());
        }
        return true;
    }

    /**
     * Dialogue of the plugin
     *
     * @return ok or cancel
     */
    private boolean Dialogue() {
        String unit = calibration.getUnits();
        GenericDialog gd = new GenericDialog("3D_Filter");
        gd.addChoice("Filter", filters, filters[0]);
        gd.addMessage("Kernel_X", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("Radius_X_unit", voisx * calibration.pixelWidth, 0, 8, unit);
        gd.addNumericField("Radius_X_pix", voisx, 0, 8, "pix");
        gd.addMessage("Kernel_Y", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("Radius_Y_unit", voisy * calibration.pixelHeight, 0, 8, unit);
        gd.addNumericField("Radius_Y_pix", voisy, 0, 8, "pix");
        gd.addCheckbox("Synchronize X-Y", xy);
        gd.addMessage("kernel_Z", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("Radius_Z_unit", voisz * calibration.pixelDepth, 0, 8, unit);
        gd.addNumericField("Radius_Z_pix", voisz, 0, 8, "pix");
        gd.addMessage("Parallelization", new Font("Arial", Font.BOLD, 12));
        gd.addChoice("Algorithm", algos, algos[algo]);
        gd.addSlider("Nb_cpus", 1, ThreadUtil.getNbCpus(), ThreadUtil.getNbCpus());
        if (!IJ.macroRunning()) {
            gd.addDialogListener(this);
        }
        gd.showDialog();
        filter = gd.getNextChoiceIndex();
        uvoisx = gd.getNextNumber();
        voisx = (int) gd.getNextNumber();
        uvoisy = gd.getNextNumber();
        voisy = (int) gd.getNextNumber();
        xy = gd.getNextBoolean();
        uvoisz = gd.getNextNumber();
        voisz = (int) gd.getNextNumber();
        algo = gd.getNextChoiceIndex();
        nbcpus = (int) gd.getNextNumber();

        return (!gd.wasCanceled());
    }

    /**
     * setup
     *
     * @param arg Argument of setup
     * @param imp ImagePlus info
     * @return ok
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;

        return DOES_8G + DOES_16 + DOES_32;
    }

    public void setRadiusXYPix(int rad) {
        voisx = voisy = rad;
    }

    public void setRadiusZPix(int rad) {
        voisz = rad;
    }

    public void setFilter(int f) {
        filter = f;
    }

    public void setNbCpus(int nb) {
        if (nb > 0) {
            nbcpus = nb;
        } else {
            nbcpus = ThreadUtil.getNbCpus();
        }
    }
}
