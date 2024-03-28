def getDriverDate() { return "2024-03-27" + " (Basic)" }	// **** DATE OF THE DEVICE DRIVER
/*
*  Replacement stripped down driver for the Inovelli VZM36 Zigbee Canopy Fan. Features on/off, setSpeed, cycleSpeedUp/cycleSpeedDown and Breeze.
*  
*  Remember...I am not a programmer, everything I do takes a lot of time and research!
*  Donations are never necessary but always appreciated. Donations to support development efforts are accepted via: 
*
*  Paypal at: https://paypal.me/bptworld
*  https://github.com/bptworld/Hubitat
*
* ------------------------ Original Header ------------------------
* Author: Eric Maycock (erocm123)
* Contributor: Mark Amber (marka75160)
* Platform: Hubitat
*
* Copyright 2023 Eric Maycock / Inovelli

* Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
* in compliance with the License. You may obtain a copy of the License at:
*
*	http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
* on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
* for the specific language governing permissions and limitations under the License.
*
* ------------------------------
*           CHANGE LOG          
* ------------------------------
*
* 2024-03-21 (BPTWorld) Stripped down BASIC Fan Driver
*/

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import groovy.transform.Field
import hubitat.device.HubAction
import hubitat.device.HubMultiAction
import hubitat.device.Protocol
import hubitat.helper.HexUtils
import java.security.MessageDigest

metadata {
    definition (name: "Inovelli VZM36 Zigbee Canopy Fan BASIC", namespace: "InovelliUSA", author: "M.Amber/E.Maycock", importUrl:"") {
		capability "Actuator"	//device can "do" something (has commands)
        capability "Sensor"		//device can "report" something (has attributes)

        capability "Configuration" 
        capability "FanControl"
		capability "Initialize"
        capability "Switch"

        command "configure",           [[name:"Configure"]]		
        command "initialize",		   [[name:"Clear State Variables"]]
        command "setSpeed",            [[name:"FanSpeed*", type:"ENUM",   constraints:["off","low","medium-low","medium","medium-high","high","up","down"]]]
        command "cycleSpeed",        [[name:"Same as Cycle Speed UP"]]
        command "cycleSpeedUp",        [[name:"Cycle Speed UP"]]
        command "cycleSpeedDown",      [[name:"Cycle Speed DOWN"]]
        command "breeze",              [[name:"Random speeds creating a nice Breeze"]]

		attribute "speed", "String"
        attribute "switch", "String"
    }

    preferences {
        input name: "about", type: "paragraph", element: "paragraph", title: "<b>A BPTWorld Driver</b>", description: "BASIC Driver for use with the Inovelli VZM36 Zigbee Canopy Fan"
        input name: "logEnable", type: "bool", title: "Enable Debug Logging", defaultValue: true, description: ""
        input name: "disableLogLogging", type: "number", title: "Disable Debug Logging after this number of minutes", description: "(0=Do not disable)", defaultValue: 20
    }
}

def initialize() {
    log.info "${device.displayName} initialize()"
    //save the group IDs before clearing all the state variables and reset them after
    state.clear()
    state.lastCommandSent = "initialize()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = parent.getDataValue('model') + "-Fan"
    device.removeSetting("parameter23level")
    device.removeSetting("parameter95custom")
    device.removeSetting("parameter96custom")
}

def installed() {
    log.info "${device.displayName} installed()"
    state.lastCommandSent = "installed()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = parent.getDataValue('model') + "-Fan"
    log.info "${device.displayName} Driver Date $state.driverDate"
    log.info "${device.displayName} Model=$state.model"
    return
}

def updated(option) {
    option = (option==null||option==" ")?"":option
    if (logEnable) log.info "${device.displayName} updated(${option})"
    state.lastCommandSent = "updated(${option})"
    state.lastCommandTime = nowFormatted()
    def nothingChanged = true
    int defaultValue
    int newValue
	configParams.each {	//loop through all parameters
		int i = it.value.number.toInteger()
		newValue = calculateParameter(i).toInteger()
		defaultValue=getDefaultValue(i).toInteger()
		if ([9,10,13,14,15,24,55,56].contains(i)) defaultValue=convertPercentToByte(defaultValue) //convert percent values back to byte values
		if ((i==95 && parameter95custom!=null)||(i==96 && parameter96custom!=null)) {                                         //IF   a custom hue value is set
			if ((Math.round(settings?."parameter${i}custom"?.toInteger()/360*255)==settings?."parameter${i}"?.toInteger())) { //AND  custom setting is same as normal setting
				device.removeSetting("parameter${i}custom")                                                                   //THEN clear custom hue and use normal color 
				if (logEnable||logEnable) log.info "${device.displayName} Cleared Custom Hue setting since it equals standard color setting"
			}
		}
    }

    if (nothingChanged && (logEnable)) log.info "${device.displayName} No DEVICE settings were changed"
	log.debug "${device.displayName} Debug logging " + (logEnable?"Enabled":"Disabled")
    if (logEnable && disableLogLogging) {
		log.info "${device.displayName} Info Logging will be disabled in $disableLogLogging minutes"
		runIn(disableLogLogging*60,infoLogsOff)
	}
}

def debugLogsOff() {
    log.warn "${device.displayName} " + "Disabling Debug logging after timeout"
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS ADDED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (logEnable) log.info "${device.displayName} configure($option)"
    state.lastCommandSent = "configure($option)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
	if (logEnable) log.info "${device.displayName} re-establish lifeline bindings to hub"
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}"] //On_Off Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}"] //On_Off Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}"] //Level Control Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}"] //Level Control Cluster ep2
    if (logEnable) log.debug "${device.displayName} configure $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
}

def cycleSpeed() {
    cycleSpeedUp()
}

def cycleSpeedUp() {
    def cmds =[]
    def currentSpeed = device.currentValue("speed")
    if (device.currentValue("switch")=="off") currentSpeed = "off"
    def newLevel = 0
    def newSpeed = ""
    if      (currentSpeed=="off" ) {newLevel=33;  newSpeed="low" }
    else if (currentSpeed=="low") {newLevel=66;  newSpeed="medium"}
    else if (currentSpeed=="medium") {newLevel=100; newSpeed="high"}
    else                       {newLevel=0;   newSpeed="off"}
    if (logEnable) log.info "${device.displayName} cycleSpeedUp(${device.currentValue("speed")?:off}->${newSpeed})"
    state.lastCommandSent =                        "cycleSpeedUp(${device.currentValue("speed")?:off}->${newSpeed})"
    state.lastCommandTime = nowFormatted()
    cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(newLevel),2)} FFFF}"
    if (logEnable) log.debug "${device.displayName} cycleSpeedUp $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"speed", value: "${newSpeed}")
    if(newSpeed != "off") {
        sendEvent(name:"switch", value: "on")
    } else {
        sendEvent(name:"switch", value: "off")
    }
}

def cycleSpeedDown() {
    def cmds =[]
    def currentSpeed = device.currentValue("speed")
    if (device.currentValue("switch")=="off") currentSpeed = "off"
    def newLevel = 0
    def newSpeed = ""
    if      (currentSpeed=="off" ) {newLevel=100;  newSpeed="high" }
    else if (currentSpeed=="high") {newLevel=66;  newSpeed="medium"}
    else if (currentSpeed=="medium") {newLevel=33; newSpeed="low"}
    else                       {newLevel=0;   newSpeed="off"}
    if (logEnable) log.info "${device.displayName} cycleSpeedDown(${device.currentValue("speed")?:off}->${newSpeed})"
    state.lastCommandSent =                        "cycleSpeedDown(${device.currentValue("speed")?:off}->${newSpeed})"
    state.lastCommandTime = nowFormatted()
    cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(newLevel),2)} FFFF}"
    if (logEnable) log.debug "${device.displayName} cycleSpeedDown $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"speed", value: "${newSpeed}")
    if(newSpeed != "off") {
        sendEvent(name:"switch", value: "on")
    } else {
        sendEvent(name:"switch", value: "off")
    }
}

def breeze() {
    on()
    pauseExecution(500)
    breezy()
}

def breezy() {
    def currentSwitch = device.currentValue("switch")
    if(currentSwitch == "on") {
        currentSpeed = device.currentValue("speed")        
        speeds = ["low","medium","high"]
        def randomKey = new Random().nextInt(3)
        rSpeed = speeds[randomKey]
        if(logEnable) log.debug "In breezy - currentSpeed: ${currentSpeed} -VS- rSpeed: ${rSpeed}"
        if(currentSpeed == rSpeed) {
            if(logEnable) log.debug "In breezy - currentSpeed: ${currentSpeed} == rSpeed: ${rSpeed} - Trying again!"
            runIn(1, breezy)
        } else {
            setSpeed(rSpeed)
            int randomNum = 20 + (int)(Math.random() * (60 - 20 + 1))
            if(logEnable) log.debug "In breezy - Random seconds to stay at this speed - randomNum: ${randomNum}"
            runIn(randomNum, breezy)
        }
    }
}

def off() {
	if (logEnable) log.info "${device.displayName} off()"
    unschedule(breezy)
    state.lastCommandSent = "off()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 6 0 {}"
    if (logEnable) log.debug "${device.displayName} off $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"switch", value: "off")
}

def on() {
    if (logEnable) log.info "${device.displayName} on()"
    state.lastCommandSent = "on()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 6 1 {}"
    if (logEnable) log.debug "${device.displayName} on $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    if(device.currentValue("speed")=="off") {
       setSpeed("low")
    } else {
        sendEvent(name:"switch", value: "on")
    }
}

def parse(String description) {
    //
}

def setSpeed(value) {
    if (logEnable) log.info "${device.displayName} setSpeed(${value})"
    state.lastCommandSent = "setSpeed(${value})"
    state.lastCommandTime = nowFormatted()
	def currentLevel = device.currentValue("level")==null?0:device.currentValue("level").toInteger()
	if (device.currentValue("switch")=="off") currentLevel = 0
	boolean smartMode = device.currentValue("smartFan")=="Enabled"
	def newLevel = 0
    def cmds = []
    switch (value) {
        case "off":
            cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 6 0 {}"
            break
        case "low": 
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(smartMode?20:33),2)} 0xFFFF}"
            break
        case "medium-low":             //placeholder since Hubitat natively supports 5-speed fans
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(smartMode?40:33),2)} 0xFFFF}"
            break
        case "medium": 
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(smartMode?60:66),2)} 0xFFFF}"
            break
        case "medium-high":            //placeholder since Hubitat natively supports 5-speed fans
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(smartMode?80:66),2)} 0xFFFF}"
            break
        case "high": 
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(100),2)} 0xFFFF}"
            break
        case "on":
            cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 6 1 {}"
            break
		case "up":
			if      (currentLevel<=0 )  {newLevel=20}
			else if (currentLevel<=20)  {newLevel=(smartMode?40:60)}
			else if (currentLevel<=40)  {newLevel=60}
			else if (currentLevel<=60)  {newLevel=(smartMode?80:100)}
			else if (currentLevel<=100) {newLevel=100}
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(newLevel),2)} 0xFFFF}"
			break
		case "down":
			if      (currentLevel>80) {newLevel=(smartMode?80:60)}
			else if (currentLevel>60) {newLevel=60}
			else if (currentLevel>40) {newLevel=(smartMode?40:20)}
			else if (currentLevel>20) {newLevel=20}
			else if (currentLevel>0)  {newLevel=currentLevel}
	        cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(newLevel),2)} 0xFFFF}"
			break
    }
    if (logEnable) log.debug "${device.displayName} setSpeed $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"speed", value: "${value}")
    if(value == "off") {
        sendEvent(name:"switch", value: "off")
    } else {
        sendEvent(name:"switch", value: "on")
    }
}

def toggle() {	
    def toggleDirection = device.currentValue("switch")=="off"?"off->on":"on->off"
    if (logEnable) log.info "${device.displayName} toggle(${toggleDirection})"
    state.lastCommandSent = "toggle(${toggleDirection})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    if(device.currentValue("switch")=="off") {
        on()
    } else {
        off()
    }
}

def getDefaultValue(paramNum=0) {
    paramValue=configParams["parameter${paramNum.toString()?.padLeft(3,"0")}"]?.default?.toInteger()
    return paramValue?:0
}

def nowFormatted() {
    if(location.timeZone) return new Date().format("yyyy-MMM-dd h:mm:ss a", location.timeZone)
    else                  return new Date().format("yyyy MMM dd EEE h:mm:ss a")
}

def convertPercentToByte(int value=0) {                  //convert a 0-100 range where 100%=254.  255 is reserved for special meaning.
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),101)  //make sure input percent value is in the 0-101 range
    value = Math.floor(value/100*255)                    //convert to 0-255 where 100%=254 and 101 becomes 255 for special meaning
    value = value==255?254:value                         //this ensures that 100% rounds down to byte value 254
    value = value>255?255:value                          //this ensures that 101% rounds down to byte value 255
    return value
}
