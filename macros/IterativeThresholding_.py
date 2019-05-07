from mcib3d.image3d.IterativeThresholding import TrackThreshold
from ij import IJ,WindowManager

# get current image
plus=WindowManager.getCurrentImage()
# init the iterative thresholding
volMin=10 # minimum volume
volMax=1000 # maximum volume
minCont=0 # min contrast
step=1 # check threshold every step 
thmin=50 # minimum threshold to start = background noise
IT = TrackThreshold(volMin, volMax, minCont, step, step, thmin)
# various methods
tmethod = TrackThreshold.THRESHOLD_METHOD_STEP # check threshold every step 
IT.setMethodThreshold(tmethod)
cri = TrackThreshold.CRITERIA_METHOD_MIN_ELONGATION # favours less elongated objects = rounder objects
IT.setCriteriaMethod(cri)
# segment best objects, else use segment(..)
res = IT.segmentBest(plus, True)
res.show()