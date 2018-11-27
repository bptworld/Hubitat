import groovy.time.TimeCategory
/**
 *  ****************  Welcome Home Child App  ****************
 *
 *  Design Usage:
 *  This app is designed to give a personal welcome announcement after you have entered the home.
 *
 *  Copyright 2018 Bryan Turcotte (@bptworld)
 *
 *  Special thanks to (@Cobra) for use of his Parent/Child code and various other bits and pieces.
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *
 *
 *
 *  V1.0.0 - 11/25/18 - Initial release.
 *
 */

definition(
    name: "Welcome Home Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "This app is designed to give a personal welcome announcement after you have entered the home.",
    category: "",
    
parent: "BPTWorld:Welcome Home",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    )

preferences {
    display()
        section ("This app is designed to give a personal welcome announcement after you have entered the home."){}    
        section("Instructions:", hideable: true, hidden: true) {
        	paragraph "<b>Info:</b>"
    		paragraph "This app is pretty straight forward. Follow the prompts and you should be fine."
			paragraph "<b>Notes:</b>"
			paragraph "This app is designed to give a personal welcome announcement after you have entered the home."
			paragraph "<b>Requirements:</b>"
			paragraph "Be sure to enter in the Preset Values in Advanced Config before creating Child Apps."
		}
		section() {
    	input "triggerMode", "enum", title: "Select activation Type", submitOnChange: true,  options: ["Contact_Sensor","Door_Lock"], required: true, Multiple: false
			if(triggerMode == "Door_Lock"){
				input "lock1", "capability.lock", title: "Activate the welcome message when this door is unlocked", required: true, multiple: false
			}
			if(triggerMode == "Contact_Sensor"){
				input "contactSensor", "capability.contactSensor", title: "Activate the welcome message when this contact sensor is opened", required: true, multiple: false
			}
		}			
		section() {
				input "presenceSensor1", "capability.presenceSensor", title: "and this presence sensor has been detected for less than X minutes (set the minutes below)", required: true, multiple: false
			} 
		section() {
			input "friendlyName1", "text", title: "Friendly name for presence sensor, this is what will be spoken", required: true, multiple: false
		}
		section() { 
           input "speechMode", "enum", required: true, title: "Select Speaker Type", submitOnChange: true,  options: ["Music Player", "Speech Synth"] 
			if (speechMode == "Music Player"){ 
              	input "speaker1", "capability.musicPlayer", title: "Choose speaker(s)", required: false, multiple: true, submitOnChange:true
              	input "volume1", "number", title: "Speaker volume", description: "0-100%", required: false
              	input "volume2", "number", title: "Quiet Time Speaker volume", description: "0-100%",  required: false // defaultValue: "0",
    		  	input "fromTime2", "time", title: "Quiet Time Start", required: false
    		  	input "toTime2", "time", title: "Quiet Time End", required: false
          	}   
        	if (speechMode == "Speech Synth"){ 
         		input "speaker1", "capability.speechSynthesis", title: "Choose speaker(s)", required: false, multiple: true
          	}
      	}
    	if(speechMode){ 
        	section("Allow messages between what times? (Optional)") {
        		input "fromTime", "time", title: "From", required: false
        		input "toTime", "time", title: "To", required: false
			}
    	}
		section() {
			input "message1", "text", title: "Message to hear - Use %random% to have a random message spoken from the list entered within the parent app",  required: false
		}
		section() {
			input "delay1", "number", required: true, title: "How many seconds from the time the trigger being activated to the announcement being made (default=10)", defaultValue: 10
		}
		section() {
			input "timeHome", "number", required: true, title: "How many minutes can the presence sensor be home and still be considered for a welcome home message (default=10)", defaultValue: 10
		}
    	section() {
        	input name: "logEnable", type: "bool", title: "Enable debug logging", defaultValue: false, description: ""
    	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    LOGDEBUG("Updated with settings: ${settings}")
    unsubscribe()
	unschedule()
	//if(logEnable) runIn(10,logsOff)
	initialize()
}

def initialize() {
	if(triggerMode == "Door_Lock"){subscribe(lock1, "lock", lockHandler)}
	if(triggerMode == "Contact_Sensor"){subscribe(contactSensor, "contact", contactSensorHandler)}

}

def lockHandler(evt) {
	state.lockStatus = evt.value
	LOGDEBUG("Lock Status: = ${state.lockStatus}")
	if(state.lockStatus == "unlocked") {
		if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    	if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
			LOGDEBUG("In lockHandler...Pause: ${pause1}")
			getTimeDiff()
		}
	}
}

def contactSensorHandler(evt) {
	state.contactStatus = evt.value
	LOGDEBUG("contact Status: = ${state.contactStatus}")
	if(state.contactStatus == "open") {
		if(pause1 == true){log.warn "${app.label} - Unable to continue - App paused"}
    	if(pause1 == false){LOGDEBUG("Continue - App NOT paused")
			LOGDEBUG("In contactSensorHandler...Pause: ${pause1}")
			getTimeDiff()
		}
	}
}
										
def getTimeDiff() {
	LOGDEBUG("In getTimeDiff...")
	def sensorStatus = presenceSensor1.currentValue("presence")
	LOGDEBUG("Presence Sensor Status: = ${sensorStatus}")
	if(sensorStatus == "present") {
		// ********** Used for Testing **********
		//def lastActivity = "2018-11-27 09:41:00"
		//timeDiff = 8
		//timeDiff = 12
			
		def lastActivity = presenceSensor1.getLastActivity()
			
		LOGDEBUG("lastActivity: ${lastActivity}")
    	long timeDiff
   		def now = new Date()
    	def prev = Date.parse("yyy-MM-dd HH:mm:ss","${lastActivity}".replace("+00:00","+0000"))
    	long unxNow = now.getTime()
    	long unxPrev = prev.getTime()
    	unxNow = unxNow/1000
    	unxPrev = unxPrev/1000
    	timeDiff = Math.abs(unxNow-unxPrev)
    	timeDiff = Math.round(timeDiff/60)
    
		LOGDEBUG("timeDiff: ${timeDiff}")
	
		def delay1ms = delay1 * 1000	
  		if(timeDiff < timeHome) {
			log.info "${app.label} - ${friendlyName1} just got here! Time Diff = ${timeDiff}"
			LOGDEBUG("Wait ${delay1} seconds to Speak")
			pauseExecution(delay1ms)
			talkNow1()
		} else{
			log.info "${app.label} - ${friendlyName1} - No announcement needed. Time Diff = ${timeDiff}"
		}
	} else {
		LOGDEBUG("Presence Sensor: ${sensorStatus} - No announcement needed.")
	}
}

def talkNow1() {
	LOGDEBUG("In talkNow1...")
	checkTime()
	
	if(state.timeOK == true) {
		compileMsg1()
		LOGDEBUG("Speaker(s) in use: ${speaker1}")
		state.fullMsg1 = "${state.msgComp}"
  		if (speechMode == "Music Player"){ 
    		LOGDEBUG("Music Player")
    		setVolume()
    		speaker1.playTextAndRestore(state.fullMsg1)
  		}   
		if (speechMode == "Speech Synth"){ 
			LOGDEBUG("Speech Synth - ${state.fullMsg1}")
			speaker1.speak(state.fullMsg1)
		}
	} else {
		LOGDEBUG("It's quiet time...Can't talk right now")
	}
}

def setVolume(){
	LOGDEBUG("In setVolume...")
	def timecheck = fromTime2
	if (timecheck != null){
		def between2 = timeOfDayIsBetween(toDateTime(fromTime2), toDateTime(toTime2), new Date(), location.timeZone)
    if (between2) {
    	state.volume = volume2
   		speaker1.setLevel(state.volume)
   		LOGDEBUG("Quiet Time = Yes - Setting Quiet time volume")
   		LOGDEBUG("between2 = $between2 - state.volume = $state.volume - Speaker = $speaker1") 
	}
	if (!between2) {
		state.volume = volume1
		LOGDEBUG("Quiet Time = No - Setting Normal time volume")
		LOGDEBUG("between2 = $between2 - state.volume = $state.volume - Speaker = $speaker1")
		speaker1.setLevel(state.volume)
	}
	}
	else if (timecheck == null){
		state.volume = volume1
		speaker1.setLevel(state.volume)
	}
}

def checkTime(){
	LOGDEBUG("In checkTime...")
	def timecheckNow = fromTime
	if (timecheckNow != null){
    
	def between = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
    if (between) {
    	state.timeOK = true
   		LOGDEBUG("Time is ok so can continue")
	}
	else if (!between) {
		state.timeOK = false
		LOGDEBUG("Time is NOT ok so can't continue")
	}
  	}
	else if (timecheckNow == null){  
		state.timeOK = true
  		LOGDEBUG("Time restrictions have not been configured - Continue")
  	}
}

private compileMsg1() {
	LOGDEBUG("In compileMsg...message1 = ${message1}")
    def msgComp = ""
    msgComp = message1.toLowerCase()
		LOGDEBUG("Changed msgComp to lowercase = ${msgComp}")
	if (msgComp.toLowerCase().contains("%random%")) {msgComp = msgComp.toLowerCase().replace('%random%', getGroup1() )}
		LOGDEBUG("In compileMsg...AFTER random...msgComp = ${msgComp}")
	if (msgComp.toLowerCase().contains("%greeting%")) {msgComp = msgComp.toLowerCase().replace('%greeting%', getGreeting() )}
		LOGDEBUG("In compileMsg...AFTER greeting...msgComp = ${msgComp}")
	if (msgComp.toLowerCase().contains("%name%")) {msgComp = msgComp.toLowerCase().replace('%name%', getName() )}
		LOGDEBUG("In compileMsg...AFTER name...msgComp = ${msgComp}")
	state.msgComp = "${msgComp}"
	return state.msgComp
}	

private getName(){
	LOGDEBUG("In getName...Name = ${friendlyName1}")
	name1 = "${friendlyName1}"
	if(name1 == null) {name1 = "Whoever you are"}
	LOGDEBUG("AGAIN...Name = ${name1}")
	return name1
}

private getGreeting(){
	LOGDEBUG("In getGreeting...")
    def calendar = Calendar.getInstance()
	calendar.setTimeZone(location.timeZone)
	def timeHH = calendar.get(Calendar.HOUR) toInteger()
    def timeampm = calendar.get(Calendar.AM_PM) ? "pm" : "am" 
	LOGDEBUG("timeHH = $timeHH")
	if(timeampm == 'am'){
		state.greeting = "${parent.greeting1}"
	}
	else if(timeampm == 'pm' && timeHH < 6){
		state.greeting = "${parent.greeting2}"
		LOGDEBUG("timeampm = ${timeampm} - timehh = ${timeHH}")
	}
	else if(timeampm == 'pm' && timeHH >= 6){
		LOGDEBUG("timehh = ${timeHH} - timeampm = ${timeampm}")
		state.greeting = "${parent.greeting3}"
	} 
	LOGDEBUG("Greeting = ${state.greeting}")
	return state.greeting
}

private getGroup1(msgGroup1item) {
	LOGDEBUG("In getGroup1...")
    def group1List = [
        "${parent.msg1}", 
		"${parent.msg2}",
        "${parent.msg3}",
        "${parent.msg4}",
        "${parent.msg5}",
        "${parent.msg6}",
		"${parent.msg7}", 
		"${parent.msg8}",
        "${parent.msg9}",
        "${parent.msg10}"
    ]
    if(state.group1Count>10){
        for(int i = 10;i<state.group1Count;i++) {
            def group1Display = i.toInteger() + 1
            group1List.add("Group 1 - Message ${group1Display}")
        }
    }
    if(msgGroup1item == null) {
        MaxRandom = (group1List.size() >= state.group1Count ? group1List.size() : state.group1Count)
		LOGDEBUG("MaxRandom = ${MaxRandom}") 
        def randomKey1 = new Random().nextInt(MaxRandom)
		LOGDEBUG("randomKey1 = ${randomKey1}") 
		msgGroup1 = group1List[randomKey1]
    } else {
        msgGroup1 = group1List[msgGroup1item]
    }
	return msgGroup1
}

// ********** Normal Stuff **********

def logsOff(){
    log.warn "${app.label} - debug logging auto disabled"
    device.updateSetting("logEnable",[value:"false",type:"bool"])
}

def pauseOrNot(){
    state.pauseNow = pause1
        if(state.pauseNow == true){
            state.pauseApp = true
            if(app.label){
            if(app.label.contains('red')){
                log.warn "Paused"}
            else{app.updateLabel(app.label + ("<font color = 'red'> (Paused) </font>" ))
              LOGDEBUG("App Paused - state.pauseApp = $state.pauseApp ")   
            }
            }
        }
    
     if(state.pauseNow == false){
         state.pauseApp = false
         if(app.label){
     		if(app.label.contains('red')){ app.updateLabel(app.label.minus("<font color = 'red'> (Paused) </font>" ))
     		LOGDEBUG("App Released - state.pauseApp = $state.pauseApp ")                          
          	}
         }
	}      
}

def setDefaults(){
    pauseOrNot()
    if(pause1 == null){pause1 = false}
    if(state.pauseApp == null){state.pauseApp = false}
	if(logEnable == null){logEnable = false}
}

def logCheck(){
	state.checkLog = logEnable
	if(state.logEnable == true){
		log.info "${app.label} - All Logging Enabled"
	}
	else if(state.logEnable == false){
		log.info "${app.label} - Further Logging Disabled"
	}
}

def LOGDEBUG(txt){
    try {
		if (settings.logEnable) { log.debug("${app.label} - ${txt}") }
    } catch(ex) {
    	log.error("${app.label} - LOGDEBUG unable to output requested data!")
    }
}

def display(){
	setDefaults()
	section{paragraph "Child App Version: 1.0.0"}
	section(){input "pause1", "bool", title: "Pause This App", required: true, submitOnChange: true, defaultValue: false }
} 
