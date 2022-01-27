/**
 *  **************** Package Explorer App ****************
 *
 *  Design Usage:
 *  Conveniently explore the apps and drivers available within the Hubitat Package Manager.
 *
 *  Copyright 2020-2022 Bryan Turcotte (@bptworld)
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
 *  Special thanks to @dman2306 for his amazing work on Hubitat Package Manager.
 *  All code by dman2306 used with permission. Thank you.
 *
 *  Changes:
 *  1.1.0 - 01/27/22 - Major changes to make it work much better
 *  ---
 *  1.0.0 - 05/01/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat
import groovy.transform.Field


@Field static String repositoryListing = "https://raw.githubusercontent.com/dcmeglio/hubitat-packagerepositories/master/repositories.json"
@Field static List categories = [] 
@Field static List allPackages = []
@Field static def listOfRepositories = [:]
    
def setVersion(){
    state.name = "Package Explorer"
	state.version = "1.1.0"
}

definition(
    name: "Package Explorer",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Conveniently explore the apps and drivers available within the Hubitat Package Manager.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Package%20Explorer/pe.groovy",
)

preferences {
    page(name: "pageConfig")
    page(name: "searchOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "tagOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "categoryOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "developerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "whatsNewOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "masterListOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {      
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        installCheck()
		if(state.appInstalled == 'COMPLETE'){
            section("${getImage('instructions')} <b>Information:</b>", hideable: true, hidden: true) {
                paragraph "<b>Use:</b>"
                paragraph "Conveniently explore the apps and drivers available within the Hubitat Package Manager.<br>All credit goes to @dman2306 for his amazing work on HPM."
                paragraph "<b>Notes:</b>"
                paragraph "As more and more packages come online, it will take a bit longer for this app to sort through all the great packages. Please be patient after clicking the Update Button. Two ways to know when the app is ready<br> - 1) The Last Updated date/time will change.<br> - 2) Watch the Log for the 'Finished' message."
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" HPM Information")) {
                paragraph "Hubitat Package Manager is now serving <b>${state.packCount} packages</b> (apps/drivers) from <b>${state.repoCount} developers</b>!"
                input "installedRepositories", "enum", title: "Available Repositories", options:state.reposToShow, multiple:true, required:false, submitOnChange:true
                input "checkForNewRepos", "button", title: "Check For New Developer Repositories", width:6
                input "checkForUpdates", "button", title: "Download all package descritpions", width:6
                paragraph "* Last Updated: $state.lastUpdated</small>"
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
                app.removeSetting("pkgCategory")
                app.removeSetting("pkgSearch")
                app.removeSetting("pkgSearchNOT")
                app.removeSetting("pkgSearchType")
                app.removeSetting("pkgAuthor")

                checkEnableHandler()
                if(pauseApp || state.eSwitch) {
                    paragraph "${app.label} is Paused or Disabled"
                    log.info "${app.label} is Paused or Disabled"
                } else {
                    href "categoryOptions", title:"Category Options", description:"See all Apps and Drivers the selected Category has to offer"
                    href "tagOptions", title:"Tag Options", description:"Search by Tags across all Developers and Categories"
                    href "developerOptions", title:"Developer Options", description:"See all Apps and Drivers the selected Developer has to offer"
                    href "searchOptions", title:"Keyword Options", description:"Use Keywords to search for an App or Driver across all Developers and Categories"
                    href "masterListOptions", title:"Master List Options", description:"Creates an alphabetical list of ALL packages. <i><b>This will take a minute to run! Please be patient.</b></i>"
                    href "whatsNewOptions", title:"What's New Options", description:"See 'What's New' within the past 7 days. <i><b>This will take a minute to run! Please be patient.</b></i>"
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
                if(pauseApp) { 
                    paragraph app.label
                } else {
                    label title: "Enter a name for this automation", required:false
                }

                input "logEnable", "bool", title: "Enable Debug Logging", description: "Debug Log", defaultValue:false, submitOnChange:true
                if(logEnable) {
                    input "logOffTime", "enum", title: "Logs Off Time", required:false, multiple:false, options: ["1 Hour", "2 Hours", "3 Hours", "4 Hours", "5 Hours", "Keep On"]
                } else {
                    app.removeSetting("logOffTime")
                }
            }
            display2()
        }
	}
}

def installCheck(){
    display()
	state.appInstalled = app.getInstallationState() 
	if(state.appInstalled != 'COMPLETE'){
		section{paragraph "Please hit 'Done' to install '${app.label}' app "}
  	}
  	else{
    	if(logEnable) log.info "App Installed OK"
  	}
}

def categoryOptions() {
    dynamicPage(name: "categoryOptions", title: "", install: false, uninstall:false){
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
            input "pkgCategory", "enum", title: "Choose a Category To Explore", options:state.categories, required:false, submitOnChange:true
            if(pkgCategory) {
                input "sortBy", "bool", title: "Sort packages by Name (off) or by Author (on)", description: "Sorting", defaultValue:false, submitOnChange:true
            }
        }
        
        if(pkgCategory) {
            findPackagesByCategory()
            section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
                paragraph appsList
            }
	    }
    }
}

def tagOptions() {
    dynamicPage(name: "tagOptions", title: "", install: false, uninstall:false){
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Tag Options")) {
            input "pkgTag", "enum", title: "Choose a Tag To Explore", options:state.tags, required:false, submitOnChange:true
            if(pkgTag) {
                input "sortBy", "bool", title: "Sort packages by Name (off) or by Author (on)", description: "Sorting", defaultValue:false, submitOnChange:true
            }
            paragraph "<small>Note: It's up to each developer to maintain the tags to their packages. Not all developers utitlize the Tag option.</small>"
        }
        
        if(pkgTag) {
            findPackagesByTag()
            section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
                paragraph appsList
            }
	    }
    }
}

def developerOptions() {
    dynamicPage(name: "developerOptions", title: "", install: false, uninstall:false){
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
            input "pkgAuthor", "enum", title: "Choose a Developer To Explore", options:state.allDevNames, required:true, submitOnChange:true
            if(pkgAuthor) {
                input "sortBy", "bool", title: "Sort packages by App Name (off) or by Category (on)", description: "Sorting", defaultValue:false, submitOnChange:true
            }
        }
        
        if(pkgAuthor) {
            findPackagesByDeveloper()
            section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
                paragraph appsList
            }
	    }
    }
}

def searchOptions() {
    dynamicPage(name: "searchOptions", title: "", install: false, uninstall:false){
        display()
		section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
            input "pkgSearch", "text", title: "Search ALL Apps for a specific keyword", required:false, submitOnChange:true, width:6
            input "pkgSearchNOT", "text", title: "But does NOT contain keyword (optional)", required:false, submitOnChange:true, width:6         
            if(pkgSearch) {
                input "pkgSearchType", "enum", title: "In which Field", options:['name','description'], required:true, submitOnChange:true
                input "sortBy", "bool", title: "Sort packages by Name (off) or by Author (on)", description: "Sorting", defaultValue:false, submitOnChange:true
            }
        }
        
        if(pkgSearch) {
            findPackagesBySearch()
            section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
                paragraph appsList
            }
	    }
    }
}

def masterListOptions() {
    dynamicPage(name: "masterListOptions", title: "", install: false, uninstall:false){
        display()		
        findAllPackages()
        section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
            paragraph appsList
        }
    }
}

def whatsNewOptions() {
    dynamicPage(name: "whatsNewOptions", title: "", install: false, uninstall:false){
        display()
		
        findPackagesByDateAdded()

        section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
            paragraph appsList

        }
    }
}

def findPackagesByCategory() {
    if(logEnable) log.debug "In findPackagesByCategory (${state.version})"   
    if(!sortBy) allPackages = state.allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = state.allPackages.sort{ name-> name.author}    
                
    state.resultsTitle = "<b>Search Results for Category - ${pkgCategory}</b>"
    appsList = ""
    
	for (pkg in allPackages) {
		if (pkg.category == pkgCategory) {      
            def info = getJSONFile(pkg.location)
                        
            theLinks = ""            
            if(info.documentationLink) {
                theLinks += "| <a href='${info.documentationLink}' target='_blank'>Documentation</a> "
            }
            if(info.communityLink) {
                theLinks += "| <a href='${info.communityLink}' target='_blank'>Community Thread</a> "
            }
            if(pkg.gitHubUrl) {
                theLinks += "| <a href='${pkg.gitHubUrl}' target='_blank'>GitHub</a> "
            }
            if(pkg.payPalUrl) {
                theLinks += "| <a href='${pkg.payPalUrl}' target='_blank'>Donate</a> "
            }
            if(theLinks != "") theLinks += "|"               
                
            if(!sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'><b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"
                        
            if(sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.author}) - <b>${pkg.name}</b><br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"
            theLinks = ""            
        }
    }
}
    
def findPackagesByTag() {
    if(logEnable) log.debug "In findPackagesByTag (${state.version})"   
    if(!sortBy) allPackages = state.allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = state.allPackages.sort{ name-> name.author}    
                
    state.resultsTitle = "<b>Search Results for Tag - ${pkgTag}</b>"
    appsList = ""
    
	for (pkg in allPackages) {
        if(pkg.tags) {            
            pkg.tags.each{ it ->
                if (it == pkgTag) {      
                    def info = getJSONFile(pkg.location)

                    theLinks = ""            
                    if(info.documentationLink) {
                        theLinks += "| <a href='${info.documentationLink}' target='_blank'>Documentation</a> "
                    }
                    if(info.communityLink) {
                        theLinks += "| <a href='${info.communityLink}' target='_blank'>Community Thread</a> "
                    }
                    if(pkg.gitHubUrl) {
                        theLinks += "| <a href='${pkg.gitHubUrl}' target='_blank'>GitHub</a> "
                    }
                    if(pkg.payPalUrl) {
                        theLinks += "| <a href='${pkg.payPalUrl}' target='_blank'>Donate</a> "
                    }
                    if(theLinks != "") theLinks += "|"               

                    if(pkg.tags) {
                        tags = pkg.tags
                    } else tags = ""
                    
                    if(!sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'><b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"

                    if(sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.author}) - <b>${pkg.name}</b><br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"
                    theLinks = ""            
                }
            }
        }
    }
}

def findPackagesByDeveloper() {
    if(logEnable) log.debug "In findPackagesByDeveloper (${state.version})"
    if(!sortBy) allPackages = state.allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = state.allPackages.sort{ name-> name.category}

    state.resultsTitle = "<b>Search Results for Developer - ${pkgAuthor}</b>"
    appsList = ""
    
	for (pkg in allPackages) {
        if(logEnable) log.debug "In findPackagesByDeveloper - name: ${pkg.author} - search: ${pkgAuthor}"
		if (pkg.author == pkgAuthor) {      
            def info = getJSONFile(pkg.location)
                        
            theLinks = ""
            if(info) {
                if(info.documentationLink) {
                    theLinks += "| <a href='${info.documentationLink}' target='_blank'>Documentation</a> "
                }
                if(info.communityLink) {
                    theLinks += "| <a href='${info.communityLink}' target='_blank'>Community Thread</a> "
                }
                if(pkg.gitHubUrl) {
                    theLinks += "| <a href='${pkg.gitHubUrl}' target='_blank'>GitHub</a> "
                }
                if(pkg.payPalUrl) {
                    theLinks += "| <a href='${pkg.payPalUrl}' target='_blank'>Donate</a> "
                }
                if(theLinks != "") theLinks += "|"               
                
                if(pkg.tags) {
                    tags = pkg.tags
                } else tags = ""
                
                appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - <b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"
            }
        }
    }
}

def findPackagesBySearch() {
    if(logEnable) log.debug "In findPackagesBySearch (${state.version})"
    if(!sortBy) allPackages = state.allPackages.sort{ name-> name.name }
    if(sortBy) allPackages = state.allPackages.sort{ name-> name.author }
                
    state.resultsTitle = "<b>Search Results for Keyword - ${pkgSearch}</b>"
    theLinks = "" 
    appsList = ""
    if(pkgSearchNOT == null) pkgSearchNOT = "abcdefghijklmnop1234567890"
    
	for(pkg in allPackages) {
        if(logEnable) log.debug "In findPackageBySearch - name: ${pkg.name.toLowerCase()} - search: ${pkgSearch.toLowerCase()}"
        if(pkgSearchType == "name") { thePkg = pkg.name }
        if(pkgSearchType == "description") { thePkg = pkg.description }
        
        if(thePkg && pkgSearch) {
            if(thePkg.toLowerCase().contains("${pkgSearch.toLowerCase()}")) {            
                if(thePkg.toLowerCase().contains("${pkgSearchNOT.toLowerCase()}")) {
                    if(logEnable) log.debug "In findPackageBySearch - Found Keyword not allowed - Skipping"
                } else {               
                    if(logEnable) log.info "In findPackageBySearch - MATCHED!"
                    def info = getJSONFile(pkg.location)
                    theLinks = ""
                    if(info.documentationLink) {
                        theLinks += "| <a href='${info.documentationLink}' target='_blank'>Documentation</a> "
                    }
                    if(info.communityLink) {
                        theLinks += "| <a href='${info.communityLink}' target='_blank'>Community Thread</a> "
                    }
                    if(pkg.gitHubUrl) {
                        theLinks += "| <a href='${pkg.gitHubUrl}' target='_blank'>GitHub</a> "
                    }
                    if(pkg.payPalUrl) {
                        theLinks += "| <a href='${pkg.payPalUrl}' target='_blank'>Donate</a> "
                    }
                    if(theLinks != "") theLinks += "|"
                    //log.warn info

                    if(pkg.tags) {
                        tags = pkg.tags
                    } else tags = ""
                    
                    if(!sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - <b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"

                    if(sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - (${pkg.author}) - <b>${pkg.name}</b><br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"
                    theLinks = "" 
                }
            }
        }
    }
}

def findAllPackages() {
    if(logEnable) log.debug "In findAllPackages (${state.version})"
    allPackages = state.allPackages.sort{ name-> name.name}   
            
    state.resultsTitle = "<b>Search Results for All Packages</b>"
    appsList = ""
    
	for (pkg in allPackages) {
        if(logEnable) log.debug "In findPackagesByDeveloper - Getting all packages - author: ${pkg.author} - package: ${pkg.name}"   
        def info = getJSONFile(pkg.location)
        theLinks = ""
        if(info) {
            if(info.documentationLink) {
                theLinks += "| <a href='${info.documentationLink}' target='_blank'>Documentation</a> "
            }
            if(info.communityLink) {
                theLinks += "| <a href='${info.communityLink}' target='_blank'>Community Thread</a> "
            }
            if(pkg.gitHubUrl) {
                theLinks += "| <a href='${pkg.gitHubUrl}' target='_blank'>GitHub</a> "
            }
            if(pkg.payPalUrl) {
                theLinks += "| <a href='${pkg.payPalUrl}' target='_blank'>Donate</a> "
            }
            if(theLinks != "") theLinks += "|"               

            if(pkg.tags) {
                tags = pkg.tags
            } else tags = ""
            
            appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - <b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}<br><br>Tags: ${tags}</div>"
        }
    }
}

def findPackagesByDateAdded() {
    if(logEnable) log.debug "In findPackagesByDateAdded (${state.version})"   
    allPackages = state.allPackages.sort{ name-> name.name}                
    state.resultsTitle = "<b>Search Results for New Packages</b>"
    appsList = ""
    allDays7 = []
    
	for (pkg in allPackages) {
        def info = getJSONFile(pkg.location)

        theLinks = "" 
        try {
            if(info.documentationLink) {
                theLinks += "| <a href='${info.documentationLink}' target='_blank'>Documentation</a> "
            }
            if(info.communityLink) {
                theLinks += "| <a href='${info.communityLink}' target='_blank'>Community Thread</a> "
            }
            if(pkg.gitHubUrl) {
                theLinks += "| <a href='${pkg.gitHubUrl}' target='_blank'>GitHub</a> "
            }
            if(pkg.payPalUrl) {
                theLinks += "| <a href='${pkg.payPalUrl}' target='_blank'>Donate</a> "
            }
            if(theLinks != "") theLinks += "|"
        } catch (a) {
            //log.error a
        }
        
        try {
            theDate = "${info.dateReleased} 01:00:00"
            def prev = Date.parse("yyy-MM-dd HH:mm:ss","${theDate}".replace("+00:00","+0000"))
            def now = new Date()

            use(TimeCategory) {       
                dur = now - prev
                days = dur.days
                if(logDebug) log.debug "Author/Package: ${pkg.author} - ${pkg.name} - Days: ${days}"
            }
        } catch (e) {            
            //log.error e
        }
        
        if(days) {
            theDays = days.toInteger()

            if(theDays <= 7 && pkg.description) {    
                def mapInfo = [
                    cat: pkg.category,
                    name: pkg.name,
                    author: pkg.author,
                    theDays: theDays,
                    theLinks: theLinks,
                    description: pkg.description,
                    tags: pkg.tags
                ]               
                allDays7 << mapInfo      
            }
        }
    }
    
    sortedByDays = allDays7.sort{ stuff -> stuff.theDays }
    sortedByDays.each { data -> 
        if(data.tags) {
            tags = data.tags
        } else tags = ""
        
        if(data.theDays == 1) {
            appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>${data.theDays} day - <b>${data.name}</b> - ${data.cat} - ${data.author}<br>${data.theLinks}<br>${data.description}<br><br>Tags: ${tags}</div>"
        } else {
            appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>${data.theDays} days - <b>${data.name}</b> - ${data.cat} - ${data.author}<br>${data.theLinks}<br>${data.description}<br><br>Tags: ${tags}</div>"
        }
    }
    theLinks = ""     
}

def reposToShowHandler() {        // Code by dman2306
    if(logEnable) log.debug "In reposToShowHandler (${state.version})"
    def reposToShow = [:]
    state.repoCount = 0
	listOfRepositories.repositories.each { r -> reposToShow << ["${r.location}":r.name] }
	if (state.customRepositories != null)
		state.customRepositories.each { r -> reposToShow << ["${r.key}":r.value] }
	state.reposToShow = reposToShow.sort { r -> r.value }
    state.repoCount = state.reposToShow.size()
    theDate = new Date()
    state.lastUpdated = theDate.format("MM-dd - h:mm:ss a")
}

def getRepoName(location) {        // Code by dman2306
	return listOfRepositories.repositories.find { it -> it.location == location }?.name
}

def performRepositoryRefresh() {        // Code by dman2306, mods by bptworld
    if(logEnable) log.debug "In performRepositoryRefresh (${state.version})"
    theDate = new Date()
    state.lastUpdated = theDate.format("MM-dd - h:mm:ss a")
	allPackages = []
	categories = []
    tags = []
    allDevNames = []
    tempList = ""
    state.packCount = 0

	for (repo in installedRepositories) {
		def repoName = getRepoName(repo)
        if(logEnable) log.debug "In performRepositoryRefresh - Working on repo: ${repoName}"
		def fileContents = getJSONFile(repo)
		if (!fileContents) {
			log.warn "Error refreshing ${repoName}"
			continue
		}
		for (pkg in fileContents.packages) {
			def pkgDetails = [
				repository: repoName,
				author: fileContents.author,
				githubUrl: fileContents.gitHubUrl,
				payPalUrl: fileContents.payPalUrl,
				name: pkg.name,
				description: pkg.description,
				location: pkg.location,
				category: pkg.category,
                tags: pkg.tags
			]
			allPackages << pkgDetails
            if(!tempList.contains("${pkgDetails.author}") && pkgDetails.author != null && pkgDetails.author != "null") {
                tempList += "${pkgDetails.author}, "
                allDevNames << pkgDetails.author
            }
            
			if (!categories.contains(pkgDetails.category) && pkgDetails.category != null && pkgDetails.category != "null") {
				categories << pkgDetails.category
            }
            
            if (pkgDetails.tags && pkgDetails.tags != null && pkgDetails.tags != "null") {
                def nTags = pkgDetails.tags
                nTags.each{ it ->
                    if(!tags.contains(it)) {
                        tags << it
                    }
                }        
            }
		}
	}
    state.allPackages = allPackages.sort()
	state.categories = categories.sort()
    state.tags = tags.sort()
    state.allDevNames = allDevNames.sort()    
    state.packCount = allPackages.size()
}

def getJSONFile(uri) {        // Code by dman2306
	try {
		def fileContents = downloadFile(uri)
		return new groovy.json.JsonSlurper().parseText(fileContents)
	} catch (e) {
		return null
	}	
}

def installed() {
    if(logEnable) log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
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
        updateRepositoryListing()
        performRepositoryRefresh()       
        reposToShowHandler()
    }
}

def downloadFile(file) {        // Code by dman2306
	try {
		def params = [
			uri: file,
			requestContentType: "application/json",
			contentType: "application/json",
			textParser: true,
			timeout: 300
		]
		def result = null
		httpGet(params) { resp ->
			result = resp.data.text
		}
		return result
	} catch (e) {
		log.error "In downloadFile - Error downloading ${file}: ${e}"
		return null
	}
}

def updateRepositoryListing() {
	if(logEnable) log.debug "In updateRepositoryListing - Refreshing repository list (${state.version})"
    listOfRepositories = getJSONFile(repositoryListing)
    def repos = [] as List
    listOfRepositories.repositories.each { it -> repos << it.location }
    if(logEnable) log.debug "In updateRepositoryListing - Finished refreshing"
}

def getAppList() {    // Thanks to gavincampbell for the code below!
    if(logEnable) log.debug "In getAppList (${state.version})"
    def params = [
    	uri: "http://127.0.0.1:8080/app/list",
        textParser: true,
        headers: [
			Cookie: state.cookie
		]
      ]    
	state.result = []
    try {
        httpGet(params) { resp ->     
            def matcherText = resp.data.text.replace("\n","").replace("\r","")
            def matcher = matcherText.findAll(/(<tr class="app-row" data-app-id="[^<>]+">.*?<\/tr>)/).each {
                def allFields = it.findAll(/(<td .*?<\/td>)/) // { match,f -> return f } 
                def id = it.find(/data-app-id="([^"]+)"/) { match,i -> return i.trim() }
                def title = allFields[0].find(/title="([^"]+)/) { match,t -> return t.trim() }
                def namespace = allFields[1].find(/>([^"]+)</) { match,ns -> return ns.trim() }
                state.result += [id:id,title:title,namespace:namespace]
            }
        }
    } catch (e) {
		log.error "Error retrieving installed apps: ${e}"
    }
}

def appButtonHandler(evt) {
    if(logEnable) log.debug "In buttonHandler (${state.version}) - ($evt)"
    def buttonAction = evt.value.toString()
    if(logEnable) log.debug "********** Starting $buttonAction **********"
    if(buttonAction == "checkForNewRepos") {
        updateRepositoryListing()
        reposToShowHandler()
        log.info "Package Explorer has finished refreshing the repositories (Developer Listing)"
    } else if(buttonAction == "checkForUpdates") {
        if(logEnable) log.debug "In buttonHandler - $buttonAction"
        performRepositoryRefresh()       
        reposToShowHandler()
        log.info "Package Explorer has finished looking for all Apps/Drivers"
    } else {
        if(logEnable) log.debug "In buttonHandler - Button NOT recognized - ${buttonAction}"
    }
    if(logEnable) log.debug "********** End $buttonAction **********"
}

// ~~~~~ start include (2) BPTWorld.bpt-normalStuff ~~~~~
library ( // library marker BPTWorld.bpt-normalStuff, line 1
        base: "app", // library marker BPTWorld.bpt-normalStuff, line 2
        author: "Bryan Turcotte", // library marker BPTWorld.bpt-normalStuff, line 3
        category: "Apps", // library marker BPTWorld.bpt-normalStuff, line 4
        description: "Standard Things for use with BPTWorld Apps", // library marker BPTWorld.bpt-normalStuff, line 5
        name: "bpt-normalStuff", // library marker BPTWorld.bpt-normalStuff, line 6
        namespace: "BPTWorld", // library marker BPTWorld.bpt-normalStuff, line 7
        documentationLink: "", // library marker BPTWorld.bpt-normalStuff, line 8
        version: "1.0.0", // library marker BPTWorld.bpt-normalStuff, line 9
        disclaimer: "This library is only for use with BPTWorld Apps and Drivers. If you wish to use any/all parts of this Library, please be sure to copy it to a new library and use a unique name. Thanks!" // library marker BPTWorld.bpt-normalStuff, line 10
) // library marker BPTWorld.bpt-normalStuff, line 11

import groovy.time.TimeCategory // library marker BPTWorld.bpt-normalStuff, line 13
import java.text.SimpleDateFormat // library marker BPTWorld.bpt-normalStuff, line 14

def checkHubVersion() { // library marker BPTWorld.bpt-normalStuff, line 16
    hubVersion = getHubVersion() // library marker BPTWorld.bpt-normalStuff, line 17
    hubFirmware = location.hub.firmwareVersionString // library marker BPTWorld.bpt-normalStuff, line 18
    log.trace "Hub Info: ${hubVersion} - ${hubFirware}" // library marker BPTWorld.bpt-normalStuff, line 19
} // library marker BPTWorld.bpt-normalStuff, line 20

def parentCheck(){   // library marker BPTWorld.bpt-normalStuff, line 22
	state.appInstalled = app.getInstallationState()  // library marker BPTWorld.bpt-normalStuff, line 23
	if(state.appInstalled != 'COMPLETE'){ // library marker BPTWorld.bpt-normalStuff, line 24
		parentChild = true // library marker BPTWorld.bpt-normalStuff, line 25
  	} else { // library marker BPTWorld.bpt-normalStuff, line 26
    	parentChild = false // library marker BPTWorld.bpt-normalStuff, line 27
  	} // library marker BPTWorld.bpt-normalStuff, line 28
} // library marker BPTWorld.bpt-normalStuff, line 29

def createDeviceSection(driverName) { // library marker BPTWorld.bpt-normalStuff, line 31
    paragraph "This child app needs a virtual device to store values. Remember, multiple child apps can share this device if needed." // library marker BPTWorld.bpt-normalStuff, line 32
    input "useExistingDevice", "bool", title: "Use existing device (off) or have one created for you (on)", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 33
    if(useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 34
        input "dataName", "text", title: "Enter a name for this vitual Device (ie. 'Front Door')", required:true, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 35
        paragraph "<b>A device will automatically be created for you as soon as you click outside of this field.</b>" // library marker BPTWorld.bpt-normalStuff, line 36
        if(dataName) createDataChildDevice(driverName) // library marker BPTWorld.bpt-normalStuff, line 37
        if(statusMessageD == null) statusMessageD = "Waiting on status message..." // library marker BPTWorld.bpt-normalStuff, line 38
        paragraph "${statusMessageD}" // library marker BPTWorld.bpt-normalStuff, line 39
    } // library marker BPTWorld.bpt-normalStuff, line 40
    input "dataDevice", "capability.actuator", title: "Virtual Device specified above", required:true, multiple:false // library marker BPTWorld.bpt-normalStuff, line 41
    if(!useExistingDevice) { // library marker BPTWorld.bpt-normalStuff, line 42
        app.removeSetting("dataName") // library marker BPTWorld.bpt-normalStuff, line 43
        paragraph "<small>* Device must use the '${driverName}'.</small>" // library marker BPTWorld.bpt-normalStuff, line 44
    } // library marker BPTWorld.bpt-normalStuff, line 45
} // library marker BPTWorld.bpt-normalStuff, line 46

def createDataChildDevice(driverName) {     // library marker BPTWorld.bpt-normalStuff, line 48
    if(logEnable) log.debug "In createDataChildDevice (${state.version})" // library marker BPTWorld.bpt-normalStuff, line 49
    statusMessageD = "" // library marker BPTWorld.bpt-normalStuff, line 50
    if(!getChildDevice(dataName)) { // library marker BPTWorld.bpt-normalStuff, line 51
        if(logEnable) log.debug "In createDataChildDevice - Child device not found - Creating device: ${dataName}" // library marker BPTWorld.bpt-normalStuff, line 52
        try { // library marker BPTWorld.bpt-normalStuff, line 53
            addChildDevice("BPTWorld", driverName, dataName, 1234, ["name": "${dataName}", isComponent: false]) // library marker BPTWorld.bpt-normalStuff, line 54
            if(logEnable) log.debug "In createDataChildDevice - Child device has been created! (${dataName})" // library marker BPTWorld.bpt-normalStuff, line 55
            statusMessageD = "<b>Device has been been created. (${dataName})</b>" // library marker BPTWorld.bpt-normalStuff, line 56
        } catch (e) { if(logEnable) log.debug "Unable to create device - ${e}" } // library marker BPTWorld.bpt-normalStuff, line 57
    } else { // library marker BPTWorld.bpt-normalStuff, line 58
        statusMessageD = "<b>Device Name (${dataName}) already exists.</b>" // library marker BPTWorld.bpt-normalStuff, line 59
    } // library marker BPTWorld.bpt-normalStuff, line 60
    return statusMessageD // library marker BPTWorld.bpt-normalStuff, line 61
} // library marker BPTWorld.bpt-normalStuff, line 62

def uninstalled() { // library marker BPTWorld.bpt-normalStuff, line 64
	removeChildDevices(getChildDevices()) // library marker BPTWorld.bpt-normalStuff, line 65
} // library marker BPTWorld.bpt-normalStuff, line 66

private removeChildDevices(delete) { // library marker BPTWorld.bpt-normalStuff, line 68
	delete.each {deleteChildDevice(it.deviceNetworkId)} // library marker BPTWorld.bpt-normalStuff, line 69
} // library marker BPTWorld.bpt-normalStuff, line 70

def letsTalk(msg) { // library marker BPTWorld.bpt-normalStuff, line 72
    if(logEnable) log.debug "In letsTalk (${state.version}) - Sending the message to Follow Me - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 73
    if(useSpeech && fmSpeaker) { // library marker BPTWorld.bpt-normalStuff, line 74
        fmSpeaker.latestMessageFrom(state.name) // library marker BPTWorld.bpt-normalStuff, line 75
        fmSpeaker.speak(msg,null) // library marker BPTWorld.bpt-normalStuff, line 76
    } // library marker BPTWorld.bpt-normalStuff, line 77
} // library marker BPTWorld.bpt-normalStuff, line 78

def pushHandler(msg){ // library marker BPTWorld.bpt-normalStuff, line 80
    if(logEnable) log.debug "In pushNow (${state.version}) - Sending a push - msg: ${msg}" // library marker BPTWorld.bpt-normalStuff, line 81
    theMessage = "${app.label} - ${msg}" // library marker BPTWorld.bpt-normalStuff, line 82
    if(logEnable) log.debug "In pushNow - Sending message: ${theMessage}" // library marker BPTWorld.bpt-normalStuff, line 83
    sendPushMessage.deviceNotification(theMessage) // library marker BPTWorld.bpt-normalStuff, line 84
} // library marker BPTWorld.bpt-normalStuff, line 85

// ********** Normal Stuff ********** // library marker BPTWorld.bpt-normalStuff, line 87
def logsOff() { // library marker BPTWorld.bpt-normalStuff, line 88
    log.info "${app.label} - Debug logging auto disabled" // library marker BPTWorld.bpt-normalStuff, line 89
    app.updateSetting("logEnable",[value:"false",type:"bool"]) // library marker BPTWorld.bpt-normalStuff, line 90
} // library marker BPTWorld.bpt-normalStuff, line 91

def checkEnableHandler() { // library marker BPTWorld.bpt-normalStuff, line 93
    state.eSwitch = false // library marker BPTWorld.bpt-normalStuff, line 94
    if(disableSwitch) {  // library marker BPTWorld.bpt-normalStuff, line 95
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}" // library marker BPTWorld.bpt-normalStuff, line 96
        disableSwitch.each { it -> // library marker BPTWorld.bpt-normalStuff, line 97
            theStatus = it.currentValue("switch") // library marker BPTWorld.bpt-normalStuff, line 98
            if(theStatus == "on") { state.eSwitch = true } // library marker BPTWorld.bpt-normalStuff, line 99
        } // library marker BPTWorld.bpt-normalStuff, line 100
        if(logEnable) log.debug "In checkEnableHandler - eSwitch: ${state.eSwitch}" // library marker BPTWorld.bpt-normalStuff, line 101
    } // library marker BPTWorld.bpt-normalStuff, line 102
} // library marker BPTWorld.bpt-normalStuff, line 103

def getImage(type) {					// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 105
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/" // library marker BPTWorld.bpt-normalStuff, line 106
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>" // library marker BPTWorld.bpt-normalStuff, line 107
    if(type == "checkMarkGreen") return "${loc}checkMarkGreen2.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 108
    if(type == "optionsGreen") return "${loc}options-green.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 109
    if(type == "optionsRed") return "${loc}options-red.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 110
    if(type == "instructions") return "${loc}instructions.png height=30 width=30>" // library marker BPTWorld.bpt-normalStuff, line 111
    if(type == "logo") return "${loc}logo.png height=60>" // library marker BPTWorld.bpt-normalStuff, line 112
} // library marker BPTWorld.bpt-normalStuff, line 113

def getFormat(type, myText="") {			// Modified from @Stephack Code // library marker BPTWorld.bpt-normalStuff, line 115
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>" // library marker BPTWorld.bpt-normalStuff, line 116
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>" // library marker BPTWorld.bpt-normalStuff, line 117
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>" // library marker BPTWorld.bpt-normalStuff, line 118
} // library marker BPTWorld.bpt-normalStuff, line 119

def display(data) { // library marker BPTWorld.bpt-normalStuff, line 121
    if(data == null) data = "" // library marker BPTWorld.bpt-normalStuff, line 122
    setVersion() // library marker BPTWorld.bpt-normalStuff, line 123
    getHeaderAndFooter() // library marker BPTWorld.bpt-normalStuff, line 124
    if(app.label) { // library marker BPTWorld.bpt-normalStuff, line 125
        if(app.label.contains("(Paused)")) { // library marker BPTWorld.bpt-normalStuff, line 126
            theName = app.label - " <span style='color:red'>(Paused)</span>" // library marker BPTWorld.bpt-normalStuff, line 127
        } else { // library marker BPTWorld.bpt-normalStuff, line 128
            theName = app.label // library marker BPTWorld.bpt-normalStuff, line 129
        } // library marker BPTWorld.bpt-normalStuff, line 130
    } // library marker BPTWorld.bpt-normalStuff, line 131
    if(theName == null || theName == "") theName = "New Child App" // library marker BPTWorld.bpt-normalStuff, line 132
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) { // library marker BPTWorld.bpt-normalStuff, line 133
        paragraph "${state.headerMessage}" // library marker BPTWorld.bpt-normalStuff, line 134
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 135
        input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true // library marker BPTWorld.bpt-normalStuff, line 136
    } // library marker BPTWorld.bpt-normalStuff, line 137
} // library marker BPTWorld.bpt-normalStuff, line 138

def display2() { // library marker BPTWorld.bpt-normalStuff, line 140
    section() { // library marker BPTWorld.bpt-normalStuff, line 141
        if(state.appType == "parent") { href "removePage", title:"${getImage("optionsRed")} <b>Remove App and all child apps</b>", description:"" } // library marker BPTWorld.bpt-normalStuff, line 142
        paragraph getFormat("line") // library marker BPTWorld.bpt-normalStuff, line 143
        paragraph "<div style='color:#1A77C9;text-align:center;font-size:20px;font-weight:bold'>${state.name} - ${state.version}</div>" // library marker BPTWorld.bpt-normalStuff, line 144
        paragraph "${state.footerMessage}" // library marker BPTWorld.bpt-normalStuff, line 145
    } // library marker BPTWorld.bpt-normalStuff, line 146
} // library marker BPTWorld.bpt-normalStuff, line 147

def getHeaderAndFooter() { // library marker BPTWorld.bpt-normalStuff, line 149
    timeSinceNewHeaders() // library marker BPTWorld.bpt-normalStuff, line 150
    if(state.checkNow == null) state.checkNow = true // library marker BPTWorld.bpt-normalStuff, line 151
    if(state.totalHours > 6 || state.checkNow) { // library marker BPTWorld.bpt-normalStuff, line 152
        def params = [ // library marker BPTWorld.bpt-normalStuff, line 153
            uri: "https://raw.githubusercontent.com/bptworld/Hubitat/master/info.json", // library marker BPTWorld.bpt-normalStuff, line 154
            requestContentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 155
            contentType: "application/json", // library marker BPTWorld.bpt-normalStuff, line 156
            timeout: 10 // library marker BPTWorld.bpt-normalStuff, line 157
        ] // library marker BPTWorld.bpt-normalStuff, line 158
        try { // library marker BPTWorld.bpt-normalStuff, line 159
            def result = null // library marker BPTWorld.bpt-normalStuff, line 160
            httpGet(params) { resp -> // library marker BPTWorld.bpt-normalStuff, line 161
                state.headerMessage = resp.data.headerMessage // library marker BPTWorld.bpt-normalStuff, line 162
                state.footerMessage = resp.data.footerMessage // library marker BPTWorld.bpt-normalStuff, line 163
            } // library marker BPTWorld.bpt-normalStuff, line 164
        } catch (e) { } // library marker BPTWorld.bpt-normalStuff, line 165
    } // library marker BPTWorld.bpt-normalStuff, line 166
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>" // library marker BPTWorld.bpt-normalStuff, line 167
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>" // library marker BPTWorld.bpt-normalStuff, line 168
} // library marker BPTWorld.bpt-normalStuff, line 169

def timeSinceNewHeaders() {  // library marker BPTWorld.bpt-normalStuff, line 171
    if(state.previous == null) {  // library marker BPTWorld.bpt-normalStuff, line 172
        prev = new Date() // library marker BPTWorld.bpt-normalStuff, line 173
    } else { // library marker BPTWorld.bpt-normalStuff, line 174
        try { // library marker BPTWorld.bpt-normalStuff, line 175
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ") // library marker BPTWorld.bpt-normalStuff, line 176
            prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000")) // library marker BPTWorld.bpt-normalStuff, line 177
        } catch(e) { // library marker BPTWorld.bpt-normalStuff, line 178
            prev = state.previous // library marker BPTWorld.bpt-normalStuff, line 179
        } // library marker BPTWorld.bpt-normalStuff, line 180
    } // library marker BPTWorld.bpt-normalStuff, line 181
    def now = new Date() // library marker BPTWorld.bpt-normalStuff, line 182
    use(TimeCategory) { // library marker BPTWorld.bpt-normalStuff, line 183
        state.dur = now - prev // library marker BPTWorld.bpt-normalStuff, line 184
        state.days = state.dur.days // library marker BPTWorld.bpt-normalStuff, line 185
        state.hours = state.dur.hours // library marker BPTWorld.bpt-normalStuff, line 186
        state.totalHours = (state.days * 24) + state.hours // library marker BPTWorld.bpt-normalStuff, line 187
    } // library marker BPTWorld.bpt-normalStuff, line 188
    state.previous = now // library marker BPTWorld.bpt-normalStuff, line 189
} // library marker BPTWorld.bpt-normalStuff, line 190

// ~~~~~ end include (2) BPTWorld.bpt-normalStuff ~~~~~
