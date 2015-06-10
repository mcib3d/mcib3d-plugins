package mcib_plugins.tools;

/*
 * _3D_objects_counter.java
 *
 * Created on 7 novembre 2007, 11:54
 *
 * Copyright (C) 2007 Fabrice P. Cordeli�res
 *
 * License: This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */
// Modified by Thomas Boudier for RoiManager3D_
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import java.awt.Font;

/**
 *
 * @author Fabrice P. Cordelières, fabrice.cordelieres@gmail.com
 * @version 1.0, 7/11/07
 */
public class RoiManager3D_Options implements PlugIn {

    @Override
    public void run(String arg) {
        // Parameters to compute

        String[] label = new String[22];
        boolean[] state = new boolean[label.length];
        int h = 0;
        label[h] = "Volume (unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_volume.boolean", true);
        label[h] = "Surface (unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_surface.boolean", true);
        label[h] = "Compactness";
        state[h++] = Prefs.get("RoiManager3D-Options_compacity.boolean", true);
        label[h] = "Fit_Ellipse";
        state[h++] = Prefs.get("RoiManager3D-Options_ellipse.boolean", true);
        label[h] = "3D_Moments";
        state[h++] = Prefs.get("RoiManager3D-Options_invariants.boolean", false);
        label[h] = "Convex_Hull (slow)";
        state[h++] = Prefs.get("RoiManager3D-Options_convexhull.boolean", false);
        label[h] = "Integrated_Density";
        state[h++] = Prefs.get("RoiManager3D-Options_intDens.boolean", true);
        label[h] = "Mean_Grey_Value";
        state[h++] = Prefs.get("RoiManager3D-Options_mean.boolean", true);
        label[h] = "Std_Dev_Grey_Value";
        state[h++] = Prefs.get("RoiManager3D-Options_stdDev.boolean", true);
        label[h] = "Mode_Grey_Value";
        state[h++] = Prefs.get("RoiManager3D-Options_Mode.boolean", false);
        label[h] = "Feret (unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_feret.boolean", false);
        label[h] = "Minimum_Grey_Value";
        state[h++] = Prefs.get("RoiManager3D-Options_min.boolean", true);
        label[h] = "Maximum_Grey_Value";
        state[h++] = Prefs.get("RoiManager3D-Options_max.boolean", true);
        label[h] = "Centroid_(pix)";
        state[h++] = Prefs.get("RoiManager3D-Options_centroid-pix.boolean", true);
        label[h] = "Centroid_(unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_centroid-unit.boolean", true);
        label[h] = "Distance_to_surface (unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_dist2Surf.boolean", true);
        label[h] = "Centre_of_mass_(pix)";
        state[h++] = Prefs.get("RoiManager3D-Options_COM-pix.boolean", true);
        label[h] = "Centre_of_mass_(unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_COM-unit.boolean", true);
        label[h] = "Bounding_box (pix)";
        state[h++] = Prefs.get("RoiManager3D-Options_BB.boolean", true);
        label[h] = "Radial_distance (unit)";
        state[h++] = Prefs.get("RoiManager3D-Options_RadDist.boolean", true);
        label[h] = "Surface_Contact (voxel)";
        state[h++] = Prefs.get("RoiManager3D-Options_SurfContact.boolean", false);
        label[h] = "Closest Object";
        state[h++] = Prefs.get("RoiManager3D-Options_Closest.boolean", true);

        // options        
        int splitDist = (int) Prefs.get("RoiManager3D-Options_splitDist.double", 10);
        double surfDist = (double) Prefs.get("RoiManager3D-Options_surfDist.double", 1.8);
        boolean useFloatSegment = Prefs.get("RoiManager3D-Options_Seg32.boolean", false);
        boolean excludeXY = Prefs.get("RoiManager3D-Options_ExcludeXY.boolean", false);
        boolean excludeZ = Prefs.get("RoiManager3D-Options_ExcludeZ.boolean", false);

        GenericDialog gd = new GenericDialog("RoiManager3D Set Measurements");
        gd.addMessage("Measurements :", Font.decode("dialog bold 14"));
        gd.addCheckboxGroup(8, 3, label, state);
        gd.addMessage("Options :", Font.decode("dialog bold 14"));
        gd.addCheckbox("Use 32-bits image for segmentation (nb objects > 65 535)", useFloatSegment);
        gd.addCheckbox("Exclude_objects_on_edges_XY", excludeXY);
        gd.addCheckbox("Exclude_objects_on_edges_Z", excludeZ);
        gd.addMessage("Split options : ");
        gd.addNumericField("Distance_between_centers (pixel)", splitDist, 0);
        gd.addMessage("Surf. contact options : ");
        gd.addNumericField("Distance_max_contact", surfDist, 2);

        gd.showDialog();

        if (gd.wasCanceled()) {
            return;
        }
        // analyse
        Prefs.set("RoiManager3D-Options_volume.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_surface.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_compacity.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_ellipse.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_invariants.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_convexhull.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_intDens.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_mean.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_stdDev.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_Mode.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_feret.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_min.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_max.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_centroid-pix.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_centroid-unit.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_dist2Surf.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_COM-pix.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_COM-unit.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_BB.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_RadDist.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_SurfContact.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_Closest.boolean", gd.getNextBoolean());
        // options
        Prefs.set("RoiManager3D-Options_Seg32.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_ExcludeXY.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_ExcludeZ.boolean", gd.getNextBoolean());
        Prefs.set("RoiManager3D-Options_splitDist.double", gd.getNextNumber());
        Prefs.set("RoiManager3D-Options_surfDist.double", gd.getNextNumber());

    }
}
