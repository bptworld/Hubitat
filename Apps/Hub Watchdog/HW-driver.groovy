/**
 *  ****************  Hub Watchdog Driver  ****************
 *
 *  Design Usage:
 *  This driver formats data to be displayed on Hubitat's Dashboards.
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to mention it on the Hubitat forums!  Thanks.
 *
 *  Remember...I am not a programmer, everything I do takes a lot of time and research (then MORE research)!
 *  Donations are never necessary but always appreciated.  Donations to support development efforts are accepted via: 
 *
 *  Paypal at: https://paypal.me/bptworld
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
 *  1.1.1 - 09/01/20 - Now only holds 40 max data points, Changed short names.
 *  1.1.0 - 05/01/20 - Removed some old code
 *  1.0.9 - 04/30/20 - Fixed a bug
 *  1.0.8 - 09/30/19 - Lots of little changes
 *  1.0.7 - 09/29/19 - Added support for 'Examiner' child app
 *  1.0.6 - 09/28/19 - Fixed the '60' error.
 *  1.0.5 - 09/26/19 - More color choices, rounded Med to 3
 *  1.0.4 - 09/26/19 - Holds up to 80 data points, added color coding
 *  1.0.3 - 09/25/19 - More tweaks
 *  1.0.2 - 09/25/19 - Attempt to fix a null object error
 *  1.0.1 - 09/25/19 - Added a lot of data points
 *  1.0.0 - 09/24/19 - Initial release
 */

metadata {
	definition (name: "Hub Watchdog Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Hub%20Watchdog/HW-driver.groovy") {
   		capability "Initialize"
		capability "Actuator"
        capability "Switch"
		
        attribute "readings1", "string"
    	attribute "dataPoints1", "string"
        attribute "dataPoints2", "string"

        attribute "lastDataPoint1", "string"
        attribute "numOfCharacters1", "string"
        attribute "numOfCharacters2", "string"
        
        attribute "maxDelay", "number"
        attribute "warnValue", "number"
        attribute "readingsSize1", "number"
        attribute "listSizeB", "number"
        attribute "listSizeW", "number"
        attribute "meanD", "number"
        attribute "midNumberD", "number"
        attribute "medianD", "number"
        attribute "minimumD", "number"
        attribute "maximumD", "number"
        attribute "list1", "string"
        attribute "theListSize", "number"
        attribute "lastUpdated", "string"
        
        command "dataPoint1"
        command "on"
        command "off"
        command "clearData1"
        command "maxDelay"
        command "warnValue"
	}
	preferences() {    	
        section(){
			input("fontSize", "text", title: "Font Size", required: true, defaultValue: "12")
			input("hourType", "bool", title: "Time Selection (Off for 24h, On for 12h)", required: false, defaultValue: false)
            input("warnColor", "text", title: "Change color for 'Close to Threshold' readings", required: true, defaultValue: "orange")
            input("overColor", "text", title: "Change color for 'Over Threshold' readings", required: true, defaultValue: "red")
            input("uniqueID", "text", title: "Unique Identifier (Vt, Zw, Zb or O)", required: true)
			input("logEnable", "bool", title: "Enable logging", required: false, defaultValue: false)
        }
    }
}

def dataPoint1(message) {
    if(logEnable) log.trace "In dataPoint1 - Received: ${message}"
    theMessage = message
    sendEvent(name: "lastDataPoint1", value: theMessage)
    makeList(theMessage)
}

def maxDelay(message) {
    if(logEnable) log.trace "In maxDelay - Received: ${message}"
    if(message) maxDelay = message.toFloat()
    sendEvent(name: "maxDelay", value: maxDelay)
}

def warnValue(message) {
    if(logEnable) log.trace "In warnValue - Received: ${message}"
    if(message) warnValue = message.toFloat()
    sendEvent(name: "warnValue", value: warnValue)
}

def makeList(theMessage) {
    if(logEnable) log.trace "In makeList - working on ${theMessage}"
    def maxDelay = device.currentValue('maxDelay')
    def warnValue = device.currentValue('warnValue')
    
        try {
            if(state.readings1 == null) state.readings1 = []
            state.readings1.add(0,theMessage) 
            
            if(state.readings1) readingsSize1 = state.readings1.size()
            if(readingsSize1 > 40) state.readings1.removeAt(40)
            
            getDateTime()
            
            if(theMessage >= maxDelay) {
                nMessage1 = "${newdate} - <span style='color: ${overColor}'>${theMessage}</span> ${uniqueID}"
                if(state.listSizeB == null) state.listSizeB = 0
                state.listSizeB = state.listSizeB + 1
            } else if(theMessage >= warnValue) {
                nMessage1 = "${newdate} - <span style='color: ${warnColor}'>${theMessage}</span> ${uniqueID}"
                if(state.listSizeW == null) state.listSizeW = 0
                state.listSizeW = state.listSizeW + 1
            } else {
                nMessage1 = "${newdate} - ${theMessage} ${uniqueID}"
            }
            if(state.list1 == null) state.list1 = []          
            state.list1.add(0,nMessage1)

            if(state.list1) {
                int listSize1 = state.list1.size()
                keepListUnder()
            } else {
                int listSize1 = 0
            }

            int listSizeLines = state.list1.size()
            String result1 = state.list1.join(";")
            def lines1 = result1.split(";")
            
            theData1 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            for(x=0;x < listSizeLines;x++) {
                if(x > 0 && x <= 19) {
                    theData1 += "${lines1[x]}<br>"
                }
            }
            theData1 += "</div></td></tr></table>"
            numOfCharacters1 = theData1.size()
            
            theData2 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            for(x=19;x < listSize1;x++) {
                if(x > 19 && x <= 39) {
                    theData2 += "${lines1[x]}<br>"
                }
            }
            theData2 += "</div></td></tr></table>"
            numOfCharacters2 = theData2.size()            
            
	        sendEvent(name: "dataPoints1", value: theData1, displayed: true)
            sendEvent(name: "numOfCharacters1", value: numOfCharacters1, displayed: true)
            
            sendEvent(name: "dataPoints2", value: theData2, displayed: true)
            sendEvent(name: "numOfCharacters2", value: numOfCharacters2, displayed: true)
            
            sendEvent(name: "readings1", value: state.readings1, displayed: true)
            
            String result = state.list1.join(",")
            theListSize = result.size()
            sendEvent(name: "theListSize", value: theListSize, displayed: true)
            sendEvent(name: "list1", value: state.list1, displayed: true, isStateChange:true)           
            
            sendEvent(name: "listSizeB", value: state.listSizeB, displayed: true)
            sendEvent(name: "listSizeW", value: state.listSizeW, displayed: true)
            
            if(state.readings1) readingsSize1 = state.readings1.size()
            sendEvent(name: "readingsSize1", value: readingsSize1, displayed: true)
            
            // Lets make sure Data
            if(logEnable) log.debug "Hub Watchdog Driver - Lets Make Some Data"            
            
    //  ** From https://www.javaworld.com/article/2073174/groovy--means--medians--modes--and-ranges-calculations.html 
            try {
                numberItems = state.readings1.size()
                sum = 0
                modeMap = new HashMap<BigDecimal, Integer>()
                for (item in state.readings1) {
                   sum += item
                   if (modeMap.get(item) == null) {
                       modeMap.put(item, 1)
                   } else {
                      count = modeMap.get(item) + 1
                      modeMap.put(item, count)
                   }
                }
                mode = new ArrayList<Integer>()
                modeCount = 0
                modeMap.each() { key, value -> 
                   if (value > modeCount) { mode.clear(); mode.add(key); modeCount = value}
                   else if (value == modeCount) { mode.add(key) }
                }
                sumDelayRecorded = sum 
                mn = sum / numberItems
                mean = mn.toFloat().round(3)
                midNumber = (int)(numberItems/2)
                med = numberItems %2 != 0 ? state.readings1[midNumber] : (state.readings1[midNumber] + state.readings1[midNumber-1])/2
                median = med.toFloat().round(3)
                minimum = state.readings1.min()
                maximum = state.readings1.max()
            } catch(e) {
                log.error "Hub Watchdog Driver - error to follow"
                log.error e
            }
            
    // *** end From
            if(logEnable) log.debug "Hub Watchdog Driver - Sending Data to attributes"
            sendEvent(name: "meanD", value: mean, displayed: true)
            sendEvent(name: "midNumberD", value: midNumber, displayed: true)
            sendEvent(name: "medianD", value: median, displayed: true)
            sendEvent(name: "minimumD", value: minimum, displayed: true)
            sendEvent(name: "maximumD", value: maximum, displayed: true)
            if(logEnable) log.debug "Hub Watchdog Driver - Finished"
        }
        catch(e1) {
            log.error "${e1}"
        }
}

def installed(){
    log.info "Hub Watchdog Driver has been Installed"
    clearData1()
    initialize()
}

def updated() {
    log.info "Hub Watchdog Driver has been Updated"
    initialize()
}

def initialize() {
    log.info "In initialize"
    
}

def keepListUnder() {    
    int listSizeLines = state.list1.size()
    
    String result = state.list1.join(",")
    int listSizeCharacters = result.size()
    
    if(logEnable) log.debug "In keepListUnder - listSizeLines: ${listSizeLines} - listSizeCharacters: ${listSizeCharacters}"
    
    if(listSizeCharacters > 1024) {
        state.list1.remove(listSizeLines - 1)
        runIn(1, keepListUnder)
    } else {
        if(logEnable) log.debug "In keepListUnder - All good!"
    }
}

def getDateTime() {
	def date = new Date()
    if(hourType == false) {
        newdate=date.format("MM-dd HH:mm")
        sendEvent( name: "lastUpdated", value: date.format("MM-dd - HH:mm:ss") )
    }
    if(hourType == true) {
        newdate=date.format("MM-dd hh:mm a")
        sendEvent( name: "lastUpdated", value: date.format("MM-dd - h:mm:ss a") )
    }
    return newdate
}

def on() {
    sendEvent(name: "switch", value: on, isStateChange: true)
}

def off() {
    sendEvent(name: "switch", value: off, isStateChange: true)
}

def clearData1() {
    state.theDataMap = [:]
    
    state.readings1 = []
    state.list1 = []
    state.listB = []
    state.listSizeB = 0
    state.listSizeW = 0
    
    sendEvent(name: "dataPoints1", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters1", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints2", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters2", value: 0, displayed: true)
    
    sendEvent(name: "readings1", value: state.readings1, displayed: true)
    sendEvent(name: "readingsSize1", value: 0, displayed: true)
    sendEvent(name: "listSizeB", value: 0, displayed: true)
    sendEvent(name: "listSizeW", value: 0, displayed: true)
    sendEvent(name: "listSize1", value: 0, displayed: true)
    sendEvent(name: "list1", value: 0, displayed: true)
    
    sendEvent(name: "meanD", value: 0, displayed: true)
    sendEvent(name: "midNumberD", value: 0, displayed: true)
    sendEvent(name: "medianD", value: 0, displayed: true)
    sendEvent(name: "minimumD", value: 0, displayed: true)
    sendEvent(name: "maximumD", value: 0, displayed: true)
    
    state.clear()
    
    if(uniqueID == "Virt") { app?.updateSetting("uniqueID",[value:"Vt",type:"text"]) }
    if(uniqueID == "Zwav") { app?.updateSetting("uniqueID",[value:"Zw",type:"text"]) }
    if(uniqueID == "Zigb") { app?.updateSetting("uniqueID",[value:"Zb",type:"text"]) }
    if(uniqueID == "Other") { app?.updateSetting("uniqueID",[value:"O",type:"text"]) }
}
