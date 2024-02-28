#
# New derived parameter
#
#
#
#    SOFTWARE HISTORY
#
#    Date            Engineer       Description
#    ------------    -----------    --------------------------
#    Feb 05, 2018    cstumpf        Initial Creation.
#
#     Code courtesy of Chris Stumpf (WFO HUN)
#

from numpy import arccos, degrees, float32, less, greater, NaN, where
import numpy as np
import Direction
import Vector

def execute(*args):

    # Passing in the BlkShr vector and the magnitude let's set variables.
    blkShr = np.asarray(args[0])
    blkMag = np.asarray(args[1])

    blkShrDir = Direction.execute(blkShr[0],blkShr[1]) # This gets our direction from the U/V components of BlkShr

    tmpDir = blkShrDir
    tmpMag = blkMag * 1.94 # Convert m/s to knots only for our calculation. We don't want knots in final product.

    calcMag = where(less(tmpMag,30.0),float32(NaN), 30.0/tmpMag) # If values are below 30 kts we don't want to calculate them
    calcMag = where(greater(calcMag,1.0),1.0,calcMag)  # Arccos needs values to be between -1 and 1 so let's set that just in case
    calcMag = where(less(calcMag,-1.0),-1.0,calcMag)   # Arccos needs values to be between -1 and 1 so let's set that just in case
    angle = arccos(calcMag)  # Do our calc of the angle.

    # To get the left Vector Subtract our new angle from the BlkShr direction.
    newDir = tmpDir - degrees(angle)

    # We've got our final Magnitude and Direction. Now need to convert back to
    # U,V components.

    return Vector.execute(blkMag, newDir,1)  # Utilize the Vector function in AWIPS (1=Degrees, 0=Radians)
