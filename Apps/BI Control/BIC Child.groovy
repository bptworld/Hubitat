/**
 *  ****************  BI Control Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to work locally with Blue Iris security software.
 *
 *  Copyright 2018-2020 Bryan Turcotte (@bptworld)
 *
 *  Thanks to (@jpark40) for the original 'Blue Iris Profiles based on Modes' code that I based this app off of.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  2.1.0 - 08/24/20 - Added Motion as trigger, added Reboot command
 *  2.0.9 - 07/09/20 - Fixed Disable switch
 *  2.0.8 - 06/26/20 - Fixed a typo
 *  2.0.7 - 06/25/20 - Add App Control options
 *  2.0.6 - 05/22/20 - Add toggle for camera triggers option
 *  2.0.5 - 05/22/20 - More contact options
 *  2.0.4 - 05/18/20 - Added contact triggers
 *  2.0.2 - 04/27/20 - Cosmetic changes
 *  2.0.2 - 02/16/20 - Fixed typo, thanks to @mluck
 *  2.0.1 - 12/07/19 - Added a delay command option, code cleanup, cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  ---
 *  1.0.0 - 11/03/18 - Hubitat Port of ST app 'Blue Iris Profiles based on Modes' - 2016 (@jpark40)
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "BI Control"
	state.version = "2.1.0"
}

definition(
	name: "BI Control Child",
	namespace: "BPTWorld",
	author: "Bryan Turcotte",
	description: "This app is designed to work locally with Blue Iris security software.",
	category: "Convenience",
	parent: "BPTWorld:BI Control",
	iconUrl: "",
	iconX2Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/BI%20Control/BIC%20Child.groovy",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "BI Control keeps everything local, no Internet required!"
			paragraph "This app uses 'Virtual Switches' and Contacts, instead of buttons. That way the devices can be used within Google Assistant and Rule Machine. Be sure to set 'Enable auto off' within each Virtual Device to '1s' (except for recording device)."
       	 paragraph "<b>Blue Iris requirements:</b>"
			paragraph "In Blue Iris settings > Web Server > Advanced > Advanced Settings<br> - Ensure 'Use secure session keys and login page' is not checked.<br> - Disable authentication, select “Non-LAN only” (preferred) or “No” to disable authentication altogether.<br> - Blue Iris only allows Admin Users to toggle profiles."	
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Control Type")) {
			input "triggerType", "enum", title: "Select Control Type", submitOnChange: true, options: ["Profile", "Schedule", "Camera"], required: true, Multiple: false
        }
        
		if(triggerType == "Profile"){
            section() {
    			input "triggerMode", "enum", title: "Select Trigger Type", submitOnChange: true, options: ["Mode","Switch_Contact_Motion"], required: true, Multiple: false
            }
			if(triggerMode == "Mode"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Ability to change BI Profile based on HE Mode")) {
					input "biProfile1", "mode", title: "Profile 1 Mode(s)", required: false, multiple: true, width:3
					input "biProfile2", "mode", title: "Profile 2 Mode(s)", required: false, multiple: true, width:3
					input "biProfile3", "mode", title: "Profile 3 Mode(s)", required: false, multiple: true, width:3 
					input "biProfile4", "mode", title: "Profile 4 Mode(s)", required: false, multiple: true, width:3
					input "biProfile5", "mode", title: "Profile 5 Mode(s)", required: false, multiple: true, width:3
					input "biProfile6", "mode", title: "Profile 6 Mode(s)", required: false, multiple: true, width:3
					input "biProfile7", "mode", title: "Profile 7 Mode(s)", required: false, multiple: true, width:3
				}
            }
			if(triggerMode == "Switch_Contact_Motion"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Ability to change the BI Profile using a Switch, Contact or Motion")) {
					paragraph "Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger Mode change", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger Mode change", required: false, multiple: true
                    input "motions", "capability.motionSensor", title: "Select motion sensor to trigger Mode change", required: false, multiple: true
                    
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

		if(triggerType == "Schedule"){
            section() {
    		    input "triggerMode", "enum", title: "Select Trigger Type", submitOnChange: true, options: ["Mode","Switch_Contact_Motion"], required: true, Multiple: false
            }
			if(triggerMode == "Mode"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Ability to change BI Schedule based on HE Mode")) {
					input "biScheduleName1", "text", title: "Schedule 1 Name", description: "The exact name of the BI schedule"
					input "biSchedule1", "mode", title: "Schedule 1 Mode(s)", required: false, multiple: true, width:3
                   	input "biScheduleName2", "text", title: "Schedule 2 Name", description: "The exact name of the BI schedule"
                   	input "biSchedule2", "mode", title: "Schedule 2 Mode(s)", required: false, multiple: true, width:3
               	 	input "biScheduleName3", "text", title: "Schedule 3 Name", description: "The exact name of the BI schedule"
               	 	input "biSchedule3", "mode", title: "Schedule 3 Mode(s)", required: false, multiple: true, width:3
                   	input "biScheduleName4", "text", title: "Schedule 4 Name", description: "The exact name of the BI schedule"
                   	input "biSchedule4", "mode", title: "Schedule 4 Mode(s)", required: false, multiple: true, width:3
                   	input "biScheduleName5", "text", title: "Schedule 5 Name", description: "The exact name of the BI schedule"
                   	input "biSchedule5", "mode", title: "Schedule 5 Mode(s)", required: false, multiple: true, width:3
                   	input "biScheduleName6", "text", title: "Schedule 6 Name", description: "The exact name of the BI schedule"
                   	input "biSchedule6", "mode", title: "Schedule 6 Mode(s)", required: false, multiple: true, width:3
				}
			}
                
			if(triggerMode == "Switch_Contact_Motion"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Ability to change the BI Schedule using a Switch, Contact or Motion")) {
					paragraph "Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger Mode change", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger Mode change", required: false, multiple: true
                    input "motions", "capability.motionSensor", title: "Select motion sensor to trigger Mode change", required: false, multiple: true
                    
                   	input "biScheduleSwitch", "text", title: "Schedule Name", description: "The exact name of the BI schedule to trigger with the switch"
                }
			}
		}
        
		if(triggerType == "Camera"){
            section() {
			    input "triggerMode", "enum", title: "Select Trigger Type", submitOnChange: true, options: ["Camera_Preset","Camera_Snapshot","Camera_Trigger","Camera_PTZ","Camera_Reboot"], required: true, Multiple: false
            }
			if(triggerMode == "Camera_Preset"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Camera Preset")) {
					paragraph "<b>Ability to move a camera to a Preset using a Switch, Contact or Motion.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger Camera Preset", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger Camera Preset", required: false, multiple: true
                    input "motions", "capability.motionSensor", title: "Select motion sensor to trigger Camera Preset", required: false, multiple: true
                    
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
				section(getFormat("header-green", "${getImage("Blank")}"+" Camera Snapshot")) {
					paragraph "<b>Ability to get a Camera Snapshot using a Switch, Contact or Motion.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
                section(){
					input "switches", "capability.switch", title: "Select switch to trigger Camera Snapshot", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger Camera Snapshot", required: false, multiple: true
                    input "motions", "capability.motionSensor", title: "Select motion sensor to trigger Camera Snapshot", required: false, multiple: true
                    
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
				}
			}
            
			if(triggerMode == "Camera_Trigger"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Camera Trigger")) {
					paragraph "<b>Ability to start or stop manual recording on camera using a Switch, Contact or Motion.</b><br>This ability uses both the On and Off so no need to set 'Enable auto off'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to Trigger Camera", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger Camera", required: false, multiple: true
                    input "motions", "capability.motionSensor", title: "Select motion sensor to trigger Camera", required: false, multiple: true
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
                    
                    paragraph "Camera Trigger can use two methods. If one doesn't work for you, please try the other."
                    input "useMethod", "bool", title: "Manrec (off) or Trigger (on)", defaultValue:false, submitOnChange:true
				}
			}
            
			if(triggerMode == "Camera_PTZ"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Camera PTZ")) {
					paragraph "<b>Ability to use PTZ commands using a Switch, Contact or Motion.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger PTZ command", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger PTZ command", required: false, multiple: true
                    input "motions", "capability.motionSensor", title: "Select motion sensor to trigger PTZ command", required: false, multiple: true
                    
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
					input "biCameraPTZ", "enum", title: "PTZ Command", options: [
						[PTZ0:"0 - Left"],
						[PTZ1:"1 - Right"],
						[PTZ2:"2 - Up"],
						[PTZ3:"3 - Down"],
						[PTZ4:"4 - Home"],
						[PTZ5:"5 - Zoom In"],
						[PTZ6:"6 - Zoom Out"],
					], required: true, multiple: false
				}
			}
            
            if(triggerMode == "Camera_Reboot"){
				section(getFormat("header-green", "${getImage("Blank")}"+" Camera Reboot")) {
					paragraph "<b>Ability to reboot camers using a Switch or Contact.</b><br>Be sure to set 'Enable auto off' within the Virtual Device to '1s'."
				}
				section(){
					input "switches", "capability.switch", title: "Select switch to trigger PTZ command", required: false, multiple: true
                    input "contacts", "capability.contactSensor", title: "Select contact sensor to trigger PTZ command", required: false, multiple: true
                    
					input "biCamera", "text", title: "Camera Name (use short name from BI, MUST BE EXACT)", required: true, multiple: false
				}
			}
		}
        if(triggerType) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
                input "delay", "number", title: "Delay commands by X seconds", defaultValue: 0
            }
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
            label title: "Enter a name for this child app", required: false, submitOnChange: true
        	input "logEnable", "bool", title: "Enable Debug Logging", required: true, defaultValue: false
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
	unschedule()
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In initialize - Initializing (${state.version})"

        if(app.label) {
            if(app.label.contains("Paused")) {
                app.updateLabel(app.label.replaceAll(" Paused",""))
                app.updateLabel(app.label + " <font color='red'>Paused</font>")
            }
        }

        if(logEnable) log.debug "In initialize - triggerMode: ${triggerMode}"
        if(triggerType == "Profile") {
            if(triggerMode == "Mode"){subscribe(location, "mode", profileModeChangeHandler)}
            if(triggerMode == "Switch_Contact_Motion") {
                if(switches) { subscribe(switches, "switch", profileSwitchHandler) }
                if(contacts) { subscribe(contacts, "contact", profileSwitchHandler) }
                if(motions) { subscribe(motions, "motion", profileSwitchHandler) }
            }
        } else if (triggerType == "Schedule") {
            if(triggerMode == "Mode"){subscribe(location, "mode", scheduleModeChangeHandler)}
            if(triggerMode == "Switch_Contact_Motion") {
                if(switches) { subscribe(switches, "switch", scheduleSwitchHandler) }
                if(contacts) { subscribe(contacts, "contact", scheduleSwitchHandler) }
                if(motions) { subscribe(motions, "motion", scheduleSwitchHandler) }
            }
        }
        if(triggerMode == "Camera_Preset") {
            if(switches) { subscribe(switches, "switch", cameraPresetHandler) }
            if(contacts) { subscribe(contacts, "contact", cameraPresetHandler) }  
            if(motions) { subscribe(motions, "motion", cameraPresetHandler) }
        }
        if(triggerMode == "Camera_Snapshot") {
            if(switches) { subscribe(switches, "switch", cameraSnapshotHandler) }
            if(contacts) { subscribe(contacts, "contact", cameraSnapshotHandler) }
            if(motions) { subscribe(motions, "motion", cameraSnapshotHandler) }
        }
        if(triggerMode == "Camera_Trigger") {
            if(switches) { subscribe(switches, "switch", cameraTriggerHandler) }
            if(contacts) { subscribe(contacts, "contact", cameraTriggerHandler) }
            if(motions) { subscribe(motions, "motion", cameraTriggerHandler) }
        }
        if(triggerMode == "Camera_PTZ") {
            if(switches) { subscribe(switches, "switch", cameraPTZHandler) }
            if(contacts) { subscribe(contacts, "contact", cameraPTZHandler) }
            if(motions) { subscribe(motions, "motion", cameraPTZHandler) }
        }
    }
}

def profileModeChangeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-modeChangeHandler (${state.version})"
        if(logEnable) log.debug "Mode changed to ${evt.value}"

        if(biProfile1 != null && evt.value in biProfile1) {
            def setProfile = "1"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile1 ${settings.biProfile1}"
        } 
        else if(biProfile2 != null && evt.value in biProfile2) {
            def setProfile = "2"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile2 ${settings.biProfile2}"
        } 
        else if(biProfile3 != null && evt.value in biProfile3) {
            def setProfile = "3"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile3 ${settings.biProfile3}"
        }
        else if(biProfile4 != null && evt.value in biProfile4) {
            def setProfile = "4"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile4 ${settings.biProfile4}"
        }
        else if(biProfile5 != null && evt.value in biProfile5) {
            def setProfile = "5"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile5 ${settings.biProfile5}"
        }
        else if(biProfile6 != null && evt.value in biProfile6) {
            def setProfile = "6"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile6 ${settings.biProfile6}"
        }
        else if(biProfile7 != null && evt.value in biProfile7) {
            def setProfile = "7"
            biChangeProfile(setProfile)
            if(logEnable) log.debug "biProfile7 ${settings.biProfile7}"
        }
    }
}

def profileSwitchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-switchChangeHandler (${state.version})"
        if(logEnable) log.debug "Switch on/off - $evt.device : $evt.value"

        checkSwitchesContacts()

        if(contin) {
            if(logEnable) log.debug "switchChangeHandler - switchProfileOn = ${switchProfileOn}"
            if(switchProfileOn == "Pon1") {
                def setProfile = "1"
                biChangeProfile(setProfile)
            } else if(switchProfileOn == "Pon2") {
                def setProfile = "2"
                biChangeProfile(setProfile)
            } else if(switchProfileOn == "Pon3") {
                def setProfile = "3"
                biChangeProfile(setProfile)
            } else if(switchProfileOn == "Pon4") {
                def setProfile = "4"
                biChangeProfile(setProfile)
            } else if(switchProfileOn == "Pon5") {
                def setProfile = "5"
                biChangeProfile(setProfile)
            } else if(switchProfileOn == "Pon6") {
                def setProfile = "6"
                biChangeProfile(setProfile)
            } else if(switchProfileOn == "Pon7") {
                def setProfile = "7"
                biChangeProfile(setProfile)
            }
        }
    }
}

def scheduleModeChangeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-modeChangeHandler (${state.version})"
        if(logEnable) log.debug "Mode changed to ${evt.value}"

        if(biScheduleName1 != null && evt.value in biSchedule1) {
            biChangeSchedule(biScheduleName1)
            if(logEnable) log.debug "biSchedule1 ${settings.biScheduleName1}"
        }
        else if(biScheduleName2 != null && evt.value in biSchedule2) {
            biChangeSchedule(biScheduleName2)
            if(logEnable) log.debug "biSchedule2 ${settings.biScheduleName2}"
        }
        else if(biScheduleName3 != null && evt.value in biSchedule3) {
            biChangeSchedule(biScheduleName3)
            if(logEnable) log.debug "biSchedule3 ${settings.biScheduleName3}"
        }
        else if(biScheduleName4 != null && evt.value in biSchedule4) {
            biChangeSchedule(biScheduleName4)
            if(logEnable) log.debug "biSchedule4 ${settings.biScheduleName4}"
        }
        else if(biScheduleName5 != null && evt.value in biSchedule5) {
            biChangeSchedule(biScheduleName5)
            if(logEnable) log.debug "biSchedule4 ${settings.biScheduleName5}"
        }
        else if(biScheduleName6 != null && evt.value in biSchedule6) {
            biChangeSchedule(biScheduleName6)
            if(logEnable) log.debug "biSchedule6 ${settings.biScheduleName6}"
        }
    }
}

def scheduleSwitchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-switchChangeHandler (${state.version})"
        if(logEnable) log.debug "Switch on/off - $evt.device : $evt.value"

        checkSwitchesContacts()

        if(contin) {
            if(logEnable) log.debug "scheduleSwitchHandler - switchScheduleOn = ${biScheduleSwitch}"
            biChangeSchedule(biScheduleSwitch)
        }
    }
}

def cameraPresetHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-cameraPresetHandler (${state.version})"
        if(logEnable) log.debug "Switch on/off - $evt.device : $evt.value"

        checkSwitchesContacts()

        if(contin) {
            if(logEnable) log.debug "cameraPresetHandler - biCameraPreset = ${biCameraPreset}"
            if(biCameraPreset == "PS1") {
                def setPreset = "1"
                biChangeProfile(setPreset)
            } else if(biCameraPreset == "PS2") {
                def setPreset = "2"
                biChangeProfile(setPreset)
            } else if(biCameraPreset == "PS3") {
                def setPreset = "3"
                biChangeProfile(setPreset)
            } else if(biCameraPreset == "PS4") {
                def setPreset = "4"
                biChangeProfile(setPreset)
            } else if(biCameraPreset == "PS5") {
                def setPreset = "5"
                biChangeProfile(setPreset)
            }
        }	
    }
}	

def cameraSnapshotHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-cameraSnapshotHandler (${state.version})"
        if(logEnable) log.debug "Switch on/off - $evt.device : $evt.value"

        checkSwitchesContacts()

        if(contin) {
            if(logEnable) log.debug "cameraSnapshotHandler - Nothing"
            def setPreset = "0"
            biChangeProfile(setPreset)
        }
    }
}

def cameraTriggerHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-cameraTriggerHandler (${state.version})"
        if(logEnable) log.debug "Switch on/off - $evt.device : $evt.value"

        checkSwitchesContacts()

        if(contin) {
            if(logEnable) log.debug "cameraTriggerHandler - On"
            def setPreset = "1"
            biChangeProfile(setPreset)
        } else {
            if(logEnable) log.debug "cameraTriggerHandler - Off"
            def setPreset = "0"
            biChangeProfile(setPreset)
        }
    }
}

def cameraPTZHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "BI Control-cameraPTZHandler (${state.version})"
        if(logEnable) log.debug "Switch on/off - $evt.device : $evt.value"

        checkSwitchesContacts()

        if(contin) {
            if(logEnable) log.debug "cameraPTZHandler - biCameraPTZ = ${biCameraPTZ}"
            if(biCameraPTZ == "PTZ0") {
                def setPreset = "0"
                biChangeProfile(setPreset)
            } else if(biCameraPTZ == "PTZ1") {
                def setPreset = "1"
                biChangeProfile(setPreset)
            } else if(biCameraPTZ == "PTZ2") {
                def setPreset = "2"
                biChangeProfile(setPreset)
            } else if(biCameraPTZ == "PTZ3") {
                def setPreset = "3"
                biChangeProfile(setPreset)
            } else if(biCameraPTZ == "PTZ4") {
                def setPreset = "4"
                biChangeProfile(setPreset)
            } else if(biCameraPTZ == "PTZ5") {
                def setPreset = "5"
                biChangeProfile(setPreset)
            } else if(biCameraPTZ == "PTZ6") {
                def setPreset = "6"
                biChangeProfile(setPreset)
            }
        }	
    }
}

def biChangeProfile(num) {
	if(logEnable) log.debug "BI Control-biChangeProfile (${state.version})"

	biHost = "${parent.biServer}:${parent.biPort}"

	if(triggerMode == "Mode") {
		if(logEnable) log.debug "I'm in Mode"
		biRawCommand = "/admin?profile=${num}&user=${parent.biUser}&pw=${parent.biPass}"
        
    } else if(triggerMode == "Switch_Contact_Motion") {
        if(logEnable) log.debug "I'm in Switch, Contact or Motion"
        biRawCommand = "/admin?profile=${num}&user=${parent.biUser}&pw=${parent.biPass}"
        
    } else if(triggerMode == "Camera_Preset") {
        if(logEnable) log.debug "I'm in Camera_Preset"
        biRawCommand = "/admin?camera=${biCamera}&preset=${num}&user=${parent.biUser}&pw=${parent.biPass}"
        
        // /admin?camera=x&preset=x
    } else if(triggerMode == "Camera_Snapshot") {
        if(logEnable) log.debug "I'm in Camera_Snapshot"
        biRawCommand = "/admin?camera=${biCamera}&snapshot&user=${parent.biUser}&pw=${parent.biPass}"
        
        // /admin?camera=x&snapshot
    } else if(triggerMode == "Camera_Trigger") {
        if(logEnable) log.debug "I'm in Camera_Trigger"
        if(!useMethod) biRawCommand = "/admin?camera=${biCamera}&manrec=${num}&user=${parent.biUser}&pw=${parent.biPass}"
        if(useMethod) biRawCommand = "/admin?camera=${biCamera}&trigger&user=${parent.biUser}&pw=${parent.biPass}"
        
        // NOTE: if this Command doesn't work for you, try the second one instead
        // /admin?camera=x&manrec=1
    } else if(triggerMode == "Camera_PTZ") {
        if(logEnable) log.debug "I'm in Camera_PTZ"
        biRawCommand = "/cam/${biCamera}/pos=${num}"
        
        // /cam/{cam-short-name}/pos=x Performs a PTZ command on the specified camera, where x= 0=left, 1=right, 2=up, 3=down, 4=home, 5=zoom in, 6=zoom out
    } else if(triggerMode == "Camera_Reboot") {
        if(logEnable) log.debug "I'm in Camera_Reboot"
        biRawCommand = "/admin?camera=${biCamera}&reboot&user=${parent.biUser}&pw=${parent.biPass}"

        // /admin?camera=x&reboot
    }

	if(logEnable) log.debug "sending GET to URL http://${biHost}${biRawCommand}"
	if(logEnable) log.debug "biUser: ${parent.biUser} - biPass: ${parent.biPass} - num: ${num}"

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
    if(delay == null || delay == "") delay = 0
    if(delay > 0) {
        delayM = delay * 1000
        if(logEnable) log.debug "In biChangeProfile - pausing ${delay} second(s) before sending command"
        pauseExecution(delayM)
    }
	sendHubCommand(hubAction)
}

def biChangeSchedule(schedule) {
    if(logEnable) log.debug "BI Control-biChangeSchedule (${state.version})"

	biHost = "${parent.biServer}:${parent.biPort}"

	biRawCommand = "/admin?schedule=${schedule}&user=${parent.biUser}&pw=${parent.biPass}"

	if(logEnable) log.debug "sending GET to URL ${biHost}${biRawCommand}"
	if(logEnable) log.debug "biUser: ${parent.biUser} - biPass: ${parent.biPass} - num: ${num}"

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
    if(delay == null || delay == "") delay = 0
    if(delay > 0) {
        delayM = delay * 1000
        if(logEnable) log.debug "In biChangeSchedule - pausing ${delay} second(s) before sending command"
        pauseExecution(delayM)
    }
    sendHubCommand(hubAction)
}

def checkSwitchesContacts() {
    contin = false
    if(switches) {
        switches.each { it1 ->
            if(it1.currentValue("switch") == "on") {
                contin = true
            }
        }
    }
    
    if(contacts) { 
        contacts.each { it2 ->
            if(it2.currentValue("contact") == "open") {
                contin = true
            }
        }
    }
    
    if(motions) { 
        motions.each { it3 ->
            if(it3.currentValue("motion") == "active") {
                contin = true
            }
        }
    }
    
    return contin
}

// ********** Normal Stuff **********
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            state.eSwitch = it.currentValue("switch")
            if(state.eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable) log.debug "In setDefaults (${state.version})"
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
        if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
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
