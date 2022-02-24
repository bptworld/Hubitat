/**
 *  **************** Patterns Plus Child App  ****************
 *
 *  Design Usage:
 *  Create any pattern you want using the zones associated with the Lifx Light Strip (or any color bulb).
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  1.1.0 - 02/21/22 - Massive update
 *  1.0.2 - 02/17/22 - Attempt to stop the flashing before turning off
 *  1.0.1 - 02/14/22 - Fixed a stubborn error!
 *  1.0.0 - 02/14/22 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat
import groovy.transform.Field

def setVersion(){
    state.name = "Patterns Plus"
	state.version = "1.1.0"
}

definition(
    name: "Patterns Plus Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create any pattern you want using the zones associated with the Lifx Light Strip (or any color bulb).",
    category: "Convenience",
	parent: "BPTWorld:Patterns Plus",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page name: "pageConfig"
    page name: "importExportPage", title: "", install:false, uninstall:false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Information:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Create any pattern you want using the zones associated with the Lifx Light Strip (or any color bulb)."
            paragraph "This can be used with ANY color bulbs, not just Lifx. Results may vary when using non-Lifx and/or Hue devices."
            paragraph "<b>Important Note:</b> Leave the parent device 'on'. Then control each zone with the child devices."
            paragraph "Each zone is individualy controlled using a set of commands. The command set looks like this:"
            paragraph "<b>zone/group ; zgNum ; theCMD ; theColor ; theLevel ; theDelay</b>"
            paragraph "<b>theZone/Group:</b><br> - z - ie. z<br><b>zgNum</b> - # - ie. 1<br>* Zone 1 is closest to the power cord<br><b>theCMD:</b><br> - on - turn on the zone<br> - off - turn off the zone<br> - c - set the zone to a certain color<br><b>theColor:</b><br> - Available Colors: white, daylight, soft white, warm white, blue, green, yellow, orange, pink and red<br> - Custom colors coming soon<br><b>theLevel:</b> - Set the brightness level of the zone (1-100)<br><b>theDelay:</b> - Time in milliseconds to when the next command will be sent"
            paragraph "ie.<br><b>g;2;c;green;50;1</b><br> - group ; group 2 ; set color ; color is green ; brightness is 50 ; next command sent in 1 millisecond"
            paragraph "<b>z;4;c;red;100;100</b><br> - zone ; zone 4 ; set color ; color is red ; brightness is 100 ; next command sent in 100 milliseconds"
            paragraph "Play around with the options and you'll get the hang of it pretty quickly!"
            paragraph "<hr>"
            paragraph "<b>Warning:</b> As with ANY app, sending many commands too fast can and will slow down your hub. This is not meant to turn your house into a dance party but rather as a visual status system."
            paragraph "ie. door was opened, laundry done, motion warnings, sports team scores, etc."
		}   
        
        section("${getImage('optionsGreen')} <b>Zone Groups:</b>", hideable: true, hidden: true) {            
            paragraph "Remember: Actual zones are setup in the parent app."
            if(parent.masterZones) {
                masterList = parent.masterZones.toString().replace("[","").replace("]","").replace(" ","").split(",")
                state.deviceList = masterList.sort { a, b -> a <=> b }
            } else {
                paragraph "Please setup the zones within the parent app. Thanks."
                theZones = []
            }
            input "group1", "enum", title: "Select Group 1 (g1)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group2", "enum", title: "Select Group 2 (g2)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group3", "enum", title: "Select Group 3 (g3)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group4", "enum", title: "Select Group 4 (g4)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group5", "enum", title: "Select Group 5 (g5)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group6", "enum", title: "Select Group 6 (g6)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group7", "enum", title: "Select Group 7 (g7)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
            input "group8", "enum", title: "Select Group 8 (g8)", options: state.deviceList, required:false, multiple:true, submitOnChange:true, width:6
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Pattern")) {
            paragraph "Pattern Command:<br>zone/group ; zgNum ; theCMD ; theColor ; theLevel ; theDelay<br>ie. z;1;c;blue;50;1 - g;1;off;n;n;250<br><small>* See Information Section above for more!</small>"
            input "inputSwitch", "bool", title: "Click Mode or Manual Input", defaultValue:false, submitOnChange:true
            paragraph "<hr>"
            input "pNumber", "text", title: "Pattern Number (ie. p01, p05, p10)", submitOnChange:true
            if(pNumber) {
                if(pNumber.size() < 3) {
                    newNumber = new StringBuilder(pNumber).insert(pNumber.length()-1, "0").toString()
                    app.updateSetting("pNumber",[type:"text", value:"${newNumber}"])
                }
            }
            if(state.cNum == null) state.cNum = pNumber
            if(pNumber != state.cNum) {
                state.working = false
                state.cNum = pNumber
            }
            if(state.mPattern == null) state.mPattern = [:]
            if(state.working == null) state.working = false
            workingData = state.mPattern.get(pNumber)
            if(logEnable) paragraph "In Section Pattern - Working: ${state.working} - workingData: ${workingData}"
            if(inputSwitch) {
                data = state.mPattern.get(pNumber)
                if(data) {
                    app.updateSetting("mData",[type:"text", value:"${data.toLowerCase()}"])
                }
                input "mData", "text", title: "Pattern Command", submitOnChange:true
            } else {
                if(pNumber) {
                    if(!state.working) {
                        state.working = true
                        data = state.mPattern.get(pNumber)
                        if(data) {
                            state.working = true
                            def theData = data.split(";")
                            theZG = theData[0]
                            theZGNum = theData[1]
                            theCommand = theData[2]
                            theColor = theData[3]
                            theLevel = theData[4]
                            theDelay = theData[5]

                            app.updateSetting("pZG",[type:"enum", value:"${theZG}"])
                            app.updateSetting("pZGNum",[type:"number", value:"${theZGNum}"])
                            app.updateSetting("pCommand",[type:"enum", value:"${theCommand}"])
                            app.updateSetting("pColor",[type:"enum", value:"${theColor}"])
                            app.updateSetting("pLevel",[type:"number", value:"${theLevel}"])
                            app.updateSetting("pDelay",[type:"number", value:"${theDelay}"])
                        } else {
                            app.updateSetting("pZG",[type:"enum", value:""])
                            app.updateSetting("pZGNum",[type:"number", value:""])
                            app.updateSetting("pCommand",[type:"enum", value:""])
                            app.updateSetting("pColor",[type:"enum", value:""])
                            app.updateSetting("pLevel",[type:"number", value:""])
                            app.updateSetting("pDelay",[type:"number", value:""])
                        }
                    }

                    input "pZG", "enum", title: "Zone/Group", options: [
                        ["z":"Zone"],
                        ["g":"Group"]
                    ], submitOnChange:true, width:3
                    if(pZG == "g") {
                        input "pZGNum", "text", title: "Number (1 to 8)", range: '1..8', submitOnChange:true, width:3
                    } else {
                        input "pZGNum", "text", title: "Number (01 to 100)", range: '1..100', submitOnChange:true, width:3
                    }
                    input "pCommand", "enum", title: "Command", options: [
                        ["on":"On"],
                        ["off":"Off"],
                        ["c":"Color"]
                    ], submitOnChange:true, width:3
                    input "pColor", "enum", title: "Color", options: [
                        ["white":"White"],
                        ["daylight":"Daylight"],
                        ["soft white":"Soft White"],
                        ["warm white":"Warm White"],
                        ["blue":"Blue"],
                        ["green":"Green"],
                        ["yellow":"Yellow"],
                        ["orange":"Orange"],
                        ["purple":"Purple"],
                        ["pink":"Pink"],
                        ["red":"Red"]             
                    ], submitOnChange:true, width:3
                    input "pLevel", "number", title: "Level (1 to 100)", range: '1..100', submitOnChange:true, width:3
                    input "pDelay", "number", title: "Delay (1 to 10000)", range: '1..10000', submitOnChange:true, width:3
                    paragraph " ", width:3
                    paragraph " ", width:3
                } else {
                    app.updateSetting("pZG",[type:"enum", value:""])
                    app.updateSetting("pZGNum",[type:"number", value:""])
                    app.updateSetting("pCommand",[type:"enum", value:""])
                    app.updateSetting("pColor",[type:"enum", value:""])
                    app.updateSetting("pLevel",[type:"number", value:""])
                    app.updateSetting("pDelay",[type:"number", value:""])
                } 
            }
            paragraph "<small>Remember to click outside of any field before clicking on a button.</small>"
            input "enterPattern", "button", title: "Add/Edit Pattern", textColor: "white", backgroundColor: "green", width:3
            input "cancelPattern", "bool", title: "Clear Pattern", submitOnChange:true, width:3
            if(cancelPattern) {
                app.updateSetting("pZG",[type:"enum", value:""])
                app.updateSetting("pZGNum",[type:"number", value:""])
                app.updateSetting("pCommand",[type:"enum", value:""])
                app.updateSetting("pColor",[type:"enum", value:""])
                app.updateSetting("pLevel",[type:"number", value:""])
                app.updateSetting("pDelay",[type:"number", value:""])
                app?.updateSetting("cancelPattern",[value:"false",type:"bool"])
            }               
            input "rPatterns", "bool", title: "Reset ALL Patterns", defaultValue:false, submitOnChange:true, width:3
            if(rPatterns) {
                input "resetPatterns", "bool", title: "Are you Sure?", submitOnChange:true, width:3
                if(resetPatterns) {
                    state.mPattern = [:]
                    makeTheTable()
                    clearTheBoard()
                    app?.updateSetting("resetPatterns",[value:"false",type:"bool"])
                }
            }
            paragraph "<hr>"
            makeTheTable()
            paragraph "${state.theTable}"
            paragraph "<hr>"
            if(state.mPattern) {
                input "testPattern", "bool", title: "Test Pattern", submitOnChange:true, width: 3
                if(testPattern) {
                    startTheProcess()
                    app?.updateSetting("testPattern",[value:"false",type:"bool"])
                }
                input "resetZones", "bool", title: "Reset ALL Device to Off", submitOnChange:true, width: 3
                if(resetZones) {
                    log.debug "$parent.masterswitchLevel"
                    parent.masterswitchLevel.each { msl ->
                        log.debug "resetZones - working on: $msl"
                        msl.off()
                        pauseExecution(500)
                    }
                    app?.updateSetting("resetZones",[value:"false",type:"bool"])
                }
                input "refreshMap", "bool", title: "Refresh Pattern", defaultValue:false, submitOnChange:true, width: 3
                if(refreshMap) {
                    makeTheTable()
                    app?.updateSetting("refreshMap",[value:"false",type:"bool"])
                }
            }
            state.mPattern = state.mPattern.sort { a, b -> a.key <=> b.key }
            if(logEnable) paragraph "$state.mPattern"
            paragraph "<hr>"
            href "importExportPage", title:"Import/Export Pattern", description:"Export your Patterns! Then import into a new child app to change then up (colors, speed, etc.)"
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {      
            input "numOfPasses", "number", title: "Number of Passes through the Pattern (1 to 10)", range: '1..10', defaultValue:1, required:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Control Switch")) {
            input "cSwitch", "capability.switch", title: "Control Switch", submitOnChange:true       
            input "turnTriggerOff", "bool", title: "Turn the Control Switch Off after Pattern is complete", defaultValue:false, submitOnChange:true
            input "rZones", "bool", title: "Reset all zones after Pattern is complete (even if control switch is still on)", defaultValue:false, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this automation", required:false, submitOnChange:true
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"], submitOnChange:true
            }
		}
		display2()
	}
}

def importExportPage() {
    dynamicPage(name: "importExportPage", title: "", install:false, uninstall:false) {
		display() 
        section(getFormat("header-green", "${getImage("Blank")}"+" Import/Export Pattern")) {
            paragraph "Import/Export your favorite Patterns! Great for using the same pattern but changing up the colors/speed/etc. File includes all Groups and Pattern Data."
            paragraph "<small>Note: 1.0.x files are not compatible with this version.</small>"
            paragraph "<hr>"
            paragraph "<b>Hub Security</b><br>In order to read/write files you must specify your Hubitat admin username and password, if enabled."
            input "hubSecurity", "bool", title: "Hub Security", submitOnChange: true
            if(hubSecurity) {
                input "hubUsername", "string", title: "Hub Security username", submitOnChange:true
                input "hubPassword", "password", title: "Hub Security password", submitOnChange:true
            } else {
                app.removeSetting("hubUsername")
                app.removeSetting("hubPassword")
            }
            paragraph "<hr>"
            input "iPattern", "bool", title: "Import Pattern", defaultValue:false, submitOnChange:true, width:6
            input "ePattern", "bool", title: "Export Pattern", defaultValue:false, submitOnChange:true, width:6
            
            if(iPattern) {
                paragraph "<hr>"
                paragraph "<b>Import Pattern</b>"
                getFileList()
                input "pName", "enum", title: "List of Files", options: fileList, multiple:false, submitOnChange:true
                if(pName) {
                    input "importPattern", "bool", title: "Input Pattern", submitOnChange:true
                    if(importPattern) {
                        importFileHandler()
                        app?.updateSetting("importPattern",[value:"false",type:"bool"])
                        app.removeSetting("pName")
                    }
                }
                paragraph "Note: If you want to delete the file at any time. <a href='http://${location.hub.localIP}:8080/hub/fileManager' target=_blank>Click Here</a> to visit the File Manager."
            }
            
            if(ePattern) {
                paragraph "<hr>"
                paragraph "<b>Export Pattern</b>"
                input "newORold", "bool", title: "New File or Existing File", submitOnChange:true
                if(newORold) {
                    getFileList()
                    input "pName", "enum", title: "List of Files", options: fileList, multiple:false, submitOnChange:true
                } else {
                    input "pName", "text", title: "Name of file to Export <small>(ie. myPattern.txt)</small>", submitOnChange:true
                }
                if(pName) {
                    input "exportPattern", "bool", title: "Export Pattern", submitOnChange:true
                    if(exportPattern) {
                        exportFileHandler()
                        app?.updateSetting("exportPattern",[value:"false",type:"bool"])
                        app.removeSetting("pName")
                    }
                }
            }
        }
        display2()
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {  
        if(cSwitch) subscribe(cSwitch, "switch.on", startTheProcess)
        if(cSwitch) subscribe(cSwitch, "switch.off", resetCurrentStatus)
        subscribe(location, "pattern", checkPatternHandler)
        parent.pcMapOfChildren("nothing")
    }
}

def startTheProcess(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        if(logEnable) log.debug "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In startTheProcess (${state.version})"
        if(state.mPattern) {
            state.mPattern = state.mPattern.sort { a, b -> a.key <=> b.key }
            if(logEnable) log.debug "In startTheProcess - mPattern: ${state.mPattern}"
            getCurrentStatus()
            numOfPasses = numOfPasses ?: 1
            for(x=0;x < numOfPasses;x++) {
                if(logEnable) log.debug "     - - - - - Starting the Pattern (pass: $x) - - - - -     "
                pSplit = state.mPattern.toString().replace("[","").replace("]","").replace(" ","").split(",")
                pSplit.each { it ->
                    tZone = []
                    stuff = it.replace(" ","")
                    (theZone,theNum,theCMD,theColor,theLevel,theDelay) = stuff.split(";")                  
                    if(theZone.contains("z")) {
                        if(logEnable) log.debug "In startTheProcess - zone: $theNum"
                        tZone << parent."zone${theNum}"
                    }
                    
                    if(theZone.contains("g")) {
                        theGroup = app."group${theNum}".toString()
                        if(logEnable) log.debug "In startTheProcess - theGroup: $theGroup"
                        for(tz=1;tz<101;tz++) {
                            if(theGroup.contains("${tz}:")) {
                                tZone << parent."zone${tz}"
                            }
                        }
                    }
                   
                    if(logEnable) log.debug "In startTheProcess - tZone: $tZone | $theCMD | $theColor | $theLevel | $theDelay"
                    if(theCMD.toLowerCase() == "on") {
                        tZone.each {
                            if(logEnable) log.debug "In startTheProcess - Working on $it - theLevel: $theLevel"
                            it.setLevel(theLevel)
                        }
                    } else if(theCMD.toLowerCase() == "off") {
                        tZone.each {
                            if(logEnable) log.debug "In startTheProcess - Working on $it - Turning: off"
                            it.off()
                        }
                    } else if(theCMD == "c") {
                        tZone.each {
                            setLevelandColorHandler(theColor,theLevel)
                            if(logEnable) log.debug "In startTheProcess - Working on $it - setColor: $value"
                            it.setColor(value)
                        }
                    }
                    pauseExecution(theDelay.toInteger())
                }
                if(logEnable) log.debug "     - - - - - End Pattern (pass: $x) - - - - -     "
            }
            if(rZones) resetCurrentStatus()
        }
        if(turnTriggerOff) {
            if(logEnable) log.debug "In startTheProcess - Turning $cSwitch OFF"
            cSwitch.off()
        }
    }
}

def getCurrentStatus() {
    if(logEnable) log.debug "In getCurrentStatus (${state.version})"
    allDevices = [parent.zone1, parent.zone2, parent.zone3, parent.zone4, parent.zone5, parent.zone6, parent.zone7, parent.zone8, parent.zone9, parent.zone10, parent.zone11, parent.zone12, parent.zone13, parent.zone14, parent.zone15, parent.zone16].flatten().findAll{it}
    state.oldStatusMap = [:]
    if(allDevices) {
        allDevices.each {
            oldHueColor = it.currentValue("hue")
            oldSaturation = it.currentValue("saturation")
            oldLevel = it.currentValue("level")
            oldColorTemp = it.currentValue("colorTemperature")
            oldColorMode = it.currentValue("colorMode")
            name = (it.displayName).replace(" ","")
            status = it.currentValue("switch")
            oldStatus = "${status}::${oldHueColor}::${oldSaturation}::${oldLevel}::${oldColorTemp}::${oldColorMode}"
            state.oldStatusMap.put(name,oldStatus) 
            if(logEnable) log.debug "In getCurrentStatus - Saving Old Status - ${name} - ${oldStatus}"
        }
    }
}

def resetCurrentStatus(evt) {
    if(logEnable) log.debug "In resetCurrentStatus (${state.version})"
    allDevices = parent.masterswitchLevel
    for(x=0;x<2;x++) {
        allDevices.each { it ->
            name = (it.displayName).replace(" ","")
            try {
                //if(logEnable) log.debug "In resetCurrentStatus - Getting data for ${name}"
                theData = state.oldStatusMap.get(name)
                if(theData) {
                    data = theData.split("::")
                    oldStatus = data[0]
                    oldHueColor = data[1]
                    oldSaturation = data[2]
                    oldLevel = data[3]
                    oldColorTemp = data[4]
                    oldColorMode = data[5]
                    setLevelandColorHandler(oldHueColor,oldLevel)
                    //if(logEnable) log.debug "In resetCurrentStatus - Working on: $it.displayName - $value"
                    if(oldStatus == "off") {
                        it.off()
                    } else {
                        it.setColor(value)
                    }
                    pauseExecution(100)
                } else {
                    //if(logEnable) log.debug "In resetCurrentStatus - Found NO data"
                }
            } catch(e) {
                log.error(getExceptionMessageWithLine(e))
                if(logEnable) log.debug "In resetCurrentStatus - Oops, no DATA - Turning Off (${it.displayName})"
                pauseExecution(actionDelay)
                it.off()
            }
        }
    }
}

def setLevelandColorHandler(theColor,onLevel=null) {  
    //if(logEnable) log.debug "In setLevelandColorHandler - color: ${theColor} - onLevel: ${onLevel}"
    theColor = theColor.toLowerCase()
    switch(theColor) {
        case "na":
            hueColor = null
            saturation = null
            break;
        case "white":
            hueColor = 52
            saturation = 19
            break;
        case "daylight":
            hueColor = 53
            saturation = 91
            break;
        case "soft white":
            hueColor = 23
            saturation = 56
            break;
        case "warm white":
            hueColor = 20
            saturation = 80
            break;
        case "blue":
            hueColor = 70
            break;
        case "green":
            hueColor = 39
            break;
        case "yellow":
            hueColor = 12
            break;
        case "orange":
            hueColor = 10
            break;
        case "purple":
            hueColor = 75
            break;
        case "pink":
            hueColor = 83
            break;
        case "red":
            hueColor = 100
            break;
        case "cc1":
            hueColor = cc1hue.toInteger()
            saturation = cc1sat.toInteger()
            break;
        case "cc2":
            hueColor = cc2hue.toInteger()
            saturation = cc2sat.toInteger()
            break;
        case "cc3":
            hueColor = cc3hue.toInteger()
            saturation = cc3sat.toInteger()
            break;
        case "cc4":
            hueColor = cc4hue.toInteger()
            saturation = cc4sat.toInteger()
            break;
        case "cc5":
            hueColor = cc5hue.toInteger()
            saturation = cc5sat.toInteger()
            break;
        case "cc6":
            hueColor = cc6hue.toInteger()
            saturation = cc6sat.toInteger()
            break;
    }
    saturation = saturation ?: 100
    onLevel = onLevel ?: 50
    value = [hue: hueColor, saturation: saturation, level: onLevel.toInteger()]
    return value
}

def checkPatternHandler(evt) {
    if(logEnable) log.debug "In checkPatternHandler (${state.version})"
    theData = evt.value.replace("[","").replace("]","")
    if(logEnable) log.debug "In checkPatternHandler - theData: $theData"
    (theId, theCommand) = theData.split(",")
    if(theId.toString() == app.id.toString()) {
        if(logEnable) log.debug "In checkPatternHandler - MATCH!"
        startTheProcess()
    } else {
        if(logEnable) log.debug "In checkPatternHandler - NO MATCH!"
    }
}

HashMap securityLogin(){
    def result = false
    try{
        httpPost(
            [
                uri: "http://127.0.0.1:8080",
                path: "/login",
                query: 
                [
                    loginRedirect: "/"
                ],
                body:
                [
                    username: username,
                    password: password,
                    submit: "Login"
                ],
                textParser: true,
                ignoreSSLIssues: true
            ]
        )
        { resp ->
            //			log.debug resp.data?.text
            if (resp.data?.text?.contains("The login information you supplied was incorrect."))
            result = false
            else {
                cookie = resp?.headers?.'Set-Cookie'?.split(';')?.getAt(0)
                result = true
            }
        }
    } catch (e) {
        log.error "Error logging in: ${e}"
        result = false
        cookie = null
    }
    return [result: result, cookie: cookie]
}

Boolean fileExists(pName){
    if(logEnable) log.debug "In fileExists - ${pName}"
    uri = "http://${location.hub.localIP}:8080/local/${pName}";
    def params = [
        uri: uri
    ]
    try {
        httpGet(params) { resp ->
            if (resp != null){
                if(logEnable) log.debug "In fileExists - File Exists! ${pName}"
            } else {
                if(logEnable) log.debug "In fileExists - File DOES NOT Exist! ${pName}"
            }
        }
    } catch (e) {
        if(logEnable) log.debug "In fileExists - File DOES NOT Exist! ${pName}"
    }
}

String readFile(pName){
    fileExists(pName)
    if(logEnable) log.debug "In readFile - ${pName}"
    if(security) cookie = securityLogin().cookie
    uri = "http://${location.hub.localIP}:8080/local/${pName}"
    def params = [
        uri: uri,
        contentType: "text/html; charset=UTF-8",
        headers: [
            "Cookie": cookie
        ]
    ]
    try {
        httpGet(params) { resp ->
            if(resp!= null) {
                data = resp.getData().toString()
                if(logEnable) log.trace "In readFile - data: $data"             
                theData = data.replace("[","").replace("]","").split(", ")
                theData.each { it ->
                    (theKey, theValue) = it.split(":")
                    if(logEnable) log.debug "In readFile - theKey: ${theKey} - theValue: ${theValue}"
                    if(theKey.contains("group")) {
                        gData = theValue.split(";")
                        g=1
                        gData.each { gd ->
                            c = gd.replace("!",":").toString()
                            if(g==1) {
                                newValues = "[\"${c}\""
                            } else {
                                newValues += ",\"${c}\""
                            }
                            g+=1
                        }
                        newValues += "]"
                        if(logEnable) log.debug "In readFile - Sending $theKey:$newValues" 
                        app.updateSetting("${theKey}", [type: "enum", value:"${newValues}"])
                    } else {
                        if(logEnable) log.debug "In readFile - Sending $theKey:$theValue"
                        tKey = theKey.replace(" ","").trim()
                        tValue = theValue.replace(" ","").trim()
                        state.mPattern.put(tKey,tValue)
                    }
                }
            } else {
                if(logEnable) log.debug "In readFile - Data: NO DATA"
            }
        }
    } catch (e) {
        log.error "In readFile - Error: ${e}"
    }
}

Boolean writeFile(pName, fData) {
    if(logEnable) log.debug "Writing to file - ${pName} - ${fData}"
    if(security) cookie = securityLogin().cookie
	try {
		def params = [
			uri: "http://127.0.0.1:8080",
			path: "/hub/fileManager/upload",
			query: [
				"folder": "/"
			],
			headers: [
				"Cookie": cookie,
				"Content-Type": "multipart/form-data; boundary=----WebKitFormBoundaryDtoO2QfPwfhTjOuS"
			],
			body: """------WebKitFormBoundaryDtoO2QfPwfhTjOuS
Content-Disposition: form-data; name="uploadFile"; filename="${pName}"
Content-Type: text/plain

${fData}

------WebKitFormBoundaryDtoO2QfPwfhTjOuS
Content-Disposition: form-data; name="folder"


------WebKitFormBoundaryDtoO2QfPwfhTjOuS--""",
			timeout: 300,
			ignoreSSLIssues: true
		]
		httpPost(params) { resp ->	
		}
	} catch (e) {
        log.error "Error writing file $pName: ${e}"
	}
}

def makeTheTable() {
    if(logEnable) log.debug "In makeTheTable (${state.version})"
    mPatternSorted = state.mPattern.sort { a, b -> a.key <=> b.key }
    t=0
    state.theTable = "<table width=100%>"
    mPatternSorted.each { it ->
        theKey = it.key
        theValue = it.value
        if(theKey=="p01" || theKey=="p05" || theKey=="p09" || theKey=="p13" || theKey=="p17" || theKey=="p21" || theKey=="p25" || theKey=="p29" || theKey=="p33" || theKey=="p37") {
            state.theTable += "<tr><td><b>$theKey</b><br>$theValue"
        } else {
            state.theTable += "<td><b>$theKey</b><br>$theValue"
        }
    }
    state.theTable += "</table>"
}

def clearTheBoard() {
    if(logEnable) log.debug "In clearTheBoard (${state.version})"
    app.removeSetting("mData")
    app.removeSetting("pZG")
    app.removeSetting("pZGNum")
    app.removeSetting("pCommand")
    app.removeSetting("pColor")
    app.removeSetting("pLevel")
    app.removeSetting("pDelay")
    state.working = false    
}

def exportFileHandler() {
    if(logEnable) log.debug "In exportFileHandler (${state.version})"
    state.theGroups = [:]
    for(x=0;x<8;x++) {
        if(x==0) aGroup = group1
        if(x==1) aGroup = group2
        if(x==2) aGroup = group3
        if(x==3) aGroup = group4
        if(x==4) aGroup = group5
        if(x==5) aGroup = group6
        if(x==6) aGroup = group7
        if(x==7) aGroup = group8
        if(aGroup) {
            group = aGroup.toString().replace("[","").replace("]","").replace(" ","").split(",")
            if(logEnable) log.debug "In exportFileHandler - Working group: ${x+1} - ${group}"
            t = 1
            group.each { it ->
                if(logEnable) log.debug "In exportFileHandler - Working on:${it}"
                newIt = it.replace(":","!")
                if(t==1) {
                    theZ = "${newIt}"
                } else {
                    theZ += ";${newIt}"
                }
                t += 1
            }
            if(logEnable) log.debug "In exportFileHandler  - Exporting - group${x+1}:$theZ"
            state.theGroups.put("group${x+1}",theZ)
            theZ = null
        }
    }
    state.mPattern = state.mPattern.sort { a, b -> a.key <=> b.key }
    theValue = state.theGroups + mPat
    if(logEnable) log.debug "In exportFileHandler (${state.version})"
    writeFile(pName, theValue) 
}

def importFileHandler() {
    if(logEnable) log.debug "In importFileHandler (${state.version})"
    state.mPattern = [:]
    app.updateSetting("group1",[type:"enum", value:""])
    app.updateSetting("group2",[type:"enum", value:""])
    app.updateSetting("group3",[type:"enum", value:""])
    app.updateSetting("group4",[type:"enum", value:""])
    app.updateSetting("group5",[type:"enum", value:""])
    app.updateSetting("group6",[type:"enum", value:""])
    app.updateSetting("group7",[type:"enum", value:""])
    app.updateSetting("group8",[type:"enum", value:""])
    readFile(pName)
}

Boolean getFileList(){
    if(logEnable) log.debug "Getting list of files"
    uri = "http://${location.hub.localIP}:8080/hub/fileManager/json";
    def params = [
        uri: uri
    ]
    try {
        fileList = []
        httpGet(params) { resp ->
            if (resp != null){
                if(logEnable) log.debug "Found the files"
                def json = resp.data
                for (rec in json.files) {
                    fileType = rec.name[-3..-1]
                    if(fileType == "txt") {
                        fileList << rec.name
                    }
                }
            } else {
                //
            }
        }
        return fileList
    } catch (e) {
        log.error e
    }
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "*************************************************************************"
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "enterPattern") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        if(inputSwitch) {
            newPattern = "${mData}"
        } else {
            newPattern = "${pZG};${pZGNum};${pCommand};${pColor.toLowerCase()};${pLevel};${pDelay}".trim()
        }
        if(logEnable) log.debug "In appButtonHandler - Setting new pattern: (${pNumber}) - $newPattern"
        if(state.mPattern == null) state.mPattern = [:]
        state.mPattern.put("${pNumber}",newPattern)
        makeTheTable()
        clearTheBoard()
    }
    
    if(logEnable) log.debug "In appButtonHandler - Finished Working"
    buttonPressed = null
}

// ********** Normal Stuff **********

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def getImage(type) {					// Modified from @Stephack Code
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
}
