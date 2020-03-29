/**
 *  Simple Kitchen Timer Driver
 *
 *  Design Usage:
 *  Create a simple kitchen timer with controls for use with Dashboards
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
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
 * v1.0.1 - 03/29/20 - Added code for naming timers
 * v1.0.0 - 03/29/20 - Initial release
 *
 */

def setVersion(){
    appName = "SimpleKitchenTimerDriver"
	version = "v1.0.1" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

metadata {
    definition(name: "Simple Kitchen Timer Driver", namespace: "BPTWorld", author: "Bryan Turcotte", component: true, importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Simple%20Kitchen%20Timer/SKT-driver.groovy") {
        capability "Switch"
        
        command "sendDataToDriver"
        command "a1", [[name:"On", description: "Shortcut to comamnd - On"]]
        command "a2", [[name:"Off", description: "Shortcut to comamnd - Off"]]
        command "a3", [[name:"Reset", description: "Shortcut to comamnd - Reset"]]
        command "a4", [[name:"setTimer1", description: "Shortcut to comamnd - setTimer1"]]
        command "a5", [[name:"setTimer2", description: "Shortcut to comamnd - setTimer2"]]
        command "a6", [[name:"setTimer3", description: "Shortcut to comamnd - setTimer3"]]
        
        attribute "switch", "string"
        attribute "timer", "number"
        attribute "timeLeft", "number"
        attribute "tile01", "string"
        attribute "tileCount", "number"
        
        attribute "setTimer1", "number"
        attribute "setTimer2", "number"
        attribute "setTimer3", "number"
        attribute "isFinished", "string"
        
        attribute "dwDriverInfo", "string"
    }
    preferences {
        input name: "about", type: "paragraph", element: "paragraph", title: "Simple Timer Driver", description: "Create a simple kitchen timer with controls for use with Dashboards"
        input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false, description: ""
    }
}

def updated() {
    if(logEnable) log.info "In Updated"
    setVersion()
    if(logEnable) runIn(1800,logsOff)
    setTimer1()
}

def installed() {
    if(logEnable) log.info "In installed"
    setVersion()
    setTimer1()
}

def logsOff(){
    if(logEnable) log.info "In LogsOff - debug logging disabled"
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

// ***** Used to shorten the Maker API URLs *****
def a1() { on() }
def a2() { off() }
def a3() { reset() }
def a4() { setTimer1() }
def a5() { setTimer2() }
def a6() { setTimer3() }

def on() {
    if(logEnable) log.debug "In on - Turning Switch On"
    device.on
    sendEvent(name: "switch", value: "on", displayed: true)
    timeLeft = state.timeLeft
    runIn(1,countdown,[data:timeLeft])
}

def off() {
    if(logEnable) log.debug "In off - Turning Switch Off"
    device.off
    sendEvent(name: "switch", value: "off", displayed: true)
    timeLeft = device.currentValue('timeLeft')
    timerPaused(timeLeft)
}

def countdown(timeLeft) {
    if(device.currentValue("switch") == "on") {
        if(timeLeft == null) timeLeft = device.currentValue('setTimer1')
        timer = timeLeft
        
        if(timeLeft == null) timeLeft = timer
        if(logEnable) log.debug "In countdown - timeLeft: ${timeLeft}"
        
        timeLeft = timeLeft - 1
        sendEvent(name: "timeLeft", value: timeLeft, displayed:true)
        if(logEnable) log.debug "In countdown - timer: ${timer} - timeLeft: ${timeLeft}"
        if(timeLeft == 10) sendEvent(name: "isFinished", value: "at10", isStateChange: true)
        if(timeLeft > 0) {
            makeTile01(timeLeft)
            runIn(1,countdown, [data:timeLeft])
        } else {
            device.off
            sendEvent(name: "switch", value: "off", displayed: true)
            sendEvent(name: "timeLeft", value: "FINISHED", diplayed:true)
            makeTile01(timeLeft)
            timerFinished()
        }
    } else {
        timerPaused(timeLeft)
    }
}

def timerFinished() {
    if(logEnable) log.info "In timerFinished"
    sendEvent(name: "isFinished", value: "finished", isStateChange: true)
}

def timerPaused(timeLeft) {
    if(logEnable) log.debug "In timerPaused - timeLeft: ${timeLeft}"
    sendEvent(name: "timeLeft", value: "${timeLeft}", displayed: true)
    state.timeLeft = timeLeft
}

def reset() {
    if(logEnable) log.debug "In timerReset"
    off()
    if(state.theTimer == null) state.theTimer = device.currentValue('setTimer1')
    timeLeft = state.theTimer
    state.timeLeft = timeLeft
    sendEvent(name: "timeLeft", value: "${timeLeft}", displayed: true)
    sendEvent(name: "isFinished", value: "no", isStateChange: true)
    if(logEnable) log.debug "In timerReset - timeLeft: ${timeLeft}"
    makeTile01(timeLeft)
}

def setTimer1() {
    if(logEnable) log.debug "In setTimer1"
    timeLeft = device.currentValue('setTimer1')
    state.theTimer = timeLeft
    sendEvent(name: "timeLeft", value: "${timeLeft}", displayed: true)
    reset()
}

def setTimer2() {
    if(logEnable) log.debug "In setTimer2"
    timeLeft = device.currentValue('setTimer2')
    state.theTimer = timeLeft
    sendEvent(name: "timeLeft", value: "${timeLeft}", displayed: true)
    reset()
}

def setTimer3() {
    if(logEnable) log.debug "In setTimer3"
    timeLeft = device.currentValue('setTimer3')
    state.theTimer = timeLeft
    sendEvent(name: "timeLeft", value: "${timeLeft}", displayed: true)
    reset()
}

def makeTile01(timeLeft) {
    if(logEnable) log.debug "In makeTile01"

    if(timeLeft == null) timeLeft = device.currentValue('setTimer1')
    controlOn = "http://${state.hubIP}/apps/api/${state.makerID}/devices/${state.cDevID}/a1?access_token=${state.accessToken}"
    controlOff = "http://${state.hubIP}/apps/api/${state.makerID}/devices/${state.cDevID}/a2?access_token=${state.accessToken}"
    reset = "http://${state.hubIP}/apps/api/${state.makerID}/devices/${state.cDevID}/a3?access_token=${state.accessToken}"
    
    setTimer1 = "http://${state.hubIP}/apps/api/${state.makerID}/devices/${state.cDevID}/a4?access_token=${state.accessToken}"
    setTimer2 = "http://${state.hubIP}/apps/api/${state.makerID}/devices/${state.cDevID}/a5?access_token=${state.accessToken}"
    setTimer3 = "http://${state.hubIP}/apps/api/${state.makerID}/devices/${state.cDevID}/a6?access_token=${state.accessToken}"
    
    if(state.timer1n != "null") {
        theTimer1 = state.timer1n
    } else {
        theTimer1 = device.currentValue('setTimer1')
    }
    if(state.timer2n != "null") {
        theTimer2 = state.timer2n
    } else {
        theTimer2 = device.currentValue('setTimer2')
    }
    if(state.timer3n != "null") {
        theTimer3 = state.timer3n
    } else {
        theTimer3 = device.currentValue('setTimer3')
    }
    
    if(theTimer2 == null) theTimer2 = "-"
    if(theTimer3 == null) theTimer3 = "-"
    
    theTile =  "<table width=100% align=center><tr><td colspan=3 align=center>"
    
    if(timeLeft > 10) theTile += "<span style='font-size:${state.countFontSize}px;color:${state.countColor1}'>${timeLeft}</span>"
    if(timeLeft <= 10 && timeLeft > 5) theTile += "<span style='font-size:${state.countFontSize}px;color:${state.countColor2}'>${timeLeft}</span>"
    if(timeLeft <= 5 && timeLeft > 0) theTile += "<span style='font-size:${state.countFontSize}px;color:${state.countColor3}'>${timeLeft}</span>"
    if(timeLeft == 0) theTile += "<span style='font-size:25px;color:${state.countColor4}'>FINISHED</span>"
    theTile += "<tr><td><a href=${controlOn} target=a>START</a><td><a href=${controlOff} target=a>PAUSE</a><td><a href=${reset} target=a>RESET</a>"
    theTile += "<tr><td><a href=${setTimer1} target=a>${theTimer1}</a><td><a href=${setTimer2} target=a>${theTimer2}</a><td><a href=${setTimer3} target=a>${theTimer3}</a>"
   
    if(state.iFrameOff == "false") {
        if(theTimer1 == null) {
            theTile += "<tr><td colspan=3 align=left><small>Please press RESET</small>"
            theTile += "<tr><td colspan=3><iframe name=a width=1 height=1 />"
        } else {
            theTile += "<tr><td colspan=3><iframe name=a width=1 height=1 />"
        }
    } else {
        if(theTimer1 == null) {
            theTile += "<tr><td colspan=3 align=left><small>Please press RESET - iFrame is off</small>"
        } else {
            theTile += "<tr><td colspan=3 align=left><small>iFrame is off</small>"
        }
    }
    theTile += "</table>"
    
	tileCount = theTile.length()

	sendEvent(name: "tile01", value: theTile, displayed: true)
    sendEvent(name: "tileCount", value: tileCount, displayed: true)
}

def sendDataToDriver(theData) {
    if(logEnable) log.debug "In sendDataToDriver - Received: ${theData}"
    def (hubIP,makerID,accessToken,cDevID,timer1,timer2,timer3,timer1n,timer2n,timer3n,iFrameOff,countFontSize,countColor1,countColor2,countColor3,countColor4) = theData.split(":")

    state.hubIP = hubIP
    state.makerID = makerID
    state.accessToken = accessToken
    state.cDevID = cDevID
    state.timer1n = timer1n
    state.timer2n = timer2n
    state.timer3n = timer3n
    if(iFrameOff == "false") state.iFrameOff = "false"
    if(iFrameOff == "true") state.iFrameOff = "true"
    state.countFontSize = countFontSize
    state.countColor1 = countColor1
    state.countColor2 = countColor2
    state.countColor3 = countColor3
    state.countColor4 = countColor4
    
    sendEvent(name: "setTimer1", value: timer1, displayed: true)
    sendEvent(name: "setTimer2", value: timer2, displayed: true)
    sendEvent(name: "setTimer3", value: timer3, displayed: true)
    
    reset()
}

