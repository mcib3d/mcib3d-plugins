package mcib_plugins.segmentation;

import ij.IJ;
import ij.process.AutoThresholder;
import ij.util.ThreadUtil;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.Point3D;
import mcib3d.image3d.ImageByte;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.IterativeThresholding.TrackThreshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by thomasb on 22/8/16.
 */
public class SegNuclei {
    ImageInt rawImage;
    ImageInt watImage;
    Objects3DPopulation population = null;
    HashMap<Integer, Object3D> object3DHashMap;
    double margin = 0.25;

    public SegNuclei(ImageInt rawImage, ImageInt watImage) {
        this.rawImage = rawImage;
        this.watImage = watImage;
        setPopulation(new Objects3DPopulation(watImage));
    }

    public void setPopulation(Objects3DPopulation pop) {
        population = pop;
    }

    public ImageInt getSeg() {
        return process();
    }

    private ImageInt process() {
        TreeMap<Float, int[]> bounds = watImage.getBounds(false);
        final ImageInt[] watershedRegions = watImage.crop3D(bounds);
        final ImageInt[] rawRegions = rawImage.crop3D(bounds);
        final ImageByte[] thresholdedRegions = new ImageByte[rawRegions.length];
        final int nbRegions = watershedRegions.length;
        IJ.log("Nb regions " + rawRegions.length + " " + watershedRegions.length);

        // link population and watershed
        object3DHashMap = new HashMap<>(population.getNbObjects());
        for (Object3D object3D : population.getObjectsList()) {
            object3DHashMap.put((int) object3D.getPixModeValue(watImage), object3D);
        }

        final AtomicInteger ai = new AtomicInteger(0);

        final int nbCPUs = ThreadUtil.getNbCpus();
        final int dec = (int) Math.ceil((double) nbRegions / (double) nbCPUs);
        Thread[] threads = ThreadUtil.createThreadArray(nbCPUs);
        for (int iThread = 0; iThread < nbCPUs; iThread++) {
            threads[iThread] = new Thread() {
                @Override
                public void run() {
                    for (int i = ai.getAndIncrement(); i < nbCPUs; i = ai.getAndIncrement()) {
                        for (int r = i * dec; r < (i + 1) * dec; r++) {
                            if (r >= nbRegions) {
                                break;
                            }
                            ImageInt wa = watershedRegions[r];
                            ImageInt ra = rawRegions[r];
                            thresholdedRegions[r] = processRegion(wa, ra);
                            if (thresholdedRegions[r] == null) IJ.log("process null region " + r);
                        }
                    }
                }
            };
        }
        ThreadUtil.startAndJoin(threads);


        // copy offset to thresholdedRegions
        for (int i = 0; i < nbRegions; i++) {
            if (rawRegions[i] == null) {
                IJ.log("raw " + i + " " + null);
            }
            if (thresholdedRegions[i] == null) {
                IJ.log("thres " + i + " " + null);
            }
            if ((rawRegions[i] != null) && (thresholdedRegions[i] != null))
                thresholdedRegions[i].setOffset(rawRegions[i]);
            if (rawRegions[i] == null) {
                IJ.log("raw " + i + " " + null);
            }
            if (thresholdedRegions[i] == null) {
                IJ.log("thres " + i + " " + null);
            }
        }

        ImageInt mergedImage = ImageShort.merge3DBinary(thresholdedRegions, watImage.sizeX, watImage.sizeY, watImage.sizeZ);

        Objects3DPopulation population = new Objects3DPopulation(mergedImage);

        int max = (int) mergedImage.getMax() + 1;
        for (Object3D object3D : population.getObjectsList()) {
            if (!((Object3DVoxels) (object3D)).isConnex()) {
                IJ.log("not connex " + object3D);
                ArrayList<Object3DVoxels> conn = ((Object3DVoxels) (object3D)).getConnexComponents();
                for (int i = 1; i < conn.size(); i++) {
                    conn.get(i).draw(mergedImage, max);
                    max++;
                }
            }
        }

        return mergedImage;
    }


    private ImageByte processRegion(ImageInt watershed, ImageInt raw) {
        ImageByte thresholded;
        AutoThresholder thresholder = new AutoThresholder();
        int value = (int) watershed.getPixel(watershed.sizeX / 2, watershed.sizeY / 2, watershed.sizeZ / 2);
        ImageByte mask = watershed.thresholdRangeInclusive(value, value);
        ImageInt ma = null;
        int[] hist = raw.getHistogram(mask, 256, raw.getMin(), raw.getMax());
        double threshold8bits = thresholder.getThreshold(AutoThresholder.Method.Triangle, hist);
        double step = (raw.getMax() - raw.getMin() + 1) / 256.0;
        thresholded = raw.thresholdAboveExclusive((float) (threshold8bits * step + raw.getMin()));
        thresholded.intersectMask((ImageHandler) mask);

        Object3DVoxels O = new Object3DVoxels(thresholded);
        if (!O.isConnex()) {
            ArrayList<Object3DVoxels> conn = O.getConnexComponents();
            int max = 0;
            int maxi = 0;
            for (int o = 0; o < conn.size(); o++) {
                if (conn.get(o).getVolumePixels() > max) {
                    max = conn.get(o).getVolumePixels();
                    maxi = o;
                }
            }
            for (int o = 0; o < conn.size(); o++) {
                if (o != maxi) {
                    conn.get(o).draw(thresholded, 0);
                }
            }
        }
        return thresholded;
    }

    private ImageByte processRegionIT(int region, ImageInt watershed, ImageInt raw, int volMin, int volMax, int thMin, int contMin) {
        int value = (int) watershed.getPixel(watershed.sizeX / 2, watershed.sizeY / 2, watershed.sizeZ / 2);
        ImageHandler mask = watershed.thresholdRangeInclusive(value, value);
        raw.intersectMask(mask);

        if ((region == -1) || (region == -2)) {
            watershed.show("wat" + value);
            raw.show("raw" + value);
            IJ.log("value " + value + " " + volMin + " " + volMax + " " + thMin);
            IJ.log("object " + object3DHashMap.get(value));
        }

        Object3D object3D = object3DHashMap.get(value);
        if (object3D == null) {
            IJ.log("No object " + region + " " + value);
            return new ImageByte("", raw.sizeX, raw.sizeY, raw.sizeZ);
        }
        volMin = (int) (object3D.getVolumePixels() * (1.0 - 2 * margin)); // mitosis --> 50 % volume
        volMax = (int) (object3D.getVolumePixels() * (1.0 + margin));

        thMin = (int) (object3D.getType() * (1.0 - margin));


        TrackThreshold trackThreshold = new TrackThreshold(volMin, volMax, contMin, 1, 1, thMin);
        // draw object as marker
        ImageByte markerObject = new ImageByte("marker", watershed.sizeX, watershed.sizeY, watershed.sizeZ);
        object3D.drawAt(markerObject, 1, new Point3D(object3D.getCenterX() - raw.offsetX, object3D.getCenterY() - raw.offsetY, object3D.getCenterZ() - raw.offsetZ));
        trackThreshold.setImageMarkers(markerObject);

        trackThreshold.setImageZones(watershed);
        trackThreshold.setMethodThreshold(TrackThreshold.THRESHOLD_METHOD_STEP);
        trackThreshold.setCriteriaMethod(TrackThreshold.CRITERIA_METHOD_MSER);

        ImageHandler thresholded = null;
        thresholded = trackThreshold.segment(raw, false);
        // no objects found
        if (thresholded != null) {
            thresholded.intersectMask(mask);
        } else {
            return new ImageByte("", raw.sizeX, raw.sizeY, raw.sizeZ);
            /*
            // try mitosis
            volMin = (int) (object3D.getVolumePixels() * (0.5));
            volMax = (int) (object3D.getVolumePixels() * (1.0));
            thMin = (int) (object3D.getType() * (1.0));
            trackThreshold = new TrackThreshold(volMin, volMax, contMin, 1, 1, thMin);
            trackThreshold.setImageMarkers(null);
            trackThreshold.setImageZones(watershed);
            trackThreshold.setMethodThreshold(TrackThreshold.THRESHOLD_METHOD_STEP);
            trackThreshold.setCriteriaMethod(TrackThreshold.CRITERIA_METHOD_MSER);
            thresholded = trackThreshold.segment(raw, false);
            if (thresholded != null) {
                thresholded.intersectMask(mask);
            } else
                return new ImageByte("", raw.sizeX, raw.sizeY, raw.sizeZ);
                */
        }

        //TEST
        //if ((region == -1) || (region == -2)) {
        //    watershed.show("wat" + value);
        //    raw.show("raw" + value);
        //}

        ImageByte bin = thresholded.thresholdAboveExclusive(0);

        // TEST
        // if ((region == -1) || (region == -2)) {
        //     thresholded.duplicate().show("thD" + value);
        //     bin.show("bin" + value);
        // }

        return bin;
    }
}
