/**
 * ****************  Schedule Switch ****************
 *
 *  Design Usage:
 *	This was designed to schedule a switch on/off some months ahead
 *
 *
 *  Copyright 2018 Andrew Parker
 *  
 *  This App is free!
 *  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://www.paypal.me/smartcobra
 *  
 *
 *  I'm very happy for you to use this app without a donation, but if you find it useful then it would be nice to get a 'shout out' on the forum! -  @Cobra
 *  Have an idea to make this app better?  - Please let me know :)
 *
 *  
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
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @Cobra
 *
 *-------------------------------------------------------------------------------------------------------------------
 *
 *  Last Update: 31/07/2018
 *
 *  Changes:
 *
 * 
 *         - @bptworld - Modified to have up to 5 days scheduled in a month within one child. Also renamed Date field to Day.
 *  V1.0.1 - Code cleanup & revised version checking - Debug - March was not working correctly when selected
 *  V1.0.0 - POC 
 */
 
 
 
 
definition(
    name: "Schedule Switch Child",
    namespace: "Cobra",
    author: "Andrew Parker",
    description: "Schedule a switch on a certain date & time",
    category: "Convenience",
        
    parent: "Cobra:Scheduled Switch",
    
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
    )


preferences {

section ("") {

  paragraph title: "Schedule Switch Child", "Schedule a switch on a certain date & time"
 
 }
 display()
	 section(){
		input "switch1",  "capability.switch",  title: "Switch to Schedule", multiple: false, required: true
	  
		input "month1", "enum", title: "Select Month", required: true, options: [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
	
		input "date1", "enum", title: "Select Day 1", required: true, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
		input "date2", "enum", title: "Select Day 2", required: false, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
		input "date3", "enum", title: "Select Day 3", required: false, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
		input "date4", "enum", title: "Select Day 4", required: false, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
		input "date5", "enum", title: "Select Day 5", required: false, options: [ "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"]
		
		input "hour1", "enum", title: "Select Hour", required: true,  options: [ "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23"]
		
		input "min1", "enum", title: "Select Minute", required: true, options: [ "0", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"]
	
        input "mode1", "bool", title: "Turn Switch On or Off", required: true, submitOnChange: true, defaultValue: false    
	    }
    
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unschedule()
	unsubscribe()
	initialize()
}

def initialize() {
    version()
    state.switchMode == true
	subscribe(switch1, "switch", switchHandler1)
    calculateCron()
   
}



def calculateCron(){

state.selectedMonth = month1    
    if(state.selectedMonth == "Jan"){state.runMonth = "1"}
    if(state.selectedMonth == "Feb"){state.runMonth = "2"}
    if(state.selectedMonth == "Mar"){state.runMonth = "3"}
    if(state.selectedMonth == "Apr"){state.runMonth = "4"}
    if(state.selectedMonth == "May"){state.runMonth = "5"}
    if(state.selectedMonth == "Jun"){state.runMonth = "6"}
    if(state.selectedMonth == "Jul"){state.runMonth = "7"}
    if(state.selectedMonth == "Aug"){state.runMonth = "8"}
    if(state.selectedMonth == "Sep"){state.runMonth = "9"}
    if(state.selectedMonth == "Oct"){state.runMonth = "10"}
    if(state.selectedMonth == "Nov"){state.runMonth = "11"}
    if(state.selectedMonth == "Dec"){state.runMonth = "12"}
 
state.selectedDate1 = date1
state.selectedDate2 = date2
state.selectedDate3 = date3
state.selectedDate4 = date4
state.selectedDate5 = date5
state.selectedHour = hour1
state.selectedMin = min1
    
state.schedule1 = "0 ${state.selectedMin} ${state.selectedHour} ${state.selectedDate1} ${state.runMonth} ? *"
    
    schedule(state.schedule1, switchNow1) 
    
    if(state.selectedDate2 != null) {
        state.schedule2 = "0 ${state.selectedMin} ${state.selectedHour} ${state.selectedDate2} ${state.runMonth} ? *"
    
    	schedule(state.schedule2, switchNow2)
        
    }
    
    if(state.selectedDate3 != null) {
        state.schedule3 = "0 ${state.selectedMin} ${state.selectedHour} ${state.selectedDate3} ${state.runMonth} ? *"
    
    	schedule(state.schedule3, switchNow3)
    
    }
    
    if(state.selectedDate4 != null) {
        state.schedule4 = "0 ${state.selectedMin} ${state.selectedHour} ${state.selectedDate4} ${state.runMonth} ? *"
    
    	schedule(state.schedule4, switchNow4)
    
    }
    
    if(state.selectedDate5 != null) {
        state.schedule5 = "0 ${state.selectedMin} ${state.selectedHour} ${state.selectedDate5} ${state.runMonth} ? *"
    
    	schedule(state.schedule5, switchNow5)

    }
    
}

def switchNow1(){
state.switchMode = mode1
    
    if(state.switchMode == true){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate1 so switching on: $switch1"
        switch1.on()
    } 
        if(state.switchMode == false){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate1 so switching off: $switch1"
        switch1.off()
    } 
    
}

def switchNow2(){
state.switchMode = mode1
    
    if(state.switchMode == true){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate2 so switching on: $switch1"
        switch1.on()
    } 
        if(state.switchMode == false){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate2 so switching off: $switch1"
        switch1.off()
    } 
    
}

def switchNow3(){
state.switchMode = mode1
    
    if(state.switchMode == true){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate3 so switching on: $switch1"
        switch1.on()
    } 
        if(state.switchMode == false){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate3 so switching off: $switch1"
        switch1.off()
    } 
    
}

def switchNow4(){
state.switchMode = mode1
    
    if(state.switchMode == true){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate4 so switching on: $switch1"
        switch1.on()
    } 
        if(state.switchMode == false){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate4 so switching off: $switch1"
        switch1.off()
    } 
    
}

def switchNow5(){
state.switchMode = mode1
    
    if(state.switchMode == true){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate5 so switching on: $switch1"
        switch1.on()
    } 
        if(state.switchMode == false){
        log.info "It's $state.selectedHour:$state.selectedMin on $state.selectedMonth $state.selectedDate5 so switching off: $switch1"
        switch1.off()
    } 
    
}

def switchHandler1 (evt) {
def switching = evt.value
    if(switching == "on"){
        log.info "Switch is turned on"
    }
        
    if(switching == "off"){
        log.info "Switch is turned off"
    }    
        
        
        
}


def version(){
    updatecheck()
    if (state.Type == "Application"){schedule("0 0 9 ? * FRI *", updatecheck)}
    if (state.Type == "Driver"){schedule("0 0 8 ? * FRI *", updatecheck)}
}

def display(){
    section{paragraph "Version: $state.version -  $state.Copyright"}
	if(state.Status != "Current"){
       section{ 
       paragraph "$state.Status"
       paragraph "$state.updateInfo"
    }
    }
}


def updatecheck(){
    setAppVersion()
    def paramsUD = [uri: "http://update.hubitat.uk/cobra.json"]
       try {
        httpGet(paramsUD) { respUD ->
//  log.info " Version Checking - Response Data: ${respUD.data}"   // Debug Code 
       def copyNow = (respUD.data.copyright)
       state.Copyright = copyNow
            def newver = (respUD.data.versions.(state.Type).(state.InternalName))
            def cobraVer = (respUD.data.versions.(state.Type).(state.InternalName).replace(".", ""))
       def cobraOld = state.version.replace(".", "")
       state.updateInfo = (respUD.data.versions.UpdateInfo.(state.Type).(state.InternalName)) 
            if(cobraVer == "NLS"){
            state.Status = "<b>** This $state.Type is no longer supported by Cobra  **</b>"       
            log.warn "** This $state.Type is no longer supported by Cobra **"      
      }           
      		else if(cobraOld < cobraVer){
        	state.Status = "<b>New Version Available (Version: $newver)</b>"
        	log.warn "** There is a newer version of this $state.Type available  (Version: $newver) **"
        	log.warn "** $state.updateInfo **"
       } 
            else{ 
      		state.Status = "Current"
      		log.info "$state.Type is the current version"
       }
       
       }
        } 
        catch (e) {
        log.error "Something went wrong: $e"
    }
}        

def setAppVersion(){
     state.version = "1.0.1"
     state.InternalName = "SchedSwitchchild"
     state.Type = "Application"
 //  state.Type = "Driver"

}
 
