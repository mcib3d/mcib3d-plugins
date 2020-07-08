package mcib_plugins;

import ij.IJ;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import mcib3d.image3d.ImageInt;
import mcib3d.tracking_dev.TrackingAssociation;

public class Association_ implements PlugIn {
    private int idxA;
    private int idxB;

    @Override
    public void run(String s) {
        if (!dialog()) return;

        ImageInt img1 = ImageInt.wrap(WindowManager.getImage(idxA + 1));
        ImageInt img2 = ImageInt.wrap(WindowManager.getImage(idxB + 1));

        TrackingAssociation trackingAssociation = new TrackingAssociation(img1, img2);
        trackingAssociation.setMerge(false);
        trackingAssociation.getTracked().show(img1.getTitle() + "-association");
    }

    private boolean dialog() {
        int nbima = WindowManager.getImageCount();
        if (nbima < 2) {
            IJ.error("Needs at least two labelled images");
            return false;
        }
        idxA = 0;
        idxB = nbima > 1 ? 1 : 0;
        String[] namesA = new String[nbima];
        String[] namesB = new String[nbima];
        for (int i = 0; i < nbima; i++) {
            namesA[i] = WindowManager.getImage(i + 1).getShortTitle();
            namesB[i] = WindowManager.getImage(i + 1).getShortTitle();
        }

        GenericDialog dia = new GenericDialog("All Distances");
        dia.addChoice("Image_A", namesA, namesA[idxA]);
        dia.addChoice("Image_B", namesB, namesB[idxB]);
        dia.showDialog();
        if (dia.wasOKed()) {
            idxA = dia.getNextChoiceIndex();
            idxB = dia.getNextChoiceIndex();
        }

        return dia.wasOKed();
    }
}
