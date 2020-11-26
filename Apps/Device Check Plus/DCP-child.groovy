/**
 *  **************** Device Check Plus Child App  ****************
 *
 *  Design Usage:
 *  Check selected devices, then warn you what's not in the right state.
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
 *  1.1.9 - 11/24/20 - Added more logging
 *  1.1.8 - 08/30/20 - Fixed a typo, other changes
 *  1.1.7 - 08/23/20 - No wrong state device Notification is now optional
 *  1.1.6 - 06/22/20 - Changes to letsTalk
 *  1.1.5 - 06/13/20 - Fixed letsTalk typo
 *  1.1.4 - 06/13/20 - Cosmetic changes
 *  1.1.3 - 06/11/20 - All speech now goes through Follow Me
 *  1.1.2 - 06/07/20 - More options added to 'Actions'
 *  1.1.1 - 06/05/20 - Fixed deviceNotTriggeredHandler, other minor changes
 *  1.1.0 - 04/27/20 - Cosmetic changes
 *  ***
 *  1.0.0 - 10/13/19 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Device Check Plus"
	state.version = "1.1.9"
}

definition(
    name: "Device Check Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Check selected devices, then warn you what's not in the right state.",
    category: "Convenience",
	parent: "BPTWorld:Device Check Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Check%20Plus/DCP-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "checkConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "actionConfig", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "triggerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "notificationOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "speechOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Check selected devices, then warn you what's not in the right state."
            paragraph "<b>Examples of Usage:</b>"
            paragraph " - <u>Getting ready to go to bed</u><br> * hit the 'On demand' switch (or use Google to turn it on)<br> * Check will run and announce any problems!<br> * Go to bed knowing everything is secure!"
            paragraph " - <u>Heat is on</u><br> * Someone opens a window or door<br> * Check will run and announce what window is open!"
            paragraph " - <u>Cool is on</u><br> * Someone closes a door<br> * Check will run and announce that the door should be open when cool is on!"
            paragraph " - <u>Other usage...</u><br> * Going out? Make sure all your windows are closed<br> * Is it raining, check the windows!<br> * Think you forgot to do something? This will let you know!"
            paragraph " - <u>Power...</u><br> * Power plug is below 5, announce that the washer or dryer is finished!"
            paragraph "<b>The only limit is your imagination!</b>"
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Options")) {
            if(onDemandSwitch || days || modeName || thermostats || powerEvent || humidityEvent || tempEvent || useTime) {
                href "triggerOptions", title:"${getImage("optionsGreen")} Select Trigger", description:"Click here for Options"
            } else {
                href "triggerOptions", title:"${getImage("optionsRed")} Select Trigger", description:"Click here for Options"
            }
            
            if(switchesOn || switchesOff || contactsOpen || contactsClosed || locksLocked || locksUnlocked) {
                href "checkConfig", title:"${getImage("optionsGreen")} Select Devices", description:"Click here for Options"
            } else {
                href "checkConfig", title:"${getImage("optionsRed")} Select Devices", description:"Click here for Options"
            }
            
            if(switchesToTurnOn || switchesToTurnOff || switchesToFlash) {
                href "actionConfig", title:"${getImage("optionsGreen")} Select Actions", description:"Click here for Options"
            } else {
                href "actionConfig", title:"${getImage("optionsRed")} Select Actions", description:"Click here for Options"
            }
            
            if(isDataDevice || preMsg || postMsg) {
                href "notificationOptions", title:"${getImage("optionsGreen")} Select Notifications", description:"Click here for Options"
            } else {
                href "notificationOptions", title:"${getImage("optionsRed")} Select Notifications", description:"Click here for Options"
            }
            
            if(fmSpeaker) {
                href "speechOptions", title:"${getImage("optionsGreen")} Select Speech options", description:"Click here for Options"
            } else {
                href "speechOptions", title:"${getImage("optionsRed")} Select Speech options", description:"Click here for Options"
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
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def triggerOptions() {
    dynamicPage(name: "triggerOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Trigger Options")) {
            paragraph "This will check if a device is not in the right state (ie. Open when it should be closed)"
            input "useState", "bool", title: "Use Device State as Trigger", description: "use State", submitOnChange:true, defaultValue:false
            
            input "useTime", "bool", title: "Use Device Time in State as Trigger", description: "use Time", submitOnChange:true, defaultValue:false
            paragraph "<hr>"
        }
        
        if(useState) { 
            section(getFormat("header-green", "${getImage("Blank")}"+" Select Trigger Type")) {
                input "triggerType1", "enum", title: "Tigger", required: true, multiple: false, submitOnChange: true, options: [
                    ["xDay":"Days/Time to Run"],
                	["xHumidity":"Humidity Activity"],
                    ["xMode":"Mode Changes"],
                    ["xOnDemand":"On Demand"],
                    ["xTemp":"Temperature Activity"],
                    ["xTherm":"Thermostat Activity"],
                    ["xPower":"Power Activity"]
                ]
                paragraph "<hr>"
		    }
               
            if(triggerType1 == "xOnDemand") {
                section() {
                    paragraph "<b>Run 'Device Check' anytime this switch is turned on.</b> Recommended to create a 'virtual switch' with 'Enable auto off' set to '1s'"
                    input "createDevice", "bool", title: "Create Device needed for On Demand?", defaultValue: false, submitOnChange: true
                    if(createDevice) {
                        input "userName", "text", title: "Enter a name for this Device (ie. 'Kitchen' will become 'DCP - Kitchen')", required:true, submitOnChange:true
                        paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                        if(userName) createChildDevice()
                        if(statusMessage == null) statusMessage = "Waiting on status message..."
                        paragraph "${statusMessage}"
                        input "onDemandSwitch", "capability.switch", title: "On Demand Switch just created for you", required:true
                    } else {
                        input "onDemandSwitch", "capability.switch", title: "On Demand Switch", required:false
                    }
                }  
            }
            
            if(triggerType1 == "xDay") {
                section() {
                    paragraph "<b>Run 'Device Check' on a set schedule</b>"
                    input "days", "enum", title: "Only run on these days", description: "Days to run", required:false, multiple:true, submitOnChange:true, options: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"]
                    input "timeToRun", "time", title: "Auto Run at", required:true
                }
            }
                
            if(triggerType1 == "xMode") {
                section() {
                    paragraph "<b>Run 'Device Check' on Mode Changes</b> (optional)"
                    input "modeName", "mode", title: "When hub changes to this Mode", required:false, multiple:true
                }
            }
                
            if(triggerType1 == "xTherm") {
                section() {
                    paragraph "<b>Run 'Device Check' on Thermostat Activity</b> (optional)"
                    input "thermostats", "capability.thermostat", title: "Thermostat to track", required:false, multiple:true, submitOnChange:true
                    if(thermostats) {
                        input "thermOption", "bool", title: "Use Mode or State (off=Mode, on=State)", description: "Therm Options", defaultValue:true, submitOnChange:true
                        paragraph " - <b>Mode</b>: When in heat or cool mode, it will trigger a 'Device Check' anytime a selected device changes state."
                        paragraph " - <b>State</b>: This will trigger a 'Device Check' anytime the thermostat goes into heating or cooling state."
                    }
                }
            }
                
            if(triggerType1 == "xPower") {
                section() {
                    paragraph "<b>Run 'Device Check' on Power Activity</b> (optional)"
                    input "powerEvent", "capability.powerMeter", title: "Power Devices to monitor", required:false, multiple:true, submitOnChange:true
                    if(powerEvent) {
				        input "oSetPointHigh", "bool", defaultValue: "false", title: "<b>Trigger when Power is too High?</b>", description: "Power High", submitOnChange:true
				        if(oSetPointHigh) input "setPointHigh", "number", title: "Power High Setpoint", required:true, defaultValue: 75, submitOnChange:true
				        input "oSetPointLow", "bool", defaultValue: "false", title: "<b>Trigger when Power is too Low?</b>", description: "Power Low", submitOnChange:true
				        if(oSetPointLow) input "setPointLow", "number", title: "Power Low Setpoint", required:true, defaultValue: 30, submitOnChange:true

			            if(oSetPointHigh) paragraph "You will recieve notifications if Power reading is above ${setPointHigh}"
				        if(oSetPointLow) paragraph "You will recieve notifications if Power reading is below ${setPointLow}"
				        input "oSwitchTime", "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange:true
				        if(oSwitchTime) {
					        paragraph "Delay the notification until the device has been in state for XX minutes."
					        input "notifyDelay", "number", title: "Delay (1 to 60)", required: true,multiple:false, range: '1..60'
				        }
			        } 
                }
            }
                
            if(triggerType1 == "xHumidity") {
                section() {
                    paragraph "<b>Run 'Device Check' on Humidity Activity</b> (optional)"
                    input "humidityEvent", "capability.relativeHumidityMeasurement", title: "Humidity Devices to monitor", required:false, multiple:true, submitOnChange:true
                    if(humidityEvent) {
				        paragraph "<b>by Humidity Level</b>"
				        input "oSetPointHigh", "bool", defaultValue: "false", title: "<b>Trigger when Humidity is too High?</b>", description: "Humidity High", submitOnChange:true
				        if(oSetPointHigh) input "setPointHigh", "number", title: "Humidity High Setpoint", required: true, defaultValue: 75, submitOnChange: true
				        input "oSetPointLow", "bool", defaultValue: "false", title: "<b>Trigger when Humidity is too Low?</b>", description: "Humidity Low", submitOnChange:true
				        if(oSetPointLow) input "setPointLow", "number", title: "Humidity Low Setpoint", required: true, defaultValue: 30, submitOnChange: true
				        if(humidityEvent) {
					        if(oSetPointHigh) paragraph "You will recieve notifications if Humidity reading is above ${setPointHigh}"
					        if(oSetPointLow) paragraph "You will recieve notifications if Humidity reading is below ${setPointLow}"
					        input "oSwitchTime", "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange:true
					        if(oSwitchTime) {
						        paragraph "Delay the notification until the device has been in state for XX minutes."
						        input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					        }
				        }
                    }
                }
            }
                
            if(triggerType1 == "xTemp") {
                section() {
                    paragraph "<b>Run 'Device Check' on Temperature Activity</b> (optional)"
                    input "tempEvent", "capability.temperatureMeasurement", title: "Temperature Devices to monitor", required:false, multiple:true, submitOnChange:true
                    if(tempEvent) {
				        paragraph "<b>by Temperature</b>"
				        input "oSetPointHigh", "bool", defaultValue: "false", title: "<b>Trigger when Temperature is too High?</b>", description: "Temp High", submitOnChange:true
				        if(oSetPointHigh) input "setPointHigh", "number", title: "Temperature High Setpoint", required: true, defaultValue: 75, submitOnChange: true
				        input "oSetPointLow", "bool", defaultValue: "false", title: "<b>Trigger when Temperature is too Low?</b>", description: "Temp Low", submitOnChange:true
				        if(oSetPointLow) input "setPointLow", "number", title: "Temperature Low Setpoint", required: true, defaultValue: 30, submitOnChange: true
				        if(tempEvent) {
					        if(oSetPointHigh) paragraph "You will recieve notifications if Temperature reading is above ${setPointHigh}"
					        if(oSetPointLow) paragraph "You will recieve notifications if Temperature reading is below ${setPointLow}"
					        input "oSwitchTime", "bool", defaultValue: "false", title: "<b>Set Delay?</b>", description: "Switch Time", submitOnChange:true
					        if(oSwitchTime) {
						        paragraph "Delay the notification until the device has been in state for XX minutes."
						        input "notifyDelay", "number", title: "Delay (1 to 60)", required: true, multiple: false, range: '1..60'
					        }
				        }
			        }
                }
            }
        }
            
        if(useTime) {
            section() {
                paragraph "<b>Time Triggers</b>"
                input "timeInState", "number", title: "How many minutes should the device be in state before notification", defaultValue: 2, required: true, submitOnChange: true
            }
        }
    }
}

def checkConfig() {
    dynamicPage(name: "checkConfig", title: "", install:false, uninstall:false) {
        display()
        if(triggerType1 == "xOnDemand" || triggerType1 == "xDay" || triggerType1 == "xMode" || triggerType1 == "xTherm" || useTime) {
		    section(getFormat("header-green", "${getImage("Blank")}"+" Devices to Check")) {
                paragraph "<b>Select your devices from the options below</b>"
			    input "switchesOn", "capability.switch", title: "Switches that should be ON", multiple:true, required:false
			    input "switchesOff", "capability.switch", title: "Switches that should be OFF", multiple:true, required:false
			    input "contactsOpen", "capability.contactSensor", title: "Contact Sensors that should be OPEN", multiple:true, required:false
			    input "contactsClosed", "capability.contactSensor", title: "Contact Sensors that should be CLOSED", multiple:true, required:false
			    input "locksLocked", "capability.lock", title: "Door Locks that should be LOCKED", multiple:true, required:false
			    input "locksUnlocked", "capability.lock", title: "Door Locks that should be UNLOCKED", multiple:true, required:false
                
                input "tryToFix", "bool", title: "Should DCP try to 'fix' the devices for you?<br><small>* Only for Switches and Door Locks.</small>", submitOnChange: true
                if(tryToFix) {
                    paragraph "With this option:<br> - When triggered, DCP will run and try to 'fix' the devices selected.<br> - After a 5 second wait, it will run a second time, again trying to 'fix' the devices.<br> - Then on the third time, If any devices are still found to be in the wrong state, it will activate the notification options for the devices."
                }
		    }
        }
    }
}

def actionConfig() {
    dynamicPage(name: "actionConfig", title: "", install:false, uninstall:false) {
        display()        
        section(getFormat("header-green", "${getImage("Blank")}"+" Actions")) {
            paragraph "<b>If anything was in the wrong state</b>"
			input "switchesToTurnOn", "capability.switch", title: "Switches to Turn ON", multiple:true, required:false
			input "switchesToTurnOff", "capability.switch", title: "Switches to Turn OFF", multiple:true, required:false

			input "switchesToFlash", "capability.switch", title: "Flash these lights", multiple: true
		    input "numOfFlashes", "number", title: "Number of times (default: 2)", required: false, width: 6
            input "delayFlashes", "number", title: "Milliseconds for lights to be on/off (default: 500 - 500=.5 sec, 1000=1 sec)", required: false, width: 6
            
            paragraph "<b>If everything was in the correct state</b>"
			input "switchesToTurnOnC", "capability.switch", title: "Switches to Turn ON", multiple:true, required:false
			input "switchesToTurnOffC", "capability.switch", title: "Switches to Turn OFF", multiple:true, required:false
        }
    }
}
    
def notificationOptions() {
    dynamicPage(name: "notificationOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Devices Found Notification Options")) {
            input "isDataDevice", "capability.switch", title: "Turn this device on if there are devices to report", submitOnChange:true, required:false, multiple:false
			paragraph "<hr>"
            paragraph "Receive device notifications with voice and push options. Each of the following messages will only be spoken if necessary."
			
			input "preMsg", "text", title: "Random Pre Message - Separate each message with <b>;</b> (semicolon)", required:true, submitOnChange:true
			input "oPreList", "bool", defaultValue:false, title: "Show a list view of the random pre messages?", description: "List View", submitOnChange:true
			if(oPreList) {
				def valuesPre = "${preMsg}".split(";")
				listMapPre = ""
    			valuesPre.each { itemPre -> listMapPre += "${itemPre}<br>" }
				paragraph "${listMapPre}"
			}
            paragraph "<hr>"
            
            if(triggerType1 == "xOnDemand" || triggerType1 == "xDay" || triggerType1 == "xMode" || triggerType1 == "xTherm" || useTime) paragraph "<b>All switches/contacts/locks in the wrong state will then be spoken</b>"
            
            if(triggerType1 == "xPower" || triggerType1 == "xHumidity" || triggerType1 == "xTemp") {
                input "speakDevice", "bool", defaultValue:false, title: "Speak name of device?", description: "Power Device", submitOnChange:true
                if(speakDevice) {
                    paragraph "<b>Device name will then be spoken</b>"
                } else {
                    paragraph "<b>Device name will NOT be spoken</b>"
                }
            }
            
            paragraph "<hr>"
			input "postMsg", "text", title: "Random Post Message - Separate each message with <b>;</b> (semicolon)", required: true, submitOnChange:true
			input "oPostList", "bool", defaultValue:false, title: "Show a list view of the random post messages?", description: "List View", submitOnChange:true
			if(oPostList) {
				def valuesPost = "${postMsg}".split(";")
				listMapPost = ""
    			valuesPost.each { itemPost -> listMapPost += "${itemPost}<br>" }
				paragraph "${listMapPost}"
			}
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" No Devices found Notification Options")) {
            paragraph "Receive notifications with voice and push options. This will only notify if there are no devices in the wrong state."
			
            input "useNoDeviceMsg", "bool", title: "Use No wrong state device notifications", defaultValue:false, submitOnChange:true
            if(useNoDeviceMsg) {
                input "noDeviceMsg", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)", required:true, submitOnChange:true
                input "oNoList", "bool", defaultValue:false, title: "Show a list view of the random pre messages?", description: "List View", submitOnChange:true
                if(oNoList) {
                    def valuesNo = "${noDeviceMsg}".split(";")
                    listMapNo = ""
                    valuesNo.each { itemNo -> listMapNo += "${itemNo}<br>" }
                    paragraph "${listMapNo}"
                }
            }
        }
    }
}

def speechOptions() {
    dynamicPage(name: "speechOptions", title: "", install:false, uninstall:false) {
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
    	}
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

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        if(triggerType1 == "xOnDemand" && onDemandSwitch) subscribe(onDemandSwitch, "switch.on", checkDeviceHandler)
        if(triggerType1 == "xDay" && timeToRun) schedule(timeToRun, checkDeviceHandler)
        if(triggerType1 == "xMode" && modeName) subscribe(location, "mode", modeHandler)
        if(triggerType1 == "xTherm" && thermostats && thermOption == true) subscribe(thermostats, "thermostatOperatingState.heating", thermostatHandler)
        if(triggerType1 == "xTherm" && thermostats && thermOption == true) subscribe(thermostats, "thermostatOperatingState.cooling", thermostatHandler) 
        if(triggerType1 == "xTherm" && thermostats && thermOption == false) subscribe(contactsOpen, "contact", thermostatModeHandler)
        if(triggerType1 == "xTherm" && thermostats && thermOption == false) subscribe(contactsClosed, "contact", thermostatModeHandler)
        if(triggerType1 == "xPower" && powerEvent) subscribe(powerEvent, "power", setPointHandler)
        if(triggerType1 == "xHumidity" && humidityEvent) subscribe(humidityEvent, "humidity", setPointHandler)
        if(triggerType1 == "xTemp" && tempEvent) subscribe(tempEvent, "temperature", setPointHandler)

        if(useTime) {
            if(contactsOpen) subscribe(contactsOpen, "contact", checkTimeInState)
            if(contactsClosed) subscribe(contactsClosed, "contact", checkTimeInState)

            if(switchesOn) subscribe(switchesOn, "switch", checkTimeInState)
            if(switchesOff) subscribe(switchesOff, "switch", checkTimeInState)

            if(locksLocked) subscribe(locksLocked, "lock", checkTimeInState)
            if(locksUnlocked) subscribe(locksUnlocked, "lock", checkTimeInState)
        }
    }
}

def checkDeviceHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkDeviceHandler (${state.version})"
        state.wrongSwitchesMSG = ""
        state.wrongLocksMSG = ""
        maxCheck = 3
        if(state.round == null) state.round = 1
        int x = state.round

        if(!tryToFix) { x=maxCheck }
        somethingWrong = false
        if(logEnable) log.info "Pass: ${x} - round: ${state.round}"

        if(switchesOn) {
            switchesOn.each { sOn -> 
                switchName = sOn.displayName
                switchStatus = sOn.currentValue("switch")
                if(logEnable) log.debug "In checkDeviceHandler - Switch On - CHECKING - ${switchName} - ${switchStatus}"
                if(switchStatus == "off") {
                    if(x == maxCheck) state.wrongSwitchesMSG += "${switchName}, "
                    if(x < maxCheck && tryToFix) {
                        sOn.on()
                        somethingWrong = true
                        if(logEnable) log.debug "In checkDeviceHandler - Device: ${switchName} is in the wrong state, will try to fix."
                    }
                } else {
                    if(logEnable) log.debug "In checkDeviceHandler - Device: ${switchName} is good!"
                }
            }
        }

        if(switchesOff) {
            switchesOff.each { sOff -> 
                switchName = sOff.displayName
                switchStatus = sOff.currentValue("switch")
                if(logEnable) log.debug "In checkDeviceHandler - Switch Off - ${switchName} - ${switchStatus}"
                if(switchStatus == "on") {
                    if(x == maxCheck) state.wrongSwitchesMSG += "${switchName}, "
                    if(x < maxCheck && tryToFix) {
                        sOff.off()
                        somethingWrong = true
                        if(logEnable) log.debug "In checkDeviceHandler - Device: ${switchName} is in the wrong state, will try to fix."
                    }
                } else {
                    if(logEnable) log.debug "In checkDeviceHandler - Device: ${switchName} is good!"
                }
            }
        }

        if(locksUnlocked) {
            locksUnlocked.each { lUnlocked ->
                def lockName = lUnlocked.displayName
                def lockStatus = lUnlocked.currentValue("lock")
                if(logEnable) log.debug "In checkDeviceHandler - Locks Unlocked - ${lockName} - ${lockStatus}"
                if(lockStatus == "locked") {
                    if(x == maxCheck) state.wrongLocksMSG += "${lockName}, "
                    if(x <= maxCheck && tryToFix) {
                        lUnlocked.unlock()
                        somethingWrong = true
                        if(logEnable) log.debug "In checkDeviceHandler - Lock: ${lockName} is in the wrong state, will try to fix."
                    }
                }
            }
        }

        if(locksLocked) {
            locksLocked.each { lLocked ->
                def lockName = lLocked.displayName
                def lockStatus = lLocked.currentValue("lock")
                if(logEnable) log.debug "In checkDeviceHandler - Locks Locked - ${lockName} - ${lockStatus}"
                if(lockStatus == "unlocked") {
                    if(x == maxCheck) state.wrongLocksMSG += "${lockName}, "
                    if(x <= maxCheck && tryToFix) {
                        lLocked.lock()
                        somethingWrong = true
                        if(logEnable) log.debug "In checkDeviceHandler - Lock: ${lockName} is in the wrong state, will try to fix."
                    }
                }
            }
        }

        if(somethingWrong && x < maxCheck) {
            if(logEnable) log.debug "In checkDeviceHandler - Device was in wrong state.  Please wait..."
            x=x+1
            state.round = x
            runIn(5,checkDeviceHandler)
        } else {
            state.round = null
            checkContactHandler()
        }
    }
}

def checkContactHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkContactHandler (${state.version})"
        state.wrongContactsMSG = ""

        if(contactsOpen) {
            contactsOpen.each { cOpen ->
                def contactName = cOpen.displayName
                def contactStatus = cOpen.currentValue("contact")
                if(logEnable) log.debug "In checkContactHandler - Contact Open - ${contactName} - ${contactStatus}"
                if(contactStatus == "closed") state.wrongContactsMSG += "${contactName}, "
            }
        }
        if(contactsClosed) {
            contactsClosed.each { cClosed ->
                def contactName = cClosed.displayName
                def contactStatus = cClosed.currentValue("contact")
                if(logEnable) log.debug "In checkContactHandler - Contact Closed - ${contactName} - ${contactStatus}"
                if(contactStatus == "open") state.wrongContactsMSG += "${contactName}, "
            }
        }

        // Is there Data
        if((state.wrongSwitchesMSG != "") || (state.wrongContactsMSG != "") || (state.wrongLocksMSG != "")) {
            if(isDataDevice) { isDataDevice.on() }
            state.isData = "yes"
            deviceTriggeredHandler()
        }
        if((state.wrongSwitchesMSG == "") && (state.wrongContactsMSG == "") && (state.wrongLocksMSG == "")) {
            if(isDataDevice) { isDataDevice.off() }
            state.isData = "no"
            deviceNotTriggeredHandler()
        }
        messageHandler()
    }
}
    
def deviceTriggeredHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In deviceTriggeredHandler (${state.version})"
        if(switchesToTurnOn) {
            switchesToTurnOn.each { it ->
                if(logEnable) log.debug "In deviceTriggeredHandler - Turning on ${it}"
                it.on()
            }
        }

        if(switchesToTurnOff) {
            switchesToTurnOff.each { it ->
                if(logEnable) log.debug "In deviceTriggeredHandler - Turning off ${it}"
                it.off()
            }
        }

        if(switchesToFlash) {
            flashLights()
        }
    }
}

def deviceNotTriggeredHandler() {
    if(logEnable) log.debug "In deviceNotTriggeredHandler (${state.version})"
    if(switchesToTurnOnC) {
        switchesToTurnOnC.each { it ->
		    if(logEnable) log.debug "In deviceNotTriggeredHandler - Turning on ${it}"
		    it.on()
        }
	}
    
    if(switchesToTurnOffC) {
        switchesToTurnOffC.each { it ->
		    if(logEnable) log.debug "In deviceNotTriggeredHandler - Turning off ${it}"
		    it.off()
        }
	}
}

def checkTimeInState(evt) {
    if(logEnable) log.debug "In checkTimeInState (${state.version})"
    state.isData = "no"
    
    if(contactsClosed) {
        contactsClosed.each { it ->
            if(it.currentValue("contact") == "open") {
                if(logEnable) log.debug "In checkTimeInState - Contacts Open should be Closed - Working on: $it"
                state.lastActivity = it.getLastActivity()
                getTimeDiff()
                if(logEnable) log.debug "In checkTimeInState - Contacts Open should be Closed - timeDiff: ${state.timeDiff} vs. timeInState: ${timeInState}"
                if(state.timeDiff >= timeInState) {
                    state.isData = "yes"
                    messageHandler()
                    if(logEnable) log.debug "In checkTimeInState - Contacts Open should be Closed - Running again in 2 minutes"
                    runIn(120,checkTimeInState)
                } else {
                    if(logEnable) log.debug "In checkTimeInState - Contacts Open should be Closed - Running again in 1 minute"
                    runIn(60,checkTimeInState)
                }
            }
        }
    }
    
    if(contactsOpen) {
        contactsOpen.each { it ->
            if(it.currentValue("contact") == "closed") {
                if(logEnable) log.debug "In checkTimeInState - Contacts Closed should be Open - Working on: $it"
                state.lastActivity = it.getLastActivity()
                getTimeDiff()
                if(logEnable) log.debug "In checkTimeInState - Contacts Closed should be Open - timeDiff: ${state.timeDiff} vs. timeInState: ${timeInState}"
                if(state.timeDiff >= timeInState) {
                    state.isData = "yes"
                    messageHandler()
                    if(logEnable) log.debug "In checkTimeInState - Contacts Closed should be Open - Running again in 2 minutes"
                    runIn(120,checkTimeInState)
                } else {
                    if(logEnable) log.debug "In checkTimeInState - Contacts Closed should be Open - Running again in 1 minute"
                    runIn(60,checkTimeInState)
                }
            }
        }
    }
    
    if(switchesOn) {
        switchesOn.each { it ->
            if(it.currentValue("switch") == "off") {
                if(logEnable) log.debug "In checkTimeInState - Switches Off should be On - Working on: $it"
                state.lastActivity = it.getLastActivity()
                getTimeDiff()
                if(logEnable) log.debug "In checkTimeInState - Switches Off should be On - timeDiff: ${state.timeDiff} vs. timeInState: ${timeInState}"
                if(state.timeDiff >= timeInState) {
                    state.isData = "yes"
                    messageHandler()
                    if(logEnable) log.debug "In checkTimeInState - Switches Off should be On - Running again in 2 minutes"
                    runIn(120,checkTimeInState)
                } else {
                    if(logEnable) log.debug "In checkTimeInState - Switches Off should be On - Running again in 1 minute"
                    runIn(60,checkTimeInState)
                }
            }
        }
    }
    
    if(switchesOff) {
        switchesOff.each { it ->
            if(it.currentValue("switch") == "on") {
                if(logEnable) log.debug "In checkTimeInState - Switches On should be Off - Working on: $it"
                state.lastActivity = it.getLastActivity()
                getTimeDiff()
                if(logEnable) log.debug "In checkTimeInState - Switches On should be Off - timeDiff: ${state.timeDiff} vs. timeInState: ${timeInState}"
                if(state.timeDiff >= timeInState) {
                    state.isData = "yes"
                    messageHandler()
                    if(logEnable) log.debug "In checkTimeInState - Switches On should be Off - Running again in 2 minutes"
                    runIn(120,checkTimeInState)
                } else {
                    if(logEnable) log.debug "In checkTimeInState - Switches On should be Off - Running again in 1 minute"
                    runIn(60,checkTimeInState)
                }
            }
        }
    }
    
    if(locksLocked) {
        locksLocked.each { it ->
            if(it.currentValue("switch") == "unlocked") {
                if(logEnable) log.debug "In checkTimeInState - Locks Unlocked should be Locked - Working on: $it"
                state.lastActivity = it.getLastActivity()
                getTimeDiff()
                if(logEnable) log.debug "In checkTimeInState - Locks Unlocked should be Locked - timeDiff: ${state.timeDiff} vs. timeInState: ${timeInState}"
                if(state.timeDiff >= timeInState) {
                    state.isData = "yes"
                    messageHandler()
                    if(logEnable) log.debug "In checkTimeInState - Locks Unlocked should be Locked - Running again in 2 minutes"
                    runIn(120,checkTimeInState)
                } else {
                    if(logEnable) log.debug "In checkTimeInState - Locks Unlocked should be Locked - Running again in 1 minute"
                    runIn(60,checkTimeInState)
                }
            }
        }
    }
    
    if(locksUnlocked) {
        locksUnlocked.each { it ->
            if(it.currentValue("switch") == "locked") {
                if(logEnable) log.debug "In checkTimeInState - Locks Locked should be Unlocked - Working on: $it"
                state.lastActivity = it.getLastActivity()
                getTimeDiff()
                if(logEnable) log.debug "In checkTimeInState - Locks Locked should be Unlocked - timeDiff: ${state.timeDiff} vs. timeInState: ${timeInState}"
                if(state.timeDiff >= timeInState) {
                    state.isData = "yes"
                    messageHandler()
                    if(logEnable) log.debug "In checkTimeInState - Locks Locked should be Unlocked - Running again in 2 minutes"
                    runIn(120,checkTimeInState)
                } else {
                    if(logEnable) log.debug "In checkTimeInState - Locks Locked should be Unlocked - Running again in 1 minute"
                    runIn(60,checkTimeInState)
                }
            }
        }
    }
}

def letsTalk(msg) {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}"
    dayOfTheWeekHandler()
    if(state.daysMatch && useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(msg)
    }
    msg = ""
    if(logEnable) log.debug "In letsTalk - *** Finished ***"
}

def modeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In modeHandler (${state.version})"
        state.modeNow = location.mode
        state.matchFound = false

        if(modeName) {
            modeName.each { it ->
                if(logEnable) log.debug "In modeHandler - Checking if ${state.modeNow} contains ${it}"
                if(state.modeNow.contains(it)) {
                    state.matchFound = true
                    if(logEnable) log.debug "In modeHandler - Match Found - modeName: ${modeName} - modeNow: ${state.modeNow}"
                }
            }
            if(state.matchFound) {
                checkDeviceHandler()
            } else {
                if(logEnable) log.debug "In modeHandler - No Match Found"
            }
        }
    }
}

def dayOfTheWeekHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
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
}

def thermostatHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In thermostatHandler (${state.version})"
        state.thermFound = false
        if(thermostats) {
            thermostats.each { therm ->
                def thermName = therm.displayName
                def thermStatus = therm.currentValue("thermostatOperatingState")
                if(thermStatus != "idle") {
                    state.thermFound = true
                    if(logEnable) log.debug "In thermostatHandler - Match Found - thermName: ${thermName} - thermStatus: ${thermStatus}"
                }
            }
            if(state.thermFound) {
                checkDeviceHandler()
            } else {
                if(logEnable) log.debug "In thermostatHandler - No Match Found"
            }
        }
    }
}

def thermostatModeHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In thermostatModeHandler (${state.version})"
        state.thermModeFound = false
        if(thermostats) {
            thermostats.each { thermMode ->
                def thermModeName = thermMode.displayName
                def thermModeStatus = thermMode.currentValue("thermostatMode")
                if(thermModeStatus != "off") {
                    state.thermModeFound = true
                    if(logEnable) log.debug "In thermostatModeHandler - Match Found - thermModeName: ${thermModeName} - thermMStatus: ${thermModeStatus}"
                }
            }
            if(state.thermModeFound) {
                checkDeviceHandler()
            } else {
                if(logEnable) log.debug "In thermostatModeHandler - No Match Found"
            }
        } 
    }
}

def setPointHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        state.setPointDevice = evt.displayName
        setPointValue = evt.value	
        state.setPointMSG = ""

        setPointValue1 = setPointValue.toDouble()
        if(logEnable) log.debug "In setPointHandler - Device: ${state.setPointDevice}, setPointHigh: ${setPointHigh}, setPointLow: ${setPointLow}, Acutal value: ${setPointValue1} - setPointHighOK: ${state.setPointHighOK}, setPointLowOK: ${state.setPointLowOK}"
        // *** setPointHigh ***
        if(oSetPointHigh && !oSetPointLow) {
            if(setPointValue1 > setPointHigh) {
                if(state.setPointHighOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Hgh) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${setPointHigh}"
                    state.setPointHighOK = "no"
                    state.isData = "yes"
                    state.setPointMSG += "${state.setPointDevice}, "
                } else {
                    if(logEnable) log.debug "In setPointHandler (High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 < setPointHigh) {
                if(state.setPointHighOK == "no") {
                    if(logEnable) log.debug "In setPointHandler (High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is Less THAN setPointHigh: ${setPointHigh}"
                    state.setPointHighOK = "yes"
                    state.isData = "no"
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
        // *** setPointLow ***
        if(oSetPointLow && !oSetPointHigh) {
            if(setPointValue1 < setPointLow) {
                if(state.setPointLowOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${setPointLow}"
                    state.setPointLowOK = "no"
                    state.isData = "yes"
                    state.setPointMSG += "${state.setPointDevice}, "
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 > setPointLow) {
                if(state.setPointLowOK == "no") {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointLow: ${setPointLow}"
                    state.setPointLowOK = "yes"
                    state.isData = "no"
                } else {
                    if(logEnable) log.debug "In setPointHandler (Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
        // *** Inbetween ***
        if(oSetPointHigh && oSetPointLow) {
            if(setPointValue1 > setPointHigh) {
                if(state.setPointHighOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is GREATER THAN setPointHigh: ${setPointHigh}"
                    state.setPointHighOK = "no"
                    state.isData = "yes"
                    state.setPointMSG += "${state.setPointDevice}, "
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both-High) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if(setPointValue1 < setPointLow) {
                if(state.setPointLowOK != "no") {
                    if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${state.setPointDevice}, (Low) - Actual value: ${setPointValue1} is LESS THAN setPointLow: ${setPointLow}"
                    state.setPointLowOK = "no"
                    state.isData = "yes"
                    state.setPointMSG += "${state.setPointDevice}, "
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both-Low) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
            if((setPointValue1 <= setPointHigh) && (setPointValue1 >= setPointLow)) {
                if(state.setPointHighOK == "no" || state.setPointLowOK == "no") {
                    if(logEnable) log.debug "InsetPointHandler (Both) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is BETWEEN tempHigh: ${setPointHigh} and setPointLow: ${setPointLow}"
                    state.setPointHighOK = "yes"
                    state.setPointLowOK = "yes"
                    state.isData = "no"
                } else {
                    if(logEnable) log.debug "In setPointHandler (Both) - Device: ${state.setPointDevice}, Actual value: ${setPointValue1} is good.  Nothing to do."
                }
            }
        }
        if(state.isData == "yes") {
            if(logEnable) log.debug "In setPointHandler - Data Found"
            if(notifyDelay) {
                state.notifyDel = notifyDelay * 60
                runIn(state.notifyDel,deviceTriggeredHandler)
                runIn(state.notifyDel,messageHandler)
            } else {
                deviceTriggeredHandler()
                messageHandler()
            }
        } else {
            if(logEnable) log.debug "In setPointHandler - No Data Found"
            deviceNotTriggeredHandler()
            unschedule
        }
    }
}

def messageHandler() {
	if(logEnable) log.debug "In messageHandler (${state.version})"
    state.theMsg = ""
    
    if(state.isData == "no") {
        if(useNoDeviceMsg) {
	        def valuesNo = "${noDeviceMsg}".split(";")
            vSizeNo = valuesNo.size()
            countNo = vSizeNo.toInteger()
            def randomKeyNo = new Random().nextInt(countNo)
            state.noMsg = valuesNo[randomKeyNo]
            if(logEnable) log.debug "In messageHandler - Random Pre - vSize: ${vSizeNo}, randomKey: ${randomKeyNo}, noMsg: ${state.noMsg}"

            state.theMsg = "${state.noMsg}"
        }
    }
      
    if(state.isData == "yes") {
	    def valuesPre = "${preMsg}".split(";")
	    vSizePre = valuesPre.size()
		countPre = vSizePre.toInteger()
    	def randomKeyPre = new Random().nextInt(countPre)
		state.preMsgR = valuesPre[randomKeyPre]
		if(logEnable) log.debug "In messageHandler - Random Pre - vSize: ${vSizePre}, randomKey: ${randomKeyPre}, Pre Msg: ${state.preMsgR}"
	   
	    def valuesPost = "${postMsg}".split(";")
	    vSizePost = valuesPost.size()
		countPost = vSizePost.toInteger()
        def randomKeyPost = new Random().nextInt(countPost)
		state.postMsgR = valuesPost[randomKeyPost]
		if(logEnable) log.debug "In messageHandler - Random Post - vSize: ${vSizePost}, randomKey: ${randomKeyPost}, Msg: ${state.postMsgR}"
	
	    state.theMsg = "${state.preMsgR}, "
    
        if(logEnable) log.debug "---------- Start - Devices in the Wrong State ----------"
        if(logEnable && state.wrongSwitchesMSG) log.debug "wrongSwitchesMSG - ${state.wrongSwitchesMSG.substring(0, state.wrongSwitchesMSG.length() - 2)}"
        if(logEnable && state.wrongDevicesMSG) log.debug "wrongDevicesMSG - ${state.wrongDevicesMSG.substring(0, state.wrongDevicesMSG.length() - 2)}"
        if(logEnable && state.wrongContactsMSG) log.debug "wrongContactsMSG - ${state.wrongContactsMSG.substring(0, state.wrongContactsMSG.length() - 2)}"
        if(logEnable && state.wrongLocksMSG) log.debug "wrongLocksMSG - ${state.wrongLocksMSG.substring(0, state.wrongLocksMSG.length() - 2)}"
        if(logEnable && state.setPointMSG) log.debug "setPointMSG - ${state.setPointMSG.substring(0, state.setPointMSG.length() - 2)}"
        if(logEnable) log.debug "---------- End - Devices in the Wrong State ----------"
        if(state.wrongSwitchesMSG) { state.theMsg += " Switches: ${state.wrongSwitchesMSG.substring(0, state.wrongSwitchesMSG.length() - 2)}." }
        if(state.wrongDevicesMSG) { state.theMsg += " Devices: ${state.wrongDevicesMSG.substring(0, state.wrongDevicesMSG.length() - 2)}." }
        if(state.wrongContactsMSG) { state.theMsg += " Contacts: ${state.wrongContactsMSG.substring(0, state.wrongContactsMSG.length() - 2)}." }
        if(state.wrongLocksMSG) { state.theMsg += " Locks: ${state.wrongLocksMSG.substring(0, state.wrongLocksMSG.length() - 2)}." }
        if(state.setPointMSG && speakDevice) { state.theMsg += " ${state.setPointMSG.substring(0, state.setPointMSG.length() - 2)}." }
    
	    state.theMsg += " ${state.postMsgR}"
    }
    if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
 
    if(state.theMsg) {
        letsTalk(state.theMsg)
        if(sendPushMessage) pushHandler()
    }
}

def pushHandler() {
	if(logEnable) log.debug "In pushNow (${state.version})"
	theMessage = "${app.label} - ${state.theMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
	state.theMsg = ""
}

def getTimeDiff() {
    if(logEnable) log.debug "In getTimeDiff (${state.version})"
    
    long timeDiff
   	def now = new Date()
    def prev = Date.parse("yyy-MM-dd HH:mm:ss","${state.lastActivity}".replace("+00:00","+0000"))

    long unxNow = now.getTime()
    long unxPrev = prev.getTime()
    unxNow = unxNow/1000
    unxPrev = unxPrev/1000
    state.timeDiffSecs = Math.abs(unxNow-unxPrev)         // Seconds
    state.timeDiff = Math.round(state.timeDiffSecs/60)    // Minutes
}

private flashLights() {    // Modified from ST documents
    if(logEnable) log.debug "In flashLights (${state.version})"
	def doFlash = true
	def delay = delayFlashes ?: 500
	def numFlashes = numOfFlashes ?: 2

	if(logEnable) log.debug "In flashLights - LAST ACTIVATED: ${state.lastActivated}"
	if(state.lastActivated) {
		def elapsed = now() - state.lastActivated
		def sequenceTime = (numFlashes + 1) * (delay)
		doFlash = elapsed > sequenceTime
		if(logEnable) log.debug "In flashLights - DO FLASH: $doFlash - ELAPSED: $elapsed - LAST ACTIVATED: ${state.lastActivated}"
	}

	if(doFlash) {
		if(logEnable) log.debug "In flashLights - FLASHING $numFlashes times"
		state.lastActivated = now()
		if(logEnable) log.debug "In flashLights - LAST ACTIVATED SET TO: ${state.lastActivated}"
		def initialActionOn = switchesToFlash.collect{it.currentSwitch != "on"}

		numFlashes.times {
			if(logEnable) log.debug "In flashLights - Switch on after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.on()
				}
				else {
                    pauseExecution(delay)
					s.off()
				}
			}
			if(logEnable) log.debug "In flashLights - Switch off after $delay milliseconds"
			switchesToFlash.eachWithIndex {s, i ->
				if (initialActionOn[i]) {
                    pauseExecution(delay)
					s.off()
				}
				else {
                    pauseExecution(delay)
					s.on()
				}
			}
		}
	}
}

def createChildDevice() {    
    if(logEnable) log.debug "In createChildDevice (${state.version})"
    statusMessage = ""
    if(!getChildDevice("DCP - " + userName)) {
        if(logEnable) log.debug "In createChildDevice - Child device not found - Creating device Device Check Plus - ${userName}"
        try {
            addChildDevice("BPTWorld", "Device Check Plus Driver", "DCP - " + userName, 1234, ["name": "DCP - ${userName}", isComponent: false])
            if(logEnable) log.debug "In createChildDevice - Child device has been created! (SKT - ${userName})"
            statusMessage = "<b>Device has been been created. (DCP - ${userName})</b>"
        } catch (e) { if(logEnable) log.debug "Device Check Plus unable to create device - ${e}" }
    } else {
        statusMessage = "<b>Device Name (DCP - ${userName}) already exists.</b>"
    }
    return statusMessage
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
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable == null){logEnable = false}
	if(state.msg == null){state.msg = ""}
    if(state.lastActivated == null){state.lastActivated == now()}
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
