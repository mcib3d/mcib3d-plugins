/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.analysis;

import ij.ImagePlus;
import java.util.ArrayList;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.image3d.ImageHandler;

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
public class simpleMeasure {

    Objects3DPopulation pop;
    ImagePlus input;

    public simpleMeasure(ImagePlus in) {
        input = in;
        pop = new Objects3DPopulation();
        pop.addImage(input);
    }

    public ArrayList<double[]> getMeasuresCentroid() {
        return pop.getMeasureCentroid();
    }

    public ArrayList<double[]> getMeasuresBase() {
        return pop.getMeasuresGeometrical();
    }

    public ArrayList<double[]> getMeasuresStats(ImageHandler raw) {
        return pop.getMeasuresStats(raw);
    }

    public ArrayList<double[]> getMeasuresStats(ImagePlus myPlus) {
        return getMeasuresStats(ImageHandler.wrap(myPlus));
    }

    public ArrayList<double[]> getMeasuresShape() {
        return pop.getMeasuresShape();
    }

    public ArrayList<double[]> getMeshSurfaces() {
        return pop.getMeasuresMesh();
    }
}
