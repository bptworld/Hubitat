/**
 *  ****************  Device Watchdog Child ****************
 *
 *  Design Usage:
 *  Keep an eye on your devices and see how long it's been since they checked in.
 *
 *  Copyright 2018-2021 Bryan Turcotte (@bptworld)
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
 *  2.4.1 - 06/23/21 - Added a pause to refresh
 *  2.4.0 - 05/24/21 - Added second switch option to Run Reports on Demand, fixed typo with Activity push
 *  ---
 *  1.0.0 - 12/21/18 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Device Watchdog"
	state.version = "2.4.1"
}

definition(
    name: "Device Watchdog Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Keep an eye on your devices and see how long it's been since they checked in.",
    category: "",
	parent: "BPTWorld:Device Watchdog",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Device%20Watchdog/DW-child.groovy",
)

preferences {
    page name: "pageConfig"
	page name: "reportHandler", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "batteryConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "activityConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "statusConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "activityAttConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
    page name: "specialTrackingConfig", title: "", install: false, uninstall: false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "", nextPage: null, install: true, uninstall: true) {	
    display()
		section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- All changes are saved right away, no need to exit out and back in before generating a new report."
		}
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the Tile data. One device can hold the data for all 3 reports."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have DW create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this Data Device (ie. 'DW - Status')", required:true, submitOnChange:true
                paragraph "<b>A device will automaticaly be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "watchdogTileDevice", "capability.actuator", title: "Virtual Device to send the data to", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'Device Watchdog Tile' Driver.</small>"
            }
        }

		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
            app.removeSetting("reportType")
            if(batteryDevices || activityDevices || statusDevices || activityAttDevices || specialDevices1) {
			    href "reportHandler", title: "${getImage('reports')} Device Reports", description: "Click here to view the Device Reports."
            } else {
                paragraph "Be sure to fill out the options below, then you can run a report."
            }
		}
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Select The Report Type To Configure")) {
            if(batteryDevices) {
                href "batteryConfig", title:"${getImage("optionsGreen")} Battery Level Report Options", description:"Click here for Options"
            } else {
                href "batteryConfig", title:"${getImage("optionsRed")} Battery Level Report Options", description:"Click here for Options"
            }
            
            if(activityDevices) {
                href "activityConfig", title:"${getImage("optionsGreen")} Activity Report Options", description:"Click here for Options"
            } else {
                href "activityConfig", title:"${getImage("optionsRed")} Activity Report Options", description:"Click here for Options"
            }
            
            if(statusDevices) {
                href "statusConfig", title:"${getImage("optionsGreen")} Status Report Options", description:"Click here for Options"
            } else {
                href "statusConfig", title:"${getImage("optionsRed")} Status Report Options", description:"Click here for Options"
            }
            
            if(activityAttDevices) {
                href "activityAttConfig", title:"${getImage("optionsGreen")} Activity Report with Attributes Report Options", description:"Click here for Options"
            } else {
                href "activityAttConfig", title:"${getImage("optionsRed")} Activity Report with Attributes Report Options", description:"Click here for Options"
            }
            
            if(specialDevices1) {
                href "specialTrackingConfig", title:"${getImage("optionsGreen")} Special Tracking Report Options", description:"Track devices that don't report 'Last Activity' or 'Battery Level'"
            } else {
                href "specialTrackingConfig", title:"${getImage("optionsRed")} Special Tracking Report Options", description:"Track devices that don't report 'Last Activity' or 'Battery Level'"
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
			input "timeToRun", "time", title: "Check Devices at this time daily", required:false, submitOnChange:true
            input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to a run new report at any time (with push)", required:false, submitOnChange:true
            input "runReportSwitchNoPush", "capability.switch", title: "Turn this switch 'on' to a run new report at any time WITHOUT sending a push", required:false, submitOnChange:true
            paragraph "Push messages will only go out when Time and/or Switch options are choosen and triggered. This way you can view as many manual reports as needed to troubleshoot your system without being flooded with push notifications."
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification", multiple:true, required:false, submitOnChange:true
            if(sendPushMessage) {
                input "activityPush", "bool", title: "Send Activity Report", defaultValue:false, submitOnChange:true, width:6
                input "batteryPush", "bool", title: "Send Battery Report", defaultValue:false, submitOnChange:true, width:6
                input "statusPush", "bool", title: "Send Status Report", defaultValue:false, submitOnChange:true, width:6
                input "specialTrackingPush", "bool", title: "Send Special Tracking Report", defaultValue:false, submitOnChange:true, width:6
                
                input "pushAll", "bool", title: "Only send Push if there is something to actually report", description: "Push", defaultValue:false, submitOnChange:true, width:6
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Report Options")) {
            paragraph "<b>Font Size:</b> Smaller number, smaller characters. So more data can fit on tile."
            
            input "fontSize", "number", title: "Font Size for Reports", required:false, defaultValue:12, submitOnChange:true
            input "timeFormat", "bool", title: "Time Format - 12h=OFF - 24h=ON", description: "Time Format", defaultValue:false, submitOnChange:true
            input "mmddyy", "bool", title: "Time Format - mm dd, yy=OFF - dd mm, yy=ON", description: "Time Format", defaultValue:false, submitOnChange:true
            input "includeDate", "bool", title: "Include Timestamp on Report", description: "Timestamp", defaultValue:false, submitOnChange:true
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
            label title: "Enter a name for this automation", required:false, submitOnChange:true
			input "logEnable", "bool", title: "Enable Debug Logging", description: "debugging", defaultValue:false, submitOnChange:true
		}
		display2()
	}
}

def batteryConfig() {
	dynamicPage(name: "batteryConfig", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "<b>Select the Battery Level Options</b>"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
			input "batteryDevices", "capability.battery", title: "Select Battery Device(s)", required:true, multiple:true, submitOnChange:true
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Battery Options")) {
			input "batteryThreshold", "number", title: "Battery will be considered low when below this level", required:false, submitOnChange:true
			input "isDataBatteryDevice", "capability.switch", title: "Turn this device on if there is Battery data to report", required:false, submitOnChange:true
		}
		section() {
			input "batteryBadORgood", "bool", title: "Below Threshold (off) or Above Threshold (on)", description: "Threshold", defaultValue:false, submitOnChange:true
			if(batteryBadORgood) {
				paragraph "App will only display Devices ABOVE Threshold."
			} else {
				paragraph "App will only display Devices BELOW Threshold."
			}
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Filter Options")) {
            paragraph "To save characters, enter in a filter to remove characters from each device name. Must be exact, including case.<br><small>ie. 'Motion Sensor', 'Bedroom', 'Contact'</small>"
			input "bFilter1", "text", title: "Filter 1", required:false, submitOnChange:true, width:6
            input "bFilter2", "text", title: "Filter 2", required:false, submitOnChange:true, width:6
            
            input "bFilter3", "text", title: "Filter 3", required:false, submitOnChange:true, width:6
            input "bFilter4", "text", title: "Filter 4", required:false, submitOnChange:true, width:6
        }
        display2()
    }
}

def activityConfig() {
	dynamicPage(name: "activityConfig", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "<b>Select the Activity Options</b>"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
			input "activityDevices", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
        }
            
        section(getFormat("header-green", "${getImage("Blank")}"+" Refresh Options")) {
            paragraph "Device Watchdog can try to do a refresh on a device before setting it to inactive. Great for seldom used devices, such as a spare bedroom light switch.<br><small>* Note: This does not work on battery operated devices.</small>"
            input "useRefresh", "bool", title: "Use Refresh Options", defaultValue:false, submitOnChange:true
            if(useRefresh) {
				input "maxTimeDiff", "number", title: "How many hours 'since activity' before trying refresh", required:true, defaultValue:24, submitOnChange:true
                paragraph "Sometimes devices can miss commands due to HE's speed. This option will allow you to adjust the time between commands (refresh) being sent."
                input "actionDelay", "number", title: "Delay (in milliseconds - 1000 = 1 second, 3 sec max)", range: '1..3000', defaultValue:100, required:false, submitOnChange:true
            }
        }
            
	    section(getFormat("header-green", "${getImage("Blank")}"+" Activity Options")) {
			input "timeAllowed", "number", title: "Number of hours for Devices to be considered inactive", required:true, submitOnChange:true
			input "isDataActivityDevice", "capability.switch", title: "Turn this device on if there is Activity data to report", submitOnChange:true, required:false, multiple:false
		}
        
		section() {
			input "activityBadORgood", "bool", title: "Inactive (off) or active (on)", description: "Devices", defaultValue:false, submitOnChange:true
			if(activityBadORgood) {
				paragraph "App will only display ACTIVE Devices."
			} else {
				paragraph "App will only display INACTIVE Devices."
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Filter Options")) {
            paragraph "Last Activity can be displayed in two ways<br> - Time Since Last Activity: 0 D, 6 H, 49 M<br> - Actual Time Stamp of Last Activity: May 18, 2020 - 6:07 am"
            input "laDisplay", "bool", title: "Time Since(OFF) or Time Stamp(ON)", description: "laDisplay", defaultValue:false, submitOnChange:true
            
            paragraph "To save characters, enter in a filter to remove characters from each device name. Must be exact, including case.<br><small>ie. 'Motion Sensor', 'Bedroom', 'Contact'</small>"
			input "aFilter1", "text", title: "Filter 1", required:false, submitOnChange:true, width:6
            input "aFilter2", "text", title: "Filter 2", required:false, submitOnChange:true, width:6
            
            input "aFilter3", "text", title: "Filter 3", required:false, submitOnChange:true, width:6
            input "aFilter4", "text", title: "Filter 4", required:false, submitOnChange:true, width:6
        }
        display2()
    }
}

def statusConfig() {
	dynamicPage(name: "statusConfig", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "<b>Select the Status Options</b>"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
			input "statusDevices", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
            input "colorCodeStatus", "bool", title: "Color Code Status Values", description: "Color", defaultValue:false, submitOnChange:true
			if(colorCodeStatus) {
                if(parent.colorActive && parent.colorClear && parent.colorLocked && parent.colorOn && parent.colorPresent && parent.colorWet) {
				    paragraph "Status will be color coded."
                } else {
                    paragraph "Please be sure to completely fill out the 'Device Attribute Color Options' in the parent app before using the option here."
                }
			} else {
				paragraph "Color Code Status will not be color coded."
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Filter Options")) {
            paragraph "To save characters, enter in a filter to remove characters from each device name. Must be exact, including case.<br><small>ie. 'Motion Sensor', 'Bedroom', 'Contact'</small>"
			input "sFilter1", "text", title: "Filter 1", required:false, submitOnChange:true, width:6
            input "sFilter2", "text", title: "Filter 2", required:false, submitOnChange:true, width:6
            
            input "sFilter3", "text", title: "Filter 3", required:false, submitOnChange:true, width:6
            input "sFilter4", "text", title: "Filter 4", required:false, submitOnChange:true, width:6
        }
        display2()
    }
}

def activityAttConfig() {
	dynamicPage(name: "activityAttConfig", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "<b>Select the Status with Attributes Options</b>"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
			input "activityAttDevices", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
            
            if(activityAttDevices) {
                allAttrs = []
                allAttrs = activityAttDevices.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrsa = allAttrs.sort { a, b -> a.value <=> b.value }
                input "attOptions", "enum", title: "Attributes to display (up to 4)", options: allAttrsa, required:true, multiple:true, submitOnChange:true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Filter Options")) {
            paragraph "Last Activity can be displayed in two ways<br> - Time Since Last Activity: 0 D, 6 H, 49 M<br> - Actual Time Stamp of Last Activity: May 18, 2020 - 6:07 am"
            input "laDisplay", "bool", title: "Time Since(OFF) or Time Stamp(ON)", description: "laDisplay", defaultValue:false, submitOnChange:true
            
            paragraph "To save characters, enter in a filter to remove characters from each device name. Must be exact, including case.<br><small>ie. 'Motion Sensor', 'Bedroom', 'Contact'</small>"
			input "filter1", "text", title: "Filter 1", required:false, submitOnChange:true, width:6
            input "filter2", "text", title: "Filter 2", required:false, submitOnChange:true, width:6
            
            input "filter3", "text", title: "Filter 3", required:false, submitOnChange:true, width:6
            input "filter4", "text", title: "Filter 4", required:false, submitOnChange:true, width:6
        }
        
        section() {
            if(attOptions) {               
                String result1 = attOptions.join(",")
                def theOptions = result1.split(",")               
                int optionSize = theOptions.size()
                
                if(optionSize < 5) {
                    if(optionSize >= 1) att1 = theOptions[0]
                    if(optionSize >= 2) att2 = theOptions[1]
                    if(optionSize >= 3) att3 = theOptions[2]
                    if(optionSize >= 4) att4 = theOptions[3]

                    if(optionSize == 1) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1.capitalize()}<td>Last Activity</table>"

                    if(optionSize == 2) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1.capitalize()}<td>${att2.capitalize()}<td>Last Activity</table>"

                    if(optionSize == 3) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1.capitalize()}<td>${att2.capitalize()}<td>${att3.capitalize()}<td>Last Activity</table>"

                    if(optionSize == 4) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1.capitalize()}<td>${att2.capitalize()}<td>${att3.capitalize()}<td>${att4.capitalize()}<td>Last Activity</table>"

                    paragraph "<b>Example Report:</b><br><br>${exDisplay}"
                } else {
                    paragraph "<b>Please only choose 4 Attributes.</b>"
                }
            }
        }
        display2()
    }
}

def specialTrackingConfig() {
	dynamicPage(name: "specialTrackingConfig", title: "", install:false, uninstall:false) {
        display()
        section() {
            paragraph "<b>Select the Special Tracking Options</b>"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
            paragraph "Track devices that do not have a 'Last Activity' value. Trigger is based on any attribute."
			input "specialDevices1", "capability.*", title: "Select Device(s)", required:false, multiple:true, submitOnChange:true
            
            if(specialDevices1) {
                allAttrs1 = []
                allAttrs1 = specialDevices1.supportedAttributes.flatten().unique{ it.name }.collectEntries{ [(it):"${it.name.capitalize()}"] }
                allAttrs1a = allAttrs1.sort { a, b -> a.value <=> b.value }
                input "specialOptions1", "enum", title: "Attributes to track (max=4)", options: allAttrs1a, required:true, multiple:true, submitOnChange:true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Attribute Triggers")) {
            paragraph "For each attribute selected, a trigger value will need to entered. When a report is created and the device is found to be displaying this value, it will add the device to the report and notify you that there may be an issue. Just like a when a battery is below a set level."

            if(specialOptions1) {
                String result1 = specialOptions1.join(",")
                def theOptions = result1.split(",")               
                int optionSize = theOptions.size()

                if(optionSize < 5) {
                    if(optionSize >= 1) stAtt1 = theOptions[0]
                    if(optionSize >= 2) stAtt2 = theOptions[1]
                    if(optionSize >= 3) stAtt3 = theOptions[2]
                    if(optionSize >= 4) stAtt4 = theOptions[3]
                } else {
                    paragraph "<b>Please only choose 4 Attributes.</b>"
                }

                if(stAtt1) {
                    input "stAttTValue1", "text", title: "<b>${stAtt1.capitalize()}</b> - Trigger Value for attribute", submitOnChange:true, width:6
                    input "stAttTValue1a", "text", title: "2nd Trigger Value (optional)", submitOnChange:true, width:6
                }
                if(stAtt2) {
                    input "stAttTValue2", "text", title: "<b>${stAtt2.capitalize()}</b> - Trigger Value for attribute", submitOnChange:true, width:6
                    input "stAttTValue2a", "text", title: "2nd Trigger Value (optional)", submitOnChange:true, width:6
                }
                if(stAtt3) {
                    input "stAttTValue3", "text", title: "<b>${stAtt3.capitalize()}</b> - Trigger Value for attribute", submitOnChange:true, width:6
                    input "stAttTValue3a", "text", title: "2nd Trigger Value (optional)", submitOnChange:true, width:6
                }
                if(stAtt4) {
                    input "stAttTValue4", "text", title: "<b>${stAtt4.capitalize()}</b> - Trigger Value for attribute", submitOnChange:true, width:6
                    input "stAttTValue4a", "text", title: "2nd Trigger Value (optional)", submitOnChange:true, width:6
                }
            } 
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Filter Options")) {
            paragraph "To save characters, enter in a filter to remove characters from each device name. Must be exact, including case.<br><small>ie. 'Motion Sensor', 'Bedroom', 'Contact'</small>"
			input "stFilter1", "text", title: "Filter 1", required:false, submitOnChange:true, width:6
            input "stFilter2", "text", title: "Filter 2", required:false, submitOnChange:true, width:6
            
            input "stFilter3", "text", title: "Filter 3", required:false, submitOnChange:true, width:6
            input "stFilter4", "text", title: "Filter 4", required:false, submitOnChange:true, width:6
        }
        
        section() {
            if(specialOptions1) {
                String result1 = specialOptions1.join(",")
                def theOptions = result1.split(",")               
                int optionSize = theOptions.size()
             
                if(optionSize == 1) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report<td>${stAtt1.capitalize()}</table>"

                if(optionSize == 2) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report<td>${stAtt1.capitalize()}<td>${stAtt2.capitalize()}</table>"

                if(optionSize == 3) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report<td>${stAtt1.capitalize()}<td>${stAtt2.capitalize()}<td>${stAtt3.capitalize()}</table>"

                if(optionSize == 4) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report<td>${stAtt1.capitalize()}<td>${stAtt2.capitalize()}<td>${stAtt3.capitalize()}<td>${stAtt4.capitalize()}</table>"

                paragraph "<b>Example Report:</b><br><br>${exDisplay}"
            } else {
                paragraph "<b>Please only choose 4 Attributes.</b>"
            }
        }
        display2()
    }
}

def reportHandler() {
	dynamicPage(name: "reportHandler", title: "", install:false, uninstall:false) {
        display()
        section() {
            input "reportType", "enum", title: "Select Report Type", options: ["Activity", "Battery", "Status", "Activity with Attributes", "Special Tracking", "Combo-Activity-Battery"], required:true, submitOnChange:true
        }
			
		if(reportType == "Activity") {
            myActivityHandler()
            pauseExecution(1000)
            section() {
                if(activityDevices) {
                    activityMap1 = watchdogTileDevice.currentValue("bpt-watchdogActivity1")
                    activityMap2 = watchdogTileDevice.currentValue("bpt-watchdogActivity2")
                    activityMap3 = watchdogTileDevice.currentValue("bpt-watchdogActivity3")

                    activityCount1 = watchdogTileDevice.currentValue("watchdogActivityCount1")
                    activityCount2 = watchdogTileDevice.currentValue("watchdogActivityCount2")
                    activityCount3 = watchdogTileDevice.currentValue("watchdogActivityCount3")

                    if(activityCount1) a1 = activityCount1.toInteger()
                    if(activityCount2) a2 = activityCount2.toInteger()
                    if(activityCount3) a3 = activityCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - a1: ${a1} - a2: ${a2} - a3: ${a3}"

                    if(a1 >= 63) {
                        paragraph "${activityMap1}"
                        if(a1 <= 1024) paragraph "Tile Count: <span style='color:green'>${a1}</span>"
                        if(a1 > 1024) paragraph "<span style='color:red'>Tile Count: ${a1}</span>"
                        paragraph "<hr>"
                    }
                    if(a2 >= 63) {
                        paragraph "${activityMap2}"
                        if(a2 <= 1024) paragraph "Tile Count: <span style='color:green'>${a2}</span>"
                        if(a2 > 1024) paragraph "<span style='color:red'>Tile Count: ${a2}</span>"
                        paragraph "<hr>"
                    }
                    if(a3 >= 63) {
                        paragraph "${activityMap3}"
                        if(a3 <= 1024) paragraph "Tile Count: <span style='color:green'>${a3}</span>"
                        if(a3 > 1024) paragraph "<span style='color:red'>Tile Count: ${a3}</span>"
                    }

                    if(a1 < 63 && a2 < 63 && a3 < 63) {
                        paragraph "<div style='font-size:${fontSize}px'>Activity Report<br>Nothing to report</div>"
                    }
                    paragraph "${state.activityMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
            }
		}

        if(reportType == "Battery") {
            myBatteryHandler()
            pauseExecution(1000)
            section() {
                if(batteryDevices) {
                    batteryMap1 = watchdogTileDevice.currentValue("bpt-watchdogBattery1")
                    batteryMap2 = watchdogTileDevice.currentValue("bpt-watchdogBattery2")
                    batteryMap3 = watchdogTileDevice.currentValue("bpt-watchdogBattery3")

                    batteryCount1 = watchdogTileDevice.currentValue("watchdogBatteryCount1")
                    batteryCount2 = watchdogTileDevice.currentValue("watchdogBatteryCount2")
                    batteryCount3 = watchdogTileDevice.currentValue("watchdogBatteryCount3")

                    if(batteryCount1) bc1 = batteryCount1.toInteger()
                    if(batteryCount2) bc2 = batteryCount2.toInteger()
                    if(batteryCount3) bc3 = batteryCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - bc1: ${bc1} - bc2: ${bc2} - bc3: ${bc3}"

                    if(bc1 >= 59) {
                        paragraph "${batteryMap1}"
                        if(bc1 <= 1024) paragraph "Tile Count: <span style='color:green'>${bc1}</span>"
                        if(bc1 > 1024) paragraph "<span style='color:red'>Tile Count: ${bc1}</span>"
                        paragraph "<hr>"
                    }
                    if(bc2 >= 59) {
                        paragraph "${batteryMap2}"
                        if(bc2 <= 1024) paragraph "Tile Count: <span style='color:green'>${bc2}</span>"
                        if(bc2 > 1024) paragraph "<span style='color:red'>Tile Count: ${bc2}</span>"
                        paragraph "<hr>"
                    }
                    if(bc3 >= 59) {
                        paragraph "${batteryMap3}"
                        if(bc3 <= 1024) paragraph "Tile Count: <span style='color:green'>${bc3}</span>"
                        if(bc3 > 1024) paragraph "<span style='color:red'>Tile Count: ${bc3}</span>"
                    }

                    if(bc1 < 59 && bc2 < 59 && bc3 < 59) {
                        paragraph "<div style='font-size:${fontSize}px'>Battery Report<br>Nothing to report</div>"
                    }
                    paragraph "${state.batteryMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
        	}
        }
        
		if(reportType == "Status") {
            myStatusHandler()
            pauseExecution(1000)
        	section() {
                if(statusDevices) {
                    statusMap1 = watchdogTileDevice.currentValue("bpt-watchdogStatus1")
                    statusMap2 = watchdogTileDevice.currentValue("bpt-watchdogStatus2")
                    statusMap3 = watchdogTileDevice.currentValue("bpt-watchdogStatus3")

                    statusCount1 = watchdogTileDevice.currentValue("watchdogStatusCount1")
                    statusCount2 = watchdogTileDevice.currentValue("watchdogStatusCount2")
                    statusCount3 = watchdogTileDevice.currentValue("watchdogStatusCount3")

                    if(statusCount1) s1 = statusCount1.toInteger()
                    if(statusCount2) s2 = statusCount2.toInteger()
                    if(statusCount3) s3 = statusCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - s1: ${s1} - s2: ${s2} - s3: ${s3}"

                    if(s1 >= 58) {
                        paragraph "${statusMap1}"
                        if(s1 <= 1024) paragraph "Tile Count: <span style='color:green'>${s1}</span>"
                        if(s1 > 1024) paragraph "<span style='color:red'>Tile Count: ${s1}</span>"
                        paragraph "<hr>"
                    }
                    if(s2 >= 58) {
                        paragraph "${statusMap2}"
                        if(s2 <= 1024) paragraph "Tile Count: <span style='color:green'>${s2}</span>"
                        if(s2 > 1024) paragraph "<span style='color:red'>Tile Count: ${s2}</span>"
                        paragraph "<hr>"
                    }
                    if(s3 >= 58) {
                        paragraph "${statusMap3}"
                        if(s3 <= 1024) paragraph "Tile Count: <span style='color:green'>${s3}</span>"
                        if(s3 > 1024) paragraph "<span style='color:red'>Tile Count: ${s3}</span>"
                    }

                    if(s1 < 58 && s2 < 58 && s3 < 58) {
                        paragraph "<div style='font-size:${fontSize}px'>Status Report<br>Nothing to report</div>"
                    }
                    paragraph "${state.statusMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
			}
        }
        
        if(reportType == "Activity with Attributes") {
            myActivityAttHandler()
            pauseExecution(1000)
            section() {
                if(activityAttDevices) {
                    activityAttMap1 = watchdogTileDevice.currentValue("bpt-watchdogActivityAtt1")
                    activityAttMap2 = watchdogTileDevice.currentValue("bpt-watchdogActivityAtt2")
                    activityAttMap3 = watchdogTileDevice.currentValue("bpt-watchdogActivityAtt3")

                    activityAttCount1 = watchdogTileDevice.currentValue("watchdogActivityAttCount1")
                    activityAttCount2 = watchdogTileDevice.currentValue("watchdogActivityAttCount2")
                    activityAttCount3 = watchdogTileDevice.currentValue("watchdogActivityAttCount3")

                    if(activityAttCount1) aa1 = activityAttCount1.toInteger()
                    if(activityAttCount2) aa2 = activityAttCount2.toInteger()
                    if(activityAttCount3) aa3 = activityAttCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - aa1: ${aa1} - a2: ${aa2} - a3: ${aa3}"

                    if(aa1 >= 67) {
                        paragraph "${activityAttMap1}"
                        if(aa1 <= 1024) paragraph "Tile Count: <span style='color:green'>${aa1}</span>"
                        if(aa1 > 1024) paragraph "<span style='color:red'>Tile Count: ${aa1}</span>"
                        paragraph "<hr>"
                    }
                    if(aa2 >= 67) {
                        paragraph "${activityAttMap2}"
                        if(aa2 <= 1024) paragraph "Tile Count: <span style='color:green'>${aa2}</span>"
                        if(aa2 > 1024) paragraph "<span style='color:red'>Tile Count: ${aa2}</span>"
                        paragraph "<hr>"
                    }
                    if(aa3 >= 67) {
                        paragraph "${activityAttMap3}"
                        if(aa3 <= 1024) paragraph "Tile Count: <span style='color:green'>${aa3}</span>"
                        if(aa3 > 1024) paragraph "<span style='color:red'>Tile Count: ${aa3}</span>"
                    }

                    if(aa1 < 67 && aa2 < 67 && aa3 < 67) {
                        paragraph "<div style='font-size:${fontSize}px'>Activity with Attributes Report<br>Nothing to report</div>"
                    }
                    paragraph "${state.activityAttMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
            }
		}
        
        if(reportType == "Special Tracking") {
            specialTrackingHandler()
            pauseExecution(1000)
        	section() {
                if(specialDevices1) {
                    specialMap1 = watchdogTileDevice.currentValue("bpt-watchdogSpecial1")
                    specialMap2 = watchdogTileDevice.currentValue("bpt-watchdogSpecial2")
                    specialMap3 = watchdogTileDevice.currentValue("bpt-watchdogSpecial3")

                    specialCount1 = watchdogTileDevice.currentValue("watchdogSpecialCount1")
                    specialCount2 = watchdogTileDevice.currentValue("watchdogSpecialCount2")
                    specialCount3 = watchdogTileDevice.currentValue("watchdogSpecialCount3")

                    if(specialCount1) st1 = specialCount1.toInteger()
                    if(specialCount2) st2 = specialCount2.toInteger()
                    if(specialCount3) st3 = specialCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - st1: ${st1} - st2: ${st2} - st3: ${st3}"

                    if(st1 >= 71) {
                        paragraph "${specialMap1}"
                        if(st1 <= 1024) paragraph "Tile Count: <span style='color:green'>${st1}</span>"
                        if(st1 > 1024) paragraph "<span style='color:red'>Tile Count: ${st1}</span>"
                        paragraph "<hr>"
                    }
                    if(st2 >= 71) {
                        paragraph "${specialMap2}"
                        if(st2 <= 1024) paragraph "Tile Count: <span style='color:green'>${st2}</span>"
                        if(st2 > 1024) paragraph "<span style='color:red'>Tile Count: ${st2}</span>"
                        paragraph "<hr>"
                    }
                    if(st3 >= 71) {
                        paragraph "${specialMap3}"
                        if(st3 <= 1024) paragraph "Tile Count: <span style='color:green'>${st3}</span>"
                        if(st3 > 1024) paragraph "<span style='color:red'>Tile Count: ${st3}</span>"
                    }

                    if(st1 < 71 && st2 < 71 && st3 < 71) {
                        paragraph "<div style='font-size:${fontSize}px'>Special Tracking Report<br>Nothing to report</div>"
                    }
                    paragraph "${state.specialMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
			}
        }
        
        if(reportType == "Combo-Activity-Battery") {
            myActivityHandler()
            myBatteryHandler()
            pauseExecution(1000)
            section() {
                paragraph "Remember, this will still have to be under the 1024 character limit on dashboard tiles. So if combining the two reports results in the character count over 1024, it can not be displayed on the dashboard."
                if(batteryDevices && activityDevices) {
                    batteryMap1 = watchdogTileDevice.currentValue("bpt-watchdogBattery1")
                    activityMap1 = watchdogTileDevice.currentValue("bpt-watchdogActivity1")
                        
                    batteryCount1 = watchdogTileDevice.currentValue("watchdogBatteryCount1")
                    if(batteryCount1) bc1 = batteryCount1.toInteger()
                    
                    combo = "<div style='overflow:auto;height:90%'>"
                    combo += "${activityMap1}<br>${batteryMap1}"
                    combo += "</div>"
                    int comboCount = combo.length()
                    
                    if(comboCount >= 42) {    
                        paragraph "${combo}"
                        if(comboCount <= 1024) {
                            paragraph "Tile Count: <span style='color:green'>${comboCount}</span>"
                            if(watchdogTileDevice) {
                                if(logEnable) log.debug "In comboActBatHandler - Sending new Combo Watchdog data to Tiles"
                                sending = "1::${combo}"
                                watchdogTileDevice.sendWatchdogComboActBatMap(sending)
                            }
                        }
                        if(comboCount > 1024) {
                            paragraph "<span style='color:red'>Tile Count: ${comboCount}</span>"
                            if(watchdogTileDevice) {
                                if(logEnable) log.debug "In comboActBatHandler - Sending new Combo Watchdog data to Tiles"
                                sending = "1::Too many characters to display on Dashboard (${comboCount})"
                                watchdogTileDevice.sendWatchdogComboActBatMap(sending)
                            }
                        }
                        paragraph "<hr>"
                    }
                        
                    if(comboCount < 42) {
                        paragraph "<div style='font-size:${fontSize}px'>Combo-Activity-Battery Report<br>Nothing to report</div>"
                    }
                    //paragraph "${state.statusMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
            }
		}
        section() { paragraph getFormat("line") }
	}
}

def installed() {
    if(logEnable) log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
   	if(logEnable) log.info "Updated with settings: ${settings}"
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
        if(timeToRun) schedule(timeToRun, activityHandler)
        if(runReportSwitch) subscribe(runReportSwitch, "switch.on", activityHandler)
        if(runReportSwitchNoPush) subscribe(runReportSwitchNoPush, "switch.on", activityHandler)
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def activityHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        clearMaps()
        if(logEnable) log.debug "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
        if(evt) {
            state.whoHappened = evt.displayName
            if(logEnable) log.debug "In activityHandler - whoHappened: ${state.whoHappened}" 
        } else {
            state.whoHappened = ""
        }
        if(activityDevices) myActivityHandler()
        if(batteryDevices) myBatteryHandler()
        if(statusDevices) myStatusHandler()
        if(activityAttDevices) myActivityAttHandler()
        if(specialDevices1) specialTrackingHandler()

        if(isDataActivityDevice) isThereData()
        if(isDataBatteryDevice) isThereData()
        if(isDataStatusDevice) isThereData()
        if(isDataSpecialDevice) isThereData()
        if(logEnable) log.debug "In activityHandler - whoHappened: ${state.whoHappened} VS runReportSwitchNoPush: ${runReportSwitchNoPush}" 
        if(sendPushMessage && state.whoHappened.toString() != runReportSwitchNoPush.toString()) pushNow()
        if(logEnable) log.debug "     * * * * * * * * End ${app.label} * * * * * * * *     "
    }
}	

def myActivityHandler() {
    if(activityDevices) {
        if(useRefresh) refreshDevices(activityDevices)    // Refresh Devices before checking    
        if(logEnable) log.debug "     - - - - - Start (Activity) - - - - -     "
        if(logEnable) log.debug "In myActivityHandler ${state.version}"

        if(includeDate) { 
            def rightNow = new Date()
            dateFormatHandler(rightNow)
            reportDateTime = " - ${newDate}"
        } else {
            reportDateTime = ""
        }
        
        def tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td><b>Activity Report${reportDateTime}</b><td><b>Value</b>"
        
        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.activityCount = 0
        state.activityMapPhoneS = ""
        activityMapPhone = "Activity Report \n"
        data = false
        theDevices = activityDevices.sort { a, b -> a.displayName <=> b.displayName }    

        theDevices.each { it ->
            if(!it.isDisabled()) {
                getTimeDiff(it)
                if(state.since != null) {
                    if(logEnable) log.debug "In myActivityHandler - ${it.displayName} totalHours: ${state.totalHours} vs timeAllowed: ${timeAllowed}"
                    if(state.totalHours > timeAllowed) {
                        if(!activityBadORgood) {
                            state.activityCount = state.activityCount + 1
                            if(logEnable) log.debug "In myActivityHandler - ${it.displayName} hasn't checked in since ${state.theDuration} ago."                   
                            data = true
                        }
                    } else {
                        if(activityBadORgood) {
                            if(logEnable) log.debug "In myActivityHandler - ${it.displayName} last checked in ${state.theDuration} ago."
                            data = true
                        }
                    }
                } else {
                    log.warn "${app.displayName} - ${it.displayName} has no activity. It will not show up in the reports."
                    data = false
                } 

                if(data) {
                    state.activityCount = state.activityCount + 1
                    if(!laDisplay) {
                        getTimeDiff(it)
                        lastAct = state.theDuration
                    } else {
                        theDate = it.getLastActivity()
                        dateFormatHandler(theDate)
                        lastAct = newDate
                    }

                    theName = it.displayName              
                    if(aFilter1) { theName = theName.replace("${aFilter1}", "") }
                    if(aFilter2) { theName = theName.replace("${aFilter2}", "") }
                    if(aFilter3) { theName = theName.replace("${aFilter3}", "") }
                    if(aFilter4) { theName = theName.replace("${aFilter4}", "") }

                    line = "<tr><td>${theName}<td>${lastAct}"
                    activityMapPhone += "${theName} - ${lastAct} \n"

                    totalLength = tbl.length() + line.length()
                    if(logEnable) log.debug "In myActivityHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"

                    if (totalLength < 1007) {
                        tbl += line
                    } else {
                        tbl += "</table></div>"
                        if(logEnable) log.debug "${tbl}"
                        if(watchdogTileDevice) {
                            if(logEnable) log.debug "In myActivityHandler - Sending new Activity Watchdog data to Tiles (${tileCount})"
                            sending = "${tileCount}::${tbl}"
                            watchdogTileDevice.sendWatchdogActivityMap(sending)
                            tileCount = tileCount + 1
                        }
                        tbl = tblhead + line 
                    }
                }
                data = false
            }
        }

        if (tbl != tblhead) {
            tbl += "</table></div>"
            if(logEnable) log.debug "${tbl}"
            if(watchdogTileDevice) {
                if(logEnable) log.debug "In myActivityHandler - Sending new Activity Watchdog data to Tiles (${tileCount})"
                sending = "${tileCount}::${tbl}"
                watchdogTileDevice.sendWatchdogActivityMap(sending)
                tileCount = tileCount + 1
            }
        }

        for(x=tileCount;x<4;x++) {
            sending = "${x}::<div style='font-size:${fontSize}px'>Activity Report - No Data</div>"
            watchdogTileDevice.sendWatchdogActivityMap(sending)
        }

        def rightNow = new Date()
        dateFormatHandler(rightNow)
        state.activityMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${newDate}</table>"
        activityMapPhone += "Report generated: ${newDate} \n"
        state.activityMapPhoneS = activityMapPhone
        if(logEnable) log.debug "     - - - - - End (Activity) - - - - -     "
    }
}

def myBatteryHandler() {
    if(batteryDevices) {
        if(logEnable) log.debug "     - - - - - Start (Battery) - - - - -     "
        if(logEnable) log.debug "In myBatteryHandler ${state.version}"

        if(includeDate) { 
            def rightNow = new Date()
            dateFormatHandler(rightNow)
            reportDateTime = " - ${newDate}"
        } else {
            reportDateTime = ""
        }
        
        def tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Battery Report${reportDateTime}<td>Level<td>Last Activity"
        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.batteryCount = 0
        state.batteryMapPhoneS = ""
        batteryMapPhone = "Battery Report \n"
        data = false
        theDevices = batteryDevices.sort { a, b -> a.displayName <=> b.displayName }

        // is device disabled?  it.isDisabled()
        
        theDevices.each { it ->
            if(!it.isDisabled()) {
                def cv = it.currentValue("battery")
                if(cv == null) cv = -999  //RayzurMod
                if(cv <= batteryThreshold && cv > -999) { //RayzurMod
                    if(!batteryBadORgood) {
                        if(logEnable) log.debug "In myBatteryHandler - ${it.displayName} battery is ${cv} less than ${batteryThreshold} threshold"
                        data = true
                    }
                } else {
                    if(batteryBadORgood && cv > -999) { //RayzurMod 
                        if(logEnable) log.debug "In myBatteryHandler - ${it.displayName} battery is ${cv}, over ${batteryThreshold} threshold"
                        data = true
                    } else {
                        if (cv == -999) { //RayzurMod
                            if(logEnable) log.debug "In myBatteryHandler - ${it.displayName} battery hasn't reported in." //RayzurMod
                            data = true
                        }
                    }
                }

                if(data) {
                    state.batteryCount = state.batteryCount + 1
                    def lastActivity = it.getLastActivity()
                    if(lastActivity) {
                        dateFormatHandler(lastActivity)
                        // Handler Returns newDate
                    } else {
                        newDate = "No Data"
                    }

                    theName = it.displayName              
                    if(bFilter1) { theName = theName.replace("${bFilter1}", "") }
                    if(bFilter2) { theName = theName.replace("${bFilter2}", "") }
                    if(bFilter3) { theName = theName.replace("${bFilter3}", "") }
                    if(bFilter4) { theName = theName.replace("${bFilter4}", "") }

                    line = "<tr><td>${theName}<td>${cv}<td>${newDate}"
                    batteryMapPhone += "${theName} - ${cv} - ${newDate}\n"

                    totalLength = tbl.length() + line.length()
                    if(logEnable) log.debug "In myBatteryHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"

                    if (totalLength < 1007) {
                        tbl += line
                    } else {
                        tbl += "</table></div>"
                        if(logEnable) log.debug "${tbl}"
                        if(watchdogTileDevice) {
                            if(logEnable) log.debug "In myBatteryHandler - Sending new Battery Watchdog data to Tiles (${tileCount})"
                            sending = "${tileCount}::${tbl}"
                            watchdogTileDevice.sendWatchdogBatteryMap(sending)
                            tileCount = tileCount + 1
                        }
                        tbl = tblhead + line 
                    }
                    data = false
                }
            }
        }

        if (tbl != tblhead) {
            tbl += "</table></div>"
            if(logEnable) log.debug "${tbl}"
            if(watchdogTileDevice) {
                if(logEnable) log.debug "In myBatteryHandler - Sending new Battery Watchdog data to Tiles (${tileCount})"
                sending = "${tileCount}::${tbl}"
                watchdogTileDevice.sendWatchdogBatteryMap(sending)
                tileCount = tileCount + 1
            }
        }

        for(x=tileCount;x<4;x++) {
            sending = "${x}::<div style='font-size:${fontSize}px'>Battery Report - No Data</div>"
            watchdogTileDevice.sendWatchdogBatteryMap(sending)
        }

        def rightNow = new Date()
        dateFormatHandler(rightNow)
        state.batteryMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${newDate}</table>"
        batteryMapPhone += "Report generated: ${newDate} \n"
        state.batteryMapPhoneS = batteryMapPhone
        if(logEnable) log.debug "     - - - - - End (Battery) - - - - -     "
    }
}

def myStatusHandler() {
    if(statusDevices) {
        if(logEnable) log.debug "     - - - - - Start (Status) - - - - -     "
        if(logEnable) log.debug "In myStatusHandler ${state.version}"

        if(includeDate) { 
            def rightNow = new Date()
            dateFormatHandler(rightNow)
            reportDateTime = " - ${newDate}"
        } else {
            reportDateTime = ""
        }
        
        def tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Status Report${reportDateTime}<td>Value<td>Last Activity"
        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.statusCount = 0
        state.statusMapPhoneS = ""
        statusMapPhone = "Status Report \n"
        sortedMap = statusDevices.sort { a, b -> a.displayName <=> b.displayName }

        sortedMap.each { it ->
            if(!it.isDisabled()) {
                deviceStatus = null
                if(logEnable) log.debug "In myStatusHandler - Working on: ${it.displayName}"
                if(it.hasAttribute("accelerationSensor")) {
                    deviceStatus = it.currentValue("accelerationSensor")
                    if(colorCodeStatus) {
                        if(deviceStatus == "inactive") { dStatus = "<div style='color:${parent.colorInactive}'>${deviceStatus}</div>" }
                        if(deviceStatus == "active") { dStatus = "<div style='color:${parent.colorActive}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("alarm")) {
                    deviceStatus = it.currentValue("alarm")
                    if(colorCodeStatus) {
                        if(deviceStatus == "off") { dStatus = "<div style='color:${parent.colorOff}'>${deviceStatus}</div>" }
                        if(deviceStatus == "siren") { dStatus = "<div style='color:${parent.colorSiren}'>${deviceStatus}</div>" }
                        if(deviceStatus == "strobe") { dStatus = "<div style='color:${parent.colorStrobe}'>${deviceStatus}</div>" }
                        if(deviceStatus == "both") { dStatus = "<div style='color:${parent.colorBoth}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("battery")) {
                    deviceStatus = it.currentValue("battery")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("carbonMonoxideDetector")) {
                    deviceStatus = it.currentValue("carbonMonoxideDetector")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("energyMeter")) {
                    deviceStatus = it.currentValue("energyMeter")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("illuminanceMeasurement")) {
                    deviceStatus = it.currentValue("illuminanceMeasurement")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("lock")) {
                    deviceStatus = it.currentValue("lock")
                    if(colorCodeStatus) {
                        if(deviceStatus == "locked") { dStatus = "<div style='color:${parent.colorLocked}'>${deviceStatus}</div>" }
                        if(deviceStatus == "unlocked") { dStatus = "<div style='color:red'>${parent.colorUnlocked}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("powerMeter")) {
                    deviceStatus = it.currentValue("powerMeter")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("presence")) {
                    deviceStatus = it.currentValue("presence")
                    if(colorCodeStatus) {
                        if(deviceStatus == "present") { dStatus = "<div style='color:${parent.colorPresent}'>${deviceStatus}</div>" }
                        if(deviceStatus == "not present") { dStatus = "<div style='color:${parent.colorNotPresent}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("pushed")) {
                    deviceStatus = it.currentValue("pushed")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("relativeHumidityMeasurement")) {
                    deviceStatus = it.currentValue("relativeHumidityMeasurement")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("smokeDetector")) {
                    deviceStatus = it.currentValue("smokeDetector")
                    if(colorCodeStatus) {
                        if(deviceStatus == "clear") { dStatus = "<div style='color:${parent.colorClear}'>${deviceStatus}</div>" }
                        if(deviceStatus == "detected") { dStatus = "<div style='color:${parent.colorDetected}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("switchLevel")) {
                    deviceStatus = it.currentValue("switchLevel")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("temperatureMeasurement")) {
                    deviceStatus = it.currentValue("temperatureMeasurement")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("valve")) {
                    deviceStatus = it.currentValue("valve")
                    if(colorCodeStatus) {
                        if(deviceStatus == "open") { dStatus = "<div style='color:${parent.colorOpen}'>${deviceStatus}</div>" }
                        if(deviceStatus == "closed") { dStatus = "<div style='color:${parent.colorClosed}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("voltageMeasurement")) {
                    deviceStatus = it.currentValue("voltageMeasurement")
                    if(colorCodeStatus) {
                        dStatus = deviceStatus
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("waterSensor")) {
                    deviceStatus = it.currentValue("waterSensor")
                    if(colorCodeStatus) {
                        if(deviceStatus == "wet") { dStatus = "<div style='color:${parent.colorWet}'>${deviceStatus}</div>" }
                        if(deviceStatus == "dry") { dStatus = "<div style='color:${parent.colorDry}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }

                if(it.hasAttribute("motion")) {
                    deviceStatus = it.currentValue("motion")
                    if(colorCodeStatus) {
                        if(deviceStatus == "active") { dStatus = "<div style='color:${parent.colorActive}'>${deviceStatus}</div>" }
                        if(deviceStatus == "inactive") { dStatus = "<div style='color:${parent.colorInactive}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }
                if(it.hasAttribute("contact")) {
                    deviceStatus = it.currentValue("contact")
                    if(colorCodeStatus) {
                        if(deviceStatus == "open") { dStatus = "<div style='color:${parent.colorOpen}'>${deviceStatus}</div>" }
                        if(deviceStatus == "closed") { dStatus = "<div style='color:${parent.colorClosed}'>${deviceStatus}</div>" }
                    } else {
                        dStatus = deviceStatus
                    }
                }

                if(deviceStatus == null || deviceStatus == "") {
                    if(it.hasAttribute("switch")) {
                        deviceStatus = it.currentValue("switch")
                        if(colorCodeStatus) {
                            if(deviceStatus == "on") { dStatus = "<div style='color:${parent.colorOn}'>${deviceStatus}</div>" }
                            if(deviceStatus == "off") { dStatus = "<div style='color:${parent.colorOff}'>${deviceStatus}</div>" }
                        } else {
                            dStatus = deviceStatus
                        }
                    } else {
                        deviceStatus = "unavailable"
                    }
                }

                def lastActivity = it.getLastActivity()
                if(lastActivity) {
                    dateFormatHandler(lastActivity)
                    // Handler Returns newDate
                } else {
                    newDate = "No Data"
                }

                if(logEnable) log.debug "In myStatusHandler - device: ${it.displayName} - myStatus: ${dStatus} - last checked: ${newDate}"

                state.statusCount = state.statusCount + 1

                theName = it.displayName              
                if(sFilter1) { theName = theName.replace("${sFilter1}", "") }
                if(sFilter2) { theName = theName.replace("${sFilter2}", "") }
                if(sFilter3) { theName = theName.replace("${sFilter3}", "") }
                if(sFilter4) { theName = theName.replace("${sFilter4}", "") }

                line = "<tr><td>${theName}<td>${dStatus}<td>${newDate}"
                statusMapPhone += "${theName} \n"
                statusMapPhone += "${dStatus} - ${newDate} \n"

                totalLength = tbl.length() + line.length()
                if(logEnable) log.debug "In myStatusHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"

                if (totalLength < 1007) {
                    tbl += line
                } else {
                    tbl += "</table></div>"
                    if(logEnable) log.debug "${tbl}"
                    if(watchdogTileDevice) {
                        if(logEnable) log.debug "In myStatusHandler - Sending new Status Watchdog data to Tiles (${tileCount})"
                        sending = "${tileCount}::${tbl}"
                        watchdogTileDevice.sendWatchdogStatusMap(sending)
                        tileCount = tileCount + 1
                    }
                    tbl = tblhead + line 
                }
            }
        }

        if (tbl != tblhead) {
            tbl += "</table></div>"
            if(logEnable) log.debug "${tbl}"
            if(watchdogTileDevice) {
                if(logEnable) log.debug "In myStatusHandler - Sending new Status Watchdog data to Tiles (${tileCount})"
                sending = "${tileCount}::${tbl}"
                watchdogTileDevice.sendWatchdogStatusMap(sending)
                tileCount = tileCount + 1
            }
        }

        for(x=tileCount;x<4;x++) {
            sending = "${x}::<div style='font-size:${fontSize}px'>Status Report - No Data</div>"
            watchdogTileDevice.sendWatchdogStatusMap(sending)
        }

        def rightNow = new Date()
        dateFormatHandler(rightNow)
        state.statusMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${newDate}</table>"
        statusMapPhone += "Report generated: ${newDate} \n"
        state.statusMapPhoneS = statusMapPhone
        if(logEnable) log.debug "     - - - - - End (Status) - - - - -     "
    }
}

def myActivityAttHandler() {
    if(activityAttDevices && attOptions) {
        if(useRefresh) refreshDevices(activityDevices)    // Refresh Devices before checking    
        if(logEnable) log.debug "     - - - - - Start (Activity with Attributes) - - - - -     "
        if(logEnable) log.debug "In myActivityAttributeHandler ${state.version}"

        String result1 = attOptions.join(",")
        def theOptions = result1.split(",")               
        int optionSize = theOptions.size()

        if(includeDate) { 
            def rightNow = new Date()
            dateFormatHandler(rightNow)
            reportDateTime = " - ${newDate}"
        } else {
            reportDateTime = ""
        }
        
        if(optionSize >= 1) att1 = theOptions[0]
        if(optionSize >= 2) att2 = theOptions[1]
        if(optionSize >= 3) att3 = theOptions[2]
        if(optionSize >= 4) att4 = theOptions[3]

        if(optionSize == 1) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report${reportDateTime}<td>${att1.capitalize()}<td>Last Activity"

        if(optionSize == 2) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report${reportDateTime}<td>${att1.capitalize()}<td>${att2.capitalize()}<td>Last Activity"

        if(optionSize == 3) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report${reportDateTime}<td>${att1.capitalize()}<td>${att2.capitalize()}<td>${att3.capitalize()}<td>Last Activity"

        if(optionSize == 4) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report${reportDateTime}<td>${att1.capitalize()}<td>${att2.capitalize()}<td>${att3.capitalize()}<td>${att4.capitalize()}<td>Last Activity"

        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.activityAttCount = 0
        state.activityMapPhoneS = ""
        activityMapPhone = "Activity with Attributes Report \n"

        theDevices = activityAttDevices.sort { a, b -> a.displayName <=> b.displayName }    

        theDevices.each { it ->
            if(!it.isDisabled()) {
                if(!laDisplay) {
                    getTimeDiff(it)
                    lastAct = state.theDuration
                } else {
                    theDate = it.getLastActivity()
                    dateFormatHandler(theDate)
                    lastAct = newDate
                }

                if(state.since != null) {
                    if(att1) att1Value = it.currentValue("${att1}")
                    if(att2) att2Value = it.currentValue("${att2}")
                    if(att3) att3Value = it.currentValue("${att3}")
                    if(att4) att4Value = it.currentValue("${att4}")

                    if(att1Value == null) att1Value = "-"
                    if(att2Value == null) att2Value = "-"
                    if(att3Value == null) att3Value = "-"
                    if(att4Value == null) att4Value = "-"

                    theName = it.displayName              
                    if(filter1) { theName = theName.replace("${filter1}", "") }
                    if(filter2) { theName = theName.replace("${filter2}", "") }
                    if(filter3) { theName = theName.replace("${filter3}", "") }
                    if(filter4) { theName = theName.replace("${filter4}", "") }

                    if(optionSize == 1) line = "<tr><td>${theName}<td>${att1Value}<td>${lastAct}"
                    if(optionSize == 2) line = "<tr><td>${theName}<td>${att1Value}<td>${att2Value}<td>${lastAct}"
                    if(optionSize == 3) line = "<tr><td>${theName}<td>${att1Value}<td>${att2Value}<td>${att3Value}<td>${lastAct}"
                    if(optionSize == 4) line = "<tr><td>${theName}<td>${att1Value}<td>${att2Value}<td>${att3Value}<td>${att4Value}<td>${lastAct}"

                    activityMapPhone += "${it.displayName} - ${lastAct} \n"

                    totalLength = tbl.length() + line.length()
                    if(logEnable) log.debug "In myActivityAttributeHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"

                    if (totalLength < 1007) {
                        tbl += line
                    } else {
                        tbl += "</table></div>"
                        if(logEnable) log.debug "${tbl}"
                        if(watchdogTileDevice) {
                            if(logEnable) log.debug "In myActivityAttributeHandler - Sending new Activity Att Watchdog data to Tiles (${tileCount})"
                            sending = "${tileCount}::${tbl}"
                            watchdogTileDevice.sendWatchdogActivityAttMap(sending)
                            tileCount = tileCount + 1
                        }
                        tbl = tblhead + line 
                    }
                }
            }
        }

        if (tbl != tblhead) {
            tbl += "</table></div>"
            if(logEnable) log.debug "${tbl}"
            if(watchdogTileDevice) {
                if(logEnable) log.debug "In myActivityAttributeHandler - Sending new Activity Att Watchdog data to Tiles (${tileCount})"
                sending = "${tileCount}::${tbl}"
                watchdogTileDevice.sendWatchdogActivityAttMap(sending)
                tileCount = tileCount + 1
            }
        }

        for(x=tileCount;x<4;x++) {
            sending = "${x}::<div style='font-size:${fontSize}px'>Activity Att Report - No Data</div>"
            watchdogTileDevice.sendWatchdogActivityAttMap(sending)
        }

        def rightNow = new Date()
        dateFormatHandler(rightNow)
        state.activityAttMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${newDate}</table>"
        activityMapPhone += "Report generated: ${newDate} \n"
        state.activityMapPhoneS = activityMapPhone
        if(logEnable) log.debug "     - - - - - End (Activity with Attributes) - - - - -     "
    }
}

def specialTrackingHandler() {
    if(specialDevices1 && specialOptions1) {   
        if(logEnable) log.debug "     - - - - - Start (Special Tracking) - - - - -     "
        if(logEnable) log.debug "In specialTrackingHandler (${state.version})"

        String result1 = specialOptions1.join(",")
        def theOptions = result1.split(",")               
        int optionSize = theOptions.size()

        if(includeDate) { 
            def rightNow = new Date()
            dateFormatHandler(rightNow)
            reportDateTime = " - ${newDate}"
        } else {
            reportDateTime = ""
        }
        
        if(optionSize >= 1) att1 = theOptions[0]
        if(optionSize >= 2) att2 = theOptions[1]
        if(optionSize >= 3) att3 = theOptions[2]
        if(optionSize >= 4) att4 = theOptions[3]

        if(optionSize == 1) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report${reportDateTime}<td>${att1.capitalize()}"

        if(optionSize == 2) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report${reportDateTime}<td>${att1.capitalize()}<td>${att2.capitalize()}"

        if(optionSize == 3) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report${reportDateTime}<td>${att1.capitalize()}<td>${att2.capitalize()}<td>${att3.capitalize()}"

        if(optionSize == 4) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Special Tracking Report${reportDateTime}<td>${att1.capitalize()}<td>${att2.capitalize()}<td>${att3.capitalize()}<td>${att4.capitalize()}"

        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.specialCount = 0
        state.specialMapPhoneS = ""
        specialMapPhone = "Special Tracking Report \n"

        theDevices = specialDevices1.sort { a, b -> a.displayName <=> b.displayName }    

        theDevices.each { it ->
            data = false
            if(!it.isDisabled()) {
                if(att1) att1Value = it.currentValue("${att1}")
                if(att2) att2Value = it.currentValue("${att2}")
                if(att3) att3Value = it.currentValue("${att3}")
                if(att4) att4Value = it.currentValue("${att4}")

                if(att1Value == null) att1Value = "-"
                if(att2Value == null) att2Value = "-"
                if(att3Value == null) att3Value = "-"
                if(att4Value == null) att4Value = "-"

                if(att1 && att1Value == stAttTValue1) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att1.capitalize()} EQUALS ${stAttTValue1}"
                    data = true
                }
                if(att1 && att1Value == stAttTValue1a) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att1.capitalize()} EQUALS ${stAttTValue1a}"
                    data = true
                }

                if(att2 && att2Value == stAttTValue2) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att2.capitalize()} EQUALS ${stAttTValue2}"
                    data = true
                }
                if(att2 && att2Value == stAttTValue2a) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att2.capitalize()} EQUALS ${stAttTValue2a}"
                    data = true
                }
                
                if(att3 && att3Value == stAttTValue3) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att3.capitalize()} EQUALS ${stAttTValue3}"
                    data = true
                }
                if(att3 && att3Value == stAttTValue3a) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att3.capitalize()} EQUALS ${stAttTValue3a}"
                    data = true
                }
                
                if(att4 && att4Value == stAttTValue4) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att4.capitalize()} EQUALS ${stAttTValue4}"
                    data = true
                }
                if(att4 && att4Value == stAttTValue4a) { 
                    if(logEnable) log.debug "In specialTrackingHandler - ${it.displayName} - Attribute ${att4.capitalize()} EQUALS ${stAttTValue4a}"
                    data = true
                }

                if(data) {
                    state.specialCount = state.specialCount + 1
                    theName = it.displayName              
                    if(stFilter1) { theName = theName.replace("${stFilter1}", "") }
                    if(stFilter2) { theName = theName.replace("${stFilter2}", "") }
                    if(stFilter3) { theName = theName.replace("${stFilter3}", "") }
                    if(stFilter4) { theName = theName.replace("${stFilter4}", "") }

                    if(optionSize == 1) line = "<tr><td>${theName}<td>${att1Value}"
                    if(optionSize == 2) line = "<tr><td>${theName}<td>${att1Value}<td>${att2Value}"
                    if(optionSize == 3) line = "<tr><td>${theName}<td>${att1Value}<td>${att2Value}<td>${att3Value}"
                    if(optionSize == 4) line = "<tr><td>${theName}<td>${att1Value}<td>${att2Value}<td>${att3Value}<td>${att4Value}"

                    if(optionSize == 1) specialMapPhone += "${it.displayName} - ${att1Value} \n"
                    if(optionSize == 2) specialMapPhone += "${it.displayName} - ${att1Value} - ${att2Value} \n"
                    if(optionSize == 3) specialMapPhone += "${it.displayName} - ${att1Value} - ${att2Value} - ${att3Value} \n"
                    if(optionSize == 4) specialMapPhone += "${it.displayName} - ${att1Value} - ${att2Value} - ${att3Value} - ${att4Value} \n"

                    totalLength = tbl.length() + line.length()
                    if(logEnable) log.debug "In specialTrackingHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"

                    if (totalLength < 1007) {
                        tbl += line
                    } else {
                        tbl += "</table></div>"
                        if(logEnable) log.debug "${tbl}"
                        if(watchdogTileDevice) {
                            if(logEnable) log.debug "In specialTrackingHandler - Sending new Special Tracking Watchdog data to Tiles (${tileCount})"
                            sending = "${tileCount}::${tbl}"
                            watchdogTileDevice.sendWatchdogSpecialMap(sending)
                            tileCount = tileCount + 1
                        }
                        tbl = tblhead + line 
                    }
                }
            }
        }

        if (tbl != tblhead) {
            tbl += "</table></div>"
            if(logEnable) log.debug "${tbl}"
            if(watchdogTileDevice) {
                if(logEnable) log.debug "In specialTrackingHandler - Sending new Special Tracking Watchdog data to Tiles (${tileCount})"
                sending = "${tileCount}::${tbl}"
                watchdogTileDevice.sendWatchdogSpecialMap(sending)
            }
        } else {
            if(watchdogTileDevice) {
                if(logEnable) log.debug "In specialTrackingHandler - Sending new Special Tracking Watchdog data to Tiles (${tileCount})"
                sending = "${tileCount}::<div style='font-size:${fontSize}px'>Special Tracking Report - No Data</div>"
                watchdogTileDevice.sendWatchdogSpecialMap(sending)
            }
        }

        def rightNow = new Date()
        dateFormatHandler(rightNow)
        state.specialMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${newDate}</table>"
        specialMapPhone += "Report generated: ${newDate} \n"
        state.specialMapPhoneS = specialMapPhone
        if(logEnable) log.debug "     - - - - - End (Special Tracking) - - - - -     "
    }
}

def refreshDevices(devices) {
    if(logEnable) log.debug "In refreshDevices (${state.version})"

    devices.each { it ->
        if(logEnable) log.debug "---------- ---------- --------- --------- Trying to REFRESH ---------- --------- --------- ---------- ---------"
        getTimeDiff(it)
        actionDelay = actionDelay ?: 250
        if(state.totalHours >= maxTimeDiff) {
            if(it.hasCommand("refresh")) {
                pauseExecution(actionDelay)
                it.refresh()
                if(logEnable) log.debug "In refreshDevices - ${it} attempting update using refresh command"
            } else if(it.hasCommand("configure")) {
                pauseExecution(actionDelay)
                it.configure()
                if(logEnable) log.debug "In refreshDevices - ${it} attempting update using configure command"
            } else {
                if(logEnable) log.debug "In refreshDevices - ${it} not updated - No refresh or configure commands available."
            }
        } else {
            if(logEnable) log.debug "In refreshDevices - ${it} not updated - Time since was only ${state.totalHours} hours."
        }
    }
    if(logEnable) log.debug "In refreshDevices - Pausing 3 seconds, so devices have time to respond."
    pauseExecution(3000)
    if(logEnable) log.debug "---------- ---------- --------- --------- End REFRESH ---------- --------- --------- ---------- ---------"
    if(logEnable) log.debug "In refreshDevices - Finished refreshing!"
}

def getTimeDiff(aDevice) { 
    if(logEnable) log.debug "In getTimeDiff (${state.version}) - working on ${aDevice}"
    try {
	    state.since = aDevice.getLastActivity()
        def prev = Date.parse("yyy-MM-dd HH:mm:ssZ","${state.since}".replace("+00:00","+0000"))
        def now = new Date()
        use(TimeCategory) {       
            state.dur = now - prev
            state.days = state.dur.days
            state.hours = state.dur.hours
            state.minutes = state.dur.minutes
        }
    } catch (e) {
        log.warn "Device Watchdog - ${aDevice} does not have a Last Activity value, This device will not work with Device Watchdog"
    }
    
    if(!state.days) state.days = 0
    if(!state.hours) state.hours = 0
    if(!state.minutes) state.minutes = 0
    state.theDuration =  "${state.days} D, ${state.hours} H, ${state.minutes} M"
        
    theDays = state.days.toInteger()
    theHours = state.hours.toInteger()
    state.totalHours = ((theDays * 24) + theHours)
        
    if(logEnable) log.info "In getTimeDiff - ${aDevice} - dur: ${state.dur} - days: ${state.days} - hours: ${state.hours} - minutes: ${state.minutes} - totalHours: ${state.totalHours}"
}

def setupNewStuff() {
	if(logEnable) log.debug "In setupNewStuff..."
    if(state.batteryMapPhoneS == null) clearMaps()
	if(state.statusMapPhoneS == null) clearMaps()
	if(state.activityMapPhoneS == null) clearMaps()
}
	
def clearMaps() {
    state.batteryMapPhoneS = [:]
	state.statusMapPhoneS = [:]
	state.activityMapPhoneS = [:]
}

def isThereData(){
	if(logEnable) log.debug "In isThereData..."
	if(logEnable) log.debug "In isThereData - Activity - ${state.activityCount}"
    if(isDataActivityDevice) {
        if(state.activityCount >= 1) {
            isDataActivityDevice.on()
        } else {
            isDataActivityDevice.off()
        }
    }

    if(isDataBatteryDevice) {
        if(logEnable) log.debug "In isThereData - Battery - ${state.batteryCount}"
        if(state.batteryCount >= 1) {
            isDataBatteryDevice.on()
        } else {
            isDataBatteryDevice.off()
        }
    }

    if(isDataStatusDevice) {
        if(logEnable) log.debug "In isThereData - Status - ${state.statusCount}"
        if(state.statusCount >= 1) {
            isDataStatusDevice.on()
        } else {
            isDataStatusDevice.off()
        }
    }
    
    if(isDataSpecialDevice) {
        if(logEnable) log.debug "In isThereData - Special Tracking - ${state.specialCount}"
        if(state.specialCount >= 1) {
            isDataSpecialDevice.on()
        } else {
            isDataSpecialDevice.off()
        }
    }
}

def pushNow(){
	if(logEnable) log.debug "In pushNow"
    if(activityPush) {
        if(state.activityCount >= 1) {
            if(logEnable) log.debug "In pushNow - Status - ${state.activityCount}"
            activityPhone = "${state.activityMapPhoneS}"
            if(logEnable) log.debug "In pushNow - Sending message: ${activityPhone}"
            sendPushMessage.deviceNotification(activityPhone)
        } else {
            if(pushAll) {
                if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
            } else {
                emptyMapPhone = "Nothing to report."
                if(logEnable) log.debug "In pushNow - Sending message: ${emptyMapPhone}"
                sendPushMessage.deviceNotification(emptyMapPhone)
            }
        }
    }
    
    if(batteryPush) {
        if(state.batteryCount >= 1) {
            if(logEnable) log.debug "In pushNow - Battery - ${state.batteryCount}"
            batteryPhone = "${state.batteryMapPhoneS}"
            if(logEnable) log.debug "In pushNow - Sending message: ${batteryPhone}"
            sendPushMessage.deviceNotification(batteryPhone)
        } else {
            if(pushAll) {
                if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
            } else {
                emptyBatteryPhone = "Nothing to report."
                if(logEnable) log.debug "In pushNow - Sending message: ${emptyBatteryPhone}"
                sendPushMessage.deviceNotification(emptyBatteryPhone)
            }
        }
    }
    
    if(statusPush) {
        if(logEnable) log.debug "In pushNow - Status - ${state.statusCount}"
        if(state.statusCount >= 1) {
            statusPhone = "${state.statusMapPhoneS}"
            if(logEnable) log.debug "In pushNow - Sending message: ${statusPhone}"
            sendPushMessage.deviceNotification(statusPhone)
        } else {
            if(pushAll) {
                if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
            } else {
                emptyStatusPhone = "Nothing to report."
                if(logEnable) log.debug "In pushNow - Sending message: ${emptyStatusPhone}"
                sendPushMessage.deviceNotification(emptyStatusPhone)
            }
        }	
    }
    
    if(specialTrackingPush) {
        if(logEnable) log.debug "In pushNow - Special Tracking - ${state.specialCount}"
        if(state.specialCount >= 1) {
            specialPhone = "${state.specialMapPhoneS}"
            if(logEnable) log.debug "In pushNow - Sending message: ${specialPhone}"
            sendPushMessage.deviceNotification(specialPhone)
        } else {
            if(pushAll) {
                if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
            } else {
                emptySpecialPhone = "Nothing to report."
                if(logEnable) log.debug "In pushNow - Sending message: ${emptySpecialPhone}"
                sendPushMessage.deviceNotification(emptySpecialPhone)
            }
        }	
    }
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "Device Watchdog Tile", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child tile device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "Device Watchdog unable to create data device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}

def dateFormatHandler(theDate) {
    if(!timeFormat && !mmddyy) { newDate = theDate.format( 'MMM d, yyy - h:mm: a' ) }
    if(timeFormat && !mmddyy) { newDate = theDate.format( 'MMM d, yyy - HH:mm' ) }
    if(!timeFormat && mmddyy) { newDate = theDate.format( 'd MMM, yyy - h:mm: a' ) }
    if(timeFormat && mmddyy) { newDate = theDate.format( 'd MMM, yyy - HH:mm' ) }
    return newDate
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
	setupNewStuff()
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "reports") return "${loc}reports.jpg height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight:bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
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
}
