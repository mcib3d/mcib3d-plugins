package mcib_plugins;

import mcib3d.geom.ObjectCreator3D;
import ij.*;
import ij.plugin.*;


/**
 *  Description of the Class
 *
 * @author     thomas
 * @created    22 février 2008
 */
public class ObjectCreator_ implements PlugIn {

	/**
	 *  Main processing method for the ObjectCreator_ object
	 *
	 * @param  arg  Description of the Parameter
	 */
	public void run(String arg) {
		int sx = 256;
		int sy = 256;
		int sz = 256;
		// Cell
		ObjectCreator3D cell = new ObjectCreator3D(sx, sy, sz);
		cell.createEllipsoid(sx / 2, sy / 2, sz / 2, sx / 4, sy / 4, sz / 4, 255, false);
		new ImagePlus("Cell", cell.getStack()).show();
		// RFP
		ObjectCreator3D rfp = new ObjectCreator3D(sx, sy, sz);
		rfp.createEllipsoid(sx / 2 + sx / 16, sy / 2, sz / 2, 4, 4, 4, 200, false);
		new ImagePlus("RFP", rfp.getStack()).show();
		// GFP
		ObjectCreator3D gfp = new ObjectCreator3D(sx, sy, sz);
		// O deg
		gfp.createEllipsoid(sx / 2 + sx / 8, sy / 2, sz / 2, 8, 4, 4, 128, false);
		// 90
		gfp.createEllipsoid(sx / 2 + sx / 16, sy / 2 + sy / 8, sz / 2, 8, 4, 4, 128, false);
		// more than 90
		gfp.createEllipsoid(sx / 2 - sx / 16, sy / 2 + sy / 8, sz / 2, 8, 4, 4, 128, false);
		new ImagePlus("GFP", gfp.getStack()).show();
	}

}

