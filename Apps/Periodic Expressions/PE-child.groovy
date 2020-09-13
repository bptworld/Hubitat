/**
 *  **************** Periodic Expressions Child App  ****************
 *
 *  Design Usage:
 *  Create Periodic Cron Expression to be used with Event Engine
 *
 *  Copyright 2019-2020 Bryan Turcotte (@bptworld)
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
 *  Changes:
 *
 *  1.0.0 - 09/10/20 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "Periodic Expressions Child"
	state.version = "1.0.0"
}

definition(
    name: "Periodic Expressions Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Create Periodic Cron Expression to be used with Event Engine",
    category: "Convenience",
    parent: "BPTWorld:Periodic Expressions",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "",
)

preferences {
    page(name: "pageConfig")
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
			paragraph "<b>Notes:</b>"
    		paragraph "Create Periodic Cron Expression to be used with Event Engine"
		}

        section(getFormat("header-green", "${getImage("Blank")}"+" Periodic Schedule Options")) {
            paragraph "If not familiar with Cron Generators, please visit <a href='https://www.freeformatter.com/cron-expression-generator-quartz.html#' target='_blank'>this link</a> to see what this can do! Be sure to scroll down on that page to see the Cron expression examples."
            input "frequency", "enum", title: "Frequency", options: ["Seconds", "Minutes", "Hours", "Day", "Month", "Year"], submitOnChange:true
                        
            if(frequency == "Seconds") {
                input "secondTrigger", "enum", title: "Select the Seconds Option", options: [
                    ["1":"1. Every Second"],
                    ["2":"2. Every 'x' Second(s), starting at 'Second'"],
                    ["3":"3. Specific Second(s)"],
                    ["4":"4. Every Second between Second 'x' and Second 'y'"]
                ], submitOnChange:true
                
                if(secondTrigger == "1") {
                    paragraph "No options available (notice the '*' in Second)"
                    state.seconds = "*"
                    
                    // in English
                    state.inEnglish1s = "Every Second."
                    
                } else if(secondTrigger == "2") {
                    input "everySeconds2", "number", title: "Every 'x' Seconds (1 to 24)", range: '1..24', submitOnChange:true
                    input "startingSeconds2", "enum", title: "Starting at Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true 
                    
                    if(everySeconds2 == null) everySeconds2 = "0"
                    if(startingSeconds2 == null) startingSeconds2 = "0"
                    state.seconds = "${startingSeconds2}/${everySeconds2}" 
                    
                    // in English
                    state.inEnglish1s = "Every ${everySeconds2} Second(s), starting at second ${startingSeconds2}."
                    
                } else if(secondTrigger == "3") {
                    input "seconds3", "enum", title: "Specific Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], multiple:true, submitOnChange:true
                    state.seconds = seconds3
                    
                    // in English
                    state.inEnglish1s = "Specific Second(s) - ${seconds3}"
                    
                } else if(secondTrigger == "4") {
                    input "sbetweenX4", "enum", title: "Between Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    input "sbetweenY4", "enum", title: "and Second", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    state.seconds = "${sbetweenX4}-${sbetweenY4}"
                    
                    // in English
                    state.inEnglish1s = "Every Second between Second ${sbetweenX4} and Second ${sbetweenY4}"
                    
                } else {
                    state.seconds = "*"
                }
                
                state.seconds = state.seconds.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Minutes") {
                input "minuteTrigger", "enum", title: "Select the Minute Option", options: [
                    ["1":"1. Every Minute"],
                    ["2":"2. Every 'x' Minute(s), starting at 'Minute'"],
                    ["3":"3. Specific Minute(s)"],
                    ["4":"4. Every Minute between Minute 'x' and Minute 'y'"]
                ], submitOnChange:true
                
                if(minuteTrigger == "1") {
                    paragraph "No options available (notice the '*' in Minutes)"
                    state.minutes = "*"
                    
                    // in English
                    state.inEnglish1m = "Every Minute."
                    
                } else if(minuteTrigger == "2") {
                    input "everyMinutes2", "number", title: "Every 'x' Minutes (1 to 59)", range: '1..59', submitOnChange:true
                    input "startingMinutes2", "enum", title: "Starting at Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true 
                    
                    if(everyMinutes2 == null) everyMinutes2 = "0"
                    if(startingMinutes2 == null) startingMinutes2 = "0"
                    state.minutes = "${startingMinutes2}/${everyMinutes2}" 
                    
                    // in English
                    state.inEnglish1m = "Every ${everyMinutes2} Minute(s), starting at Minute ${startingMinutes2}."
                    
                } else if(minuteTrigger == "3") {
                    input "minutes3", "enum", title: "Specific Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], multiple:true, submitOnChange:true
                    state.minutes = minutes3
                    
                    // in English
                    state.inEnglish1m = "Specific Minute(s) - ${minutes3}"
                    
                } else if(minuteTrigger == "4") {
                    input "mbetweenX4", "enum", title: "Between Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    input "mbetweenY4", "enum", title: "and Minute", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    state.minutes = "${mbetweenX4}-${mbetweenY4}"
                    
                    // in English
                    state.inEnglish1m = "Every Minute between Minute ${mbetweenX4} and Minute ${mbetweenY4}"
                    
                } else {
                    state.minutes = "*"
                }
                
                state.minutes = state.minutes.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Hours") {
                input "hourTrigger", "enum", title: "Select the Hour Option", options: [
                    ["1":"1. Every Hour"],
                    ["2":"2. Every 'x' Hour(s), starting at 'Hour'"],
                    ["3":"3. Specific Hour(s)"],
                    ["4":"4. Every Hour between Hour 'x' and Hour 'y'"]
                ], submitOnChange:true
                
                if(hourTrigger == "1") {
                    paragraph "No options available (notice the '*' in Hours)"
                    state.theHours = "*"
                    
                    // in English
                    state.inEnglish1m = "Every Hour."
                    
                } else if(hourTrigger == "2") {
                    input "everyHours2", "number", title: "Every 'x' Hours (1 to 24)", range: '1..24', submitOnChange:true
                    input "startingHours2", "enum", title: "Starting at Hours", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true 
                    
                    if(everyHours2 == null) everyHours2 = "0"
                    if(startingHours2 == null) startingHours2 = "0"
                    state.theHours = "${startingHours2}/${everyHours2}" 
                    
                    // in English
                    state.inEnglish1m = "Every ${everyHours2} Hour(s), starting at Hour ${startingHours2}."
                    
                } else if(hourTrigger == "3") {
                    input "hours3", "enum", title: "Specific Hour", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], multiple:true, submitOnChange:true
                    state.theHours = hours3
                    
                    // in English
                    state.inEnglish1m = "Specific Hour(s) - ${hours3}"
                    
                } else if(hourTrigger == "4") {
                    input "mbetweenX4", "enum", title: "Between Hour", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    input "mbetweenY4", "enum", title: "and Hour", options: ["0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "50", "51", "52", "53", "54", "55", "56", "57", "58", "59"], submitOnChange:true
                    state.theHours = "${mbetweenX4}-${mbetweenY4}"
                    
                    // in English
                    state.inEnglish1m = "Every Hour between Hour ${mbetweenX4} and Hour ${mbetweenY4}"
                    
                } else {
                    state.theHours = "*"
                }
                
                state.theHours = state.theHours.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Day") {
                input "dayTrigger", "enum", title: "Select the Day Option", options: [
                    ["1":"1. Every Day"],
                    ["2":"2. Every 'x' Day(s), starting on 'Day'"],
                    ["3":"3. Every 'x' Day(s), starting on the 'x' Day of the Month"],
                    ["4":"4. Specific Day(s) of Week"],
                    ["5":"5. Specific Day(s) of the Month"],
                    ["6":"6. On the Last Day of the Month"],
                    ["7":"7. On the Last Weekday of the Month"],
                    ["8":"8. On the Last 'x' of the Month"],
                    ["9":"9. 'x' Day(s) before the end of the Month"],
                    ["10":"10. Nearest Weekday (Monday to Friday) to the 'x' of the Month"],
                    ["11":"11. On the '(1st, 2nd, etc)' '(Mon, Tue, etc)' of the Month"],
                ], submitOnChange:true
                              
                if(dayTrigger == "1") {
                    paragraph "1. No options available (notice the '*' in the Day in the Week)"
                    state.daily = "?"
                    state.dayOfTheWeek = "*"
                    state.inEnglish2 = "Every Day."
                    
                } else if(dayTrigger == "2") {
                    input "everyDays2", "number", title: "2. Every How Many Days (1 to 7)", range: '1..7', submitOnChange:true
                    input "startingDays2", "enum", title: "Starting on Day", options: [
                        ["1":"Sunday"],
                        ["2":"Monday"], 
                        ["3":"Tuesday"],
                        ["4":"Wednesday"],
                        ["5":"Thursday"],
                        ["6":"Friday"],
                        ["7":"Saturday"]
                    ], submitOnChange:true 

                    if(everyDays2 == null) everyDays2 = "1"
                    if(startingDays2 == null) startingDays2 = "1"
                    state.daily = "?"
                    state.dayOfTheWeek = "${startingDays2}/${everyDays2}"
                    
                    // In English                    
                    if(startingDays2 == "1") theDayName = "Sunday"
                    if(startingDays2 == "2") theDayName = "Monday"
                    if(startingDays2 == "3") theDayName = "Tuesday"
                    if(startingDays2 == "4") theDayName = "Wednesday"
                    if(startingDays2 == "5") theDayName = "Thursday"
                    if(startingDays2 == "6") theDayName = "Friday"
                    if(startingDays2 == "7") theDayName = "Saturday"
                    state.inEnglish2 = "Every ${everyDays2} Day(s), starting on ${theDayName}."
                    
                } else if(dayTrigger == "3") {
                    input "everyDays3", "number", title: "3. Every How Many Days (1 to 31)", range: '1..31', submitOnChange:true
                    input "startingDays3", "enum", title: "Starting on Day", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true 

                    if(everyDays3 == null) everyDays3 = "1"
                    if(startingDays3 == null) startingDays3 = "1"
                    state.daily = "${startingDays3}/${everyDays3}"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "Every ${everyDays3} Day(s), starting on the ${startingDays3} of the Month."
                    
                } else if(dayTrigger == "4") {
                    input "everyDays4", "enum", title: "4. Specific Day(s) of the Week", multiple:true, options: [
                        ["SUN":"Sunday"],
                        ["MON":"Monday"], 
                        ["TUE":"Tuesday"],
                        ["WED":"Wednesday"],
                        ["THU":"Thursday"],
                        ["FRI":"Friday"],
                        ["SAT":"Saturday"]
                    ], submitOnChange:true  

                    if(everyDays4 == null) everyDays4 = "SUN"
                    state.daily = "?"
                    state.dayOfTheWeek = "${everyDays4}"
                    
                    // in English
                    state.inEnglish2 = "Every ${everyDays4} of the Month."
                    
                } else if(dayTrigger == "5") {
                    input "everyDays5", "enum", title: "5. Specific Day(s) of the Month", multiple:true, options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true  

                    if(everyDays5 == null) everyDays5 = "1"
                    state.daily = "${everyDays}"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "Every ${everyDays5} of the Month."
                    
                } else if(dayTrigger == "6") {
                    paragraph "No options available (notice the 'L' in the Day of Month)"
                    state.daily = "L"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "On the Last Day of the Month."
                    
                } else if(dayTrigger == "7") {
                    paragraph "No options available (notice the 'LW' in the Day of Month)"
                    state.daily = "LW"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "On the Last Weekday of the Month."
                    
                } else if(dayTrigger == "8") {
                    input "everyDays8", "enum", title: "On the Last 'x' of the Month", options: [
                        ["1L":"Sunday"],
                        ["2L":"Monday"], 
                        ["3L":"Tuesday"],
                        ["4L":"Wednesday"],
                        ["5L":"Thursday"],
                        ["6L":"Friday"],
                        ["7L":"Saturday"]
                    ], submitOnChange:true  

                    if(everyDays8 == null) everyDays8 = "1L"
                    state.daily = "?"
                    state.dayOfTheWeek = "${everyDays8}"
                    
                    // in English
                    if(everyDays8 == "1L") dayName = "Sunday"
                    if(everyDays8 == "2L") dayName = "Monday"
                    if(everyDays8 == "3L") dayName = "Tuesday"
                    if(everyDays8 == "4L") dayName = "Wednesday"
                    if(everyDays8 == "5L") dayName = "Thursday"
                    if(everyDays8 == "6L") dayName = "Friday"
                    if(everyDays8 == "7L") dayName = "Saturday"
                    state.inEnglish2 = "On the Last ${dayName} of the Month."
                    
                } else if(dayTrigger == "9") {
                    input "everyDays9", "enum", title: "'x' Day(s) before the end of the Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true  

                    if(everyDays9 == null) everyDays9 = "1"
                    state.daily = "L-${everyDays9}"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "${everyDays9} Day(s) before the end of the Month."
                    
                } else if(dayTrigger == "10") {
                    input "everyDays10", "enum", title: "Nearest Weekday (Monday to Friday) to the 'x' of the Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"], submitOnChange:true  

                    if(everyDays10 == null) everyDays10 = "1"
                    state.daily = "${everyDays10}W"
                    state.dayOfTheWeek = "?"
                    
                    // in English
                    state.inEnglish2 = "Nearest Weekday (Monday to Friday) to the ${everyDays10} of the Month."
                    
                } else if(dayTrigger == "11") {
                    input "everyDays11", "enum", title: "On the 'x' of the Month", options: [
                        ["1":"1st"],
                        ["2":"2nd"],
                        ["3":"3rd"],
                        ["4":"4th"],
                        ["5":"5th"]
                    ], submitOnChange:true
                    input "startingDays11", "enum", title: "Day", options: [
                        ["1":"Sunday"],
                        ["2":"Monday"], 
                        ["3":"Tuesday"],
                        ["4":"Wednesday"],
                        ["5":"Thursday"],
                        ["6":"Friday"],
                        ["7":"Saturday"]
                    ], submitOnChange:true 
                    
                    if(everyDays11 == null) everyDays11 = "1"
                    if(startingDays11 == null) startingDays11 = "1"
                    state.daily = "?"
                    state.dayOfTheWeek = "${startingDays11}#${everyDays11}" 
                    
                    // in English
                    if(everyDays11 == "1") which = "1st"
                    if(everyDays11 == "2") which = "2nd"
                    if(everyDays11 == "3") which = "3rd"
                    if(everyDays11 == "4") which = "4th"
                    if(everyDays11 == "5") which = "5th"
                    
                    if(startingDays11 == "1") dName = "Sunday"
                    if(startingDays11 == "2") dName = "Monday"
                    if(startingDays11 == "3") dName = "Tuesday"
                    if(startingDays11 == "4") dName = "Wednesday"
                    if(startingDays11 == "5") dName = "Thursday"
                    if(startingDays11 == "6") dName = "Friday"
                    if(startingDays11 == "7") dName = "Saturday"
                    state.inEnglish2 = "On the ${which} ${dName} of the Month"
                }
                
                state.dayOfTheWeek = state.dayOfTheWeek.toString().replace("[", "").replace("]", "").replace(" ","")
            }
            
            if(frequency == "Month") {
                input "monthTrigger", "enum", title: "Select the Month Option", options: [
                    ["1":"1. Every Month"],
                    ["2":"2. Every 'x' Month(s), starting at 'Month'"],
                    ["3":"3. Specific Month(s)"],
                    ["4":"4. Every Month between Month 'x' and Month 'y'"]
                ], submitOnChange:true
                
                if(monthTrigger == "1") {
                    paragraph "No options available (notice the '*' in Months)"
                    state.months = "*"
                    
                    // in English
                    state.inEnglish3 = "Every Month."
                    
                } else if(monthTrigger == "2") {
                    input "everyMonth2", "number", title: "Every How Many Months (1 to 12)", range: '1..12', submitOnChange:true
                    input "startingMonths2", "enum", title: "Starting at Month", options: ["*", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"], submitOnChange:true 

                    if(everyMonth2 == null) everyMonth2 = "0"
                    if(startingMonths2 == null) startingMonths2 = "0"
                    state.months = "${startingMonths2}/${everyMonth2}" 
                    
                    // in English
                    state.inEnglish3 = "Every ${everyMonth2} Month(s), starting at ${startingMonths2}."
                    
                } else if(monthTrigger == "3") {    
                    input "monthly3", "enum", title: "Specific Hour", options: ["JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"], multiple:true, submitOnChange:true
                    state.months = monthly3
                    
                    // in English
                    state.inEnglish3 = "Specific Month(s) - ${monthly3}"
                    
                } else if(monthTrigger == "4") {
                    input "mbetweenX4", "enum", title: "Between Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"], submitOnChange:true
                    input "mbetweenY4", "enum", title: "and Month", options: ["1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"], submitOnChange:true
                    state.months = "${mbetweenX4}-${mbetweenY4}"
                    
                    // in English
                    state.inEnglish3 = "Every Month between Month ${mbetweenX4} and Month ${mbetweenY4}."
                    
                } else {
                    state.months = "*"
                }
                
                state.months = state.months.toString().replace("[", "").replace("]", "").replace(" ","")
            }

            if(frequency == "Year") {
                input "yearTrigger", "enum", title: "Select the Year Option", options: [
                    ["1":"1. Every Year"],
                    ["2":"2. Every 'x' Year(s), starting at 'Year'"],
                    ["3":"3. Specific Year(s)"],
                    ["4":"4. Every Year between Month 'x' and Year 'y'"]
                ], submitOnChange:true
                
                state.inEnglish4 = "Every Year."
                
                if(yearTrigger == "1") {
                    paragraph "No options available (notice the '*' in Years)"
                    state.years = "*"
                } else if(yearTrigger == "2") {
                    input "everyYear2", "number", title: "Every How Many Years (1 to 10)", range: '1..10', submitOnChange:true
                    input "startingYears2", "enum", title: "Starting at year", options: ["2020", "2021", "2022", "2023", "2024", "2025"], submitOnChange:true 

                    if(everyYear2 == null) everyYear2 = "0"
                    if(startingYears2 == null) startingYears2 = "0"
                    state.years = "${startingYears2}/${everyYear2}" 
                    
                    // in English
                    state.inEnglish4 = "Every ${everyYear2} Years(s), starting at ${startingYears2}."
                    
                } else if(yearTrigger == "3") {
                    input "yearly3", "enum", title: "Specific Year", options: ["2020", "2021", "2022", "2023", "2025", "2026", "2027", "2028", "2029", "2030"], multiple:true, submitOnChange:true
                    state.years = yearly3

                    // in English
                    state.inEnglish4 = "Specific Year(s) - ${yearly3}"
                    
                } else if(yearTrigger == "4") {
                    input "ybetweenX4", "enum", title: "Between Month", options: ["2020", "2021", "2022", "2023", "2025", "2026", "2027", "2028", "2029", "2030"], submitOnChange:true
                    input "ybetweenY4", "enum", title: "and Month", options: ["2020", "2021", "2022", "2023", "2025", "2026", "2027", "2028", "2029", "2030"], submitOnChange:true
                    state.years = "${ybetweenX4}-${ybetweenY4}"
                    
                    // in English
                    state.inEnglish4 = "Every Year between Year ${ybetweenX4} and Year ${ybetweenY4}."
                    
                } else {
                    state.years = "*"
                }
                
                state.years = state.years.toString().replace("[", "").replace("]", "").replace(" ","")
            }
               
            if(state.seconds == null) state.seconds = "*"
            if(state.minutes == null) state.minutes = "*"
            if(state.theHours == null) state.theHours = "*"
            if(state.daily == null) state.daily = "?"
            if(state.months == null) state.months = "*"
            if(state.dayOfTheWeek == null) state.dayOfTheWeek = "*"
            if(state.years == null) state.years = "*"

            paragraph "<hr>"
            
            if(state.inEnglish1h == null || state.inEnglish1h == "") state.inEnglish1h = "Every Hour."
            if(state.inEnglish1m == null || state.inEnglish1m == "") state.inEnglish1m = "Every Minute."
            if(state.inEnglish1s == null || state.inEnglish1s == "") state.inEnglish1s = "Every Second."
            
            if(state.inEnglish2 == null || state.inEnglish2 == "") state.inEnglish2 = "Every Day."
            if(state.inEnglish3 == null || state.inEnglish3 == "") state.inEnglish3 = "Every Month."
            if(state.inEnglish4 == null || state.inEnglish4 == "") state.inEnglish4 = "Every Year."
            
            state.inEnglish = "<b>Will Run:<br>${state.inEnglish1h} ${state.inEnglish1m} ${state.inEnglish1s}<br>${state.inEnglish2} ${state.inEnglish3} ${state.inEnglish4}</b>"
            
            paragraph "${state.inEnglish}"
            state.theSchedule = "${state.seconds} ${state.minutes} ${state.theHours} ${state.daily} ${state.months} ${state.dayOfTheWeek} ${state.years}"
            
            table = "<table width=90% align=center><tr align=center>"
            table += "<td><b>Seconds</b><br>${state.seconds}<td><b>Minutes</b><br>${state.minutes}<td><b>Hours</b><br>${state.theHours}<td><b>Day Of Month</b><br>${state.daily}<td><b>Months</b><br>${state.months}<td><b>Day Of Week</b><br>${state.dayOfTheWeek}<td><b>Years</b><br>${state.years}"
            table += "</table>"
            
            paragraph "<hr>"
            paragraph "${table}"
            paragraph "<hr>"
            paragraph "Cron Schedule: <b>${state.theSchedule}</b>" 
            paragraph "<hr>"
        }

        section(getFormat("header-green", "${getImage("Blank")}"+" App Control")) {
            input "pauseApp", "bool", title: "Pause App", defaultValue:false, submitOnChange:true            
            if(pauseApp) {
                if(app.label) {
                    if(!app.label.contains(" (Paused)")) {
                        app.updateLabel(app.label + " (Paused)")
                    }
                }
            } else {
                if(app.label) {
                    app.updateLabel(app.label - " (Paused)")
                }
            }
            paragraph "This app can be enabled/disabled by using a switch. The switch can also be used to enable/disable several apps at the same time."
            input "disableSwitch", "capability.switch", title: "Switch Device(s) to Enable / Disable this app", submitOnChange:true, required:false, multiple:true
        }
        
		section(getFormat("header-green", "${getImage("Blank")}"+" General")) {
            label title: "Enter a name for this automation", required:false
            input "logEnable", "bool", title: "Enable Debug Logging", description: "Enable logging for debugging.", defaultValue:false
		}
		display2()
	}
}

def installed() {
    log.debug "Installed with settings: ${settings}"
	initialize()
}

def updated() {	
    if(logEnable) log.debug "Updated with settings: ${settings}"
	unschedule()
    if(logEnable && logOffTime == "1 Hour") runIn(3600, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "2 Hours") runIn(7200, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "3 Hours") runIn(10800, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "4 Hours") runIn(14400, logsOff, [overwrite: false])
    if(logEnable && logOffTime == "5 Hours") runIn(18000, logsOff, [overwrite: false])
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        // do nothing
    }
}
    
// ********** Normal Stuff **********

def logsOff() {
    log.info "${app.label} - Debug logging auto disabled"
    app?.updateSetting("logEnable",[value:"false",type:"bool"])
}

def checkEnableHandler() {
    state.eSwitch = false
    if(disableSwitch) { 
        if(logEnable) log.debug "In checkEnableHandler - disableSwitch: ${disableSwitch}"
        disableSwitch.each { it ->
            state.eSwitch = it.currentValue("switch")
            if(state.eSwitch == "on") { state.eSwitch = true }
        }
    }
}

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

def getFormat(type, myText="") {			// Modified from @Stephack Code   
	if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
    if(type == "line") return "<hr style='background-color:#1A77C9; height: 1px; border: 0;'>"
    if(type == "title") return "<h2 style='color:#1A77C9;font-weight: bold'>${myText}</h2>"
}

def display() {
    setVersion()
    getHeaderAndFooter()
    theName = app.label
    if(theName == null || theName == "") theName = "New Child App"
    section (getFormat("title", "${getImage("logo")}" + " ${state.name} - ${theName}")) {
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
    timeSinceNewHeaders()   
    if(state.totalHours > 4) {
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
        }
        catch (e) { }
    }
    if(state.headerMessage == null) state.headerMessage = "<div style='color:#1A77C9'><a href='https://github.com/bptworld/Hubitat' target='_blank'>BPTWorld Apps and Drivers</a></div>"
    if(state.footerMessage == null) state.footerMessage = "<div style='color:#1A77C9;text-align:center'>BPTWorld Apps and Drivers<br><a href='https://github.com/bptworld/Hubitat' target='_blank'>Donations are never necessary but always appreciated!</a><br><a href='https://paypal.me/bptworld' target='_blank'><b>Paypal</b></a></div>"
}

def timeSinceNewHeaders() { 
    if(state.previous == null) { 
        prev = new Date()
    } else {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        prev = dateFormat.parse("${state.previous}".replace("+00:00","+0000"))
    }
    def now = new Date()
    use(TimeCategory) {       
        state.dur = now - prev
        state.days = state.dur.days
        state.hours = state.dur.hours
        state.totalHours = (state.days * 24) + state.hours
    }
    state.previous = now
    //if(logEnable) log.warn "In checkHoursSince - totalHours: ${state.totalHours}"
}
