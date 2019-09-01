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
 *  V1.0.0 - 08/31/19 - Initial release
 */

def setVersion(){
    appName = "LogWatchdogDriver"
	version = "v1.0.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    if(logEnable) log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Log Watchdog Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"
       
        attribute "status", "string"
        attribute "lastLogMessage", "string"
        attribute "logData", "string"
        attribute "numOfCharacters", "number"
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
            input("numOfLines", "number", title: "How many lines to display (from 1 to 10 only)", required:true, defaultValue: 5)
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
    def (keyword1,sKeyword1,sKeyword2,sKeyword3,sKeyword4) = keys.split(";")
    if(keyword1 != "-") {
        state.keyword1 = keyword1
    } else {
        state.keyword1 = ""
    }
    if(sKeyword1 != "-") {
        state.sKeyword1 = sKeyword1
    } else {
        state.sKeyword1 = ""
    }
    if(sKeyword2 != "-") {
        state.sKeyword2 = sKeyword2
    } else {
        state.sKeyword2 = ""
    }
    if(sKeyword3 != "-") {
        state.sKeyword3 = sKeyword3
    } else {
        state.sKeyword3 = ""
    }
    if(sKeyword4 != "-") {
        state.sKeyword4 = sKeyword4
    } else {
        state.sKeyword4 = ""
    }
}

def parse(String description) {
    theData = "${description}"
    
    // This is what the incoming data looks like
    //{"name":"aWeb socket","msg":"{"name":"Test","msg":"In motionSensorHandler (v2.0.0) - sZone: true - Status: active","id":7371,"time":"2019-08-31 08:19:02.942","type":"app","level":"debug"}","id":5758,"time":"2019-08-31 08:19:03.770","type":"dev","level":"debug"}
    
    def (name, msg) = theData.split(",")
    def (msgKey, msgValue) = msg.split(":")
    msgValue = msgValue.replace("\"","")
    msgCheck = msgValue.toLowerCase()
    keyword1 = state.keyword1.toLowerCase()
    if(state.sKeyword1 != "") sKeyword1 = state.sKeyword1.toLowerCase()
    if(state.sKeyword2 != "") sKeyword2 = state.sKeyword2.toLowerCase()
    if(state.sKeyword3 != "") sKeyword3 = state.sKeyword3.toLowerCase()
    if(state.sKeyword4 != "") sKeyword4 = state.sKeyword4.toLowerCase()

    if( msgCheck.contains("${keyword1}") && (msgCheck.contains("${sKeyword1}") || msgCheck.contains("${sKeyword2}") || msgCheck.contains("${sKeyword3}") || msgCheck.contains("${sKeyword4}")) ) {
        sendEvent(name: "lastLogMessage", value: msgValue, displayed: true)
        if(logEnable) log.debug "Log Watchdog Driver - Keywords Found"
        populateMap(msgValue)
    }
}

def clearData(){
	if(logEnable) log.debug "Log Watchdog Driver - Clearing the data"
	nMessage = "No Data"
	state.logMap1 = [:]
	state.logMap1.put("s",nMessage)
	state.logMap2 = [:]
	state.logMap2.put("s",nMessage)
	state.logMap3 = [:]
	state.logMap3.put("s",nMessage)
	state.logMap4 = [:]
	state.logMap4.put("s",nMessage)
	state.logMap5 = [:]
	state.logMap5.put("s",nMessage)
	state.logMap6 = [:]
	state.logMap6.put("s",nMessage)
	state.logMap7 = [:]
	state.logMap7.put("s",nMessage)
	state.logMap8 = [:]
	state.logMap8.put("s",nMessage)
	state.logMap9 = [:]
	state.logMap9.put("s",nMessage)
	state.logMap10 = [:]
	state.logMap10.put("s",nMessage)
	
	state.logTop = "Waiting for Data..."
	sendEvent(name: "logData", value: state.logTop, displayed: true)
}

def populateMap(msgValue) {
	//if(logEnable) log.debug "Log Watchdog Driver - Received new Data! ${msgValue}"
    state.msgValue = msgValue.take(70)
	
	// Read in the maps
	try {
		sOne = state.logMap1.get("s",nMessage)
		sTwo = state.logMap2.get("s",nMessage)
		sThree = state.logMap3.get("s",nMessage)
		sFour = state.logMap4.get("s",nMessage)
		sFive = state.logMap5.get("s",nMessage)
		sSix = state.logMap6.get("s",nMessage)
		sSeven = state.logMap7.get("s",nMessage)
		sEight = state.logMap8.get("s",nMessage)
		sNine = state.logMap9.get("s",nMessage)
		sTen = state.logMap10.get("s",nMessage)
	}
	catch (e) {
        //log.error "Error:  $e"
    }
	
	if(sOne == null) sOne = "${state.nMessage}"
	if(sTwo == null) sTwo = "${state.nMessage}"
	if(sThree == null) sThree = "${state.nMessage}"
	if(sFour == null) sFour = "${state.nMessage}"
	if(sFive == null) sFive = "${state.nMessage}"
	if(sSix == null) sSix = "${state.nMessage}"
	if(sSeven == null) sSeven = "${state.nMessage}"
	if(sEight == null) sEight = "${state.nMessage}"
	if(sNine == null) sNine = "${state.nMessage}"
	if(sTen == null) sTen = "${state.nMessage}"
	
	// Move all messages down 1 slot
	mTen = sNine
	mNine = sEight
	mEight = sSeven
	mSeven = sSix
	mSix = sFive
	mFive = sFour
	mFour = sThree
	mThree = sTwo
	mTwo = sOne
	
	getDateTime()
	mOne = newdate + " - " + state.msgValue

	// Fill the maps back in
	try {
		state.logMap1.put("s",mOne)
		state.logMap2.put("s",mTwo)
		state.logMap3.put("s",mThree)
		state.logMap4.put("s",mFour)
		state.logMap5.put("s",mFive)
		state.logMap6.put("s",mSix)
		state.logMap7.put("s",mSeven)
		state.logMap8.put("s",mEight)
		state.logMap9.put("s",mNine)
		state.logMap10.put("s",mTen)
	}
	catch (e) {
        //log.error "Error:  $e"
    }
	
	state.logTop = "<table width='100%'><tr><td align='left'>"
	if(numOfLines == 1) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}</div>"
	}
	if(numOfLines == 2) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}</div>"
	}
	if(numOfLines == 3) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}</div>"
	}
	if(numOfLines == 4) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}</div>"
	}
	if(numOfLines == 5) {
		state.logTop+= "<div style=';font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}</div>"
	} 
	if(numOfLines == 6) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}</div>"
	}
	if(numOfLines == 7) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}</div>"
	}
	if(numOfLines == 8) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}</div>"
	}
	if(numOfLines == 9) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}</div>"
	}
	if(numOfLines == 10) {
		state.logTop+= "<div style='font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}<br>${mTen}</div>"
	}
	state.logTop+= "</td></tr></table>"
	
	logCharCount = state.logTop.length()
	if(logTopCount <= 1000) {
		if(logEnable) log.debug "Log Watchdog Driver - ${logCharCount} Characters"
	} else {
		state.logTop = "Too many characters to display on Dashboard"
	}
	sendEvent(name: "logData", value: state.logTop, displayed: true)
    sendEvent(name: "numOfCharacters", value: logCharCount, displayed: true)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newdate=date.format("MM-d HH:mm")
	if(hourType == true) newdate=date.format("MM-d hh:mm")
    return newdate
}
