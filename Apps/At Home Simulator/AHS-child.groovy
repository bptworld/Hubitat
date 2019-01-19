/**
 *  ****************  At Home Simulator Child  ****************
 *
 *  Design Usage:
 *	Turn lights on and off to simulate the appearance of an occupied home using YOUR normal routine.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
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
 *  V0.0.9 - 01/19/19 - More testing, more bug fixing.
 *  V0.0.8 - 01/19/19 - More bug fixes and better logging.
 *  V0.0.7 - 01/18/19 - Made some code changes and fixed some of the wording. Hopefully it makes more sense.
 *  V0.0.6 - 01/18/19 - Finally got the control switch to turn off the simulation.
 *  V0.0.5 - 01/16/19 - Startup now has a random delay. Pause button and Enable/Disable switch should now work.
 *  V0.0.4 - 01/16/19 - Changed the delay between groups to be a random time within a user selected range.
 *  V0.0.3 - 01/15/19 - Updated footer with update check and links
 *  V0.0.2 - 01/14/19 - Added update information to custom footer. Used code from @Stephack as example, thank you.  
 *  V0.0.1 - 01/14/19 - Initial Beta Release
 *
 */

def version(){"v0.0.9"}

definition(
    name: "At Home Simulator Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Turn lights on and off to simulate the appearance of an occupied home using YOUR normal routine.",
    category: "",
    
parent: "BPTWorld:At Home Simulator",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    )

preferences {
    page name: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>At Home Simulator</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {		display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>What does this do?</b>"
			paragraph "This app is designed to give a lived in look based on <b>your</b> daily routine. This is not a random lights generator. Think about your normal daily routine and then program the lights to duplicate how you would normally move about the house. So if you get up at 6:00am but the sun doesn't come up until 8:00am, be sure to create a simulator that lasts at least 2 hours."
			paragraph "<b>Requirements:</b>"
			paragraph "- Rule Machine and a Virtual Switch"
			paragraph "Using the power of Rule Machine, set up a rule to turn the Virtual Switch to control this simulator on. This way you get to use every type of restriction available including offsets, without reinventing the wheel over here."
			paragraph "<b>Notes:</b>"
			paragraph "* Select as many devices from each group as needed.<br>* Child Apps are activated/deactivated by the Control Switch.<br>* When activated, group 1 will run first, then group 2, group 3, group 4 and group 5.<br>* Set how long each group of lights stays on<br>* Each group can have a different pause between devices AND a different pause between groups.<br>* Best to create overlaps in lighting from room to room, using multiple groups"	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Control Switch")) {
			input "controlSwitch", "capability.switch", title: "Select the switch to control the Lighting Simulation (on/off)", required: true, multiple: false
			input "tRunTime", "number", title: "Max run time. This will stop the app after a certain amount of time, even if it didn't finish the entire simulator (in minutes)", required: true, defaultValue: 360
			paragraph "Extra Time to pause before Group 1 starts. This is a random delay based on the two numbers you select below. Makes it so the lights don't turn on at exactly the same time each time the simulation starts."
			input "pFromS", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			input "pToS", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		} 
		section(getFormat("header-green", "${getImage("Blank")}"+" Lighting Groups")) {}
		section("<b>Group 1</b>", hideable: true, hidden: true) {
			input "g1Switches", "capability.switch", title: "Switches to control", required: true, multiple: true, submitOnChange: true
			if(g1Switches) input "g1TimeToStayOn", "number", title: "How long should each light stay On (in minutes)", required: true, defaultValue: 5
			if(g1Switches) input "timeToPause1", "number", title: "Time to pause between devices turning On within group 1 (in seconds)", required: true, defaultValue: 1
			if(g1Switches) paragraph "Extra Time to pause between Group 1 and Group 2. This is a random delay based on the two numbers you select below."
			if(g1Switches) input "pFrom1", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(g1Switches) input "pTo1", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
		section("<b>Group 2</b>", hideable: true, hidden: true) {
			input "g2Switches", "capability.switch", title: "Switches to control", required: false, multiple: true, submitOnChange: true
			if(g2Switches) input "g2TimeToStayOn", "number", title: "How long should lights stay On (in minutes)", required: true, defaultValue: 5
			if(g2Switches) input "timeToPause2", "number", title: "Time to pause between devices turning On within group 2 (in seconds)", required: true, defaultValue: 1
			if(g2Switches) paragraph "Extra Time to pause between Group 2 and Group 3. This is a random delay based on the two numbers you select below."
			if(g2Switches) input "pFrom2", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(g2Switches) input "pTo2", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
		section("<b>Group 3</b>", hideable: true, hidden: true) {
			input "g3Switches", "capability.switch", title: "Switches to control", required: false, multiple: true, submitOnChange: true
			if(g3Switches) input "g3TimeToStayOn", "number", title: "How long should lights stay On (in minutes)", required: true, defaultValue: 5
			if(g3Switches) input "timeToPause3", "number", title: "Time to pause between devices turning On within group 3 (in seconds)", required: true, defaultValue: 1
			if(g3Switches) paragraph "Extra Time to pause between Group 3 and Group 4. This is a random delay based on the two numbers you select below."
			if(g3Switches) input "pFrom3", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(g3Switches) input "pTo3", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
		section("<b>Group 4</b>", hideable: true, hidden: true) {
			input "g4Switches", "capability.switch", title: "Switches to control", required: false, multiple: true, submitOnChange: true
			if(g4Switches) input "g4TimeToStayOn", "number", title: "How long should lights stay On (in minutes)", required: true, defaultValue: 5
			if(g4Switches) input "timeToPause4", "number", title: "Time to pause between devices turning On within group 4 (in seconds)", required: true, defaultValue: 1
			if(g4Switches) paragraph "Extra Time to pause between Group 4 and Group 5. This is a random delay based on the two numbers you select below."
			if(g4Switches) input "pFrom4", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(g4Switches) input "pTo4", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
		section("<b>Group 5</b>", hideable: true, hidden: true) {
			input "g5Switches", "capability.switch", title: "Switches to control", required: false, multiple: true, submitOnChange: true
			if(g5Switches) input "g5TimeToStayOn", "number", title: "How long should lights stay On (in minutes)", required: true, defaultValue: 5
			if(g5Switches) input "timeToPause5", "number", title: "Time to pause between devices turning On within group 5 (in seconds)", required: true, defaultValue: 1
			if(g5Switches) paragraph "Extra Time to pause between Group 5 and ending the Simulation. This is a random delay based on the two numbers you select below."
			if(g5Switches) input "pFrom4", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(g5Switches) input "pTo4", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Random Lights")) {}
		section("<b>Random Lights</b>", hideable: true, hidden: true) {
			paragraph "Some times you just need to break away from the daily routine. Choose a couple of lights here (not included in the above Groups) to randomly turn on and off while the Group routines continue to run."
			input "gRSwitches", "capability.switch", title: "Switches to randomly control, only one light in this group will be on at a time", required: false, multiple: true, submitOnChange: true
			if(gRSwitches) input "rTimeToStayOn", "number", title: "How long should each light stay On (in minutes)", required: true, defaultValue: 5
			if(gRSwitches) input "timeToPauseR", "number", title: "Time to pause between devices turning On (in minutes)", required: true, defaultValue: 10
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false, submitOnChange: true}
		
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
	unschedule()
    unsubscribe()
	initialize()
}

def initialize() {
	setDefaults()
	logCheck()
	
	subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	subscribe(controlSwitch, "switch", deviceHandler)
	
	int tRT = (tRunTime * 60)			// Minutes
	runIn(tRT, deviceOffHandler)
}

def deviceHandler(evt) {
	LOGDEBUG("In DeviceHandler...")
	state.controllerSwitch = evt.value
	if(state.controllerSwitch == "on") {
		atomicState.cSwitch = 1
		LOGDEBUG("In deviceHandler...CS JUST TURNED ON.")
	}
	
	if(state.controllerSwitch == "off") {
		atomicState.cSwitch = 0
		LOGDEBUG("In deviceHandler...CS JUST TURNED OFF.")
	}
	
	LOGDEBUG("In deviceHandler... cs: ${atomicState.cSwitch}")
	if(state.enablerSwitch2 == "off") {
		if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    	if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
			if(atomicState.cSwitch == 1) {
				if(gRSwitches) randomSwitchesHandler()
				
				def delaySb = Math.abs(new Random().nextInt() % ([pToS] - [pFromS])) + [pFromS]
				LOGDEBUG("In deviceOnHandler S...Delay: ${pFromS} to ${pToS} = ${delaySb} till next Group - cs: ${atomicState.cSwitch} **********")
				int delaySc = (delaySb * 60) * 1000			// Minutes
				log.info "Starting - Waiting Random Pause: ${delaySb} minutes"
				if(atomicState.cSwitch == 1) pauseExecution(delaySc)
				
				LOGDEBUG("In between S and 1 ... cs: ${atomicState.cSwitch}   *   *   *")
				
				if(atomicState.cSwitch == 1 && g1Switches) {
					int delay1 = (timeToPause1 * 1000)			// Seconds
					int g1TTSO = (g1TimeToStayOn * 60)			// Minutes
					totalDevices = g1Switches.size()
					numOfDevices = 0
					log.info "Group 1 - Total Devices in Group: ${totalDevices}"
					if(totalDevices == 1) {
						g1Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								LOGDEBUG("In deviceOnHandler 1...turning on ${device}, Time to Stay On: ${g1TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 1 - Turning on ${device}"
								device.on()
							}
    					}
					}
					if(totalDevices >= 2) {
   						g1Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								numOfDevices = numOfDevices + 1
								LOGDEBUG("In deviceOnHandler 1...turning on ${device}, Time to Stay On: ${g1TimeToStayOn} - cs: ${atomicState.cSwitch}")
								log.info "Group 1 - Turning on ${device}"
        						device.on()
								if(numOfDevices < totalDevices) log.info "Group 1 - Waiting Pause between devices: ${timeToPause1} seconds"
								if(numOfDevices < totalDevices) pauseExecution(delay1)
							}
						}
    				}
					runIn(g1TTSO, g1SwitchesOff)
					def delay1b = Math.abs(new Random().nextInt() % ([pTo1] - [pFrom1])) + [pFrom1]
					LOGDEBUG("In deviceOnHandler 1...Delay: ${pFrom1} to ${pTo1} = ${delay1b} till next Group **********")
					int delay1c = (delay1b * 60) * 1000			// Minutes
					if(atomicState.cSwitch == 1 && g2Switches) log.info "Group 1 - Waiting Random Pause: ${delay1b} minutes before heading to Group 2"
					if(atomicState.cSwitch == 1 && g2Switches) pauseExecution(delay1c)
				}
				
				LOGDEBUG("In between 1 and 2 ... cs: ${atomicState.cSwitch}   *   *   *")
				
				if(atomicState.cSwitch == 1 && g2Switches) {
					int delay2 = (timeToPause2 * 1000) 			// Seconds
					int g2TTSO = (g2TimeToStayOn * 60)			// Minutes
					totalDevices = g2Switches.size()
					numOfDevices = 0
					log.info "Group 2 - Total Devices in Group: ${totalDevices}"
					if(totalDevices == 1) {
						g2Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								LOGDEBUG("In deviceOnHandler 2...turning on ${device}, Time to Stay On: ${g2TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 2 - Turning on ${device}"
								device.on()
							}
    					}
					}
					if(totalDevices >= 2) {
   						g2Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								numOfDevices = numOfDevices + 1
								LOGDEBUG("In deviceOnHandler 2...turning on ${device}, Time to Stay On: ${g2TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 2 - Turning on ${device}"
								device.on()
								if(numOfDevices < totalDevices) log.info "Group 2 - Waiting Pause between devices: ${timeToPause2} seconds"
								if(numOfDevices < totalDevices) pauseExecution(delay2)
							}
    					}
					}
					runIn(g2TTSO, g2SwitchesOff)
					def delay2b = Math.abs(new Random().nextInt() % ([pTo2] - [pFrom2])) + [pFrom2]
					LOGDEBUG("In deviceOnHandler 2...Delay: ${pFrom2} to ${pTo2} = ${delay2b} till next Group **********")
					int delay2c = (delay2b * 60) * 1000			// Minutes
					if(atomicState.cSwitch == 1 && g3Switches) log.info "Group 2 - Waiting Random Pause: ${delay2b} minutes before heading to Group 3"
					if(atomicState.cSwitch == 1 && g3Switches) pauseExecution(delay2c)
				}
		
				if(atomicState.cSwitch == 1 && g3Switches) {
					int delay3 = timeToPause3 * 1000 			// Seconds
					int g3TTSO = (g3TimeToStayOn * 60)			// Minutes
					totalDevices = g3Switches.size()
					numOfDevices = 0
					log.info "Group 3 - Total Devices in Group: ${totalDevices}"
					if(totalDevices == 1) {
						g3Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								LOGDEBUG("In deviceOnHandler 3...turning on ${device}, Time to Stay On: ${g3TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 3 - Turning on ${device}"
								device.on()
							}
    					}
					}
					if(totalDevices >= 2) {
   						g3Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								numOfDevices = numOfDevices + 1
								LOGDEBUG("In deviceOnHandler 3...turning on ${device}, Time to Stay On: ${g3TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 3 - Turning on ${device}"
								device.on()
								if(numOfDevices < totalDevices) log.info "Group 3 - Waiting Pause between devices: ${timeToPause3} seconds"
								if(numOfDevices < totalDevices) pauseExecution(delay3)
							}
    					}
					}
					runIn(g3TTSO, g3SwitchesOff)
					def delay3b = Math.abs(new Random().nextInt() % ([pTo3] - [pFrom3])) + [pFrom3]
					LOGDEBUG("In deviceOnHandler 3...Delay: ${pFrom3} to ${pTo3} = ${delay3b} till next Group **********")
					int delay3c = (delay3b * 60) * 1000			// Minutes
					if(atomicState.cSwitch == 1 && g4Switches)log.info "Group 3 - Waiting Random Pause: ${delay3b} minutes before heading to Group 4"
					if(atomicState.cSwitch == 1 && g4Switches) pauseExecution(delay3c)
				}
				
				if(atomicState.cSwitch == 1 && g4Switches) {
					int delay4 = timeToPause4 * 1000 			// Seconds
					int g4TTSO = (g4TimeToStayOn * 60)			// Minutes
					totalDevices = g4Switches.size()
					numOfDevices = 0
					log.info "Group 4 - Total Devices in Group: ${totalDevices}"
					if(totalDevices == 1) {
						g4Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								LOGDEBUG("In deviceOnHandler 4...turning on ${device}, Time to Stay On: ${g4TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 4 - Turning on ${device}"
								device.on()
							}
    					}
					}
					if(totalDevices >= 2) {
   						g4Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								numOfDevices = numOfDevices + 1
								LOGDEBUG("In deviceOnHandler 4...turning on ${device}, Time to Stay On: ${g4TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 4 - Turning on ${device}"
								device.on()
								if(numOfDevices < totalDevices) log.info "Group 4 - Waiting Pause between devices: ${timeToPause4} seconds"
								if(numOfDevices < totalDevices) pauseExecution(delay4)
							}
						}
    				}
					runIn(g4TTSO, g4SwitchesOff)
					def delay4b = Math.abs(new Random().nextInt() % ([pTo4] - [pFrom4])) + [pFrom4]
					LOGDEBUG("In deviceOnHandler 4...Delay: ${pFrom4} to ${pTo4} = ${delay4b} till next Group **********")
					int delay4c = (delay4b * 60) * 1000			// Minutes
					if(atomicState.cSwitch == 1 && g5Switches)log.info "Group 4 - Waiting Random Pause: ${delay4b} minutes before heading to Group 5"
					if(atomicState.cSwitch == 1 && g5Switches) pauseExecution(delay4c)			
				}
					
				if(atomicState.cSwitch == 1 && g5Switches) {
					int delay5 = timeToPause5 * 1000 			// Seconds
					int g5TTSO = (g5TimeToStayOn * 60)			// Minutes
					totalDevices = g5Switches.size()
					numOfDevices = 0
					log.info "Group 5 - Total Devices in Group: ${totalDevices}"
					if(totalDevices == 1) {
						g5Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								LOGDEBUG("In deviceOnHandler 5...turning on ${device}, Time to Stay On: ${g5TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 5 - Turning on ${device}"
								device.on()
							}
    					}
					}
					if(totalDevices >= 2) {
   						g5Switches.each { device ->
							if(atomicState.cSwitch == 1) {
								numOfDevices = numOfDevices + 1
								LOGDEBUG("In deviceOnHandler 5...turning on ${device}, Time to Stay On: ${g5TimeToStayOn} - cs: ${atomicState.cSwitch}")
        						log.info "Group 5 - Turning on ${device}"
								device.on()
								if(numOfDevices < totalDevices) log.info "Group 5 - Waiting Pause between devices: ${timeToPause5} seconds"
								if(numOfDevices < totalDevices) pauseExecution(delay5)	
							}
						}
					}
					runIn(g5TTSO, g5SwitchesOff)
					def delay5b = Math.abs(new Random().nextInt() % ([pTo5] - [pFrom5])) + [pFrom5]
					LOGDEBUG("In deviceOnHandler 5...Delay: ${pFrom5} to ${pTo5} = ${delay5b} till Simulation Finished **********")
					int delay5c = (delay5b * 60) * 1000			// Minutes
					if(atomicState.cSwitch == 1) pauseExecution(delay5c)
				}
			} else {
				deviceOffHandler()
			}
		}
	} else {
		LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	}
}

def randomSwitchesHandler() {
	if(atomicState.cSwitch == 1) {
		LOGDEBUG("In randomSwitchesHandler...timeToPauseR: ${timeToPauseR}, rTimeToStayOn: ${rTimeToStayOn} - cs: ${atomicState.cSwitch}")
		int delayR = (timeToPauseR * 60)		 	// Minutes
		int rTTSO = (rTimeToStayOn * 60)			// Minutes
	
		def randomS = gRSwitches.size();
		def randomKey1 = Math.abs(new Random().nextInt() % randomS)
		rSwitch = gRSwitches[randomKey1]
	
		LOGDEBUG("In randomSwitchesHandler...turning on ${rSwitch}, Time to Stay On: ${rTimeToStayOn} ----------")
		log.info "Random - Turning on Random switch: ${rSwitch}"
    	if(atomicState.cSwitch == 1) rSwitch.on()
		runIn(rTTSO, gRSwitchesOff)
		
		log.info "Random - Waiting Pause: ${timeToPauseR}"
    	if(atomicState.cSwitch == 1) runIn(delayR, randomSwitchesHandler)
	}
}

def g1SwitchesOff() { 
	int delay1 = timeToPause1 * 1000 		// Seconds
   	g1Switches.each { device ->
		LOGDEBUG("In g1SwitchesOff 1...turning off ${device}")
		log.info "offGroup 1 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay1)
    }
}

def g2SwitchesOff() { 
	int delay2 = timeToPause2 * 1000 		// Seconds
   	g2Switches.each { device ->
		LOGDEBUG("In g2witchesOff 2...turning off ${device}")
		log.info "offGroup 2 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay2)
    }
}

def g3SwitchesOff() { 
	int delay3 = timeToPause3 * 1000 		// Seconds
   	g3Switches.each { device ->
		LOGDEBUG("In g3witchesOff 3...turning off ${device}")
		log.info "offGroup 3 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay3)
    }
}

def g4SwitchesOff() { 
	int delay4 = timeToPause4 * 1000 		// Seconds
   	g4Switches.each { device ->
		LOGDEBUG("In g4witchesOff 4...turning off ${device}")
		log.info "offGroup 4 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay4)
    }
}

def g5SwitchesOff() { 
	int delay5 = timeToPause5 * 1000 		// Seconds
   	g5Switches.each { device ->
		LOGDEBUG("In g5witchesOff 5...turning off ${device}")
		log.info "offGroup 5 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay5)
    }
}

def gRSwitchesOff() { 
	int delayR = timeToPauseR * 1000 		// Seconds
   	gRSwitches.each { device ->
		LOGDEBUG("In gRwitchesOff R...turning off ${device}")
		log.info "offGroup R - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delayR)
    }
}

def deviceOffHandler() {
	atomicState.cSwitch = 0
	LOGDEBUG("In deviceOffHandler... cs: ${atomicState.cSwitch}")
	if(g1Switches) { 
   		g1Switches.each { device ->
			LOGDEBUG("In deviceOffHandler 1...turning all lights off ${device}")
			log.info "End of Sim - Group 1 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g2Switches) { 
   		g2Switches.each { device ->
			LOGDEBUG("In deviceOffHandler 2...turning all lights off ${device}")
			log.info "End of Sim - Group 2 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g3Switches) { 
   		g3Switches.each { device ->
			LOGDEBUG("In deviceOffHandler 3...turning all lights off ${device}")
			log.info "End of Sim - Group 3 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g4Switches) { 
   		g4Switches.each { device ->
			LOGDEBUG("In deviceOffHandler 4...turning all lights off ${device}")
			log.info "End of Sim - Group 4 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g5Switches) { 
   		g5Switches.each { device ->
			LOGDEBUG("In deviceOffHandler 5...turning all lights off ${device}")
			log.info "End of Sim - Group 5 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(gRSwitches) { 
   		gRSwitches.each { device ->
			LOGDEBUG("In deviceOffHandler R...turning all lights off ${device}")
			log.info "End of Sim - Group R - Turning off device: ${device}"
        	device.off()
    	}
	}
	unschedule()
	controlSwitch.off()
	LOGDEBUG("In deviceOffHandler... cs: ${atomicState.cSwitch}")
	LOGDEBUG("Finishing up...")
}

// ***** Normal Stuff *****

def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	LOGDEBUG("IN enablerSwitchHandler - Enabler Switch = ${enablerSwitch2}")
	LOGDEBUG("Enabler Switch = $state.enablerSwitch2")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("Enabler Switch is OFF - Child app is active.")
    }
}

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

def setDefaults(){
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
	if(atomicState.cSwitch == null){atomicState.cSwitch = 1}
	if(numOfDevices == null){numOfDevices = 0}
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

def checkForUpdate(){
	def params = [uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/At%20Home%20Simulator/version.json",
				   	contentType: "application/json"]
       	try {
			httpGet(params) { response ->
				def results = response.data
				def appStatus
				if(version() == results.currChildVersion){
					appStatus = "${version()} - No Update Available - ${results.discussion}"
				}
				else {
					appStatus = "<div style='color:#FF0000'>${version()} - Update Available (${results.currChildVersion})!</div><br>${results.parentRawCode}  ${results.childRawCode}  ${results.discussion}"
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

def display2() {
	section() {
		def verUpdate = "${checkForUpdate()}"
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>At Home Simulator - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>${verUpdate}</div>"
	}
}
