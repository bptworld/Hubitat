/**
 *  ****************  What Did I Say Driver  ****************
 *
 *  Design Usage:
 *  This driver formats Speech data to be displayed on Hubitat's Dashboards.
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
 *  V1.0.3 - 03/02/19 - Fix the date being display on tile
 *  V1.0.2 - 02/18/19 - Adding command initialize
 *  V1.0.1 - 02/08/19 - Changed the 'How many lines' field from 5 or 10, to any number from 1 to 10. Attempt to fix a reported error.
 *  V1.0.0 - 01/27/19 - Initial release
 */

def version(){"v1.0.3"}

metadata {
	definition (name: "What Did I Say", namespace: "BPTWorld", author: "Bryan Turcotte") {
   		capability "Initialize"
		capability "Actuator"
		capability "Speech Synthesis"
		capability "Music Player"
		
		command "sendSpeechMap", ["string"]
		command "playTextAndRestore", ["string"]
		command "setVolumeSpeakAndRestore", ["string"]
		command "speak", ["string"]
		
    	attribute "whatDidISay", "string"
		attribute "lastSpoken", "string"
	}
	preferences() {    	
        section(){
            input("debugMode", "bool", title: "Enable logging", required: false, defaultValue: false)
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "40")
			input("numOfLines", "number", title: "How many lines to display (from 1 to 10 only)", required:true, defaultValue: 5)
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
			input("clearData", "bool", title: "Clear All Speech Data", required: false, defaultValue: false)
        }
    }
}

//Received new messages from apps
def sendSpeechMap(speechMap) {
	state.speechReceived = speechMap
	populateMap()
}

def playTextAndRestore(speechMap) {
	state.speechReceived = speechMap
	populateMap()
}

def setVolumeSpeakAndRestore(speechMap) {
	state.speechReceived = speechMap
	populateMap()
}

def speak(speechMap) {
	state.speechReceived = speechMap
	populateMap()
}

def populateMap() {
	LOGDEBUG("What Did I Say - Received new Speech! ${state.speechReceived}")
	
	sendEvent(name: "lastSpoken", value: state.speechReceived, displayed: true)
	
	// Read in the maps
	try {
		sOne = state.speechMap1.get(state.s,nMessage)
		sTwo = state.speechMap2.get(state.s,nMessage)
		sThree = state.speechMap3.get(state.s,nMessage)
		sFour = state.speechMap4.get(state.s,nMessage)
		sFive = state.speechMap5.get(state.s,nMessage)
		sSix = state.speechMap6.get(state.s,nMessage)
		sSeven = state.speechMap7.get(state.s,nMessage)
		sEight = state.speechMap8.get(state.s,nMessage)
		sNine = state.speechMap9.get(state.s,nMessage)
		sTen = state.speechMap10.get(state.s,nMessage)
	}
	catch (e) {
        //log.error "Error:  $e"
    }
	
	LOGDEBUG("What Did I Say - OLD -<br>sOne: ${sOne}<br>sTwo: ${sTwo}<br>sThree: ${sThree}<br>sFour: ${sFour}<br>sFive: ${sFive}")
	
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
	mOne = state.newdate + " - " + state.speechReceived
	
	LOGDEBUG("What Did I Say - NEW -<br>mOne: ${mOne}<br>mTwo: ${mTwo}<br>mThree: ${mThree}<br>mFour: ${mFour}<br>mFive: ${mFive}")
	
	// Fill the maps back in
	try {
		state.speechMap1.put(state.s,mOne)
		state.speechMap2.put(state.s,mTwo)
		state.speechMap3.put(state.s,mThree)
		state.speechMap4.put(state.s,mFour)
		state.speechMap5.put(state.s,mFive)
		state.speechMap6.put(state.s,mSix)
		state.speechMap7.put(state.s,mSeven)
		state.speechMap8.put(state.s,mEight)
		state.speechMap9.put(state.s,mNine)
		state.speechMap10.put(state.s,mTen)
	}
	catch (e) {
        //log.error "Error:  $e"
    }
	
	state.speechTop = "<table width='100%'><tr><td align='left'>"
	if(numOfLines == 1) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}</div>"
	}
	if(numOfLines == 2) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}</div>"
	}
	if(numOfLines == 3) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}</div>"
	}
	if(numOfLines == 4) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}</div>"
	}
	if(numOfLines == 5) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}</div>"
	} 
	if(numOfLines == 6) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}</div>"
	}
	if(numOfLines == 7) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}</div>"
	}
	if(numOfLines == 8) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}</div>"
	}
	if(numOfLines == 9) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}</div>"
	}
	if(numOfLines == 10) {
		state.speechTop+= "<div style='line-height=75%;margin-top:0em;margin-bottom:0em;font-size:.${fontSize}em;'>${mOne}<br>${mTwo}<br>${mThree}<br>${mFour}<br>${mFive}<br>${mSix}<br>${mSeven}<br>${mEight}<br>${mNine}<br>${mTen}</div>"
	}
	state.speechTop+= "</td></tr></table>"
	
	LOGDEBUG("What Did I Say -<br>${state.speechTop}")
	
	sendEvent(name: "whatDidISay", value: state.speechTop, displayed: true)
}

def installed(){
    log.info "What Did I Say has been Installed"
    clearSpeechData()
}

def updated() {
    log.info "What Did I Say has been Updated"
    if (clearData) runIn(2,clearSpeechData)
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) state.newdate=date.format("MM-d HH:mm:ss")
	if(hourType == true) state.newdate=date.format("MM-d hh:mm:ss")
}

def clearDataOff(){
    log.info "What Did I Say has cleared the data"
    device.updateSetting("clearData",[value:"false",type:"bool"])
}

def clearSpeechData(){
	LOGDEBUG("What Did I Say - clearing the data")
	state.nMessage = "No Data"
	state.s = "s"
	state.speechMap1 = [:]
	state.speechMap1.put(state.s,state.nMessage)
	state.speechMap2 = [:]
	state.speechMap2.put(state.s,state.nMessage)
	state.speechMap3 = [:]
	state.speechMap3.put(state.s,state.nMessage)
	state.speechMap4 = [:]
	state.speechMap4.put(state.s,state.nMessage)
	state.speechMap5 = [:]
	state.speechMap5.put(state.s,state.nMessage)
	state.speechMap6 = [:]
	state.speechMap6.put(state.s,state.nMessage)
	state.speechMap7 = [:]
	state.speechMap7.put(state.s,state.nMessage)
	state.speechMap8 = [:]
	state.speechMap8.put(state.s,state.nMessage)
	state.speechMap9 = [:]
	state.speechMap9.put(state.s,state.nMessage)
	state.speechMap10 = [:]
	state.speechMap10.put(state.s,state.nMessage)
	
	state.speechTop = "Waiting for Data..."
	sendEvent(name: "whatDidISay", value: state.speechTop, displayed: true)
	if (clearData) runIn(2,clearDataOff)
}	
	
def LOGDEBUG(txt) {
    try {
    	if (settings.debugMode) { log.debug("${txt}") }
    } catch(ex) {
    	log.error("LOGDEBUG unable to output requested data!")
    }
}
