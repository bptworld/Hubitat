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
 *  V1.2.4 - 02/27/19 - Add option to send notification when back and functions cleanup
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
	state.version = "v1.2.4"
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
				paragraph "- Pick a device = Device Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = watchdogActivity, watchdogBattery or watchdogStatus"
			}
			section() {
				input(name: "watchdogTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
			section() {
				input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
				input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
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
				input "energyMeteDevicer", "capability.energyMeter", title: "Select Energy Meter Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
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
				if(sendPushMessage) input(name: "pushOnline", type: "bool", defaultValue: "false", submitOnChange: true, title: "Send another Push when device came back online", description: "Push when back online")
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
				paragraph "- Pick a device = Device Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = watchdogActivity or watchdogBattery"
			}
			section() {
				input(name: "watchdogTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
			section() {
				input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
				input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
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
				input "energyMeteDevicer", "capability.energyMeter", title: "Select Energy Meter Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
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
				paragraph "- Pick a device = Device Watchdog Tile<br>- Pick a template = attribute<br>- 3rd box = watchdogActivity, watchdogBattery or watchdogStatus"
			}
			section() {
				input(name: "watchdogTileDevice", type: "capability.actuator", title: "Vitual Device created to send the Data to:", submitOnChange: true, required: false, multiple: false)		
			}
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
			section() {
				input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
				input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
			}
		}
		display2()
	}
}

def pageStatus(params) {
	dynamicPage(name: "pageStatus", title: "Device Watchdog - Status", nextPage: null, install: false, uninstall: false) {
		activityHandler()
		log.warn("state.reportCount: ${state.reportCount} ***")
		log.warn("state.timeSinceMap: ${state.timeSinceMap}")
		if(triggerMode == "Battery_Level") {  // Battery
			if(badORgood == false) {  // less than
				if(state.batteryMap) {
        			section("Devices that have reported Battery levels less than $batteryThreshold") {
						paragraph "${state.batteryMap}"
        			}
				} else {
					section("Devices that have reported Battery levels less than $batteryThreshold") { 
						paragraph "Nothing to report"
					}
				}
			} else {  // more than
				if(state.batteryMap) {
        			section("Devices with Battery reporting more than $batteryThreshold") {
						paragraph "${state.batteryMap}"
        			}
				} else {
					section("Devices with Battery reporting more than $batteryThreshold") { 
						paragraph "Nothing to report"
					}
				}
			}
		}
		if(triggerMode == "Activity") {
			if(badORgood == false) {
				if(state.timeSinceMap) {
        			section("Devices that have not reported in for $timeAllowed hour(s)") {
						paragraph "${state.timeSinceMap}"
        			}
				} else {
					section("Devices that have not reported in for $timeAllowed hour(s)") {
						paragraph "Nothing to report"
        			}
				}
			} else {
				if(state.timeSinceMap) {
        			section("Devices that have reported in less than $timeAllowed hour(s)") {
						paragraph "${state.timeSinceMap}"
        			}
				} else {
					section("Devices that have reported in less than $timeAllowed hour(s)") {
						paragraph "Nothing to report"
					}
				}
			}
		}
		if(triggerMode == "Status") {
        	section("Device Status Report") {
				paragraph "${state.statusMap}"
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
	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	logCheck()
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
			def watchdogActivityMap = "${state.timeSinceMap}"
			LOGDEBUG("In watchdogMapHandler...Sending new Device Watchdog data to ${watchdogTileDevice}")
    		watchdogTileDevice.sendWatchdogActivityMap(watchdogActivityMap)
		} catch (e) {
			log.warn "${app.label}...Can't send data to Tile Device."
			LOGDEBUG("In watchdogMapHandler...${e}")
		}
	}
	if(triggerMode == "Battery_Level") {
		try {
			def watchdogBatteryMap = "${state.batteryMap}"
			LOGDEBUG("In watchdogMapHandler...Sending new Batterry Watchdog data to ${watchdogTileDevice}")
    		watchdogTileDevice.sendWatchdogBatteryMap(watchdogBatteryMap)
		} catch (e) {
			log.warn "${app.label}...Can't send data to Tile Device."
			LOGDEBUG("In watchdogMapHandler...${e}")
		}
	}
	if(triggerMode == "Status") {
		try {
			def watchdogStatusMap = "${state.statusMap}"
			LOGDEBUG("In watchdogStatusMap...Sending new Status Watchdog data to ${watchdogTileDevice}")
    		watchdogTileDevice.sendWatchdogStatusMap(watchdogStatusMap)
		} catch (e) {
			log.warn "${app.label}...Can't send data to Tile Device."
			LOGDEBUG("In watchdogStatusMap...${e}")
		}
	}
}

def activityHandler(evt) {
	clearMaps()
	if(state.enablerSwitch2 == "off") {
		if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
   		if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
			log.info "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
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
			log.info "     * * * * * * * * End ${app.label} * * * * * * * *     "
			def rightNow = new Date()
			if(triggerMode == "Activity") {state.timeSinceMap += "<br>Report generated: ${rightNow}<br>"}
			if(triggerMode == "Battery_Level") {state.batteryMap += "<br>Report generated: ${rightNow}<br>"}
			if(triggerMode == "Status") {state.statusMap += "<br>Report generated: ${rightNow}<br>"}
			if(watchdogTileDevice) watchdogMapHandler()
			if(isDataActivityDevice) isThereData()
			if(isDataBatteryDevice) isThereData()
			if(isDataStatusDevice) isThereData()
			if(sendPushMessage) pushNow()
		}
	}
}	

def myBatteryHandler(myType, mySensors) {
	log.info "     - - - - - Start (B) ${myType} - - - - -     "
	LOGDEBUG("In myBatteryHandler...")
	mySensors.each { device ->
		log.info "Working on... ${device}"
		def currentValue = device.currentValue("battery")
		if(currentValue == null) currentValue = -999  //RayzurMod
		LOGDEBUG("In myBatteryHandler...${device} - ${currentValue}")
		if(currentValue < batteryThreshold && currentValue > -999) { //RayzurMod
			if(badORgood == false) {
				log.info "${myType} - mySensors: ${device} battery is ${currentValue} less than ${batteryThreshold} threshold."
				state.batteryMap += "${myType} - ${device} battery level is ${currentValue}<br>"
				state.batteryMapPhone += "${device} - ${currentValue} \n"
			}
		} else {
			if(badORgood == true && currentValue > -999) { //RayzurMod
				log.info "${myType} - ${device} battery is ${currentValue}, over threshold."
				state.batteryMap += "${myType} - ${device} battery level is ${currentValue}, over threshold.<br>"
				state.batteryMapPhone += "${device} - ${currentValue} \n"
			} else
				if (currentValue == -999) { //RayzurMod
					log.info "${myType} - ${device} battery hasn't reported in." //RayzurMod
					state.batteryMap += "${myType} - <i>${device} battery level isn't reporting</i><br>" //RayzurMod
					state.batteryMapPhone += "${device} - isn't reporting \n" //RayzurMod
				} //RayzurMod
		}
	}
	log.info "     - - - - - End (B) ${myType} - - - - -     "
}

def mySensorHandler(myType, mySensors) {
	log.info "     - - - - - Start (S) ${myType} - - - - -     "
	state.reportCount = 0
	LOGDEBUG("In mySensorHandler...")
	mySensors.each { device ->
		log.info "Working on... ${device}"
		def lastActivity = device.getLastActivity()
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
		LOGDEBUG("${myType} - mySensors: ${device} hour: ${hour} min: ${min}")
		LOGDEBUG("${myType} - mySensors: ${device} hourDiff: ${hourDiff} vs timeAllowed: ${timeAllowed}")
  		if(hourDiff > timeAllowed) {
			if(badORgood == false) {
				log.info "${myType} - ${device} hasn't checked in since ${hour}h ${min}m ago."
				state.timeSinceMap += "${myType} - ${device} hasn't checked in since ${hour}h ${min}m ago.<br>"
				state.timeSinceMapPhone += "${device} - ${hour}h ${min}m \n"
				state.reportCount = state.reportCount + 1
				log.info "state.reportCount: ${state.reportCount}"
				if(pushOnline) {
					subscribe(device, myType, eventCheck, [filterEvents: false])
				}
			}
		} else {
			if(badORgood == true) {
				log.info "${myType} - mySensors: ${device} last checked in ${hour}h ${min}m ago.<br>"
				state.timeSinceMap += "${myType} - ${device} last checked in ${hour}h ${min}m ago.<br>"
				state.timeSinceMapPhone += "${device} - ${hour}h ${min}m \n"
				state.reportCount = state.reportCount + 1
				log.info "state.reportCount: ${state.reportCount}"
			}
		}
	}
	log.info "     - - - - - End (S) ${myType} - - - - -     "
}

def myStatusHandler(myType, mySensors) {
	log.info "     - - - - - Start (S) ${myType} - - - - -     "
	LOGDEBUG("In myStatusHandler...")
	mySensors.each { device ->
		log.info "Working on... ${device}"
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
		
		def noLastActivity = "ok"
		try {
			def lastActivity = device.getLastActivity()
			def newDate = lastActivity.format( 'EEE, MMM d,yyy - h:mm:ss a' )
			LOGDEBUG("In myStatusHandler - No lastActivity is ${device} - ${newDate}")
		} 
		catch (e) {
			LOGDEBUG("In myStatusHandler - No lastActivity on ${device}")
			noLastActivity = "No Last Activity date available"
		}
		LOGDEBUG("In myStatusHandler - noLastActivity: ${noLastActivity}")
		if(noLastActivity == "ok") {
			log.info "${myType} - myStatus: ${device} is ${deviceStatus} - last checked in ${newDate}<br>"
			state.statusMap += "${device} is ${deviceStatus} - last checked in ${newDate}<br>"
			state.statusMapPhone += "${device} \n"
			state.statusMapPhone += "${deviceStatus} - ${newDate} \n"
		} else {
			log.info "${myType} - myStatus: ${device} is ${deviceStatus} - ${noLastActivity}<br>"
			state.statusMap += "${device} is ${deviceStatus} - ${noLastActivity}<br>"
			state.statusMapPhone += "${device} \n"
			state.statusMapPhone += "${deviceStatus} - ${noLastActivity} \n"
		} 
	}
	log.info "     - - - - - End (S) ${myType} - - - - -     "
}

def setupNewStuff() {
	LOGDEBUG("In setupNewStuff...")
	if(state.timeSinceMap == null) clearMaps()
	if(state.timeSinceMapPhone == null) clearMaps()
	if(state.batteryMap == null) clearMaps()
	if(state.batteryMapPhone == null) clearMaps()
	if(state.statusMap == null) clearMaps()
	if(state.statusMapPhone == null) clearMaps()
}
	
def clearMaps() {
	state.timeSinceMap = [:]
	state.timeSinceMapPhone = [:]
	state.timeSinceMap = ""
	state.timeSinceMapPhone = ""
	
	state.batteryMap = [:]
	state.batteryMapPhone = [:]
	state.batteryMap = ""
	state.batteryMapPhone = ""
	
	state.statusMap = [:]
	state.statusMapPhone = [:]
	state.statusMap = ""
	state.statusMapPhone = ""
}

def isThereData(){
	LOGDEBUG("In isThereData...")
	if(triggerMode == "Activity") {
		LOGDEBUG("In isThereData...Activity")
		if(state.timeSinceMapPhone) {
			isDataActivityDevice.on()
		} else {
			isDataActivityDevice.off()
		}
	}
	if(triggerMode == "Battery_Level") {
		LOGDEBUG("In isThereData...Battery")
		if(state.batteryMapPhone) {
			isDataBatteryDevice.on()
		} else {
			isDataBatteryDevice.off()
		}
	}
	if(triggerMode == "Status") {
		LOGDEBUG("In isThereData...Status")
		if(state.statusMapPhone) {
			isDataStatusDevice.on()
		} else {
			isDataStatusDevice.off()
		}
	}
}

def pushNow(){
	LOGDEBUG("In pushNow...triggerMode: ${triggerMode}")
	if(triggerMode == "Activity") {
		if(state.timeSinceMapPhone) {
			timeSincePhone = "${app.label} \n"
			timeSincePhone += "${state.timeSinceMapPhone}"
			LOGDEBUG("In pushNow...Sending message: ${timeSincePhone}")
        	sendPushMessage.deviceNotification(timeSincePhone)
		} else {
			if(pushAll == true) {
				log.info "${app.label} - No push needed...Nothing to report."
			} else {
				emptyMapPhone = "${app.label} \n"
				emptyMapPhone += "Nothing to report."
				LOGDEBUG("In pushNow...Sending message: ${emptyMapPhone}")
        		sendPushMessage.deviceNotification(emptyMapPhone)
			}
		}
	}	
	if(triggerMode == "Battery_Level") {
		if(state.batteryMapPhone) {
			batteryPhone = "${app.label} \n"
			batteryPhone += "${state.batteryMapPhone}"
			LOGDEBUG("In pushNow...Sending message: ${batteryPhone}")
			sendPushMessage.deviceNotification(batteryPhone)
		} else {
			if(pushAll == true) {
				log.info "${app.label} - No push needed...Nothing to report."
			} else {
				emptyBatteryPhone = "${app.label} \n"
				emptyBatteryPhone += "Nothing to report."
				LOGDEBUG("In pushNow...Sending message: ${emptyBatteryPhone}")
        		sendPushMessage.deviceNotification(emptyBatteryPhone)
			}
		}
	}	
	if(triggerMode == "Status") {
		if(state.statusMapPhone) {
			statusPhone = "${app.label} \n"
			statusPhone += "${state.statusMapPhone}"
			LOGDEBUG("In pushNow...Sending message: ${statusPhone}")
			sendPushMessage.deviceNotification(statusPhone)
		} else {
			if(pushAll == true) {
				log.info "${app.label} - No push needed...Nothing to report."
			} else {
				emptyStatusPhone = "${app.label} \n"
				emptyStatusPhone += "Nothing to report."
				LOGDEBUG("In pushNow...Sending message: ${emptyStatusPhone}")
        		sendPushMessage.deviceNotification(emptyStatusPhone)
			}
		}
	}	
}

def eventCheck(evt) {
	def device = evt.getDevice()
	LOGDEBUG("${device} come back online, sending message...")
	sendPushMessage.deviceNotification("${device} is come back online!")
	unsubscribe(device)
}

// ********** Normal Stuff **********

def pauseOrNot(){								// Modified from @Cobra Code
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
	setupNewStuff()
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
	if(state.enablerSwitch2 == null){state.enablerSwitch2 = "off"}
	if(pushAll == null){pushAll = false}
	if(state.reportCount == null){state.reportCount = 0}
}

def logCheck(){									// Modified from @Cobra Code
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){								// Modified from @Cobra Code
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def getImage(type) {							// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
}

def getFormat(type, myText=""){					// Modified from @Stephack Code
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
	section() {
		setVersion()
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Watchdog - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}