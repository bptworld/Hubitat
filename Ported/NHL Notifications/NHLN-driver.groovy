/**
 *  ****************  NHL Notifications Driver  ****************
 *
 *  Design Usage:
 *  This driver formats the NHL Notifications data to be used with Hubitat's Dashboards.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V1.0.0 - 10/06/19 - Initial release.
 */

def setVersion(){
    appName = "NHLNotificationsDriver"
	version = "v1.0.0" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "NHL Notifications Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "") {
   		capability "Actuator"

        command "todayTeamRecords", ["string"]
        command "todayHomeTeamData", ["string"] 
		command "todayGameData", ["string"]
        command "todayTeamRecords", ["string"]
        
        command "d1TeamRecords", ["string"]
		command "d1GameData", ["string"]
        
        command "d2TeamRecords", ["string"]
		command "d2GameData", ["string"]             
        
        command "d3TeamRecords", ["string"]
		command "d3GameData", ["string"]
        
        command "scoreUpdateHome", ["string"]
        command "scoreUpdateAway", ["string"]
        command "gameStatus", ["string"]
		
    	attribute "todayGameDate", "string"
        attribute "todayGameTime", "string"
        attribute "todayAwayTeam", "string"
        attribute "todayAwayRecord", "string"
        attribute "todayHomeTeam", "string"
        attribute "todayHomeRecord", "string"
        attribute "todayVenue", "string"
        
        attribute "gameStatus", "string"
        attribute "gameMessage", "string"
        
        attribute "liveScoreHome", "string"
        attribute "liveScoreAway", "string"
        
        attribute "d1GameDate", "string"
        attribute "d1AwayTeam", "string"
        attribute "d1AwayTeamRecord", "string"
        attribute "d1HomeTeam", "string"
        attribute "d1HomeTeamRecord", "string"
        attribute "d1GameTime", "string"
        attribute "d1Venue", "string"
        
        attribute "d2GameDate", "string"
        attribute "d2AwayTeam", "string"
        attribute "d2AwayTeamRecord", "string"
        attribute "d2HomeTeam", "string"
        attribute "d2HomeTeamRecord", "string"
        attribute "d2GameTime", "string"
        attribute "d2Venue", "string"
        
        attribute "d3GameDate", "string"
        attribute "d3AwayTeam", "string"
        attribute "d3AwayTeamRecord", "string"
        attribute "d3HomeTeam", "string"
        attribute "d3HomeTeamRecord", "string"
        attribute "d3GameTime", "string"
        attribute "d3Venue", "string"
        
        attribute "liveGameTile", "string"
        attribute "liveGameTileCount", "number"
        
        attribute "gameSchedule", "string"
        attribute "gameScheduleCount", "number"
        
        attribute "lastUpdated", "string"
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
	}
	preferences() {    	
        section(""){
            input "logEnable", "bool", title: "Enable logging", required: true, defaultValue: true
        }
    }
}
	
def todayTeamRecords(data) {
    if(logEnable) log.debug "In todayTeamRecords - Received new data! ${data}"
    def (aTeamRecord,hTeamRecord) = data.split(";")
    state.awayTeamRecord = aTeamRecord
    state.homeTeamRecord = HTeamRecord
    sendEvent(name: "homeTeamRecord", value: hTeamRecord, displayed: true)
    sendEvent(name: "awayTeamRecord", value: aTeamRecord, displayed: true)
    liveGameTile()
}

def todayGameData(game) {
    if(logEnable) log.debug "In todayGameData - Received new data! ${game}"
    def (gameDate,awayTeam,homeTeam,gameTime,gameVenue) = game.split(";") 

    state.todayGameDate = "${gameDate}"
    state.todayGameTime = "${gameTime}"
    state.todayAwayTeam = "${awayTeam}"
    state.todayHomeTeam = "${homeTeam}"
    state.todayVenue = "${gameVenue}"

    sendEvent(name: "todayGameDate", value: gameDate, displayed: true)
    sendEvent(name: "todayGameTime", value: gameTime, displayed: true)
    sendEvent(name: "todayAwayTeam", value: awayTeam, displayed: true)
    sendEvent(name: "todayHomeTeam", value: homeTeam, displayed: true)   
    sendEvent(name: "todayVenue", value: gameVenue, displayed: true)
        
    lastUpdated = new Date()
    sendEvent( name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a") )
    liveGameTile()
}

def d1TeamRecords(data) {
    if(logEnable) log.debug "In d1TeamRecords - Received new data! ${data}"
    def (aTeamRecord,hTeamRecord) = data.split(";")
    state.d1AwayTeamRecord = aTeamRecord
    state.d1HomeTeamRecord = HTeamRecord
    sendEvent(name: "d1HomeTeamRecord", value: hTeamRecord, displayed: true)
    sendEvent(name: "d1AwayTeamRecord", value: aTeamRecord, displayed: true)
}

def d1GameData(game) {
    if(logEnable) log.debug "In d1GameData - Received new data! ${game}"
    def (gameDate,awayTeam,homeTeam,gameTime,gameVenue) = game.split(";") 
    
    state.d1GameDate = "${gameDate}"
    state.d1GameTime = "${gameTime}"
    state.d1AwayTeam = "${awayTeam}"
    state.d1HomeTeam = "${homeTeam}"
    state.d1Venue = "${gameVenue}"
    
    sendEvent(name: "d1GameDate", value: gameDate, displayed: true)
    sendEvent(name: "d1GameTime", value: gameTime, displayed: true)
    sendEvent(name: "d1AwayTeam", value: awayTeam, displayed: true)
    sendEvent(name: "d1HomeTeam", value: homeTeam, displayed: true)   
    sendEvent(name: "d1Venue", value: gameVenue, displayed: true)
    
    lastUpdated = new Date()
    sendEvent( name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a") )
}

def d2TeamRecords(data) {
    if(logEnable) log.debug "In d2TeamRecords - Received new data! ${data}"
    def (aTeamRecord,hTeamRecord) = data.split(";")
    state.d2AwayTeamRecord = aTeamRecord
    state.d2HomeTeamRecord = HTeamRecord
    sendEvent(name: "d2HomeTeamRecord", value: hTeamRecord, displayed: true)
    sendEvent(name: "d2AwayTeamRecord", value: aTeamRecord, displayed: true)
}

def d2GameData(game) {
    if(logEnable) log.debug "In d2GameData - Received new data! ${game}"
    def (gameDate,awayTeam,homeTeam,gameTime,gameVenue) = game.split(";") 
    
    state.d2GameDate = "${gameDate}"
    state.d2GameTime = "${gameTime}"
    state.d2AwayTeam = "${awayTeam}"
    state.d2HomeTeam = "${homeTeam}"
    state.d2Venue = "${gameVenue}"
    
    sendEvent(name: "d2GameDate", value: gameDate, displayed: true)
    sendEvent(name: "d2GameTime", value: gameTime, displayed: true)
    sendEvent(name: "d2AwayTeam", value: awayTeam, displayed: true)
    sendEvent(name: "d2HomeTeam", value: homeTeam, displayed: true)   
    sendEvent(name: "d2Venue", value: gameVenue, displayed: true)
    
    lastUpdated = new Date()
    sendEvent( name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a") )
}

def d3TeamRecords(data) {
    if(logEnable) log.debug "In d3TeamRecords - Received new data! ${data}"
    def (aTeamRecord,hTeamRecord) = data.split(";")
    state.d3AwayTeamRecord = aTeamRecord
    state.d3HomeTeamRecord = HTeamRecord
    sendEvent(name: "d3HomeTeamRecord", value: hTeamRecord, displayed: true)
    sendEvent(name: "d3AwayTeamRecord", value: aTeamRecord, displayed: true)
    gameScheduleTile()
}

def d3GameData(game) {
    if(logEnable) log.debug "In d3GameData - Received new data! ${game}"
    def (gameDate,awayTeam,homeTeam,gameTime,gameVenue) = game.split(";") 
    
    state.d3GameDate = "${gameDate}"
    state.d3GameTime = "${gameTime}"
    state.d3AwayTeam = "${awayTeam}"
    state.d3HomeTeam = "${homeTeam}"
    state.d3Venue = "${gameVenue}"
    
    sendEvent(name: "d3GameDate", value: gameDate, displayed: true)
    sendEvent(name: "d3GameTime", value: gameTime, displayed: true)
    sendEvent(name: "d3AwayTeam", value: awayTeam, displayed: true)
    sendEvent(name: "d3HomeTeam", value: homeTeam, displayed: true)   
    sendEvent(name: "d3Venue", value: gameVenue, displayed: true)
    
    lastUpdated = new Date()
    sendEvent( name: "lastUpdated", value: lastUpdated.format("MM-dd - h:mm:ss a") )
    gameScheduleTile()
}

def scoreUpdateHome(scoreUpdate) {
    if(logEnable) log.debug "In scoreUpdateHome - Received new data! - ${scoreUpdate}"
    state.liveScoreHome = scoreUpdate
    sendEvent(name: "liveScoreHome", value: scoreUpdate, displayed: true)
    liveGameTile()
}

def scoreUpdateAway(scoreUpdate) {
    if(logEnable) log.debug "In scoreUpdateAway - Received new data! - ${scoreUpdate}"
    state.liveScoreAway = scoreUpdate
    sendEvent(name: "liveScoreAway", value: scoreUpdate, displayed: true)
    liveGameTile()
}

def gameStatus(stats) {
    if(logEnable) log.debug "In gameStatus - Received new data! - ${stats}"
    def (status,message) = stats.split(";")
    state.gameStatus = status
    state.message = message
    sendEvent(name: "gameStatus", value: status, displayed: true)
    sendEvent(name: "gameMessage", value: message, displayed: true)
    liveGameTile()
}

def gameScheduleTile() {
    if(logEnable) log.debug "In gameScheduleTile (${state.version})"
    gameSchedule = "<table>"
    gameSchedule += "<tr><td><b>Games over the next 4 days</b></td></tr>"
    gameSchedule += "<tr><td>${state.todayGameDate} - ${state.todayGameTime} - ${state.todayAwayTeam} vs ${state.todayHomeTeam} @ ${state.todayVenue}</td></tr>"
    gameSchedule += "<tr><td>${state.d1GameDate} - ${state.d1GameTime} - ${state.d1AwayTeam} vs ${state.d1HomeTeam} @ ${state.d1Venue}</td></tr>"
    gameSchedule += "<tr><td>${state.d2GameDate} - ${state.d2GameTime} - ${state.d2AwayTeam} vs ${state.d2HomeTeam} @ ${state.d2Venue}</td></tr>"
    gameSchedule += "<tr><td>${state.d3GameDate} - ${state.d3GameTime} - ${state.d3AwayTeam} vs ${state.d3HomeTeam} @ ${state.d3Venue}</td></tr>"
    gameSchedule += "</table>"
    
    gameScheduleCount = gameSchedule.length()
	if(gameScheduleCount <= 1024) {
		if(logEnable) log.trace "NHL gameSchedule - has ${gameScheduleCount} Characters"
	} else {
		gameScheduleCount = "Too many characters to display on Dashboard (${gameScheduleCount})"
        if(logEnable) log.trace "NHL gameSchedule- has too many Characters (${gameScheduleCount})"
	}
    sendEvent(name: "gameScheduleCount", value: gameScheduleCount, displayed: true)
    sendEvent(name: "gameSchedule", value: gameSchedule, displayed: true)
}

def liveGameTile() {
    if(logEnable) log.debug "In liveGameTile  (${state.version}) - Updating Live Game Tile - gameStatus: ${state.gameStatus}"

    if(state.liveScoreAway == null || state.liveScoreAway == "") state.liveScoreAway = "0"
    if(state.liveScoreHome == null || state.liveScoreHome == "") state.liveScoreHome = "0"
/*    
    state.GAME_STATUS_SCHEDULED            = '1'
    state.GAME_STATUS_PREGAME              = '2'
    state.GAME_STATUS_IN_PROGRESS          = '3'
    state.GAME_STATUS_IN_PROGRESS_CRITICAL = '4'
    state.GAME_STATUS_UNKNOWN              = '5'
    state.GAME_STATUS_FINAL6               = '6'
    state.GAME_STATUS_FINAL7               = '7'
*/    
    if(state.gameStatus == "1" || state.gameStatus == "2") {
        gameTile = "<table>"
        gameTile += "<tr><td colspan=2><b>${state.gameMessage}</b></td></tr>"
        gameTile += "</table>"
    } else if(state.gameStatus == "3" || state.gameStatus == "4" || state.gameStatus == "5") {
        gameTile = "<table>"
        gameTile += "<tr><td colspan=2><b>Live Game Updates</b></td></tr>"
        gameTile += "<tr><td><b>${state.awayTeamName}</b></td><td><b>${state.homeTeamName}</b></td></tr>"
        gameTile += "<tr><td><small>${state.awayTeamRecord}</small></td><td><small>${state.homeTeamRecord}</small></td></tr>"
        gameTile += "<tr><td>${state.liveScoreAway}</td><td>${state.liveScoreHome}</td></tr>"
        gameTile += "<tr><td colspan=2>${state.todayVenue}</td></tr>"
        gameTile += "</table>"
    } else if(state.gameStatus == "6" || state.gameStatus == "7") {
        gameTile = "<table>"
        gameTile += "<tr><td colspan=2><b>Game is Final</b></td></tr>"
        gameTile += "<tr><td><b>${state.awayTeamName}</b></td><td><b>${state.homeTeamName}</b></td></tr>"
        gameTile += "<tr><td><small>${state.awayTeamRecord}</small></td><td><small>${state.homeTeamRecord}</small></td></tr>"
        gameTile += "<tr><td>${state.liveScoreAway}</td><td>${state.liveScoreHome}</td></tr>"
        gameTile += "<tr><td colspan=2>${state.todayVenue}</td></tr>"
        gameTile += "</table>"
    } else {
        gameTile = "<table>"
        gameTile += "<tr><td colspan=2><b>No Data to report</b></td></tr>"
        gameTile += "</table>"
    }
    
    gameTileCount = gameTile.length()
	if(gameTileCount <= 1024) {
		if(logEnable) log.trace "NHL gameTile - has ${gameTileCount} Characters"
	} else {
		gameTileCount = "Too many characters to display on Dashboard (${gameTileCount})"
        if(logEnable) log.trace "NHL gameTile - has too many Characters (${gameTileCount})"
	}
    sendEvent(name: "liveGameTileCount", value: gameTileCount, displayed: true)
    sendEvent(name: "liveGameTile", value: gameTile, displayed: true)
}
