package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import ij.measure.ResultsTable;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.*;
import mcib3d.image3d.ImageInt;
import mcib3d.utils.ArrayUtil;

import java.text.NumberFormat;

/**
 * plugin to fit an 3D ellipsoid to a shape
 *
 * @author Thomas BOUDIER
 * @created avril 2003
 */
public class Ellipsoids_3D implements PlugInFilter {

    ImagePlus imp;
    float rad;

    /**
     * Main processing method for the Axes3D_ object
     *
     * @param ip Description of the Parameter
     */
    @Override
    public void run(ImageProcessor ip) {
        Calibration cal = imp.getCalibration();
        double resXY = 1.0;
        double resZ = 1.0;
        String unit = "pix";
        if (cal != null) {
            if (cal.scaled()) {
                resXY = cal.pixelWidth;
                resZ = cal.pixelDepth;
                unit = cal.getUnits();
            }
        }
        //drawing of ellipses
        ObjectCreator3D ellipsoid = new ObjectCreator3D(imp.getWidth(), imp.getHeight(), imp.getStackSize());
        ellipsoid.setResolution(resXY, resZ, unit);
        //drawing of main direction vectors 
        ObjectCreator3D vectors = new ObjectCreator3D(imp.getWidth(), imp.getHeight(), imp.getStackSize());
        vectors.setResolution(resXY, resZ, unit);
        //drawing of oriented contours
        ObjectCreator3D oriC = new ObjectCreator3D(imp.getWidth(), imp.getHeight(), imp.getStackSize());
        oriC.setResolution(resXY, resZ, unit);
        // all objects from count masks
        ImageInt imageInt = ImageInt.wrap(imp);
        Objects3DPopulation pop = new Objects3DPopulation(imageInt);
        // ResultsTable
        ResultsTable rt;
        if (ResultsTable.getResultsTable() != null) {
            rt = ResultsTable.getResultsTable();
        } else {
            rt = new ResultsTable();
        }
        int row = rt.getCounter();
        for (int ob = 0; ob < pop.getNbObjects(); ob++) {
            IJ.log("Processing obj " + ob);
            Object3D obj = pop.getObject(ob);

            NumberFormat nf = NumberFormat.getInstance();
            nf.setMaximumFractionDigits(3);

            if (obj.getVolumePixels() > 0) {
                IJ.log("\nobj " + ob + "-" + obj.getValue() + " (2 is main axis)");
                //obj.computeMoments();
                double[] axy = new double[3];
                double[] axz = new double[3];
                double[] ayz = new double[3];
                for (int i = 0; i < 3; i++) {
                    Vector3D V = obj.getVectorAxis(i);
                    IJ.log(ob + ": Vector " + i + " : " + V);
                    IJ.log(ob + ": Value  " + i + " : " + nf.format(obj.getValueAxis(i)));
                    IJ.log(ob + ": Value  sqrt " + i + " : " + nf.format(Math.sqrt(obj.getValueAxis(i))));
                    axy[i] = V.anglePlaneDegrees(0, 0, 1, 0);
                    axz[i] = V.anglePlaneDegrees(0, 1, 0, 0);
                    ayz[i] = V.anglePlaneDegrees(1, 0, 0, 0);
                    IJ.log("Angle " + i + " with plane XY " + axy[i]);
                    IJ.log("Angle " + i + " with plane XZ " + axz[i]);
                    IJ.log("Angle " + i + " with plane YZ " + ayz[i]);
                }
                Vector3D V = obj.getVectorAxis(2);
                Vector3D W = obj.getVectorAxis(1);
                Vector3D U = obj.getVectorAxis(0);
                //obj.computeContours();
                //double r1 = obj.getDistCenterMax();
                double r1 = obj.getRadiusMoments(2);
                double rad1 = r1;
                double rad2 = Double.NaN;
                if (!Double.isNaN(obj.getMainElongation())) {
                    rad2 = rad1 / obj.getMainElongation();
                }
                double rad3 = Double.NaN;
                if (!Double.isNaN(obj.getMedianElongation())) {
                    rad3 = rad2 / obj.getMedianElongation();
                }
                if ((!Double.isNaN(rad2)) && (!Double.isNaN(rad3))) {
                    IJ.log(ob + ": radii=" + nf.format(rad1) + " " + nf.format(rad2) + " " + nf.format(rad3));
                } else {
                    IJ.log(ob + ": radii=" + nf.format(rad1) + " " + nf.format(rad2) + " " + "NaN");
                }
                IJ.log("Max :" + r1);
                // dist in first axis
                double d1 = obj.distPixelBorderUnit((int) obj.getCenterX(), (int) obj.getCenterY(), (int) obj.getCenterZ(), obj.getVectorAxis(2));
                double d2 = obj.distPixelBorderUnit((int) obj.getCenterX(), (int) obj.getCenterY(), (int) obj.getCenterZ(), obj.getVectorAxis(2).multiply(-1));
                IJ.log("major from distance " + d1 + " " + d2);
                // center
                IJ.log("Center : " + obj.getCenterX() + " " + obj.getCenterY() + " " + obj.getCenterZ());
                // draw ellipsoid
                int val = obj.getValue();
                ellipsoid.createEllipsoidAxesUnit(obj.getCenterX() * resXY, obj.getCenterY() * resXY, obj.getCenterZ() * resZ, rad1, rad2, rad3, val, V, W, false);
                // draw line for direction vectors
                Vector3D Vec = obj.getCenterAsVectorUnit();
                Vector3D end = Vec.add(obj.getVectorAxis(2), 1, rad1);
                vectors.createLineUnit(Vec, end, val, 1);

                // The two poles as Feret 
                Voxel3D Feret1 = obj.getFeretVoxel1();
                Voxel3D Feret2 = obj.getFeretVoxel2();
                IJ.log("Pole1 as Feret 1 : " + Feret1);
                IJ.log("Pole2 as Feret 2 : " + Feret2);
                IJ.log("Pole1 as Feret 1 (calibrated) : " + Feret1.getX() * resXY + " " + Feret1.getY() * resXY + " " + Feret1.getZ() * resZ);
                IJ.log("Pole2 as Feret 2 (calibrated) : " + Feret2.getX() * resXY + " " + Feret2.getY() * resXY + " " + Feret2.getZ() * resZ);

                // The two poles as Feret of ellipsoid
                Object3D ell = new Object3DVoxels(ellipsoid.getImageHandler(), val);
                //ell.computeContours();
                Voxel3D Ell1 = null;
                Voxel3D Ell2 = null;
                if (ell.isEmpty()) IJ.log("Cannot compute ellipsoid.");
                else {
                    Ell1 = ell.getFeretVoxel1();
                    Ell2 = ell.getFeretVoxel2();
                    IJ.log("Pole1 as ellipsoid 1 : " + Ell1);
                    IJ.log("Pole2 as ellipsoid 2 : " + Ell2);
                    IJ.log("Pole1 as ellipsoid 1 (calibrated) : " + Ell1.getX() * resXY + " " + Ell1.getY() * resXY + " " + Ell1.getZ() * resZ);
                    IJ.log("Pole2 as ellipsoid 2 (calibrated) : " + Ell2.getX() * resXY + " " + Ell2.getY() * resXY + " " + Ell2.getZ() * resZ);
                }

                //  ORIENTED BB
                oriC.drawVoxels(obj.getBoundingOriented());

                //  BOUNDING BOX + ORIENTED //
                ArrayUtil tab = new ArrayUtil(obj.getBoundingBox());
                IJ.log("BB  : " + tab.toString());
                //  VOLUMES
                IJ.log("Volumes:");
                IJ.log("obj:" + obj.getVolumeUnit() + " units");
                IJ.log("ell:" + obj.getVolumeEllipseUnit() + " unit");
                IJ.log("obj:" + obj.getVolumePixels() + " pixels");
                IJ.log("bb:" + obj.getVolumeBoundingBoxPixel() + " pixels");
                IJ.log("bbo:" + obj.getVolumeBoundingBoxOrientedPixel() + " pixels");

                // RESULTS TABLE
                rt.incrementCounter();
                // center
                rt.setValue("Cx(pix)", row, obj.getCenterX());
                rt.setValue("Cy(pix)", row, obj.getCenterY());
                rt.setValue("Cz(pix)", row, obj.getCenterZ());
                // third axis
                rt.setValue("Vx0(pix)", row, U.getX());
                rt.setValue("Vy0(pix)", row, U.getY());
                rt.setValue("Vz0(pix)", row, U.getZ());
                // second axis
                rt.setValue("Vx1(pix)", row, W.getX());
                rt.setValue("Vy1(pix)", row, W.getY());
                rt.setValue("Vz1(pix)", row, W.getZ());
                // main axis
                rt.setValue("Vx2(pix)", row, V.getX());
                rt.setValue("Vy2(pix)", row, V.getY());
                rt.setValue("Vz2(pix)", row, V.getZ());
                // radii
                rt.setValue("R1(unit)", row, rad1);
                rt.setValue("R2(unit)", row, rad2);
                rt.setValue("R3(unit)", row, rad3);
                // angles
                rt.setValue("XY0(deg)", row, axy[0]);
                rt.setValue("XZ0(deg)", row, axz[0]);
                rt.setValue("YZ0(deg)", row, ayz[0]);
                rt.setValue("XY1(deg)", row, axy[1]);
                rt.setValue("XZ1(deg)", row, axz[1]);
                rt.setValue("YZ1(deg)", row, ayz[1]);
                rt.setValue("XY2(deg)", row, axy[2]);
                rt.setValue("XZ2(deg)", row, axz[2]);
                rt.setValue("YZ2(deg)", row, ayz[2]);
                // volumes
                rt.setValue("Vobj(pix)", row, obj.getVolumePixels());
                rt.setValue("Vobj(unit)", row, obj.getVolumeUnit());
                rt.setValue("Vell(unit)", row, obj.getVolumeEllipseUnit());
                rt.setValue("RatioVobjVell", row, obj.getRatioEllipsoid());
                rt.setValue("Vbb(pix)", row, obj.getVolumeBoundingBoxPixel());
                rt.setValue("Vbbo(pix)", row, obj.getVolumeBoundingBoxOrientedPixel());
                // poles obj
                rt.setValue("Feret1.X", row, Feret1.getX());
                rt.setValue("Feret1.Y", row, Feret1.getY());
                rt.setValue("Feret1.Z", row, Feret1.getZ());
                rt.setValue("Feret2.X", row, Feret2.getX());
                rt.setValue("Feret2.Y", row, Feret2.getY());
                rt.setValue("Feret2.Z", row, Feret2.getZ());
                // poles obj
                if ((Ell1 != null) && (Ell2 != null)) {
                    rt.setValue("Pole1.X", row, Ell1.getX());
                    rt.setValue("Pole1.Y", row, Ell1.getY());
                    rt.setValue("Pole1.Z", row, Ell1.getZ());
                    rt.setValue("Pole2.X", row, Ell2.getX());
                    rt.setValue("Pole2.Y", row, Ell2.getY());
                    rt.setValue("Pole2.Z", row, Ell2.getZ());
                }
                row++;
            }
        }
        rt.show("Results");

        ImagePlus plus = new ImagePlus("Ellipsoids", ellipsoid.getStack());
        if (cal != null) {
            plus.setCalibration(cal);
        }
        plus.show();
        ImagePlus plus2 = new ImagePlus("Vectors", vectors.getStack());
        if (cal != null) {
            plus2.setCalibration(cal);
        }
        plus2.show();

        ImagePlus plus3 = new ImagePlus("Oriented Contours", oriC.getStack());
        if (cal != null) {
            plus3.setCalibration(cal);
        }
        plus3.show();

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
