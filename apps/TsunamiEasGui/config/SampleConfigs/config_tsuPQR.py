# Tsunami EAS GUI
# Config Version 20210101
# NEED HELP?
# Ryan Kittell (LOX) 805.988.6618
# Tony Freeman and Jon Bonk (PQR) 503-326-2340 
# James Buchman (Office of Central Processing) 443-618-2594
# Credit to Bill Schneider, Original Co-developer (now retired)
# Notes:
#  - On initial installation, 
#    1) Copy this file into the TsunamiEasGui/etc directory
#    2) The config file sent with the installation package is is configured for WFO LOX
#       Edit the etc/configu_tsu.py file, and replace the LOCALIZED CONFIGURATION settings to your CWA
#
#  - If a line gets too long (like in zone_list), you can add a \ to the end 
#    of the line to allow a line break.

#  ========  DIRECTORY CONFIGURATION VARIABLES ===========
#  The variables below MAY NEED to be changed for your local office.  

#  Base directory for all project files. As of AWIPS 21.4.1, this is fixed as /awips2/apps/TsunamiEasGui.
local_dir="/awips2/apps/TsunamiEasGui"

#  Text database directory.  
db="/awips2/fxa/bin/textdb" 

# Switch to activate program's ability to send to NWR.  
# Comment out this variable to disable program from activating NWR. Good for testing purposes. 
# Set nwrsend to "activate" to turn on ability to send to NWR
# Program will now use built in script to send to NWR, no longer relies on transferNWR (as of version 20180510)
#nwrsend="activate"  # Uncomment to activate.

#  ========  LOCALIZED CONFIGURATION VARIABLES ===========
#  The variables below will LIKELY NEED to be changed for your local office.  

#  The first 3 letters of your local 9-letter awips products (like SFO in SFOSPSMTR)
#  West Coast Offices would be either: LAX -or- SFO -or- SEA
node="PDX"

#  Simple/General description of the ENTIRE coastal area for your CWA.
#  Used in the event that all of your coastal zones are covered in a Tsunami Hazard
#  Used to make the statement simpler and more concise that listing every zone
#  Set this to "" if you don't want this and want every zone listed in such a case
localphrase="SOUTH WASHINGTON COAST AND OREGONs NORTH AND CENTRAL COAST"

#  General actions for any tsunami statement. Added to the very end of the statement.
local_actions="Stay tuned to your local news source and NOAA weather radio for further information and updates."

# CRS/BMH PILS. What are the PILs in BMH that your office configured for each WWA type.
# ONLY CHANGE IF YOU NEED TO.  Changing will make parts of the installation instructions incorrect.
advisoryPIL="TSYWCA"
watchPIL="TSAWCA"
warningPIL="TSWWCA"

# ========  LOCALIZED CONFIGURATION ARRAYS/LISTS ===========
# Lists that LIKELY NEED to be changed for your local office.
# These Lists follow standard Python conventions, and act like an array in other languages
# Lists that use curly braces "{}" are dictionaries, and use an "index/key":"value" convention
# Lists that use square brackets "[]" are simple arrays that do not need an index/key

#  This is a list of 6 character zone codes for all the coastal zones in your CWA
#  ...along with the formal long name for each zone.  
#  The format for each entry is: "zone code":"zone long name",
#  Examples of the zones codes: CAZ041, ORZ001, WAZ002
zone_list={
"WAZ021":"SOUTH WASHINGTON COAST",
"ORZ001":"NORTH OREGON COAST",
"ORZ002":"CENTRAL COAST OF OREGON",
}


#  This list contains all the 6 character zone codes in the zone_list setting above
#  ...but in the order that you want them to appear in the statement. 
#  (needed because Python does not maintain order of dictionary lists).
zone_order=["WAZ021","ORZ001","ORZ002"]

# NEW in 2021
# Include full zone names instead of just zone codes in the GUI Zone portion
# True or False (no quotations)
include_zone_names=True

#  This is a list identifying which zones exist in each county.
#  Zones are identified by the same 6 character zone codes used in the zone_list setting above
#  Counties are identified by their 6 character FIPS code (CACNNN -or ORCNNN -or- WACNNN)
#  The format for each entry is: "county fips code":"zone code 1,zone code 2,...",
#  It is ok to have the same zone in more than one county, sometimes it happens that way
# Tsunami EAS for WA Zone 021 - South Washington Coast - Coastal and Inland Counties - Inland needed to trigger EAS at major media stations.
county_list={
"WAC027":"WAZ021", #Grays Harbor - affected
"WAC049":"WAZ021", #Pacific - affected
"ORC007":"ORZ001", #Clatsop - affected	     
"ORC009":"WAZ021,ORZ001", #Columbia - inland market (not sure if this is needed because there may not be any stations in Columbia county)
"ORC051":"WAZ021,ORZ001", #Multnomah - inland market	     
"ORC005":"WAZ021,ORZ001", #Clackamas - inland market
"ORC067":"WAZ021,ORZ001",#Washington - inaland market
"ORC057":"ORZ001", #Tillamook - affected
"ORC041":"ORZ001,ORZ002", #Lincoln - affected	     
"ORC071":"ORZ001", #Yamhill - inland market
"ORC039":"ORZ002", #Lane - affected and inland market
"ORC003":"ORZ001", #Benton - inland market
"ORC043":"ORZ001", #Linn - inland market
"ORC047":"ORZ001", #Marion - inland market
"ORC053":"ORZ001", #Polk - inland market
"ORC019":"ORZ002"  #Douglas - inland market
}

#county_list - ***** sorted by zone for reference - dont use directly because the software doesnt work this way******
#        # Tsunami EAS for WA Zone 021 - South Washington Coast - Coastal and Inland Counties - Inland needed to trigger EAS at major media stations.
#	     "WAC027":"WAZ021", #Grays Harbor - affected
#            "WAC049":"WAZ021", #Pacific - affected
#	     "ORC051":"WAZ021", #Multnomah - inland market	     
#	     "ORC005":"WAZ021", #Clackamas - inland market
#	     "ORC067":"WAZ021", #Washington - inaland market
#	     "ORC009":"WAZ021", #Columbia - inland market (not sure if this is needed because there may not be any stations in Columbia county)
#             #			     
#	     # Tsunami EAS for Oregon Zone 001 - North Oregon Coast - Coastal and Inland Counties - Inland needed to trigger EAS at major media stations.
#	     "ORC007":"ORZ001", #Clatsop - affected
#	     "ORC057":"ORZ001", #Tillamook - affected
#	     "ORC041":"ORZ001", #Lincoln - adjecent affected	     
#	     "ORC005":"ORZ001", #Clackamas - inland market
#	     "ORC051":"ORZ001", #Multnomah - inland market	     
#	     "ORC005":"ORZ001", #Clackamas - inland market
#	     "ORC067":"ORZ001", #Washington - inland market
#	     "ORC071":"ORZ001", #Yamhill - inland market
#	     "ORC009":"ORZ001", #Columbia - inaland market
#	     "ORC003":"ORZ001", #Benton - adjecent inland market
#	     "ORC043":"ORZ001", #Linn - adjecent inland market
#	     "ORC047":"ORZ001", #Marion - adjecent inland market or inland market??
#	     "ORC053":"ORZ001", #Polk - adjecent inland market or inland market??
#	     #
#	     # Tsunami EAS for Oregon Zone 002 - Central Oregon Coast - Coastal and Inland Counties - Inland needed to trigger EAS at major media stations.
#	     "ORC041":"ORZ002", #Lincoln - affected
#	     "ORC039":"ORZ002", #Lane - affected and inland market	     
#	     "ORC019":"ORZ002", #Douglas - adjecent inland market
#	     
#county_list={"WAC999":"WAZ021",
#	     "ORC999":"ORZ001,ORZ002",
#	     }


# NEW IN 2021 !!!!
# The arrival/forecast list is now configured by a list (config/tsunami_forecast_points.csv) managed by NTWC
#      and no longer needs to be locally configured
# The managed list can be locally modified however with the new format of arrival_list
# TO ADD A STATION/POINT to the default list: 
#   add an entry to the arrival_list like: "Point Name,State,ADD"
#   for example: arrival_list=["Craig,Alaska,ADD","Sleepytown,California,ADD"]
# TO IGNORE A STATION/POINT in the default list: 
#   add an entry to arrival_list like: "Point Name,State,IGNORE"
#   for example: arrival_list=["Craig,Alaska,IGNORE","Sleepytown,California,IGNORE"]
# arrival_list can have a combination of ADD and IGNORE entries.
# leave arrival_list blank if you do not need to change anything from the default
# arrival_list=["Craig,Alaska,ADD","Sleepytown,California,IGNORE"]
arrival_list=[]


#  ========  STANDARD CONFIGURATION VARIABLES ===========
#  The variables below should be standardized for all west coast offices.
#  In other words, you should not need to change any of these for your local office.

#  Advisory Handle: 
#   "yes" if you want the GUI to handle advisories.
#   "no"  if you want the GUI to only handle watches/warnings.
advisory_handle="yes"

# Open in Practice Mode Every Time?
#no:always open in live mode, unless AWIPS is in practice/test mode
#yes:always open in practice mode
openPractice="no"

# Do you want the statement to automatically send to radio, or go to pending section in the NWR Browser
pending="no" # no:Automatically Send | yes:Go to pending

#  Tsunami Message Product from Tsunami Warning Center with VTEC
#  (should be: TSUWCA for west coast/Alaska, TSUHWX for Hawaii, TSUGUM for Guam,
#              TSUCA1 for Caribbean, or TSUAT1 for east coast)
wcapil="TSUWCA"

#  Standard time UTZ offset in hours (omit +/- signs).
tz=8 # standard time utc offset (in hours, omit +/- signs)

#  Standard time ID (PST, AST ,etc)
tzid="pst" # standard time id (pst, ast)

#  Font sizes
fontsize=12 # for gui text
textsize=15  # formain text windows

#  Default number of minutes product should remain on NWR.
#  Note: this is configurable on the fly in the CRS Settings menu.
expiresin=60

# ======== That should do it. Hopefully that wasn't so bad! ==========
