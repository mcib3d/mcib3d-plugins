w=getWidth();
h=getHeight();
d=nSlices;
run("3D Manager");
Ext.Manager3D_AddImage();
newImage("Random", "RGB Black", w,h, d);
Ext.Manager3D_Count(nb);
viewer3d=true;
for(i=0;i<nb;i++) {
	Ext.Manager3D_Select(i);
	r=255*random;
	g=255*random;
	b=255*random;
	Ext.Manager3D_FillStack(r, g, b);
	if(viewer3d) Ext.Manager3D_Fill3DViewer(r, g, b);
}
