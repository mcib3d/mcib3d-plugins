package mcib_plugins.tools;

import ij.IJ;
import ij.Prefs;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Voxel3D;
import mcib3d.image3d.ImageHandler;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Manager3DMeasurements {
    RoiManager3D_2 manager3D;

    public static ResultsFrame measurements3D(List<Object3D> object3DList) {
        if (object3DList == null) return null;
        ArrayList<String> headings = new ArrayList<String>();
        headings.add("Nb");
        headings.add("Name");
        headings.add("Label");
        headings.add("Type");
        if (Prefs.get("RoiManager3D-Options_centroid-pix.boolean", true)) {
            headings.add("CX (pix)");
            headings.add("CY (pix)");
            headings.add("CZ (pix)");
        }
        if (Prefs.get("RoiManager3D-Options_centroid-unit.boolean", true)) {
            headings.add("CX (unit)");
            headings.add("CY (unit)");
            headings.add("CZ (unit)");
        }
        if (Prefs.get("RoiManager3D-Options_BB.boolean", true)) {
            headings.add("Xmin (pix)");
            headings.add("Ymin (pix)");
            headings.add("Zmin (pix)");
            headings.add("Xmax (pix)");
            headings.add("Ymax (pix)");
            headings.add("Zmax (pix)");
            headings.add("VolBounding (pix)");
            headings.add("RatioVolbox");
        }
        if (Prefs.get("RoiManager3D-Options_volume.boolean", true)) {
            headings.add("Vol (unit)");
            headings.add("Vol (pix)");
        }
        if (Prefs.get("RoiManager3D-Options_surface.boolean", true)) {
            headings.add("Surf (unit)");
            headings.add("Surf (pix)");
            headings.add("SurfCorr (pix)");
        }
        if (Prefs.get("RoiManager3D-Options_compacity.boolean", true)) {
            headings.add("Comp (pix)");
            headings.add("Spher (pix)");
            headings.add("CompCorr (pix)");
            headings.add("SpherCorr (pix)");
            headings.add("Comp (unit)");
            headings.add("Spher (unit)");
            headings.add("CompDiscrete");
        }
        if (Prefs.get("RoiManager3D-Options_feret.boolean", false)) {
            headings.add("Feret (unit)");
        }
        if (Prefs.get("RoiManager3D-Options_ellipse.boolean", true)) {
            headings.add("Ell_MajRad");
            headings.add("Ell_Elon");
            headings.add("Ell_Flatness");
            headings.add("volEllipsoid (unit)");
            headings.add("RatioVolEllipsoid");
        }
        if (Prefs.get("RoiManager3D-Options_invariants.boolean", false)) {
            for (int g = 0; g < Object3D.getNbMoments3D(); g++) {
                headings.add("Moment" + (g + 1));
            }
        }
        // CONVEX HULL
        if (Prefs.get("RoiManager3D-Options_convexhull.boolean", false)) {
            //headings.add("SurfMesh (unit)");
            //headings.add("SurfMeshsmooth (unit)");
            //headings.add("SurfMeshHull (unit)");
            headings.add("VolHull (unit)");
        }
        if (Prefs.get("RoiManager3D-Options_dist2Surf.boolean", true)) {
            headings.add("DCMin (unit)");
            headings.add("DCMax (unit)");
            headings.add("DCMean (unit)");
            headings.add("DCSD (unit)");
        }

        final Object[][] data = new Object[object3DList.size()][headings.size()];

        Object3D obj;
        double resXY;
        double resZ;
        //int count = rtMeasure.getCounter();
        for (int i = 0; i < object3DList.size(); i++) {
            obj = object3DList.get(i);
            int h = 0;
            data[i][h++] = i;
            data[i][h++] = obj.getName();
            data[i][h++] = obj.getValue();
            data[i][h++] = obj.getType();
            resXY = obj.getResXY();
            resZ = obj.getResZ();
            if (Prefs.get("RoiManager3D-Options_centroid-pix.boolean", true)) {
                data[i][h++] = obj.getCenterX();
                data[i][h++] = obj.getCenterY();
                data[i][h++] = obj.getCenterZ();
            }
            if (Prefs.get("RoiManager3D-Options_centroid-unit.boolean", true)) {
                data[i][h++] = obj.getCenterX() * resXY;
                data[i][h++] = obj.getCenterY() * resXY;
                data[i][h++] = obj.getCenterZ() * resZ;
            }
            if (Prefs.get("RoiManager3D-Options_BB.boolean", true)) {
                data[i][h++] = obj.getXmin();
                data[i][h++] = obj.getYmin();
                data[i][h++] = obj.getZmin();
                data[i][h++] = obj.getXmax();
                data[i][h++] = obj.getYmax();
                data[i][h++] = obj.getZmax();
                data[i][h++] = obj.getVolumeBoundingBoxPixel();
                data[i][h++] = obj.getRatioBox();
            }
            if (Prefs.get("RoiManager3D-Options_volume.boolean", true)) {
                data[i][h++] = obj.getVolumeUnit();
                data[i][h++] = obj.getVolumePixels();
            }
            if (Prefs.get("RoiManager3D-Options_surface.boolean", true)) {
                data[i][h++] = obj.getAreaUnit();
                data[i][h++] = obj.getAreaPixels();
                if (obj instanceof Object3DVoxels) {
                    data[i][h++] = ((Object3DVoxels) obj).getAreaPixelsCorrected();
                } else {
                    data[i][h++] = -1;
                }
            }
            if (Prefs.get("RoiManager3D-Options_compacity.boolean", true)) {
                data[i][h++] = obj.getCompactness(false);
                data[i][h++] = obj.getSphericity(false);
                // corrected surface
                if (obj instanceof Object3DVoxels) {
                    data[i][h++] = ((Object3DVoxels) obj).getCompactnessCorrected();
                    data[i][h++] = ((Object3DVoxels) obj).getSphericityCorrected();
                } else {
                    data[i][h++] = -1;
                    data[i][h++] = -1;
                }
                data[i][h++] = obj.getCompactness(true);
                data[i][h++] = obj.getSphericity(true);

                // TEST DISCRETE COMPACITE
                if (obj instanceof Object3DVoxels) {
                    data[i][h++] = ((Object3DVoxels) obj).getDiscreteCompactness();
                } else {
                    data[i][h++] = -1;
                }
            }
            if (Prefs.get("RoiManager3D-Options_feret.boolean", false)) {
                data[i][h++] = obj.getFeret();
            }
            if (Prefs.get("RoiManager3D-Options_ellipse.boolean", true)) {
                data[i][h++] = obj.getRadiusMoments(2);
                data[i][h++] = obj.getMainElongation();
                data[i][h++] = obj.getMedianElongation();
                data[i][h++] = obj.getVolumeEllipseUnit();
                data[i][h++] = obj.getRatioEllipsoid();
            }
            if (Prefs.get("RoiManager3D-Options_invariants.boolean", false)) {
                double[] geoinv = obj.getMoments3D();
                for (int g = 0; g < geoinv.length; g++) {
                    data[i][h++] = geoinv[g];
                }
            }
            // CONVEX HULL //TODO put in a thread because very long
            if (Prefs.get("RoiManager3D-Options_convexhull.boolean", false)) {
                Object3D object3DConvex = obj.getConvexObject();
                double volHull = object3DConvex.getVolumeUnit();
                data[i][h++] = volHull;
            }
            if (Prefs.get("RoiManager3D-Options_dist2Surf.boolean", true)) {
                data[i][h++] = obj.getDistCenterMin();
                data[i][h++] = obj.getDistCenterMax();
                data[i][h++] = obj.getDistCenterMean();
                data[i][h++] = obj.getDistCenterSigma();
            }
        }

        // JTABLE
        //Create and set up the window.
        String[] heads = new String[headings.size()];
        heads = headings.toArray(heads);
        ResultsFrame tableResultsMeasure = new ResultsFrame("3D Measure", heads, data, ResultsFrame.OBJECT_1);

        return tableResultsMeasure;
    }

    public static ResultsFrame quantif3D(List<Object3D> object3DList, ImageHandler ima) {
        ArrayList<String> headings = new ArrayList<String>();
        headings.add("Nb");
        headings.add("Name");
        headings.add("Label");
        headings.add("Type");
        headings.add("AtCenter");
        if (Prefs.get("RoiManager3D-Options_COM-pix.boolean", true)) {
            headings.add("CMx (pix)");
            headings.add("CMy (pix)");
            headings.add("CMz (pix)");
        }
        if (Prefs.get("RoiManager3D-Options_COM-unit.boolean", true)) {
            headings.add("CMx (unit)");
            headings.add("CMy (unit)");
            headings.add("CMz (unit)");
        }
        if (Prefs.get("RoiManager3D-Options_intDens.boolean", true)) {
            headings.add("IntDen");
        }
        if (Prefs.get("RoiManager3D-Options_min.boolean", true)) {
            headings.add("Min");
        }
        if (Prefs.get("RoiManager3D-Options_max.boolean", true)) {
            headings.add("Max");
        }
        if (Prefs.get("RoiManager3D-Options_mean.boolean", true)) {
            headings.add("Mean");
        }
        if (Prefs.get("RoiManager3D-Options_stdDev.boolean", true)) {
            headings.add("Sigma");
        }
        if (Prefs.get("RoiManager3D-Options_Mode.boolean", true)) {
            headings.add("Mode");
            headings.add("Mode NonZero");
        }
        if (Prefs.get("RoiManager3D-Options_Numbering.boolean", true)) {
            headings.add("NbObjects");
            headings.add("VolObjects");
        }


        Object[][] data = new Object[object3DList.size()][headings.size()];
        double resXY, resZ;
        Object3D obj;

        for (int i = 0; i < object3DList.size(); i++) {
            obj = object3DList.get(i);
            int h = 0;
            data[i][h++] = i;
            data[i][h++] = obj.getName();
            data[i][h++] = obj.getValue();
            data[i][h++] = obj.getType();
            resXY = obj.getResXY();
            resZ = obj.getResZ();
            data[i][h++] = obj.getPixCenterValue(ima);
            if (Prefs.get("RoiManager3D-Options_COM-pix.boolean", true)) {
                data[i][h++] = obj.getMassCenterX(ima);
                data[i][h++] = obj.getMassCenterY(ima);
                data[i][h++] = obj.getMassCenterZ(ima);
            }
            if (Prefs.get("RoiManager3D-Options_COM-unit.boolean", true)) {
                data[i][h++] = obj.getMassCenterX(ima) * resXY;
                data[i][h++] = obj.getMassCenterY(ima) * resXY;
                data[i][h++] = obj.getMassCenterZ(ima) * resZ;
            }
            if (Prefs.get("RoiManager3D-Options_intDens.boolean", true)) {
                data[i][h++] = obj.getIntegratedDensity(ima);
            }
            if (Prefs.get("RoiManager3D-Options_min.boolean", true)) {
                data[i][h++] = obj.getPixMinValue(ima);
            }
            if (Prefs.get("RoiManager3D-Options_max.boolean", true)) {
                data[i][h++] = obj.getPixMaxValue(ima);
            }
            if (Prefs.get("RoiManager3D-Options_mean.boolean", true)) {
                data[i][h++] = obj.getPixMeanValue(ima);
            }
            if (Prefs.get("RoiManager3D-Options_stdDev.boolean", true)) {
                data[i][h++] = obj.getPixStdDevValue(ima);
            }
            if (Prefs.get("RoiManager3D-Options_Mode.boolean", true)) {
                data[i][h++] = obj.getPixModeValue(ima);
                data[i][h++] = obj.getPixModeNonZero(ima);
            }
            if (Prefs.get("RoiManager3D-Options_Numbering.boolean", true)) {
                int[] res = obj.getNumbering(ima);
                data[i][h++] = res[0];
                data[i][h++] = res[1];
            }
        }

        // JTABLE
        //Create and set up the window.
        String[] heads = new String[headings.size()];
        heads = headings.toArray(heads);
        ResultsFrame tableResultsQuantif = new ResultsFrame("3D Quantif", heads, data, ResultsFrame.OBJECT_NO);

        return tableResultsQuantif;
    }

    public static ResultsFrame listVoxels(List<Object3D> object3DList, ImageHandler ima) {
        ArrayList<String> headings = new ArrayList<String>();
        headings.add("Nb");
        headings.add("Name");
        headings.add("Label");
        headings.add("Type");
        headings.add("X");
        headings.add("Y");
        headings.add("Z");
        headings.add("Value");

        // nb totals de voxels
        int vol = 0;
        for (Object3D object3D : object3DList) {
            vol += object3D.getVolumePixels();
        }

        Object[][] data = new Object[vol][headings.size()];

        int count = 0;
        LinkedList<Voxel3D> voxel3DS;
        for (int ob = 0; ob < object3DList.size(); ob++) {
            Object3D obj = object3DList.get(ob);
            voxel3DS = obj.listVoxels(ima);
            if (voxel3DS == null) {
                IJ.log("No voxels to display for " + obj.getName());
            }
            for (Voxel3D voxel : voxel3DS) {
                int h = 0;
                data[count][h++] = count;
                data[count][h++] = obj.getName();
                data[count][h++] = obj.getValue();
                data[count][h++] = obj.getType();
                data[count][h++] = voxel.getX();
                data[count][h++] = voxel.getY();
                data[count][h++] = voxel.getZ();
                data[count][h++] = voxel.getValue();
                count++;
            }
        }

        // JTABLE
        //Create and set up the window.
        String[] heads = new String[headings.size()];
        heads = headings.toArray(heads);
        ResultsFrame tableResultsVoxels = new ResultsFrame("3D Voxels", heads, data, ResultsFrame.OBJECT_1);

        return tableResultsVoxels;
    }
}
