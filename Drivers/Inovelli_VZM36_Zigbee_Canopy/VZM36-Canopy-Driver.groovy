def getDriverDate() { return "2024-03-27" + " (Basic)" }	// **** DATE OF THE DEVICE DRIVER
/*
*  Replacement stripped down driver for the Inovelli VZM36 Zigbee Canopy.
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
* 2024-03-21 (BPTWorld) Stripped down BASIC Driver
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
    definition (name: "Inovelli VZM36 Zigbee Canopy BASIC", namespace: "InovelliUSA", author: "M.Amber/E.Maycock", filename: "") {
		capability "Actuator"	//device can "do" something (has commands)
        capability "Sensor"		//device can "report" something (has attributes)

        capability "Configuration"
		capability "Initialize"

		attribute "internalTemp", "String"		//Internal Temperature in Celsius	(read-only P32)
		attribute "overHeat", "String"			//Overheat Indicator				(read-only P33)
		attribute "powerSource", "String"		//Neutral/non-Neutral				(read-only P21)


        command "getTemperature",		[[name:"Get the switch internal operating temperature"]]		
        command "initialize",		   [[name:"create child devices, refresh current states"]]
        command "updateFirmware",	   [[name:"Firmware in this channel may be \"beta\" quality"]]

		fingerprint profileId:"0104", endpointId:"01", inClusters:"0000,0003,0004,0005,0006,0008,0B05,1000,FC31,FC57", outClusters:"0019", model:"VZM36", manufacturer:"Inovelli"
    }

    preferences {
        input name: "about", type: "paragraph", element: "paragraph", title: "<b>A BPTWorld Driver</b>", description: "BASIC Driver for use with the Inovelli VZM36 Zigbee Canopy"
        input name: "logEnable",          type: "bool",   title: "Enable Debug Logging",   defaultValue: true,  description: ""
        input name: "disableLogging",  type: "number", title: "Disable Debug Logging after this number of minutes",  description: "(0=Do not disable, default=20)", defaultValue: 20
    }
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
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0000 {${device.zigbeeId}} {}"] //Basic Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0000 {${device.zigbeeId}} {}"] //Basic Cluster ep2
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0003 {${device.zigbeeId}} {}"] //Identify Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0003 {${device.zigbeeId}} {}"] //Identify Cluster ep2
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0004 {${device.zigbeeId}} {}"] //Group Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0004 {${device.zigbeeId}} {}"] //Group Cluster ep2
    cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0005 {${device.zigbeeId}} {}"] //Scenes Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0005 {${device.zigbeeId}} {}"] //Scenes Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0006 {${device.zigbeeId}} {}"] //On_Off Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0006 {${device.zigbeeId}} {}"] //On_Off Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0008 {${device.zigbeeId}} {}"] //Level Control Cluster
    cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0x0008 {${device.zigbeeId}} {}"] //Level Control Cluster ep2
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x0019 {${device.zigbeeId}} {}"] //OTA Upgrade Cluster

	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x8021 {${device.zigbeeId}} {}"] //Binding Cluster 
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0x8022 {${device.zigbeeId}} {}"] //UnBinding Cluster
	cmds += ["zdo bind ${device.deviceNetworkId} 0x01 0x01 0xFC31 {${device.zigbeeId}} {}"] //Private Cluster
	cmds += ["zdo bind ${device.deviceNetworkId} 0x02 0x01 0xFC31 {${device.zigbeeId}} {}"] //Private Cluster ep2
    
    // Delete obsolete children
    getChildDevices().each {child->
        if (!child.deviceNetworkId.startsWith(device.id) || child.deviceNetworkId == "${device.id}-00") {
            log.info "Deleting ${child.deviceNetworkId}"
  		    deleteChildDevice(child.deviceNetworkId)
        }
    }
	// Create Child Devices
    for (i in 1..2) {
        def childId = "${device.id}-0${i}"
        def existingChild = getChildDevices()?.find { it.deviceNetworkId == childId}
        if (existingChild) {
            log.info "${device.displayName} Child device ${childId} already exists (${existingChild})"
        } else {
			log.info "Creating device ${childId}"
            if(i == 1) {
				addChildDevice("InovelliUSA","Inovelli VZM36 Zigbee Canopy Light BASIC",childId,[isComponent:true,name:"Canopy Light EP0${i}",label: "${device.displayName} Light"])
            } else { if(i == 2) {
				addChildDevice("InovelliUSA","Inovelli VZM36 Zigbee Canopy Fan BASIC",  childId,[isComponent:true,name:"Canopy Fan EP0${i}",  label: "${device.displayName} Fan"])
				}
            }
        }
    }
    if (logEnable) log.debug "${device.displayName} configure $cmds"
    return delayBetween(cmds, longDelay)
}

private String getChildId(childDevice) {
    return childDevice.deviceNetworkId?.substring(childDevice.deviceNetworkId.length() - 2)?:"00"
}

def getRssiLQI(){ 
    if (logEnable) log.info "${device.displayName} getRssiLQI()"
    state.lastCommandSent =                        "getRssiLQI()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.readAttribute(0x0b05, 0x011c, [destEndpoint: 0x01], 50)    //CLUSTER_BASIC Mfg
    cmds += zigbee.readAttribute(0x0b05, 0x011d, [destEndpoint: 0x01], 50)
    return cmds
}

def getTemperature() {
    if (logEnable) log.info "${device.displayName} getTemperature()"
    state.lastCommandSent =                        "getTemperature()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
     cmds += zigbee.readAttribute(0xfc31, 0x0021, ["mfgCode": "0x122f"], 100)
     cmds += zigbee.readAttribute(0xfc31, 0x0020, ["mfgCode": "0x122f"], 100)
    return cmds
}

def initialize() {    //CALLED DURING HUB BOOTUP IF "INITIALIZE" CAPABILITY IS DECLARED IN METADATA SECTION
    log.info "${device.displayName} initialize()"
    //save the group IDs before clearing all the state variables and reset them after
    state.clear()
	log.info state
    state.lastCommandSent = "initialize()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = device.getDataValue('model')
    device.removeSetting("parameter23level")
    device.removeSetting("parameter95custom")
    device.removeSetting("parameter96custom")
    if (logEnable) log.debug "${device.displayName} initialize $cmds"
    return
}

def installed() {    //THIS IS CALLED WHEN A DEVICE IS INSTALLED
    log.info "${device.displayName} installed()"
    state.lastCommandSent =        "installed()"
    state.lastCommandTime = nowFormatted()
    state.driverDate = getDriverDate()
	state.model = device.getDataValue('model')
    log.info "${device.displayName} Driver Date $state.driverDate"
    log.info "${device.displayName} Model=$state.model"
    return
}

def nowFormatted() {
    if(location.timeZone) return new Date().format("yyyy-MMM-dd h:mm:ss a", location.timeZone)
    else                  return new Date().format("yyyy MMM dd EEE h:mm:ss a")
}

def off(childDevice) {
    if (logEnable) log.info "${device.displayName} off($childDevice)"
    state.lastCommandSent =                        "off($childDevice)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x0 {}"
    if (logEnable) log.debug "${device.displayName} off $cmds"
	return cmds
}

def on(childDevice) {
    if (logEnable) log.info "${device.displayName} on($childDevice)"
    state.lastCommandSent =                        "on($childDevice)"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += "he cmd 0x${device.deviceNetworkId} 0xFF 0x0006 0x1 {}"
    if (logEnable) log.debug "${device.displayName} on $cmds"
	return cmds
}

def parse(String description) {
    Map descMap = zigbee.parseDescriptionAsMap(description)
    if (logEnable) log.trace "${device.displayName} parse($descMap)"
}

def updated(option) { // called when "Save Preferences" is requested
    option = (option==null||option==" ")?"":option
    if (logEnable) log.info "${device.displayName} updated(${option})" + (traceEnable||logEnable)?" $settings":""
    state.lastCommandSent =                        "updated(${option})"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    def nothingChanged = true
    int defaultValue
    int newValue

    if (nothingChanged && (logEnable||logEnable)) log.info "${device.displayName} No DEVICE settings were changed"
	log.debug "${device.displayName} Debug logging " + (logEnable?"Enabled":"Disabled")

    if (logEnable && disableDebugLogging) {
		log.debug "${device.displayName} Debug Logging will be disabled in $disableDebugLogging minutes"
		runIn(disableDebugLogging*60,debugLogsOff) 
	}
    return cmds
}

List updateFirmware() {
    if (logEnable) log.info "${device.displayName} updateFirmware(switch's fwDate: ${state.fwDate}, switch's fwVersion: ${state.fwVersion})"
    state.lastCommandSent =                        "updateFirmware()"
    state.lastCommandTime = nowFormatted()
    def cmds = []
    cmds += zigbee.updateFirmware()
    if (logEnable) log.debug "${device.displayName} updateFirmware $cmds"
    return cmds
}
