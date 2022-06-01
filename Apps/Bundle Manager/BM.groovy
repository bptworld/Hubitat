/**
 *  **************** Bundle Manager ****************
 *  Design Usage:
 *  The place to explore Bundles. Find, install and update bundles quickly and easily.
 *
 *  Copyright 2022 Bryan Turcotte (@bptworld)
 * 
 *  This App is free. If you like and use this app, please be sure to mention it on the Hubitat forums! Thanks.
 *
 *  Remember...I am not a professional programmer, everything I do takes a lot of time and research!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
 * 
 *  Unless noted in the code, ALL code contained within this app is mine. You are free to change, ripout, copy, modify or
 *  otherwise use the code in anyway you want. This is a hobby, I'm more than happy to share what I have learned and help
 *  the community grow. Have FUN with it!
 *-------------------------------------------------------------------------------------------------------------------
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  1.0.6 - 05/31/22 - adjustments
 *  1.0.5 - 05/31/22 - Added Launch New App Config option
 *  1.0.4 - 05/30/22 - Rearranged some things
 *  1.0.3 - 05/29/22 - More options - changes and enhancements
 *  1.0.2 - 05/29/22 - Minor changes
 *  1.0.1 - 05/29/22 - Minor changes
 *  1.0.0 - 05/28/22 - Initial release.
 */

import groovy.json.*
import groovy.time.TimeCategory
import java.text.SimpleDateFormat

// The setVersion section is required in any app that uses Bundle Manager. It needs to be called from a spot that runs often.
// That way anytime the app runs, it lets Bundle Manager know what version it is.

// Start Required Section
def setVersion(){
    state.name = "Bundle Manager"
	state.version = "1.0.6"
    sendLocationEvent(name: "updateVersionInfo", value: "${state.name}:${state.version}")
}
// End Required Section

definition(
    name: "Bundle Manager",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "The place to explore Bundles. Find, install and update bundles quickly and easily.",
    tags: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
    page(name: "searchOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        display()
        section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
            app.removeSetting("searchtags")
            app.removeSetting("bunSearch")
            app.removeSetting("bunSearchNOT")
            app.removeSetting("bunSearchType")
            app.removeSetting("bunAuthor")
            app.removeSetting("showAllBundles")
            app.removeSetting("showNewBundles")
            input "checkMasterBundles", "bool", title: "Update Master Bundle List <small><abbr title='Remember, it sometimes takes a minute or two for GitHub to reflect any changes made by a developer.'><b>- INFO -</b></abbr></small><br><small>* Switch will turn back off when finished</small>", defaultValue:false, submitOnChange:true, width:6
            paragraph "<center><i>This can take a minute to run!<br>Please be patient.</i></center>", width:6
            if(checkMasterBundles) {
                getMasterBundleList()    
                app.updateSetting("checkMasterBundles",[value:"false",type:"bool"])
            }
            href "searchOptions", title:"Search Options", description:"Click here to search for Bundles!"
            paragraph "<center>${state.theStats}<br><small>Bundles Last Updated: $state.lastUpdated</small></center>"
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Installed Bundle Options")) { 
            input "checkBundles", "bool", title: "Check for Installed Bundle Updates", defaultValue:false, submitOnChange:true,width:6
            if(checkBundles) {
                if(checkMasterBundlesFirst) getMasterBundleList()
                checkBundleHandler()
                app.updateSetting("checkBundles",[value:"false",type:"bool"])
            }
            input "checkMasterBundlesFirst", "bool", title: "Auto Check Master Bundles First", defaultValue:false, submitOnChange:true, width:6
            input "autoUpdate", "bool", title: "Auto update Bundles", defaultValue:false, submitOnChange:true

            if(state.appsToUpdate == [:]) {
                theApps = "<table><tr><td><b><u>Bundles to Update</u></b>"
                theApps += "<tr><td>There are no Bundles to update. If you think this may not be right, be sure to 'Update Master Bundle List' above. Then come back here and 'Check for Installed Bundle Updates' again."
                theApps += "</table>"
                paragraph "${theApps}"
            } else {
                if(!autoUpdate) {
                    try{
                        theInputList = [:]
                        tbundles = []
                        theApps = "<table><tr><td><b><u>Bundles to Update</u></b><td> <td><b><u>New Version</u></b>"
                        state.appsToUpdate.each { tapps ->
                            theKey = tapps.key
                            theValue = tapps.value
                            (tVer, tBun) = theValue.split(";")
                            theApps += "<tr><td>$theKey<td> - <td>$tVer"
                            theInputList.put(theKey, tBun)  
                        }
                        theApps += "</table>"
                        paragraph "${theApps}"
                        theInputList.each{ il ->
                            theKey = il.key
                            tbundles << theKey
                        }
                        input "toUpdate", "enum", title: "Select Bundles to Update", options: tbundles, multiple:true, submitOnChange:true
                        if(toUpdate) {
                            input "doUpdates", "bool", title: "Update Bundles", defaultValue:false, submitOnChange:true
                            paragraph "<small>* Switch will turn back off when finished<br><i>Please be patient.</i></small>"
                            if(doUpdates) {
                                toUpdate.each{ tu ->
                                    theBundle = state.appsToUpdate.get(tu)
                                    (bVer, bURL) = theBundle.split(";")
                                    installBundleHandler(bURL)
                                }
                                app.updateSetting("doUpdates",[value:"false",type:"bool"])
                            }
                        }
                        app.updateSetting("checkBundles",[value:"false",type:"bool"])
                    } catch(e) { 
                        log.error(getExceptionMessageWithLine(e))
                    }
                }
            }

            paragraph "<hr>"
            if(state.versionMap) smSize = state.versionMap.size()
            input "showMap", "bool", title: "Show Installed Bundle Versions <small><abbr title='This list will auto-populate as each app/driver compatible with BM runs.'><b>- INFO -</b></abbr></small><br><small>* Note, you won't see the updated version until the app is either opened or executes.</small><br><small>* There are ${smSize} bundles installed</small>", defaultValue:false, submitOnChange:true
            if(showMap) {
                if(state.versionMap) {
                    sortedMap = state.versionMap.sort()
                    smHalf = (smSize / 2).toInteger()
                    count = 0
                    col1 = "<table><tr><td><b><u>App Name</u></b><td><td><b><u>Version</u></b>"
                    col2 = "<table><tr><td><b><u>App Name</u></b><td><td><b><u>Version</u></b>"
                    sortedMap.each { stuff ->
                        if(count <= smHalf) {
                            col1 += "<tr><td>${stuff.key}<td> - <td> ${stuff.value}"
                        } else {
                            col2 += "<tr><td>${stuff.key}<td> - <td> ${stuff.value}"
                        }
                        count += 1
                    }
                    if(smSize / 2 != 0) { col2 += "<tr><td colspan=3> " }
                    col1 += "</table>"
                    col2 += "</table>"
                    theMap = "<table align=center>"
                    theMap += "<tr><td>${col1}<td>      <td>${col2}"                  
                    theMap += "</table>"
                    paragraph "${theMap}"
                } else {
                    paragraph "Version Info not available"
                }
            }
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
            input "hubSecurity", "bool", title: "Hub Security", submitOnChange:true
            if(hubSecurity) {
                input "hubUsername", "string", title: "Hub Username", required:true, submitOnChange:true, width:6
                input "hubPassword", "password", title: "Hub Password", required:true, submitOnChange:true, width:6
            }
            input "updateBundleListAt", "bool", title: "Automatically Check for New Bundles each day", defaultValue:false, submitOnChange:true, width:7
            if(updateBundleListAt) {
                input "timeUpdate", "time", title: "Time to update bundle list", submitOnChange:true, width:5
            }
            input "checkBundlesAt", "bool", title: "Automatically Check Bundles each day", defaultValue:false, submitOnChange:true, width:7
            if(checkBundlesAt) {
                input "timeCheck", "time", title: "Time to check bundles for updates", submitOnChange:true, width:5
                input "pushUpdates", "bool", title: "Receive push when updates are available", defaultValue:false, submitOnChange:true
                if(pushUpdates) {
                    input "sendPushMessage", "capability.notification", title: "Send a Push notification", multiple:true, required:false, submitOnChange:true
                    input "pushICN", "bool", title: "Include cog name in message", defaultValue:false, submitOnChange:true
                    input "pushHN", "bool", title: "Include hub name in message", defaultValue:false, submitOnChange:true
                    input "pushNoUpdates", "bool", title: "Also send when no bundles need updating", defaultValue:false, submitOnChange:true
                } else {
                    app.removeSetting("sendPushMessage")
                    app.updateSetting("pushUpdates",[value:"false",type:"bool"])
                    app.updateSetting("pushNoUpdates",[value:"false",type:"bool"])
                }
            } else {
                app.removeSetting("sendPushMessage")
                app.updateSetting("pushUpdates",[value:"false",type:"bool"])
                app.updateSetting("pushNoUpdates",[value:"false",type:"bool"])
            }
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            input "showUpdates", "bool", title: "Show updating process in log", defaultValue:false, submitOnChange:true
            input "logEnable", "bool", title: "Enable Debug Options", description: "Log Options", defaultValue:false, submitOnChange:true
            if(logEnable) {
                input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
                input "clearMap", "bool", title: "Clear Map", defaultValue:false, submitOnChange:true, width:3
                if(clearMap) {
                    input "clearMap2", "bool", title: "<b>Are you sure</b>", defaultValue:false, submitOnChange:true, width:3
                    if(clearMap2) {
                        state.versionMap = [:]
                        app.updateSetting("clearMap2",[value:"false",type:"bool"])
                        app.updateSetting("clearMap",[value:"false",type:"bool"])
                    }
                } else {
                    app.updateSetting("clearMap2",[value:"false",type:"bool"])
                }
            }
        }
        display2()
    }
}

def checkRequirements() {
    if(location.hub.firmwareVersionString > "2.3.2.117") {
        state.firmwarePassed = true
    } else {
        state.firmwarePassed = false
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
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        checkRequirements()
        subscribe(location, "updateVersionInfo", updateVersionHandler)
        if(updateBundleListAt) { schedule(timeUpdate, "getMasterBundleList") }
        if(checkBundlesAt) { schedule(timeCheck, "checkBundleHandler") }
    }
}

def searchOptions() {
    dynamicPage(name: "searchOptions", title: "", install: false, uninstall:false){
        getBundleList()
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
            input "showAllBundles", "bool", title: "Show ALL bundles", defaultValue:false, submitOnChange:true, width:5
            paragraph "<b>OR</b> ", width:2
            input "showNewBundles", "bool", title: "Show New Bundles (last 7 days)", defaultValue:false, submitOnChange:true, width:5
            if(showAllBundles && showNewBundles) {
                paragraph "<b>Please only select ONE option at a time.</b>"
            }
            if(showAllBundles || showNewBundles) {
                app.removeSetting("searchtags")
                app.removeSetting("bunAuthor")
                app.removeSetting("bunSearch")
                app.removeSetting("bunSearchNOT")
                app.removeSetting("bunSearchType")               
            } else {
                paragraph "<hr>"
                paragraph "Any combination of the options below can be used."
                tTags = state.allTags.unique().sort()
                input "searchtags", "enum", title: "Choose a Tag To Explore", options:tTags, required:false, submitOnChange:true

                tDevs = state.allDevNames.unique().sort()
                input "bunAuthor", "enum", title: "Choose a Developer To Explore", options:tDevs, required:false, submitOnChange:true

                input "bunSearch", "text", title: "Search ALL Bundles for a specific keyword", required:false, submitOnChange:true, width:6
                input "bunSearchNOT", "text", title: "But does NOT contain keyword (optional)", required:false, submitOnChange:true, width:6         
                if(bunSearch || bunSearchNOT) {
                    input "bunSearchType", "enum", title: "In which Field", options:['name','description'], defaultValue:"name", required:true, submitOnChange:true
                } else {
                    app.removeSetting("bunSearchType")
                }
            }
        }

        findBundles()
        if(state.iBundles) {
            section() {
                doConfig = false
                prevInstalled = false
                input "inputBundle", "enum", title: "Choose Bundle to Install", options: state.iBundles, required:false, submitOnChange:true, width:6
                if(inputBundle) {
                    input "inputBundle2", "bool", title: "<b>Install Bundle</b><br>Switch will turn off when finished", defaultValue:false, submitOnChange:true, width:6
                    if(inputBundle2) {
                        theURL = inputBundle
                        if(logEnable) log.debug "In searchOptions - Sending to installBundleHandler: ${theURL}"
                        state.iBundles.each { ib ->
                            if(ib.key == theURL) bValue = ib.value
                        }
                        state.installedBundles.each { ib ->
                            if(bValue == ib) prevInstalled = true
                        }
                        
                        installBundleHandler(theURL)
                        if(!prevInstalled) doConfig = true
                        app.updateSetting("inputBundle2",[value:"false",type:"bool"])
                    }
                    if(logEnable) log.debug "In searchOptions - prevInstalled: ${prevInstalled}"
                    if(prevInstalled) paragraph "<small>(* Bundle was previously installed, no need to launch Config)</small>"
                    if(doConfig) {
                        if(bValue) getAppsList()
                        if(state.allAppsList) {
                            state.allAppsList.each { al ->
                                if(al.title == bValue) theID = al.id
                            }
                            if(logEnable) log.debug "In searchOptions - Getting app ID: ${theID}"
                            if(theID) {
                                paragraph "<a href='/installedapp/create/${theID}' target='_blank'>CLICK HERE</a> to configure ${bValue}"
                            } else {
                                paragraph "There was an issue getting the app ID"
                            }
                        }
                    }
                }
            }
            section(getFormat("header-green", "${getImage("Blank")} ${state.resultsTitle}  (${getImage("checkMarkGreen2")} means Bundle is already installed.)")) {
                paragraph appsList
            }
        } else {
            section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
                paragraph "No Matches found."
            }
            app.removeSetting("inputBundle")
        }
        display2()
    }
}

def findBundles() {
    if(logEnable) log.debug "In findBundles (${state.version})"
    if(logEnable) log.debug "*"
    if(logEnable) log.debug "*************** Start findBundles ***************"
    allBundles = state.allBundles.sort()
    countToReach = 0
    state.resultsTitle = "<b>Search Results"
    if(showAllBundles) {
        state.resultsTitle += " - All Bundles"
        countToReach = 1
    } else if(showNewBundles) {
        state.resultsTitle += " - New Bundles"
        countToReach += 1
    } else {
        if(searchtags) {
            state.resultsTitle += " - Tags: ${searchtags}"
            countToReach += 1
        }
        if(bunAuthor) {
            state.resultsTitle += " - Developers: ${bunAuthor}"
            countToReach += 1
        }
        if(bunSearch || bunSearchNOT) {
            state.resultsTitle += " - Keywords: ${bunSearch} - NOT: ${bunSearchNOT}"
            countToReach += 1
        }
    }
    state.resultsTitle += "</b>"
    state.iBundles = null
    appsList = ""
    iBundles = [:]
    iMatches = []
    
    if(countToReach) {
        for (bun in allBundles) {
            theName = bun[2]
            if(logEnable) log.debug "*************** Start - ${theName} ***************"
            matchedCount = 0
            try{
                if(showAllBundles) {
                    matchedCount += 1
                } else if(showNewBundles) {
                    try {
                        theUpdated = bun[4]
                        theDate = "${theUpdated} 01:00:00"
                        def prev = Date.parse("yyy-MM-dd HH:mm:ss","${theDate}".replace("+00:00","+0000"))
                        def now = new Date()

                        use(TimeCategory) {       
                            dur = now - prev
                            days = dur.days
                            if(logEnable) log.debug "Author/Package: ${hubitatName} - ${theName} - Days: ${days}"
                        }
                    } catch (e) {
                        log.error(getExceptionMessageWithLine(e))
                    }

                    theDays = days.toInteger()
                    if(theDays < 8) {
                        matchedCount += 1
                    }
                } else {
                    if(searchtags) {
                        theTagValue = bun[9].split(";")       
                        theTagValue.each { eTag ->
                            if(searchtags.contains("${eTag}")) {
                                matchedCount += 1
                                if(logEnable) log.debug "In findBundles - Matched Tag: ${eTag} - matchedCount: ${matchedCount} - VS - countToReach: ${countToReach}"
                            }
                        }
                    }
                    if(bunAuthor) {
                        theAuthorValue = bun[0].split(";")
                        theAuthorValue.each { eAuthor ->
                            if(bunAuthor.contains("${eAuthor}")) {
                                matchedCount += 1
                                if(logEnable) log.debug "In findBundles - Matched Author: ${eAuthor} - matchedCount: ${matchedCount} - VS - countToReach: ${countToReach}"
                            }
                        }
                    }
                    if(bunSearch || bunSearchNOT) {
                        theName           = bun[2]
                        theDescription    = bun[6]
                        searchCount = 0  
                        if(bunSearchType == "description") {
                            if(bunSearch) {
                                if(theDescription.toLowerCase().contains("${bunSearch.toLowerCase()}")) {
                                    searchCount += 1
                                    if(logEnable) log.debug "In findBundles - Matched Description Keyword: ${bunSearch} - matchedCount: ${matchedCount} - VS - countToReach: ${countToReach}"
                                }
                            } else {
                                searchCount += 1
                                if(logEnable) log.debug "In findBundles - Description Keyword: NO MATCH - searchCount: ${searchCount}"
                            }
                            if(bunSearchNOT) {
                                if(theDescription.toLowerCase().contains("${bunSearchNOT.toLowerCase()}")) {
                                    // Do nothing
                                } else {
                                    searchCount += 1
                                    if(logEnable) log.debug "In findBundles - Matched Description NOT Keyword: ${bunSearchNOT} - searchCount: ${searchCount}"
                                }
                            } else {
                                searchCount += 1
                                if(logEnable) log.debug "In findBundles - Description NOT Keyword: Not used - searchCount: ${searchCount}"
                            } 
                        } else {
                            if(bunSearch) {
                                if(theName.toLowerCase().contains("${bunSearch.toLowerCase()}")) {
                                    searchCount += 1
                                    if(logEnable) log.debug "In findBundles - Matched Name Keyword: ${bunSearch} - searchCount: ${searchCount}"
                                }
                            } else {
                                searchCount += 1
                                if(logEnable) log.debug "In findBundles - Name Keyword: Not used - searchCount: ${searchCount}"
                            }
                            if(bunSearchNOT) {
                                if(theName.toLowerCase().contains("${bunSearchNOT.toLowerCase()}")) {
                                    // Do nothing
                                } else {
                                    searchCount += 1
                                    if(logEnable) log.debug "In findBundles - Matched Name NOT Keyword: ${bunSearchNOT} - searchCount: ${searchCount}"
                                }
                            } else {
                                searchCount += 1
                                if(logEnable) log.debug "In findBundles - Name NOT Keyword: Not used - searchCount: ${searchCount}"
                            }
                        }
                        if(searchCount == 2) {
                            matchedCount += 1
                        }
                    }
                }

                if(logEnable) log.debug "In findBundles - matchedCount: ${matchedCount} - VS - countToReach: ${countToReach}"
                if(matchedCount == countToReach) {
                    hubitatName       = bun[0]
                    authorName        = bun[1]
                    paypalURL         = bun[2]
                    theName           = bun[3]
                    theVersion        = bun[4]
                    theUpdated        = bun[5]
                    theChanges        = bun[6]
                    theDescription    = bun[7]
                    theSpecialInfo    = bun[8]
                    theRequiredLib    = bun[9]
                    theTags           = bun[10].replace(";"," - ")
                    bundleURL         = bun[11]
                    forumURL          = bun[12]
                    
                    iBundles.put(bundleURL, theName)
                    iMatches << "${hubitatName};${authorName};${paypalURL};${theName};${theVersion};${theUpdated};${theChanges};${theDescription};${theSpecialInfo};${theRequiredLib};${theTags};${bundleURL};${forumURL}"          
                }
            } catch(e) {
                theName = bun[3]
                if(logEnable) log.debug "In findBundles - Something went wrong, skipping ${theName}"
                log.error(getExceptionMessageWithLine(e))
            }
            if(logEnable) log.debug "------------------------------ End - ${theName} ------------------------------"
        }
    }
    state.iBundles = iBundles.sort()
    if(iMatches) {
        theMat = iMatches.sort()
        theMat.each { mat ->
            state.skip = false
            details = mat.split(";")
            try{
                hubitatName      = details[0]
                authorName       = details[1]
                paypalURL        = details[2]
                theName          = details[3] 
                theVersion       = details[4]
                theUpdated       = details[5]
                theChanges       = details[6]
                theDescription   = details[7]
                theSpecialInfo   = details[8]
                theRequiredLib   = details[9]
                theTags          = details[10]
                bundleURL        = details[11]
                forumURL         = details[12]                
            } catch(e) {
                theName          = details[3]
                hubitatName      = details[0]
                if(logEnable) log.debug "In findBundles - Something went wrong, skipping ${theName} by ${hubitatName}"
                //log.error(getExceptionMessageWithLine(e))
                state.skip = true
            }  
            if(state.skip) {
                if(logEnable) log.debug ""
            } else {
                // Check if installed
                isInstalled = false
                state.installedBundles.each { ib ->
                    if(theName == ib) isInstalled = true
                }
// solid grey
                appsList += "<div style='background-color: white;width: 90%;border: 2px solid green;border-radius: 10px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'><b>${theName}</b>"
                if(isInstalled) appsList += " ${getImage("checkMarkGreen2")}"
                appsList += " - ${authorName} (${hubitatName})<br>Version: ${theVersion} - Updated: ${theUpdated} "
                if(theChanges) {
                    appsList += " <abbr title='${theChanges}'><b>-Change Info- </b></abbr>"
                }
                
                appsList += "<br>${theDescription}"
                
                libInstalled = false
                state.installedBundles.each { lib ->
                    if(theRequiredLib == lib) libInstalled = true
                }
                if(libInstalled) {
                    appsList += getFormat("line")
                } else {
                    if(theSpecialInfo == "na" || theSpecialInfo == "NA") {
                        appsList += getFormat("line")
                    } else {
                        appsList += "<br><br><i>${theSpecialInfo}</i><br><br>"
                    }
                }
                appsList += "Tags: ${theTags}<br>"
                if(forumURL) {
                    if(forumURL == "na" || forumURL == "NA") {
                        theLinks = "Community Thread"
                    } else {
                        theLinks = "<a href='${forumURL}' target='_blank'>Community Thread</a>"
                    }
                }
                if(paypalURL) {
                    if(paypalURL == "na" || paypalURL == "NA") {
                        theLinks += " | Paypal"
                    } else {
                        theLinks += " | <a href='${paypalURL}' target='_blank'><img height='20' src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a>"
                    }
                }
                
                
                appsList += "${theLinks}</div>"
            }
        }
    }
    if(logEnable) log.debug "*************** End findBundles ***************"
    if(logEnable) log.debug "*"
}

def getMasterBundleList() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In getMasterBundleList (${state.version})"
        state.masterBundleList = []    
        def params = [
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Bundles/masterBundles.json",
            requestContentType: "application/json",
            contentType: "application/json",
            timeout: 10
        ]
        try {
            if(showUpdates) log.info "--------------- Getting Master Bundle List ---------------"
            httpGet(params) { resp ->
                def json = resp.data
                for(rec in json.masterBundles) {
                    hName = rec.hubitatName
                    theLoc = rec.location
                    if(logEnable) log.debug "In getMasterBundleList - hName: ${hName} - theLoc: ${theLoc}"
                    state.masterBundleList << theLoc
                }
            }
        } catch(e) {
            log.warn "There was a problem retrieving bundles from hName: ${hName} - theLoc: ${theLoc}"
            log.error(getExceptionMessageWithLine(e))
        }
        getAllBundlesHandler()
    }
}

def getAllBundlesHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In getAllBundlesHandler (${state.version})"
        allBundles = []
        allTags = []
        allDevNames = []
        state.masterBundleList.each { it ->  
            if(logEnable) log.debug "In getAllBundlesHandler - mainURL: ${it}"
            def params = [
                uri: it,
                requestContentType: "application/json",
                contentType: "application/json",
                timeout: 10
            ]

            try {
                if(showUpdates) log.info "--------------- Getting All Bundles from ${it} ---------------"
                httpGet(params) { resp ->
                    def info    = resp.data.info
                    def bundles = resp.data.bundles
                    for(rec in info) {
                        state.combinedInfo = [
                            hubitatName       = rec.hubitatName,
                            authorName        = rec.authorName,
                            paypalURL         = rec.paypal
                        ]
                    }
                    for(rec in bundles) {
                        def combinedBundle = [
                            theName           = rec.name,
                            theVersion        = rec.version,
                            theUpdated        = rec.updated,
                            theChanges        = rec.changes,
                            theDescription    = rec.description,
                            specialInfo       = rec.specialInfo,
                            requiredLib       = rec.requiredLib,
                            theTags           = rec.tags,
                            bundleURL         = rec.bundleURL,
                            forumURL          = rec.forumURL
                        ]

                        if(theName.toLowerCase() != "test" && theName.toLowerCase() != "blank") {
                            combinedRecords = state.combinedInfo + combinedBundle
                            allBundles << combinedRecords
                            allDevNames << hubitatName

                            if(theTags) {
                                def cats = theTags.replace("[","").replace("]","").split(";")
                                cats.each{ cat ->
                                    if(!allTags.contains(cat)) {
                                        allTags << cat
                                    }
                                }
                            }
                        }
                    }
                }
                state.allBundles = allBundles.sort()
                state.allDevNames = allDevNames.unique().sort()
                state.allTags = allTags.unique().sort()
            } catch(e) {
                log.warn "There was a problem retrieving bundle from ${it}"
                log.error(getExceptionMessageWithLine(e))
            }
        }
    }
    theDate = new Date()
    state.lastUpdated = theDate.format("MM-dd-yyyy - h:mm:ss a")
    if(state.allBundles) {
        allBundlesCount = state.allBundles.size()
    } else {
        allBundlesCount = "0"
    }
    if(state.masterBundleList) {
        masterCount = state.masterBundleList.size()
    } else {
        masterCount = "0"
    }
    state.theStats = "Bundle Manager is now serving <b>${allBundlesCount} bundles</b> from <b>${masterCount} developers</b>!"
}

def updateVersionHandler(evt) {
    theData = evt.value.replace("[","").replace("]","")
    (theName, theVersion) = theData.split(":")    // 4508:3.5.0
    if(theVersion == "remove") {
        if(state.versionMap) { state.versionMap.remove(theId) }
    } else {
        if(state.versionMap == null) state.versionMap = [:]
        state.versionMap.put(theName, theVersion)
    }
}

def checkBundleHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "---------- In checkBundleHandler (${state.version}) ----------"
        state.masterBundleList.each { it ->
            if(logEnable) log.debug "In getAllBundlesHandler - mainURL: ${it}"

            def params = [
                uri: it,
                requestContentType: "application/json",
                contentType: "application/json",
                timeout: 10
            ]
            try {
                if(showUpdates) log.info "-"
                if(showUpdates) log.info "--------------- Starting Bundle Manager ---------------"
                state.nameMatch = false
                needsUpdating = false
                msg = ""
                state.appsToUpdate = [:]
                httpGet(params) { resp ->
                    def json = resp.data
                    for(rec in json.bundles) {
                        state.versionMap.each { vm ->
                            aName = vm.key
                            aVersion = vm.value
                            jsonName = rec.name.toString()
                            if(logEnable) log.debug "In checkBundleHandler - Checking aName: ${aName} - VS - jsonName: ${jsonName}"
                            if(aName == jsonName) {
                                jsonVersion = rec.version
                                if(logEnable || showUpdates) log.info "App: ${jsonName} - Checking version: ${aVersion} - VS - jsonVersion: ${jsonVersion}"
                                if(jsonVersion > aVersion) {
                                    if(logEnable || showUpdates) log.info "${jsonName} - Update available!"
                                    needsUpdating = true
                                } else {
                                    if(logEnable || showUpdates) log.info "${jsonName} - No update available."
                                    needsUpdating = false
                                }
                                state.nameMatch = true
                                if(needsUpdating) {
                                    if(logEnable) log.debug "In checkBundleHandler - Needs Updating - ${jsonName}"
                                    bundle = rec.bundleURL
                                    tData = "${jsonVersion};${bundle}"
                                    state.appsToUpdate.put(jsonName,tData)                
                                    if(autoUpdate) { 
                                        installBundleHandler(bundle)
                                    } else {
                                        if(pushUpdates) {
                                            if(msg == "") {
                                                msg += "$jsonName"
                                            } else {
                                                msg += ", $jsonName"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if(!state.nameMatch) {
                        if(logEnable) log.debug "In checkBundleHandler - No Match found in the json file"
                    }
                }
                if(state.appsToUpdate == [:]) {
                    app.updateLabel(state.name + " - <span style='color:green'>No updates available</span>")
                    if(pushUpdates && pushNoUpdates) {
                        theMsg = "No Bundles need updating"
                        pushHandler(theMsg)
                    }
                } else {
                    if(state.appsToUpdate) tCount = state.appsToUpdate.size()
                    if(tCount == 1) {
                        app.updateLabel(state.name + " - <span style='color:red'>" + tCount + " Update Available!</span>")
                        if(logEnable || showUpdates) log.info "**** ${tCount} Update Available! ****"
                        if(pushUpdates) {
                            theMsg = "Bundle that need updating: ${msg}"
                            pushHandler(theMsg)
                        }
                    } else {
                        app.updateLabel(state.name + " - <span style='color:red'>" + tCount + " Updates Available!</span>")
                        if(logEnable || showUpdates) log.info "**** ${tCount} Updates Available! ****"
                        if(pushUpdates) {
                            theMsg = "Bundles that need updating: ${msg}"
                            pushHandler(theMsg)
                        }
                    }
                }
                if(showUpdates) log.info "--------------- End Bundle Manager ---------------"
                if(showUpdates) log.info "-"
            } catch (e) { 
                log.error(getExceptionMessageWithLine(e))
            }
        }
        if(logEnable) log.debug "---------- End checkBundleHandler ----------"
    }
}

def login() {        // code modified from @dman2306
    if(logEnable) log.debug "In installBundleHandler - Checking Hub Security"
    try{
        httpPost(
            [
                uri: "http://127.0.0.1:8080",
                path: "/login",
                query: 
                [
                    loginRedirect: "/"
                ],
                body:
                [
                    username: hubUsername,
                    password: hubPassword,
                    submit: "Login"
                ],
                textParser: true,
                ignoreSSLIssues: true
            ]
        )
        { resp ->
            if (resp.data?.text?.contains("The login information you supplied was incorrect.")) {
                log.warn "Bundle Manager - username/password is incorrect."
            } else {
                state.cookie = resp?.headers?.'Set-Cookie'?.split(';')?.getAt(0)
            }
        }
    } catch (e) {
        og.error(getExceptionMessageWithLine(e))
    }
}

def installBundleHandler(bundle) {
    if(hubSecurity) { login() }
    def jsonData =  JsonOutput.toJson([url:"$bundle",installer:FALSE, pwd:''])
    try {
        def params = [
            uri: 'http://127.0.0.1:8080/bundle/uploadZipFromUrl',
            headers: [
                "Accept": '*/*',
                "ContentType": 'text/plain; charset=utf-8',
                "Cookie": state.cookie
            ],
            body: "$jsonData",
            timeout: 90,
            ignoreSSLIssues: true
        ]
        if(logEnable) log.debug "In installBundleHandler - Getting data ($params)"
        httpPost(params) { resp ->
             if(logEnable) log.debug "In installBundleHandler - Receiving file: ${bundle}"
        }
   } catch (e) {
        log.error(getExceptionMessageWithLine(e))
   }
    if(logEnable) log.debug "In installBundleHandler - Finished"
}


def getBundleList() {        // Code by gavincampbell, modified to work with bundles
    if(hubSecurity) { login() }
    if(logEnable) log.debug "In getBundleList - Getting installed Bundles list"
    def params = [
        uri: "http://127.0.0.1:8080/bundle/list",
        textParser: true,
        headers: [
            Cookie: state.cookie
        ]
    ]
	
	state.installedBundles = []
	try {
		httpGet(params) { resp ->     
			def matcherText = resp.data.text.replace("\n","").replace("\r","")
			def matcher = matcherText.findAll(/(<tr class="bundle-row" data-bundle-id="[^<>]+">.*?<\/tr>)/).each {
				def allFields = it.findAll(/(<td .*?<\/td>)/) // { match,f -> return f } 
				def title = allFields[0].find(/title="([^"]+)/) { match,t -> return t.trim() }
				state.installedBundles << title
			}
		}
	} catch (e) {
		log.error "Error retrieving installed apps: ${e}"
	}
}

// Thanks to gavincampbell for the code below!
def getAppsList() {
    if(hubSecurity) { login() }
    if(logEnable) log.debug "In getAppsList - Getting installed Apps list"
	def params = [
		uri: "http://127.0.0.1:8080/app/list",
		textParser: true,
		headers: [
			Cookie: state.cookie
		]
	  ]
	
	def result = []
	try {
		httpGet(params) { resp ->     
			def matcherText = resp.data.text.replace("\n","").replace("\r","")
			def matcher = matcherText.findAll(/(<tr class="app-row" data-app-id="[^<>]+">.*?<\/tr>)/).each {
				def allFields = it.findAll(/(<td .*?<\/td>)/) // { match,f -> return f } 
				def id = it.find(/data-app-id="([^"]+)"/) { match,i -> return i.trim() }
				def title = allFields[0].find(/title="([^"]+)/) { match,t -> return t.trim() }
				def namespace = allFields[1].find(/>([^"]+)</) { match,ns -> return ns.trim() }
				result += [id:id,title:title,namespace:namespace]
			}
		}
	} catch (e) {
		log.error "Error retrieving installed apps: ${e}"
	}
	state.allAppsList = result
}

def pushHandler(msg){
    if(logEnable) log.debug "In pushHandler (${state.version}) - Sending a push - msg: ${msg}"
    theMessage = ""
    if(pushICN) {
        theMessage += "Bundle Manager - "
    }
    if(pushHN) {
        theMessage += "${location.name} - "
    }
    theMessage += "${msg}"
    if(logEnable) log.debug "In pushHandler - Sending message: ${theMessage}"
    sendPushMessage.deviceNotification(theMessage)
}

// *************************************************
def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    setVersion()
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            theStatus = it.currentValue("switch")
            if(theStatus == "on") { state.eSwitch = true }
        }
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}"
    }
}

def getImage(type) {					// Modified code from @Stephack
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=33>"
    if(type == "checkMarkGreen2") return "${loc}checkMarkGreen2.png height=20 width=22>"
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>"
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>"
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>"
    if(type == "logo") return "${loc}logo.png height=60>"
}

def getFormat(type, myText="") {			// Modified code from @Stephack
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;' />"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    section (getFormat("title", "${getImage("logo")}" + " ${state.name}")) {
        paragraph "<div style='color:#000000;text-align:left;font-size:18px;font-weight:bold'><i>The place</i> to explore Bundles. Find, install and update bundles quickly and easily.</div>"
        paragraph getFormat("line")
    }
}

def display2() {
    section() {
        paragraph getFormat("line")
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br>Donations are never necessary but always appreciated!<br><a href='https://paypal.me/bptworld' target='_blank'><img src='https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/pp.png'></a></div>"
        paragraph "${state.footerMessage}"
    }
}
