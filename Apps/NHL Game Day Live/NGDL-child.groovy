/**
 *  **************** MLB Game Day Live Child App  ****************
 *
 *  Design Usage:
 *  Follow your favorite MLB teams Game Day Live, put a Scoreboard right on your Dashboard!
 *
 *  Copyright 2020 Bryan Turcotte (@bptworld)
 * 
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
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
 *  1.0.8 - 08/28/20 - Fixed a typo
 *  1.0.7 - 08/27/20 - Lots of little changes
 *  1.0.6 - 08/24/20 - Separate options for devices on when Score and/or Final, other enhancements
 *  ---
 *  1.0.0 - 07/21/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "MLB Game Day Live"
	state.version = "1.0.8"
}

definition(
    name: "MLB Game Day Live Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Follow your favorite MLB teams Game Day Live, put a Scoreboard right on your Dashboard!",
    category: "Convenience",
	parent: "BPTWorld:MLB Game Day Live",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/MLB%20Game%20Day%20Live/MGDL-child.groovy",
)

preferences {
    page(name: "pageConfig")
    page name: "notificationOptions", title: "", install: false, uninstall: false, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        urlSetup()
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Follow your favorite MLB teams Game Day Live, put a Scoreboard right on your Dashboard!"
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Virtual Device")) {
            paragraph "Each child app needs a virtual device to store the game data."
            input "useExistingDevice", "bool", title: "Use existing device (off) or have MLB GDL create a new one for you (on)", defaultValue:false, submitOnChange:true
            if(useExistingDevice) {
			    input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'GDL - Red Sox')", required:true, submitOnChange:true
                paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>"
                if(dataName) createDataChildDevice()
                if(statusMessageD == null) statusMessageD = "Waiting on status message..."
                paragraph "${statusMessageD}"
            }
            input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false
            if(!useExistingDevice) {
                app.removeSetting("dataName")
                paragraph "<small>* Device must use the 'MLB Game Day Live Driver'.</small>"
            }
        }      
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Your Team")) {
            input "mlbTeam", "enum", title: "Select MLB Team", required: true, displayDuringSetup: true, options: getTeamList()
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {
            if(useSpeech || pushMessage) {
                href "notificationOptions", title:"${getImage("checkMarkGreen")} Run Notifications", description:"Click here for options"
            } else {
                href "notificationOptions", title:"Run Notifications", description:"Click here for options"
            }
        }
    
        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "serviceStartTime", "time", title: "Check for Games Daily at", required: false
            input "serviceStartTime2", "time", title: "Update the Schedule Daily at", required: false
            label title: "Enter a name for this automation", required:false, submitOnChange:true
            input "logEnable","bool", title: "Enable Debug Logging and Test Buttons", description: "Debugging", defaultValue: false, submitOnChange: true
            if(logEnable) {
                input "testOtherScore", "button", title: "Test otherTeam Score", width:4
                input "testMyTeamScore", "button", title: "Test myTeam Score", width:4
            }
		}
		display2()
	}
}

def notificationOptions(){
    dynamicPage(name: "notificationOptions", title: "", install: false, uninstall:false){
        section(getFormat("header-green", "${getImage("Blank")}"+" Speech Options")) { 
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-follow-me-speaker-control-with-priority-messaging-volume-controls-voices-and-sound-files/12139' target=_blank>Follow Me</a> to process Notifications.  Please be sure to have Follow Me installed before trying to send any notifications."
            input "useSpeech", "bool", title: "Use Speech through Follow Me", defaultValue:false, submitOnChange:true

            if(useSpeech) input "fmSpeaker", "capability.speechSynthesis", title: "Select your Follow Me device", required: true, submitOnChange:true
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Push Options")) { 
            input "pushMessage", "capability.notification", title: "Send a Push notification to certain users", multiple:true, required:false, submitOnChange:true
        }
        
        if(useSpeech || pushMessage) {
            section(getFormat("header-green", "${getImage("Blank")}"+" Message Options")) {}
            if(useSpeech) {
                section("Instructions for Message Priority Features of Follow Me:", hideable: true, hidden: true) {
                    paragraph "Message Priority is a unique feature only found with 'Follow Me'! Simply place the option bracket in front of any message to be spoken and the Volume, Voice and/or Speaker will be adjusted accordingly."
                    paragraph "Format: [priority:sound:speaker]<br><small>Note: Any option not needed, replace with a 0 (zero).</small>"

                    paragraph "<b>Priority:</b><br>This can change the voice used and the color of the message displayed on the Dashboard Tile.<br>[F:0:0] - Fun<br>[R:0:0] - Random<br>[L:0:0] - Low<br>[N:0:0] - Normal<br>[H:0:0] - High"

                    paragraph "<b>Sound:</b><br>You can also specify a sound file to be played before a message!<br>[1] - [5] - Specify a files URL"
                    paragraph "<b>ie.</b> [L:0:0]Amy is home or [N:3:0]Window has been open too long or [H:0:0]Heat is on and window is open"
                    paragraph "If you JUST want a sound file played with NO speech after, use [L:1:0]. or [N:3:0]. etc. Notice the DOT after the [], that is the message and will not be spoken."

                    paragraph "<b>Speaker:</b><br>While Follow Me allows you to setup your speakers in many ways, sometimes you want it to ONLY speak on a specific device. This option will do just that! Just replace with the corresponding speaker number from the Follow Me Parent App."
                    paragraph "<b>*</b> <i>Be sure to have the 'Priority Speaker Options' section completed in the Follow Me Parent App.</i>"

                    paragraph "<hr>"
                    paragraph "<b>General Notes:</b>"
                    paragraph "Priority Voice and Sound options are only available when using Speech Synth option.<br>Also notice there is no spaces between the option and the message."
                    paragraph "<b>ie.</b> [N:3:0]Window has been open too long"
                }
            }
            
            section() {
                paragraph "Wildcards:<br>%myTeam% - Will be replaced with MY Team Name<br>%otherTeam% - will be replaced with the OTHER Team Name<br>%myTeamScore% - will be replaced with MY Team Score<br>%otherTeamScore% - will be replaced with the OTHER Team Score"
                paragraph "<hr>"
                input "myTeamScore", "text", title: "Message when My Team scores - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                input "myTeamScoreList", "bool", title: "Show a list view of the messages?", description: "List View", defaultValue:false, submitOnChange:true
                if(myTeamScoreList) {
                    def myTeamList = "${myTeamScore}".split(";")
                    theListH = ""
                    myTeamList.each { item -> theListH += "${item}<br>"}
                    paragraph "${theListH}"
                }

                input "otherTeamScore", "text", title: "Message when the Other Team scores - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                input "otherTeamScoreList", "bool", title: "Show a list view of the messages?", description: "List View", defaultValue:false, submitOnChange:true
                if(otherTeamScoreList) {
                    def otherList = "${otherTeamScore}".split(";")
                    theListA = ""
                    otherList.each { item -> theListA += "${item}<br>"}
                    paragraph "${theListA}"
                }
                
                paragraph "<hr>"
                input "pregameMessage", "text", title: "Pregame message (optional) - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                paragraph "<small>* Message will be spoken/pushed 15 minutes prior to game time</small>"
                input "pregameList", "bool", title: "Show a list view of the messages?", description: "List View", defaultValue:false, submitOnChange:true
                if(pregameList) {
                    def pList = "${pregameMessage}".split(";")
                    theListP = ""
                    pList.each { item -> theListP += "${item}<br>"}
                    paragraph "${theListP}"
                }
                
                paragraph "<hr>"
                input "postgameMessage", "text", title: "Postgame message (optional) - Separate each message with <b>;</b> (semicolon)", required:false, submitOnChange:true
                input "postgameList", "bool", title: "Show a list view of the messages?", description: "List View", defaultValue:false, submitOnChange:true
                if(postgameList) {
                    def ptList = "${postgameMessage}".split(";")
                    theListPt = ""
                    ptList.each { item -> theListPt += "${item}<br>"}
                    paragraph "${theListPt}"
                }
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {
            input "onScore", "bool", title: "Turn devices on for Score", description: "onScore", defaultValue:false, submitOnChange:true
            if(onScore) {
                input "howLongLightsOn", "number", title: "How long should the light stay on (in seconds)", defaultValue:10, requied: false, submitOnChange:true
            }
            
            input "onFinal", "bool", title: "Turn devices on for Final", description: "onFinal", defaultValue:false, submitOnChange:true          
            if(onFinal) {
                input "leaveOn", "bool", title: "Turn off after set time (off) - Leave on until turned off manually (on)", defaultValue:false, submitOnChange:true
                if(leaveOn) {
                    paragraph "<small>* Light will stay on until manually turned off.</small>"
                    app.removeSetting("leaveOnTime")
                } else {
                    input "leaveOnTime", "number", title: "Leave on for (minutes)", submitOnChange:true
                }
            }
            
            if(onScore || onFinal) {
                input "switchesOnMyTeam", "capability.switch", title: "Turn this switch ON when My Team Scores/Wins", required: false, submitOnChange: true
                if(switchesOnMyTeam) {    
                    if(switchesOnMyTeam.hasCommand('setColor')) {                   
                        input "colorMT", "enum", title: "Color", required: true, multiple:false, options: [
                            ["Soft White":"Soft White - Default"],
                            ["White":"White - Concentrate"],
                            ["Daylight":"Daylight - Energize"],
                            ["Warm White":"Warm White - Relax"],
                            "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                    }
                }

                input "switchesOnOtherTeam", "capability.switch", title: "Turn this switch ON when the Other Team Scores/Wins", required: false, submitOnChange: true
                if(switchesOnOtherTeam) {
                    if(switchesOnOtherTeam.hasCommand('setColor')) {
                        input "colorOT", "enum", title: "Color", required: true, multiple:false, options: [
                            ["Soft White":"Soft White - Default"],
                            ["White":"White - Concentrate"],
                            ["Daylight":"Daylight - Energize"],
                            ["Warm White":"Warm White - Relax"],
                            "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
                    }
                }         
            }
        }
                
        section(getFormat("header-green", "${getImage("Blank")}"+" Flash Lights Options")) {
            paragraph "All BPTWorld Apps use <a href='https://community.hubitat.com/t/release-the-flasher-flash-your-lights-based-on-several-triggers/30843' target=_blank>The Flasher</a> to process Flashing Lights.  Please be sure to have The Flasher installed before trying to use this option."
            input "useTheFlasher", "bool", title: "Use The Flasher", defaultValue:false, submitOnChange:true
            if(useTheFlasher) {
                input "theFlasherDevice", "capability.actuator", title: "The Flasher Device containing the Presets you wish to use", required:false, multiple:false
                input "flashMyTeamScorePreset", "number", title: "Select the Preset to use when My Team scores (1..5)", required:false, submitOnChange:true
                input "flashOtherTeamScorePreset", "number", title: "Select the Preset to use when the Other Team scores (1..5)", required:false, submitOnChange:true
                input "flashMyTeamWinsPreset", "number", title: "Select the Preset to use when the My Team wins (1..5)", required:false, submitOnChange:true
                input "flashOtherTeamWinsPreset", "number", title: "Select the Preset to use when the Other Team wins (1..5)", required:false, submitOnChange:true
            }
        }
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
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        urlSetup()
        getTeamInfo()

        schedule(serviceStartTime, startGameDay, [overwrite: false])
        schedule(serviceStartTime2, checkSchedule, [overwrite: false])
        checkSchedule()
        startGameDay()
    }
}

def uninstalled() {
	removeChildDevices(getChildDevices())
}

private removeChildDevices(delete) {
	delete.each {deleteChildDevice(it.deviceNetworkId)}
}

def urlSetup() {  // Modified from code by Eric Luttmann
    if(logEnable) log.debug "Initialize static states"

    state.MLB_URL = "http://statsapi.mlb.com"
    state.MLB_API = "/api/v1"
    state.MLB_API_URL = "${state.MLB_URL}${state.MLB_API}"
    
    // lets make the url
    // http://statsapi.mlb.com/api/v1/schedule/games/?sportId=1&date=04/10/2018
}

def getTeamList() {  // Modified from code by Eric Luttmann
    try {
	    def teams = []
        def params = [
            uri: "${state.MLB_API_URL}/teams/?sportId=1",
        ]
        if(state.teamList == null) {
            if(logEnable) log.debug "In getTeamList - httpGet: ${params}"
            httpGet(params) { resp ->
                def json = resp.data
                for (rec in json.teams) {
                    teams += rec.teamName
                } 
            }
            
    		state.teamList = teams
            state.prevTeamList = teams
		    if(logEnable) log.debug "In getTeamList - New Team List: ${state.teamList}"
       } else {
		    if(logEnable) log.debug "In getTeamList - Use existing team list"
       }
    } catch (e) {
        if(logEnable) log.warn "In getTeamList - Something went wrong, error to follow"
        log.error e
    }

    if(state.teamList == null) {
    	if(state.prevTeamList == null) {
            if(logEnable) log.debug "In getTeamList - Initialize team list"
            state.teamList = []
        } else {
            if(logEnable) log.debug "In getTeamList - Reset to previous Team list"
    		state.teamList = state.prevTeamList
        }
    }    
    return state.teamList.sort()
}

def getTeamInfo() {  // Modified from code by Eric Luttmann
    if(logEnable) log.debug "In getTeamInfo - Setup for team ${settings.mlbTeam}"
    def found = false
    def params = [
        uri: "${state.MLB_API_URL}/teams/?sportId=1",
    ]
    try {
        if(logEnable) log.debug "httpGet: ${params}"
        httpGet(params) { resp ->
            def json = resp.data
            for(rec in json.teams) {
                if(settings.mlbTeam == rec.teamName) {
                    state.Team = rec
                    if(logEnable) log.debug "In getTeamInfo - Found info on team ${state.Team.teamName}, id=${state.Team.id}"
                    found = true
                    break
                }
            } 
        }
    } catch (e) {
        if(logEnable) log.warn "In getTeamInfo - Something went wrong, error to follow."
        log.error e
    }

    if(!found) {
        if(logEnable) log.debug "Unable to find team, trying again in 30 seconds"
        def now = new Date()
        def runTime = new Date(now.getTime() + (30 * 1000))
        runOnce(runTime, getTeamInfo)
    }
}

def startGameDay() {  // Modified from code by Eric Luttmann
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In startGameDay (${state.version})"
        if(checkIfGameDay()) {
            gameStart = Date.parse("yyyy-MM-dd'T'HH:mm:ssX", state.gameDate)

            if(gameStart) {
                def now = new Date()
                def gameTime = new Date(gameStart.getTime())           

                // check for pregame message
                if(pregameMessage) {
                    def minutesBefore = 15
                    def pregameTime = new Date(gameStart.getTime() - ((minutesBefore * 60) * 1000))
                    if(logEnable) log.debug "In startGameDay - Schedule pregame message for ${app.label} at ${pregameTime.format('h:mm:ss a', location.timeZone)}"
                    runOnce(pregameTime, pregameMessageHandler, [overwrite: false])
                }
                state.todaysGameTime = "${gameTime.format('h:mm:ss a', location.timeZone)}"
                runOnce(gameTime, checkLiveGameStats, [overwrite: false])
            } else {
                if(logEnable) log.debug "In startGameDay - Unable to retrieve game time from ${app.label}"
            }
        } else {
            if(logEnable) log.debug "In startGameDay - Today is not a gameday for the ${settings.mlbTeam}"
        }
    }
}

def checkIfGameDay() {  // Modified from code by Eric Luttmann
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkIfGameDay (${state.version})"
        def isGameDay = false
        try {
            def todayDate = new Date().format('yyyy-MM-dd', location.timeZone)
            // http://statsapi.mlb.com/api/v1/schedule/games/?sportId=1&teamId=111&date=07/23/2020
            log.trace "todayDate: ${todayDate}"
            def params = [uri: "${state.MLB_API_URL}/schedule/games/?sportId=1&teamId=${state.Team.id}&date=${todayDate}"] 
            if(logEnable) log.debug "In checkIfGameDay - ${params}"
            
            def tDate = new Date().format('MM-dd-yyyy', location.timeZone)
            if(logEnable) log.debug "In checkIfGameDay - Determine if it's a game day for the ${settings.mlbTeam}, requesting game day schedule for ${tDate}"
            httpGet(params) { resp ->
                isGameDay = checkIfGameDayHandler(resp,tDate)
            }
        } catch (e) {
            if(logEnable) log.warn "In checkIfGameDay - Something went wrong, error to follow"
            log.error e
        }

        if(logEnable) log.debug "In checkIfGameDay - isGameDay: ${isGameDay}"
        return isGameDay
    }
}

def checkIfGameDayHandler(resp,gDate) {  // Modified from code by Eric Luttmann
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkIfGameDayHandler (${state.version})"
        def isGameDay = false

        if(resp.status == 200) {
            def result = resp.data
            state.homeTeam = null

            for(date in result.dates) {
                for(game in date.games) {
                    isGameDay = true
                    state.gameLink = game.link
                    state.gameDate = game.gameDate               
                    if(logEnable) log.info "A game is scheduled for ${gDate} - ${game.teams.away.team.name} vs ${game.teams.home.team.name}"

                    if(dataDevice) {        // NEW - only in BPTWorld's Hubitat version!                                  
                        state.homeTeam = "${game.teams.home.team.name}"
                        state.awayTeam = "${game.teams.away.team.name}"
                        
                        if(logEnable) log.info "In checkIfGameDayHandler - awayTeam: ${state.awayTeam} vs homeTeam: ${state.homeTeam}"
                        if(logEnable) log.info "In checkIfGameDayHandler - state.Team.teamName: ${state.Team.teamName}"
                        
                        if(state.awayTeam.contains(state.Team.teamName)) {
                            state.myTeamIs = "away"
                        } else {
                            state.myTeamIs = "home"
                        }

                        state.awayLogo = "www.mlbstatic.com/team-logos/team-cap-on-light/${game.teams.away.team.id}.svg"
                        state.homeLogo = "www.mlbstatic.com/team-logos/team-cap-on-light/${game.teams.home.team.id}.svg"

                        awayTeamWins = game.teams.away.leagueRecord.wins ?: "0"
                        awayTeamLosses = game.teams.away.leagueRecord.losses ?: "0"
                        homeTeamWins = game.teams.home.leagueRecord.wins ?: "0"
                        homeTeamLosses = game.teams.home.leagueRecord.losses ?: "0"

                        state.awayRecord = " (${awayTeamWins} - ${awayTeamLosses})"
                        state.homeRecord = " (${homeTeamWins} - ${homeTeamLosses})"

                        gameStats = "${state.awayTeam};${state.homeTeam};0;0;1"
                        checkLiveGameStats()

                        if(logEnable) log.debug "In checkIfGameDayHandler - Game Day! - My Team is ${state.myTeamIs}"
                    }
                    break
                }
            } 
            if(isGameDay == false) {
                if(logEnable) log.debug "In checkIfGameDayHandler - No Game Today"
                lastUpdated = new Date().format('MM-dd-yyyy  h:mm:ss a', location.timeZone)
                scoreBoard =  "<table align=center width=100%><tr align=center><td colspan=13>" 

                scoreBoard += "<table width=100%>"
                scoreBoard += "<tr style='text-align:center;font-weight:bold'>"
                scoreBoard += "<td> - "
                scoreBoard += "<td>No Game Today<br><small>${lastUpdated}</small>"
                scoreBoard += "<td> - "
                scoreBoard += "</table>"

                scoreBoard += "<tr style='text-align:center;font-weight:bold'><td width=40%>Teams<td> 1 <td> 2 <td> 3 <td> 4 <td> 5 <td> 6 <td> 7 <td> 8 <td> 9 <td> R <td> H <td> E "

                scoreBoard += "<tr style='text-align:center'><td><b> - </b> -<td> - <td>-<td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - "

                scoreBoard += "<tr style='text-align:center'><td><b> - </b>  - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - <td> - "

                scoreBoard += "</table>"

                theLastPlay = "<table width=100%>"
                theLastPlay += "<tr><td style='text-align:center'><b>Inning:</b> - of the - | <b>At Bat:</b> - | <b>Balls:</b> - | <b>Strikes:</b> - | <b>Outs:</b> -"
                theLastPlay += "<tr style='text-align:left'><td><b>Description:</b> No Game Today"
                theLastPlay += "</table>"

                if(dataDevice) {
                    dataDevice.liveScoreboard(scoreBoard)
                    gameStats = "-;-;-;-;-"
                    dataDevice.gameStats(gameStats)
                    dataDevice.lastPlay(theLastPlay)
                    if(logEnable) log.debug "In checkLiveGameStatsHandler - Data sent"
                }
            }
        } else {
            if(logEnable) log.error "${app.label}: resp.status = ${resp.status}"
        }
        return isGameDay
    }
}

def checkLiveGameStats() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkLiveGameStats (${state.version})"
        try {
            def todayDate = new Date().format('yyyy-MM-dd', location.timeZone)
            if(settings.debugCheckDate) {
                todayDate = settings.debugCheckDate
            }

            //def params = [uri: "http://statsapi.mlb.com/api/v1.1/game/529568/feed/live"]
            def params = [uri: "${state.MLB_URL}${state.gameLink}"] 
            if(logEnable) log.debug "In checkLiveGameStats - ${params}"

            def tDate = new Date().format('MM-dd-yyyy', location.timeZone)
            if(logEnable) log.debug "Requesting ${settings.mlbTeam} game stats for ${tDate}"
            asynchttpGet("checkLiveGameStatsHandler", params)
        } catch (e) {
            if(logEnable) log.warn "In checkLiveGameStats - Something went wrong, error to follow"
            log.error e
        }
        
        if(state.gameStatus == "Preview") {
            if(logEnable) log.debug "In checkLiveGameStats - Game status: ${state.gameStatus}."
            runIn(60, checkLiveGameStats)
        } else if(state.gameStatus == "Final") {
            if(logEnable) log.debug "In checkLiveGameStats - Game status: ${state.gameStatus}."
            messageHandler(postgameMessage)
            data = "final;final"
            notificationHandler(data)
        } else {
            if(latestPlay) {
                if(latestPlay.contains("Delayed: Rain") || latestPlay.contains("Postponed")) {
                    log.info "${app.label} - Game under delay, will check again in 20 minutes."
                    rainMessage = "Rain Delay"
                    messageHandler(rainMessage)
                    if(useSpeech) letsTalk()
                    if(pushMessage) pushNow()
                    runIn(1200, checkLiveGameStats)
                } else {              
                    if(logEnable) log.debug "In checkLiveGameStats - Game status: ${state.gameStatus}. Updated score: Home: ${state.homeScore} (${state.totalHomeScore})- Away: ${state.awayScore} (${state.totalAwayScore})"           
                    runIn(10, checkLiveGameStats)
                }
            } else {              
                if(logEnable) log.debug "In checkLiveGameStats - Game status: ${state.gameStatus}. Updated score: Home: ${state.homeScore} (${state.totalHomeScore})- Away: ${state.awayScore} (${state.totalAwayScore})"           
                runIn(10, checkLiveGameStats)
            }
        }
    }
}

def checkLiveGameStatsHandler(resp, data) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkLiveGameStatsHandler (${state.version})"
        if(resp.status == 200) {        
            def slurper = new groovy.json.JsonSlurper()
            def result = slurper.parseText(resp.getData())       
            liveData = result.liveData
                
            venueName = result.gameData.venue.name ?: "-"

            state.gameState = result.gameData.status.detailedState
            state.gameStatus = result.gameData.status.abstractGameState
            lastUpdated = new Date().format('h:mm:ss a', location.timeZone)
            
            if(state.gameStatus == "Preview") {
                inningHalf = " - "
                currentInning = "- "
                atBat = " - "
                balls = "0"
                strikes = "0"
                outs = "0"
                statsHomeScore1 = "-"
                statsHomeScore2 = "-"
                statsHomeScore3 = "-"
                statsHomeScore4 = "-"
                statsHomeScore5 = "-"
                statsHomeScore6 = "-"
                statsHomeScore7 = "-"
                statsHomeScore8 = "-"
                statsHomeScore9 = "-"

                statsAwayScore1 = "-"
                statsAwayScore2 = "-"
                statsAwayScore3 = "-"
                statsAwayScore4 = "-"
                statsAwayScore5 = "-"
                statsAwayScore6 = "-"
                statsAwayScore7 = "-"
                statsAwayScore8 = "-"
                statsAwayScore9 = "-"

                state.homeScore = "0"
                state.totalHomeScore = "0"
                totalHomeHits = "0"
                totalHomeErrors = "0"          
                totalHomeLeftOnBase = "0"

                state.awayScore = "0"
                state.totalAwayScore = "0"
                totalAwayHits = "0"
                totalAwayErrors = "0"
                totalAwayLeftOnBase = "0"

                latestPlay = "Game starts at ${state.todaysGameTime}"
            } else {
                if(state.gameState == "Postponed") {
                    latestPlay = "Game has been ${state.gameState}"
                } else {
                    currentInning = liveData.linescore.currentInningOrdinal ?: "-"
                    inningHalf = liveData.linescore.inningHalf
                    atBat = liveData.plays.currentPlay.matchup.batter.fullName ?: "Not Available"
                    balls = liveData.linescore.balls ?: "0"
                    strikes = liveData.linescore.strikes ?: "0"
                    outs = liveData.linescore.outs ?: "0"

                    try {
                        statsHomeScore1 = liveData.linescore.innings[0].home.runs ?: "0"
                        statsHomeScore2 = liveData.linescore.innings[1].home.runs ?: "0"
                        statsHomeScore3 = liveData.linescore.innings[2].home.runs ?: "0"
                        statsHomeScore4 = liveData.linescore.innings[3].home.runs ?: "0"
                        statsHomeScore5 = liveData.linescore.innings[4].home.runs ?: "0"
                        statsHomeScore6 = liveData.linescore.innings[5].home.runs ?: "0"
                        statsHomeScore7 = liveData.linescore.innings[6].home.runs ?: "0"
                        statsHomeScore8 = liveData.linescore.innings[7].home.runs ?: "0"
                        statsHomeScore9 = liveData.linescore.innings[8].home.runs ?: "0"
                    }
                    catch (e) {
                        if(statsHomeScore1 == null) statsHomeScore1 = "-"
                        if(statsHomeScore2 == null) statsHomeScore2 = "-"
                        if(statsHomeScore3 == null) statsHomeScore3 = "-"
                        if(statsHomeScore4 == null) statsHomeScore4 = "-"
                        if(statsHomeScore5 == null) statsHomeScore5 = "-"
                        if(statsHomeScore6 == null) statsHomeScore6 = "-"
                        if(statsHomeScore7 == null) statsHomeScore7 = "-"
                        if(statsHomeScore8 == null) statsHomeScore8 = "-"
                        if(statsHomeScore9 == null) statsHomeScore9 = "-"
                    }

                    try {
                        statsAwayScore1 = liveData.linescore.innings[0].away.runs ?: "0"
                        statsAwayScore2 = liveData.linescore.innings[1].away.runs ?: "0"
                        statsAwayScore3 = liveData.linescore.innings[2].away.runs ?: "0"
                        statsAwayScore4 = liveData.linescore.innings[3].away.runs ?: "0"
                        statsAwayScore5 = liveData.linescore.innings[4].away.runs ?: "0"
                        statsAwayScore6 = liveData.linescore.innings[5].away.runs ?: "0"
                        statsAwayScore7 = liveData.linescore.innings[6].away.runs ?: "0"
                        statsAwayScore8 = liveData.linescore.innings[7].away.runs ?: "0"
                        statsAwayScore9 = liveData.linescore.innings[8].away.runs ?: "0"
                    }
                    catch (e) {
                        if(statsAwayScore1 == null) statsAwayScore1 = "-"
                        if(statsAwayScore2 == null) statsAwayScore2 = "-"
                        if(statsAwayScore3 == null) statsAwayScore3 = "-"
                        if(statsAwayScore4 == null) statsAwayScore4 = "-"
                        if(statsAwayScore5 == null) statsAwayScore5 = "-"
                        if(statsAwayScore6 == null) statsAwayScore6 = "-"
                        if(statsAwayScore7 == null) statsAwayScore7 = "-"
                        if(statsAwayScore8 == null) statsAwayScore8 = "-"
                        if(statsAwayScore9 == null) statsAwayScore9 = "-"
                    }

                    state.totalHomeScore = liveData.linescore.teams.home.runs ?: "0"
                    totalHomeHits = liveData.linescore.teams.home.hits ?: "0"
                    totalHomeErrors = liveData.linescore.teams.home.errors ?: "0"
                    totalHomeLeftOnBase = liveData.linescore.teams.home.leftOnBase ?: "0"

                    state.totalAwayScore = liveData.linescore.teams.away.runs ?: "0"
                    totalAwayHits = liveData.linescore.teams.away.hits ?: "0"
                    totalAwayErrors = liveData.linescore.teams.away.errors ?: "0"
                    totalAwayLeftOnBase = liveData.linescore.teams.away.leftOnBase ?: "0"

                    latestPlay = liveData.plays.currentPlay.result.description ?: " - "
                }
            }

            scoreBoard =  "<table align=center width=100%><tr align=center><td colspan=13>"   
            
            scoreBoard += "<table width=100%>"
            scoreBoard += "<tr style='text-align:center;font-weight:bold'>"
            scoreBoard += "<td><img src='https://${state.awayLogo}' width=40 height=40>"
            scoreBoard += "<td>${venueName}<br><small>${lastUpdated}</small>"
            scoreBoard += "<td><img src='https://${state.homeLogo}' width=40 height=40>"
            scoreBoard += "</table>"

            scoreBoard += "<tr style='text-align:center;font-weight:bold'><td width=40%>Teams<td>1<td>2<td>3<td>4<td>5<td>6<td>7<td>8<td>9<td>R<td>H<td>E"

            scoreBoard += "<tr style='text-align:center'><td><b>${state.awayTeam}</b> ${state.awayRecord}<td>${statsAwayScore1}<td>${statsAwayScore2}<td>${statsAwayScore3}<td>${statsAwayScore4}<td>${statsAwayScore5}<td>${statsAwayScore6}<td>${statsAwayScore7}<td>${statsAwayScore8}<td>${statsAwayScore9}<td>${state.totalAwayScore}<td>${totalAwayHits}<td>${totalAwayErrors}"

            scoreBoard += "<tr style='text-align:center'><td><b>${state.homeTeam}</b> ${state.homeRecord}<td>${statsHomeScore1}<td>${statsHomeScore2}<td>${statsHomeScore3}<td>${statsHomeScore4}<td>${statsHomeScore5}<td>${statsHomeScore6}<td>${statsHomeScore7}<td>${statsHomeScore8}<td>${statsHomeScore9}<td>${state.totalHomeScore}<td>${totalHomeHits}<td>${totalHomeErrors}"

            scoreBoard += "</table>"
            
            theLastPlay = "<table width=100%>"
            if(state.gameStatus == "Final") {
                wPitcher = liveData.decisions.winner.fullName
                lPitcher = liveData.decisions.loser.fullName
                theLastPlay += "<tr><td style='text-align:center'><b>Game is Final</b>"
                theLastPlay += "<tr><td><b>Winning Pitcher:</b> ${wPitcher} - <b>Lossing Pitcher:</b> ${lPitcher}"
            } else {
                theLastPlay += "<tr style='text-align:center'><td><b>Inning:</b> ${inningHalf} of the ${currentInning} | <b>At Bat:</b> ${atBat} | <b>Balls:</b> ${balls} | <b>Strikes:</b> ${strikes} | <b>Outs:</b> ${outs}"
            }
            theLastPlay += "<tr style='text-align:left'><td><b>Description:</b> ${latestPlay}"
            theLastPlay += "</table>"
         
            if(dataDevice) {
                dataDevice.liveScoreboard(scoreBoard)
                gameStats = "${state.awayTeam};${state.homeTeam};${state.totalAwayScore};${state.totalHomeScore};${currentInning}"
                dataDevice.gameStats(gameStats)
                dataDevice.lastPlay(theLastPlay)
                charCount = scoreBoard.length()
                if(logEnable) log.debug "In checkLiveGameStatsHandler - Data sent - charCount: ${charCount}"
            }

            if(state.gameStatus == "Preview") {
                if(logEnable) log.debug "In checkLiveGameStatsHandler - Checking Score - Pregame"
            } else {              
                //if(logEnable) log.debug "In checkLiveGameStatsHandler - Checking Score - away: ${state.awayScore} VS ${tate.totalAwayScore} - home: ${state.homeScore} VS ${state.totalHomeScore}"
                
                if(state.awayScore != state.totalAwayScore) {
                    log.info "In checkLiveGameStatsHandler - Away Team Score!"
                    state.awayScore = state.totalAwayScore
                    if(state.myTeamIs == "away") {
                        messageHandler(myTeamScore)
                        data = "myTeam;live"
                    } else {
                        messageHandler(otherTeamScore)
                        data = "otherTeam;live"
                    }
                    notificationHandler(data)
                    
                    if(useTheFlasher && state.myTeamIs == "away") {
                        flashData = "Preset::${flashMyTeamScorePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    } else if(useTheFlasher && state.myTeamIs == "away") {
                        flashData = "Preset::${flashOtherTeamScorePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                }

                if(state.homeScore != state.totalHomeScore) {
                    log.info "In checkLiveGameStatsHandler - Home Team Score!"
                    state.homeScore = state.totalHomeScore
                    if(state.myTeamIs == "home") {
                        messageHandler(myTeamScore)
                        data = "myTeam;live"
                    } else {
                        messageHandler(otherTeamScore)
                        data = "otherTeam;live"
                    }
                    notificationHandler(data)
                    
                    if(useTheFlasher && state.myTeamIs == "home") {
                        flashData = "Preset::${flashMyTeamScorePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    } else if(useTheFlasher && state.myTeamIs == "away") {
                        flashData = "Preset::${flashOtherTeamScorePreset}"
                        theFlasherDevice.sendPreset(flashData)
                    }
                }
            }
            
            //update Score
            state.awayScore = state.totalAwayScore
            state.homeScore = state.totalHomeScore
        }
    }
}

def notificationHandler(data) {
    if(logEnable) log.debug "In notificationHandler (${state.version})"
    
    def (theTeam, theStatus) = data.split(";")
    if(logEnable) log.debug "In notificationHandler - theTeam: ${theTeam} - theStatus: ${theStatus}"
    
    if(useSpeech) letsTalk()
    if(pushMessage) pushNow()
    
    doIt = false
    if(state.gameStatus != "Final" && onScore) {
        doIt = true
    } else if(state.gameStatus == "Final" && onFinal) {
        doIt = true
    }
    
    if((switchesOnMyTeam || switchesOnOtherTeam) && doIt) {
        if(theTeam == "myTeam") {
            if(colorMT) {
                if(switchesOnMyTeam) {
                    hue = switchesOnMyTeam.currentValue("hue")
                    level = switchesOnMyTeam.currentValue("level")
                    saturation = switchesOnMyTeam.currentValue("saturation")
                    setLevelandColorHandler("myTeam")
                    
                    switchesOnMyTeam.setColor(lightValue)
                }
            } else {
                if(switchesOnMyTeam) switchesOnMyTeam.on()
            }
        } else {
            if(colorOT) {
                if(switchesOnOtherTeam) {
                    hue = switchesOnMyTeam.currentValue("hue")
                    level = switchesOnMyTeam.currentValue("level")
                    saturation = switchesOnMyTeam.currentValue("saturation")
                    setLevelandColorHandler("otherTeam")
                    
                    switchesOnOtherTeam.setColor(lightValue)
                }
            } else {
                if(switchesOnOtherTeam) switchesOnOtherTeam.on()
            }
        }
        
        if(state.gameStatus != "Final" || theStatus == "test") {
            howLong = howLongLightsOn ?: 10
            theData = "${oldHue};${oldLevel};${oldSaturation}"
            runIn(howLong, resetScoringSwitches, [data: theData])
        }
        
        if(state.gameStatus == "Final" || leaveOnTime) {
            howLong = leaveOnTime ?: 60
            theData = "${oldHue};${oldLevel};${oldSaturation}"
            runIn(howLong, resetScoringSwitches, [data: theData])
        }
    }
    
    if(useTheFlasher && state.gameStatus != "Final") {
        if(theTeam == "myTeam") {
            flashData = "Preset::${flashMyTeamScorePreset}"
        } else if(useTheFlasher && theTeam == "otherTeam") {
            flashData = "Preset::${flashOtherTeamScorePreset}"
        }
    
        theFlasherDevice.sendPreset(flashData)
    }
    
    if(useTheFlasher && state.gameStatus == "Final") {
        if(state.myTeamIs == "away") {
            if(state.awayScore > state.homeScore) {
                flashData = "Preset::${flashMyTeamWinsPreset}"
            } else {
                flashData = "Preset::${flashOtherTeamWinsPreset}"
            }
        }
        
        if(state.myTeamIs == "home") {
            if(state.homeScore > state.awayScore) {
                flashData = "Preset::${flashMyTeamWinsPreset}"
            } else {
                flashData = "Preset::${flashOtherTeamWinsPreset}"
            }
        }   
        theFlasherDevice.sendPreset(flashData)
    }
}

def resetScoringSwitches(data) {
    if(data) {
        def (theHue, theSaturation, theLevel) = data.split(";")
        if(theHue == null) theHue = 52
        if(theSaturation == null) theSaturation = 19
        if(theLevel == null) theLevel = 100
        
        state.lightValue = [switch: "off", hue: theHue, saturation: theSaturation, level: theLevel]
    }
    
    if(switchesOnMyTeam) {
        if(switchesOnMyTeam.hasCommand('setColor')) switchesOnMyTeam.setColor(state.lightValue)
        switchesOnMyTeam.off()
    }
    if(switchesOnOtherTeam) {
        if(switchesOnOtherTeam.hasCommand('setColor')) switchesOnOtherTeam.setColor(state.lightValue)
        switchesOnOtherTeam.off()
    }
}

def letsTalk() {
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - theMsg: ${state.theMsg}"
    if(useSpeech && fmSpeaker) {
        fmSpeaker.latestMessageFrom(state.name)
        fmSpeaker.speak(state.theMsg)
    }
}

def pregameMessageHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(pregameMessage) {
            messageHandler(pregameMessage)
            if(useSpeech) letsTalk()
            if(pushMessage) pushNow()
        }
    }
}

def messageHandler(data) {
    if(logEnable) log.debug "In messageHandler (${state.version})"
    state.theMsg = ""
    def theMessages = "${data}".split(";")
	mSize = theMessages.size()
	pickOne = mSize.toInteger()
    def whichMessage = new Random().nextInt(pickOne)
    state.theMsg = theMessages[whichMessage]
    
    if(state.myTeamIs == "away") { 
        if(state.theMsg.contains("%myTeam%")) {state.theMsg = state.theMsg.replace('%myTeam%', "${state.awayTeam}" )}
        if(state.theMsg.contains("%otherTeam%")) {state.theMsg = state.theMsg.replace('%otherTeam%', "${state.homeTeam}" )}
        
        if(state.theMsg.contains("%myTeamScore%")) {state.theMsg = state.theMsg.replace('%myTeamScore%', "${state.totalAwayScore}" )}
        if(state.theMsg.contains("%otherTeamScore%")) {state.theMsg = state.theMsg.replace('%otherTeamScore%', "${state.totalHomeScore}" )}
    }
    if(state.myTeamIs == "home") { 
        if(state.theMsg.contains("%myTeam%")) {state.theMsg = state.theMsg.replace('%myTeam%', "${state.homeTeam}" )}
        if(state.theMsg.contains("%otherTeam%")) {state.theMsg = state.theMsg.replace('%otherTeam%', "${state.awayTeam}" )}
        
        if(state.theMsg.contains("%myTeamScore%")) {state.theMsg = state.theMsg.replace('%myTeamScore%', "${state.totalHomeScore}" )}
        if(state.theMsg.contains("%otherTeamScore%")) {state.theMsg = state.theMsg.replace('%otherTeamScore%', "${state.totalAwayScore}" )}
    }

    if(logEnable) log.debug "In messageHandler - theMsg: ${state.theMsg}"
}

def pushNow() {
    if(logEnable) log.debug "In pushNow (${state.version})"
    thePushMessage = "${app.label} \n"
    thePushMessage += state.theMsg
    if(logEnable) log.debug "In pushNow - Sending message: ${thePushMessage}"
    pushMessage.deviceNotification(thePushMessage)
}

def checkSchedule() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In checkSchedule"
        // http://statsapi.mlb.com/api/v1/schedule/games/?sportId=1&startDate=07/20/2020&endDate=08/01/2020&teamId=111

        def now = new Date().format("MM/dd/yyyy")
        def days7 = new Date().plus(10).format("MM/dd/yyyy")

        if(logEnable) log.debug "In checkSchedule - Checking for data between - now: ${now} - days7: ${days7}"

        def params = [uri: "http://statsapi.mlb.com/api/v1/schedule/games/?sportId=1&startDate=${now}&endDate=${days7}&teamId=${state.Team.id}"]
        asynchttpGet("getScheduleHandler", params)
    }
}

def getScheduleHandler(resp, data) {
    if(logEnable) log.debug "In getScheduleHandler (${state.version})"
    
    if(resp.status == 200) {        
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(resp.getData())
        
        games = result.dates.games

        howManyGames = games.size()       
        if(logEnable) log.debug "In getScheduleHandler - Games found: ${howManyGames}"
        
        state.list1 = []
        for(x=0;x < howManyGames;x++) {
            theDate = result.dates[x].games[0].gameDate
            gameStart = Date.parse("yyyy-MM-dd'T'HH:mm:ssX", theDate)
            
            def nTime = gameStart.format('h:mm a', location.timeZone)
            def nDate = gameStart.format('MM/dd', location.timeZone)

            awayTeam = result.dates[x].games[0].teams.away.team.name
            homeTeam = result.dates[x].games[0].teams.home.team.name

            last = "${nDate} - ${nTime} - ${awayTeam} at ${homeTeam}"
            state.list1.add(0,last)  

            if(state.list1) {
                state.listSize1 = state.list1.size()
            } else {
                state.listSize1 = 0
            }
            
            int intNumOfLines = 10
            if (state.listSize1 > intNumOfLines) state.list1.removeAt(intNumOfLines)
        }
        makeScheduleListHandler()
    } else {
        log.debug "In checkLiveGameStatsHandler - Request Failed! Response: $resp.errorData"
    }
}

def makeScheduleListHandler() {
    if(logEnable) log.debug "In makeScheduleListHandler (${state.version})"
    int intNumOfLines = 10
    def revlst = state.list1.reverse()               
    String result1 = revlst.join(",")
    def lines1 = result1.split(",")

    if(logEnable) log.debug "In makeScheduleListHandler - All - listSize1: ${state.listSize1} - intNumOfLines: ${intNumOfLines}"

    if(fontFamily) {
        theSchedule = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px;font-family:${fontFamily}'><tr><td><b>Schedule for the next 10 days.</b><br>"
    } else {
        theSchedule = "<div style='overflow:auto;height:90%'><table style='text-align:left;font-size:${fontSize}px'><tr><td><b>Schedule for the next 10 days</b><br>"
    }
    for (i=0; i<intNumOfLines && i<state.listSize1 && theSchedule.length() < 927;i++)
    if(state.list1) { theSchedule += "${lines1[i]}<br>" }

    if(!state.list1) { theSchedule += "No Scheduled Games" }
    theSchedule += "</table></div>"
    if(logEnable) log.debug "makeScheduleListHandler - ${theSchedule.replace("<","!")}"       

    dataCharCount1 = theSchedule.length()
    if(dataCharCount1 <= 1024) {
        if(logEnable) log.debug "In makeScheduleListHandler - theSchedule - ${dataCharCount1} Characters"
    } else {
        theSchedule = "Too many characters to display on Dashboard (${dataCharCount1})"
    }
   
    if(theSchedule){
        dataDevice.gameSchedule(theSchedule)
        if(logEnable) log.debug "In makeScheduleListHandler - Data sent"
    }
}

def setLevelandColorHandler(data) {
    if(logEnable) log.debug "In setLevelandColorHandler (${state.version})"
    log.trace "In setLevelandColorHandler - data: ${data}"
    if(data == "myTeam") fColor = colorMT
    if(data == "otherTeam") fColor = colorOT
    
    def hueColor = 0
    def saturation = 100
	int onLevel = 100
    
    switch(fColor) {
            case "White":
            hueColor = 52
            saturation = 19
            break;
        case "Daylight":
            hueColor = 53
            saturation = 91
            break;
        case "Soft White":
            hueColor = 23
            saturation = 56
            break;
        case "Warm White":
            hueColor = 20
            saturation = 80
            break;
        case "Blue":
            hueColor = 70
            break;
        case "Green":
            hueColor = 39
            break;
        case "Yellow":
            hueColor = 25
            break;
        case "Orange":
            hueColor = 10
            break;
        case "Purple":
            hueColor = 75
            break;
        case "Pink":
            hueColor = 83
            break;
        case "Red":
            hueColor = 100
            break;
    }
    
    log.debug "In setLevelandColorHandler - fColor: ${fColor} | hueColor: ${hueColor} | saturation: ${saturation} | level: ${onLevel}"
    
	state.lightValue = [switch: "on", hue: hueColor, saturation: saturation, level: onLevel as Integer ?: 100]
    log.debug "In setLevelandColorHandler - lightValue: $state.lightValue"
    state.lightValue
}

def createDataChildDevice() {    
    if(logEnable) log.debug "In createDataChildDevice (${state.version})"
    statusMessageD = ""
    if(!getChildDevice(dataName)) {
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}"
        try {
            addChildDevice("BPTWorld", "MLB Game Day Live Driver", dataName, 1234, ["name": "${dataName}", isComponent: false])
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})"
            statusMessageD = "<b>Device has been been created. (${dataName})</b>"
        } catch (e) { if(logEnable) log.debug "MLB Game Day Live unable to create device - ${e}" }
    } else {
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>"
    }
    return statusMessageD
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    
    if(state.whichButton == "testOtherScore"){
        log.debug "In appButtonHandler - testOtherScore - other Team"
        if(useSpeech && otherTeamScore) messageHandler(otherTeamScore)
        data = "otherTeam;test"       
        notificationHandler(data)
    } else if(state.whichButton == "testMyTeamScore"){
        log.debug "In appButtonHandler - testMyTeamGoal - My Team"
        if(useSpeech && myTeamScore) messageHandler(myTeamScore)
        data = "myTeam;test"
        notificationHandler(data)
    }
}

// ********** Normal Stuff **********

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            theSwitch = it.currentValue("switch")
            if(theSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults() {
    state.homeTeam = null
    state.awayTeam = null
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
