package mcib_plugins;


import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.frame.Recorder;
import ij.process.*;
import java.awt.Checkbox;
import java.awt.Font;
import java.awt.TextField;
import java.io.*;
import java.text.NumberFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *  3D filtering 
 *
 * @author     Thomas BOUDIER
 * @created    feb 2008
 */
@SuppressWarnings("empty-statement")
public class Filters3d_jni implements PlugInFilter, DialogListener {

    static int count = 0;
    // jni is available
    static boolean jni_available = false;

    private native byte[][] jniMean3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniTopHat3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniTopHat3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels 16 bits
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a mean filter inside an ellipsoid
     */
    private native short[][] jniMean3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a median filter inside an ellipsoid
     */
    private native byte[][] jniMedian3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels 16 bits
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a median filter inside an ellipsoid
     */
    private native short[][] jniMedian3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a minimum filter inside an
     *      ellipsoid
     */
    private native byte[][] jniMinimum3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels 16 bits
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a minimum filter inside an
     *      ellipsoid
     */
    private native short[][] jniMinimum3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a maximum filter inside an
     *      ellipsoid
     */
    private native byte[][] jniMaximum3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniMaximumLocal3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    /**
     *  Description of the Method
     *
     * @param  test  Array of pixels 16 bits
     * @param  w     Width of the image
     * @param  h     Height of the image
     * @param  p     Z size of the image
     * @param  rx    Radius x of filtering
     * @param  ry    Radius y of filtering
     * @param  rz    Radius z of filtering
     * @return       Array of pixels filtered by a maximum filter inside an   ellipsoid
     */
    private native short[][] jniMaximum3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniMaximumLocal3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);
    ImagePlus imp;
    String filters[] = {"Mean", "Median", "Minimum", "Maximum", "MaximumLocal", "TopHat", "Variance"};
    int filter;
    int voisx = 2;
    int voisy = 2;
    int voisz = 2;
    boolean xy = true;
    Calibration calibration;
    double uvoisx = 0;
    double uvoisy = 0;
    double uvoisz = 0;

    /**
     *  Main processing method for the Median3D_ object
     *
     * @param  ip  Image
     */
    @Override
    public void run(ImageProcessor ip) {

        calibration = imp.getCalibration();

        if (Dialogue()) {
            // Macro
            if (Recorder.record) {
                Recorder.setCommand(null);
                Recorder.record("run", "3D JNI Filters\",\"filter=" + filters[filter] + " radius_x_pix=" + voisx + " radius_y_pix=" + voisy + " radius_z_pix=" + voisz);

                IJ.log("Performing 3D filter " + filters[filter] + " " + voisx + "x" + voisy + "x" + voisz);
            }

            Date t0 = new Date();

            // get stack info
            final ImageStack stack = imp.getStack();
            int s = stack.getSize();
            int w = stack.getWidth();
            int h = stack.getHeight();
            if ((jni_available)) {
                IJ.log("Using JNI");
                // JNI 8
                if (stack.getProcessor(1) instanceof ByteProcessor) {
                    // get stack arrays
                    byte[][] tabstack = new byte[s][w * h];
                    for (int i = 1; i <= s; i++) {
                        System.arraycopy((byte[]) stack.getPixels(i), 0, tabstack[i - 1], 0, w * h);
                    }
                    // calls jni
                    byte[][] res = null;
                    if (filter == 0) {
                        res = jniMean3D(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 1) {
                        res = jniMedian3D(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 2) {
                        res = jniMinimum3D(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 3) {
                        res = jniMaximum3D(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 4) {
                        res = jniMaximumLocal3D(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 5) {
                        res = jniTopHat3D(tabstack, w, h, s, voisx, voisy, voisz);
                    }
                    // get the result as a image stack and display it
                    ImageStack resstack = new ImageStack(w, h);
                    for (int i = 0; i < s; i++) {
                        resstack.addSlice("", res[i]);
                    }
                    // TOPHAT
                    if (filter == 5) {
                        StackProcessor stackprocess = new StackProcessor(stack, null);
                        stackprocess.copyBits(resstack, 0, 0, Blitter.SUBTRACT);
                        imp.updateAndDraw();
                    } else {
                        ImagePlus plus = new ImagePlus("3D_" + filters[filter], resstack);
                        plus.setCalibration(calibration);
                        plus.show();
                    }
                    tabstack = null;
                    System.gc();
                }

                // JNI 16
                if (stack.getProcessor(1) instanceof ShortProcessor) {
                    // get stack arrays
                    short[][] tabstack = new short[s][w * h];
                    for (int i = 1; i <= s; i++) {
                        System.arraycopy((short[]) stack.getPixels(i), 0, tabstack[i - 1], 0, w * h);
                    }
                    // calls jni
                    short[][] res = null;
                    if (filter == 0) {
                        res = jniMean3D_16(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 1) {
                        res = jniMedian3D_16(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 2) {
                        res = jniMinimum3D_16(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 3) {
                        res = jniMaximum3D_16(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 4) {
                        res = jniMaximumLocal3D_16(tabstack, w, h, s, voisx, voisy, voisz);
                    } else if (filter == 5) {
                        res = jniTopHat3D_16(tabstack, w, h, s, voisx, voisy, voisz);
                    }

                    // get the result as a image stack and display it
                    ImageStack resstack = new ImageStack(w, h);
                    for (int i = 0; i < s; i++) {
                        resstack.addSlice("", res[i]);
                    }
                    // TOPHAT
                    if (filter == 5) {
                        StackProcessor stackprocess = new StackProcessor(stack, null);
                        stackprocess.copyBits(resstack, 0, 0, Blitter.SUBTRACT);
                        imp.updateAndDraw();
                    } else {
                        ImagePlus plus = new ImagePlus("3D_" + filters[filter], resstack);
                        plus.setCalibration(calibration);
                        plus.show();
                    }
                    tabstack = null;
                    res = null;
                    System.gc();
                }
            }
            // time to process
            Date t1 = new Date();
            IJ.log("time : " + (t1.getTime() - t0.getTime()) + " ms");
        }
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, java.awt.AWTEvent e) {
        Vector fields = gd.getNumericFields();
        Vector fieldsb = gd.getCheckboxes();
        xy = ((Checkbox) fieldsb.elementAt(0)).getState();
        //System.out.println("" + voisx + " " + voisy + " " + voisz);
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        nf.setMaximumFractionDigits(3);


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
                                ((TextField) fields.elementAt(2)).setText(nf.format(uvoisy));

                                ((TextField) fields.elementAt(3)).setText(Integer.toString((int) Math.round(voisy)));
                            }
                        }
                        break;


                    case 1:
                        int v1 = Integer.valueOf(((TextField) fields.elementAt(1)).getText()).intValue();
                        if (v1 != voisx) {
                            ((TextField) fields.elementAt(0)).setText(nf.format(v1 * calibration.pixelWidth));
                            voisx = v1;
                            uvoisx = v1 * calibration.pixelWidth;
                            if (xy) {
                                uvoisy = uvoisx;
                                voisy = voisx;
                                ((TextField) fields.elementAt(2)).setText(nf.format(uvoisy));
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
                                ((TextField) fields.elementAt(0)).setText(nf.format(uvoisx));
                                ((TextField) fields.elementAt(1)).setText(Integer.toString((int) Math.round(voisx)));
                            }
                        }
                        break;


                    case 3:
                        int v2 = Integer.valueOf(((TextField) fields.elementAt(3)).getText()).intValue();
                        if (v2 != voisy) {
                            ((TextField) fields.elementAt(2)).setText(nf.format(v2 * calibration.pixelHeight));
                            voisy = v2;
                            uvoisy = v2 * calibration.pixelHeight;
                            if (xy) {
                                uvoisx = uvoisy;
                                voisx = voisy;
                                ((TextField) fields.elementAt(0)).setText(nf.format(uvoisx));
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
                            ((TextField) fields.elementAt(4)).setText(nf.format(v5 * calibration.pixelDepth));
                            voisz = v5;
                            uvoisz = v5 * calibration.pixelDepth;
                        }
                        break;
                    default:
                        break;
                }
            }

            if (!gd.invalidNumber());
        } catch (NumberFormatException nfe) {
            IJ.log(nfe.getMessage());
        }
        return true;
    }

    /**
     *  Dialogue of the plugin
     *
     * @return    ok or cancel
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


        gd.addDialogListener(this);

        gd.showDialog();
        filter = gd.getNextChoiceIndex();
        uvoisx = gd.getNextNumber();
        voisx = (int) gd.getNextNumber();
        uvoisy = gd.getNextNumber();
        voisy = (int) gd.getNextNumber();
        xy = gd.getNextBoolean();
        uvoisz = gd.getNextNumber();
        voisz = (int) gd.getNextNumber();

        // if first time


        if (count == 0) {
            // try to copy libfilter3d to plugins directory
            String lib = "";
            // jni = false;
            String bits = "";
            String ext = "";


            if (IJ.isLinux()) {
                ext = "so";


            } else if (IJ.isWindows()) {
                ext = "dll";


            } else if (IJ.isMacOSX()) {
                ext = "jnilib";


            }
            if (IJ.is64Bit()) {
                bits = "64";


            } else {
                bits = "32";


            }
            lib = "libfilter3d" + bits + "." + ext;
            InputStream is = getClass().getClassLoader().getResourceAsStream("jni/" + lib);



            try {
                File fi = new File(IJ.getDirectory("plugins") + lib);
                // File exists


                if (fi.exists()) {
                    IJ.log("Library " + lib + " already exists, delete it for update");


                } else {
                    OutputStream os = new FileOutputStream(fi);


                    byte[] buf = new byte[1024];


                    int len;


                    while ((len = is.read(buf)) > 0) {
                        os.write(buf, 0, len);


                    }
                    is.close();
                    os.close();
                    IJ.log(lib + " copied to " + fi.getAbsolutePath());



                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(Fast_filters3D.class.getName()).log(Level.SEVERE, null, ex);
            } catch (java.io.IOException ex) {
                Logger.getLogger(Fast_filters3D.class.getName()).log(Level.SEVERE, null, ex);
            }



            try {
                System.load(IJ.getDirectory("plugins") + lib);
                jni_available = true;



            } catch (UnsatisfiedLinkError ex) {
                Logger.getLogger(Fast_filters3D.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException e) {
                IJ.log("PB : " + e);


            }
        }
        count++;



        return (!gd.wasCanceled());


    }

    /**
     *  setup
     *
     * @param  arg  Argument of setup
     * @param  imp  ImagePlus info
     * @return      ok
     */
    @Override
    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;



        return DOES_8G + DOES_16;


    }
}
