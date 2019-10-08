/**
 *  ****************  Log Watchdog Driver  ****************
 *
 *  Design Usage:
 *  This driver opens a webSocket to capture Log info.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V1.0.7 - 10/08/19 - Now handles only 1 keyset per child app
 *  V1.0.6 - 09/07/19 - Added some error catching
 *  V1.0.5 - 09/05/19 - Getter better!
 *  V1.0.4 - 09/05/19 - Trying some new things
 *  V1.0.3 - 09/03/19 - Added 'does not contain' keywords
 *  V1.0.2 - 09/02/19 - Evolving fast!
 *  V1.0.1 - 09/01/19 - Major changes to the driver
 *  V1.0.0 - 08/31/19 - Initial release
 */

def setVersion(){
    appName = "LogWatchdogDriver"
	version = "v1.0.7" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    if(logEnable) log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Log Watchdog Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Log%20Watchdog/LW-driver.groovy") {
   		capability "Actuator"
       
        attribute "status", "string"
        attribute "lastLogMessage1", "string"       
        attribute "logData1", "string"        
        attribute "numOfCharacters1", "number"
        attribute "keywordInfo", "string"
        
        command "connect"
        command "close"
        command "clearData"
        command "keywordInfo"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
    }
    preferences() {    	
        section(){
            input name: "about", type: "paragraph", element: "paragraph", title: "<b>Log Watchdog Driver</b>", description: "ONLY click 'Clear Data' to clear the message data."
            input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
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
    if(state.keysMap == null) state.keysMap = [:]
    
    def (keySet,keySetType,keyword1,sKeyword1,sKeyword2,sKeyword3,sKeyword4,nKeyword1,nKeyword2) = keys.split(";")
    
    def keyValue = "${keySetType};${keyword1};${sKeyword1};${sKeyword2};${sKeyword3};${sKeyword4};${nKeyword1};${nKeyword2}"

    if(keySet == "keySet01") {
        newMap = "${keySet}:${keyValue}"
        def newData = stringToMap(newMap)
        state.keysMap << newData
        if(traceEnable) log.trace "Recieved ${keySet}"
    }
    if(traceEnable) log.trace "${state.keysMap}"
}

def parse(String description) {
    theData = "${description}"
    
    // This is what the incoming data looks like
    //{"name":"aWeb socket","msg":"{"name":"Test","msg":"In motionSensorHandler (v2.0.0) - sZone: true - Status: active","id":7371,"time":"2019-08-31 08:19:02.942","type":"app","level":"debug"}","id":5758,"time":"2019-08-31 08:19:03.770","type":"dev","level":"debug"}
    
    def (name, msg, id, time, type, level) = theData.split(",")
    def (nameName, nameValue) = name.split(":")
    def (msgName, msgValue) = msg.split(":")
    msgValue = msgValue.replace("\"","")
    nameValue = nameValue.replace("\"","")
    msgCheck = msgValue.toLowerCase()
    
    def (theKey, lvlValue) = level.split(":")
    lvlValue = lvlValue.replace("\"","").replace("}","")
    lvlCheck = lvlValue.toLowerCase()
 
// *****************************
    def allMap = state.keysMap.collectEntries{ [(it.key):(it.value)] }
    allMap.each { it ->
        def match = "no"
        def keyName = it.key
        def keyValue = it.value.toLowerCase()
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
                    lCheck3 = "yes"
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
        if((keySetType == "k") && (msgCheck.contains("${keyword1}"))) {
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
            if(keyName.contains("1")) listNum = "1"
            if(keyName.contains("2")) listNum = "2"
            if(keyName.contains("3")) listNum = "3"
            if(keyName.contains("4")) listNum = "4"
            if(keyName.contains("5")) listNum = "5"
            if(traceEnable) log.trace "In keyword: ${keyword1a} - ${listNum} - WE HAD A MATCH"
            makeList(msgValue,nameValue,listNum)
        }
    }
}
// *****************************
 
def makeList(msgValue,nameValue,listNum) {
    if(traceEnable) log.trace "In makeList - working on ${listNum} - nameValue: ${nameValue}"

    if(listNum == "1") {
        try {
            if(traceEnable) log.trace "In listNum: ${listNum}"
            msgValueShort1 = msgValue.take(70)
            getDateTime()
	        nMessage1 = newdate + " - " + msgValueShort1
        
            if(state.list1 == null) state.list1 = []
            state.list1.add(0,nMessage1)  

            listSize1 = state.list1.size()
            if(listSize1 > 10) state.list1.removeAt(10)

            String result1 = state.list1.join(";")
            logCharCount1 = result1.length()
	        if(logTopCount1 <= 1000) {
	        	if(logEnable) log.debug "Log Watchdog Driver - ${logCharCount1} Characters"
	        } else {
	        	logTop101 = "Too many characters to display on Dashboard"
	        }

            def lines1 = result1.split(";")
            linesSize1 = lines1.size()
            if(traceEnable) log.trace "In makeList - lines1: ${linesSize1}"
            logTop101= "<table><tr><td><div style='font-size:${fontSize}px;'>"
            if(linesSize1 >= 1) logTop101 += "${lines1[0]}<br>"
            if(linesSize1 >= 2) logTop101 += "${lines1[1]}<br>"
            if(linesSize1 >= 3) logTop101 += "${lines1[2]}<br>"
            if(linesSize1 >= 4) logTop101 += "${lines1[3]}<br>"
            if(linesSize1 >= 5) logTop101 += "${lines1[4]}<br>"
            if(linesSize1 >= 6) logTop101 += "${lines1[5]}<br>"
            if(linesSize1 >= 7) logTop101 += "${lines1[6]}<br>"
            if(linesSize1 >= 8) logTop101 += "${lines1[7]}<br>"
            if(linesSize1 >= 9) logTop101 += "${lines1[8]}<br>"
            if(linesSize1 >= 10) logTop101 += "${lines1[9]}"
            logTop101 += "</div></td></tr></table>"
    
	        sendEvent(name: "logData1", value: logTop101, displayed: true)
            sendEvent(name: "numOfCharacters1", value: logCharCount1, displayed: true)
            sendEvent(name: "lastLogMessage1", value: msgValueShort1, displayed: true)
        }
        catch(e1) {
            
        }
    }
}

def clearData(){
	if(logEnable) log.debug "Log Watchdog Driver - Clearing the data"
    msgValue = "-"
    logCharCount = "0"
    
    state.list1 = []
    sendEvent(name: "logData1", value: state.list1, displayed: true)
	
    sendEvent(name: "lastLogMessage1", value: msgValue, displayed: true)
    sendEvent(name: "numOfCharacters1", value: logCharCount, displayed: true)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newdate=date.format("MM-d HH:mm")
	if(hourType == true) newdate=date.format("MM-d hh:mm")
    return newdate
}
