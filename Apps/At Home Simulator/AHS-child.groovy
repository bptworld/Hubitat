/**
 *  ****************  At Home Simulator Child  ****************
 *
 *  Design Usage:
 *	Turn lights on and off to simulate the appearance of an occupied home using YOUR normal routine.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
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
 *  2.0.4 - 09/02/20 - Cosmetic changes
 *  2.0.3 - 07/09/20 - Fixed Disable Switch
 *  2.0.2 - 06/25/20 - Added App Control options
 *  2.0.1 - 04/27/20 - Cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  --
 *  1.0.0 - 01/14/19 - Initial Beta Release
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "At Home Simulator"
	state.version = "2.0.4"
}

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
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/At%20Home%20Simulator/AHS-child.groovy",
)

preferences {
    page name: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", nextPage: null, install: true, uninstall: true, refreshInterval:0) {
        display()
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
			input "g1Switches", "capability.switch", title: "Switches to control", required: false, multiple: true, submitOnChange: true
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
			if(g5Switches) input "pFrom5", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(g5Switches) input "pTo5", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Random Lights")) {}
		section("<b>Random Lights</b>", hideable: true, hidden: true) {
			paragraph "Some times you just need to break away from the daily routine. Choose a couple of lights here (not included in the above Groups) to randomly turn on and off while the Group routines continue to run."
			input "gRSwitches", "capability.switch", title: "Switches to randomly control.", required: false, multiple: true, submitOnChange: true
			if(gRSwitches) paragraph "How long should each light stay On. This is a random delay based on the two numbers you select below."
			if(gRSwitches) input "tFromR", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(gRSwitches) input "tToR", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
			
			if(gRSwitches) paragraph "Extra Time to pause between lights. This is a random delay based on the two numbers you select below."
			if(gRSwitches) input "pFromR", "number", title: "<b>*</b> From...", required: true, defaultValue: 5, width: 6
			if(gRSwitches) input "pToR", "number", title: "<b>*</b> ...To (in minutes)", required: true, defaultValue: 10, width: 6
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this automation", required: false, submitOnChange: true
            input "logEnable", "bool", defaultValue:true, title: "Enable Debug Logging", description: "Logging"
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        subscribe(controlSwitch, "switch", deviceHandler)
        int tRT = (tRunTime * 60)			// Minutes
        runIn(tRT, deviceOffHandler)
    }
}

def deviceHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In DeviceHandler..."
        state.controllerSwitch = evt.value
        if(state.controllerSwitch == "on") {
            atomicState.cSwitch = 1
            if(logEnable) log.debug "In deviceHandler...CS JUST TURNED ON."
        }

        if(state.controllerSwitch == "off") {
            atomicState.cSwitch = 0
            if(logEnable) log.debug "In deviceHandler...CS JUST TURNED OFF."
        }

        if(logEnable) log.debug "In deviceHandler... cs: ${atomicState.cSwitch}"

        if(atomicState.cSwitch == 1) {
            def delaySb = Math.abs(new Random().nextInt() % ([pToS] - [pFromS])) + [pFromS]
            if(logEnable) log.debug "In deviceOnHandler S...Delay: ${pFromS} to ${pToS} = ${delaySb} till next Group - cs: ${atomicState.cSwitch} **********"
            int delaySc = (delaySb * 60) * 1000			// Minutes
            log.info "Starting - Waiting Random Pause: ${delaySb} minutes"
            if(atomicState.cSwitch == 1) pauseExecution(delaySc)
            if(gRSwitches) randomSwitchesHandler()

            if(logEnable) log.debug "In between S and 1 ... cs: ${atomicState.cSwitch}   *   *   *"

            if(atomicState.cSwitch == 1 && g1Switches) {
                int delay1 = (timeToPause1 * 1000)			// Seconds
                int g1TTSO = (g1TimeToStayOn * 60)			// Minutes
                totalDevices = g1Switches.size()
                numOfDevices = 0
                log.info "Group 1 - Total Devices in Group: ${totalDevices}"
                if(totalDevices == 1) {
                    g1Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            if(logEnable) log.debug "In deviceOnHandler 1...turning on ${device}, Time to Stay On: ${g1TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 1 - Turning on ${device}"
                            device.on()
                        }
                    }
                }
                if(totalDevices >= 2) {
                    g1Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            numOfDevices = numOfDevices + 1
                            if(logEnable) log.debug "In deviceOnHandler 1...turning on ${device}, Time to Stay On: ${g1TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 1 - Turning on ${device}"
                            device.on()
                            if(numOfDevices < totalDevices) log.info "Group 1 - Waiting Pause between devices: ${timeToPause1} seconds"
                            if(numOfDevices < totalDevices) pauseExecution(delay1)
                        }
                    }
                }
                runIn(g1TTSO, g1SwitchesOff)
                def delay1b = Math.abs(new Random().nextInt() % ([pTo1] - [pFrom1])) + [pFrom1]
                if(logEnable) log.debug "In deviceOnHandler 1...Delay: ${pFrom1} to ${pTo1} = ${delay1b} till next Group **********"
                int delay1c = (delay1b * 60) * 1000			// Minutes
                if(atomicState.cSwitch == 1 && g2Switches) log.info "Group 1 - Waiting Random Pause: ${delay1b} minutes before heading to Group 2"
                if(atomicState.cSwitch == 1 && g2Switches) pauseExecution(delay1c)
            }

            if(logEnable) log.debug "In between 1 and 2 ... cs: ${atomicState.cSwitch}   *   *   *"

            if(atomicState.cSwitch == 1 && g2Switches) {
                int delay2 = (timeToPause2 * 1000) 			// Seconds
                int g2TTSO = (g2TimeToStayOn * 60)			// Minutes
                totalDevices = g2Switches.size()
                numOfDevices = 0
                log.info "Group 2 - Total Devices in Group: ${totalDevices}"
                if(totalDevices == 1) {
                    g2Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            if(logEnable) log.debug "In deviceOnHandler 2...turning on ${device}, Time to Stay On: ${g2TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 2 - Turning on ${device}"
                            device.on()
                        }
                    }
                }
                if(totalDevices >= 2) {
                    g2Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            numOfDevices = numOfDevices + 1
                            if(logEnable) log.debug "In deviceOnHandler 2...turning on ${device}, Time to Stay On: ${g2TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 2 - Turning on ${device}"
                            device.on()
                            if(numOfDevices < totalDevices) log.info "Group 2 - Waiting Pause between devices: ${timeToPause2} seconds"
                            if(numOfDevices < totalDevices) pauseExecution(delay2)
                        }
                    }
                }
                runIn(g2TTSO, g2SwitchesOff)
                def delay2b = Math.abs(new Random().nextInt() % ([pTo2] - [pFrom2])) + [pFrom2]
                if(logEnable) log.debug "In deviceOnHandler 2...Delay: ${pFrom2} to ${pTo2} = ${delay2b} till next Group **********"
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
                            if(logEnable) log.debug "In deviceOnHandler 3...turning on ${device}, Time to Stay On: ${g3TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 3 - Turning on ${device}"
                            device.on()
                        }
                    }
                }
                if(totalDevices >= 2) {
                    g3Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            numOfDevices = numOfDevices + 1
                            if(logEnable) log.debug "In deviceOnHandler 3...turning on ${device}, Time to Stay On: ${g3TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 3 - Turning on ${device}"
                            device.on()
                            if(numOfDevices < totalDevices) log.info "Group 3 - Waiting Pause between devices: ${timeToPause3} seconds"
                            if(numOfDevices < totalDevices) pauseExecution(delay3)
                        }
                    }
                }
                runIn(g3TTSO, g3SwitchesOff)
                def delay3b = Math.abs(new Random().nextInt() % ([pTo3] - [pFrom3])) + [pFrom3]
                if(logEnable) log.debug "In deviceOnHandler 3...Delay: ${pFrom3} to ${pTo3} = ${delay3b} till next Group **********"
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
                            if(logEnable) log.debug "In deviceOnHandler 4...turning on ${device}, Time to Stay On: ${g4TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 4 - Turning on ${device}"
                            device.on()
                        }
                    }
                }
                if(totalDevices >= 2) {
                    g4Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            numOfDevices = numOfDevices + 1
                            if(logEnable) log.debug "In deviceOnHandler 4...turning on ${device}, Time to Stay On: ${g4TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 4 - Turning on ${device}"
                            device.on()
                            if(numOfDevices < totalDevices) log.info "Group 4 - Waiting Pause between devices: ${timeToPause4} seconds"
                            if(numOfDevices < totalDevices) pauseExecution(delay4)
                        }
                    }
                }
                runIn(g4TTSO, g4SwitchesOff)
                def delay4b = Math.abs(new Random().nextInt() % ([pTo4] - [pFrom4])) + [pFrom4]
                if(logEnable) log.debug "In deviceOnHandler 4...Delay: ${pFrom4} to ${pTo4} = ${delay4b} till next Group **********"
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
                            if(logEnable) log.debug "In deviceOnHandler 5...turning on ${device}, Time to Stay On: ${g5TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 5 - Turning on ${device}"
                            device.on()
                        }
                    }
                }
                if(totalDevices >= 2) {
                    g5Switches.each { device ->
                        if(atomicState.cSwitch == 1) {
                            numOfDevices = numOfDevices + 1
                            if(logEnable) log.debug "In deviceOnHandler 5...turning on ${device}, Time to Stay On: ${g5TimeToStayOn} - cs: ${atomicState.cSwitch}"
                            log.info "Group 5 - Turning on ${device}"
                            device.on()
                            if(numOfDevices < totalDevices) log.info "Group 5 - Waiting Pause between devices: ${timeToPause5} seconds"
                            if(numOfDevices < totalDevices) pauseExecution(delay5)	
                        }
                    }
                }
                runIn(g5TTSO, g5SwitchesOff)
                def delay5b = Math.abs(new Random().nextInt() % ([pTo5] - [pFrom5])) + [pFrom5]
                if(logEnable) log.debug "In deviceOnHandler 5...Delay: ${pFrom5} to ${pTo5} = ${delay5b} till Simulation Finished **********"
                int delay5c = (delay5b * 60) * 1000			// Minutes
                if(atomicState.cSwitch == 1) pauseExecution(delay5c)
            }
        } else {
            deviceOffHandler()
        }
    }
}

def randomSwitchesHandler() {
	if(atomicState.cSwitch == 1) {
		if(logEnable) log.debug "In randomSwitchesHandler...cs: ${atomicState.cSwitch}"
		
		int rTTSOa = Math.abs(new Random().nextInt() % ([tToR] - [tFromR])) + [tFromR]
		int rTTSO = (rTTSOa * 60)			// Minutes
		
		def delayRb = Math.abs(new Random().nextInt() % ([pToR] - [pFromR])) + [pFromR]
		int delayRc = (delayRb * 60)		// Minutes
		
		def randomS = gRSwitches.size();
		def randomKey1 = Math.abs(new Random().nextInt() % randomS)
		rSwitch = gRSwitches[randomKey1]
	
		if(logEnable) log.debug "In randomSwitchesHandler...Delay: ${pFromR} to ${pToR} = ${delayRb}"
				 
		if(logEnable) log.debug "In randomSwitchesHandler...turning on ${rSwitch}, Time to Stay On: ${rTTSOa} ----------"
		log.info "Random - Turning on Random switch: ${rSwitch}, Time to Stay On: ${rTTSOa} minutes"
    	if(atomicState.cSwitch == 1) rSwitch.on()
		if(atomicState.cSwitch == 1) runIn(rTTSO, gRSwitchesOff)
		
		if(atomicState.cSwitch == 1) log.info "Random - Waiting Random Pause: ${delayRb} minutes before next light"
		if(atomicState.cSwitch == 1) runIn(delayRc, randomSwitchesHandler)
	}
}

def g1SwitchesOff() { 
	int delay1 = timeToPause1 * 1000 		// Seconds
   	g1Switches.each { device ->
		if(logEnable) log.debug "In g1SwitchesOff 1...turning off ${device}"
		log.info "offGroup 1 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay1)
    }
}

def g2SwitchesOff() { 
	int delay2 = timeToPause2 * 1000 		// Seconds
   	g2Switches.each { device ->
		if(logEnable) log.debug "In g2witchesOff 2...turning off ${device}"
		log.info "offGroup 2 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay2)
    }
}

def g3SwitchesOff() { 
	int delay3 = timeToPause3 * 1000 		// Seconds
   	g3Switches.each { device ->
		if(logEnable) log.debug "In g3witchesOff 3...turning off ${device}"
		log.info "offGroup 3 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay3)
    }
}

def g4SwitchesOff() { 
	int delay4 = timeToPause4 * 1000 		// Seconds
   	g4Switches.each { device ->
		if(logEnable) log.debug "In g4witchesOff 4...turning off ${device}"
		log.info "offGroup 4 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay4)
    }
}

def g5SwitchesOff() { 
	int delay5 = timeToPause5 * 1000 		// Seconds
   	g5Switches.each { device ->
		if(logEnable) log.debug "In g5witchesOff 5...turning off ${device}"
		log.info "offGroup 5 - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delay5)
    }
}

def gRSwitchesOff() { 
	int rTTSOa = Math.abs(new Random().nextInt() % ([tToR] - [tFromR])) + [tFromR]
	int delayR = (rTTSOa * 60) * 1000			// Minutes
	
   	gRSwitches.each { device ->
		if(logEnable) log.debug "In gRwitchesOff R...turning off ${device}"
		log.info "offGroup R - Turning Off ${device}"
        device.off()
		if(atomicState.cSwitch == 1) pauseExecution(delayR)
    }
}

def deviceOffHandler() {
	atomicState.cSwitch = 0
	if(logEnable) log.debug "In deviceOffHandler... cs: ${atomicState.cSwitch}"
	if(g1Switches) { 
   		g1Switches.each { device ->
			if(logEnable) log.debug "In deviceOffHandler 1...turning all lights off ${device}"
			log.info "End of Sim - Group 1 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g2Switches) { 
   		g2Switches.each { device ->
			if(logEnable) log.debug "In deviceOffHandler 2...turning all lights off ${device}"
			log.info "End of Sim - Group 2 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g3Switches) { 
   		g3Switches.each { device ->
			if(logEnable) log.debug "In deviceOffHandler 3...turning all lights off ${device}"
			log.info "End of Sim - Group 3 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g4Switches) { 
   		g4Switches.each { device ->
			if(logEnable) log.debug "In deviceOffHandler 4...turning all lights off ${device}"
			log.info "End of Sim - Group 4 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(g5Switches) { 
   		g5Switches.each { device ->
			if(logEnable) log.debug "In deviceOffHandler 5...turning all lights off ${device}"
			log.info "End of Sim - Group 5 - Turning off device: ${device}"
        	device.off()
    	}
	}
	if(gRSwitches) { 
   		gRSwitches.each { device ->
			if(logEnable) log.debug "In deviceOffHandler R...turning all lights off ${device}"
			log.info "End of Sim - Group R - Turning off device: ${device}"
        	device.off()
    	}
	}
	unschedule()
	controlSwitch.off()
	if(logEnable) log.debug "In deviceOffHandler... cs: ${atomicState.cSwitch}"
	if(logEnable) log.debug "Finishing up..."
}

// ***** Normal Stuff *****

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(atomicState.cSwitch == null){atomicState.cSwitch = 1}
	if(numOfDevices == null){numOfDevices = 0}
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
        paragraph "${state.headerMessage}"
		paragraph getFormat("line")
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        paragraph "${state.footerMessage}"
	}       
}

def getHeaderAndFooter() {
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
        //if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
        def params = [
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json",
            requestContentType: "application/json",
            contentType: "application/json",
            timeout: 30
        ]

        try {
            def result = null
            httpGet(params) { resp ->
                state.headerMessage = resp.data.headerMessage
                state.footerMessage = resp.data.footerMessage
            }
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
