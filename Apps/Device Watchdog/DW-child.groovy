/**
 *  ****************  Device Watchdog Child ****************
 *
 *  Design Usage:
 *  Keep an eye on your devices and see how long it's been since they checked in.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
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
    dynamicPage(name: "pageConfig", title: "<h2 style='color:#1A77C9;font-weight: bold'>Device Watchdog</h2>", nextPage: null, install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- Devices may show up in multiple lists but each device only needs to be selected once.<br>- Be sure to generate a new report before trying to view the 'Last Device Status Report'.<br>- All changes are saved right away, no need to exit out and back in before generating a new report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Reports")) {
			paragraph "<b>Click the button below to generate a new report. Be sure to watch your log to see when it finishes.<br>Usually only takes a minute or two depending on how many devices it's looking at.</b>"
			paragraph "                        ", width:3
			input "runButton", "button", title: "Click here to generate a new report.", width:5
			paragraph "                        ", width:3
			href "pageStatus", title: "Last Device Status Report", description: "Click here to view the Device Status Report."
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Define whether this child app will be for checking Activity or Battery Levels")) {
			input "triggerMode", "enum", required: true, title: "Select Trigger Type", submitOnChange: true,  options: ["Activity", "Battery_Level"]
		}
			if(triggerMode == "Battery_Level") {
				section(getFormat("header-green", "${getImage("Blank")}"+" Select your battery devices")) {
					input(name: "allDevices", type: "bool", defaultValue: "false", title: "Select ALL battery devices?", submitOnChange: "true")
					if(allDevices) {
						paragraph "<b>** This will check all Battery device levels. **</b>"
					} else {
						input "batteryDevice", "capability.battery", title: "Select Battery Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
					}
				}
				section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
					input "batteryThreshold", "number", title: "Battery will be considered low when below this level", required: false, submitOnChange: true
					input "timeToRun", "time", title: "Check Devices at this time daily", required: true, submitOnChange: true
					input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false, submitOnChange: true
				}
				section() {
					input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Below Threshold or Above Threshold", description: "On is Active, Off is Inactive.")
					if(badORgood) {
						paragraph "App will only display Devices ABOVE Threshold."
					} else {
						paragraph "App will only display Devices BELOW Threshold."
					}
				}
				section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false}
				section() {
					input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
					input(name: "debugMode", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
    			}
			} else if(triggerMode == "Activity") {
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
			input "sendPushMessage", "capability.notification", title: "Send a Pushover notification?", multiple: true, required: false
		}
		section() {
			input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Inactive or active", description: "On is Active, Off is Inactive.")
				if(badORgood) {
					paragraph "App will only display ACTIVE Devices."
				} else {
					paragraph "App will only display INACTIVE Devices."
				}
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
	dynamicPage(name: "pageStatus", title: "Device Watchdog - Status", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		if(triggerMode == "Battery_Level") {  // Battery
			if(badORgood == false) {  // less than
				section("Devices that have reported Battery levels less than $batteryThreshold") {}
				if (state.batteryMap) {
        			section() {
						paragraph "${state.batteryMap}"
        			}
				} else {
					section("Oops!") { 
						paragraph "Something went wrong. Please hit 'Done' and come back here again.<br>Still working on what causes this sometimes!"
					}
				}
			} else {  // more than
				section("Devices with Battery reporting more than $batteryThreshold") {}
				if (state.batteryMap) {
        			section() {
						paragraph "${state.batteryMap}"
        			}
				} else {
					section("Oops!") { 
						paragraph "Something went wrong. Please hit 'Done' and come back here again.<br>Still working on what causes this sometimes!"
					}
				}
			}
		}
		if(triggerMode == "Activity") {
			if(badORgood == false) {
				section("Devices that have not reported in for $timeAllowed hour(s)") {}
				if (state.timeSinceMap) {
        			section() {
						paragraph "${state.timeSinceMap}"
        			}
				} else {
					section("Oops!") {
						paragraph "Something went wrong. Please hit 'Done' and come right back here again.<br>Still working on what causes this sometimes!"
					}
				}
			} else {
				section("Devices that have reported in less than $timeAllowed hour(s)") {}
				if (state.timeSinceMap) {
        			section() {
						paragraph "${state.timeSinceMap}"
        			}
				} else {
					section("Oops!") {
						paragraph "Something went wrong. Please hit 'Done' and come right back here again.<br>Still working on what causes this sometimes!"
					}
				}
			}
		}
	}
}

def getImage(type) {
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=35 width=5}>"
}

def getFormat(type, myText=""){
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def installed() {
    log.info "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	logCheck()
	initialize()
}

def initialize() {
	setDefaults()
	if(triggerMode == "Activity") {
		state.timeSinceMap = ""
		state.timeSinceMapPhone = ""
		schedule(timeToRun, activityHandler)
	}
	if(triggerMode == "Battery_Level") {
		state.batteryMap = ""
		state.batteryMapPhone = ""
		if(allDevices) subscribe(location, "battery", activityHandler)
		schedule(timeToRun, activityHandler)
	}
}

def activityHandler(evt) {
	log.info "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
	if(actuatorDevice) {
		state.myType = "Actuator"
		state.mySensors = actuatorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(sensorDevice) {
		state.myType = "Sensor"
		state.mySensors = sensorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(accelerationSensorDevice) {
	  	state.myType = "Acceleration"
		state.mySensors = accelerationSensorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(alarmDevice) {
		state.myType = "Alarm"
		state.mySensors = alarmDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(batteryDevice) {
		state.myType = "Battery"
		state.mySensors = batteryDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(carbonMonoxideDetectorDevice) {
	  	state.myType = "Carbon Monoxide Detector"
		state.mySensors = carbonMonoxideDetectorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(contactSensorDevice) {
		state.myType = "Contact Sensor"
		state.mySensors = contactSensorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(energyMeterDevice) {
		state.myType = "Energy Meter"
		state.mySensors = energyMeterDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(illuminanceMeasurementDevice) {
		state.myType = "Illuminance Measurement"
		state.mySensors = illuminanceMeasurementDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(lockDevice) {
		state.myType = "Lock"
		state.mySensors = lockDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(motionSensorDevice) {
		state.myType = "Motion Sensor"
		state.mySensors = motionSensorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(powerMeterDevice) {
		state.myType = "Power Meter"
		state.mySensors = powerMeterDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(presenceSensorDevice) {
		state.myType = "Presence Sensor"
		state.mySensors = presenceSensorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(pushableButtonDevice) {
		state.myType = "Pushable Button"
		state.mySensors = pushableButtonDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(relativeHumidityMeasurementDevice) {
		state.myType = "Relative Humidity Measurement"
		state.mySensors = relativeHumidityMeasurementDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(smokeDetectorDevice) {
		state.myType = "Smoke Detector"
		state.mySensors = smokeDetectorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(switchDevice) {
		state.myType = "Switch"
		state.mySensors = switchDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(switchLevelDevice) {
		state.myType = "Switch Level"
		state.mySensors = switchLevelDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(temperatureMeasurementDevice) {
		state.myType = "Temperature Measurement"
		state.mySensors = temperatureMeasurementDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(valveDevice) {
		state.myType = "Valve"
		state.mySensors = valveDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(voltageMeasurementDevice) {
		state.myType = "Voltage Measurement"
		state.mySensors = voltageMeasurementDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	if(waterSensorDevice) {
		state.myType = "Water Sensor"
		state.mySensors = waterSensorDevice
		if(triggerMode == "Activity") mySensorHandler()
		if(triggerMode == "Battery_Level") myBatteryHandler()
	}
	log.info "     * * * * * * * * End ${app.label} * * * * * * * *     "
	def rightNow = new Date()
	if(triggerMode == "Activity") {state.timeSinceMap += "<br>Report generated: ${rightNow}<br>"}
	if(triggerMode == "Battery_Level") {state.batteryMap += "<br>Report generated: ${rightNow}<br>"}
	if(sendPushMessage) pushNow()
}	

def myBatteryHandler() {
	log.info "     - - - - - Start (B) ${state.myType} - - - - -     "
	LOGDEBUG("In myBatteryHandler...")
	state.mySensors.each { device ->
		log.info "Working on... ${device}"
		def currentValue = device.currentValue("battery")
		if(currentValue == null) currentValue = -999  //RayzurMod
		LOGDEBUG("In myBatteryHandler...${device} - ${currentValue}")
		if(currentValue < batteryThreshold && currentValue > -999) { //RayzurMod
			if(badORgood == false) {
				log.info "${state.myType} - mySensors: ${device} battery is ${currentValue} less than ${batteryThreshold} threshold."
				state.batteryMap += "${state.myType} - ${device} battery level is ${currentValue}<br>"
				state.batteryMapPhone += "${device}-${currentValue} : "
			}
		} else {
			if(badORgood == true && currentValue > -999) { //RayzurMod
				log.info "${state.myType} - ${device} battery is ${currentValue}, over threshold."
				state.batteryMap += "${state.myType} - ${device} battery level is ${currentValue}, over threshold.<br>"
				state.batteryMapPhone += "${device}-${currentValue} : "
			} else
				if (currentValue == -999) { //RayzurMod
					log.info "${state.myType} - ${device} battery hasn't reported in." //RayzurMod
					state.batteryMap += "${state.myType} - <i>${device} battery level isn't reporting</i><br>" //RayzurMod
					state.batteryMapPhone += "${device}-isn't reporting : " //RayzurMod
				} //RayzurMod
		}
	}
	log.info "     - - - - - End (B) ${state.myType} - - - - -     "
}

def mySensorHandler() {
	log.info "     - - - - - Start (S) ${state.myType} - - - - -     "
	LOGDEBUG("In mySensorHandler...")
	state.mySensors.each { device ->
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
		LOGDEBUG("${state.myType} - mySensors: ${device} hour: ${hour} min: ${min}")
		LOGDEBUG("${state.myType} - mySensors: ${device} hourDiff: ${hourDiff} vs timeAllowed: ${timeAllowed}")
  		if(hourDiff > timeAllowed) {
			if(badORgood == false) {
				log.info "${state.myType} - ${device} hasn't checked in since ${hour}h ${min}m ago."
				state.timeSinceMap += "${state.myType} - ${device} hasn't checked in since ${hour}h ${min}m ago.<br>"
				state.timeSinceMapPhone += "${device}-${hour}h ${min}m : "
			}
		} else {
			if(badORgood == true) {
				log.info "${state.myType} - mySensors: ${device} last checked in ${hour}h ${min}m ago.<br>"
				state.timeSinceMap += "${state.myType} - ${device} last checked in ${hour}h ${min}m ago.<br>"
				state.timeSinceMapPhone += "${device}-${hour}h ${min}m : "
			}
		}
	}
	log.info "     - - - - - End (S) ${state.myType} - - - - -     "
}

def appButtonHandler(btn){
	LOGDEBUG("In appButtonHandler...")
    state.btnCall = btn
	LOGDEBUG("In appButtonHandler...state.btnCall: ${state.btnCall}")
    if(state.btnCall == "runButton"){
        log.info "${app.label} - Run Now button was pressed..."
		state.timeSinceMap = ""
		state.timeSinceMapPhone = ""
		state.batteryMap = ""
		state.batteryMapPhone = ""
		runIn(1, activityHandler)
    }
}

def pushNow(){
	LOGDEBUG("In pushNow...")
	if(triggerMode == "Activity") {
		if(state.timeSinceMapPhone) {
			timeSincePhone = "${app.label} - ${state.timeSinceMapPhone}"
			LOGDEBUG("In pushNow...Sending message: ${timeSincePhone}")
        	sendPushMessage.deviceNotification(timeSincePhone)
		} else {
			emptyMapPhone = "${app.label} - Nothing to report."
			LOGDEBUG("In pushNow...Sending message: ${emptyMapPhone}")
        	sendPushMessage.deviceNotification(emptyMapPhone)
		}
	}	
	if(triggerMode == "Battery_Level") {
		if(state.batteryMapPhone) {
			batteryPhone = "${app.label} - ${state.batteryMapPhone}"
			LOGDEBUG("In pushNow...Sending message: ${batteryPhone}")
			sendPushMessage.deviceNotification(batteryPhone)
		} else {
			emptyBatteryPhone = "${app.label} - Nothing to report."
			LOGDEBUG("In pushNow...Sending message: ${emptyBatteryPhone}")
        	sendPushMessage.deviceNotification(emptyBatteryPhone)
		}
	}	
}

// ********** Normal Stuff **********

def pauseOrNot(){
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
	if(state.timeSinceMap == null){state.timeSinceMap = ""}
	if(state.timeSinceMapPhone == null){state.timeSinceMapPhone = ""}
	if(state.batteryMap == null){state.batteryMap = ""}
	if(state.batteryMapPhone == null){state.batteryMapPhone = ""}
}

def logCheck(){
	state.checkLog = debugMode
	if(state.checkLog == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.checkLog == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.debugMode) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def display() {
	section() {
		paragraph getFormat("line")
		input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false
	}
}

def display2() {
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>Device Watchdog - App Version: 1.0.7 - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a></div>"
	}
}

