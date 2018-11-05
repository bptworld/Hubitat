/**
 *  ****************  BI Control Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to work locally with Blue Iris security software.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  Also thanks to (@jpark40) for the original 'Blue Iris Profiles based on Modes' code that I based this app off of.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V1.0.2 - 11/05/18 - Added in the ability to move a camera to a Preset. Also added the ability to take a camera snapshot and
 *						to start or stop manual recording on camera from a Switch.
 *  V1.0.1 - 11/03/18 - Changed into Parent/Child app. BI Control now works with Modes and Switches to change Profiles.
 *  V1.0.0 - 11/03/18 - Hubitat Port of ST app 'Blue Iris Profiles based on Modes' - 2016 (@jpark40)
 *
 */

definition(
	name: "BI Control Child",
	namespace: "BPTWorld",
	author: "Bryan Turcotte",
	description: "This app is designed to work locally with Blue Iris security software.",
	category: "Convenience",
	iconUrl: "",
	iconX2Url: "",
	
	parent: "BPTWorld:BI Control",
)

preferences {
	display()
	section("Instructions:", hideable: true, hidden: true) {
		paragraph "<b>Notes:</b>"
		paragraph "BI Control keeps everything local, no Internet required!"
		paragraph "This app uses 'Virtual Switches', instead of buttons. That way the devices can be used within Google Assistant and Rule Machine. Be sure to set 'Enable auto off' within each Virtual Device to '1s' (except for recording device)."
        paragraph "<b>Blue Iris requirements:</b>"
		paragraph "In Blue Iris settings > Web Server > Advanced > Advanced Settings<br> - Ensure 'Use secure session keys and login page' is not checked.<br> - Disable authentication, select “Non-LAN only” (preferred) or “No” to disable authentication altogether.<br> - Blue Iris only allows Admin Users to toggle profiles."	
	}
	
	section() {
		input "triggerType", "enum", title: "Select Control Type", submitOnChange: true, options: ["Profile","Camera"], required: true, Multiple: false
		
		if(triggerType == "Profile"){
    		input "triggerMode", "enum", title: "Select Trigger Type", submitOnChange: true, options: ["Mode","Switch"], required: true, Multiple: false
			if(triggerMode == "Mode"){
				section(){
					paragraph "<b>Ability to change BI Profile based on HE Mode</b>"
				}
				section(""){
					input "biProfile1", "mode", title: "Profile 1 Mode(s)", required: false, multiple: true, width:3
					input "biProfile2", "mode", title: "Profile 2 Mode(s)", required: false, multiple: true, width:3
					input "biProfile3", "mode", title: "Profile 3 Mode(s)", required: false, multiple: true, width:3 
					input "biProfile4", "mode", title: "Profile 4 Mode(s)", required: false, multiple: true, width:3
					input "biProfile5", "mode", title: "Profile 5 Mode(s)", required: false, multiple: true, width:3
					input "biProfile6", "mode", title: "Profile 6 Mode(s)", required: false, multiple: true, width:3
					input "biProfile7", "mode", title: "Profile 7 Mode(s)", required: false, multiple: true, width:3
				}
			}
			if(triggerMode == "Switch"){
				section(){
					paragraph "<b>Ability to change the BI Profile from a Switch.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger Mode change", required: true, multiple: false
					input "switchProfileOn", "enum", title: "Profile to change to when switch is On", options: [
							[Pon1:"Profile 1"],
							[Pon2:"Profile 2"],
							[Pon3:"Profile 3"],
							[Pon4:"Profile 4"],
							[Pon5:"Profile 5"],
							[Pon6:"Profile 6"],
							[Pon7:"Profile 7"],
						], required: true, multiple: false
				}
			}
		}
		if(triggerType == "Camera"){
			input "triggerMode", "enum", title: "Select Trigger Type", submitOnChange: true, options: ["Camera_Preset","Camera_Snapshot","Camera_Trigger"], required: true, Multiple: false
			if(triggerMode == "Camera_Preset"){
				section(){
					paragraph "<b>Ability to move a camera to a Preset from a Switch.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger Camera Preset", required: true, multiple: false
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
					input "biCameraPreset", "enum", title: "Preset number", options: [
							[PS1:"Preset 1"],
							[PS2:"Preset 2"],
							[PS3:"Preset 3"],
							[PS4:"Preset 4"],
							[PS5:"Preset 5"],
						], required: true, multiple: false
				}
			}
			if(triggerMode == "Camera_Snapshot"){
				paragraph "<b>Ability to get a Camera Snapshot from a Switch.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger Camera Snapshot", required: true, multiple: false
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
				}
			}
			if(triggerMode == "Camera_Trigger"){
				paragraph "<b>Ability to start or stop manual recording on camera from a Switch.</b><br>This ability uses both the On and Off so no need to set 'Enable auto off'."
				section(){
					input "switches", "capability.switch", title: "Select switch to Trigger Camera", required: true, multiple: false
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
				}
			}
		}
	}
	section() {
        input "debugMode", "bool", title: "Enable Debug Logging", required: true, defaultValue: false
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
    unsubscribe()
	unschedule()
	initialize()
}

def initialize() {
	LOGDEBUG("Initializing...")
	LOGDEBUG("triggerMode = ${triggerMode}")
	if(triggerMode == "Mode"){subscribe(location, "mode", modeChangeHandler)}
	if(triggerMode == "Switch"){subscribe(switches, "switch", switchHandler)}
	if(triggerMode == "Camera_Preset"){subscribe(switches, "switch", cameraPresetHandler)}
	if(triggerMode == "Camera_Snapshot"){subscribe(switches, "switch", cameraSnapshotHandler)}
	if(triggerMode == "Camera_Trigger"){subscribe(switches, "switch", cameraTriggerHandler)}
}

def modeChangeHandler(evt) {
	LOGDEBUG("BI Control-modeChangeHandler...")
	LOGDEBUG("Mode changed to ${evt.value}")

	if(biProfile1 != null && evt.value in biProfile1) {
    	def setProfile = "1"
		biChangeProfile(setProfile)
		LOGDEBUG("biProfile1 ${settings.biProfile1}")
    } 
    else if(biProfile2 != null && evt.value in biProfile2) {
		def setProfile = "2"
        biChangeProfile(setProfile)
		LOGDEBUG("biProfile2 ${settings.biProfile2}")
	} 
    else if(biProfile3 != null && evt.value in biProfile3) {
    	def setProfile = "3"
		biChangeProfile(setProfile)
		LOGDEBUG("biProfile3 ${settings.biProfile3}")
    }
	else if(biProfile4 != null && evt.value in biProfile4) {
    	def setProfile = "4"
		biChangeProfile(setProfile)
		LOGDEBUG("biProfile4 ${settings.biProfile4}")
    }
	else if(biProfile5 != null && evt.value in biProfile5) {
    	def setProfile = "5"
		biChangeProfile(setProfile)
		LOGDEBUG("biProfile5 ${settings.biProfile5}")
    }
	else if(biProfile6 != null && evt.value in biProfile6) {
    	def setProfile = "6"
		biChangeProfile(setProfile)
		LOGDEBUG("biProfile6 ${settings.biProfile6}")
    }
	else if(biProfile7 != null && evt.value in biProfile7) {
    	def setProfile = "7"
		biChangeProfile(setProfile)
		LOGDEBUG("biProfile7 ${settings.biProfile7}")
    }
}

def switchHandler(evt) {
	LOGDEBUG("BI Control-switchChangeHandler...")
	LOGDEBUG("Switch on/off - $evt.device : $evt.value")

	if(switches.currentValue("switch") == "on") {
		LOGDEBUG("switchChangeHandler - switchProfileOn = ${switchProfileOn}")
		if(switchProfileOn == "Pon1") {
			def setProfile = "1"
			biChangeProfile(setProfile)
		} else
		if(switchProfileOn == "Pon2") {
			def setProfile = "2"
			biChangeProfile(setProfile)
		} else
		if(switchProfileOn == "Pon3") {
			def setProfile = "3"
			biChangeProfile(setProfile)
		} else
		if(switchProfileOn == "Pon4") {
			def setProfile = "4"
			biChangeProfile(setProfile)
		} else
		if(switchProfileOn == "Pon5") {
			def setProfile = "5"
			biChangeProfile(setProfile)
		} else
		if(switchProfileOn == "Pon6") {
			def setProfile = "6"
			biChangeProfile(setProfile)
		} else
		if(switchProfileOn == "Pon7") {
			def setProfile = "7"
			biChangeProfile(setProfile)
		}
	}
}

def cameraPresetHandler(evt) {
	LOGDEBUG("BI Control-cameraPresetHandler...")
	LOGDEBUG("Switch on/off - $evt.device : $evt.value")

	if(switches.currentValue("switch") == "on") {
		LOGDEBUG("cameraPresetHandler - biCameraPreset = ${biCameraPreset}")
		if(biCameraPreset == "PS1") {
			def setPreset = "1"
			biChangeProfile(setPreset)
		} else
		if(biCameraPreset == "PS2") {
			def setPreset = "2"
			biChangeProfile(setPreset)
		} else
		if(biCameraPreset == "PS3") {
			def setPreset = "3"
			biChangeProfile(setPreset)
		} else
		if(biCameraPreset == "PS4") {
			def setPreset = "4"
			biChangeProfile(setPreset)
		} else
		if(biCameraPreset == "PS5") {
			def setPreset = "5"
			biChangeProfile(setPreset)
		}
	}	
}	

def cameraSnapshotHandler(evt) {
	LOGDEBUG("BI Control-cameraSnapshotHandler...")
	LOGDEBUG("Switch on/off - $evt.device : $evt.value")

	if(switches.currentValue("switch") == "on") {
		LOGDEBUG("cameraSnapshotHandler - Nothing")
		def setPreset = "0"
		biChangeProfile(setPreset)
	}
}

def cameraTriggerHandler(evt) {
	LOGDEBUG("BI Control-cameraTriggerHandler...")
	LOGDEBUG("Switch on/off - $evt.device : $evt.value")

	if(switches.currentValue("switch") == "on") {
		LOGDEBUG("cameraTriggerHandler - On")
		def setPreset = "1"
		biChangeProfile(setPreset)
	} else
	if(switches.currentValue("switch") == "off") {
		LOGDEBUG("cameraTriggerHandler - Off")
		def setPreset = "0"
		biChangeProfile(setPreset)
	}
}

def biChangeProfile(num) {
    LOGDEBUG("BI Control-biChangeProfile...")
	
	biHost = "${parent.biServer}:${parent.biPort}"
	
	if(triggerMode == "Mode") {
		LOGDEBUG("I'm in Mode")
		biRawCommand = "/admin?profile=${num}&user=${parent.biUser}&pw=${parent.biPass}"
	} else
	if(triggerMode == "Switch") {
		LOGDEBUG("I'm in Switch")
		biRawCommand = "/admin?profile=${num}&user=${parent.biUser}&pw=${parent.biPass}"
	} else
	if(triggerMode == "Camera_Preset") {
		LOGDEBUG("I'm in Camera_Preset")
		biRawCommand = "/admin?camera=${biCamera}&preset=${num}&user=${parent.biUser}&pw=${parent.biPass}"
		// /admin?camera=x&preset=x
	} else
	if(triggerMode == "Camera_Snapshot") {
		LOGDEBUG("I'm in Camera_Snapshot")
		biRawCommand = "/admin?camera=${biCamera}/snapshot&user=${parent.biUser}&pw=${parent.biPass}"
		// /admin?camera=x&snapshot
	} else
	if(triggerMode == "Camera_Trigger") {
		LOGDEBUG("I'm in Camera_Trigger")
		biRawCommand = "/admin?camera=${biCamera}&manrec=${num}&user=${parent.biUser}&pw=${parent.biPass}"
		// /admin?camera=x&manrec=1
	}
	
	LOGDEBUG("sending GET to URL http://${biHost}/${biRawCommand}")
	LOGDEBUG("biUser: ${parent.biUser} - biPass: ${parent.biPass} - num: ${num}")
	
	def httpMethod = "GET"
	def httpRequest = [
		method:		httpMethod,
		path: 		biRawCommand,
		headers:	[
       				HOST:		biHost,
					Accept: 	"*/*",
                    ]
		]
	def hubAction = new hubitat.device.HubAction(httpRequest)
	sendHubCommand(hubAction)
}

// define debug action
def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "Further Logging Disabled"
	}
}

// logging...
def LOGDEBUG(txt){
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}

def display(){
	section{paragraph "Version: 1.0.2<br>@BPTWorld"}
}

def setVersion(){
		state.InternalName = "BIControlChild"  
}
