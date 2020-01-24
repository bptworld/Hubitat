/**
* *** Original Header ***
*
* NHL Notification Service 
*
*  Copyright 2017 Eric Luttmann
*
*  Description:
*  Handles the sport services for NHL notifications.
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
* *** End original Header ***
* Find original code at https://github.com/ejluttmann/SmartThings/tree/master/smartapps
*
* ---- New Header ----
*
 *  ****************  NHL Game Day App  ****************
 *
 *  Design Usage:
 *  Get NHL notifications when your favorite team is playing!
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums to let!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Special thanks goes out to Eric Luttmann for his work on this.
 *
 *  Changes:
 *
 *  V1.0.4 - 01/23/20 - Attempt to fix horn stuck in repeat, Added test buttons for Horn and Boo
 *  V1.0.3 - 11/27/19 - Major enhancements! Reworked several modules and added a couple too!
 *    - Fixed Pregame Message
 *    - Major enhancements to speaker section
 *    - Horn and Boo now play over speakers, also honors the Sound Duration setting
 *    - Added text to speech if your speakers can't handle Sounds
 *    - Changed up the Tile, much better formatting
 *
 *  V1.0.2 - 10/06/19 - Changed child name to NHL Game Day
 *    - Started to streamline the code
 *    - fixed daily check.
 *    - Started collecting data to virtual device
 *  V1.0.1 - 10/04/19 - Now Parent/Child. Create new child apps for each team you want to follow
 *  V1.0.0 - 10/03/19 - Initial release for Hubitat
 *    - Fixed the async to be compatible with Hubitat
 *    - Fixed the push notifications 
 *    - Fixed the debug logging, now able to turn on/off
 *    - Removed text notifications, buttons, tile stuff and more junk
 *    - Added Speech Options to support almost all speaker types (only Sonos was supported before)
 *    - Added Time restrictions for notifications
 *    - Thanks to Chuck Schwer (@chuck.schwer) for solving the date/time/timezone problems
 *    - Gave it some flair, with colored headers and better layout
 *
*/

import groovy.time.TimeCategory
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "NHLGameDayChildVersion"
	state.version = "v1.0.4"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "NHL Game Day Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Get NHL notifications when your favorite team is playing!",
    category: "",
    parent: "BPTWorld:NHL Game Day",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
)

preferences {
    page name: "pageMain"
    page name: "pageGoals"
    page name: "pageText"
    page name: "pageGame"
}

def pageMain() {
    intitInitalStates()
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
            paragraph "<b>Notes:</b>"
            paragraph "Goal announcements can be drastically delayed depending on your internet connection and your means of watching the game."
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Your Team")) {
            input "nhlTeam", "enum", title: "Select NHL Team", required: true, displayDuringSetup: true, options: getTeamEnums()
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Options")) {
            input "enableGoals", "bool", title: "Enable Goal Notifications", defaultValue: "true", required: "false", submitOnChange: true
            if(enableGoals) {
                href(name: "goals", title:"Goal Notifications", description:"Tap to setup goal scoring", required: false, page: "pageGoals")
            }
            input "enableTextNotifications", "bool", title: "Enable Push Notifications", defaultValue: "false", required: "false", submitOnChange: true
            if(enableTextNotifications) {
                href(name: "notify", title:"Push Notifications", description:"Tap to setup game notifications", required: false, page: "pageText")
            }
            input "enableGame", "bool", title: "Enable Game Actions", defaultValue: "false", required: "false", submitOnChange: true
            if (enableGame) { 
                href(name: "game", title:"Game Actions", description:"Tap to setup game state actions", required: false, page: "pageGame")
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Misc Options")) {
            input "useTeamLocation", "bool", title: "Use Time Zone of Selected Team?", defaultValue: "true", required: "false"
            input "serviceStartTime", "time", title: "Daily Game Check", defaultValue: "1:00", required: false
            input "hourBeforeGame", "number", title: "Hours Before Game Start", description: "0-12 hours", required: false, range: "0..12"
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {
            input(name: "tileDevice", type: "capability.actuator", title: "Vitual Device created to send the data to:", required: false, multiple: false)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
        section() {
			input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "debugging")
    	}
        display2()
    }
}

def pageGoals() {
    dynamicPage(name: "pageGoals", title: "Goal Notifications") {
        section(getFormat("header-green", "${getImage("Blank")}"+" Device Options")) {}
        section("<b>Turn On/Off Switches</b>") {
            input "switchDevices", "capability.switch", title: "Select Switches to turn on", required: false, multiple: true, submitOnChange: true
            if(switchDevices) {
                input "switchOnFor", "number", title: "Turn Off After", description: "1-120 seconds", required: false, multiple: false, range: "1..120"
                input "switchDelay", "number", title: "Delay after goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
            }
        }
        section("<b>Flashing Lights</b>"){
            input "flashLights", "capability.switch", title: "Select Lights", multiple: true, required: false, submitOnChange: true
            if(flashLights) {
                input "numFlashes", "number", title: "Number Of Times To Flash", description: "1-50 times", required: false, range: "1..50"
                input "flashOnFor", "number", title: "On For (default 1000ms)", description: "milliseconds", required: false
                input "flashOffFor", "number", title: "Off For (default 1000ms)", description: "milliseconds", required: false
                input "flashDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
            }
        }
        section("<b>Lighting Level And Color Settings</b>"){
            input "lightColor", "enum", title: "Lighting Color?", required: false, multiple:false, options: ["White", "Red","Green","Blue","Yellow","Orange","Purple","Pink"]
            input "lightLevel", "enum", title: "Lighting Level?", required: false, options: [[10:"10%"],[20:"20%"],[30:"30%"],[40:"40%"],[50:"50%"],[60:"60%"],[70:"70%"],[80:"80%"],[90:"90%"],[100:"100%"]]
        }
        section("<b>Sirens To Trigger</b>"){
            input "sirens", "capability.alarm", title: "Select Sirens", required: false, multiple: true, submitOnChange: true
            if (sirens) {
                input "sirensOnly", "bool", title: "Don't Use The Strobe", defaultValue: "false", required:false
                input "sirensOnFor", "number", title: "Turn Off After", description: "1-10 seconds", required: false, multiple: false, range: "1..10"
                input "sirenDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
            }
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Sound Options")) {
            input "soundHornTeam", "bool", title: "Sound Horn When Your Team Scores?", defaultValue: "true", required:false
            input "soundDuration", "number", title: "Duration To Play (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
            input "soundDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
	        input "soundBooOpponent", "bool", title: "Boo When The Opponent Scores?", defaultValue: "true", required:false
            paragraph "Note: Not all speakers can play sounds. If your speaker doesn't have this ability then play the following speech."
            input "hornMessage", "text", title: "My Team Scores", required: false, defaultValue: "Oh ya, Goal!"
            input "booMessage", "text", title: "Other Team Scores", required: false, defaultValue: "Oh No! Opponent goal."
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Test Sound Options")) {
            input "testHorn", "bool", defaultValue:false, title: "Test Horn", description: "Test Horn", submitOnChange:true, width:6
            input "testBoo", "bool", defaultValue:false, title: "Test Boo", description: "Test Boo", submitOnChange:true, width:6
            
            if(testHorn) triggerHorn()
            if(testBoo) triggerBoo()
            resetHornBoo()
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Options")) {
            paragraph "Please select your speakers below from each field.<br><small>Note: Some speakers may show up in each list but each speaker only needs to be selected once.</small>"
           input "speakerMP", "capability.musicPlayer", title: "Choose Music Player speaker(s)", required:false, multiple:true, submitOnChange:true
           input "speakerSS", "capability.speechSynthesis", title: "Choose Speech Synthesis speaker(s)", required:false, multiple:true, submitOnChange:true
           paragraph "This app supports speaker proxies like, 'Follow Me'. This allows all speech to be controlled by one app. Follow Me features - Priority Messaging, volume controls, voices, sound files and more!"
           input "speakerProxy", "bool", defaultValue: "false", title: "Is this a speaker proxy device", description: "speaker proxy", submitOnChange:true
        }
        if(!speakerProxy) {
            if(speakerMP || speakerSS) {
		        section(getFormat("header-green", "${getImage("Blank")}"+" Volume Control Options")) {
		            paragraph "NOTE: Not all speakers can use volume controls.", width:8
                    paragraph "Volume will be restored to previous level if your speaker(s) have the ability, as a failsafe please enter the values below."
                    input "volSpeech", "number", title: "Speaker volume for speech", description: "0-100", required:true, width:6
		            input "volRestore", "number", title: "Restore speaker volume to X after speech", description: "0-100", required:true, width:6
                    input "volQuiet", "number", title: "Quiet Time Speaker volume (Optional)", description: "0-100", required:false, submitOnChange:true
		    	    if(volQuiet) input "QfromTime", "time", title: "Quiet Time Start", required:true, width:6
    	    	    if(volQuiet) input "QtoTime", "time", title: "Quiet Time End", required:true, width:6
                }
		    }
		    section(getFormat("header-green", "${getImage("Blank")}"+" Allow messages between what times? (Optional)")) {
                input "fromTime", "time", title: "From", required:false, width: 6
        	    input "toTime", "time", title: "To", required:false, width: 6
		    }
        } else {
            section(getFormat("header-green", "${getImage("Blank")}"+" Speaker Proxy")) {
		        paragraph "Speaker proxy in use."
            }
        }
    }
}

def pageText() {
    dynamicPage(name: "pageText", title: "Push Notifications") {
        section(getFormat("header-green", "${getImage("Blank")}"+" Notification Options")) {}
        section("Notification Types") {
            input "sendGoalMessage", "bool", title: "Enable Goal Score Notifications?", defaultValue: "true", required:false
            input "sendGameDayMessage", "bool", title: "Enable Game Day Status Notifications?", defaultValue: "false", required:false
            input "sendPregameMessage", "bool", title: "Send Custom Pregame Message?", required: false, submitOnChange: true
            if (sendPregameMessage) {
                input "pregameMinutesBefore", "number", title: "Minutes Before Game", description: "1-120 minutes (default 10)", default: 10, required: false, range: "1..2400"
                input "pregameMessage", "text", title: "Pregame Message", description: "Tap to Set Message", required: false
            }
        }
        section("Notification Options") {
            input "sendPushMessage", "capability.notification", title: "Send a Push notification?", multiple: true, required: false, submitOnChange: true
            input "sendDelay", "number", title: "Delay After Goal (in seconds)", description: "1-120 seconds", required: false, range: "1..120"
        }
    }
}

def pageGame() {
    dynamicPage(name: "pageGame", title: "Game Actions") {
        section(getFormat("header-green", "${getImage("Blank")}"+" Game Actions")) {}
        section("Turn On At Start Of Game"){
            input "gameSwitches", "capability.switch", title: "Select Switches", required: false, multiple: true, submitOnChange: true
            if (gameSwitches) {
                input "gameSwitchOff", "bool", title: "Turn Off After Game?", defaultValue: "true", required:false
            }
        }
        section("Misc Game Actions") {
            input "gameGoalIfWin", "bool", title: "Send Goal Notification If Team Wins", defaultValue: "false", required:false
        }
    }
}

// states only iniitalized once
def intitInitalStates() {
    if(logEnable) log.debug "Initialize static states"

    state.NHL_URL = "http://statsapi.web.nhl.com"
    state.NHL_API = "/api/v1"
    state.NHL_API_URL = "${state.NHL_URL}${state.NHL_API}"
    state.HORN_URL = "http://wejustscored.com/audio/"
	state.BOO_URL = "http://soundbible.com/mp3/Crowd Boo 5-SoundBible.com-339165240.mp3"
    
    // lets make the url
    // http://statsapi.web.nhl.com/api/v1/schedule?teamId=6&date=2019-10-03&expand=schedule.teams,schedule.broadcasts.all

    state.GAME_STATUS_SCHEDULED            = '1'
    state.GAME_STATUS_PREGAME              = '2'
    state.GAME_STATUS_IN_PROGRESS          = '3'
    state.GAME_STATUS_IN_PROGRESS_CRITICAL = '4'
    state.GAME_STATUS_UNKNOWN              = '5'
    state.GAME_STATUS_FINAL6               = '6'
    state.GAME_STATUS_FINAL7               = '7'
    
    state.Game = null
    state.gameStatus = null
    state.gameDate = null
    state.gameStations = null
    state.gameLocation = null
}

def installed() {
	// create during install only 
    state.prevTeamList = null

    intitInitalStates()
    initialize()
}

def updated() {
    intitInitalStates()
    unschedule()
    unsubscribe()
    initialize()
}

def uninstalled() {

}

def initialize() {
    if(logEnable) log.debug "In initialize (${state.version})"
    setVersion()
    
    state.Team = null
    state.teamList = null
    
    state.teamScore = 0
    state.opponentScore = 0

    state.gameStarted = false   
    
    state.lightsPrevious = [:]

    setGameStates(null)

    getTeam()

    // setup schedule
    schedule(serviceStartTime, startGameDay)
    
    if(logEnable) log.debug "In initialize - Making initial game day checks"
    startGameDay()
}

def getTeamEnums() {
    try {
	    def teams = []
        def params = [
            uri: "${state.NHL_API_URL}/teams",
        ]
        if (state.teamList == null) {
            if(logEnable) log.debug "httpGet: ${params}"
            httpGet(params) { resp ->
                def json = resp.data
                for (rec in json.teams) {
                    teams += rec.teamName
                } 
            }
            
    		state.teamList = teams
            state.prevTeamList = teams
		    if(logEnable) log.debug "New Team List: ${state.teamList}"
       } else {
		    if(logEnable) log.debug "Use existing team list"
       }
    } catch (e) {
        if(logEnable) log.error "caught exception ${e}"
    }

    if (state.teamList == null) {
    	if (state.prevTeamList == null) {
            if(logEnable) log.debug "Initialize team list"
            state.teamList = []
        } else {
            if(logEnable) log.debug "Reset to previous Team list"
    		state.teamList = state.prevTeamList
        }
    }    
    return state.teamList.sort()
}

def getTeam() {
    if(logEnable) log.debug "Setup for team ${settings.nhlTeam}"
    def found = false
    def params = [
        uri: "${state.NHL_API_URL}/teams",
    ]
    try {
        if(logEnable) log.debug "httpGet: ${params}"
        httpGet(params) { resp ->
            def json = resp.data
            for (rec in json.teams) {
                if (settings.nhlTeam == rec.teamName) {
                    state.Team = rec
                    if(logEnable) log.debug "Found info on team ${state.Team.teamName}, id=${state.Team.id}"
                    found = true
                    break
                }
            } 
        }
    } catch (e) {
        if(logEnable) log.error "caught exception ${e}"
    }

    if (!found) {
        if(logEnable) log.debug "Unable to find team, trying again in 30 seconds"

        def now = new Date()
        def runTime = new Date(now.getTime() + (30 * 1000))
        runOnce(runTime, getTeam)
    }
}

def setGameStates(game) { 
    if(logEnable) log.debug "In setGameStates (${state.version})"
	if (game) {
        // set current game info
        state.Game = game

		// set game status and date
        state.gameStatus = game.status.statusCode
        state.gameDate = game.gameDate

        // set game day stations and locations
        state.gameStations = getBroadcastStations(game)
        state.gameLocation = getLocation(game)
    } else {
        state.Game = null
        state.gameStatus = null
        state.gameDate = null
        state.gameStations = null
        state.gameLocation = null
    }
}

// game day URL fuctions and handlers
def startGameDay() {
    if(logEnable) log.debug "In startGameDay (${state.version})"
    if (checkIfGameDay()) {
        def gameStartDate =  gameDateTime()

        if (gameStartDate) {
            def hoursBefore = hourBeforeGame ?: 0
            def now = new Date()
            def gameTime = new Date(gameStartDate.getTime())
            def startTime = new Date(gameStartDate.getTime() - (((hoursBefore * (60 * 60))+30) * 1000))

			// if startTime is later than game time, set to run at game time minus 5 seconds
            if (gameTime < startTime) {
            	if(logEnable) log.debug "Reset start time to game time minus 5 seconds"
                startTime = new Date(gameTime.getTime() - (5 * 1000))
            }

			// if startTime is prior to current time, set to run current time plus 5 seconds
            if (startTime <= now) {
            	if(logEnable) log.debug "Reset start time to current time plus 5 seconds"
                startTime = new Date(now.getTime() + (5 * 1000))
            }
            
            // check for pregame message
            if (settings.sendPregameMessage) {
                def minutesBefore = settings.pregameMinutesBefore ?: 10
	            def pregameTime = new Date(gameStartDate.getTime() - ((minutesBefore * 60) * 1000))
                
                if (pregameTime <= now) {
                    if(logEnable) log.debug "Past pregame reminder, just ignore"
                } else {
                    if(logEnable) log.debug "Schedule pregame message for ${app.label} at ${pregameTime.format('h:mm:ss a', getTimeZone())}"
                    runOnce(pregameTime, sendPregameText)
                }
            }

            if(logEnable) log.debug "Schedule game status checks for ${app.label} at ${startTime.format('h:mm:ss a', getTimeZone())}"
            runOnce(startTime, checkGameStatus)

        } else {
            if(logEnable) log.debug "Unable to retrieve game time from ${app.label}"
        }
    } else {
        if(logEnable) log.debug "Today is not a gameday for the ${settings.nhlTeam}"
    }
}

def checkIfGameDayHandler(resp,gDate) {    
    if(logEnable) log.debug "In checkIfGameDayHandler (${state.version})"
    def isGameDay = false

    if (resp.status == 200) {
        def result = resp.data

        for (date in result.dates) {
            for (game in date.games){
                isGameDay = true
				setGameStates(game)
                
                if(logEnable) log.info "A game is scheduled for ${gDate} - ${game.teams.away.team.name} vs ${game.teams.home.team.name} at ${gameTimeText()}"

                if(tileDevice) {        // NEW - only in the Hubitat version!
                    hVenue = game.venue.name
                    if(hVenue == null || hVenue =="") hVenue = "-"
                    gameData = "${gDate};${game.teams.away.team.name};${game.teams.home.team.name};${gameTimeText()};${hVenue}"
                    tileDevice.todayGameData(gameData)                   
                    
                    hTeamWins = game.teams.home.leagueRecord.wins
                    hTeamLosses = game.teams.home.leagueRecord.losses
                    hTeamOTs = game.teams.home.leagueRecord.ot
                    hTeamRecord = "(${hTeamWins}-${hTeamLosses}-${hTeamOTs})"

                    aTeamWins = game.teams.away.leagueRecord.wins
                    aTeamLosses = game.teams.away.leagueRecord.losses
                    aTeamOTs = game.teams.away.leagueRecord.ot
                    aTeamRecord = "(${aTeamWins}-${aTeamLosses}-${aTeamOTs})"
                    
                    teamData = "${aTeamRecord};${hTeamRecord}"
                    tileDevice.todayTeamRecords(teamData)
                    
                    if(logEnable) log.debug "In checkIfGameDayHandler1 - sending gameData: ${gameData} - teamData: ${teamData}"
                }
                // break out of loop
                break
            }
        }
        if(hVenue == null || hVenue == "" || hVenue == "-") {
            if(tileDevice) {        // NEW - only in the Hubitat version!
                gameData = "${gDate};-;-;No Game;-"
                tileDevice.todayGameData(gameData)
                
                teamData = "-;-"
                tileDevice.todayTeamRecords(teamData)
                if(logEnable) log.debug "In checkIfGameDayHandler2 - sending gameData: ${gameData} - teamData: ${teamData}"
            }
        }
    } else {
        if(logEnable) log.error "${app.label}: resp.status = ${resp.status}"
    }

    return isGameDay
}

def checkIfGameDay() {
    if(logEnable) log.debug "In checkIfGameDay (${state.version})"
	def isGameDay = false
    try {  // Today
        def todayDate = new Date().format('yyyy-MM-dd', getTimeZone())
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todayDate}&expand=schedule.teams,schedule.broadcasts.all"] 
            
        def tDate = new Date().format('MM-dd-yyyy', getTimeZone())
        if(logEnable) log.debug "Determine if it's a game day for the ${settings.nhlTeam}, requesting game day schedule for ${tDate}"
        httpGet(params) { resp ->
            isGameDay = checkIfGameDayHandler(resp,tDate)
        }
    } catch (e) {
        if(logEnable) log.error "caught exception ${e}"
    }

    return isGameDay
}

def sendPregameText() {
    try {
        if (settings.sendPregameMessage) {
            sendTextNotification(settings.pregameMessage)
        }
    } catch (e) {
        log.error("caught exception", e)
    }
}

def checkGameStatusHandler(resp, data) {
   if(logEnable) log.debug "In checkGameStatusHandler (${state.version})"
   def rescheduleNextCheck = true
   def runDelay = 30
    
    // check for valid response
    if (resp.status == 200) {
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(resp.getData())
        def gamveOver = false
        def gameFound = false

        for (date in result.dates) {
            for (game in date.games)
            {
                // set current game info
                state.Game = game
                state.gamePk = game.gamePk

                // set game status and date
                state.gameStatus = game.status.statusCode
                state.gameDate = game.gameDate
                
                gameFound = true

                if(logEnable) log.debug "Current game status = ${state.gameStatus}"
                switch (state.gameStatus) {
                    case state.GAME_STATUS_SCHEDULED:
                    if(logEnable) log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - scheduled for today at ${gameTimeText()}!"

                    if(tileDevice) {        // NEW - only in the Hubitat version!
                        gameMessage = "${game.teams.away.team.name} vs ${game.teams.home.team.name} - scheduled for today at ${gameTimeText()}!"
                        gameStatus = "${game.status.statusCode};${gameMessage}"
                        tileDevice.gameStatus(gameStatus)
                        if(logEnable) log.debug "In checkGameStatusHandler1 - tileData sent"
                    }
    
                    // delay for 2 minutes before checking game day status again
                    runDelay = (2 * 60) 
                    
                    //done
                    break

                    case state.GAME_STATUS_PREGAME:
                    if(logEnable) log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - pregame!"

                    if(tileDevice) {        // NEW - only in the Hubitat version!
                        gameMessage = "${game.teams.away.team.name} vs ${game.teams.home.team.name} - pregame!"
                        gameStatus = "${game.status.statusCode};${gameMessage}"
                        tileDevice.gameStatus(gameStatus)
                        if(logEnable) log.debug "In checkGameStatusHandler2 - tileData sent"
                    }
                    
                    // start checking every 15 seconds now that it is pregame status
                    runDelay = 15

                    //done                    
                    break

                    case state.GAME_STATUS_IN_PROGRESS:
                    case state.GAME_STATUS_IN_PROGRESS_CRITICAL:
                    if(logEnable) log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - game is on!"

                    if(tileDevice) {        // NEW - only in the Hubitat version!
                        gameMessage = "${game.teams.away.team.name} vs ${game.teams.home.team.name} - game is on!"
                        gameStatus = "${game.status.statusCode};${gameMessage}"
                        tileDevice.gameStatus(gameStatus)
                        if(logEnable) log.debug "In checkGameStatusHandler3 - tileData sent"
                    }
                    
                    // check every 5 seconds when game is active, looking for score changes asap
                    runDelay = 5

                    // first time just initialize the scores - this is preventing the issue of sending 
                    // goal notifications when app is started in the middle of the game after scores have 
                    // occurred.
                    if (!state.gameStarted) {
                        def team = getTeamScore(game.teams)
                        def opponent = getOpponentScore(game.teams)

                        if(logEnable) log.debug "Game started, initialize scores and start switches..."
                        state.teamScore = team
                        state.opponentScore = opponent

                        if (settings.enableGame) {
                            // turn on any game action switches at start of game
                            if (settings.gameSwitches) {
                                setSwitches(settings.gameSwitches, true)
                            }
                        }

                        // indicate game has started
                        state.gameStarted = true
                    }

                    // check for new goal
                    checkForGoal()
                    
                    // check live game stats - Only available in Hubitat version!
                    checkLiveGameStats()
                    
                    //done
                    break

                    case state.GAME_STATUS_FINAL6:
                    case state.GAME_STATUS_FINAL7:
                    if(logEnable) log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} - game is over."

                    if(tileDevice) {        // NEW - only in the Hubitat version!
                        gameMessage = "${game.teams.away.team.name} vs ${game.teams.home.team.name} - game is over."
                        gameStatus = "${game.status.statusCode};${gameMessage}"
                        tileDevice.gameStatus(gameStatus)
                        if(logEnable) log.debug "In checkGameStatusHandler4 - tileData sent"
                    }
                    
                    // check for overtime score
                    def overtimeScore = checkForGoal()
    
                    // execute goal routine if team wins
                    if (settings.gameGoalIfWin) {
                        // execute goal at end of game, if there was no overtime goal already
                        if (overtimeScore == false) {
                            def teamGoals = getTeamScore(game.teams)
                            def opponentGoals = getOpponentScore(game.teams)

                            if (teamGoals > opponentGoals) {
                                def delay = settings.goalDelay ?: 0
                                runIn(delay, teamGoalScored)
                            }
                        }
                    }
                    
					if (settings.enableGame) {
                        // turn off any game action switches at end of game
                        if (settings.gameSwitches) {
                            if (settings.gameSwitchOff == true) {
                                setSwitches(settings.gameSwitches, false)
                            } else {
                                if(logEnable) log.debug "Switches are being left on after game!"
                            }
                        }
                    } 

                    // game over, no more game day status checks required for the day
                    gamveOver = true

                    //done
                    break

                    case state.GAME_STATUS_UNKNOWN:
                    default:
                        if(logEnable) log.debug "${game.teams.away.team.name} vs ${game.teams.home.team.name} game status is unknown!"

                    // check again in 15 seconds if game day status is unknown
                    runDelay = 15

                    //done
                    break
                }

                if (state.gameStatus != state.GAME_STATUS_UNKNOWN && state.notifiedGameStatus != state.gameStatus) {
                    if (settings.enableTextNotifications) {
                    	// use goal delay if set, ensure messages arrive after goal messages
                        def delay = settings.goalDelay ?: 0 
                        runIn(delay, triggerStatusNotifications)
                    }

                    //  set game status notified
                    state.notifiedGameStatus = state.gameStatus    	    	
                }

                // break out of loop
                break
            }
        }

        if (gamveOver) {
            if(logEnable) log.debug "Game is over, no more game status checks required for today."
            rescheduleNextCheck = false
            state.gameStarted = false
        }
        
        if (!gameFound) {
            if(logEnable) log.error "Game info was not found!"
        }

    } else {
        if(logEnable) log.debug "Request Failed!"
        if(logEnable) log.debug "Response: $resp.errorData"
    }
    
    if (rescheduleNextCheck) {
        def now = new Date()
        def nextGameCheck = new Date(now.getTime() + (runDelay * 1000))

        if(logEnable) log.debug "Checking game status again in ${runDelay} seconds..."
        runOnce(nextGameCheck, checkGameStatus)
    }
}

def checkLiveGameStatsHandler(resp, data) {        // NEW - only in the Hubitat version!
   if(logEnable) log.debug "In checkLiveGameStatsHandler (${state.version})"

    // check for valid response
    if (resp.status == 200) {
        def slurper = new groovy.json.JsonSlurper()
        def result = slurper.parseText(resp.getData())
        
        liveData = result.liveData

        currentPeriod = liveData.linescore.currentPeriodOrdinal
        timeRemaining = liveData.linescore.currentPeriodTimeRemaining
        
        awayGoals = liveData.linescore.teams.away.goals
        homeGoals = liveData.linescore.teams.home.goals
        
        awaySOG = liveData.linescore.teams.away.shotsOnGoal
        homeSOG = liveData.linescore.teams.home.shotsOnGoal
        
        if(logEnable) log.debug "In checkLiveGameStatsHandler - currentPeriod: ${currentPeriod} - timeRemaining: ${timeRemaining} - awayGoals:${awayGoals} - homeGoals:${homeGoals} - awaySOG:${awaySOG} - homeSOG:${homeSOG}"
      
        if(tileDevice) {
            gameStatus2 = "${currentPeriod};${timeRemaining};${awayGoals};${homeGoals};${awaySOG};${homeSOG}"
            tileDevice.gameStatus2(gameStatus2)
            if(logEnable) log.debug "In checkLiveGameStatsHandler - tileData sent"
        }
    } else {
        if(logEnable) log.debug "In checkLiveGameStatsHandler - Request Failed! Response: $resp.errorData"
    }
}

def checkGameStatus() {
    if(logEnable) log.debug "In checkGameStatus (${state.version})"
    try {
        def todayDate = new Date().format('yyyy-MM-dd', getTimeZone())
        if (settings.debugCheckDate) {
            todayDate = settings.debugCheckDate
        }
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todayDate}"] 

        def tDate = new Date().format('MM-dd-yyyy', getTimeZone())
        if(logEnable) log.debug "Requesting ${settings.nhlTeam} game schedule for ${tDate}"
        asynchttpGet("checkGameStatusHandler", params)
    } catch (e) {
        if(logEnable) log.error "In checkGameStatus - caught exception ${e}"
    }
}

def checkLiveGameStats() {        // NEW - only in the Hubitat version!
    if(logEnable) log.debug "In checkLiveGameStats (${state.version})"
    try {
        def todayDate = new Date().format('yyyy-MM-dd', getTimeZone())
        if (settings.debugCheckDate) {
            todayDate = settings.debugCheckDate
        }
        def params = [uri: "${state.NHL_API_URL}/game/${state.gamePk}/feed/live"] 

        def tDate = new Date().format('MM-dd-yyyy', getTimeZone())
        if(logEnable) log.debug "Requesting ${settings.nhlTeam} game stats for ${tDate}"
        asynchttpGet("checkLiveGameStatsHandler", params)
    } catch (e) {
        if(logEnable) log.error "In checkLiveGameStats - caught exception ${e}"
    }
}

def checkForGoal() {
    def game = state.Game
    
    if (game) {
        def team = getTeamScore(game.teams)
        def opponent = getOpponentScore(game.teams)

        // check for change in scores
        def delay = settings.goalDelay ?: 0
        if (team > state.teamScore) {
            if(logEnable) log.debug "Change in team score"
            state.teamScore = team
           	runIn(delay, teamGoalScored)
        }
        
        if (opponent > state.opponentScore) {
            if(logEnable) log.debug "Change in opponent score"
            state.opponentScore = opponent
            runIn(delay, opponentGoalScored)
        } 
    } else {
        if(logEnable) log.debug "No game setup yet!"
    }
}


// misc helper functions

def getBroadcastStations(game) {
    def stations = null

    try {
        def broadcasts = game.broadcasts

        if (broadcasts) {
            for (station in broadcasts) {
                if (station.name) {
                    if (stations == null) {
                        stations = station.name
                    } else {
                        stations = stations + ", " + station.name
                    }
                }
            } 
        }
    } catch(ex) {
        if(logEnable) log.error "caught exception 1 - ${ex}"
        stations = null
    }
    return stations
}

def getTimeZone() {
    try {
        if (useTeamLocation) {
            if (state.Team) {
                def tz = state.Team.venue.timeZone.id
                //if(logEnable) log.debug "In getTimeZone - tz: ${tz}"
                return TimeZone.getTimeZone(tz)    
            }
        }
    } catch(ex) {
        if(logEnable) log.error "caught exception 2 - ${ex}"
    }  
	return location.timeZone
}

def getLocation(game) {
    def location = null

    try {
        def team = game.teams.home.team
        location = team.venue.name + ", " + team.venue.city
    } catch(ex) {
        if(logEnable) log.error "caught exception 3 - ${ex}"
        location = null
    }
    return location
}

def gameDateTime() {
	def gameStartTime = null
    
    if (state.gameDate) {
    	gameStartTime = Date.parse("yyyy-MM-dd'T'HH:mm:ssX", state.gameDate)
    }
    if(logEnable) log.debug "In gameDateTime - gameDate: ${state.gameDate} - gameStartTime: ${gameStartTime}"
	return gameStartTime
}

def gameTimeText() {
    def gameTime = gameDateTime()
    if(logEnable) log.debug "In gameTimeText - gameTime: ${gameTime}"
    if (gameTime) {
		return gameTime.format('h:mm:ss a', getTimeZone())
    }   
    return "?:??:??"
}

def setSwitches(switches, turnon) {
    if(logEnable) log.debug "In setSwitches"
    switches.eachWithIndex {s, i ->
    	if (turnon) {
            s.on()
            if(logEnable) log.debug "Switch=$s.id on"
        } else {
            s.off()
            if(logEnable) log.debug "Switch=$s.id off"
        }
    }
}

def setLightPrevious(lights) {
    if(logEnable) log.debug "In setLightPrevious"
    lights.each {
        if (it.hasCapability("Color Control")) {
            if(logEnable) log.debug "save light color values"
            state.lightsPrevious[it.id] = [
                "switch": it.currentValue("switch"),
                "level" : it.currentValue("level"),
                "hue": it.currentValue("hue"),
                "saturation": it.currentValue("saturation")
            ]
        } else if (it.hasCapability("Switch Level")) {
            if(logEnable) log.debug "save light level"
            state.lightsPrevious[it.id] = [
                "switch": it.currentValue("switch"),
                "level" : it.currentValue("level"),
            ]
        } else {
            if(logEnable) log.debug "save light switch"
            state.lightsPrevious[it.id] = [
                "switch": it.currentValue("switch"),
            ]
        }
        
        if(logEnable) log.debug "$it.id - old light values = $state.lightsPrevious"
    }
}

def setLightOptions(lights) {
    if(logEnable) log.debug "In setLightOptions"
    def color = settings.lightColor
    def level = (settings.lightLevel as Integer) ?: 100

    // default to Red
    def hueColor = 100
    def saturation = 100

    if (color) {
        switch(color) {
            case "White":
            hueColor = 52
            saturation = 19
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
    }

    setLightPrevious(lights)

    lights.each {
        if (settings.lightColor && it.hasCapability("Color Control")) {
            def newColorValue = [hue: hueColor, saturation: saturation, level: level]
            if(logEnable) log.debug "$it.id - new light color values = $newColorValue"
            it.setColor(newColorValue)
        } 

        if (settings.lightLevel && it.hasCapability("Switch Level")) {
            if(logEnable) log.debug "$it.id - new light level = $level"
            it.setLevel(level)
        } 
    }
}

def restoreLightOptions(lights) {
    if(logEnable) log.debug "In restoreLightOptions"
    lights.each {
        if (settings.lightColor && it.hasCapability("Color Control")) {
           def oldColorValue = [hue: state.lightsPrevious[it.id].hue, saturation: state.lightsPrevious[it.id].saturation, level: state.lightsPrevious[it.id].level]
           if(logEnable) log.debug "$it.id - restore light color = $oldColorValue"
            it.setColor(oldColorValue) 
        } else if (settings.lightLevel && it.hasCapability("Switch Level")) {
            def level = state.lightsPrevious[it.id].level ?: 100
            if(logEnable) log.debug "$it.id - restore light level = $level"
            it.setLevel(level) 
        }

        def lightSwitch = state.lightsPrevious[it.id].switch ?: "off"
        if(logEnable) log.debug "$it.id - turn light $lightSwitch"
        if (lightSwitch == "on") {
            it.on()
        } else {
            it.off()
        }
    }
}

def randomizeRunTime(runTime, seconds) {
    def randSecs = new Random().nextInt(seconds) + 1
	def returnTime = new Date(runTime.getTime() + (randSecs * 1000))
    return returnTime
}

def getHornURL(team) {
    if(logEnable) log.debug "In getHornURL - ${team.teamName}"
    def hornURL = null

    try {
        def audio = null

        switch (team.teamName) {
            case "Devils":
            audio = "njd.mp3"
            break

            case "Islanders":
            audio = "nyi.mp3"
            break

            case "Rangers":
            audio = "nyr.mp3"
            break

            case "Flyers":
            audio = "phi.mp3"
            break

            case "Penguins":
            audio = "pit.mp3"
            break

            case "Bruins":
            audio = "bos.mp3"
            break

            case "Sabres":
            audio = "buf.mp3"
            break

            case "Canadiens":
            audio = "mon.mp3"
            break

            case "Senators":
            audio = "ott.mp3"
            break

            case "Maple Leafs":
            audio = "tor.mp3"
            break

            case "Hurricanes":
            audio = "car.mp3"
            break

            case "Panthers":
            audio = "fla.mp3"
            break

            case "Lightning":
            audio = "tbl.mp3"
            break

            case "Capitals":
            audio = "wsh.mp3"
            break

            case "Blackhawks":
            audio = "chi.mp3"
            break

            case "Red Wings":
            audio = "det.mp3"
            break

            case "Predators":
            audio = "nsh.mp3"
            break

            case "Blues":
            audio = "stl.mp3"
            break

            case "Flames":
            audio = "cgy.mp3"
            break

            case "Avalanche":
            audio = "col.mp3"
            break

            case "Oilers":
            audio = "edm.mp3"
            break

            case "Canucks":
            audio = "van.mp3"
            break

            case "Ducks":
            audio = "ana.mp3"
            break

            case "Stars":
            audio = "dal.mp3"
            break

            case "Kings":
            audio = "lak.mp3"
            break

            case "Sharks":
            audio = "sjs.mp3"
            break

            case "Blue Jackets":
            audio = "cbj.mp3"
            break

            case "Wild":
            audio = "min.mp3"
            break

            case "Jets":
            audio = "wpg.mp3"
            break

            case "Coyotes":
            audio = "ari.mp3"
            break

            default:
                break
        }

        if (audio) {
            hornURL = state.HORN_URL + audio
        }

    } catch(ex) {
        if(logEnable) log.error "caught exception ${ex}"
        hornURL = null
    }
    return hornURL
}

def getBooURL() {
    def booURL = state.BOO_URL

    return booURL
}

def getTeamScore(teams) {
    return getScore(teams, false)
}

def getOpponentScore(teams) {
    return getScore(teams, true)
}

def getScore(teams, opponent) {
    if(logEnable) log.debug "Getting current score"

    def score = 0

    if (state.Team.id == teams.away.team.id) {
        if (opponent) {
            score = teams.home.score
        } else {
            score = teams.away.score
        }
    } else {
        if (opponent) {
            score = teams.away.score
        } else {
            score = teams.home.score
        }
    }

    if (opponent) {
        if(logEnable) log.debug "found opponent score ${score}"
    } else {
        if(logEnable) log.debug "found team score ${score}"
    }
    return score
}

def getTeamName(teams) {
    return getName(teams, false)
}

def getOpponentName(teams) {
    return getName(teams, true)
}

def getName(teams, opponent) {
    def name = "unknown"

    if (state.Team.id == teams.away.team.id) {
        if (opponent) {
            return teams.home.team.name
        } else {
            return teams.away.team.name
        }
    } else {
        if (opponent) {
            return teams.away.team.name
        } else {
            return teams.home.team.name
        }
    }
    return name
}

// goal and message notifications

def teamGoalScored() {
    if(logEnable) log.debug "In teamGoalScored - GGGOOOAAALLL!!!"

	// Only send goal text notifications if game in progress, if game
    // is over there will be a final score sent already
	if (state.gameStarted) { 
        if (settings.enableTextNotifications) {
            triggerTeamGoalNotifications()
        }
	}
    
    if (settings.enableGoals) {
        triggerSwitches()
        triggerFlashing()
        triggerSirens()
        triggerHorn()
    }
}

def opponentGoalScored() {
     if(logEnable) log.warn "In opponentGoalScored - BOOOOOOO!!!"
    
    // Only send goal text notifications if game in progress, if game
    // is over there will be a final score sent already
    if (state.gameStarted) { 
        if (settings.enableTextNotifications) {
            triggerOpponentGoalNotifications()
        }
    }

    if (settings.enableGoals) {
    	if (settings.soundBooOpponent) {
	        triggerBoo()
        }
    }
}

def triggerSwitches() {
    if(logEnable) log.debug "In triggerSwitches"
    try {
        def delay = settings.switchDelay ?: 0
        if (settings.switchDevices) {
            runIn(delay, switchOnHandler)
        }
    } catch(ex) {
        if(logEnable) log.error "Error triggering switches: ${ex}"
    }
}

def switchOnHandler() {
    if(logEnable) log.debug "In switchOnHandler"
    try {
        def switchOffSecs = settings.switchOnFor ?: 5

		setSwitches(settings.switchDevices, true)

        runIn(switchOffSecs, switchOffHandler)
    } catch(ex) {
        if(logEnable) log.error "Error turning on switches: ${ex}"
    }
}

def switchOffHandler() {
    if(logEnable) log.debug "In switchOffHandler"
    try {        
        if(logEnable) log.debug "turn switches off"
        
		setSwitches(settings.switchDevices, false)
    } catch(ex) {
        if(logEnable) log.error "Error turning off switches: ${ex}"
    }
}

def triggerFlashing() {
    if(logEnable) log.debug "In triggerFlashing"
    try {
        def delay = settings.flashDelay ?: 0
        if (settings.flashLights) {
            runIn(delay, flashingHandler)
        }
   } catch(ex) {
        if(logEnable) log.error "Error Flashing Lights: ${ex}"
    }
}

def flashingHandler() {
    if(logEnable) log.debug "In flashingHandler"
    try {
        def doFlash = true
        def numFlash = settings.numFlashes ?: 3
        def onFor = settings.flashOnFor ?: 1000
        def offFor = settings.flashOffFor ?: 1000

        setLightOptions(settings.flashLights)

        if(logEnable) log.debug "LAST ACTIVATED IS: ${state.lastActivated}"
        if (state.lastActivated) {
            def elapsed = now() - state.lastActivated
            def sequenceTime = (numFlash + 1) * (onFor + offFor)
            doFlash = elapsed > sequenceTime
            if(logEnable) log.debug "DO FLASH: $doFlash, ELAPSED: $elapsed, LAST ACTIVATED: ${state.lastActivated}"
        }

        if (doFlash) {
            if(logEnable) log.debug "FLASHING $numFlash times"
            state.lastActivated = now()
            if(logEnable) log.debug "LAST ACTIVATED SET TO: ${state.lastActivated}"
            def initialActionOn =  settings.flashLights.collect{it.currentSwitch != "on"}
            def delay = 0
            numFlash.times {
                if(logEnable) log.debug "Switch on after  $delay msec"
                settings.flashLights.eachWithIndex {s, i ->
                    if (initialActionOn[i]) {
                        s.on(delay:delay)
                    }
                    else {
                        s.off(delay:delay)
                    }
                }
                delay += onFor
                if(logEnable) log.debug "Switch off after $delay msec"
                settings.flashLights.eachWithIndex {s, i ->
                    if (initialActionOn[i]) {
                        s.off(delay:delay)
                    }
                    else {
                        s.on(delay:delay)
                    }
                }
                delay += offFor
            }

            def restoreDelay = (delay/1000) + 1
            if(logEnable) log.debug "restore flash devices after $restoreDelay seconds"
            runIn(restoreDelay, flashRestoreLightsHandler)
        }

    } catch(ex) {
        if(logEnable) log.error "Error Flashing Lights: ${ex}"
    }
}

def flashRestoreLightsHandler() {
    if(logEnable) log.debug "In flashRestoreLightsHandler"
    try {
        if(logEnable) log.debug "restoring flash devices"
        restoreLightOptions(settings.flashLights)
    } catch(ex) {
        if(logEnable) log.error "Error restoring flashing lights: ${ex}"
    }
}

def triggerSirens() {
    if(logEnable) log.debug "In triggerSirens"
    try {
        def delay = settings.sirenDelay ?: 0
        if (settings.sirens) {
            runIn(delay, sirenOnHandler)
        }
    } catch(ex) {
        if(logEnable) log.error "Error triggering sirens: ${ex}"
    }
}

def sirenOnHandler() {
    if(logEnable) log.debug "In sirenOnHandler"
    try {
        def sirensOffSecs = settings.sirensOnFor ?: 3

        settings.sirens.eachWithIndex {s, i ->
            if (settings.sirensOnly) {
                s.siren()
            } else {
                s.both()
            }
            if(logEnable) log.debug "Siren=$s.id on"
        }

        runIn(sirensOffSecs, sirenOffHandler)
    } catch(ex) {
        if(logEnable) log.error "Error turning on sirens: ${ex}"
    }
}

def sirenOffHandler() {
    if(logEnable) log.debug "In sirenOffHandler"
    try {
        if(logEnable) log.debug "turn sirens off"
        settings.sirens.eachWithIndex {s, i ->
            s.off()
            if(logEnable) log.debug "Siren=$s.id off"
        }
    } catch(ex) {
        if(logEnable) log.error "Error turning off sirens: ${ex}"
    }
}

def triggerHorn() {
    if(logEnable) log.warn "In triggerHorn"
    try {
        def delay = settings.soundDelay ?: 0
        if (settings.soundHornTeam) {
           	runIn(delay, playHornHandler)
        }
    } catch(ex) {
        if(logEnable) log.error "Error running horn: ${ex}"
    }
}

def triggerBoo() {
    if(logEnable) log.warn "In triggerBoo"
    try {
        def delay = settings.soundDelay ?: 0
        if (settings.soundBooOpponent) {
           	runIn(delay, playBooHandler)
        }
    } catch(ex) {
        if(logEnable) log.error "Error running boo: ${ex}"
    }
}

def playHornHandler() {
    if(logEnable) log.warn "In playHornHandler"
    def hornURI = getHornURL(state.Team)
    type = "horn"
    if(!state.playing) letsTalk(type,hornURI)
}

def playBooHandler() {
    if(logEnable) log.warn "In playBooHandler"
    def booURI = getBooURL()
    type = "boo"
    if(!state.playing) letsTalk(type,booURI)
}

def letsTalk(type,uri) {
	if(logEnable) log.warn "In letsTalk (${state.version}) - Here we go"
	checkTime()
	checkVol()
    if(state.timeBetween == true) {
        state.playing = true
		theMsg = uri
        theDuration = soundDuration
        state.speakers = [speakerSS, speakerMP].flatten().findAll{it}
        if(logEnable) log.warn "In letsTalk - speaker: ${state.speakers}, vol: ${state.volume}, msg: ${theMsg}, volRestore: ${volRestore}"
        state.speakers.each { it ->
            if(logEnable) log.debug "Speaker in use: ${it}"
            if(speakerProxy) {
                if(logEnable) log.warn "In letsTalk - speakerProxy - ${it}"
                it.speak(theMsg)
            } else if(it.hasCommand('setVolumeSpeakAndRestore')) {
                if(logEnable) log.debug "In letsTalk - setVolumeSpeakAndRestore - ${it}"
                def prevVolume = it.currentValue("volume")
                it.setVolumeSpeakAndRestore(state.volume, theMsg, prevVolume)
                pauseExecution(theDuration)
                it.stop()
            } else if(it.hasCommand('playTextAndRestore')) {   
                if(logEnable) log.warn "In letsTalk - playTextAndRestore - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                def prevVolume = it.currentValue("volume")
                it.playTextAndRestore(theMsg, prevVolume)
                pauseExecution(theDuration)
                it.stop()
            } else if(it.hasCommand('playTrack')) { 
                if(logEnable) log.warn "In letsTalk - playTrack - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                it.playTrack(theMsg)
                pauseExecution(theDuration)
                it.stop()
                if(it.hasCommand('setLevel')) it.setLevel(volRestore)
                if(it.hasCommand('setVolume')) it.setVolume(volRestore)
            } else {		        
                if(logEnable) log.warn "In letsTalk - default - ${it}"
                if(volSpeech && (it.hasCommand('setLevel'))) it.setLevel(state.volume)
                if(volSpeech && (it.hasCommand('setVolume'))) it.setVolume(state.volume)
                    
                if(type == "horn") {
                    theMsg2 = hornMessage
                    it.speak(theMsg2)
                } else if(type == "boo") {
                    theMsg2 = booMessage
                    it.speak(theMsg2)
                }   
                pauseExecution(theDuration)
                if(it.hasCommand('setLevel')) it.setLevel(volRestore)
                if(it.hasCommand('setVolume')) it.setVolume(volRestore)
            }
        }
	    if(logEnable) log.warn "In letsTalk - Finished speaking"  
        state.playing = false
	} else {
		if(logEnable) log.warn "In letsTalk - Messages not allowed at this time"
    }
}

def checkVol(){
	if(logEnable) log.debug "In checkVol (${state.version})"
	if(QfromTime) {
		state.quietTime = timeOfDayIsBetween(toDateTime(QfromTime), toDateTime(QtoTime), new Date(), location.timeZone)
		if(logEnable) log.debug "In checkVol - quietTime: ${state.quietTime}"
    	if(state.quietTime) state.volume = volQuiet
		if(!state.quietTime) state.volume = volSpeech
	} else {
		state.volume = volSpeech
	}
	if(logEnable) log.debug "In checkVol - setting volume: ${state.volume}"
}

def checkTime() {
	if(logEnable) log.debug "In checkTime (${state.version}) - ${fromTime} - ${toTime}"
	if((fromTime != null) && (toTime != null)) {
		state.betweenTime = timeOfDayIsBetween(toDateTime(fromTime), toDateTime(toTime), new Date(), location.timeZone)
		if(state.betweenTime) state.timeBetween = true
		if(!state.betweenTime) state.timeBetween = false
  	} else {  
		state.timeBetween = true
  	}
	if(logEnable) log.debug "In checkTime - timeBetween: ${state.timeBetween}"
}

def triggerTeamGoalNotifications() {
    if (settings.sendGoalMessage) {
        def game = state.Game
        def msg = null

        if (game) {           
            def goals = getTeamScore(game.teams)

            if (goals == 1) {
                msg = getTeamName(game.teams) + " scored their first goal!"
            } else {
                msg = getTeamName(game.teams) + " have scored ${goals} goals!"
            }
            msg = msg + "\n${game.teams.away.team.name} ${game.teams.away.score}, ${game.teams.home.team.name} ${game.teams.home.score}"
        } else {
            msg = "${settings.nhlTeam} just Scored!"
        }

        sendTextNotification(msg)
    } else {
    	if(logEnable) log.debug "Goal notifications are OFF"
    }
}

def triggerOpponentGoalNotifications() {
    if (settings.sendGoalMessage) {
        def game = state.Game
        def msg = null

        if (game) {           
            def goals = getOpponentScore(game.teams)

            msg = getOpponentName(game.teams) + " scored."
            msg = msg + "\n${game.teams.away.team.name} ${game.teams.away.score}, ${game.teams.home.team.name} ${game.teams.home.score}"
        } else {
            msg = "Opponent Scored."
        }

        sendTextNotification(msg)
    }
}

def triggerStatusNotifications() {  
    if (settings.sendGameDayMessage) {
        def game = state.Game
        def msg = null
        def msg2 = null

        if (game) {
            switch (state.gameStatus) {
                case state.GAME_STATUS_SCHEDULED:
                msg = "${game.teams.away.team.name} vs ${game.teams.home.team.name}"
                
                if (state.gameStations) {
                    msg = msg + "\nToday, ${gameTimeText()} on ${state.gameStations}"
                } else {
                    msg = msg + "\nToday, ${gameTimeText()}"
                }
                if (state.gameLocation) {
                    msg = msg + "\n${state.gameLocation}"
                }
                break

                case state.GAME_STATUS_PREGAME:
                msg = "Pregame for ${game.teams.away.team.name} vs ${game.teams.home.team.name} is starting soon, game is at ${gameTimeText()}!"
                break

                case state.GAME_STATUS_IN_PROGRESS:
                msg = "${game.teams.away.team.name} vs ${game.teams.home.team.name} is now in progress!"
                break

                case state.GAME_STATUS_IN_PROGRESS_CRITICAL:
                msg = "${game.teams.away.team.name} vs ${game.teams.home.team.name} is in critial last minutes of the game, Go " + getTeamName(game.teams) + "!"
                break

                case state.GAME_STATUS_FINAL6:
                case state.GAME_STATUS_FINAL7:
                msg = "Final Score:\n${game.teams.away.team.name} ${game.teams.away.score}\n${game.teams.home.team.name} ${game.teams.home.score}"

                if (getTeamScore(game.teams) > getOpponentScore(game.teams)) {
                    msg2 =  getTeamName(game.teams) + " win!!!"
                } else if (getTeamScore(game.teams) < getOpponentScore(game.teams)) {
                    msg2 =  getTeamName(game.teams) + " lost."
                } else {
                    msg2 = "Tie game!"
                }
                break

                case state.GAME_STATUS_UNKNOWN:
                default:
                    break
            }

            if (msg) {
                if (sendTextNotification(msg)) {
                    if (msg2) {
                        sendTextNotification(msg2)
                    }
                }
            }
        } else {
            if(logEnable) log.debug "invalid game object"
        }
    }
    else {
    	if(logEnable) log.debug "Game Status notifications are OFF"
    }
}

def sendTextNotification(msg) {
    try {
        sendPushMessage.deviceNotification("${app.label} - ${msg}")
    } catch(ex) {
        if(logEnable) log.error "Error sending notifications: ${ex}"
        return false
    }

    return true
}

def resetHornBoo() {
    app?.updateSetting("testHorn",[value:"false",type:"bool"])
    app?.updateSetting("testBoo",[value:"false",type:"bool"])
}

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
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

def getFormat(type, myText=""){			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " NHL Game Day - ${theName}")) {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>NHL Game Day - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
