requires("1.46g");
rename("image");
w=getWidth();
h=getHeight();
s=nSlices();
selectWindow("image");
run("3D Manager");
Ext.Manager3D_AddImage();
Ext.Manager3D_Measure();
newImage("color size", "8-bit Black", w, h, s);
selectWindow("color size");
Ext.Manager3D_Count(nb);
Ext.Manager3D_Measure3D(0,"Vol",V);
max=V; min=V;
// loop to find max and min volumes
for(i=1;i<nb;i++) {
	Ext.Manager3D_Measure3D(i,"Vol",V);
	if(V>max) {
		max=V;
	}
	if(V<min){
		min=V;
	}		
}
Ext.Manager3D_MonoSelect();
Ext.Manager3D_DeselectAll();
for(i=0;i<nb;i++) {
     Ext.Manager3D_Measure3D(i,"Vol",V);
     s=(V-min)/(max-min);
     Ext.Manager3D_Select(i);
     Ext.Manager3D_FillStack(255*s, 255*s, 255*s);
}
run("Fire");
setSlice(s/2+1);
run("Enhance Contrast", "saturated=0.35");
print("finished");
