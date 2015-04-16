package jni;


import ij.*;
import ij.gui.*;
import ij.plugin.filter.*;
import ij.process.*;

import java.io.File;

import java.util.*;

/**
 *  3D filtering by jni
 *
 * @author     Thomas BOUDIER
 * @created    feb 2008
 */
@SuppressWarnings("empty-statement")
public class filters3d_JNI implements PlugInFilter {

    static boolean jni = false;

    private native byte[][] jniMean3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniTopHat3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniTopHat3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniMean3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniMedian3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniMedian3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniMinimum3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniMinimum3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniMaximum3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native byte[][] jniMaximumLocal3D(byte[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniMaximum3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);

    private native short[][] jniMaximumLocal3D_16(short[][] test, int w, int h, int p, int rx, int ry, int rz);
    
    ImagePlus imp;
    String filters[] = {"Mean", "Median", "Minimum", "Maximum", "MaximumLocal", "TopHat"};
    int filter;
    int voisx = 2;
    int voisy = 2;
    int voisz = 2;

    /**
     *  Main processing method for the Median3D_ object
     *
     * @param  ip  Image
     */
    @Override
    public void run(ImageProcessor ip) {
        if (!jni) {
            IJ.log("NO JNI\nInstall JNI from\nhttp://imagejdocu.tudor.lu/doku.php?id=plugin:filter:3d_filters_with_jni:start");
        }

        if (Dialogue()) {
            Date t0 = new Date();

            // get stack info
            ImageStack stack = imp.getStack();
            int s = stack.getSize();
            int w = stack.getWidth();
            int h = stack.getHeight();
            if ((jni) && (filter < 6)) {
                // JNI 8
                if (stack.getProcessor(1) instanceof ByteProcessor) {
                    // get stack arrays
                    byte[][] tabstack = new byte[s][w * h];
                    for (int i = 1; i <= s; i++) {
                        for (int j = 0; j < w * h; j++) {
                            tabstack[i - 1][j] = ((byte[]) stack.getPixels(i))[j];
                        }
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
                    } else {
                        new ImagePlus("3D " + filters[filter], resstack).show();
                    }
                    tabstack = null;
                    System.gc();
                }

                // JNI 16
                if (stack.getProcessor(1) instanceof ShortProcessor) {
                    // get stack arrays
                    short[][] tabstack = new short[s][w * h];
                    for (int i = 1; i <= s; i++) {
                        for (int j = 0; j < w * h; j++) {
                            tabstack[i - 1][j] = ((short[]) stack.getPixels(i))[j];
                        }
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
                    } else {
                        new ImagePlus("3D_" + filters[filter], resstack).show();
                    }
                    tabstack = null;
                    res = null;
                    System.gc();
                }
            }
            // time to process
            Date t1 = new Date();
            System.out.println("time : " + (t1.getTime() - t0.getTime()) + " ms");
        }
    }

    /**
     *  Dialogue of the plugin
     *
     * @return    ok or cancel
     */
    private boolean Dialogue() {
        GenericDialog gd = new GenericDialog("3D_Filter");
        gd.addChoice("Filter", filters, filters[0]);
        gd.addNumericField("Radius_X", voisx, 0);
        gd.addNumericField("Radius_Y", voisy, 0);
        gd.addNumericField("Radius_Z", voisz, 0);
        gd.showDialog();
        filter = gd.getNextChoiceIndex();
        voisx = (int) gd.getNextNumber();
        voisy = (int) gd.getNextNumber();
        voisz = (int) gd.getNextNumber();
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

    static {
        String lib = "";
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
        lib = IJ.getDirectory("plugins") + "libfilter3d" + bits + "." + ext;
        File libfile = new File(lib);
        // if file exists and jni not already loaded
        if (libfile.exists()) {
            try {
                System.load(lib);
                jni = true;
                IJ.log(lib + " loaded");
            } catch (SecurityException e) {
                IJ.log("PB : " + e);
            }
            ;

        } else {
            IJ.log(lib + " does not exist");
            jni = false;
        }
    }
}
