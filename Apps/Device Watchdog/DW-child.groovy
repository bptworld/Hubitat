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
 *  2.1.0 - 05/09/20 - Let's try this again, now using displayName
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
	state.version = "2.1.0"
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
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
			input "timeToRun", "time", title: "Check Devices at this time daily", required:false, submitOnChange:true
            input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to a run new report at any time", required:false, submitOnChange:true
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification", multiple:true, required:false, submitOnChange:true
			if(sendPushMessage) input "pushAll", "bool", title: "Only send Push if there is something to actually report", description: "Push", defaultValue:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Table Options")) {
            paragraph "<b>Font Size:</b> Smaller number, smaller characters. So more data can fit on tile.<br><b>Table Height:</b> Adjust this number to match your tile size. When set correctly, data will scroll within the tile if needed."
            
            input "fontSize", "number", title: "Font Size for Reports", required:true, defaultValue:12, submitOnChange:true
            input "tableHeight", "number", title: "Table Height for Reports", required:true, defaultValue:70, submitOnChange:true  
		}
        
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this automation", required:false
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
        }
        display2()
    }
}

def reportHandler() {
	dynamicPage(name: "reportHandler", title: "", install:false, uninstall:false) {
		activityHandler()
        pauseExecution(1000)
        display()
        section() {
        input "reportType", "enum", title: "Select Report Type", options: ["Activity", "Battery", "Status"], required:true, submitOnChange:true
        }
		if(reportType == "Battery") {  // Battery
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

                    if(bc1 >= 15) {
                        paragraph "${batteryMap1}<br>Tile Count: ${bc1}"
                        paragraph "<hr>"
                    }
                    if(bc2 >= 15) {
                        paragraph "${batteryMap2}<br>Tile Count: ${bc2}"
                        paragraph "<hr>"
                    }
                    if(bc3 >= 15) {
                        paragraph "${batteryMap3}<br>Tile Count: ${bc3}"
                    }

                    if(bc1 < 15 && bc2 < 15 && bc3 < 15) {
                        paragraph "Nothing to report"
                    }
                    paragraph "${state.batteryMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
        	}
        }
			
		if(reportType == "Activity") {
            section() {
                if(activityDevices) {
                    timeSinceMap1 = watchdogTileDevice.currentValue("watchdogActivity1")
                    timeSinceMap2 = watchdogTileDevice.currentValue("watchdogActivity2")
                    timeSinceMap3 = watchdogTileDevice.currentValue("watchdogActivity3")

                    timeSinceCount1 = watchdogTileDevice.currentValue("watchdogActivityCount1")
                    timeSinceCount2 = watchdogTileDevice.currentValue("watchdogActivityCount2")
                    timeSinceCount3 = watchdogTileDevice.currentValue("watchdogActivityCount3")

                    ts1 = timeSinceCount1.toInteger()
                    ts2 = timeSinceCount2.toInteger()
                    ts3 = timeSinceCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - ts1: ${ts1} - ts2: ${ts2} - ts3: ${ts3}"

                    if(ts1 >= 15) {
                        paragraph "${timeSinceMap1}<br>Tile Count: ${ts1}"
                        paragraph "<hr>"
                    }
                    if(ts2 >= 15) {
                        paragraph "${timeSinceMap2}<br>Tile Count: ${ts2}"
                        paragraph "<hr>"
                    }
                    if(ts3 >= 15) {
                        paragraph "${timeSinceMap3}<br>Tile Count: ${ts3}"
                    }

                    if(ts1 < 15 && ts2 < 15 && ts3 < 15) {
                        paragraph "Nothing to report"
                    }
                    paragraph "${state.activityMapGen}"
                } else {
                    paragraph "No devices have been selected for this option."
                }
            }
		}

		if(reportType == "Status") {
        	section() {
                if(statusDevices) {
                    statusMap1 = watchdogTileDevice.currentValue("watchdogStatus1")
                    statusMap2 = watchdogTileDevice.currentValue("watchdogStatus2")
                    statusMap3 = watchdogTileDevice.currentValue("watchdogStatus3")

                    statusCount1 = watchdogTileDevice.currentValue("watchdogStatusCount1")
                    statusCount2 = watchdogTileDevice.currentValue("watchdogStatusCount2")
                    statusCount3 = watchdogTileDevice.currentValue("watchdogStatusCount3")

                    ts1 = statusCount1.toInteger()
                    ts2 = statusCount2.toInteger()
                    ts3 = statusCount3.toInteger()

                    if(logEnable) log.debug "In reportHandler - ts1: ${ts1} - ts2: ${ts2} - ts3: ${ts3}"

                    if(ts1 >= 15) {
                        paragraph "${statusMap1}<br>Tile Count: ${ts1}"
                        paragraph "<hr>"
                    }
                    if(ts2 >= 15) {
                        paragraph "${statusMap2}<br>Tile Count: ${ts2}"
                        paragraph "<hr>"
                    }
                    if(ts3 >= 15) {
                        paragraph "${statusMap3}<br>Tile Count: ${ts3}"
                    }

                    if(ts1 < 15 && ts2 < 15 && ts3 < 15) {
                        paragraph "Nothing to report"
                    }
                    paragraph "${state.statusMapGen}"
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
	schedule(timeToRun, activityHandler)

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
			
	if(isDataActivityDevice) isThereData()
	if(isDataBatteryDevice) isThereData()
	if(isDataStatusDevice) isThereData()
	if(sendPushMessage) pushNow()
    if(logEnable) log.debug "     * * * * * * * * End ${app.label} * * * * * * * *     "
}	

def myBatteryHandler() {
	if(logEnable) log.debug "     - - - - - Start (Battery) - - - - -     "
    if(logEnable) log.debug "In myBatteryHandler ${state.version}"
	
    def tblhead = "<div style='overflow:auto;height:${tableHeight}px'><table width=100% style='line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td width=80%><b>Battery Devices</b><td width=20%><b>Value</b>"
    def line = "" 
    def tbl = tblhead
    def tileCount = 1
    state.batteryCount = 0
    state.batteryMapPhoneS = ""
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
            line = "<tr><td>${it.displayName}<td>${cv}"
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
        sending = "${x}::No Data"
        watchdogTileDevice.sendWatchdogBatteryMap(sending)
    }
    
    def rightNow = new Date()
    state.batteryMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
	batteryMapPhone += "Report generated: ${rightNow} \n"
    state.batteryMapPhoneS = batteryMapPhone
	if(logEnable) log.debug "     - - - - - End (Battery) - - - - -     "
}

def myActivityHandler() {
    if(useRefresh) refreshDevices()    // Refresh Devices before checking    
	if(logEnable) log.debug "     - - - - - Start (Activity) - - - - -     "
    if(logEnable) log.debug "In myActivityHandler ${state.version}"
    
    def tblhead = "<div style='overflow:auto;height:${tableHeight}px'><table width=100% style='line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td width=60%><b>Device Activity</b><td width=40%><b>Value</b>"
    def line = "" 
    def tbl = tblhead
    def tileCount = 1
	state.timeSinceCount = 0
	state.timeSinceMapPhoneS = ""
    data = false
    theDevices = activityDevices.sort { a, b -> a.displayName <=> b.displayName }    
    
    theDevices.each { it ->
        getTimeDiff(it)
		if(state.since != null) {
			if(logEnable) log.debug "In myActivityHandler - ${it.displayName} totalHours: ${state.totalHours} vs timeAllowed: ${timeAllowed}"
  			if(state.totalHours > timeAllowed) {
				if(!activityBadORgood) {
					state.timeSinceCount = state.timeSinceCount + 1
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
            timeSinceMapPhone += "${it.displayName} - ${state.theDuration} \n"

            totalLength = tbl.length() + line.length()
            if(logEnable) log.debug "In myActivityHandler - tbl Count: ${tbl.length()} - line Count: ${line.length()} - Total Count: ${totalLength}"
            if (totalLength < 1009) {
                tbl += line
            } else {
                tbl += "</table></div>"
                if(logEnable) log.debug "${tbl}"
                if(watchdogTileDevice) {
                    if(logEnable) log.debug "In myActivityHandler - Sending new Battery Watchdog data to Tiles (${tileCount})"
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
            if(logEnable) log.debug "In myActivityHandler - Sending new Battery Watchdog data to Tiles (${tileCount})"
            sending = "${tileCount}::${tbl}"
    	    watchdogTileDevice.sendWatchdogActivityMap(sending)
            tileCount = tileCount + 1
        }
    }
    
    for(x=tileCount;x<4;x++) {
        sending = "${x}::No Data"
        watchdogTileDevice.sendWatchdogActivityMap(sending)
    }
    
    def rightNow = new Date()
    state.activityMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
	timeSinceMapPhone += "Report generated: ${rightNow} \n"
    state.timeSinceMapPhoneS = timeSinceMapPhone
	if(logEnable) log.debug "     - - - - - End (Activity) - - - - -     "
}

def myStatusHandler() {
	if(logEnable) log.debug "     - - - - - Start (Status) - - - - -     "
    if(logEnable) log.debug "In myStatusHandler ${state.version}"
    
    def tblhead = "<div style='overflow:auto;height:${tableHeight}px'><table width=100% style='line-height:1.00;font-size:${fontSize}px;text-align:left'><tr><td width=45%><b>Device</b><td width=20%><b>Status</b><td width=35%><b>Last Activity</b>"
    def line = "" 
    def tbl = tblhead
    def tileCount = 1
    state.statusCount = 0
	state.statusMapPhoneS = ""
    sortedMap = statusDevices.sort { a, b -> a.displayName <=> b.displayName }
    
    sortedMap.each { it ->
        deviceStatus = null
		if(logEnable) log.debug "In myStatusHandler - Working on: ${it.displayName}"
        if(it.hasAttribute("accelerationSensor")) deviceStatus = it.currentValue("accelerationSensor")
		if(it.hasAttribute("alarm")) deviceStatus = it.currentValue("alarm")
		if(it.hasAttribute("battery")) deviceStatus = it.currentValue("battery")
		if(it.hasAttribute("carbonMonoxideDetector")) deviceStatus = it.currentValue("carbonMonoxideDetector")		
		if(it.hasAttribute("energyMeter")) deviceStatus = it.currentValue("energyMeter")
		if(it.hasAttribute("illuminanceMeasurement")) deviceStatus = it.currentValue("illuminanceMeasurement")
		if(it.hasAttribute("lock")) deviceStatus = it.currentValue("lock")
		if(it.hasAttribute("powerMeter")) deviceStatus = it.currentValue("powerMeter")
		if(it.hasAttribute("presence")) deviceStatus = it.currentValue("presence")
        if(it.hasAttribute("pushed")) deviceStatus = it.currentValue("pushed")
		if(it.hasAttribute("relativeHumidityMeasurement")) deviceStatus = it.currentValue("relativeHumidityMeasurement")
		if(it.hasAttribute("smokeDetector")) deviceStatus = it.currentValue("smokeDetector")
		if(it.hasAttribute("switchLevel")) deviceStatus = it.currentValue("switchLevel")
		if(it.hasAttribute("temperatureMeasurement")) deviceStatus = it.currentValue("temperatureMeasurement")
		if(it.hasAttribute("valve")) deviceStatus = it.currentValue("valve")
		if(it.hasAttribute("voltageMeasurement")) deviceStatus = it.currentValue("voltageMeasurement")
		if(it.hasAttribute("waterSensor")) deviceStatus = it.currentValue("waterSensor")
        
        if(it.hasAttribute("motion")) deviceStatus = it.currentValue("motion")
        if(it.hasAttribute("contact")) deviceStatus = it.currentValue("contact")
        
        if(deviceStatus == null || deviceStatus == "") {
            if(it.hasAttribute("switch")) {
                deviceStatus = it.currentValue("switch")
            } else {
                deviceStatus = "unavailable"
            }
        }
        
		def lastActivity = it.getLastActivity()
		def newDate = lastActivity.format( 'EEE, MMM d,yyy - h:mm:ss a' )
		
		if(logEnable) log.debug "In myStatusHandler - device: ${it.displayName} - myStatus: ${deviceStatus} - last checked: ${newDate}"
        
        state.statusCount = state.statusCount + 1
        line = "<tr><td>${it.displayName}<td>${deviceStatus}<td>${newDate}"
        statusMapPhone += "${it.displayName} \n"
		statusMapPhone += "${deviceStatus} - ${newDate} \n"

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
        sending = "${x}::No Data"
        watchdogTileDevice.sendWatchdogStatusMap(sending)
    }
    
    def rightNow = new Date()
    state.statusMapGen = "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</table>"
	timeSinceMapPhone += "Report generated: ${rightNow} \n"
    state.statusMapPhoneS = statusMapPhone
	if(logEnable) log.debug "     - - - - - End (Status) - - - - -     "
}

def refreshDevices() {
    if(logEnable) log.debug "In refreshDevices (${state.version})"
    devices.each { it ->
        if(logEnable) log.debug "---------- ---------- --------- --------- Trying to REFRESH ---------- --------- --------- ---------- ---------"
        getTimeDiff(it)
        if(state.hour >= maxTimeDiff) {
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
            if(logEnable) log.debug "In refreshDevices - ${it} not updated - Time since was only ${state.hour} hours."
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
	if(state.timeSinceMapPhoneS == null) clearMaps()
}
	
def clearMaps() {
    state.batteryMapPhoneS = [:]
	state.statusMapPhoneS = [:]
	state.timeSinceMapPhoneS = [:]
}

def isThereData(){
	if(logEnable) log.debug "In isThereData..."
	if(logEnable) log.debug "In isThereData - Activity - ${state.timeSinceCount}"
	if(state.timeSinceCount >= 1) {
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
	if(logEnable) log.debug "In pushNow - Activity - ${state.timeSinceCount}"
	if(state.timeSinceCount >= 1) {
		timeSincePhone = "${app.label} \n"
		timeSincePhone += "${state.timeSinceMapPhoneS}"
		if(logEnable) log.debug "In pushNow - Sending message: ${timeSincePhone}"
        sendPushMessage.deviceNotification(timeSincePhone)
	} else {
		if(pushAll == true) {
			if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
		} else {
			emptyMapPhone = "${app.label} \n"
			emptyMapPhone += "Nothing to report."
			if(logEnable) log.debug "In pushNow - Sending message: ${emptyMapPhone}"
        	sendPushMessage.deviceNotification(emptyMapPhone)
		}
	}
    
	if(state.batteryCount >= 1) {
		if(logEnable) log.debug "In pushNow - Battery - ${state.batteryCount}"
		batteryPhone = "${app.label} \n"
		batteryPhone += "${state.batteryMapPhoneS}"
		if(logEnable) log.debug "In pushNow - Sending message: ${batteryPhone}"
		sendPushMessage.deviceNotification(batteryPhone)
	} else {
		if(pushAll == true) {
			if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
		} else {
			emptyBatteryPhone = "${app.label} \n"
			emptyBatteryPhone += "Nothing to report."
			if(logEnable) log.debug "In pushNow - Sending message: ${emptyBatteryPhone}"
        	sendPushMessage.deviceNotification(emptyBatteryPhone)
		}
	}
    
	if(logEnable) log.debug "In pushNow - Status - ${state.statusCount}"
	if(state.statusCount >= 1) {
		statusPhone = "${app.label} \n"
		statusPhone += "${state.statusMapPhoneS}"
		if(logEnable) log.debug "In pushNow - Sending message: ${statusPhone}"
		sendPushMessage.deviceNotification(statusPhone)
	} else {
		if(pushAll == true) {
			if(logEnable) log.debug "${app.label} - No push needed - Nothing to report."
		} else {
			emptyStatusPhone = "${app.label} \n"
			emptyStatusPhone += "Nothing to report."
			if(logEnable) log.debug "In pushNow - Sending message: ${emptyStatusPhone}"
        	sendPushMessage.deviceNotification(emptyStatusPhone)
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
