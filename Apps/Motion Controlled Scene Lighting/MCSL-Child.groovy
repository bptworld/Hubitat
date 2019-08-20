/**
 *  ****************  Motion Controlled Scene Lighting Child App  ****************
 *
 *  Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'.
 *  Hubitat has all these great built-in apps. This app brings a few of them together!
 *		- Uses 'Modes', 'Groups and Scenes' and 'Zone Motion Controller'
 *		- Motion is the key to making everything happen. User can select individual motion sensors
 *		  or choose a Zone Motion Controller device
 *		- Turn lights on/off based on Mode, utilizing Hubitat Scenes
 *		- Optionally: Can also use light level as a condition
 *		- Select up to 8 different Modes/Scene combinations per child app
 *		- Each child app has a Lights Dim Warning when X amount of time has passed without motion
 *		- If still no motion after Dim Warning, turn off all lights in Scene after X amount of time
 *		- When motion is activated, lights are reset to the appropriate Scene
 *		- Each child app also has a Safety Net which turns off any user selected lights after X
 *		  amount of time has passed. Can be any lights, in Scene or not. (ie. all lights in room)
 *		- Ability to pause any child app
 *		- Ability to Enable/Disable child app via a switch
 *		- Parent/Child App structure
 *		- Create as many child apps as needed
 *		- Displays the current Mode next to the Parent App Name for easy reference
 *
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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.5 - 04/15/19 - Code cleanup
 *  V1.0.4 - 02/13/19 - Bug fixes
 *  V1.0.3 - 01/15/19 - Updated footer with update check and links
 *  V1.0.2 - 01/10/19 - Fixed Enabler/Disable switch. It wasn't working.
 *  V1.0.1 - 12/30/18 - Updated to new color theme.
 *  V1.0.0 - 12/19/18 - Initial release.
 *
 */

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "MotionControlledSceneLightingChildVersion"
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
    name: "Motion Controlled Scene Lighting Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'.",
    category: "",
	parent: "BPTWorld:Motion Controlled Scene Lighting",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Motion%20Controlled%20Scene%20Lighting/MCSL-Child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Motion Controlled Scene Lighting</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
        	paragraph "<b>Info:</b>"
    		paragraph "Automate your lights based on Motion and Current Mode, utilizing Hubitat 'Scenes'."
			paragraph "<b>Prerequisites:</b>"
			paragraph "- Must already have at least one Scene setup in Hubitats 'Groups and Scenes' built in app.<br>- Have at least one dimmable buld included in each Scene."
		}
    	section(getFormat("header-green", "${getImage("Blank")}"+" Setup")) {
			input "motionSensors", "capability.motionSensor", title: "Select Motion Sensor(s):", required: true, multiple:true
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Mode 1")) {
			input "modeName1", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
			input "sceneSwitch1", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
			input "lightsToDim1", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
			input "dimTime1", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        	input "offTime1", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
		}
		section() {input(name: "mode2Enable", type: "bool", defaultValue: "false", submitOnChange: true, title: "Need another Mode?", description: "Enable another mode set.")}
		if(mode2Enable) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 2")) {
				input "modeName2", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch2", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim2", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime2", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime2", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
			section() {input(name: "mode3Enable", type: "bool", defaultValue: "false", submitOnChange: true, title: "Need another Mode?", description: "Enable another mode set.")}
		}
		if(mode3Enable) {	
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 3")) {	
				input "modeName3", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch3", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim3", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime3", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime3", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
			section() {input(name: "mode4Enable", type: "bool", defaultValue: "false", submitOnChange: true, title: "Need another Mode?", description: "Enable another mode set.")}
		}
		if(mode4Enable) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 4")) {	
				input "modeName4", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch4", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim4", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime4", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime4", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
			section() {input(name: "mode5Enable", type: "bool", defaultValue: "false", submitOnChange: true, title: "Need another Mode?", description: "Enable another mode set.")}
		}
		if(mode5Enable) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 5")) {	
				input "modeName5", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch5", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim5", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime5", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime5", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
			section() {input(name: "mode6Enable", type: "bool", defaultValue: "false", submitOnChange: true, title: "Need another Mode?", description: "Enable another mode set.")}
		}
		if(mode6Enable) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 6")) {	
				input "modeName6", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch6", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim6", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime6", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime6", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
		}
		if(mode7Enable) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 7")) {	
				input "modeName7", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch7", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim7", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime7", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime7", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
		}
		if(mode8Enable) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Mode 8")) {	
				input "modeName8", "mode", title: "When in this Mode...", required: true, multiple: false, width:6
				input "sceneSwitch8", "capability.switch", title: "...turn on this Switch to Activate Scene.", required: true, multiple: false, width:6
				input "lightsToDim8", "capability.switch", title: "Select light(s) to dim when no motion (will dim to 50% of whatever level they are currently)", required: true, multiple: true
				input "dimTime8", "number", title: "Time since Motion inactivity, before lights selected dim (in minutes)", required: true, width:6
        		input "offTime8", "number", title: "Time since lights Dimmed, before all lights in Scene turn off (in seconds)", required: true, width:6
			}
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Restrictions (optional)")) {
        	input "lightSensor", "capability.illuminanceMeasurement", title: "Only when illuminance on this light sensor...", required: false, width:6
            input "lightLevel", "number", title: "...is equal to or below this illuminance level", required: false, width:6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Safety Net (optional - used to turn off all lights selected, not just the ones in the Scene.)")) {
			input "safetySwitches", "capability.switch", title: "Turn off these switches when...", required: false, multiple: true, width:6
			input "safetyTime", "number", title: "...Time since Motion inactivity (in minutes)", required: false, width:6
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
		section() {
			input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    	}
		display2()
	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	initialize()
}

def initialize() {
    setDefaults()
	subscribe(location, "mode", modeHandler)
    subscribe(motionSensors, "motion", motionHandler)
}

def modeHandler(evt){
		allInactive()
		if(logEnable) log.debug "In modeHandler...Before if's - Thinks it's Mode: ${state.modeNow} - Actual mode: ${location.mode}"
		state.prevMode = state.modeNow
		state.modeNow = location.mode
		if(state.modeNow.contains(modeName1)){
			state.currentMode = "1"
			state.dimTime = dimTime1
			state.offTime = offTime1
			if(logEnable) log.debug "In modeHandler 1...Match Found - modeName1: ${modeName1} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode} "
			log.info "Setting Mode 1 - ${modeName1}"
			if(state.allInactive == "false") {
				if(state.prevMode != state.modeNow) { setScene() }
			}
		}
		if(modeName2 != null) {
			if(state.modeNow.contains(modeName2)){
				state.currentMode = "2"
				state.dimTime = dimTime2
				state.offTime = offTime2
				if(logEnable) log.debug "In modeHandler 2...Match Found - modeName2: ${modeName2} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode}"
				log.info "Setting Mode 2 - ${modeName2}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
		if(modeName3 != null) {
			if(state.modeNow.contains(modeName3)){
				state.currentMode = "3"
				state.dimTime = dimTime3
				state.offTime = offTime3
				if(logEnable) log.debug "In modeHandler 3...Match Found - modeName3: ${modeName3} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode}"
				log.info "Setting Mode 3 - ${modeName3}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
		if(modeName4 != null) {
			if(state.modeNow.contains(modeName4)){
				state.currentMode = "4"
				state.dimTime = dimTime4
				state.offTime = offTime4
				if(logEnable) log.debug "In modeHandler 4...Match Found - modeName4: ${modeName4} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode}"
				log.info "Setting Mode 4 - ${modeName4}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
		if(modeName5 != null) {
			if(state.modeNow.contains(modeName5)){
				state.currentMode = "5"
				state.dimTime = dimTime5
				state.offTime = offTime5
				if(logEnable) log.debug "In modeHandler 5...Match Found - modeName5: ${modeName5} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode} "
				log.info "Setting Mode 5 - ${modeName5}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
		if(modeName6 != null) {
			if(state.modeNow.contains(modeName6)){
				state.currentMode = "6"
				state.dimTime = dimTime6
				state.offTime = offTime6
				if(logEnable) log.debug "In modeHandler 6...Match Found - modeName6: ${modeName6} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode} "
				log.info "Setting Mode 6 - ${modeName6}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
		if(modeName7 != null) {
			if(state.modeNow.contains(modeName7)){
				state.currentMode = "7"
				state.dimTime = dimTime7
				state.offTime = offTime7
				if(logEnable) log.debug "In modeHandler 7...Match Found - modeName7: ${modeName7} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode} "
				log.info "Setting Mode 7 - ${modeName7}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
		if(modeName8 != null) {
			if(state.modeNow.contains(modeName8)){
				state.currentMode = "8"
				state.dimTime = dimTime8
				state.offTime = offTime8
				if(logEnable) log.debug "In modeHandler 8...Match Found - modeName8: ${modeName8} modeNow: ${state.modeNow}-So state.currentMode: ${state.currentMode} "
				log.info "Setting Mode 8 - ${modeName8}"
				if(state.allInactive == "false") {
					if(state.prevMode != state.modeNow) { setScene() }
				}
			}
		}
}

def allInactive(){
	if(logEnable) log.debug "In allInactive..."
    state.allInactive = "true"
	motionSensors.each {eachMotion->
        if(eachMotion.currentValue("motion") == "active"){
            state.allInactive = "false"
			if(logEnable) log.debug "In allInactive...${eachMotion} - allInactive: ${state.allInactive}"
		} else {
			if(logEnable) log.debug "In allInactive...${eachMotion} - allInactive: ${state.allInactive}"
		}
	}
}

def motionHandler(evt) {
	if(logEnable) log.debug "In motionHandler..."
	if(pauseApp == true){log.warn "Unable to continue - App paused"}
    if(pauseApp == false){if(logEnable) log.debug "Continue - App NOT paused"
		modeHandler()
		if(logEnable) log.debug "In motionHandler...Mode: ${state.modeNow}"
    		if(evt.value == "inactive" && state.allInactive == "true") {
        		if(logEnable) log.debug "Inactive received, Starting timers"
				dimTimeSec = (state.dimTime * 60)
				if(logEnable) log.debug "Time to dimLights ${dimTimeSec}"
    			runIn(dimTimeSec, dimLights)
    		}
    		if(evt.value == "active") {
        		state.lastActive = now()
        		log.info "Motion activated, Setting Scene"
				modeHandler()
    			setScene()
    		}
	}
}


def setScene() {
	luxLevel()
	if(logEnable) log.debug "In setScene...Mode is ${state.currentMode} Mode: ${state.modeNow}"
	if(state.isItDark == "true") {
		if(state.currentMode == "1"){
			if(logEnable) log.debug "In setScene...1: currentMode: ${state.currentMode}"
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch1.on()
		} else
		if(state.currentMode == "2"){
			if(logEnable) log.debug "In setScene...2: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch2.on()
		} else
		if(state.currentMode == "3"){
			if(logEnable) log.debug "In setScene...3: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch3.on()
		} else
		if(state.currentMode == "4"){
			if(logEnable) log.debug "In setScene...4: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch4.on()
		} else
		if(state.currentMode == "5"){
			if(logEnable) log.debug "In setScene...5: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch5.on()
		} else
		if(state.currentMode == "6"){
			if(logEnable) log.debug "In setScene...6: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch6.on()
		} else
		if(state.currentMode == "7"){
			if(logEnable) log.debug "In setScene...7: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "8") sceneSwitch8.off()
			sceneSwitch7.on()
		} else
		if(state.currentMode == "8"){
			if(logEnable) log.debug "In setScene...8: currentMode: ${state.currentMode}"
			if(state.prevMode == "1") sceneSwitch1.off()
			if(state.prevMode == "2") sceneSwitch2.off()
			if(state.prevMode == "3") sceneSwitch3.off()
			if(state.prevMode == "4") sceneSwitch4.off()
			if(state.prevMode == "5") sceneSwitch5.off()
			if(state.prevMode == "6") sceneSwitch6.off()
			if(state.prevMode == "7") sceneSwitch7.off()
			sceneSwitch8.on()
		} else	
		if(state.currentMode == "NONE"){
			if(logEnable) log.debug "In setScene...Something went wrong, no Mode matched!"
		}
	} else{
		log.info "It's too light in here, no Scenes activated."
	}
}

def luxLevel() {
    if(logEnable) log.debug "In luxLevel..."
    if (lightSensor != null) {
		if(lightLevel == null) {lightLevel = 0}
        def curLev = lightSensor.currentValue("illuminance").toInteger()
        if (curLev >= lightLevel.toInteger()) {
            if(logEnable) log.debug "In luxLevel...Current Light Level: ${curLev} is greater than lightValue: ${lightLevel}"
			state.isItDark = "false"
        } else {
            if(logEnable) log.debug "In luxLevel...Current Light Level: ${curLev} is less than lightValue: ${lightLevel}"
			state.isItDark = "true"
        }
    }
}

def dimLights() {
	if(logEnable) log.debug "In dimLights...Mode: ${state.modeNow}"
	dimTimeSec = (state.dimTime * 60)
	safetyTimeSec = (safetyTime * 60)
	if(logEnable) log.debug "In dimLights...dimTimeSec: ${dimTimeSec}"
    def delta = (now() - (state.lastActive ?:0))/1000
    if(delta < dimTimeSec) {
        if(logEnable) log.debug "In dimLights...Time Since Last Active = ${delta} less than time to Dim = ${dimTimeSec}"
		setScene()
    } else {
		if(logEnable) log.debug "In dimLights...Time Since Last Active = ${delta} greater than time to Dim = ${dimTimeSec}"
		if(state.currentMode == "1") {lightsToDim = lightsToDim1}
		if(state.currentMode == "2") {lightsToDim = lightsToDim2}
		if(state.currentMode == "3") {lightsToDim = lightsToDim3}
		if(state.currentMode == "4") {lightsToDim = lightsToDim4}
		if(state.currentMode == "5") {lightsToDim = lightsToDim5}
		if(state.currentMode == "6") {lightsToDim = lightsToDim6}
		if(state.currentMode == "7") {lightsToDim = lightsToDim7}
		if(state.currentMode == "8") {lightsToDim = lightsToDim8}
		if(state.currentMode != "NONE") {
        	lightsToDim.each {eachLight->
				startLevel = eachLight.currentLevel.toInteger()
				setDimLevel1 = (startLevel * 0.5)
				setDimLevel = (startLevel - setDimLevel1)
				if(logEnable) log.debug "In dimLights...eachlight: ${eachLight}, startLevel: ${startLevel}, dimLevel: ${setDimLevel} and is ${eachLight.currentSwitch}"
				log.info "Motion has been inactive for ${delta} seconds - Starting Dim Warning"
    			if(eachLight.currentSwitch == "on") eachLight.setLevel(setDimLevel)
				runIn(state.offTime, setOff)
				runIn(safetyTimeSec, safetyNet)
			}
		} else{
			if(logEnable) log.debug "In dimLights...Something went wrong, no Mode matched!"
		}
	}
}

def setOff() {
	if(logEnable) log.debug "In setOff...currentMode: ${state.currentMode} Mode: ${state.modeNow}"
    def delta = (now() - (state.lastActive ?:0))/1000
    if(delta < state.offTime) {
        if(logEnable) log.debug "In setOff...Cancelling setOff: Time Since Last Active: ${delta} and OffTime: ${state.offTime}"
	} else {
		if(sceneSwitch1 != null) sceneSwitch1.off()
		if(sceneSwitch2 != null) sceneSwitch2.off()
		if(sceneSwitch3 != null) sceneSwitch3.off()
		if(sceneSwitch4 != null) sceneSwitch4.off()
		if(sceneSwitch5 != null) sceneSwitch5.off()
		if(sceneSwitch6 != null) sceneSwitch6.off()
		if(sceneSwitch7 != null) sceneSwitch7.off()
		if(sceneSwitch8 != null) sceneSwitch8.off()
	}
}

def safetyNet() {
	if(logEnable) log.debug "In safetyNet...Mode: ${state.modeNow}"
	safetyTimeSec = (safetyTime * 60)
    def delta = (now() - (state.lastActive ?:0))/1000
    if(delta < safetyTimeSec) {
        if(logEnable) log.debug "In safetyNet...Cancelling safetyNet: Time Since Last Active= ${delta} and Off Window = ${safetyTimeSec}"
	} else {
		log.info "Motion Safety Net - turning all selected lights off"
		safetySwitches.off()
	}
}

// ********** Normal Stuff **********

def setDefaults(){
    if(pauseApp == null){pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.currentMode == null){state.currentMode = "NONE"}
}

def getImage(type) {									// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){							// Modified from @Stephack Code
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Motion Controlled Scene Lighting - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}  
