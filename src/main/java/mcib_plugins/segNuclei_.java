package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.Duplicator;
import ij.plugin.ZProjector;
import ij.plugin.filter.Binary;
import ij.plugin.filter.EDM;
import ij.plugin.filter.ParticleAnalyzer;
import ij.plugin.filter.PlugInFilter;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;
import mcib3d.image3d.ImageStats;
import mcib_plugins.segmentation.SegNuclei;

public class segNuclei_ implements PlugInFilter {
    private int method = 0;

    private static String[] methods = AutoThresholder.getMethods();
    ImagePlus myPlus;
    private boolean separate = true;
    private float manual = 0;

    @Override
    public int setup(String s, ImagePlus imagePlus) {
        myPlus = imagePlus;
        return PlugInFilter.DOES_16 + PlugInFilter.DOES_8G;
    }

    @Override
    public void run(ImageProcessor imageProcessor) {
        if (dialog()) {
            ImageHandler imageHandler = ImageHandler.wrap(myPlus);
            ImageHandler seg = segment2D(imageHandler);
            ImagePlus segPlus = seg.getImagePlus();
            segPlus.setCalibration(myPlus.getCalibration());
            segPlus.setTitle(myPlus.getTitle() + "_segNuclei");
            segPlus.setSlice(segPlus.getNSlices() / 2);
            segPlus.getProcessor().setMinAndMax(0, seg.getMax());
            seg.getImagePlus().show();
        }
    }

    private boolean dialog() {
        GenericDialog dialog = new GenericDialog("Nuclei Segmentation");
        dialog.addMessage("3D segmentation of fluorescent nuclei for cell cultures.");
        dialog.addChoice("Auto_Threshold", methods, methods[0]);
        dialog.addNumericField("Manual threshold (0=auto)", 0, 0, 6, null);
        dialog.addCheckbox("Separate_nuclei", separate);
        dialog.showDialog();
        method = dialog.getNextChoiceIndex();
        manual = (float) dialog.getNextNumber();
        separate = dialog.getNextBoolean();

        return dialog.wasOKed();
    }

    private float getThtreshold(ImagePlus plus) {
        if (manual > 0) return manual;
        // compute histogram
        ImageHandler imageHandler = ImageHandler.wrap(plus);
        ImageStats stat = imageHandler.getImageStats(null);
        int[] histo = stat.getHisto256();
        double binSize = stat.getHisto256BinSize();
        double min = stat.getMin();

        AutoThresholder at = new AutoThresholder();
        double thld = at.getThreshold(methods[method], histo);
        float threshold;
        if (plus.getBitDepth() > 8)
            threshold = (float) (thld * binSize + min);
        else
            threshold = (float) thld;
        IJ.log(methods[method] + " threshold (2D) :" + threshold);

        return threshold;
    }

    private ImageHandler segment2D(ImageHandler input) {
        // do projection
        ZProjector zProjector = new ZProjector();
        zProjector.setMethod(ZProjector.MAX_METHOD);
        zProjector.setStartSlice(1);
        zProjector.setStopSlice(input.sizeZ);
        zProjector.setImage(input.getImagePlus());
        zProjector.doProjection();
        ImagePlus plus = zProjector.getProjection();

        // threshold
        float threshold = getThtreshold(plus);
        plus.getProcessor().threshold((int) threshold);
        Duplicator duplicator = new Duplicator();
        ImagePlus bin2 = duplicator.run(plus);
        ByteProcessor byteProcessor = bin2.getProcessor().convertToByteProcessor();
        bin2.setProcessor(byteProcessor);

        // fill holes
        ij.plugin.filter.Binary binary = new Binary();
        binary.setup("fill", bin2);
        binary.run(bin2.getProcessor());
        // watershed IJ = separate
        if (separate) {
            ij.plugin.filter.EDM edm = new EDM();
            edm.setup("watershed", bin2);
            edm.toWatershed(bin2.getProcessor());
        }
        // count mask
        ParticleAnalyzer particleAnalyzer = new ParticleAnalyzer(ParticleAnalyzer.SHOW_ROI_MASKS, ParticleAnalyzer.AREA, null, 10, 1000000, 0, 1);
        particleAnalyzer.setHideOutputImage(true);
        particleAnalyzer.analyze(bin2);
        ImagePlus seg2D = particleAnalyzer.getOutputImage();
        ImageInt seg3D = expand3D(ImageInt.wrap(seg2D), input.sizeZ);
        // perform deep segmentation
        SegNuclei deepSeg = new SegNuclei((ImageInt) input, seg3D);
        // result
        ImageInt result = deepSeg.getSeg();

        return result;
    }

    private ImageInt expand3D(ImageInt imageInt, int nbZ) {
        ImageInt expand = new ImageShort("expand", imageInt.sizeX, imageInt.sizeY, nbZ);
        for (int z = 0; z < nbZ; z++)
            expand.insert(imageInt, 0, 0, z, false);

        return expand;
    }
}
