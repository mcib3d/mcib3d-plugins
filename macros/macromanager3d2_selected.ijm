requires("1.46g");
rename("image");
selectWindow("image");
run("3D Manager");
Ext.Manager3D_AddImage();
waitForUser("Select some objects in 3D, you may need to Deselect first to refresh list");
list="";
name="";
Ext.Manager3D_GetSelected(list);
idx=split(list,":");
print("Selected objects:");
for(i=0;i<idx.length;i++) {
Ext.Manager3D_GetName(idx[i],name);
print(idx[i]+" "+name);
}
