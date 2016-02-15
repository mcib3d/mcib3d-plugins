package mcib_plugins.Filter3D;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.*;
import ij.process.ImageProcessor;

/**
 * default filter class walks through the image in 'snakewise' order and uses
 * previous values for calculating the succeeding ones
 *
 * note: this was written for the mean filter and adapted for other filters a
 * lot of code and comments have been kept to improve understanding
 *
 * the calculate_border_xx methods are very similar, they could have been
 * combined but this would have made the code even more difficult to read rl vs
 * rl use a different check against the borders rl vs tb vs fb 'walk' along
 * different axes
 *
 * @author Matthias Labschuetz
 */
public class Filter3Ddefault {

    /*
     * 'struct' that saves an integer and a float
     */
    protected class Voxel_value {

        Voxel_value(double v, int v_c) {
            value = v;
            voxel_count = v_c;
        }
        public double value; // values of pixel 
        public int voxel_count;// number of pixels in neighborhood
    }
    private int radius;
    private Kernel3D filter_kernel;
    //filter kernels for marking the voxels that differ in calculating the succeeding voxel
    private Kernel3D filter_kernel_left;
    private Kernel3D filter_kernel_right;
    private Kernel3D filter_kernel_top;
    private Kernel3D filter_kernel_bottom;
    private Kernel3D filter_kernel_front;
    private Kernel3D filter_kernel_back;
    private int[] image_size = {100, 100, 100};
    private ImageStack in_image;
    private ImageStack out_image;
    protected double initial_value = 0.0;

    /*
     * Initial remarks:
     *
     * Data-type: the images can be byte, the intermediate results need to be
     * something bigger
     *
     * e.g. images byte, calculation float (since division is invluded)
     */
    public Filter3Ddefault(ImageStack in, ImageStack out, int r) {
        radius = r; //set radius
        this.out_image = out;//image
        this.in_image = in;

        //get image dimensions
        image_size[0] = in_image.getWidth();
        image_size[1] = in_image.getHeight();
        image_size[2] = in_image.getSize();
    }

    /*
     * 'main' of filter
     */
    public void filter() {
        //create spherical filter
        filter_kernel = new Kernel3D(radius);
        filter_kernel.setSpherical();

        //create spherical difference filters 
        //(difference of spheres for neighbor voxels)
        filter_kernel_left = new Kernel3D(radius);
        filter_kernel_left.setDifferenceKernel(Kernel3D.LEFT);

        filter_kernel_right = new Kernel3D(radius);
        filter_kernel_right.setDifferenceKernel(Kernel3D.RIGHT);

        filter_kernel_top = new Kernel3D(radius);
        filter_kernel_top.setDifferenceKernel(Kernel3D.TOP);

        filter_kernel_bottom = new Kernel3D(radius);
        filter_kernel_bottom.setDifferenceKernel(Kernel3D.BOTTOM);

        filter_kernel_front = new Kernel3D(radius);
        filter_kernel_front.setDifferenceKernel(Kernel3D.FRONT);

        filter_kernel_back = new Kernel3D(radius);
        filter_kernel_back.setDifferenceKernel(Kernel3D.BACK);

        //filter image routine
        //steps through every voxel using neighbor information

        //calculate first voxel (left/top/front = 0/0/0)
        final Voxel_value first = iterate_and_read(new int[]{0, 0, 0});
        //division by number of voxels in post_process
        out_image.setVoxel(0, 0, 0, post_process_voxel(first.value, (double) first.voxel_count));

        /*
         * has to: process from: left to right, top to bottom, right to left,
         * bottom to top, front to back
         */



        /*
         * outer loop: through slices front to back
         *
         * note: this is probably too much code but less code would make it even
         * harder to understand
         */
        // PARALLEL (Thomas Boudier) !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // PB WITH PARALLEL (if nbsilces <= nbcpus !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        /*
         * final AtomicInteger ai = new AtomicInteger(zmin); final int zm=zmax;
         * final ImageStack output=out_image; int n_cpus =
         * Runtime.getRuntime().availableProcessors(); n_cpus=1; Thread[]
         * threads = new Thread[n_cpus];
         *
         * for (int ithread = 0; ithread < threads.length; ithread++) {
         *
         * threads[ithread] = new Thread() {
         *
         * @Override public void run() {
         *
         */
        Voxel_value the_voxel = new Voxel_value(initial_value, 0);
        Voxel_value previous_voxel = first;
        double val;
        boolean do_first = true;

        for (int k = 0; k < image_size[2]; k++) {
            IJ.log("processing z " + k);
            boolean move_slice_back = true; //mark first voxel in new slice
            ImageProcessor ima_out = out_image.getProcessor(k + 1);
            if (k % 2 == 0) {
                /*
                 * middle loop: through every 2d image (in 'snakewise' order)
                 * (there are 2 middle loops for top-bottom and bottom-top)
                 */
                for (int j = 0; j < image_size[1]; j++) {
                    boolean move_row_down = true; //mark first voxel in new row
                    if (j % 2 == 0) {
                        /*
                         * left->right (x-axis)
                         */
                        for (int i = 0; i < image_size[0]; i++) {
                            if (move_slice_back) {
                                if (do_first) {
                                    the_voxel = previous_voxel;
                                    do_first = false;
                                } else {
                                    the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.FRONT);
                                }
                                move_slice_back = false;
                                move_row_down = false;
                            } else if (move_row_down) {
                                //top bottom
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.TOP);

                                move_row_down = false;
                            } else {
                                //left right
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.LEFT);
                            }
                            //division (for mean)

                            val = post_process_voxel(the_voxel.value, (double) the_voxel.voxel_count);
                            ima_out.putPixelValue(i, j, val);
                            //output.setVoxel(i, j, k, val);
                            previous_voxel = the_voxel; //save this as previous
                        }
                    } else {
                        /*
                         * right->left
                         */
                        for (int i = image_size[0] - 1; i >= 0; i--) {

                            if (move_slice_back) {
                                //never reached normally? (depends on image dimensions)
                                //front back
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.FRONT);

                                move_slice_back = false;
                                move_row_down = false;
                            } else if (move_row_down) {
                                //top bottom
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.TOP);

                                move_row_down = false;
                            } else {
                                //right left
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.RIGHT);
                            }

                            //division (for mean)

                            val = post_process_voxel(the_voxel.value, (double) the_voxel.voxel_count);
                            ima_out.putPixelValue(i, j, val);
                            //output.setVoxel(i, j, k, val);
                            previous_voxel = the_voxel; //save this as previous
                        }
                    }
                }
            } else {
                /*
                 * middle loop: through every 2d image (in 'snakewise' order)
                 * backwards := bottom to top
                 */
                for (int j = image_size[1] - 1; j >= 0; j--) {
                    boolean move_row_up = true;
                    if (j % 2 == 1) { //has to be inverted!
						/*
                         * left->right (x-axis)
                         */
                        for (int i = 0; i < image_size[0]; i++) {

                            if (move_slice_back) {
                                //front back
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.FRONT);

                                move_slice_back = false;
                                move_row_up = false;
                            } else if (move_row_up) {
                                //bottom up
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.BOTTOM);

                                move_row_up = false;
                            } else {
                                //left right
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.LEFT);
                            }

                            //division (for mean)
                            val = post_process_voxel(the_voxel.value, (double) the_voxel.voxel_count);
                            ima_out.putPixelValue(i, j, val);
                            //output.setVoxel(i, j, k, val);
                            previous_voxel = the_voxel; //save this as previous

                        }
                    } else {
                        /*
                         * right->left
                         */
                        for (int i = image_size[0] - 1; i >= 0; i--) {

                            if (move_slice_back) {
                                //System.out.println("down: " + k + "/x: " + i); //DEBUG
                                //front back
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.FRONT);

                                move_slice_back = false;
                                move_row_up = false;
                            } else if (move_row_up) {
                                //bottom up
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.BOTTOM);

                                move_row_up = false;
                            } else {
                                //right left
                                the_voxel = process_next_voxel(i, j, k, previous_voxel, Kernel3D.RIGHT);
                            }

                            //division (for mean)
                            val = post_process_voxel(the_voxel.value, (double) the_voxel.voxel_count);
                            ima_out.putPixelValue(i, j, val);
                            //output.setVoxel(i, j, k, val);
                            previous_voxel = the_voxel; //save this as previous
                        }
                    }
                }
            }
            //IJ.showProgress((float)k/(float)image_size[2]);
            IJ.showStatus("Processed " + k + "/" + image_size[2]);
        } // k


        //IJ.showProgress(1,1);
        IJ.showStatus("Done");
    }

    /*
     * helper method: clamps at 0
     */
    private int clamp_min(int in) {
        if (in < 0) {
            return 0;
        } else {
            return in;
        }
    }

    /*
     * clamps to the border (will be the border if larger)
     *
     * second argument is the dimension in direction 0 -> x, 1 -> y, 2 -> z
     */
    private int clamp_max(int in, int i) {
        if (image_size[i] < in) {
            return image_size[i];
        } else {
            return in;
        }
    }

    /*
     * The 6 succeeding methods describe: A 2D (offset) image moves along the
     * direction of propagation and finds all voxels that are new (or old) to
     * the 3d filter kernel if moved along a certain direction. All voxels
     * either new or old are processed (e.g. summed for mean) and returned by
     * these methods.
     *
     * This method was created by thinking about a left right movement of the
     * spherical mean kernel.
     *
     * move along global x axis
     */
    private Voxel_value calculate_border_lr(int x, int y, int z, Kernel3D kernel, boolean debug) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        //local y equals global y
        int start_local_y = clamp_min(y - radius);
        int end_local_y = clamp_max(y + radius + 1, 1); //2nd argument is the direction
        //process rows (avoid borders)
        for (int j = start_local_y; j < end_local_y; j++) { //also needs <=
            //process columns (avoid borders)
            //local x is global z (swapped or not? does it matter?)
            //this uses the special array of the filter
            int start_local_x = clamp_min(z - kernel.filter_mask_array[j - y + radius]);
            //lookups in relation to center point: val-center+rad //could be moved to kernel3d
            int end_local_x = clamp_max(z + kernel.filter_mask_array[j - y + radius] + 1, 2);
            for (int k = start_local_x; k < end_local_x; k++) { //needs <= since position relative to origin
                //process depth (avoid borders)
                //depth is always needed
                int depth = kernel.difference_kernel_offset_matrix[k - z + radius][j - y + radius];
                if (x + radius < image_size[0]) { //check against border
                    out.value = process_voxel(out.value, in_image.getVoxel(x + depth, j, k));
                    out.voxel_count++;
                    //debug color
                    if (debug) {
                        if (y == 6 && z == 0 && x == 10) {
                            out_image.setVoxel(x + depth, j, k, 0.0);
                        }
                    }
                } else if (x + depth < image_size[0]) { //finer check
                    out.value = process_voxel(out.value, in_image.getVoxel(x + depth, j, k));
                    out.voxel_count++;
                    //debug color
                    if (debug) {
                        if (y == 6 && z == 0 && x == 10) {
                            out_image.setVoxel(x + depth, j, k, 0.0);
                        }
                    }
                }
            }
        }

        return out;
    }

    /*
     * copy for right left case, move along global x axis
     */
    private Voxel_value calculate_border_rl(int x, int y, int z, Kernel3D kernel, boolean debug) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        //local y equals global y
        int start_local_y = clamp_min(y - radius);
        int end_local_y = clamp_max(y + radius + 1, 1); //2nd argument is the direction
        //process rows (avoid borders)
        for (int j = start_local_y; j < end_local_y; j++) { //also needs <=
            //process columns (avoid borders)
            //local x is global z (swapped or not? does it matter?)
            //this uses the special array of the filter
            int start_local_x = clamp_min(z - kernel.filter_mask_array[j - y + radius]);
            int end_local_x = clamp_max(z + kernel.filter_mask_array[j - y + radius] + 1, 2);
            for (int k = start_local_x; k < end_local_x; k++) { //needs <= since position relative to origin
                //process depth (avoid borders)
                //depth is always needed
                int depth = kernel.difference_kernel_offset_matrix[k - z + radius][j - y + radius];
                if (x - radius >= 0) { //check against border //WEIRD +- due to storage of -depth
                    out.value = process_voxel(out.value, in_image.getVoxel(x + depth, j, k));
                    out.voxel_count++;
                    //debug color
                    if (debug) {
                        if (y == 6 && z == 0 && x == 10) {
                            out_image.setVoxel(x + depth, j, k, 0.0);
                        }
                    }
                } else if (x + depth >= 0) { //finer check
                    out.value = process_voxel(out.value, in_image.getVoxel(x + depth, j, k));
                    out.voxel_count++;
                    //debug color
                    if (debug) {
                        if (y == 6 && z == 0 && x == 10) {
                            out_image.setVoxel(x + depth, j, k, 0.0);
                        }
                    }
                }
            }
        }

        return out;
    }

    /*
     * copy for top bottom case, move along global y axis
     */
    private Voxel_value calculate_border_tb(int x, int y, int z, Kernel3D kernel, boolean debug) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        //local y is global x (bottom up traversal)
        int start_local_y = clamp_min(x - radius);
        int end_local_y = clamp_max(x + radius + 1, 0);
        for (int i = start_local_y; i < end_local_y; i++) {
            //local x is global z
            int start_local_x = clamp_min(z - kernel.filter_mask_array[i - x + radius]);
            int end_local_x = clamp_max(z + kernel.filter_mask_array[i - x + radius] + 1, 2);
            for (int k = start_local_x; k < end_local_x; k++) {
                int depth = kernel.difference_kernel_offset_matrix[k - z + radius][i - x + radius];
                if (y + radius < image_size[1]) { //check against border (local front,back movement; global y axis)
                    out.value = process_voxel(out.value, in_image.getVoxel(i, y + depth, k));
                    out.voxel_count++;
                    if (debug) {
                        if (y == 1 && z == 0) {
                            System.out.println(out.value);
                        }
                    }
                } else if (y + depth < image_size[1]) { //finer check
                    out.value = process_voxel(out.value, in_image.getVoxel(i, y + depth, k));
                    out.voxel_count++;
                    if (debug) {
                        if (y == 1 && z == 0) {
                            System.out.println(out.value);
                        }
                    }
                }
            }
        }

        return out;
    }

    /*
     * copy for bottom top case, move along global y axis
     */
    private Voxel_value calculate_border_bt(int x, int y, int z, Kernel3D kernel, boolean debug) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        //local y is global x (bottom up traversal)
        int start_local_y = clamp_min(x - radius);
        int end_local_y = clamp_max(x + radius + 1, 0);
        for (int i = start_local_y; i < end_local_y; i++) {
            //local x is global z
            int start_local_x = clamp_min(z - kernel.filter_mask_array[i - x + radius]);
            int end_local_x = clamp_max(z + kernel.filter_mask_array[i - x + radius] + 1, 2);
            for (int k = start_local_x; k < end_local_x; k++) {
                int depth = kernel.difference_kernel_offset_matrix[k - z + radius][i - x + radius];
                if (y - radius >= 0) { //check against border (local front,back movement; global y axis)
                    out.value = process_voxel(out.value, in_image.getVoxel(i, y + depth, k));
                    out.voxel_count++;
                    if (debug) {
                        if (y == 5 && z == 0) {
                            out_image.setVoxel(i, y + depth, k, 0.0);
                        }
                    }
                } else if (y + depth >= 0) { //finer check
                    out.value = process_voxel(out.value, in_image.getVoxel(i, y + depth, k));
                    out.voxel_count++;
                    if (debug) {
                        if (y == 5 && z == 0) {
                            out_image.setVoxel(i, y + depth, k, 0.0);
                        }
                    }
                }
            }
        }

        return out;
    }

    /*
     * copy for front back case, move along global z axis
     */
    private Voxel_value calculate_border_fb(int x, int y, int z, Kernel3D kernel, boolean debug) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        //local y is global y
        int start_local_y = clamp_min(y - radius);
        int end_local_y = clamp_max(y + radius + 1, 1);
        for (int j = start_local_y; j < end_local_y; j++) {
            //local x is global x
            int start_local_x = clamp_min(x - kernel.filter_mask_array[j - y + radius]);
            int end_local_x = clamp_max(x + kernel.filter_mask_array[j - y + radius] + 1, 0);
            for (int i = start_local_x; i < end_local_x; i++) {
                int depth = kernel.difference_kernel_offset_matrix[i - x + radius][j - y + radius];
                if (z + radius < image_size[2]) { //check against border (local front,back movement; global z axis)
                    out.value = process_voxel(out.value, in_image.getVoxel(i, j, z + depth));
                    out.voxel_count++;
                    if (debug) {
                        if (z == 1) {
                            out_image.setVoxel(i, j, z + depth, 0.0);
                        }
                    }
                } else if (z + depth < image_size[2]) { //finer check
                    out.value = process_voxel(out.value, in_image.getVoxel(i, j, z + depth));
                    out.voxel_count++;
                    if (debug) {
                        if (z == 1) {
                            out_image.setVoxel(i, j, z + depth, 0.0);
                        }
                    }
                }
            }
        }

        return out;
    }

    /*
     * copy for back front case, move along global z axis
     */
    private Voxel_value calculate_border_bf(int x, int y, int z, Kernel3D kernel, boolean debug) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        //local y is global y
        int start_local_y = clamp_min(y - radius);
        int end_local_y = clamp_max(y + radius + 1, 1);
        for (int j = start_local_y; j < end_local_y; j++) {
            //local x is global x
            int start_local_x = clamp_min(x - kernel.filter_mask_array[j - y + radius]);
            int end_local_x = clamp_max(x + kernel.filter_mask_array[j - y + radius] + 1, 0);
            for (int i = start_local_x; i < end_local_x; i++) {
                int depth = kernel.difference_kernel_offset_matrix[i - x + radius][j - y + radius];
                if (z - radius >= 0) { //check against border (local front,back movement; global z axis)
                    out.value = process_voxel(out.value, in_image.getVoxel(i, j, z + depth));
                    out.voxel_count++;
                    if (debug) {
                        if (z == 1) {
                            out_image.setVoxel(i, j, z + depth, 0.0);
                        }
                    }
                } else if (z + depth >= 0) { //finer check
                    out.value = process_voxel(out.value, in_image.getVoxel(i, j, z + depth));
                    out.voxel_count++;
                    if (debug) {
                        if (z == 1) {
                            out_image.setVoxel(i, j, z + depth, 0.0);
                        }
                    }
                }
            }
        }

        return out;
    }

    /*
     * This method simply executes the filter for one voxel without any use of
     * neighbor information (brute force to run this on every voxel)
     *
     * in ... voxel position
     */
    protected Voxel_value iterate_and_read(int[] in) {
        int[] start_values = new int[3];
        int[] end_values = new int[3];

        //clamp start and end at image borders
        for (int i = 0; i < 3; i++) {
            start_values[i] = clamp_min(in[i] - radius);
            end_values[i] = clamp_max(in[i] + radius + 1, i);
            //System.out.println(start_values[i]+ "/" +end_values[i]);
        }

        //fill
        double voxel = initial_value;
        int num_voxel = 0;
        for (int k = start_values[2]; k < end_values[2]; k++) {
            for (int j = start_values[1]; j < end_values[1]; j++) {
                for (int i = start_values[0]; i < end_values[0]; i++) {

                    //lookup in filter need to be in 0-2*radius intervall
                    if (filter_kernel.getValueAt(i - in[0], j - in[1], k - in[2])) {
                        voxel = process_voxel(voxel, in_image.getVoxel(i, j, k));
                        num_voxel++;
                    }

                }
            }
        }

        return new Voxel_value(voxel, num_voxel);
    }

    /*
     * Method to find the values new at the new voxel.
     *
     * Basically a switch for different directions.
     */
    protected Voxel_value iterate_and_add(int x, int y, int z, byte type) {
        //this is executed every voxel
        //how to avoid too many lookups?
        //specially at the borders

        Voxel_value out = new Voxel_value(initial_value, 0);

        switch (type) {
            case Kernel3D.LEFT: //add: kernel moving from left to right
                out = calculate_border_lr(x, y, z, filter_kernel_right, false);

                break;
            case Kernel3D.RIGHT:
                out = calculate_border_rl(x, y, z, filter_kernel_left, false);

                break;
            case Kernel3D.TOP:
                out = calculate_border_tb(x, y, z, filter_kernel_bottom, false);

                break;
            case Kernel3D.BOTTOM:
                out = calculate_border_bt(x, y, z, filter_kernel_top, false);

                break;
            case Kernel3D.FRONT:
                out = calculate_border_fb(x, y, z, filter_kernel_back, false);

                break;
        }

        return out;
    }

    /*
     * Method to find the old values that just moved out of the area of
     * interest. The center-point for this method is the previous voxel.
     *
     * Basically a switch for different directions.
     */
    protected Voxel_value iterate_and_subtract(int x, int y, int z, byte type) {
        Voxel_value out = new Voxel_value(initial_value, 0);

        switch (type) {
            case Kernel3D.LEFT: //add: left to right
                out = calculate_border_rl(x - 1, y, z, filter_kernel_left, false);

                break;
            case Kernel3D.RIGHT:
                out = calculate_border_lr(x + 1, y, z, filter_kernel_right, false);

                break;
            case Kernel3D.TOP:
                out = calculate_border_bt(x, y - 1, z, filter_kernel_top, false);

                break;
            case Kernel3D.BOTTOM:
                out = calculate_border_tb(x, y + 1, z, filter_kernel_bottom, false);

                break;
            case Kernel3D.FRONT:
                out = calculate_border_bf(x, y, z - 1, filter_kernel_front, false);

                break;
        }

        return out;
    }

    /*
     * This method processes the succeeding voxel with use of previous
     * information. to_add is the new area of the filter to_subtract is the area
     * moved out of the region of interest
     *
     * the output will be post processed and written to the image (in the
     * filter() method)
     *
     * to be overridden
     */
    protected Voxel_value process_next_voxel(int x, int y, int z, Voxel_value prev_voxel, byte type) {
        //method is generally overwritten! (code only for understanding)
        //mean example
        Voxel_value out = new Voxel_value(prev_voxel.value, prev_voxel.voxel_count);
        Voxel_value to_add = iterate_and_add(x, y, z, type);
        Voxel_value to_subtract = iterate_and_subtract(x, y, z, type);

        out.value = out.value - to_subtract.value + to_add.value;
        out.voxel_count = out.voxel_count - to_subtract.voxel_count + to_add.voxel_count;

        return out;
    }

    /*
     * Generic process method (e.g. addition for summing all values for mean
     * filter)
     *
     * to be overridden
     */
    protected double process_voxel(double temp_value, double operand) {
        //mean example
        return (temp_value + operand);
    }

    /*
     * Generic post process method (e.g. division to get the mean value)
     *
     * to be overridden
     */
    protected double post_process_voxel(double temp_value, double operand) {
        //mean example
        if (operand != 0.0) {
            return temp_value / operand;
        } else {
            return 0.0;
        }
    }

    protected double getVoxel(int i, int j, int k) {
        return in_image.getVoxel(i, j, k);
    }
}
