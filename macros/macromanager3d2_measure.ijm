// open image
open("");
// some options in Manager3D Options, select Plugins/Record to record
run("3D Manager Options", "volume surface compactness fit_ellipse 3d_moments integrated_density std_dev_grey_value mode_grey_value feret minimum_grey_value maximum_grey_value distance_to_surface bounding_box sync distance_between_centers=10 distance_max_contact=1.80");
// run the manager 3D and add image
run("3D Manager");
Ext.Manager3D_AddImage();
// do some measurements, save measurements and close window
Ext.Manager3D_Measure();
Ext.Manager3D_SaveResult("M","/home/thomasb/Results3D.csv");
Ext.Manager3D_CloseResult("M");
// Use Q for the Quantification window, D for distances, C for colocalisation, L or V for list voxels and A for A windows
// if list is not visible please refresh list by using Deselect
//Ext.Manager3D_Select(0);
//Ext.Manager3D_DeselectAll();

