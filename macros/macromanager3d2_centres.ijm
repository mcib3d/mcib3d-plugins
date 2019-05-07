requires("1.46g");
rename("image");
w=getWidth();
h=getHeight();
s=nSlices();
rx=5;
ry=5;
rz=2;
selectWindow("image");
run("3D Manager");
Ext.Manager3D_AddImage();
newImage("centres", "16-bit Black", w, h, s);
setBatchMode("hide");
selectWindow("centres");
Ext.Manager3D_Count(nb);
Ext.Manager3D_MonoSelect();
Ext.Manager3D_DeselectAll();
for(i=0;i<nb;i++) {
     Ext.Manager3D_Centroid3D(i,cx,cy,cz);
     run("3D Draw Shape", "size="+w+","+h+","+s+" center="+cx+","+cy+","+cz+" radius="+rx+","+ry+","+rz+" vector1=1.0,0.0,0.0 vector2=0.0,1.0,0.0 res_xy=1.000 res_z=1.000 unit=pix value=i display=Overwrite");
}
run("3-3-2 RGB");
setSlice(s/2+1);
run("Enhance Contrast", "saturated=0.35");
setBatchMode("show");
print("finished");
// TEST
listidx="";
Ext.Manager3D_GetSelected(listidx);
print("selected "+listidx);
