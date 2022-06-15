/**
 *  **************** Bundle Manager HPM ****************
 *  Design Usage:
 *  The place to explore Bundles. Find, install and update bundles quickly and easily.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums! Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  1.0.0 - 06/08/22 - Initial release.
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Bundle Manager HPM"
	state.version = "1.0.0"
    //sendLocationEvent(name: "updateVersionInfo", value: "${state.name}:${state.version}")
}

definition(
    name: "Bundle Manager HPM",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "The place to explore Bundles. Find, install and update bundles quickly and easily.",
    tags: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
    singleInstance: true
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        display()
        state.theStatus = false
        section(getFormat("header-green", "${getImage("Blank")}"+" Install Bundle Manager")) {
            getAppsList()
            if(state.allAppsList) {
                bValue = "Bundle Manager"
                state.allAppsList.each { al ->
                    if(al.title == bValue) {
                        theID = al.id
                        state.theStatus = true
                    }
                }
            }
            if(logEnable) log.debug "In installBM (${state.version}) - App ID was found: ${theID}"
            if(theID) {
                paragraph "Welcome to Bundle Manager! The version on HPM is a place holder to make it easy to find and install."
                paragraph "Looks like you already have the full version of Bundle Manager Installed!"
                paragraph "Nothing to see here, move along!"
            } else {                        
                paragraph "Welcome to Bundle Manager! The version on HPM is a place holder to make it easy to find and install. Now it's time to download and install the full version!"
                paragraph "When you're ready, toggle the switch below. The switch will turn back off when finished."

                input "firstInstall", "bool", title: "Install the Full Bundle Manager", defaultValue:false, submitOnChange:true
                if(firstInstall) {
                    bURL = "https://github.com/bptworld/Hubitat/raw/master/Bundles/BundleManager.zip"
                    installBundleHandler(bURL)
                    app.updateSetting("firstInstall",[value:"false",type:"bool"])
                }
                if(state.theStatus) {
                    getAppsList()
                    bValue = "Bundle Manager"
                    if(state.allAppsList) {
                        state.allAppsList.each { al ->
                            log.trace "$al.title -VS- $bValue"
                            if(al.title == bValue) theID = al.id
                        }
                        if(logEnable) log.debug "In installBM (${state.version}) - App ID: ${theID}"
                        if(theID) {
                            if(logEnable) log.debug "In installBM - Creating config link for ${bValue} (${theID})"
                            paragraph "<b>Success!</b> Now that the full version is installed, it's time to set it up!"
                            paragraph "<a href='/installedapp/create/${theID}'>CLICK HERE</a> to configure ${bValue}"
                        } else {
                            paragraph "There was an issue getting the app ID (${theID})"
                        }
                    }
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            input "hubSecurity", "bool", title: "Hub Security", submitOnChange:true, width:4
            if(hubSecurity) {
                input "hubUsername", "string", title: "Hub Username", required:true, submitOnChange:true, width:4
                input "hubPassword", "password", title: "Hub Password", required:true, submitOnChange:true, width:4
            } else {
                paragraph " ", width:8
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
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
        subscribe(location, "updateVersionInfo", updateVersionHandler)
    }
}

def login() {        // Modified from code by @dman2306
    if(logEnable) log.debug "In installBundleHandler - Checking Hub Security"
    state.cookie = ""
    if(hubSecurity) {
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
                        username: hubUsername,
                        password: hubPassword,
                        submit: "Login"
                    ],
                    textParser: true,
                    ignoreSSLIssues: true
                ]
            )
            { resp ->
                if (resp.data?.text?.contains("The login information you supplied was incorrect.")) {
                    log.warn "Bundle Manager - username/password is incorrect."
                } else {
                    state.cookie = resp?.headers?.'Set-Cookie'?.split(';')?.getAt(0)
                }
            }
        } catch (e) {
            log.error(getExceptionMessageWithLine(e))
        }
    }
}

def installBundleHandler(bundle) {
    login() 
    def jsonData =  JsonOutput.toJson([url:"$bundle",installer:FALSE, pwd:''])
    try {
        def params = [
            uri: 'http://127.0.0.1:8080/bundle/uploadZipFromUrl',
            headers: [
                "Accept": '*/*',
                "ContentType": 'text/plain; charset=utf-8',
                "Cookie": state.cookie
            ],
            body: "$jsonData",
            timeout: 180,
            ignoreSSLIssues: true
        ]
        if(logEnable) log.debug "In installBundleHandler - Getting data ($params)"
        httpPost(params) { resp ->
            if(logEnable) log.debug "In installBundleHandler - Receiving file: ${bundle}"
            state.theStatus = true
        }
        if(logEnable) log.debug "In installBundleHandler (${state.version}) - Finished"
   } catch (e) {
        log.error(getExceptionMessageWithLine(e))
        state.theStatus = false
   }
}

def getAppsList() {        // Modified from code by gavincampbell
    login() 
    //if(logEnable) log.debug "In getAppsList (${state.version}) - Getting installed Apps list"
	def params = [
		uri: "http://127.0.0.1:8080/app/list",
		textParser: true,
		headers: [
			Cookie: state.cookie
		]
	  ]
	
	def allAppsList = []
    def allAppNames = []
	try {
		httpGet(params) { resp ->     
			def matcherText = resp.data.text.replace("\n","").replace("\r","")
			def matcher = matcherText.findAll(/(<tr class="app-row" data-app-id="[^<>]+">.*?<\/tr>)/).each {
				def allFields = it.findAll(/(<td .*?<\/td>)/) // { match,f -> return f } 
				def id = it.find(/data-app-id="([^"]+)"/) { match,i -> return i.trim() }
				def title = allFields[0].find(/title="([^"]+)/) { match,t -> return t.trim() }
				allAppsList += [id:id,title:title]
                allAppNames << title
			}
		}
	} catch (e) {
		log.error "Error retrieving installed apps: ${e}"
        log.error(getExceptionMessageWithLine(e))
	}
    state.allAppsList = allAppsList
    state.allAppNames = allAppNames.sort { a, b -> a.toLowerCase() <=> b.toLowerCase() }
}

// *************************************************
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    setVersion()
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            theStatus = it.currentValue("switch")
            if(theStatus == "on") { state.eSwitch = true }
        }
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}"
    }
}

def getImage(type) {					// Modified code from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=33>"
    if(type == "checkMarkGreen2") return "${loc}checkMarkGreen2.png height=20 width=22>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified code from @Stephack
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    section (getFormat("title", "${getImage("logo")}" + " ${state.name}")) {
        paragraph "<div style='color:#000000;text-align:left;font-size:18px;font-weight:bold'><i>The place</i> to explore Bundles. Find, install and update bundles quickly and easily.</div>"
        paragraph getFormat("line")
    }
}

def display2() {
    section() {
        paragraph getFormat("line")
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
        paragraph "${state.footerMessage}"
    }
}
