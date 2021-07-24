/**
 *  ****************  Event Watchdog for Event Engine Driver  ****************
 *
 *  Design Usage:
 *  This driver opens a webSocket to capture Event info.
 *
 *  Copyright 2021 Bryan Turcotte (@bptworld)
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
 *  Special thanks to @dan.t and his sample code for making the websocket connection.
 *
 *  Changes:
 *
 *  1.0.0 - 07/23/21 - Initial Release
 *
 */

import groovy.json.*
    
metadata {
	definition (name: "Event Watchdog for EE Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Event%20Engine/EE-EWDriver.groovy") {
   		capability "Actuator"
        capability "Initialize"
        
        command "close"
        command "clearData"
        command "clearList"
        command "keywordInfo"
        command "appStatus"
       
        attribute "watching", "string"
        attribute "status", "string"
        attribute "bpt-lastEventMessage", "string"       
        attribute "bpt-eventData", "string"        
        attribute "numOfCharacters", "number"
        attribute "keywordInfo", "string"
        attribute "appStatus", "string"
    }
    preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Event Watchdog for EE Driver</b>", description: "For use with Event Engine."
            input("disableConnection", "bool", title: "Disable Connection", required: true, defaultValue: false)
            input("displayNameMessage", "bool", title: "Display Device Name in Message", required: true, defaultValue: false)
            input("fontSize", "text", title: "Font Size", required: true, defaultValue: "15")
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
            input("eventDebugEnable", "bool", title: "Enable Event Trace", required: true, defaultValue: false)
            input("logDebugEnable", "bool", title: "Enable Log Trace", required: true, defaultValue: false)
        }
    }
}

def installed(){
    log.info "Event Watchdog for EE Driver has been Installed"
    clearData()
    initialize()
}

def updated() {
    log.info "Event Watchdog for EE Driver has been Updated"
    initialize()
}

def initialize() {
    log.info "In initialize"
    state.version = "1.0.0"
    if(disableConnection) {
        log.info "Event Watchdog for EE Driver - webSocket Connection is Disabled by the Device"
    } else {
        log.info "Event Watchdog for EE Driver - Connecting webSocket - (${state.version})"
        watch = device.currentValue("watching")
        if(watch == "event") {
            interfaces.webSocket.connect("ws://localhost:8080/eventsocket")
        } else {
            interfaces.webSocket.connect("ws://localhost:8080/logsocket")
        }
    }
}

void uninstalled() {
	interfaces.webSocket.close()
}

def close() {
    interfaces.webSocket.close()
    log.warn "Event Watchdog for EE Driver - Closing webSocket"
}

def webSocketStatus(String socketStatus) {
    if(logEnabled) log.debug "In webSocketStatus - socketStatus: ${socketStatus}"
	if(socketStatus.startsWith("status: open")) {
		log.warn "Event Watchdog for EE Driver - Connected - (${state.version})"
        sendEvent(name: "status", value: "connected", displayed: true)
        pauseExecution(500)
        state.delay = null
        return
	} else if(socketStatus.startsWith("status: closing")) {
		log.warn "Event Watchdog for EE Driver - Closing connection - (${state.version})"
        sendEvent(name: "status", value: "disconnected")
        return
	} else if(socketStatus.startsWith("failure:")) {
		log.warn "Event Watchdog for EE Driver - Connection has failed with error [${socketStatus}]. - (${state.version})"
        sendEvent(name: "status", value: "disconnected", displayed: true)
        autoReconnectWebSocket()
	} else {
        log.warn "WebSocket error, reconnecting."
        autoReconnectWebSocket()
	}
}

def autoReconnectWebSocket() {
    state.delay = (state.delay ?: 0) + 30    
    if(state.delay > 600) state.delay = 600

    log.warn "Event Watchdog for EE Driver - Connection lost, will try to reconnect in ${state.delay} seconds - (${state.version})"
    runIn(state.delay, initialize)
}

def keywordInfo(keys) {
    if(eventDebugEnable || logDebugEnable) log.trace "In keywordInfo"    
    if(keys) {
        def (ewKeyword1, ewKeyword2, ewKeyword3) = keys.split(":")
        if(eventDebugEnable) log.trace "In keywordInfo (${state.version}) - ${ewKeyword1} - AND - ${ewKeyword2} - BUT NOT - ${ewKeyword3}"
        if(ewKeyword1) state.ewKeyword1 = ewKeyword1.toLowerCase()
        if(ewKeyword2) state.ewKeyword2 = ewKeyword2.toLowerCase()
        if(ewKeyword3) state.ewKeyword3 = ewKeyword3.toLowerCase()
    }
}

def parse(String description) {
    def aStatus = device.currentValue('appStatus')
    if(aStatus == "active" && !disableConnection) {
        newLine = "${description}"
        def message = new JsonSlurper().parseText(newLine)
        theLine = newLine.toString().toLowerCase()
        watch = device.currentValue("watching")
        
        // This is what the incoming EVENT looks like
        //{"source":"DEVICE","name":"contact","displayName":"Kitchen Freezer Door Sensor","value":"open","unit":null,"deviceId":157,"hubId":null,"locationId":null,"installedAppId":null,"descriptionText":"Kitchen Freezer Door Sensor was opened"}
        
        // This is what the incoming LOG looks like
        //{"name":"Log Watchdog","msg":"Log Watchdog Driver - Connected","id":365,"time":"2019-11-24 10:05:07.518","type":"dev","level":"warn"}
        
/*        // To Test
        if(theLine.contains("error")) {
            if(eventDebugEnable) {
                theLinea = theLine.replace("a","A").replace("e","E").replace("i","I").replace("o","O",).replace("u","U")
                log.trace "-----------------------------------------------------------------------------------"
                log.trace "In keyword (${state.version}) - Found NAME - sourceToCheck: ${sourceToCheck}"
                log.trace "theLinea: $theLinea"
            }
        }
        // End Test
*/
        if(theLine && state.ewKeyword1) {
            readyToGo = false
            try {
                keywords1 = state.ewKeyword1.split(";")
                keywords1.each {
                    if(theLine.contains(it)) {
                        readyToGo = true
                    }
                }

                if(readyToGo) {
                    msgToChecka = theLine.replace("a","A").replace("a","A").replace("e","E").replace("i","I").replace("o","O",).replace("u","U")
                    if(eventDebugEnable) log.trace "readyToGo - Message to Check - theLine: ${msgToChecka}"
                    if(eventDebugEnable) log.trace "readyToGo - Keywords to Check - state.ewKeyword2: ${state.ewKeyword2}"
                    if(eventDebugEnable) log.trace "readyToGo - Not Keywords to Check - state.ewKeyword3: ${state.ewKeyword3}"
                    if(state.ewKeyword2 != "null" && theLine) {
                        state.kCheck1Count = 0
                        if(eventDebugEnable) log.trace "In Secondary Keywords - Checking for ${state.ewKeyword2} - IN - ${msgToChecka}"

                        keywords2 = state.ewKeyword2.split(";")
                        keywords2.each {
                            if(theLine.contains(it)) {
                                if(eventDebugEnable) log.trace "In Secondary Keywords - ${it} Found! That's GOOD!"
                                state.kCheck1Count += 1
                            } else {
                                if(eventDebugEnable) log.trace "In Secondary Keywords - ${it} NOT Found! Next!"
                            }
                        }

                        if(state.kCheck1Count == 0) {
                            if(eventDebugEnable) log.trace "In Secondary Keywords - None Found! That's BAD! - Moving on."
                            state.kCheck1 = false
                        } else {
                            state.kCheck1 = true
                        }
                    } else {
                        state.kCheck1 = true
                    }

                    if(state.ewKeyword3 != "null" && theLine) { 
                        state.kCheck2Count = 0
                        if(eventDebugEnable) log.trace "In Secondary NOT Keywords - Checking for ${state.ewKeyword3}"

                        keywords3 = state.ewKeyword3.split(";")
                        keywords3.each {
                            if(theLine.contains(it)) {
                                if(eventDebugEnable) log.trace "In Not Keyword3 - ${it} found! That's BAD! - Moving on."
                                state.kCheck2Count += 1
                            }
                        }

                        if(state.kCheck2Count == 0) {
                            if(eventDebugEnable) log.trace "In Not Keyword1 - None found - That's GOOD!"
                            state.kCheck2 = true
                        } else {
                            state.kCheck2 = false
                        }
                    } else {
                        state.kCheck2 = true
                    }
                    if(eventDebugEnable) log.trace "In keyword - ${keyword1a} - kCheck1: ${state.kCheck1} - kCheck2: ${state.kCheck2}"

                    if(state.kCheck1 && state.kCheck2) {
                        if(eventDebugEnable) log.trace "In keyword (${state.version}) - ${keyword1a} - Everything Passed!"
                        if(watch == "event") {
                            if(displayNameMessage) {
                                newText = "${message.displayName} - ${message.descriptionText}"
                            } else {
                                newText = "${message.descriptionText}"
                            }
                        } else {
                            if(displayNameMessage) {
                                newText = "${message.name} - ${message.msg}"
                            } else {
                                newText = "${message.msg}"
                            }
                        }
                        if(newText == state.oldText) {
                            if(eventDebugEnable) log.trace "newText = oldText - Not adding to list."
                        } else {
                            if(nexText != "null") {
                                makeList(newText)
                                state.oldText = newText
                            } else {
                                if(eventDebugEnable) log.trace "newText is null - Not adding to list."
                            }
                        }
                    }
                }
            } catch (e) {
                log.error(getExceptionMessageWithLine(e))
            }
        }
    }
}
 
def makeList(data) {
    def watch = device.currentValue("watching")
    if(watch == "event") {
        msgValue = data
    } else {
        msgValue = data.toUpperCase()
    }
    if(eventDebugEnable || logDebugEnable) log.trace "In makeList (${state.version}) - ${watch} - Working on - ${msgValue}"
    try {
        if(state.list == null) state.list = []
        getDateTime()
        last = "${newDate}::${msgValue}"
        state.list.add(0,last)  
        if(state.list) {
            listSize1 = state.list.size()
        } else {
            listSize1 = 0
        }

        int intNumOfLines = 10
        if (listSize1 > intNumOfLines) state.list.removeAt(intNumOfLines)
        String result = state.list.join(",")
        def lines = result.split(",")

        theData = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px'><tr><td width=10%><td width=1%><td width=89%>"

        for (i=0;i<intNumOfLines && i<listSize1;i++) {
            combined = theData.length() + lines[i].length() + 16
            if(combined < 1000) {             
                def (theTime, theMsg) = lines[i].split("::") 
                theData += "<tr><td>${theTime}<td> - <td>${theMsg}"
            }
        }

        theData += "</table></div>"
        if(eventDebugEnable || logDebugEnable) log.debug "theData - ${theData.replace("<","!")}"       

        dataCharCount1 = theData.length()
        if(dataCharCount1 <= 1024) {
            if(eventDebugEnable || logDebugEnable) log.debug "Event Watchdog Attribute - theData - ${dataCharCount1} Characters"
        } else {
            theData = "Event Watchdog for EE - Too many characters to display on Dashboard (${dataCharCount1})"
        }

        sendEvent(name: "bpt-eventData", value: theData, displayed: true)
        sendEvent(name: "numOfCharacters", value: dataCharCount1, displayed: true)
        sendEvent(name: "bpt-lastEventMessage", value: msgValue, isStateChange: true)
        theData = null
        msgValue = null
    }
    catch(e) {
        log.error(getExceptionMessageWithLine(e)) 
    }
}

def appStatus(data){
	if(eventDebugEnable || logDebugEnable) log.debug "Event Watchdog Driver for EE - In appStatus"
    sendEvent(name: "appStatus", value: data, displayed: true)
}

def clearData(){
	if(eventDebugEnable || logDebugEnable) log.debug "Log Watchdog Driver for EE - Clearing the data"
    state.clear()
    msgValue = "-"
    logCharCount = "0"
    state.list = null
    sendEvent(name: "bpt-eventData", value: state.list, displayed: true)	
    sendEvent(name: "bpt-lastEventMessage", value: msgValue, isStateChange: true)
    sendEvent(name: "numOfCharacters", value: logCharCount, displayed: true)
}

def clearList(){
	if(eventDebugEnable || logDebugEnable) log.debug "Log Watchdog Driver for EE - Clearing the list"
    state.list = null
    sendEvent(name: "bpt-eventData", value: state.list, displayed: true)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newDate=date.format("MM-d HH:mm")
	if(hourType == true) newDate=date.format("MM-d hh:mm")
    return newDate
}
