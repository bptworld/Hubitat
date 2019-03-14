/**
 *  ****************  Magic Cube Child  ****************
 *
 *  Design Usage:
 *  Take control of your Xiaomi Mi Cube. Control devices based on Flip, Slide, Knock, Rotation and Shake.
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
 *  V1.0.0 - 03/14/19 - Initial Release
 *
 */

def setVersion() {
	state.version = "v1.0.0"
}

definition(
    name: "Magic Cube Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Take control of your Xiaomi Mi Cube. Control devices based on Flip, Slide, Knock, Rotation and Shake.",
    category: "",
	parent: "BPTWorld:Magic Cube",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
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
			paragraph "For use with the Xiaomi Mi Cube using the 'Xiaomi Mi Cube Controller device driver'"
			paragraph "Driver information can be found here: <a href='https://community.hubitat.com/t/release-xiaomi-aqara-device-drivers/631/527' target='_Blank'>[TUTORIAL] Use of the Xiaomi Mi Cube Controller device driver</a>"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Xiaomi Mi Cube Controller")) {
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
		//	input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def face0Options(){
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
			}
		}
		if(oF0Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Slide Options")) {
				input(name: "f0SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF0Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Knock Options")) {
				input(name: "f0KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF0rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Rotate Left Options")) {
				input(name: "f0rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF0rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Rotate Right Options")) {
				input(name: "f0rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF0Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 0 - Shake Options")) {
				input(name: "f0ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f0ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
	}
	}
}

def face1Options(){
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
			}
		}
		if(oF1Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Slide Options")) {
				input(name: "f1SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF1Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Knock Options")) {
				input(name: "f1KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF1rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Rotate Left Options")) {
				input(name: "f1rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF1rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Rotate Right Options")) {
				input(name: "f1rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF1Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 1 - Shake Options")) {
				input(name: "f1ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f1ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
	}
	}
}

def face2Options(){
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
			}
		}
		if(oF2Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Slide Options")) {
				input(name: "f2SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF2Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Knock Options")) {
				input(name: "f2KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF2rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Rotate Left Options")) {
				input(name: "f2rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF2rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Rotate Right Options")) {
				input(name: "f2rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF2Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 2 - Shake Options")) {
				input(name: "f2ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f2ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
	}
	}
}

def face3Options(){
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
			}
		}
		if(oF3Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Slide Options")) {
				input(name: "f3SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF3Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Knock Options")) {
				input(name: "f3KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF3rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Rotate Left Options")) {
				input(name: "f3rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF3rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Rotate Right Options")) {
				input(name: "f3rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF3Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 3 - Shake Options")) {
				input(name: "f3ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f3ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
	}
	}
}

def face4Options(){
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
			}
		}
		if(oF4Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Slide Options")) {
				input(name: "f4SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF4Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Knock Options")) {
				input(name: "f4KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF4rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Rotate Left Options")) {
				input(name: "f4rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF4rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Rotate Right Options")) {
				input(name: "f4rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF4Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 4 - Shake Options")) {
				input(name: "f4ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f4ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
	}
	}
}

def face5Options(){
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
			}
		}
		if(oF5Slide) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Slide Options")) {
				input(name: "f5SlideOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5SlideOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF5Knock) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Knock Options")) {
				input(name: "f5KnockOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5KnockOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF5rLeft) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Rotate Left Options")) {
				input(name: "f5rLeftOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5rLeftOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF5rRight) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Rotate Right Options")) {
				input(name: "f5rRightOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5rRightOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
			}
		}
		if(oF5Shake) {
			section(getFormat("header-green", "${getImage("Blank")}"+" Face 5 - Shake Options")) {
				input(name: "f5ShakeOn", type: "capability.switch", title: "Turn these switches ON", required: false, multiple: true)
				input(name: "f5ShakeOff", type: "capability.switch", title: "Turn these switches OFF", required: false, multiple: true)
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
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	logCheck()
	initialize()
}

def initialize() {
	if(enableSwitch1) subscribe(enablerSwitch1, "switch", enablerSwitchHandler)
	subscribe(xCube, "pushed", pushedHandler)
	subscribe(xCube, "face", faceHandler)
	subscribe(xCube, "angle", angleHandler)
}

def enablerSwitchHandler(evt){
	state.enablerSwitch2 = evt.value
	LOGDEBUG("In enablerSwitchHandler - Enabler Switch = ${state.enablerSwitch2}")
    if(state.enablerSwitch2 == "on"){
    	LOGDEBUG("In enablerSwitchHandler - Enabler Switch is ON - Child app is disabled.")
	} else {
		LOGDEBUG("In enablerSwitchHandler - Enabler Switch is OFF - Child app is active.")
    }
}

def faceHandler(msg) {
	state.faceValue = msg.value.toString()
	LOGDEBUG("In faceHandler - Face: ${state.faceValue}")
	waitHere()
}

def pushedHandler(msg) {
	state.pushedValue = msg.value.toString()
	LOGDEBUG("In pushedHandler - Pushed: ${state.pushedValue}")
	waitHere()
}

def angleHandler(msg) {
	state.angleValue = msg.value.toString()
	LOGDEBUG("In angleHandler - Angle: ${state.angleValue}")
	waitHere()
}

def waitHere() {
	if(state.angleValue == null) state.angleValue = "0"
	// Wait here to make sure all info is up to date
	runIn(oXDelay, doSomethingHandler)
}

def doSomethingHandler() {
	LOGDEBUG("In doSomethingHandler - Face: ${state.faceValue}, Pushed: ${state.pushedValue}, Angle: ${state.angleValue}")
	if(state.pushedValue == "1" || state.pushedValue == "7"  || state.pushedValue == "13" || state.pushedValue == "19" || state.pushedValue == "25" || state.pushedValue == "31") { state.face = "f0" } 
	if(state.pushedValue == "2" || state.pushedValue == "8"  || state.pushedValue == "14" || state.pushedValue == "20" || state.pushedValue == "26" || state.pushedValue == "32") { state.face = "f1" }
	if(state.pushedValue == "3" || state.pushedValue == "9"  || state.pushedValue == "15" || state.pushedValue == "21" || state.pushedValue == "27" || state.pushedValue == "33") { state.face = "f2" }
	if(state.pushedValue == "4" || state.pushedValue == "10"  || state.pushedValue == "16" || state.pushedValue == "22" || state.pushedValue == "28" || state.pushedValue == "34") { state.face = "f3" }
	if(state.pushedValue == "5" || state.pushedValue == "11"  || state.pushedValue == "17" || state.pushedValue == "23" || state.pushedValue == "29" || state.pushedValue == "35") { state.face = "f4" }
	if(state.pushedValue == "6" || state.pushedValue == "12"  || state.pushedValue == "18" || state.pushedValue == "24" || state.pushedValue == "30" || state.pushedValue == "36") { state.face = "f5" }
	magicHappensHandler()
}

def magicHappensHandler() {
	LOGDEBUG("In magicHappensHandler...")
//	if(state.enablerSwitch2 == "off") {
 		if(pause1 == false){
			if((state.pushedValue == "1") && (f0FlipToOn)) { 
				switchesOn = f0FlipToOn
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Flip To On")
			}
			if((state.pushedValue == "1") && (f0FlipToOff)) {
				switchesOff = f0FlipToOff
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Flip To Off")
			}
			if((state.pushedValue == "2") && (f1FlipToOn)) { switchesOn = f1FlipToOn }
			if((state.pushedValue == "2") && (f1FlipToOff)) { switchesOff = f1FlipToOff }
			if((state.pushedValue == "3") && (f2FlipToOn)) { switchesOn = f2FlipToOn }
			if((state.pushedValue == "3") && (f2FlipToOff)) { switchesOff = f2FlipToOff }				
			if((state.pushedValue == "4") && (f3FlipToOn)) { switchesOn = f3FlipToOn }
			if((state.pushedValue == "4") && (f3FlipToOff)) { switchesOff = f3FlipToOff }				
			if((state.pushedValue == "5") && (f4FlipToOn)) { switchesOn = f4FlipToOn }
			if((state.pushedValue == "5") && (f4FlipToOff)) { switchesOff = f4FlipToOff }				
			if((state.pushedValue == "6") && (f5FlipToOn)) { switchesOn = f5FlipToOn }
			if((state.pushedValue == "6") && (f5FlipToOff)) { switchesOff = f5FlipToOff }				
											
			if((state.pushedValue == "7") && (f0SlideOn)) { 
				switchesOn = f0SlideOn
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Slide On")
			}
			if((state.pushedValue == "7") && (f0SlideOff)) {
				switchesOff = f0SlideOff
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Slide Off")
			}
			if((state.pushedValue == "8") && (f1SlideOn)) { switchesOn = f1SlideOn }
			if((state.pushedValue == "8") && (f1SlideOff)) { switchesOff = f1SlideOff }
			if((state.pushedValue == "9") && (f2SlideOn)) { switchesOn = f2SlideOn }
			if((state.pushedValue == "9") && (f2SlideOff)) { switchesOff = f2SlideOff }
			if((state.pushedValue == "10") && (f3SlideOn)) { switchesOn = f3SlideOn }
			if((state.pushedValue == "10") && (f3SlideOff)) { switchesOff = f3SlideOff }
			if((state.pushedValue == "11") && (f4SlideOn)) { switchesOn = f4SlideOn }
			if((state.pushedValue == "11") && (f4SlideOff)) { switchesOff = f4SlideOff }
			if((state.pushedValue == "12") && (f5SlideOn)) { switchesOn = f5SlideOn }
			if((state.pushedValue == "12") && (f5SlideOff)) { switchesOff = f5SlideOff }				
			
			if((state.pushedValue == "13") && (f0KnockOn)) {
				switchesOn = f0KnockOn
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Knock On")
			}
			if((state.pushedValue == "13") && (f0KnockOff)) { 
				switchesOff = f0KnockOff
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Knock Off")
			}
			if((state.pushedValue == "14") && (f1KnockOn)) { switchesOn = f1KnockOn }
			if((state.pushedValue == "14") && (f1KnockOff)) { switchesOff = f1KnockOff }
			if((state.pushedValue == "15") && (f2KnockOn)) { switchesOn = f2KnockOn }
			if((state.pushedValue == "15") && (f2KnockOff)) { switchesOff = f2KnockOff }				
			if((state.pushedValue == "16") && (f3KnockOn)) { switchesOn = f3KnockOn }
			if((state.pushedValue == "16") && (f3KnockOff)) { switchesOff = f3KnockOff }				
			if((state.pushedValue == "17") && (f4KnockOn)) { switchesOn = f4KnockOn }
			if((state.pushedValue == "17") && (f4KnockOff)) { switchesOff = f4KnockOff }				
			if((state.pushedValue == "18") && (f5KnockOn)) { switchesOn = f5KnockOn }
			if((state.pushedValue == "18") && (f5KnockOff)) { switchesOff = f5KnockOff }				
							
			if((state.pushedValue == "19") && (f0rRightOn)) {
				switchesOn = f0rRightOn
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Rotate Right On")
			}
			if((state.pushedValue == "19") && (f0rRightOff)) { 
				switchesOff = f0rRightOff
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Rotate Right Off")
			}
			if((state.pushedValue == "20") && (f1rRightOn)) { switchesOn = f1rRightOn }
			if((state.pushedValue == "20") && (f1rRightOff)) { switchesOff = f1rRightOff }
			if((state.pushedValue == "21") && (f2rRightOn)) { switchesOn = f2rRightOn }
			if((state.pushedValue == "21") && (f2rRightOff)) { switchesOff = f2rRightOff }				
			if((state.pushedValue == "22") && (f3rRightOn)) { switchesOn = f3rRightOn }
			if((state.pushedValue == "22") && (f3rRightOff)) { switchesOff = f3rRightOff }				
			if((state.pushedValue == "23") && (f4rRightOn)) { switchesOn = f4rRightOn }
			if((state.pushedValue == "23") && (f4rRightOff)) { switchesOff = f4rRightOff }				
			if((state.pushedValue == "24") && (f5rRightOn)) { switchesOn = f5rRightOn }
			if((state.pushedValue == "24") && (f5rRightOff)) { switchesOff = f5rRightOff }				
											
			if((state.pushedValue == "25") && (f0rLeftOn)) {
				switchesOn = f0rLeftOn
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Rotate Left On")
			}
			if((state.pushedValue == "25") && (f0rLeftOff)) {
				switchesOff = f0rLeftOff
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Rotate Left Off")
			}
			if((state.pushedValue == "26") && (f1rLeftOn)) { switchesOn = f1rLeftOn }
			if((state.pushedValue == "26") && (f1rLeftOff)) { switchesOff = f1rLeftOff }
			if((state.pushedValue == "27") && (f2rLeftOn)) { switchesOn = f2rLeftOn }
			if((state.pushedValue == "27") && (f2rLeftOff)) { switchesOff = f2rLeftOff }				
			if((state.pushedValue == "28") && (f3rLeftOn)) { switchesOn = f3rLeftOn }
			if((state.pushedValue == "28") && (f3rLeftOff)) { switchesOff = f3rLeftOff }				
			if((state.pushedValue == "29") && (f4rLeftOn)) { switchesOn = f4rLeftOn }
			if((state.pushedValue == "29") && (f4rLeftOff)) { switchesOff = f4rLeftoff }				
			if((state.pushedValue == "30") && (f5rLeftOn)) { switchesOn = f5rLeftOn }
			if((state.pushedValue == "30") && (f5rLeftOff)) { switchesOff = f5rLeftOff }				
							
			if((state.pushedValue == "31") && (f0ShakeOn)) { 
				switchesOn = f0ShakeOn
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Shake On")
			}
			if((state.pushedValue == "31") && (f0ShakeOff)) { 
				switchesOff = f0ShakeOff
				LOGDEBUG("In magicHappensHandler...Cube: ${xCube}, Pushed: ${state.pushedValue} = Shake Off")
			}
			if((state.pushedValue == "32") && (f1ShakeOn)) { switchesOn = f1ShakeOn }
			if((state.pushedValue == "32") && (f1ShakeOff)) { switchesOff = f1ShakeOff }	
			if((state.pushedValue == "33") && (f2ShakeOn)) { switchesOn = f2ShakeOn }
			if((state.pushedValue == "33") && (f2ShakeOff)) { switchesOff = f2ShakeOff }
			if((state.pushedValue == "34") && (f3ShakeOn)) { switchesOn = f3ShakeOn }
			if((state.pushedValue == "34") && (f3ShakeOff)) { switchesOff = f3ShakeOff }
			if((state.pushedValue == "35") && (f4ShakeOn)) { witchesOn = f4ShakeOn }
			if((state.pushedValue == "35") && (f4ShakeOff)) { switchesOff = f4ShakeOff }
			if((state.pushedValue == "36") && (f5ShakeOn)) { switchesOn = f5ShakeOn }
			if((state.pushedValue == "36") && (f5ShakeOff)) { switchesOff = f5ShakeOff }
							
			if(switchesOn) switchesOnHandler()
			if(switchesOff) switchesOffHandler()
		} else {
			log.info "${app.label} - Unable to continue - App paused"
		}
//	} else {
//		log.info "${app.label} - Enabler Switch is ON - Child app is disabled."
//	}
}

def switchesOnHandler() {
	switchesOn.each { it ->
		LOGDEBUG("In switchOnHandler - Turning on ${it}")
		it.on()
	}
}

def switchesOffHandler() {
	switchesOff.each { it ->
		LOGDEBUG("In switchOffHandler - Turning off ${it}")
		it.off()
	}
}

// ***** Normal Stuff *****

def pauseOrNot(){										// Modified from @Cobra Code
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
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
}

def logCheck(){											// Modified from @Cobra Code
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){										// Modified from @Cobra Code
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
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
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Magic Cube - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}         
