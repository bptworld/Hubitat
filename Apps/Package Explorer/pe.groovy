/**
 *  **************** Package Explorer App ****************
 *
 *  Design Usage:
 *  Conveniently explore the apps and drivers available within the Hubitat Package Manager.
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
 *  Special thanks to @dman2306 for his amazing work on Hubitat Package Manager.
 *  All code by dman2306 used with permission. Thank you.
 *
 *  Changes:
 *
 *  1.0.6 - 06/03/20 - Added a Master List Option
 *  1.0.5 - 05/23/20 - Added What's New Option
 *  1.0.4 - 05/20/20 - Fix for HE update
 *  1.0.3 - 05/16/20 - Error trapping
 *  1.0.2 - 05/07/20 - Added 'Developer Options' Search, cosmetic changes
 *  1.0.1 - 05/02/20 - Minor changes
 *  1.0.0 - 05/01/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import groovy.transform.Field
@Field static String repositoryListing = "https://raw.githubusercontent.com/dcmeglio/hubitat-packagerepositories/master/repositories.json"
@Field static List categories = [] 
@Field static List allPackages = []
@Field static def listOfRepositories = [:]
    
def setVersion(){
    state.name = "Package Explorer"
	state.version = "1.0.6"
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
    page(name: "categoryOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "developerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "whatsNewOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
    page(name: "masterListOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    if(logEnable) log.debug "In pageConfig - Starting Up ***************************************"    
    
    dynamicPage(name: "", title: "", install: true, uninstall: true) {
        installCheck()
		if(state.appInstalled == 'COMPLETE'){
            performRepositoryRefresh()
            updateRepositoryListing()
            reposToShowHandler()
            section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
                paragraph "<b>Notes:</b>"
                paragraph "Conveniently explore the apps and drivers available within the Hubitat Package Manager.<br>All credit goes to @dman2306 for his amazing work on HPM."
            }
            section(getFormat("header-green", "${getImage("Blank")}"+" HPM Information")) {
                paragraph "<b>As more and more packages come online, it will take a bit longer for this app to sort through all the great packages. Please be patient after making your selection.</b>"
                paragraph "Hubitat Package Manager is now serving <b>${state.packCount} packages</b> (apps/drivers) from <b>${state.repoCount} developers</b>!"
            }
            
            section(getFormat("header-green", "${getImage("Blank")}"+" Search Options")) {
                app.removeSetting("pkgCategory")
                app.removeSetting("pkgSearch")
                app.removeSetting("pkgSearchNOT")
                app.removeSetting("pkgSearchType")
                app.removeSetting("pkgAuthor")
                
                input "installedRepositories", "enum", title: "Available Repositories", options:state.reposToShow, multiple:true, required:true, submitOnChange:true     
                //app.updateSetting("installedRepositories", state.reposToShow)

                href "categoryOptions", title:"Category Options", description:"See all Apps and Drivers the selected Category has to offer"
                href "developerOptions", title:"Developer Options", description:"See all Apps and Drivers the selected Developer has to offer"
                href "searchOptions", title:"Keyword Options", description:"Use Keywords to search for an App or Driver across all Developers and Categories"
                href "masterListOptions", title:"Master List Options", description:"Creates an alphabetical list of ALL packages. <i><b>This will take a minute to run! Please be patient.</b></i>"
                href "whatsNewOptions", title:"What's New Options", description:"See 'What's New' within the past 7 days. <i><b>This will take a minute to run! Please be patient.</b></i>"
            }

            section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
                label title: "Enter a name for this child app", required: false, submitOnChange: true
                input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
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
		
        findPakcagesByDateAdded()

        section(getFormat("header-green", "${getImage("Blank")}"+" ${state.resultsTitle}")) {
            paragraph appsList

        }
    }
}

def findPackagesByCategory() {
    if(logEnable) log.debug "In findPackagesByCategory (${state.version})"   
    if(!sortBy) allPackages = state.allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = state.allPackages.sort{ name-> name.author}    
    state.packageCount = allPackages.size()
                
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
                
            if(!sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'><b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}</div>"
                        
            if(sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.author}) - <b>${pkg.name}</b><br>${theLinks}<br>${pkg.description}</div>"
            theLinks = ""            
        }
    }
}
    
def findPackagesByDeveloper() {
    if(logEnable) log.debug "In findPackagesByDeveloper (${state.version})"
    if(!sortBy) allPackages = state.allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = state.allPackages.sort{ name-> name.category}
    
    //allPackages = state.allPackages.sort{ name-> name.author}    
    state.packageCount = allPackages.size()
            
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
                
                appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - <b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}</div>"
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

                    if(!sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - <b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}</div>"

                    if(sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - (${pkg.author}) - <b>${pkg.name}</b><br>${theLinks}<br>${pkg.description}</div>"
                    theLinks = "" 
                }
            }
        }
    }
}

def findAllPackages() {
    if(logEnable) log.debug "In findAllPackages (${state.version})"
    allPackages = state.allPackages.sort{ name-> name.name}   
    state.packageCount = allPackages.size()
            
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

            appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.category}) - <b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}</div>"
        }
    }
}

def findPakcagesByDateAdded() {
    if(logEnable) log.debug "In findPakcagesByDateAdded (${state.version})"   
    allPackages = state.allPackages.sort{ name-> name.name}
    state.packageCount = allPackages.size()
                
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
        }
        catch (a) {
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

            if(theDays <= 7) {
                def mapInfo = [
                    cat: pkg.category,
                    name: pkg.name,
                    author: pkg.author,
                    theDays: theDays,
                    theLinks: theLinks,
                    description: pkg.description
                ]
                allDays7 << mapInfo      
            }
        }
    }
    sortedByDays = allDays7.sort{ stuff -> stuff.theDays }

    sortedByDays.each { data -> 
        if(data.theDays == 1) {
            appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>${data.theDays} day - <b>${data.name}</b> - ${data.cat} - ${data.author}<br>${data.theLinks}<br>${data.description}</div>"
        } else {
            appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>${data.theDays} days - <b>${data.name}</b> - ${data.cat} - ${data.author}<br>${data.theLinks}<br>${data.description}</div>"
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
}

def getRepoName(location) {        // Code by dman2306
	return listOfRepositories.repositories.find { it -> it.location == location }?.name
}

def performRepositoryRefresh() {        // Code by dman2306, mods by bptworld
    if(logEnable) log.debug "In performRepositoryRefresh (${state.version})"
	allPackages = []
	categories = []
    allDevNames = []
    tempList = ""
    state.packCount = 0

	for (repo in installedRepositories) {
		def repoName = getRepoName(repo)
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
				category: pkg.category
			]
			allPackages << pkgDetails
            if(!tempList.contains("${pkgDetails.author}")) {
                tempList += "${pkgDetails.author}, "
                allDevNames << pkgDetails.author
            }
			if (!categories.contains(pkgDetails.category))
				categories << pkgDetails.category
		}
	}
    state.allPackages = allPackages.sort()
	state.categories = categories.sort()
    state.allDevNames = allDevNames.sort()    
    state.packCount = allPackages.size()
}

def getJSONFile(uri) {        // Code by dman2306
    //if(logEnable) log.debug "In getJSONFile"
	try {
		def fileContents = downloadFile(uri)
		return new groovy.json.JsonSlurper().parseText(fileContents)
	}
	catch (e) {
		return null
	}	
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	initialize()
}

def initialize() {
    setDefaults()
}

def downloadFile(file) {        // Code by dman2306
    //if(logEnable) log.debug "In downloadFile"
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
	}
	catch (e) {
		log.error "In downloadFile - Error downloading ${file}: ${e}"
		return null
	}
}

def updateRepositoryListing() {        // Code by dman2306
	if(logEnable) log.debug "In updateRepositoryListing - Refreshing repository list (${state.version})"
	def oldListOfRepositories = listOfRepositories
	listOfRepositories = getJSONFile(repositoryListing)
	if (installedRepositories == null) {
		def repos = [] as List
		listOfRepositories.repositories.each { it -> repos << it.location }
		app.updateSetting("installedRepositories", repos)
	} else {
		for (newRepo in listOfRepositories.repositories) {
			if (oldListOfRepositories.size() > 0 && !oldListOfRepositories.repositories.find { it -> it.location == newRepo.location} && !installedRepositories.contains(newRepo.location)) {
				if(logEnable) log.debug "Found new repository ${newRepo.location}"
				installedRepositories << newRepo.location
			}
		}
		app.updateSetting("installedRepositories", installedRepositories)
	}
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

// ********** Normal Stuff **********

def setDefaults() {
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

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    section (getFormat("title", "${getImage("logo")}" + " ${state.name}")) {
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
    //if(logEnable) log.debug "In getHeaderAndFooter (${state.version})"
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
        //if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        //if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
