/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_16;
import static ij.plugin.filter.PlugInFilter.DOES_32;
import static ij.plugin.filter.PlugInFilter.DOES_8G;
import ij.process.ImageProcessor;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import mcib3d.geom.Point3D;
import mcib3d.geom.Vector3D;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.processing.CannyDeriche1D;

/**
 *
 * @author thomasb
 */
public class Canny_Symmetry implements PlugInFilter {
    
    ImagePlus plus;
    
    double alpha = 0.5;
    int radius = 20;
    double normalize = 10;
    double scaling = 2;
    boolean showEdgesXYZ = false;
    boolean showIntermediate = false;
    boolean improved = true;
    
    @Override
    public int setup(String string, ImagePlus ip) {
        plus = ip;
        
        return DOES_16 + DOES_8G + DOES_32;
    }
    
    @Override
    public void run(ImageProcessor ip) {
        if (!dialog()) {
            return;
        }
        
        ImageHandler img = ImageHandler.wrap(plus);
        ImageHandler bin = new ImageFloat("BinNumber", img.sizeX, img.sizeY, img.sizeZ);
        ImageHandler bin2 = new ImageFloat("BinEdge", img.sizeX, img.sizeY, img.sizeZ);
        CannyDeriche1D canny;
        double[] line;
        double[] res;
        
        ImageHandler gx = new ImageFloat("EdgeX", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            IJ.showStatus("Edge X " + z + "/" + img.sizeZ);
            for (int y = 0; y < img.sizeY; y++) {
                line = img.getLineX(0, y, z, img.sizeX);
                canny = new CannyDeriche1D(line, alpha);
                res = canny.getCannyDeriche();
                gx.setLineX(0, y, z, res);
            }
        }
        if (showEdgesXYZ) {
            gx.show();
        }
        
        ImageHandler gy = new ImageFloat("EdgeY", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            IJ.showStatus("Edge Y " + z + "/" + img.sizeZ);
            for (int x = 0; x < img.sizeX; x++) {
                line = img.getLineY(x, 0, z, img.sizeY);
                canny = new CannyDeriche1D(line, alpha);
                res = canny.getCannyDeriche();
                gy.setLineY(x, 0, z, res);
            }
        }
        if (showEdgesXYZ) {
            gy.show();
        }
        
        ImageHandler gz = new ImageFloat("EdgeZ", img.sizeX, img.sizeY, img.sizeZ);
        for (int x = 0; x < img.sizeX; x++) {
            IJ.showStatus("Edge Z " + x + "/" + img.sizeX);
            for (int y = 0; y < img.sizeY; y++) {
                line = img.getLineZ(x, y, 0, img.sizeZ);
                canny = new CannyDeriche1D(line, alpha);
                res = canny.getCannyDeriche();
                gz.setLineZ(x, y, 0, res);
            }
        }
        if (showEdgesXYZ) {
            gz.show();
        }
        
        ImageHandler edge = new ImageFloat("Edge", img.sizeX, img.sizeY, img.sizeZ);
        for (int z = 0; z < img.sizeZ; z++) {
            IJ.showStatus("Edge and Symmetry " + z + "/" + img.sizeZ);
            for (int x = 0; x < img.sizeX; x++) {
                for (int y = 0; y < img.sizeY; y++) {
                    float ex = gx.getPixel(x, y, z);
                    float ey = gy.getPixel(x, y, z);
                    float ez = gz.getPixel(x, y, z);
                    float ee = (float) sqrt(ex * ex + ey * ey + ez * ez);
                    edge.setPixel(x, y, z, ee);
                    // bin
                    Vector3D grad = new Vector3D(ex, ey, ez);
                    grad.normalize();
                    Point3D pos = new Vector3D(x, y, z);
                    for (int d = 0; d < radius; d++) {
                        pos.translate(grad);
                        if ((d > 0) && (img.contains(pos.getRoundX(), pos.getRoundY(), pos.getRoundZ()))) {
                            bin.setPixel(pos, bin.getPixel(pos) + 1);
                            if (improved) {
                                bin2.setPixel(pos, (float) (bin2.getPixel(pos) + d * ee));
                            } else {
                                bin2.setPixel(pos, (float) (bin2.getPixel(pos) + ee));
                            }
                        }
                    }
                }
            }
        }
        if (showIntermediate) {
            bin.showDuplicate("BinNumber");
            bin2.showDuplicate("BinEdge");
        }
        edge.show();
        bin2.multiplyByValue((float) scaling);
        bin.multiplyByValue((float) pow(normalize, scaling));
        (bin.addImage(bin2, 1, 1)).show("Symmetry_"+radius);
    }
    
    private boolean dialog() {
        GenericDialog gd = new GenericDialog("Edge and Symmetry");
        gd.addMessage("Edge detection Canny Deriche");
        gd.addNumericField("alpha Canny", alpha, 3, 10, "");
        gd.addCheckbox("Show edges in X-Y-Z", showEdgesXYZ);
        gd.addMessage("Symmetry detection");
        gd.addNumericField("Radius", radius, 0, 10, "pix");
        gd.addNumericField("Normalization", normalize, 2, 10, "");
        gd.addNumericField("Scaling", scaling, 2, 10, "");
        gd.addCheckbox("Show intermediate", showIntermediate);
        gd.addCheckbox("Improved seed detection ", improved);
        gd.showDialog();
        alpha = gd.getNextNumber();
        showEdgesXYZ = gd.getNextBoolean();
        radius = (int) gd.getNextNumber();
        normalize = gd.getNextNumber();
        scaling = gd.getNextNumber();
        showIntermediate = gd.getNextBoolean();
        improved = gd.getNextBoolean();
        
        return gd.wasOKed();
    }
}
