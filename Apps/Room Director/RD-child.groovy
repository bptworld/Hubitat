/**
 *  **************** Room Director Child App ****************
 *  Design Usage:
 *  Make your rooms smarter by directing them to do what you want, automatically.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  1.2.0 - 08/15/20 - Added ability to use multiple triggers, Added Time and Sunset/Sunrise triggers, Added Permadent Dim
 *  1.1.9 - 08/13/20 - Added Time Delay to each Mode, Added Dashboard Room Tile, several fixes/improvements
 *  1.1.8 - 07/15/20 - Added Humidity and Power to secondary triggers. Added up to 3 secondary triggers.
 *  1.1.7 - 07/05/20 - Added optional Data device, New driver to support data device, Added Sleep options
 *  1.1.6 - 07/04/20 - Added more logging
 *  1.1.5 - 06/22/20 - Changes to letsTalk
 *  1.1.4 - 06/19/20 - Added The Flasher
 *  1.1.3 - 06/13/20 - Fixed typo with daysMatch, thanks @fourwhitehouse!
 *  1.1.2 - 06/11/20 - All speech now goes through Follow Me
 *  1.1.1 - 06/10/20 - Made a few changes
 *  1.1.0 - 05/22/20 - Override switch now supports multiple switches
 *  1.0.9 - 05/17/20 - Added Mode Override option
 *  1.0.8 - 05/02/20 - More changes to repeat option
 *  1.0.7 - 04/28/20 - Added a Switch option to Notifications
 *  1.0.6 - 04/27/20 - Cosmetic changes
 *  1.0.5 - 04/27/20 - Chasing a gremlin
 *  1.0.4 - 04/24/20 - Fixed lights not respecting new motion after warning
 *  1.0.3 - 04/16/20 - Fixed pause duration error
 *  1.0.2 - 02/24/20 - Attempt to fix sunrise/sunset settings
 *  1.0.1 - 12/10/19 - Cosmetic typo fix
 *  1.0.0 - 11/12/19 - Initial release.
 *
 */

import groovy.json.*
import hubitat.helper.RMUtils
import groovy.time.TimeCategory
import java.text.SimpleDateFormat
    
def setVersion(){
    state.name = "Room Director"
	state.version = "1.2.0"
}

definition(
    name: "Room Director Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Make your rooms smarter by directing them to do what you want, automatically.",
    category: "",
	parent: "BPTWorld:Room Director",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Room%20Director/RD-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "onConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "offConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "sleepConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "examples", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display()
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "Make your rooms smarter by directing them to do what you want, automatically."
            paragraph "Note: Remember Vibration Sensors can be used as Motion Sensors. They work great as a 'helper device'! (ie. In a chair cushion)"
		}
	    section(getFormat("header-green", "${getImage("Blank")}"+" Occupancy Trigger")) {
            paragraph "This is what will trigger the room to be occupied."
    		
            input "myContacts", "capability.contactSensor", title: "Select the contact sensor(s) to activate the room", required:false, multiple:true, submitOnChange:true
            if(myContacts) input "contactOption", "enum", title: "Select contact option - If (option), Room is occupied", options: ["Open","Closed"], required:true

            input "myMotion", "capability.motionSensor", title: "Select the motion sensor(s) to activate the room", required:false, multiple:true

            input "myPresence", "capability.presenceSensor", title: "Select the Presence Sensor(s) to activate the room", required:false, multiple:true

            input "mySwitches", "capability.switch", title: "Select Switch(es) to activate the room", required: false, multiple:true
            
            input "sunRestriction", "bool", title: "From Sunset to Sunrise?", description: "sun", defaultValue:false, submitOnChange:true
            if(sunRestriction) {
                input "sunsetOffsetValue", "text", title: "Sunset Offset (HH:MM)", required: true, defaultValue: "00:10", width: 6
                input "sunsetOffsetDir", "enum", title: "Before or After", required: true, options: ["Before","After"], defaultValue: "Before", width: 6
                
                input "sunriseOffsetValue", "text", title: "Sunrise Offset (HH:MM)", required: true, defaultValue: "00:10", width: 6
                input "sunriseOffsetDir", "enum", title: "Before or After", required: true, options: ["Before","After"], defaultValue: "After", width: 6
                
                app.removeSetting("fromTime")
                app.removeSetting("toTime")
                app.removeSetting("midnightCheckR")
            } else {
                input "fromTime", "time", title: "From", required: false, width: 6, submitOnChange:true
                input "toTime", "time", title: "To", required: false, width: 6
                input "midnightCheckR", "bool", title: "Does this time frame cross over midnight", defaultValue:false, submitOnChange:true
                
                app.removeSetting("sunriseOffsetValue")
                app.removeSetting("sunriseOffsetDir") 
                app.removeSetting("sunsetOffsetValue")
                app.removeSetting("sunsetOffsetDir")
            }
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Occupancy Helper Device (optional)")) {
            paragraph "This will help the room stay occupied but not trigger the room to be active."
            href "examples", title:"${getImage("instructions")} Find Examples of Secondary Trigger use here", description:"Click here for examples"
            paragraph "<hr>"
            input "useHelper2", "bool", title: "Use Helper?", defaultValue:false, submitOnChange:true
            if(useHelper2) {
                input "triggerMode2", "enum", title: "Select room helper Type", submitOnChange: true, options: ["Contact", "Humidity", "Motion", "Power", "Presence", "Switch"], required: false
                if(triggerMode2 == "Contact"){
                    input "myContacts2", "capability.contactSensor", title: "Select the Contact Sensor(s) to help keep the room occupied", required: false, multiple:true
                    input "contactOption2", "enum", title: "Select contact option - If (option), Room is occupied", options: ["Open","Closed"], required:true
                }
                if(triggerMode2 == "Humidity"){
                    input "myHumidity2", "capability.relativeHumidityMeasurement", title: "Select the Humidity Sensor(s) to help keep the room occupied", required: false, multiple:true
                    input "myHumiditySP2", "number", title: "Select humidity breakpoint - if above then Occupied, below is Unoccupied", required:false
                }
                if(triggerMode2 == "Motion"){
                    input "myMotion2", "capability.motionSensor", title: "Select the Motion Sensor(s) to help keep the room occupied", required:false, multiple:true
                }
                if(triggerMode2 == "Power"){
                    input "myPower2", "capability.powerMeter", title: "Select the Power Sensor(s) to help keep the room occupied", required: false, multiple:true
                    input "myPowerSP2", "number", title: "Select power breakpoint - if above then Occupied, below is Unoccupied", required:false
                }
                if(triggerMode2 == "Presence"){
                    input "myPresence2", "capability.presenceSensor", title: "Select the Presence Sensor(s) to help keep the room occupied", required:false, multiple:true
                }
                if(triggerMode2 == "Switch"){
                    input "mySwitches2", "capability.switch", title: "Select Switch(es) to help keep the room occupied", required:false, multiple:true
                }
            } else {
                app.removeSetting("triggerMode2")
                app.removeSetting("myContacts2")
                app.removeSetting("myHumidity2")
                app.removeSetting("myMotion2")
                app.removeSetting("myPower2")
                app.removeSetting("myPresence2")
                app.removeSetting("mySwitches2")
            }
            
            if(useHelper2) {
                paragraph "<hr>"
                input "useHelper3", "bool", title: "Use another Helper?", defaultValue:false, submitOnChange:true
                if(useHelper3) {
                    input "triggerMode3", "enum", title: "Select room helper 2 Type", submitOnChange: true, options: ["Contact", "Humidity", "Motion", "Presence", "Switch"], required: false
                    if(triggerMode3 == "Contact"){
                        input "myContacts3", "capability.contactSensor", title: "Select the Contact Sensor(s) to help keep the room occupied", required: false, multiple:true
                        input "contactOption3", "enum", title: "Select contact option - If (option), Room is occupied", options: ["Open","Closed"], required:true
                    }
                    if(triggerMode3 == "Humidity"){
                        input "myHumidity3", "capability.relativeHumidityMeasurement", title: "Select the Humidity Sensor(s) to help keep the room occupied", required: false, multiple:true
                        input "myHumiditySP3", "number", title: "Select humidity breakpoint - if above then bad, below is good", required:false
                    }
                    if(triggerMode3 == "Motion"){
                        input "myMotion3", "capability.motionSensor", title: "Select the Motion Sensor(s) to help keep the room occupied", required:false, multiple:true
                    }
                    if(triggerMode3 == "Power"){
                        input "myPower3", "capability.powerMeter", title: "Select the Power Sensor(s) to help keep the room occupied", required: false, multiple:true
                        input "myPowerSP3", "number", title: "Select power breakpoint - if above then Occupied, below is Unoccupied", required:false
                    }
                    if(triggerMode3 == "Presence"){
                        input "myPresence3", "capability.presenceSensor", title: "Select the Presence Sensor(s) to help keep the room occupied", required:false, multiple:true
                    }
                    if(triggerMode3 == "Switch"){
                        input "mySwitches3", "capability.switch", title: "Select Switch(es) to help keep the room occupied", required:false, multiple:true
                    }
                } else {
                    app.removeSetting("triggerMode3")
                    app.removeSetting("myContacts3")
                    app.removeSetting("myHumidity3")
                    app.removeSetting("myMotion3")
                    app.removeSetting("myPower3")
                    app.removeSetting("myPresence3")
                    app.removeSetting("mySwitches3")
                }
            }
            
            if(useHelper3) {
                paragraph "<hr>"
                input "useHelper4", "bool", title: "Use another Helper?", defaultValue:false, submitOnChange:true
                if(useHelper4) {
                    input "triggerMode4", "enum", title: "Select room helper 3 Type", submitOnChange: true, options: ["Contact", "Humidity", "Motion", "Presence", "Switch"], required: false
                    if(triggerMode4 == "Contact"){
                        input "myContacts4", "capability.contactSensor", title: "Select the Contact Sensor(s) to help keep the room occupied", required: false, multiple:true
                        input "contactOption4", "enum", title: "Select contact option - If (option), Room is occupied", options: ["Open","Closed"], required:true
                    }
                    if(triggerMode4 == "Humidity"){
                        input "myHumidity4", "capability.relativeHumidityMeasurement", title: "Select the Humidity Sensor(s) to help keep the room occupied", required: false, multiple:true
                        input "myHumiditySP4", "number", title: "Select humidity breakpoint - if above then bad, below is good", required:false
                    }
                    if(triggerMode4 == "Motion"){
                        input "myMotion4", "capability.motionSensor", title: "Select the Motion Sensor(s) to help keep the room occupied", required:false, multiple:true
                    }
                    if(triggerMode4 == "Power"){
                        input "myPower4", "capability.powerMeter", title: "Select the Power Sensor(s) to help keep the room occupied", required: false, multiple:true
                        input "myPowerSP4", "number", title: "Select power breakpoint - if above then Occupied, below is Unoccupied", required:false
                    }
                    if(triggerMode4 == "Presence"){
                        input "myPresence4", "capability.presenceSensor", title: "Select the Presence Sensor(s) to help keep the room occupied", required:false, multiple:true
                    }
                    if(triggerMode4 == "Switch"){
                        input "mySwitches4", "capability.switch", title: "Select Switch(es) to help keep the room occupied", required:false, multiple:true
                    }
                } else {
                    app.removeSetting("triggerMode4")
                    app.removeSetting("myContacts4")
                    app.removeSetting("myHumidity4")
                    app.removeSetting("myMotion4")
                    app.removeSetting("myPower4")
                    app.removeSetting("myPresence4")
                    app.removeSetting("mySwitches4")
                }
            }
		}

		section(getFormat("header-green", "${getImage("Blank")}"+" Control Options")) {
            paragraph "<b>Room Override</b>, If this device is On, Room Director events will NOT happen for this child app."
            input "roomOverride", "capability.switch", title: "Select the device(s) to 'disable' the room", multiple: true, submitOnChange:true 
            paragraph "<b>Mode Override</b>, Great for rooms that are on a different schedule than the current Mode."
            input "modeOverride", "capability.switch", title: "Select the device to 'control' the Mode override.", submitOnChange:true
            if(modeOverride) {
                paragraph "When Device is OFF, Room will follow the current Mode settings.<br>When Device is ON, Room will behave like it's the Mode selected below."
                input "moMode", "mode", title: "If Mode Override is ON, use Mode", required: true, multiple: false, submitOnChange: true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Control")) { 
            if(modeName1) {
                href "onConfig", title:"${getImage("optionsGreen")} Select Device 'OCCUPIED' options here", description:"Click here for Options"
            } else {
                href "onConfig", title:"${getImage("optionsRed")} Select Device 'OCCUPIED' options here", description:"Click here for Options"
            }
            
            if(unSwitchesOff || unSwitchesOn) {
                href "offConfig", title:"${getImage("optionsGreen")} Select Device 'UNOCCUPIED' options here", description:"Click here for Options"
            } else {
                href "offConfig", title:"${getImage("optionsRed")} Select Device 'UNOCCUPIED' options here", description:"Click here for Options"
            }
            
            if(sleepDeviceContact || sleepDevicePresense || sleepDeviceSwitch) {
                href "sleepConfig", title:"${getImage("optionsGreen")} Select Device 'SLEEP' options here", description:"Click here for Options"
            } else {
                href "sleepConfig", title:"${getImage("optionsRed")} Select Device 'SLEEP' options here", description:"Click here for Options"
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Restrictions")) { 
            input(name: "days", type: "enum", title: "Only on these days", description: "Days", required: false, multiple: true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"])
            
            input "luxRestriction", "bool", title: "Use Lux Restriction?", description: "lux", defaultValue:false, submitOnChange:true   
            if(luxRestriction) {
                input "lightSensor", "capability.illuminanceMeasurement", title: "Only when illuminance on this light sensor..."
                input "lightLevel", "number", title: "...is equal to or below this illuminance level"
            } else {
                app.removeSetting("lightSensor")
                app.removeSetting("lightLevel")    
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            if(fmSpeaker || usePush || useRule || useSwitch || useTheFlasher) {
                href "speechOptions", title:"${getImage("optionsGreen")} Notification Options", description:"Click here to setup the Notification Options"
            } else {
                href "speechOptions", title:"${getImage("optionsRed")} Notification Options", description:"Click here to setup the Notification Options"
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device (Optional)")) {
            paragraph "Each child app can also create a virtual device to store the Room Data. This device will display the status of the room. ie. occupied, unoccupied or Sleep"
            input "useExistingDevice", "bool", title: "Use existing device (off) or have RD create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'RD - Kitchen')", required:true, submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            
            input "dataDevice", "capability.*", title: "Virtual Device specified above", required:false, multiple:false, submitOnChange:true
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Room Director Driver'.</small>"
            }
            
            if(dataDevice) {
                input "useTile", "bool", title: "Create Informational Room Tile for Dashboards", defaultValue:false, submitOnChange:true
                if(useTile) {
                    paragraph "<small>* Devices can be a single device or a device created with 'Average Plus'</small>"
                    input "tileName", "text", title: "Enter a Room Name to use on the Tile", required:false, submitOnChange:true
                    input "roomTemp", "capability.temperatureMeasurement", title: "Room Temperature Device", required:false, multiple:false, submitOnChange:true
                    input "roomHumidity", "capability.relativeHumidityMeasurement", title: "Room Humidity Device", required:false, multiple:false, submitOnChange:true 
                } else {
                    app.removeSetting("tileName")
                    app.removeSetting("roomTemp")
                    app.removeSetting("roomHumidity")
                }
            }
        }  
        
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this automation", required: false
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Debugging", submitOnChange:true
		}
		display2()
	}
}

def onConfig() {
    dynamicPage(name: "onConfig", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Device 'OCCUPIED' Options")) {
            input "modeName1", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
            if(modeName1) {
                input "sceneSwitch1", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			    input "oSwitchesOn1", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                input "oSwitchesOff1", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                input "oDimmers1", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                if(oDimmers1) input "dimLevel1", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                input "timeDelayed1", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
            
                input "modeName2", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName2) {
                    input "sceneSwitch2", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn2", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff2", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers2", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers2) input "dimLevel2", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed2", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch2")
                    app.removeSetting("oSwitchesOn2")    
                    app.removeSetting("oSwitchesOff2")
                    app.removeSetting("oDimmers2")
                    app.removeSetting("dimLevel2")
                }
            }
            if(modeName2) {
                input "modeName3", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName3) {
                    input "sceneSwitch3", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn3", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff3", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers3", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers3) input "dimLevel3", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed3", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch3")
                    app.removeSetting("oSwitchesOn3")    
                    app.removeSetting("oSwitchesOff3")
                    app.removeSetting("oDimmers3")
                    app.removeSetting("dimLevel3")
                }
            }
            if(modeName3) {
                input "modeName4", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName4) {
                    input "sceneSwitch4", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn4", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff4", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers4", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers4) input "dimLevel4", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed4", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch4")
                    app.removeSetting("oSwitchesOn4")    
                    app.removeSetting("oSwitchesOff4")
                    app.removeSetting("oDimmers4")
                    app.removeSetting("dimLevel4")
                }
            }
            if(modeName4) {
                input "modeName5", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName5) {
                    input "sceneSwitch5", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn5", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff5", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers5", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers5) input "dimLevel5", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed5", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch5")
                    app.removeSetting("oSwitchesOn5")    
                    app.removeSetting("oSwitchesOff5")
                    app.removeSetting("oDimmers5")
                    app.removeSetting("dimLevel5")
                }
            }
            if(modeName5) {
                input "modeName6", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName6) {
                    input "sceneSwitch6", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn6", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff6", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers6", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers6) input "dimLevel6", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed6", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch6")
                    app.removeSetting("oSwitchesOn6")    
                    app.removeSetting("oSwitchesOff6")
                    app.removeSetting("oDimmers6")
                    app.removeSetting("dimLevel6")
                }
            }
            if(modeName6) {
                input "modeName7", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName7) {
                    input "sceneSwitch7", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn7", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff7", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers7", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers7) input "dimLevel7", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed7", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch7")
                    app.removeSetting("oSwitchesOn7")    
                    app.removeSetting("oSwitchesOff7")
                    app.removeSetting("oDimmers7")
                    app.removeSetting("dimLevel7")
                }
            }
            if(modeName7) {
                input "modeName8", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName8) {
                    input "sceneSwitch8", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn8", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff8", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers8", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers8) input "dimLevel8", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed8", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch8")
                    app.removeSetting("oSwitchesOn8")    
                    app.removeSetting("oSwitchesOff8")
                    app.removeSetting("oDimmers8")
                    app.removeSetting("dimLevel8")
                }
            }
            if(modeName8) {
                input "modeName9", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName9) {
                    input "sceneSwitch9", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn9", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff9", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers9", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers9) input "dimLevel9", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed9", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch9")
                    app.removeSetting("oSwitchesOn9")    
                    app.removeSetting("oSwitchesOff9")
                    app.removeSetting("oDimmers9")
                    app.removeSetting("dimLevel9")
                }
            }
            if(modeName9) {
                input "modeName0", "mode", title: "When in this Mode...", required: false, multiple: true, submitOnChange: true
                if(modeName0) {
                    input "sceneSwitch0", "capability.pushableButton", title: "...push this Button to activate a Scene.", submitOnChange: true
			        input "oSwitchesOn0", "capability.switch", title: "Select Switch(es) to turn ON", multiple: true
                    input "oSwitchesOff0", "capability.switch", title: "Select Switch(es) to turn OFF", multiple: true
                    input "oDimmers0", "capability.switchLevel", title: "Select Dimmers Set to Level", multiple: true, submitOnChange: true
                    if(oDimmers0) input "dimLevel0", "number", title: "Set Level (1 to 99)", required: true, defaultValue: 99, range: '1..99'
                    input "timeDelayed0", "number", title: "How long should the lights stay on when room becomes vacant (in minutes)", range: '1..60', required:true
                } else {
                    app.removeSetting("sceneSwitch0")
                    app.removeSetting("oSwitchesOn0")    
                    app.removeSetting("oSwitchesOff0")
                    app.removeSetting("oDimmers0")
                    app.removeSetting("dimLevel0")
                }
            }
        }
    }
}

def offConfig() {
    dynamicPage(name: "offConfig", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Device 'UNOCCUPIED' Options")) {
            input "permanentDim", "bool", title: "Use Permanent Dim instead of Off", defaultValue:false, submitOnChange:true
            if(permanentDim) {
                paragraph "Instead of turning off, lights will dim to a set level"
                input "permanentDimLvl", "number", title: "Permanent Dim Level (1 to 99)", range: '1..99'
                
                app.removeSetting("unSwitchesOff")
            } else {
                input "unSwitchesOff", "capability.switch", title: "Devices that should be turned off", multiple:true, required:false
                
                app.removeSetting("permanentDimLvl")
            }
            input "unSwitchesOn", "capability.switch", title: "Devices that should be turned on", multiple:true, required:false    
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Repeat Option")) {
            paragraph "This option is used to repeat turning all devices selected above off/on. Just to make sure nothing was missed."
			input "repeatTime", "number", title: "Repeat on/off (in seconds)", required: false, defaultValue: 1
		}
    }
}

def sleepConfig() {
    dynamicPage(name: "sleepConfig", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Device 'SLEEP' Options")) {
            paragraph "Sleep status works as an automatic override.<br> - If Sleep is detected, nothing will automatically turn the devices in the room on.<br> - BUT the devices in the room <i>will turn off</i> based on your Unoccupied settings.<br> - When Sleep is detected, it will also skip all of the warnings before adjusting the lights."
            paragraph "This is designed to work with things like the Withings Sleep Tracking Mat, but can also work by simply closing a door."
            input "sleepDeviceContact", "capability.contactSensor", title: "Sleep Devices that act like Contact Sensors", multiple:true, required:false, submitOnChange:true
            if(sleepDeviceContact) input "contactOptionSleep", "enum", title: "Select contact option - Sleep when - ", options: ["Open","Closed"], required:true
            input "sleepDevicePresense", "capability.presenceSensor", title: "Sleep Devices that act like Presence Sensors", multiple:true, required:false
            input "sleepDeviceSwitch", "capability.switch", title: "Sleep Devices that act like Switches", multiple:true, required:false
        }
    }
}

def speechOptions(){
    dynamicPage(name: "speechOptions", title: "Notification Options", install: false, uninstall:false){
        section() {
            paragraph "Room Director offers many options to warn you before the lights go out. Choose as many options as you like."
            paragraph "<hr>"
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
                paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
                input "useSpeech", "bool", title: "<b>Use Speech through Follow Me</b>", defaultValue:false, submitOnChange:true
                if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
            }
        }
        section() {
            input "usePush", "bool", defaultValue:false, title: "<b>Use Push?</b>", description: "Push", submitOnChange:true
        }
        if(usePush) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
                input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
            }
        }
        
        if(useSpeech || usePush) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Message")) {
                input "omessage", "text", title: "Random <b>Warning message</b> to be spoken and/or pushed before turning lights off - Separate each message with <b>;</b> (semicolon)", required: false, submitOnChange: true
                input "oMsgList", "bool", defaultValue:true, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
                if(oMsgList) {
                    def ovalues = "${omessage}".split(";")
                    olistMap = ""
                    ovalues.each { item -> olistMap += "${item}<br>"}
                    paragraph "${olistMap}"
                }
            }
        }
        
        section() {
            input "useRule", "bool", defaultValue:false, title: "<b>Use Rule Machine?</b>", description: "Rule", submitOnChange:true
        }
        if(useRule) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Rule Machine Options")) {
                paragraph "<b>Run a Rule Machine Rule before turning lights off</b>"
                def rules = RMUtils.getRuleList()
                input "rMachine", "enum", title: "Select which rules to run", options: rules, multiple: true
            }
        }
        
        section() {
            input "useSwitch", "bool", defaultValue:false, title: "<b>Use Switch?</b>", description: "Switch", submitOnChange:true
        }
        if(useSwitch) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Switch Options")) {
                paragraph "<b>Turn a switch on. Helpful to trigger other events outside of Room Director</b><br><small>Switch will turn on with warning and then off when warning is over.</small>"
                input "warningSwitches", "capability.switch", title: "Select Switch to turn ON", multiple: true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:true, multiple:false
                input "flashPreset", "number", title: "Select the Preset to use (1..5)", required:true, submitOnChange:true
            }
        }  
    }
}

def examples(){
    dynamicPage(name: "examples", title: "Notification Options", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Examples")) { 
            paragraph "Examples of Primary and Secondary trigger use"
            paragraph "<b>Bathroom</b><br>Walk into bathroom and trigger the 'Ceiling Motions Sensor' (primary), lights come on. Stay still too long and lights will turn off."
            paragraph "Close the door to trigger the 'contact sensor' (secondary). Even if the motion becomes inactive, (it can't see you when in the shower), the lights will not turn off until that door is opened and the motion is inactive."
            paragraph "<hr>"
            paragraph "<b>Kitchen</b><br>Lights are off - 'Kitchen Ceiling Motion Sensor' (primary) triggers room to be occupied, lights come on.  'Motion sensor under table' (secondary) helps lights to stay on even if 'Kitchen Ceiling Motion Sensor' becomes inactive."
            paragraph "Dog walks under table and triggers the 'Motion sensor under table' (secondary) but the lights were off, lights stay off."
            paragraph "<hr>"
            paragraph "<b>Living Room</b><br>Walk into the room and trigger the 'Ceiling Motion Sensor' (primary), lights come on. If sensor becomes inactive, lights will turn off"
            paragraph "Place phone on 'charger' (secondary). Lights will stay on even if 'Ceiling Motion Sensor' becomes inactive."
            paragraph "<hr>"
            paragraph "<i>Have something neat that you do with primary and secondary triggers? Please post it on the forums and I just might add it here! Thanks</i>"
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
    unschedule()
	initialize()
}

def initialize() {
    if(logEnable) log.debug "In initialize (${state.version}) - triggerMode2: ${triggerMode2}"
    setDefaults()    
    
	if(myContacts) subscribe(myContacts, "contact", primaryHandler)
	if(myMotion) subscribe(myMotion, "motion", primaryHandler)
    if(myPresence) subscribe(myPresence, "presence", primaryHandler)
	if(mySwitches) subscribe(mySwitches, "switch", primaryHandler)
	
    if(triggerMode2 == "Contact") subscribe(myContacts2, "contact", primaryHandler)
    if(triggerMode2 == "Humidity") subscribe(myHumidity2, "humidity", primaryHandler)
	if(triggerMode2 == "Motion") subscribe(myMotion2, "motion", primaryHandler)
    if(triggerMode2 == "Power") subscribe(myPower2, "power", primaryHandler)
    if(triggerMode2 == "Presence") subscribe(myPresence2, "presence", primaryHandler)
	if(triggerMode2 == "Switch") subscribe(mySwitches2, "switch", primaryHandler)
    
    if(triggerMode3 == "Contact") subscribe(myContacts3, "contact", primaryHandler)
    if(triggerMode3 == "Humidity") subscribe(myHumidity3, "humidity", primaryHandler)
	if(triggerMode3 == "Motion") subscribe(myMotion3, "motion", primaryHandler)
    if(triggerMode3 == "Power") subscribe(myPower3, "power", primaryHandler)
    if(triggerMode3 == "Presence") subscribe(myPresence3, "presence", primaryHandler)
	if(triggerMode3 == "Switch") subscribe(mySwitches3, "switch", primaryHandler)
    
    if(triggerMode4 == "Contact") subscribe(myContacts4, "contact", primaryHandler)
    if(triggerMode4 == "Humidity") subscribe(myHumidity4, "humidity", primaryHandler)
	if(triggerMode4 == "Motion") subscribe(myMotion4, "motion", primaryHandler)
    if(triggerMode4 == "Power") subscribe(myPower4, "power", primaryHandler)
    if(triggerMode4 == "Presence") subscribe(myPresence4, "presence", primaryHandler)
	if(triggerMode4 == "Switch") subscribe(mySwitches4, "switch", primaryHandler)
    
    if(sunRestriction) {
        def sunriseTime = location.sunrise.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        def sunsetTime = location.sunset.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    
        if(logEnable) log.debug "In initialize - sunrise: ${sunriseTime} - sunset: ${sunsetTime}"
        
        subscribe(location, "sunriseTime", sunriseTimeHandler)
        subscribe(location, "sunsetTime", sunsetTimeHandler)
        
        //Run today too
        scheduleWithOffset(sunsetTime, sunsetOffsetValue, sunsetOffsetDir, "sunsetHandler")
        scheduleWithOffset(sunriseTime, sunriseOffsetValue, sunriseOffsetDir, "sunriseHandler")
    }
    
    if(logEnable) log.debug "In initialize - Finished initialize"
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def primaryHandler(evt) {
    if(logEnable) log.warn "********** Starting Room Director **********"
	if(logEnable) log.debug "In primaryHandler (${state.version})"
    state.occupancy1 = "no"
    
    myContacts.each { it ->
        status = it.currentValue("contact")
        if(logEnable) log.debug "In primaryHandler - Contact: ${it} - value: ${status}"
        if(contactOption == "Closed") {
            if(status == "closed") {
                state.occupancy1 = "yes"
            }
        }
        if(contactOption == "Open") {
            if(status == "open") {
                state.occupancy1 = "yes" 
            }
        }
    }

    myMotion.each { it ->
        status = it.currentValue("motion")
        if(logEnable) log.debug "In primaryHandler - Motion Sensor: ${it} - value: ${status}"
        if(status == "active") {
            state.occupancy1 = "yes"
        }
    }

    myPresence.each { it ->
        status = it.currentValue("presence")
        if(logEnable) log.debug "In primaryHandler - Presence: ${it} - value: ${status}"
        if(status == "present") {
            state.occupancy1 = "yes"
        }
    }

    mySwitches.each { it ->
        status = it.currentValue("switch")
        if(logEnable) log.debug "In primaryHandler - Switch: ${it} - value: ${status}"
        if(status == "on") {
            state.occupancy1 = "yes" 
        }
    }
    
    if(logEnable) log.debug "In primaryHandler - occupancy1: ${state.occupancy1}"
    secondaryHandler()
}

def secondaryHandler() {
    if(logEnable) log.debug "In secondaryHandler (${state.version}) - triggerMode2: ${triggerMode2}"
    state.occupancy2 = "no"
    
    if(myContacts2 || myContacts3 || myContacts4) {
        allContacts = [myContacts2, myContacts3, myContacts4].flatten().findAll{it}
        allContacts.each { it ->
            status = it.currentValue("contact")
            if(logEnable) log.debug "In secondaryHandler - Contact: ${it} - value: ${status}"
            if(contactOption2 == "Closed") {
                if(status == "closed") {
                    state.occupancy2 = "yes"
                }
	        }
	        if(contactOption2 == "Open") {
                if(status == "open") {
                    state.occupancy2 = "yes" 
                }
		    }
        }
	}
    
    if(myHumidity2 || myHumidity3 || myHumidity4) {
        allHumidity = [myHumidity2, myHumidity3, myHumidity4].flatten().findAll{it}
        allHumidity.each { it ->
            status = it.currentValue("humidity")
            if(logEnable) log.debug "In secondaryHandler - H Sensor: ${it} - value: ${status}"
            if(status > myHumiditySP2) {
		        state.occupancy2 = "yes"
            }
        }
    }
    
    if(myMotion2 || myMotion3 || myMotion4) {
        allMotion = [myMotion2, myMotion3, myMotion4].flatten().findAll{it}
        allMotion.each { it ->
            status = it.currentValue("motion")
            if(logEnable) log.debug "In secondaryHandler - M Sensor: ${it} - value: ${status}"
            if(status == "active") {
		        state.occupancy2 = "yes"
            }
        }
    }

    if(myPower2 || myPower3 || myPower4) {
        allPower = [myPower2, myPower3, myPower4].flatten().findAll{it}
        allPower.each { it ->
            status = it.currentValue("power")
            if(logEnable) log.debug "In secondaryHandler - P Sensor: ${it} - value: ${status}"
            if(status > myPowerSP2) {
		        state.occupancy2 = "yes"
            }
        }
    }
    
    if(myPresence2 || myPresence3 || myPresence4) {
        allPresence = [myPresence2, myPresence3, myPresence4].flatten().findAll{it}
        allPresence.each { it ->
            status = it.currentValue("presence")
            if(logEnable) log.debug "In secondaryHandler - Presence: ${it} - value: ${status}"
            if(status == "present") {
		        state.occupancy2 = "yes" 
            }
        }
    }
    
    if(mySwitches2 || mySwitches3 || mySwitches4) {
        allSwitches = [mySwitches2, mySwitches3, mySwitches4].flatten().findAll{it}
        allSwitches.each { it ->
            status = it.currentValue("switch")
            if(logEnable) log.debug "In secondaryHandler - Switch: ${it} - value: ${status}"
            if(status == "on") {
		        state.occupancy2 = "yes" 
            }
        }
    }
    if(logEnable) log.trace "In secondaryHandler - occupancy1: ${state.occupancy1} - occupancy2: ${state.occupancy2}"
    whatToDo()
}

def whatToDo() {
    if(logEnable) log.debug "In whatToDo (${state.version}) - occ1: ${state.occupancy1} - occ2: ${state.occupancy2} - sunRiseTosunSet: ${state.sunRiseTosunSet}"
    dayOfTheWeekHandler()
    checkForSleep()
    checkTime()
    
    if(logEnable) log.debug "In whatToDo - daysMatch: ${state.daysMatch} - sunRiseTosunSet: (${state.sunRiseTosunSet}) - timeBetween: ${state.timeBetween}"
    
    if(state.daysMatch && state.sunRiseTosunSet && state.timeBetween) {
        if(state.sleeping) {
            if(logEnable) log.debug "In whatToDo - Sleeping - Going to vacantHandler"
            vacantHandler()
        } else if(state.occupancy1 == "no" && state.occupancy2 == "no") { 
            if(logEnable) log.debug "In whatToDo - Going to vacantHandler"
            vacantHandler()
        } else if(state.occupancy1 == "no" && state.occupancy2 == "yes") {
            if(logEnable) log.debug "In whatToDo - Doing nothing"
            unschedule()
        } else {
            if(logEnable) log.debug "In whatToDo - Going to occupancyHandler"
            if(warningSwitches) warningSwitches.off()
            unschedule()
            occupancyHandler()
        }
    }
}

def checkForSleep() {
    if(logEnable) log.debug "In checkForSleep (${state.version})"
    state.sleeping = false
    
    if(sleepDeviceContact) {
        sleepDeviceContact.each { it ->
            status = it.currentValue("contact")
            if(logEnable) log.debug "In checkForSleep - Contact: ${it} - value: ${status}"
            if(contactOptionSleep == "Closed") {
                if(status == "closed") {
                    state.sleeping = true
                }
	        }
	        if(contactOptionSleep == "Open") {
                if(status == "open") {
                    state.sleeping = true 
                }
		    }
        }
    }
    
    if(sleepDevicePresense) {       
        sleepDevicePresense.each { it ->
            status = it.currentValue("presence")
            if(logEnable) log.debug "In sleepDevicePresense - Presence: ${it} - value: ${status}"
            if(status == "present") {
		        state.sleeping = true 
            }
        }
    }
    
    if(sleepDeviceSwitch) {
        sleepDeviceSwitch.each { it ->
            status = it.currentValue("switch")
            if(logEnable) log.debug "In sleepDeviceSwitch - Switch: ${it} - value: ${status}"
            if(status == "on") {
		        state.sleeping = true 
            }
        }
    }
    if(logEnable) log.debug "In sleepDeviceSwitch - sleeping: ${state.sleeping}"
}

def luxLevel() {
    if(logEnable) log.debug "In luxLevel (${state.version})"
    if (lightSensor != null) {
		if(lightLevel == null) {lightLevel = 0}
        state.curLev = lightSensor.currentValue("illuminance").toInteger()
        if (state.curLev >= lightLevel.toInteger()) {
            if(logEnable) log.debug "In luxLevel...Current Light Level: ${state.curLev} is greater than lightValue: ${lightLevel}"
			state.isItDark = false
        } else {
            if(logEnable) log.debug "In luxLevel...Current Light Level: ${state.curLev} is less than lightValue: ${lightLevel}"
			state.isItDark = true
        }
    }
}

def occupancyHandler() {
    roomOverrideHandler()
    checkForSleep()
    
    if(logEnable) log.debug "In occupancyHandler (${state.version}) - roStatus: ${state.roStatus} - occupancy1: ${state.occupancy1} - occupancy2: ${state.occupancy2} - sleep: ${state.sleeping}"
    
    if(state.sleeping) {
        if(logEnable) log.debug "In occupancyHandler - Sleeping (${state.sleeping}), leaving room alone."
    } else {
        if(!state.roStatus) {
            if(luxRestriction) {
                luxLevel()
                if(logEnable) log.debug "In occupancyHandler - Using Lux Level: ${state.curLev} and isItDark: ${state.isItDark}"
                if(state.isItDark) {
                    if(logEnable) log.debug "In occupancyHandler - It's dark, adjusting room."
                    modeHandler()            
                } else { // Too light in room
                    if(logEnable) log.debug "In occupancyHandler - It's not dark, leaving room alone."
                }          
            } else {  // No lux restrictions
                if(logEnable) log.debug "In occupancyHandler - Not using Lux, adjusting room."
                modeHandler()
            }
        } else { // roomOverride is on
            unschedule()
            if(logEnable) log.debug "In occupancyHandler - roomOverride is ON (${state.roStatus}), leaving room alone."
        }
    }
}

def vacantHandler() {
    roomOverrideHandler()
    
    if(logEnable) log.debug "In vacantHandler (${state.version}) - roStatus: ${state.roStatus} - occupancy1: ${state.occupancy1} - occupancy2: ${state.occupancy2} - timeDelayed: ${state.timeDelayed}"
    if(!state.roStatus && state.occupancy1 == "no" && state.occupancy2 == "no") {
        if(permanentDim) {
            if(oSwitchesOn1) theDevices = oSwitchesOn1
            if(oSwitchesOn2) theDevices = oSwitchesOn2
            if(oSwitchesOn3) theDevices = oSwitchesOn3
            if(oSwitchesOn4) theDevices = oSwitchesOn4
            if(oSwitchesOn5) theDevices = oSwitchesOn5
            if(oSwitchesOn6) theDevices = oSwitchesOn6
            if(oSwitchesOn7) theDevices = oSwitchesOn7
            if(oSwitchesOn8) theDevices = oSwitchesOn8
            if(oSwitchesOn9) theDevices = oSwitchesOn9
            if(oSwitchesOn0) theDevices = oSwitchesOn0
            
            if(theDevices) {
                theDevices.each { it ->
                    if(it.hasCommand("setLevel")) {
                        if(logEnable) log.debug "In roomWarningHandler - working on ${it} - has setLevel: ${it.hasCommand("setLevel")}"                     
                        it.setLevel("${permanentDim}")
                        if(logEnable) log.debug "In roomWarningHandler - working on ${it} - setLevel to: ${warnLevel}"
                    }
                }
            }
        } else {
            int theTimeToDelay = state.timeDelayed ?: 5
            timeD = theTimeToDelay * 60
            runIn(timeD, roomWarningHandler)              
            if(logEnable) log.debug "In vacantHandler - Room Warning has been scheduled in ${theTimeToDelay} minute(s) (timeD: ${timeD})"
            if(useTile) { checkRoomTile("scheduled") }
        }
    } else {
        unschedule()
        occupancyHandler()
    }
}

def roomWarningHandler() {
    roomOverrideHandler()
    
    if(logEnable) log.debug "In roomWarningHandler (${state.version}) - roStatus: ${state.roStatus} - occupancy1: ${state.occupancy1} - occupancy2: ${state.occupancy2}"
    if(!state.roStatus && state.occupancy1 == "no" && state.occupancy2 == "no") {
        if(unSwitchesOff) {
            unSwitchesOff.each { it ->
                if(logEnable) log.debug "In roomWarningHandler - working on ${it}"
                if(it.currentValue("switch") == "on" || it.currentValue("switch") == "dim") {
                    if(logEnable) log.debug "In roomWarningHandler - working on ${it} - value: ${it.currentValue("switch")}"
                    if(it.hasCommand("setLevel")) {
                        if(logEnable) log.debug "In roomWarningHandler - working on ${it} - has setLevel: ${it.hasCommand("setLevel")}"
                        int currentLevel = it.currentValue("level")
                        int warnLevel = currentLevel / 2                       
                        it.setLevel("${warnLevel}")
                        if(logEnable) log.debug "In roomWarningHandler - working on ${it} - setLevel to: ${warnLevel}"
                    }
                }
            }
        }
        
        if(warningSwitches) warningSwitches.on()
        if(rMachine) rulesHandler(rMachine)
        if(omessage) messageHandler()

        if(useTheFlasher) {
            if(logEnable) log.debug "In roomWarningHandler - Sending Preset: ${flashPreset} to The Flasher device (${theFlasherDevice.displayName})"
            flashData = "Preset::${flashPreset}"
            theFlasherDevice.sendPreset(flashData)
        }
        
        if(logEnable) log.debug "In roomWarningHandler - Going to lightsHandler in 30 seconds"
        runIn(30, lightsHandler)
        if(useTile) { checkRoomTile("warning") }
    } else {
        unschedule()
        occupancyHandler()
    }
}

def lightsHandler() {
    roomOverrideHandler()    
    checkForSleep()
    
    if(logEnable) log.debug "In lightsHandler (${state.version}) - roStatus: ${state.roStatus} - occupancy1: ${state.occupancy1} - occupancy2: ${state.occupancy2} - sleeping: ${state.sleeping}"
    
    if(state.sleeping) {            
        if(unSwitchesOff) {
            unSwitchesOff.each { it ->
                it.off()
            }
        }
        if(unSwitchesOn) {
            unSwitchesOn.each { it ->
                it.on()
            }
        }
        if(warningSwitches) warningSwitches.off()
        if(useTile) { checkRoomTile("sleep") }
    } else {
        if(!state.roStatus && state.occupancy1 == "no" && state.occupancy2 == "no") {
            if(unSwitchesOff) {
                unSwitchesOff.each { it ->
                    it.off()
                }
            }
            if(unSwitchesOn) {
                unSwitchesOn.each { it ->
                    it.on()
                }
            }
            if(warningSwitches) warningSwitches.off()
            int repeatTime
            now = new Date()
            use( TimeCategory ) {
                newTime = now + repeatTime.seconds
            }
            runOnce(newTime, lightsHandler2)
            if(useTile) { checkRoomTile("unoccupied") }
        } else {
            unschedule()
            occupancyHandler()
        }
    }
}

def lightsHandler2() {
    roomOverrideHandler()
    
    if(logEnable) log.debug "In lightsHandler2 (${state.version}) - roStatus: ${state.roStatus} - occupancy1: ${state.occupancy1} - occupancy2: ${state.occupancy2}"
    if(!state.roStatus && state.occupancy1 == "no" && state.occupancy2 == "no") {
        if(unSwitchesOff) {
            unSwitchesOff.each { it ->
                it.off()
            }
        }
        if(unSwitchesOn) {
            unSwitchesOn.each { it ->
                it.on()
            }
        }
        if(warningSwitches) warningSwitches.off()               
        if(useTile) { checkRoomTile("off") }
    } else {
        unschedule()
        occupancyHandler()
    }
}

def modeHandler(){
    if(logEnable) log.debug "In modeHandler (${state.version}) - Mode: ${location.mode}"
    if(modeOverride) {
        moStatus = modeOverride.currentValue("switch")
        if(moStatus == "on") {
            state.modeNow = moMode
        } else {
            state.modeNow = location.mode
        }
    } else {
        state.modeNow = location.mode
    }
    state.matchFound = false
    
    for(x=0;x<10;x++) {
        if(x == 0) modeName = modeName0
        if(x == 1) modeName = modeName1
        if(x == 2) modeName = modeName2
        if(x == 3) modeName = modeName3
        if(x == 4) modeName = modeName4
        if(x == 5) modeName = modeName5
        if(x == 6) modeName = modeName6
        if(x == 7) modeName = modeName7
        if(x == 8) modeName = modeName8
        if(x == 9) modeName = modeName9
        
        modeName.each { it ->
            if(logEnable) log.debug "In modeHandler ${x} - Checking if ${state.modeNow} contains ${it}"
            if(state.modeNow.contains(it)){
                state.currentMode = "${x}"
                state.matchFound = true
                if(logEnable) log.debug "In modeHandler ${x} - Match Found - modeNow: ${state.modeNow}"
            }
        }
        if(!state.matchFound) if(logEnable) log.debug "In modeHandler ${x} - No Match Found"
    }
    
    if(logEnable) log.debug "In modeHandler - matchFound: ${state.matchFound}"
    if(state.matchFound) {
        unschedule()
        setScene()
    } else {
        if(logEnable) log.debug "In modeHandler - No match found."
    }
}

def setScene() {
	if(logEnable) log.debug "In setScene (${state.version}) - Mode is ${state.currentMode} - Mode: ${state.modeNow}"    

    if(state.currentMode == "1"){
        if(logEnable) log.debug "In setScene - 1: currentMode: ${state.currentMode}"
        if(sceneSwitch1) sceneSwitch1.push()
        if(oSwitchesOn1) oSwitchesOn1.on()
        if(oSwitchesOff1) oSwitchesOff1.off()
        if(oDimmers1) oDimmers1.setLevel(dimLevel1)
        if(timeDelayed1) state.timeDelayed = timeDelayed1
    } else if(state.currentMode == "2"){
        if(logEnable) log.debug "In setScene - 2: currentMode: ${state.currentMode}"
        if(sceneSwitch2) sceneSwitch2.push()
        if(oSwitchesOn2) oSwitchesOn2.on()
        if(oSwitchesOff2) oSwitchesOff2.off()
        if(oDimmers2) oDimmers2.setLevel(dimLevel2)
        if(timeDelayed2) state.timeDelayed = timeDelayed2
    } else if(state.currentMode == "3"){
        if(logEnable) log.debug "In setScene - 3: currentMode: ${state.currentMode}"
        if(sceneSwitch3) sceneSwitch3.push()
        if(oSwitchesOn3) oSwitchesOn3.on()
        if(oSwitchesOff3) oSwitchesOff3.off()
        if(oDimmers3) oDimmers3.setLevel(dimLevel3)
        if(timeDelayed3) state.timeDelayed = timeDelayed3
    } else if(state.currentMode == "4"){
        if(logEnable) log.debug "In setScene - 4: currentMode: ${state.currentMode}"
        if(sceneSwitch4) sceneSwitch4.push()
        if(oSwitchesOn4) oSwitchesOn4.on()
        if(oSwitchesOff4) oSwitchesOff4.off()
        if(oDimmers4) oDimmers4.setLevel(dimLevel4)
        if(timeDelayed4) state.timeDelayed = timeDelayed4
    } else if(state.currentMode == "5"){
        if(logEnable) log.debug "In setScene - 5: currentMode: ${state.currentMode}"
        if(sceneSwitch5) sceneSwitch5.push()
        if(oSwitchesOn5) oSwitchesOn5.on()
        if(oSwitchesOff5) oSwitchesOff5.off()
        if(oDimmers5) oDimmers5.setLevel(dimLevel5)
        if(timeDelayed5) state.timeDelayed = timeDelayed5
    } else if(state.currentMode == "6"){
        if(logEnable) log.debug "In setScene - 6: currentMode: ${state.currentMode}"
        if(sceneSwitch6) sceneSwitch6.push()
        if(oSwitchesOn6) oSwitchesOn6.on()
        if(oSwitchesOff6) oSwitchesOff6.off()
        if(oDimmers6) oDimmers6.setLevel(dimLevel6)
        if(timeDelayed6) state.timeDelayed = timeDelayed6
    } else if(state.currentMode == "7"){
        if(logEnable) log.debug "In setScene - 7: currentMode: ${state.currentMode}"
        if(sceneSwitch7) sceneSwitch7.push()
        if(oSwitchesOn7) oSwitchesOn7.on()
        if(oSwitchesOff7) oSwitchesOff7.off()
        if(oDimmers7) oDimmers7.setLevel(dimLevel7)
        if(timeDelayed7) state.timeDelayed = timeDelayed7
    } else if(state.currentMode == "8"){
        if(logEnable) log.debug "In setScene - 8: currentMode: ${state.currentMode}"
        if(sceneSwitch8) sceneSwitch8.push()
        if(oSwitchesOn8) oSwitchesOn8.on()
        if(oSwitchesOff8) oSwitchesOff8.off()
        if(oDimmers8) oDimmers8.setLevel(dimLevel8)
        if(timeDelayed8) state.timeDelayed = timeDelayed8
    } else if(state.currentMode == "9"){
        if(logEnable) log.debug "In setScene - 9: currentMode: ${state.currentMode}"
        if(sceneSwitch9) sceneSwitch9.push()
        if(oSwitchesOn9) oSwitchesOn9.on()
        if(oSwitchesOff9) oSwitchesOff9.off()
        if(oDimmers9) oDimmers9.setLevel(dimLevel9)
        if(timeDelayed9) state.timeDelayed = timeDelayed9
    } else if(state.currentMode == "0"){
        if(logEnable) log.debug "In setScene - 0: currentMode: ${state.currentMode}"
        if(sceneSwitch0) sceneSwitch0.push()
        if(oSwitchesOn0) oSwitchesOn0.on()
        if(oSwitchesOff0) oSwitchesOff0.off()
        if(oDimmers0) oDimmers0.setLevel(dimLevel0)
        if(timeDelayed0) state.timeDelayed = timeDelayed0
    } else if(state.currentMode == "NONE"){
        if(logEnable) log.debug "In setScene - Something went wrong, no Mode matched!"
    }
    
    if(logEnable) log.debug "In setScene - currentMode: ${state.currentMode} - timeDelayed: ${state.timeDelayed}"
    if(useTile) { checkRoomTile("occupied") }
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
	def ovalues = "${omessage}".split(";")
	ovSize = ovalues.size()
	ocount = ovSize.toInteger()
    def orandomKey = new Random().nextInt(ocount)

	theMessage = ovalues[orandomKey]
	if(logEnable) log.debug "In messageHandler - Random - ovSize: ${ovSize}, orandomKey: ${orandomKey}"
	
    if(logEnable) log.debug "In messageHandler - going to letsTalk and/or pushNow with: ${theMessage}"
    if(sendPushMessage) pushNow(theMessage)
    if(omessage) letsTalk(theMessage)
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(theMsg)
    }
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def rulesHandler(rules) {
    if(logEnable) log.debug "In rulesHandler - rules: ${rules}"
    RMUtils.sendAction(rules, "runRule", app.label)
}

def pushNow(msg) {
	if(logEnable) log.debug "In pushNow (${state.version})"
	if(sendPushMessage) {
		pushMessage = "${app.label} \n"
		pushMessage += msg
		if(logEnable) log.debug "In pushNow - Sending message: ${pushMessage}"
        sendPushMessage.deviceNotification(pushMessage)
	}	
}

def dayOfTheWeekHandler() {
	if(logEnable) log.debug "In dayOfTheWeek (${state.version})"
    state.daysMatch = false
    if(days) {
        def df = new java.text.SimpleDateFormat("EEEE")
        df.setTimeZone(location.timeZone)
        def day = df.format(new Date())
        def dayCheck = days.contains(day)

        if(dayCheck) {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Passed"
            state.daysMatch = true
        } else {
            if(logEnable) log.debug "In dayOfTheWeekHandler - Days of the Week Check Failed"
            state.daysMatch = false
        }
    } else {
        state.daysMatch = true
    }
    if(logEnable) log.debug "In dayOfTheWeekHandler - daysMatch: ${state.daysMatch}"
}

// ** Start Sunset to Sunrise code - Modified from Smartthings docs
def sunsetTimeHandler(evt) {
    if(logEnable) log.debug "In sunsetTimeHandler (${state.version})"
    scheduleWithOffset(evt.value, sunsetOffsetValue, sunsetOffsetDir, "sunsetHandler")
}

def sunriseTimeHandler(evt) {
    if(logEnable) log.debug "In sunriseTimeHandler (${state.version})"
    scheduleWithOffset(evt.value, sunriseOffsetValue, sunriseOffsetDir, "sunriseHandler")
}

def scheduleWithOffset(nextSunriseSunsetTime, offset, offsetDir, handlerName) {
    if(logEnable) log.debug "In scheduleWithOffset (${state.version}) - nextSunriseSunsetTime: ${nextSunriseSunsetTime} - offset: ${offset} - offsetDir: ${offsetDir}"
    def nextSunriseSunsetTimeDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss.SSSZ", nextSunriseSunsetTime)
    def offsetTime = new Date(nextSunriseSunsetTimeDate.time + getOffset(offset, offsetDir))

    if(logEnable) log.debug "In scheduleWithOffset - Scheduling Sunrise/Sunset for $offsetTime"
    runOnce(offsetTime, handlerName, [overwrite: false])
}

def sunriseHandler() {
    if(logEnable) log.debug "In sunriseHandler (${state.version})"
    if(sunRestriction) {
        state.sunRiseTosunSet = false
    } else {
        state.sunRiseTosunSet = true
    }
    if(logEnable) log.debug "In sunriseHandler - sunRiseTosunSet: ${state.sunRiseTosunSet}"
}

def sunsetHandler() {
    if(logEnable) log.debug "In sunsetHandler (${state.version})"
    if(sunRestriction) {
        state.sunRiseTosunSet = true
    } else {
        state.sunRiseTosunSet = true
    }
    if(logEnable) log.debug "In sunsetHandler - sunRiseTosunSet: ${state.sunRiseTosunSet}"
}

private getOffset(String offsetValue, String offsetDir) {
    def timeOffsetMillis = calculateTimeOffsetMillis(offsetValue)
    if (offsetDir == "Before") {
        return -timeOffsetMillis
    }
    return timeOffsetMillis
}

private calculateTimeOffsetMillis(String offset) {
    def result = 0
    if (!offset) {
        return result
    }
    def before = offset.startsWith('-')
    if (before || offset.startsWith('+')) {
        offset = offset[1..-1]
    }
    if (offset.isNumber()) {
        result = Math.round((offset as Double) * 60000L)
    } else if (offset.contains(":")) {
        def segs = offset.split(":")
        result = (segs[0].toLong() * 3600000L) + (segs[1].toLong() * 60000L)
    }
    if (before) {
        result = -result
    }
    result
}
// ** end Sunrise to Sunset code

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if(fromTime) {
        if(midnightCheckR) {
            state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime)+1, new Date(), location.timeZone)
        } else {
		    state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
        }
		if(state.betweenTime) {
            if(logEnable) log.debug "In checkTime - Time within range - Don't Speak"
			state.timeBetween = true
		} else {
            if(logEnable) log.debug "In checkTime - Time outside of range - Can Speak"
			state.timeBetween = false
		}
  	} else {  
        if(logEnable) log.debug "In checkTime - NO Time Restriction Specified"
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def roomOverrideHandler() {
    if(logEnable) log.debug "In roomOverrideHandler (${state.version})"
    state.roStatus = false
    if(roomOverride) {
        roomOverride.each { it ->
            roStat = it.currentValue("switch")
            if(roStat == "on") state.roStatus = true
        }
    }
    if(logEnable) log.debug "In roomOverrideHandler - roStatus: ${state.roStatus}"
}

def checkRoomTile(data) {
    if(useTile) {
        if(logEnable) log.trace "In checkRoomTile (${state.version}) - data: ${data}"

        if(data == "scheduled") {
            state.lightingStatus = "scheduled"
            now = new Date()
            int theTimeToDelay = state.timeDelayed ?: 5
            use( TimeCategory ) { theDate = now + theTimeToDelay.minutes }
            if(logEnable) log.trace "In checkRoomTile - Scheduled - ${theTimeToDelay} from now - theDate: ${theDate}"
            makeTileHandler(theDate)
        } else if(data == "warning") {
            state.lightingStatus = "warning"
            now = new Date()
            int thirty = 30
            use( TimeCategory ) { theDate = now + thirty.seconds }
            if(logEnable) log.trace "In checkRoomTile - Warning - 30 seconds from now - theDate: ${theDate}"
            makeTileHandler(theDate)
        } else if(data == "sleep") {
            dataDevice.settingStatus("sleep")
            if(sleepDeviceContact) theDate = new Date()
            if(sleepDevicePresense) theDate = new Date()
            if(sleepDeviceSwitch) theDate = new Date()
            if(logEnable) log.trace "In checkRoomTile - Sleeping - theDate: ${theDate}"
        } else if(data == "unoccupied") {
            state.roomStatus = "unoccupied"
            dataDevice.settingStatus("unoccupied")
            theDate = new Date()
            if(logEnable) log.trace "In checkRoomTile - Unoccupied - theDate: ${theDate}"
            makeTileHandler(theDate)
        } else if(data == "off") {
            state.roomStatus = "unoccupied"
            state.lightingStatus = "off"
            theDate = new Date()
            if(logEnable) log.trace "In checkRoomTile - Off - theDate: ${theDate}"
            makeTileHandler(theDate)
        } else if(data == "occupied") {
            state.roomStatus = "occupied"
            state.lightingStatus = "on"
            dataDevice.settingStatus("occupied")
            theDate = new Date()
            if(logEnable) log.trace "In checkRoomTile - Occupied - theDate: ${theDate}"
            makeTileHandler(theDate)
        } else {
            state.roomStatus = "unknown"
            theDate = new Date()
            if(logEnable) log.trace "In checkRoomTile - Unknown - theDate: ${theDate}"
            makeTileHandler(theDate)
        }
    }
}

def makeTileHandler(theDate) {
    if(logEnable) log.trace "In makeTileHandler (${state.version}) - theDate: ${theDate}"
    roomOverrideHandler()

    if(roomTemp) {
        theTemp = roomTemp.currentValue("temperature")
    } else theTemp = ""

    if(roomHumidity) {
        theHumidity = roomHumidity.currentValue("humidity")
    } else theHumidity = ""

    try {
        if(logEnable) log.trace "In makeTileHandler - Trying Parse 1"
        prev = Date.parse("mm-dd hh:mm:ss a","${theDate}")
        if(logEnable) log.trace "In makeTileHandler - Parse 1 - Worked"
    } catch (e1) {
        try {
            if(logEnable) log.trace "In makeTileHandler - Trying Parse 2"
            prev = Date.parse("yyy-MM-dd'T'HH:mm:ssZ","${theDate}".replace("+00:00","+0000"))
            if(logEnable) log.trace "In makeTileHandler - Parse 2 - Worked"
        } catch (e2) {
            if(logEnable) log.trace "In makeTileHandler - Parse 3 - Using raw Data - theDate ***********"   
            prev = theDate
        }
    }

    String newPrev = prev.format( 'MM-dd - hh:mm:ss a' )

    if(logEnable) log.trace "In makeTileHandler - newPrev: ${newPrev}"

    theTile =  "<table align=center width=100%>"
    theTile += "<tr align=center><td colspan=2><table align=center width=100%><tr align=center><td width=20%><small>${theTemp}</small><td><b>${tileName}</b><td width=20%><small>${theHumidity}</small></table>"
    theTile += "<tr><td>Room Status: <td> ${state.roomStatus}"
    theTile += "<tr><td>Light Status: <td> ${state.lightingStatus}"

    if(state.lightingStatus == "on") {
        theTile += "<tr><td>Since: <td><small>${newPrev}</small>"
    } else if(state.lightingStatus == "scheduled") {
        theTile += "<tr><td>Warning at: <td><small>${newPrev}</small>"
    } else if(state.lightingStatus == "warning") {
        theTile += "<tr><td>Off at: <td><small>${newPrev}</small>"
    } else if(state.lightingStatus == "off") {
        theTile += "<tr><td>Since: <td><small>${newPrev}</small>"
    } else {
        theTile += "<tr><td>Status<td> Not Available"
    }

    if(roomOverride) theTile += "<tr><td>Override: <td> ${state.roStatus}"
    theNow = new Date()
    theTile += "<tr align=center><td colspan=2><small>${theNow}</small>"
    theTile += "</table>"
    dataDevice.createTile(theTile)
    if(logEnable) log.trace "In makeTileHandler - Finished"
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Room Director Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Room Director unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable) log.debug "In setDefaults (${state.version})"
    if(state.roStatus == null || state.roStatus == "") state.roStatus = false
    if(state.occupancy1 == null || state.occupancy1 == "") state.occupancy1 = "no"
    if(state.occupancy2 == null || state.occupancy2 == "") state.occupancy2 = "no"
    if(state.sunRiseTosunSet == null || state.sunRiseTosunSet == "") state.sunRiseTosunSet = true
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
