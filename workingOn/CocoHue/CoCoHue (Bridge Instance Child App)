/**
 * ==========================  CoCoHue (Bridge Instance Child App) ==========================
 *
 *  DESCRIPTION:
 *  Community-developed Hue Bridge integration app for Hubitat, including support for lights,
 *  groups, and scenes.
 
 *  TO INSTALL:
 *  See documentation on Hubitat Community forum.
 *
 *  Copyright 2019-2020 Robert Morris
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
 *  Last modified: 2020-01-22
 * 
 *  Changelog:
 * 
 *  v1.0 - Initial Public Release
 *  v1.1 - Added more polling intervals
 *  v1.5 - Added scene integration
 *  v1.6 - Added options for bulb and group deivce naming
 *  v1.7 - Addition of new child device types, updating groups from member bulbs
 *  v1.9 - Added CT and dimmable bulb types
 */ 

definition (
    name: "CoCoHue (Bridge Instance Child App)",
    namespace: "RMoRobert",
    author: "Robert Morris",
    description: "Integrate Hue Bridge lights, groups, and scenes into Hubitat (use parent app to create instances)",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    parent: "RMoRobert:CoCoHue - Hue Bridge Integration",
)

preferences {
    page(name: "pageFirstPage", content: "pageFirstPage")
    page(name: "pageAddBridge", content: "pageAddBridge")
    page(name: "pageLinkBridge", content: "pageLinkBridge")
    page(name: "pageBridgeLinked", content: "pageBridgeLinked")
    page(name: "pageManageBridge", content: "pageManageBridge")
    page(name: "pageSelectLights", content: "pageSelectLights")
    page(name: "pageSelectGroups", content: "pageSelectGroups")
    page(name: "pageSelectScenes", content: "pageSelectScenes")
    page(name: "pageSelectSchedules", content: "pageSelectSchedules")
    page(name: "pageSelectSensors", content: "pageSelectSensors")
    page(name: "pageSelectResourcelinks", content: "pageSelectResourcelinks")
}

/** Namespace to search/use for child device driver creation */
def getChildNamespace() {
    return "RMoRobert"
}

def installed() {
    log.info("Installed with settings: ${settings}")
    initialize()
}

def uninstalled() {
    log.info("Uninstalling")
}

def updated() {
    log.info("Updated with settings: ${settings}")
    initialize()
}

def initialize() {
    log.debug("Initializing...")
    unschedule()
    unsubscribe()    
    int disableTime = 1800
    if (enableDebug) {
        log.debug "Debug logging will be automatically disabled in ${disableTime} seconds"
        runIn(disableTime, debugOff)
    }
    def pollInt = settings["pollInterval"]?.toInteger()
    // If change polling options in UI, may need to modify some of these cases:
    switch (pollInt ?: 0) {
        case 0:
            logDebug("Polling disabled; not scheduling")
            break
        case 1..59:
            logDebug("Scheduling polling every ${pollInt} seconds")
            schedule("${Math.round(Math.random() * pollInt)}/${pollInt} * * ? * * *", "refreshBridge")
            break
        case 60..259:
            logDebug("Scheduling polling every 1 minute")
            runEvery1Minute("refreshBridge")
            break
        case 300..1800:
            logDebug("Schedulig polling every 5 minutes")
            runEvery5Minutes("refreshBridge")
            break
        default:
            logDebug("Scheduling polling every hour")
            runEvery1Hour("refreshBridge")                
    }
}

def debugOff() {
    log.warn("Disabling debug logging")
    app.updateSetting("enableDebug", [value:"false", type:"bool"])
}

def pageFirstPage() {
    return (state.bridgeLinked ? pageManageBridge() : pageAddBridge())
}

def pageAddBridge() {
    dynamicPage(name: "pageAddBridge", uninstall: true, install: false, nextPage: pageLinkBridge) {
        state.authRefreshInterval = 4
        state.authTryCount = 0
        section("Add Hue Bridge") {
            input(name: "bridgeIP", type: "string", title: "Hue Bridge IP address:", required: false, defaultValue: null, submitOnChange: true)            
            if (settings["bridgeIP"] && state.bridgeLinked) {
                input(name: "boolForceCreateBridge", type: "bool", title: "Force recreation of Bride child device (WARNING: will un-link any " +
                      "existing Bridge child device from this child app if one still exists)", submitOnChange: true)
            }
            if (settings["bridgeIP"] && !state.bridgeLinked || settings["boolForceCreateBridge"]) {
                paragraph("<strong>Press the button on your Hue Bridge,</strong> then click/tap the \"Next\" button to continue.")
            }
        }
    }
}

def pageLinkBridge() {
    def authMaxTries = 20
    if (settings["boolForceCreateBridge"]) {
        state.remove("bridgeAuthorized")
        app.updateSetting("boolForceCreateBridge", false)
    }
    
    dynamicPage(name: "pageLinkBridge", refreshInterval: state.authRefreshInterval, uninstall: true, install: false, nextPage: "pageBridgeLinked") {  
        section("Linking Hue Bridge") {
            if (!(state["bridgeAuthorized"])) {
                log.debug("Attempting Hue Bridge authorization; attempt number ${state.authTryCount+1}")
                sendUsernameRequest()
                state.authTryCount = state.authTryCount + 1
                paragraph("Waiting for Bridge to authorize. This page will automatically refresh.")
                if (state.authTryCount > 5 && state.authTryCount < authMaxTries) {
                    def strParagraph = "Still waiting for authorization. Please make sure you pressed " +
                        "the button on the Hue Bridge."
                    if (state.authTryCount > 10) {
                        strParagraph + "Also, verify that your Bridge IP address is correct: ${settings["bridgeIP"]}"
                    }
                    paragraph(strParagraph)
                }
                if (state.authTryCount >= authMaxTries) {
                    paragraph("<b>Authorization timed out. Please go back to the previous page, check your settings, " +
                              "and try again.</b>")
                }                
            }
            else {
                if (!state.bridgeLinked) {
                    log.debug("Hue Bridge authorized. Requesting info from Bridge and creating Bridge device...")
                    sendBridgeInfoRequest()
                } else {
                    logDebug("Bridge device already exits; skipping creation")
                }
                paragraph("<b>Your Hue Bridge has been linked!</b> Press \"Next\" to continue.")
            }
        }
    }
}

def pageBridgeLinked() {
    dynamicPage(name: "pageBridgeLinked", uninstall: true, install: true, nextPage: pageFirstPage) {
        state.authRefreshInterval = 4
        state.authTryCount = 0
        if (state["bridgeAuthorized"] && state["bridgeLinked"]) {
            section("Bridge Linked") {
                paragraph("Your Hue Bridge has been successfully linked to Hubitat. Press \"Done\" to finish " +
                          "installing the app, then re-open it to discover/add devices.")
            }
        } else {
            section("Bridge Not Linked") {
                paragraph("There was a problem authorizing or linking your Hue Bridge. Please start over and try again.")
            }
        }
    }
}         

def pageManageBridge() {
    if (settings["newBulbs"]) {
        logDebug("New bulbs selected. Creating...")
        createNewSelectedBulbDevices()
    }
    if (settings["newGroups"]) {
        logDebug("New groups selected. Creating...")
        createNewSelectedGroupDevices()
    }
    if (settings["newScenes"]) {
        logDebug("New scenes selected. Creating...")
        createNewSelectedSceneDevices()
    }
    if (settings["newSchedules"]) {
        logDebug("New scheules selected. Creating...")
        createNewSelectedScheduleDevices()
    }
    if (settings["newSensors"]) {
        logDebug("New sensors selected. Creating...")
        createNewSelectedSensorDevices()
    }
    if (settings["newResourcelinks"]) {
        logDebug("New Resourcelinks selected. Creating...")
        createNewSelectedResourcelinkDevices()
    }
    dynamicPage(name: "pageManageBridge", uninstall: true, install: true) {  
        section("Manage Hue Bridge Devices") {
            href(name: "hrefSelectLights", title: "Select Lights",
                description: "", page: "pageSelectLights")
            href(name: "hrefSelectGroups", title: "Select Groups",
                description: "", page: "pageSelectGroups")
            href(name: "hrefSelectScenes", title: "Select Scenes",
                description: "", page: "pageSelectScenes")
            href(name: "hrefSelectSchedules", title: "Select Schedules",
                description: "", page: "pageSelectSchedules")
            href(name: "hrefSelectSensors", title: "Select Sensors",
                description: "", page: "pageSelectSensors")
            href(name: "hrefSelectResourcelinks", title: "Select Resourcelinks",
                description: "", page: "pageSelectResourcelinks")
        }
        section("Bridge Device Options", hideable: true, hidden: true) {
            input(name: "pollInterval", type: "enum", title: "Poll bridge every...",
               options: [0:"Disabled", 10:"10 seconds", 15:"15 seconds", 20:"20 seconds", 30:"30 seconds", 60:"1 minute (recommended)", 300:"5 minutes", 3600:"1 hour"], defaultValue:60)
            href(name: "hrefAddBridge", title: "Edit Bridge IP or re-authorize",
                 description: "", page: "pageAddBridge")
            input(name: "showAllScenes", type: "bool", title: "Allow adding scenes not associated with rooms/zones (not recommended; devices will not support \"off\" command)")
            input(name: "enableDebug", type: "bool", title: "Enable debug logging", defaultValue: true)
            label(title: "Name for this Hue Bridge child app (optional)", required: false)
        }
    }
}

def pageSelectLights() {   
    dynamicPage(name: "pageSelectLights", refreshInterval: refreshInt, uninstall: true, install: false, nextPage: pageManageBridge) {
        state.addedBulbs = [:]  // To be populated with lights user has added, matched by Hue ID
        def bridge = getChildDevice("CCH/${state.bridgeID}")
        if (!bridge) {
            log.error "No Bridge device found"
            return
        }
        bridge.getAllBulbs()
        def refreshInt = 10
        def arrNewBulbs = []
        def bulbCache = bridge.getAllBulbsCache()
        if (bulbCache) {
            refreshInt = 0
            bulbCache.each {
                def bulbChild = getChildDevice("CCH/${state.bridgeID}/Light/${it.key}")
                if (bulbChild) {
                    state.addedBulbs.put(it.key, bulbChild.name)
                } else {
                    def newBulb = [:]
                    newBulb << [(it.key): (it.value.name)]
                    arrNewBulbs << newBulb
                }
            }
            arrNewBulbs = arrNewBulbs.sort { a, b ->
                // Sort by bulb name (default would be hue ID)
                a.entrySet().iterator().next()?.value <=> b.entrySet().iterator().next()?.value
            }
            state.addedBulbs = state.addedBulbs.sort { it.value }
        }
        if (!bulbCache) {            
            refreshInt = 10
            section("Discovering bulbs/lights. Please wait...") {            
                paragraph("Press \"Refresh\" if you see this message for an extended period of time")
                input(name: "btnBulbRefresh", type: "button", title: "Refresh", submitOnChange: true)
            }
        }
        else {
            section("Manage Lights") {
                input(name: "newBulbs", type: "enum", title: "Select Hue lights to add:",
                      multiple: true, options: arrNewBulbs)
                input(name: "boolAppendBulb", type: "bool", title: "Append \"(Hue Light)\" to Hubitat device name")
            }
            section("Previously added lights") {
                if (state.addedBulbs) {
                    state.addedBulbs.each {
                        paragraph(it.value)
                    }
                }
                else {
                    paragraph("No bulbs added")
                }
            }
            section("Rediscover Bulbs") {
                paragraph("If you added new lights to the Hue Bridge and do not see them above, click/tap the button " +
                          "below to retrieve new information from the Bridge.")
                input(name: "btnBulbRefresh", type: "button", title: "Refresh Bulb List", submitOnChange: true)
            }
        }
    }    
}


def pageSelectGroups() {
    dynamicPage(name: "pageSelectGroups", refreshInterval: refreshInt, uninstall: true, install: false, nextPage: pageManageBridge) {
        state.addedGroups = [:]  // To be populated with groups user has added, matched by Hue ID
        def bridge = getChildDevice("CCH/${state.bridgeID}")
        if (!bridge) {
            log.error "No Bridge device found"
            return
        }
        bridge.getAllGroups()
        def refreshInt = 10
        def arrNewGroups = []
        def groupCache = bridge.getAllGroupsCache()

        if (groupCache) {
            refreshInt = 0
            groupCache.each {
                def groupChild = getChildDevice("CCH/${state.bridgeID}/Group/${it.key}")
                if (groupChild) {
                    state.addedGroups.put(it.key, groupChild.name)
                } else {
                    def newGroup = [:]
                    newGroup << [(it.key): (it.value.name)]
                    arrNewGroups << newGroup
                }
            }
            arrNewGroups = arrNewGroups.sort {a, b ->
                // Sort by group name (default would be Hue ID)
                a.entrySet().iterator().next()?.value <=> b.entrySet().iterator().next()?.value
                }
            state.addedGroups = state.addedGroups.sort { it.value }
        }

        if (!groupCache) {            
            refreshInt = 10
            section("Discovering groups. Please wait...") {            
                paragraph("Press \"Refresh\" if you see this message for an extended period of time")
                input(name: "btnGroupRefresh", type: "button", title: "Refresh", submitOnChange: true)
            }
        }
        else {
            section("Manage Groups") {
                input(name: "newGroups", type: "enum", title: "Select Hue groups to add:",
                      multiple: true, options: arrNewGroups)
                input(name: "boolAppendGroup", type: "bool", title: "Append \"(Hue Group)\" to Hubitat device name")
            }
            section("Previously added groups") {
                if (state.addedGroups) {
                    state.addedGroups.each {
                        paragraph(it.value)
                    }
                }
                else {
                    paragraph("No groups added")
                }
            }
            section("Rediscover Groups") {
                paragraph("If you added new groups to the Hue Bridge and do not see them above, click/tap the button " +
                          "below to retrieve new information from the Bridge.")
                input(name: "btnGroupRefresh", type: "button", title: "Refresh Group List", submitOnChange: true)
            }
        }
    }    
}

def pageSelectScenes() {
    dynamicPage(name: "pageSelectScenes", uninstall: true, install: false, nextPage: pageManageBridge) {  
        state.addedScenes = [:]  // To be populated with scenes user has added, matched by Hue ID
        def bridge = getChildDevice("CCH/${state.bridgeID}")
        if (!bridge) {
            log.error "No Bridge device found"
            return
        }
        bridge.getAllScenes()
        def refreshInt = 10
        def arrNewScenes = []
        def sceneCache = bridge.getAllScenesCache()

        def groupCache = bridge.getAllGroupsCache()
        def grps = [:]
        groupCache?.each { grps << [(it.key) : (it.value.name)] }

        if (sceneCache) {
            refreshInt = 0
            state.sceneFullNames = [:]
            sceneCache.each { sc ->
                def sceneChild = getChildDevice("CCH/${state.bridgeID}/Scene/${sc.key}")
                if (sceneChild) {
                    state.addedScenes.put(sc.key, sceneChild.name)
                } else {
                    def newScene = [:]
                    def sceneName = sc.value.name
                    if (sc.value.group) {
                        grps.each { g ->
                            def k = g.key
                            if (k && k == sc.value.group) {
                                def v = g.value
                                // "Group Name - Scene Name" naming convention:
                                if (v) sceneName = "$v - $sceneName"
                                }
                            }
                        }
                        if (sc.value?.group || settings["showAllScenes"]) {
                            state.sceneFullNames.put(sc.key, sceneName)
                            newScene << [(sc.key): (sceneName)]                        
                            arrNewScenes << newScene
                        }
                    }
                }
            arrNewScenes = arrNewScenes.sort {a, b ->
                // Sort by group name (default would be Hue ID)
                a.entrySet().iterator().next()?.value <=> b.entrySet().iterator().next()?.value
            }
            state.addedScenes = state.addedScenes.sort { it.value }
        }

        if (!sceneCache) {            
            refreshInt = 10
            section("Discovering scenes. Please wait...") {            
                paragraph("Press \"Refresh\" if you see this message for an extended period of time")
                input(name: "btnSceneRefresh", type: "button", title: "Refresh", submitOnChange: true)
            }
        }
        else {
            section("Manage Scenes") {
                input(name: "newScenes", type: "enum", title: "Select Hue scenes to add:",
                      multiple: true, options: arrNewScenes)
            }
            section("Previously added scenes") {
                if (state.addedScenes) {
                    state.addedScenes.each {
                        paragraph(it.value)
                    }
                }
                else {
                    paragraph("No scenes added")
                }
            }
            section("Rediscover Scenes") {
                paragraph("If you added new scenes to the Hue Bridge and do not see them above, if room/zone names are " +
                          "missing from scenes (if assigned to one), or if you changed the \"Include scenes...\" setting above, " +
                          "click/tap the button below to retrieve new information from the Bridge.")
                input(name: "btnSceneRefresh", type: "button", title: "Refresh Scene List", submitOnChange: true)
            }
        }
    }     
}

//  start New Stuff *****************************************************************************

def pageSelectSchedules() {   
    dynamicPage(name: "pageSelectSchedules", refreshInterval: refreshInt, uninstall: true, install: false, nextPage: pageManageBridge) {
        state.addedSchedules = [:]  // To be populated with Schedules user has added, matched by Hue ID
        def bridge = getChildDevice("CCH/${state.bridgeID}")
        if (!bridge) {
            log.error "No Bridge device found"
            return
        }
        bridge.getAllSchedules()
        def refreshInt = 10
        def arrNewSchedules = []
        def scheduleCache = bridge.getAllSchedulesCache()
        
        log.debug "scheduleCache: ${scheduleCache}"
        
        if (scheduleCache) {
            refreshInt = 0
            scheduleCache.each {
                def scheduleChild = getChildDevice("CCH/${state.bridgeID}/Schedule/${it.key}")
                if (scheduleChild) {
                    state.addedSchedules.put(it.key, scheduleChild.name)
                } else {
                    def newSchedule = [:]
                    newSchedule << [(it.key): (it.value.name)] 
                    arrNewSchedules << newSchedule
                }
            }
            arrNewSchedules = arrNewSchedules.sort { a, b ->
                // Sort by Schedule name (default would be hue ID)
                a.entrySet().iterator().next()?.value <=> b.entrySet().iterator().next()?.value
            }
            state.addedSchedules = state.addedSchedules.sort { it.value }
        }
        if (!scheduleCache) {            
            refreshInt = 10
            section("Discovering Schedules. Please wait...") {            
                paragraph("Press \"Refresh\" if you see this message for an extended period of time")
                input(name: "btnScheduleRefresh", type: "button", title: "Refresh", submitOnChange: true)
            }
        }
        else {
            section("Manage Schedules") {
                input(name: "newSchedules", type: "enum", title: "Select Hue Schedules to add:",
                      multiple: true, options: arrNewSchedules)
                input(name: "boolAppendSchedule", type: "bool", title: "Append \"(Hue Schedule)\" to Hubitat device name")
            }
            section("Previously added Schedules") {
                if (state.addedSchedules) {
                    state.addedSchedules.each {
                        paragraph(it.value)
                    }
                }
                else {
                    paragraph("No Schedules added")
                }
            }
            section("Rediscover Schedules") {
                paragraph("If you added new Schedules to the Hue Bridge and do not see them above, click/tap the button " +
                          "below to retrieve new information from the Bridge.")
                input(name: "btnScheduleRefresh", type: "button", title: "Refresh Schedule List", submitOnChange: true)
            }
        }
    }    
}

def pageSelectSensors() {   
    dynamicPage(name: "pageSelectSensors", refreshInterval: refreshInt, uninstall: true, install: false, nextPage: pageManageBridge) {
        state.addedSensors = [:]  // To be populated with Sensors user has added, matched by Hue ID
        def bridge = getChildDevice("CCH/${state.bridgeID}")
        if (!bridge) {
            log.error "No Bridge device found"
            return
        }
        bridge.getAllSensors()
        def refreshInt = 10
        def arrNewSensors = []
        def sensorCache = bridge.getAllSensorsCache()
        
        log.debug "sensorCache: ${sensorCache}"
        
        if (sensorCache) {
            refreshInt = 0
            sensorCache.each {
                def sensorChild = getChildDevice("CCH/${state.bridgeID}/Sensor/${it.key}")
                if (sensorChild) {
                    state.addedSensors.put(it.key, sensorChild.name)
                } else {
                    def newSensor = [:]
                    newSensor << [(it.key): (it.value.name)] 
                    arrNewSensors << newSensor
                }
            }
            arrNewSensors = arrNewSensors.sort { a, b ->
                // Sort by Sensor name (default would be hue ID)
                a.entrySet().iterator().next()?.value <=> b.entrySet().iterator().next()?.value
            }
            state.addedSensors = state.addedSensors.sort { it.value }
        }
        if (!sensorCache) {            
            refreshInt = 10
            section("Discovering Sensors. Please wait...") {            
                paragraph("Press \"Refresh\" if you see this message for an extended period of time")
                input(name: "btnSensorRefresh", type: "button", title: "Refresh", submitOnChange: true)
            }
        }
        else {
            section("Manage Sensors") {
                input(name: "newSensors", type: "enum", title: "Select Hue Sensors to add:",
                      multiple: true, options: arrNewSensors)
                input(name: "boolAppendSensor", type: "bool", title: "Append \"(Hue Sensor)\" to Hubitat device name")
            }
            section("Previously added Sensors") {
                if (state.addedSensors) {
                    state.addedSensors.each {
                        paragraph(it.value)
                    }
                }
                else {
                    paragraph("No Sensors added")
                }
            }
            section("Rediscover Sensors") {
                paragraph("If you added new Sensors to the Hue Bridge and do not see them above, click/tap the button " +
                          "below to retrieve new information from the Bridge.")
                input(name: "btnSensorRefresh", type: "button", title: "Refresh Sensor List", submitOnChange: true)
            }
        }
    }    
}

def pageSelectResourcelinks() {   
    dynamicPage(name: "pageSelectResourcelinks", refreshInterval: refreshInt, uninstall: true, install: false, nextPage: pageManageBridge) {
        state.addedResourcelinks = [:]  // To be populated with Resourcelink user has added, matched by Hue ID
        def bridge = getChildDevice("CCH/${state.bridgeID}")
        if (!bridge) {
            log.error "No Bridge device found"
            return
        }
        bridge.getAllResourcelinks()
        def refreshInt = 10
        def arrNewResourcelinks = []
        def resourcelinkCache = bridge.getAllResourcelinksCache()
        
        log.debug "resourcelinkCache: ${resourcelinkCache}"
        
        if (resourcelinkCache) {
            refreshInt = 0
            resourcelinkCache.each {
                def resourcelinkChild = getChildDevice("CCH/${state.bridgeID}/Resourcelink/${it.key}")
                if (resourcelinkChild) {
                    state.addedResourcelinks.put(it.key, resourcelinkChild.name)
                } else {
                    def newResourcelink = [:]
                    newResourcelink << [(it.key): (it.value.name)] 
                    arrNewResourcelinks << newResourcelink
                }
            }
            arrNewResourcelinks = arrNewResourcelinks.sort { a, b ->
                // Sort by Resourcelink name (default would be hue ID)
                a.entrySet().iterator().next()?.value <=> b.entrySet().iterator().next()?.value
            }
            state.addedResourcelinks = state.addedResourcelinks.sort { it.value }
        }
        if (!resourcelinkCache) {            
            refreshInt = 10
            section("Discovering Resourcelink. Please wait...") {            
                paragraph("Press \"Refresh\" if you see this message for an extended period of time")
                input(name: "btnResourcelinkRefresh", type: "button", title: "Refresh", submitOnChange: true)
            }
        }
        else {
            section("Manage Resourcelinks") {
                input(name: "newResourcelinks", type: "enum", title: "Select Hue Resourcelinks to add:",
                      multiple: true, options: arrNewResourcelinks)
                input(name: "boolAppendResourcelink", type: "bool", title: "Append \"(Hue Resourcelink)\" to Hubitat device name")
            }
            section("Previously added Resourcelinks") {
                if (state.addedResourcelinks) {
                    state.addedResourcelinks.each {
                        paragraph(it.value)
                    }
                }
                else {
                    paragraph("No Resourcelinks added")
                }
            }
            section("Rediscover Resourcelinks") {
                paragraph("If you added new Resourcelinks to the Hue Bridge and do not see them above, click/tap the button " +
                          "below to retrieve new information from the Bridge.")
                input(name: "btnResourcelinkRefresh", type: "button", title: "Refresh Resourcelink List", submitOnChange: true)
            }
        }
    }    
}

//  end New Stuff *****************************************************************************

/** Creates new Hubitat devices for new user-selected bulbs on lights-selection
 * page (intended to be called after navigating away/using "Done" from that page)
 */
def createNewSelectedBulbDevices() {
    // TODO: Change most of these when new drivers made
    def driverMap = ["extended color light": "CoCoHue RGBW Bulb",
                     "color light": "CoCoHue RGBW Bulb",            
                     "color temperature light": "CoCoHue CT Bulb",
                     "dimmable light": "CoCoHue Dimmable Bulb",
                     "on/off light": "CoCoHue On/Off Plug",
                     "on/off plug-in unit": "CoCoHue On/Off Plug",
                     "DEFAULT": "CoCoHue RGBW Bulb"]
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) log.error("Unable to find bridge device")
    def bulbCache = bridge?.getAllBulbsCache()
    settings["newBulbs"].each {
        def b = bulbCache.get(it)
        if (b) {
            try {
                logDebug("Creating new device for Hue light ${it} (${b.name})")
                def devDriver = driverMap[b.type.toLowerCase()] ?: driverMap["DEFAULT"]
                def devDNI = "CCH/${state.bridgeID}/Light/${it}"
                def devProps = [name: (settings["boolAppendBulb"] ? b.name + " (Hue Bulb)" : b.name)]
                addChildDevice(getChildNamespace(), devDriver, devDNI, null, devProps)

            } catch (Exception ex) {
                log.error("Unable to create new device for $it: $ex")
            }
        } else {
            log.error("Unable to create new device for bulb $it: ID not found on Hue Bridge")
        }
    }    
    bridge.clearBulbsCache()
    bridge.getAllBulbs()
    app.removeSetting("newBulbs")
}

/** Creates new Hubitat devices for new user-selected groups on groups-selection
 * page (intended to be called after navigating away/using "Done" from that page)
 */
def createNewSelectedGroupDevices() {
    def driverName = "CoCoHue Group"
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) log.error("Unable to find bridge device")
    def groupCache = bridge?.getAllGroupsCache()
    settings["newGroups"].each {
        def g = groupCache.get(it)
        if (g) {
            try {
                logDebug("Creating new device for Hue group ${it} (${g.name})")
                def devDNI = "CCH/${state.bridgeID}/Group/${it}"
                def devProps = [name: (settings["boolAppendGroup"] ? g.name + " (Hue Group)" : g.name)]
                addChildDevice(getChildNamespace(), driverName, devDNI, null, devProps)

            } catch (Exception ex) {
                log.error("Unable to create new group device for $it: $ex")
            }
        } else {
            log.error("Unable to create new device for group $it: ID not found on Hue Bridge")
        }
    }    
    bridge.clearGroupsCache()
    bridge.getAllGroups()
    bridge.refresh()
    app.removeSetting("newGroups")
}


/** Creates new Hubitat devices for new user-selected scenes on scene-selection
 * page (intended to be called after navigating away/using "Done" from that page)
 */
def createNewSelectedSceneDevices() {
    def driverName = "CoCoHue Scene"
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) log.error("Unable to find bridge device")
    def sceneCache = bridge?.getAllScenesCache()
    settings["newScenes"].each {
        def sc = sceneCache.get(it)
        if (sc) {
            try {
                logDebug("Creating new device for Hue group ${it}" +
                         " (state.sceneFullNames?.get(it) ?: sc.name)")
                def devDNI = "CCH/${state.bridgeID}/Scene/${it}"
                def devProps = [name: (state.sceneFullNames?.get(it) ?: sc.name)]
                def dev = addChildDevice(getChildNamespace(), driverName, devDNI, null, devProps)
            } catch (Exception ex) {
                log.error("Unable to create new scene device for $it: $ex")
            }
        } else {
            log.error("Unable to create new scene for scene $it: ID not found on Hue Bridge")
        }
    }  
    bridge.clearScenesCache()
    //bridge.getAllScenes()
    app.removeSetting("newScenes")
    state.remove("sceneFullNames")
}

def createNewSelectedScheduleDevices() {
    def driverMap = ["nothing":"making a map","DEFAULT": "CoCoHue On/Off Plug"]
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) log.error("Unable to find bridge device")
    def scheduleCache = bridge?.getAllSchedulesCache()
    settings["newSchedules"].each {
        def b = scheduleCache.get(it)
        if (b) {
            try {
                logDebug("Creating new device for Hue Schedule ${it} (${b.name})")
                //def devDriver = driverMap[b.type.toLowerCase()] ?: driverMap["DEFAULT"]
                def devDriver = "CoCoHue On/Off Plug"
                def devDNI = "CCH/${state.bridgeID}/Schedule/${it}"
                def devProps = [name: (settings["boolAppendSchedule"] ? b.name + " (Hue Schedule)" : b.name)]
                addChildDevice(getChildNamespace(), devDriver, devDNI, null, devProps)

            } catch (Exception ex) {
                log.error("Unable to create new device for $it: $ex")
            }
        } else {
            log.error("Unable to create new device for Schedule $it: ID not found on Hue Bridge")
        }
    }    
    bridge.clearSchedulesCache()
    bridge.getAllSchedules()
    app.removeSetting("newSchedules")
}

def createNewSelectedSensorDevices() {
    def driverMap = ["nothing":"making a map","DEFAULT": "CoCoHue On/Off Plug"]
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) log.error("Unable to find bridge device")
    def sensorCache = bridge?.getAllSensorsCache()
    settings["newSensors"].each {
        def b = sensorCache.get(it)
        if (b) {
            try {
                logDebug("Creating new device for Hue Sensor ${it} (${b.name})")
                //def devDriver = driverMap[b.type.toLowerCase()] ?: driverMap["DEFAULT"]
                def devDriver = "CoCoHue On/Off Plug"
                def devDNI = "CCH/${state.bridgeID}/Sensor/${it}"
                def devProps = [name: (settings["boolAppendSensor"] ? b.name + " (Hue Sensor)" : b.name)]
                addChildDevice(getChildNamespace(), devDriver, devDNI, null, devProps)

            } catch (Exception ex) {
                log.error("Unable to create new device for $it: $ex")
            }
        } else {
            log.error("Unable to create new device for Sensor $it: ID not found on Hue Bridge")
        }
    }    
    bridge.clearSensorsCache()
    bridge.getAllSensors()
    app.removeSetting("newSensors")
}

def createNewSelectedResourcelinkDevices() {
    def driverMap = ["nothing":"making a map","DEFAULT": "CoCoHue On/Off Plug"]
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) log.error("Unable to find bridge device")
    def resourcelinkCache = bridge?.getAllResourcelinksCache()
    settings["newResourcelinks"].each {
        def b = resourcelinkCache.get(it)
        if (b) {
            try {
                logDebug("Creating new device for Hue Resourcelink ${it} (${b.name})")
                //def devDriver = driverMap[b.type.toLowerCase()] ?: driverMap["DEFAULT"]
                def devDriver = "CoCoHue On/Off Labs"
                def devDNI = "CCH/${state.bridgeID}/Resourcelink/${it}"
                def devProps = [name: (settings["boolAppendResourcelink"] ? b.name + " (Hue Resourcelink)" : b.name)]
                addChildDevice(getChildNamespace(), devDriver, devDNI, null, devProps)

            } catch (Exception ex) {
                log.error("Unable to create new device for $it: $ex")
            }
        } else {
            log.error("Unable to create new device for Resourcelink $it: ID not found on Hue Bridge")
        }
    }    
    bridge.clearResourcelinksCache()
    bridge.getAllResourcelinks()
    app.removeSetting("newResourcelinks")
}

/** Sends request for username creation to Bridge API. Intended to be called after user
 *  presses link button on Bridge
 */
private sendUsernameRequest() {
    def userDesc = location.name ? "Hubitat CoCoHue#${location.name}" : "Hubitat CoCoHue"
    def host = settings["bridgeIP"] + ":80"
    sendHubCommand(new hubitat.device.HubAction([
        method: "POST",
        path: "/api",
        headers: [HOST: host],
        body: [devicetype: userDesc]
        ], null, [callback: "parseUsernameResponse"])
    )
}

/** Callback for sendUsernameRequest. Saves username in app state if Bridge is
 * successfully authorized, or logs error if unable to do so.
 */
def parseUsernameResponse(hubitat.device.HubResponse resp) {
    def body = resp.json
    logDebug("Attempting to request Hue Bridge username; result = ${body}")
    
    if (body.success != null) {
        if (body.success[0] != null) {
            if (body.success[0].username) {
                state["username"] = body.success[0].username
                state["bridgeAuthorized"] = true
            }
        }
    }
    else {
        if (body.error != null) {
            log.warn("Error from Bridge: ${body.error}")
        }
        else {
            log.error("Unknown error adding Hue Bridge")
        }
    }
}

/** Requests Bridge info (description.xml) to verify that device is a
 *  Hue Bridge and to retrive (when parsed in parseBridgeInfoRequest)
 *  information necessary to create the Bridge device
 */
private sendBridgeInfoRequest() {
    log.debug("Sending request for Bridge information")
    def host = settings["bridgeIP"] + ":80"
    sendHubCommand(new hubitat.device.HubAction([
        method: "GET",
        path: "/description.xml",
        headers: [  HOST: host  ],
        body: []], null, [callback: "parseBridgeInfoResponse"])
    )
}

/** Parses response from GET of description.xml on the Bridge;
 *  verifies that device is a Hue Bridge (modelName contains "Philips Hue Bridge")
 * and obtains MAC address for use in creating Bridge DNI and device name
 */
private parseBridgeInfoResponse(hubitat.device.HubResponse resp) {
    log.debug("Parsing response from Bridge information request")
    def body = resp.xml
    log.debug "In parseBridge... body: ${body}"
    
    if (body?.device?.modelName?.text().contains("Philips hue bridge")) {
        state.serial
        def serial = body?.device?.serialNumber?.text()
        if (serial) {
            log.debug("Hue Bridge serial parsed as ${serial}; creating device")
            state.bridgeID = serial.reverse().take(6).reverse().toUpperCase() // last 6 of MAC
            def bridgeDevice
            try {
                bridgeDevice = addChildDevice(getChildNamespace(), "CoCoHue Bridge", "CCH/${state.bridgeID}", null,
                                              [label: "CoCoHue Bridge (${state.bridgeID})", name: "CoCoHue Bridge"])
                state.bridgeLinked = true
            } catch (Exception e) {
                log.error("Error creating Bridge device: $e")
            }
            if (!state.bridgeLinked) log.error("Unable to create Bridge device. Make sure driver installed and no Bridge device for this MAC already exists.")
        } else {
            log.error("Unexpected response received from Hue Bridge")
        } 
    } else {
        log.error("No Hue Bridge found at IP address")
    }
}

/** Returns map containing Bridge username, IP, and full HTTP post/port, intended to be
 *  called by child devices so they can send commands to the Hue Bridge API using info
 */
def getBridgeData() {
    logDebug("Running getBridgeData()...")
    if (!state["username"] || !settings["bridgeIP"]) log.error "Missing username or IP address from Bridge"
    def map = [username: state["username"], host: settings["bridgeIP"] + ":80", fullHost: "http://${settings['bridgeIP']}:80"]
    return map
}

/** Calls refresh() method on Bridge child, intended to be called at user-specified
 *  polling interval
 */
private refreshBridge() {
    def bridge = getChildDevice("CCH/${state.bridgeID}")
    if (!bridge) {
            log.error "No Bridge device found; could not refresh/poll"
            return
    }
    logDebug("Polling Bridge...")
    bridge.refresh()
}

/**
 *  Intended to be called by group child device when state is manipulated in a way that would affect
 *  all member bulbs. Updates member bulb states (so doesn't need to wait for next poll to update)
 *  @param states Map of states in Hue Bridge format (e.g., ["on": true])
 *  @param ids Hue IDs of member bulbs to update
 */
 def updateMemberBulbStatesFromGroup(Map states, List ids) {
    logDebug("Updating member bulb $ids states after group device change...")
    ids?.each {
        def device = getChildDevice("CCH/${state.bridgeID}/Light/${it}")
        device?.createEventsFromMap(states, false)
    }
 }

 /**
 *  Intended to be called by bulb child device when state is manipulated in a way that would affect
 *  group and user has enabled this option. Updates group device states if this bulb ID is found as a
 *  member of that group (so doesn't need to wait for next poll to update)
 *  @param states Map of states in Hue Bridge format (e.g., ["on": true])
 *  @param id Hue bulb ID to search all groups for (will update group if bulb found in group)
 */
 def updateGroupStatesFromBulb(Map states, id) {
    logDebug("Searching for group devices containing bulb $id to update group state after bulb state change...")
    //TODO: There is a better, Groovier way to do this search...
    def matchingGroups = []
    getChildDevices()?.each {
        if (it.getDeviceNetworkId()?.startsWith("CCH/${state.bridgeID}/Group/")) {
            if (it.getMemberBulbIDs()?.contains(id)) {
                logDebug("Bulb $id found in group. Updating states.")
                matchingGroups.add(it)
            }
        }
    }
    matchingGroups.each {
        // Hue app reports "on" if any members on but takes last color/level/etc. from most recent
        // change, so emulate that behavior here
        def onState = getIsAnyGroupMemberBulbOn(it)
        it.createEventsFromMap(states << ["on": onState], false)
    }
 }

 /**
 * Finds Hubitat devices for member bulbs of group and returns true if any (that are found) are on; returns false
 * if all off or no member bulb devices found
 * @param Instance of CoCoHue Group device on which to check member bulb states
 */
def getIsAnyGroupMemberBulbOn(groupDevice) {
    logDebug ("Determining whether any group member bulbs on for group $groupID")
    def retVal = false
    def memberDevices = []
    if (groupDevice) {
        groupDevice.getMemberBulbIDs().each {
            if (!retVal) { // no point in continuing to check if already found one on
                def memberLight = getChildDevice("CCH/${state.bridgeID}/Light/${it}")
                if (memberLight?.currentValue("switch") == "on") retVal = true
            }
        }
    } else {
        logDebug "No group device found for group ID $groupID"
    }
    logDebug("Determined if any group member bulb on: $retVal")
    return retVal
 }

def appButtonHandler(btn) {
    switch(btn) {
        case "btnBulbRefresh":
        case "btnGroupRefresh":
        case "btnSceneRefresh":
        case "btnScheduleRefresh":
        case "btnSensorRefresh":
        case "btnResourcelinkRefresh":
            // Just want to resubmit page, so nothing
            break
        default:
            log.warn "Unhandled app button press: $btn"
    }
}

def logDebug(str) {
    if (enableDebug) log.debug(str)
}
