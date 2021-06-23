##

import numpy


def execute(cCape,LI,LR,vTot,dCape,PW):
  a = MC1(cCape) + MC2(LI) + MC3(LR) + MC4(vTot) + MC5(dCape) + MC6(PW)
  return a

def MC1(cCape):
    rtn = numpy.zeros(shape = cCape.shape, dtype=numpy.float32)
    rtn[(cCape < 3100)] = 0
    rtn[(cCape >= 3100) & (cCape < 4000)] = 1
    rtn[(cCape >= 4000)] = 2
    return rtn

def MC2(LI):
    rtn = numpy.zeros(shape = LI.shape, dtype=numpy.float32)
    rtn[(LI > -8)] = 0
    rtn[(LI <= -8) & (LI > -9)] = 1
    rtn[(LI <= -9) & (LI > -10)] = 2
    rtn[(LI <= -10)] = 3
    return rtn

def MC3(LR):
    rtn = numpy.zeros(shape = LR.shape, dtype=numpy.float32)
    rtn[(LR <= 8.4)] = 0
    rtn[(LR > 8.4)] = 1
    return rtn

def MC4(vTot):
    rtn = numpy.zeros(shape = vTot.shape, dtype=numpy.float32)
    rtn[(vTot < 27)] = 0
    rtn[(vTot >= 27) & (vTot < 28)] = 1
    rtn[(vTot >= 28) & (vTot < 29)] = 2
    rtn[(vTot >= 29)] = 3
    return rtn

def MC5(dCape):
    rtn = numpy.zeros(shape = dCape.shape, dtype=numpy.float32)
    rtn[(dCape < 900)] = 0
    rtn[(dCape >= 900) & (dCape < 1100)] = 1
    rtn[(dCape >= 1100) & (dCape < 1300)] = 2
    rtn[(dCape >= 1300)] = 3
    return rtn

def MC6(PW):
    rtn = numpy.zeros(shape = PW.shape, dtype=numpy.float32)
    rtn[(PW <= 1.5)] = -5
    rtn[(PW > 1.5)] = 0
    return rtn


