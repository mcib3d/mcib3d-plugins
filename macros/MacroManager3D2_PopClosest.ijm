run("3D Manager");
// population 1
selectImage("pop1");
Ext.Manager3D_AddImage();
Ext.Manager3D_Count(nb1);
// population 2
selectImage("pop2");
Ext.Manager3D_AddImage();
Ext.Manager3D_Count(nbTotal);
nb2=nbTotal-nb1;

// select objects in population 2 
Ext.Manager3D_SelectFor(nb2,nbTotal,1);

// find closest object in pop2 for all objects in pop1
// cc = center to center distance, bb=border to border distance (slower)
for(i=0;i<nb1;i++) {
	Ext.Manager3D_Closest(i,"cc",closest); 
	Ext.Manager3D_GetName(closest, name);
	print("Closest center to "+i+" is "+name);
}

