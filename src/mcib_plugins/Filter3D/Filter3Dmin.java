package mcib_plugins.Filter3D;

import ij.ImagePlus;
import ij.ImageStack;

/**
 * 3D min
 *
 * @author Matthias Labschuetz
 */

public class Filter3Dmin extends Filter3Ddefault {

	/*
	 * can be optimized further (filter was written for mean)
	 * min filter requires additional lookups each time the minimum
	 * leaves the spherical area
	 */
	
	public Filter3Dmin(ImageStack inPlus, ImageStack out, int r) {
		super(inPlus, out, r);
		
		initial_value = Double.MAX_VALUE;
	}
	
	@Override
	protected Voxel_value process_next_voxel(int x, int y, int z, Voxel_value prev_voxel, byte type) {
		Voxel_value out = new Voxel_value(prev_voxel.value, prev_voxel.voxel_count);
		//find smallest value
		Voxel_value to_add = iterate_and_add(x, y, z, type);
		//find smallest value
		Voxel_value to_subtract = iterate_and_subtract(x, y, z, type);
		
		//previous minimum in subtract
		if (out.value == to_subtract.value) {
			out.value = iterate_and_read(new int []{x, y, z}).value;
		}
		if (out.value > to_add.value) { //new minimum
			out.value = to_add.value;
		}
		
		//we don't care about the voxel count
		
		return out;
	}
	
	@Override
	protected double process_voxel(double temp_value, double operand) {		
		//output the smaller value
		if (temp_value > operand) {
			return operand;
		} else {
			return temp_value;
		}
	}
	
	@Override
	protected double post_process_voxel(double temp_value, double operand) {		
		//simply pass
		return temp_value;
	}

}
