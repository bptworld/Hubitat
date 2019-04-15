/**
 *  ****************  Abacus - Intense Counting Child ****************
 *
 *  Design Usage:
 *  Count how many times a Device is triggered. Displays Daily, Weekly, Monthly and Yearly counts!
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
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
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
 *	V1.1.0 - 04/15/19 - Code cleanup
 *	V1.0.9 - 03/20/19 - Major rewrite to comply with Hubitat's new dashboard requirements.
 *  V1.0.8 - 02/16/19 - Big maintenance release. Reworked a lot of code as I continue to learn new things.
 *  V1.0.7 - 01/25/19 - Create a tile so counts can be used on Dashboard
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

def setVersion() {
	state.version = "v1.1.0"
}

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
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Abacus%20-%20Intense%20Counting/AIC-child.groovy",
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
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your counts on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Abacus - Counting Tile'<br> - Use our 'Abacus - Counting Tile' as the Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Abacus - Counting Tile<br>- Pick a template = attribute<br>- 3rd box = EACH attribute holds 5 lines of data. So mulitple boxes are now necessary. The options are abacusMotion1-5, abacusContact1-5, abacusSwitch1-5 or abacusThermostat1"
			}
		section() {
			input(name: "countTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Counts to:", submitOnChange: true, required: false, multiple: false)
			input("updateTime", "number", title: "How long between updates (in minutes)", required:true, defaultValue: 15)
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
		section() {
			input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	}
		display2()
	}
}

def pageCounts(params) {
	dynamicPage(name: "pageStatus", title: "<h2 style='color:#1A77C9;font-weight: bold'>Abacus - Intense Counting</h2>", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		buildMotionMaps()
		buildContactMaps()
		buildSwitchMaps()
		buildThermostatMaps()
		section(getFormat("header-green", "${getImage("Blank")}"+" Motion Sensors")) {
			if(logEnable) log.debug "In pageCounts...Motion Sensors"
			paragraph "${state.motionMap1S}${state.motionMap2S}${state.motionMap3S}${state.motionMap4S}${state.motionMap5S}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Contact Sensors")) {
			if(logEnable) log.debug "In pageCounts...Contact Sensors"
			paragraph "${state.contactMap1S}${state.contactMap2S}${state.contactMap3S}${state.contactMap4S}${state.contactMap5S}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Switch Events")) {
			if(logEnable) log.debug "In pageCounts...Switch Events"
			paragraph "${state.switchMap1S}${state.switchMap2S}${state.switchMap3S}${state.switchMap4S}${state.switchMap5S}"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Thermostat Events")) {
			if(logEnable) log.debug "In pageCounts...Thermostat Events"
			paragraph "${state.thermostatMap1S}"
		}
		section() {
			paragraph getFormat("line")
			def rightNow = new Date()
			paragraph "<div style='color:#1A77C9'>Report generated: ${rightNow}</div>"
		}
		countMapHandler()
	}
}

def installed() {
	log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	if(logEnable) log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
	if(logEnable) log.debug "In initialize..."
	setDefaults()
	subscribe(motionEvent, "motion.active", motionHandler)
	subscribe(contactEvent, "contact.open", contactHandler)
	subscribe(switchEvent, "switch.on", switchHandler)
	subscribe(thermostatEvent, "thermostatOperatingState", thermostatHandler)

	schedule("0 5 0 * * ? *", resetMotionCountHandler)
	schedule("0 6 0 * * ? *", resetContactCountHandler)
	schedule("0 7 0 * * ? *", resetSwitchCountHandler)
	schedule("0 8 0 * * ? *", resetThermostatCountHandler)
	
	if(countTileDevice) schedule("0 */${updateTime} * ? * *", countMapHandler)  	// send new Counts every XX minutes
}

def countMapHandler(evt) {
	if(logEnable) log.debug "In countMapHandler...Sending new Abacus Motion Counts to ${countTileDevice}"
	countTileDevice.sendMotionMap1(state.motionMap1S)
	countTileDevice.sendMotionMap2(state.motionMap2S)
	countTileDevice.sendMotionMap3(state.motionMap3S)
	countTileDevice.sendMotionMap4(state.motionMap4S)
	countTileDevice.sendMotionMap5(state.motionMap5S)
	
	if(logEnable) log.debug "In countMapHandler...Sending new Abacus Contact Counts to ${countTileDevice}"
	countTileDevice.sendContactMap1(state.contactMap1S)
	countTileDevice.sendContactMap2(state.contactMap2S)
	countTileDevice.sendContactMap3(state.contactMap3S)
	countTileDevice.sendContactMap4(state.contactMap4S)
	countTileDevice.sendContactMap5(state.contactMap5S)
	
	if(logEnable) log.debug "In countMapHandler...Sending new Abacus Switch Counts to ${countTileDevice}"
	countTileDevice.sendSwitchMap1(state.switchMap1S)
	countTileDevice.sendSwitchMap2(state.switchMap2S)
	countTileDevice.sendSwitchMap3(state.switchMap3S)
	countTileDevice.sendSwitchMap4(state.switchMap4S)
	countTileDevice.sendSwitchMap5(state.switchMap5S)
	
	if(logEnable) log.debug "In countMapHandler...Sending new Abacus Thermostat Counts to ${countTileDevice}"
	countTileDevice.sendThermostatMap1(state.thermostatMap1S)
}

def setupNewStuff() {
	if(logEnable) log.debug "In setupNewStuff..."
	
	// ********** Starting Motion Devices **********
	
	if(logEnable) log.debug "In setupNewStuff...Setting up Motion Maps"
	if(state.motionMapD == null) resetMotionMapHandler()
	if(state.motionMapW == null) resetMotionMapHandler()
	if(state.motionMapM == null) resetMotionMapHandler()
	if(state.motionMapY == null) resetMotionMapHandler()

	if(logEnable) log.debug "In setupNewStuff...Looking for new Motion devices"
	motionEvent.each { it -> 
		if(logEnable) log.debug "Working on... ${it.displayName}"
		if(state.motionMapD.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map D...Adding it in."
			state.motionMapD.put(it.displayName, 0)
		}
		if(state.motionMapW.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map W...Adding it in."
			state.motionMapW.put(it.displayName, 0)
		}
		if(state.motionMapM.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map M...Adding it in."
			state.motionMapM.put(it.displayName, 0)
		}
		if(state.motionMapY.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in."
			state.motionMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Motion Devices **********
	
	// ********** Starting Contact Devices **********
	
	if(logEnable) log.debug "In setupNewStuff...Setting up Contact Maps"
	if(state.contactMapD == null) resetContactMapHandler()
	if(state.contactMapW == null) resetContactMapHandler()
	if(state.contactMapM == null) resetContactMapHandler()
	if(state.contactMapY == null) resetContactMapHandler()

	if(logEnable) log.debug "In setupNewStuff...Looking for new Contact devices"
	contactEvent.each { it -> 
		if(logEnable) log.debug "Working on... ${it.displayName}"
		if(state.contactMapD.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map D...Adding it in."
			state.contactMapD.put(it.displayName, 0)
		}
		if(state.contactMapW.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map W...Adding it in."
			state.contactMapW.put(it.displayName, 0)
		}
		if(state.contactMapM.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map M...Adding it in."
			state.contactMapM.put(it.displayName, 0)
		}
		if(state.contactMapY.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in."
			state.contactMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Contact Devices **********
	
	// ********** Starting Switch Devices **********
	
	if(logEnable) log.debug "In setupNewStuff...Setting up Switch Maps"
	if(state.switchMapD == null) resetSwitchMapHandler()
	if(state.switchMapW == null) resetSwitchMapHandler()
	if(state.switchMapM == null) resetSwitchMapHandler()
	if(state.switchMapY == null) resetSwitchMapHandler()

	if(logEnable) log.debug "In setupNewStuff...Looking for new Switch devices"
	switchEvent.each { it -> 
		if(logEnable) log.debug "Working on... ${it.displayName}"
		if(state.switchMapD.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map D...Adding it in."
			state.switchMapD.put(it.displayName, 0)
		}
		if(state.switchMapW.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map W...Adding it in."
			state.switchMapW.put(it.displayName, 0)
		}
		if(state.switchMapM.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map M...Adding it in."
			state.switchMapM.put(it.displayName, 0)
		}
		if(state.switchMapY.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in."
			state.switchMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Switch Devices **********
	
	// ********** Starting Thermostat Devices **********
	
	if(logEnable) log.debug "In setupNewStuff...Setting up Thermostat Maps"
	if(state.thermostatMapD == null) resetThermostatMapHandler()
	if(state.thermostatMapW == null) resetThermostatMapHandler()
	if(state.thermostatMapM == null) resetThermostatMapHandler()
	if(state.thermostatMapY == null) resetThermostatMapHandler()

	if(logEnable) log.debug "In setupNewStuff...Looking for new Thermostat devices"
	thermostatEvent.each { it -> 
		if(logEnable) log.debug "Working on... ${it.displayName}"
		if(state.thermostatMapD.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map D...Adding it in."
			state.thermostatMapD.put(it.displayName, 0)
		}
		if(state.thermostatMapW.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map W...Adding it in."
			state.thermostatMapW.put(it.displayName, 0)
		}
		if(state.thermostatMapM.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map M...Adding it in."
			state.thermostatMapM.put(it.displayName, 0)
		}
		if(state.thermostatMapY.get(it.displayName) == null) {
			if(logEnable) log.debug "In setupNewStuff: ${it.displayName} not found in Map Y...Adding it in."
			state.thermostatMapY.put(it.displayName, 0)
		}
	}
	
	// ********** Ending Thermostat Devices **********
}

def motionHandler(evt) {
	if(logEnable) log.debug "In motionHandler: Device: $evt.displayName is $evt.value"
	try {
		motionEvent.each { it -> 
			if(evt.displayName == it.displayName) {
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
			}
		}
	} 	
	catch (e) {}
	buildMotionMaps()
}

def contactHandler(evt) {
	if(logEnable) log.debug "In contactHandler: $evt.displayName: $evt.value"
	state.contactMap = ""
	try {
		contactEvent.each { it -> 
			if(evt.displayName == it.displayName) {
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
			}
		}
	} 	
	catch (e) {}
}

def switchHandler(evt) {
	if(logEnable) log.debug "In switchHandler: $evt.displayName: $evt.value"
	state.switchMap = ""
	try {
		switchEvent.each { it -> 
			if(evt.displayName == it.displayName) {
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
			}
		}
	} 	
	catch (e) {}		
}

def thermostatHandler(evt) {
	state.tStat = evt.value
	if(logEnable) log.debug "In thermostatHandler...Current Status: ${state.tStat}"
	if(state.tStat != "idle") {
		if(logEnable) log.debug "In thermostatHandler...Starting to count: ${state.tStat}"
		state.thermostatMap = ""
		try {
			thermostatEvent.each { it -> 
				if(evt.displayName == it.displayName) {
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
				}
			}
		} 	
		catch (e) {}
	} else {
		if(logEnable) log.debug "In thermostatHandler...Nothing to do because it change to ${state.tStat}"
	}
}

def resetMotionMapHandler() {
	if(logEnable) log.debug "In resetMotionMapHandler..."
	if(state.motionMapD == null) {
		if(logEnable) log.debug "In resetMotionMapHandler...Reseting motionMapD"
    	state.motionMapD = [:]
		motionEvent.each { it -> state.motionMapD.put(it.displayName, 0)}
	}
	if(state.motionMapW == null) {
		if(logEnable) log.debug "In resetMotionMapHandler...Reseting motionMapW"
    	state.motionMapW = [:]
		motionEvent.each { it -> state.motionMapW.put(it.displayName, 0)}
	}
	if(state.motionMapM == null) {
		if(logEnable) log.debug "In resetMotionMapHandler...Reseting motionMapM"
    	state.motionMapM = [:]
		motionEvent.each { it -> state.motionMapM.put(it.displayName, 0)}
	}
	if(state.motionMapY == null) {
		if(logEnable) log.debug "In resetMotionMapHandler...Reseting motionMapY"
    	state.motionMapY = [:]
		motionEvent.each { it -> state.motionMapY.put(it.displayName, 0)}
	}
}

def resetContactMapHandler() {
	if(logEnable) log.debug "In resetContactMapHandler..."
	if(state.contactMapD == null) {
		if(logEnable) log.debug "In resetContactMapHandler...Reseting contactMapD"
    	state.contactMapD = [:]
		contactEvent.each { it -> state.contactMapD.put(it.displayName, 0)}
	}
	if(state.contactMapW == null) {
		if(logEnable) log.debug "In resetContactMapHandler...Reseting contactMapW"
    	state.contactMapW = [:]
		contactEvent.each { it -> state.contactMapW.put(it.displayName, 0)}
	}
	if(state.contactMapM == null) {
		if(logEnable) log.debug "In resetContactMapHandler...Reseting contactMapM"
    	state.contactMapM = [:]
		contactEvent.each { it -> state.contactMapM.put(it.displayName, 0)}
	}
	if(state.contactMapY == null) {
		if(logEnable) log.debug "In resetContactMapHandler...Reseting contactMapY"
    	state.contactMapY = [:]
		contactEvent.each { it -> state.contactMapY.put(it.displayName, 0)}
	}
}

def resetSwitchMapHandler() {
	if(logEnable) log.debug "In resetSwitchMapHandler..."
	if(state.switchMapD == null) {
		if(logEnable) log.debug "In resetSwitchMapHandler...Reseting switchMapD"
    	state.switchMapD = [:]
		switchEvent.each { it -> state.switchMapD.put(it.displayName, 0)}
	}
	if(state.switchMapW == null) {
		if(logEnable) log.debug "In resetSwitchMapHandler...Reseting switchMapW"
    	state.switchMapW = [:]
		switchEvent.each { it -> state.switchMapW.put(it.displayName, 0)}
	}
	if(state.switchMapM == null) {
		if(logEnable) log.debug "In resetSwitchMapHandler...Reseting switchMapM"
    	state.switchMapM = [:]
		switchEvent.each { it -> state.switchMapM.put(it.displayName, 0)}
	}
	if(state.switchMapY == null) {
		if(logEnable) log.debug "In resetSwitchMapHandler...Reseting switchMapY"
    	state.switchMapY = [:]
		switchEvent.each { it -> state.switchMapY.put(it.displayName, 0)}
	}
}

def resetThermostatMapHandler() {
	if(logEnable) log.debug "In resetThermostatMapHandler..."
	if(state.thermostatMapD == null) {
		if(logEnable) log.debug "In resetThermostatMapHandler...Reseting thermostatMapD"
    	state.thermostatMapD = [:]
		thermostatEvent.each { it -> state.thermostatMapD.put(it.displayName, 0)}
	}
	if(state.thermostatMapW == null) {
		if(logEnable) log.debug "In resetThermostatMapHandler...Reseting thermostatMapW"
    	state.thermostatMapW = [:]
		thermostatEvent.each { it -> state.thermostatMapW.put(it.displayName, 0)}
	}
	if(state.thermostatMapM == null) {
		if(logEnable) log.debug "In resetThermostatMapHandler...Reseting thermostatMapM"
    	state.thermostatMapM = [:]
		thermostatEvent.each { it -> state.thermostatMapM.put(it.displayName, 0)}
	}
	if(state.thermostatMapY == null) {
		if(logEnable) log.debug "In resetThermostatMapHandler...Reseting thermostatMapY"
    	state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
}

def resetMotionCountHandler() {
	if(logEnable) log.debug "In resetMotionCountHandler..."
	// Resetting Daily Counter
		motionEvent.each { it -> state.motionMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		motionEvent.each { it -> state.motionMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		motionEvent.each { it -> state.motionMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		motionEvent.each { it -> state.motionMapY.put(it.displayName, 0)}
	}
}

def resetContactCountHandler() {
	if(logEnable) log.debug "In resetContactCountHandler..."
	// Resetting Daily Counter
    	state.contactMapD = [:]
		contactEvent.each { it -> state.contactMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(logEnable) log.debug "In resetContactCountHandler...dayOfWeek: ${dayOfWeek}"
	if(dayOfWeek == 1) {
		state.contactMapW = [:]
		contactEvent.each { it -> state.contactMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.contactMapM = [:]
		contactEvent.each { it -> state.contactMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.contactMapY = [:]
		contactEvent.each { it -> state.contactMapY.put(it.displayName, 0)}
	}
}

def resetSwitchCountHandler() {
	if(logEnable) log.debug "In resetSwitchCountHandler..."
	// Resetting Daily Counter
    	state.switchMapD = [:]
		switchEvent.each { it -> state.switchMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.switchMapW = [:]
		switchEvent.each { it -> state.switchMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.switchMapM = [:]
		switchEvent.each { it -> state.switchMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.switchMapY = [:]
		switchEvent.each { it -> state.switchMapY.put(it.displayName, 0)}
	}
}

def resetThermostatCountHandler() {
	if(logEnable) log.debug "In resetThermostatCountHandler..."
	// Resetting Daily Counter
    	state.thermostatMapD = [:]
		thermostatEvent.each { it -> state.thermostatMapD.put(it.displayName, 0)}
	// Resetting Weekly Counter
	def date1 = new Date()
	def dayOfWeek = date1.getAt(Calendar.DAY_OF_WEEK)
	if(dayOfWeek == 1) {
		state.thermostatMapW = [:]
		thermostatEvent.each { it -> state.thermostatMapW.put(it.displayName, 0)}
	}
	// Resetting Monthly Counter
	def date2 = new Date()
	def dayOfMonth = date2.getAt(Calendar.DAY_OF_MONTH)
	if(dayOfMonth == 1) {
		state.thermostatMapM = [:]
		thermostatEvent.each { it -> state.thermostatMapM.put(it.displayName, 0)}
	}
	// Resetting Yearly Counter
	def date3 = new Date()
	def dayOfYear = date3.getAt(Calendar.DAY_OF_YEAR)
	if(dayOfYear == 1) {
		state.thermostatMapY = [:]
		thermostatEvent.each { it -> state.thermostatMapY.put(it.displayName, 0)}
	}
}

def sendMessage(msg) {
	if(logEnable) log.debug "${msg}"
    if (pushNotification) {
        sendPush(msg)
    }
}

def buildMotionMaps() {
	if(logEnable) log.debug "In buildMotionMaps - Map: ${motionEvent}"
	state.count = 0
	if(motionEvent) {
		state.motionEventS = motionEvent.sort{it.name}
	} else {
		state.motionEventS = ""
	}
	if(logEnable) log.debug "In buildMotionMaps - Sorted Map: ${state.motionEventS}"
	state.motionMap1S = "<table width='100%'>"
	state.motionMap2S = "<table width='100%'>"
	state.motionMap3S = "<table width='100%'>"
	state.motionMap4S = "<table width='100%'>"
	state.motionMap5S = "<table width='100%'>"
	
	state.motionEventS.each { it ->
		state.count = state.count + 1
		countD = state.motionMapD.get(it.displayName)
		countW = state.motionMapW.get(it.displayName)
		countM = state.motionMapM.get(it.displayName)
		countY = state.motionMapY.get(it.displayName)
		if(logEnable) log.debug "Adding - ${it.displayName} — Today: ${countD} Week: ${countW} Month: ${countM} Year: ${countY}"
		if(state.count == 1) {
			state.motionMap1S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.motionMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 2) && (state.count <= 5)) state.motionMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 6) {
			state.motionMap2S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.motionMap2S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 7) && (state.count <= 10)) state.motionMap2S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 11) {
			state.motionMap3S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.motionMap3S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 12) && (state.count <= 15)) state.motionMap3S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>{countY}</td></tr>"
		if(state.count == 16) {
			state.motionMap4S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.motionMap4S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 17) && (state.count <= 20)) state.motionMap4S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 21) {
			state.motionMap5S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.motionMap5S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 22) && (state.count <= 25)) state.motionMap5S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
	}
	state.motionMap1S += "</table>"
	state.motionMap2S += "</table>"
	state.motionMap3S += "</table>"
	state.motionMap4S += "</table>"
	state.motionMap5S += "</table>"
}

def buildContactMaps() {
	if(logEnable) log.debug "In buildContactMaps - Map: ${contactEvent}"
	state.count = 0
	if(contactEvent) {
		state.contactEventS = contactEvent.sort{it.name}
	} else {
		state.contactEventS = ""
	}
	if(logEnable) log.debug "In buildContactMaps - Sorted Map: ${state.contactEventS}"
	state.contactMap1S = "<table width='100%'>"
	state.contactMap2S = "<table width='100%'>"
	state.contactMap3S = "<table width='100%'>"
	state.contactMap4S = "<table width='100%'>"
	state.contactMap5S = "<table width='100%'>"
	
	state.contactEventS.each { it ->
		state.count = state.count + 1
		countD = state.contactMapD.get(it.displayName)
		countW = state.contactMapW.get(it.displayName)
		countM = state.contactMapM.get(it.displayName)
		countY = state.contactMapY.get(it.displayName)
		if(logEnable) log.debug "Adding - ${it.displayName} — Today: ${countD} Week: ${countW} Month: ${countM} Year: ${countY}"
		if(state.count == 1) {
			state.contactMap1S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.contactMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 2) && (state.count <= 5)) state.contactMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 6) {
			state.contactMap2S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.contactMap2S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 7) && (state.count <= 10)) state.contactMap2S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 11) {
			state.contactMap3S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.contactMap3S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 12) && (state.count <= 15)) state.contactMap3S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 16) {
			state.contactMap4S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.contactMap4S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 17) && (state.count <= 20)) state.contactMap4S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 21) {
			state.contactMap5S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.contactMap5S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 22) && (state.count <= 25)) state.contactMap5S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
	}
	state.contactMap1S += "</table>"
	state.contactMap2S += "</table>"
	state.contactMap3S += "</table>"
	state.contactMap4S += "</table>"
	state.contactMap5S += "</table>"
}

def buildSwitchMaps() {
	if(logEnable) log.debug "In buildSwitchMaps - Map: ${switchEvent}"
	state.count = 0
	if(switchEvent) {
		state.switchEventS = switchEvent.sort{it.name}
	} else {
		state.switchEventS = ""
	}
	if(logEnable) log.debug "In buildSwitchMaps - Sorted Map: ${state.switchEventS}"
	state.switchMap1S = "<table width='100%'>"
	state.switchMap2S = "<table width='100%'>"
	state.switchMap3S = "<table width='100%'>"
	state.switchMap4S = "<table width='100%'>"
	state.switchMap5S = "<table width='100%'>"
	
	state.switchEventS.each { it ->
		state.count = state.count + 1
		countD = state.switchMapD.get(it.displayName)
		countW = state.switchMapW.get(it.displayName)
		countM = state.switchMapM.get(it.displayName)
		countY = state.switchMapY.get(it.displayName)
		if(logEnable) log.debug "Adding - ${it.displayName} — Today: ${countD} Week: ${countW} Month: ${countM} Year: ${countY}"
		if(state.count == 1) {
			state.switchMap1S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.switchMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 2) && (state.count <= 5)) state.switchMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 6) {
			state.switchMap2S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.switchMap2S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 7) && (state.count <= 10)) state.switchMap2S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 11) {
			state.switchMap3S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.switchMap3S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 12) && (state.count <= 15)) state.switchMap3S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 16) {
			state.switchMap4S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.switchMap4S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 17) && (state.count <= 20)) state.switchMap4S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		if(state.count == 21) {
			state.switchMap5S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.switchMap5S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 22) && (state.count <= 25)) state.switchMap5S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
	}
	state.switchMap1S += "</table>"
	state.switchMap2S += "</table>"
	state.switchMap3S += "</table>"
	state.switchMap4S += "</table>"
	state.switchMap5S += "</table>"
}

def buildThermostatMaps() {
	if(logEnable) log.debug "In buildThermostatMaps - Map: ${thermostatEvent}"
	state.count = 0
	if(thermostatEvent) {
		state.thermostatEventS = thermostatEvent.sort{it.name}
	} else {
		state.thermostatEventS = ""
	}
	if(logEnable) log.debug "In buildThermostatMaps - Sorted Map: ${state.thermostatEventS}"
	state.thermostatMap1S = "<table width='100%'>"
	
	state.thermostatEventS.each { it ->
		state.count = state.count + 1
		countD = state.thermostatMapD.get(it.displayName)
		countW = state.thermostatMapW.get(it.displayName)
		countM = state.thermostatMapM.get(it.displayName)
		countY = state.thermostatMapY.get(it.displayName)
		if(logEnable) log.debug "Adding - ${it.displayName} — Today: ${countD} Week: ${countW} Month: ${countM} Year: ${countY}"
		if(state.count == 1) {
			state.thermostatMap1S += "<tr><td><b>Name</b></td><td><b>Today</b></td><td><b>Week</b></td><td><b>Month</b></td><td><b>Year</b></td></tr>"
			state.thermostatMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
		}
		if((state.count >= 2) && (state.count <= 5)) state.thermostatMap1S += "<tr><td>${it.displayName}</td><td>${countD}</td><td>${countW}</td><td>${countM}</td><td>${countY}</td></tr>"
	}
	state.thermostatMap1S += "</table>"
}

// ********** Normal Stuff **********

def setDefaults(){
	setupNewStuff()
	if(pauseApp == null){pauseApp = false}
	if(logEnable == null){logEnable = false}
}

def getImage(type) {							// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){					// Modified from @Stephack
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pauseApp", "bool", title: "Pause App", required: true, submitOnChange: true, defaultValue: false
		if(pauseApp) {paragraph "<font color='red'>App is Paused</font>"}
		if(!pauseApp) {paragraph "App is not Paused"}
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Abacus - Intense Counting - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
} 
