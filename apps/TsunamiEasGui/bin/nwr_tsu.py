#!/awips2/python/bin/python3

# Tsunami Radio Sender
# Ryan Kittell
# WFO Los Angeles/Oxnard (LOX)
# 805.988.6618 (unlisted)
Version="20220331"

#"BIG COMMAND search for 'nwrsend'. Comment nwrsend line in config to hard turn off"

# 20220331 - Ryan
#  
#  Fixed small bug in daylight savings time function (started one day too late)
# 20211013 - J. Buchman
#  Also get the time that the bulletin was added to the AWIPS Text database, and log it.
# 20211006 - J. Buchman
#  No error if chmod() on a temporary file fails.
# 20210915 - J. Buchman
#  Updated with second round of Gerrit code review comments.
# 20210910 - J. Buchman
#  Per Dave G's recommendation, replaced logMe() with standard Python logging module.
# 20210901 - Ryan, Jim
#  Applied most of recommendations from code review for AWIPS 21.4.1 version.
# 20210728 - J. Buchman
#  Add "arrival_localname{}" lookup to let the WFO configure local aliases for the
#  forecast point names in config/tsunami_forecast_points.csv.
# 20210701 - Ryan
#  Fixed zone listing bug in text window (elipses and commas dispersed)
#  Improved zone name button display in gui
#  Added guifont and text window font configurations
#  Added dynamic main textwindow height sizing based on screen resolution, so bottom buttons do not get chopped
# 20210617 - J. Buchman
#  Add option to print zone names with the UGC codes in the button panel.
#  Make text box configurable based on font size.
# 20210507 - J. Buchman
#  Handle the American Samoa bulletin, whose format is the same as Guam's.
# 20210326 - J. Buchman
#  Handle a second way that Hawaii arrival times can be formatted.
# 20210226 - Ryan
#  Added option to pass wfo id in command line to test different config file
# 20210121 - J. Buchman
#  Changed names of printCRS/previewCRS functions to printBMH/previewBMH
# 20210114 - Ryan
#  Created dummy modecheck.sh for awips cloud version
#  Change function of arrival_list from replacing default to augmenting
#  Updated config_tsu.py and cleaned up.
# 20210105 - J. Buchman
#  Move the Forecast Points file from etc/ to config/, since it is the same for all WFOs.
# 20201216 - J. Buchman
#  If list of forecast warning points is not defined in config_tsu.py, read the forecast
#  points file "tsunami_forecast_points.csv", and extract points based on the list of zones.
# 20201210 - J. Buchman
#  Updated grabTWC() to properly parse preliminary earthquake parameters from
#  Hawaii and Guam bulletins.
# 20201105 - J. Buchman
#  Ran Python conversion tool 2to3 on Ryan's source code, which applied several updates.
#  Ran reindent.py script to adjust indentation.
#  Ran another script to flag possible integer division; replaced one instance
#  with // operator (integer "floor" division).
# 20180919
#  Fixed template bug when AAAAAA. \n is used. Replaced search of ". " to "." near line 1107
# 20180601 - MAJOR
#  Further implimented nwrsend.sh as default process to push to NWR
#  Added cancellation template handling. Added cancellation and imminent option menus.
#  Add logs for sending, practicing, and troubleshooting
#  Renamed config_tsu.py template
#  Fixed 17.2.1 extended screen issue of windows popping everywhere
#  Removed auto test word removal when switching to Live mode
#  Added note of opening NWR Browser to complete if using pending option
#  Fixed TIME TIME issue
#  Minor bug issues (node handling with BMH pils, change lingering references to WCATWC, and others)
# 20180426
#  Added support file nwrsend.sh to replace transferNWR dependance in case baseline functionality goes away (17.2.1?)-tony.freeman@noaa.gov
# 20170919
#  Fixed bug for breakpointMe with empty key
#  Added more local option to add templates. Solve for EKA imminent warning scenario
#  Replaced CRS with BMH wording in GUI
# 20160704
#  Modified checks for existance of configuration file and notifications for syntax errors
# 20160511 - MAJOR
#  Made subvserion compatible:
#    etc/config_tsu.py required, templates->etc/, created examples/,  temp files->data/
#    Able to handle mixed case NTWC statements (in the future)
#  Add "Test Statement" option when a test NTWC message is detected
#  Dependable awips mode check - drive gui mode decisions.  Test mode added.
#  Add support for most US timezones
#  Fix another bug in local area phrase. Combo options do not handle local phrase correctly
# 20160214
#  5 second Countdown Timer after sending
#  Fixed workstation practice mode test - forces into practice mode
# 20151023
#  Fix SEW discovered bug in local area phrase initiation
# 20150917a
#  Minor bug fix for extra lines added at the bottom of statement when zones selected
#  Minor bug fix for extra '.' at end of statement
# 20150917
#  Band Aid fix for bad characters in NTWC statements for arrival times
#  Modified some of the error messages
# 20150825
#  Added Count down timer after sending to radio
# 20150414
#  Add editable template files
#  Work with test VTEC from NTWC (I think it works)
#  Add option for send to pending directory
#  Add AWIPS practice/test mode check???
#  Configurable CRS Pils, locked zones
#  Open in practice mode option
# 20150401
#  Fix info array bug by declaring info at top of program, and "issue" check in 346, and remove info={} in 750
# 20150313 - intermediate fix.  should be good but less precise than 0401 fix
# 20150312
#  iSpell may not be installed on the local machine
#  Fixed quirk when going from all zones selected to only a few. All zones phrase stayed in area description
#  Fixed window appearance issues for RH6. All windows are now relative and not fixed.
# 20141011
#  Made configuration file relative to main path, regardless of where file is called (Bill)
#  Switched autofill button to view product on the front of the GUI
#  Allowed View product to look at different versions on the fly and load into GUI
#  Made AWIPS 2 compatable

# ISSUES TO FIX LATER
#  Arrival times assume same time zone for all stations

import os, re, string, textwrap, subprocess, sys
import getpass
import datetime, time
import importlib
from tkinter import *
import tkinter,tkinter.font # need to import twice. (Here for the toop tip)

# Use Python standard logging module.
import logging

RunPath=os.path.dirname(__file__)
os.chdir(RunPath)
BasePath=os.path.realpath('../')
print("TsunamiEasGui Version "+Version)

#
# Initialize logging parameters. Messages will be logged to a dated file in
# the local subdirectory log/.
#
logPath = os.path.join(BasePath,"log")
os.makedirs(logPath, mode=0o775, exist_ok=True)
today = datetime.date.today()
logFileName = os.path.join(logPath, "TsuGuiLog-{0}.{1}".format(getpass.getuser(), today.strftime("%Y%m%d")))
try:
    logging.basicConfig(filename=logFileName, level=logging.INFO, format="%(asctime)s:%(levelname)s-%(message)s")
    logging.info("TsunamiEasGui started - Version " + Version)
except:
    print("ERROR - Could not open log file {0}!".format(logFileName))

#
# See if any variables were passed from command line. If the WFO was specified,
# use file config_tsuXXX.py instead of config_tsu.py, where XXX is the WFO ID.
# (These files are used for testing, and reside in the config/ subdirectory
# rather than etc/).
#
inwfo = ""
ConfigPath  = BasePath + "/etc"
ConfigFName = "config_tsu.py"

# Check whether the WFO ID was specified on the command line for testing
# If so, adjust the file name and subdirectory.
invars = sys.argv
if len(invars)>1 and len(invars[1])==3:
    inwfo = invars[1].upper()
    # print ("Using configuration for WFO " + inwfo)
    ConfigPath  = os.path.join(BasePath, "config", "SampleConfigs")
    ConfigFName = "config_tsu" + inwfo +".py" # wfo id passed

ConfigFileName = os.path.join(ConfigPath, ConfigFName)

sys.path.append(ConfigPath)

### WRS 20160703 - added code
### First check for existence of config_tsu.py file in the programs "etc/" directory
### If config file is not found send an error message
### Check that config file is readable
### Try reading the config file. If the python interperter returns an error
### let user know there is a python syntax error in the config file.

if os.path.isfile(ConfigFileName):
    logging.debug("Reading configuration file {0}".format(ConfigFileName))
else:
    logging.error("{0} file does not exist. You need to copy the config_tsu.template.py file from the programs config/ directory to the etc/ directory, then rename to config_tsu.py, then edit and configure for your site before running this program.".format(ConfigFileName))
    sys.exit(1)

if os.access(ConfigFileName, os.R_OK) == False:
    logging.error("{0} file exists but is not readable.".format(ConfigFileName))
    sys.exit(1)

# Do the import 
try: 
    ConfigFName = ConfigFName.replace(".py","") # Config file name, without directory.
    mdl=importlib.import_module(ConfigFName)
    names = [x for x in mdl.__dict__ if not x.startswith("_")]
    # now drag them in
    globals().update({k: getattr(mdl, k) for k in names})
except:
    logging.exception("Syntax error in configuration file {0}. Please edit and correct\n".format(ConfigFileName))
    sys.exit(1)

######################
# End config import
######################

# Activate GUI IF READY.
try:    nwrsend
except: nwrsend="" # variable for main radio script location was not specified in config file.

if nwrsend=="": Version+=" - SEND TO RADIO DISABLED!!! UPDATE NWRSEND VARIABLE IN CONFIG FILE"
else: nwrsend=local_dir+"/bin/nwrsend.sh" # hard code to use send script included in tsunami package instead of baseline transferNWR

# Grid Padding
global gp
gp=10 # default grid padding in window

#
# Read the forecast arrival points file, and extract those points which fall
# into zones specified for this WFO.
# 
def readArrivalList(filePath, zoneOut):
    arrFileName = os.path.join(filePath, "tsunami_forecast_points.csv")
    if os.path.isfile(arrFileName):
        logging.info("Reading forecast points file {0}".format(arrFileName))
    else:
        logging.error("{0} - file does not exist! No forecast points will be included in the tsunami message.".format(arrFileName))
        return []
    if os.access(arrFileName, os.R_OK) == False:
        logging.error("{0} - file exists but is not readable. No forecast points will be included in the tsunami message.".format(arrFileName))
        return []

    #
    # Read each line in the Comma Separated Variable file, and break fields at the commas.
    #
    try:
        with open(arrFileName, "r") as arrFile:
            lines = arrFile.readlines()
    except:
        logging.error("An error occurred reading from file {0}\nNo forecast points will be included in the tsunami message.".format(arrFileName))
        return []

    forcPoints = []
    for line in lines:
        fields = line.rstrip().split(",")
 
        # Each line contains location, state, and zone. If the zone is on the list of zones of interest,
        # add this forecast point to the list.

        if len(fields) > 2 and len(fields[0]) > 0 and len(fields[1]) > 0 and len(fields[2]) > 0:
            if fields[2] in zoneOut:
                # print("   Found zone " + fields[2])
                forcPoints.append(fields[0] + " " + fields[1])

    return forcPoints

# Daylight Savings Time Fix - dt = datetime object
def daylightMe(dt): 
    y=dt.year
    m=dt.month
    d=dt.day
    h=dt.hour

    if m>=4 and m<=10: return 1 #PDT
    elif m==12 or m<=2: return 0 #PST
    # left with March and November.  Find target date (Mar:2nd Sunday, Nov:1st Sunday)
    target_sunday=1
    if m==3: target_sunday=2
    compare=int(str(y)+str(m).zfill(2)+str(d).zfill(2)) # todays datecode
    aa=datetime.datetime(y,m,1,h,0)
    target=16
    count=0
    for d in range(0,16):
        b=aa+datetime.timedelta(hours=d*24)
        if b.weekday()==6:
            count=count+1 # Sunday
            if count>=target_sunday:
                target=int(str(b.year)+str(b.month).zfill(2)+str(b.day).zfill(2)) #hit!
                break
    if m==3 and compare>=target: return 1 #Mar2022 (change > to >=)
    if m==11 and compare<target: return 1
    return 0

#
# NEW FOR 2021!
# If the list of arrival points is not defined in the config file, construct
# it by looking up the locations in the Forecast Points file, based on the
# zones for this WFO.
#
try: local_list = arrival_list # first check if it exists from config_tsu.py
except: local_list=[] # it does not
arrival_list = [] 

# Process the default arrival list. The file is the same for all WFOs, so it is not in .../etc.
pointsPath = BasePath + '/config'
arrival_list = readArrivalList(pointsPath, zone_order) # function above

# Process locally configured arrival_list augmentations
for a in local_list:
    if a.find(",")==-1: continue # legacy arrival_list
    tmp=a.split(",")
    if len(tmp)<2: continue # not formatted correctly
    into=tmp[0]+" "+tmp[1]
    if tmp[2].upper()=="ADD" and into not in arrival_list: arrival_list.append(into)
    if tmp[2].upper()=="IGNORE" and into in arrival_list: arrival_list.remove(into)
#print("Arrival list: ", arrival_list)


# ======== Autoconfig
zone_ids=zone_order
helpfont=str(fontsize-2)
impactsfont=str(fontsize-3)
windowfont=str(fontsize+2)
fontsize=str(fontsize)

# Universal Variables
global modeme,modeme_options,combo_need,combo_issued,still_need,optset,active,spell
combo_need=[] # to prompt user at end
still_need=[]
combo_issued=[]
optset={}
active="" # hazards that are active
spell={}

# Fix Items from Config
if local_dir[-1:]!="/": local_dir=local_dir+"/" # in case we forgot the ending slash
teststatementdir=local_dir+"examples/"
combo_handle="no" # used to be an option, just need to disable it

# Pil
global wcapil # may change if user wants to
wcapil_raw=wcapil # store for later if needed
if len(wcapil)<9: wcapil=node+wcapil
tmp=wcapil.split(":")
if len(tmp)>1: wcapil=tmp[0]+":"+node+tmp[1]
wcapil=wcapil.upper()

# Backwards Compatible with older Versions.
# New config items added since main release
try: test=local_actions # canned local actions section
except: local_actions=""
try: test=pending # go to pending in NWR Browser 201504014
except: pending="no"
try: test=advisoryPIL # BMH PIL for advisory
except: advisoryPIL="TSYWCA"
try: test=watchPIL # BMH PIL for watch
except: watchPIL="TSAWCA"
try: test=warningPIL # BMH PIL for warning
except: warningPIL="TSWWCA"
try: test=textsize
except: textsize=fontsize
breakpoint_list={} # empty list to turn off old feature 20210101

# Handle Modes From the Start
try: test=openPractice # open always in practice mode
except: openPractice="no"

# Is AWIPS Practice or Live Mode?
def checkAwipsMode(what=""):
    modeout="live" # default
    ###return modeout # UNCOMMENT TO TURN OFF FOR AWIPS CLOUD. 
  # Version 1
    #try:
    #   findme="'showBanner practice'"  #showBanner test works for test mode
    #   a=os.popen("ps -efw | grep "+findme).read()
    #   b=a.strip().splitlines()
    #   c=[]
    #   for bb in b:
    #      if bb.find("grep")==-1: c.append(bb)
    #   if len(c)>0: modeme="practice"
    #   cmd=db+" -r NCFTSTNCF" # test message from NCF that all offices should receive
    #   test=os.popen(cmd).read()
    #   if test=="": # product not found, must be in practice mode
    #      modeme="practice"
    #except: a=1
  # Version 2
    #shellout=os.popen("getTestMode ; ${?}").read()
    shellout=os.popen(local_dir+"bin/modecheck.sh").read().strip().lower()
    if shellout=="test" or shellout=="practice": modeout=shellout # otherwise default to live.
    # Toggle options
    global modeme_options
    modeme_options=["live","practice"] # determines toggle options (global)
    if modeout=="test": modeme_options=["test","practice"]
    if modeout=="practice": modeme_options=["practice"]
    # spit it out already
    return modeout

# Set Mode - live/pracitce/test
modeme=checkAwipsMode() # defaults to live (global)

# Hard set to practice mode automatically at start if wfo chooses
if openPractice=="yes": modeme="practice"

# Date Setup
global todayutc,todaylocal,dst
tz_list=[tzid]
tz_list.append(tzid.replace("st","dt"))
todayutc=datetime.datetime.utcnow() # UTC time (awips already default to UTC)
todaylocal=todayutc
tzoff=datetime.timedelta(hours=tz)  # Create timezone offset, tz from config_tsu
todaylocal=todayutc-tzoff           # create local time
dst=daylightMe(todaylocal)          # are we in daylight savings time?
todaylocal=todaylocal+datetime.timedelta(hours=dst) # fix for daylight savings time (1:PDT 0:PST)
tzset=tz_list[dst]
mon_long=["","JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"]
mon_longer=["","JANUARY","FEBRUARY","MARCH","APRIL","MAY","JUNE","JULY","AUGUST","SEPTEMBER","OCTOBER","NOVEMBER","DECEMBER"]
wkdy={"Sun":"Sunday","Mon":"Monday","Tue":"Tuesday","Wed":"Wednesday","Thu":"Thursday","Fri":"Friday","Sat":"Saturday"}
tz_offlist={"ALASKAN":9,"ALASKA":9,"PACIFIC":8,"MOUNTAIN":7,"CENTRAL":6,"EASTERN":5,"ATLANTIC":4}
tz_long={"pst":"PACIFIC","akst":"ALASKA","mst":"MOUNTAIN","cst":"CENTRAL","est":"EASTERN","ast":"ATLANTIC","hst":"HAWAII","chst":"CHAMORRO"}

# Phrasing
localheadline="for "+localphrase
if localphrase.replace(" ","")=="":
    localheadline=""
    localphrase="the local coastal area"
else: localphrase=localphrase.replace("...",", ")

# Universal Variables
menu_list={} # store all menu instances
entry_list={} # store all the entry variables
skip_list={} # Entries Left All Blank...skip section in output
help_list={} # Where help box info is stored
global info
info={"warning":[],"watch":[],"advisory":[]} # main array of information

# Set window placement based on where main window is
def windowXgeo(xx=0,yy=0):
    #x = root.winfo_rootx() + root.winfo_width() + 3
    #y = root.winfo_rooty() + root.winfo_height() + 3
    x = root.winfo_rootx()+50
    y = root.winfo_rooty()+50
    return "+"+str(x+xx)+"+"+str(y+yy)

# Simple Help Dialog
def viewHelp():
    destroyBoxes() # kill all other popups
    # Text for Help
    helpFile = os.path.join(local_dir, "docs", "help.txt")
    try:
        with open(helpFile, "r") as helpMe:
            t = helpMe.read()
    except:
        logging.error("Unable to open file {0}".format(helpFile))
        t = "There was an error opening the HELP file {0}".format(helpFile)

    # Display the help text.
    bg="#ff0"
    alertframe=Toplevel()
    alertframe.title('Tsunami EAS GUI Helper')
    alertframe.option_add('*Font', ('Arial',10))
    alertframe.geometry(windowXgeo())
    Button(alertframe,text="   Close   ",command=alertframe.destroy,bg="#000",fg="#fff").grid(row=0,column=0,ipadx=5,ipady=5)
    text=Text(alertframe,height=34,width=72,bg="#004",fg="#fff",font=('Courier','12'),wrap=WORD)
    scroll=Scrollbar(alertframe,command=text.yview)
    text.configure(yscrollcommand=scroll.set)
    text.grid(row=1,column=0)
    scroll.grid(row=1,column=1,sticky=N+S+E)
    text.delete(0.0,END)
    text.insert(0.0,t)

# ToolTip For Help - http://tkinter.unpy.net/wiki/ToolTip
class ToolTip:
    def __init__(self, master, text='Your text here', delay=100, **opts):
        self.master = master
        self._opts = {'anchor':'center', 'bd':1, 'bg':'lightyellow', 'delay':delay, 'fg':'black',\
                      'follow_mouse':0, 'font':tfont, 'justify':'left', 'padx':4, 'pady':2,\
                      'relief':'solid', 'state':'normal', 'text':text, 'textvariable':None,\
                      'width':0, 'wraplength':500}
        self.configure(**opts)
        self._tipwindow = None
        self._id = None
        self._id1 = self.master.bind("<Enter>", self.enter, '+')
        self._id2 = self.master.bind("<Leave>", self.leave, '+')
        self._id3 = self.master.bind("<ButtonPress>", self.leave, '+')
        self._follow_mouse = 0
        if self._opts['follow_mouse']:
            self._id4 = self.master.bind("<Motion>", self.motion, '+')
            self._follow_mouse = 1

    def configure(self, **opts):
        for key in opts:
            if key in self._opts: self._opts[key] = opts[key]
            else:
                KeyError = 'KeyError: Unknown option: "%s"' %key
                raise KeyError

    def enter(self, event=None):
        self._schedule()

    def leave(self, event=None):
        self._unschedule()
        self._hide()

    def motion(self, event=None):
        if self._tipwindow and self._follow_mouse:
            x, y = self.coords()
            self._tipwindow.wm_geometry("+%d+%d" % (x, y))

    def _schedule(self):
        self._unschedule()
        if self._opts['state'] == 'disabled': return
        self._id = self.master.after(self._opts['delay'], self._show)

    def _unschedule(self):
        id = self._id
        self._id = None
        if id: self.master.after_cancel(id)

    def _show(self):
        if self._opts['state'] == 'disabled':
            self._unschedule()
            return
        if not self._tipwindow:
            self._tipwindow = tw = tkinter.Toplevel(self.master)
            tw.withdraw()
            tw.wm_overrideredirect(1)
            if tw.tk.call("tk", "windowingsystem") == 'aqua':
                tw.tk.call("::tk::unsupported::MacWindowStyle", "style", tw._w, "help", "none")
            self.create_contents()
            tw.update_idletasks()
            x, y = self.coords()
            tw.wm_geometry("+%d+%d" % (x, y))
            tw.deiconify()

    def _hide(self):
        tw = self._tipwindow
        self._tipwindow = None
        if tw: tw.destroy()

    def coords(self):
        tw = self._tipwindow
        twx, twy = tw.winfo_reqwidth(), tw.winfo_reqheight()
        w, h = tw.winfo_screenwidth(), tw.winfo_screenheight()
        if self._follow_mouse:
            y = tw.winfo_pointery() + 20
            if y + twy > h: y = y - twy - 30
        else:
            y = self.master.winfo_rooty() + self.master.winfo_height() + 3
            if y + twy > h: y = self.master.winfo_rooty() - twy - 3
        x = tw.winfo_pointerx() - twx // 2    # Using integer division for Python 3 compatibility.
        if x < 0: x = 0
        elif x + twx > w: x = w - twx
        return x, y

    def create_contents(self):
        opts = self._opts.copy()
        for opt in ('delay', 'follow_mouse', 'state'):
            del opts[opt]
        label = tkinter.Label(self._tipwindow, **opts)
        label.pack()

# Make nice number date
def niceFirst(a):
    nicefirst=["Zeroth","First","Second","Third","Fourth","Fifth","Sixth","Seventh","Eighth","Nine","Ten","Eleventh","Twelfth","Thirteenth","Fourteenth","Fifteenth","Sixteenth","Seventeenth","Eighteenth","Nineteenth","Twentieth","Twenty First","Twenty Second","Twenty Third","Twenty Fourth","Twenty Fifth","Twenty Sixth","Twenty Seventh","Twenty Eighth","Twenty Nine","Thirtieth","Thirty First"]
    #print nicefirst[int(a)]
    try: return nicefirst[int(a)]
    except: return str(a)

# Print Radio Script
def printRadio():
    global printout
    pout=textwrap.fill(radiotext.get(0.0,END).replace("...",", "),42)
    pout="Initial Tsunami Statement Radio Script\n\n"+pout
    with open(local_dir+"data/print.txt","w") as pp:
        pp.write(pout)  # write to temporary file
    pipe=subprocess.getoutput("a2ps "+local_dir+"data/print.txt -R -B -1 -f 20") # use commands to remove a2ps output

# Print Radio Script
def printBMH():
    pipe=subprocess.getoutput("a2ps "+local_dir+"data/tsradio.txt -r -B -1") # use commands to remove a2ps output

# =============== FILL TEXT FUNCTIONS ==================
def hazDefMe(a):
    hazdef=""
    a=str(a)
#   if a.find("warning")!=-1: hazdef+=" TSUNAMI WARNINGS MEAN THAT A TSUNAMI WITH SIGNIFICANT INUNDATION IS EXPECTED OR IS \
#ALREADY OCCURRING."
#   if a.find("watch")!=-1: hazdef+=" TSUNAMI WATCHES ARE AN ADVANCE NOTICE TO AREAS THAT COULD BE IMPACTED BY A TSUNAMI AT \
#A LATER TIME."
#   if a.find("advisory")!=-1: hazdef+=" TSUNAMI ADVISORIES MEAN THAT A TSUNAMI CAPABLE OF PRODUCING STRONG CURRENTS OR \
#WAVES DANGEROUS TO PERSONS IN OR VERY NEAR THE WATER IS EXPECTED OR IS ALREADY OCCURRING. AREAS IN THE ADVISORY SHOULD NOT \
#EXPECT WIDESPREAD INUNDATION."
    if a.find("warning")!=-1: hazdef+=" IF YOU ARE LOCATED IN THIS COASTAL AREA, MOVE INLAND TO HIGHER GROUND."
    if a.find("watch")!=-1: hazdef+=" IF YOU ARE LOCATED IN THIS COASTAL AREA, STAY ALERT FOR FURTHER UPDATES."
    if a.find("advisory")!=-1: hazdef+=" IF YOU ARE LOCATED IN THIS COASTAL AREA, MOVE OFF THE BEACH AND OUT OF HARBORS."
    return hazdef

# autofill from different version. used in viewTsu
def autoFillPop(pil):
    global wcapil,mainLabels
    wcapil=pil
    wcapil_display=wcapil.replace(".txt","")
    mainLabels["bt_viewStatement"].configure(text=" View "+wcapil_display.replace(teststatementdir,"")+" Statement")
    autoFill()

# create text statement from a TSU source (either active or example)
def autoFill(combowhat=""):
    global info,bt_zones,wcapil,crscode,radiotext,alertframe,alertframe1,active,combo_need,still_need,masterTone
    saveMe() # autosave
    destroyBoxes()
    masterTone=1
    for z in zone_ids: bt_zones[z].set(0) # clear out zone buttons
  # Reset
    if combowhat=="":
        combo_need=[]
        still_need=[]
        active=""
    #print "autofill1",info
    info=grabTWC(wcapil,zone_ids) #dir,dist,mag,landmark,daytime,[arrivals],warning,watch
    #print "autofill2",info
    radiotext.delete(0.0,END) # clear out text
    # for test wording
    if len(info)==0 or (len(info)==1 and info["extra"]!=""): # There was a serious error!
        msg="The "+wcapil+" product was not parsed correctly, many apologies."
        if info["extra"]!="": msg=info["extra"]
        flagMeBasic(msg+".\nView the TSU statement from the black button at the top right and try another \
  version, or use one of the options from the Templates menu. code:autoFillONE")
        return # abort!
    testmode=0
    if info["extra"]!="": # There was a less serious error with a notice
        if info["extra"].find("appears to be a test")!=-1: # special case for test message
            if combowhat!="test": # from first loading a statement
                flagMeBasic(info["extra"],"What do you want to do?","Load with Test Wording") # prompt user for reply
                return # stop for the moment
            else: # user selected to load into test mode (from flagmebasic just above)
                testmode=1
                combowhat="" # use combowhat as a holder to tell that user bypassed cancel
        else: flagMeBasic(info["extra"]) # There was a less serious error with a notice
    #print info,zone_ids
    # Determine Scenario
    dowhat=""
    if combowhat!="" and combowhat!="all": dowhat=combowhat # single hazard assigned from combo option
    elif len(info["warning"])==0 and len(info["watch"])==0 and len(info["advisory"])==0: dowhat=""
    elif advisory_handle=="no": # Ignoring Advisories. One less thing to worry about
        if len(info["warning"])==0 and len(info["watch"])==0: dowhat=""
        elif len(info["warning"])==0: dowhat="watch"
        elif len(info["watch"])==0: dowhat="warning"
        else:
            if combowhat=="all": dowhat="warning_watch" # user already chose combo, proceed
            else:
                comboOption("warning_watch") # must be a warning/watch combo. Ask user how to proceed
                return
    elif len(info["warning"])==0 and len(info["watch"])==0: dowhat="advisory"
    elif len(info["warning"])==0 and len(info["advisory"])==0: dowhat="watch"
    elif len(info["watch"])==0 and len(info["advisory"])==0: dowhat="warning"
    else: # Must be more than one!
        if len(info["warning"])==0: dowhat="watch_advisory"
        elif len(info["watch"])==0: dowhat="warning_advisory"
        elif len(info["advisory"])==0: dowhat="warning_watch"
        else: dowhat="warning_watch_advisory" # dreaded triple combo. No solution for that!
        if combowhat!="all":
            comboOption(dowhat) # must be a combo. Ask user how to proceed
            return
        # otherwise user already chose combo, proced
# Determine area description
    breakpoints=breakpointMe(info,dowhat) #advisory, warning, watch (string)
    #print breakpoints
# Create definitions statement
    hazdef=hazDefMe(dowhat)
    active=dowhat
# Handle different scenarios
    if dowhat=="":
        out=""
        continuebutton=1 # turn on option to continue
        # advisory handle turned off
        if len(info["advisory"])>0: out="While an advisory was found, no warnings or watches were detected in your area."
        elif "issue" in info and len(info["issue"])==1 and info["issue"][0]=="final": #20150401 fix
            out="It appears that there was a cancellation in the most recent bulletin. "
        else:
            out="No tsunami hazards were detected in your area. "
            continuebutton=0 # turn off option to continue
        if cancelflg==1: out+="If you want to issue a cancellation message, please use one of the options from the Cancelations menu. Remember to manually delete the previous BMH Tsunami statement if still active."
        out+="\n\nView TSU statement from the black button at the top right and load another version, or use one of the options from the Templates menu."
        flagMeBasic(out,"What do you want to do?",continuebutton)
        return #abort
  # Determine if this is a simple single warning case
    elif dowhat.find("_")==-1: #no combo found, simple case:
        wwa=dowhat
        out=statementMake(wwa)  ### AAAAAA:Area Description, NNNNNN:WWA Call2Action, EEEEEE:Earthquake, LLLLLL:Arrival Times, SSSSSS:Local Actions
        # earthquake info
        if "eseg" in info and info["eseg"]!="": out=out.replace("EEEEEE",info["eseg"])
        else: out=out.replace("EEEEEE.","").replace("EEEEEE","") # not available, skip
        # Area Desciption
        local=localphrase # from
        if wwa in breakpoints and breakpoints[wwa]!="": local="following locations, "+breakpoints[wwa] #Mar2022
        out=out.replace("AAAAAA","for the "+local)
        # Call to Action
        if hazdef!="": out=out.replace("NNNNNN",hazdef)
        else: out=out.replace("NNNNNN.","XXXXXX").replace("NNNNNN","XXXXXX")
        # Arrival Times
        arrive=""
        tz2="PACIFIC STANDARD TIME"
        if tzid in tz_long: tz2=tz_long[tzid]+" STANDARD TIME" # LOCAL TIME
        if "daytime" in info and info["daytime"].find("STANDARD")==-1: tz2=tz2.replace("STANDARD","DAYLIGHT")
        # Wave Arrivals
        key="arrivals_"+wwa
        if key in info and len(info[key])>0: # put segment header only if there is something here
            arrive+="\n\nEstimated arrival times for the first in the series of tsunami waves are as follows in "+tz2
            for a in info[key]: arrive+=".\n"+a
            arrive+="."
        if arrive!="": out=out.replace("LLLLLL",arrive)
        else: out=out.replace("LLLLLL.","").replace("LLLLLL","") # not available, skip
        # Local Actions Generic
        out=out.replace("SSSSSS",local_actions) # from config_tsu
        # Band Aids
        out=cleanStatement(out)
        # Insert into window
        radiotext.insert(0.0,out.upper().strip())
  # 2 Combo option - Will not use the template option for now.  (triple option not handled here)
    else:
        tmp=dowhat.split("_")
        wwa1=tmp[0]
        wwa2=tmp[1]
        out="Tsunami "+wwa1+" and "+wwa2+". Repeat, Tsunami "+wwa1+" and "+wwa2+".\n\n"
        break1="XXXXXX"
        break2="XXXXXX"
        if breakpoints[wwa1]!="" and breakpoints[wwa2]!="":
            break1=breakpoints[wwa1]
            break2=breakpoints[wwa2]
        # earthquake info
        eq=""
        if "eseg" in info: eq=info["eseg"]
        out+="The National Weather Service has issued a tsunami "+wwa1+" for the "+break1+" and a tsunami \
  "+wwa2+" for the following locations, "+break2+"." #Mar2022
        out+=hazdef
        out+="\n\n"+eq
        # Arrival Section
        tz2="PACIFIC STANDARD TIME"
        if tzid in tz_long: tz2=tz_long[tzid]+" STANDARD TIME" # LOCAL TIME
        if info["daytime"].find("STANDARD")==-1: tz2=tz2.replace("STANDARD","DAYLIGHT")
        arrive=""
        track=[] # no duplicates
        for www in [wwa1,wwa2]:
            key="arrivals_"+www
            if len(info[key])>0:
                for a in info[key]:
                    if a not in track:
                        arrive+=".\n"+a
                        track.append(a)
        if arrive!="": out+="\n\nEstimated arrival times for the first in the series of tsunami waves are as follows in "+tz2+arrive+"."
        # Final warning
        out+="\n\nOnce again, The National Weather Service has issued a tsunami "+wwa1+" for the "+break1+" and a \
  tsunami "+wwa2+" for the following locations, "+break2+"." #Mar2022
        out+=hazdef+" "+local_actions
        out=out.replace(" .",".") # fix potential quirk
        radiotext.insert(0.0,out.upper().strip())
    # Zone Buttons
    if dowhat.find("_")==-1: # single hazard case
        if dowhat in info and len(info[dowhat])>0:
            for z in info[dowhat]:
                if z in bt_zones: bt_zones[z].set(1)
    else:
        tmp=dowhat.split("_")
        for w in tmp:
            if w in info and len(info[w])>0:
                for z in info[w]:
                    if z in bt_zones: bt_zones[z].set(1)
    radiotext=highlightMe(radiotext,"XXX","#000","#fff")
    # testmode activate?
    untestMe(1)
    if wcapil.find("/")!=-1: testMe(1) # hard coded for example statements
    elif testmode==1 or modeme=="test": testMe()

# Creates text for the statement...both autofill and templates
def statementMake(wwa,directory="",what="",withwhat=""):
    # Some preparation
    #print directory # should be the etc/ version if available
    out=""
    if directory=="": directory="config/template_"+wwa+".txt" # specified warning statement
    d=todaylocal.strftime("%B %d")
    dt="STANDARD"
    if daylightMe(todaylocal)==1: dt="DAYLIGHT"
    tz2="PACIFIC "+dt+" TIME"
    if tzid in tz_long: tz2=tz_long[tzid]+" "+dt+" TIME" # LOCAL TIME
    # ===Create Template Skeleton===
    # Check if local template files are available
    if os.path.isfile(local_dir+directory):
        #print "etc template"
        with open(local_dir+directory,'r') as tmplt:
            lines = tmplt.read().splitlines()
        for a in lines:
            if len(a)==0 or a[0]!="#": out+=a+"\n"
        out=out.strip()
    # No Template Files Found, generate in house.
    else:
        #print "Built in template"
        wwa2=wwa.upper()
        out+="TSUNAMI "+wwa2+". REPEAT, TSUNAMI "+wwa2+".\n\n"
        out+="THE NATIONAL WEATHER SERVICE HAS ISSUED A TSUNAMI "+wwa2+" AAAAAA. NNNNNN.\n\n"
        out+="EEEEEE.\n\n"
        out+="LLLLLL.\n\n"
        out+="ONCE AGAIN, THE NATIONAL WEATHER SERVICE HAS ISSUED A TSUNAMI "+wwa2+" AAAAAA. NNNNNN. "
        out+="SSSSSS. IN HOUSE."
    # ===Fill in placeholders===
    if what!="":
        # Area Description
        if what=="AAAAAA" or what=="XXXXXX":
            if withwhat!="": out=out.replace("AAAAAA","for the following locations, "+withwhat) #Mar2022
            else: out=out.replace("AAAAAA","for the following locations, XXXXXX") # don't ignore if "" #Mar2022
        # Call to Action
        if what=="NNNNNN" or what=="XXXXXX":
            out=out.replace("NNNNNN",hazDefMe(wwa))
        # Earthquake info
        if what=="EEEEEE" or what=="XXXXXX":
            if what=="XXXXXX" or withwhat=="XXXXXX":
                eq="On "+d+" AT XXXXXX "+tz2+" a large earthquake with a preliminary magnitude of XXXXXX occurred near XXXXXX."
                out=out.replace("EEEEEE",eq)
            elif withwhat!="": out=out.replace("EEEEEE",withwhat)  #if withwhat=="" ignore this section
            else: # Remove Section
                out=out.replace("EEEEEE.","").replace("EEEEEE","")
        # Arrival Information
        if what=="LLLLLL" or what=="XXXXXX":
            if str(withwhat).find("[")!=-1: # is it a list?
                aaa="Estimated arrival times for the first in the series of tsunami waves are as follows in "+tz2+","
                for a in withwhat: aaa+=".\n"+a
                out=out.replace("LLLLLL",aaa)
            elif what=="XXXXXX" or withwhat!="":  # Anything else use empty template
                aaa="Estimated arrival times for the first in the series of tsunami waves are as follows in "+tz2+", \n"
                d=todaylocal.strftime("%B %d")

                # Add arrival locations/times.
                for a in arrival_list:
                    stn_id = a.upper()
                    # See if the WFO has configured an alternate name for this point.
                    try:
                        if arrival_localname[stn_id] != None:
                            stn_id = arrival_localname[stn_id]
                            # print("\tLocal alias is " + stn_id)
                    except:
                        pass
                    aaa+= stn_id +" on "+d+" at XXXXXX. \n"

                out=out.replace("LLLLLL",aaa)
            else: # Remove Section
                out=out.replace("LLLLLL.","").replace("LLLLLL","")
        # Closing
        if what=="SSSSSS" or what=="XXXXXX":
            out=out.replace("SSSSSS",local_actions)
    return out

# Bandaid fixes for syntax and punctuation
def cleanStatement(out):
    out=out.replace(" .",".")
    out=out.replace("...",", ")
    out=out.replace("..",".")
    out=out.replace("\n.\n","\n\n")
    out=out.replace("  "," ")
    out=out.replace("\n\n\n","\n\n")
    out=out.replace("\n\n\n","\n\n")
    out=out.replace("\n\n\n","\n\n")
    return out

# For warning and watch template - create statement for main text window
def fillWWA(wwa,d=""):
    global radiotext,bt_zones,zone_ids,active,combo_need
    saveMe() # autosave
    destroyBoxes()
    active=wwa
    combo_need=[] # clear out in case filled from initial autofill
    hazdef=hazDefMe(wwa)
    # Determine Breakpoints to potentially save some time
    arr={} # to trick breakpoints function
    arr["warning"]=[]
    arr["watch"]=[]
    arr["advisory"]=[]
    for z in zone_ids:
        if z in bt_zones and bt_zones[z].get()==1: arr[wwa].append(z)
    breakpoints=breakpointMe(arr) #advisory, warning, watch (string)
    lphrase="XXXXXX"
    if wwa in breakpoints and breakpoints[wwa]!="": lphrase=breakpoints[wwa]
    # Get and fill out Template
    out=statementMake(wwa,d,"XXXXXX") ### AAAAAA:Area Description, NNNNNN:WWA Call2Action, EEEEEE:Earthquake, LLLLLL:Arrival Times, SSSSSS:Local Actions
    # Clean Statement
    out=cleanStatement(out)
    # Send to Window
    radiotext.delete(0.0,END)
    radiotext.insert(0.0,out.upper().strip())
    radiotext=highlightMe(radiotext,"XXX","#03f","#fff")
    untestMe(1)
    if modeme=="test": testMe()
    # Zone Buttons - if none selected, remind user
    if len(arr[wwa])==0: flagMeBasic("Don't forget to select the Applicable Zones from the check buttons on the main window.")

# Intermediate GUI Window prompting options for combo sitionations
def comboOption(a=""):
    global alertframe,combo_need
    saveMe() # autosave
    destroyBoxes()
    if a!="" and a.find("_")==-1: a="warning_watch" # have to force to something
    bg="#eee"
    alertframe=Toplevel()
    alertframe.title('Combo Select')
    alertframe.configure(bg=bg)
    alertframe.option_add('*Font', ('Arial',12))
    alertframe.geometry(windowXgeo())
    lbl="Multiple Hazards were detected in your CWA.\nWhich do you want to load into the GUI at this time?"
    if a=="": lbl="Which Combo Set would you like a template for?"
    Label(alertframe,text=lbl,bg=bg,fg="#000").grid(row=0,column=0,columnspan=3,padx=gp,pady=gp)
    if a=="" and advisory_handle=="no": # no advisory option, choice is easy (when segment option is used: default not)
        fillWarningWatch()
        return
    elif a=="": # just ask for which combos to use (for autofill templates)
        Button(alertframe,text=" Warning&Watch ",command=fillWarningWatch,bg="#000",fg="#fff").grid(row=1,column=0,sticky=W+E)
        Button(alertframe,text=" Warning&Advisory ",command=fillWarningAdvisory,bg="#000",fg="#fff").grid(row=1,column=1,sticky=W+E)
        Button(alertframe,text=" Watch&Advisory ",command=fillWatchAdvisory,bg="#000",fg="#fff").grid(row=1,column=2,sticky=W+E)
    else:
        tmp=a.split("_")
        col=0
        combo_need=[] # to track combos
        if combo_handle=="yes" and len(tmp)<3:
            Button(alertframe,text=" All ",command=fillcomboAll,bg="#000",fg="#fff").grid(row=1,column=col,sticky=W+E)
            col+=1
        if "warning" in tmp:
            Button(alertframe,text=" Warning ",command=fillcomboWarning,bg="#000",fg="#fff").grid(row=1,column=col,sticky=W+E)
            col+=1
            combo_need.append("warning")
        if "watch" in tmp:
            Button(alertframe,text=" Watch ",command=fillcomboWatch,bg="#000",fg="#fff").grid(row=1,column=col,sticky=W+E)
            col+=1
            combo_need.append("watch")
        if "advisory" in tmp:
            Button(alertframe,text=" Advisory ",command=fillcomboAdvisory,bg="#000",fg="#fff").grid(row=1,column=col,sticky=W+E)
            col+=1
            combo_need.append("advisory")
    Button(alertframe,text=" Cancel  ",command=alertframe.destroy,bg="#00a",fg="#fff").grid(row=2,column=0,columnspan=3,padx=gp,pady=gp)

# Autofill from TSUWCA - from comboOption
def fillcomboAll(): autoFill("all")
def fillcomboWarning(): autoFill("warning")
def fillcomboWatch(): autoFill("watch")
def fillcomboAdvisory(): autoFill("advisory")

# Autofill blank templates. d=etc/template_advisory.txt
def fillWhat(d):
    # Clear Zone Check Boxes
    global bt_zones,info,masterTone
    for z in zone_ids: bt_zones[z].set(0) # clear zone check boxes
    # Clear WWA tracking
    info["warning"]=[]
    info["watch"]=[]
    info["advisory"]=[]
    # Determine warning type from template
    if os.path.isfile(local_dir+d):
        with open(local_dir+d,'r') as tmplt:
            lines = tmplt.read().upper().splitlines()
        out=""
        for a in lines:
            if len(a)==0 or a[0]!="#": out+=a+"\n"
        out.replace("WARNING CENTER","WARNI CENTER") # in case warning center is in the template???
        wwafind=[0,0,0]
        if out.find("WARNING")!=-1: wwafind[0]=1
        if out.find("WATCH")!=-1: wwafind[1]=1
        if out.find("ADVISORY")!=-1: wwafind[2]=1
        if sum(wwafind)==0: # WWA not in the template :-\
            return flagMeBasic("Something went wrong with your template selection. No WWA was found in the template. Please check your template file. code:fillWhatONE")
        if sum(wwafind)>1:
            return flagMeBasic("It appears that the template file has multiple WWA type. Sorry but combo templates are not supported at this time. code:fillWhatTWO")
        if wwafind[0]==1: fillWWA("warning",d)
        if wwafind[1]==1: fillWWA("watch",d)
        if wwafind[2]==1: fillWWA("advisory",d)
        if d.find("cancel")!=-1: masterTone=0 #turn off tone alert
        else: masterTone=1
        #print masterTone,d
    return

#def fillWarning():
#   global bt_zones
#   for z in zone_ids: bt_zones[z].set(0) # clear zone check boxes
#   info["warning"]=[] # clear tracking
#   info["watch"]=[]
#   info["advisory"]=[]
#   fillWWA("warning")
#def fillWatch():
#   global bt_zones
#   for z in zone_ids: bt_zones[z].set(0) # clear zone check boxes
#   info["warning"]=[] # clear tracking
#   info["watch"]=[]
#   info["advisory"]=[]
#   fillWWA("watch")
#def fillAdvisory():
#   global bt_zones
#   for z in zone_ids: bt_zones[z].set(0) # clear zone check boxes
#   info["warning"]=[] # clear tracking
#   info["watch"]=[]
#   info["advisory"]=[]
#   fillWWA("advisory")
def fillWarningWatch(): fillCombo("warning_watch")
def fillWarningAdvisory(): fillCombo("warning_advisory")
def fillWatchAdvisory(): fillCombo("watch_advisory")
def fillCombo(a="warning_watch"): # like fillWWA, but for unique combo situation
    global alertframe,radiotext,bt_zones,active,combo_need
    saveMe() # Autosave
    destroyBoxes()
    combo_need=[] # clear out in case filled from initial autofill
    active=a # assign for future
    hazdef=hazDefMe(a)
    # Determine Hazards
    if a.find("_")==-1:
        flagMeBasic("Something bad happened. Many apologies. code:fillComboONE")
        return # abort
    tmp=a.split("_")
    wwa1=tmp[0]
    wwa2=tmp[1]
    # Zone Buttons - check them all (no better way)
    arr=[]
    for z in zone_ids:
        if z in bt_zones and bt_zones[z].get()==1: arr.append(z)
    if len(arr)==0: flagMeBasic("Don't forget to select the Applicable Zones.")
    # Now get on with it!
    d=todaylocal.strftime("%B %d")
    dt="STANDARD"
    if daylightMe(todaylocal)==1: dt="DAYLIGHT"
    tz2="PACIFIC "+dt+" TIME"
    if tzid in tz_long: tz2=tz_long[tzid]+" "+dt+" TIME" # LOCAL TIME
    out="Tsunami "+wwa1+" and "+wwa2+". Repeat, Tsunami "+wwa1+" and "+wwa2+".\n\n"
    break1="XXXXXX"
    break2="XXXXXX"
    out+="The National Weather Service has issued a tsunami "+wwa1+" for "+break1+" and a tsunami \
 "+wwa2+" for "+break2+"."
    out+=hazDefMe(a)
    out+="\n\nOn "+d+" AT XXXXXX "+tz2+" a large earthquake with \
 a preliminary magnitude of XXXXXX occurred near XXXXXX."
    out+="\n\nEstimated arrival times for the initial wave are as follows, \n"

    # Add arrival locations/times.
    for a in arrival_list:
        stn_id = a.upper()
        # See if the WFO has configured an alternate name for this point.
        try:
            if arrival_localname[stn_id] != None:
                stn_id = arrival_localname[stn_id]
                # print("\tLocal alias is " + stn_id)
        except:
            pass
        out+= stn_id +" on "+d+" at XXXXXX. \n"

    out+="\nOnce again, The National Weather Service has issued a tsunami "+wwa1+" for "+break1+" and a \
 tsunami "+wwa2+" for "+break2+"."
    out+=hazdef+" "+local_actions
    out=out.replace(" .",".") # fix potential quirk
    radiotext.delete(0.0,END)
    radiotext.insert(0.0,out.upper().strip())
    radiotext=highlightMe(radiotext,"XXX","#03f","#fff")

# == Pull TWC Data ====

# Pull information from TSUWCA
def grabTWC(pil,zone_ids):
    global arrival_list,alertframe
    destroyBoxes()
    info={} # amin array to store information
    info["arrivals_advisory"]=[]
    info["arrivals_warning"]=[]
    info["arrivals_watch"]=[]
    info["warning"]=[]
    info["watch"]=[]
    info["advisory"]=[]
    info["issue"]=[]
    info["eseg"]=""
    info["daytime"]="XXXXXX"
    info["extra"]=""
  # timezone offsets
    tz_set=""
    if tzid in tz_long: tz_set=tz_long[tzid] # what earthquake info should be set to
    tsuin=""
    #print "here"
  # Pull and Play TSu Bulletin
    try:
        if pil.find("/")!=-1 or pil.find(".")!=-1:
            if not os.path.isfile(pil): # Fail!
                flagMeBasic("Specified Input File Not Found. Try changing file or PIL in the Autofill options. code:grabTWCONE")
                return info
            with open(pil,"r") as pillow:  # open file and read
                tsuin = pillow.read()
        else:
            cmd=db+" -r "+pil
            tsuin=os.popen(cmd).read()

            try:
                # Check when this bulletin was added to Text DB.
                cmd = db + " -tU " + pil
                bulletinTm = int(os.popen(cmd).read())
                now = int(time.time())

                if bulletinTm > 0:
                    dif = now - bulletinTm
                    minutes,seconds = divmod(dif,60)
                    hours,minutes = divmod(minutes,60)
                    days,hours = divmod(hours,24)

                    sPlu = mPlu = hPlu = dPlu = "s"
                    if seconds == 1: sPlu = ""
                    if minutes == 1: mPlu = ""
                    if hours   == 1: hPlu = ""
                    if days    == 1: dPlu = ""

                    if days > 0:
                        timeAgo = "{0} day{1} and {2} hour{3}".format(days, dPlu, hours, hPlu)
                    elif hours > 0:
                        timeAgo = "{0} hour{1} and {2} minute{3}".format(hours, hPlu, minutes, mPlu)
                    else:
                        timeAgo = "{0} minute{1} and {2} second{3}".format(minutes, mPlu, seconds, sPlu)
                    timeGoneBy = "A {0} bulletin was received {1} ago".format(pil, timeAgo)
                    # print(timeGoneBy)
                    flagMeBasic(timeGoneBy, "Tsunami Bulletin Found", 0)

            except:
                logging.exception("An error occurred getting bulletin creation time.")

        segs=tsuin.replace("\r","").upper().split("$$") # by segment
        pat="[0-9][0-9][0-9]>[0-9][0-9][0-9]"

        #
        # Hawaii and Guam bulletins require some specific processing.
        # American Samoa and Guam bulletins have the same format, apart from the heading.
        #
        isHawaii = isGuam = isCarib = False
        if segs[0].find("\nTSUHWX") > 0:
            isHawaii = True
            # print("This is a Hawaii bulletin.\n")
        if (segs[0].find("\nTSUGUM") > 0) or (segs[0].find("\nTSUPPG") > 0):
            isGuam = True
            # print("This is a Guam/American Samoa bulletin.\n")
        if segs[0].find("\nTSUCA1") > 0:
            isCarib = True
            # print("This is a Caribbean bulletin.\n")

        # Go through each segment. Pull hazard time and arrival times
        testvtec="no"
        for s in segs:
         # Determine hazard type for segment (crude...make more robust later)
         # Because vtec could be two lines (for cancellations or upgrades) will need to look at last vtec line
            ss = s.replace("\r","").replace("\n ","\n").splitlines()
            vline=""
            for line in ss:
                if line.find("TS.W")!=-1 or line.find("TS.A")!=-1 or line.find("TS.Y")!=-1:
                    ##if line.find(".CAN.")!=-1: vline=line # set vline but keep searching in case it is an upgrade
                    if line.find(".EXP.")==-1 and line.find(".CAN.")==-1 and line.find(".UPG.")==-1:
                        vline=line # hit
                        break
            if vline=="": continue # no vtec detected in segment
            wwa=""
            #if s.find("TSUNAMI WARNING")!=-1: wwa="warning"
            #if s.find("TSUNAMI WATCH")!=-1: wwa="watch"
            if vline.find("TS.A")!=-1: wwa="watch"
            elif vline.find("TS.W")!=-1: wwa="warning" # yeah VTEC!
            elif vline.find("TS.Y")!=-1: wwa="advisory"
            if wwa=="": continue # might be a blank line. Either way, not whay i am expecting
            if vline.find("/T.")!=-1: testvtec="yes"
         # Determine if new issuance or continued
            issue="new"
            #print vline
            if vline.find(".EXP.")!=-1 or vline.find(".CAN.")!=-1: issue="final"
            if vline.find(".CON.")!=-1 or vline.find(".UPG.")!=-1 or s.find(".EXT.")!=-1 or s.find(".EXB.")!=-1 or s.find(".EXA.")!=-1:
                issue="continue"
            if vline.find(".NEW.")!=-1: issue="new"
            if issue=="final": continue # should not count these zones...they are no longer in effect
         # Find Applicable Zones
         # First get all zones mentioned (expand condensed shorthand CAZ039>041)
            tmp=re.compile(pat).findall(s)
            if len(tmp)>0:
                b="" # string to substitute
                for a in tmp: #['505>509','039>041']
                    tmp2=a.split(">")
                    for i in range(int(tmp2[0]),int(tmp2[1])+1):
                        b=b+str(i).zfill(3)+"-"
                    s=s.replace(a,b) # replace condense pattern with newly expanded
            s=s.replace("--","-") # just in case
            pat2="\n[A-Z][A-Z][Z][0-9][0-9][0-9]-[A-Z0-9-\n]+" # find zone string
            tmp=re.compile(pat2).findall(s)
            if len(tmp)>0: # hit, zones present and detected!
                arr=tmp[0].replace("\n","").split("-")
                pre=""
                for a in arr:
                    #print pre+"|"+str(a)+","
                    if len(a)>3:
                        if a.find("Z")!=-1: # should have Z in zone code. Skips date code.
                            pre=a[0:3]
                            if a not in info[wwa] and a in zone_ids: # hit, these are the zones we want
                                info[wwa].append(a)
                                if issue not in info["issue"]: info["issue"].append(issue)
                    else:
                        id=pre+str(a)
                        if len(a)==3 and id not in info[wwa] and id in zone_ids:
                            info[wwa].append(id)
                            if issue not in info["issue"]: info["issue"].append(issue)
                info[wwa].sort()

            # ==== Parse Out Arrival Information
            # Find Arrival Section
            ss=s.replace("\r","").replace("\n ","\n").split("\n\n")
            for j in range(0,len(ss)): # go through each section
                #
                # Hawaii and Caribbean bulletins only have one paragraph indicating the start
                # of the hazard in that area, not a table of arrival times for specific locations.
                #
                if isHawaii or isCarib:
                    arriveLine = ""
                    if isHawaii:
                        #
                        # There are two different phrases which may flag the start of the Arrival
                        # statement in a Hawaii bulletin.
                        #
                        if (ss[j].find("ESTIMATED TIME OF ARRIVAL OF THE INITIAL WAVE") >= 0 or \
                            ss[j].find("IF TSUNAMI WAVES IMPACT HAWAII THE ESTIMATED EARLIEST ARRIVAL") >= 0) and \
                           ((j+1) < len(ss)):

                            # print("Found Hawaii arrival: " + ss[j+1])
                            #
                            # The arrival time is a line by itself in the next section. Example:
                            # 0327 PM HST MON 19 OCT 2020
                            #
                            fields = ss[j+1].lstrip().split(" ")
                            if len(fields) >= 6 and len(fields[0]) == 4:
                                arrTime = fields[0]
                                if arrTime[0] == '0':
                                    arrTime = arrTime[1:]
                                arrZone = "HAWAII STANDARD TIME"
                                if arrZone != "HST":
                                    arrZone = fields[2]
                                try:
                                    arrMonth = mon_longer[mon_long.index(fields[5])]
                                except:
                                    arrMonth = fields[5]
                                arriveLine = "HAWAIIAN ISLANDS ON {0} {1} AT {2} {3} {4}".format(arrMonth, \
                                              fields[4], arrTime, fields[1], arrZone)

                    if isCarib:
                        #
                        # The Caribbean arrival time is in a section two lines long. Example:
                        # THE HAZARD IS FORECAST TO BEGIN AROUND 1109 PM AST ON FRIDAY
                        # JANUARY 29 2016 AND IT CAN PERSIST FOR MANY HOURS OR LONGER.
                        #
                        if ss[j].find("HAZARD IS FORECAST TO BEGIN") >= 0:
                            airLine = ss[j].splitlines()

                            if len(airLine) > 1:
                                arriveLine = airLine[0] + " " + airLine[1]
                                beg = arriveLine.index("AROUND")
                                mid = arriveLine.index("AST ON ")
                                end = arriveLine.index("AND IT CAN")
                                if beg > 0 and mid > beg and end > beg:
                                    arriveLine = "PUERTO RICO/VIRGIN ISLANDS " + arriveLine[beg:mid] + arriveLine[mid+4:end]

                    if len(arriveLine) > 0:
                        # We found the arrival text block! Add it to the info to be returned.
                        # print(arriveLine)
                        key="arrivals_"+wwa
                        info[key].append(arriveLine)
                        break

                #
                # Not a Hawaii or Caribbean bulletin; process arrivals normally.
                #
                elif (ss[j].find("START")!=-1 and ss[j].find("SELECTED")!=-1) or \
                         (isGuam and ss[j].find("-ETA- OF THE INITIAL TSUNAMI WAVE") >= 0): # isolate arrival segment
                    # print("Found arrival segment: " + ss[j])
                    for ii in range(1,3): # find first station...in case there are many empty lines
                        k=j+ii #ss[k] should include entire arrival times segment
                        if len(ss)>k and ss[k].replace(" ","").replace("\n","")!="": # non empty line, HIT!
                            # print("Found station list segment: " + ss[k])
                            sss = ss[k].splitlines()
                            for line in sss:  # go through each line
                                if isGuam:    # Skip the two header lines in the Guam bulletin.
                                    if line.find("LOCATION") >= 0 and line.find("REGION") >= 0:
                                        continue
                                    if line.find("------------") >= 0:
                                        continue
                                # Isolate just the station information
                                # Standard line: "MANHATTAN          NEW YORK            700 PM  EDT MARCH 28"
                                # Guam line:     "   PAGO BAY            GUAM      13.4N 144.8E    817 AM 12/09"
                                tmp=line.lstrip().replace("   "," ").replace("   "," ").replace("  "," ").replace("  "," ").split(" ")
                                tmp.pop()
                                tmp.pop()
                                tmp.pop()
                                tmp.pop()
                                tmp.pop()
                                stnid=' '.join(tmp).upper()
                                # print("Looking for station " + stnid)
                                # Search through users arrival list for a hit
                                hitme=0
                                for a in arrival_list:
                                    if stnid.find(a.upper())!=-1:
                                        hitme=1
                                        break
                                if hitme==1: # This station is on the Arrival list.
                                    # print("Found station " + stnid)
                                    # See if the WFO has configured an alternate name for this point.
                                    try:
                                        if arrival_localname[stnid] != None:
                                            stnid = arrival_localname[stnid]
                                            # print("\tLocal alias is " + stnid)
                                    except:
                                        pass

                                    arriveLine = ""
                                    if isGuam:
                                        arriveLine = formatGuamArrival(stnid, line)
                                        # print(arriveLine)
                                    else:
                                        #state=line[19:30].upper().replace("  "," ").lstrip(" ").rstrip(" ") # grab state
                                        line=re.compile("[\W]").sub(" ",line) # band-aid for occasional bad characters from NTWC
                                        pat2="  ([0-9]+)\s+([A,P,M]+)\s+([A-Z][A-Z]?[S,D]T)\s+([A-Z]+)\s+([0-9]+)" # find time string
                                        tmp=re.compile(pat2).findall(line) #[('724', 'AM', 'PST', 'MARCH', '11')]
                                        if len(tmp)>0 and len(tmp[0])>4:
                                            arriveLine=str(stnid)+" ON "+str(tmp[0][3])+" "+str(tmp[0][4])+" AT "+str(tmp[0][0])+" "+str(tmp[0][1])
                                    if len(arriveLine) > 0:
                                        key="arrivals_"+wwa
                                        info[key].append(arriveLine)
                            break # no need to keep looking since we found what we need
                    break

    # Find Timestamp
        pat="([0-9]+[\s][A,P][M][\s][P,M,C,E,A][D,S][T][\s][A-Za-z][A-Za-z][A-Za-z][\s][A-Z][A-Z][A-Z][\s][0-9]+[\s]+[0-9]+)"
        tmp=re.compile(pat).findall(tsuin)
        prodtime=""
        if len(tmp)>0: prodtime=tmp[0]
        tmp=prodtime.split(" ")
        yy=todaylocal.year # save product issue time year
        if len(tmp)>6: yy=tmp[6]
    # ==== Parse Out Earthquake Information
    # Find a segment with earthquake information Earthquake section
        eseg=""
        for i in range(0,len(segs)): # isolate earthquake segment
            #
            # The segment should contain the words EARTHQUAKE and MAGNITUDE.
            #
            if segs[i].find("EARTHQUAKE")!=-1 and segs[i].find("MAGNITUDE") != -1: # main segments
                ss=segs[i].replace("\r","").replace("  "," ",).replace("  "," ").replace("\n ","\n").split("\n\n")
                for j in range(0,len(ss)): # isolated paragraph in segment
                    #
                    # The Hawaii bulletin has its preliminary EQ information in 4-5 lines in the paragraph
                    # following the line saying an earthquake has occurred. So parse it differently.
                    #
                    if isHawaii:
                        if ss[j].find("AN EARTHQUAKE HAS OCCURRED WITH THESE")!=-1 and len(ss) > j:
                            eseg = parseHawaiiEQ(ss[j+1])
                            break

                    elif ss[j].find("EARTHQUAKE")!=-1 and ss[j].find("MAGNITUDE") != -1: # with some phrasing cleanup
                        eseg=ss[j].replace("WITH PRELIMINARY","WITH A PRELIMINARY").replace("MAGNITUDE","MAGNITUDE OF").replace("OF OF","OF")
                        if eseg[0:2] == "* ":
                            eseg = eseg[2:]
                        # print("Earthquake segment, after editing:\n"+eseg)
                        break
                break
    # Time & Date - Convert to local time in case earthquake information is in something else
        if tzset!="" and eseg.find(tzset)==-1: # need to convert to local time
            tzpatarr=[]
            for a,b in tz_offlist.items(): tzpatarr.append(a) # grab available timezones from arr at top
            tzpat=str(tzpatarr).replace("[","").replace("]","").replace(" ","").replace("'","").replace(",","|")
            pat="AT ([0-9][0-9][0-9][0-9]? [A,P][M]) ("+tzpat+") (DAYLIGHT|STANDARD|)"
            tmp=re.compile(pat).findall(eseg) #[('839 AM', 'ALASKAN', 'DAYLIGHT')]
            if len(tmp)>0 and len(tmp[0])>2:  # Enough for a match
                # find month and day in eseg
                dd="" # date to replace
                for m in mon_longer:
                    if m!="" and eseg.find(m)!=-1:
                        i=eseg.find(m)
                        t=eseg[i:].split(" ")
                        if len(t)>1: dd=str(m)+" "+str(t[1])
                dstring=dd+" "+str(yy)+" "+tmp[0][0]
                ut=time.mktime(time.strptime(dstring,"%B %d %Y %I%M %p")) #unix time of earthquake
                tzname=str(tmp[0][1])+" "+str(tmp[0][2])+" "+"TIME" # time zone name
                if tmp[0][1] in tz_offlist:
                    eoff=tz_offlist[tmp[0][1]] # earthquake GMT offset hours
                    ut=ut+(3600*int(eoff)) #earthquake in GMT
                    ut=ut-(3600*tz) #earthquake in local time
                    if tzid in tz_long:
                        tzname=tz_long[tzid]+" STANDARD TIME"
                        if dst==1: tzname=tzname.replace("STANDARD","DAYLIGHT")
                tzdt=datetime.datetime.fromtimestamp(ut) # new time in daytime format...almost there
                d=str(int(tzdt.strftime("%d"))) # annoying preceding "0" fix
                t2=str(int(tzdt.strftime("%I")))
                # Save for later
                daytime=tzdt.strftime("%B DDD %Y AT TTT%M %p XXX")
                daytime=daytime.replace("DDD",d).replace("TTT",t2).replace("XXX",tzname)
                info["daytime"]=daytime #April 16 2012 AT 939 AM PACIFIC DAYLIGHT TIME
                dtarr=daytime.split(" ") # element by element
                # now fix up eseg
                # print dd,dtarr[0]+" "+dtarr[1]
                eseg=eseg.replace(tmp[0][0],dtarr[4]+" "+dtarr[5]) # time replaced
                eseg=eseg.replace(str(tmp[0][1])+" "+str(tmp[0][2]),dtarr[6]+" "+dtarr[7]) # Timezone replaced
                eseg=eseg.replace(dd,dtarr[0]+" "+dtarr[1]) # month/day replaced
        info["eseg"]=eseg.replace("\n"," ").replace("  "," ").replace("  "," ").lstrip(" ").rstrip(" ") # all done!
    except:
    #else:
        info={"warning":[],"watch":[],"advisory":[]}
        #info={} 20150401 fix
        info["extra"]="Autofill parsing failed!\nView TSU statement from the black button at the top right and try another version, or use one of the options from the Templates menu."
        return info
        pass
    if len(info["warning"])==0 and len(info["watch"])==0 and len(info["advisory"])==0: # bad parse
        info={"warning":[],"watch":[],"advisory":[]}
        #info={}
        info["extra"]="No tsunami WWA is in effect for your area, or the parsing failed.\nClick the View TSU statement to check or load a previous version"
        return info
        pass
    if tsuin!="" and ((testvtec=="yes" and tsuin.find("TEST")!=-1) or (tsuin.find("TEST")!=-1)): # test product
        info["extra"]="The latest message from the Tsunami Warning Center appears to be a test.\nYou may Load it \
with test wording or cancel."
    return info

#
# Extract the arrival time information from a line in the ETA table of a Guam bulletin.
# Also used for the American Samoa bulletin since it has the same format as Guam.
#
def formatGuamArrival(stnid, line):
    # Guam line:     "   PAGO BAY            GUAM      13.4N 144.8E    817 AM 12/09"
    # ['CHULU', 'BEACH', 'TINIAN', '15.1N', '145.6E', '828', 'AM', '12/09']
    fields = line.lstrip().replace("   "," ").replace("   "," ").replace("  "," ").replace("  "," ").split(" ")
    # print(fields)

    # The location name could be several words long.
    start = len(fields) - 5
    day = fields[start+4].split("/")
    if len(day) != 2:
        return ""
    try:
        month = int(day[0])
        if month < 1 or month > 12:
            return ""
        dom = day[1]
        if dom[0] == '0':
            dom = day[1][1:]
    
        retString = stnid + " ON " + mon_longer[month] + " " + dom + " AT " + fields[start+2] + " " + fields[start+3]
    except:
        logging.exception("An error occurred formatting arrival time for station {0}".format(stnid))
        return ""

    return retString

#
# The Hawaii earthquake preliminary parameters are spread across 4 to 5 lines following
# the phrase which flags their presence. Below is an example:
#
# ORIGIN TIME - 1055 AM HST 19 OCT 2020
# COORDINATES - 54.7 NORTH 159.6 WEST
# LOCATION - SOUTH OF ALASKA
# MAGNITUDE - 7.5 MOMENT
#
def parseHawaiiEQ(segment):
    otime = ""
    location = ""
    magnitude = ""
    lines = segment.upper().splitlines()
    
    for line in lines:
        if line.find("ORIGIN TIME") >= 0:
            field = line.rstrip().split(" ")
            ozone = "HAWAII STANDARD TIME"
            if field[5] != "HST":
                ozone = field[3]
            otime = "{0} {1} {2} ON {3} {4} {5}".format(field[3], field[4], ozone, field[6], field[7], field[8])

        if line.find("LOCATION") >= 0:
            idx = line.find(" - ")
            if idx > 0:
                location = line[idx+3:]

        if line.find("MAGNITUDE") >= 0:
            field = line.split(" ")
            magnitude = field[2]

    eqStatement = "AN EARTHQUAKE WITH A PRELIMINARY MAGNITUDE OF {0} OCCURRED {1} AT {2}".format(magnitude, location, otime)

    # print(eqStatement)
    return eqStatement

# Action taken each time a zone is checked
def changeZone():
    global info,active,radiotext,bt_zones # active=current wwa
    zones=[]
    for z,a in bt_zones.items():
        if a.get()==1: zones.append(z)
    #print zones
    #print "changezone",info
    info[active]=zones # alter main array
    a=breakpointMe(info) # grab new phrase
    try:
        if len(a)<1 or active=="": return  # no hazard selected yet
        replacewith=a[active].upper()
        if replacewith.replace(" ","")=="": replacewith="XXXXXX"
        # Play with main text box
        # Find phrase to replace
        text=radiotext.get(0.0,END)
        text=text.strip()
        #print text
        findme="TSUNAMI "+active.upper()+" FOR THE FOLLOWING LOCATIONS," #Mar2022
        #findme=" FOR THE "
        #if text.find("TEST TSUNAMI")!=-1: findme=findme.replace("TSUNAMI","TEST TSUNAMI")
        a=text.split(findme) # Isolate findme phrase. Looking for one hit to use to replace phrase
        #print len(a),findme,replacewith
        if len(a)>1:  #a[0]before phrase, b[0]phrase
            b=a[1].split(".") # 20180919 fix.  There was an issue with just using "." in 20150312.
            replaceme="FOR THE FOLLOWING LOCATIONS,"+b[0] #Mar2022
            replacewith="FOR THE FOLLOWING LOCATIONS,"+" "+(replacewith.strip()) #Mar2022
            #print "A",replaceme,"B",replacewith
            text=text.replace(replaceme,replacewith)
            radiotext.delete(0.0,END)
            radiotext.insert(0.0,text.strip())
            radiotext=highlightMe(radiotext,"XXX","#03f","#fff")
    except:
        logging.error("changeZone() function had an exception")


# Makes nice long string for all zones in list
def zonelistFull(zones):
    pre=[]
    try:
        for z in zones:
            if z in zone_list: pre.append(zone_list[z])
            else: pre.append("XXXXXX")
        if "XXXXXX" in pre or len(pre)==0:
            out="XXXXXX" # If any zone name is missing, cancel zone names and just use framing
        elif len(pre)==1: out=pre[0]
        else:
            pre[len(pre)-1]="AND "+pre[len(pre)-1]
            preme=str(pre).replace("[","").replace("]","").replace('"',"").replace("'","")
            if len(pre)==2: preme=preme.replace(", AND"," AND")
            out=preme
        return out
    except: flagMeBasic("Zone names did not list properly. Code:zonelistFull")
    return ""

# Determine Landmark Breakpoints Based on Zones
# Create area description phrase
def breakpointMe(info,wwa=""):
    # Setup some empty lists
    out={"watch":"","warning":"","advisory":""}
    others={"warning":["watch","advisory"],"watch":["warning","advisory"],"advisory":["watch","warning"]}
    # ZONE LISTING SCHEME - NO BREAKPOINTS
    if len(breakpoint_list)==0: # new deafult, everything is simple then
        if wwa=="":
            if active!="": wwa=active
            else: return []
        if len(info[wwa])==len(zone_ids): # simple! all zones plan A
            if localphrase=="": out[wwa]=zonelistFull(info[wwa])
            else: out[wwa]=localphrase
        else: # subset of zones.
            out[wwa]=zonelistFull(info[wwa])
        return out

    # BREAKPOINT SCHEME!
    # Format with breakpoint_list
    bpoints={}
    compass={}
    for b,arr in list(breakpoint_list.items()):
        bpoints[b]={}
        compass[b]=[]
        i=-1
        for a in arr:
            if len(a)!=6: # then compass direction
                i=i+1
                bpoints[b][i]=[]
                compass[b].append(a) # save directions for later
            else: bpoints[b][i].append(a)
    # Nothing Out
    if len(info["warning"])==0 and len(info["watch"])==0 and len(info["advisory"])==0: return out
    # Triple Combo...NO!!!!
    if len(info["warning"])!=0 and len(info["watch"])!=0 and len(info["advisory"])!=0: # all 3 hazards
        for wwa in ["watch","warning","advisory"]: out[wwa]=zonelistFull(info[wwa])
        return out
    for wwa in ["watch","warning","advisory"]:
        if len(info[wwa])!=0 and len(info[others[wwa][0]])==0 and len(info[others[wwa][1]])==0: # simple only one hazard
            if len(info[wwa])==len(zone_ids): # simple! all zones plan A
                if localphrase=="": out[wwa]="the local coastal areas"
                else: out[wwa]=localphrase
            else: # subset of zones. Can we use a breakpoint description? plan B
                for b,arr in list(bpoints.items()): # find which breakpoint matches
                    if 0 in arr and info[wwa]==arr[0] and b in compass: out[wwa]="coastal areas "+compass[b][0]+" of "+b
                    if 1 in arr and info[wwa]==arr[1] and b in compass: out[wwa]="coastal areas "+compass[b][1]+" of "+b
            if out[wwa]=="": # no breakpoint found...plan C (list all zones names)
                out[wwa]=zonelistFull(info[wwa])
            return out # remove "." for change zone function
        if len(info[wwa])==0 and len(info[others[wwa][0]])!=0 and len(info[others[wwa][1]])!=0: # otherwise must be 2 hazards
            wwa1=others[wwa][0]
            wwa2=others[wwa][1]
            # determine if all zones are covered (if complex where two products and two breakpoints...have to ignore)
            zct=0 # count up zones
            for z in zone_ids:
                if z in info[wwa1] or z in info[wwa2]: zct=zct+1
            if zct==len(zone_ids): # yes...all zones covered
                for b,arr in list(bpoints.items()): # find which breakpoint matches
                    if b in compass and 0 in arr and 1 in arr:
                        if info[wwa1]==arr[0]:
                            out[wwa1]="coastal areas "+compass[b][0]+" of "+b
                            out[wwa2]="coastal areas "+compass[b][1]+" of "+b
                        if info[wwa1]==arr[1]:
                            out[wwa1]="coastal areas "+compass[b][1]+" of "+b
                            out[wwa2]="coastal areas "+compass[b][0]+" of "+b
                if len(out[wwa1])==0 or len(out[wwa2])==0: # could not find a match
                    out[wwa1]=zonelistFull(info[wwa1])
                    out[wwa2]=zonelistFull(info[wwa2])
                return out
            else: # too comlpex...just list out all zones
                for wwa in ["watch","warning","advisory"]: out[wwa]=zonelistFull(info[wwa])
                #print "AAA"+zonelistFull(info[wwa])+"AAA"
                return out
    return out # must spit out something...not sure what this will be if it gets to this point!

# ===================== Options Functions ====================

def testMe(passit=0): # Add Test Wording to Statement
    global radiotext,alertframe
    saveMe()
    destroyBoxes()
    text=radiotext.get(0.0,END)
    if len(text)<25: # too short
        if passit==0:
            flagMeBasic("No Statement Loaded Yet.\nPlease load a statement before adding test wording.")
            highlightMe(radiotext,"TEST")
        return
    if text.find("TEST..")!=-1 and passit==0:
        if passit==0:
            flagMeBasic("Test wording already found in statement")
            highlightMe(radiotext,"TEST","#000","#ff0")
        return
    text="TEST...TEST...TEST...THIS IS ONLY A TEST.\n\n"+text.replace("TSUNAMI","TEST TSUNAMI")
    text+="\nREPEAT...THIS WAS A TEST...THIS WAS ONLY A TEST. THERE IS NO TSUNAMI DANGER AT THIS TIME."
    radiotext.delete(0.0,END)
    radiotext.insert(0.0,text.strip())

def untestMe(passit=0): # Remove Test Wording to Statement
    global radiotext,alertframe
    saveMe()
    destroyBoxes()
    text=radiotext.get(0.0,END)
    if len(text)<25: # too short
        if passit==0:
            flagMeBasic("No Statement Loaded Yet.")
            highlightMe(radiotext,"TEST")
        return
    if text.find("TEST")==-1 and passit==0:
        if passit==0: flagMeBasic("No Test wording found in statement")
        return
    text=text.replace("TEST...TEST...TEST...THIS IS ONLY A TEST.\n\n","")
    text=text.replace("TEST...TEST...TEST...THIS IS ONLY A TEST.\n","")
    text=text.replace("TEST TSUNAMI","TSUNAMI")
    text=text.replace("\nREPEAT...THIS WAS A TEST...THIS WAS ONLY A TEST. THERE IS NO TSUNAMI DANGER AT THIS TIME.","")
    radiotext.delete(0.0,END)
    radiotext.insert(0.0,text.strip())

def highlightMe(what,s,fg="#000",bg="#fff"): # visually hightlight text in statement window
    what.tag_remove("framing",0.0,END)
    index="0.0"
    while 1:
        index=what.search(s,index,nocase=1,stopindex=END)
        if not index: break
        tmp=index.split(".")
        if len(tmp)<2: break
        lastindex=tmp[0]+"."+str(int(tmp[1])+len(s))
        what.tag_add("framing",index,lastindex)
        index=lastindex
    what.tag_config("framing",foreground=fg,background=bg)
    return what

def modemeLabelChange(): # change settings when going to live/practice/test mode
    global modeme,mainLabels,bgmain,fontsize
    mainLabels["modeme"].configure(text=modeme.title()+" Mode")
    fg="#000"
    if modeme=="practice":
        bgmain=bgmain_list["practice"]
        mainLabels["modeme"].configure(fg="#fff",font=('Arial',str(int(fontsize)+10),'bold'))
        if "bt_eas" in mainLabels: mainLabels["bt_eas"].configure(text=" Pretend Send to Radio ")
    elif modeme=="test":
        bgmain=bgmain_list["test"]
        mainLabels["modeme"].configure(fg="#c00",font=('Arial',str(int(fontsize)+10),'bold'))
        if "bt_eas" in mainLabels: mainLabels["bt_eas"].configure(text=" Send to Radio as Test")
        fg="#aaa"
    else:
        bgmain=bgmain_list["live"]
        mainLabels["modeme"].configure(fg="#c33",font=('Arial',str(int(fontsize)+10),'bold'))
        if "bt_eas" in mainLabels: mainLabels["bt_eas"].configure(text=" Send to Radio ")
    # change colors
    root.configure(bg=bgmain)
    for a,b in list(mainLabels.items()):
        try:
            if a[0:3]=="no_": continue
            if a[0:3]!="bt_":
                mainLabels[a].configure(bg=bgmain)
                if a!="title": mainLabels[a].configure(fg=fg)
            else: mainLabels[a].configure(highlightbackground=bgmain) # special case for buttons
        except: aaa=1

# toggle modes
def toggleMode():
    global modeme, modeme_options, optset
    destroyBoxes()
    # Check Awips mode
    modemeAwips=checkAwipsMode()
    # Hard Rules
    if modemeAwips=="practice": modeme="practice"
    # Toggle
    if modeme=="live" or modeme=="test": modeme="practice"
    elif modeme=="practice":
        if modemeAwips=="live": modeme="live"
        if modemeAwips=="test": modeme="test"
    #untestMe(1)
    if modeme=="test": testMe(1)
    modemeLabelChange()

# Action to change autofill settings
def autofillChangesettings():
    global optset,wcapil,mainLabels,alertframe
    version=""
    wcapil_display=""
    if "version" in optset and optset["version"]!="":
        version=str(optset["version"].get())+":"
        if version=="Latest:": version=""
    if "wcapil" in optset and optset["wcapil"]!="":
        wcapil=version+str(optset["wcapil"].get())
        wcapil_display=version+str(optset["wcapil"].get()).upper() # mirror for button
        if wcapil.find("/")!=-1 or wcapil.find(".")!=-1: # local file for testing
            tmp=wcapil.split("/")
            tmp2=tmp[len(tmp)-1].split(".")
            if len(tmp2)>0: wcapil_display=tmp2[0]
        else: wcapil=wcapil.upper()
    mainLabels["bt_viewStatement"].configure(text=" View "+wcapil_display.replace(teststatementdir,"")+" Statement")
    #mainLabels["bt_autofill"].configure(text=" Autofill from "+wcapil_display+" ")
    destroyBoxes()
    autoFill()

# Box to change autofill settings
def autofillSettings():
    global alertframe,wcapil,optset
    saveMe()
    destroyBoxes()
    bg="#eee"
    wcapil2=wcapil
    version="Latest"
    if wcapil2.find(":")!=-1: # older version
        tmp=wcapil2.split(":")
        if len(tmp)>1:
            wcapil2=tmp[1]
            version=tmp[0]
            if version=="0": version="Latest"
            if version.find("-")==-1 and version!="Latest": version="-"+version
    alertframe=Toplevel()
    alertframe.title('Autofill Settings')
    alertframe.configure(bg=bg)
    alertframe.option_add('*Font', ('Arial',12))
    alertframe.geometry(windowXgeo())
    Label(alertframe,text="Product PIL for Autofill to parse",bg=bg,fg="#00a").grid(row=0,column=0,sticky=E)
    optset["wcapil"]=Entry(alertframe,width=25)
    noteme=Label(alertframe,text="Note",bg=bg,fg="#00a",font=('Arial','8'))
    noteme.grid(row=0,column=2,sticky=E)
    ToolTip(noteme,text="For testing purposes, you may add a path name.")
    optset["wcapil"].grid(row=0,column=1,sticky=W)
    optset["wcapil"].delete(0,END)
    optset["wcapil"].insert(0,str(wcapil2))
    Label(alertframe,text="PIL Version To Use",bg=bg,fg="#00a").grid(row=1,column=0,sticky=E)
    optset["version"]=StringVar()
    OptionMenu(alertframe,optset["version"],*["Latest","-1","-2","-3","-4","-5"]).grid(row=1,column=1,sticky=W)
    optset["version"].set(version)
    Button(alertframe,text="  Save  ",command=autofillChangesettings,bg="#37c",fg="#fff").grid(row=2,column=0,sticky=E)
    Button(alertframe,text="  Cancel  ",command=alertframe.destroy,bg="#000",fg="#fff").grid(row=2,column=1,sticky=W)

# Load latest TSUWCA product (after loading an example statement)
def loadLatestStatement():
    global wcapil
    wcapil=wcapil_raw
    mainLabels["bt_viewStatement"].configure(text=" View "+wcapil+" Statement")
    #if modeme=="practice": toggleMode() # force to practice mode
    autoFill()

# Action to load example statement
def loadExampleGo():
    global optset,wcapil,mainLabels,modeme
    wcapil_display=""
    if 1:
    #try:
        if "wcapil" in optset and optset["wcapil"]!="":
            wcapil=teststatementdir+str(optset["wcapil"].get())
            wcapil_display=str(optset["wcapil"].get()).upper() # mirror for button
            if wcapil.find("/")!=-1 or wcapil.find(".")!=-1: # local file for testing
                tmp=wcapil.split("/")
                tmp2=tmp[len(tmp)-1].split(".")
                if len(tmp2)>0: wcapil_display=tmp2[0]
            else: wcapil=wcapil.upper()
        mainLabels["bt_viewStatement"].configure(text=" View "+wcapil_display.replace(teststatementdir,"")+" Statement")
        destroyBoxes()
        if modeme=="live": toggleMode() # force to practice mode
        autoFill()   # Grab Information
    #except: flagMeBasic("The example statement failed to load. code:loadExampleGo")

# Load Example Statement from list
def loadExampleStatement():
    global alertframe,optset
    saveMe()
    destroyBoxes()
    bg="#eee"
    alertframe=Toplevel()
    alertframe.title('Load Example Statement')
    alertframe.configure(bg=bg)
    alertframe.option_add('*Font', ('Arial',12))
    alertframe.geometry(windowXgeo())
    Label(alertframe,text="Select Example Statement",bg=bg,fg="#00a").grid(row=0,column=0,columnspan=2,padx=gp)
    noteme=Label(alertframe,text="Note",bg=bg,fg="#00a",font=('Arial','8'))
    noteme.grid(row=0,column=2,sticky=E,padx=gp)
    ToolTip(noteme,text="The statements in the list below are pulled from txt files found in the examples directory.")
    # Create list from the tsuwca_examples directory
    arr2=os.listdir(teststatementdir)
    arr2.sort()
    arr=[]
    for a in arr2:
        if not os.path.isdir(teststatementdir+a): arr.append(a)

    optset["wcapil"]=StringVar()
    OptionMenu(alertframe,optset["wcapil"],*arr).grid(row=1,column=0,columnspan=3,pady=gp)
    if len(arr)>0: optset["wcapil"].set(arr[0])
    Button(alertframe,text="  Load  ",command=loadExampleGo,bg="#37c",fg="#fff").grid(row=2,column=0,sticky=W)
    Button(alertframe,text="  Cancel  ",command=alertframe.destroy,bg="#000",fg="#fff").grid(row=2,column=1,columnspan=2)

# see BMH script before sending out
def previewBMH():
    global alertframe,active,radiotext
    saveMe()
    destroyBoxes()
    bg="#ff0"
    # Create Text
    text=radiotext.get(0.0,END)
    # Make sure we have text to use
    if len(text)<10:
        flagMeBasic("Text is missing from the main text window. Nothing to preview.")
        return
    # Format BMH Header and Send Out
    crsout=crsCodeme(active,text)
    #print local_dir+"data/tsradio.txt"
    tmpFile = os.path.join(local_dir, "data", "tsradio.txt")
    pp=open(tmpFile, "w")  # write to temporary file. useful for printing
    pp.write(crsout)
    pp.close()
    # Set the mode so that other users in the AWIPS group can write to the file later.
    try:
        os.chmod(tmpFile, 0o664)
    except:
        logging.exception("(nonfatal) Could not change mode for file {0}".format(tmpFile))

    # display
    alertframe1=Toplevel()
    alertframe1.title('BMH Radio Script Preview')
    alertframe1.option_add('*Font', ('Arial',12))
    alertframe1.geometry(windowXgeo())
    Label(alertframe1,text="Script NOT sent to NWR/EAS!\nYou are only previewing...",bg="#cdf",fg="#000").grid(row=0,column=0,columnspan=2)
    text=Text(alertframe1,height=22,width=72,bg="#004",fg="#fff",font=('Courier','12'),wrap=WORD)
    scroll=Scrollbar(alertframe1,command=text.yview)
    text.configure(yscrollcommand=scroll.set)
    text.grid(row=1,column=0)
    scroll.grid(row=1,column=1,sticky=N+S+E)
    text.delete(0.0,END)
    text.insert(0.0,crsout)
    bframe=Frame(alertframe1)
    bframe.grid(row=2,column=0,columnspan=2)
    Button(bframe,text="   Close   ",command=alertframe1.destroy,bg="#000",fg="#fff").grid(row=0,column=0)
    Button(bframe,text="   Print   ",command=printBMH,bg="#afa",fg="#000").grid(row=0,column=1)

# Print source tsunami bulletin.
def printTSU():
    global wcapil
    cmd=db+" -r "+wcapil
    tsuin=os.popen(cmd).read()
    tmpFile = os.path.join(local_dir, "data", "TSUWCA.txt")
    pp=open(tmpFile, "w")  # write to temporary file.
    pp.write(tsuin)
    pp.close()
    # Set the mode so that other users in the AWIPS group can write to the file later.
    try:
        os.chmod(tmpFile, 0o664)
    except:
        logging.exception("(nonfatal) Could not change mode for file {0}".format(tmpFile))

    pipe=subprocess.getoutput("lp " + tmpFile) # use commands to remove a2ps output

# launch point to view TSU product
def showTSU():
    global alerframe,wcapil
    loadTSU(wcapil)

# To fill ViewTSU
def loadTSU(wcapil2):
    global alertframe
    # For stored examples
    if wcapil2.find("/")!=-1 or wcapil2.find(".")!=-1:
        if not os.path.isfile(wcapil2): # Fail!
            flagMeBasic("Specified Input File Not. Try changing file or PIL in the Autofill options.")
            return info
        with open(wcapil2,"r") as f:  # open file and read
            tsuin = f.read().replace("\r","")
    else: # read from textDB
        if wcapil2.find(":")!=-1: # previous version
            tmp=wcapil2.replace("-","").split(":")
            rawpil=tmp[1]
        cmd=db+" -r "+wcapil2
        tsuin=os.popen(cmd).read()
    if tsuin=="": tsuin="No Statement Available"
    viewTSU(tsuin,wcapil2)

# View loaded TSUWCA statement
def viewTSU(tsuin,wcapil2):
    global alertframe
    destroyBoxes()
    # Start GUI Box
    bg="#ff0"
    alertframe=Toplevel()
    alertframe.title('View Product')
    alertframe.option_add('*Font', ('Arial',12))
    alertframe.geometry(windowXgeo())
    Label(alertframe,text="View "+wcapil2.replace(teststatementdir,"")+" Statement",fg="#000").grid(row=0,column=0,columnspan=3)
    bsmallframe=Frame(alertframe) # Frame just for big buttons
    bsmallframe.grid(row=0,column=0,sticky=W)
    # Small Buttons
    # Previous Version if needed
    if 1:#try:
        if wcapil2.find("/")==-1 and wcapil2.find(".")==-1: # NOT loaded statement
            earlier=""
            later=""
            if wcapil2.find(":")!=-1: # find version number
                tmp=wcapil2.replace("-","").split(":")  #0:version, 1:pil
                version=int(tmp[0])
                earlier="-"+str(version+1)+":"+tmp[1]
                Button(bsmallframe,text="Earlier",command=lambda i=earlier:loadTSU(i),\
                   fg="#000",relief=FLAT,font=('Arial','10')).grid(row=0,column=0)
                if version>0:
                    later="-"+str(version-1)+":"+tmp[1]
                    Button(bsmallframe,text="Later",command=lambda i=later:loadTSU(i),\
                       fg="#000",relief=FLAT,font=('Arial','10')).grid(row=0,column=1)
                else: Button(bsmallframe,text="Later",fg="#999",relief=FLAT,font=('Arial','10')).grid(row=0,column=1)
            else: # must be latest version
                earlier="-1"+":"+wcapil2
                Button(bsmallframe,text="Earlier",command=lambda i=earlier:loadTSU(i),\
                   fg="#000",relief=FLAT,font=('Arial','10')).grid(row=0,column=0)
                Button(bsmallframe,text="Later",fg="#999",relief=FLAT,font=('Arial','10')).grid(row=0,column=1)
        else: # loaded example statement
            arr2=os.listdir(teststatementdir)
            arr2.sort()
            arr=[]
            k=0
            ver=0 # keep track of where loaded statement falls in array
            for a in arr2:
                if not os.path.isdir(teststatementdir+a):
                    arr.append(teststatementdir+a)
                    if (teststatementdir+a)==wcapil2: ver=k
                    k+=1
            if ver>0:
                earlier=arr[ver-1]
                Button(bsmallframe,text="Previous",command=lambda i=earlier:loadTSU(i),\
                   fg="#000",relief=FLAT,font=('Arial','10')).grid(row=0,column=0)
            else: Button(bsmallframe,text="Previous",fg="#999",relief=FLAT,font=('Arial','10')).grid(row=0,column=0)
            if ver<len(arr)-1:
                later=arr[ver+1]
                Button(bsmallframe,text="Next",command=lambda i=later:loadTSU(i),\
                   fg="#000",relief=FLAT,font=('Arial','10')).grid(row=0,column=1)
            else: Button(bsmallframe,text="Next",fg="#999",relief=FLAT,font=('Arial','10')).grid(row=0,column=1)
    #except: a=1
                # Print
    Button(bsmallframe,text="Print",command=printTSU,fg="#000",relief=FLAT,font=('Arial','10')).grid(row=0,column=2)
    # Big Buttons
    bframe=Frame(alertframe) # New Frame
    bframe.grid(row=1,column=0,columnspan=3)
    Button(bframe,text=" Load Statement To Main Window",command=lambda i=wcapil2:autoFillPop(i),bg="#000",fg="#fff").grid(row=0,column=0)
    Button(bframe,text="   Close   ",command=alertframe.destroy,bg="#000",fg="#fff").grid(row=0,column=1)
    text=Text(alertframe,height=45,width=72,bg="#004",fg="#fff",font=('Courier','12'),wrap=WORD)
    scroll=Scrollbar(alertframe,command=text.yview)
    text.configure(yscrollcommand=scroll.set)
    text.grid(row=2,column=0,columnspan=2)
    scroll.grid(row=2,column=3,sticky=N+S+E)
    # Pump Text To Window
    text.delete(0.0,END)
    text.insert(0.0,tsuin.strip())

# Spell Check Interface
def spellMe():
    global radiotext,spell,alertframe
    destroyBoxes()
    spell={}
    try:
        out=""
        text=radiotext.get(0.0,END)
        words=re.compile("([A-Za-z]+)").findall(text) # add colons to times
        for w in words:
            pipe=subprocess.getoutput("echo "+w+" | ispell -a") # Pipe to built-in spell checker
            tmp=pipe.split(":")
            if str(pipe).find("command not found")!=-1: # ispell is not istalled :(
                flagMeBasic("Sorry, but iSpell is not installed on this machine :(")
                return
            if len(tmp)>1: spell[w]=tmp[1].strip().split(",") #hit
        # Any hits?
        if len(spell)==0: flagMeBasic("No Spelling Errors Found","Congratulations!")
        spellAction()
    except: flagMeBasic("Spell Checker Failed. Code:spellMe")

# Spell Checker Dialog Box
def spellBox(word,suggestions):
    global alertframe,mainLabels,radiotext
    saveMe()
    destroyBoxes()
    alertframe=Toplevel()
    alertframe.title('Spelling Error Detected...')
    alertframe.option_add('*Font', ('Arial',12))
    alertframe.geometry(windowXgeo())
    Label(alertframe,text=word,bg="#ff5").grid(row=0,column=0,columnspan=3,padx=gp,pady=gp)
    mainLabels["spell"]=Listbox(alertframe,height=5)
    scroll=Scrollbar(alertframe,command=mainLabels["spell"].yview)
    mainLabels["spell"].configure(yscrollcommand=scroll.set)
    mainLabels["spell"].grid(row=1,column=0,columnspan=2,padx=gp,pady=gp)
    scroll.grid(row=1,column=3,sticky=N+S+E)
    radiotext=highlightMe(radiotext,word,"#03f","#ff0")
    for s in suggestions:
        mainLabels["spell"].insert(END,s.strip())
    Button(alertframe,text="Replace All",command=lambda i=word:spellReplace(i),bg="#37c",fg="#fff").grid(row=2,column=0,padx=gp,pady=gp)
    Button(alertframe,text="Ignore All",command=spellAction,bg="#000",fg="#fff").grid(row=2,column=1,padx=gp,pady=gp)

# Find Misspelled Words
def spellAction():
    global spell,mainLabels,radiotext,alertframe
    destroyBoxes()
    try:
        radiotext.tag_remove("framing",0.0,END)
        if len(spell)==0: # All Done!
            flagMeBasic("Spell Check Completed.","Congratulations!")
            return
        tmp=list(spell.keys())
        word=tmp[0] # word to check now
        suggestions=spell[word]
        del spell[word] # remove from mainlist
        spellBox(word,suggestions) # call for user input
        mainLabels["spell"].selection_set(0) # activate first selection
    except: flagMeBasic("Spell Checker Failed. Code:spellMe")

# Perform Spell Substitution
def spellReplace(w):
    global alertframe,radiotext,mainLabels
    text=radiotext.get(0.0,END)
    if w=="" or text.find(w)==-1:
        spellAction()
        return
    radiotext.delete(0.0,END)
    replace=mainLabels["spell"].get(ACTIVE).upper()
    text=text.replace(w,replace)
    radiotext.insert(0.0,text)
    spellAction()

# ======================== Dialog Boxes =============================
# Get rid of all dialog boxes if they are open
def destroyBoxes():
    global alertframe,alertframe1
    try: alertframe.destroy()
    except: a=1 #print "no"
    try: alertframe1.destroy()
    except: a=1 #print "no"

# Warning Box if errors were detected at end of process
def flagMeSend(mesg): # window to display errors
    global alertframe
    destroyBoxes()
    alertframe=Toplevel()
    alertframe.title('There are potential problems that need you attention.')
    alertframe.configure(bg="#ccc")
    alertframe.option_add('*Font', ('Arial',windowfont))
    alertframe.geometry(windowXgeo())
    ftext=Text(alertframe,height=5,width=71,bg="#004",fg="#fff",wrap=WORD)
    scroll=Scrollbar(alertframe,command=ftext.yview)
    ftext.configure(yscrollcommand=scroll.set)
    ftext.grid(row=0,column=0,columnspan=2)
    scroll.grid(row=0,column=2,sticky=E+N+S)
    ftext.delete(0.0,END)
    ftext.insert(0.0,mesg)
    Button(alertframe,text="Go Back And Fix",command=alertframe.destroy,bg="#ddd",fg="#000").grid(row=1,column=0,padx=gp,pady=gp)
    Button(alertframe,text="Continue Anyway",command=almostsendtoRadio,bg="#ddd",fg="#000").grid(row=1,column=1,padx=gp,pady=gp)
    logging.error(mesg)


def flagMeBasic(msg,title="Attention...",prompt=0): # window to display errors
    global alertframe
    destroyBoxes()
    alertframe=Toplevel()
    alertframe.title(title)
    alertframe.configure(bg="#ccc")
    alertframe.option_add('*Font', ('Arial',windowfont))
    alertframe.geometry(windowXgeo())
    ftext=Text(alertframe,height=5,width=71,bg="#004",fg="#fff",wrap=WORD)
    scroll=Scrollbar(alertframe,command=ftext.yview)
    ftext.configure(yscrollcommand=scroll.set)
    ftext.grid(row=0,column=0,columnspan=2)
    scroll.grid(row=0,column=2,sticky=N+S+E)
    ftext.delete(0.0,END)
    ftext.insert(0.0,msg)
    if prompt==0: Button(alertframe,text="   Ok   ",command=alertframe.destroy,bg="#ccc",fg="#000").grid(row=1,column=0,columnspan=3,padx=gp,pady=gp)
    elif str(prompt).isdigit():
        Button(alertframe,text="Cancel",command=alertframe.destroy,bg="#ccc",fg="#000").grid(row=1,column=0,padx=gp,pady=gp)
        Button(alertframe,text="Continue Anyway",command=lambda i="test":autoFill(i),bg="#37c",fg="#fff").grid(row=1,column=1,padx=gp,pady=gp)
    else:
        Button(alertframe,text="Cancel",command=alertframe.destroy,bg="#ccc",fg="#000").grid(row=1,column=0,padx=gp,pady=gp)
        Button(alertframe,text=str(prompt),command=lambda i="test":autoFill(i),bg="#37c",fg="#fff").grid(row=1,column=1,padx=gp,pady=gp)
    logging.error(msg)


def sendtoRadioCheck():
    global alertframe,radiotext
    saveMe() # autosave in case something breaks
    destroyBoxes()
    err=""
    try:
        text=radiotext.get(0.0,END)
        if len(text)<20: # check if something is in there
            err+="Please make sure that there is text in the main window\n\n"
        if text.find("none are detected")!=-1: # check is a warning or watch is in the text box
            err+="No hazard was found in the main text window. "
            if advisory_handle=="no": err+="Only warnings and watches are EAS'd\n\n"
        if text.find("XXX")!=-1: # framing code still there
            radiotext.tag_remove("framing",0.0,END)
            radiotext=highlightMe(radiotext,"XXX","#000","#ff0")
            err+="Framing code 'XXXXXX' was found in the main text window. Make sure to replace these with relevant info.\n\n"
        arr=[]
        for c,zlist in list(county_list.items()):
            for z,a in list(bt_zones.items()):
                if a.get()==1 and z in zlist and c not in arr: arr.append(c)
        if len(arr)<=0:
            err+="No zones selected or zone/counties associatations are not configured correctly\n\n"
    except: err+="Radio Check Failed. Code:sendtoRadioCheck\n\n"
    if err!="":
        flagMeSend(err)
        return
    almostsendtoRadio() # made it this far...must assume its good

def almostsendtoRadio():
    global alertframe,modeme,active,mainLabels,counter
    saveMe()
    destroyBoxes()
    counter=5 # for count down later
    bg="#ff0"
    alertframe=Toplevel()
    alertframe.title('Are You Sure!?!?!?!')
    alertframe.configure(bg=bg)
    alertframe.option_add('*Font', ('Arial',12))
    alertframe.geometry(windowXgeo())
    if modeme=="live": # Send to radio with tones
        if active.find("warning")!=-1 or active.find("watch")!=-1:
            Label(alertframe,text="YOU ARE IN LIVE MODE!!!",bg=bg,fg="#000",font=('Arial',str(int(fontsize)+6)),justify=CENTER).grid(row=0,column=0,padx=gp,pady=gp)
            mainLabels["no_send"]=Button(alertframe,text=" Go Ahead...Send To Radio! ",command=countdowntoRadio,bg="#c00",fg="#ff9")
            mainLabels["no_send"].grid(row=2,column=0,padx=gp,pady=gp)
        else: # send to radio without tones
            Label(alertframe,text="YOU ARE IN LIVE MODE!\n Pressing Yes WILL send message\n to the radio!!!",bg=bg,fg="#000",font=('Arial',str(int(fontsize)+6))).grid(row=0,column=0,padx=gp,pady=gp)
            mainLabels["no_send"]=Button(alertframe,text="  Go Ahead...Send To Radio!  ",command=countdowntoRadio,bg="#c00",fg="#ff9")
            mainLabels["no_send"].grid(row=2,column=0,padx=gp,pady=gp)
    else: # pretend to send to radio
        bg="#afc"
        alertframe.configure(bg=bg)
        Label(alertframe,text="YOU ARE IN PRACTICE MODE.\n NOTHING will get sent to the Radio.",bg=bg,fg="#000").grid(row=0,column=0,padx=gp,pady=gp)
        mainLabels["no_send"]=Button(alertframe,text="  PRETEND to Send to Radio!  ",command=countdowntoRadio,bg="#c00",fg="#fff")
        mainLabels["no_send"].grid(row=2,column=0,padx=gp,pady=gp)
    Button(alertframe,text="\n  Abort...I made a mistake!  \n ",command=abortCountdown,bg="#ddd",fg="#000").grid(row=4,column=0,padx=gp,pady=gp)
    mainLabels["no_timerLabel"]=Label(alertframe,text="",bg=bg,fg="#700",font=('Arial',str(int(16))))
    mainLabels["no_timerLabel"].grid(row=1,column=0,padx=gp,pady=gp)

# ====================== Autosave/Tracking Functions ========================================

# Fill in text window with last autosave text
def restoreMe():
    global radiotext,bt_zones,active,combo_need
    try:
        with open(local_dir+"data/autosave.txt",'r') as txtFile:
            full = txtFile.read()
        arr=full.split("|||")
        # Zone Buttons
        hitz=arr[0].split(",")
        for z in zone_ids:
            if z in hitz: bt_zones[z].set(1)
            else: bt_zones[z].set(0)
        # Text Box
        out=arr[1]
        radiotext.delete(0.0,END)
        radiotext.insert(0.0,out.upper().strip())
        radiotext=highlightMe(radiotext,"XXX","#03f","#fff")
        # define some important features
        combo_need=[]
        wwarr=[]
        if out.find("WARNING")!=-1: wwarr.append("warning")
        if out.find("WATCH")!=-1: wwarr.append("watch")
        if out.find("ADVISORY")!=-1: wwarr.append("advisory")
        active=""
        for wwa in wwarr:
            if len(wwarr)>1: combo_need
            active+=wwa+"_"
        active=active[:-1]
        saveMe()
    except: flagMeBasic("Restore function failed. Code:restoreMe")

# Autosave Radiotext
def saveMe():
    global radiotext,bt_zones
    try:
        out=""
        text=radiotext.get(0.0,END)
        if len(text)<30: return # make sure something worthwhile is there to save
        for z,a in list(bt_zones.items()):
            if a.get()==1: out+=z+","
        out+="|||"
        out+=text
        tmpFile = os.path.join(local_dir, "data", "autosave.txt")
        with open(tmpFile,"w") as pp:  # write to temporary file
            pp.write(out)

        # Set the mode so that other users in the AWIPS group can write to the file later.
        try:
            os.chmod(tmpFile, 0o664)
        except:
            logging.exception("(nonfatal) Could not change mode for file {0}".format(tmpFile))
    except:
        logging.exception("saveMe Failed")

def closeMe():
    saveMe()
    root.destroy()
    sys.exit(0)

# Clears out last_issued.txt, to start over in tracking WWA issued
def clearTrack():
    global alertframe,combo_issued
    saveMe()
    destroyBoxes()
    try:
        combo_issued=[]
        tmpFile = os.path.join(local_dir, "data", "last_issued.txt")
        with open(tmpFile, "w") as pp:  # create and write to temporary file
            pp.write("")

        # Set the mode so that other users in the AWIPS group can write to the file later.
        try:
            os.chmod(tmpFile, 0o664)
        except:
            logging.exception("(nonfatal) Could not change mode for file {0}".format(tmpFile))

        bg="#eee"
        alertframe=Toplevel()
        alertframe.title('Success!')
        alertframe.option_add('*Font', ('Arial',12))
        alertframe.geometry(windowXgeo())
        Label(alertframe,text="  Tracking Cleared   ",fg="#000").grid(row=0,column=0,padx=gp,pady=gp)
        Button(alertframe,text="   OK   ",command=alertframe.destroy,bg="#000",fg="#fff").grid(row=1,column=0,padx=gp,pady=gp)
    except: flagMeBasic("Tracking failed to clear. Code:clearTrack")

# ====================== Main Scripts to Generate/Send Radio Formatted Statements ===============================
# Open NWR Browser
def openNWRBrowser():
    cmd2="" # find the NWR Browser
    if os.path.exists("/awips/adapt/NWRWAVES/browser/AWIPS2-browser.tcl"): cmd2="/awips/adapt/NWRWAVES/browser/AWIPS2-browser.tcl"
    elif os.path.exists("/awips/adapt/NWRWAVES/browser/AWIPS1-browser.tcl"): cmd2="/awips/adapt/NWRWAVES/browser/AWIPS1-browser.tcl"
    elif os.path.exists("/awips/adapt/NWRWAVES/browser/AWIPS-browser.tcl"): cmd2="/awips/adapt/NWRWAVES/browser/AWIPS-browser.tcl"
    try:
        if cmd2!="": os.system(cmd2)
        else: logging.error("NWR Browser Not Found")
    except:
        logging.exception("NWSBrowser did not open")

def zoneMe():
    global bt_zones
    try:
        zones=[]
        for z,a in list(bt_zones.items()):
            if a.get()==1: zones.append(z)
        z=str(zones).replace("[","").replace("]","").replace('"',"").replace("'","").replace(",","-").replace(" ","").replace("\n","")
        return z
    except: flagMeBasic("Zone name listing failed. Code:zoneMe")
    return ""

def countyMe():
    global bt_zones
    try:
        counties=[]
        for c,zlist in list(county_list.items()):
            for z,a in list(bt_zones.items()):
                if a.get()==1 and z in zlist and c not in counties: counties.append(c)
        counties=str(counties).replace("[","").replace("]","").replace('"',"").replace("'","").replace(",","-").replace(" ","").replace("\n","")
        return counties
    except: flagMeBasic("County name listing failed. Code:countyMe")
    return ""

def highestPri(active,text):
    wwa=""
    if active.find("warning")!=-1: wwa="warning"
    elif active.find("watch")!=-1: wwa="watch"
    elif active.find("advisory")!=-1: wwa="advisory"
    else: # plan B
        if text.find("REPEAT, TSUNAMI WARNING")!=-1: wwa="warning"
        elif text.find("REPEAT, TSUNAMI WATCH")!=-1: wwa="watch"
        elif text.find("REPEAT, TSUNAMI ADVISORY")!=-1: wwa="advisory"
        elif text.find("TSUNAMI WARNING")!=-1: wwa="warning"  # plan c
        elif text.find("TSUNAMI WATCH")!=-1: wwa="watch"
        elif text.find("TSUNAMI ADVISORY")!=-1: wwa="advisory"
        else: wwa="warning" # gotta do something, assume the worst
    return wwa

def crsDatestring(dt):
    return str(dt.year)[2:]+str(dt.month).zfill(2)+str(dt.day).zfill(2)+str(dt.hour).zfill(2)+str(dt.minute).zfill(2)

# Create BMH Code header string and text
def crsCodeme(active,text):
    global optset
    todayutc=datetime.datetime.utcnow() # UTC time (awips already default to UTC). need to assign it now because GUI may be opened for a long time
    out="\x1baT_ENG"
  # First Determine Highest order Hazard
    try:
        wwa=highestPri(active,text)
    # Next Make BMH Header Code String
        #$aT_ENGLAXHWRCPK12112821121211282112        CD IACAC079c1211282220
        #if "crslanguage" in optset and optset["crslanguage"].get()=="Spanish": out="\x1baT_SPA"+node
        #else: out="\x1baT_ENG"+node
        if wwa=="advisory":
            if len(advisoryPIL)<7: out+=node+advisoryPIL
            else: out+=advisoryPIL
        elif wwa=="watch":
            if len(watchPIL)<7: out+=node+watchPIL
            else: out+=watchPIL
        else:
            if len(warningPIL)<7: out+=node+warningPIL
            else: out+=warningPIL
        ctime=crsDatestring(todayutc)
        out+=ctime+ctime # create time and effective time
        if wwa=="advisory" or masterTone==0: out+="        CD IN" # no tone alert
        else: out+="        CD IA" #periodicity, active, delete, msg confirm, interupt, tonealert.
        # county string
        out+=countyMe()
        # Expiration
        out+="c"
        tchange=15
        if "crsexpire" in optset:
            exp=optset["crsexpire"].get().replace("mins","").replace(" ","")
            forwardtime=todayutc+datetime.timedelta(minutes=int(exp))
        else: forwardtime=todayutc+datetime.timedelta(minutes=15)
        out+=crsDatestring(forwardtime)
        header=out
    # Now make main text
        # Post Process. Format Text
        text=(text.replace("\n"," ").replace("...",", ").replace("  "," ").replace("  "," ")).upper() # simple fixes
            # add colons to times
        tmp=re.compile("(\d+) (AM|PM)").findall(text) # add colons to times
        for a in tmp: # [('939', 'AM'), ('808', 'AM'), ('723', 'AM')]
            if len(a)>1 and len(a[0])>2: # if already has : re will only find minutes
                replA=a[0]+" "+a[1] # what to replace
                if len(a[0])>3: replB=a[0][0:2]+":"+a[0][2:]+" "+a[1] # four digit time
                else: replB=a[0][0:1]+":"+a[0][1:]+" "+a[1] # four digit time
                text=text.replace(replA,replB)
            # make dates nice
        tmp=re.compile("(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER) ([0-9][0-9])").findall(text)
        for a in tmp: # [('APRIL', '16'), ('APRIL', '17')]
            if len(a)>1:
                replA=a[0]+" "+a[1] # what to replace
                replB=a[0]+" "+niceFirst(a[1]).upper()
                text=text.replace(replA,replB)
        tmp=re.compile("(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER) ([0-9])").findall(text)
        for a in tmp: # [('APRIL', '16'), ('APRIL', '17')]
            if len(a)>1:
                replA=a[0]+" "+a[1] # what to replace
                replB=a[0]+" "+niceFirst(a[1]).upper()
                text=text.replace(replA,replB)
        out=header+"\n"+text.replace("\n","")+"\x1bb"
        return out
    except: flagMeBasic("BMH formatting failed. Code:crsCodeMe")
    return ""

# Timer Function
def timerMe():
    global afterme
    afterme=root.after(1000,countdowntoRadio)

# Cancel Countdown and Interim Popup
def abortCountdown():
    global counter,afterme,alertframe
    try: root.after_cancel(afterme) # cancel loop, not present if transmit not started
    except: a=1
    alertframe.destroy() # close window

# Countdown Timer Last Resort to Send to Radio
# From almostsendtoRadio
def countdowntoRadio():
    global mainLabels,counter
    mainLabels["no_send"].configure(state=DISABLED) # turn off send button while timer is going
    mainLabels["no_timerLabel"].configure(text="Transmit in "+str(counter)) # Visual Timer
    counter-=1
    if counter>=0: timerMe() # bounce back to timerMe
    else: sendtoRadio() # Go ahead with it

# Grab text from window and format for BMH
def sendtoRadio():
    global alertframe,alertframe1,modeme,active,combo_need,combo_issued,optset,mainLabels
    todayutc=datetime.datetime.utcnow() # UTC time (awips already default to UTC)

    if modeme!="wait": destroyBoxes()
    try:
        # Get text from window
        text=radiotext.get(0.0,END)
    # Format BMH Header and Send Out
        crsout=crsCodeme(active,text)
        logging.info("SEND BMH SCRIPT GENERATED:\n"+crsout)
        alertframe1=Toplevel()
        alertframe1.title('Complete!')
        alertframe1.option_add('*Font', ('Arial',12))
        tmpFile = os.path.join(local_dir, "data", "tsradio.txt")
        with open(tmpFile, "w") as pp:  # write to temporary file
            pp.write(crsout)

        # Set the mode so that other users in the AWIPS group can write to the file later.
        try:
            os.chmod(tmpFile, 0o664)
        except:
            logging.exception("(nonfatal) Could not change mode for file {0}".format(tmpFile))

    # Time to send away!
        cmd=""
        if modeme=="live": # !!!!!!!!!!!!!!!!! THIS IS THE BIG COMMAND THAT SENDS TO THE RADIO!!!!!!!!!!!!!!!!!!!!!!!
            a=1
            if pending=="no": cmd=nwrsend+" -a "+local_dir+"data/tsradio.txt" # -a automatically transfer, -d pending directory
            else: cmd=nwrsend+" -d "+local_dir+"data/tsradio.txt" # -a automatically transfer, -d pending directory
        ret=0
        if nwrsend == "":      #  SEND TO RADIO DISABLED! Don't try to make system call.
            ret = 127
        else:
            ret=os.system(cmd) # Launch Initiated.
    except: flagMeBasic("Send to radio function failed. Statement likely did not make it to the radio. Code:sendtoRadioONE")
    try:
    # Some book keeping
        # for combo situations
        still_need=[]
        wwarr=active.split("_") # works in both combo mode and single mode
        for a in wwarr:
            if a not in combo_issued: combo_issued.append(a)
        for a in combo_need:
            if a not in combo_issued: still_need.append(a)
        # keep track of what and when issued, in case prior issuance still active
        # read in previous
        track={"warning":[0,""],"watch":[0,""],"advisory":[0,""]}
        if os.path.isfile(local_dir+"data/last_issued.txt"):
            with open(local_dir+"data/last_issued.txt",'r') as lFile:
                full = lFile.readlines()
            for a in full:
                tmp=a.strip().split("|")
                if len(tmp)>2: # assume a hit if this
                    track[tmp[0]]=[tmp[1],tmp[2]]
        # find new values
        if "crsexpire" in optset:
            exp=optset["crsexpire"].get().replace("mins","").replace(" ","")
            forwardtime=todayutc+datetime.timedelta(minutes=int(exp))
        else: forwardtime=todayutc+datetime.timedelta(minutes=15)
        forwardtime=int(forwardtime.strftime("%s")) # new time to log
        zones=zoneMe()
        # Check most recent versus now
        recentflg=""
        for wwa in ["warning","watch","advisory"]:
            if wwa in track and len(track[wwa])>1 and int(track[wwa][0])>int(time.time()): # passes time check
                arr=track[wwa][1].split("-")
                #print wwa,track[wwa]
                for a in arr:
                    if zones.find(a)!=-1 and recentflg.find(wwa)==-1: recentflg+=" "+wwa # passes county check, alert!
            # Save for another time
        for wwa in wwarr:
            track[wwa][0]=forwardtime
            track[wwa][1]=zones
        # Rebuild and store
        if modeme=="live":
            out=""
            for wwa,arr in list(track.items()):
                if len(arr)>1: out+=wwa+"|"+str(arr[0])+"|"+arr[1]+"\n"
            with open(local_dir+"data/last_issued.txt","w") as pp:   # create and write to temporary file
                pp.write(out)

        # Display to user
        if len(still_need)>0:
            if recentflg=="": alertframe1.geometry(windowXgeo()) # we're going to need a bigger box
            else: alertframe1.geometry(windowXgeo()) # we're going to need a bigger box
        elif recentflg!="": alertframe1.geometry(windowXgeo())
        else: alertframe1.geometry(windowXgeo())
        colen=1
        if len(still_need)>1: colen=len(still_need)
        #
        # The following statement was flagged by the Python 3 conversion tool as possible integer
        # division. But since it's dividing by a float, this looks okay, doesn't need a // operator.
        #
        if ret/256.0==0: # all went well
            bg="#afc"
            alertframe1.configure(bg=bg)
            if modeme=="live":
                if pending=="no": Label(alertframe1,text="Statement Successfully Sent to BMH",bg=bg,fg="#000").grid(row=0,column=0,columnspan=colen,padx=gp,pady=gp)
                else: Label(alertframe1,text="Statement Successfully Sent to Pending. Please open the NWR Browser to complete.",bg=bg,fg="#000").grid(row=0,column=0,columnspan=colen,padx=gp,pady=gp)
            else:
                Label(alertframe1,text="Congratulations.\nLooks like a sucessful test.",bg=bg,fg="#000").grid(row=0,column=0,columnspan=colen,padx=gp,pady=gp)
            if len(still_need)>0:
                Label(alertframe1,text="You may still need to issue a Tsunami:",bg=bg,fg="#000").grid(row=1,column=0,columnspan=colen,padx=gp,pady=gp)
                col=0
                for a in still_need:
                    if a=="watch":
                        Button(alertframe1,text="   Watch   ",command=fillcomboWatch,bg="#009",fg="#fff").grid(row=2,column=col)
                    elif a=="warning":
                        Button(alertframe1,text="   Warning   ",command=fillcomboWarning,bg="#009",fg="#fff").grid(row=2,column=col)
                    elif a=="advisory":
                        Button(alertframe1,text="   Advisory   ",command=fillcomboAdvisory,bg="#009",fg="#fff").grid(row=2,column=col)
                    col+=1
            if pending=="yes":
                Button(alertframe1,text=" Open NWR Browser ",command=openNWRBrowser,bg="#000",fg="#fff").grid(row=3,column=0,columnspan=colen,padx=gp,pady=gp)
            Button(alertframe1,text="   Close   ",command=alertframe1.destroy,bg="#000",fg="#fff").grid(row=4,column=0,columnspan=colen,padx=gp,pady=gp)
            if recentflg!="":
                Label(alertframe1,text="\n*There may be a previous "+recentflg[1:].replace(" "," & ")+" for a\nzone you just issued that may \
    still be playing on the radio.\nYou may want to manually remove it from BMH.",bg=bg,fg="#a00").grid(row=5,column=0,columnspan=colen,padx=gp,pady=gp)
            # log it
            pil=wcapil
            tmp=pil.split(":")
            if len(tmp)>1: pil=tmp[1]
            cmd=db+" -tU "+pil
            statementin=int(os.popen(cmd).read())
            now=int(time.time())
            dif=now-statementin
            m,s=divmod(dif,60)
            logit=pil+" in at: "+datetime.datetime.utcfromtimestamp(statementin).strftime('%Y-%m-%d %H:%M:%S')
            logit+="\nNWR activated at: "+datetime.datetime.utcfromtimestamp(now).strftime('%Y-%m-%d %H:%M:%S')
            logit+="\nDifference of "+str(m)+"mins "+str(s)+"secs"
            if modeme=="live":
                logging.info(logit)
            else:
                logging.info("(PRACTICE mode) " + logit)

        else: # sendtoradio function failed
            bg="#fac"
            alertframe1.configure(bg=bg)
            msg="Practice is complete, but something went strangely.\nCode:nwrTransfer"
            if modeme=="live":
                if nwrsend == "":
                    msg = "SEND TO RADIO is disabled - please set 'nwrsend' parameter in etc/config_tsu.py file.\nCode:nwrTransfer "
                else:
                    msg="There was an error. "+active.capitalize().replace("_","+")+" Product not sent to BMH.\nCode:nwrTransfer "
            Label(alertframe1,text=msg+str(ret),bg=bg,fg="#000").grid(row=0,column=0,padx=gp,pady=gp)  # error 32512, check nwrsend command
            Button(alertframe1,text="   OK   ",command=alertframe1.destroy,bg="#000",fg="#fff").grid(row=1,column=0,padx=gp,pady=gp)
    except: flagMeBasic("There was an error. Statement likely sent to radio (please verify), but an error occurred after. Code:sendtoRadioTWO")

# ================================= GUI ====================================
# Background Setup
bgmain_list={"live":"#bbb","practice":"#fa3","test":"#000"}
if modeme in bgmain_list: bgmain=bgmain_list[modeme]
else: bgmain=bgmain_list["live"]

# Main Setup
root=Tk()
global windowX # for extended 3 panel display
windowX="+"+str(int(0.328*root.winfo_screenwidth()-100))
root.geometry(windowX+'+75') #780x720+250+75
root.configure(bg=bgmain)
root.title('Tsunami EAS GUI v'+str(Version))
root.option_add('*Font', ('Arial',fontsize,'bold'))
absmaxcol=2 # most columns in any frame
tfont=tkinter.font.Font(size=fontsize,family='Arial') # for tooltip

# Main interface for Radio Functionality
global printout,alertframe,radiotext,mainLabels,mainButtons,alertframe,cancelflg,masterTone
mainLabels={} # dictionary to easily change background color
out=""
printout=out
masterTone=1 # one way of turning on/off EAS tone alert
rows=0
# Menu Bar
mainLabels["mframe"]=Frame(root)
mainLabels["mframe"].grid(row=rows,column=0,sticky=NW,columnspan=absmaxcol)
mainLabels["mframe"].configure(bg=bgmain)
# File Menu
mainLabels["menufile"]=Menubutton(mainLabels["mframe"],text='File',justify=RIGHT,bg=bgmain)
mainLabels["menufile"].menu=Menu(mainLabels["menufile"])
mainLabels["menufile"].menu.add_command(label='Print',command=printRadio)
mainLabels["menufile"].menu.add_command(label='Restore',command=restoreMe)
#mainLabels["menufile"].menu.add_command(label='Spell Check',command=spellMe)  ispell and aspell nt in A2!
mainLabels["menufile"].menu.add_command(label='Close Program',command=closeMe)
mainLabels["menufile"]['menu']=mainLabels["menufile"].menu
mainLabels["menufile"].grid(row=0,column=0,sticky=NW,padx=3)
# Options Menu
mainLabels["menuoptions"]=Menubutton(mainLabels["mframe"],text='Options',justify=RIGHT,bg=bgmain)
mainLabels["menuoptions"].menu=Menu(mainLabels["menuoptions"])
mainLabels["menuoptions"].menu.add_command(label='Load Latest From NTWC Product',command=loadLatestStatement)
mainLabels["menuoptions"].menu.add_command(label='Load Example Statement',command=loadExampleStatement)
mainLabels["menuoptions"].menu.add_command(label='Preview BMH Script',command=previewBMH)
mainLabels["menuoptions"].menu.add_command(label='Autofill Settings',command=autofillSettings)
mainLabels["menuoptions"].menu.add_command(label='Add Test Wording',command=testMe)
mainLabels["menuoptions"].menu.add_command(label='Remove Test Wording',command=untestMe)
mainLabels["menuoptions"].menu.add_command(label='Clear Tracked WWA',command=clearTrack)
mainLabels["menuoptions"]['menu']=mainLabels["menuoptions"].menu
mainLabels["menuoptions"].grid(row=0,column=2,sticky=NW,padx=3)
# Templates
mainLabels["menutemplate"]=Menubutton(mainLabels["mframe"],text='Backup Templates',justify=RIGHT,bg=bgmain)
mainLabels["menutemplate"].menu=Menu(mainLabels["menutemplate"])
mainLabels["menuimminent"]=Menubutton(mainLabels["mframe"],text='Imminent Tsunami',justify=RIGHT,bg=bgmain)
mainLabels["menuimminent"].menu=Menu(mainLabels["menuimminent"])
mainLabels["menucancel"]=Menubutton(mainLabels["mframe"],text='Cancellations',justify=RIGHT,bg=bgmain)
mainLabels["menucancel"].menu=Menu(mainLabels["menucancel"])
#mainLabels["menutemplate"].menu.add_command(label='Warning',command=fillWarning)
#mainLabels["menutemplate"].menu.add_command(label='Watch',command=fillWatch)
#if advisory_handle=="yes": mainLabels["menutemplate"].menu.add_command(label='Advisory',command=fillAdvisory)
#if combo_handle=="yes": mainLabels["menutemplate"].menu.add_command(label='Combo',command=comboOption)

# Menu for special canned statements. Option exists for offices to localize templates
arr=[]
arr2=os.listdir(local_dir+"/etc")    # local templates
for a in arr2:
    b=a[9:].replace(".txt","")
    if a[0:9]=="template_" and a.find("~")==-1 and a.find(".swp")==-1:
        if b=="warning" or b=="watch" or b=="advisory": arr.append("AAAetc/"+a) # trick for better sorting
        else: arr.append("etc/"+a)
arr2=os.listdir(local_dir+"/config") # standard templates
for a in arr2:
    b=a[9:].replace(".txt","")
    if a[0:9]=="template_" and a.find("~")==-1 and a.find(".swp")==-1 and "etc/"+a not in arr and "AAAetc/"+a not in arr:
        if b=="warning" or b=="watch" or b=="advisory": arr.append("AAAconfig/"+a)
        ###else: arr.append("config/"+a) # only if local option not there
arr.sort()
cancelflg=0
imminentflg=0
for a in arr:
    a=a.replace("AAA","")
    tmp2=a.split("/")
    tmp=tmp2[1][9:]
    if len(tmp)>1:
        if tmp.find("cancel")!=-1:
            mainLabels["menucancel"].menu.add_command(label=tmp[0].upper()+tmp[1:].replace(".txt","").replace("_"," ")+" template",command=lambda i=a:fillWhat(i))
            cancelflg=1
        elif tmp.find("imminent")!=-1:
            mainLabels["menuimminent"].menu.add_command(label=tmp[0].upper()+tmp[1:].replace(".txt","").replace("_"," ")+" template",command=lambda i=a:fillWhat(i))
            imminentflg=1
        else: mainLabels["menutemplate"].menu.add_command(label=tmp[0].upper()+tmp[1:].replace(".txt","").replace("_"," "),command=lambda i=a:fillWhat(i))

mainLabels["menutemplate"]['menu']=mainLabels["menutemplate"].menu
mainLabels["menutemplate"].grid(row=0,column=3,sticky=NW,padx=3)
if imminentflg==1:
    mainLabels["menuimminent"]['menu']=mainLabels["menuimminent"].menu
    mainLabels["menuimminent"].grid(row=0,column=4,sticky=NW,padx=3)
if cancelflg==1:
    mainLabels["menucancel"]['menu']=mainLabels["menucancel"].menu
    mainLabels["menucancel"].grid(row=0,column=5,sticky=NW,padx=3)
# Other menus
mainLabels["helpme"]=Button(mainLabels["mframe"],text='Help',bg=bgmain,command=viewHelp,relief=FLAT,highlightthickness=0,justify=RIGHT)
mainLabels["helpme"].grid(row=0,column=6,sticky=NW,padx=3)
mainLabels["modeme"]=Button(root,text="",fg="#000",bg=bgmain,relief=FLAT,bd=0,command=toggleMode,font=('Arial',str(int(fontsize)+10),'bold'))
mainLabels["modeme"].grid(row=rows,column=0,columnspan=absmaxcol,sticky=E)

rows+=1
# Title
mainLabels["title"]=Label(root,text=" Tsunami EAS GUI ",fg="#07a",bg=bgmain,font=('Arial',str(int(fontsize)+16)))
mainLabels["title"].grid(row=rows,column=0,columnspan=absmaxcol,sticky=NW)
mainLabels["bt_viewStatement"]=Button(root,text=" View "+wcapil.replace(teststatementdir,"")+" Statement",command=showTSU,bg="#222",fg="#ff0")
mainLabels["bt_viewStatement"].grid(row=rows,column=0,columnspan=absmaxcol,sticky=E)

rows+=1
# Zone Bar
row=0
#startCol=1
btnHeight = 1
nCols = 14-int(round(0.5*float(fontsize)))
# If the user wants the button labels to be codes + names, the columns will be wider, so need fewer of them.
try: includeNames = include_zone_names
except: includeNames = False
mainLabels["zframe"]=Frame(root)
mainLabels["zframe"].grid(row=rows,column=0,columnspan=absmaxcol,padx=gp)
mainLabels["zframe"].configure(bg=bgmain)
mainLabels["zones"]=Label(mainLabels["zframe"],text="Applicable Zones: ",bg=bgmain,fg="#009")
mainLabels["zones"].grid(row=0,column=0)
bt_zones={}
col = 1
rowadd=1
if includeNames: rowadd=2
for id in zone_ids:
    if col>0 and col%nCols==0:
        col=1
        row=row+rowadd # line break
    bt_zones[id]=IntVar()
    mainLabels[id]=Checkbutton(mainLabels["zframe"],text=id,variable=bt_zones[id],command=changeZone,bg=bgmain,highlightthickness=0,padx=4,pady=5)
    mainLabels[id].grid(row=row,column=col,sticky=W+E)
    if includeNames:
       labelText = textwrap.fill(zone_list[id].lower().replace("county","").replace(" and "," & ")[0:25].lower().title(),width=15)
       mainLabels[id+"lbl"]=Label(mainLabels["zframe"],text=labelText,bg=bgmain,fg="#009",font=('Arial',str(int(fontsize)-3)))
       mainLabels[id+"lbl"].grid(row=row+1,column=col,sticky=W+E)
    col=col+1

rows+=1
# Main Text Area
# radiotext=Text(root,height=27,width=67,bg="#ddf",fg="#000",font=('Courier','15'),wrap=WORD)
# Dynamic text box sizing, which drives most of the GUI height
screenRes=root.winfo_screenheight()
if screenRes<680: wBase=205
else: wBase=int(0.5*(screenRes-680))+205
windowHeight = int( wBase / int(textsize))
windowWidth  = int(1005 / int(textsize))
radiotext=Text(root,height=windowHeight,width=windowWidth,bg="#ddf",fg="#000",font=('Courier', str(textsize)),wrap=WORD)
scroll=Scrollbar(root,command=radiotext.yview)
radiotext.configure(yscrollcommand=scroll.set)
radiotext.grid(row=rows,column=0,sticky=W)
scroll.grid(row=rows,column=1,sticky=N+S+E)

rows+=1
# Button Bar
mainLabels["bframe"]=Frame(root)
mainLabels["bframe"].grid(row=rows,column=0,columnspan=absmaxcol,sticky=E)
mainLabels["bframe"].configure(bg=bgmain)
mainLabels["bt_eas"]=Button(root,text=" Send to Radio ",command=sendtoRadioCheck,bg="#f33",fg="#ff7")
mainLabels["bt_eas"].grid(row=rows,column=0,sticky=W)
mainLabels["expiresin"]=Label(mainLabels["bframe"],text="Product expires in: ",bg=bgmain,fg="#00a")
mainLabels["expiresin"].grid(row=rows,column=1,sticky=E)
arr=["10mins","15mins","20mins","30mins","45mins","60mins","90mins","120mins"]
if "crsexpire" in optset: setexpire=optset["crsexpire"].get()
else:
    optset["crsexpire"]=StringVar()
    optset["crsexpire"].set(str(expiresin)+"mins")
OptionMenu(mainLabels["bframe"],optset["crsexpire"],*arr).grid(row=rows,column=2)

mainLabels["bt_close"]=Button(root,text=" Close Program ",command=root.destroy,bg="#ccc",fg="#000")
mainLabels["bt_close"].grid(row=rows+1,column=0,columnspan=absmaxcol,padx=gp,pady=gp)

#mainLabels["bt_autofill"]=Button(mainLabels["bframe"],text=" Autofill from "+wcapil+" ",command=autoFill,bg="#222",fg="#ff0")
#mainLabels["bt_autofill"].grid(row=2,column=0,columnspan=col,sticky=W+E)
#mainLabels["subtitle"]=Label(mainLabels["bframe"],text="(The text box below is editable)",fg="#900",bg=bgmain,font=('Arial',fontsize))
#mainLabels["subtitle"].grid(row=3,column=0,columnspan=col,sticky=S)

# Tooltips
#ToolTip(mainLabels["menutemplate"],text="These options will fill the text area with a generic script, which may come in handy \
#in the event that Autofill fails or produces unsatisfactory results. You should replace the XXXXXX text with relavant information.\n\n\
#You can also prefill the desired zones beforehand, and the template fill will attempt to create an area description for you.")
#ToolTip(mainLabels["bt_viewStatement"],text="This function is automatically run when you start the program. You do not need to run \
#this if the text below looks good.\n\nAutofill will parse out the latest TSUWCA product display a radio script with \
#those values automatically filled in. Autofll will also assign the correct zones from the TSUWCA.")
ToolTip(mainLabels["bt_eas"],text="This is the next step to send the text above to the radio. There will be one more \
popup box that will appear after hitting this button to confirm what you want to do before anything is sent to the radio.")
#ToolTip(mainLabels["menufile"],text="Standard options found here.\n\n'Restore' will fill in the text with the last autosaved text. \
#Everytime a button or option is chosen, the program autosaves the text in the main text box. In case the program crashes, or the \
#result of an autofill was not satisfactory, restore may come in handy.")
#ToolTip(mainLabels["menuoptions"],text="Advanced options found here.\n\n'Add Test Wording' will add wording into the loaded statement \
#to make it clear that this is a test statement in the event that it is sent out to the world.\n\n'Clear Tracked WWA' will clear the \
#running log of Tsunami WWA statements sent to the radio by this GUI. This tracking will be used to remind you if two statements are \
#on the radio for the same area. While testing this GUI, those notices may be reset with this option.")
ToolTip(mainLabels["modeme"],text="Click Here to toggle between live and practice mode. Live Mode will allow you \
to send the statement to the radio. Practice mode allow you to run the program WITHOUT the possibility of accidentally \
sending anything to the radio.")
ToolTip(mainLabels["expiresin"],"This allows you to set how long the statement will play on the radio.")

# Last Things
modemeLabelChange()
afterid=root.after(250,autoFill) # need to pause in case there is a popup window loaded before root. START PROGRAM
root.mainloop()

#["CAZ042","CAZ043",||"CAZ040","CAZ041","CAZ087","CAZ039","CAZ034","CAZ035",||\
#"PZZ530","CAZ529","CAZ530","CAZ006","CAZ505","CAZ506","CAZ508","CAZ509",||\
#"CAZ002","CAZ001",||"ORZ021","ORZ022",||"ORZ002","ORZ001","WAZ021",||"WAZ001","WAZ510","WAZ514","WAZ515","WAZ516","WAZ517"]
