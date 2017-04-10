package mcib_plugins;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import mcib3d.geom.Object3D;
import mcib3d.geom.Object3DVoxels;
import mcib3d.geom.Objects3DPopulation;
import mcib3d.geom.RDAR;
import mcib3d.image3d.ImageHandler;
import mcib3d.image3d.ImageInt;
import mcib3d.image3d.ImageShort;

import java.util.ArrayList;

/**
 * Created by thomasb on 19/7/16.
 */
public class RDAR_ implements PlugInFilter {
    private ImagePlus imagePlus;
    private int minVolume = 100;

    @Override
    public int setup(String arg, ImagePlus imp) {
        imagePlus = imp;
        return DOES_8G + DOES_16;
    }

    @Override
    public void run(ImageProcessor ip) {
        if (dialog()) {
            ImageInt imageInt = ImageInt.wrap(imagePlus);
            Objects3DPopulation objects3DPopulation = new Objects3DPopulation(imageInt);
            // drawing
            ImageHandler drawIn = new ImageShort("Parts inside", imagePlus.getWidth(), imagePlus.getHeight(), imagePlus.getImageStackSize());
            drawIn.setScale(imageInt);
            ImageHandler drawOut = new ImageShort("Parts outside", imagePlus.getWidth(), imagePlus.getHeight(), imagePlus.getImageStackSize());
            drawOut.setScale(imageInt);
            ImageHandler drawEll = new ImageShort("Ellipsoid", imagePlus.getWidth(), imagePlus.getHeight(), imagePlus.getImageStackSize());
            drawEll.setScale(imageInt);
            for (Object3D object3D : objects3DPopulation.getObjectsList()) {
                processObject(object3D, drawIn, drawOut, drawEll);
            }
            drawIn.show();
            drawOut.show();
            drawEll.show();
        }
    }

    private void processObject(Object3D object3D, ImageHandler drawIn, ImageHandler drawOut, ImageHandler drawEll) {
        double resXY = object3D.getResXY();
        double resZ = object3D.getResZ();
        IJ.log("Analysing object " + object3D.getValue());
        Object3DVoxels object3DVoxels = object3D.getObject3DVoxels();

        RDAR rdar = new RDAR(object3D.getObject3DVoxels());
        IJ.log("Nb parts in : " + rdar.getPartsInNumber(minVolume) + ", nb parts out : " + rdar.getPartsOutNumber(minVolume));
        // volume
        ArrayList<Object3DVoxels> list = rdar.getPartsIn(minVolume);
        int vol = 0;
        if (list != null)
            for (Object3DVoxels object3DVoxels1 : list) vol += object3DVoxels.getVolumePixels();
        IJ.log("Volume In : " + vol);
        list = rdar.getPartsOut(minVolume);
        vol = 0;
        if (list != null)
            for (Object3DVoxels object3DVoxels1 : list) vol += object3DVoxels.getVolumePixels();
        IJ.log("Volume Out : " + vol);

        int color = object3DVoxels.getValue();
        rdar.getEllipsoid().draw(drawEll, color);
        if (rdar.getPartsIn(minVolume) != null)
            for (Object3DVoxels part : rdar.getPartsIn(minVolume)) part.draw(drawIn, color);
        color = object3DVoxels.getValue();
        if (rdar.getPartsOut(minVolume) != null)
            for (Object3DVoxels part : rdar.getPartsOut(minVolume)) part.draw(drawOut, color);
        drawIn.show("Parts_In");
        drawOut.show("Parts_Out");
        drawEll.show("Ellipsoid");
        IJ.log("");
    }

    private boolean dialog() {
        GenericDialog genericDialog = new GenericDialog("RDAR");
        genericDialog.addNumericField("Min volume parts", minVolume, 1, 10, "pix");
        genericDialog.showDialog();
        minVolume = (int) genericDialog.getNextNumber();

        return genericDialog.wasOKed();

    }
}