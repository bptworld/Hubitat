def getDriverDate() { return "2024-03-21" + " (Basic)" }	// **** DATE OF THE DEVICE DRIVER
/*
*  Replacement stripped down driver for the Inovelli VZM36 Zigbee Canopy Light. Features on/off, setLevel and toggle.
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
*
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
* 2024-03-21 (BPTWorld) Stripped down BASIC Light Driver
*
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
    definition (name: "Inovelli VZM36 Zigbee Canopy Light BASIC", namespace: "InovelliUSA", author: "M.Amber/E.Maycock", importUrl:"") {
		capability "Actuator"	//device can "do" something (has commands)
        capability "Sensor"		//device can "report" something (has attributes)

        capability "Configuration"
		capability "Initialize"
        capability "Switch"
        capability "SwitchLevel"
        
        command "configure",           [[name:"Configure"]]		
        command "initialize",		   [[name:"Clear State Variables"]]
        command "toggle"
        
        attribute "level", "number"
        attribute "switch", "text"

		fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0702,0B04,0B05,FC57,FC31", outClusters:"0003,0019",           model:"VZM31-SN", manufacturer:"Inovelli"
		fingerprint profileId:"0104", endpointId:"02", inClusters:"0000,0003",                                              outClusters:"0003,0019,0006,0008", model:"VZM31-SN", manufacturer:"Inovelli"
//      fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0702,0B04,FC31",           outClusters:"0003,0019",           model:"VZM31-SN", manufacturer:"Inovelli"
    }

    preferences {
        input name: "about", type: "paragraph", element: "paragraph", title: "<b>A BPTWorld Driver</b>", description: "BASIC Driver for use with the Inovelli VZM36 Zigbee Canopy Light"
        input name: "logEnable",         type: "bool",   title: "Enable Debug Logging",  defaultValue: false, description: "Detailed diagnostic data"
        input name: "disableDebugLogging", type: "number", title: "Disable Debug Logging after this number of minutes", description: "(0=Do not disable)", defaultValue: 5
    }
}

def userSettableParams() {   //controls which options are available depending on whether the device is configured as a switch or a dimmer.
    if (parameter258 == "1") return [258,22,52,  3,  7,  10,11,12,   15,17,23,24,25,50,95,97]  //on/off mode
    else                     return [258,22,52,1,3,5,7,9,10,11,12,14,15,17,23,24,25,50,95,97]  //dimmer mode
}

def debugLogsOff() {
    log.warn "${device.displayName} " + "Disabling Debug logging after timeout"
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def configure(option) {    //THIS GETS CALLED AUTOMATICALLY WHEN NEW DEVICE IS ADDED OR WHEN CONFIGURE BUTTON SELECTED ON DEVICE PAGE
    option = (option==null||option==" ")?"":option
    if (logEnable) log.info "${device.displayName} configure($option)"
    state.lastCommandSent =                        "configure($option)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
	if (logEnable) log.info "${device.displayName} re-establish lifeline bindings to hub"
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}"] //On_Off Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}"] //On_Off Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}"] //Level Control Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}"] //Level Control Cluster ep2
	if (state.model?.substring(0,5)!="VZM35") {  //Fan does not support power/energy reports
		cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0702 {${device.zigbeeId}} {}"] //Simple Metering - to get energy reports
		cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0B04 {${device.zigbeeId}} {}"] //Electrical Measurement - to get power reports
	}
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0xFC31 {${device.zigbeeId}} {}"] //Private Cluster
	cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0xFC31 {${device.zigbeeId}} {}"] //Private Cluster ep2

    if (logEnable) log.debug "${device.displayName} configure $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
}

def initialize() {    //CALLED DURING HUB BOOTUP IF "INITIALIZE" CAPABILITY IS DECLARED IN METADATA SECTION
    log.info "${device.displayName} initialize()"
    //save the group IDs before clearing all the state variables and reset them after
    state.clear()
    state.lastCommandSent = "initialize()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = parent.getDataValue('model') + "-Light"
    device.removeSetting("parameter23level")
    device.removeSetting("parameter95custom")
    device.removeSetting("parameter96custom")
}

def installed() {    //THIS IS CALLED WHEN A DEVICE IS INSTALLED
    log.info "${device.displayName} installed()"
    state.lastCommandSent =        "installed()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = parent.getDataValue('model') + "-Light"
    log.info "${device.displayName} Driver Date $state.driverDate"
    log.info "${device.displayName} Model=$state.model"
    return
}

def off() {
	if (logEnable) log.info "${device.displayName} off()"
    state.lastCommandSent =                        "off()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    //cmds += zigbee.off()
    cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 6 0 {}"
    if (logEnable) log.debug "${device.displayName} off $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"switch", value: "off")
}

def on() {
    if (logEnable) log.info "${device.displayName} on()"
    state.lastCommandSent =                        "on()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 6 1 {}"
    if (logEnable) log.debug "${device.displayName} on $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"switch", value: "on")
}

def nowFormatted() {
    if(location.timeZone) return new Date().format("yyyy-MMM-dd h:mm:ss a", location.timeZone)
    else                  return new Date().format("yyyy MMM dd EEE h:mm:ss a")
}

def setLevel(level, duration=0xFFFF) {
	level    = level?.toInteger()
	duration = duration?.toInteger()
	if (duration==null) duration=0xFFFF
    if (logEnable) log.info "${device.displayName} setLevel($level" + (duration==0xFFFF?")":", ${duration}s)")
    state.lastCommandSent =                        "setLevel($level" + (duration==0xFFFF?")":", ${duration}s)")
    state.lastCommandTime = nowFormatted()
    if (duration!=0xFFFF) duration = duration*10  //firmware duration in 10ths
	durationHex = zigbee.convertToHexString(duration,4)
	durationHexReverse = durationHex.substring(2,4)+durationHex.substring(0,2)
    def cmds = []
	cmds += "he cmd 0x${parent.deviceNetworkId} 0x${device.deviceNetworkId?.substring(device.deviceNetworkId.length()-2)?:"00"} 0x0008 0x04 {${zigbee.convertToHexString(convertPercentToByte(level),2)} $durationHexReverse}"
    if (logEnable) log.debug "${device.displayName} setLevel $cmds"
    sendHubCommand(new HubMultiAction(delayBetween(cmds, shortDelay), Protocol.ZIGBEE))
    sendEvent(name:"level", value: "${level}")
    sendEvent(name:"switch", value: "on")
}

def toggle() {	
    def toggleDirection = device.currentValue("switch")=="off"?"off->on":"on->off"
    if (logEnable) log.info "${device.displayName} toggle(${toggleDirection})"
    state.lastCommandSent =                        "toggle(${toggleDirection})"
    state.lastCommandTime = nowFormatted()
    if(device.currentValue("switch")=="off") {
        on()
    } else {
        off()
    }
}

def updated(option) { // called when "Save Preferences" is requested
    // nothing   
}

def parse(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    if (logEnable) {
		log.trace "${device.displayName} parse($descMap)"
		try {
			if (zigbee.getEvent(description)!=[:]) log.debug "${device.displayName} zigbee.getEvent ${zigbee.getEvent(description)}"
		} catch (e) {
			if (logEnable) log.debug "${device.displayName} "+"There was an error while calling zigbee.getEvent: $description"
		}
	}
}

def convertPercentToByte(int value=0) {                  //convert a 0-100 range where 100%=254.  255 is reserved for special meaning.
    value = value==null?0:value                          //default to 0 if null
    value = Math.min(Math.max(value.toInteger(),0),101)  //make sure input percent value is in the 0-101 range
    value = Math.floor(value/100*255)                    //convert to 0-255 where 100%=254 and 101 becomes 255 for special meaning
    value = value==255?254:value                         //this ensures that 100% rounds down to byte value 254
    value = value>255?255:value                          //this ensures that 101% rounds down to byte value 255
    return value
}
