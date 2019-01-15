/**
 *  ****************  Abacus - Intense Counting Child ****************
 *
 *  Design Usage:
 *  Count how many times a Device is triggered. Displays Daily, Weekly, Monthly and Yearly counts!
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Thanks to Stephan Hackett (@stephack) for the idea to change up the colors.
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
 *  V1.0.6 - 01/15/19 - Updated footer with update check and links
 *  V1.0.5 - 01/04/19 - Removed some left over code causing an error.
 *  V1.0.4 - 01/03/19 - Bug fixes and a much better way to remove a device and it's stats.
 *  V1.0.3 - 01/02/19 - Changed name. Cleaned up code.
 *  V1.0.2 - 01/01/19 - Fixed a typo in the countReset modules. Added in ability to count Thermostats! Again, wipe is recommended.
 *  V1.0.1 - 12/31/18 - Major rewrite to how the app finds new devices and sets them up for the first time. You will need to 
 *						delete any lines that have null in them or delete the child app and start over. Sorry.
 *  V1.0.0 - 12/30/18 - Initial release.
 *
 */

def version(){"v1.0.6"}

definition(
    name: "Abacus - Intense Counting Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Count how many times a Device is triggered. Displays Daily, Weekly, Monthly and Yearly counts!",
    category: "Useless",
	
parent: "BPTWorld:Abacus - Intense Counting",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
)

preferences {
    page(name: "pageConfig")
	page(name: "pageCounts")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Intense Counting</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {
	display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Information</b>"
			paragraph "Daily counts are reset each morning.<br>Weekly counts are reset each Sunday.<br>Monthly counts are reset at on the 1st of each month.<br>Yearly counts get reset on Jan 1st.<br>All count resets happen between 12:05am and 12:10am"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageCounts", title: "Abacus - Intense Counting Report", description: "Click here to view the Abacus Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Most Common Devices")) {
			input(name: "switchEvent", type: "capability.switch", title: "Switch Device(s) to count", submitOnChange: true, required: false, multiple: true)
			input(name: "motionEvent", type: "capability.motionSensor", title: "Motion sensor(s) to count", submitOnChange: true, required: false, multiple: true)
			input(name: "contactEvent", type: "capability.contactSensor", title: "Contact Sensor(s) to count", submitOnChange: true, required: false, multiple: true)
			input(name: "thermostatEvent", type: "capability.thermostat", title: "Thermostat(s) to count", submitOnChange: true, required: false, multiple: true)
		}
		//section(getFormat("header-green", "${getImage("Blank")}"+" Even More Devices")) {
		//	paragraph "If you have a device not found in the list above, try this option. If your device doesn't work, please message me on the forum and let me know what type of device it is. No promises but I will take a look at it."
		//	input "sensorDevice", "capability.sensor", title: "Select Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
		//}
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
		section() {
			//input(name: "editALine", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Need to edit a Device?")
		}
		if(editALine) {
			section("Instructions for Editing a line:", hideable: true, hidden: true) {
				paragraph "If a device needs to be Edited<br> - Click on 'Couting Events Reports'<br> - Highlight the complete name of the device to be editied<br> - Press 'ctrl'-C to copy it, then click 'Done'<br> - Now scroll back down to the 'Maintenance' section<br> - Flip the switch for 'Need to edit a Device'<br> - Paste in the device name to edit and then click outside the box<br> - Select which type of event the device is a part of<br> - In a second or two the line values should appear below."
			}
			section() {
				// Wipe out inputs
				whichMap = "No selection"
				lineToEdit = ""
				lineExist = "No"
				paragraph "<div style='color:red;font-weight: bold'>Use with CAUTION. Editing a line changes the count values.</b><br>Please see the Instructions above before attempting to edit a device.</div>"
				input "whichMap", "enum", title: "Which Event type...", submitOnChange: true,  options: ["Switch","Motion","Contact"], required: true, Multiple: false
				input "lineToEdit", "text", title: "Exact name of the device to be editied", required: false, submitOnChange: true
				
			}
			if(lineToEdit) {
				section() {
					if(whichMap == "Switch") {
						try {
							ecountD = state.switchMapD.get(lineToEdit)
							ecountW = state.switchMapW.get(lineToEdit)
							ecountM = state.switchMapM.get(lineToEdit)
							ecountY = state.switchMapY.get(lineToEdit)
							lineExist = "Yes"
						} catch (all) {
							log.info "${app.label} - Unable to edit, ${lineToEdit} does not exist"
							lineExist = "No"
						}
					}
					if(whichMap == "Motion") {
						try {
							ecountD = state.motionMapD.get(lineToEdit)
							ecountW = state.motionMapW.get(lineToEdit)
							ecountM = state.motionMapM.get(lineToEdit)
							ecountY = state.motionMapY.get(lineToEdit)
							lineExist = "Yes"
						} catch (all) {
							log.info "${app.label} - Unable to edit, ${lineToEdit} does not exist"
							lineExist = "No"
						}
					}
					if(whichMap == "Contact") {
						try {
							ecountD = state.contactMapD.get(lineToEdit)
							ecountW = state.contactMapW.get(lineToEdit)
							ecountM = state.contactMapM.get(lineToEdit)
							ecountY = state.contactMapY.get(lineToEdit)
							lineExist = "Yes"
						} catch (all) {
							log.info "${app.label} - Unable to edit, ${lineToEdit} does not exist"
							lineExist = "No"
						}
					}
					if(whichMap == "Thermostat") {
						try {
							ecountD = state.thermostatMapD.get(lineToEdit)
							ecountW = state.thermostatMapW.get(lineToEdit)
							ecountM = state.thermostatMapM.get(lineToEdit)
							ecountY = state.thermostatMapY.get(lineToEdit)
							lineExist = "Yes"
						} catch (all) {
							log.info "${app.label} - Unable to edit, ${lineToEdit} does not exist"
							lineExist = "No"
						}
					}
					if(lineExist == "Yes") {
						input("changeCountD", "number", title: "Day", required: "true", defaultValue: "${ecountD}", width:"3")
						input("changeCountW", "number", title: "Week", required: "true", defaultValue: "${ecountW}", width:"3")
						input("changeCountM", "number", title: "Month", required: "true", defaultValue: "${ecountM}", width:"3")
						input("changeCountY", "number", title: "Year", required: "true", defaultValue: "${ecountY}", width:"3")
					} else {
						paragraph "<div style='color:red'>Unable to edit, ${lineToEdit} does not exist</div>"
					}
				}
			} else {
				section() {
					paragraph "<div style='color:red'>Line to Edit was not found. Please double check the device name from the 'Abacus Reports' page.</div>"
				}
			}
		}
		display2()
	}
}

def pageCounts(params) {
	dynamicPage(name: "pageStatus", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Intense Counting</h2>", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
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
			if(state.motionMap == null && state.contactMap == null && state.switchMap == null && state.thermostatMap) {
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
	if(triggerMode == "Normal_Tracking") subscribe(motionEvent, "motion.active", motionHandler)
	if(triggerMode == "Normal_Tracking") subscribe(contactEvent, "contact.open", contactHandler)
	if(triggerMode == "Normal_Tracking") subscribe(switchEvent, "switch.on", switchHandler)
	if(triggerMode == "Normal_Tracking") subscribe(thermostatEvent, "thermostatOperatingState", thermostatHandler)
	
	if(triggerMode == "Delete_A_Device") subscribe(deleteMotionEvent, "motion.active", deleteMotionHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteContactEvent, "contact.open", deleteContactHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteSwitchEvent, "switch.on", deleteSwitchHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteThermostatEvent, "thermostatOperatingState.heating", deleteThermostatHandler)
	if(triggerMode == "Delete_A_Device") subscribe(deleteThermostatEvent, "thermostatOperatingState.cooling", deleteThermostatHandler)
	
	schedule("0 5 0 * * ? *", resetMotionCountHandler)
	schedule("0 6 0 * * ? *", resetContactCountHandler)
	schedule("0 7 0 * * ? *", resetSwitchCountHandler)
	schedule("0 8 0 * * ? *", resetThermostatCountHandler)
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
	}
	
	// ********** Ending Motion Devices **********
	
	// ********** Starting Contact Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Contact Maps")
	
	if(state.contactMap == null) resetContactMapHandler()
	if(state.contactMapD == null) resetContactMapHandler()
	if(state.contactMapW == null) resetContactMapHandler()
	if(state.contactMapM == null) resetContactMapHandler()
	if(state.contactMapY == null) resetContactMapHandler()

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
	}
	
	// ********** Ending Contact Devices **********
	
	// ********** Starting Switch Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Switch Maps")
	
	if(state.switchMap == null) resetSwitchMapHandler()
	if(state.switchMapD == null) resetSwitchMapHandler()
	if(state.switchMapW == null) resetSwitchMapHandler()
	if(state.switchMapM == null) resetSwitchMapHandler()
	if(state.switchMapY == null) resetSwitchMapHandler()

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
	}
	
	// ********** Ending Switch Devices **********
	
	// ********** Starting Thermostat Devices **********
	
	LOGDEBUG("In setupNewStuff...Setting up Thermostat Maps")
	
	if(state.thermostatMap == null) resetThermostatMapHandler()
	if(state.thermostatMapD == null) resetThermostatMapHandler()
	if(state.thermostatMapW == null) resetThermostatMapHandler()
	if(state.thermostatMapM == null) resetThermostatMapHandler()
	if(state.thermostatMapY == null) resetThermostatMapHandler()

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
		
		try {
			state.motionMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
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
		
		try {
			state.contactMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
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
		
		try {
			state.switchMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
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
		
		try {
			state.thermostatMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
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
	LOGDEBUG("In motionHandler...")
	LOGDEBUG("In motionHandler: Device: $evt.displayName is $evt.value")
	
	if(state.motionMap == null) resetMotionMapHandler()
	if(state.motionMapD == null) resetMotionMapHandler()
	if(state.motionMapW == null) resetMotionMapHandler()
	if(state.motionMapM == null) resetMotionMapHandler()
	if(state.motionMapY == null) resetMotionMapHandler()
	
	if(state.motionMapD.get(evt.displayName) == null) {
		LOGDEBUG("In motionHandler: ${evt.displayName} not found in Map D...Adding it in.")
		state.motionMapD.put(evt.displayName, 0)
	}
	if(state.motionMapW.get(evt.displayName) == null) {
		LOGDEBUG("In motionHandler: ${evt.displayName} not found in Map W...Adding it in.")
		state.motionMapW.put(evt.displayName, 0)
	}
	if(state.motionMapM.get(evt.displayName) == null) {
		LOGDEBUG("In motionHandler: ${evt.displayName} not found in Map M...Adding it in.")
		state.motionMapM.put(evt.displayName, 0)
	}
	if(state.motionMapY.get(evt.displayName) == null) {
		LOGDEBUG("In motionHandler: ${evt.displayName} not found in Map Y...Adding it in.")
		state.motionMapY.put(evt.displayName, 0)
	}
	countD = state.motionMapD.get(evt.displayName)
    newCountD = countD + 1
    state.motionMapD.put(evt.displayName, newCountD)
	
	countW = state.motionMapW.get(evt.displayName)
    newCountW = countW + 1
    state.motionMapW.put(evt.displayName, newCountW)
	
	countM = state.motionMapM.get(evt.displayName)
    newCountM = countM + 1
    state.motionMapM.put(evt.displayName, newCountM)
	
    countY = state.motionMapY.get(evt.displayName)
    newCountY = countY + 1
    state.motionMapY.put(evt.displayName, newCountY)
	
	LOGDEBUG("To Delete - ${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
	
	state.motionMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
	state.motionMap += "${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
	
	LOGDEBUG("Adding In - ${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
}

def contactHandler(evt) {
	LOGDEBUG("In contactHandler...")
	LOGDEBUG("$evt.displayName: $evt.value")

	if(state.contactMap == null) resetContactMapHandler()
	if(state.contactMapD == null) resetContactMapHandler()
	if(state.contactMapW == null) resetContactMapHandler()
	if(state.contactMapM == null) resetContactMapHandler()
	if(state.contactMapY == null) resetContactMapHandler()
	
	if(state.contactMapD.get(evt.displayName) == null) {
		LOGDEBUG("In contactHandler: ${evt.displayName} not found in Map D...Adding it in.")
		state.contactMapD.put(evt.displayName, 0)
	}
	if(state.contactMapW.get(evt.displayName) == null) {
		LOGDEBUG("In contactHandler: ${evt.displayName} not found in Map W...Adding it in.")
		state.contactMapW.put(evt.displayName, 0)
	}
	if(state.contactMapM.get(evt.displayName) == null) {
		LOGDEBUG("In contactHandler: ${evt.displayName} not found in Map M...Adding it in.")
		state.contactMapM.put(evt.displayName, 0)
	}
	if(state.contactMapY.get(evt.displayName) == null) {
		LOGDEBUG("In contactHandler: ${evt.displayName} not found in Map Y...Adding it in.")
		state.contactMapY.put(evt.displayName, 0)
	}
	countD = state.contactMapD.get(evt.displayName)
    newCountD = countD + 1
    state.contactMapD.put(evt.displayName, newCountD)
	
	countW = state.contactMapW.get(evt.displayName)
    newCountW = countW + 1
    state.contactMapW.put(evt.displayName, newCountW)
	
	countM = state.contactMapM.get(evt.displayName)
    newCountM = countM + 1
    state.contactMapM.put(evt.displayName, newCountM)
	
    countY = state.contactMapY.get(evt.displayName)
    newCountY = countY + 1
    state.contactMapY.put(evt.displayName, newCountY)
	
	LOGDEBUG("To Delete - ${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
	
	state.contactMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
	state.contactMap += "${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
	
	LOGDEBUG("Adding In - ${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
}

def switchHandler(evt) {
	LOGDEBUG("In switchHandler...")
	LOGDEBUG("$evt.displayName: $evt.value")

	if(state.switchMap == null) resetSwitchMapHandler()
	if(state.switchMapD == null) resetSwitchMapHandler()
	if(state.switchMapW == null) resetSwitchMapHandler()
	if(state.switchMapM == null) resetSwitchMapHandler()
	if(state.switchMapY == null) resetSwitchMapHandler()
	
	if(state.switchMapD.get(evt.displayName) == null) {
		LOGDEBUG("In switchHandler: ${evt.displayName} not found in Map D...Adding it in.")
		state.switchMapD.put(evt.displayName, 0)
	}
	if(state.switchMapW.get(evt.displayName) == null) {
		LOGDEBUG("In switchHandler: ${evt.displayName} not found in Map W...Adding it in.")
		state.switchMapW.put(evt.displayName, 0)
	}
	if(state.switchMapM.get(evt.displayName) == null) {
		LOGDEBUG("In switchHandler: ${evt.displayName} not found in Map M...Adding it in.")
		state.switchMapM.put(evt.displayName, 0)
	}
	if(state.switchMapY.get(evt.displayName) == null) {
		LOGDEBUG("In switchHandler: ${evt.displayName} not found in Map Y...Adding it in.")
		state.switchMapY.put(evt.displayName, 0)
	}
	countD = state.switchMapD.get(evt.displayName)
    newCountD = countD + 1
    state.switchMapD.put(evt.displayName, newCountD)
	
	countW = state.switchMapW.get(evt.displayName)
    newCountW = countW + 1
    state.switchMapW.put(evt.displayName, newCountW)
	
	countM = state.switchMapM.get(evt.displayName)
    newCountM = countM + 1
    state.switchMapM.put(evt.displayName, newCountM)
	
    countY = state.switchMapY.get(evt.displayName)
    newCountY = countY + 1
    state.switchMapY.put(evt.displayName, newCountY)
	
	LOGDEBUG("To Delete - ${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
	
	state.switchMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
	state.switchMap += "${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
	
	LOGDEBUG("Adding In - ${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
}

def thermostatHandler(evt) {
	state.tStat = evt.value
	LOGDEBUG("In thermostatHandler...Current Status: ${state.tStat}")
	if(state.tStat != "idle") {
		LOGDEBUG("In thermostatHandler...Starting to count: ${state.tStat}")
		if(state.thermostatMap == null) resetThermostatMapHandler()
		if(state.thermostatMapD == null) resetThermostatMapHandler()
		if(state.thermostatMapW == null) resetThermostatMapHandler()
		if(state.thermostatMapM == null) resetThermostatMapHandler()
		if(state.thermostatMapY == null) resetThermostatMapHandler()
	
		if(state.thermostatMapD.get(evt.displayName) == null) {
			LOGDEBUG("In thermostatHandler: ${evt.displayName} not found in Map D...Adding it in.")
			state.thermostatMapD.put(evt.displayName, 0)
		}
		if(state.thermostatMapW.get(evt.displayName) == null) {
			LOGDEBUG("In thermostatHandler: ${evt.displayName} not found in Map W...Adding it in.")
			state.thermostatMapW.put(evt.displayName, 0)
		}
		if(state.thermostatMapM.get(evt.displayName) == null) {
			LOGDEBUG("In thermostatHandler: ${evt.displayName} not found in Map M...Adding it in.")
			state.thermostatMapM.put(evt.displayName, 0)
		}
		if(state.thermostatMapY.get(evt.displayName) == null) {
			LOGDEBUG("In thermostatHandler: ${evt.displayName} not found in Map Y...Adding it in.")
			state.thermostatMapY.put(evt.displayName, 0)
		}
		countD = state.thermostatMapD.get(evt.displayName)
    	newCountD = countD + 1
    	state.thermostatMapD.put(evt.displayName, newCountD)
	
		countW = state.thermostatMapW.get(evt.displayName)
   	 	newCountW = countW + 1
   	 	state.thermostatMapW.put(evt.displayName, newCountW)
	
		countM = state.thermostatMapM.get(evt.displayName)
    	newCountM = countM + 1
    	state.thermostatMapM.put(evt.displayName, newCountM)
	
   	 	countY = state.thermostatMapY.get(evt.displayName)
   	 	newCountY = countY + 1
    	state.thermostatMapY.put(evt.displayName, newCountY)
	
		LOGDEBUG("To Delete - ${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
	
		state.thermostatMap -= "${evt.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
		state.thermostatMap += "${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}<br>"
	
		LOGDEBUG("Adding In - ${evt.displayName} — Today's count: ${newCountD}, Week: ${newCountW}, Month: ${newCountM}, Year: ${newCountY}")
	} else {
		LOGDEBUG("In thermostatHandler...Nothing to do because it change to ${state.tStat}")
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
}

def resetMotionCountHandler() {
	LOGDEBUG("In resetMotionCountHandler...")
	// Resetting Daily Counter
		motionEvent.each { it -> 
			countD = state.motionMapD.get(it.displayName)
			countW = state.motionMapW.get(it.displayName)
			countM = state.motionMapM.get(it.displayName)
			countY = state.motionMapY.get(it.displayName)
			newCountD = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.motionMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.motionMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
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
			newCountW = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.motionMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}")
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
			newCountM = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.motionMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}")
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
			newCountY = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.motionMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.motionMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}")
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
			newCountD = 0
			LOGDEBUG("In resetContactCountHandler...New day, so setting countD to 0")
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.contactMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.contactMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
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
			newCountW = 0
			LOGDEBUG("In resetContactCountHandler...dayOfWeek: ${dayOfWeek} so setting countW to 0")
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.contactMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}")
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
			newCountM = 0
			LOGDEBUG("In resetContactCountHandler...dayOfMonth: ${dayOfMonth} so setting countM to 0")
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.contactMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}")
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
			newCountY = 0
			LOGDEBUG("In resetContactCountHandler...dayOfYear: ${dayOfYear} so setting countY to 0")
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.contactMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.contactMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}")
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
			newCountD = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.switchMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.switchMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
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
			newCountW = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.switchMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}")
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
			newCountM = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.switchMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}")
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
			newCountY = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.switchMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.switchMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}")
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
			newCountD = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.thermostatMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.thermostatMap += "${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${newCountD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
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
			newCountW = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.thermostatMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${newCountW}, Month: ${countM}, Year: ${countY}")
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
			newCountM = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.thermostatMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${newCountM}, Year: ${countY}")
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
			newCountY = 0
			LOGDEBUG("To Delete - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}")
   			state.thermostatMap -= "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${countY}<br>"
			state.thermostatMap += "${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}<br>"
			LOGDEBUG("Adding In - ${it.displayName} — Today's count: ${countD}, Week: ${countW}, Month: ${countM}, Year: ${newCountY}")
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

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def checkForUpdate(){
	def params = [uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Abacus%20-%20Intense%20Counting/version.json",
				   	contentType: "application/json"]
       	try {
			httpGet(params) { response ->
				def results = response.data
				def appStatus
				if(version() == results.currVersion){
					appStatus = "${version()} - No Update Available - ${results.discussion}"
				}
				else {
					appStatus = "<div style='color:#FF0000'>${version()} - Update Available (${results.currVersion})!</div><br>${results.parentRawCode}  ${results.childRawCode}  ${results.discussion}"
					log.warn "${app.label} has an update available - Please consider updating."
				}
				return appStatus
			}
		} 
        catch (e) {
        	log.error "Error:  $e"
    	}
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	section() {
		def verUpdate = "${checkForUpdate()}"
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Abacus - Intense Counting - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${verUpdate}</div>"
	}       
}  
