/**
 *  **************** Life Event Calendar App  ****************
 *
 *  Design Usage:
 *  Never miss an important Life Event again! Schedule reminders easily and locally.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
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
 *  1.0.4 - 04/27/22 - Major rewrite
 *  1.0.3 - 03/31/22 - Added optional 'delay' between commands. Now displays the Next Event and the Next 3 Events in the Data Device.
 *  1.0.2 - 03/27/22 - Added 'repeat yearly'
 *  1.0.1 - 03/27/22 - Fixed schedules overwriting each other. Added 'repeat in x days'.
 *  1.0.0 - 03/27/22 - Initial release.
 *
 */



def setVersion(){
    state.name = "Life Event Calendar Child"
	state.version = "1.0.4"
}

definition(
    name: "Life Event Calendar Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Never miss an important Life Event again! Schedule reminders easily and locally.",
    category: "Convenience",
	parent: "BPTWorld:Life Event Calendar",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Live%20Event%20Reminders/ler-child.groovy",
)

preferences {
    page name: "pageConfig"
    page name: "speechPushOptions", title: "", install:false, uninstall:false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        checkMapHandler()
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Never miss an important Life Event again! Schedule reminders easily and locally."
		}

        section(getFormat("header-green", "${getImage("Blank")}"+" Data Device")) {
            createDeviceSection("Life Event Calendar Driver")
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Add to Calendar")) {
            if(state.calendarMap == null) state.calendarMap = [:]
            theMessage = ""
            input "theTitle", "text", title: "<b>Event Title</b>", width:6
            input "findRec", "bool", title: "<b>Find Record</b>", defaultValue:false, submitOnChange:true, width:6
            if(theTitle && findRec) {
                getRecord = state.calendarMap.get(theTitle)
                if(getRecord) {
                    theMessage = "<b><i> - Record Found - </i></b>"
                    if(logEnable) log.debug "In Add to Calendar - Record Found - $theTitle"
                    try{
                        (gDate, gTime1, gTime2, gEDate, gETime1, gETime2, gDesc, gRepeatMinutes, gRepeatMax, gDays, gYearly) = getRecord.split(";")
                        if(logEnable) log.debug "In Add to Calendar - $gDate, $gTime1, $gTime2, $gEDate, $gETime1, $gETime2, $gDesc, $gRepeatMinutes, $gRepeatMax, $gDays, $gYearly"
                        app.updateSetting("theDate", [value: gDate, type: "date"])
                        app.updateSetting("theTime", [value: gTime1, type: "time"])
                        app.updateSetting("endDate", [value: gEDate, type: "date"])
                        app.updateSetting("endTime", [value: gETime1, type: "time"])
                        app.updateSetting("theText", [value: gDesc, type: "text"])
                        app.updateSetting("msgRepeatMinutes", [value: gRepeatMinutes, type: "number"])
                        app.updateSetting("msgRepeatMax", [value: gRepeatMax, type: "number"])
                        if(gYearly == "T") { app.updateSetting("repeatYearly", [value: "true",type: "bool"]) }
                        app.updateSetting("repeatInDays", [value: gDays, type: "text"])
                    } catch(e) {
                        log.error "Life Event Calendar - Something seems to be wrong with the database: ${getRecord}"
                    }
                } else {
                    theMessage = "<b><i> - Record Not Found - Adding new record - </i></b>"
                    if(logEnable) log.debug "In Add to Calendar - Clearing the record"
                    app.removeSetting("theDate")
                    app.removeSetting("theTime")
                    app.removeSetting("endDate")
                    app.removeSetting("endTime")
                    app.removeSetting("theText")
                    app.removeSetting("msgRepeatMinutes")
                    app.removeSetting("msgRepeatMax")
                    app.removeSetting("repeatYearly")
                    app.removeSetting("repeatInDays")    
                }
                app.updateSetting("findRec", [value: "false",type: "bool"]) 
            }
            paragraph "<hr>"
            paragraph "${theMessage}"
            input "theDate", "date", title: "Event Start Date", width:6, submitOnChange:true
            input "theTime", "time", title: "Event Start Time", width:6, submitOnChange:true
            input "endDate", "date", title: "Event End Date", width:6, submitOnChange:true
            input "endTime", "time", title: "Event End Time", width:6, submitOnChange:true  
            input "theText", "text", title: "Event Description", submitOnChange:true

            input "msgRepeatMinutes", "number", title: "Repeat every XX minutes (optional)", width:6, submitOnChange:true
            input "msgRepeatMax", "number", title: "Max number of repeats (optional)", width:6, submitOnChange:true

            input "repeatYearly", "bool", title: "Repeat Yearly from start date (optional)", defaultValue:false, submitOnChange:true
            input "repeatInDays", "text", title: "Repeat Every X days from start date (optional)", required:false, submitOnChange:true

            paragraph "<small>* Remember to click outside any field before clicking on a button.</small>"

            input "bCancel", "button", title: "Cancel", width: 3
            input "bAdd", "button", title: "Add/Edit", width: 3
            input "bDel", "button", title: "Delete", width: 3
            input "bClear", "button", title: "Clear All", width: 3
            input "refreshPage", "bool", title: "Refresh Page", submitOnChange:true
            if(refreshPage) {
                app.updateSetting("refreshPage", [value: "false",type: "bool"])
            }
        }

        section() {
            paragraph "<hr>"
            if(state.calendarMap == null) {
                theMap = "No devices are setup"
            } else {
                if(logEnable) log.info "Making new Map display"
                theMap = "<table width=100%><tr><td><b>Title</b><td><b>S Date</b><td><b>S Time</b><td><b>E Date</b><td><b>E Time</b><td><b>Repeat<br>Every</b><td><b>Repeat<br>Max</b><td><b>Days</b><td><b>Year</b>"
                sortedMap = state.calendarMap.sort { a, b -> a.value <=> b.value }
                sortedMap.each { cm ->
                    mTitle = cm.key
                    try {
                        (mDate, mTime1, mTime2, mEDate, mETime1, mETime2, mDesc, mRepeatMinutes, mRepeatMax, mDays, mYearly) = cm.value.split(";")
                    } catch(e) {}
                    theMap += "<tr><td>$mTitle<td>$mDate<td>$mTime2<td>$mEDate<td>$mETime2<td>$mRepeatMinutes<td>$mRepeatMax<td>$mDays<td>$mYearly"
                    theMap += "<tr><td colspan=9> - <b>Description:</b> $mDesc"
                }
                theMap += "</table>"
            }
            paragraph "${theMap}"
            paragraph "<hr>"
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            if(useSpeech || sendPushMessage) {
                href "speechPushOptions", title:"${getImage("checkMarkGreen")} Speech/Push Options", description:"Click here for options"
            } else {
                href "speechPushOptions", title:"Speech/Push Options", description:"Click here for options"
            }
            input "theSwitches", "capability.switch", title: "Additional Switches to Turn On", required:false, multiple:true, submitOnChange:true
            paragraph "<small>* The switches selected here will automatically turn on with the Event trigger and then turn off when the Event has finished.</small>"
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Other Options")) {
            paragraph "Sometimes devices can miss commands due to HE's speed. This option will allow you to adjust the time between commands being sent."
            input "actionDelay", "number", title: "Delay (in milliseconds - 1000 = 1 second, 3 sec max)", range: '1..3000', defaultValue:actionDelayValue, required:false, submitOnChange:true
            input "updateTime", "time", title: "App has to check the calendar each morning, choose the Time to check", required:true, submitOnChange:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains("(Paused)")) {
                        app.updateLabel(app.label + " <span style='color:red'>(Paused)</span>")
                    }
                }
            } else {
                if(app.label) {
                    if(app.label.contains("(Paused)")) {
                        app.updateLabel(app.label - " <span style='color:red'>(Paused)</span>")
                    }
                }
            }
        }
        section() {
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            if(pauseApp) { 
                paragraph app.label
            } else {
                label title: "Enter a name for this automation", required:false
            }
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
            }
        }
		display2()
	}
}

def speechPushOptions(){
    dynamicPage(name: "speechPushOptions", title: "", install: false, uninstall:false){
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) { 
           paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true
            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required:true, submitOnChange:true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Messages")) {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple:true, required:false, submitOnChange:true
        }
        
        if(useSpeech || sendPushMessage) {
            wc = "<u>Optional wildcards:</u><br>"
            wc += "%title% - returns the Event Title<br>"
            wc += "%description% - returns the Event Description<br>"
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {
                paragraph "${wc}"
                input "messages", "text", title: "Random Message - Separate each message with <b>;</b> (semicolon)", required:true, submitOnChange:true
                input "msgList", "bool", defaultValue: true, title: "Show a list view of the messages?", description: "List View", submitOnChange:true
                if(msgList) {
                    def values = "${messages}".split(";")
                    listMap = ""
                    values.each { item -> listMap += "${item}<br>"}
                    paragraph "${listMap}"
                }
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Repeat Notifications Control")) {
                paragraph "If using the Repeat option with any Event Reminder, please use the ${dataDevice} device as a control switch. This switch will automatically turn on when the Event triggers and back off again when the Event has finished. If you want the Event to finish early, manually turn this switch off."
            }
        }
        display2()
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    unsubscribe()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite:false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite:false])
    if(logEnagle && logOffTime == "Keep On") unschedule(logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp) {
        log.info "${app.label} is Paused"
    } else {
        state.oldSwitchValue = null
        state.numOfRepeats = 1
        dateTime = updateTime.split("T")
        (updateHour, updateMin) = dateTime[1].split(":")
        theSchedule = "0 ${updateMin} ${updateHour} * * ? *"
        schedule(theSchedule, nextHandler)
        
        scheduleHandler()
        nextHandler()
    }
}

def startTheProcess(data) {
    if(logEnable) log.debug "******************** Start - startTheProcess (${state.version}) ********************"
    if(logEnable) log.debug "In startTheProcess (${state.version}) - data: ${data}"
    (nTitle, nStatus) = data.split(";")
    state.calendarMap.each { cm ->
        theTitle = cm.key
        if(theTitle == nTitle) {
            pauseExecution(actionDelay)
            dataDevice.$nStatus()
            if(theSwitches) {
                theSwitches.each { it ->
                    pauseExecution(actionDelay)
                    it.$nStatus()
                }
            }
            
            if(nStatus == "off") {
                state.oldSwitchValue = null
                state.numOfRepeats = 1
            }
            
            try {
                (theDate, theTime1, theTime2, eDate, eTime1, eTime2, theDesc, theRepeatMinutes, theRepeatMax, theDays, yearly) = cm.value.split(";")
            } catch(e) {}
            if(theDesc) { dataDevice.sendEvent(name: "currentEvent", value: theDesc, isStateChange: true) }
            if((useSpeech || sendPushMessage) && nStatus == "on") messageHandler(theTitle, theDesc)
        }
    }
    
    if(msgRepeat && nStatus == "on") {
        if(logEnable) log.debug "In startTheProcess - Repeat - numOfRepeats: ${state.numOfRepeats} - msgRepeatMax: ${theRepeatMax}"
        repeat = false
        if(state.numOfRepeats == null) state.numOfRepeats = 1
        if(state.numOfRepeats < msgRepeatMax) {
            if(state.numOfRepeats == 1) {
                pauseExecution(actionDelay)
                dataDevice.on()
            }
            repeat = dataDevice.currentValue("switch")
            if(repeat == "on") {
                if(logEnable) log.debug "In startTheProcess - repeat is ${repeat}"
                rTime = (theRepeatMinutes.toInteger() * 60)
                state.numOfRepeats += 1
                if(logEnable) log.debug "In startTheProcess - Repeat - Running again in ${theRepeatMinutes} minutes (${rTime})"
                runIn(rTime, startTheProcess, [data: theTitle]) 
            } else {
                state.oldSwitchValue = null
                state.numOfRepeats = 1
            }
        } else {
            pauseExecution(actionDelay)
            dataDevice.off()
            dataDevice.sendEvent(name: "currentEvent", value: "-", isStateChange: true)
            if(theSwitches) {
                theSwitches.each { it ->
                    pauseExecution(actionDelay)
                    it.off()
                }
            }
            state.oldSwitchValue = null
            state.numOfRepeats = 1
        }
    } else {
        if(logEnable) log.debug "In startTheProcess - No repeats today"
    }
    if(theDays != "-") { futureHandler(theTitle, theDate, theTime1, eDate, eTime1, theDays) }
    if(logEnable) log.debug "******************** End startTheProcess (${state.version}) ********************"
}

def messageHandler(theTitle, theDesc) {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    def mValues = "${messages}".split(";")
	mSize = mValues.size().toInteger()
    def randomKey = new Random().nextInt(mSize)
    state.message = mValues[randomKey]

    if(state.message.contains("%title%")) {state.message = state.message.replace('%title%', "${theTitle}")}
    if(state.message.contains("%description%")) {state.message = state.message.replace('%description%', "${theDesc}")}
    if(logEnable) log.debug "In messageHandler - message: ${state.message}"
    if(useSpeech) letsTalk(state.message)
    if(sendPushMessage) pushHandler(state.message)
}

def nextHandler() {
    if(logEnable) log.debug "In nextHandler (${state.version})"
    x = 1
    tDate = new Date()
    sortedMap = state.calendarMap.sort { a, b -> a.value <=> b.value }
    sortedMap.each { cm ->
        theTitle = cm.key
        try {
            (theDate, theTime1, theTime2, theDesc, theRepeatMinutes, theRepeatMax, theDays, yearly) = cm.value.split(";")
        } catch(e) {}
        (tYear, tMonth, tDay) = theDate.split("-")
        Date mDate = new Date("${tMonth}/${tDay}/${tYear}")
        if(mDate.after(tDate)) {
            if(x == 1) {
                nextEvent = "$theDate $theTime2 - $theDesc"
                nextThree = "$theDate $theTime2 - $theDesc<br>"
                dataDevice.sendEvent(name: "nextEvent", value: nextEvent, isStateChange: true)
            } else if(x <=3) {
                nextThree += "$theDate $theTime2 - $theDesc<br>"
                dataDevice.sendEvent(name: "nextThree", value: nextThree, isStateChange: true)
            }
            x += 1
        }
    }
}

def scheduleHandler() {
	if(logEnable) log.debug "In scheduleHandler (${state.version})"
    state.calendarMap.each { cm ->
        theTitle = cm.key
        try {
            (theDate, theTime1, theTime2, endDate, endTime1, endTime2, theDesc, theRepeatMinutes, theRepeatMax, theDays, yearly) = cm.value.split(";")
        } catch(e) {}
        (theYear, theMonth, theDay) = theDate.split("-")
        (theHour, theMin) = theTime1.split(":")
        
        (eYear, eMonth, eDay) = endDate.split("-")
        (eHour, eMin) = endTime1.split(":")

        Date checkDate = new Date("${theMonth}/${theDay}/${theYear}")
        Date now = new Date()
        if(yearly == "F") {    // Just THIS year
            if(checkDate.after(now)) {          
                startSchedule = "0 ${theMin} ${theHour} ${theDay} ${theMonth} ? ${theYear}"
                endSchedule = "0 ${eMin} ${eHour} ${eDay} ${eMonth} ? ${eYear}"
                if(logEnable) log.debug "In scheduleHandler - Setting schedule for START: ${theTitle}: 0 ${theMin} ${theHour} ${theDay} ${theMonth} ? ${theYear}"
                if(logEnable) log.debug "In scheduleHandler - Setting schedule for END: ${theTitle}: 0 ${eMin} ${eHour} ${eDay} ${eMonth} ? ${eYear}"
            }
        } else {               // Year after year after year
            startSchedule = "0 ${theMin} ${theHour} ${theDay} ${theMonth} ? *"
            endSchedule = "0 ${eMin} ${eHour} ${eDay} ${eMonth} ? *"
            if(logEnable) log.debug "In scheduleHandler - Setting schedule for START: ${theTitle}: 0 ${theMin} ${theHour} ${theDay} ${theMonth} ? *"
            if(logEnable) log.debug "In scheduleHandler - Setting schedule for END: ${theTitle}: 0 ${eMin} ${eHour} ${eDay} ${eMonth} ? *"
        }
                
        startStuff = "$theTitle;on"
        endStuff = "$theTitle;off"
        if(startSchedule && endSchedule) {
            schedule(startSchedule, startTheProcess, [data: startStuff, overwrite:false])
            schedule(endSchedule, startTheProcess, [data: endStuff, overwrite:false])
        }
    }
}

def futureHandler(theTitle, theDate, theTime1, eDate, eTime1, theDays) {
    if(logEnable) log.debug "In futureHandler (${state.version}) - theTitle: ${theTitle} - theDate: ${theDate} - theTime1: ${theTime1} - eDate: ${eDate} - eTime1: ${eTime1} - theDays: ${theDays}"
    hmd = theDays.toInteger()
	Date futureDate = new Date().plus(hmd)
    Date futureEndDate = new Date().plus(hmd)
    if(logEnable) log.debug "In futureHandler - theDays: ${theDays} - futureDate: ${futureDate} - futureEndDate: ${futureEndDate}"
    
    hmdMonth = futureDate.format("MM")
    hmdDay = futureDate.format("dd")
    hmdYear = futureDate.format("yyyy")
    (hmdHour, hmdMin) = theTime1.split(":")
    
    ehmdMonth = futureEndDate.format("MM")
    ehmdDay = futureEndDate.format("dd")
    ehmdYear = futureEndDate.format("yyyy")
    (ehmdHour, ehmdMin) = eTime1.split(":")

    hmdSchedule = "0 ${hmdMin} ${hmdHour} ${hmdDay} ${hmdMonth} ? ${hmdYear}"
    ehmdSchedule = "0 ${ehmdMin} ${ehmdHour} ${ehmdDay} ${ehmdMonth} ? ${ehmdYear}"
	if(logEnable) log.debug "In futureHandler - schedule START: 0 ${hmdMin} ${hmdHour} ${hmdDay} ${hmdMonth} ? ${hmdYear}"
    if(logEnable) log.debug "In futureHandler - schedule END: 0 ${ehmdMin} ${ehmdHour} ${ehmdDay} ${ehmdMonth} ? ${ehmdYear}"
    startStuff = "$theTitle;on"
    endStuff = "$theTitle;off"
    schedule(hmdSchedule, startTheProcess, [data: startStuff, overwrite:false])
    schedule(ehmdSchedule, startTheProcess, [data: endStuff, overwrite:false])
}

def appButtonHandler(buttonPressed) {
    if(logEnable) log.debug "In appButtonHandler (${state.version}) - Button Pressed: ${buttonPressed}"
    if(state.calendarMap == null) state.calendarMap = [:]

    if(buttonPressed == "bDel") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"       
        state.calendarMap.remove(theTitle)
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "bAdd") {
        
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"        
        if(logEnable) log.debug "In appButtonHandler - ADD - theDate: ${theDate} - theTime: ${theTime}"
        newDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", theTime)
        String timePart1 = newDate.format("HH:mm")      // 24 h
        String timePart2 = newDate.format("hh:mm a")    // 12 h
        if(logEnable) log.debug "In appButtonHandler - timePart1: ${timePart1} - timePart2: ${timePart2}"
        if(endTime == null) {
            use( TimeCategory ) { newEndDate = newDate + 1.minutes }
        } else {
            newEndDate = Date.parse("yyyy-MM-dd'T'HH:mm:ss", endTime)
        }
        String endTimePart1 = newEndDate.format("HH:mm")      // 24 h
        String endTimePart2 = newEndDate.format("hh:mm a")    // 12 h
        if(logEnable) log.debug "In appButtonHandler - endTimePart1: ${endTimePart1} - endTimePart2: ${endTimePart2}"
        if(endDate == null) endDate = theDate
        if(msgRepeatMinutes == null) msgRepeatMinutes = "-"
        if(msgRepeatMax == null) msgRepeatMax = "-"
        if(repeatInDays == null) repeatInDays = "-"
        if(theText == null || theText == "null") theText = "-"
        if(repeatYearly == null) repeatYearly = "F"
        if(repeatYearly == false) repeatYearly = "F"
        if(repeatYearly == true) repeatYearly = "T"
        
        if(logEnable) log.debug "In appButtonHandler - ${theDate} - ${timePart1} - ${timePart2} - ${endDate} - ${endTimePart1} - ${endTimePart2} - ${theText} - ${msgRepeatMinutes} - ${msgRepeatMax} - ${repeatInDays} - ${repeatYearly}"   
       //state.calendarMap.put(theTitle,"${theDate};${timePart1};${timePart2};${theText};${msgRepeatMinutes};${msgRepeatMax};${repeatInDays};${repeatYearly}")
        state.calendarMap.put(theTitle,"${theDate};${timePart1};${timePart2};${endDate};${endTimePart1};${endTimePart2};${theText};${msgRepeatMinutes};${msgRepeatMax};${repeatInDays};${repeatYearly}")
        if(logEnable) log.debug "In appButtonHandler - Finished Working"       
    } else if(buttonPressed == "bCancel") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    } else if(buttonPressed == "bClear") {
        if(logEnable) log.debug "In appButtonHandler - Working on: ${buttonPressed}"       
        state.calendarMap = [:]
        if(logEnable) log.debug "In appButtonHandler - Finished Working"
    }
}

def checkMapHandler() {
    if(logEnable) log.debug "In checkMapHandler (${state.version})" 
    if(state.calendarMap == null) state.calendarMap = [:]
    log.debug "calendarMap: ${state.calendarMap}"
    sortedMap = state.calendarMap.sort { a, b -> a.value <=> b.value }
    sortedMap.each { cm ->
        mTitle = cm.key
        if(logEnable) log.debug "In checkMapHandler - Checking Map line: ${cm.value}"
        (mDate, mTime1, mTime2, mDesc, mRepeatMinutes, mRepeatMax, mDays, mYearly) = cm.value.split(";")
        bulk = cm.value.split(";")
        bulkSize = bulk.size()
        if(logEnable) log.debug "In checkMapHandler - bulkSize: ${bulkSize}"
        if(bulkSize > 10) {
            if(logEnable) log.debug "In checkMapHandler - map is the right size, something else going on."
            return
        } else {
            if(logEnable) log.debug "In checkMapHandler - Fixing Map"
            mEDate = mDate
            endTime = "${mDate} ${mTime1}"
            newEndDate = Date.parse("yyyy-MM-dd HH:mm", endTime)

            String mETime1 = newEndDate.format("HH:mm")      // 24 h
            String mETime2 = newEndDate.format("hh:mm a")    // 12 h
            if(logEnable) log.debug "In checkMapHandler - mEDate: ${mEDate} - mETime1: ${mETime1} - mETime2: ${mETime2}"
            
            mRepeatMinutes = "-"
            mRepeatMax = "-"
            mDays = "-"        
            if(mYearly == "") mYearly = "F"
            if(mYearly == false) mYearly = "F"
            if(mYearly == true) mYearly = "T"

            if(logEnable) log.debug "In checkMapHandler - ${mDate}; ${mTime1}; ${mTime2}; ${mEDate}; ${mETime1}; ${mETime2}; ${mDesc}; ${mRepeatMinutes}; ${mRepeatMax}; ${mDays}; ${mYearly}"   
            state.calendarMap.put(mTitle,"${mDate};${mTime1};${mTime2};${mEDate};${mETime1};${mETime2};${mDesc};${mRepeatMinutes};${mRepeatMax};${mDays};${mYearly}")
        }
    }   
}

// ~~~~~ start include (2) BPTWorld.bpt-normalStuff ~~~~~
library ( // library marker BPTWorld.bpt-normalStuff, line 1
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-normalStuff, line 2
        category: "Apps", // library marker BPTWorld.bpt-normalStuff, line 3
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-normalStuff, line 4
        name: "bpt-normalStuff", // library marker BPTWorld.bpt-normalStuff, line 5
        namespace: "BPTWorld", // library marker BPTWorld.bpt-normalStuff, line 6
        documentationLink: "", // library marker BPTWorld.bpt-normalStuff, line 7
        version: "1.0.0", // library marker BPTWorld.bpt-normalStuff, line 8
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-normalStuff, line 9
) // library marker BPTWorld.bpt-normalStuff, line 10

import groovy.json.* // library marker BPTWorld.bpt-normalStuff, line 12
import hubitat.helper.RMUtils // library marker BPTWorld.bpt-normalStuff, line 13
import java.util.TimeZone // library marker BPTWorld.bpt-normalStuff, line 14
import groovy.transform.Field // library marker BPTWorld.bpt-normalStuff, line 15
import groovy.time.TimeCategory // library marker BPTWorld.bpt-normalStuff, line 16
import java.text.SimpleDateFormat // library marker BPTWorld.bpt-normalStuff, line 17

def checkForLibrary() { // library marker BPTWorld.bpt-normalStuff, line 19
    state.libraryInstalled = true // library marker BPTWorld.bpt-normalStuff, line 20
    state.libName = "BPTWorld-NormalStuff" // library marker BPTWorld.bpt-normalStuff, line 21
    state.libVersion = "1.0.0" // library marker BPTWorld.bpt-normalStuff, line 22
    sendLocationEvent(name: "updateVersionInfo", value: "${state.libName}:${state.libVersion}") // library marker BPTWorld.bpt-normalStuff, line 23
} // library marker BPTWorld.bpt-normalStuff, line 24

def checkHubVersion() { // library marker BPTWorld.bpt-normalStuff, line 26
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-normalStuff, line 27
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-normalStuff, line 28
    if(logEnable) log.debug "In checkHubVersion - Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-normalStuff, line 29
} // library marker BPTWorld.bpt-normalStuff, line 30

def parentCheck(){   // library marker BPTWorld.bpt-normalStuff, line 32
	state.appInstalled = app.getInstallationState()  // library marker BPTWorld.bpt-normalStuff, line 33
	if(state.appInstalled != 'COMPLETE'){ // library marker BPTWorld.bpt-normalStuff, line 34
		parentChild = true // library marker BPTWorld.bpt-normalStuff, line 35
  	} else { // library marker BPTWorld.bpt-normalStuff, line 36
    	parentChild = false // library marker BPTWorld.bpt-normalStuff, line 37
  	} // library marker BPTWorld.bpt-normalStuff, line 38
} // library marker BPTWorld.bpt-normalStuff, line 39

def createDeviceSection(driverName) { // library marker BPTWorld.bpt-normalStuff, line 41
    paragraph "This child app needs a virtual device to store values. Remember, multiple child apps can share this device if needed." // library marker BPTWorld.bpt-normalStuff, line 42
    input "useExistingDevice", "bool", title: "Use existing device (off) or have one created for you (on)", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 43
    if(useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 44
        input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Front Door')", required:true, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 45
        paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>" // library marker BPTWorld.bpt-normalStuff, line 46
        if(dataName) createDataChildDevice(driverName) // library marker BPTWorld.bpt-normalStuff, line 47
        if(statusMessageD == null) statusMessageD = "Waiting on status message..." // library marker BPTWorld.bpt-normalStuff, line 48
        paragraph "${statusMessageD}" // library marker BPTWorld.bpt-normalStuff, line 49
    } // library marker BPTWorld.bpt-normalStuff, line 50
    input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false // library marker BPTWorld.bpt-normalStuff, line 51
    if(!useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 52
        app.removeSetting("dataName") // library marker BPTWorld.bpt-normalStuff, line 53
        paragraph "<small>* Device must use the '${driverName}'.</small>" // library marker BPTWorld.bpt-normalStuff, line 54
    } // library marker BPTWorld.bpt-normalStuff, line 55
} // library marker BPTWorld.bpt-normalStuff, line 56

def createDataChildDevice(driverName) {     // library marker BPTWorld.bpt-normalStuff, line 58
    if(logEnable) log.debug "In createDataChildDevice (${state.version})" // library marker BPTWorld.bpt-normalStuff, line 59
    statusMessageD = "" // library marker BPTWorld.bpt-normalStuff, line 60
    if(!getChildDevice(dataName)) { // library marker BPTWorld.bpt-normalStuff, line 61
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}" // library marker BPTWorld.bpt-normalStuff, line 62
        try { // library marker BPTWorld.bpt-normalStuff, line 63
            addChildDevice("BPTWorld", driverName, dataName, 1234, ["name": "${dataName}", isComponent: false]) // library marker BPTWorld.bpt-normalStuff, line 64
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})" // library marker BPTWorld.bpt-normalStuff, line 65
            statusMessageD = "<b>Device has been been created. (${dataName})</b>" // library marker BPTWorld.bpt-normalStuff, line 66
        } catch (e) { if(logEnable) log.debug "Unable to create device - ${e}" } // library marker BPTWorld.bpt-normalStuff, line 67
    } else { // library marker BPTWorld.bpt-normalStuff, line 68
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>" // library marker BPTWorld.bpt-normalStuff, line 69
    } // library marker BPTWorld.bpt-normalStuff, line 70
    return statusMessageD // library marker BPTWorld.bpt-normalStuff, line 71
} // library marker BPTWorld.bpt-normalStuff, line 72

def uninstalled() { // library marker BPTWorld.bpt-normalStuff, line 74
    sendLocationEvent(name: "updateVersionInfo", value: "${app.id}:remove") // library marker BPTWorld.bpt-normalStuff, line 75
	removeChildDevices(getChildDevices()) // library marker BPTWorld.bpt-normalStuff, line 76
} // library marker BPTWorld.bpt-normalStuff, line 77

private removeChildDevices(delete) { // library marker BPTWorld.bpt-normalStuff, line 79
	delete.each {deleteChildDevice(it.deviceNetworkId)} // library marker BPTWorld.bpt-normalStuff, line 80
} // library marker BPTWorld.bpt-normalStuff, line 81

def letsTalk(msg) { // library marker BPTWorld.bpt-normalStuff, line 83
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 84
    if(useSpeech && fmSpeaker) { // library marker BPTWorld.bpt-normalStuff, line 85
        fmSpeaker.latestMessageFrom(state.name) // library marker BPTWorld.bpt-normalStuff, line 86
        fmSpeaker.speak(msg,null) // library marker BPTWorld.bpt-normalStuff, line 87
    } // library marker BPTWorld.bpt-normalStuff, line 88
} // library marker BPTWorld.bpt-normalStuff, line 89

def pushHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 91
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 92
    theMessage = "${app.label} - ${msg}" // library marker BPTWorld.bpt-normalStuff, line 93
    if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}" // library marker BPTWorld.bpt-normalStuff, line 94
    sendPushMessage.deviceNotification(theMessage) // library marker BPTWorld.bpt-normalStuff, line 95
} // library marker BPTWorld.bpt-normalStuff, line 96

def useWebOSHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 98
    if(logEnable) log.debug "In useWebOSHandler (${state.version}) - Sending to webOS - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 99
    useWebOS.deviceNotification(msg) // library marker BPTWorld.bpt-normalStuff, line 100
} // library marker BPTWorld.bpt-normalStuff, line 101

def logsOff() { // library marker BPTWorld.bpt-normalStuff, line 103
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-normalStuff, line 104
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-normalStuff, line 105
} // library marker BPTWorld.bpt-normalStuff, line 106

def checkEnableHandler() { // library marker BPTWorld.bpt-normalStuff, line 108
    state.eSwitch = false // library marker BPTWorld.bpt-normalStuff, line 109
    if(disableSwitch) {  // library marker BPTWorld.bpt-normalStuff, line 110
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-normalStuff, line 111
        disableSwitch.each { it -> // library marker BPTWorld.bpt-normalStuff, line 112
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-normalStuff, line 113
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-normalStuff, line 114
        } // library marker BPTWorld.bpt-normalStuff, line 115
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-normalStuff, line 116
    } // library marker BPTWorld.bpt-normalStuff, line 117
} // library marker BPTWorld.bpt-normalStuff, line 118

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 120
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-normalStuff, line 121
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-normalStuff, line 122
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 123
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 124
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 125
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 126
    if(type == "logo") return "${loc}logo.png height=60>" // library marker BPTWorld.bpt-normalStuff, line 127
} // library marker BPTWorld.bpt-normalStuff, line 128

def getFormat(type, myText="") {			// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 130
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>" // library marker BPTWorld.bpt-normalStuff, line 131
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>" // library marker BPTWorld.bpt-normalStuff, line 132
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-normalStuff, line 133
} // library marker BPTWorld.bpt-normalStuff, line 134

def display(data) { // library marker BPTWorld.bpt-normalStuff, line 136
    if(data == null) data = "" // library marker BPTWorld.bpt-normalStuff, line 137
    setVersion() // library marker BPTWorld.bpt-normalStuff, line 138
    getHeaderAndFooter() // library marker BPTWorld.bpt-normalStuff, line 139
    if(app.label) { // library marker BPTWorld.bpt-normalStuff, line 140
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-normalStuff, line 141
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-normalStuff, line 142
        } else { // library marker BPTWorld.bpt-normalStuff, line 143
            theName = app.label // library marker BPTWorld.bpt-normalStuff, line 144
        } // library marker BPTWorld.bpt-normalStuff, line 145
    } // library marker BPTWorld.bpt-normalStuff, line 146
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-normalStuff, line 147
    if(state.name == theName) { // library marker BPTWorld.bpt-normalStuff, line 148
        section (getFormat("title", "${getImage("logo")}" + " ${state.name}")) {} // library marker BPTWorld.bpt-normalStuff, line 149
    } else { // library marker BPTWorld.bpt-normalStuff, line 150
        section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {} // library marker BPTWorld.bpt-normalStuff, line 151
    } // library marker BPTWorld.bpt-normalStuff, line 152
    section() { // library marker BPTWorld.bpt-normalStuff, line 153
        paragraph "${state.headerMessage}" // library marker BPTWorld.bpt-normalStuff, line 154
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 155
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 156
    } // library marker BPTWorld.bpt-normalStuff, line 157
} // library marker BPTWorld.bpt-normalStuff, line 158

def display2() { // library marker BPTWorld.bpt-normalStuff, line 160
    section() { // library marker BPTWorld.bpt-normalStuff, line 161
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" } // library marker BPTWorld.bpt-normalStuff, line 162
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 163
        if(state.version) { // library marker BPTWorld.bpt-normalStuff, line 164
            paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>" // library marker BPTWorld.bpt-normalStuff, line 165
        } else { // library marker BPTWorld.bpt-normalStuff, line 166
            paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name}</div>" // library marker BPTWorld.bpt-normalStuff, line 167
        } // library marker BPTWorld.bpt-normalStuff, line 168
        paragraph "${state.footerMessage}" // library marker BPTWorld.bpt-normalStuff, line 169
    } // library marker BPTWorld.bpt-normalStuff, line 170
} // library marker BPTWorld.bpt-normalStuff, line 171

def getHeaderAndFooter() { // library marker BPTWorld.bpt-normalStuff, line 173
    timeSinceNewHeaders() // library marker BPTWorld.bpt-normalStuff, line 174
    if(state.checkNow == null) state.checkNow = true // library marker BPTWorld.bpt-normalStuff, line 175
    if(state.totalHours > 6 || state.checkNow) { // library marker BPTWorld.bpt-normalStuff, line 176
        def params = [ // library marker BPTWorld.bpt-normalStuff, line 177
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json", // library marker BPTWorld.bpt-normalStuff, line 178
            requestContentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 179
            contentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 180
            timeout: 10 // library marker BPTWorld.bpt-normalStuff, line 181
        ] // library marker BPTWorld.bpt-normalStuff, line 182
        try { // library marker BPTWorld.bpt-normalStuff, line 183
            def result = null // library marker BPTWorld.bpt-normalStuff, line 184
            httpGet(params) { resp -> // library marker BPTWorld.bpt-normalStuff, line 185
                state.headerMessage = resp.data.headerMessage // library marker BPTWorld.bpt-normalStuff, line 186
                state.footerMessage = resp.data.footerMessage // library marker BPTWorld.bpt-normalStuff, line 187
            } // library marker BPTWorld.bpt-normalStuff, line 188
        } catch (e) { } // library marker BPTWorld.bpt-normalStuff, line 189
    } // library marker BPTWorld.bpt-normalStuff, line 190
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>" // library marker BPTWorld.bpt-normalStuff, line 191
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>" // library marker BPTWorld.bpt-normalStuff, line 192
} // library marker BPTWorld.bpt-normalStuff, line 193

def timeSinceNewHeaders() {  // library marker BPTWorld.bpt-normalStuff, line 195
    if(state.previous == null) {  // library marker BPTWorld.bpt-normalStuff, line 196
        prev = new Date() // library marker BPTWorld.bpt-normalStuff, line 197
    } else { // library marker BPTWorld.bpt-normalStuff, line 198
        try { // library marker BPTWorld.bpt-normalStuff, line 199
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // library marker BPTWorld.bpt-normalStuff, line 200
            prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000")) // library marker BPTWorld.bpt-normalStuff, line 201
        } catch(e) { // library marker BPTWorld.bpt-normalStuff, line 202
            prev = state.previous // library marker BPTWorld.bpt-normalStuff, line 203
        } // library marker BPTWorld.bpt-normalStuff, line 204
    } // library marker BPTWorld.bpt-normalStuff, line 205
    def now = new Date() // library marker BPTWorld.bpt-normalStuff, line 206
    use(TimeCategory) { // library marker BPTWorld.bpt-normalStuff, line 207
        state.dur = now - prev // library marker BPTWorld.bpt-normalStuff, line 208
        state.days = state.dur.days // library marker BPTWorld.bpt-normalStuff, line 209
        state.hours = state.dur.hours // library marker BPTWorld.bpt-normalStuff, line 210
        state.totalHours = (state.days * 24) + state.hours // library marker BPTWorld.bpt-normalStuff, line 211
    } // library marker BPTWorld.bpt-normalStuff, line 212
    state.previous = now // library marker BPTWorld.bpt-normalStuff, line 213
} // library marker BPTWorld.bpt-normalStuff, line 214

// ~~~~~ end include (2) BPTWorld.bpt-normalStuff ~~~~~
