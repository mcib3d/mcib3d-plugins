package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Toolbar;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import ij3d.Content;
import ij3d.Image3DUniverse;
import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import org.scijava.vecmath.Color3f;
import org.scijava.vecmath.Point3f;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.image3d.legacy.IntImage3D;
import mcib3d.utils.ArrayUtil;

/**
 * Description of the Class
 *
 * @author thomas @created 19 avril 2005
 */
public class Line3D_ implements PlugIn {

    /**
     * Main processing method for the Sphere3D_ object
     *
     * @param arg Description of the Parameter
     */
    @Override
    public void run(String arg) {
        int tx = 512;
        int ty = 512;
        int tz = 512;
        int x0 = tx / 2;
        int y0 = ty / 2;
        int z0 = tz / 2;
        int x1 = tx;
        int y1 = ty / 2;
        int z1 = tz / 2;
        double rad = 1;


        double resXY = 1.0;
        double resZ = 1.0;
        String unit = "pix";


        // 3D VIEWER
        List Viewers3D = Image3DUniverse.universes;
        Image3DUniverse universe;
        boolean viewer3d;
        if (Viewers3D.size() > 0) {
            viewer3d = true;
            universe = (Image3DUniverse) Viewers3D.get(0);
        } else {
            viewer3d = false;
            universe = null;
        }

        boolean profile = false;

        String title;

        ImagePlus plus = WindowManager.getCurrentImage();
        if (plus != null) {
            title = plus.getShortTitle();
        } else {
            title = "*None*";
        }
        Calibration cal = null;
        ImageStack stack = null;
        if (WindowManager.getWindowCount() > 0) {
            profile = true;
            stack = plus.getStack();
            tx = stack.getWidth();
            ty = stack.getHeight();
            tz = stack.getSize();
            x0 = tx / 2;
            y0 = ty / 2;
            z0 = tz / 2;
            x1 = tx;
            y1 = ty / 2;
            z1 = tz / 2;
            // calibration
            cal = WindowManager.getImage(WindowManager.getIDList()[0]).getCalibration();
            if (cal != null) {
                if (cal.scaled()) {
                    resXY = cal.getX(1.0);
                    resZ = cal.getZ(1.0);
                    unit = cal.getUnits();
                }
            }
        }
        String[] displays = {"New stack", "Overwrite", "None"};
        int display = 0;
        boolean arrow = true;
        GenericDialog gd = new GenericDialog("Line_3D");
        int w = 10;
        gd.addMessage("Image: " + title, new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("Size_X", tx, 0, w, "pixel");
        gd.addNumericField("Size_Y", ty, 0, w, "pixel");
        gd.addNumericField("Size_Z", tz, 0, w, "pixel");
        gd.addMessage("First Coordinate", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("x0", x0, 0, w, "pixel");
        gd.addNumericField("y0", y0, 0, w, "pixel");
        gd.addNumericField("z0", z0, 0, w, "pixel");
        gd.addMessage("Second Coordinate", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("x1", x1, 0, w, "pixel");
        gd.addNumericField("y1", y1, 0, w, "pixel");
        gd.addNumericField("z1", z1, 0, w, "pixel");
        gd.addMessage("Display", new Font("Arial", Font.BOLD, 12));
        gd.addCheckbox("Mark ends", arrow);
        gd.addCheckbox("Plot profile", profile);
        gd.addNumericField("thickness", rad, 3, w, "pixel");
        gd.addNumericField("Value", 255, 0);
        gd.addChoice("Display", displays, displays[display]);
        gd.addCheckbox("3D_viewer", viewer3d);
        gd.showDialog();
        if (gd.wasOKed()) {
            tx = (int) gd.getNextNumber();
            ty = (int) gd.getNextNumber();
            tz = (int) gd.getNextNumber();
            x0 = (int) gd.getNextNumber();
            y0 = (int) gd.getNextNumber();
            z0 = (int) gd.getNextNumber();
            x1 = (int) gd.getNextNumber();
            y1 = (int) gd.getNextNumber();
            z1 = (int) gd.getNextNumber();
            arrow = gd.getNextBoolean();
            profile = gd.getNextBoolean();
            rad = gd.getNextNumber();
            int val = (int) gd.getNextNumber();
            display = gd.getNextChoiceIndex();
            viewer3d = gd.getNextBoolean();
            ObjectCreator3D obj;
            int r = (int) (rad);
            
            // profile
            if (profile) {
                IntImage3D ima3d = new IntImage3D(stack);
                double[] line3d = ima3d.getLinePixelValue(x0, y0, z0, x1, y1, z1, false);
                ArrayUtil lineutil = new ArrayUtil(line3d);
                lineutil.getPlot().show();
            }
            // new stack
            if (display == 0) {
                obj = new ObjectCreator3D(tx, ty, tz);
                obj.setResolution(resXY, resZ, unit);
                obj.createLine(x0, y0, z0, x1, y1, z1, val, r);
                if (arrow) {
                    obj.createEllipsoid(x0, y0, z0, 2 * r, 2 * r, 2 * r, val, false);
                    obj.createEllipsoid(x1, y1, z1, 2 * r, 2 * r, 2 * r, val, false);
                }

                ImagePlus plusLine = new ImagePlus("Line3D", obj.getStack());
                plusLine.setCalibration(cal);
                plusLine.show("3D Profile of "+title);
            } // OVERWRITE
            else if (display == 1) {
                if (stack == null) {
                    IJ.log("No Stack !");
                } else {
                    obj = new ObjectCreator3D(stack);
                    obj.createLine(x0, y0, z0, x1, y1, z1, val, r);
                    plus.updateAndDraw();
                }
            }
            // 3D VIEWER
            if (viewer3d) {
                float rxy = (float) resXY;
                float rz = (float) resZ;
                Point3f p1 = new Point3f(x0 * rxy, y0 * rxy, z0 * rz);
                Point3f p2 = new Point3f(x1 * rxy, y1 * rxy, z1 * rz);
                List<Point3f> line = new ArrayList();
                line.add(p1);
                line.add(p2);
                if (universe == null) {
                    universe = new Image3DUniverse(512, 512);
                    universe.show();
                }
                // new name
                int l = 1;
                while (universe.contains("line" + l)) {
                    l++;
                }
                Color foreground = Toolbar.getForegroundColor();
                Content cline = universe.addLineMesh(line, new Color3f(foreground.getRed() / 255.0f, foreground.getGreen() / 255.0f, foreground.getBlue() / 255.0f), "line" + l, true);
                cline.setVisible(true);
                // Points  
                l = 1;
                while (universe.contains("point" + l)) {
                    l++;
                }
                Content cpoints = universe.addPointMesh(line, new Color3f(foreground.getRed() / 255.0f, foreground.getGreen() / 255.0f, foreground.getBlue() / 255.0f), 8, "point" + l);
                cpoints.setVisible(true);
            }            
        }
    }
}
