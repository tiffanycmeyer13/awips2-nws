#
# Python based Decoder for Gridding NUCAPS Sounding data. The decoder gets a list of sounding files
# to process. It loads the soundings from hdf files, and vertically interpolates the data to standard levels,
# then it horizontally interpolates the data using nearest neighbor. Finally, it masks the data outside the 
# area where data is available. Output is an array of GridRecords containing the gridded NUCAPS data.
# The grids are global in coverage and have variables such as temperature, ozone, water vapor, RH, total ozone, 
# Ozone anomaly, and tropopause height. This software was adapted from work by Nadia Smith and Emily Berndt.
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    Oct 4, 2018     DCS-18691     jburks         Initial creation
#    Mar 15,2019     VLab-61425    jburks         Fixed calc issue with lowest pressure level water vapor
#    
#
from __future__ import print_function

import calendar
import sys, os, time, re, getopt

from ufpy import TimeUtil
 
from com.raytheon.uf.common.wmo import WMOTimeParser
from com.raytheon.uf.edex.decodertools.time import TimeTools
import LogStream
from com.raytheon.uf.common.dataplugin.grid import GridRecord, GridInfoRecord
from java.util import GregorianCalendar
from java.util import Date

from com.raytheon.uf.common.time import DataTime
from com.raytheon.uf.common.time import TimeRange

from com.raytheon.uf.common.dataplugin.grid import GridRecord

from com.raytheon.uf.common.gridcoverage import LambertConformalGridCoverage
from com.raytheon.uf.common.gridcoverage import LatLonGridCoverage
from com.raytheon.uf.common.gridcoverage import MercatorGridCoverage
from com.raytheon.uf.common.gridcoverage import PolarStereoGridCoverage
from com.raytheon.uf.common.gridcoverage.lookup import GridCoverageLookup
from com.raytheon.uf.common.gridcoverage import Corner

from com.raytheon.uf.common.grib import GribModelLookup
from com.raytheon.uf.common.grib.tables import GribTableLookup

from com.raytheon.uf.common.dataplugin.level.mapping import LevelMapper
from com.raytheon.uf.common.dataplugin.level import Level
from com.raytheon.uf.common.dataplugin.level import LevelFactory

from com.raytheon.edex.plugin.grib.spatial import GribSpatialCache
from com.raytheon.uf.common.util import GridUtil


from com.raytheon.edex.util.grib import GribParamTranslator

from com.raytheon.uf.common.parameter import Parameter;
from com.raytheon.uf.common.parameter.mapping import ParameterMapper
import UFStatusHandler
import logging
import re
from datetime import datetime
import pytz

import h5py
import numpy as np
import scipy.interpolate
import math

# #Number of nx and ny points for the grid. 720 nx, 360 ny creates .5 degree grid.
nx = 720
ny = 360
nx_dim = 720j
ny_dim = 360j
dist = 1.0
# #Coverage for the global grid.
regionCoverage = [-179.9999999749438 , -89.9999999874719 , 179.9999999749438 , 89.9999999874719]

F32_GRID_FILL_VALUE = np.nan
FILL_VAL = np.nan

# #Standard Pressure levels to output the data
stdplev = [100., 125. , 150. , 175. , 200. , 225. , 250. , 275. , 300. , 325. , 350., \
           375. , 400. , 425. , 450. , 475. , 500. , 525. , 550. , 575. , 600., \
           625. , 650. , 675. , 700. , 725. , 750. , 775. , 800. , 825. , 850, \
           875. , 900. , 925. , 950. , 975. , 1000. , 1025. , 1050. , 1075. , 1100.  ]  # 41 levels
bottom = 1100
low = 800
mid = 500
high = 300

levlow = 88;
levmid = 74;
levhigh = 62
# #Define some constants
navog = 6.02214199e+23  # NIST Avogadro's number (molecules/mole)
nloschmidt = 2.6867775e+19  # NIST Loschmidt's molec/cm^3 per atm @ stdP
t_std = 273.15  # standard temperature [K]
mw_wv = 18.0151  # gm/mole water
mw_oz = 47.9982  # gm/mole ozone
mw_d = 28.9644  # gm/mole dry air
g_std = 980.664  # acceleration of gravity, cm/s^2
c_wcd = (1000.0 * navog / mw_wv * g_std)  # constant to convert dp(L) to LCD_dry(L)
c_ocd = c_wcd * mw_wv / mw_oz
wv_eps = mw_wv / mw_d
oz_eps = mw_oz / mw_d
p_std = 1013.25
cdair = 1000.0 * p_std * navog / (mw_d * g_std)

gmt = pytz.utc

logHandler = UFStatusHandler.UFStatusHandler("gov.noaa.nws.sti.mdl.edex.plugin.griddednucaps", "EDEX")


class GriddedNucapsDecoder():
    # Constructor setup intial data arrays, and process the files
    #
    def __init__(self, text=None, files=None, command=None):
        self.log = logging.getLogger("GriddedNucapsDecoder")
        self.log.addHandler(logHandler)
        self.toProcess = False
        if files != None:
            files = files.strip()
        if files != None and files != "":
            self.toProcess = True
            files = files.split(",")

            self.files = files
            self.latitude = np.empty((0))
            self.longitude = np.empty((0))
            self.temperature = np.empty((0, 100))
            self.pressure = np.empty((0, 100))
            self.surface_pressure = np.empty((0))
            self.h2o_mr = np.empty((0, 100))
            self.O3_MR = np.empty((0, 100))
            self.times = np.empty((0))
            self.Quality_Flag = np.empty((0))
            self.satelliteId = ""

            for fileToProcess in files:
                self.__processFile__(fileToProcess)
            # #Detect the times used in the products
            self.maxtime = int(np.max(self.times))
            self.mintime = int(np.min(self.times))
            # #Load Ozone Climate data
            self.loadOzoneClimoData()

    # #Loads data arrays for observations from hdf files.
    def __processFile__(self, file):
        #         self.log.info("Processing file: "+str(file))
        rootgrp = h5py.File(file, "r")
        self.attributes = rootgrp.attrs
        self.satelliteId = self.attributes["satellite_name"]
        self.latitude = np.append(self.latitude, np.array(rootgrp["Latitude@NUCAPS_EDR"][:], copy=True))
        self.longitude = np.append(self.longitude, np.array(rootgrp["Longitude@NUCAPS_EDR"][:], copy=True))
        self.temperature = np.append(self.temperature, np.array(rootgrp["Temperature@NUCAPS_EDR"], copy=True), axis=0)
        self.pressure = np.append(self.pressure, np.array(rootgrp["Pressure@NUCAPS_EDR"], copy=True), axis=0)
        self.surface_pressure = np.append(self.surface_pressure,
                                          np.array(rootgrp["Surface_Pressure@NUCAPS_EDR"][:], copy=True))
        self.h2o_mr = np.append(self.h2o_mr, np.array(rootgrp["H2O_MR@NUCAPS_EDR"], copy=True), axis=0)
        self.O3_MR = np.append(self.O3_MR, np.array(rootgrp["O3_MR@NUCAPS_EDR"], copy=True), axis=0)
        self.Quality_Flag = np.append(self.Quality_Flag, np.array(rootgrp["Quality_Flag@NUCAPS_EDR"][:], copy=True))

        self.times = np.append(self.times, np.array(rootgrp["Time@NUCAPS_EDR"], copy=True), axis=0)
        rootgrp.close()

    # #Find the surface within a sounding
    def findSurface(self, pres, surfpres):
        pres = np.array(pres)
        diff = surfpres - (pres + 5.0)
        idx = np.where(diff > 0.0)
        lsurface = np.size(idx)

        return int(lsurface)


    # #Calculate the 2m parameters such as temperature at 2m
    def calc_2m_param(self):
        nobs = np.shape(self.latitude)[0]
        botlevel = np.ones(nobs, dtype=np.float) * FILL_VAL
        pres = self.pressure[0]
        surfpres = self.surface_pressure
        temp2 = np.ones(nobs, dtype=np.float) * FILL_VAL

        for iobs in range(0, nobs):
            surflev = self.findSurface(pres, surfpres[iobs])
            num = surfpres[iobs] - pres[surflev - 1]
            denom = pres[surflev] - pres[surflev - 1]
            blmult = num / denom
            t_diff = self.temperature[iobs, surflev] - self.temperature[iobs, surflev - 1]
            temp2[iobs] = self.temperature[iobs, surflev - 1] + blmult * t_diff
            botlevel[iobs] = surflev
        return botlevel, temp2

    # #Interpolate the temperature vertically
    def interpolate_temperature(self, botlevel, blmult):
        pres = self.pressure[0, :]
        nlev = np.shape(pres)[0]
        nlev_std = len(stdplev)
        nobs = len(self.latitude)
        stdT = np.ones((nobs, nlev_std), dtype=float) * FILL_VAL
        for iobs in range(0, nobs):
            sfc = botlevel[iobs]

            if sfc + 1 <= nlev:
                self.temperature[iobs, sfc + 1:nlev ] = FILL_VAL
            stdT[iobs, :] = scipy.interpolate.griddata(pres, self.temperature[iobs, :], stdplev, method='linear')
        return stdT

    # Interpolating the gases vertically
    def interpolate_gases(self, wvcd, botlevel):
        pres = self.pressure[0, :]
        nlev = np.shape(pres)[0]
        nlev_std = len(stdplev)
        nobs = len(self.latitude)
        stdwv = np.ones((nobs, nlev_std), dtype=float) * FILL_VAL

        for iobs in range(0, nobs):
            sfc = botlevel[iobs]
            lev_wvcd = np.ones(nlev, dtype=np.float) * FILL_VAL

            lev_wvcd[0] = wvcd[iobs, 0]

            for L in range(1, int(sfc) + 1):
                lev_wvcd[L] = 0.5 * wvcd[iobs, L - 1] + 0.5 * wvcd[iobs, L]

            alog_wv = np.log10(lev_wvcd)
            alog_retp = np.log10(pres)
            alog_stdp = np.log10(stdplev)

            # interpol(alog_wv, alog_retp, alog_stdp, / NaN)
            logwvcd = scipy.interpolate.griddata(alog_retp, alog_wv, alog_stdp, method='linear')

            stdwv[iobs, :] = 10 ** logwvcd

        return stdwv

    # #Perform restructuring of the data points so the structure is easier to use.
    def restructurePoints(self, lats, lons):
        length = np.shape(lats)[0]
        points = np.zeros((length, 2))
        for i in range(length):
            points[i, 0] = lons[i]
            points[i, 1] = lats[i]
        return points

    # #Horizontally interpolate the data using the nearest neighbor method
    def horizontallyInter(self, points, variable, X, Y, mask):
        gridOut = scipy.interpolate.griddata(points, variable, (X, Y), method='nearest')
        gridOut[mask == 0] = np.nan
        return gridOut

    # #Generate a mask to mask out data not in the area of soundings.
    def generateMask(self, points, X, Y, threshold=1):
        mask = np.zeros((np.shape(X)))
        for point in points:
            lon = point[0]
            lat = point[1]
            distance = np.sqrt(np.power(X - lon, 2) + np.power(Y - lat, 2))

            mask[np.where(distance <= threshold)] = 1
        return mask

    # #Calculate the Boundary Layer Parameters.
    def get_BL_params(self, pres, psurf, nobs):
        botlev = np.zeros((nobs), dtype=float)
        blmult = np.zeros((nobs), dtype=float)

        for i in range(nobs):
            surflev = self.findSurface(pres, psurf[i]) - 1
            num = psurf[i] - pres[surflev - 1]
            denom = pres[surflev] - pres[surflev - 1]
            blmult[i] = num / denom
            botlev[i] = surflev
        return blmult, botlev

    # #Find the 2m Parameters such as temperature and water vapor
    def get_2m_param(self, sfcpres, temp, wvcd, botlev, blmult):
        shapes = np.shape(temp)
        nlev = shapes[1]
        nobs = shapes[0]
        temp_2m = np.ones((nobs), dtype=float) * FILL_VAL
        wv_2m = np.ones((nobs), dtype=float) * FILL_VAL
        for i in range(nobs):
            sfc = botlev[i]
            temp_2m[i] = temp[i, sfc - 1]
            wv_2m[i] = wvcd[i, sfc - 1]
        return temp_2m, wv_2m

    # Convert mixing ratio to relative humidity for a single value
    def convert_mr2rh_single(self, wvmr, pres, temp):
        svp = 6.112 * math.exp(17.67 * (temp - t_std) / (temp - 29.66))
        if (svp < pres / 2.0):
            mmr_s = 1000. * wv_eps * svp / (pres - svp)
        else:
            mmr_s = 1000. * wv_eps
        relhum = 100.0 * wvmr / mmr_s
        return relhum

    # Convert mixing ratio to relative humidity for sounding
    def convert_mr2rh(self, wvmr, pres, temp):
        nlev = np.shape(wvmr)[0]
        relhum = np.ones((nlev), dtype=float) * FILL_VAL
        for i in range(nlev):
            if (wvmr[i] != np.nan or temp[i] != np.nan):
                relhum[i] = self.convert_mr2rh_single(wvmr[i], pres[i], temp[i])
        return relhum

    # Convert mixing ratio to concentration
    def convert_mr2cd(self, botlev):
        pres = self.pressure[0, :]
        nlev = len(pres)
        nobs = len(self.latitude)
        wvcd = np.ones((nobs, nlev), dtype=float) * FILL_VAL
        ozcd = np.ones((nobs, nlev), dtype=float) * FILL_VAL
        delta_p = np.zeros((nlev), dtype=float)
        delta_p[0] = pres[0]
        delta_p[1:nlev] = pres[1:nlev ] - pres[0: nlev - 1]
        for i in range(nobs):
            wvmr = self.h2o_mr[i, :]
            ozmr = self.O3_MR[i, :]
            ozmr = ozmr * 1.0e-09 * oz_eps
            plev = nlev
            for j in range(plev):
                wvcd[i, j] = wvmr[j] * ((cdair * delta_p[j] / p_std) / wv_eps)
                ozcd[i, j] = ozmr[j] * ((cdair * delta_p[j] / p_std) / oz_eps)
        return wvcd, ozcd

        # #Calculate total ozone and water vapor in the column

    def calc_tot(self, wvcd, ozcd, botlev, blmult):
        shape = np.shape(wvcd)
        nlev = shape[1]
        nobs = shape[0]

        totwat = np.zeros((nobs), dtype=np.float)
        totoz = np.zeros((nobs), dtype=np.float)
        totDU = np.ones((nobs), dtype=np.float) * FILL_VAL
        pwlow = np.zeros((nobs), dtype=np.float)
        pwmid = np.zeros((nobs), dtype=np.float)
        pwhigh = np.zeros((nobs), dtype=np.float)

        for i in range(nobs):
            totwat[i] = wvcd[i, 0]
            totoz[i] = ozcd[i, 0]
            if botlev[i] < nlev:
                sfc = int(botlev[i])
            if botlev[i] == nlev:
                sfc = nlev
            for j in range(1, sfc):
                if math.isnan(wvcd[i, j]) == False:
                    totwat[i] = totwat[i] + wvcd[i, j]
                if math.isnan(ozcd[i, j]) == False:
                    totoz[i] = totoz[i] + ozcd[i, j]

            totwat[i] = totwat[i] + wvcd[i, int(botlev[i])] * blmult[i]
            totoz[i] = totoz[i] + ozcd[i, int(botlev[i])] * blmult[i]
            totDU[i] = totoz[i] * 1000.0 / nloschmidt
            totwat[i] = totwat[i] * mw_wv / navog

            for j in range(levlow, sfc):
                if math.isnan(wvcd[i, j]) == False:
                    pwlow[i] = pwlow[i] + wvcd[i, j]
            pwlow[i] = pwlow[i] + wvcd[i, int(botlev[i])] * blmult[i]
            pwlow[i] = pwlow[i] * mw_wv / navog

            for j in range(levmid, levlow):
                if math.isnan(wvcd[i, j]) == False:
                    pwmid[i] = pwmid[i] + wvcd[i, j]
            pwmid[i] = pwmid[i] * mw_wv / navog

            for j in range(levhigh, levmid):
                if math.isnan(wvcd[i, j]) == False:
                    pwhigh[i] = pwhigh[i] + wvcd[i, j]
            pwhigh[i] = pwhigh[i] * mw_wv / navog

        return totwat, totDU, pwlow, pwmid, pwhigh

    # Convert concentration to mixing ratio
    def convert_cd2mr(self, wvcd, pres, psurf, botlev):
        nlev = len(pres)
        shape = np.shape(wvcd)
        nobs = shape[0]
        wvmr = np.ones((nobs, nlev), dtype=float) * FILL_VAL

        deltap = np.zeros((nlev), dtype=float)
        deltap[0] = pres[0]
        deltap[1:nlev] = pres[1:nlev] - pres[0:nlev - 1]

        for i in range(nobs):
            sfc = int(botlev[i]) + 1
            for j in range(sfc):
                wvmr[i, j] = wvcd[i, j] / ((cdair * deltap[j] / p_std) / wv_eps)

        wvmr = wvmr * 1000.
        return wvmr

    # Simple conversion of concentration to mixing ratio
    def convert_cd2mr_simple(self, wvcd, deltap):
        wvmr = wvcd / ((cdair * deltap / p_std) / wv_eps)
        wvmr = wvmr * 1000.
        return wvmr

    # Load the Ozone Climate data. Data is from Emily Berndt at NASA SPoRT
    def loadOzoneClimoData(self):
        self.monthlyData = [
            [311, 350, 352, 340, 324, 297, 269, 253, 243, 246, 267, 279],
            [314, 363, 361, 359, 330, 300, 269, 252, 244, 251, 270, 281],
            [319, 363, 363, 359, 330, 301, 272, 254, 248, 254, 275, 287],
            [326, 359, 361, 359, 333, 303, 275, 262, 256, 259, 279, 293],
            [330, 359, 359, 355, 332, 304, 280, 269, 262, 265, 280, 299],
            [333, 359, 359, 352, 331, 306, 286, 274, 267, 269, 283, 306],
            [332, 355, 354, 347, 329, 307, 291, 277, 270, 270, 282, 309],
            [329, 349, 348, 340, 325, 306, 290, 276, 267, 267, 279, 308],
            [321, 338, 338, 331, 318, 299, 280, 269, 262, 260, 273, 302],
            [307, 320, 324, 318, 304, 286, 266, 259, 254, 252, 265, 289],
            [284, 292, 300, 298, 286, 271, 256, 252, 248, 243, 252, 269],
            [257, 262, 272, 275, 269, 259, 251, 248, 244, 237, 237, 247],
            [234, 238, 248, 255, 256, 252, 248, 246, 242, 234, 229, 230],
            [221, 224, 234, 242, 246, 246, 245, 244, 240, 232, 224, 219],
            [215, 218, 227, 235, 240, 242, 244, 245, 241, 232, 223, 215],
            [214, 216, 224, 232, 237, 240, 244, 245, 242, 232, 223, 214],
            [215, 217, 223, 230, 234, 237, 240, 242, 240, 231, 224, 216],
            [219, 221, 226, 231, 232, 233, 236, 238, 237, 229, 225, 219],
            [223, 224, 228, 231, 230, 229, 230, 233, 233, 228, 225, 222],
            [225, 226, 228, 229, 227, 224, 225, 228, 230, 228, 227, 225],
            [227, 227, 227, 227, 223, 221, 223, 226, 230, 229, 230, 228],
            [228, 226, 226, 225, 222, 222, 225, 228, 234, 234, 233, 230],
            [228, 225, 225, 225, 224, 226, 230, 235, 242, 241, 239, 232],
            [229, 226, 227, 228, 229, 234, 239, 248, 254, 251, 247, 237],
            [232, 230, 231, 233, 239, 249, 255, 267, 271, 267, 259, 243],
            [238, 235, 236, 240, 250, 265, 275, 288, 292, 287, 274, 252],
            [248, 242, 241, 247, 259, 276, 290, 303, 308, 304, 288, 263],
            [260, 250, 247, 255, 267, 282, 298, 310, 316, 313, 298, 273],
            [269, 257, 252, 259, 270, 282, 294, 305, 314, 313, 302, 280],
            [275, 262, 257, 262, 271, 278, 281, 284, 290, 299, 299, 283],
            [274, 264, 258, 261, 269, 273, 264, 247, 242, 271, 283, 280],
            [269, 261, 257, 256, 263, 264, 247, 211, 186, 233, 254, 273],
            [264, 257, 257, 250, 254, 253, 235, 198, 150, 196, 224, 267],
            [259, 253, 252, 242, 247, 244, 226, 196, 135, 173, 205, 260],
            [258, 252, 246, 237, 238, 237, 225, 202, 134, 147, 190, 256],
            [252, 246, 237, 230, 237, 236, 225, 204, 135, 142, 187, 252]
        ]
        # #Given a datetime extract the month

    def getMonth(self, time):
        timeofObservation = datetime.fromtimestamp(time / 1000., gmt)
        return int(timeofObservation.month) - 1

    # #Get the ozone climate value given a month and latitude
    def getOzoneClimoValue(self, monthIndex, latitude):
        # #Calculate the correct latitude bin
        index = -1 * int(latitude / 5) + 17
        if index < 0:
            index = 0
        if index > 35:
            index = 35
        return self.monthlyData[index][monthIndex]

    # #Find Ozone anomaly based on totalOzone and latitude of the observation points
    def calculateOzoneAnomaly(self, totalOzone):
        nobs = len(self.latitude)
        ozanom = np.ones((nobs), dtype=float) * FILL_VAL
        for i in range(nobs):
            month = self.getMonth(self.times[i])
            climoValue = self.getOzoneClimoValue(month, self.latitude[i])
            ozanom[i] = (totalOzone[i] / climoValue) * 100.0
        return ozanom

    # #Calculate Tropopause level
    def calculateTropLevel(self):
        nobs = len(self.latitude)
        nlev = len(self.pressure[0, :])

        troplevel = np.ones((nobs), dtype=float) * FILL_VAL
        for i in range(nobs):
            month = self.getMonth(self.times[i])
            ozthresh = 91. + 28. * (np.sin(np.pi * (int(month) - 2) / 6))
            for k in range(nlev):
                if self.O3_MR[i, k] != FILL_VAL:
                    if self.O3_MR[i, k] >= ozthresh:
                        troplevel[i] = self.pressure[i, k]
                    else:
                        troplevel[i] = troplevel[i]
                else:
                    troplevel[i] = FILL_VAL

        return troplevel
     # #Create coverage for the output grid
    def createCoverage(self):
        lo1 = regionCoverage[0]
        la1 = regionCoverage[1]
        lo2 = regionCoverage[2]
        la2 = regionCoverage[3]
        gridCoverage = LatLonGridCoverage()
        gridCoverage.setNx(nx)
        gridCoverage.setNy(ny)
        gridCoverage.setLa1(la1)
        gridCoverage.setLo1(lo1)
        gridCoverage.setLa2(la2)
        gridCoverage.setLo2(lo2)
        gridCoverage.setSpacingUnit("degree");
        dx = abs(lo1 - lo2) / nx
        dy = abs(la1 - la2) / ny
        gridCoverage.setDx(dx)
        gridCoverage.setDy(dy)
        gridCoverage.setFirstGridPointCorner(Corner.LowerLeft)
        gridCoverage.setName("nucaps-global")
        gridCoverage = GridCoverageLookup.getInstance().getCoverage(gridCoverage, True)
        return gridCoverage

    # #Create Grid Record    
    def createRecord(self, array, level, parameter, coverage, dataTime):
        record = GridRecord()

        # #Reshape the array
        shape = np.shape(array)
        nx = shape[1]
        ny = shape[0]
        numpyDataArray = np.reshape(array.astype(np.float32), (1, (nx * ny)))
        record.setMessageData(numpyDataArray)
        record.setLevel(level)

        record.setParameter(parameter)
        record.setDataTime(dataTime)

        record.setLocation(coverage)
        record.setDatasetId("griddednucaps")
        record.setSecondaryId(self.satelliteId)

        return record
    
    def grossBoundsChecking(self, min, max, grid):
        # Set missing value if the values outside the bounds.
        grid[grid > max] = F32_GRID_FILL_VALUE
        grid[grid < min] = F32_GRID_FILL_VALUE
        return grid
        
    # #Decode the data 
    def decode(self):
#         self.log.info('Decoding GriddedNucaps File : "%s"' % (self.files))
        records = []
        if self.toProcess == False:
            return records
        # #Turn off the numpy error temporarily
        npstderr = np.seterr(all='ignore')
        try:
            
            # ##Calculate the time of the image
            dataTime = DataTime(Date(self.mintime)) 
            coverage = self.createCoverage()
            points = self.restructurePoints(self.latitude, self.longitude)
            nlev = len(stdplev)
            psurf = self.surface_pressure
            nobs = len(self.latitude)
            temp = self.temperature

            plev = self.pressure[1, :]
            blmult, botlev = self.get_BL_params(plev, psurf, nobs)
            wvcd, ozcd = self.convert_mr2cd(botlev)

            totwat, totDU, pwlow, pwmid, pwhigh = self.calc_tot(wvcd, ozcd, botlev, blmult)

            stdwv = self.interpolate_gases(wvcd, botlev)
            stdT = self.interpolate_temperature(botlev, blmult)

            botlev = 0
            blmult = 0
            blmult, botlev = self.get_BL_params(stdplev, psurf, nobs)
            ####Flag this needs to be checked
            t2m, wvcd2m = self.get_2m_param(psurf, stdT, stdwv, botlev, blmult)

            rh2m = np.ones((nobs), dtype=float) * FILL_VAL

            for i in range(nobs - 1):
                deltap = psurf[i] - stdplev[int(botlev[i]) - 1]
                wvmr2m = self.convert_cd2mr_simple(wvcd2m[i], deltap)
                rh2m[i] = self.convert_mr2rh_single(wvmr2m, psurf[i], t2m[i])
            
            rh2m[np.where(rh2m > 100)] = 100.

            for i in range(nobs - 1):
                sfc = botlev[i] + 1
                if sfc < nlev - 1:
                    stdwv[i, sfc:nlev - 1] = FILL_VAL
                else:
                    stdwv[i, sfc] = FILL_VAL

            stdrelhum = np.ones((nobs, nlev), dtype=np.float) * FILL_VAL

            stdwvmr = self.convert_cd2mr(stdwv, np.asarray(stdplev, dtype=np.float), psurf, botlev)

            for i in range(nobs - 1):
                stdrelhum[i, :] = self.convert_mr2rh(stdwvmr[i, :], stdplev, stdT[i, :])

            Y, X = np.mgrid[regionCoverage[3]:regionCoverage[1]:ny_dim, regionCoverage[0]:regionCoverage[2]:nx_dim]
            
            mask = self.generateMask(points, X, Y, threshold=dist)

            level = LevelFactory.getInstance().getLevel("FHAG", 2, -999999., "m")
            parameter = Parameter("T", "Temperature", "K")
            temp2mGrid = self.horizontallyInter(points, t2m, X, Y, mask)
            temp2mGrid = self.grossBoundsChecking(-400, 400, temp2mGrid)
            records.append(self.createRecord(temp2mGrid, level, parameter, coverage, dataTime))

            parameter = Parameter("RH", "Relative Humidity", "%")
            rh2mGrid = self.horizontallyInter(points, rh2m, X, Y, mask)
            rh2mGrid = self.grossBoundsChecking(0, 110, rh2mGrid)
            records.append (self.createRecord(rh2mGrid, level, parameter, coverage, dataTime))

            level = LevelFactory.getInstance().getLevel("MB", bottom, low, "hPa")
            parameter = Parameter("PWAT", "Integrated Total Precipitable Water", "cm")
            pwlowGrid = self.horizontallyInter(points, pwlow, X, Y, mask)
            pwlowGrid = self.grossBoundsChecking(0, 1000, pwlowGrid)
            records.append(self.createRecord(pwlowGrid, level, parameter, coverage, dataTime))
            
            level = LevelFactory.getInstance().getLevel("MB", low, mid, "hPa")
            parameter = Parameter("PWAT", "Integrated Total Precipitable Water", "cm")
            pwmidGrid = self.horizontallyInter(points, pwmid, X, Y, mask)
            pwmidGrid = self.grossBoundsChecking(0, 1000, pwmidGrid)
            records.append(self.createRecord(pwmidGrid, level, parameter, coverage, dataTime))
            
            level = LevelFactory.getInstance().getLevel("MB", mid, high, "hPa")
            parameter = Parameter("PWAT", "Integrated Total Precipitable Water", "cm")
            pwhighGrid = self.horizontallyInter(points, pwhigh, X, Y, mask)
            pwhighGrid = self.grossBoundsChecking(0, 1000, pwhighGrid)
            records.append(self.createRecord(pwhighGrid, level, parameter, coverage, dataTime))
            
            level = LevelFactory.getInstance().getLevel("EA", 0, -999999., "")
            parameter = Parameter("PWAT", "Integrated Total Precipitable Water", "cm")
            totwatGrid = self.horizontallyInter(points, totwat, X, Y, mask)
            totwatGrid = self.grossBoundsChecking(0, 1000, totwatGrid)
            records.append(self.createRecord(totwatGrid, level, parameter, coverage, dataTime))

            parameter = Parameter("TOZNE", "Total Ozone", "du")
            totozGrid = self.horizontallyInter(points, totDU, X, Y, mask)
            totozGrid = self.grossBoundsChecking(100, 600, totozGrid)
            records.append(self.createRecord(totozGrid, level, parameter, coverage, dataTime))

            # #Calculate Ozone Anomaly

            o3anom = self.calculateOzoneAnomaly(totDU)
            # #Grid Ozone Anomaly
            ozAnomalyGrid = self.horizontallyInter(points, o3anom, X, Y, mask)
            ozAnomalyGrid = self.grossBoundsChecking(0, 200, ozAnomalyGrid)
            # #Create Record with Ozone Anomaly
            level = LevelFactory.getInstance().getLevel("EA", 0, -999999., "")
            parameter = Parameter("OZA", "Ozone Anomaly", "%")
            records.append(self.createRecord(ozAnomalyGrid, level, parameter, coverage, dataTime))

            # #Calculate Tropopause Height
            tropLevel = self.calculateTropLevel()
            # #Grid the Tropopause Height
            tropLevelGrid = self.horizontallyInter(points, tropLevel, X, Y, mask)
            tropLevelGrid = self.grossBoundsChecking(100, 600, tropLevelGrid)
            # #Create Record with Tropopause Height
            parameter = Parameter("TPH", "Tropopause Level", "mb")
            records.append(self.createRecord(tropLevelGrid, level, parameter, coverage, dataTime))

            qualityGrid = self.horizontallyInter(points, self.Quality_Flag, X, Y, mask)
            parameter = Parameter("QUAL", "Quality Flag", "")
            records.append(self.createRecord(qualityGrid, level, parameter, coverage, dataTime))

            for levelIndex in range(0, len(stdplev)):
                # self.log.info("Working on Level: "+str(stdplev[levelIndex])+"mb")
                level = LevelFactory.getInstance().getLevel("MB", stdplev[levelIndex], -999999. , "hPa")
                parameter = param = Parameter("T", "Temperature", "K")
                tempGrid = self.horizontallyInter(points, stdT[:, levelIndex], X, Y, mask)
                tempGrid = self.grossBoundsChecking(-400, 400, tempGrid)
                records.append(self.createRecord(tempGrid, level, parameter, coverage, dataTime))

                parameter = Parameter("RH", "Relative Humidity", "%")
                rhGrid = self.horizontallyInter(points, stdrelhum[:, levelIndex], X, Y, mask)
                rhGrid = self.grossBoundsChecking(0, 200, rhGrid)
                records.append(self.createRecord(rhGrid, level, parameter, coverage, dataTime))

        except ValueError as e:
            self.log.info('Issue GriddedNUCAPs: "%s"' % e)
        np.seterr(**npstderr)
        # self.log.info('done Decoding GriddedNUCAPs: "%s"' % (self.files))
        return records

