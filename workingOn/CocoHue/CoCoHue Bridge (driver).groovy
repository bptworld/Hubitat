/**
 * =============================  CoCoHue Bridge (Driver) ===============================
 *
 *  Copyright 2019 Robert Morris
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * =======================================================================================
 *
 *  Last modified: 2019-12-01
 *
 *  Changelog:
 * 
 *  v1.0 - Initial Release
 *  v1.5 - Additional methods to support scenes and improved group behavior
 *
 */ 

//import groovy.json.JsonBuilder

metadata {
    definition (name: "CoCoHue Bridge", namespace: "RMoRobert", author: "Robert Morris", importUrl: "https://raw.githubusercontent.com/HubitatCommunity/CoCoHue/master/drivers/cocohue-bridge-driver.groovy") {
        capability "Refresh"
        attribute "status", "string"
    }
    
    preferences() {
        section("") {
            input(name: "enableDebug", type: "bool", title: "Enable debug logging", defaultValue: true)
            input(name: "enableDesc", type: "bool", title: "Enable descriptionText logging", defaultValue: true)
        }
    }   
}

def debugOff() {
    log.warn("Disabling debug logging")
    device.updateSetting("enableDebug", [value:"false", type:"bool"])
}

def installed(){
    log.debug "Installed..."
    initialize()
}

def updated(){
    log.debug "Updated..."
    initialize()
}

def initialize() {
    log.debug "Initializing"
    int disableTime = 1800
    log.debug "Debug logging will be automatically disabled in ${disableTime} seconds"
    if (enableDebug) runIn(disableTime, debugOff)    
}

// Probably won't happen but...
def parse(String description) {
    logDebug("Running parse for: '${description}'")
}

def refresh() {
    logDebug("Refresh...")
    def data = parent.getBridgeData()
    def lightParams = [
        uri: data.fullHost,
        path: "/api/${data.username}/lights",
        contentType: 'application/json',
        body: cmds
        ]
    def groupParams = [
        uri: data.fullHost,
        path: "/api/${data.username}/groups",
        contentType: 'application/json',
        body: cmds
        ]
    def scheduleParams = [
        uri: data.fullHost,
        path: "/api/${data.username}/schedules",
        contentType: 'application/json',
        body: cmds
        ]
    def sensorParams = [
        uri: data.fullHost,
        path: "/api/${data.username}/sensors",
        contentType: 'application/json',
        body: cmds
        ]
    def resourcelinkParams = [
        uri: data.fullHost,
        path: "/api/${data.username}/resourcelinks",
        contentType: 'application/json',
        body: cmds
        ]
    asynchttpGet("parseLightStates", lightParams)    
    asynchttpGet("parseGroupStates", groupParams)
    asynchttpGet("parseScheduleStates", scheduleParams)
    asynchttpGet("parseSensorStates", sensorParams)
    asynchttpGet("parseResourcelinkStates", resourcelinkParams)
}

def configure() {}

// ------------ BULBS ------------

/** Requests list of all bulbs/lights from Hue Bridge; updates
 *  allBulbs in state when finished. Intended to be called
 *  during bulb discovery in app.
 */
def getAllBulbs() {
    logDebug("Getting bulb list from Bridge...")
    //clearBulbsCache()
    def data = parent.getBridgeData()
    def params = [
        uri: data.fullHost,
        path: "/api/${data.username}/lights",
        contentType: "application/json",
        ]
    asynchttpGet("parseGetAllBulbsResponse", params)
}

private parseGetAllBulbsResponse(resp, data) {
    logDebug("Parsing in parseGetAllBulbsResponse") 
    def body = resp?.json
    def bulbs = [:]
    body?.each { key, val ->
        bulbs[key] = [name: val.name, type: val.type]
    }
    state.allBulbs = bulbs
}


/** Intended to be called from parent Bridge Child app to retrive previously
 *  requested list of bulbs
 */
def getAllBulbsCache() {
    return state.allBulbs 
}

/** Clears cache of bulb IDs/names/types; useful for parent app to call if trying to ensure
 * not working with old data
 */
def clearBulbsCache() {
    logDebug("Running clearBulbsCache...")
    state.allBulbs = [:]
}

/** Callback method that handles updating attributes on child light
 *  devices when Bridge refreshed
 */
def parseLightStates(resp, data) { 
    logDebug("Parsing light states from Bridge...")
    def lightStates
    try {
        lightStates = resp.json
    } catch (ex) {
        log.error("Error requesting light data: ${resp.errorMessage ?: ex}")
        if (device.currentValue("status") != "Offline") doSendEvent("status", "Offline")        
        return        
    }
    if (device.currentValue("status") != "Online")  {
        def ret = doSendEvent("status", "Online")
    }
    lightStates.each { id, val ->
        def device = parent.getChildDevice("${device.deviceNetworkId}/Light/${id}")
        if (device) {
            device.createEventsFromMap(val.state, true)
        }
    }
}

// ------------ GROUPS ------------

/** Requests list of all bulbs/lights from Hue Bridge; updates
 *  allBulbs in state when finished. Intended to be called
 *  during bulb discovery in app.
 */
def getAllGroups() {
    logDebug("Getting group list from Bridge...")
    //clearGroupsCache()
    def data = parent.getBridgeData()
    def params = [
        uri: data.fullHost,
        path: "/api/${data.username}/groups",
        contentType: "application/json",
        ]
    asynchttpGet("parseGetAllGroupsResponse", params)
}

private parseGetAllGroupsResponse(resp, data) {
    logDebug("Parsing in parseGetAllGroupsResponse") 
    def body = resp?.json
    def groups = [:]
    body?.each { key, val ->
        groups[key] = [name: val.name, type: val.type]
    }
    state.allGroups = groups
}


/** Intended to be called from parent Bridge Child app to retrive previously
 *  requested list of groups
 */
def getAllGroupsCache() {
    return state.allGroups
}

/** Clears cache of group IDs/names; useful for parent app to call if trying to ensure
 * not working with old data
 */
def clearGroupsCache() {
    logDebug("Running clearGroupsCache...")
    state.allGroups = [:]
}

/** Callback method that handles updating attributes on child group
 *  devices when Bridge refreshed
 */
def parseGroupStates(resp, data) {
    logDebug("Parsing group states from Bridge...")
    def groupStates
    try {
        groupStates = resp.json
    } catch (ex) {
        log.error("Error requesting light data: ${resp.errorMessage ?: ex}")
        if (device.currentValue("status") != "Offline") doSendEvent("status", "Offline")
        return        
    }
    if (device.currentValue("status") != "Online") doSendEvent("status", "Online")
    groupStates.each { id, val ->
        def device = parent.getChildDevice("${device.deviceNetworkId}/Group/${id}")
        if (device) {
            device.createEventsFromMap(val.action, true)
            device.createEventsFromMap(val.state, true)
            device.setMemberBulbIDs(val.lights)
        }
    }
}

// ------------ SCENES ------------

/** Requests list of all scenes from Hue Bridge; updates
 *  allScenes in state when finished. Intended to be called
 *  during bulb discovery in app.
 */
def getAllScenes() {
    logDebug("Getting scene list from Bridge...")
    getAllGroups() // so can get room names, etc.
    //clearScenesCache()
    def data = parent.getBridgeData()
    def params = [
        uri: data.fullHost,
        path: "/api/${data.username}/scenes",
        contentType: "application/json",
        ]
    asynchttpGet("parseGetAllScenesResponse", params)
}

private parseGetAllScenesResponse(resp, data) {
    logDebug("Parsing in parseGetAllScenesResponse") 
    def body = resp?.json
    def scenes = [:]
    body?.each { key, val ->
        scenes[key] = ["name": val.name]
        if (val.group) scenes[key] << ["group": val.group]
    }
    state.allScenes = scenes
}


/** Intended to be called from parent Bridge Child app to retrive previously
 *  requested list of scenes
 */
def getAllScenesCache() {
    return state.allScenes
}

/** Clears cache of scene IDs/names; useful for parent app to call if trying to ensure
 * not working with old data
 */
def clearScenesCache() {
    logDebug("Running clearScenesCache...")
    state.allScenes = [:]
}

// ********************* NEW ****************************

// ------------ SCHEDULES ------------

/** Requests list of all Schedules from Hue Bridge; updates
 *  allSchedules in state when finished. Intended to be called
 *  during Schedule discovery in app.
 */
def getAllSchedules() {
    logDebug("Getting Schedule list from Bridge...")
    //clearSchedulesCache()
    def data = parent.getBridgeData()
    def params = [
        uri: data.fullHost,
        path: "/api/${data.username}/schedules",
        contentType: "application/json",
        ]
    asynchttpGet("parseGetAllSchedulesResponse", params)
}

private parseGetAllSchedulesResponse(resp, data) {
    logDebug("Parsing in parseGetAllSchedulesResponse") 
    def body = resp?.json
    def schedules = [:]
    body?.each { key, val ->
        schedules[key] = [name: val.name, type: val.type]
    }
    state.allSchedules = schedules
    log.info "allSchedules: ${state.allSchedules}"
}


/** Intended to be called from parent Bridge Child app to retrive previously
 *  requested list of Schedules
 */
def getAllSchedulesCache() {
    return state.allSchedules 
}

/** Clears cache of Schedule IDs/names/types; useful for parent app to call if trying to ensure
 * not working with old data
 */
def clearSchedulesCache() {
    logDebug("Running clearSchedulesCache...")
    state.allSchedules = [:]
}

/** Callback method that handles updating attributes on child Schedule
 *  devices when Bridge refreshed
 */
def parseScheduleStates(resp, data) { 
    logDebug("Parsing Schedule states from Bridge...")
    def scheduleStates
    try {
        scheduleStates = resp.json
    } catch (ex) {
        log.error("Error requesting Schedule data: ${resp.errorMessage ?: ex}")
        if (device.currentValue("status") != "Offline") doSendEvent("status", "Offline")        
        return        
    }
    if (device.currentValue("status") != "Online")  {
        def ret = doSendEvent("status", "Online")
    }
    scheduleStates.each { id, val ->
        def device = parent.getChildDevice("${device.deviceNetworkId}/Schedule/${id}")
        if (device) {
            device.createEventsFromMap(val.state, true)
        }
    }
}

// ------------ SENSORS ------------

/** Requests list of all sensors from Hue Bridge; updates
 *  allSensors in state when finished. Intended to be called
 *  during Sensor discovery in app.
 */
def getAllSensors() {
    logDebug("Getting Sensor list from Bridge...")
    //clearSensorsCache()
    def data = parent.getBridgeData()
    def params = [
        uri: data.fullHost,
        path: "/api/${data.username}/sensors",
        contentType: "application/json",
        ]
    asynchttpGet("parseGetAllSensorsResponse", params)
}

private parseGetAllSensorsResponse(resp, data) {
    logDebug("Parsing in parseGetAllSensorsResponse") 
    def body = resp?.json
    def sensors = [:]
    body?.each { key, val ->
        sensors[key] = [name: val.name, type: val.type]
    }
    state.allSensors = sensors
    log.info "allSensors: ${state.allSensors}"
}


/** Intended to be called from parent Bridge Child app to retrive previously
 *  requested list of Sensors
 */
def getAllSensorsCache() {
    return state.allSensors 
}

/** Clears cache of Sensor IDs/names/types; useful for parent app to call if trying to ensure
 * not working with old data
 */
def clearSensorsCache() {
    logDebug("Running clearSensorsCache...")
    state.allSensors = [:]
}

/** Callback method that handles updating attributes on child Sensor
 *  devices when Bridge refreshed
 */
def parseSensorStates(resp, data) { 
    logDebug("Parsing Sensor states from Bridge...")
    def sensorStates
    try {
        sensorStates = resp.json
    } catch (ex) {
        log.error("Error requesting Sensor data: ${resp.errorMessage ?: ex}")
        if (device.currentValue("status") != "Offline") doSendEvent("status", "Offline")        
        return        
    }
    if (device.currentValue("status") != "Online")  {
        def ret = doSendEvent("status", "Online")
    }
    sensorStates.each { id, val ->
        def device = parent.getChildDevice("${device.deviceNetworkId}/Sensor/${id}")
        if (device) {
            device.createEventsFromMap(val.state, true)
        }
    }
}

// ------------ Resourcelink ------------

/** Requests list of all Resourcelinks from Hue Bridge; updates
 *  allResourcelinks in state when finished. Intended to be called
 *  during Sensor discovery in app.
 */
def getAllResourcelinks() {
    logDebug("Getting Resourcelink list from Bridge...")
    //clearResourcelinksCache()
    def data = parent.getBridgeData()
    def params = [
        uri: data.fullHost,
        path: "/api/${data.username}/resourcelinks",
        contentType: "application/json",
        ]
    asynchttpGet("parseGetAllResourcelinksResponse", params)
}

private parseGetAllResourcelinksResponse(resp, data) {
    logDebug("Parsing in parseGetAllResourcelinksResponse") 
    def body = resp?.json
    def resourcelinks = [:]
    body?.each { key, val ->
        resourcelinks[key] = [name: val.name, type: val.type]
    }
    state.allResourcelinks = resourcelinks
    log.info "allResourcelinks: ${state.allResourcelinks}"
}


/** Intended to be called from parent Bridge Child app to retrive previously
 *  requested list of Sensors
 */
def getAllResourcelinksCache() {
    return state.allResourcelinks 
}

/** Clears cache of Resourcelink IDs/names/types; useful for parent app to call if trying to ensure
 * not working with old data
 */
def clearResourcelinksCache() {
    logDebug("Running clearResourcelinksCache...")
    state.allResourcelinks = [:]
}

/** Callback method that handles updating attributes on child Resourcelink
 *  devices when Bridge refreshed
 */
def parseResourcelinkStates(resp, data) { 
    logDebug("Parsing Resourcelink states from Bridge...")
    def resourcelinkStates
    try {
        resourcelinkStates = resp.json
    } catch (ex) {
        log.error("Error requesting Resourcelink data: ${resp.errorMessage ?: ex}")
        if (device.currentValue("status") != "Offline") doSendEvent("status", "Offline")        
        return        
    }
    if (device.currentValue("status") != "Online")  {
        def ret = doSendEvent("status", "Online")
    }
    resourcelinkStates.each { id, val ->
        def device = parent.getChildDevice("${device.deviceNetworkId}/Resourcelink/${id}")
        if (device) {
            device.createEventsFromMap(val.state, true)
        }
    }
}

// ******************************** END NEW **************************************

def doSendEvent(eventName, eventValue) {
    logDebug("Creating event for $eventName...")
    def descriptionText = "${device.displayName} ${eventName} is ${eventValue}"
    logDesc(descriptionText)
    def event = sendEvent(name: eventName, value: eventValue, descriptionText: descriptionText) 
    return event
}

def logDebug(str) {
    if (settings.enableDebug) log.debug(str)
}

def logDesc(str) {
    if (settings.enableDesc) log.info(str)
}
