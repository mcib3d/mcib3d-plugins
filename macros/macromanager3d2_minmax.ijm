run("3D Manager");
viewer3d=true;
run("Duplicate...", "title=copie duplicate range=1-"+nSlices);
// add image and count nb of objects
Ext.Manager3D_AddImage();
Ext.Manager3D_Count(nb);

// initialize min and max
Ext.Manager3D_Measure3D(0,"Vol",V);
max=V; min=V;
minobj=0; maxobj=0;

// loop to find max and min volumes
for(i=1;i<nb;i++) {
	Ext.Manager3D_Measure3D(i,"Vol",V);
	if(V>max) {
		max=V; maxobj=i;
	}
	if(V<min){
		min=V; minobj=i;
	}		
}
Ext.Manager3D_GetName(minobj,nameMin);
Ext.Manager3D_GetName(maxobj,nameMax);
// print the results
print("The smallest : "+nameMin+" has a volume of "+min);
print("The biggest : "+nameMax+" has a volume of "+max);
Ext.Manager3D_MonoSelect();
Ext.Manager3D_DeselectAll();
// normal objects in white
// smallest object in red, biggest in green
selectImage("copie");
run("RGB Color");
for(i=0;i<nb;i++){
	Ext.Manager3D_Select(i);
	if(i==minobj) Ext.Manager3D_FillStack(255, 0, 0);
	else if(i==maxobj) Ext.Manager3D_FillStack(0, 255, 0);
	else Ext.Manager3D_FillStack(255, 255, 255);
	if(viewer3d) {
		if(i==minobj) Ext.Manager3D_Fill3DViewer(255, 0, 0);
		else if(i==maxobj) Ext.Manager3D_Fill3DViewer(0, 255, 0);
		//else Ext.Manager3D_Fill3DViewer(255, 255, 255);
	}
}
//Ext.Manager3D_Close();


