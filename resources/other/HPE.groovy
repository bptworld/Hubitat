/**
 *  **************** Hubitat Package Explorer App ****************
 *
 *  NOT for download or use at this time!
 *
 *  Design Usage:
 *  Conveniently browse the apps and drivers available with the Hubitat Package Manager.
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
 *
 *  Changes:
 *
 *  1.0.0 - 01/01/20 - Initial release.
 *
 */

import groovy.transform.Field
@Field static String repositoryListing = "https://raw.githubusercontent.com/dcmeglio/hubitat-packagerepositories/master/repositories.json"
@Field static List categories = [] 
@Field static List allPackages = []
@Field static groovy.json.internal.LazyMap listOfRepositories = [:]
@Field static groovy.json.internal.LazyMap completedActions = [:]

def setVersion(){
    state.name = "Hubitat Package Explorer"
	state.version = "1.0.0"
}

definition(
    name: "Hubitat Package Explorer",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Conveniently browse the apps and drivers available with the Hubitat Package Manager.",
    category: "Convenience",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
    page(name: "triggerOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig")
}

def pageConfig() {
    if(logEnable) log.debug "In pageConfig - Starting Up ***************************************"    
	updateRepositoryListing()

    dynamicPage(name: "", title: "", install: true, uninstall: true) {
		display() 
        section("${getImage('instructions')} <b>Instructions:</b>", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Conveniently explore the apps and drivers available with the Hubitat Package Manager.<br>All credit goes to @dman2306 for his amazing work on HPM."
		}
             
        section(getFormat("header-green", "${getImage("Blank")}"+" Select Options")) {          
            // Start Code by dman2306
            def reposToShow = [:]
			listOfRepositories.repositories.each { r -> reposToShow << ["${r.location}":r.name] }
			if (state.customRepositories != null)
				state.customRepositories.each { r -> reposToShow << ["${r.key}":r.value] }
			reposToShow = reposToShow.sort { r -> r.value }
            
			input "installedRepositories", "enum", title: "Available Repositories", options:reposToShow, multiple:true, required:true, submitOnChange:true
			if(installedRepositories) performRepositoryRefresh()
				                   
            input "pkgCategory", "enum", title: "Choose a Category To Explore", options:categories, required:false, submitOnChange:true
            // End Code by dman2306
            
            paragraph "<b> - OR - </b>" 
            input "pkgSearch", "text", title: "Search ALL Apps", required:false, submitOnChange:true
            if(pkgSearch) { app.removeSetting("pkgCategory") }  // wipe out pkgCategory if search is used
			
			if(pkgCategory || pkgSearch) {
				input "sortBy", "bool", title: "Sort packages by Author?", description: "Sorting", defaultValue:false, submitOnChange:true
                if(pkgCategory) findPackagesByCategory()
                if(pkgSearch) findPackagesBySearch()
				paragraph "<hr>"
                paragraph appsList
			}
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Maintenance")) {
            label title: "Enter a name for this child app", required: false, submitOnChange: true
            input "logEnable","bool", title: "Enable Debug Logging", description: "Debugging", defaultValue: false, submitOnChange: true
		}
		display2()
	}
}

def findPackagesByCategory() {
    if(!sortBy) allPackages = allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = allPackages.sort{ name-> name.author}
                
    appsList = "<b>Apps By Category</b> (${pkgCategory})<br>"
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
            //log.warn info
                        
            if(!sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'><b>${pkg.name}</b> - (${pkg.author})<br>${theLinks}<br>${pkg.description}</div>"
                        
            if(sortBy) appsList += "<div style='background-color: white;width: 90%;border: 1px solid grey;border-radius: 5px;box-shadow: 3px 3px;padding: 20px;margin: 20px;'>(${pkg.author}) - <b>${pkg.name}</b><br>${theLinks}<br>${pkg.description}</div>"
            theLinks = ""            
        }
    }
}
             
def findPackagesBySearch() {
    if(logEnable) log.debug "In findPackagesBySearch"
    if(!sortBy) allPackages = allPackages.sort{ name-> name.name}
    if(sortBy) allPackages = allPackages.sort{ name-> name.author}
                
    appsList = "<b>Apps By Search</b> (${pkgSearch})<br>"
    
	for(pkg in allPackages) {
        if(logEnable) log.debug "In findPackageBySearch - name: ${pkg.name.toLowerCase()} - search: ${pkgSearch.toLowerCase()}"
        if(pkg.name.toLowerCase().contains("${pkgSearch.toLowerCase()}")) {
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

def getRepoName(location) {        // Code by dman2306
	return listOfRepositories.repositories.find { it -> it.location == location }?.name
}

def performRepositoryRefresh() {        // Code by dman2306
	allPackages = []
	categories = []

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
			if (!categories.contains(pkgDetails.category))
				categories << pkgDetails.category
		}
	}
	allPackages = allPackages.sort()
	categories = categories.sort()
}

def getJSONFile(uri) {        // Code by dman2306
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
	if(logEnable) log.debug "Refreshing repository list"
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
        if(logEnable) log.debug "In getHeaderAndFooter - headerMessage: ${state.headerMessage}"
        if(logEnable) log.debug "In getHeaderAndFooter - footerMessage: ${state.footerMessage}"
    }
    catch (e) {
        state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
        state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Find more apps on my Github, just click here!</a><br><a href='https://paypal.me/bptworld' target='_blank'>Paypal</a></div>"
    }
}
