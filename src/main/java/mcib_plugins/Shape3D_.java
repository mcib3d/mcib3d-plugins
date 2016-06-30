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
import java.text.NumberFormat;
import java.util.List;
import org.scijava.vecmath.Color3f;
import mcib3d.geom.GeomTransform3D;
import mcib3d.geom.Object3DSurface;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Vector3D;
import org.scijava.vecmath.Point3f;

/**
 * Description of the Class
 *
 * @author thomas
 * @created 19 avril 2005
 */
public class Shape3D_ implements PlugIn {

    /**
     * Main processing method for the Shape3D_ object
     *
     * @param arg Description of the Parameter
     */
    @Override
    public void run(String arg) {
        int tx = 512;
        int ty = 512;
        int tz = 512;
        double cx = tx / 2;
        double cy = ty / 2;
        double cz = tz / 2;
        double rx = tx / 8;
        double ry = ty / 8;
        double rz = tz / 8;
        double vx = 1;
        double vy = 0;
        double vz = 0;
        double wx = 0;
        double wy = 1;
        double wz = 0;
        //boolean sphere = true;

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

        boolean gauss = false;

        //boolean overwrite = false;
        String title;
        Calibration cal = null;
        ImagePlus plus = WindowManager.getCurrentImage();
        if (plus != null) {
            title = plus.getShortTitle();
        } else {
            title = "*None*";
        }
        ImageStack stack = null;
        if (WindowManager.getWindowCount() > 0) {
            stack = plus.getStack();
            tx = stack.getWidth();
            ty = stack.getHeight();
            tz = stack.getSize();
            cx = tx / 2;
            cy = ty / 2;
            cz = tz / 2;
            rx = tx / 8;
            ry = ty / 8;
            rz = tz / 8;
            // calibration
            cal = WindowManager.getImage(WindowManager.getIDList()[0]).getCalibration();
            if (cal != null) {
                if (cal.scaled()) {
                    resXY = cal.pixelWidth;
                    resZ = cal.pixelDepth;
                    unit = cal.getUnits();
                    cx = cal.getX(cx);
                    cy = cal.getY(cy);
                    cz = cal.getZ(cz);
                    rx = cal.getX(rx);
                    ry = cal.getY(ry);
                    rz = cal.getZ(rz);
                }
            }
        }
        if (cal == null) {
            cal = new Calibration();
            cal.pixelWidth = resXY;
            cal.pixelHeight = resXY;
            cal.pixelDepth = resZ;
            cal.setUnit(unit);
            unit = "unit";
        }
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(3);
        String[] displays = {"New stack", "Overwrite", "None"};
        int display = 0;
        GenericDialog gd = new GenericDialog("Sphere");
        gd.addMessage("Image: ", new Font("Arial", Font.BOLD, 12));
        gd.addMessage("Size in pixel");
        gd.addStringField("Size sx,sy,sz", tx + "," + ty + "," + tz, 25);
//        gd.addNumericField("Size_X", tx, 0);
//        gd.addNumericField("Size_Y", ty, 0);
//        gd.addNumericField("Size_Z", tz, 0);
        gd.addMessage("Shape: ", new Font("Arial", Font.BOLD, 12));
        gd.addMessage("Center in " + unit);
        gd.addStringField("Center cx,cy,cz", nf.format(cx) + "," + nf.format(cy) + "," + nf.format(cz), 25);
//        gd.addNumericField("Center_X (" + unit + ")", cx, 2);
//        gd.addNumericField("Center_Y (" + unit + ")", cy, 2);
//        gd.addNumericField("Center_Z (" + unit + ")", cz, 2);
        gd.addMessage("Radius in " + unit);
        gd.addStringField("Radius rx,ry,rz", nf.format(rx) + "," + nf.format(ry) + "," + nf.format(rz), 25);
//        gd.addNumericField("Radius_X (" + unit + ")", rx, 2);
//        gd.addNumericField("Radius_Y (" + unit + ")", ry, 2);
//        gd.addNumericField("Radius_Z (" + unit + ")", rz, 2);
        gd.addMessage("Orientation: ", new Font("Arial", Font.BOLD, 12));
        gd.addStringField("Vector1 vx,vy,vz", vx + "," + vy + "," + vz, 25);
//        gd.addNumericField("Orientation1_X", vx, 3);
//        gd.addNumericField("Orientation1_Y", vy, 3);
//        gd.addNumericField("Orientation1_Z", vz, 3);
        gd.addStringField("Vector2 wx,wy,wz", wx + "," + wy + "," + wz, 25);
//        gd.addNumericField("Orientation2_X", wx, 3);
//        gd.addNumericField("Orientation2_Y", wy, 3);
//        gd.addNumericField("Orientation2_Z", wz, 3);
        gd.addMessage("Resolution (for new stack): ", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("Res_XY", resXY, 3);
        gd.addNumericField("Res_Z", resZ, 3);
        gd.addStringField("Unit", unit);
        gd.addMessage("Display: ", new Font("Arial", Font.BOLD, 12));
        gd.addNumericField("Value", 255, 0);
        gd.addCheckbox("Gradient", gauss);
        gd.addChoice("Display", displays, displays[display]);
        gd.addCheckbox("3D_viewer", viewer3d);
        gd.showDialog();
        if (gd.wasOKed()) {
            String res = gd.getNextString();
            String[] vals = res.split(",");
            tx = Integer.parseInt(vals[0]);
            ty = Integer.parseInt(vals[1]);
            tz = Integer.parseInt(vals[2]);
//            tx = (int) gd.getNextNumber();
//            ty = (int) gd.getNextNumber();
//            tz = (int) gd.getNextNumber();
            res = gd.getNextString();
            vals = res.split(",");
            cx = Double.parseDouble(vals[0]);
            cy = Double.parseDouble(vals[1]);
            cz = Double.parseDouble(vals[2]);
//            cx = gd.getNextNumber();
//            cy = gd.getNextNumber();
//            cz = gd.getNextNumber();
            res = gd.getNextString();
            vals = res.split(",");
            rx = Double.parseDouble(vals[0]);
            ry = Double.parseDouble(vals[1]);
            rz = Double.parseDouble(vals[2]);
//            rx = gd.getNextNumber();
//            ry = gd.getNextNumber();
//            rz = gd.getNextNumber();
            res = gd.getNextString();
            vals = res.split(",");
            vx = Double.parseDouble(vals[0]);
            vy = Double.parseDouble(vals[1]);
            vz = Double.parseDouble(vals[2]);
//            vx = gd.getNextNumber();
//            vy = gd.getNextNumber();
//            vz = gd.getNextNumber();
            res = gd.getNextString();
            vals = res.split(",");
            wx = Double.parseDouble(vals[0]);
            wy = Double.parseDouble(vals[1]);
            wz = Double.parseDouble(vals[2]);
//            wx = gd.getNextNumber();
//            wy = gd.getNextNumber();
//            wz = gd.getNextNumber();
            //sphere = gd.getNextBoolean();
            resXY = gd.getNextNumber();
            resZ = gd.getNextNumber();
            unit = gd.getNextString();
            int val = (int) gd.getNextNumber();
            gauss = gd.getNextBoolean();
            display = gd.getNextChoiceIndex();
            viewer3d = gd.getNextBoolean();

            ObjectCreator3D obj;
            Vector3D V = new Vector3D(vx, vy, vz);
            Vector3D W = new Vector3D(wx, wy, wz);
            if (Math.abs(V.dotProduct(W)) > 0.001) {
                IJ.log("ERROR : vectors should be perpendicular");
            }

            /// test cone
//            obj = new ObjectCreator3D(tx, ty, tz);
//            obj.createConeAxes((int) cx, (int) cy, (int) cz, rx, rx, rx / 2, rx / 2, tz / 4, val, V, W);
//            obj.getImageHandler().show("cone");

            // ellipsoid
            Vector3D a = new Vector3D(vx, vy, vz);
            a.normalize();
            Vector3D b = new Vector3D(wx, wy, wz);
            b.normalize();
            Vector3D c = a.crossProduct(b);
            c.normalize();
            GeomTransform3D transform = new GeomTransform3D(new double[][]{
                {rx * a.getX(), ry * b.getX(), rz * c.getX(), cx},
                {rx * a.getY(), ry * b.getY(), rz * c.getY(), cy},
                {rx * a.getZ(), ry * b.getZ(), rz * c.getZ(), cz},
                {0, 0, 0, 1}});

            // new stack
            if (display == 0) {
                obj = new ObjectCreator3D(tx, ty, tz);
                obj.setResolution(resXY, resZ, unit);
                obj.createEllipsoidAxesUnit(cx, cy, cz, rx, ry, rz, (float) val, V, W, gauss);
                ImagePlus plusShape = new ImagePlus("Shape3D", obj.getStack());
                cal.pixelWidth = resXY;
                cal.pixelHeight = resXY;
                cal.pixelDepth = resZ;
                plusShape.setCalibration(cal);
                plusShape.setSlice((int) (cz / resZ));
                plusShape.setDisplayRange(0, val);
                plusShape.show();
            } // OVERWRITE
            else if (display == 1) {
                if (stack == null) {
                    IJ.log("No Stack !");
                } else {
                    obj = new ObjectCreator3D(stack);
                    obj.setCalibration(cal);
                    obj.createEllipsoidAxesUnit(cx, cy, cz, rx, ry, rz, (float) val, V, W, gauss);
                    plus.setSlice((int) (cz / resXY));
                    plus.setDisplayRange(0, val);
                    plus.updateAndDraw();
                }
            }
            if (viewer3d) {
                // 3D Viewer
                if (universe == null) {
                    universe = new Image3DUniverse(512, 512);
                    universe.show();
                }
                // new name
                int l = 1;
                while (universe.contains("ellipsoid" + l)) {
                    l++;
                }
                Color foreground = Toolbar.getForegroundColor();
                Content ellipsoid = universe.addTriangleMesh(Object3DSurface.createSphere(transform, 24, 24), new Color3f(foreground.getRed() / 255.0f, foreground.getGreen() / 255.0f, foreground.getBlue() / 255.0f), "ellipsoid" + l);
                ellipsoid.setVisible(true);
            }
        }
    }
}
