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
 *
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
    dynamicPage(name: "pageConfig", title: "Device Watchdog", nextPage: null, install: true, uninstall: true, refreshInterval:0) {	
    display()
		section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
			paragraph "- Devices may show up in multiple lists but each device only needs to be selected once.<br>- Be sure to generate a new report before trying to view the 'Last Device Status Report'."	
			paragraph "<b>Bonus Feature:</b>"
			paragraph "- All changes are saved right away, no need to exit out and back in before generating a new report."
		}
		section("<b>Click the button below to generate a new report. Be sure to watch your log to see when it finishes.<br>Usually only takes a minute or two depending on how many devices it's looking at.</b>") {
			paragraph ">   >   >   >   >   >   ", width:3
			input "runButton", "button", title: "Click here to generate a new report.", width:5
			paragraph "   <   <   <   <   <   <", width:3
			href "pageStatus", title: "Last Device Status Report", description: "Click here to view the Device Status Report."
		}
   		section("<b>Setup to automatically run once a day</b>") {
			input "timeToRun", "time", title: "Check Devices at this time daily", required: true, submitOnChange: true
		}
		section("<b>Define whether this child app will be for checking Activity or Battery Levels</b>") {
			input(name: "activityORbattery", type: "bool", defaultValue: "false", submitOnChange: true, title: "Activity or Battery", description: "")
			paragraph "Off to check Activity, On to check battery levels."
		}
			if(activityORbattery) {
				section("<b>Select your battery devices</b>") {
					input "batteryDevice", "capability.battery", title: "Select Battery Device(s)", submitOnChange: true, hideWhenEmpty: true, required: false, multiple: true
				}
				section("<b>Options:</b>") {
					input "batteryThreshold", "number", title: "Battery will be considered low when below this level", required: false, submitOnChange: true
					input "sendPushMessage", "capability.notification", title: "Send a push notification?", multiple: true, required: false, submitOnChange: true
				}
			} else {
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
		section("<b>Options:</b>") {
			input "timeAllowed", "number", title: "Number of hours for Devices to be considered inactive", required: true, submitOnChange: true
		}
		section() {
			input "sendPushMessage", "capability.notification", title: "Send a push notification?", multiple: true, required: false
		}
				}  // End of activityORbattery Switch
		section() {label title: "Enter a name for this automation", required: false, submitOnChange: true}
		section() {
			input(name: "enablerSwitch1", type: "capability.switch", title: "Enable/Disable child app with this switch - If Switch is ON then app is disabled, if Switch is OFF then app is active.", required: false, multiple: false)
		}
        section() {
            input(name: "debugMode", type: "bool", defaultValue: "true", submitOnChange: true, title: "Enable Debug Logging", description: "Enable extra logging for debugging.")
		}
			if(activityORbattery) {
				section() {
					input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Below Threshold or Above Threshold", description: "On is Active, Off is Inactive.")
					if(badORgood) {
						paragraph "App will only display Devices ABOVE Threshold."
					} else {
						paragraph "App will only display Devices BELOW Threshold."
					}
				}
			} else {
				section() {
					input(name: "badORgood", type: "bool", defaultValue: "false", submitOnChange: true, title: "Inactive or active", description: "On is Active, Off is Inactive.")
					if(badORgood) {
						paragraph "App will only display ACTIVE Devices."
					} else {
						paragraph "App will only display INACTIVE Devices."
					}
				}
			}
	}
}

def pageStatus(params) {
	dynamicPage(name: "pageStatus", title: "Device Watchdog - Status", nextPage: null, install: false, uninstall: false, refreshInterval:0) {
		if(activityORbattery) {  // Battery
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
		} else {  // Activity
			if(badORgood == false) {
				section("Devices that have not reported in for $timeAllowed hour(s)") {}
				if (state.timeSinceMap) {
        			section() {
						paragraph "${state.timeSinceMap}"
        			}
				} else {
					section("Oops!") {
						paragraph "Something went wrong. Please hit 'Done' and come back here again.<br>Still working on what causes this sometimes!"
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
						paragraph "Something went wrong. Please hit 'Done' and come back here again.<br>Still working on what causes this sometimes!"
					}
				}
			}
		}
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
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
	if(activityORbattery == false) schedule(timeToRun, activityHandler)
	if(activityORbattery == true) schedule(timeToRun, batteryHandler)
}

def batteryHandler(evt) {
	log.info "     * * * * * * * * Starting (B) ${app.label} * * * * * * * *     "
	if(batteryDevice) {
		state.myType = "Battery"
		state.mySensors = batteryDevice
		myBatteryHandler()
	}
	log.info "     * * * * * * * * End (B) ${app.label} * * * * * * * *     "
	def rightNow = new Date()
	state.batteryMap += "<br>Report generated: ${rightNow}<br>"
	if(sendPushMessage) pushNow()
}

def activityHandler(evt) {
	log.info "     * * * * * * * * Starting ${app.label} * * * * * * * *     "
	if(actuatorDevice) {
		state.myType = "Actuator"
		state.mySensors = actuatorDevice
		mySensorHandler()
	}
	if(sensorDevice) {
		state.myType = "Sensor"
		state.mySensors = sensorDevice
		mySensorHandler()
	}
	if(accelerationSensorDevice) {
	  	state.myType = "Acceleration"
		state.mySensors = accelerationSensorDevice
		mySensorHandler()
	}
	if(alarmDevice) {
		state.myType = "Alarm"
		state.mySensors = alarmDevice
		mySensorHandler()
	}
	if(batteryDevice) {
		state.myType = "Battery"
		state.mySensors = batteryDevice
		mySensorHandler()
	}
	if(carbonMonoxideDetectorDevice) {
	  	state.myType = "Carbon Monoxide Detector"
		state.mySensors = carbonMonoxideDetectorDevice
		mySensorHandler()
	}
	if(contactSensorDevice) {
		state.myType = "Contact Sensor"
		state.mySensors = contactSensorDevice
		mySensorHandler()
	}
	if(energyMeterDevice) {
		state.myType = "Energy Meter"
		state.mySensors = energyMeterDevice
		mySensorHandler()
	}
	if(illuminanceMeasurementDevice) {
		state.myType = "Illuminance Measurement"
		state.mySensors = illuminanceMeasurementDevice
		mySensorHandler()
	}
	if(lockDevice) {
		state.myType = "Lock"
		state.mySensors = lockDevice
		mySensorHandler()
	}
	if(motionSensorDevice) {
		state.myType = "Motion Sensor"
		state.mySensors = motionSensorDevice
		mySensorHandler()
	}
	if(powerMeterDevice) {
		state.myType = "Power Meter"
		state.mySensors = powerMeterDevice
		mySensorHandler()
	}
	if(presenceSensorDevice) {
		state.myType = "Presence Sensor"
		state.mySensors = presenceSensorDevice
		mySensorHandler()
	}
	if(pushableButtonDevice) {
		state.myType = "Pushable Button"
		state.mySensors = pushableButtonDevice
		mySensorHandler()
	}
	if(relativeHumidityMeasurementDevice) {
		state.myType = "Relative Humidity Measurement"
		state.mySensors = relativeHumidityMeasurementDevice
		mySensorHandler()
	}
	if(smokeDetectorDevice) {
		state.myType = "Smoke Detector"
		state.mySensors = smokeDetectorDevice
		mySensorHandler()
	}
	if(switchDevice) {
		state.myType = "Switch"
		state.mySensors = switchDevice
		mySensorHandler()
	}
	if(switchLevelDevice) {
		state.myType = "Switch Level"
		state.mySensors = switchLevelDevice
		mySensorHandler()
	}
	if(temperatureMeasurementDevice) {
		state.myType = "Temperature Measurement"
		state.mySensors = temperatureMeasurementDevice
		mySensorHandler()
	}
	if(valveDevice) {
		state.myType = "Valve"
		state.mySensors = valveDevice
		mySensorHandler()
	}
	if(voltageMeasurementDevice) {
		state.myType = "Voltage Measurement"
		state.mySensors = voltageMeasurementDevice
		mySensorHandler()
	}
	if(waterSensorDevice) {
		state.myType = "Water Sensor"
		state.mySensors = waterSensorDevice
		mySensorHandler()
	}
	log.info "     * * * * * * * * End ${app.label} * * * * * * * *     "
	def rightNow = new Date()
	state.timeSinceMap += "<br>Report generated: ${rightNow}<br>"
	if(sendPushMessage) pushNow()
}	

def myBatteryHandler() {
	log.info "     - - - - - Start (B) ${state.myType} - - - - -     "
	LOGDEBUG("In myBatteryHandler...")
	state.mySensors.each { device ->
		log.info "Working on... ${device}"
		def currentValue = device.currentValue("battery")
		if(currentValue == null) currentValue = 0
		LOGDEBUG("In myBatteryHandler...${device} - ${currentValue}")
		if(currentValue < batteryThreshold) {
			if(badORgood == false) {
				log.info "${state.myType} - mySensors: ${device} battery is ${currentValue} less than ${batteryThreshold} threshold."
				state.batteryMap += "${state.myType} - ${device} battery level is ${currentValue}<br>"
				state.batteryMapPhone += "${device} - ${currentValue} - "
			}
		} else {
			if(badORgood == true) {
				log.info "${state.myType} - ${device} battery is ${currentValue}, over threshold."
				state.batteryMap += "${state.myType} - ${device} battery level is ${currentValue}, over threshold.<br>"
				state.batteryMapPhone += "${device} - ${currentValue} - "
			}
		}
	}
	log.info "     - - - - - End (B)${state.myType} - - - - -     "
}

def mySensorHandler() {
	log.info "     - - - - - Start ${state.myType} - - - - -     "
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
				state.timeSinceMapPhone += "${device} - ${hour}h ${min}m - "
			}
		} else {
			if(badORgood == true) {
				log.info "${state.myType} - mySensors: ${device} last checked in ${hour}h ${min}m ago.<br>"
				state.timeSinceMap += "${state.myType} - ${device} last checked in ${hour}h ${min}m ago.<br>"
				state.timeSinceMapPhone += "${device} - ${hour}h ${min}m - "
			}
		}
	}
	log.info "     - - - - - End ${state.myType} - - - - -     "
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
		if(activityORbattery) {
        	runIn(1, batteryHandler)
		} else {
			runIn(1, activityHandler)
		}
    }
}

def pushNow(){
	LOGDEBUG("In pushNow...")
		if(activityORbattery) {
			LOGDEBUG("In pushNow...Sending message: ${state.timeSinceMapPhone}")
        	sendPushMessage.deviceNotification(state.timeSinceMapPhone)
		} else {
			LOGDEBUG("In pushNow...Sending message: ${state.batteryMapPhone}")
			sendPushMessage.deviceNotification(state.batteryMapPhone)
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

def display(){
	section{paragraph "<b>Device Watchdog</b><br>App Version: 1.0.0<br>@BPTWorld"}
	section(){input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false}
}
