/**
 *  ****************  Device Watchdog Child ****************
 *
 *  Design Usage:
 *  Keep an eye on your devices and see how long it's been since they checked in.
 *
 *  Copyright 2018-2019 Bryan Turcotte (@bptworld)
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
 *  V1.3.2 - 04/22/19 - Put a trap in to catch devices that have no previous activity.
 *  V1.3.1 - 04/15/19 - More Code cleanup
 *  V1.3.0 - 04/14/19 - Adjusted reports, added importUrl and some code cleanup.
 *  V1.2.9 - 03/31/19 - Fix bug in push for Device Status. Status report now available for dashboard tiles.
 *  V1.2.8 - 03/30/19 - Fix push notifications going out even if there was nothing to report.
 *  V1.2.7 - 03/18/19 - BIG changes due to tile limit size.
 *  V1.2.6 - 03/12/19 - Battery report is now sorted lowest to highest battery percentage. Activity is sorted newest to oldest.
 *						Status is sorted by Device Name.
 *  V1.2.5 - 03/03/19 - Removed some error checking put in v1.2.2 - If you have a device causing an error in the Device Status
 *						reporting, you will need to remove the device from this app rather than the app catching it.
 *  V1.2.4 - 03/03/19 - Functions cleanup. Changes by @gabriele
 *  V1.2.3 - 02/26/19 - Removed Actuator and Sensor options for Device Status reporting
 *  V1.2.2 - 02/26/19 - Attempt to fix an error in the new Device Status reporting
 *  V1.2.1 - 02/25/19 - Second attempt at new Device Status reporting
 *  V1.2.0 - 02/25/19 - Added a new report type - Device Status
 *  V1.1.9 - 02/24/19 - Fixed Pushover reports.
 *  V1.1.8 - 02/16/19 - Trying to track down an error - Resolved.
 *  V1.1.7 - 02/13/19 - Added more error checking.
 *  V1.1.6 - 02/12/19 - Removed 'All battery devices' switch and other code cleanup.
 *  V1.1.5 - 02/11/19 - Fix the previous report not sometimes clearing before displaying the new report.
 *  V1.1.4 - 02/10/19 - Added a switch to run a report any time.
 *  V1.1.3 - 01/31/19 - Fixed Pause and Disable/Enable not working.
 *  V1.1.2 - 01/31/19 - Added ability to turn on a device when there is something to report
 *  V1.1.1 - 01/28/19 - Under the hood rewrite, better reporting. Also added NEW Device Watchdog Tile for use with dashboards
 *  V1.1.0 - 01/25/19 - Added more wording regarding the 'all battery devices' switch
 *  V1.0.9 - 01/17/19 - Toggle switch added, Send or not to send Push notification when there is nothing to report.
 *  V1.0.8 - 01/15/19 - Updated footer with update check and links
 *  V1.0.7 - 01/04/19 - Modification by rayzurbock. Report now shows 'battery level isn't reporting' when a device's battery
 *						attribute is null/blank/non-existent. Previously it showed 0. Also adjusted the output on the Push report.
 *  V1.0.6 - 01/01/19 - Fixed typo in Pushover module.
 *  V1.0.5 - 12/31/18 - Fixed debug logging.
 *  V1.0.4 - 12/30/18 - Updated to my new color theme.
 *  V1.0.3 - 12/30/18 - Added 'app child name' to Pushover reports
 *  V1.0.2 - 12/29/18 - Changed wording on Push notification option to specify Pushover.
 *						Added option to select 'all devices' for Battery Level trigger.
 *						Fixed Pushover to send a 'No devices to report' message instead of a blank message.
 *  V1.0.1 - 12/27/18 - Code cleanup.
 *  V1.0.0 - 12/21/18 - Initial release.
 *
 */


def setVersion() {
	state.version = "v1.3.1"
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
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Device Watchdog</h2>", nextPage: null, install: true, uninstall: true) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- Devices may show up in multiple lists but each device only needs to be selected once.<br>- All changes are saved right away, no need to exit out and back in before generating a new report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			href "pageStatus", title: "Device Report", description: "Click here to view the Device Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Define whether this child app will be for checking Activity, Battery Levels or Status")) {
			input "triggerMode", "enum", required: true, title: "Select Trigger Type", submitOnChange: true,  options: ["Activity", "Battery_Level", "Status"]
		}
// **** Battery Level ****
		if(triggerMode == "Battery_Level") {
			section(getFormat("header-green", "${getImage("Blank")}"+" Select your battery devices")) {
				input "batteryDevice", "capability.battery", title: "Select Battery Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
				input "batteryThreshold", "number", title: "Battery will be considered low when below this level", required: false, submitOnChange: true
				input "timeToRun", "time", title: "Check Devices at this time daily", required: true, submitOnChange: true
				input "isDataBatteryDevice", "capability.switch", title: "Turn this device on if there is Battery data to report", submitOnChange: true, required: false, multiple: false
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false, submitOnChange: true
				if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
			}
			section() {
				input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Below Threshold or Above Threshold", description: "On is Active, Off is Inactive.")
				if(badORgood) {
					paragraph "App will only display Devices ABOVE Threshold."
				} else {
					paragraph "App will only display Devices BELOW Threshold."
				}
			}
			section() {
				input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to run a new report", submitOnChange: true, required: false, multiple: false
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
			section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
				paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
				paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Device Watchdog Tile'<br> - Use our 'Device Watchdog Tile' Driver<br> - Then select 	this new device below"
			paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
			paragraph "- Pick a device = Device Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = EACH attribute holds 5 lines of data. So mulitple boxes are now necessary. The options are watchdogActivity1-5 OR watchdogBattery1-5"
			paragraph "NOTE: There is a MAX of 25 devices that can be shown on the dashboard"
			}
			section() {
				input(name: "watchdogTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
			section() {
				input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    		}
		}
// **** Activity ****		
		if(triggerMode == "Activity") {
			section("<b>Devices may show up in multiple lists but each device only needs to be selected once.</b>") {
				input "accelerationSensorDevice", "capability.accelerationSensor", title: "Select Acceleration Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "alarmDevice", "capability.alarm", title: "Select Alarm Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "batteryDevice", "capability.battery", title: "Select Battery Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "carbonMonoxideDetectorDevice", "capability.carbonMonoxideDetector", title: "Select Carbon Monoxide Detector Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "contactSensorDevice", "capability.contactSensor", title: "Select Contact Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "energyMeterDevice", "capability.energyMeter", title: "Select Energy Meter Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "illuminanceMeasurementDevice", "capability.illuminanceMeasurement", title: "Select Illuminance Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "lockDevice", "capability.lock", title: "Select Lock Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "motionSensorDevice", "capability.motionSensor", title: "Select Motion Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "powerMeterDevice", "capability.powerMeter", title: "Select Power Meter Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "presenceSensorDevice", "capability.presenceSensor", title: "Select Presence Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "pushableButtonDevice", "capability.pushableButton", title: "SelectPushable Button Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "relativeHumidityMeasurementDevice", "capability.relativeHumidityMeasurement", title: "Select Relative Humidity Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "smokeDetectorDevice", "capability.smokeDetector", title: "Select Smoke Detector Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "switchDevice", "capability.switch", title: "Select Switch Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "switchLevelDevice", "capability.switchLevel", title: "Select Switch Level Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "temperatureMeasurementDevice", "capability.temperatureMeasurement", title: "Select Temperature Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "valveDevice", "capability.valve", title: "Select Valve Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "voltageMeasurementDevice", "capability.voltageMeasurement", title: "Select Voltage Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "waterSensorDevice", "capability.waterSensor", title: "Select Water Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
			}
			section("If you have a device not found in the list above, try these two options.") {
				input "actuatorDevice", "capability.actuator", title: "Select Actuator Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "sensorDevice", "capability.sensor", title: "Select Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
				input "timeAllowed", "number", title: "Number of hours for Devices to be considered inactive", required: true, submitOnChange: true
				input "timeToRun", "time", title: "Check Devices at this time daily", required: true, submitOnChange: true
				input "isDataActivityDevice", "capability.switch", title: "Turn this device on if there is Activity data to report", submitOnChange: true, required: false, multiple: false
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
				if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
				//if(sendPushMessage) input(name: "pushOnline", type: "bool", defaultValue: "false", submitOnChange: true, title: "Send another Push when device comes back online", description: "Push when back online")
			}
			section() {
				input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Inactive or active", description: "On is Active, Off is Inactive.")
				if(badORgood) {
					paragraph "App will only display ACTIVE Devices."
				} else {
					paragraph "App will only display INACTIVE Devices."
			}
				}
			section() {
				input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to a run new report", submitOnChange: true, required: false, multiple: false
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {}
			section("Instructions for Dashboard Tile:", hideable: true, hidden: true) {
				paragraph "<b>Want to be able to view your data on a Dashboard? Now you can, simply follow these instructions!</b>"
				paragraph " - Create a new 'Virtual Device'<br> - Name it something catchy like: 'Device Watchdog Tile'<br> - Use our 'Device Watchdog Tile' Driver<br> - Then select this new device below"
				paragraph "Now all you have to do is add this device to one of your dashboards to see your counts on a tile!<br>Add a new tile with the following selections"
				paragraph "- Pick a device = Device Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = EACH attribute holds 5 lines of data. So mulitple boxes are now necessary. The options are watchdogActivity1-5 OR watchdogBattery1-5"
				paragraph "NOTE: There is a MAX of 25 devices that can be shown on the dashboard"
			}
			section() {
				input(name: "watchdogTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
			section() {
				input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
			}
		}
// **** Device Status ****
		if(triggerMode == "Status") {
			section("<b>Devices may show up in multiple lists but each device only needs to be selected once.</b>") {
				input "accelerationSensorDevice", "capability.accelerationSensor", title: "Select Acceleration Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "alarmDevice", "capability.alarm", title: "Select Alarm Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "batteryDevice", "capability.battery", title: "Select Battery Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "carbonMonoxideDetectorDevice", "capability.carbonMonoxideDetector", title: "Select Carbon Monoxide Detector Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "contactSensorDevice", "capability.contactSensor", title: "Select Contact Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "energyMeterDevice", "capability.energyMeter", title: "Select Energy Meter Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "illuminanceMeasurementDevice", "capability.illuminanceMeasurement", title: "Select Illuminance Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "lockDevice", "capability.lock", title: "Select Lock Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "motionSensorDevice", "capability.motionSensor", title: "Select Motion Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "powerMeterDevice", "capability.powerMeter", title: "Select Power Meter Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "presenceSensorDevice", "capability.presenceSensor", title: "Select Presence Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "pushableButtonDevice", "capability.pushableButton", title: "SelectPushable Button Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "relativeHumidityMeasurementDevice", "capability.relativeHumidityMeasurement", title: "Select Relative Humidity Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "smokeDetectorDevice", "capability.smokeDetector", title: "Select Smoke Detector Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "switchDevice", "capability.switch", title: "Select Switch Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "switchLevelDevice", "capability.switchLevel", title: "Select Switch Level Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "temperatureMeasurementDevice", "capability.temperatureMeasurement", title: "Select Temperature Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "valveDevice", "capability.valve", title: "Select Valve Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "voltageMeasurementDevice", "capability.voltageMeasurement", title: "Select Voltage Measurement Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				input "waterSensorDevice", "capability.waterSensor", title: "Select Water Sensor Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
				input "timeToRun", "time", title: "Check Devices at this time daily", required: false, submitOnChange: true
				input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
				if(sendPushMessage) input(name: "pushAll", type: "bool", defaultValue: "false", submitOnChange: true, title: "Only send Push if there is something to actually report", description: "Push All")
			}
			section() {
				input "runReportSwitch", "capability.switch", title: "Turn this switch 'on' to a run new report", submitOnChange: true, required: false, multiple: false
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
		}
		display2()
	}
}

def pageStatus(params) {
	dynamicPage(name: "pageStatus", title: "Device Watchdog - Status", nextPage: null, install: false, uninstall: false) {
		activityHandler()
		if(triggerMode == "Battery_Level") {  // Battery
			if(badORgood == false) {  // less than
				if(state.count >= 1) {
        			section("${state.count} devices have reported Battery levels less than $batteryThreshold - From low to high<br>* Only showing the lowest 25") {
						paragraph "${state.batteryMap1S}<br>${state.batteryMap2S}<br>${state.batteryMap3S}<br>${state.batteryMap4S}<br>${state.batteryMap5S}<br>${state.batteryMap6S}"
        			}
				} else {
					section("${state.count} devices have reported Battery levels less than $batteryThreshold") { 
						paragraph "Nothing to report"
					}
				}
			} else {  // more than
				if(state.count >= 1) {
        			section("${state.count} devices with Battery reporting more than $batteryThreshold - From low to high<br>* Only showing the lowest 25") {
						paragraph "${state.batteryMap1S}<br>${state.batteryMap2S}<br>${state.batteryMap3S}<br>${state.batteryMap4S}<br>${state.batteryMap5S}<br>${state.batteryMap6S}"
        			}
				} else {
					section("${state.count} devices with Battery reporting more than $batteryThreshold") { 
						paragraph "Nothing to report"
					}
				}
			}
		}
		if(triggerMode == "Activity") {
			if(badORgood == false) {
				if(state.count >= 1) {
        			section("${state.count} devices have not reported in for $timeAllowed hour(s)") {
						paragraph "${state.timeSinceMap1S}<br>${state.timeSinceMap2S}<br>${state.timeSinceMap3S}<br>${state.timeSinceMap4S}<br>${state.timeSinceMap5S}<br>${state.timeSinceMap6S}"
        			}
				} else {
					section("${state.count} devices have not reported in for $timeAllowed hour(s)") {
						paragraph "Nothing to report"
        			}
				}
			} else {
				if(state.count >= 1) {
        			section("${state.count} devices have reported in less than $timeAllowed hour(s)") {
						paragraph "${state.timeSinceMap1S}<br>${state.timeSinceMap2S}<br>${state.timeSinceMap3S}<br>${state.timeSinceMap4S}<br>${state.timeSinceMap5S}<br>${state.timeSinceMap6S}"
        			}
				} else {
					section("${state.count} devices have reported in less than $timeAllowed hour(s)") {
						paragraph "Nothing to report"
					}
				}
			}
		}
		if(triggerMode == "Status") {
        	section("Device Status Report") {
				paragraph "${state.statusMap1S}<br>${state.statusMap2S}<br>${state.statusMap3S}<br>${state.statusMap4S}<br>${state.statusMap5S}"
			}
		}
	}
}

def installed() {
    log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {
	if(state.mySensors) state.remove('mySensors')
	if(state.myType) state.remove('myType')
	
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
		if(pauseApp == true){log.warn "${app.label} - App paused"}
    	if(pauseApp == false){
			if(logEnable) log.debug "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
			if(actuatorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Actuator", actuatorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Actuator", actuatorDevice)
				if(triggerMode == "Status") myStatusHandler("Actuator", actuatorDevice)
			}
			if(sensorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Sensor", sensorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Sensor", sensorDevice)
				if(triggerMode == "Status") myStatusHandler("Sensor", sensorDevice)
			}
			if(accelerationSensorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Acceleration", accelerationSensorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Acceleration", accelerationSensorDevice)
				if(triggerMode == "Status") myStatusHandler("Acceleration", accelerationSensorDevice)
			}
			if(alarmDevice) {
				if(triggerMode == "Activity") mySensorHandler("Alarm", alarmDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Alarm", alarmDevice)
				if(triggerMode == "Status") myStatusHandler("Alarm", alarmDevice)
			}
			if(batteryDevice) {
				if(triggerMode == "Activity") mySensorHandler("Battery", batteryDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Battery", batteryDevice)
				if(triggerMode == "Status") myStatusHandler("Battery", batteryDevice)
			}
			if(carbonMonoxideDetectorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Carbon Monoxide Detector", carbonMonoxideDetectorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Carbon Monoxide Detector", carbonMonoxideDetectorDevice)
				if(triggerMode == "Status") myStatusHandler("Carbon Monoxide Detector", carbonMonoxideDetectorDevice)
			}
			if(contactSensorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Contact Sensor", contactSensorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Contact Sensor", contactSensorDevice)
				if(triggerMode == "Status") myStatusHandler("Contact Sensor", contactSensorDevice)
			}
			if(energyMeterDevice) {
				if(triggerMode == "Activity") mySensorHandler("Energy Meter", energyMeterDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Energy Meter", energyMeterDevice)
				if(triggerMode == "Status") myStatusHandler("Energy Meter", energyMeterDevice)
			}
			if(illuminanceMeasurementDevice) {
				if(triggerMode == "Activity") mySensorHandler("Illuminance Measurement", illuminanceMeasurementDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Illuminance Measurement", illuminanceMeasurementDevice)
				if(triggerMode == "Status") myStatusHandler("Illuminance Measurement", illuminanceMeasurementDevice)
			}
			if(lockDevice) {
				if(triggerMode == "Activity") mySensorHandler("Lock", lockDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Lock", lockDevice)
				if(triggerMode == "Status") myStatusHandler("Lock", lockDevice)
			}
			if(motionSensorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Motion Sensor", motionSensorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Motion Sensor", motionSensorDevice)
				if(triggerMode == "Status") myStatusHandler("Motion Sensor", motionSensorDevice)
			}
			if(powerMeterDevice) {
				if(triggerMode == "Activity") mySensorHandler("Power Meter", powerMeterDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Power Meter", powerMeterDevice)
				if(triggerMode == "Status") myStatusHandler("Power Meter", powerMeterDevice)
			}
			if(presenceSensorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Presence Sensor", presenceSensorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Presence Sensor", presenceSensorDevice)
				if(triggerMode == "Status") myStatusHandler("Presence Sensor", presenceSensorDevice)
			}
			if(pushableButtonDevice) {
				if(triggerMode == "Activity") mySensorHandler("Pushable Button", pushableButtonDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Pushable Button", pushableButtonDevice)
				if(triggerMode == "Status") myStatusHandler("Pushable Button", pushableButtonDevice)
			}
			if(relativeHumidityMeasurementDevice) {
				if(triggerMode == "Activity") mySensorHandler("Relative Humidity Measurement", relativeHumidityMeasurementDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Relative Humidity Measurement", relativeHumidityMeasurementDevice)
				if(triggerMode == "Status") myStatusHandler("Relative Humidity Measurement", relativeHumidityMeasurementDevice)
			}
			if(smokeDetectorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Smoke Detector", smokeDetectorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Smoke Detector", smokeDetectorDevice)
				if(triggerMode == "Status") myStatusHandler("Smoke Detector", smokeDetectorDevice)
			}
			if(switchDevice) {
				if(triggerMode == "Activity") mySensorHandler("Switch", switchDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Switch", switchDevice)
				if(triggerMode == "Status") myStatusHandler("Switch", switchDevice)
			}
			if(switchLevelDevice) {
				if(triggerMode == "Activity") mySensorHandler("Switch Level", switchLevelDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Switch Level", switchLevelDevice)
				if(triggerMode == "Status") myStatusHandler("Switch Level", switchLevelDevice)
			}
			if(temperatureMeasurementDevice) {
				if(triggerMode == "Activity") mySensorHandler("Temperature Measurement", temperatureMeasurementDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Temperature Measurement", temperatureMeasurementDevice)
				if(triggerMode == "Status") myStatusHandler("Temperature Measurement", temperatureMeasurementDevice)
			}
			if(valveDevice) {
				if(triggerMode == "Activity") mySensorHandler("Valve", valveDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Valve", valveDevice)
				if(triggerMode == "Status") myStatusHandler("Valve", valveDevice)
			}
			if(voltageMeasurementDevice) {
				if(triggerMode == "Activity") mySensorHandler("Voltage Measurement", voltageMeasurementDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Voltage Measurement", voltageMeasurementDevice)
				if(triggerMode == "Status") myStatusHandler("Voltage Measurement", voltageMeasurementDevice)
			}
			if(waterSensorDevice) {
				if(triggerMode == "Activity") mySensorHandler("Water Sensor", waterSensorDevice)
				if(triggerMode == "Battery_Level") myBatteryHandler("Water Sensor", waterSensorDevice)
				if(triggerMode == "Status") myStatusHandler("Water Sensor", waterSensorDevice)
			}
			if(logEnable) log.debug "     * * * * * * * * End ${app.label} * * * * * * * *     "
			if(watchdogTileDevice) watchdogMapHandler()
			if(isDataActivityDevice) isThereData()
			if(isDataBatteryDevice) isThereData()
			if(isDataStatusDevice) isThereData()
			if(sendPushMessage) pushNow()
		}
}	

def myBatteryHandler(myType, mySensors) {
	if(logEnable) log.debug "     - - - - - Start (B) ${myType} - - - - -     "
	if(logEnable) log.debug "In myBatteryHandler..."
	
	mySensors.each { device ->
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
		state.theBatteryMap.each { it -> 
			if(logEnable) log.debug "In buildBatteryMapHandler - Building Table with ${it.key}"
			def currentValue = it.value
			if(logEnable) log.debug "In myBatteryHandler - ${device} - ${currentValue}"
			if(currentValue < batteryThreshold && currentValue > -999) { //RayzurMod
				if(badORgood == false) {
					state.count = state.count + 1
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

def mySensorHandler(myType, mySensors) {
	if(logEnable) log.debug "     - - - - - Start (S) ${myType} - - - - -     "
	if(logEnable) log.debug "In mySensorHandler - ${mySensors}"
	mySensors.each { device ->
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

def myStatusHandler(myType, mySensors) {
	if(logEnable) log.debug "     - - - - - Start (S) ${myType} - - - - -     "
	if(logEnable) log.debug "In myStatusHandler..."
	state.statusMap = ""
	state.statusDash = ""
	state.statusMapPhone = ""
	
	state.sortedMap = mySensors.sort { a, b -> a.displayName <=> b.displayName }
	
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
	state.sortedMap.each { device ->
		state.count = state.count + 1
		if(logEnable) log.debug "Working on: ${device}"
		if(myType == "Acceleration") { deviceStatus = device.currentValue("accelerationSensor") }
		if(myType == "Alarm") { deviceStatus = device.currentValue("alarm") }
		if(myType == "Battery") { deviceStatus = device.currentValue("battery") }
		if(myType == "Carbon Monoxide Detector") { deviceStatus = device.currentValue("carbonMonoxideDetector") } 
		if(myType == "Contact Sensor") { deviceStatus = device.currentValue("contact") }
		if(myType == "Energy Meter") { deviceStatus = device.currentValue("energyMeter") }
		if(myType == "Illuminance Measurement") { deviceStatus = device.currentValue("illuminanceMeasurement") }
		if(myType == "Lock") { deviceStatus = device.currentValue("lock") }
		if(myType == "Motion Sensor") { deviceStatus = device.currentValue("motion") }
		if(myType == "Power Meter") { deviceStatus = device.currentValue("powerMeter") }
		if(myType == "Presence Sensor") { deviceStatus = device.currentValue("presence") }
		if(myType == "Pushable Button") { deviceStatus = device.currentValue("pushableButton") }
		if(myType == "Relative Humidity Measurement") { deviceStatus = device.currentValue("relativeHumidityMeasurement") }
		if(myType == "Smoke Detector") { deviceStatus = device.currentValue("smokeDetector") }
		if(myType == "Switch") { deviceStatus = device.currentValue("switch") }
		if(myType == "Switch Level") { deviceStatus = device.currentValue("switchLevel") }
		if(myType == "Temperature Measurement") { deviceStatus = device.currentValue("temperatureMeasurement") }
		if(myType == "Valve") { deviceStatus = device.currentValue("valve") }
		if(myType == "Voltage Measurement") { deviceStatus = device.currentValue("voltageMeasurement") }
		if(myType == "Water Sensor") { deviceStatus = device.currentValue("waterSensor") }
		
		if(logEnable) log.debug "In myStatusHandler - Working On: ${device}, myType: ${myType}, deviceStatus: ${deviceStatus}"
		def lastActivity = device.getLastActivity()
		def newDate = lastActivity.format( 'EEE, MMM d,yyy - h:mm:ss a' )
		if(logEnable) log.debug "In myStatusHandler - ${device} - ${newDate}"
		
		if(logEnable) log.debug "${myType} - myStatus: ${device} is ${deviceStatus} - last checked in ${newDate}<br>"
		if((state.count >= 1) && (state.count <= 5)) state.statusMap1S += "<tr><td width='45%'>${device}</td><td width='10%'>${deviceStatus}</td><td width='45%'>${newDate}</td></tr>"
		if((state.count >= 6) && (state.count <= 10)) state.statusMap2S += "<tr><td width='45%'>${device}</td><td width='10%'>${deviceStatus}</td><td width='45%'>${newDate}</td></tr>"
		if((state.count >= 11) && (state.count <= 15)) state.statusMap3S += "<tr><td width='45%'>${device}</td><td width='10%'>${deviceStatus}</td><td width='45%'>${newDate}</td></tr>"
		if((state.count >= 16) && (state.count <= 20)) state.statusMap4S += "<tr><td width='45%'>${device}</td><td width='10%'>${deviceStatus}</td><td width='45%'>${newDate}</td></tr>"
		if((state.count >= 21) && (state.count <= 25)) state.statusMap5S += "<tr><td width='45%'>${device}</td><td width='10%'>${deviceStatus}</td><td width='45%'>${newDate}</td></tr>"
		state.statusMapPhone += "${device} \n"
		state.statusMapPhone += "${deviceStatus} - ${newDate} \n"
	}
	state.statusMap1S += "</table>"
	state.statusMap2S += "</table>"
	state.statusMap3S += "</table>"
	state.statusMap4S += "</table>"
	state.statusMap5S += "</table>"
	if(logEnable) log.debug "     - - - - - End (S) ${myType} - - - - -     "
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
		if(logEnable) log.debug "In isThereData - Activity"
		if(state.timeSinceMapPhoneS) {
			isDataActivityDevice.on()
		} else {
			isDataActivityDevice.off()
		}
	}
	if(triggerMode == "Battery_Level") {
		if(logEnable) log.debug "In isThereData - Battery"
		if(state.batteryMapPhoneS) {
			isDataBatteryDevice.on()
		} else {
			isDataBatteryDevice.off()
		}
	}
	if(triggerMode == "Status") {
		if(logEnable) log.debug "In isThereData - Status"
		if(state.statusMapPhoneS) {
			isDataStatusDevice.on()
		} else {
			isDataStatusDevice.off()
		}
	}
}

def pushNow(){
	if(logEnable) log.debug "In pushNow - triggerMode: ${triggerMode}"
	if(triggerMode == "Activity") {
		if(state.count >= 1) {
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
		if(state.count >= 1) {
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
		if(state.count >= 1) {
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
    if(pauseApp == null){pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(pushAll == null){pushAll = false}
	if(state.reportCount == null){state.reportCount = 0}
}

def getImage(type) {							// Modified Code from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){					// Modified Code from @Stephack
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
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
