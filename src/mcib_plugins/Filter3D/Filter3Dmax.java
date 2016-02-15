package mcib_plugins.Filter3D;

import ij.ImagePlus;
import ij.ImageStack;

/**
 * 3D max
 *
 * @author Matthias Labschuetz
 */

public class Filter3Dmax extends Filter3Ddefault {

	/*
	 * can be optimized further (filter was written for mean)
	 * max filter requires additional lookups each time the maximum
	 * leaves the spherical area
	 */
	
	public Filter3Dmax(ImageStack inPlus, ImageStack out, int r) {
		super(inPlus, out, r);
		
		initial_value = 0.0;
	}
	
	@Override
	protected Voxel_value process_next_voxel(int x, int y, int z, Voxel_value prev_voxel, byte type) {
		Voxel_value out = new Voxel_value(prev_voxel.value, prev_voxel.voxel_count);
		//find largest value
		Voxel_value to_add = iterate_and_add(x, y, z, type);
		//find largest value
		Voxel_value to_subtract = iterate_and_subtract(x, y, z, type);
		
		//previous maximum in subtract
		if (out.value == to_subtract.value) {
			out.value = iterate_and_read(new int []{x, y, z}).value;
		}
		if (out.value < to_add.value) { //new maximum
			out.value = to_add.value;
		}
		
		//we don't care about the voxel count
		
		return out;
	}
	
	@Override
	protected double process_voxel(double temp_value, double operand) {		
		//output the larger value
		if (temp_value < operand) {
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
