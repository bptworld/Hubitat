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
 *  V1.0.2 - 09/02/19 - Evolving fast!
 *  V1.0.1 - 09/01/19 - Major changes to the driver
 *  V1.0.0 - 08/31/19 - Initial release
 */

def setVersion(){
    appName = "LogWatchdogDriver"
	version = "v1.0.2" 
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
        attribute "lastLogMessage2", "string"
        attribute "lastLogMessage3", "string"
        attribute "lastLogMessage4", "string"
        attribute "lastLogMessage5", "string"
        attribute "logData1", "string"
        attribute "logData2", "string"
        attribute "logData3", "string"
        attribute "logData4", "string"
        attribute "logData5", "string"
        attribute "numOfCharacters1", "number"
        attribute "numOfCharacters2", "number"
        attribute "numOfCharacters3", "number"
        attribute "numOfCharacters4", "number"
        attribute "numOfCharacters5", "number"
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
    if(logEnable) log.info "In keywordInfo"
    if(state.keysMap == null) state.keysMap = [:]
    def (keySet,keyword1,sKeyword1,sKeyword2,sKeyword3,sKeyword4) = keys.split(";")
    def keyValue = "${keyword1};${sKeyword1};${sKeyword2};${sKeyword3};${sKeyword4}"
    
    if(keySet == "keySet01") {
        state.keys1 = keys
        newMap = "${keySet}:${keyValue}"
        def newData = stringToMap(newMap)
        state.keysMap << newData
        log.info "Recieved ${keySet}"
    }
    if(keySet == "keySet02") {
        state.keys2 = keys
        newMap = "${keySet}:${keyValue}"
        def newData = stringToMap(newMap)
        state.keysMap << newData
        log.info "Recieved ${keySet}"
    }
    if(keySet == "keySet03") {
        state.keys3 = keys
        newMap = "${keySet}:${keyValue}"
        def newData = stringToMap(newMap)
        state.keysMap << newData
        log.info "Recieved ${keySet}"
    }
    if(keySet == "keySet04") {
        state.keys4 = keys
        newMap = "${keySet}:${keyValue}"
        def newData = stringToMap(newMap)
        state.keysMap << newData
        log.info "Recieved ${keySet}"
    }
    if(keySet == "keySet05") {
        state.keys5 = keys
        newMap = "${keySet}:${keyValue}"
        def newData = stringToMap(newMap)
        state.keysMap << newData
        log.info "Recieved ${keySet}"
    }
    log.info "${state.keysMap}"
}

def parse(String description) {
    theData = "${description}"
    
    // This is what the incoming data looks like
    //{"name":"aWeb socket","msg":"{"name":"Test","msg":"In motionSensorHandler (v2.0.0) - sZone: true - Status: active","id":7371,"time":"2019-08-31 08:19:02.942","type":"app","level":"debug"}","id":5758,"time":"2019-08-31 08:19:03.770","type":"dev","level":"debug"}
    
    def (name, msg, id, time, type, level) = theData.split(",")
    def (name2, msgValue) = msg.split(":")
    msgValue = msgValue.replace("\"","")
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
        def (keyword1,sKeyword1,sKeyword2,sKeyword3,sKeyword4) = keyValue.split(";")
        if(keyword1 == "-") keyword1 = ""
        if(sKeyword1 == "-") sKeyword1 = ""
        if(sKeyword2 == "-") sKeyword2 = ""
        if(sKeyword3 == "-") sKeyword3 = ""
        if(sKeyword4 == "-") sKeyword4 = ""
        if( lvlCheck.contains("${keyword1}") && (msgCheck.contains("${sKeyword1}")) ) {
            log.debug "Log Watchdog Driver - Match Found - Logging Level - ${keyName}"
            match = "yes"
        } else if( msgCheck.contains("${keyword1}") && (msgCheck.contains("${sKeyword1}") || msgCheck.contains("${sKeyword2}") || msgCheck.contains("${sKeyword3}") || msgCheck.contains("${sKeyword4}")) ) {
            log.debug "Log Watchdog Driver - Match Found - Keywords - ${keyName}"
            match = "yes"
        }
        if(match == "yes") {
            if(keyName.contains("1")) listNum = "1"
            if(keyName.contains("2")) listNum = "2"
            if(keyName.contains("3")) listNum = "3"
            if(keyName.contains("4")) listNum = "4"
            if(keyName.contains("5")) listNum = "5"
            makeList(msgValue,listNum)
        }
    }
}
// *****************************
 
def makeList(msgValue,listNum) {
    log.info "In makeList"
    
    msgValue = msgValue.take(70)
    getDateTime()
	nMessage = newdate + " - " + msgValue
    
    if(list$listNum == null) list$listNum = ["-","-","-","-","-","-","-","-","-","-"]
    list$listNum.add(0,nMessage)  

    listSize = list$listNum.size()
    if(listSize > 10) list$listNum.removeAt(10)
    
    String result = list$listNum.join(";")
    logCharCount = result.length()
	if(logTopCount <= 1000) {
		if(logEnable) log.debug "Log Watchdog Driver - ${logCharCount} Characters"
	} else {
		logTop$listNum = "Too many characters to display on Dashboard"
	}
    
    def (mOne,mTwo,mThree,mFour,mFive,mSix,mSeven,mEight,mNine,mTen) = result.split(";")
    logTop10 = "<table><tr><td><div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}<br>${mTen}</div></td></tr></table>"
    
	sendEvent(name: "logData$listNum", value: logTop10, displayed: true)
    sendEvent(name: "numOfCharacters$listNum", value: logCharCount, displayed: true)
    sendEvent(name: "lastLogMessage$listNum", value: msgValue, displayed: true)
}

def clearData(){
	if(logEnable) log.debug "Log Watchdog Driver - Clearing the data"
    msgValue = ""
    logCharCount = ""
	state.list1 = ["-","-","-","-","-","-","-","-","-","-"]
    state.list2 = ["-","-","-","-","-","-","-","-","-","-"]
    state.list3 = ["-","-","-","-","-","-","-","-","-","-"]
    state.list4 = ["-","-","-","-","-","-","-","-","-","-"]
    state.list5 = ["-","-","-","-","-","-","-","-","-","-"]
	sendEvent(name: "logData1", value: state.list1, displayed: true)
    sendEvent(name: "logData2", value: state.list2, displayed: true)
    sendEvent(name: "logData3", value: state.list3, displayed: true)
    sendEvent(name: "logData4", value: state.list4, displayed: true)
    sendEvent(name: "logData5", value: state.list5, displayed: true)
    sendEvent(name: "lastLogMessage1", value: msgValue, displayed: true)
    sendEvent(name: "lastLogMessage2", value: msgValue, displayed: true)
    sendEvent(name: "lastLogMessage3", value: msgValue, displayed: true)
    sendEvent(name: "lastLogMessage4", value: msgValue, displayed: true)
    sendEvent(name: "lastLogMessage5", value: msgValue, displayed: true)
    sendEvent(name: "numOfCharacters1", value: logCharCount, displayed: true)
    sendEvent(name: "numOfCharacters2", value: logCharCount, displayed: true)
    sendEvent(name: "numOfCharacters3", value: logCharCount, displayed: true)
    sendEvent(name: "numOfCharacters4", value: logCharCount, displayed: true)
    sendEvent(name: "numOfCharacters5", value: logCharCount, displayed: true)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newdate=date.format("MM-d HH:mm")
	if(hourType == true) newdate=date.format("MM-d hh:mm")
    return newdate
}
