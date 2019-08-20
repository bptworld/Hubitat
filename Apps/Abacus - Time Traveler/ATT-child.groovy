import groovy.time.TimeCategory
/**
 *  ****************  Abacus - Time Traveler Child ****************
 *
 *  Design Usage:
 *  Track how long a Device has been active. Displays Daily, Weekly, Monthly and Yearly Timers!
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.5 - 03/22/19 - Maintenance release to prepare for V2.0.0. You will need this version to save all of your stats!
 *  V1.0.4 - 02/16/19 - Big maintenance release. Reworked a lot of code as I continue to learn new things.
 *  V1.0.3 - 01/15/19 - Updated footer with update check and links
 *  V1.0.2 - 01/06/19 - Squashed a bug in the Weekly count reset. Also added in a way to delete a single line from the reports.
 *						This is needed to get rid of the orphans created from the Weekly Count bug.
 *  V1.0.1 - 01/04/19 - Major logic change to calculate how long a device was active.
 *  V1.0.0 - 01/03/19 - Initial release.
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "AbacusTimeTravelerChildVersion"
	state.version = "v2.0.0"
    
    try {
        if(parent.sendToAWSwitch && parent.awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    parent.awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
            schedule("0 0 3 ? * * *", setVersion)
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "Abacus - Time Traveler Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Track how long a Device has been active. Displays Daily, Weekly, Monthly and Yearly Timers!",
    category: "Useless",
	parent: "BPTWorld:Abacus - Time Traveler",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
	page(name: "pageCounts")
	page(name: "rawStats")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Time Traveler</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {
	display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Information</b>"
			paragraph "Daily timers are reset each morning.<br>Weekly timers are reset each Sunday.<br>Monthly timers are reset at on the 1st of each month.<br>Yearly timers get reset on Jan 1st.<br>All timers resets happen between 12:05am and 12:10am"
			paragraph "Also, times are not added into totals until the device turns off (off/inactive/closed/idle)"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageCounts", title: "Abacus - Time Traveler Report", description: "Click here to view the Abacus Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Most Common Devices")) {
			input(name: "motionEvent", type: "capability.motionSensor", title: "Motion sensor(s) to time", submitOnChange: true, required: false, multiple: true)
			input(name: "contactEvent", type: "capability.contactSensor", title: "Contact Sensor(s) to time", submitOnChange: true, required: false, multiple: true)
			input(name: "switchEvent", type: "capability.switch", title: "Switch Device(s) to time", submitOnChange: true, required: false, multiple: true)
			input(name: "thermostatEvent", type: "capability.thermostat", title: "Thermostat(s) to time", submitOnChange: true, required: false, multiple: true)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance - Preparing for V2.0.0")) {}
		section("Instructions for Maintenance section:", hideable: true, hidden: true) {
			paragraph "If upgrading from v1.0.5 and want to keep your current stats. Run the report below. It will display all the numbers you need to make sure you don't lose your stats!"
			paragraph "NOTE: The numbers produced here will need to be manually added into V2.0.0"
		}
		section() {
			href "rawStats", title: "Run Stats Report", description: "Click here to run the stats report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
			input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	}
		display2()
	}
}

def pageCounts(params) {
	dynamicPage(name: "pageStatus", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Time Traveler</h2>", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		if(state.motionMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Motion Sensors")) {
				if(state.motionMap) {
					LOGDEBUG("In pageCounts...Motion Sensors")
					paragraph "${state.motionMap}"
				} else {
					LOGDEBUG("In pageCounts...Motion Sensors")
					paragraph "No Motion data to display."
				}
			}
		}
		if(state.contactMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Contact Sensors")) {
				if(state.contactMap) {
					LOGDEBUG("In pageCounts...Contact Sensors")
					paragraph "${state.contactMap}"
				} else {
					LOGDEBUG("In pageCounts...Contact Sensors")
					paragraph "No Contact data to display."
				}
			}
		}
		if(state.switchMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Switch Events")) {
				if(state.switchMap) {
					LOGDEBUG("In pageCounts...Switch Events")
					paragraph "${state.switchMap}"
				} else {
					LOGDEBUG("In pageCounts...Switch Events")
					paragraph "No Switch data to display."
				}
			}
		}
		if(state.thermostatMap) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Thermostat Events")) {
				if(state.thermostatMap) {
					LOGDEBUG("In pageCounts...Thermostat Events")
					paragraph "${state.thermostatMap}"
				} else {
					LOGDEBUG("In pageCounts...Thermostat Events")
					paragraph "No Thermostat data to display."
				}
			}
		}
		section() {
			if(state.motionMap == null && state.contactMap == null && state.switchMap == null && state.actuatorMap && state.thermostatMap) {
				paragraph "No data to display."
			}
		}
		section() {
			paragraph getFormat("line")
			def rightNow = new Date()
			paragraph "<div style='color:#1A77C9'>Report generated: ${rightNow}</div>"
		}
	}
}

def rawStats(params) {
	dynamicPage(name: "rawStats", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Time Traveler</h2>", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		rawStatsHandler()
		section(getFormat("header-green", "${getImage("Blank")}"+" Raw Motion Stats")) {
			if(state.rawMotionMap) paragraph "${state.rawMotionMap}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Raw Switch Stats")) {
			if(state.rawSwitchMap) paragraph "${state.rawSwitchMap}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Raw Contact Stats")) {
			if(state.rawContactMap) paragraph "${state.rawContactMap}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Raw Thermostat Stats")) {
			if(state.rawThermostatMap) paragraph "${state.rawThermostatMap}"
		}
	}
}

def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	LOGDEBUG("Updated with settings: ${settings}")
	unsubscribe()
	logCheck()
	initialize()
}

def initialize() {
	LOGDEBUG("In initialize...")
	setDefaults()
	subscribe(motionEvent, "motion", motionHandler)
	subscribe(contactEvent, "contact", contactHandler)
	subscribe(switchEvent, "switch", switchHandler)
	subscribe(thermostatEvent, "thermostatOperatingState", thermostatHandler)

	schedule("0 5 0 * * ? *", resetMotionCountHandler)
	schedule("0 6 0 * * ? *", resetContactCountHandler)
	schedule("0 7 0 * * ? *", resetSwitchCountHandler)
	schedule("0 9 0 * * ? *", resetThermostatCountHandler)
}

def setupNewStuff() {
	LOGDEBUG("In setupNewStuff...")
	
	// ********** Starting Motion Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Motion Maps")
	
	if(state.motionMap == null) resetMotionMapHandler()
	if(state.motionMapD == null) resetMotionMapHandler()
	if(state.motionMapW == null) resetMotionMapHandler()
	if(state.motionMapM == null) resetMotionMapHandler()
	if(state.motionMapY == null) resetMotionMapHandler()
	if(state.motionPrevMap == null) resetMotionMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Motion devices")
	motionEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.motionMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.motionMapD.put(it.displayName, 0)
		}
		if(state.motionMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.motionMapW.put(it.displayName, 0)
		}
		if(state.motionMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.motionMapM.put(it.displayName, 0)
		}
		if(state.motionMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.motionMapY.put(it.displayName, 0)
		}
		if(state.motionPrevMap.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map prev...Adding it in.")
			def now1 = new Date()
    		prev = now1.getTime()
			state.motionPrevMap.put(it.displayName, prev)
		}
	}
	
	// ********** Ending Motion Devices **********
	
	// ********** Starting Contact Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Contact Maps")
	
	if(state.contactMap == null) resetContactMapHandler()
	if(state.contactMapD == null) resetContactMapHandler()
	if(state.contactMapW == null) resetContactMapHandler()
	if(state.contactMapM == null) resetContactMapHandler()
	if(state.contactMapY == null) resetContactMapHandler()
	if(state.contactPrevMap == null) resetContactMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Contact devices")
	contactEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.contactMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.contactMapD.put(it.displayName, 0)
		}
		if(state.contactMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.contactMapW.put(it.displayName, 0)
		}
		if(state.contactMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.contactMapM.put(it.displayName, 0)
		}
		if(state.contactMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.contactMapY.put(it.displayName, 0)
		}
		if(state.contactPrevMap.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map prev...Adding it in.")
			def now1 = new Date()
    		prev = now1.getTime()
			state.contactPrevMap.put(it.displayName, prev)
		}
	}
	
	// ********** Ending Contact Devices **********
	
	// ********** Starting Switch Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Switch Maps")
	
	if(state.switchMap == null) resetSwitchMapHandler()
	if(state.switchMapD == null) resetSwitchMapHandler()
	if(state.switchMapW == null) resetSwitchMapHandler()
	if(state.switchMapM == null) resetSwitchMapHandler()
	if(state.switchMapY == null) resetSwitchMapHandler()
	if(state.switchPrevMap == null) resetSwitchMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Switch devices")
	switchEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.switchMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.switchMapD.put(it.displayName, 0)
		}
		if(state.switchMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.switchMapW.put(it.displayName, 0)
		}
		if(state.switchMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.switchMapM.put(it.displayName, 0)
		}
		if(state.switchMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.switchMapY.put(it.displayName, 0)
		}
		if(state.switchPrevMap.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map prev...Adding it in.")
			def now1 = new Date()
    		prev = now1.getTime()
			state.switchPrevMap.put(it.displayName, prev)
		}
	}
	
	// ********** Ending Switch Devices **********
	
	// ********** Starting Thermostat Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Thermostat Maps")
	
	if(state.thermostatMap == null) resetThermostatMapHandler()
	if(state.thermostatMapD == null) resetThermostatMapHandler()
	if(state.thermostatMapW == null) resetThermostatMapHandler()
	if(state.thermostatMapM == null) resetThermostatMapHandler()
	if(state.thermostatMapY == null) resetThermostatMapHandler()
	if(state.thermostatPrevMap == null) resetThermostatMapHandler()

	LOGDEBUG("In setupNewStuff...Looking for new Thermostat devices")
	thermostatEvent.each { it -> 
		LOGDEBUG("Working on... ${it.displayName}")
		if(state.thermostatMapD.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map D...Adding it in.")
			state.thermostatMapD.put(it.displayName, 0)
		}
		if(state.thermostatMapW.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map W...Adding it in.")
			state.thermostatMapW.put(it.displayName, 0)
		}
		if(state.thermostatMapM.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map M...Adding it in.")
			state.thermostatMapM.put(it.displayName, 0)
		}
		if(state.thermostatMapY.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in.")
			state.thermostatMapY.put(it.displayName, 0)
		}
		if(state.thermostatPrevMap.get(it.displayName) == null) {
			LOGDEBUG("In setupNewStuff: ${it.displayName} not found in Map prev...Adding it in.")
			def now1 = new Date()
    		prev = now1.getTime()
			state.thermostatPrevMap.put(it.displayName, prev)
		}
	}
	
	// ********** Ending Thermostat Devices **********
}

def rawStatsHandler() {
	// Motion
	if(motionEvent) {
		state.motionEventS = motionEvent.sort{it.name}
		state.rawMotionMap = "<table width='100%'>"
		state.motionEventS.each { it ->
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			LOGDEBUG("Motion - ${it.displayName} - D: ${countD}, W: ${countW}, M: ${countM}, Y: ${countY}")
			state.rawMotionMap += "<tr><td><b>${it.displayName}</b></td><td>D: ${countD}</td><td>W: ${countW}</td><td>M: ${countM}</td><td>Y: ${countY}</td></tr>"
		}
		state.rawMotionMap += "</table>"
	}
	// Switch
	if(switchEvent) {
		state.switchEventS = switchEvent.sort{it.name}
		state.rawSwitchMap = "<table width='100%'>"
		state.switchEventS.each { it ->
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			LOGDEBUG("Switch - ${it.displayName} - D: ${countD}, W: ${countW}, M: ${countM}, Y: ${countY}")
			state.rawSwitchMap += "<tr><td><b>${it.displayName}</b></td><td>D: ${countD}</td><td>W: ${countW}</td><td>M: ${countM}</td><td>Y: ${countY}</td></tr>"
		}
		state.rawSwitchMap += "</table>"
	}
	//Contact
	if(contactEvent) {
		state.contactEventS = contactEvent.sort{it.name}
		state.rawContactMap = "<table width='100%'>"
		state.contactEventS.each { it ->
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			LOGDEBUG("Contact - ${it.displayName} - D: ${countD}, W: ${countW}, M: ${countM}, Y: ${countY}")
			state.rawContactMap += "<tr><td><b>${it.displayName}</b></td><td>D: ${countD}</td><td>W: ${countW}</td><td>M: ${countM}</td><td>Y: ${countY}</td></tr>"
		}
		state.rawContactMap += "</table>"
	}
	// Thermostat
	if(thermostatEvent) {
		state.thermostatEventS = thermostatEvent.sort{it.name}
		state.rawThermostatMap = "<table width='100%'>"
		state.thermostatEventS.each { it ->
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			LOGDEBUG("Thermostat - ${it.displayName} - D: ${countD}, W: ${countW}, M: ${countM}, Y: ${countY}")
			state.rawThermostatMap += "<tr><td><b>${it.displayName}</b></td><td>D: ${countD}</td><td>W: ${countW}</td><td>M: ${countM}</td><td>Y: ${countY}</td></tr>"
		}
		state.rawThermostatMap += "</table>"
	}
}

def motionHandler(evt) {
	LOGDEBUG("In motionHandler...")
	LOGDEBUG("In motionHandler: Device: $evt.displayName is $evt.value")
	state.motionStatus = evt.value
				
	if(state.motionStatus == "active") {
		def now1 = new Date()
    	prev = now1.getTime()
		state.motionPrevMap.put(evt.displayName, prev)
		LOGDEBUG("In motionHandler...${evt.displayName} became ${state.motionStatus} at ${now1}")
	}
	
	if(state.motionStatus == "inactive") {
		state.motionMap = ""
		try {
			motionEvent.each { it -> 
				if(evt.displayName == it.displayName) {
					prev = state.motionPrevMap.get(evt.displayName, prev)
					def now2 = new Date()
					LOGDEBUG("In motionHandler...${evt.displayName} became ${state.motionStatus} at ${now2}")
					long unxNow = now2.getTime()
    				long unxPrev = prev
    				unxNow = unxNow/1000 
    				unxPrev = unxPrev/1000
					timeDiff = (unxNow-unxPrev)
					LOGDEBUG("In motionHandler...${evt.displayName}: timeDiff in Seconds: ${timeDiff}")
	
					countD = state.motionMapD.get(evt.displayName)
					if(countD == null) countD = 0
  					newCountD = countD + timeDiff
    				state.motionMapD.put(evt.displayName, newCountD)
	
					countW = state.motionMapW.get(evt.displayName)
					if(countW == null) countW = 0
    				newCountW = countW + timeDiff
    				state.motionMapW.put(evt.displayName, newCountW)
	
					countM = state.motionMapM.get(evt.displayName)
					if(countM == null) countM = 0
   	 				newCountM = countM + timeDiff
    				state.motionMapM.put(evt.displayName, newCountM)
	
    				countY = state.motionMapY.get(evt.displayName)
					if(countY == null) countY = 0
    				newCountY = countY + timeDiff
    				state.motionMapY.put(evt.displayName, newCountY)	
	
				// Now Triggered Numbers
					int inputNow=timeDiff
					int nDayNow = inputNow / 86400
					int nHrsNow = (inputNow % 86400 ) / 3600
					int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
					int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
				// End Now Triggered Nubmers
	
				// Today's Numbers
					int inputD=newCountD
					int newnDayD = inputD / 86400
					int newnHrsD = (inputD % 86400 ) / 3600
					int newnMinD = ((inputD % 86400 ) % 3600 ) / 60
					int newnSecD = ((inputD % 86400 ) % 3600 ) % 60
				// End Today's Numbers
		
				// Weekly Numbers
					int inputW=newCountW
					int newnDayW = inputW / 86400
					int newnHrsW = (inputW % 86400 ) / 3600
					int newnMinW = ((inputW % 86400 ) % 3600 ) / 60
					int newnSecW = ((inputW % 86400 ) % 3600 ) % 60
				// End Weekly Numbers
		
				// Monthly Numbers
					int inputM=newCountM
					int newnDayM = inputM / 86400
					int newnHrsM = (inputM % 86400 ) / 3600
					int newnMinM = ((inputM % 86400 ) % 3600 ) / 60
					int newnSecM = ((inputM % 86400 ) % 3600 ) % 60
				// End Monthly Numbers
		
				// Yearly Numbers
					int inputY=newCountY
					int newnDayY = inputY / 86400
					int newnHrsY = (inputY % 86400 ) / 3600
					int newnMinY = ((inputY % 86400 ) % 3600 ) / 60
					int newnSecY = ((inputY % 86400 ) % 3600 ) % 60
				// End Yearly Numbers
		
				LOGDEBUG("In motionHandler...${evt.displayName}: This Time: ${nDayNow} Days, ${nHrsNow} Hours, ${nMinNow} Minutes, ${nSecNow} Seconds")
					
					state.motionMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
					state.motionMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
					state.motionMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
					state.motionMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
					
					LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
				} else {
					countD = state.motionMapD.get(it.displayName)
					countW = state.motionMapW.get(it.displayName)
					countM = state.motionMapM.get(it.displayName)
					countY = state.motionMapY.get(it.displayName)
				// Before Triggered Numbers
					int inputDBef=countD
					int nDayDBef = inputDBef / 86400
					int nHrsDBef = (inputDBef % 86400 ) / 3600
					int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
					int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
					int inputWBef=countW
					int nDayWBef = inputWBef / 86400
					int nHrsWBef = (inputWBef % 86400 ) / 3600
					int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
					int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
					int inputMBef=countM
					int nDayMBef = inputMBef / 86400
					int nHrsMBef = (inputMBef % 86400 ) / 3600
					int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
					int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
					int inputYBef=countY
					int nDayYBef = inputYBef / 86400
					int nHrsYBef = (inputYBef % 86400 ) / 3600
					int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
					int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
				// End Before Triggered Nubmers
					
					state.motionMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
					state.motionMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
					state.motionMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
					state.motionMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
				}
			}
		} catch(ex) {
		LOGDEBUG("In motionHandler...${evt.displayName}: Device was not found.")
		}
	}
}
	
def contactHandler(evt) {
	LOGDEBUG("In contactHandler...")
	LOGDEBUG("$evt.displayName: $evt.value")
	state.contactStatus = evt.value
	
	if(state.contactStatus == "open") {
		def now1 = new Date()
    	prev = now1.getTime()
		state.contactPrevMap.put(evt.displayName, prev)
		LOGDEBUG("In contactHandler...${evt.displayName} became ${state.contactStatus} at ${now1}")
	}
		
	if(state.contactStatus == "closed") {
		state.contactMap = ""
		try {
			contactEvent.each { it -> 
				if(evt.displayName == it.displayName) {
					prev = state.contactPrevMap.get(evt.displayName, prev)
					def now2 = new Date()
					LOGDEBUG("In contactHandler...${evt.displayName} became ${state.contactStatus} at ${now2}")
					long unxNow = now2.getTime()
    				long unxPrev = prev
    				unxNow = unxNow/1000 
    				unxPrev = unxPrev/1000
					timeDiff = (unxNow-unxPrev)
					LOGDEBUG("In contactHandler...${evt.displayName}: timeDiff in Seconds: ${timeDiff}")
	
					countD = state.contactMapD.get(evt.displayName)
					if(countD == null) countD = 0
   					newCountD = countD + timeDiff
   					state.contactMapD.put(evt.displayName, newCountD)
	
					countW = state.contactMapW.get(evt.displayName)
					if(countW == null) countW = 0
   					newCountW = countW + timeDiff
    				state.contactMapW.put(evt.displayName, newCountW)
	
					countM = state.contactMapM.get(evt.displayName)
					if(countM == null) countM = 0
    				newCountM = countM + timeDiff
   					state.contactMapM.put(evt.displayName, newCountM)
	
    				countY = state.contactMapY.get(evt.displayName)
					if(countY == null) countY = 0
   				 	newCountY = countY + timeDiff
    				state.contactMapY.put(evt.displayName, newCountY)
		
				// Now Triggered Numbers
					int inputNow=timeDiff
					int nDayNow = inputNow / 86400
					int nHrsNow = (inputNow % 86400 ) / 3600
					int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
					int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
				// End Now Triggered Nubmers
	
				// Today's Numbers
					int inputD=newCountD
					int newnDayD = inputD / 86400
					int newnHrsD = (inputD % 86400 ) / 3600
					int newnMinD = ((inputD % 86400 ) % 3600 ) / 60
					int newnSecD = ((inputD % 86400 ) % 3600 ) % 60
				// End Today's Numbers
		
				// Weekly Numbers
					int inputW=newCountW
					int newnDayW = inputW / 86400
					int newnHrsW = (inputW % 86400 ) / 3600
					int newnMinW = ((inputW % 86400 ) % 3600 ) / 60
					int newnSecW = ((inputW % 86400 ) % 3600 ) % 60
				// End Weekly Numbers
		
				// Monthly Numbers
					int inputM=newCountM
					int newnDayM = inputM / 86400
					int newnHrsM = (inputM % 86400 ) / 3600
					int newnMinM = ((inputM % 86400 ) % 3600 ) / 60
					int newnSecM = ((inputM % 86400 ) % 3600 ) % 60
				// End Monthly Numbers
		
				// Yearly Numbers
					int inputY=newCountY
					int newnDayY = inputY / 86400
					int newnHrsY = (inputY % 86400 ) / 3600
					int newnMinY = ((inputY % 86400 ) % 3600 ) / 60
					int newnSecY = ((inputY % 86400 ) % 3600 ) % 60
				// End Yearly Numbers
		
					LOGDEBUG("In contactHandler...${evt.displayName}: This Time: ${nDayNow} Days, ${nHrsNow} Hours, ${nMinNow} Minutes, ${nSecNow} Seconds")
					state.contactMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
					state.contactMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
					state.contactMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
					state.contactMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
					
					LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
				} else {
					countD = state.contactMapD.get(it.displayName)
					countW = state.contactMapW.get(it.displayName)
					countM = state.contactMapM.get(it.displayName)
					countY = state.contactMapY.get(it.displayName)
				// Before Triggered Numbers
					int inputDBef=countD
					int nDayDBef = inputDBef / 86400
					int nHrsDBef = (inputDBef % 86400 ) / 3600
					int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
					int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
					int inputWBef=countW
					int nDayWBef = inputWBef / 86400
					int nHrsWBef = (inputWBef % 86400 ) / 3600
					int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
					int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
					int inputMBef=countM
					int nDayMBef = inputMBef / 86400
					int nHrsMBef = (inputMBef % 86400 ) / 3600
					int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
					int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
					int inputYBef=countY
					int nDayYBef = inputYBef / 86400
					int nHrsYBef = (inputYBef % 86400 ) / 3600
					int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
					int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
				// End Before Triggered Nubmers
					state.contactMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
					state.contactMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
					state.contactMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
					state.contactMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
				}
			}
		} catch(ex) {
			LOGDEBUG("In contactHandler...${evt.displayName}: Device was not found.")
		}
	}
}

def switchHandler(evt) {
	LOGDEBUG("In switchHandler...")
	LOGDEBUG("$evt.displayName: $evt.value")
	switchStatus = evt.value
	
	if(switchStatus == "on") {
		def now1 = new Date()
    	prev = now1.getTime()
		state.switchPrevMap.put(evt.displayName, prev)
		LOGDEBUG("In switchHandler...${evt.displayName} turned ${switchStatus} at ${now1}")
	}
		
	if(switchStatus == "off") {
		state.switchMap = ""
		try {
			switchEvent.each { it -> 
				if(evt.displayName == it.displayName) {
					prev = state.switchPrevMap.get(evt.displayName, prev)
					def now2 = new Date()
					LOGDEBUG("In switchHandler...${evt.displayName} turned ${switchStatus} at ${now2}")
					long unxNow = now2.getTime()
  			  		long unxPrev = prev
  			  		unxNow = unxNow/1000 
  			  		unxPrev = unxPrev/1000
					timeDiff = (unxNow-unxPrev)
					LOGDEBUG("In switchHandler...${evt.displayName}: timeDiff in Seconds: ${timeDiff}")
	
					countD = state.switchMapD.get(evt.displayName)
					if(countD == null) countD = 0
    				newCountD = countD + timeDiff
    				state.switchMapD.put(evt.displayName, newCountD)
	
					countW = state.switchMapW.get(evt.displayName)
					if(countW == null) countW = 0
   	 				newCountW = countW + timeDiff
    				state.switchMapW.put(evt.displayName, newCountW)
	
					countM = state.switchMapM.get(evt.displayName)
					if(countM == null) countM = 0
    				newCountM = countM + timeDiff
    				state.switchMapM.put(evt.displayName, newCountM)
	
    				countY = state.switchMapY.get(evt.displayName)
					if(countY == null) countY = 0
    				newCountY = countY + timeDiff
    				state.switchMapY.put(evt.displayName, newCountY)
		
				// Now Triggered Numbers
					int inputNow=timeDiff
					int nDayNow = inputNow / 86400
					int nHrsNow = (inputNow % 86400 ) / 3600
					int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
					int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
				// End Now Triggered Nubmers
	
				// Today's Numbers
					int inputD=newCountD
					int newnDayD = inputD / 86400
					int newnHrsD = (inputD % 86400 ) / 3600
					int newnMinD = ((inputD % 86400 ) % 3600 ) / 60
					int newnSecD = ((inputD % 86400 ) % 3600 ) % 60
				// End Today's Numbers
		
				// Weekly Numbers
					int inputW=newCountW
					int newnDayW = inputW / 86400
					int newnHrsW = (inputW % 86400 ) / 3600
					int newnMinW = ((inputW % 86400 ) % 3600 ) / 60
					int newnSecW = ((inputW % 86400 ) % 3600 ) % 60
				// End Weekly Numbers
		
				// Monthly Numbers
					int inputM=newCountM
					int newnDayM = inputM / 86400
					int newnHrsM = (inputM % 86400 ) / 3600
					int newnMinM = ((inputM % 86400 ) % 3600 ) / 60
					int newnSecM = ((inputM % 86400 ) % 3600 ) % 60
				// End Monthly Numbers
		
				// Yearly Numbers
					int inputY=newCountY
					int newnDayY = inputY / 86400
					int newnHrsY = (inputY % 86400 ) / 3600
					int newnMinY = ((inputY % 86400 ) % 3600 ) / 60
					int newnSecY = ((inputY % 86400 ) % 3600 ) % 60
				// End Yearly Numbers
		
					LOGDEBUG("In switchHandler...${evt.displayName}: This Time: ${nDayNow} Days, ${nHrsNow} Hours, ${nMinNow} Minutes, ${nSecNow} Seconds")
					state.switchMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
					state.switchMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
					state.switchMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
					state.switchMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
					
					LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
				} else {
					countD = state.switchMapD.get(it.displayName)
					countW = state.switchMapW.get(it.displayName)
					countM = state.switchMapM.get(it.displayName)
					countY = state.switchMapY.get(it.displayName)
				// Before Triggered Numbers
					int inputDBef=countD
					int nDayDBef = inputDBef / 86400
					int nHrsDBef = (inputDBef % 86400 ) / 3600
					int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
					int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
					int inputWBef=countW
					int nDayWBef = inputWBef / 86400
					int nHrsWBef = (inputWBef % 86400 ) / 3600
					int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
					int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
					int inputMBef=countM
					int nDayMBef = inputMBef / 86400
					int nHrsMBef = (inputMBef % 86400 ) / 3600
					int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
					int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
					int inputYBef=countY
					int nDayYBef = inputYBef / 86400
					int nHrsYBef = (inputYBef % 86400 ) / 3600
					int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
					int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
				// End Before Triggered Nubmers
					state.switchMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
					state.switchMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
					state.switchMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
					state.switchMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
				}
			}
		} catch(ex) {
			LOGDEBUG("In switchHandler...${evt.displayName}: Device was not found.")
		}
	}
}

def thermostatHandler(evt) {
	state.tStat = evt.value
	LOGDEBUG("In thermostatHandler...Current Status: ${state.tStat}")
	
	if(state.tStat == "heating") {
		def now1 = new Date()
		prev = now1.getTime()
		state.thermostatPrevMap.put(evt.displayName, prev)
		LOGDEBUG("In thermostatHandler...${evt.displayName} started ${state.tStat} at ${now1}")
	}
	
	if(state.tStat == "cooling") {
		def now1 = new Date()
		prev = now1.getTime()
		state.thermostatPrevMap.put(evt.displayName, prev)
		LOGDEBUG("In thermostatHandler...${evt.displayName} started ${state.tStat} at ${now1}")
	}
	
	if(state.tStat == "idle") {
		state.thermostatMap = ""
		try {
			thermostatEvent.each { it -> 
				if(evt.displayName == it.displayName) {
					prev = state.thermostatPrevMap.get(evt.displayName, prev)
					def now2 = new Date()
					LOGDEBUG("In thermostatHandler...${evt.displayName} is ${state.tStat} at ${now2}")
					long unxNow = now2.getTime()
    				long unxPrev = prev
    				unxNow = unxNow/1000 
    				unxPrev = unxPrev/1000
					timeDiff = (unxNow-unxPrev)
					LOGDEBUG("In thermostatHandler...${evt.displayName}: timeDiff in Seconds: ${timeDiff}")
	
					countD = state.thermostatMapD.get(evt.displayName)
					if(countD == null) countD = 0
    				newCountD = countD + timeDiff
    				state.thermostatMapD.put(evt.displayName, newCountD)
	
					countW = state.thermostatMapW.get(evt.displayName)
					if(countW == null) countW = 0
    				newCountW = countW + timeDiff
    				state.thermostatMapW.put(evt.displayName, newCountW)
	
					countM = state.thermostatMapM.get(evt.displayName)
					if(countM == null) countM = 0
    				newCountM = countM + timeDiff
					LOGDEBUG("${countM} + ${timeDiff} = ${newCountM}")
    				state.thermostatMapM.put(evt.displayName, newCountM)
	
    				countY = state.thermostatMapY.get(evt.displayName)
					if(countY == null) countY = 0
    				newCountY = countY + timeDiff
    				state.thermostatMapY.put(evt.displayName, newCountY)
	
				// Now Triggered Numbers
					int inputNow=timeDiff
					int nDayNow = inputNow / 86400
					int nHrsNow = (inputNow % 86400 ) / 3600
					int nMinNow = ((inputNow % 86400 ) % 3600 ) / 60
					int nSecNow = ((inputNow % 86400 ) % 3600 ) % 60
				// End Now Triggered Nubmers
	
				// Today's Numbers
					int inputD=newCountD
					int newnDayD = inputD / 86400
					int newnHrsD = (inputD % 86400 ) / 3600
					int newnMinD = ((inputD % 86400 ) % 3600 ) / 60
					int newnSecD = ((inputD % 86400 ) % 3600 ) % 60
				// End Today's Numbers
		
				// Weekly Numbers
					int inputW=newCountW
					int newnDayW = inputW / 86400
					int newnHrsW = (inputW % 86400 ) / 3600
					int newnMinW = ((inputW % 86400 ) % 3600 ) / 60
					int newnSecW = ((inputW % 86400 ) % 3600 ) % 60
				// End Weekly Numbers
		
				// Monthly Numbers
					int inputM=newCountM
					int newnDayM = inputM / 86400
					int newnHrsM = (inputM % 86400 ) / 3600
					int newnMinM = ((inputM % 86400 ) % 3600 ) / 60
					int newnSecM = ((inputM % 86400 ) % 3600 ) % 60
				// End Monthly Numbers
		
				// Yearly Numbers
					int inputY=newCountY
					int newnDayY = inputY / 86400
					int newnHrsY = (inputY % 86400 ) / 3600
					int newnMinY = ((inputY % 86400 ) % 3600 ) / 60
					int newnSecY = ((inputY % 86400 ) % 3600 ) % 60
				// End Yearly Numbers
		
					LOGDEBUG("In thermostatHandler...${evt.displayName}: This Time: ${nDayNow} Days, ${nHrsNow} Hours, ${nMinNow} Minutes, ${nSecNow} Seconds")
					state.thermostatMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
					state.thermostatMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
					state.thermostatMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
					state.thermostatMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
					
					LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
				} else {
					countD = state.thermostatMapD.get(it.displayName)
					countW = state.thermostatMapW.get(it.displayName)
					countM = state.thermostatMapM.get(it.displayName)
					countY = state.thermostatMapY.get(it.displayName)
				// Before Triggered Numbers
					int inputDBef=countD
					int nDayDBef = inputDBef / 86400
					int nHrsDBef = (inputDBef % 86400 ) / 3600
					int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
					int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
					int inputWBef=countW
					int nDayWBef = inputWBef / 86400
					int nHrsWBef = (inputWBef % 86400 ) / 3600
					int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
					int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
					int inputMBef=countM
					int nDayMBef = inputMBef / 86400
					int nHrsMBef = (inputMBef % 86400 ) / 3600
					int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
					int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
					int inputYBef=countY
					int nDayYBef = inputYBef / 86400
					int nHrsYBef = (inputYBef % 86400 ) / 3600
					int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
					int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
				// End Before Triggered Nubmers
					state.thermostatMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
					state.thermostatMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
					state.thermostatMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
					state.thermostatMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
				}
			}
		} catch(ex) {
			LOGDEBUG("In thermostatHandler...${evt.displayName}: Device was not found.")
		}
	}
}

def resetMotionMapHandler() {
	LOGDEBUG("In resetMotionMapHandler...")
	if(state.motionMap == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMap")
    	state.motionMap = [:]
		state.motionMap = ""
	}
	if(state.motionMapD == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapD")
    	state.motionMapD = [:]
		motionEvent.each { it -> state.motionMapD.put(it.displayName, 0)}
	}
	if(state.motionMapW == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapW")
    	state.motionMapW = [:]
		motionEvent.each { it -> state.motionMapW.put(it.displayName, 0)}
	}
	if(state.motionMapM == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapM")
    	state.motionMapM = [:]
		motionEvent.each { it -> state.motionMapM.put(it.displayName, 0)}
	}
	if(state.motionMapY == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionMapY")
    	state.motionMapY = [:]
		motionEvent.each { it -> state.motionMapY.put(it.displayName, 0)}
	}
	if(state.motionPrevMap == null) {
		LOGDEBUG("In resetMotionMapHandler...Reseting motionPrevMap")
		def now1 = new Date()
    	prev = now1.getTime()
    	state.motionPrevMap = [:]
		motionEvent.each { it -> state.motionPrevMap.put(it.displayName, prev)}
	}
}

def resetContactMapHandler() {
	LOGDEBUG("In resetContactMapHandler...")
	if(state.contactMap == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMap")
    	state.contactMap = [:]
		state.contactMap = ""
	}
	if(state.contactMapD == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapD")
    	state.contactMapD = [:]
		contactEvent.each { it -> state.contactMapD.put(it.displayName, 0)}
	}
	if(state.contactMapW == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapW")
    	state.contactMapW = [:]
		contactEvent.each { it -> state.contactMapW.put(it.displayName, 0)}
	}
	if(state.contactMapM == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapM")
    	state.contactMapM = [:]
		contactEvent.each { it -> state.contactMapM.put(it.displayName, 0)}
	}
	if(state.contactMapY == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactMapY")
    	state.contactMapY = [:]
		contactEvent.each { it -> state.contactMapY.put(it.displayName, 0)}
	}
	if(state.contactPrevMap == null) {
		LOGDEBUG("In resetContactMapHandler...Reseting contactPrevMap")
		def now1 = new Date()
    	prev = now1.getTime()
    	state.contactPrevMap = [:]
		contactEvent.each { it -> state.contactPrevMap.put(it.displayName, prev)}
	}
}

def resetSwitchMapHandler() {
	LOGDEBUG("In resetSwitchMapHandler...")
	if(state.switchMap == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMap")
    	state.switchMap = [:]
		state.switchMap = ""
	}
	if(state.switchMapD == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapD")
    	state.switchMapD = [:]
		switchEvent.each { it -> state.switchMapD.put(it.displayName, 0)}
	}
	if(state.switchMapW == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapW")
    	state.switchMapW = [:]
		switchEvent.each { it -> state.switchMapW.put(it.displayName, 0)}
	}
	if(state.switchMapM == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapM")
    	state.switchMapM = [:]
		switchEvent.each { it -> state.switchMapM.put(it.displayName, 0)}
	}
	if(state.switchMapY == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchMapY")
    	state.switchMapY = [:]
		switchEvent.each { it -> state.switchMapY.put(it.displayName, 0)}
	}
	if(state.switchPrevMap == null) {
		LOGDEBUG("In resetSwitchMapHandler...Reseting switchPrevMap")
		def now1 = new Date()
    	prev = now1.getTime()
    	state.switchPrevMap = [:]
		switchEvent.each { it -> state.switchPrevMap.put(it.displayName, prev)}
	}
}

def resetThermostatMapHandler() {
	LOGDEBUG("In resetThermostatMapHandler...")
	if(state.thermostatMap == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMap")
    	state.thermostatMap = [:]
		state.thermostatMap = ""
	}
	if(state.thermostatMapD == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapD")
    	state.thermostatMapD = [:]
		thermostatEvent.each { it -> state.thermostatMapD.put(it.displayName, 0)}
	}
	if(state.thermostatMapW == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapW")
    	state.thermostatMapW = [:]
		thermostatEvent.each { it -> state.thermostatMapW.put(it.displayName, 0)}
	}
	if(state.thermostatMapM == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapM")
    	state.thermostatMapM = [:]
		thermostatEvent.each { it -> state.thermostatMapM.put(it.displayName, 0)}
	}
	if(state.thermostatMapY == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatMapY")
    	state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
	if(state.thermostatPrevMap == null) {
		LOGDEBUG("In resetThermostatMapHandler...Reseting thermostatPrevMap")
		def now1 = new Date()
    	prev = now1.getTime()
    	state.thermostatPrevMap = [:]
		thermostatEvent.each { it -> state.thermostatPrevMap.put(it.displayName, prev)}
	}
}

def removingFromMap() {
	LOGDEBUG("In removingStuff...Time to Clean up the Maps")
	LOGDEBUG("In removingStuff...Checking Motion Map: ${state.motionMap}")
	if(state.motionMap) {
		try {
			state.motionMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(motionEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.motionMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.motionMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
	
	LOGDEBUG("In removingStuff...Checking Contact Map: ${state.contactMap}")
	if(state.contactMap) {
		try {
			state.contactMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(contactEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.contactMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.contactMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
	
	LOGDEBUG("In removingStuff...Checking Switch Map: ${state.switchMap}")
	if(state.switchMap) {
		try {
			state.switchMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(switchEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.switchMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.switchMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
	
	LOGDEBUG("In removingStuff...Checking Thermostat Map: ${state.thermostatMap}")
	if(state.thermostatMap) {
		try {
			state.thermostatMap.each { stuff2 -> 
				LOGDEBUG("In removingStuff...Checking: ${stuff2.key}")
				if(thermostatEvents.contains(stuff2.key)) {
					LOGDEBUG("In removingStuff...Found ${stuff2.key}! All is good.")
				} else {
					LOGDEBUG("In removingStuff...Did not find ${stuff2.key}. Removing from Maps.")	 
					state.thermostatMap.remove(stuff2.key)
					LOGDEBUG("In removingStuff...${stuff2.key} was removed.")
				}
			}
		}
		catch (e) {
        	//log.error "Error:  $e"
    	}
		LOGDEBUG("In removingStuff...Finished Map: ${state.thermostaMap}")
	} else { LOGDEBUG("In removingStuff...state.motionMap was NULL") }
}

def resetMotionCountHandler() {
	LOGDEBUG("In resetMotionCountHandler...")
	// Resetting Daily Counter
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayDBef = 0
			newnHrsDBef = 0
			newnMinDBef = 0
			newnSecDBef = 0
		
			state.motionMap += "<b>${it.displayName}</b> - Today: ${newnDayDBef} Days, ${newnHrsDBef} Hours, ${newnMinDBef} Minutes, ${newnSecDBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
    	state.motionMapD = [:]
		motionEvent.each { it -> state.motionMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayWBef = 0
			newnHrsWBef = 0
			newnMinWBef = 0
			newnSeWBef = 0
			
			state.motionMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Week: ${newnDayWBef} Days, ${newnHrsWBef} Hours, ${newnMinWBef} Minutes, ${newnSecWBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.motionMapW = [:]
		motionEvent.each { it -> state.motionMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayMBef = 0
			newnHrsMBef = 0
			newnMinMBef = 0
			newnSecMBef = 0
			
			state.motionMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Month: ${newnDayMBef} Days, ${newnHrsMBef} Hours, ${newnMinMBef} Minutes, ${newnSecMBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.motionMapM = [:]
		motionEvent.each { it -> state.motionMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.motionMap = ""
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayYBef = 0
			newnHrsYBef = 0
			newnMinYBef = 0
			newnSecYBef = 0
			
			state.motionMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap += "<b>${it.displayName}</b> - Year: ${newnDayYBef} Days, ${newnHrsYBef} Hours, ${newnMinYBef} Minutes, ${newnSecYBef} Seconds<br><br>"
		}
		state.motionMapY = [:]
		motionEvent.each { it -> state.motionMapY.put(it.displayName, 0)}
	}
}

def resetContactCountHandler() {
	LOGDEBUG("In resetContactCountHandler...")
	// Resetting Daily Counter
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayDBef = 0
			newnHrsDBef = 0
			newnMinDBef = 0
			newnSecDBef = 0
			
			state.contactMap += "<b>${it.displayName}</b> - Today: ${newnDayDBef} Days, ${newnHrsDBef} Hours, ${newnMinDBef} Minutes, ${newnSecDBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
    	state.contactMapD = [:]
		contactEvent.each { it -> state.contactMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	LOGDEBUG("In resetContactCountHandler...dayOfWeek: ${dayOfWeek}")
	if(dayOfWeek == 1) {
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayWBef = 0
			newnHrsWBef = 0
			newnMinWBef = 0
			newnSecWBef = 0
			
			state.contactMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Week: ${newnDayWBef} Days, ${newnHrsWBef} Hours, ${newnMinWBef} Minutes, ${newnSecWBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.contactMapW = [:]
		contactEvent.each { it -> state.contactMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayMBef = 0
			newnHrsMBef = 0
			newnMinMBef = 0
			newnSecMBef = 0
			
			state.contactMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Month: ${newnDayMBef} Days, ${newnHrsMBef} Hours, ${newnMinMBef} Minutes, ${newnSecMBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.contactMapM = [:]
		contactEvent.each { it -> state.contactMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.contactMap = ""
		contactEvent.each { it -> 
			countD = state.contactMapD.get(it.displayName)
			countW = state.contactMapW.get(it.displayName)
			countM = state.contactMapM.get(it.displayName)
			countY = state.contactMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayYBef = 0
			newnHrsYBef = 0
			newnMinYBef = 0
			newnSecYBef = 0
			
			state.contactMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap += "<b>${it.displayName}</b> - Year: ${newnDayYBef} Days, ${newnHrsYBef} Hours, ${newnMinYBef} Minutes, ${newnSecYBef} Seconds<br><br>"
		}
		state.contactMapY = [:]
		contactEvent.each { it -> state.contactMapY.put(it.displayName, 0)}
	}
}

def resetSwitchCountHandler() {
	LOGDEBUG("In resetSwitchCountHandler...")
	// Resetting Daily Counter
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayDBef = 0
			newnHrsDBef = 0
			newnMinDBef = 0
			newnSecDBef = 0
			
			state.switchMap += "<b>${it.displayName}</b> - Today: ${newnDayDBef} Days, ${newnHrsDBef} Hours, ${newnMinDBef} Minutes, ${newnSecDBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
    	state.switchMapD = [:]
		switchEvent.each { it -> state.switchMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayWBef = 0
			newnHrsWBef = 0
			newnMinWBef = 0
			newnSecWBef = 0
			
			state.switchMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Week: ${newnDayWBef} Days, ${newnHrsWBef} Hours, ${newnMinWBef} Minutes, ${newnSecWBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.switchMapW = [:]
		switchEvent.each { it -> state.switchMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayMBef = 0
			newnHrsMBef = 0
			newnMinMBef = 0
			newnSecMBef = 0
			
			state.switchMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Month: ${newnDayMBef} Days, ${newnHrsMBef} Hours, ${newnMinMBef} Minutes, ${newnSecMBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.switchMapM = [:]
		switchEvent.each { it -> state.switchMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.switchMap = ""
		switchEvent.each { it -> 
			countD = state.switchMapD.get(it.displayName)
			countW = state.switchMapW.get(it.displayName)
			countM = state.switchMapM.get(it.displayName)
			countY = state.switchMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayYBef = 0
			newnHrsYBef = 0
			newnMinYBef = 0
			newnSecYBef = 0
			
			state.switchMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap += "<b>${it.displayName}</b> - Year: ${newnDayYBef} Days, ${newnHrsYBef} Hours, ${newnMinYBef} Minutes, ${newnSecYBef} Seconds<br><br>"
		}
		state.switchMapY = [:]
		switchEvent.each { it -> state.switchMapY.put(it.displayName, 0)}
	}
}

def resetThermostatCountHandler() {
	LOGDEBUG("In resetThermostatCountHandler...")
	// Resetting Daily Counter
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayDBef = 0
			newnHrsDBef = 0
			newnMinDBef = 0
			newnSecDBef = 0
			
			state.thermostatMap += "<b>${it.displayName}</b> - Today: ${newnDayDBef} Days, ${newnHrsDBef} Hours, ${newnMinDBef} Minutes, ${newnSecDBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
    	state.thermostatMapD = [:]
		thermostatEvent.each { it -> state.thermostatMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayWBef = 0
			newnHrsWBef = 0
			newnMinWBef = 0
			newnSecWBef = 0
			
			state.thermostatMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Week: ${newnDayWBef} Days, ${newnHrsWBef} Hours, ${newnMinWBef} Minutes, ${newnSecWBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.thermostatMapW = [:]
		thermostatEvent.each { it -> state.thermostatMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayMBef = 0
			newnHrsMBef = 0
			newnMinMBef = 0
			newnSecMBef = 0
			
			state.thermostatMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Month: ${newnDayMBef} Days, ${newnHrsMBef} Hours, ${newnMinMBef} Minutes, ${newnSecMBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		}
		state.thermostatMapM = [:]
		thermostatEvent.each { it -> state.thermostatMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.thermostatMap = ""
		thermostatEvent.each { it -> 
			countD = state.thermostatMapD.get(it.displayName)
			countW = state.thermostatMapW.get(it.displayName)
			countM = state.thermostatMapM.get(it.displayName)
			countY = state.thermostatMapY.get(it.displayName)
			// Before Triggered Numbers
		int inputDBef=countD
		int nDayDBef = inputDBef / 86400
		int nHrsDBef = (inputDBef % 86400 ) / 3600
		int nMinDBef = ((inputDBef % 86400 ) % 3600 ) / 60
		int nSecDBef = ((inputDBef % 86400 ) % 3600 ) % 60
		
		int inputWBef=countW
		int nDayWBef = inputWBef / 86400
		int nHrsWBef = (inputWBef % 86400 ) / 3600
		int nMinWBef = ((inputWBef % 86400 ) % 3600 ) / 60
		int nSecWBef = ((inputWBef % 86400 ) % 3600 ) % 60
		
		int inputMBef=countM
		int nDayMBef = inputMBef / 86400
		int nHrsMBef = (inputMBef % 86400 ) / 3600
		int nMinMBef = ((inputMBef % 86400 ) % 3600 ) / 60
		int nSecMBef = ((inputMBef % 86400 ) % 3600 ) % 60
		
		int inputYBef=countY
		int nDayYBef = inputYBef / 86400
		int nHrsYBef = (inputYBef % 86400 ) / 3600
		int nMinYBef = ((inputYBef % 86400 ) % 3600 ) / 60
		int nSecYBef = ((inputYBef % 86400 ) % 3600 ) % 60
	// End Before Triggered Nubmers	
			
			newnDayYBef = 0
			newnHrsYBef = 0
			newnMinYBef = 0
			newnSecYBef = 0
			
			state.thermostatMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Year: ${newnDayYBef} Days, ${newnHrsYBef} Hours, ${newnMinYBef} Minutes, ${newnSecYBef} Seconds<br><br>"
		}
		state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
}

def sendMessage(msg) {
	LOGDEBUG("${msg}")
    if (pushNotification) {
        sendPush(msg)
    }
}

// ********** Normal Stuff **********

def pauseOrNot(){							// Modified from @Cobra Code
	LOGDEBUG("In pauseOrNot...")
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     	LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
        }
     }
  }    
}

def setDefaults(){
	setVersion()
	setupNewStuff()
	removingFromMap()
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	
	editALine = "false"
	whichMap = ""
	lineToEdit = ""
	ecountD = 0
	ecountW = 0
	ecountM = 0
	ecountY = 0
}

def logCheck(){									// Modified from @Cobra Code
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){								// Modified from @Cobra Code
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {							// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){					// Modified from @Stephack Code
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Abacus - Time Traveler - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
