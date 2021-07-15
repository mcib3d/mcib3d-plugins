/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package mcib_plugins.analysis;

import ij.IJ;
import ij.ImagePlus;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import ij.plugin.Duplicator;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom2.Objects3DIntPopulation;
import mcib3d.geom2.measurements.*;
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
public class SimpleMeasure {
   Objects3DIntPopulation population;
    ImagePlus input;

    public SimpleMeasure(ImagePlus in) {
        input = in;
        population = new Objects3DIntPopulation(ImageHandler.wrap(input));
    }

    public List<Double[]> getMeasuresCentroid() {
        try {
            return population.getMeasurements(MeasureCentroid.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public List<Double[]> getMeasuresVolume() {
        List<Double[]> results = new LinkedList<>();
        try {
            return population.getMeasurements(MeasureVolume.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Double[]> getMeasuresSurface() {
        List<Double[]> results = new LinkedList<>();
        try {
            return population.getMeasurements(MeasureSurface.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return results;
    }



    public List<Double[]> getMeasuresStats(ImageHandler raw) {
        return population.getMeasurementsIntensity(raw);
    }

    public List<Double[]> getMeasuresStats(ImagePlus myPlus) {
        return getMeasuresStats(ImageHandler.wrap(myPlus));
    }

    public List<Double[]> getMeasuresCompactness() {
        List<Double[]> results = new LinkedList<>();
        try {
            results.addAll(population.getMeasurements(MeasureCompactness.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Double[]> getMeasuresEllipsoid() {
        List<Double[]> results = new LinkedList<>();
        try {
            results.addAll(population.getMeasurements(MeasureEllipsoid.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Double[]> getMeasuresDistanceCentreContour() {
        List<Double[]> results = new LinkedList<>();
        try {
            results.addAll(population.getMeasurements(MeasureDistancesCenter.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return results;
    }

    public List<Double[]> getMeasuresFeret() {
        List<Double[]> results = new LinkedList<>();
        try {
            results.addAll(population.getMeasurements(MeasureFeret.class));
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return results;
    }


    @Deprecated
    public List<Double[]> getMeshSurfaces() {
        Objects3DPopulation pop = new Objects3DPopulation(ImageHandler.wrap(input));

        return pop.getMeasuresMesh();
    }

    public static ImagePlus extractCurrentStack(ImagePlus plus) {
        // check dimensions
        int[] dims = plus.getDimensions();//XYCZT
        int channel = plus.getChannel();
        int frame = plus.getFrame();
        ImagePlus stack;
        // crop actual frame
        if ((dims[2] > 1) || (dims[4] > 1)) {
            IJ.log("Hyperstack found, extracting current channel " + channel + " and frame " + frame);
            Duplicator duplicator = new Duplicator();
            stack = duplicator.run(plus, channel, channel, 1, dims[3], frame, frame);
        } else stack = plus.duplicate();

        return stack;
    }
}
