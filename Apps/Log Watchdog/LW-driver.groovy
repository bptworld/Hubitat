/**
 *  ****************  Log Watchdog Driver  ****************
 *
 *  Design Usage:
 *  This driver opens a webSocket to capture Log info.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
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
 *  Special thanks to @dan.t for his sample code for making the websocket connection.
 *
 *  Changes:
 *
 *  1.0.8 - 06/24/20 - Tons of little changes
 *  1.0.7 - 10/08/19 - Now handles only 1 keyset per child app
 *  1.0.6 - 09/07/19 - Added some error catching
 *  1.0.5 - 09/05/19 - Getter better!
 *  1.0.4 - 09/05/19 - Trying some new things
 *  1.0.3 - 09/03/19 - Added 'does not contain' keywords
 *  1.0.2 - 09/02/19 - Evolving fast!
 *  1.0.1 - 09/01/19 - Major changes to the driver
 *  1.0.0 - 08/31/19 - Initial release
 */

def setVersion(){
    appName = "Log Watchdog Driver"
	version = "1.0.8" 
}

metadata {
	definition (name: "Log Watchdog Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Log%20Watchdog/LW-driver.groovy") {
   		capability "Actuator"
       
        attribute "status", "string"
        attribute "bpt-lastLogMessage", "string"       
        attribute "bpt-logData", "string"        
        attribute "numOfCharacters", "number"
        attribute "keywordInfo", "string"
        
        command "connect"
        command "close"
        command "clearData"
        command "keywordInfo"
    }
    preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Log Watchdog Driver</b>", description: "ONLY click 'Clear Data' to clear the message data."
            input("fontSize", "text", title: "Font Size", required: true, defaultValue: "15")
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
            input("logEnable", "bool", title: "Enable logging", required: true, defaultValue: false)
            input("traceEnable", "bool", title: "Enable Trace", required: true, defaultValue: false)
        }
    }
}

def installed(){
    log.info "Log Watchdog Driver has been Installed"
    clearData()
    initialize()
}

def updated() {
    log.info "Log Watchdog Driver has been Updated"
    initialize()
}

def initialize() {
    setVersion()
    log.info "In initialize"
}

def connect() {
	interfaces.webSocket.connect("ws://localhost:8080/logsocket")
}

def close() {
    interfaces.webSocket.close()
}

def webSocketStatus(String socketStatus) {
	if(socketStatus.startsWith("status: open")) {
		log.warn "Log Watchdog Driver - Connected"
        sendEvent(name: "status", value: "Open", displayed: true)
		return
	} 
	else if(socketStatus.startsWith("status: closing")) {
		log.warn "Log Watchdog Driver - Closing connection"
        sendEvent(name: "status", value: "Closing", displayed: true)
		return
	} 
	else if(socketStatus.startsWith("failure:")) {
		log.warn "Log Watchdog Driver - Connection has failed with error [${socketStatus}]."
        sendEvent(name: "status", value: "Failed", displayed: true)
	} 
	else {
		log.warn "Log Watchdog Driver - Connection has been lost due to an unknown error"
        sendEvent(name: "status", value: "Lost", displayed: true)
	}
}

def keywordInfo(keys) {
    if(traceEnable) log.trace "In keywordInfo"
    
    def (keySet,keySetType,keyword1,sKeyword1,sKeyword2,sKeyword3,sKeyword4,nKeyword1,nKeyword2) = keys.split(";")
    
    state.keyValue = "${keySetType};${keyword1};${sKeyword1};${sKeyword2};${sKeyword3};${sKeyword4};${nKeyword1};${nKeyword2}"

    if(traceEnable) log.trace "In keywordInfo - Recieved ${keySet}"
    if(traceEnable) log.trace "In keywordInfo - keyValue: ${state.keyValue}"
}

def parse(String description) {
    theData = "${description}"
    //log.info "${theData}"
    // This is what the incoming data looks like
    //{"name":"Log Watchdog","msg":"Log Watchdog Driver - Connected","id":365,"time":"2019-11-24 10:05:07.518","type":"dev","level":"warn"}
    
    def (name, msg, id, time, type, level) = theData.split(",")
    def (nameKey, nameValue) = name.split(":")
    def (msgKey, msgValue) = msg.split(":")
    def (lvlKey, lvlValue) = level.split(":")
    
    msgValue = msgValue.replace("\"","")
    nameValue = nameValue.replace("\"","")
    msgCheck = msgValue.toLowerCase()
    
    lvlValue = lvlValue.replace("\"","").replace("}","")
    lvlCheck = lvlValue.toLowerCase()
 
// *****************************
    try {
        def match = "no"
        def keyValue = state.keyValue.toLowerCase()
        def (keySetType,keyword1,sKeyword1,sKeyword2,sKeyword3,sKeyword4,nKeyword1,nKeyword2) = keyValue.split(";")
        if(keyword1 == "-") keyword1 = ""
        if(sKeyword1 == "-") sKeyword1 = ""
        if(sKeyword2 == "-") sKeyword2 = ""
        if(sKeyword3 == "-") sKeyword3 = ""
        if(sKeyword4 == "-") sKeyword4 = ""
        if(nKeyword1 == "-") nKeyword1 = ""
        if(nKeyword2 == "-") nKeyword2 = ""
        lCheck1 = "no"
        lCheck2 = "no"
        lCheck3 = "no"
        kCheck1 = "no"
        kCheck2 = "no"
        kCheck3 = "no"
        
    // -- check 1 start    
            if((keySetType == "l") && (lvlCheck.contains("${keyword1}"))) {
//log.info "keySetType = l"
                if(traceEnable) {
                    keyword1a = keyword1.replace("a","@").replace("e","3").replace("i","1").replace("o","0",).replace("u","^")
                    log.trace "In level - Found lvlCheck: ${keyword1a}"
                }
                lCheck1 = "yes"
    // -- check 1 done 
    // -- check 2 start
                if(sKeyword1 || sKeyword2 || sKeyword3 || sKeyword4) {
                    if(msgCheck.contains("${sKeyword1}") || msgCheck.contains("${sKeyword2}") || msgCheck.contains("${sKeyword3}") || msgCheck.contains("${sKeyword4}")) {
                        if(traceEnable) log.trace "In level: ${keyword1a} - Passed keywords"
                        lCheck2 = "yes"
                    }
                } else {
                    if(traceEnable) log.trace "In level: ${keyword1a} - Passed keywords"
                    lCheck2 = "yes"
                }
    // -- check 2 done 
    // -- check 3 start            
                if(nKeyword1 || nKeyword2) {
                    if(!msgCheck.contains("${nKeyword1}") || !msgCheck.contains("${nKeyword2}")) {
                        if(traceEnable) log.trace "In level: ${keyword1a} - Passed NOT contain"
                    l    Check3 = "yes"
                     }
                } else {
                     if(traceEnable) log.trace "In level: ${keyword1a} - Passed NOT contain"
                     lCheck3 = "yes"
                }
    // -- check 3 done
                if(traceEnable) log.trace "In keyword: ${keyword1a} - lCheck1: ${lCheck1}, lCheck2: ${lCheck2}, lCheck3: ${lCheck3}"
                if(lCheck1 == "yes" && lCheck2 == "yes" && lCheck3 == "yes") match = "yes"
            }
        
    // -- check 1 start
//log.info "msgCheck: ${msgCheck} - keyword1: ${keyword1}"
            if((keySetType == "k") && (msgCheck.contains("${keyword1}"))) {
//log.info "keySetType = k"
                if(traceEnable) {
                    keyword1a = keyword1.replace("a","@").replace("e","3").replace("i","1").replace("o","0",).replace("u","^")
                    log.trace "In keyword - Found msgCheck: ${keyword1a}"
                }
                kCheck1 = "yes"
   // -- check 1 done 
   // -- check 2 start
                if(sKeyword1 || sKeyword2 || sKeyword3 || sKeyword4) {
                    if(msgCheck.contains("${sKeyword1}") || msgCheck.contains("${sKeyword2}") || msgCheck.contains("${sKeyword3}") || msgCheck.contains("${sKeyword4}")) {
                        if(traceEnable) log.trace "In keyword: ${keyword1a} - Passed keywords"
                        kCheck2 = "yes"
                    }
                } else {
                    if(traceEnable) log.trace "In keyword: ${keyword1a} - Passed keywords"
                    kCheck2 = "yes"
                }
    // -- check 2 done 
    // -- check 3 start            
                if(nKeyword1 || nKeyword2) {                
                    if(!msgCheck.contains("${nKeyword1}") || !msgCheck.contains("${nKeyword2}")) {
                        if(traceEnable) log.trace "In keyword: ${keyword1a} - Passed NOT contain"
                        kcheck3 = "yes"
                    }
                } else {
                    if(traceEnable) log.trace "In keyword: ${keyword1a} - Passed NOT contain"
                    kCheck3 = "yes"
                }
    // -- check 3 done
                if(traceEnable) log.trace "In keyword: ${keyword1a} - kCheck1: ${kCheck1}, kCheck2: ${kCheck2}, kCheck3: ${kCheck3}"
                if(kCheck1 == "yes" && kCheck2 == "yes" && kCheck3 == "yes") match = "yes"
            }
        
            if(match == "yes") {
                if(traceEnable) log.trace "In keyword: ${keyword1a} - WE HAD A MATCH"
                makeList(nameValue,msgValue)
            }
    } catch (e) {
        log.error "In parse - ${e}"
        close()
    }
}
// *****************************
 
def makeList(nameValue,msgValue) {
    if(traceEnable) log.trace "In makeList - working on - nameValue: ${nameValue} - ${msgValue}"

    try {
        if(state.list == null) state.list = []

        getDateTime()
        last = "${nameValue}::${newDate}::${msgValue}"
        state.list.add(0,last)  

        if(state.list) {
            listSize1 = state.list.size()
        } else {
            listSize1 = 0
        }

        int intNumOfLines = 10
        if (listSize1 > intNumOfLines) state.list.removeAt(intNumOfLines)
        String result1 = state.list.join(";")
        def lines = result1.split(";")

        theData = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px'><tr><td colspan=5>"

        for (i=0;i<intNumOfLines && i<listSize1;i++) {
            combined = theData.length() + lines[i].length() + 16
            if(combined < 1006) {
                def (theApp, theTime, theMsg) = lines[i].split("::") 
                theData += "<tr><td>${theApp} <td> - <td>${theTime}<td> - <td>${theMsg}"
            }
        }

        theData += "</table></div>"
        if(logEnable) log.debug "theData - ${theData.replace("<","!")}"       

        dataCharCount1 = theData.length()
        if(dataCharCount1 <= 1024) {
            if(logEnable) log.debug "Log Watchdog Attribute - theData - ${dataCharCount1} Characters"
        } else {
            theData = "Log Watchdog - Too many characters to display on Dashboard (${dataCharCount1})"
        }

        sendEvent(name: "bpt-logData", value: theData, displayed: true)
        sendEvent(name: "numOfCharacters", value: dataCharCount1, displayed: true)
        sendEvent(name: "lastLogMessage", value: msgValue, displayed: true)
    }
    catch(e1) {
        log.error "In makeList - ${e1}"
        close()    
    }
}

def clearData(){
	if(logEnable) log.debug "Log Watchdog Driver - Clearing the data"
    msgValue = "-"
    logCharCount = "0"
    
    state.list = []
    sendEvent(name: "bpt-logData", value: state.list, displayed: true)
	
    sendEvent(name: "bpt-lastLogMessage", value: msgValue, displayed: true)
    sendEvent(name: "numOfCharacters", value: logCharCount, displayed: true)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newDate=date.format("MM-d HH:mm")
	if(hourType == true) newDate=date.format("MM-d hh:mm")
    return newDate
}
