import groovy.time.TimeCategory
/**
 *  ****************  Abacus - Time Traveler Child ****************
 *
 *  Design Usage:
 *  Track how long a Device has been active. Displays Daily, Weekly, Monthly and Yearly Timers!
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
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
 *  V1.0.1 - 01/04/19 - Major logic change to calculate how long a device was active.
 *  V1.0.0 - 01/03/19 - Initial release.
 *
 */

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
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
			input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	}
		section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
			input "triggerMode", "enum", title: "Select a Mode", submitOnChange: true,  options: ["Normal_Tracking","Delete_A_Device"], required: true, Multiple: false
		}
		if(triggerMode == "Normal_Tracking") {
			section() {
				paragraph "Everything is up and running"
			}
		}
		if(triggerMode == "Delete_A_Device") {
			section("Instructions for Deleting a device:", hideable: true, hidden: true) {
				paragraph "If a device needs to be removed<br> - De-select it from all of the Device lists above.<br> - Then come back down here and select the device from the list below.<br> - Click 'Done' to save the new settings.<br> - Now trigger the device to be removed. ie. motion='active', contact='open', switch='on' and thermostat='heating' or 'cooling' to trigger the removal.<br> - Be sure to go back into this child app and change the 'Maintenance' setting back to 'Normal_Tracking'<br> - Hit 'Done' again and everything is all set."
			}
			section() {
				paragraph "<div style='color:red;font-weight: bold'>Use with CAUTION. Deleting a device completely removes the all of it's stats.</b><br>Please see the Instructions above before attempting to remove a device.</div>"
				input(name: "deleteMotionEvent", type: "capability.motionSensor", title: "Motion sensor(s) to REMOVE", submitOnChange: true, required: false, multiple: true)
				input(name: "deleteContactEvent", type: "capability.contactSensor", title: "Contact Sensor(s) to REMOVE", submitOnChange: true, required: false, multiple: true)
				input(name: "deleteSwitchEvent", type: "capability.switch", title: "Switch Device(s) to REMOVE", submitOnChange: true, required: false, multiple: true)
				input(name: "deleteThermostatEvent", type: "capability.thermostat", title: "Thermostat(s) to REMOVE", submitOnChange: true, required: false, multiple: true)
			}
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

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=35 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
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
	if(triggerMode == "Normal_Tracking") subscribe(motionEvent, "motion", motionHandler)
	if(triggerMode == "Normal_Tracking") subscribe(contactEvent, "contact", contactHandler)
	if(triggerMode == "Normal_Tracking") subscribe(switchEvent, "switch", switchHandler)
	if(triggerMode == "Normal_Tracking") subscribe(thermostatEvent, "thermostatOperatingState", thermostatHandler)

	if(triggerMode == "Delete_A_Device") subscribe(deleteMotionEvent, "motion.active", deleteMotionHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteContactEvent, "contact.open", deleteContactHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteSwitchEvent, "switch.on", deleteSwitchHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteThermostatEvent, "thermostatOperatingState.heating", deleteThermostatHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteThermostatEvent, "thermostatOperatingState.cooling", deleteThermostatHandler)
	
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

def deleteMotionHandler(evt) {
	if(triggerMode == "Delete_A_Device") {	
		LOGDEBUG("In deleteMotionHandler...")

		LOGDEBUG("In deleteMotionHandler...Looking for Motion devices to DELETE")
		LOGDEBUG("Working on... ${evt.displayName}")
		countD = state.motionMapD.get(evt.displayName)
		countW = state.motionMapW.get(evt.displayName)
		countM = state.motionMapM.get(evt.displayName)
		countY = state.motionMapY.get(evt.displayName)
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
		try {
			state.motionMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
			LOGDEBUG("${evt.displayName} - Removed from motionMap")
		} catch(ex) {
			LOGDEBUG("In deleteMotionHandler...${evt.displayName}: Device was not found.")
		}
		newCountD = 0
		newCountW = 0
		newCountM = 0
		newCountY = 0
   		state.motionMapD.put(evt.displayName, newCountD)
		state.motionMapW.put(evt.displayName, newCountW)
		state.motionMapM.put(evt.displayName, newCountM)
		state.motionMapY.put(evt.displayName, newCountY)
		LOGDEBUG("${evt.displayName} - Removed from motionMap D W M Y")
		LOGDEBUG("Finished removing... ${evt.displayName}")
	}
}

def deleteContactHandler(evt) {
	if(triggerMode == "Delete_A_Device") {	
		LOGDEBUG("In deleteContactHandler...")

		LOGDEBUG("In deleteContactHandler...Looking for Contact devices to DELETE")
		LOGDEBUG("Working on... ${evt.displayName}")
		countD = state.contactMapD.get(evt.displayName)
		countW = state.contactMapW.get(evt.displayName)
		countM = state.contactMapM.get(evt.displayName)
		countY = state.contactMapY.get(evt.displayName)
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
		try {
			state.contactMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
			LOGDEBUG("${evt.displayName} - Removed from contactMap")
		} catch(ex) {
			LOGDEBUG("In deleteContactHandler...${evt.displayName}: Device was not found.")
		}
		newCountD = 0
		newCountW = 0
		newCountM = 0
		newCountY = 0
   		state.contactMapD.put(evt.displayName, newCountD)
		state.contactMapW.put(evt.displayName, newCountW)
		state.contactMapM.put(evt.displayName, newCountM)
		state.contactMapY.put(evt.displayName, newCountY)
		LOGDEBUG("${evt.displayName} - Removed from contactMap D W M Y")
		LOGDEBUG("Finished removing... ${evt.displayName}")
	}
}

def deleteSwitchHandler(evt) {
	if(triggerMode == "Delete_A_Device") {	
		LOGDEBUG("In deleteSwitchHandler...")

		LOGDEBUG("In deleteSwitchHandler...Looking for Switch devices to DELETE")
		LOGDEBUG("Working on... ${evt.displayName}")
		countD = state.switchMapD.get(evt.displayName)
		countW = state.switchMapW.get(evt.displayName)
		countM = state.switchMapM.get(evt.displayName)
		countY = state.switchMapY.get(evt.displayName)
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
		try {
			state.switchMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
			LOGDEBUG("${evt.displayName} - Removed from switchMap")
		} catch(ex) {
			LOGDEBUG("In deleteSwitchHandler...${evt.displayName}: Device was not found.")
		}
		newCountD = 0
		newCountW = 0
		newCountM = 0
		newCountY = 0
   		state.switchMapD.put(evt.displayName, newCountD)
		state.switchMapW.put(evt.displayName, newCountW)
		state.switchMapM.put(evt.displayName, newCountM)
		state.switchMapY.put(evt.displayName, newCountY)
		LOGDEBUG("${evt.displayName} - Removed from switchMap D W M Y")
		LOGDEBUG("Finished removing... ${evt.displayName}")
	}
}

def deleteThermostatHandler(evt) {
	if(triggerMode == "Delete_A_Device") {	
		LOGDEBUG("In deleteThermostatHandler...")

		LOGDEBUG("In deleteThermostatHandler...Looking for Thermostat devices to DELETE")
		LOGDEBUG("Working on... ${evt.displayName}")
		countD = state.thermostatMapD.get(evt.displayName)
		countW = state.thermostatMapW.get(evt.displayName)
		countM = state.thermostatMapM.get(evt.displayName)
		countY = state.thermostatMapY.get(evt.displayName)
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
		try {
			state.thermostatMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
			LOGDEBUG("${evt.displayName} - Removed from thermostatMap")
		} catch(ex) {
			LOGDEBUG("In deleteThermostatHandler...${evt.displayName}: Device was not found.")
		}
		newCountD = 0
		newCountW = 0
		newCountM = 0
		newCountY = 0
   		state.thermostatMapD.put(evt.displayName, newCountD)
		state.thermostatMapW.put(evt.displayName, newCountW)
		state.thermostatMapM.put(evt.displayName, newCountM)
		state.thermostatMapY.put(evt.displayName, newCountY)
		LOGDEBUG("${evt.displayName} - Removed from thermostatMap D W M Y")
		LOGDEBUG("Finished removing... ${evt.displayName}")
	}
}

def motionHandler(evt) {
if(triggerMode == "Normal_Tracking") {
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
	
		LOGDEBUG("To Delete - <b>${evt.displayName}</b><br>Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br>")
		try {
			state.motionMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
			state.motionMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
			state.motionMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
			state.motionMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
			state.motionMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
		} catch(ex) {
			LOGDEBUG("In motionHandler...${evt.displayName}: Device was not found.")
		}
		LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
	
	}
}
}
	
def contactHandler(evt) {
if(triggerMode == "Normal_Tracking") {
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
	
			LOGDEBUG("To Delete - <b>${evt.displayName}</b><br>Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br>")
		try {
			state.contactMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
			state.contactMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
			state.contactMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
			state.contactMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
			state.contactMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
		} catch(ex) {
			LOGDEBUG("In contactHandler...${evt.displayName}: Device was not found.")
		}
			LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
	}
}
}

def switchHandler(evt) {
if(triggerMode == "Normal_Tracking") {
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
	
		LOGDEBUG("To Delete - <b>${evt.displayName}</b><br>Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br>")
		try {
			state.switchMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
			state.switchMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
			state.switchMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
			state.switchMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
			state.switchMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
		} catch(ex) {
			LOGDEBUG("In switchHandler...${evt.displayName}: Device was not found.")
		}
		LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
	
	}
}
}

def thermostatHandler(evt) {
if(triggerMode == "Normal_Tracking") {
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
	
		LOGDEBUG("To Delete - <b>${evt.displayName}</b><br>Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br>")
		try {
			state.thermostatMap -= "<b>${evt.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap -= "<b>${evt.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap -= "<b>${evt.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap -= "<b>${evt.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
			state.thermostatMap += "<b>${evt.displayName}</b> - Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>"
			state.thermostatMap += "<b>${evt.displayName}</b> - Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>"
			state.thermostatMap += "<b>${evt.displayName}</b> - Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>"
			state.thermostatMap += "<b>${evt.displayName}</b> - Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br><br>"
		} catch(ex) {
			LOGDEBUG("In thermostatHandler...${evt.displayName}: Device was not found.")
		}
		LOGDEBUG("Adding In - <b>${evt.displayName}</b><br>Today: ${newnDayD} Days, ${newnHrsD} Hours, ${newnMinD} Minutes, ${newnSecD} Seconds<br>Week: ${newnDayW} Days, ${newnHrsW} Hours, ${newnMinW} Minutes, ${newnSecW} Seconds<br>Month: ${newnDayM} Days, ${newnHrsM} Hours, ${newnMinM} Minutes, ${newnSecM} Seconds<br>Year: ${newnDayY} Days, ${newnHrsY} Hours, ${newnMinY} Minutes, ${newnSecY} Seconds<br>")
	
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
		switchEvent.each { it -> state.motionPrevMap.put(it.displayName, prev)}
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
		switchEvent.each { it -> state.contactPrevMap.put(it.displayName, prev)}
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
		switchEvent.each { it -> state.thermostatPrevMap.put(it.displayName, prev)}
	}
}

def resetMotionCountHandler() {
	LOGDEBUG("In resetMotionCountHandler...")
	// Resetting Daily Counter
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
			
			state.motionMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.motionMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.motionMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.motionMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.motionMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.contactMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.contactMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.contactMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.contactMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.contactMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.switchMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.switchMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.switchMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.switchMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.switchMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.thermostatMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.thermostatMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.thermostatMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
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
			
			state.thermostatMap -= "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap -= "<b>${it.displayName}</b> - Year: ${nDayYBef} Days, ${nHrsYBef} Hours, ${nMinYBef} Minutes, ${nSecYBef} Seconds<br><br>"
		
			state.thermostatMap += "<b>${it.displayName}</b> - Today: ${nDayDBef} Days, ${nHrsDBef} Hours, ${nMinDBef} Minutes, ${nSecDBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Week: ${nDayWBef} Days, ${nHrsWBef} Hours, ${nMinWBef} Minutes, ${nSecWBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Month: ${nDayMBef} Days, ${nHrsMBef} Hours, ${nMinMBef} Minutes, ${nSecMBef} Seconds<br>"
			state.thermostatMap += "<b>${it.displayName}</b> - Year: ${newnDayYBef} Days, ${newnHrsYBef} Hours, ${newnMinYBef} Minutes, ${newnSecYBef} Seconds<br><br>"
		}
		state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
}

def appButtonHandler(btn){
	LOGDEBUG("In appButtonHandler...Delete Line")
    state.btnCall = btn
	LOGDEBUG("In appButtonHandler...state.btnCall: ${state.btnCall}")
    if(state.btnCall == "runButton"){
        log.info "${app.label} - Run Now button was pressed..."
		deleteLine = "${lineToDelete}<br>"
		LOGDEBUG("Deleting Line: ${deleteLine}")
		try {
			if(whichMap == "Switch") { state.switchMap -= "${deleteLine}" }
			if(whichMap == "Motion") { state.motionMap -= "${deleteLine}" }
			if(whichMap == "Contact") { state.contactMap -= "${deleteLine}" }
			if(whichMap == "Thermostat") { state.thermostatMap -= "${deleteLine}" }
			log.info "${app.label} - ${deleteLine} was deleted.}"
		} catch (all) {
			log.info "${app.label} - Unable to delete, line did not exist"
		}
		
    }
}

def sendMessage(msg) {
	LOGDEBUG("${msg}")
    if (pushNotification) {
        sendPush(msg)
    }
}



// ********** Normal Stuff **********

def pauseOrNot(){
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
	setupNewStuff()
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

def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Abacus - Time Traveler - App Version: 1.0.1 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a></div>"
	}
}
