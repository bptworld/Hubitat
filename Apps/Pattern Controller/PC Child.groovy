/**
 *  **************** Pattern Controller Child App  ****************
 *
 *  Design Usage:
 *  Create any pattern you want using the zones associated with the Lifx Light Strip.
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
 *  1.0.0 - 02/14/22 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat
import groovy.transform.Field

def setVersion(){
    state.name = "Pattern Controller"
	state.version = "1.0.0"
}

definition(
    name: "Pattern Controller Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create any pattern you want using the zones associated with the Lifx Light Strip.",
    category: "Convenience",
	parent: "BPTWorld:Pattern Controller",
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
    		paragraph "Create any pattern you want using the zones associated with the Lifx Light Strip."
            paragraph "<b>Important Note:</b> Leave the parent device 'on'. Then control each zone with the child devices."
            paragraph "Each zone is individualy controlled using a set of commands. The command set looks like this:"
            paragraph "<b>theZone/group : theCMD : theColor : theLevel : theDelay</b>"
            paragraph "<b>theZone/Group:</b><br> - z# - ie. z3<br> - g# - ie. g1<br>* Zone 1 is closest to the power cord<br><b>theCMD:</b><br> - on - turn on the zone<br> - off - turn off the zone<br> - c - set the zone to a certain color<br><b>theColor:</b><br> - Available Colors: white, daylight, soft white, warm white, blue, green, yellow, orange, pink and red<br> - Custom colors coming soon<br><b>theLevel:</b> - Set the brightness level of the zone (1-100)<br><b>theDelay:</b> - Time in milliseconds to when the next command will be sent"
            paragraph "ie.<br><b>g2:c:green:50:1</b><br> - Group 2 : set color : color is green : brightness is 50 : next command sent in 1 millisecond"
            paragraph "<b>z4:c:red:100:100</b><br> - zone 1 : use color : color is red : brightness is 100 : next command sent in 100 milliseconds"
            paragraph "Play around with the options and you'll get the hang of it pretty quickly!"
            paragraph "<hr>"
            paragraph "<b>Warning:</b> As with ANY app, sending many commands too fast can and will slow down your hub."
		}   
        
        section("${getImage('optionsGreen')} <b>Zone Groups:</b>", hideable: true, hidden: true) {
            
            paragraph "Remember: Actual zones are setup in the parent app."
            if(parent.activeZones) {
                theZones = []
                aZone = parent.activeZones.toString().split(",")
                aZone.each { tz ->
                    theZones << tz
                }
            } else {
                paragraph "Please setup the zones within the parent app. Thanks."
                theZones = []
            }
            input "group1", "enum", title: "Select Group 1 (g1)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group2", "enum", title: "Select Group 2 (g2)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group3", "enum", title: "Select Group 3 (g3)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group4", "enum", title: "Select Group 4 (g4)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group5", "enum", title: "Select Group 5 (g5)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group6", "enum", title: "Select Group 6 (g6)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group7", "enum", title: "Select Group 7 (g7)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
            input "group8", "enum", title: "Select Group 8 (g8)", options: theZones, required:false, multiple:true, submitOnChange:true, width:6
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Pattern")) {
            paragraph "Pattern Command:<br>theZone/group : theCMD : theColor : theLevel : theDelay<br>ie. z1:c:blue:50:1 - g1:off:n:n:250<br><small>* See Information Section above for more!</small>"
            paragraph "<hr>"
            input "p1", "text", title: "1", submitOnChange:true, width:3
            input "p2", "text", title: "2", submitOnChange:true, width:3
            input "p3", "text", title: "3", submitOnChange:true, width:3
            input "p4", "text", title: "4", submitOnChange:true, width:3
            
            input "p5", "text", title: "5", submitOnChange:true, width:3
            input "p6", "text", title: "6", submitOnChange:true, width:3
            input "p7", "text", title: "7", submitOnChange:true, width:3
            input "p8", "text", title: "8", submitOnChange:true, width:3
            
            input "p9", "text", title: "9", submitOnChange:true, width:3
            input "p10", "text", title: "10", submitOnChange:true, width:3
            input "p11", "text", title: "11", submitOnChange:true, width:3
            input "p12", "text", title: "12", submitOnChange:true, width:3
            
            input "p13", "text", title: "13", submitOnChange:true, width:3
            input "p14", "text", title: "14", submitOnChange:true, width:3
            input "p15", "text", title: "15", submitOnChange:true, width:3
            input "p16", "text", title: "16", submitOnChange:true, width:3
            
            input "p17", "text", title: "17", submitOnChange:true, width:3
            input "p18", "text", title: "18", submitOnChange:true, width:3
            input "p19", "text", title: "19", submitOnChange:true, width:3
            input "p20", "text", title: "20", submitOnChange:true, width:3
            
            input "needMore", "bool", title: "Need More?", defaultValue:false, submitOnChange:true
            if(needMore) {
                input "p21", "text", title: "21", submitOnChange:true, width:3
                input "p22", "text", title: "22", submitOnChange:true, width:3
                input "p23", "text", title: "23", submitOnChange:true, width:3
                input "p24", "text", title: "24", submitOnChange:true, width:3

                input "p25", "text", title: "25", submitOnChange:true, width:3
                input "p26", "text", title: "26", submitOnChange:true, width:3
                input "p27", "text", title: "27", submitOnChange:true, width:3
                input "p28", "text", title: "28", submitOnChange:true, width:3

                input "p29", "text", title: "29", submitOnChange:true, width:3
                input "p30", "text", title: "30", submitOnChange:true, width:3
                input "p31", "text", title: "31", submitOnChange:true, width:3
                input "p32", "text", title: "32", submitOnChange:true, width:3

                input "p33", "text", title: "33", submitOnChange:true, width:3
                input "p34", "text", title: "34", submitOnChange:true, width:3
                input "p35", "text", title: "35", submitOnChange:true, width:3
                input "p36", "text", title: "36", submitOnChange:true, width:3

                input "p37", "text", title: "37", submitOnChange:true, width:3
                input "p38", "text", title: "38", submitOnChange:true, width:3
                input "p39", "text", title: "39", submitOnChange:true, width:3
                input "p40", "text", title: "40", submitOnChange:true, width:3
            }
            
            pattern = p1                // Loooooong way
            if(p2) pattern += "-$p2"
            if(p3) pattern += "-$p3"
            if(p4) pattern += "-$p4"
            if(p5) pattern += "-$p5"
            if(p6) pattern += "-$p6"
            if(p7) pattern += "-$p7"
            if(p8) pattern += "-$p8"
            if(p9) pattern += "-$p9"
            if(p10) pattern += "-$p10"
            if(p11) pattern += "-$p11"
            if(p12) pattern += "-$p12"
            if(p13) pattern += "-$p13"
            if(p14) pattern += "-$p14"
            if(p15) pattern += "-$p15"
            if(p16) pattern += "-$p16"
            if(p17) pattern += "-$p17"
            if(p18) pattern += "-$p18"
            if(p19) pattern += "-$p19"
            if(p20) pattern += "-$p20"
            if(needMore) {
                if(p21) pattern += "-$p21"
                if(p22) pattern += "-$p22"
                if(p23) pattern += "-$p23"
                if(p24) pattern += "-$p24"
                if(p25) pattern += "-$p25"
                if(p26) pattern += "-$p26"
                if(p27) pattern += "-$p27"
                if(p28) pattern += "-$p28"
                if(p29) pattern += "-$p29"
                if(p30) pattern += "-$p30"
                if(p31) pattern += "-$p31"
                if(p32) pattern += "-$p32"
                if(p33) pattern += "-$p33"
                if(p34) pattern += "-$p34"
                if(p35) pattern += "-$p35"
                if(p36) pattern += "-$p36"
                if(p37) pattern += "-$p37"
                if(p38) pattern += "-$p38"
                if(p39) pattern += "-$p39"
                if(p40) pattern += "-$p40"
            }
            
            state.pattern = pattern
            if(state.pattern) {
                input "testPattern", "button", title: "Test Pattern", width: 3
                input "resetZones", "button", title: "Reset Zones (OFF)", width: 3
            }
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
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
		}
		display2()
	}
}

def importExportPage() {
    dynamicPage(name: "importExportPage", title: "Import/Export Options", install:false, uninstall:false) {
		display() 
        section(getFormat("header-green", "${getImage("Blank")}"+" Import/Export Pattern")) {
            paragraph "Import/Export your favorite Patterns! Great for using the same pattern but changing up the colors/speed/etc. File includes all Groups and Pattern Data."
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
                input "pName", "text", title: "Name of file to Import <small>(Please do NOT include the .txt file extension)</small>", submitOnChange:true
                if(pName) {
                    input "importPattern", "button", title: "Import Pattern", width: 3
                }
                paragraph "Note: If you want to delete the file at any time. <a href='http://${location.hub.localIP}:8080/hub/fileManager' target=_blank>Click Here</a> to visit the File Manager."
            }
            
            if(ePattern) {
                paragraph "<hr>"
                paragraph "<b>Export Pattern</b>"
                input "pName", "text", title: "Name of file to Export <small>(Please do NOT include the .txt file extension)</small>", submitOnChange:true
                if(pName) {
                    input "exportPattern", "button", title: "Export Pattern", width: 3
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
        if(state.pattern) {
            log.trace "state.pattern: ${state.pattern}"
            getCurrentStatus()
            numOfPasses = numOfPasses ?: 1
            for(x=0;x < numOfPasses;x++) {
                if(logEnable) log.debug "     - - - - - Starting the Pattern (pass: $x) - - - - -     "
                pSplit = state.pattern.split("-")
                pSplit.each { it ->
                    tZone = []
                    stuff = it.replace(" ","")
                    (theZone,theCMD,theColor,theLevel,theDelay) = stuff.split(":")                  
                    if(theZone.contains("z")) {
                        theNum = theZone.drop(1)
                        tZone << parent."zone${theNum}"
                    }
                                       
                    if(theZone.contains("g")) {
                        theNum = theZone.drop(1)
                        theGroup = app."group${theNum}".toString()
                        for(tz=1;tz<17;tz++) {
                            if(theGroup.contains("zone$tz")) {
                                tZone << parent."zone${tz}"
                            }
                        }
                    }
                   
                    if(logEnable) log.debug "In startTheProcess - L379 - tZone: $tZone | $theCMD | $theColor | $theLevel | $theDelay"
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
                    log.warn "pause: $theDelay"
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
    allDevices = [parent.zone1, parent.zone2, parent.zone3, parent.zone4, parent.zone5, parent.zone6, parent.zone7, parent.zone8, parent.zone9, parent.zone10, parent.zone11, parent.zone12, parent.zone13, parent.zone14, parent.zone15, parent.zone16].flatten().findAll{it}
    for(x=0;x<2;x++) {
        allDevices.each { it ->
            name = (it.displayName).replace(" ","")
            try {
                if(logEnable) log.debug "In resetCurrentStatus - Getting data for ${name}"
                theData = state.oldStatusMap.get(name)
                if(theData) {
                    data = theData.split("::")
                    oldStatus = data[0]
                    oldHueColor = data[1]
                    oldSaturation = data[2]
                    oldLevel = data[3]
                    oldColorTemp = data[4]
                    oldColorMode = data[5]
                    value = [hue: oldHueColor, saturation: oldSaturation, level: oldLevel.toInteger()]
                    if(logEnable) log.debug "In resetCurrentStatus - Working on: $it.displayName - $value"
                    it.setColor(value)
                    if(oldStatus == "off") it.off()
                    pauseExecution(100)
                } else {
                    if(logEnable) log.debug "In resetCurrentStatus - Found NO data"
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
                
                theData = data.split(",")
                dSize = theData.size()
                for(x=0;x<8;x++) {
                    theValues = theData[x].split(":")
                    dataSize = theValues.size()
                    theGroup = theValues[0]
                    for(g=1;g<dataSize;g++) {
                        nV = theValues[g].replaceAll("[^a-zA-Z0-9]", "")
                        if(nV == "null") {
                            newValues = "["
                        } else {
                            if(g==1) newValues = "[\"${nV}\""
                            if(g>1) newValues += ",\"${nV}\""
                        }
                    }
                    newValues += "]"
                    if(logEnable) log.debug "In readFile - G - $theGroup: $newValues" 
                    app.updateSetting("group${theGroup}", [type: "enum", value:"${newValues}"])
                }

                for(p=8;p<48;p++) {
                    if(theData[p] == "null") {
                        if(logEnable) log.debug "In readFile - P - p${p-7}: ${theData[p]}" 
                        app.updateSetting("p${p-7}",[type:"text",value:""])
                    } else {
                        if(logEnable) log.debug "In readFile - P - p${p-7}: ${theData[p]}" 
                        app.updateSetting("p${p-7}",[type:"text",value:"${theData[p]}"])
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

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "*************************************************************************"
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "testPattern") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        startTheProcess()
    } else if(buttonPressed == "resetZones") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        if(parent.zone1) { parent.zone1.off() }
        if(parent.zone2) { parent.zone2.off() }
        if(parent.zone3) { parent.zone3.off() }
        if(parent.zone4) { parent.zone4.off() }
        if(parent.zone5) { parent.zone5.off() }
        if(parent.zone6) { parent.zone6.off() }
        if(parent.zone7) { parent.zone7.off() }
        if(parent.zone8) { parent.zone8.off() }
        if(parent.zone9) { parent.zone9.off() }
        if(parent.zone10) { parent.zone10.off() }
        if(parent.zone11) { parent.zone11.off() }
        if(parent.zone12) { parent.zone12.off() }
        if(parent.zone13) { parent.zone13.off() }
        if(parent.zone14) { parent.zone14.off() }
        if(parent.zone15) { parent.zone15.off() }
        if(parent.zone16) { parent.zone16.off() }
    } else if(buttonPressed == "exportPattern") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        if(logEnable) log.debug "In appButtonHandler - exportPattern - P"
        thePatterns = "$p1,$p2,$p3,$p4,$p5,$p6,$p7,$p8,$p9,$p10,$p11,$p12,$p13,$p14,$p15,$p16,$p17,$p18,$p19,$p20,$p21,$p22,$p23,$p24,$p25,$p26,$p27,$p28,$p29,$p30,$p31,$p32,$p33,$p34,$p35,$p36,$p37,$p38,$p39,$p40"
        
        if(logEnable) log.debug "In appButtonHandler - exportPattern - G"
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
                if(x==0) {
                    theGroups = "${x+1}"
                } else {
                    theGroups += ",${x+1}"
                }
                group = aGroup.toString().replace("[","").replace("]","").replace(" ","").split(",")
                if(logEnable) log.debug "In appButtonHandler - exportPattern - G - Working group: ${x+1} - ${group}"
                group.each { it ->
                    if(logEnable) log.debug "In appButtonHandler - exportPattern - G - Working on:${it}!"
                    theGroups += ":${it}"
                }
            } else {
                if(x==0) {
                    theGroups = "${x+1}:null"
                } else {
                    theGroups += ",${x+1}:null"
                }
            }
        }
 
        theName = "${pName}.txt"
        theValue = "${theGroups},${thePatterns}"
        writeFile(theName, theValue)        
        app?.updateSetting("ePattern",[type:"bool", value:"false"])
    } else if(buttonPressed == "importPattern") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"          
        theName = "${pName}.txt"
        readFile(theName)
        app?.updateSetting("iPattern",[type:"bool", value:"false"])
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
