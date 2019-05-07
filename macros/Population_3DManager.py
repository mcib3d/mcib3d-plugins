from mcib3d.geom import Objects3DPopulation,Object3DVoxels
from mcib3d.image3d import ImageInt,ImageByte,ImageShort
from ij import IJ,WindowManager
from mcib_plugins.tools import RoiManager3D_2

# open 3D Manager
manager = RoiManager3D_2()
# create empty population and load objects
pop = Objects3DPopulation()
pop.loadObjects("/home/boudier/Roi3D.zip")
manager.addObjects3DPopulation(pop)
