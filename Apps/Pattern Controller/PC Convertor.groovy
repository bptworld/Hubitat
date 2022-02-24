/**
 *  **************** Patterns Plus File Convertor Child App  ****************
 *
 *  Design Usage:
 *  Convert 1.0.x exported files to the new 1.1.x format.
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
 *  1.0.0 - 02/22/22 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat
import groovy.transform.Field

def setVersion(){
    state.name = "Patterns Plus File Convertor"
	state.version = "1.0.0"
}

definition(
    name: "Patterns Plus File Convertor Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Convert 1.0.x exported files to the new 1.1.x format.",
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
    		paragraph "Convert 1.0.x exported files to the new 1.1.x format."
		}   
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Import/Export Pattern")) {
            paragraph "Converts files from Pattern Controll to Patterns Plus"
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
            paragraph "<b>Import Pattern to Convert</b>"
            getFileList()
            input "pName", "enum", title: "File to Convert", options: fileList, mulitple:false, submitOnChange:true
            if(pName) {
                input "importPattern", "bool", title: "Import Pattern", submitOnChange:true, width: 3
                if(importPattern) {
                    readFile(pName)
                    app.updateSetting("importPattern",[type:"bool", value:"false"])
                }
                paragraph "<small>This happens instantly, once the button is pressed.</small>"
            }
            paragraph "Note: If you want to delete the file at any time. <a href='http://${location.hub.localIP}:8080/hub/fileManager' target=_blank>Click Here</a> to visit the File Manager."
            paragraph "<hr>"
            if(state.iData) {
                displayData = state.iData.replace(",", ", ")
                paragraph "<b>Old Data:</b> ${displayData}"
                exportPatternHandler()
                paragraph "<b>New Data:</b> ${state.mPattern}"
            } else {
                paragraph "<b>Old Data:</b>"
                paragraph "<b>New Data:</b>"
            }
            paragraph "<hr>"
            if(state.mPattern) {
                paragraph "<b>Export New Pattern</b>"
                input "newORold", "bool", title: "New File or Existing File", submitOnChange:true
                if(newORold) {
                    getFileList()
                    input "eName", "enum", title: "List of Files", options: fileList, multiple:false, submitOnChange:true
                } else {
                    input "eName", "text", title: "Name of file to Export <small>(ie. myPattern.txt)</small>", submitOnChange:true
                }
                if(eName) {
                    input "exportPattern", "bool", title: "Export Pattern", submitOnChange:true
                    paragraph "<small>Once the Export slider is pushed, it will write the file and the slider will return to off. It will be quick! Once it is back to off, the file is ready to be imported into 'Patterns Plus'.</small>"
                    paragraph "Remember, you will have to re-setup your groups once you inport this new pattern into 'Patterns Plus'."
                    if(exportPattern) {
                        writeFile(eName, state.mPattern)
                        displayData = ""
                        state.mPattern = [:]
                        state.iData = null
                        app?.updateSetting("exportPattern",[value:"false",type:"bool"])
                        app.updateSetting("newORold",[value:"false",type:"bool"])
                        app.removeSetting("pName")
                        app.removeSetting("eName")
                    }
                }
            }
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
        // nothing to do here
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
                state.iData = resp.getData().toString()
                if(logEnable) log.trace "In readFile - iData: $state.iData"
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

Boolean getFileList(){
    if(logEnable) log.debug "In getFileList - Getting list of files"
    uri = "http://${location.hub.localIP}:8080/hub/fileManager/json";
    def params = [
        uri: uri
    ]
    try {
        fileList = []
        httpGet(params) { resp ->
            if (resp != null){
                if(logEnable) log.debug "In getFileList - Found the files"
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
    if(logEnable) log.debug "In getFileList - Finished with the files"
}

def exportPatternHandler() {
    // OLD export
    // 1:zone1:zone8,2:zone2:zone7,3:zone3:zone6,4:zone4:zone5,5:null,6:null,7:null,8:null,
    // g1:c:green:25:1,g2:c:green:50:1,g3:c:green:75:1,g4:c:red:100:100,g4:off:n:n:100,g4:c:red:100:100,g4:off:n:n:100,g4:c:red:100:100,null,null,null
    
    // NEW export
    // [group1:1!LIFXStripDevice-Zone1;8!LIFXStripDevice-Zone8, p1:z;3;c;orange;50;1, p2:z;5;c;green;75;250, p3:g;1;c;red;50;250, p4:z;7;c;red;40;250]
    
    if(logEnable) log.debug "In exportPatternHandler (${state.version})"
    state.mPattern = [:]
    theData = state.iData.split(",")
    x = 1
    theData.each { it ->
        data = it.split(":")
        gzData = data[0]        
        firstL = data[0].substring(0, 1)
        if(firstL == "z" || firstL == "g") {
            //    g4:c:red:100:100
            // p1:z;3;c;orange;50;1
            gz = gzData.substring(0, 1)
            num = gzData.substring(1, 2)
            newData = "${gz};${num};${data[1]};${data[2]};${data[3]};${data[4]}"
            if(x<10) {
                pos = "p0${x}"
            } else {
                pos = "p${x}"
            }
            state.mPattern.put(pos, newData)
            x += 1
        }
    }
    if(logEnable) log.debug "In exportPatternHandler - mPattern: $state.mPattern"     
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "*************************************************************************"
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(buttonPressed == "exportPattern") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        exportPatternHandler()
    } else if(buttonPressed == "importPattern") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        state.mPattern = [:]
        readFile(pName)
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
