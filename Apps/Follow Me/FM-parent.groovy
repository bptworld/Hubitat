/**
 *  **************** Follow Me Parent ****************
 *
 *  Design Usage:
 *  Never miss a message again. Send messages to your occupied room speakers when home or by pushover when away. Automatically!
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  2.0.4 - 07/07/20 - Sounds Setup now in Parent App
 *  2.0.3 - 07/06/20 - Added Priority Speaker Setup
 *  2.0.2 - 04/27/20 - Cosmetic changes
 *  2.0.1 - 11/23/19 - Cosmetic changes
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  1.0.1 - 04/03/19 - Added importURL
 *  1.0.0 - 03/17/19 - Initial release.
 *
 */

def setVersion(){
    state.name = "Follow Me"
	state.version = "2.0.4"
}

definition(
    name:"Follow Me",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Never miss a message again. Send messages to your occupied room speakers when home or by pushover when away. Automatically!",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://github.com/bptworld/Hubitat/blob/master/Apps/Follow%20Me/FM-parent.groovy"
)

preferences {
    page name: "mainPage", title: "", install: true, uninstall: true
    page name: "prioritySpeakerOptions", title: "", install: true, uninstall: true
    page name: "soundOptions", title: "", install: true, uninstall: true
} 

def installed() {
    log.debug "Installed with settings: ${settings}"
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"
    unsubscribe()
    initialize()
}

def initialize() {
    log.info "There are ${childApps.size()} child apps"
    childApps.each {child ->
        log.info "Child app: ${child.label}"
    }
}


def mainPage() {
    dynamicPage(name: "mainPage") {
        installCheck()
        if(state.appInstalled == 'COMPLETE'){
            section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
                speakerNotes =  "<b>Speakers:</b><br>"
                speakerNotes += "- Create a new child app for each room that has a speaker in it you want to control."

                pushNotes =  "<b>Push:</b><br>"
                pushNotes += "- Only one child app is need for up to 5 pressence sensors<br>"
                pushNotes += "- If more than 5 sensors are needed, simply add another child app."

                pmNotes =  "<b>Priority Messages</b><br>"
                pmNotes += "- Each message sent to 'Follow Me' can have a priority assigned to it.<br>"
                pmNotes += "- Volume levels can also be adjusted by priority level."

                sAbilities = "Remember: Not all speakers can use volume controls, play sounds and/or restore to what it was doing before the speech event. Please use the report below to see some known speaker abilities."

                paragraph "${speakerNotes}"
                paragraph "${pushNotes}"
                paragraph "${pmNotes}"
                paragraph "${sAbilities}"

                paragraph "<hr>"
                href "speakerStatus", title: "Known Speaker Abilities", description: "Click to see report."
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Child Apps")) {
                app(name: "anyOpenApp", appName: "Follow Me Child", namespace: "BPTWorld", title: "<b>Add a new 'Follow Me' child</b>", multiple: true)
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Priority Options")) {
                href "prioritySpeakerOptions", title: "Priority Speaker Setup", description: "Click here for options."
                href "soundOptions", title: "Priority Sound Options", description: "Click here for options."
            }
            
			section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
       			label title: "Enter a name for parent app (optional)", required: false
 			}
			display2()
		}
	}
}

def prioritySpeakerOptions() {
    dynamicPage(name: "prioritySpeakerOptions", title: "", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Priority Speaker Options")) {
            paragraph "Priority Speaker Option is a way to select one specific speaker to send the message to."
            paragraph "<b>Speaker 1</b>", width:6
            input "sType1", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType1) input "pSpeaker1", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType1) input "pSpeaker1", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 2</b>", width:6           
            input "sType2", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType2) input "pSpeaker2", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType2) input "pSpeaker2", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 3</b>", width:6           
            input "sType3", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType3) input "pSpeaker3", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType3) input "pSpeaker3", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 4</b>", width:6           
            input "sType4", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType4) input "pSpeaker4", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType4) input "pSpeaker4", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 5</b>", width:6           
            input "sType5", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType5) input "pSpeaker5", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType5) input "pSpeaker5", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 6</b>", width:6           
            input "sType6", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType6) input "pSpeaker6", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType6) input "pSpeaker6", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 7</b>", width:6           
            input "sType7", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType7) input "pSpeaker7", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType7) input "pSpeaker7", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 8</b>", width:6           
            input "sType8", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType8) input "pSpeaker8", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType8) input "pSpeaker8", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 9</b>", width:6           
            input "sType9", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType9) input "pSpeaker9", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType9) input "pSpeaker9", "capability.musicPlayer", title: "Speaker", required: false
            
            paragraph "<b>Speaker 10</b>", width:6           
            input "sType10", "bool", title: "speechSynthesis (off) or Music Player (on)", width: 6, submitOnChange:true
            if(!sType10) input "pSpeaker10", "capability.speechSynthesis", title: "Speaker", required: false
            if(sType10) input "pSpeaker10", "capability.musicPlayer", title: "Speaker", required: false
        }
    }  
}

def soundOptions(){
    dynamicPage(name: "soundOptions", title: "Sound Option Setup", install: false, uninstall:false){
		section(getFormat("header-green", "${getImage("Blank")}"+" Sound Options")) {
			paragraph "Link to any sound file you want.  ie. http://192.168.86.81/local/bicycle_bell_ring.mp3<br><small>Note: You can always try the URL in a browser, to be sure it is valid.</small>"
            paragraph "Remember, you can now store files right on your hub!"
            paragraph "* Priority Voice and Sound options are only available when using speechSynthesis speakers."
            input "testTheSpeakers", "capability.speechSynthesis", title: "Choose speaker for testing", required: true, submitOnChange: true
			input "sound1", "text", title: "Sound - 1", required: false, submitOnChange: true
            if(sound1 && testTheSpeakers) input "s1Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound1 && testTheSpeakers && s1Length) input "testBtn1", "button", title: "Test Sound 1", width: 3
            
			input "sound2", "text", title: "Sound - 2", required: false, width: 9, submitOnChange: true
            if(sound2 && testTheSpeakers) input "s2Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound2 && testTheSpeakers && s2Length) input "testBtn2", "button", title: "Test Sound 2", width: 3
            
			input "sound3", "text", title: "Sound - 3", required: false, width: 9, submitOnChange: true
            if(sound3 && testTheSpeakers) input "s3Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound3 && testTheSpeakers && s3Length) input "testBtn3", "button", title: "Test Sound 3", width: 3
            
			input "sound4", "text", title: "Sound - 4", required: false, width: 9, submitOnChange: true
            if(sound4 && testTheSpeakers) input "s4Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound4 && testTheSpeakers && s4Length) input "testBtn4", "button", title: "Test Sound 4", width: 3
            
			input "sound5", "text", title: "Sound - 5", required: false, width: 9, submitOnChange: true
            if(sound5 && testTheSpeakers) input "s5Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound5 && testTheSpeakers && s5Length) input "testBtn5", "button", title: "Test Sound 5", width: 3
            
            input "sound6", "text", title: "Sound - 6", required: false, width: 9, submitOnChange: true
            if(sound6 && testTheSpeakers) input "s6Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound6 && testTheSpeakers && s6Length) input "testBtn6", "button", title: "Test Sound 6", width: 3
            
            input "sound7", "text", title: "Sound - 7", required: false, width: 9, submitOnChange: true
            if(sound7 && testTheSpeakers) input "s7Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound7 && testTheSpeakers && s7Length) input "testBtn7", "button", title: "Test Sound 7", width: 3
            
            input "sound8", "text", title: "Sound - 8", required: false, width: 9, submitOnChange: true
            if(sound8 && testTheSpeakers) input "s8Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound8 && testTheSpeakers && s8Length) input "testBtn8", "button", title: "Test Sound 8", width: 3
            
            input "sound9", "text", title: "Sound - 9", required: false, width: 9, submitOnChange: true
            if(sound9 && testTheSpeakers) input "s9Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound9 && testTheSpeakers && s9Length) input "testBtn9", "button", title: "Test Sound 9", width: 3
            
            input "sound10", "text", title: "Sound - 10", required: false, width: 9, submitOnChange: true
            if(sound10 && testTheSpeakers) input "s10Length", "number", title: "Sound length (in seconds)", description: "0-30", required:true, width:9, submitOnChange:true
            if(sound10 && testTheSpeakers && s10Length) input "testBtn10", "button", title: "Test Sound 0", width: 3
		}
	}
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    if(state.whichButton == "testBtn1"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 1 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound1)
        } catch(e1) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn2"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 2 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound2)
        } catch(e2) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn3"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 3 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound3)
        } catch(e3) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn4"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 4 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound4)
        } catch(e4) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn5"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 5 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound5)
        } catch(e5) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn6"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 6 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound6)
        } catch(e6) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn7"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 7 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound7)
        } catch(e7) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn8"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 8 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound8)
        } catch(e8) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn9"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 9 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound9)
        } catch(e9) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
    if(state.whichButton == "testBtn0"){
        if(logEnable) log.debug "In appButtonHandler - Testing Sound 0 on Speaker: ${testTheSpeakers}"
        try {
            testTheSpeakers.playTrack(sound0)
        } catch(e0) { log.warn "Follow Me (${state.version}) - ${testTheSpeakers} doesn't support playTrack or Test Sound was not found." }
    }
}

def installCheck(){  
    display()
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' parent app "}
  	}
  	else{
    	log.info "Parent Installed OK"
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
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
