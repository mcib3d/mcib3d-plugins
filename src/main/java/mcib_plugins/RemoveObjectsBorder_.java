/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins;

import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageFloat;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageShort;

/**
 *
 * @author thomasb
 */
public class RemoveObjectsBorder_ implements PlugInFilter {

    ImagePlus plus;

    @Override
    public int setup(String string, ImagePlus ip) {
        plus = ip;

        return DOES_16 + DOES_32 + DOES_8G;
    }

    @Override
    public void run(ImageProcessor ip) {

        GenericDialog gd = new GenericDialog("Exclude borders");
        gd.addMessage("Remove objects on X-Y borders");
        gd.addCheckbox("Remove objects on Z border ?", false);
        gd.showDialog();
        if (gd.wasCanceled()) {
            return;
        }
        boolean Z = gd.getNextBoolean();
        
        Objects3DPopulation pop = new Objects3DPopulation(plus);

        pop.removeObjectsTouchingBorders(plus, Z);

        ImageHandler objs;

        if (plus.getBitDepth() > 16) {
            objs = new ImageFloat("Objects_removed", plus.getWidth(), plus.getHeight(), plus.getNSlices());
        } else {
            objs = new ImageShort("Objects_removed", plus.getWidth(), plus.getHeight(), plus.getNSlices());
        }

        pop.draw(objs);

        ImagePlus rem = objs.getImagePlus();
        rem.setCalibration(plus.getCalibration());
        rem.show();

    }

}
