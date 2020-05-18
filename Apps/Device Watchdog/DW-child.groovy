/**
 *  ****************  Device Watchdog Child ****************
 *
 *  Design Usage:
 *  Keep an eye on your devices and see how long it's been since they checked in.
 *
 *  Copyright 2018-2020 Bryan Turcotte (@bptworld)
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
 *  2.2.0 - 05/17/20 - 
 *  2.1.9 - 05/17/20 - Added Activity with Attributes report
 *  2.1.8 - 05/14/20 - Added color coding to status reports
 *  2.1.7 - 05/12/20 - Touch up to reports
 *  2.1.6 - 05/12/20 - Overhaul of the push notification sections and reports
 *  2.1.5 - 05/12/20 - another fix and another fix and another fix
 *  2.1.4 - 05/12/20 - Minor fix for lastUpdated not found
 *  2.1.3 - 05/12/20 - Tightening up the code, new combo activity/battery report
 *  2.1.2 - 05/11/20 - Tile scrolling will now auto adjust to tile size!
 *  2.1.1 - 05/10/20 - Fixed refresh handler code
 *  2.1.0 - 05/09/20 - Lets try this again, now using displayName
 *  2.0.9 - 05/09/20 - Added more error trapping, added code to make data scroll within tile if needed. All sorting/display now uses device label.
 *  2.0.8 - 05/09/20 - Major rewrite. Thanks to @arnb for some great suggestions and sample tile code!
 *  2.0.7 - 05/07/20 - Added a disable switch to the brand new amazing feature 'refresh'.
 *  2.0.6 - 05/07/20 - Added 'Device Refresh' to Activity Handler 
 *  2.0.5 - 04/27/20 - Cosmetic changes
 *  2.0.4 - 03/26/20 - BIG changes, streamlined code, fixed status report, *** everyone needs to re-select devices ***
 *  2.0.3 - 01/07/20 - Fixed status of button devices (status report)
 *  2.0.2 - 11/26/19 - Cosmetic changes
 *  2.0.1 - 11/17/19 - Fixed 'turn device on when activity to report', code clean up
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  ---
 *  1.0.0 - 12/21/18 - Initial release.
 *
 */

import groovy.time.TimeCategory

def setVersion(){
    state.name = "Device Watchdog"
	state.version = "2.2.0"
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
            if(batteryDevices || activityDevices || statusDevices) {
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
                href "activityAttConfig", title:"${getImage("optionsGreen")} Activity Report with Attributes Options", description:"Click here for Options"
            } else {
                href "activityAttConfig", title:"${getImage("optionsRed")} Activity Report with Attributes Options", description:"Click here for Options"
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
			input "timeToRun", "time", title: "Check Devices at this time daily", required:false, submitOnChange:true
            input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to a run new report at any time", required:false, submitOnChange:true
            paragraph "Push messages will only go out when Time and/or Switch options are choosen and triggered. This way you can view as many manual reports as needed to troubleshoot your system without being flooded with push notifications."
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification", multiple:true, required:false, submitOnChange:true
            if(sendPushMessage) {
                input "activityPush", "bool", title: "Send Activity Report", defaultValue:false, submitOnChange:true, width:4
                input "batteryPush", "bool", title: "Send Battery Report", defaultValue:false, submitOnChange:true, width:4
                input "statusPush", "bool", title: "Send Status Report", defaultValue:false, submitOnChange:true, width:4
                
                input "pushAll", "bool", title: "Only send Push if there is something to actually report", description: "Push", defaultValue:false, submitOnChange:true
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Table Options")) {
            paragraph "<b>Font Size:</b> Smaller number, smaller characters. So more data can fit on tile."
            
            input "fontSize", "number", title: "Font Size for Reports", required:false, defaultValue:12, submitOnChange:true
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
                input "attOptions", "enum", title: "Attributes to display (up to 4)", options: allAttrs, required:true, multiple:true, submitOnChange:true
            }

            if(attOptions) {               
                String result1 = attOptions.join(",")
                def theOptions = result1.split(",")               
                int optionSize = theOptions.size()
                
                if(optionSize < 5) {
                    if(optionSize >= 1) att1 = theOptions[0]
                    if(optionSize >= 2) att2 = theOptions[1]
                    if(optionSize >= 3) att3 = theOptions[2]
                    if(optionSize >= 4) att4 = theOptions[3]

                    if(optionSize == 1) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>Last Activity</table>"

                    if(optionSize == 2) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>${att2}<td>Last Activity</table>"

                    if(optionSize == 3) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>${att2}<td>${att3}<td>Last Activity</table>"

                    if(optionSize == 4) exDisplay = "<table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>${att2}<td>${att3}<td>${att4}<td>Last Activity</table>"

                    paragraph "<b>Example Report:</b><br><br>${exDisplay}"
                } else {
                    paragraph "<b>Please only choose 4 Attributes.</b>"
                }
            }
        }
        display2()
    }
}

def reportHandler() {
	dynamicPage(name: "reportHandler", title: "", install:false, uninstall:false) {
        display()
        section() {
            input "reportType", "enum", title: "Select Report Type", options: ["Activity", "Battery", "Status", "Activity with Attributes", "Combo-Activity-Battery"], required:true, submitOnChange:true
        }
			
		if(reportType == "Activity") {
            myActivityHandler()
            pauseExecution(1000)
            section() {
                if(activityDevices) {
                    activityMap1 = watchdogTileDevice.currentValue("watchdogActivity1")
                    activityMap2 = watchdogTileDevice.currentValue("watchdogActivity2")
                    activityMap3 = watchdogTileDevice.currentValue("watchdogActivity3")

                    activityCount1 = watchdogTileDevice.currentValue("watchdogActivityCount1")
                    activityCount2 = watchdogTileDevice.currentValue("watchdogActivityCount2")
                    activityCount3 = watchdogTileDevice.currentValue("watchdogActivityCount3")

                    a1 = activityCount1.toInteger()
                    a2 = activityCount2.toInteger()
                    a3 = activityCount3.toInteger()

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

        if(reportType == "Activity with Attributes") {
            myActivityAttHandler()
            pauseExecution(1000)
            section() {
                if(activityDevices) {
                    activityAttMap1 = watchdogTileDevice.currentValue("watchdogActivityAtt1")
                    activityAttMap2 = watchdogTileDevice.currentValue("watchdogActivityAtt2")
                    activityAttMap3 = watchdogTileDevice.currentValue("watchdogActivityAtt3")

                    activityAttCount1 = watchdogTileDevice.currentValue("watchdogActivityAttCount1")
                    activityAttCount2 = watchdogTileDevice.currentValue("watchdogActivityAttCount2")
                    activityAttCount3 = watchdogTileDevice.currentValue("watchdogActivityAttCount3")

                    aa1 = activityAttCount1.toInteger()
                    aa2 = activityAttCount2.toInteger()
                    aa3 = activityAttCount3.toInteger()

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

        if(reportType == "Battery") {
            myBatteryHandler()
            pauseExecution(1000)
            section() {
                if(batteryDevices) {
                    batteryMap1 = watchdogTileDevice.currentValue("watchdogBattery1")
                    batteryMap2 = watchdogTileDevice.currentValue("watchdogBattery2")
                    batteryMap3 = watchdogTileDevice.currentValue("watchdogBattery3")

                    batteryCount1 = watchdogTileDevice.currentValue("watchdogBatteryCount1")
                    batteryCount2 = watchdogTileDevice.currentValue("watchdogBatteryCount2")
                    batteryCount3 = watchdogTileDevice.currentValue("watchdogBatteryCount3")

                    bc1 = batteryCount1.toInteger()
                    bc2 = batteryCount2.toInteger()
                    bc3 = batteryCount3.toInteger()

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
                    statusMap1 = watchdogTileDevice.currentValue("watchdogStatus1")
                    statusMap2 = watchdogTileDevice.currentValue("watchdogStatus2")
                    statusMap3 = watchdogTileDevice.currentValue("watchdogStatus3")

                    statusCount1 = watchdogTileDevice.currentValue("watchdogStatusCount1")
                    statusCount2 = watchdogTileDevice.currentValue("watchdogStatusCount2")
                    statusCount3 = watchdogTileDevice.currentValue("watchdogStatusCount3")

                    s1 = statusCount1.toInteger()
                    s2 = statusCount2.toInteger()
                    s3 = statusCount3.toInteger()

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
        
        if(reportType == "Combo-Activity-Battery") {
            myActivityHandler()
            myBatteryHandler()
            pauseExecution(1000)
            section() {
                paragraph "Remember, this will still have to be under the 1024 character limit on dashboard tiles. So if combining the two reports results in the character count over 1024, it can not be displayed on the dashboard."
                if(batteryDevices && activityDevices) {
                    batteryMap1 = watchdogTileDevice.currentValue("watchdogBattery1")
                    activityMap1 = watchdogTileDevice.currentValue("watchdogActivity1")
                        
                    batteryCount1 = watchdogTileDevice.currentValue("watchdogBatteryCount1")
                    bc1 = batteryCount1.toInteger()
                    
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
	initialize()
}

def initialize() {
	setDefaults()
	if(timeToRun) schedule(timeToRun, activityHandler)
	if(runReportSwitch) subscribe(runReportSwitch, "switch.on", activityHandler)
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def activityHandler(evt) {
	clearMaps()
	if(logEnable) log.debug "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
	if(activityDevices) myActivityHandler()
	if(batteryDevices) myBatteryHandler()
	if(statusDevices) myStatusHandler()
    if(activityAttDevices) myActivityAttHandler()
			
	if(isDataActivityDevice) isThereData()
	if(isDataBatteryDevice) isThereData()
	if(isDataStatusDevice) isThereData()
	if(sendPushMessage) pushNow()
    if(logEnable) log.debug "     * * * * * * * * End ${app.label} * * * * * * * *     "
}	

def myBatteryHandler() {
    if(batteryDevices) {
        if(logEnable) log.debug "     - - - - - Start (Battery) - - - - -     "
        if(logEnable) log.debug "In myBatteryHandler ${state.version}"

        def tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td><b>Battery Report</b><td><b>Level</b><td><b>Last Activity</b>"
        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.batteryCount = 0
        state.batteryMapPhoneS = ""
        batteryMapPhone = "Battery Report \n"
        data = false
        theDevices = batteryDevices.sort { a, b -> a.displayName <=> b.displayName }

        theDevices.each { it ->
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
                    newDate = lastActivity.format( 'EEE, MMM d,yyy - h:mm:ss a' )
                } else {
                    newDate = "No Data"
                }

                line = "<tr><td>${it.displayName}<td>${cv}<td>${newDate}"
                batteryMapPhone += "${it.displayName} - ${cv} \n"

                totalLength = tbl.length() + line.length()
                if(logEnable) log.debug "In myBatteryHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
                if (totalLength < 1009) {
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
        state.batteryMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
        batteryMapPhone += "Report generated: ${rightNow} \n"
        state.batteryMapPhoneS = batteryMapPhone
        if(logEnable) log.debug "     - - - - - End (Battery) - - - - -     "
    }
}

def myActivityHandler() {
    if(activityDevices) {
        if(useRefresh) refreshDevices(activityDevices)    // Refresh Devices before checking    
        if(logEnable) log.debug "     - - - - - Start (Activity) - - - - -     "
        if(logEnable) log.debug "In myActivityHandler ${state.version}"

        def tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td><b>Activity Report</b><td><b>Value</b>"
        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.activityCount = 0
        state.activityMapPhoneS = ""
        activityMapPhone = "Activity Report \n"
        data = false
        theDevices = activityDevices.sort { a, b -> a.displayName <=> b.displayName }    

        theDevices.each { it ->
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
                line = "<tr><td>${it.displayName}<td>${state.theDuration}"
                activityMapPhone += "${it.displayName} - ${state.theDuration} \n"

                totalLength = tbl.length() + line.length()
                if(logEnable) log.debug "In myActivityHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
                if (totalLength < 1009) {
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
        state.activityMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
        activityMapPhone += "Report generated: ${rightNow} \n"
        state.activityMapPhoneS = activityMapPhone
        if(logEnable) log.debug "     - - - - - End (Activity) - - - - -     "
    }
}

def myStatusHandler() {
    if(statusDevices) {
        if(logEnable) log.debug "     - - - - - Start (Status) - - - - -     "
        if(logEnable) log.debug "In myStatusHandler ${state.version}"

        def tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td><b>Device</b><td><b>Status Report</b><td><b>Last Activity</b>"
        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.statusCount = 0
        state.statusMapPhoneS = ""
        statusMapPhone = "Status Report \n"
        sortedMap = statusDevices.sort { a, b -> a.displayName <=> b.displayName }

        sortedMap.each { it ->
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
                    if(deviceStatus == "unlocked") { dStatus = "<div style='color:red'>${${parent.colorUnlocked}}</div>" }
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
                newDate = lastActivity.format( 'EEE, MMM d,yyy - h:mm:ss a' )
            } else {
                newDate = "No Data"
            }

            if(logEnable) log.debug "In myStatusHandler - device: ${it.displayName} - myStatus: ${dStatus} - last checked: ${newDate}"

            state.statusCount = state.statusCount + 1
            line = "<tr><td>${it.displayName}<td>${dStatus}<td>${newDate}"
            statusMapPhone += "${it.displayName} \n"
            statusMapPhone += "${dStatus} - ${newDate} \n"

            totalLength = tbl.length() + line.length()
            if(logEnable) log.debug "In myStatusHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
            if (totalLength < 1009) {
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
        state.statusMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
        activityMapPhone += "Report generated: ${rightNow} \n"
        state.statusMapPhoneS = statusMapPhone
        if(logEnable) log.debug "     - - - - - End (Status) - - - - -     "
    }
}

def myActivityAttHandler() {
    if(activityAttDevices) {
        if(useRefresh) refreshDevices(activityDevices)    // Refresh Devices before checking    
        if(logEnable) log.debug "     - - - - - Start (Activity with Attributes) - - - - -     "
        if(logEnable) log.debug "In myActivityAttributeHandler ${state.version}"

        String result1 = attOptions.join(",")
        def theOptions = result1.split(",")               
        int optionSize = theOptions.size()

        if(optionSize >= 1) att1 = theOptions[0]
        if(optionSize >= 2) att2 = theOptions[1]
        if(optionSize >= 3) att3 = theOptions[2]
        if(optionSize >= 4) att4 = theOptions[3]

        if(optionSize == 1) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>Last Activity"

        if(optionSize == 2) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>${att2}<td>Last Activity"

        if(optionSize == 3) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>${att2}<td>${att3}<td>Last Activity"

        if(optionSize == 4) tblhead = "<div style='overflow:auto;height:90%'><table style='width:100%;line-height:1.00;font-size:${fontSize}px;text-align:left'><tr style='font-weight:bold'><td>Activity with Attributes Report<td>${att1}<td>${att2}<td>${att3}<td>${att4}<td>Last Activity"

        def line = "" 
        def tbl = tblhead
        def tileCount = 1
        state.activityAttCount = 0
        state.activityAttMapPhoneS = ""
        activityAttMapPhone = "Activity with Attributes Report \n"

        theDevices = activityAttDevices.sort { a, b -> a.displayName <=> b.displayName }    

        theDevices.each { it ->
            getTimeDiff(it)
            if(state.since != null) {
                if(att1) att1Value = it.currentValue("${att1}")
                if(att2) att2Value = it.currentValue("${att2}")
                if(att3) att3Value = it.currentValue("${att3}")
                if(att4) att4Value = it.currentValue("${att4}")

                if(att1Value == null) att1Value = "-"
                if(att2Value == null) att2Value = "-"
                if(att3Value == null) att3Value = "-"
                if(att4Value == null) att4Value = "-"

                if(optionSize == 1) line = "<tr><td>${it.displayName}<td>${att1Value}<td>${state.theDuration}"
                if(optionSize == 2) line = "<tr><td>${it.displayName}<td>${att1Value}<td>${att2Value}<td>${state.theDuration}"
                if(optionSize == 3) line = "<tr><td>${it.displayName}<td>${att1Value}<td>${att2Value}<td>${att3Value}<td>${state.theDuration}"
                if(optionSize == 4) line = "<tr><td>${it.displayName}<td>${att1Value}<td>${att2Value}<td>${att3Value}<td>${att4Value}<td>${state.theDuration}"

                activityAttMapPhone += "${it.displayName} - ${state.theDuration} \n"

                totalLength = tbl.length() + line.length()
                if(logEnable) log.debug "In myActivityAttributeHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
                if (totalLength < 1009) {
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
        state.activityAttMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
        activityAttMapPhone += "Report generated: ${rightNow} \n"
        state.activityAttMapPhoneS = activityAttMapPhone
        if(logEnable) log.debug "     - - - - - End (Activity with Attributes) - - - - -     "
    }
}

def refreshDevices(devices) {
    if(logEnable) log.debug "In refreshDevices (${state.version})"

    devices.each { it ->
        if(logEnable) log.debug "---------- ---------- --------- --------- Trying to REFRESH ---------- --------- --------- ---------- ---------"
        getTimeDiff(it)
        if(state.totalHours >= maxTimeDiff) {
            if(it.hasCommand("refresh")) {
                it.refresh()
                if(logEnable) log.debug "In refreshDevices - ${it} attempting update using refresh command"
            } else if(it.hasCommand("configure")) {
                it.configure()
                if(logEnable) log.debug "In refreshDevices - ${it} attempting update using configure command"
            } else {
                if(logEnable) log.debug "In refreshDevices - ${it} not updated - No refresh or configure commands available."
            }
        } else {
            if(logEnable) log.debug "In refreshDevices - ${it} not updated - Time since was only ${state.totalHours} hours."
        }
    }
    if(logEnable) log.debug "---------- ---------- --------- --------- End REFRESH ---------- --------- --------- ---------- ---------"
    if(logEnable) log.debug "In refreshDevices - Finished refreshing!"
}

def getTimeDiff(aDevice) { 
    if(logEnable) log.debug "In getTimeDiff (${state.version}) - working on ${aDevice}"
    try {
	    state.since = aDevice.getLastActivity()
        def prev = Date.parse("yyy-MM-dd HH:mm:ss","${state.since}".replace("+00:00","+0000"))   
    
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
	if(state.activityCount >= 1) {
		isDataActivityDevice.on()
	} else {
			isDataActivityDevice.off()
	}
    
	if(logEnable) log.debug "In isThereData - Battery - ${state.batteryCount}"
	if(state.batteryCount >= 1) {
		isDataBatteryDevice.on()
	} else {
		isDataBatteryDevice.off()
	}
	
	if(logEnable) log.debug "In isThereData - Status - ${state.statusCount}"
	if(state.statusCount >= 1) {
		isDataStatusDevice.on()
	} else {
		isDataStatusDevice.off()
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
            if(pushAll == true) {
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
            if(pushAll == true) {
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
            if(pushAll == true) {
                if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
            } else {
                emptyStatusPhone = "Nothing to report."
                if(logEnable) log.debug "In pushNow - Sending message: ${emptyStatusPhone}"
                sendPushMessage.deviceNotification(emptyStatusPhone)
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

// ********** Normal Stuff **********

def setDefaults(){
	setupNewStuff()
	if(logEnable == null){logEnable = false}
	if(pushAll == null){pushAll = false}
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
        //if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        //if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
