/**
 *  ****************  Error Monitor Child App  ****************
 *
 *  Design Usage:
 *  Keep an eye out for errors that may pop up in the log.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  1.0.5 - 04/17/22 - Fixed typo
 *  1.0.4 - 04/15/22 - Added option to ignore same error repeating and repeating and repeating...
 *  1.0.3 - 04/04/22 - Added option for repeating errors
 *  1.0.2 - 04/03/22 - Adjustments
 *  1.0.1 - 03/31/22 - Adjustments
 *  1.0.0 - 03/25/22 - Initial release.
 *
 */

#include BPTWorld.bpt-normalStuff

def setVersion(){
    state.name = "Error Monitor"
	state.version = "1.0.5"
}

definition(
    name: "Error Monitor",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Keep an eye out for errors that may pop up in the log.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Keep an eye out for errors that may pop up in the log."
            paragraph "Error Monitor has a couple of failsafe features built in.<br> - If the error message is the same as the last error message, it won't send the push unless you tell it to.<br> - If the same error message is received 10 times in a row, EM will close the connection and put a warning in the log. This is to prevent the hub from slowing down."
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Error Monitor needs a virtual device to store the results."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have EM create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Error Monitor')", required:true, submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Error Monitor Driver'.</small>"
            }
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
			input "sendPushMessage", "capability.notification", title: "Send a push notification?", multiple:true, required:false
            input "sendDup", "bool", title: "Send push even if the Error was the same as the last Error", submitOnChange:true
            paragraph "<hr>"
            paragraph "* The data device specified above will also turn on anytime there is a new error message. This Switch device can be used to trigger any rule/cog/piston."
		}

        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            input "useSafety", "bool", title: "Hub safety option - Turn the connection off is the same error is received 10 times in a row. This can stop a runaway situation that can slow down and/or crash your hub. (recommended to turn on)", defaultValue:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
                dataDevice.closeConnection()
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "longDescription", "textarea", title: "Description (optional)", submitOnChange:true
            input "otherNotes", "textarea", title: "Other Notes (optional)", submitOnChange:true
            input "logEnable", "bool", title: "Enable Debug Logging", description: "Log Options", submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
                input "testLevel1", "button", title: "Test Error 1", width:4
                input "testLevel2", "button", title: "Test Error 2", width:4
            } else {
                app.updateSetting("logEnable",[value:"false",type:"bool"])
                app.removeSetting("logOffTime")
            }
        }
            
        section(getFormat("header-green", "${getImage("Blank")}"+" Tracking Status")) {
            try {
                if(dataDevice) theStatus = dataDevice.currentValue("status")
            }
            catch(e) {
                theStatus = "Unknown"
            }
            paragraph "<b>There is NO need to 'Connect' the service. It will automatically be turned on when you hit 'Done' below.</b><br>If you don't want the service to start when saving the app, please use the 'Pause' feature above."
            paragraph "Current Log Watchdog status: <b>${theStatus}</b>", width: 6
            input "closeConnection", "button", title: "Disconnect", width: 6
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
    if(logEnable && logOffTime == "1 Hour") runIn(3600, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, "logsOff", [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, "logsOff", [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(dataDevice) {
            subscribe(dataDevice, "bpt-lastLogMessage", theNotifyStuff)
            dataDevice.appStatus("active")
            dataDevice.initialize()
        }
    }
}

def theNotifyStuff(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In theNotifyStuff (${state.version})"
        if(sendPushMessage) pushHandler()
    }
}

def pushHandler(){
	if(logEnable) log.debug "In pushNow (${state.version})"
    theLastMsg = dataDevice.currentValue("bpt-lastLogMessage")
	theMessage = "${app.label} - ${theLastMsg}"
	if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}"
   	sendPushMessage.deviceNotification(theMessage)
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "closeConnection"){
        dataDevice.closeConnection()
    }  
    if(state.whichButton == "testLevel1"){
        log.error "test.Error1: Error 1: Temp, temp"
    } else if(state.whichButton == "testLevel2"){
        log.error "test.Error2: Error 2: temp"
    }
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Error Monitor Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Error Monitor was unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}
