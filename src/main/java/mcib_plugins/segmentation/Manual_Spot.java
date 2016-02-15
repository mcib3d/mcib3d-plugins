package mcib_plugins.segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import mcib3d.image3d.processing.ManualSpot;

public class Manual_Spot implements PlugIn {
    
    @Override
    public void run(String arg) {
        int nbima = WindowManager.getImageCount();
        ImagePlus signalImage;
        ImagePlus labelImage;
        if (nbima == 0) {
            IJ.log("Image required :");
            return;
        } else if (nbima < 2) {
            signalImage = IJ.getImage();
            Calibration cal = signalImage.getCalibration();
            // create labels image
            labelImage = IJ.createImage("labels", signalImage.getWidth(), signalImage.getHeight(), signalImage.getStackSize(), 16);
            if (cal != null) {
                labelImage.setCalibration(cal);
            }
            labelImage.show();
        }
        nbima = WindowManager.getImageCount();
        String[] names = new String[nbima];
        int signal, label;
        for (int i = 0; i < nbima; i++) {
            names[i] = WindowManager.getImage(i + 1).getShortTitle();
        }
        signal = 0;
        label = 1;
        
        GenericDialog dia = new GenericDialog("Seeds spots");
        dia.addChoice("Spots_Signal", names, names[signal]);
        dia.addChoice("Spots_Label", names, names[label]);
        dia.showDialog();
        signal = dia.getNextChoiceIndex();
        label = dia.getNextChoiceIndex();
        
        signalImage = WindowManager.getImage(signal + 1);
        labelImage = WindowManager.getImage(label + 1);
        if (label < nbima) {
            labelImage = WindowManager.getImage(label + 1);
        }
        
        new ManualSpot(signalImage, labelImage).setVisible(true);
    }
}
