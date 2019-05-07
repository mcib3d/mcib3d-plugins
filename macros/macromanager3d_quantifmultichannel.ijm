// use plugins/macros/record to record the necessary opening and channel splitting
// 3 images are opened in this example : labels.tif with segmented object, C1-signal.tif and C2-signal.tif with signals to quantify
// select measurments in Manager3D Options, select Plugins/Record to record
run("3D Manager Options", "volume integrated_density mean_grey_value std_dev_grey_value mode_grey_value minimum_grey_value maximum_grey_value distance_between_centers=10 distance_max_contact=1.80 drawing=Contour");// run the manager 3D and add image
run("3D Manager");
// select the image with the labelled objects
selectWindow("labels.tif");
Ext.Manager3D_AddImage();
// do some quantifications, save measurements and close window
selectWindow("C1-signal.tif");
Ext.Manager3D_Quantif();
Ext.Manager3D_SaveResult("Q","/home/boudier.t/C1-Quantif3D.csv");
Ext.Manager3D_CloseResult("Q");
selectWindow("C2-signal.tif");
Ext.Manager3D_Quantif();
Ext.Manager3D_SaveResult("Q","/home/boudier.t/C2-Quantif3D.csv");
Ext.Manager3D_CloseResult("Q");
// Use Q for the Quantification window, D for distances, C for colocalisation, L or V for list voxels and A for A windows
// if list is not visible please refresh list by using Deselect
//Ext.Manager3D_Select(0);
//Ext.Manager3D_DeselectAll();

