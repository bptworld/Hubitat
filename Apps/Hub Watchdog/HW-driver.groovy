/**
 *  ****************  Hub Watchdog Driver  ****************
 *
 *  Design Usage:
 *  This driver formats data to be displayed on Hubitat's Dashboards.
 *
 *  Copyright 2019 Bryan Turcotte (@bptworld)
 *  
 *  This App is free.  If you like and use this app, please be sure to give a shout out on the Hubitat forums to let
 *  people know that it exists!  Thanks.
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
 *  V1.0.7 - 09/29/19 - Added support for 'Examiner' child app
 *  V1.0.6 - 09/28/19 - Fixed the '60' error.
 *  V1.0.5 - 09/26/19 - More color choices, rounded Med to 3
 *  V1.0.4 - 09/26/19 - Holds up to 80 data points, added color coding
 *  V1.0.3 - 09/25/19 - More tweaks
 *  V1.0.2 - 09/25/19 - Attempt to fix a null object error
 *  V1.0.1 - 09/25/19 - Added a lot of data points
 *  V1.0.0 - 09/24/19 - Initial release
 */
    
def setVersion(){
    appName = "HubWatchdogDriver"
	version = "v1.0.7" 
    dwInfo = "${appName}:${version}"
    sendEvent(name: "dwDriverInfo", value: dwInfo, displayed: true)
}

def updateVersion() {
    log.info "In updateVersion"
    setVersion()
}

metadata {
	definition (name: "Hub Watchdog Driver", namespace: "BPTWorld", author: "Bryan Turcotte", importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/Hub%20Watchdog/HW-driver.groovy") {
   		capability "Initialize"
		capability "Actuator"
        capability "Switch"
		
        attribute "readings1", "string"
    	attribute "dataPoints1", "string"
        attribute "dataPoints2", "string"
        attribute "dataPoints3", "string"
        attribute "dataPoints4", "string"
        attribute "dataPoints5", "string"
        attribute "dataPoints6", "string"
        attribute "dataPoints7", "string"
        attribute "dataPoints8", "string"
        attribute "lastDataPoint1", "string"
        attribute "numOfCharacters1", "string"
        attribute "numOfCharacters2", "string"
        attribute "numOfCharacters3", "string"
        attribute "numOfCharacters4", "string"
        attribute "numOfCharacters5", "string"
        attribute "numOfCharacters6", "string"
        attribute "numOfCharacters7", "string"
        attribute "numOfCharacters8", "string"
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
        
        attribute "dwDriverInfo", "string"
        command "updateVersion"
        
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
            input("uniqueID", "text", title: "Unique Identifier (Virt, Zwav, Zigb or Other)", required: true)
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
            if(readingsSize1 > 80) state.readings1.removeAt(80)
            
            getDateTime()
            
            if(theMessage >= maxDelay) {
                nMessage1 = "${newdate} - <span style='color: ${overColor}'>${theMessage}</span> - ${uniqueID}"
                if(state.listSizeB == null) state.listSizeB = 0
                state.listSizeB = state.listSizeB + 1
            } else if(theMessage >= warnValue) {
                nMessage1 = "${newdate} - <span style='color: ${warnColor}'>${theMessage}</span> - ${uniqueID}"
                if(state.listSizeW == null) state.listSizeW = 0
                state.listSizeW = state.listSizeW + 1
            } else {
                nMessage1 = "${newdate} - ${theMessage} - ${uniqueID}"
            }
            if(state.list1 == null) state.list1 = []
            state.list1.add(0,nMessage1)  

            if(state.list1) {
                listSize1 = state.list1.size()
            } else {
                listSize1 = 0
            }
            
            if(listSize1 > 80) state.list1.removeAt(80)

            String result1 = state.list1.join(";")
            def lines1 = result1.split(";")
            
            if(logEnable) log.trace "In makeList - All - listSize1: ${listSize1}"
            theData1 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 1) theData1 += "${lines1[0]}<br>"
            if(listSize1 >= 2) theData1 += "${lines1[1]}<br>"
            if(listSize1 >= 3) theData1 += "${lines1[2]}<br>"
            if(listSize1 >= 4) theData1 += "${lines1[3]}<br>"
            if(listSize1 >= 5) theData1 += "${lines1[4]}<br>"
            if(listSize1 >= 6) theData1 += "${lines1[5]}<br>"
            if(listSize1 >= 7) theData1 += "${lines1[6]}<br>"
            if(listSize1 >= 8) theData1 += "${lines1[7]}<br>"
            if(listSize1 >= 9) theData1 += "${lines1[8]}<br>"
            if(listSize1 >= 10) theData1 += "${lines1[9]}<br>"
            theData1 += "</div></td></tr></table>"
            
            dataCharCount1 = theData1.length()
	        if(dataCharCount1 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints1 - ${dataCharCount1} Characters"
	        } else {
                theData1 = "Too many characters to display on Dashboard (${dataCharCount1})"
	        }
            
            theData2 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 11) theData2 += "${lines1[10]}<br>"
            if(listSize1 >= 12) theData2 += "${lines1[11]}<br>"
            if(listSize1 >= 13) theData2 += "${lines1[12]}<br>"
            if(listSize1 >= 14) theData2 += "${lines1[13]}<br>"
            if(listSize1 >= 15) theData2 += "${lines1[14]}<br>"
            if(listSize1 >= 16) theData2 += "${lines1[15]}<br>"
            if(listSize1 >= 17) theData2 += "${lines1[16]}<br>"
            if(listSize1 >= 18) theData2 += "${lines1[17]}<br>"
            if(listSize1 >= 19) theData2 += "${lines1[18]}<br>"
            if(listSize1 >= 20) theData2 += "${lines1[19]}"
            theData2 += "</div></td></tr></table>"
    
            dataCharCount2 = theData2.length()
	        if(dataCharCount2 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints2 - ${dataCharCount2} Characters"
	        } else {
                theData2 = "Too many characters to display on Dashboard (${dataCharCount2})"
	        }
            
            theData3 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 21) theData3 += "${lines1[20]}<br>"
            if(listSize1 >= 22) theData3 += "${lines1[21]}<br>"
            if(listSize1 >= 23) theData3 += "${lines1[22]}<br>"
            if(listSize1 >= 24) theData3 += "${lines1[23]}<br>"
            if(listSize1 >= 25) theData3 += "${lines1[24]}<br>"
            if(listSize1 >= 26) theData3 += "${lines1[25]}<br>"
            if(listSize1 >= 27) theData3 += "${lines1[26]}<br>"
            if(listSize1 >= 28) theData3 += "${lines1[27]}<br>"
            if(listSize1 >= 29) theData3 += "${lines1[28]}<br>"
            if(listSize1 >= 30) theData3 += "${lines1[29]}"
            theData3 += "</div></td></tr></table>"
    
            dataCharCount3 = theData3.length()
	        if(dataCharCount3 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints3 - ${dataCharCount3} Characters"
	        } else {
                theData3 = "Too many characters to display on Dashboard (${dataCharCount3})"
	        }
            
            theData4 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 31) theData4 += "${lines1[30]}<br>"
            if(listSize1 >= 32) theData4 += "${lines1[31]}<br>"
            if(listSize1 >= 33) theData4 += "${lines1[32]}<br>"
            if(listSize1 >= 34) theData4 += "${lines1[33]}<br>"
            if(listSize1 >= 35) theData4 += "${lines1[34]}<br>"
            if(listSize1 >= 36) theData4 += "${lines1[35]}<br>"
            if(listSize1 >= 37) theData4 += "${lines1[36]}<br>"
            if(listSize1 >= 38) theData4 += "${lines1[37]}<br>"
            if(listSize1 >= 39) theData4 += "${lines1[38]}<br>"
            if(listSize1 >= 40) theData4 += "${lines1[39]}"
            theData4 += "</div></td></tr></table>"
    
            dataCharCount4 = theData4.length()
	        if(dataCharCount4 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints4 - ${dataCharCount4} Characters"
	        } else {
                theData4 = "Too many characters to display on Dashboard (${dataCharCount4})"
	        }
            
            theData5 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 41) theData5 += "${lines1[40]}<br>"
            if(listSize1 >= 42) theData5 += "${lines1[41]}<br>"
            if(listSize1 >= 43) theData5 += "${lines1[42]}<br>"
            if(listSize1 >= 44) theData5 += "${lines1[43]}<br>"
            if(listSize1 >= 45) theData5 += "${lines1[44]}<br>"
            if(listSize1 >= 46) theData5 += "${lines1[45]}<br>"
            if(listSize1 >= 47) theData5 += "${lines1[46]}<br>"
            if(listSize1 >= 48) theData5 += "${lines1[47]}<br>"
            if(listSize1 >= 49) theData5 += "${lines1[48]}<br>"
            if(listSize1 >= 50) theData5 += "${lines1[49]}"
            theData5 += "</div></td></tr></table>"
    
            dataCharCount5 = theData5.length()
	        if(dataCharCount5 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints5 - ${dataCharCount5} Characters"
	        } else {
                theData5 = "Too many characters to display on Dashboard (${dataCharCount5})"
	        }
            
            theData6 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 51) theData6 += "${lines1[50]}<br>"
            if(listSize1 >= 52) theData6 += "${lines1[51]}<br>"
            if(listSize1 >= 53) theData6 += "${lines1[52]}<br>"
            if(listSize1 >= 54) theData6 += "${lines1[53]}<br>"
            if(listSize1 >= 55) theData6 += "${lines1[54]}<br>"
            if(listSize1 >= 56) theData6 += "${lines1[55]}<br>"
            if(listSize1 >= 57) theData6 += "${lines1[56]}<br>"
            if(listSize1 >= 58) theData6 += "${lines1[57]}<br>"
            if(listSize1 >= 59) theData6 += "${lines1[58]}<br>"
            if(listSize1 >= 60) theData6 += "${lines1[59]}"
            theData6 += "</div></td></tr></table>"
    
            dataCharCount6 = theData6.length()
	        if(dataCharCount6 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints6 - ${dataCharCount6} Characters"
	        } else {
                theData6 = "Too many characters to display on Dashboard (${dataCharCount6})"
	        }
            
            theData7 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 61) theData7 += "${lines1[60]}<br>"
            if(listSize1 >= 62) theData7 += "${lines1[61]}<br>"
            if(listSize1 >= 63) theData7 += "${lines1[62]}<br>"
            if(listSize1 >= 64) theData7 += "${lines1[63]}<br>"
            if(listSize1 >= 65) theData7 += "${lines1[64]}<br>"
            if(listSize1 >= 66) theData7 += "${lines1[65]}<br>"
            if(listSize1 >= 67) theData7 += "${lines1[66]}<br>"
            if(listSize1 >= 68) theData7 += "${lines1[67]}<br>"
            if(listSize1 >= 69) theData7 += "${lines1[68]}<br>"
            if(listSize1 >= 70) theData7 += "${lines1[69]}"
            theData7 += "</div></td></tr></table>"
    
            dataCharCount7 = theData7.length()
	        if(dataCharCount7 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints7 - ${dataCharCount7} Characters"
	        } else {
                theData7 = "Too many characters to display on Dashboard (${dataCharCount7})"
	        }
            
            theData8 = "<table><tr><td><div style='font-size:${fontSize}px'>"
            if(listSize1 >= 71) theData8 += "${lines1[70]}<br>"
            if(listSize1 >= 72) theData8 += "${lines1[71]}<br>"
            if(listSize1 >= 73) theData8 += "${lines1[72]}<br>"
            if(listSize1 >= 74) theData8 += "${lines1[73]}<br>"
            if(listSize1 >= 75) theData8 += "${lines1[74]}<br>"
            if(listSize1 >= 76) theData8 += "${lines1[75]}<br>"
            if(listSize1 >= 77) theData8 += "${lines1[76]}<br>"
            if(listSize1 >= 78) theData8 += "${lines1[77]}<br>"
            if(listSize1 >= 79) theData8 += "${lines1[78]}<br>"
            if(listSize1 >= 80) theData8 += "${lines1[79]}"
            theData8 += "</div></td></tr></table>"
    
            dataCharCount8 = theData8.length()
	        if(dataCharCount8 <= 1024) {
	        	if(logEnable) log.debug "Hub Watchdog Driver - dataPoints8 - ${dataCharCount8} Characters"
	        } else {
                theData8 = "Too many characters to display on Dashboard (${dataCharCount8})"
	        }
            
	        sendEvent(name: "dataPoints1", value: theData1, displayed: true)
            sendEvent(name: "numOfCharacters1", value: dataCharCount1, displayed: true)
            
            sendEvent(name: "dataPoints2", value: theData2, displayed: true)
            sendEvent(name: "numOfCharacters2", value: dataCharCount2, displayed: true)
            
            sendEvent(name: "dataPoints3", value: theData3, displayed: true)
            sendEvent(name: "numOfCharacters3", value: dataCharCount3, displayed: true)
            
            sendEvent(name: "dataPoints4", value: theData4, displayed: true)
            sendEvent(name: "numOfCharacters4", value: dataCharCount4, displayed: true)
            
            sendEvent(name: "dataPoints5", value: theData5, displayed: true)
            sendEvent(name: "numOfCharacters5", value: dataCharCount5, displayed: true)
            
            sendEvent(name: "dataPoints6", value: theData6, displayed: true)
            sendEvent(name: "numOfCharacters6", value: dataCharCount6, displayed: true)
            
            sendEvent(name: "dataPoints7", value: theData7, displayed: true)
            sendEvent(name: "numOfCharacters7", value: dataCharCount7, displayed: true)
            
            sendEvent(name: "dataPoints8", value: theData8, displayed: true)
            sendEvent(name: "numOfCharacters8", value: dataCharCount8, displayed: true)
            
            sendEvent(name: "readings1", value: state.readings1, displayed: true)
            
            sendEvent(name: "list1", value: state.list1, displayed: true)
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
                minimum = Collections.min(state.readings1)
                maximum = Collections.max(state.readings1)
            } catch(e) {
                log.error "Hub Watchdog Driver - ${e}"  
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
}

def updated() {
    log.info "Hub Watchdog Driver has been Updated"
}

def initialize() {
    log.info "In initialize"
}

def getDateTime() {
	def date = new Date()
	if(hourType == false) newdate=date.format("MM-d HH:mm")
	if(hourType == true) newdate=date.format("MM-d hh:mm a")
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
    
    sendEvent(name: "dataPoints3", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters3", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints4", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters4", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints5", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters5", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints6", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters6", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints7", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters7", value: 0, displayed: true)
    
    sendEvent(name: "dataPoints8", value: "-", displayed: true)
    sendEvent(name: "numOfCharacters8", value: 0, displayed: true)
    
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
}
