# Tsunami EAS GUI
# Config Version 20210701
# Updated for inclusion in AWIPS baseline, starting with Release 21.4.1.
#
# NEED HELP?
# Ryan Kittell (LOX) 805.988.6618
# Tony Freeman and Jon Bonk (PQR) 503-326-2340 
# James Buchman (Office of Central Processing) 443-618-2594
# Credit to Bill Schneider, Original Co-developer (now retired)
# Notes:
#  - On initial installation, 
#    1) Copy this file into the TsunamiEasGui/etc directory
#    2) This file is configured for WFO LOX. Edit it, and replace the LOCALIZED CONFIGURATION settings to your CWA
#
#  - If a line gets too long (like in zone_list), you can add a \ to the end 
#    of the line to allow a line break.
#
#  ========  DIRECTORY CONFIGURATION VARIABLES ===========
#  The variables below MAY NEED to be changed for your local office.  
#
#  Base directory for the Tsunami EAS GUI application.
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
node="LAX"

#  Simple/General description of the ENTIRE coastal area for your CWA.
#  Used in the event that all of your coastal zones are covered in a Tsunami Hazard
#  Used to make the statement simpler and more concise that listing every zone
#  Set this to "" if you don't want this and want every zone listed in such a case
localphrase="coastal areas of los angeles, ventura, santa barbara, and san luis obispo counties"

#  General actions for any tsunami statement. Added to the very end of the statement.
local_actions="Stay tuned to your local news source and NOAA weather radio for further information and updates."

#
# BMH PILS. What are the PILs in BMH that your office configured for each WWA type.
# Should be changed for regions outside West Coat/Alaska.
#
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
"CAZ034":"San Luis Obispo County Central Coast",
"CAZ035":"Santa Barbara County Central Coast",
"CAZ039":"Santa Barbara County South Coast",
"CAZ041":"Los Angeles County Coast",
"CAZ040":"Ventura County Coast",
"CAZ087":"Catalina and Santa Barbara Islands",
"CAZ549":"San Miguel and Santa Rosa Islands",
"CAZ550":"Santa Cruz and Anacapa Islands"
}

#  This list contains all the 6 character zone codes in the zone_list setting above
#  ...but in the order that you want them to appear in the statement. 
#  (needed because Python does not maintain order of dictionary lists).
zone_order=["CAZ034","CAZ035","CAZ039","CAZ040","CAZ041","CAZ087","CAZ550","CAZ549"] 

# Include full zone names in addition to zone codes in the GUI Zone portion
# True or False (no quotations)
include_zone_names=True

#  This is a list identifying which zones exist in each county.
#  Zones are identified by the same 6 character zone codes used in the zone_list setting above
#  Counties are identified by their 6 character FIPS code (CACNNN -or ORCNNN -or- WACNNN)
#  The format for each entry is: "county fips code":"zone code 1,zone code 2,...",
#  It is ok to have the same zone in more than one county, sometimes it happens that way
county_list={
"CAC079":"CAZ034",
"CAC083":"CAZ035,CAZ039,CAZ549,CAZ550",
"CAC111":"CAZ040",
"CAC037":"CAZ041,CAZ087"
}

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

# ========== LOCAL FORECAST POINT NAMES ==========
# Allow the user to create aliases for the names in the Arrival Time list of the bulletin.
# E.G., if the WFO wants to substitute "Los Angelos" for point "San Pedro", add the following:
# arrival_localname = { "SAN PEDRO CALIFORNIA":"LOS ANGELES CALIFORNIA" }
# (must be all uppercase with state; no commas)
#
arrival_localname = { }

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
