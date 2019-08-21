/**
 *  ****************  Magic Cube Child  ****************
 *
 *  Design Usage:
 *  Take control of your Xiaomi Aqara/Mi Cube. Control devices based on Flip, Slide, Knock, Rotation and Shake.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to Keith G. (@veeceeoh) for bringing the driver needed for this device over to Hubitat!
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  V2.0.1 - 08/21/19 - (dan.t) added ability to execute rule actions
 *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  V1.0.3 - 08/12/19 - (aaronward) Added support for Harmony Logitech control
 *  V1.0.2 - 04/15/19 - Code cleanup
 *  V1.0.1 - 03/15/19 - Added Change Mode, Toggle, Dim and Set Color to device options.
 *  V1.0.0 - 03/14/19 - Initial Release
 *
 */
import hubitat.helper.RMUtils

def setVersion(){
    // *  V2.0.0 - 08/18/19 - Now App Watchdog compliant
	if(logEnable) log.debug "In setVersion - App Watchdog Child app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion or AppWatchdogDriverVersion
    state.appName = "MagicCubeChildVersion"
	state.version = "v2.0.1"
    
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
    name: "Magic Cube Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Take control of your Xiaomi Aqara/Mi Cube. Control devices based on Flip, Slide, Knock, Rotation and Shake.",
    category: "",
	parent: "BPTWorld:Magic Cube",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Magic%20Cube/MC-child.groovy",
)

preferences {
    page name: "pageConfig"
	page name: "face0Options", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "face1Options", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "face2Options", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "face3Options", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "face4Options", title: "", install: false, uninstall: true, nextPage: "pageConfig"
	page name: "face5Options", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "<h2 style='color:#1A77C9;font-weight: bold'>Magic Cube</h2>", install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "For use with the Xiaomi Aqara/Mi Cube using the 'Xiaomi Mi Cube Controller device driver'<br>Driver must have 'Cube Mode' set to 36"
			paragraph "Driver information can be found here: <a href='https://community.hubitat.com/t/release-xiaomi-aqara-device-drivers/631/527' target='_Blank'>[TUTORIAL] Use of the Xiaomi Mi Cube Controller device driver</a>"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Xiaomi Aqara/Mi Cube Controller")) {
			input "xCube", "capability.threeAxis", title: "Select a Cube device", required: true, submitOnChange: true
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Choose which cube face to configure")) {
			href "face0Options", title:"Cube Face 0 Options", description:"Click here to setup Face 0 Options"
			href "face1Options", title:"Cube Face 1 Options", description:"Click here to setup Face 1 Options"
			href "face2Options", title:"Cube Face 2 Options", description:"Click here to setup Face 2 Options"
			href "face3Options", title:"Cube Face 3 Options", description:"Click here to setup Face 3 Options"
			href "face4Options", title:"Cube Face 4 Options", description:"Click here to setup Face 4 Options"
			href "face5Options", title:"Cube Face 5 Options", description:"Click here to setup Face 5 Options"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Set Delay")) {
			paragraph "Sometimes the cube needs to settle to get the oriantation correct. This will help with that."
			input "oXDelay", "number", title: "Delay X number of seconds BEFORE sending command", required: true, defaultValue: 2
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this automation", required: false}
        section() {
            input(name: "logEnable", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def face0Options(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "face0Options", title: "Cube Face 0 Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 Options")) {
			paragraph "<b>Face 0 is facing up...</b>"
			input(name: "oF0FlipTo", type: "bool", defaultValue: "false", title: "Flip to 0", description: "0 FlipTo", width: 4, submitOnChange: true)
			input(name: "oF0Slide", type: "bool", defaultValue: "false", title: "Slide", description: "0 Slide", width: 4, submitOnChange: true)
			input(name: "oF0Knock", type: "bool", defaultValue: "false", title: "Knock", description: "0 Knock", width: 4, submitOnChange: true)
			input(name: "oF0rLeft", type: "bool", defaultValue: "false", title: "Rotate Left", description: "0 rLeft", width: 4, submitOnChange: true)
			input(name: "oF0rRight", type: "bool", defaultValue: "false", title: "Rotate Right", description: "0 rRight", width: 4, submitOnChange: true)
			input(name: "oF0Shake", type: "bool", defaultValue: "false", title: "Shake", description: "0 Shake", width: 4, submitOnChange: true)
		}
	section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 Options")) {	
		if(oF0FlipTo) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Flip To Options")) {
				input(name: "f0FlipToOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0FlipToOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f0FlipToToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f0FlipTosetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f0FlipTosetOnLC) input "f0FlipTolevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f0FlipTosetOnLC) { input "f0FlipTocolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f0FlipTonewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f0FlipTonewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f0FlipTonewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f0FlipTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f0FlipTonewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f0FlipTonewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
                input "f0FlipToRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF0Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Slide Options")) {
				input(name: "f0SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f0SlideToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f0SlidesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f0SlidesetOnLC) input "f0SlidelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f0SlidesetOnLC) { input "f0SlidecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f0SlidenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f0SlidenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f0SlidenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f0SlidenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f0SlidenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f0SlidenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
                input "f0SlideRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF0Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Knock Options")) {
				input(name: "f0KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f0KnockToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f0KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f0KnocksetOnLC) input "f0KnocklevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f0KnocksetOnLC) { input "f0KnockcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f0KnocknewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f0KnocknewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f0KnocknewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f0KnockTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f0KnocknewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f0KnocknewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
                input "f0KnockRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF0rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Rotate Left Options")) {
				input(name: "f0rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f0rLeftToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f0rLeftsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f0rLeftsetOnLC) input "f0rLeftlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f0rLeftsetOnLC) { input "f0rLeftcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f0rLeftnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f0rLeftnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f0rLeftnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f0LeftnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f0rLeftnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f0rLeftnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false) 
                input "f0LeftRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
				
			}
		}
		if(oF0rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Rotate Right Options")) {
				input(name: "f0rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f0rRightToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f0rRightsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f0rRightsetOnLC) input "f0rRightlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f0rRightsetOnLC) { input "f0rRightcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f0rRightnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f0rRightnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f0rRightnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f0RightnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f0rRightnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f0rRightnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)               
                input "f0RightRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF0Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Shake Options")) {
				input(name: "f0ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f0ShakeToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f0ShakesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f0ShakesetOnLC) input "f0ShakelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f0ShakesetOnLC) { input "f0ShakecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f0ShakenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f0ShakenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f0ShakenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f0ShakenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f0ShakenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f0ShakenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
                input "f0ShakeRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
	}
	}
}

def face1Options(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "face1Options", title: "Cube Face 1 Options", install: false, uninstall:false){
	section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 Options")) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 Options")) {
			paragraph "<b>Face 1 is facing up...</b>"
			input(name: "oF1FlipTo", type: "bool", defaultValue: "false", title: "Flip to 1", description: "1 FlipTo", width: 6, submitOnChange: true)
			input(name: "oF1Slide", type: "bool", defaultValue: "false", title: "Slide", description: "1 Slide", width: 6, submitOnChange: true)
			input(name: "oF1Knock", type: "bool", defaultValue: "false", title: "Knock", description: "1 Knock", width: 6, submitOnChange: true)
			input(name: "oF1rLeft", type: "bool", defaultValue: "false", title: "Rotate Left", description: "1 rLeft", width: 6, submitOnChange: true)
			input(name: "oF1rRight", type: "bool", defaultValue: "false", title: "Rotate Right", description: "1 rRight", width: 6, submitOnChange: true)
			input(name: "oF1Shake", type: "bool", defaultValue: "false", title: "Shake", description: "1 Shake", width: 6, submitOnChange: true)
		}
		if(oF1FlipTo) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Flip To Options")) {
				input(name: "f10FlipToOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1FlipToOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f1FlipToToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f1FlipTosetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f1FlipTosetOnLC) input "f1FlipTolevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f1FlipTosetOnLC) { input "f1FlipTocolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f1FlipTonewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f1FlipTonewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f1FlipTonewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f1FlipTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f1FlipTonewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f1FlipTonewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f1FlipToRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF1Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Slide Options")) {
				input(name: "f1SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f1SlideToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f1SlidesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f1SlidesetOnLC) input "f1SlidelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f1SlidesetOnLC) { input "f1SlidecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f1SlidenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f1SlidenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f1SlidenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f1SlidenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f1SlidenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f1SlidenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f1SlideRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF1Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Knock Options")) {
				input(name: "f1KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f1KnockToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f1KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f1KnocksetOnLC) input "f1KnocklevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f1KnocksetOnLC) { input "f1KnockcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f1KnocknewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f1KnocknewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f1KnocknewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f1KnockTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f1KnocknewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f1KnocknewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f1KnockRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF1rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Rotate Left Options")) {
				input(name: "f1rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f1rLeftToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f1KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f1rLeftsetOnLC) input "f1rLeftlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f1rLeftsetOnLC) { input "f1rLeftcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f1rLeftnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f1rLeftnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f1rLeftnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f1LeftnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f1rLeftnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f1rLeftnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f1LeftRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF1rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Rotate Right Options")) {
				input(name: "f1rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f1rRightToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f1rRightsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f1rRightsetOnLC) input "f1rRightlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f1rRightsetOnLC) { input "f1rRightcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f1rRightnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f1rRightnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f1rRightnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f1rRightnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f1rRightnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f1rRightnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                  
				input "f1RightRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF1Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Shake Options")) {
				input(name: "f1ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f1ShakeToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f1ShakesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f1ShakesetOnLC) input "f1ShakelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f1ShakesetOnLC) { input "f1ShakecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f1ShakenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f1ShakenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f1ShakenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f1ShakenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f1ShakenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f1ShakenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f1ShakeRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
	}
	}
}

def face2Options(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "face2Options", title: "Cube Face 2 Options", install: false, uninstall:false){
	section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 Options")) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 Options")) {
			paragraph "<b>Face 2 is facing up...</b>"
			input(name: "oF2FlipTo", type: "bool", defaultValue: "false", title: "Flip to 2", description: "2 FlipTo", width: 6, submitOnChange: true)
			input(name: "oF2Slide", type: "bool", defaultValue: "false", title: "Slide", description: "2 Slide", width: 6, submitOnChange: true)
			input(name: "oF2Knock", type: "bool", defaultValue: "false", title: "Knock", description: "2 Knock", width: 6, submitOnChange: true)
			input(name: "oF2rLeft", type: "bool", defaultValue: "false", title: "Rotate Left", description: "2 rLeft", width: 6, submitOnChange: true)
			input(name: "oF2rRight", type: "bool", defaultValue: "false", title: "Rotate Right", description: "2 rRight", width: 6, submitOnChange: true)
			input(name: "oF2Shake", type: "bool", defaultValue: "false", title: "Shake", description: "2 Shake", width: 6, submitOnChange: true)
		}
		if(oF2FlipTo) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Flip To Options")) {
				input(name: "f2FlipToOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2FlipToOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f2FlipToToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f2FlipTosetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f2FlipTosetOnLC) input "f2FlipTolevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f2FlipTosetOnLC) { input "f2FlipTocolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f2FlipTonewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f2FlipTonewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f2FlipTonewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f2FlipTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f2FlipTonewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f2FlipTonewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                  
				input "f2FlipToRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF2Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Slide Options")) {
				input(name: "f2SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f2SlideToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f2SlidesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f2SlidesetOnLC) input "f2SlidelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f2SlidesetOnLC) { input "f2SlidecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f2SlidenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f2SlidenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f2SlidenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f2SlidenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f2SlidenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f2SlidenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f2SlideRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF2Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Knock Options")) {
				input(name: "f2KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f2KnockToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f2KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f2KnocksetOnLC) input "f2KnocklevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f2KnocksetOnLC) { input "f2KnockcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f2KnocknewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f2KnocknewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f2KnocknewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f2KnockTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f2KnocknewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f2KnocknewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f2KnockRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF2rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Rotate Left Options")) {
				input(name: "f2rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f2rLeftToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f2KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f2rLeftsetOnLC) input "f2rLeftlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f2rLeftsetOnLC) { input "f2rLeftcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f2rLeftnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f2rLeftnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f2rLeftnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f2rLeftnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f2rLeftnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f2rLeftnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f2LeftRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF2rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Rotate Right Options")) {
				input(name: "f2rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f2rRightToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f2rRightsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f2rRightsetOnLC) input "f2rRightlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f2rRightsetOnLC) { input "f2rRightcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f2rRightnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f2rRightnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f2rRightnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f2rRightnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f2rRightnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f2rRightnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                  
				input "f2RightRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF2Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Shake Options")) {
				input(name: "f2ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f2ShakeToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f2ShakesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f2ShakesetOnLC) input "f2ShakelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f2ShakesetOnLC) { input "f2ShakecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f2ShakenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f2ShakenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f2ShakenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f2ShakenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f2ShakenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f2ShakenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f2ShakeRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
	}
	}
}

def face3Options(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "face3Options", title: "Cube Face 3 Options", install: false, uninstall:false){
	section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 Options")) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 Options")) {
			paragraph "<b>Face 3 is facing up...</b>"
			input(name: "oF3FlipTo", type: "bool", defaultValue: "false", title: "Flip to 3", description: "3 FlipTo", width: 6, submitOnChange: true)
			input(name: "oF3Slide", type: "bool", defaultValue: "false", title: "Slide", description: "3 Slide", width: 6, submitOnChange: true)
			input(name: "oF3Knock", type: "bool", defaultValue: "false", title: "Knock", description: "3 Knock", width: 6, submitOnChange: true)
			input(name: "oF3rLeft", type: "bool", defaultValue: "false", title: "Rotate Left", description: "3 rLeft", width: 6, submitOnChange: true)
			input(name: "oF3rRight", type: "bool", defaultValue: "false", title: "Rotate Right", description: "3 rRight", width: 6, submitOnChange: true)
			input(name: "oF3Shake", type: "bool", defaultValue: "false", title: "Shake", description: "3 Shake", width: 6, submitOnChange: true)
		}
		if(oF3FlipTo) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Flip To Options")) {
				input(name: "f3FlipToOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3FlipToOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f3FlipToToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f3FlipTosetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f3FlipTosetOnLC) input "f3FlipTolevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f3FlipTosetOnLC) { input "f3FlipTocolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f3FlipTonewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f3FlipTonewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f3FlipTonewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f3FlipTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f3FlipTonewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f3FlipTonewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f3FlipToRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF3Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Slide Options")) {
				input(name: "f3SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f3SlideToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f3SlidesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f3SlidesetOnLC) input "f3SlidelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f3SlidesetOnLC) { input "f3SlidecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f3SlidenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f3SlidenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f3SlidenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f3SlidenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f3SlidenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f3SlidenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f3SlideRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF3Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Knock Options")) {
				input(name: "f3KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f3KnockToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f3KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f3KnocksetOnLC) input "f3KnocklevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f3KnocksetOnLC) { input "f3KnockcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f3KnocknewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f3KnocknewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f3KnocknewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f3KnockTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f3KnocknewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f3KnocknewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f3KnockRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF3rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Rotate Left Options")) {
				input(name: "f3rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f3rLeftToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f3KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f3rLeftsetOnLC) input "f3rLeftlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f3rLeftsetOnLC) { input "f3rLeftcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f3rLeftnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f3rLeftnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f3rLeftnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f3rLeftnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f3rLeftnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f3rLeftnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f3LeftRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF3rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Rotate Right Options")) {
				input(name: "f3rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f3rRightToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f3rRightsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f3rRightsetOnLC) input "f3rRightlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f3rRightsetOnLC) { input "f3rRightcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f3rRightnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f3rRightnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f3rRightnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f3rRightnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f3rRightnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f3rRightnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                  
				input "f3RightRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF3Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Shake Options")) {
				input(name: "f3ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f3ShakeToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f3ShakesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f3ShakesetOnLC) input "f3ShakelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f3ShakesetOnLC) { input "f3ShakecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f3ShakenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f3ShakenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f3ShakenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f3ShakenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f3ShakenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f3ShakenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                     
				input "f3ShakeRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
	}
	}
}

def face4Options(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "face4Options", title: "Cube Face 4 Options", install: false, uninstall:false){
	section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 Options")) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 Options")) {
			paragraph "<b>Face 4 is facing up...</b>"
			input(name: "oF4FlipTo", type: "bool", defaultValue: "false", title: "Flip to 4", description: "4 FlipTo", width: 6, submitOnChange: true)
			input(name: "oF4Slide", type: "bool", defaultValue: "false", title: "Slide", description: "0 Slide", width: 6, submitOnChange: true)
			input(name: "oF4Knock", type: "bool", defaultValue: "false", title: "Knock", description: "0 Knock", width: 6, submitOnChange: true)
			input(name: "oF4rLeft", type: "bool", defaultValue: "false", title: "Rotate Left", description: "0 rLeft", width: 6, submitOnChange: true)
			input(name: "oF4rRight", type: "bool", defaultValue: "false", title: "Rotate Right", description: "0 rRight", width: 6, submitOnChange: true)
			input(name: "oF4Shake", type: "bool", defaultValue: "false", title: "Shake", description: "0 Shake", width: 6, submitOnChange: true)
		}
		if(oF4FlipTo) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Flip To Options")) {
				input(name: "f4FlipToOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4FlipToOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f4FlipToToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f4FlipTosetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f4FlipTosetOnLC) input "f4FlipTolevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f4FlipTosetOnLC) { input "f4FlipTocolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f4FlipTonewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f4FlipTonewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f4FlipTonewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f4FlipTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f4FlipTonewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f4FlipTonewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f4FlipToRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF4Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Slide Options")) {
				input(name: "f4SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f4SlideToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f4SlidesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f4SlidesetOnLC) input "f4SlidelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f4SlidesetOnLC) { input "f4SlidecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f4SlidenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f4SlidenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f4SlidenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f4SlidenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f4SlidenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f4SlidenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f4SlideRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF4Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Knock Options")) {
				input(name: "f4KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f4KnockToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f4KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f4KnocksetOnLC) input "f4KnocklevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f4KnocksetOnLC) { input "f4KnockcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f4KnocknewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f4KnocknewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f4KnocknewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f4KnockTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f4KnocknewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f4KnocknewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f4KnockRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF4rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Rotate Left Options")) {
				input(name: "f4rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f4rLeftToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f4KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f4rLeftsetOnLC) input "f4rLeftlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f4rLeftsetOnLC) { input "f4rLeftcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f4rLeftnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f4rLeftnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f4rLeftnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f4rLeftnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f4rLeftnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f4rLeftnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f4LeftRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF4rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Rotate Right Options")) {
				input(name: "f4rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f4rRightToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f4rRightsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f4rRightsetOnLC) input "f4rRightlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f4rRightsetOnLC) { input "f4rRightcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f4rRightnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f4rRightnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f4rRightnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f4rRightnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f4rRightnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f4rRightnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                  
				input "f4RightRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF4Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Shake Options")) {
				input(name: "f4ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f4ShakeToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f4ShakesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f4ShakesetOnLC) input "f4ShakelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f4ShakesetOnLC) { input "f4ShakecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
                
				input(name: "f4ShakenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f4ShakenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f4ShakenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f4ShakenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f4ShakenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f4ShakenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f4ShakeRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
	}
	}
}

def face5Options(){
    def rules = RMUtils.getRuleList()
    dynamicPage(name: "face5Options", title: "Cube Face 5 Options", install: false, uninstall:false){
	section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 Options")) {
		section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 Options")) {
			paragraph "<b>Face 5 is facing up...</b>"
			input(name: "oF5FlipTo", type: "bool", defaultValue: "false", title: "Flip to 5", description: "5 FlipTo", width: 6, submitOnChange: true)
			input(name: "oF5Slide", type: "bool", defaultValue: "false", title: "Slide", description: "5 Slide", width: 6, submitOnChange: true)
			input(name: "oF5Knock", type: "bool", defaultValue: "false", title: "Knock", description: "5 Knock", width: 6, submitOnChange: true)
			input(name: "oF5rLeft", type: "bool", defaultValue: "false", title: "Rotate Left", description: "5 rLeft", width: 6, submitOnChange: true)
			input(name: "oF5rRight", type: "bool", defaultValue: "false", title: "Rotate Right", description: "5 rRight", width: 6, submitOnChange: true)
			input(name: "oF5Shake", type: "bool", defaultValue: "false", title: "Shake", description: "5 Shake", width: 6, submitOnChange: true)
		}
		if(oF5FlipTo) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Flip To Options")) {
				input(name: "f5FlipToOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5FlipToOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f5FlipToToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f5FlipTosetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f5FlipTosetOnLC) input "f5FlipTolevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f5FlipTosetOnLC) { input "f5FlipTocolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f5FlipTonewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f5FlipTonewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f5FlipTonewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f5FlipTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f5FlipTonewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f5FlipTonewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                  
				input "f5FlipToRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF5Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Slide Options")) {
				input(name: "f5SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f5SlideToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f5SlidesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f5SlidesetOnLC) input "f5SlidelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f5SlidesetOnLC) { input "f5SlidecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f5SlidenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f5SlidenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f5SlidenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f5SlidenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f5SlidenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f5SlidenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f5SlideRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF5Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Knock Options")) {
				input(name: "f5KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f5KnockToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f5KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f5KnocksetOnLC) input "f5KnocklevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f5KnocksetOnLC) { input "f5KnockcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f5KnocknewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f5KnocknewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f5KnocknewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f5KnockTonewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)                
                input(name: "f5KnocknewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f5KnocknewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                
				input "f5KnockRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF5rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Rotate Left Options")) {
				input(name: "f5rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f5rLeftToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f5KnocksetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f5rLeftsetOnLC) input "f5rLeftlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f5rLeftsetOnLC) { input "f5rLeftcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f5rLeftnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f5rLeftnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f5rLeftnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f5rLeftnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f5rLeftnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f5rLeftnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f5LeftRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
		if(oF5rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Rotate Right Options")) {
				input(name: "f5rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f5rRightToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f5rRightsetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f5rRightsetOnLC) input "f5rRightlevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f5rRightsetOnLC) { input "f5rRightcolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f5rRightnewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f5rRightnewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f5rRightnewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f5rRightnewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f5rRightnewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f5rRightnewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                 
				input "f5RightRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true   
			}
		}
		if(oF5Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Shake Options")) {
				input(name: "f5ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
				input(name: "f5ShakeToggle", type: "capability.switch", title: "Toggle these switches", required: false, multiple: true)
				input "f5ShakesetOnLC", "capability.switchLevel", title: "Select dimmers to turn on", required: false, multiple: true, submitOnChange: true
				if(f5ShakesetOnLC) input "f5ShakelevelLC", "number", title: "On Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
				if(f5ShakesetOnLC) { input "f5ShakecolorLC", "enum", title: "Color", required: false, options: [
                		["Soft White":"Soft White - Default"],
                		["White":"White - Concentrate"],
                		["Daylight":"Daylight - Energize"],
                		["Warm White":"Warm White - Relax"],
                		"Red","Green","Blue","Yellow","Orange","Purple","Pink"]
						paragraph "<hr>"
				}
				input(name: "f5ShakenewMode", type: "mode", title: "Change Mode", required: false, multiple: false)
                input(name: "f5ShakenewVolumeUp", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Up", mulitple: false, required: false)
                input(name: "f5ShakenewVolumeDown", type: "capability.audioVolume", title: "Control Harmony Logitech Volume Down", mulitple: false, required: false)
                input(name: "f5ShakenewMute", type: "capability.audioVolume", title: "Control Harmony Logitech Mute Volume", mulitple: false, required: false)   
                input(name: "f5ShakenewChannelUp", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Up", mulitple: false, required: false)
                input(name: "f5ShakenewChannelDown", type: "capability.audioVolume", title: "Control Harmony Logitech Channel Down", mulitple: false, required: false)                    
				input "f5ShakeRules", "enum", title: "Select which rule actions to run", options: rules, multiple: true
			}
		}
	}
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	initialize()
}

def initialize() {
	subscribe(xCube, "pushed", pushedHandler)
	subscribe(xCube, "face", faceHandler)
	subscribe(xCube, "angle", angleHandler)
}

def faceHandler(msg) {
	state.faceValue = msg.value.toString()
	if(logEnable) log.debug "In faceHandler - Face: ${state.faceValue}"
	waitHere()
}

def pushedHandler(msg) {
	state.pushedValue = msg.value.toString()
	if(logEnable) log.debug "In pushedHandler - Pushed: ${state.pushedValue}"
	waitHere()
}

def angleHandler(msg) {
	state.OLDangleValue = state.angleValue
	state.angleValue = msg.value.toString()
	if(logEnable) log.debug "In angleHandler - Angle: ${state.angleValue}"
	waitHere()
}

def waitHere() {
	if(state.angleValue == null) state.angleValue = "0"
	// Wait here to make sure all info is up to date
	runIn(oXDelay, doSomethingHandler)
}

def doSomethingHandler() {
	if(logEnable) log.debug "In doSomethingHandler - Face: ${state.faceValue}, Pushed: ${state.pushedValue}, Angle: ${state.angleValue}"
	if(state.pushedValue == "1" || state.pushedValue == "7"  || state.pushedValue == "13" || state.pushedValue == "19" || state.pushedValue == "25" || state.pushedValue == "31") { state.face = "f0" } 
	if(state.pushedValue == "2" || state.pushedValue == "8"  || state.pushedValue == "14" || state.pushedValue == "20" || state.pushedValue == "26" || state.pushedValue == "32") { state.face = "f1" }
	if(state.pushedValue == "3" || state.pushedValue == "9"  || state.pushedValue == "15" || state.pushedValue == "21" || state.pushedValue == "27" || state.pushedValue == "33") { state.face = "f2" }
	if(state.pushedValue == "4" || state.pushedValue == "10"  || state.pushedValue == "16" || state.pushedValue == "22" || state.pushedValue == "28" || state.pushedValue == "34") { state.face = "f3" }
	if(state.pushedValue == "5" || state.pushedValue == "11"  || state.pushedValue == "17" || state.pushedValue == "23" || state.pushedValue == "29" || state.pushedValue == "35") { state.face = "f4" }
	if(state.pushedValue == "6" || state.pushedValue == "12"  || state.pushedValue == "18" || state.pushedValue == "24" || state.pushedValue == "30" || state.pushedValue == "36") { state.face = "f5" }
	
	if(state.pushedValue == "1" || state.pushedValue == "2"  || state.pushedValue == "3" || state.pushedValue == "4" || state.pushedValue == "5" || state.pushedValue == "6") { state.action = "Flip To" }
	if(state.pushedValue == "7" || state.pushedValue == "8"  || state.pushedValue == "9" || state.pushedValue == "10" || state.pushedValue == "11" || state.pushedValue == "12") { state.action = "Slide" }
	if(state.pushedValue == "13" || state.pushedValue == "14"  || state.pushedValue == "15" || state.pushedValue == "16" || state.pushedValue == "17" || state.pushedValue == "18") { state.action = "Knock" }
	if(state.pushedValue == "19" || state.pushedValue == "20"  || state.pushedValue == "21" || state.pushedValue == "22" || state.pushedValue == "23" || state.pushedValue == "24") { state.action = "Rotate Right" }
	if(state.pushedValue == "25" || state.pushedValue == "26"  || state.pushedValue == "27" || state.pushedValue == "28" || state.pushedValue == "29" || state.pushedValue == "30") { state.action = "Rotate Left" }
	if(state.pushedValue == "31" || state.pushedValue == "32"  || state.pushedValue == "33" || state.pushedValue == "34" || state.pushedValue == "35" || state.pushedValue == "36") { state.action = "Shake" }
	
	magicHappensHandler()
}

def magicHappensHandler() {
	if(logEnable) log.debug "In magicHappensHandler..."
	log.info("Cube: ${xCube}, Button Pushed: ${state.pushedValue} (Face: ${state.face} - ${state.action} - Angle: ${state.angleValue})") 
 		if(pauseApp == false){
			if((state.pushedValue == "1") && (f0FlipToOn)) { switchesOn = f0FlipToOn }
			if((state.pushedValue == "1") && (f0FlipToOff)) { switchesOff = f0FlipToOff }
			if((state.pushedValue == "1") && (f0FlipToToggle)) { switchesToggle = f0FlipToToggle }
			if((state.pushedValue == "1") && (f0FlipTosetOnLC)) { setOnLC = f0FlipTosetOnLC; colorLC = f0FlipTocolorLC; levelLC = f0FlipTolevelLC }
			if((state.pushedValue == "1") && (f0FlipTonewMode)) { newMode = f0FlipTonewMode }
            if((state.pushedValue == "1") && (f0FliptonewVolumeUp)) {setVolup = f0FlipTonewVolumeUp}
            if((state.pushedValue == "1") && (f0FliptonewVolumeDown)) {setVoldown = f0FlipTonewVolumeDown}
            if((state.pushedValue == "1") && (f0FlipTonewMute)) {setVolmute = f0FlipTonewMute}            
            if((state.pushedValue == "1") && (f0FliptonewChannelUp)) {setChannelup = f0FlipTonewChannelUp}
            if((state.pushedValue == "1") && (f0FliptonewChannelDown)) {setChanneldown = f0FlipTonewChannelDown}
			if((state.pushedValue == "1") && (f0FlipToRules)) { rulesToRun = f0FlipToRules }
			
			if((state.pushedValue == "2") && (f1FlipToOn)) { switchesOn = f1FlipToOn }
			if((state.pushedValue == "2") && (f1FlipToOff)) { switchesOff = f1FlipToOff }
			if((state.pushedValue == "2") && (f1FlipToToggle)) { switchesToggle = f1FlipToToggle }
			if((state.pushedValue == "2") && (f1FlipTosetOnLC)) { setOnLC = f1FlipTosetOnLC; colorLC = f1FlipTocolorLC; levelLC = f1FlipTolevelLC }
			if((state.pushedValue == "2") && (f1FlipTonewMode)) { newMode = f1FlipTonewMode }
            if((state.pushedValue == "2") && (f1FliptonewVolumeUp)) {setVolup = f1FlipTonewVolumeUp}
            if((state.pushedValue == "2") && (f1FliptonewVolumeDown)) {setVoldown = f1FlipTonewVolumeDown}
            if((state.pushedValue == "2") && (f1FlipTonewMute)) {setVolmute = f1FlipTonewMute}
            if((state.pushedValue == "2") && (f1FliptonewChannelUp)) {setChannelup = f1FlipTonewChannelUp}
            if((state.pushedValue == "2") && (f1FliptonewChannelDown)) {setChanneldown = f1FlipTonewChannelDown}            
			if((state.pushedValue == "2") && (f1FlipToRules)) { rulesToRun = f1FlipToRules }
			
			if((state.pushedValue == "3") && (f2FlipToOn)) { switchesOn = f2FlipToOn }
			if((state.pushedValue == "3") && (f2FlipToOff)) { switchesOff = f2FlipToOff }
			if((state.pushedValue == "3") && (f2FlipToToggle)) { switchesToggle = f2FlipToToggle }
			if((state.pushedValue == "3") && (f2FlipTosetOnLC)) { setOnLC = f2FlipTosetOnLC; colorLC = f2FlipTocolorLC; levelLC = f2FlipTolevelLC }
			if((state.pushedValue == "3") && (f2FlipTonewMode)) { newMode = f2FlipTonewMode }
            if((state.pushedValue == "3") && (f2FliptonewVolumeUp)) {setVolup = f2FlipTonewVolumeUp}
            if((state.pushedValue == "3") && (f2FliptonewVolumeDown)) {setVoldown = f2FlipTonewVolumeDown}
            if((state.pushedValue == "3") && (f2FlipTonewMute)) {setVolmute = f2FlipTonewMute}
            if((state.pushedValue == "3") && (f2FliptonewChannelUp)) {setChannelup = f2FlipTonewChannelUp}
            if((state.pushedValue == "3") && (f2FliptonewChannelDown)) {setChanneldown = f2FlipTonewChannelDown}            
			if((state.pushedValue == "3") && (f2FlipToRules)) { rulesToRun = f2FlipToRules }
			
			if((state.pushedValue == "4") && (f3FlipToOn)) { switchesOn = f3FlipToOn }
			if((state.pushedValue == "4") && (f3FlipToOff)) { switchesOff = f3FlipToOff }	
			if((state.pushedValue == "4") && (f3FlipToToggle)) { switchesToggle = f3FlipToToggle }
			if((state.pushedValue == "4") && (f3FlipTosetOnLC)) { setOnLC = f3FlipTosetOnLC; colorLC = f3FlipTocolorLC; levelLC = f3FlipTolevelLC }
			if((state.pushedValue == "4") && (f3FlipTonewMode)) { newMode = f3FlipTonewMode }
            if((state.pushedValue == "4") && (f3FliptonewVolumeUp)) {setVolup = f3FlipTonewVolumeUp}
            if((state.pushedValue == "4") && (f3FliptonewVolumeDown)) {setVoldown = f3FlipTonewVolumeDown}
            if((state.pushedValue == "4") && (f3FlipTonewMute)) {setVolmute = f3FlipTonewMute}
            if((state.pushedValue == "4") && (f3FliptonewChannelUp)) {setChannelup = f3FlipTonewChannelUp}
            if((state.pushedValue == "4") && (f3FliptonewChannelDown)) {setChanneldown = f3FlipTonewChannelDown}            
			if((state.pushedValue == "4") && (f3FlipToRules)) { rulesToRun = f3FlipToRules }
			
			if((state.pushedValue == "5") && (f4FlipToOn)) { switchesOn = f4FlipToOn }
			if((state.pushedValue == "5") && (f4FlipToOff)) { switchesOff = f4FlipToOff }		
			if((state.pushedValue == "5") && (f4FlipToToggle)) { switchesToggle = f4FlipToToggle }
			if((state.pushedValue == "5") && (f4FlipTosetOnLC)) { setOnLC = f4FlipTosetOnLC; colorLC = f4FlipTocolorLC; levelLC = f4FlipTolevelLC }
			if((state.pushedValue == "5") && (f4FlipTonewMode)) { newMode = f4FlipTonewMode }
            if((state.pushedValue == "5") && (f4FliptonewVolumeUp)) {setVolup = f4FlipTonewVolumeUp}
            if((state.pushedValue == "5") && (f4FliptonewVolumeDown)) {setVoldown = f4FlipTonewVolumeDown}
            if((state.pushedValue == "5") && (f4FlipTonewMute)) {setVolmute = f4FlipTonewMute}
            if((state.pushedValue == "5") && (f4FliptonewChannelUp)) {setChannelup = f4FlipTonewChannelUp}
            if((state.pushedValue == "5") && (f4FliptonewChannelDown)) {setChanneldown = f4FlipTonewChannelDown}            
			if((state.pushedValue == "5") && (f4FlipToRules)) { rulesToRun = f4FlipToRules }
			
			if((state.pushedValue == "6") && (f5FlipToOn)) { switchesOn = f5FlipToOn }
			if((state.pushedValue == "6") && (f5FlipToOff)) { switchesOff = f5FlipToOff }	
			if((state.pushedValue == "6") && (f5FlipToToggle)) { switchesToggle = f5FlipToToggle }
			if((state.pushedValue == "6") && (f5FlipTosetOnLC)) { setOnLC = f5FlipTosetOnLC; colorLC = f5FlipTocolorLC; levelLC = f5FlipTolevelLC }
			if((state.pushedValue == "6") && (f5FlipTonewMode)) { newMode = f5FlipTonewMode }
            if((state.pushedValue == "6") && (f5FliptonewVolumeUp)) {setVolup = f5FlipTonewVolumeUp}
            if((state.pushedValue == "6") && (f5FliptonewVolumeDown)) {setVoldown = f5FlipTonewVolumeDown}
            if((state.pushedValue == "6") && (f5FlipTonewMute)) {setVolmute = f5FlipTonewMute}
            if((state.pushedValue == "6") && (f5FliptonewChannelUp)) {setChannelup = f5FlipTonewChannelUp}
            if((state.pushedValue == "6") && (f5FliptonewChannelDown)) {setChanneldown = f5FlipTonewChannelDown}            
			if((state.pushedValue == "6") && (f5FlipToRules)) { rulesToRun = f5FlipToRules }
											
			if((state.pushedValue == "7") && (f0SlideOn)) { switchesOn = f0SlideOn }
			if((state.pushedValue == "7") && (f0SlideOff)) { switchesOff = f0SlideOff }
			if((state.pushedValue == "7") && (f0SlideToggle)) { switchesToggle = f0SlideToggle }
			if((state.pushedValue == "7") && (f0SlidesetOnLC)) { setOnLC = f0SlidesetOnLC; colorLC = f0SlidecolorLC; levelLC = f0SlidelevelLC }
			if((state.pushedValue == "7") && (f0SlidenewMode)) { newMode = f0SlidenewMode }
            if((state.pushedValue == "7") && (f0SlidenewVolumeUp)) {setVolup = f0SlidenewVolumeUp}
            if((state.pushedValue == "7") && (f0SlidenewVolumeDown)) {setVoldown = f0SlidenewVolumeDown}
            if((state.pushedValue == "7") && (f0SlidenewMute)) {setVolmute = f0SlidenewMute}
            if((state.pushedValue == "7") && (f0SlidenewChannelUp)) {setChannelup = f0SlidenewChannelUp}
            if((state.pushedValue == "7") && (f0SlidenewChannelDown)) {setChanneldown = f0SlidenewChannelDown}             
			if((state.pushedValue == "7") && (f0SlideRules)) { rulesToRun = f0SlideRules }
			
			if((state.pushedValue == "8") && (f1SlideOn)) { switchesOn = f1SlideOn }
			if((state.pushedValue == "8") && (f1SlideOff)) { switchesOff = f1SlideOff }
			if((state.pushedValue == "8") && (f1SlideToggle)) { switchesToggle = f1SlideToggle }
			if((state.pushedValue == "8") && (f1SlidesetOnLC)) { setOnLC = f1SlidesetOnLC; colorLC = f1SlidecolorLC; levelLC = f1SlidelevelLC }
			if((state.pushedValue == "8") && (f1SlidenewMode)) { newMode = f1SlidenewMode }
            if((state.pushedValue == "8") && (f1SlidenewVolumeUp)) {setVolup = f1SlidenewVolumeUp}
            if((state.pushedValue == "8") && (f1SlidenewVolumeDown)) {setVoldown = f1SlidenewVolumeDown}
            if((state.pushedValue == "8") && (f1SlidenewMute)) {setVolmute = f1SlidenewMute}
            if((state.pushedValue == "8") && (f1SlidenewChannelUp)) {setChannelup = f1SlidenewChannelUp}
            if((state.pushedValue == "8") && (f1SlidenewChannelDown)) {setChanneldown = f1SlidenewChannelDown}            
			if((state.pushedValue == "8") && (f1SlideRules)) { rulesToRun = f1SlideRules }
			if((state.pushedValue == "9") && (f2SlideOn)) { switchesOn = f2SlideOn }
			if((state.pushedValue == "9") && (f2SlideOff)) { switchesOff = f2SlideOff }
			if((state.pushedValue == "9") && (f2SlideToggle)) { switchesToggle = f2SlideToggle }
			if((state.pushedValue == "9") && (f2SlidesetOnLC)) { setOnLC = f2SlidesetOnLC; colorLC = f2SlidecolorLC; levelLC = f2SlidelevelLC }
			if((state.pushedValue == "9") && (f2SlidenewMode)) { newMode = f2SlidenewMode }
            if((state.pushedValue == "9") && (f2SlidenewVolumeUp)) {setVolup = f2SlidenewVolumeUp}
            if((state.pushedValue == "9") && (f2SlidenewVolumeDown)) {setVoldown = f2SlidenewVolumeDown}
            if((state.pushedValue == "9") && (f2SlidenewMute)) {setVolmute = f2SlidenewMute}
            if((state.pushedValue == "9") && (f2SlidenewChannelUp)) {setChannelup = f2SlidenewChannelUp}
            if((state.pushedValue == "9") && (f2SlidenewChannelDown)) {setChanneldown = f2SlidenewChannelDown}            
			if((state.pushedValue == "9") && (f2SlideRules)) { rulesToRun = f2SlideRules }
			
			if((state.pushedValue == "10") && (f3SlideOn)) { switchesOn = f3SlideOn }
			if((state.pushedValue == "10") && (f3SlideOff)) { switchesOff = f3SlideOff }
			if((state.pushedValue == "10") && (f3SlideToggle)) { switchesToggle = f3SlideToggle }
			if((state.pushedValue == "10") && (f3SlidesetOnLC)) { setOnLC = f3SlidesetOnLC; colorLC = f3SlidecolorLC; levelLC = f3SlidelevelLC }
			if((state.pushedValue == "10") && (f3SlidenewMode)) { newMode = f3SlidenewMode }
            if((state.pushedValue == "10") && (f3SlidenewVolumeUp)) {setVolup = f3SlidenewVolumeUp}
            if((state.pushedValue == "10") && (f3SlidenewVolumeDown)) {setVoldown = f3SlidenewVolumeDown}
            if((state.pushedValue == "10") && (f3SlidenewMute)) {setVolmute = f3SlidenewMute}
            if((state.pushedValue == "10") && (f3SlidenewChannelUp)) {setChannelup = f3SlidenewChannelUp}
            if((state.pushedValue == "10") && (f3SlidenewChannelDown)) {setChanneldown = f3SlidenewChannelDown}            
			if((state.pushedValue == "10") && (f3SlideRules)) { rulesToRun = f3SlideRules }
			
			if((state.pushedValue == "11") && (f4SlideOn)) { switchesOn = f4SlideOn }
			if((state.pushedValue == "11") && (f4SlideOff)) { switchesOff = f4SlideOff }
			if((state.pushedValue == "11") && (f4SlideToggle)) { switchesToggle = f4SlideToggle }
			if((state.pushedValue == "11") && (f4SlidesetOnLC)) { setOnLC = f4SlidesetOnLC; colorLC = f4SlidecolorLC; levelLC = f4SlidelevelLC }
			if((state.pushedValue == "11") && (f4SlidenewMode)) { newMode = f4SlidenewMode }
            if((state.pushedValue == "11") && (f4SlidenewVolumeUp)) {setVolup = f4SlidenewVolumeUp}
            if((state.pushedValue == "11") && (f4SlidenewVolumeDown)) {setVoldown = f4SlidenewVolumeDown}
            if((state.pushedValue == "11") && (f4SlidenewMute)) {setVolmute = f4SlidenewMute}
            if((state.pushedValue == "11") && (f4SlidenewChannelUp)) {setChannelup = f4SlidenewChannelUp}
            if((state.pushedValue == "11") && (f4SlidenewChannelDown)) {setChanneldown = f4SlidenewChannelDown}            
			if((state.pushedValue == "11") && (f4SlideRules)) { rulesToRun = f4SlideRules }
			
			if((state.pushedValue == "12") && (f5SlideOn)) { switchesOn = f5SlideOn }
			if((state.pushedValue == "12") && (f5SlideOff)) { switchesOff = f5SlideOff }	
			if((state.pushedValue == "12") && (f5SlideToggle)) { switchesToggle = f5SlideToggle }
			if((state.pushedValue == "12") && (f5SlidesetOnLC)) { setOnLC = f5SlidesetOnLC; colorLC = f5SlidecolorLC; levelLC = f5SlidelevelLC }
			if((state.pushedValue == "12") && (f5SlidenewMode)) { newMode = f5SlidenewMode }
            if((state.pushedValue == "12") && (f5SlidenewVolumeUp)) {setVolup = f5SlidenewVolumeUp}
            if((state.pushedValue == "12") && (f5SlidenewVolumeDown)) {setVoldown = f5SlidenewVolumeDown}
            if((state.pushedValue == "12") && (f5SlidenewMute)) {setVolmute = f5SlidenewMute}
            if((state.pushedValue == "12") && (f5SlidenewChannelUp)) {setChannelup = f5SlidenewChannelUp}
            if((state.pushedValue == "12") && (f5SlidenewChannelDown)) {setChanneldown = f5SlidenewChannelDown}            
			if((state.pushedValue == "12") && (f5SlideRules)) { rulesToRun = f5SlideRules }
			
			if((state.pushedValue == "13") && (f0KnockOn)) { switchesOn = f0KnockOn }
			if((state.pushedValue == "13") && (f0KnockOff)) { switchesOff = f0KnockOff }
			if((state.pushedValue == "13") && (f0KnockToggle)) { switchesToggle = f0KnockToggle }
			if((state.pushedValue == "13") && (f0KnocksetOnLC)) { setOnLC = f0KnocksetOnLC; colorLC = f0KnockcolorLC; levelLC = f0KnocklevelLC }
			if((state.pushedValue == "13") && (f0KnocknewMode)) { newMode = f0KnocknewMode }
            if((state.pushedValue == "13") && (f0KnocknewVolumeUp)) {setVolup = f0KnocknewVolumeUp}
            if((state.pushedValue == "13") && (f0KnocknewVolumeDown)) {setVoldown = f0KnocknewVolumeDown}
            if((state.pushedValue == "13") && (f0KnocknewMute)) {setVolmute = f0KnocknewMute}
            if((state.pushedValue == "13") && (f0KnocknewChannelUp)) {setChannelup = f0KnocknewChannelUp}
            if((state.pushedValue == "13") && (f0KnocknewChannelDown)) {setChanneldown = f0KnocknewChannelDown}            
            
			if((state.pushedValue == "13") && (f0KnockRules)) { rulesToRun = f0KnockRules }
			
			if((state.pushedValue == "14") && (f1KnockOn)) { switchesOn = f1KnockOn }
			if((state.pushedValue == "14") && (f1KnockOff)) { switchesOff = f1KnockOff }
			if((state.pushedValue == "14") && (f1KnockToggle)) { switchesToggle = f1KnockToggle }
			if((state.pushedValue == "14") && (f1KnocksetOnLC)) { setOnLC = f1KnocksetOnLC; colorLC = f1KnockcolorLC; levelLC = f1KnocklevelLC }
			if((state.pushedValue == "14") && (f1KnocknewMode)) { newMode = f1KnocknewMode }
            if((state.pushedValue == "14") && (f1KnocknewVolumeUp)) {setVolup = f1KnocknewVolumeUp}
            if((state.pushedValue == "14") && (f1KnocknewVolumeDown)) {setVoldown = f1KnocknewVolumeDown}
            if((state.pushedValue == "14") && (f1KnocknewMute)) {setVolmute = f1KnocknewMute}
            if((state.pushedValue == "14") && (f1KnocknewChannelUp)) {setChannelup = f1KnocknewChannelUp}
            if((state.pushedValue == "14") && (f1KnocknewChannelDown)) {setChanneldown = f1KnocknewChannelDown}             
			if((state.pushedValue == "14") && (f1KnockRules)) { rulesToRun = f1KnockRules }
			
			if((state.pushedValue == "15") && (f2KnockOn)) { switchesOn = f2KnockOn }
			if((state.pushedValue == "15") && (f2KnockOff)) { switchesOff = f2KnockOff }
			if((state.pushedValue == "15") && (f2KnockToggle)) { switchesToggle = f2KnockToggle }
			if((state.pushedValue == "15") && (f2KnocksetOnLC)) { setOnLC = f2KnocksetOnLC; colorLC = f2KnockcolorLC; levelLC = f2KnocklevelLC }
			if((state.pushedValue == "15") && (f2KnocknewMode)) { newMode = f2KnocknewMode }
            if((state.pushedValue == "15") && (f2KnocknewVolumeUp)) {setVolup = f2KnocknewVolumeUp}
            if((state.pushedValue == "15") && (f2KnocknewVolumeDown)) {setVoldown = f2KnocknewVolumeDown}
            if((state.pushedValue == "15") && (f2KnocknewMute)) {setVolmute = f2KnocknewMute}
            if((state.pushedValue == "15") && (f2KnocknewChannelUp)) {setChannelup = f2KnocknewChannelUp}
            if((state.pushedValue == "15") && (f2KnocknewChannelDown)) {setChanneldown = f2KnocknewChannelDown}             
			if((state.pushedValue == "15") && (f2KnockRules)) { rulesToRun = f2KnockRules }
			
			if((state.pushedValue == "16") && (f3KnockOn)) { switchesOn = f3KnockOn }
			if((state.pushedValue == "16") && (f3KnockOff)) { switchesOff = f3KnockOff }	
			if((state.pushedValue == "16") && (f3KnockToggle)) { switchesToggle = f3KnockToggle }
			if((state.pushedValue == "16") && (f3KnocksetOnLC)) { setOnLC = f3KnocksetOnLC; colorLC = f3KnockcolorLC; levelLC = f3KnocklevelLC }
			if((state.pushedValue == "16") && (f3KnocknewMode)) { newMode = f3KnocknewMode }
            if((state.pushedValue == "16") && (f3KnocknewVolumeUp)) {setVolup = f3KnocknewVolumeUp}
            if((state.pushedValue == "16") && (f3KnocknewVolumeDown)) {setVoldown = f3KnocknewVolumeDown}
            if((state.pushedValue == "16") && (f3KnocknewMute)) {setVolmute = f3KnocknewMute}
            if((state.pushedValue == "16") && (f3KnocknewChannelUp)) {setChannelup = f3KnocknewChannelUp}
            if((state.pushedValue == "16") && (f3KnocknewChannelDown)) {setChanneldown = f3KnocknewChannelDown}             
			if((state.pushedValue == "16") && (f3KnockRules)) { rulesToRun = f3KnockRules }
			
			if((state.pushedValue == "17") && (f4KnockOn)) { switchesOn = f4KnockOn }
			if((state.pushedValue == "17") && (f4KnockOff)) { switchesOff = f4KnockOff }	
			if((state.pushedValue == "17") && (f4KnockToggle)) { switchesToggle = f4KnockToggle }
			if((state.pushedValue == "17") && (f4KnocksetOnLC)) { setOnLC = f4KnocksetOnLC; colorLC = f4KnockcolorLC; levelLC = f4KnocklevelLC }
			if((state.pushedValue == "17") && (f4KnocknewMode)) { newMode = f4KnocknewMode }
            if((state.pushedValue == "17") && (f4KnocknewVolumeUp)) {setVolup = f4KnocknewVolumeUp}
            if((state.pushedValue == "17") && (f4KnocknewVolumeDown)) {setVoldown = f4KnocknewVolumeDown}
            if((state.pushedValue == "17") && (f4KnocknewMute)) {setVolmute = f4KnocknewMute}
            if((state.pushedValue == "17") && (f4KnocknewChannelUp)) {setChannelup = f4KnocknewChannelUp}
            if((state.pushedValue == "17") && (f4KnocknewChannelDown)) {setChanneldown = f4KnocknewChannelDown}             
			if((state.pushedValue == "17") && (f4KnockRules)) { rulesToRun = f4KnockRules }
			
			if((state.pushedValue == "18") && (f5KnockOn)) { switchesOn = f5KnockOn }
			if((state.pushedValue == "18") && (f5KnockOff)) { switchesOff = f5KnockOff }	
			if((state.pushedValue == "18") && (f5KnockToggle)) { switchesToggle = f5KnockToggle }
			if((state.pushedValue == "18") && (f5KnocksetOnLC)) { setOnLC = f5KnocksetOnLC; colorLC = f5KnockcolorLC; levelLC = f5KnocklevelLC }
			if((state.pushedValue == "18") && (f5KnocknewMode)) { newMode = f5KnocknewMode }
            if((state.pushedValue == "18") && (f5KnocknewVolumeUp)) {setVolup = f5KnocknewVolumeUp}
            if((state.pushedValue == "18") && (f5KnocknewVolumeDown)) {setVoldown = f5KnocknewVolumeDown}
            if((state.pushedValue == "18") && (f5KnocknewMute)) {setVolmute = f5KnocknewMute}
            if((state.pushedValue == "18") && (f5KnocknewChannelUp)) {setChannelup = f5KnocknewChannelUp}
            if((state.pushedValue == "18") && (f5KnocknewChannelDown)) {setChanneldown = f5KnocknewChannelDown}             
			if((state.pushedValue == "18") && (f5KnockRules)) { rulesToRun = f5KnockRules }
							
			if((state.pushedValue == "19") && (f0rRightOn)) { switchesOn = f0rRightOn }
			if((state.pushedValue == "19") && (f0rRightOff)) { switchesOff = f0rRightOff }
			if((state.pushedValue == "19") && (f0rRightToggle)) { switchesToggle = f0rRightToggle }
			if((state.pushedValue == "19") && (f0rRightsetOnLC)) { setOnLC = f0rRightsetOnLC; colorLC = f0rRightcolorLC; levelLC = f0rRightlevelLC }
			if((state.pushedValue == "19") && (f0rRightnewMode)) { newMode = f0rRightnewMode }
            if((state.pushedValue == "19") && (f0rRightnewVolumeUp)) {setVolup = f0rRightnewVolumeUp}
            if((state.pushedValue == "19") && (f0rRightnewVolumeDown)) {setVoldown = f0rRightnewVolumeDown}
            if((state.pushedValue == "19") && (f0rRightnewMute)) {setVolmute = f0rRightnewMute}
            if((state.pushedValue == "19") && (f0rRightnewChannelUp)) {setChannelup = f0rRightnewChannelUp}
            if((state.pushedValue == "19") && (f0rRightnewChannelDown)) {setChanneldown = f0rRightnewChannelDown}             
			if((state.pushedValue == "19") && (f0RightRules)) { rulesToRun = f0RightRules }
			
			if((state.pushedValue == "20") && (f1rRightOn)) { switchesOn = f1rRightOn }
			if((state.pushedValue == "20") && (f1rRightOff)) { switchesOff = f1rRightOff }
			if((state.pushedValue == "20") && (f1rRightToggle)) { switchesToggle = f1rRightToggle }
			if((state.pushedValue == "20") && (f1rRightsetOnLC)) { setOnLC = f1rRightsetOnLC; colorLC = f1rRightcolorLC; levelLC = f1rRightlevelLC }
			if((state.pushedValue == "20") && (f1rRightnewMode)) { newMode = f1rRightnewMode }
            if((state.pushedValue == "20") && (f1rRightnewVolumeUp)) {setVolup = f1rRightnewVolumeUp}
            if((state.pushedValue == "20") && (f1rRightnewVolumeDown)) {setVoldown = f1rRightnewVolumeDown}
            if((state.pushedValue == "20") && (f1rRightnewMute)) {setVolmute = f1rRightnewMute}
            if((state.pushedValue == "20") && (f1rRightnewChannelUp)) {setChannelup = f1rRightnewChannelUp}
            if((state.pushedValue == "20") && (f1rRightnewChannelDown)) {setChanneldown = f1rRightnewChannelDown}              
			if((state.pushedValue == "20") && (f1RightRules)) { rulesToRun = f1RightRules }
			
			if((state.pushedValue == "21") && (f2rRightOn)) { switchesOn = f2rRightOn }
			if((state.pushedValue == "21") && (f2rRightOff)) { switchesOff = f2rRightOff }	
			if((state.pushedValue == "21") && (f2rRightToggle)) { switchesToggle = f2rRightToggle }
			if((state.pushedValue == "21") && (f2rRightsetOnLC)) { setOnLC = f2rRightsetOnLC; colorLC = f2rRightcolorLC; levelLC = f2rRightlevelLC }
			if((state.pushedValue == "21") && (f2rRightnewMode)) { newMode = f2rRightnewMode }
            if((state.pushedValue == "21") && (f2rRightnewVolumeUp)) {setVolup = f2rRightnewVolumeUp}
            if((state.pushedValue == "21") && (f2rRightnewVolumeDown)) {setVoldown = f2rRightnewVolumeDown}
            if((state.pushedValue == "21") && (f2rRightnewMute)) {setVolmute = f2rRightnewMute}
            if((state.pushedValue == "21") && (f2rRightnewChannelUp)) {setChannelup = f2rRightnewChannelUp}
            if((state.pushedValue == "21") && (f2rRightnewChannelDown)) {setChanneldown = f2rRightnewChannelDown}               
			if((state.pushedValue == "21") && (f2RightRules)) { rulesToRun = f2RightRules }
			
			if((state.pushedValue == "22") && (f3rRightOn)) { switchesOn = f3rRightOn }
			if((state.pushedValue == "22") && (f3rRightOff)) { switchesOff = f3rRightOff }	
			if((state.pushedValue == "22") && (f3rRightToggle)) { switchesToggle = f3rRightToggle }
			if((state.pushedValue == "22") && (f3rRightsetOnLC)) { setOnLC = f3rRightsetOnLC; colorLC = f3rRightcolorLC; levelLC = f3rRightlevelLC }
			if((state.pushedValue == "22") && (f3rRightnewMode)) { newMode = f3rRightnewMode }
            if((state.pushedValue == "22") && (f3rRightnewVolumeUp)) {setVolup = f3rRightnewVolumeUp}
            if((state.pushedValue == "22") && (f3rRightnewVolumeDown)) {setVoldown = f3rRightnewVolumeDown}
            if((state.pushedValue == "22") && (f3rRightnewMute)) {setVolmute = f3rRightnewMute}
            if((state.pushedValue == "22") && (f3rRightnewChannelUp)) {setChannelup = f3rRightnewChannelUp}
            if((state.pushedValue == "22") && (f3rRightnewChannelDown)) {setChanneldown = f3rRightnewChannelDown}              
			if((state.pushedValue == "22") && (f3RightRules)) { rulesToRun = f3RightRules }
			
			if((state.pushedValue == "23") && (f4rRightOn)) { switchesOn = f4rRightOn }
			if((state.pushedValue == "23") && (f4rRightOff)) { switchesOff = f4rRightOff }	
			if((state.pushedValue == "23") && (f4rRightToggle)) { switchesToggle = f4rRightToggle }
			if((state.pushedValue == "23") && (f4rRightsetOnLC)) { setOnLC = f4rRightsetOnLC; colorLC = f4rRightcolorLC; levelLC = f4rRightlevelLC }
			if((state.pushedValue == "23") && (f4rRightnewMode)) { newMode = f4rRightnewMode }
            if((state.pushedValue == "23") && (f4rRightnewVolumeUp)) {setVolup = f4rRightnewVolumeUp}
            if((state.pushedValue == "23") && (f4rRightnewVolumeDown)) {setVoldown = f4rRightnewVolumeDown}
            if((state.pushedValue == "23") && (f4rRightnewMute)) {setVolmute = f4rRightnewMute}
            if((state.pushedValue == "23") && (f4rRightnewChannelUp)) {setChannelup = f4rRightnewChannelUp}
            if((state.pushedValue == "23") && (f4rRightnewChannelDown)) {setChanneldown = f4rRightnewChannelDown}             
			if((state.pushedValue == "23") && (f4RightRules)) { rulesToRun = f4RightRules }
			
			if((state.pushedValue == "24") && (f5rRightOn)) { switchesOn = f5rRightOn }
			if((state.pushedValue == "24") && (f5rRightOff)) { switchesOff = f5rRightOff }	
			if((state.pushedValue == "24") && (f5rRightToggle)) { switchesToggle = f5rRightToggle }
			if((state.pushedValue == "24") && (f5rRightsetOnLC)) { setOnLC = f5rRightsetOnLC; colorLC = f5rRightcolorLC; levelLC = f5rRightlevelLC }
			if((state.pushedValue == "24") && (f5rRightnewMode)) { newMode = f5rRightnewMode }
            if((state.pushedValue == "24") && (f5rRightnewVolumeUp)) {setVolup = f5rRightnewVolumeUp}
            if((state.pushedValue == "24") && (f5rRightnewVolumeDown)) {setVoldown = f5rRightnewVolumeDown}
            if((state.pushedValue == "24") && (f5rRightnewMute)) {setVolmute = f5rRightnewMute}
            if((state.pushedValue == "24") && (f5rRightnewChannelUp)) {setChannelup = f5rRightnewChannelUp}
            if((state.pushedValue == "24") && (f5rRightnewChannelDown)) {setChanneldown = f5rRightnewChannelDown}             
			if((state.pushedValue == "24") && (f5RightRules)) { rulesToRun = f5RightRules }
											
			if((state.pushedValue == "25") && (f0rLeftOn)) { switchesOn = f0rLeftOn }
			if((state.pushedValue == "25") && (f0rLeftOff)) { switchesOff = f0rLeftOff }
			if((state.pushedValue == "25") && (f0rLeftToggle)) { switchesToggle = f0rLeftToggle }
			if((state.pushedValue == "25") && (f0rLeftsetOnLC)) { setOnLC = f0rLeftsetOnLC; colorLC = f0rLeftcolorLC; levelLC = f0rLeftlevelLC }
			if((state.pushedValue == "25") && (f0rLeftnewMode)) { newMode = f0rLeftnewMode }
            if((state.pushedValue == "25") && (f0rLeftnewVolumeUp)) {setVolup = f0rLeftnewVolumeUp}
            if((state.pushedValue == "25") && (f0rLeftnewVolumeDown)) {setVoldown = f0rLeftnewVolumeDown}
            if((state.pushedValue == "25") && (f0rLeftnewMute)) {setVolmute = f0rLeftnewMute}
            if((state.pushedValue == "25") && (f0rLeftnewChannelUp)) {setChannelup = f0rLeftnewChannelUp}
            if((state.pushedValue == "25") && (f0rLeftnewChannelDown)) {setChanneldown = f0rLeftnewChannelDown}             
			if((state.pushedValue == "25") && (f0LeftRules)) { rulesToRun = f0LeftRules }
			
			if((state.pushedValue == "26") && (f1rLeftOn)) { switchesOn = f1rLeftOn }
			if((state.pushedValue == "26") && (f1rLeftOff)) { switchesOff = f1rLeftOff }
			if((state.pushedValue == "26") && (f1rLeftToggle)) { switchesToggle = f1rLeftToggle }
			if((state.pushedValue == "26") && (f1rLeftsetOnLC)) { setOnLC = f1rLeftsetOnLC; colorLC = f1rLeftcolorLC; levelLC = f1rLeftlevelLC }
			if((state.pushedValue == "26") && (f1rLeftnewMode)) { newMode = f1rLeftnewMode }
            if((state.pushedValue == "26") && (f1rLeftnewVolumeUp)) {setVolup = f1rLeftnewVolumeUp}
            if((state.pushedValue == "26") && (f1rLeftnewVolumeDown)) {setVoldown = f1rLeftnewVolumeDown}
            if((state.pushedValue == "26") && (f1rLeftnewMute)) {setVolmute = f1rLeftnewMute}
            if((state.pushedValue == "26") && (f1rLeftnewChannelUp)) {setChannelup = f1rLeftnewChannelUp}
            if((state.pushedValue == "26") && (f1rLeftnewChannelDown)) {setChanneldown = f1rLeftnewChannelDown}            
			if((state.pushedValue == "26") && (f1LeftRules)) { rulesToRun = f1LeftRules }
			
			if((state.pushedValue == "27") && (f2rLeftOn)) { switchesOn = f2rLeftOn }
			if((state.pushedValue == "27") && (f2rLeftOff)) { switchesOff = f2rLeftOff }	
			if((state.pushedValue == "27") && (f2rLeftToggle)) { switchesToggle = f2rLeftToggle }
			if((state.pushedValue == "27") && (f2rLeftsetOnLC)) { setOnLC = f2rLeftsetOnLC; colorLC = f2rLeftcolorLC; levelLC = f2rLeftlevelLC }
			if((state.pushedValue == "27") && (f2rLeftnewMode)) { newMode = f2rLeftnewMode }
            if((state.pushedValue == "27") && (f2rLeftnewVolumeUp)) {setVolup = f2rLeftnewVolumeUp}
            if((state.pushedValue == "27") && (f2rLeftnewVolumeDown)) {setVoldown = f2rLeftnewVolumeDown}
            if((state.pushedValue == "27") && (f2rLeftnewMute)) {setVolmute = f2rLeftnewMute}
            if((state.pushedValue == "27") && (f2rLeftnewChannelUp)) {setChannelup = f2rLeftnewChannelUp}
            if((state.pushedValue == "27") && (f2rLeftnewChannelDown)) {setChanneldown = f2rLeftnewChannelDown}            
			if((state.pushedValue == "27") && (f2LeftRules)) { rulesToRun = f2LeftRules }
			
			if((state.pushedValue == "28") && (f3rLeftOn)) { switchesOn = f3rLeftOn }
			if((state.pushedValue == "28") && (f3rLeftOff)) { switchesOff = f3rLeftOff }	
			if((state.pushedValue == "28") && (f3rLeftToggle)) { switchesToggle = f3rLeftToggle }
			if((state.pushedValue == "28") && (f3rLeftsetOnLC)) { setOnLC = f3rLeftsetOnLC; colorLC = f3rLeftcolorLC; levelLC = f3rLeftlevelLC }
			if((state.pushedValue == "28") && (f3rLeftnewMode)) { newMode = f3rLeftnewMode }
            if((state.pushedValue == "28") && (f3rLeftnewVolumeUp)) {setVolup = f3rLeftnewVolumeUp}
            if((state.pushedValue == "28") && (f3rLeftnewVolumeDown)) {setVoldown = f3rLeftnewVolumeDown}
            if((state.pushedValue == "28") && (f3rLeftnewMute)) {setVolmute = f3rLeftnewMute}
            if((state.pushedValue == "28") && (f3rLeftnewChannelUp)) {setChannelup = f3rLeftnewChannelUp}
            if((state.pushedValue == "28") && (f3rLeftnewChannelDown)) {setChanneldown = f3rLeftnewChannelDown}            
			if((state.pushedValue == "28") && (f3LeftRules)) { rulesToRun = f3LeftRules }
			
			if((state.pushedValue == "29") && (f4rLeftOn)) { switchesOn = f4rLeftOn }
			if((state.pushedValue == "29") && (f4rLeftOff)) { switchesOff = f4rLeftoff }
			if((state.pushedValue == "29") && (f4rLeftToggle)) { switchesToggle = f4rLeftToggle }
			if((state.pushedValue == "29") && (f4rLeftsetOnLC)) { setOnLC = f4rLeftsetOnLC; colorLC = f4rLeftcolorLC; levelLC = f4rLeftlevelLC }
			if((state.pushedValue == "29") && (f4rLeftnewMode)) { newMode = f4rLeftnewMode }
            if((state.pushedValue == "29") && (f4rLeftnewVolumeUp)) {setVolup = f4rLeftnewVolumeUp}
            if((state.pushedValue == "29") && (f4rLeftnewVolumeDown)) {setVoldown = f4rLeftnewVolumeDown}
            if((state.pushedValue == "29") && (f4rLeftnewMute)) {setVolmute = f4rLeftnewMute}
            if((state.pushedValue == "29") && (f4rLeftnewChannelUp)) {setChannelup = f4rLeftnewChannelUp}
            if((state.pushedValue == "29") && (f4rLeftnewChannelDown)) {setChanneldown = f4rLeftnewChannelDown}            
			if((state.pushedValue == "29") && (f4LeftRules)) { rulesToRun = f4LeftRules }
			
			if((state.pushedValue == "30") && (f5rLeftOn)) { switchesOn = f5rLeftOn }
			if((state.pushedValue == "30") && (f5rLeftOff)) { switchesOff = f5rLeftOff }
			if((state.pushedValue == "30") && (f5rLeftToggle)) { switchesToggle = f5rLeftToggle }
			if((state.pushedValue == "30") && (f5rLeftsetOnLC)) { setOnLC = f5rLeftsetOnLC; colorLC = f5rLeftcolorLC; levelLC = f5rLeftlevelLC }
			if((state.pushedValue == "30") && (f5rLeftnewMode)) { newMode = f5rLeftnewMode }
            if((state.pushedValue == "30") && (f5rLeftnewVolumeUp)) {setVolup = f5rLeftnewVolumeUp}
            if((state.pushedValue == "30") && (f5rLeftnewVolumeDown)) {setVoldown = f5rLeftnewVolumeDown}
            if((state.pushedValue == "30") && (f5rLeftnewMute)) {setVolmute = f5rLeftnewMute}
            if((state.pushedValue == "30") && (f5rLeftnewChannelUp)) {setChannelup = f5rLeftnewChannelUp}
            if((state.pushedValue == "30") && (f5rLeftnewChannelDown)) {setChanneldown = f5rLeftnewChannelDown}            
			if((state.pushedValue == "30") && (f5LeftRules)) { rulesToRun = f5LeftRules }
							
			if((state.pushedValue == "31") && (f0ShakeOn)) { switchesOn = f0ShakeOn }
			if((state.pushedValue == "31") && (f0ShakeOff)) { switchesOff = f0ShakeOff }
			if((state.pushedValue == "31") && (f0ShakeToggle)) { switchesToggle = f0ShakeToggle }
			if((state.pushedValue == "31") && (f0ShakesetOnLC)) { setOnLC = f0ShakesetOnLC; colorLC = f0ShakecolorLC; levelLC = f0ShakelevelLC }
			if((state.pushedValue == "31") && (f0ShakenewMode)) { newMode = f0ShakenewMode }
            if((state.pushedValue == "31") && (f0ShakenewVolumeUp)) {setVolup = f0ShakenewVolumeUp}
            if((state.pushedValue == "31") && (f0ShakenewVolumeDown)) {setVoldown = f0ShakenewVolumeDown}
            if((state.pushedValue == "31") && (f0ShakenewMute)) {setVolmute = f0ShakenewMute}
            if((state.pushedValue == "31") && (f0ShakenewChannelUp)) {setChannelup = f0ShakenewChannelUp}
            if((state.pushedValue == "31") && (f0ShakenewChannelDown)) {setChanneldown = f0ShakenewChannelDown}               
			if((state.pushedValue == "31") && (f0ShakeRules)) { rulesToRun = f0ShakeRules }
			
			if((state.pushedValue == "32") && (f1ShakeOn)) { switchesOn = f1ShakeOn }
			if((state.pushedValue == "32") && (f1ShakeOff)) { switchesOff = f1ShakeOff }
			if((state.pushedValue == "32") && (f1ShakeToggle)) { switchesToggle = f1ShakeToggle }
			if((state.pushedValue == "32") && (f1ShakesetOnLC)) { setOnLC = f1ShakesetOnLC; colorLC = f1ShakecolorLC; levelLC = f1ShakelevelLC }
			if((state.pushedValue == "32") && (f1ShakenewMode)) { newMode = f1ShakenewMode }
            if((state.pushedValue == "32") && (f1ShakenewVolumeUp)) {setVolup = f1ShakenewVolumeUp}
            if((state.pushedValue == "32") && (f1ShakenewVolumeDown)) {setVoldown = f1ShakenewVolumeDown}
            if((state.pushedValue == "32") && (f1ShakenewMute)) {setVolmute = f1ShakenewMute}
            if((state.pushedValue == "32") && (f1ShakenewChannelUp)) {setChannelup = f1ShakenewChannelUp}
            if((state.pushedValue == "32") && (f1ShakenewChannelDown)) {setChanneldown = f1ShakenewChannelDown}             
			if((state.pushedValue == "32") && (f1ShakeRules)) { rulesToRun = f1ShakeRules }
			
			if((state.pushedValue == "33") && (f2ShakeOn)) { switchesOn = f2ShakeOn }
			if((state.pushedValue == "33") && (f2ShakeOff)) { switchesOff = f2ShakeOff }
			if((state.pushedValue == "33") && (f2ShakeToggle)) { switchesToggle = f2ShakeToggle }
			if((state.pushedValue == "33") && (f2ShakesetOnLC)) { setOnLC = f2ShakesetOnLC; colorLC = f2ShakecolorLC; levelLC = f2ShakelevelLC }
			if((state.pushedValue == "33") && (f2ShakenewMode)) { newMode = f2ShakenewMode }
            if((state.pushedValue == "33") && (f2ShakenewVolumeUp)) {setVolup = f2ShakenewVolumeUp}
            if((state.pushedValue == "33") && (f2ShakenewVolumeDown)) {setVoldown = f2ShakenewVolumeDown}
            if((state.pushedValue == "33") && (f2ShakenewMute)) {setVolmute = f2ShakenewMute}
            if((state.pushedValue == "33") && (f2ShakenewChannelUp)) {setChannelup = f2ShakenewChannelUp}
            if((state.pushedValue == "33") && (f2ShakenewChannelDown)) {setChanneldown = f2ShakenewChannelDown}             
			if((state.pushedValue == "33") && (f2ShakeRules)) { rulesToRun = f2ShakeRules }
			
			if((state.pushedValue == "34") && (f3ShakeOn)) { switchesOn = f3ShakeOn }
			if((state.pushedValue == "34") && (f3ShakeOff)) { switchesOff = f3ShakeOff }
			if((state.pushedValue == "34") && (f3ShakeToggle)) { switchesToggle = f3ShakeToggle }
			if((state.pushedValue == "34") && (f3ShakesetOnLC)) { setOnLC = f3ShakesetOnLC; colorLC = f3ShakecolorLC; levelLC = f3ShakelevelLC }
			if((state.pushedValue == "34") && (f3ShakenewMode)) { newMode = f3ShakenewMode }
            if((state.pushedValue == "34") && (f3ShakenewVolumeUp)) {setVolup = f3ShakenewVolumeUp}
            if((state.pushedValue == "34") && (f3ShakenewVolumeDown)) {setVoldown = f3ShakenewVolumeDown}
            if((state.pushedValue == "34") && (f3ShakenewMute)) {setVolmute = f3ShakenewMute}
            if((state.pushedValue == "34") && (f3ShakenewChannelUp)) {setChannelup = f3ShakenewChannelUp}
            if((state.pushedValue == "34") && (f3ShakenewChannelDown)) {setChanneldown = f3ShakenewChannelDown}             
			if((state.pushedValue == "34") && (f3ShakeRules)) { rulesToRun = f3ShakeRules }
			
			if((state.pushedValue == "35") && (f4ShakeOn)) { witchesOn = f4ShakeOn }
			if((state.pushedValue == "35") && (f4ShakeOff)) { switchesOff = f4ShakeOff }
			if((state.pushedValue == "35") && (f4ShakeToggle)) { switchesToggle = f4ShakeToggle }
			if((state.pushedValue == "35") && (f4ShakesetOnLC)) { setOnLC = f4ShakesetOnLC; colorLC = f4ShakecolorLC; levelLC = f4ShakelevelLC }
			if((state.pushedValue == "35") && (f4ShakenewMode)) { newMode = f4ShakenewMode }
            if((state.pushedValue == "35") && (f4ShakenewVolumeUp)) {setVolup = f4ShakenewVolumeUp}
            if((state.pushedValue == "35") && (f4ShakenewVolumeDown)) {setVoldown = f4ShakenewVolumeDown}
            if((state.pushedValue == "35") && (f4ShakenewMute)) {setVolmute = f4ShakenewMute}
            if((state.pushedValue == "35") && (f4ShakenewChannelUp)) {setChannelup = f4ShakenewChannelUp}
            if((state.pushedValue == "35") && (f4ShakenewChannelDown)) {setChanneldown = f4ShakenewChannelDown}             
			if((state.pushedValue == "35") && (f4ShakeRules)) { rulesToRun = f4ShakeRules }
			
			if((state.pushedValue == "36") && (f5ShakeOn)) { switchesOn = f5ShakeOn }
			if((state.pushedValue == "36") && (f5ShakeOff)) { switchesOff = f5ShakeOff }
			if((state.pushedValue == "36") && (f5ShakeToggle)) { switchesToggle = f5ShakeToggle }
			if((state.pushedValue == "36") && (f5ShakesetOnLC)) { setOnLC = f5ShakesetOnLC; colorLC = f5ShakecolorLC; levelLC = f5ShakelevelLC }
			if((state.pushedValue == "36") && (f5ShakenewMode)) { newMode = f5ShakenewMode }
            if((state.pushedValue == "36") && (f5ShakenewVolumeUp)) {setVolup = f5ShakenewVolumeUp}
            if((state.pushedValue == "36") && (f5ShakenewVolumeDown)) {setVoldown = f5ShakenewVolumeDown}
            if((state.pushedValue == "36") && (f5ShakenewMute)) {setVolmute = f5ShakenewMute}
            if((state.pushedValue == "36") && (f5ShakenewChannelUp)) {setChannelup = f5ShakenewChannelUp}
            if((state.pushedValue == "36") && (f5ShakenewChannelDown)) {setChanneldown = f5ShakenewChannelDown}             
			if((state.pushedValue == "36") && (f5ShakeRules)) { rulesToRun = f5ShakeRules }
							
			if(switchesOn) switchesOnHandler()
			if(switchesOff) switchesOffHandler()
			if(switchesToggle) switchesToggleHandler()
			if(setOnLC) dimmerOnHandler()
			if(newMode) modeHandler()
            if(setVolup) harmonyHandler("VolumeUp")
            if(setVoldown) harmonyHandler("VolumeDown")
            if(setChannelup) harmonyHandler("ChannelUp")
            if(setChanneldown) harmonyHandler("ChannelDown")
            if(setVolmute) harmonyHandler("Mute")
            if(rulesToRun) rulesHandler(rulesToRun)
		} else {
			log.info "${app.label} - Unable to continue - App paused"
		}
}

def rulesHandler(rules) {
    if(logEnable) log.debug "In rulesHandler - Running ${rules}"
    RMUtils.sendAction(rules, "runRule", app.label)
}

def switchesOnHandler() {
	switchesOn.each { it ->
		if(logEnable) log.debug "In switchOnHandler - Turning on ${it}"
		it.on()
	}
}

def switchesOffHandler() {
	switchesOff.each { it ->
		if(logEnable) log.debug "In switchOffHandler - Turning off ${it}"
		it.off()
	}
}

def switchesToggleHandler() {
	switchesToggle.each { it ->
		dStatus = it.currentValue("switch")
		if(logEnable) log.debug "In switchToggleHandler - Toggle ${it}, current status: ${dStatus}"
		if(dStatus == "on") it.off()
		if(dStatus == "off") it.on()
	}
}

def dimmerOnHandler() {
	if(logEnable) log.debug "In dimmerOnHandler..."
	state.fromWhere = "dimmerOn"
	state.color = "${colorLC}"
	state.onLevel = levelLC
	setLevelandColorHandler()
}

def setLevelandColorHandler() {
	if(logEnable) log.debug "In setLevelandColorHandler - fromWhere: ${state.fromWhere}, onLevel: ${state.onLevel}, color: ${state.color}"
    def hueColor = 0
    def saturation = 100
	int onLevel = state.onLevel
    switch(state.color) {
            case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }
	def value = [switch: "on", hue: hueColor, saturation: saturation, level: onLevel as Integer ?: 100]
    if(logEnable) log.debug "In setLevelandColorHandler - value: $value"
	if(state.fromWhere == "dimmerOn") {
    	setOnLC.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
	if(state.fromWhere == "slowOn") {
    	slowDimmerUp.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(onLevel as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
	if(state.fromWhere == "slowOff") {
    	slowDimmerDn.each {
        	if (it.hasCommand('setColor')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setColor($value)"
            	it.setColor(value)
        	} else if (it.hasCommand('setLevel')) {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, setLevel($value)"
            	it.setLevel(level as Integer ?: 100)
        	} else {
            	if(logEnable) log.debug "In setLevelandColorHandler - $it.displayName, on()"
            	it.on()
        	}
    	}
	}
}

def modeHandler() {
	if(logEnable) log.debug "In modeHandler - Changing mode to ${newMode}"
	setLocationMode(newMode)
}


def harmonyHandler(event) {
    if(logEnable) log.debug "In harmonyHandler - executing ${event}"
    switch(event) {
        case "VolumeUp":
            setVolup.volumeUp()
        break;
        case "VolumeDown":
            setVoldown.volumeDown()
        break;
        case "ChannelUp":
            setChannelup.channelUp()
        break;
        case "ChannelDown":
            setChanneldown.channelDown()
        break;
        case "Mute":
            setVolmute.mute()
        break;
    }   
}

// ***** Normal Stuff *****

def setDefaults(){
    if(pauseApp == null){pauseApp = false}
	if(logEnable == null){logEnable = false}
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Magic Cube - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}         
