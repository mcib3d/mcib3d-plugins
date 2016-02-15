/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.tools;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.ObjectCreator3D;
import mcib3d.geom.Objects3DPopulation;

/**
 *
 **
 * /**
 * Copyright (C) 2008- 2012 Thomas Boudier and others
 *
 *
 *
 * This file is part of mcib3d
 *
 * mcib3d is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author thomas
 */
public class Draw_RoiSlices implements PlugIn {
    
    public void run(String arg) {
        ImagePlus segPlus = WindowManager.getCurrentImage();
        Objects3DPopulation pop = new Objects3DPopulation(segPlus);
        
        ObjectCreator3D draw = new ObjectCreator3D(segPlus.getWidth(), segPlus.getHeight(), segPlus.getNSlices());
        
        for (int o = 0; o < pop.getNbObjects(); o++) {
            Object3DVoxels obj = (Object3DVoxels) pop.getObject(o);
            obj.computeContours();
            obj.drawContours(draw, (o + 1));
        }
        
        draw.getImageHandler().show("draw");
    }
}

