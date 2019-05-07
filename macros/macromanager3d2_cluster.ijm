w=getWidth();
h=getHeight();
d=nSlices();
na=getTitle();
newImage("cluster", "16-bit", w, h, d);
setForegroundColor(255, 255, 255);
run("3D Manager");
selectWindow(na);
Ext.Manager3D_AddImage();
//setBatchMode(true);
var nb=0;
Ext.Manager3D_Count(nb); 
var clus=newArray(nb); for(i=0;i<nb;i++) clus[i]=-1;
distArray=newArray(nb);
curclu=1;
drawline=true;
for(i=0;i<nb;i++) {
	showStatus("Processing "+i+"/"+nb);
	Ext.Manager3D_Closest(i,"cc",clo);
	Ext.Manager3D_Dist2(i,clo,"cc",dist);	
	distArray[i]=dist;
	// none are assigned to cluster
	if((clus[i]==-1)&&(clus[clo]==-1)){
		clus[i]=curclu; clus[clo]=curclu;
		curclu++;
	}
	// one is already assigned
	else if((clus[i]!=-1)&&(clus[clo]==-1)){
		clus[clo]=clus[i];	
	}
	else if((clus[i]==-1)&&(clus[clo]!=-1)){
		clus[i]=clus[clo];	
	}
	// both are already assigned, take min and propagate
	else if((clus[i]!=-1)&&(clus[clo]!=-1)){
		clu=minOf(clus[i],clus[clo]);
		if(clus[i]!=clu) {
			tmp=clus[i];
			clus[i]=clu; propagate(tmp,clu);
		}
		else {
			tmp=clus[clo];
			clus[clo]=clu; propagate(tmp,clu);
		}		
	} 
}
selectWindow("cluster");
Ext.Manager3D_MonoSelect();
for(i=0;i<nb;i++) {
	showStatus("Drawing  "+i+"/"+nb);	
	Ext.Manager3D_Select(i); Ext.Manager3D_FillStack(clus[i], clus[i], clus[i]);
	if(drawline){
		Ext.Manager3D_Centroid3D(i,cx0,cy0,cz0);
		Ext.Manager3D_DeselectAll();
		Ext.Manager3D_Closest(i,"cc",clo) ;
		Ext.Manager3D_Centroid3D(clo,cx1,cy1,cz1);
		val=clus[i];
		run("3D Draw Line", "size_x=&w size_y=&h size_z=&d x0=&cx0 y0=&cy0 z0=&cz0 x1=&cx1 y1=&cy1 z1=&cz1 thickness=1.000 value=&val display=Overwrite ");
	}
}
run("3-3-2 RGB");
run("Enhance Contrast", "saturated=0.35");

// distances analysis
Array.sort(distArray);
plotidx=newArray(nb); for(i=0;i<nb;i++) plotidx[i]=(i+1)/nb;;
Plot.create("G analysis", "Distances", "Freq", distArray, plotidx);
Plot.show();

print("finished");
updateDisplay();
//setBatchMode(false);

function propagate(a,b){
	for(i=0;i<clus.length;i++) if(clus[i]==a) clus[i]=b;	
}
