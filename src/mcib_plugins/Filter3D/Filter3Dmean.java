package mcib_plugins.Filter3D;

import ij.ImagePlus;
import ij.ImageStack;

/**
 * 3D mean
 *
 * @author Matthias Labschuetz
 */

public class Filter3Dmean extends Filter3Ddefault {
	
	public Filter3Dmean(ImageStack inPlus, ImageStack out, int r) {
		super(inPlus, out, r);
		
		initial_value = 0.0;
	}
	
	@Override
	protected  Voxel_value process_next_voxel(int x, int y, int z, Voxel_value prev_voxel, byte type) {
		Voxel_value out = new Voxel_value(prev_voxel.value, prev_voxel.voxel_count);
		Voxel_value to_add = iterate_and_add(x, y, z, type);
		Voxel_value to_subtract = iterate_and_subtract(x, y, z, type);
		
		out.value = out.value - to_subtract.value + to_add.value;
		out.voxel_count = out.voxel_count - to_subtract.voxel_count + to_add.voxel_count;
		
		return out;
	}
	
	@Override
	protected  double process_voxel(double temp_value, double operand) {
		//add values
		return (temp_value+operand);
	}
	
	@Override
	protected  double post_process_voxel(double temp_value, double operand) {
		//mean division
		if (operand != 0.0)
			return (temp_value/operand);
                        
		else
			return 0.0;
	}

}
