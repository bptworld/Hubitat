/**
 *  ****************  NHL Stats App  ****************
 *
 *  Design Usage:
 *  Get NHL notifications when your favorite team is playing!
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V1.0.0 - 10/03/19 - Initial release for Hubitat
 *
*/

import groovy.time.TimeCategory
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

def setVersion(){
	if(logEnable) log.debug "In setVersion - App Watchdog Parent app code"
    // Must match the exact name used in the json file. ie. AppWatchdogParentVersion, AppWatchdogChildVersion
    state.appName = "NHLStatsChildVersion"
	state.version = "v1.0.2"
    
    try {
        if(sendToAWSwitch && awDevice) {
            awInfo = "${state.appName}:${state.version}"
		    awDevice.sendAWinfoMap(awInfo)
            if(logEnable) log.debug "In setVersion - Info was sent to App Watchdog"
	    }
    } catch (e) { log.error "In setVersion - ${e}" }
}

definition(
    name: "NHL Stats Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Get NHL notifications when your favorite team is playing!",
    category: "",
    parent: "BPTWorld:NHL Notifications",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: ""
)

preferences {
    page name: "pageMain"
}

def pageMain() {
    intitInitalStates()
    dynamicPage(name: "pageMain", title: "<h2 style='color:#1A77C9;font-weight: bold'>NHL Stats</h2>", install: true, uninstall: true) {
        display()
        section() {
            paragraph ""
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Your Team")) {
            input "nhlTeam", "enum", title: "Select NHL Team", required: true, displayDuringSetup: true, options: getTeamEnums()
        }
        section("Misc Options") {
            input "serviceStartTime", "time", title: "Daily Game Check", defaultValue: "1:00", required: true
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" Dashboard Tile")) {
            input(name: "tileDevice", type: "capability.actuator", title: "Vitual Device created to send the data to:", required: true, multiple: false)
        }
        section(getFormat("header-green", "${getImage("Blank")}"+" General")) {label title: "Enter a name for this child app", required: false, submitOnChange: true}
        section() {
			input(name: "logEnable", type: "bool", defaultValue: "false", submitOnChange: "true", title: "Enable Debug Logging", description: "debugging")
    	}
        display2()
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

    getTeam()

    // setup schedule
    schedule(serviceStartTime, checkIfGameD1)
    schedule(serviceStartTime, checkIfGameD2)
    schedule(serviceStartTime, checkIfGameD3)
    
    if(logEnable) log.debug "In initialize - Making initial game day checks"
    checkIfGameD1()
    pauseExecution(1000)
    checkIfGameD2()
    pauseExecution(1000)
    checkIfGameD3()
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

// Start D1
def checkIfGameD1() {
    if(logEnable) log.debug "In CheckIfGameD1 (${state.version})"
	def isGameDay = false
    try {
        def todayDate = new Date().plus(1).format('yyyy-MM-dd', getTimeZone())
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todayDate}&expand=schedule.teams,schedule.broadcasts.all"] 
            
        def tDate = new Date().plus(1).format('MM-dd-yyyy', getTimeZone())
        if(logEnable) log.debug "Determine if it's a game day 1 for the ${settings.nhlTeam}, requesting game day schedule for ${tDate}"
        httpGet(params) { resp ->
            isGameDay = checkIfGameD1Handler(resp,tDate)
        }
    } catch (e) {
        if(logEnable) log.error "caught exception ${e}"
    }
    if(logEnable) log.debug "In CheckIfGameD1 - End"
}

def checkIfGameD1Handler(resp,gDate) {
    if(logEnable) log.debug "In CheckIfGameD1Handler (${state.version})"
    def isGameDay = false

    if (resp.status == 200) {
        def result = resp.data
  
        for (date in result.dates) {
            for (game in date.games){
                isGameDay = true
				setGameStates(game)
                
                if(logEnable) log.info "A game is scheduled for ${gDate} - ${game.teams.away.team.name} vs ${game.teams.home.team.name} at ${gameTimeText()}"

                if(tileDevice) {
                    hVenue1 = game.venue.name
                    if(hVenue1 == null || hVenue1 =="") hVenue1 = "Not Available"
                    gameData1 = "${gDate};${game.teams.away.team.name};${game.teams.home.team.name};${gameTimeText()};${hVenue1}"
                    tileDevice.d1GameData(gameData1)
                    
                    hTeamWins1 = game.teams.home.leagueRecord.wins
                    hTeamLosses1 = game.teams.home.leagueRecord.losses
                    hTeamOTs1 = game.teams.home.leagueRecord.ot
                    hTeamRecord1 = "(${hTeamWins1}-${hTeamLosses1}-${hTeamOTs1})"

                    aTeamWins1 = game.teams.away.leagueRecord.wins
                    aTeamLosses1 = game.teams.away.leagueRecord.losses
                    aTeamOTs1 = game.teams.away.leagueRecord.ot
                    aTeamRecord1 = "(${aTeamWins1}-${aTeamLosses1}-${aTeamOTs1})"
                    
                    teamData1 = "${aTeamRecord};${hTeamRecord}"
                    tileDevice.d1TeamRecords(teamData1)
                    
                    if(logEnable) log.debug "In checkIfGameD1Handler - D1a - sending gameData1: ${gameData1} | teamData1: ${teamData1}}"
                }
                // break out of loop
                break
            }
        }
        if(hVenue1 == null || hVenue1 == "" || hVenue1 == "-") {
            if(tileDevice) {
                gameData1 = "${gDate};-;-;No Game;-"
                tileDevice.d1GameData(gameData1)
                
                teamData1 = "-;-"
                tileDevice.d1TeamRecords(teamData1)
                if(logEnable) log.debug "In checkIfGameD1Handler - D1b - sending gameData: ${gameData1} | teamData: ${teamData1}}"
            }
        }
    } else {
        if(logEnable) log.error "${app.label}: resp.status = ${resp.status}"
    }
    if(logEnable) log.debug "In CheckIfGameD1 - Going Back"
    return isGameDay
}
// End D1

// Start D2
def checkIfGameD2() {
    if(logEnable) log.debug "In CheckIfGameD2 (${state.version})"
	def isGameDay = false
    try {  
        def todayDate = new Date().plus(2).format('yyyy-MM-dd', getTimeZone())
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todayDate}&expand=schedule.teams,schedule.broadcasts.all"] 
            
        def tDate = new Date().plus(2).format('MM-dd-yyyy', getTimeZone())
        if(logEnable) log.debug "Determine if it's a game day 2 for the ${settings.nhlTeam}, requesting game day schedule for ${tDate}"
        httpGet(params) { resp ->
            isGameDay = checkIfGameD2Handler(resp,tDate)
        }
    } catch (e) {
        if(logEnable) log.error "D2 -- caught exception ${e}"
    }
    if(logEnable) log.debug "In CheckIfGameD2 - End"
}

def checkIfGameD2Handler(resp,gDate) {  
    if(logEnable) log.debug "In CheckIfGameD2Handler (${state.version})"
    def isGameDay = false

    if (resp.status == 200) {
        def result = resp.data

        for (date in result.dates) {
            for (game in date.games){
                isGameDay = true
				setGameStates(game)
                
                if(logEnable) log.info "A game is scheduled for ${gDate} - ${game.teams.away.team.name} vs ${game.teams.home.team.name} at ${gameTimeText()}"

                if(tileDevice) {
                    hVenue2 = game.venue.name
                    if(hVenue2 == null || hVenue2 =="") hVenue2 = "Not Available"
                    gameData2 = "${gDate};${game.teams.away.team.name};${game.teams.home.team.name};${gameTimeText()};${hVenue2}"
                    tileDevice.d2GameData(gameData2)
                    
                    hTeamWins2 = game.teams.home.leagueRecord.wins
                    hTeamLosses2 = game.teams.home.leagueRecord.losses
                    hTeamOT2s = game.teams.home.leagueRecord.ot
                    hTeamRecord2 = "(${hTeamWins2}-${hTeamLosses2}-${hTeamOTs2})"

                    aTeamWins2 = game.teams.away.leagueRecord.wins
                    aTeamLosses2 = game.teams.away.leagueRecord.losses
                    aTeamOTs2 = game.teams.away.leagueRecord.ot
                    aTeamRecord2 = "(${aTeamWins2}-${aTeamLosses2}-${aTeamOTs2})"
                    
                    teamData2 = "${aTeamRecord2};${hTeamRecord2}"
                    tileDevice.d2TeamRecords(teamData2)
                    
                    if(logEnable) log.debug "In checkIfGameD2Handler - D2a - sending gameData2: ${gameData2} | teamData2: ${teamData2}}"
                }
                // break out of loop
                break
            }
        }
        if(hVenue2 == null || hVenue2 == "" || hVenue2 == "-") {
            if(tileDevice) {
                gameData2 = "${gDate};-;-;No Game;-"
                tileDevice.d2GameData(gameData2)
                
                teamData2 = "-;-"
                tileDevice.d2TeamRecords(teamData2)
                if(logEnable) log.debug "In checkIfGameD2Handler - D2b - sending gameData2: ${gameData2} | teamData2: ${teamData2}}"
            }
        }
    } else {
        if(logEnable) log.error "D2 - ${app.label}: resp.status = ${resp.status}"
    }
    if(logEnable) log.debug "In CheckIfGameD2 - Going Back"
    return isGameDay
}
// End D2

// Start D3
def checkIfGameD3() {
    if(logEnable) log.debug "In CheckIfGameD3 (${state.version})"
	def isGameDay = false
    try {  
        def todayDate = new Date().plus(3).format('yyyy-MM-dd', getTimeZone())
        def params = [uri: "${state.NHL_API_URL}/schedule?teamId=${state.Team.id}&date=${todayDate}&expand=schedule.teams,schedule.broadcasts.all"] 
            
        def tDate = new Date().plus(3).format('MM-dd-yyyy', getTimeZone())
        if(logEnable) log.debug "Determine if it's a game day 3 for the ${settings.nhlTeam}, requesting game day schedule for ${tDate}"
        httpGet(params) { resp ->
            isGameDay = checkIfGameD3Handler(resp,tDate)
        }
    } catch (e) {
        if(logEnable) log.error "D3 -- caught exception ${e}"
    }
    if(logEnable) log.debug "In CheckIfGameD3 - End"
}

def checkIfGameD3Handler(resp,gDate) {  
    if(logEnable) log.debug "In CheckIfGameD3Handler (${state.version})"
    def isGameDay = false

    if (resp.status == 200) {
        def result = resp.data

        for (date in result.dates) {
            for (game in date.games){
                isGameDay = true
				setGameStates(game)
                
                if(logEnable) log.info "A game is scheduled for ${gDate} - ${game.teams.away.team.name} vs ${game.teams.home.team.name} at ${gameTimeText()}"

                if(tileDevice) {
                    hVenue3 = game.venue.name
                    if(hVenue3 == null || hVenue3 =="") hVenue3 = "Not Available"
                    gameData3 = "${gDate};${game.teams.away.team.name};${game.teams.home.team.name};${gameTimeText()};${hVenue3}"
                    tileDevice.d3GameData(gameData3)

                    hTeamWins3 = game.teams.home.leagueRecord.wins
                    hTeamLosses3 = game.teams.home.leagueRecord.losses
                    hTeamOTs3 = game.teams.home.leagueRecord.ot
                    hTeamRecord3 = "(${hTeamWins3}-${hTeamLosses3}-${hTeamOTs3})"

                    aTeamWins3 = game.teams.away.leagueRecord.wins
                    aTeamLosses3 = game.teams.away.leagueRecord.losses
                    aTeamOTs3 = game.teams.away.leagueRecord.ot
                    aTeamRecord3 = "(${aTeamWins3}-${aTeamLosses3}-${aTeamOTs3})"
                    
                    teamData = "${aTeamRecord};${hTeamRecord}"
                    tileDevice.d3TeamRecords(teamData)
                    
                    if(logEnable) log.debug "In checkIfGameD3Handler - D3a - sending gameData3: ${gameData3} | teamData3: ${teamData3}}"
                }
                // break out of loop
                break
            }
        }
        log.trace "hVenue 3: ${hVenue3}"
        if(hVenue3 == null || hVenue3 == "" || hVenue3 == "-") {
            if(tileDevice) {
                gameData3 = "${gDate};-;-;No Game;-"
                tileDevice.d3GameData(gameData3)
                
                teamData3 = "-;-"
                tileDevice.d3TeamRecords(teamData3)
                if(logEnable) log.debug "In checkIfGameD3Handler - D3b - sending gameData3: ${gameData3} | teamData3: ${teamData3}}"
            }
        }
    } else {
        if(logEnable) log.error "D3 - ${app.label}: resp.status = ${resp.status}"
    }
    if(logEnable) log.debug "In CheckIfGameD2 - Going Back"
    return isGameDay
}
// End D3

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

// ********** Normal Stuff **********

def setDefaults(){
	if(logEnable == null){logEnable = false}
}

def getImage(type) {					// Modified from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=15 width=15>"
}

def getFormat(type, myText=""){			// Modified from @Stephack
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
	if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
}

def display() {
	section() {
		paragraph getFormat("line")
	}
}

def display2(){
	setVersion()
	section() {
		paragraph getFormat("line")
		paragraph "<div style='color:#1A77C9;text-align:center'>NHL Stats - @BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br>Get app update notifications and more with <a href='https://github.com/bptworld/Hubitat/tree/master/Apps/App%20Watchdog' target='_blank'>App Watchdog</a><br>${state.version}</div>"
	}       
}
