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

def setVersion(){
    state.name = "Device Watchdog"
	state.version = "2.0.7"
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
    page(name: "pageConfig")
	page(name: "pageStatus")
}

def pageConfig() {
    dynamicPage(name: "pageConfig", title: "", nextPage: null, install: true, uninstall: true) {	
    display()
		section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- All changes are saved right away, no need to exit out and back in before generating a new report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
            if(triggerMode && devices) {
			    href "pageStatus", title: "${getImage('reports')} Device Report", description: "Click here to view the Device Report."
            } else {
                paragraph "Be sure to fill out the options below, before you can run a report."
            }
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Define whether this child app will be for checking Activity, Battery Levels or Status")) {
			input "triggerMode", "enum", required: true, title: "Select Trigger Type", submitOnChange: true, options: ["Activity", "Battery_Level", "Status"]
		}
        
// **** Battery Level ****
		if(triggerMode == "Battery_Level") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
				input "devices", "capability.battery", title: "Select Battery Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Battery Options")) {
				input "batteryThreshold", "number", title: "Battery will be considered low when below this level", required: false, submitOnChange: true
				input "isDataBatteryDevice", "capability.switch", title: "Turn this device on if there is Battery data to report", submitOnChange: true, required: false, multiple: false
			}
			section() {
				input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Below Threshold or Above Threshold", description: "On is Active, Off is Inactive.")
				if(badORgood) {
					paragraph "App will only display Devices ABOVE Threshold."
				} else {
					paragraph "App will only display Devices BELOW Threshold."
				}
			}
        }

// **** Activity ****		
		if(triggerMode == "Activity") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
				input "devices", "capability.*", title: "Select Device(s)", submitOnChange:true, required:false, multiple:true
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
				input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Inactive or active", description: "On is Active, Off is Inactive.")
				if(badORgood) {
					paragraph "App will only display ACTIVE Devices."
				} else {
					paragraph "App will only display INACTIVE Devices."
                }
            }
        }
        
// **** Device Status ****
		if(triggerMode == "Status") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Select devices")) {
				input "devices", "capability.*", title: "Select Device(s)", submitOnChange:true, required:false, multiple:true
			}
        }
        
// **** All ****
        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
			input "timeToRun", "time", title: "Check Devices at this time daily", required: false, submitOnChange: true
            input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to a run new report", submitOnChange: true, required: false, multiple: false
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
			if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
		}
        
		section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
		section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
			paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
			paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Device Watchdog Tile'<br> - Use our 'Device Watchdog Tile' Driver<br> - Then select this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Device Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = EACH attribute holds 5 lines of data. So mulitple boxes are now necessary. The options are watchdogStatus1-5"
			paragraph "NOTE: There is a MAX of 25 devices that can be shown on the dashboard"
		}
		section() {
			input(name: "watchdogTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
		section() {
			input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
		display2()
	}
}

def pageStatus(params) {
	dynamicPage(name: "pageStatus", title: "", nextPage: null, install: false, uninstall: false) {
		activityHandler()
        display()
		if(triggerMode == "Battery_Level") {  // Battery
			if(badORgood == false) {  // less than
				if(state.count >= 1) {
        			section(getFormat("title2", "${state.count} devices have reported Battery levels less than $batteryThreshold - From low to high<br><small>* Only showing the lowest 25 battery levels</small>")) {
						paragraph "${state.batteryMap1S}<br>${state.batteryMap2S}<br>${state.batteryMap3S}<br>${state.batteryMap4S}<br>${state.batteryMap5S}<br>${state.batteryMap6S}"
        			}
				} else {
					section(getFormat("title2", "${state.count} devices have reported Battery levels less than $batteryThreshold")) { 
						paragraph "Nothing to report"
					}
				}
			} else {  // more than
				if(state.count >= 1) {
        			section(getFormat("title2", "${state.count} devices with Battery reporting more than $batteryThreshold - From low to high<br>* Only showing the lowest 25")) {
						paragraph "${state.batteryMap1S}<br>${state.batteryMap2S}<br>${state.batteryMap3S}<br>${state.batteryMap4S}<br>${state.batteryMap5S}<br>${state.batteryMap6S}"
        			}
				} else {
					section(getFormat("title2", "${state.count} devices with Battery reporting more than $batteryThreshold")) { 
						paragraph "Nothing to report"
					}
				}
			}
		}
		if(triggerMode == "Activity") {
			if(badORgood == false) {
				if(state.count >= 1) {
        			section(getFormat("title2", "${state.count} devices have not reported in for $timeAllowed hour(s)")) {
						paragraph "${state.timeSinceMap1S}<br>${state.timeSinceMap2S}<br>${state.timeSinceMap3S}<br>${state.timeSinceMap4S}<br>${state.timeSinceMap5S}<br>${state.timeSinceMap6S}"
        			}
				} else {
					section(getFormat("title2", "${state.count} devices have not reported in for $timeAllowed hour(s)")) {
						paragraph "Nothing to report"
        			}
				}
			} else {
				if(state.count >= 1) {
        			section(getFormat("title2", "${state.count} devices have reported in less than $timeAllowed hour(s)")) {
						paragraph "${state.timeSinceMap1S}<br>${state.timeSinceMap2S}<br>${state.timeSinceMap3S}<br>${state.timeSinceMap4S}<br>${state.timeSinceMap5S}<br>${state.timeSinceMap6S}"
        			}
				} else {
					section(getFormat("title2", "${state.count} devices have reported in less than $timeAllowed hour(s)")) {
						paragraph "Nothing to report"
					}
				}
			}
		}
		if(triggerMode == "Status") {
        	section(getFormat("title2", "Device Status Report")) {
				paragraph "${state.statusMap1S}<br>${state.statusMap2S}<br>${state.statusMap3S}<br>${state.statusMap4S}<br>${state.statusMap5S}"
			}
		}
        section() { paragraph getFormat("line") }
	}
}

def installed() {
    log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
   	if(logEnable) log.debug "Updated with settings: ${settings}"
    unsubscribe()
	initialize()
}

def initialize() {
	setDefaults()
	if(triggerMode == "Activity") {
		schedule(timeToRun, activityHandler)
	}
	if(triggerMode == "Battery_Level") {
		schedule(timeToRun, activityHandler)
	}
	if(triggerMode == "Status") {
		if(timeToRun) schedule(timeToRun, activityHandler)
	}
	if(runReportSwitch) subscribe(runReportSwitch, "switch", activityHandler)
}

def watchdogMapHandler(evt) {
	if(triggerMode == "Activity") {
		try {
			def watchdogActivityMap1 = "${state.timeSinceMap1S}"
			def watchdogActivityMap2 = "${state.timeSinceMap2S}"
			def watchdogActivityMap3 = "${state.timeSinceMap3S}"
			def watchdogActivityMap4 = "${state.timeSinceMap4S}"
			def watchdogActivityMap5 = "${state.timeSinceMap5S}"
			if(logEnable) log.debug "In watchdogMapHandler - Sending new Device Watchdog data to Tiles"
    		watchdogTileDevice.sendWatchdogActivityMap1(watchdogActivityMap1)
			watchdogTileDevice.sendWatchdogActivityMap2(watchdogActivityMap2)
			watchdogTileDevice.sendWatchdogActivityMap3(watchdogActivityMap3)
			watchdogTileDevice.sendWatchdogActivityMap4(watchdogActivityMap4)
			watchdogTileDevice.sendWatchdogActivityMap5(watchdogActivityMap5)
		} catch (e) {
			log.warn "${app.label} - Can't send data to Tile Device."
			if(logEnable) log.debug "In watchdogMapHandler - ${e}"
		}
	}
	if(triggerMode == "Battery_Level") {
		try {
			def watchdogBatteryMap1 = "${state.batteryMap1S}"
			def watchdogBatteryMap2 = "${state.batteryMap2S}"
			def watchdogBatteryMap3 = "${state.batteryMap3S}"
			def watchdogBatteryMap4 = "${state.batteryMap4S}"
			def watchdogBatteryMap5 = "${state.batteryMap5S}"
			if(logEnable) log.debug "In watchdogMapHandler - Sending new Battery Watchdog data to Tiles"
    		watchdogTileDevice.sendWatchdogBatteryMap1(watchdogBatteryMap1)
			watchdogTileDevice.sendWatchdogBatteryMap2(watchdogBatteryMap2)
			watchdogTileDevice.sendWatchdogBatteryMap3(watchdogBatteryMap3)
			watchdogTileDevice.sendWatchdogBatteryMap4(watchdogBatteryMap4)
			watchdogTileDevice.sendWatchdogBatteryMap5(watchdogBatteryMap5)
		} catch (e) {
			log.warn "${app.label} - Can't send data to Tile Device."
			if(logEnable) log.debug "In watchdogMapHandler - ${e}"
		}
	}
	if(triggerMode == "Status") {
		try {
			def watchdogStatusMap1 = "${state.statusMap1S}"
			def watchdogStatusMap2 = "${state.statusMap2S}"
			def watchdogStatusMap3 = "${state.statusMap3S}"
			def watchdogStatusMap4 = "${state.statusMap4S}"
			def watchdogStatusMap5 = "${state.statusMap5S}"
			if(logEnable) log.debug "In watchdogStatusMap - Sending new Status Watchdog data to Tiles"
    		watchdogTileDevice.sendWatchdogStatusMap1(watchdogStatusMap1)
			watchdogTileDevice.sendWatchdogStatusMap2(watchdogStatusMap2)
			watchdogTileDevice.sendWatchdogStatusMap3(watchdogStatusMap3)
			watchdogTileDevice.sendWatchdogStatusMap4(watchdogStatusMap4)
			watchdogTileDevice.sendWatchdogStatusMap5(watchdogStatusMap5)
		} catch (e) {
			log.warn "${app.label} - Can't send data to Tile Device."
			if(logEnable) log.debug "In watchdogStatusMap - ${e}"
		}
	}
}

def activityHandler(evt) {
	clearMaps()
			if(logEnable) log.debug "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
			if(devices) {
				if(triggerMode == "Activity") mySensorHandler()
				if(triggerMode == "Battery_Level") myBatteryHandler()
				if(triggerMode == "Status") myStatusHandler()
			}
			
			if(logEnable) log.debug "     * * * * * * * * End ${app.label} * * * * * * * *     "
			if(watchdogTileDevice) watchdogMapHandler()
			if(isDataActivityDevice) isThereData()
			if(isDataBatteryDevice) isThereData()
			if(isDataStatusDevice) isThereData()
			if(sendPushMessage) pushNow()
}	

def myBatteryHandler() {
	if(logEnable) log.debug "     - - - - - Start (B) - - - - -     "
    if(logEnable) log.debug "In myBatteryHandler ${state.version}"
	
	devices.each { device ->
		def currentValue = device.currentValue("battery")
		if(currentValue == null) currentValue = -999  //RayzurMod
		state.batteryMap.put(device, currentValue)
		if(logEnable) log.debug "Working on: ${device} - ${currentValue}"
	}
	
	state.theBatteryMap = state.batteryMap.sort { a, b -> a.value <=> b.value }

	state.batteryMap1S = ""
	state.batteryMap2S = ""
	state.batteryMap3S = ""
	state.batteryMap4S = ""
	state.batteryMap5S = ""
	state.batteryMap6S = ""
	state.batteryMapPhoneS = ""
	
	if(logEnable) log.debug "In myBatteryHandler - ${state.theBatteryMap}"			 
	state.batteryMap1S = "<table width='100%'>"
	state.batteryMap2S = "<table width='100%'>"
	state.batteryMap3S = "<table width='100%'>"
	state.batteryMap4S = "<table width='100%'>"
	state.batteryMap5S = "<table width='100%'>"
	if(state.theBatteryMap) {
		state.count = 0
		state.batteryCount = 0
		state.theBatteryMap.each { it -> 
			if(logEnable) log.debug "In buildBatteryMapHandler - Building Table with ${it.key}"
			def currentValue = it.value
			if(logEnable) log.debug "In myBatteryHandler - ${device} - ${currentValue}"
			if(currentValue < batteryThreshold && currentValue > -999) { //RayzurMod
				if(badORgood == false) {
					state.count = state.count + 1
					state.batteryCount = state.batteryCount + 1
					if(logEnable) log.debug "mySensors: ${it.key} battery is ${it.value} less than ${batteryThreshold} threshold"
					if(state.count == 1) state.batteryMap1S += "<tr><td width='90%'><b>Battery Devices</b></td><td width='10%'><b>Value</b></td></tr>"
					if((state.count >= 1) && (state.count <= 5)) state.batteryMap1S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 6) && (state.count <= 10)) state.batteryMap2S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 11) && (state.count <= 15)) state.batteryMap3S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 16) && (state.count <= 20)) state.batteryMap4S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 21) && (state.count <= 25)) state.batteryMap5S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					state.batteryMapPhoneS += "${it.key} - ${it.value} \n"
				}
			} else {
				if(badORgood == true && currentValue > -999) { //RayzurMod
					state.count = state.count + 1
					if(logEnable) log.debug "${it.key} battery is ${currentValue}, over threshold"
					if(state.count == 1) state.batteryMap1S += "<tr><td width='90%'><b>Battery Devices - over threshold</b></td><td width='10%'><b>Value</b></td></tr>"
					if((state.count >= 1) && (state.count <= 5)) state.batteryMap1S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 6) && (state.count <= 10)) state.batteryMap2S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 11) && (state.count <= 15)) state.batteryMap3S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 16) && (state.count <= 20)) state.batteryMap4S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					if((state.count >= 21) && (state.count <= 25)) state.batteryMap5S += "<tr><td width='90%'>${it.key}</td><td width='10%'>${it.value}</td></tr>"
					state.batteryMapPhoneS += "${it.key} - ${it.value} \n"
				} else {
					if (currentValue == -999) { //RayzurMod
						state.count = state.count + 1
						if(logEnable) log.debug "${myType} - ${it.key} battery hasn't reported in." //RayzurMod
						if(state.count == 1) state.batteryMap1S += "<tr><td width='90%'><b>Battery Devices - over threshold</b></td><td width='10%'><b>Value</b></td></tr>"
						if((state.count >= 1) && (state.count <= 5)) state.batteryMap1S += "<tr><td colspan='2'><i>${it.key} hasn't reported in</i></td></tr>" //RayzurMod
						if((state.count >= 6) && (state.count <= 10)) state.batteryMap2S += "<tr><td colspan='2'><i>${it.key} hasn't reported in</i></td></tr>"
						if((state.count >= 11) && (state.count <= 15)) state.batteryMap3S += "<tr><td colspan='2'><i>${it.key} hasn't reported in</i></td></tr>"
						if((state.count >= 16) && (state.count <= 20)) state.batteryMap4S += "<tr><td colspan='2'><i>${it.key} hasn't reported in</i></td></tr>"
						if((state.count >= 21) && (state.count <= 25)) state.batteryMap5S += "<tr><td colspan='2'><i>${it.key} hasn't reported in</i></td></tr>"
						state.batteryMapPhoneS += "${it.key} - isn't reporting \n" //RayzurMod
					}
				}
			}
		}
	} else {
		if(state.theBatteryMap == null) state.batteryMap1S = " Nothing to display"
	}
	def rightNow = new Date()
	state.batteryMap1S += "</table>"
	state.batteryMap2S += "</table>"
	state.batteryMap3S += "</table>"
	state.batteryMap4S += "</table>"
	state.batteryMap5S += "</table>"
	state.batteryMap6S += "<table width='100%'><tr><td colspan='2'>Report generated: ${rightNow}</td></tr></table>"
	state.batteryMapPhoneS += "Report generated: ${rightNow} \n"
	if(logEnable) log.debug "     - - - - - End (B) ${myType} - - - - -     "
}

def mySensorHandler() {
    if(useRefresh) refreshDevices()    // Refresh Devices before checking
    
	if(logEnable) log.debug "     - - - - - Start (S) - - - - -     "
    if(logEnable) log.debug "In mySensorHandler ${state.version}"
	devices.each { device ->
		def lastActivity = device.getLastActivity()
		state.timeSinceMap.put(device, lastActivity)
		if(logEnable) log.debug "Working on - ${device} - ${lastActivity}"
	}
	state.timeSinceMap1S = ""
	state.timeSinceMap2S = ""
	state.timeSinceMap3S = ""
	state.timeSinceMap4S = ""
	state.timeSinceMap5S = ""
	state.timeSinceMapPhoneS = ""
	state.theTimeSinceMap = state.timeSinceMap.sort { a, b -> b.value <=> a.value }
	if(logEnable) log.debug "In mySensorHandler - $state.theTimeSinceMap}"			 
	state.timeSinceMap1S = "<table width='100%'>"
	state.timeSinceMap2S = "<table width='100%'>"
	state.timeSinceMap3S = "<table width='100%'>"
	state.timeSinceMap4S = "<table width='100%'>"
	state.timeSinceMap5S = "<table width='100%'>"
	state.timeSinceMap6S = "<table width='100%'>"
	state.count = 0
    state.reportCount = 0
	state.timeSinceCount = 0
	state.theTimeSinceMap.each { device ->
		if(logEnable) log.debug "Working on: ${device.key} ${state.count}"
		def theName = device.key
		def lastActivity = device.value
		if(lastActivity != null) {
    		long timeDiff
   			def now = new Date()
    		def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
    		long unxNow = now.getTime()
    		long unxPrev = prev.getTime()
    		unxNow = unxNow/1000
    		unxPrev = unxPrev/1000
    		timeDiff = Math.abs(unxNow-unxPrev)
    		timeDiff = Math.round(timeDiff/60)
			hourDiff = timeDiff / 60
    		int hour = Math.floor(timeDiff / 60)
			int min = timeDiff % 60
			if(logEnable) log.debug "mySensors: ${theName} hour: ${hour} min: ${min}"
			if(logEnable) log.debug "mySensors: ${theName} hourDiff: ${hourDiff} vs timeAllowed: ${timeAllowed}"
  			if(hourDiff > timeAllowed) {
				if(badORgood == false) {
					state.count = state.count + 1
					state.timeSinceCount = state.timeSinceCount + 1
					if(logEnable) log.debug "${device} hasn't checked in since ${hour}h ${min}m ago."
					if(state.count == 1) state.timeSinceMap1S += "<tr><td width='80%'><b>Device Last Checked In</b></td><td width='20%'><b>Value</b></td></tr>"
					if((state.count >= 1) && (state.count <= 5)) state.timeSinceMap1S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 6) && (state.count <= 10)) state.timeSinceMap2S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 11) && (state.count <= 15)) state.timeSinceMap3S += "<tr><td width='80%'>${theName}</td><td width='20%'>in ${hour}h ${min}m</td></tr>"
					if((state.count >= 16) && (state.count <= 20)) state.timeSinceMap4S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 21) && (state.count <= 25)) state.timeSinceMap5S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					state.timeSinceMapPhoneS += "${theName} - ${hour}h ${min}m \n"
					state.reportCount = state.reportCount + 1
					if(pushOnline) {
						subscribe(device.key, myType, eventCheck)
					}
				}
			} else {
				if(badORgood == true) {
					state.count = state.count + 1
					if(logEnable) log.debug "${myType} - mySensors: ${theName} last checked in ${hour}h ${min}m ago.<br>"
					if(state.count == 1) state.timeSinceMap1S += "<tr><td width='80%'><b>Device Last Checked In</b></td><td width='20%'><b>Value</b></td></tr>"
					if((state.count >= 1) && (state.count <= 5)) state.timeSinceMap1S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 6) && (state.count <= 10)) state.timeSinceMap2S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 11) && (state.count <= 15)) state.timeSinceMap3S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 16) && (state.count <= 20)) state.timeSinceMap4S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					if((state.count >= 21) && (state.count <= 25)) state.timeSinceMap5S += "<tr><td width='80%'>${theName}</td><td width='20%'>${hour}h ${min}m</td></tr>"
					state.timeSinceMapPhoneS += "${theName} - ${hour}h ${min}m \n"
				}
			}
		} else {
			log.warn "${app.label} - ${theName} has no activity. It will not show up in the reports."
		}
	}
	def rightNow = new Date()
	state.timeSinceMap1S += "</table>"
	state.timeSinceMap2S += "</table>"
	state.timeSinceMap3S += "</table>"
	state.timeSinceMap4S += "</table>"
	state.timeSinceMap5S += "</table>"
	state.timeSinceMap6S += "<table><tr><td colspan='2'>Report generated: ${rightNow}</td></tr></table>"
	state.timeSinceMapPhoneS += "Report generated: ${rightNow} \n"
	
	tsMap1Size = state.timeSinceMap1S.length()
	tsMap2Size = state.timeSinceMap2S.length()
	tsMap3Size = state.timeSinceMap3S.length()
	tsMap4Size = state.timeSinceMap4S.length()
	tsMap5Size = state.timeSinceMap5S.length()
	
	if(tsMap1Size <= 1000) {
		if(logEnable) log.debug "${app.label} - Activity 1 - Characters: ${tsMap1Size}"
	} else {
		log.warn "${app.label} - Activity 1 - Too many characters to display on Dashboard"
		state.timeSinceMap1S = "Too many characters to display on Dashboard"
	}
	if(logEnable) log.debug "     - - - - - End (S) ${myType} - - - - -     "
}

def myStatusHandler() {
	if(logEnable) log.debug "     - - - - - Start (S) - - - - -     "
    if(logEnable) log.debug "In myStatusHandler ${state.version}"
	state.statusMap = ""
	state.statusDash = ""
	state.statusMapPhone = ""
	
	state.sortedMap = devices.sort { a, b -> a.displayName <=> b.displayName }
    if(logEnable) log.debug "In myStatusHandler - sortedMap: ${state.sortedMap}"
	
	state.statusMap1S = ""
	state.statusMap2S = ""
	state.statusMap3S = ""
	state.statusMap4S = ""
	state.statusMap5S = ""
	state.statusMap1S = "<table width='100%'>"
	state.statusMap2S = "<table width='100%'>"
	state.statusMap3S = "<table width='100%'>"
	state.statusMap4S = "<table width='100%'>"
	state.statusMap5S = "<table width='100%'>"
	state.count = 0
	state.statusCount = 0
	state.sortedMap.each { device ->
		state.count = state.count + 1
		state.statusCount = state.statusCount + 1
        deviceStatus = null
		if(logEnable) log.debug "Working on: ${device}"
        if(device.hasAttribute("accelerationSensor")) deviceStatus = device.currentValue("accelerationSensor")
		if(device.hasAttribute("alarm")) deviceStatus = device.currentValue("alarm")
		if(device.hasAttribute("battery")) deviceStatus = device.currentValue("battery")
		if(device.hasAttribute("carbonMonoxideDetector")) deviceStatus = device.currentValue("carbonMonoxideDetector")		
		if(device.hasAttribute("energyMeter")) deviceStatus = device.currentValue("energyMeter")
		if(device.hasAttribute("illuminanceMeasurement")) deviceStatus = device.currentValue("illuminanceMeasurement")
		if(device.hasAttribute("lock")) deviceStatus = device.currentValue("lock")
		if(device.hasAttribute("powerMeter")) deviceStatus = device.currentValue("powerMeter")
		if(device.hasAttribute("presence")) deviceStatus = device.currentValue("presence")
        if(device.hasAttribute("pushed")) deviceStatus = device.currentValue("pushed")
		if(device.hasAttribute("relativeHumidityMeasurement")) deviceStatus = device.currentValue("relativeHumidityMeasurement")
		if(device.hasAttribute("smokeDetector")) deviceStatus = device.currentValue("smokeDetector")
		if(device.hasAttribute("switchLevel")) deviceStatus = device.currentValue("switchLevel")
		if(device.hasAttribute("temperatureMeasurement")) deviceStatus = device.currentValue("temperatureMeasurement")
		if(device.hasAttribute("valve")) deviceStatus = device.currentValue("valve")
		if(device.hasAttribute("voltageMeasurement")) deviceStatus = device.currentValue("voltageMeasurement")
		if(device.hasAttribute("waterSensor")) deviceStatus = device.currentValue("waterSensor")
        
        if(device.hasAttribute("motion")) deviceStatus = device.currentValue("motion")
        if(device.hasAttribute("contact")) deviceStatus = device.currentValue("contact")
        
        if(deviceStatus == null || deviceStatus == "") {
            if(device.hasAttribute("switch")) {
                deviceStatus = device.currentValue("switch")
            } else {
                deviceStatus = "unavailable"
            }
        }
        
		def lastActivity = device.getLastActivity()
		def newDate = lastActivity.format( 'EEE, MMM d,yyy - h:mm:ss a' )
		
		if(logEnable) log.debug "In myStatusHandler - device: ${device} - myStatus: ${deviceStatus} - last checked: ${newDate}"
		if((state.count >= 1) && (state.count <= 5)) state.statusMap1S += "<tr><td width='45%'>${device}</td><td width='20%'> ${deviceStatus} </td><td width='35%'>${newDate}</td></tr>"
		if((state.count >= 6) && (state.count <= 10)) state.statusMap2S += "<tr><td width='45%'>${device}</td><td width='20%'> ${deviceStatus} </td><td width='35%'>${newDate}</td></tr>"
		if((state.count >= 11) && (state.count <= 15)) state.statusMap3S += "<tr><td width='45%'>${device}</td><td width='20%'> ${deviceStatus} </td><td width='35%'>${newDate}</td></tr>"
		if((state.count >= 16) && (state.count <= 20)) state.statusMap4S += "<tr><td width='45%'>${device}</td><td width='20%'> ${deviceStatus} </td><td width='35%'>${newDate}</td></tr>"
		if((state.count >= 21) && (state.count <= 25)) state.statusMap5S += "<tr><td width='45%'>${device}</td><td width='20%'> ${deviceStatus} </td><td width='35%'>${newDate}</td></tr>"
		state.statusMapPhone += "${device} \n"
		state.statusMapPhone += "${deviceStatus} - ${newDate} \n"
	}
	state.statusMap1S += "</table>"
	state.statusMap2S += "</table>"
	state.statusMap3S += "</table>"
	state.statusMap4S += "</table>"
	state.statusMap5S += "</table>"
	if(logEnable) log.debug "     - - - - - End (S) - - - - -     "
}

def refreshDevices() {
    if(logEnable) log.debug "In refreshDevices (${state.version})"
    devices.each { it ->
        if(logEnable) log.debug "---------- ---------- --------- --------- Trying to REFRESH ---------- --------- --------- ---------- ---------"
        getTimeDiff(it)
        if(state.timeHrs >= maxTimeDiff) {
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
            if(logEnable) log.debug "In refreshDevices - ${it} not updated - Time since was only ${state.timeHrs} hours."
        }
    }
    if(logEnable) log.debug "---------- ---------- --------- --------- End REFRESH ---------- --------- --------- ---------- ---------"
    if(logEnable) log.debug "In refreshDevices - Finished refreshing!"
}

def getTimeDiff(aDevice) { 
    if(logEnable) log.debug "In getTimeDiff (${state.version}) - working on ${aDevice}"
	def since = aDevice.getLastActivity()
    def prev = Date.parse("yyy-MM-dd HH:mm:ss","${since}".replace("+00:00","+0000"))    
   	def now = new Date()
    
    long unxNow = now.getTime()
    long unxPrev = prev.getTime()
    unxNow = unxNow/1000
    unxPrev = unxPrev/1000
    long timeDiff = Math.abs(unxNow-unxPrev)
    state.tDiff = timeDiff   
    state.timeHrs = (timeDiff / 3600).toInteger()   
    if(logEnable) log.debug "In getTimeDiff - ${aDevice} - since: ${since}, Now: ${unxNow}, Diff: ${timeDiff} - ${state.timeHrs} hrs"
}

def setupNewStuff() {
	if(logEnable) log.debug "In setupNewStuff..."
	if(state.timeSinceMap == null) clearMaps()
	if(state.timeSinceMapPhone == null) clearMaps()
	if(state.batteryMap == null) clearMaps()
	if(state.batteryMapPhone == null) clearMaps()
	if(state.statusMap == null) clearMaps()
	if(state.statusMapPhone == null) clearMaps()
	if(state.timeSinceMap1S == null) clearMaps()
	if(state.timeSinceMap2S == null) clearMaps()
	if(state.timeSinceMap3S == null) clearMaps()
	if(state.timeSinceMap4S == null) clearMaps()
	if(state.timeSinceMap5S == null) clearMaps()
	if(state.timeSinceMapPhoneS == null) clearMaps()
	if(state.batteryMap1S == null) clearMaps()
	if(state.batteryMap2S == null) clearMaps()
	if(state.batteryMap3S == null) clearMaps()
	if(state.batteryMap4S == null) clearMaps()
	if(state.batteryMap5S == null) clearMaps()
	if(state.batteryMap6S == null) clearMaps()
	if(state.batteryMapPhoneS == null) clearMaps()
	if(state.statusMapS == null) clearMaps()
	if(state.statusMapPhoneS == null) clearMaps()
}
	
def clearMaps() {
	state.timeSinceMap = [:]
	state.timeSinceMapPhone = [:]
	state.batteryMap = [:]
	state.batteryMapPhone = [:]
	state.statusMap = [:]
	state.statusMapPhone = [:]
	state.timeSinceMap1S = [:]
	state.timeSinceMap2S = [:]
	state.timeSinceMap3S = [:]
	state.timeSinceMap4S = [:]
	state.timeSinceMap5S = [:]
	state.timeSinceMap6S = [:]
	state.timeSinceMapPhoneS = [:]
	state.batteryMap1S = [:]
	state.batteryMap2S = [:]
	state.batteryMap3S = [:]
	state.batteryMap4S = [:]
	state.batteryMap5S = [:]
	state.batteryMap6S = [:]
	state.batteryMapPhoneS = [:]
	state.statusMapS = [:]
	state.statusMapPhoneS = [:]
}

def isThereData(){
	if(logEnable) log.debug "In isThereData..."
	if(triggerMode == "Activity") {
		if(logEnable) log.debug "In isThereData - Activity - ${state.timeSinceCount}"
		if(state.timeSinceCount >= 1) {
			isDataActivityDevice.on()
		} else {
			isDataActivityDevice.off()
		}
	}
	if(triggerMode == "Battery_Level") {
		if(logEnable) log.debug "In isThereData - Battery - ${state.batteryCount}"
		if(state.batteryCount >= 1) {
			isDataBatteryDevice.on()
		} else {
			isDataBatteryDevice.off()
		}
	}
	if(triggerMode == "Status") {
		if(logEnable) log.debug "In isThereData - Status - ${state.statusCount}"
		if(state.statusCount >= 1) {
			isDataStatusDevice.on()
		} else {
			isDataStatusDevice.off()
		}
	}
}

def pushNow(){
	if(logEnable) log.debug "In pushNow - triggerMode: ${triggerMode}"
	if(triggerMode == "Activity") {
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
	}	
	if(triggerMode == "Battery_Level") {
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
	}	
	if(triggerMode == "Status") {
		if(logEnable) log.debug "In pushNow - Status - ${state.statusCount}"
		if(state.statusCount >= 1) {
			statusPhone = "${app.label} \n"
			statusPhone += "${state.statusMapPhone}"
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
}

def eventCheck(evt) {						// Added by @gabriele
	def device = evt.getDevice()
	if(logEnable) log.debug "In eventCheck - ${device} is back online, sending Pushover message"
	sendPushMessage.deviceNotification("${device} is back online!")
	unsubscribe(device)
}

// ********** Normal Stuff **********

def setDefaults(){
	setupNewStuff()
	if(logEnable == null){logEnable = false}
	if(pushAll == null){pushAll = false}
	if(state.reportCount == null){state.reportCount = 0}
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
