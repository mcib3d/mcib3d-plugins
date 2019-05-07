w=getWidth();
h=getHeight();
d=nSlices;
run("3D Manager");
Ext.Manager3D_AddImage();
getLut(reds, greens, blues);
newImage("Random", "RGB Black", w,h, d);
Ext.Manager3D_Count(nb);
viewer3d=true;
if(nb<255) of=255.0/nb; else of=1;
for(i=0;i<nb;i++) {
	Ext.Manager3D_Select(i);
	r=reds[(i*of)%255];
	g=greens[(i*of)%255];
	b=blues[(i*of)%255];
	
	Ext.Manager3D_FillStack(r, g, b);
	if(viewer3d) Ext.Manager3D_Fill3DViewer(r, g, b);
}
print("finished");
 
