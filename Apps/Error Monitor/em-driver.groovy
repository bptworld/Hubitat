/**
 *  ****************  Error Monitor Driver  ****************
 *
 *  Design Usage:
 *  This driver opens a webSocket to capture Log info.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 * ------------------------------------------------------------------------------------------------------------------------------
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
 *  1.0.1 - 03/31/22 - Adjustments
 *  1.0.0 - 03/25/22 - Initial release
 *
 */

import groovy.json.*

metadata {
	definition (name: "Error Monitor Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"
        capability "Initialize"
        capability "Switch"
        
        command "closeConnection"
        command "clearAllData"
        command "appStatus"
        command "clearLogData"
        command "resetCount"
        
        attribute "switch", "string"
        attribute "status", "string"
        attribute "bpt-lastLogMessage", "string"       
        attribute "bpt-logData", "string"        
        attribute "numOfCharacters", "number"
        attribute "keywords", "string"
        attribute "nKeywords", "string"
        attribute "level", "string"
        attribute "appStatus", "string"
        attribute "useSwitch", "string"
    }
    preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Error Monitor Driver</b>", description: ""
            input("disableConnection", "bool", title: "Disable Connection", required: true, defaultValue: false)
            input("fontSize", "text", title: "Font Size", required: true, defaultValue: "15")
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
        }
    }
}

def installed(){
    log.info "Error Monitor Driver has been Installed"
    clearAllData()
    initialize()
}

def updated() {
    log.info "Error Monitor Driver has been Updated"
    initialize()
}

def initialize() {
    if(logEnable) log.info "In initialize"
    if(disableConnection) {
        if(logEnable) log.info "Error Monitor Driver - webSocket Connection is Disabled in the Device"
    } else {
        if(logEnable) log.info "Error Monitor Driver - Connecting webSocket"
        interfaces.webSocket.connect("ws://localhost:8080/logsocket")
    }
}

void uninstalled() {
	interfaces.webSocket.close()
}

def closeConnection() {
    interfaces.webSocket.close()
    if(logEnable) log.warn "Error Monitor Driver - Closing webSocket"
}

def resetCount() {
    state.sameCount = 1
    if(logEnable) log.debug "Resetting the Same Count to 1"
}

def webSocketStatus(String socketStatus) {
    if(logEnable) log.debug "In webSocketStatus - socketStatus: ${socketStatus}"
	if(socketStatus.startsWith("status: open")) {
		if(logEnable) log.warn "Error Monitor Driver - Connected"
        sendEvent(name: "status", value: "connected", displayed: true)
        pauseExecution(500)
        state.delay = null
        return
	} else if(socketStatus.startsWith("status: closing")) {
		if(logEnable) log.warn "Error Monitor Driver - Closing connection"
        sendEvent(name: "status", value: "disconnected")
        return
	} else if(socketStatus.startsWith("failure:")) {
		if(logEnable) log.warn "Error Monitor Driver - Connection has failed with error [${socketStatus}]."
        sendEvent(name: "status", value: "disconnected", displayed: true)
        autoReconnectWebSocket()
	} else {
        if(logEnable) log.warn "WebSocket error, reconnecting."
        autoReconnectWebSocket()
	}
}

def autoReconnectWebSocket() {
    state.delay = (state.delay ?: 0) + 30    
    if(state.delay > 600) state.delay = 600
    if(logEnable) log.warn "Error Monitor Driver - Connection lost, will try to reconnect in ${state.delay} seconds"
    runIn(state.delay, initialize)
}

def parse(String description) {
    def aStatus = device.currentValue('appStatus')
    if(aStatus == "active") {
        theData = "${description}"
        // This is what the incoming data looks like
        //{"name":"Error Monitor","msg":"Error Monitor Driver - Connected","id":365,"time":"2019-11-24 10:05:07.518","type":"dev","level":"warn"}
        // name, msg, id, time, type, level 
        if(state.sameCount == null) state.sameCount = 1
        def message =  new JsonSlurper().parseText(theData)                      
        theLevel = message.level.toLowerCase()
        if(theLevel == "error") {
            theName = message.name
            theMsg = message.msg.toLowerCase()
            if(state.lastMsg == null) state.lastMsg = "-"
            if(message.msg == state.lastMsg) {
                if(logEnable) log.info "New message is the same as last message, so skipping!"
                if(state.sameCount >= 11) {
                    log.warn "************************************************************"
                    log.warn "Error Monitor is CLOSING its connection!"
                    log.warn "Please fix the error, then click the 'resetCount' in the data device before turning the connection back on."
                    log.warn "************************************************************"
                    closeConnection()
                    app?.updateSetting("closeConnection",[value:"true",type:"bool"])
                } else {
                    state.sameCount += 1
                }
            } else {
                device.on()
                state.lastMsg = message.msg
                makeList(theName, message.msg)
            }
        }
    }
}

def on() {
    sendEvent(name: "switch", value: "on", displayed: true)
}

def off() {
    sendEvent(name: "switch", value: "off", displayed: true)
}

def makeList(theName,theMsg) {
    if(logEnable) log.debug "In makeList - working on - theName: ${theName} - ${theMsg}"
    try {
        if(state.list == null) state.list = []
        getDateTime()
        last = "${theName}::${newDate}::${theMsg}"
        state.list.add(0,last)  

        if(logEnable) log.debug "In makeList - added to list - last: ${last}"
        
        if(state.list) {
            listSize1 = state.list.size()
        } else {
            listSize1 = 0
        }

        if(logEnable) log.debug "In makeList - listSize1: ${listSize1}"        
        int intNumOfLines = 10
        if (listSize1 > intNumOfLines) state.list.removeAt(intNumOfLines)        
        String result1 = state.list.join(",")
        def lines = result1.split(",")

        theData = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px'><tr><td width=20%><td width=1%><td width=10%><td width=1%><td width=68%>"
        
        for (i=0;i<intNumOfLines && i<listSize1;i++) {
            combined = theData.length() + lines[i].length() + 16
            if(combined < 1000) {
                def (theApp, theTime, theLMsg) = lines[i].split("::") 
                theData += "<tr><td>${theApp} <td> - <td>${theTime}<td> - <td>${theLMsg}"
            }
        }
        
        theData += "</table></div>"
        dataCharCount1 = theData.length()
        if(dataCharCount1 <= 1024) {
            if(logEnable) log.debug "Error Monitor Attribute - theData - ${dataCharCount1} Characters"
        } else {
            theData = "Error Monitor - Too many characters to display on Dashboard (${dataCharCount1})"
        }
        
        sendEvent(name: "bpt-logData", value: theData, displayed: true)
        sendEvent(name: "numOfCharacters", value: dataCharCount1, displayed: true)
        sendEvent(name: "bpt-lastLogMessage", value: theMsg, displayed: true)
    }
    catch(e) {
        log.warn "Error Monitor Driver - In makeList - There was an error within Error Monitor Driver!"
        log.warn(getExceptionMessageWithLine(e))  
    }
}

def appStatus(data){
	if(logEnable) log.debug "Error Monitor Driver - In appStatus"
    sendEvent(name: "appStatus", value: data, displayed: true)
}

def clearAllData(){
	if(logEnable) log.debug "Error Monitor Driver - Clearing ALL data"
    theMsg = "-"
    logCharCount = "0"   
    state.clear()
    state.list = []
    sendEvent(name: "bpt-logData", value: state.list, displayed: true)	
    sendEvent(name: "bpt-lastLogMessage", value: theMsg, displayed: true)
    sendEvent(name: "numOfCharacters", value: logCharCount, displayed: true)
    
}

def clearLogData(){
	if(logEnable) log.debug "Error Monitor Driver - Clearing the Log Data"
    state.list = []
    sendEvent(name: "bpt-logData", value: state.list, displayed: true)
    sendEvent(name: "numOfCharacters", value: "", displayed: true)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newDate=date.format("MM-d HH:mm")
	if(hourType == true) newDate=date.format("MM-d hh:mm")
    return newDate
}
