// use plugins/macros/record to record the necessary opening and channel splitting
// 3 images are opened in this example : labels.tif with segmented object, C1-signal.tif and C2-signal.tif with signals to quantify
// select measurments in Manager3D Options, select Plugins/Record to record
run("3D Manager Options", "volume integrated_density mean_grey_value std_dev_grey_value mode_grey_value minimum_grey_value maximum_grey_value distance_between_centers=10 distance_max_contact=1.80 drawing=Contour");// run the manager 3D and add image
run("3D Manager");
// select the image with the labelled objects
selectWindow("labels.tif");
Ext.Manager3D_AddImage();
// if list is not visible please refresh list by using Deselect
Ext.Manager3D_Select(0);
Ext.Manager3D_DeselectAll();
// number of results, and arrays to store results
Ext.Manager3D_Count(nb);
// get object labels
labels=newArray(nb);
vols=newArray(nb);
selectWindow("labels.tif");
// loop over objects
for(i=0;i<nb;i++){
	 Ext.Manager3D_Quantif3D(i,"Mean",quantif); // quantification, use IntDen, Mean, Min,Max, Sigma
	 labels[i]=quantif;
	  Ext.Manager3D_Measure3D(i,"Vol",vol); // volume
	  vols[i]=vol;
}
signal1=newArray(nb);
selectWindow("C1-signal.tif");
// loop over objects
for(i=0;i<nb;i++){
	 Ext.Manager3D_Quantif3D(i,"Mean",quantif); // quantification, use IntDen, Mean, Min,Max, Sigma
	 signal1[i]=quantif;
}
signal2=newArray(nb);
selectWindow("C2-signal.tif");
// loop over objects
for(i=0;i<nb;i++){
	 Ext.Manager3D_Quantif3D(i,"Mean",quantif); // quantification, use IntDen, Mean, Min,Max, Sigma
	 signal2[i]=quantif;
}
// create results Table
for(i=0;i<nb;i++){
	setResult("label", i, labels[i]);
	setResult("volume", i, vols[i]);
	setResult("signal1", i, signal1[i]);
	setResult("signal2", i, signal2[i]);
}
updateResults();
// if list is not visible please refresh list by using Deselect
//Ext.Manager3D_Select(0);
//Ext.Manager3D_DeselectAll();

