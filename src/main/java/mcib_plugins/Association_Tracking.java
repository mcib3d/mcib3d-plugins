package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.Concatenator;
import ij.plugin.Duplicator;
import ij.plugin.HyperStackConverter;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageHandler;
import mcib3d.tracking_dev.TrackingAssociation;

public class Association_Tracking implements PlugInFilter {
    ImagePlus plus;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        plus = imagePlus;

        return DOES_8G + DOES_16 + STACK_REQUIRED;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        int nFrames = plus.getNFrames();
        if (nFrames < 2) IJ.error("Multi-frame stack required");

        // should work with 4D hyperstacks, extract current frame
        int[] dims = plus.getDimensions();//XYCZT
        int channel = plus.getChannel();
        int frame = plus.getFrame();
        if ((plus.isHyperStack()) || (dims[2] > 1) || (dims[4] > 1)) {
            IJ.log("Hyperstack found, extracting current channel " + channel + " and frame " + frame);
            Duplicator duplicator = new Duplicator();
            // duplicate frames
            ImagePlus plus1 = duplicator.run(plus, channel, channel, 1, dims[3], frame, frame);
            ImagePlus plus2 = duplicator.run(plus, channel, channel, 1, dims[3], frame + 1, frame + 1);
            // ImageHandlers
            ImagePlus result = plus1.duplicate();
            ImageHandler img1 = ImageHandler.wrap(plus1);
            ImageHandler img2 = ImageHandler.wrap(plus2);
            // Association
            TrackingAssociation trackingAssociation = new TrackingAssociation(img1, img2);
            trackingAssociation.setMerge(false);
            for (int i = frame + 1; i <= nFrames; i++) {
                IJ.log("Processing " + i);
                ImageHandler tracked = trackingAssociation.getTrackedImage();
                result = Concatenator.run(result, tracked.getImagePlus());
                if (i < nFrames) {
                    trackingAssociation.setImage1(tracked);
                    ImagePlus plusTmp = duplicator.run(plus, channel, channel, 1, dims[3], i + 1, i + 1);
                    trackingAssociation.setImage2(ImageHandler.wrap(plusTmp));
                }
            }
            result = HyperStackConverter.toHyperStack(result, 1, img1.sizeZ, nFrames - frame + 1, "xyzct", "composite");
            result.show();
        }
    }
}
