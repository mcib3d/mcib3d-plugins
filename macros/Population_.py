from mcib3d.geom import Objects3DPopulation,Object3DVoxels
from mcib3d.image3d import ImageInt,ImageByte,ImageShort
from ij import IJ,WindowManager

# get current image, a image with labelled objects
plus=WindowManager.getCurrentImage()
# wrap ImagePlus into 3D suite image format
img=ImageInt.wrap(plus)
# create a population of 3D objects
pop=Objects3DPopulation(img)
# create a mask where to shuffle objects
# here same as input image
maskimg = ImageByte("mask", img.sizeX, img.sizeY, img.sizeZ)
maskimg.fill(1)
obj=Object3DVoxels(maskimg)
pop.setMask(obj)
# get the shuffled objects and build a population from them
listObjects=pop.shuffle()
pop2=Objects3DPopulation(listObjects)
# draw the results
res = ImageShort("res", img.sizeX, img.sizeY, img.sizeZ)
pop2.draw(res)
res.show("shuffle")
