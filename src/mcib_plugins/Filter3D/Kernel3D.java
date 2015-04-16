package mcib_plugins.Filter3D;

import java.util.Arrays;

/**
 * Kernel class to define:
 * - a filter kernel (e.g. sphere)
 * - difference kernels in x, y, z direction
 *   (e.g. sphere subtracted from the neighbor sphere)
 *
 * @author Matthias Labschuetz
 */

public class Kernel3D {

	public static final byte LEFT 	= 0;
	public static final byte RIGHT 	= 1;
	public static final byte TOP 	= 2;
	public static final byte BOTTOM = 3;
	public static final byte FRONT 	= 4;
	public static final byte BACK 	= 5;
	
	private int[][][] kernel;
	public int filter_size;
	
	private int radius;
	
	/*
	 * offset of the voxel to the center in a 2d array 
	 * 
	 * 0 0 1 0 0
	 * 0 1 0 0 0
	 * 1 0 0 0 0
	 * 0 1 0 0 0
	 * 0 0 1 0 0
	 * 
	 * this is: 0, -1, -2, -1, 0
	 */
	public int[][] difference_kernel_offset_matrix;
	
	/*
	 * mask_array
	 * e.g.
	 * 
	 * 0 1 0
	 * 1 1 1
	 * 0 1 0
	 * 
	 * is: 0, 1, 0
	 */
	public int[] filter_mask_array;
	
	public Kernel3D(int radius) {
		this.radius = radius;
		filter_size = 2*radius+1;
		
		fill(0); //default fill with zeros
		fillMatrix(radius+1); //fill with a (temporary) value that will never be set
		
		//show_kernel(0); //examplary debug output
	}
	
	/*
	 * -radius...0...radius
	 */
	public boolean getValueAt(int filter_x, int filter_y, int filter_z) {
		if (kernel[filter_x+radius][filter_y+radius][filter_z+radius] == 1) return true;
		else return false;
	}

	/*
	 * initialize kernel with a specified value
	 */
	public void fill(int value) {
		kernel = new int[filter_size][filter_size][filter_size];
		
		//initialize with value
		for (int k=0; k<filter_size; k++) { //no arrays fill for 2d or 3d
			for (int j=0; j<filter_size; j++) {
				for (int i=0; i<filter_size; i++) {
					kernel[i][j][k] = value;
				}
			}
		}
	}
	
	/*
	 * initialize matrix with a specified value
	 */
	public void fillMatrix(int value) {
		difference_kernel_offset_matrix = new int[filter_size][filter_size];
		
		//initialize with value
		for (int j=0; j<filter_size; j++) {
			for (int i=0; i<filter_size; i++) {
				difference_kernel_offset_matrix[i][j] = value;
			}
		}
	}
	
	/*
	 * create sphere kernel
	 * creates a spherical unnormalized mean filter kernel
	 * 
	 * outer most pixel/voxel is actually at +-(radius-0.5)
	 * we are interested in pixel/voxel centers
	 */
	public void setSpherical() {
		
		for (int k=0; k<filter_size; k++) {
			for (int j=0; j<filter_size; j++) {
				for (int i=0; i<filter_size; i++) {
					if (Math.pow((((float)i+0.5 - ((float)radius+0.5))),2.0) + 
						Math.pow((((float)j+0.5 - ((float)radius+0.5))),2.0) +
						Math.pow((((float)k+0.5 - ((float)radius+0.5))),2.0) <= Math.pow(((float)radius),2.0)) {
						
						kernel[i][j][k] = 1;
					}
				}
			}
		}
		
		//debug_show_kernel(0); //debug
		//debug_show_kernel(1); //debug
		//debug_show_kernel(2); //debug
		
	}
	
	/*
	 * creates the difference kernels for adjacent voxels
	 * S1,S2 ... spherical sets
	 * diff_left = S1\S2 and diff_right = S2\S1
	 */
	public void setDifferenceKernel(int direction) {
		
		//create temporary spherical kernel for calculation
		Kernel3D temp_sphere = new Kernel3D(radius);
		temp_sphere.setSpherical();
		
		switch(direction) {
			case LEFT:
				//create from left (x-axis) negative?
				for (int k=0; k<filter_size; k++) {
					for (int j=0; j<filter_size; j++) {
						for (int i=0; i<filter_size; i++) {
							if (temp_sphere.kernel[i][j][k] == 1) {
								kernel[i][j][k] = 1;
								difference_kernel_offset_matrix[j][k] = i-radius;
								//break this loop, 
								//left most sphere boundary voxel was set to 1
								break;	
							}
						}
					}
				}
				break;
				
			case RIGHT:
				//create from right (x-axis)
				for (int k=0; k<filter_size; k++) {
					for (int j=0; j<filter_size; j++) {
						for (int i=(filter_size-1); i>=0; i--) {
							if (temp_sphere.kernel[i][j][k] == 1) {
								kernel[i][j][k] = 1;
								difference_kernel_offset_matrix[j][k] = i-radius;
								break;
							}
						}
					}
				}
				break;
				
			case TOP:
				//create from top (y-axis)
				for (int k=0; k<filter_size; k++) {
					for (int i=0; i<filter_size; i++) {
						for (int j=0; j<filter_size; j++) { //inner loop 
							if (temp_sphere.kernel[i][j][k] == 1) {
								kernel[i][j][k] = 1;
								difference_kernel_offset_matrix[i][k] = j-radius;
								break;	
							}
						}
					}
				}
				break;
				
			case BOTTOM:
				//create from bottom (y-axis)
				for (int k=0; k<filter_size; k++) {
					for (int i=0; i<filter_size; i++) {
						for (int j=(filter_size-1); j>=0; j--) { //inner loop is y
							if (temp_sphere.kernel[i][j][k] == 1) {
								kernel[i][j][k] = 1;
								difference_kernel_offset_matrix[i][k] = j-radius;
								break;	
							}
						}
					}
				}
				break;
				
			case FRONT:
				//create from front (z-axis)
				for (int j=0; j<filter_size; j++) {
					for (int i=0; i<filter_size; i++) {
						for (int k=0; k<filter_size; k++) { //z inner loop
							if (temp_sphere.kernel[i][j][k] == 1) {
								kernel[i][j][k] = 1;
								difference_kernel_offset_matrix[i][j] = k-radius;
								break;	
							}
						}
					}
				}
				break;
				
			case BACK:
				//create from back (z-axis)
				for (int j=0; j<filter_size; j++) {
					for (int i=0; i<filter_size; i++) {
						for (int k=(filter_size-1); k>=0; k--) { //z inner loop
							if (temp_sphere.kernel[i][j][k] == 1) {
								kernel[i][j][k] = 1;
								difference_kernel_offset_matrix[i][j] = k-radius; //ok
								break;	
							}
						}
					}
				}
				break;
				
		}
		
		//empty the mask array and fill it
		filter_mask_array = new int[filter_size];
		Arrays.fill(filter_mask_array, -1);
		
		for (int j=0; j<filter_size; j++) {
			for (int i=0; i<filter_size; i++) {
				//if value was set
				if (difference_kernel_offset_matrix[i][j] != radius+1) {
					filter_mask_array[j] = radius-i;
					break;
				}
			}
		}
		
		//debug_show_filter_mask_array();
		
		/*
		show_offset_matrix();
		*/
		
	}
	
	/*
	 * debug
	 */
	public void debug_show_kernel(int depth) {
		depth = depth%filter_size;
		
		System.out.println("Kernel at row "+depth);
		
		for (int j=0; j<filter_size; j++) {
			for (int i=0; i<filter_size; i++) {
				System.out.print(kernel[i][j][depth] + " ");
			}
			System.out.println("");
		}
	}
	
	/*
	 * debug
	 */
	public void debug_show_offset_matrix() {
		System.out.println("Offset matrix:");
		
		for (int j=0; j<filter_size; j++) {
			for (int i=0; i<filter_size; i++) {
				System.out.print(difference_kernel_offset_matrix[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	/*
	 * debug
	 */
	public void debug_show_filter_mask_array() {
		System.out.println("Filter mask array:");
		
		for (int i=0; i<filter_size; i++) {
			System.out.print(filter_mask_array[i] + " ");
		}
		System.out.println("");
	}
	
}
