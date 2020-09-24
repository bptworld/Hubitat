/**
 *  **************** CATT Director App  ****************
 *  Design Usage:
 *  Take control of a Chromecast device using CATT.
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
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  If modifying this project, please keep the above header intact and add your comments/credits below - Thank you! -  @BPTWorld
 *  App and Driver updates can be found at https://github.com/bptworld/Hubitat/
 *
 * ------------------------------------------------------------------------------------------------------------------------------
 *
 *  Changes:
 *
 *  2.0.4 - 09/02/20 - More Cosmetic changes
 *  2.0.3 - 04/27/20 - Cosmetic changes
 *  2.0.2 - 04/18/20 - Adjustments
 *  2.0.1 - 09/09/19 - Fixed typo
 *  2.0.0 - 08/18/19 - Now App Watchdog compliant
 *  1.0.0 - 08/17/19 - Initial release.
 *
 */

import groovy.time.TimeCategory
import java.text.SimpleDateFormat

def setVersion(){
    state.name = "CATT Director"
	state.version = "2.0.4"
}

definition(
    name: "CATT Director Child",
    namespace: "BPTWorld",
    author: "Bryan Turcotte",
    description: "Take control of a Chromecast device using CATT.",
    category: "",
	parent: "BPTWorld:CATT Director",
    iconUrl: "",
    iconX2Url: "",
    iconX3Url: "",
	importUrl: "https://raw.githubusercontent.com/bptworld/Hubitat/master/Apps/CATT%20Director/CATTD-child.groovy",
)

preferences {
    page(name: "pageConfig")
	page name: "pushOptions", title: "", install: false, uninstall: true, nextPage: "pageConfig"
}

def pageConfig() {
    dynamicPage(name: "", title: "", install: true, uninstall: true, refreshInterval:0) {
		display() 
        section("Instructions:", hideable: true, hidden: true) {
            cattAbilities = "<table align=center width=100%>"
            cattAbilities += "<tr><td><b>Abilities</b></td><td> - </td><td><b>Description</b></td></tr>"
            cattAbilities += "<tr><td>add<td> - </td><td>Add a video to the queue (YouTube only).</td></tr>"
            cattAbilities += "<tr><td>cast</td><td> - </td><td>Send a video to a Chromecast for playing.</td></tr>"
            cattAbilities += "<tr><td>cast_site</td><td> - </td><td>Cast any website to a Chromecast.</td></tr>"
            cattAbilities += "<tr><td>clear</td><td> - </td><td>Clear the queue (YouTube only).</td></tr>"
            cattAbilities += "<tr><td>ffwd</td><td> - </td><td>Fastforward a video by TIME duration.</td></tr>"
            cattAbilities += "<tr><td>info</td><td> - </td><td>Show complete information about the currently-playing video.</td></tr>"
            cattAbilities += "<tr><td>pause</td><td> - </td><td>Pause a video.</td></tr>"
            cattAbilities += "<tr><td>play</td><td> - </td><td>Resume a video after it has been paused.</td></tr>"
            cattAbilities += "<tr><td>remove</td><td> - </td><td>Remove a video from the queue (YouTube only).</td></tr>"
            cattAbilities += "<tr><td>restore</td><td> - </td><td>Return Chromecast to saved state.</td></tr>"
            cattAbilities += "<tr><td>rewind</td><td> - </td><td>Rewind a video by TIME duration.</td></tr>"
            cattAbilities += "<tr><td>save</td><td> - </td><td>Save the current state of the Chromecast for later use.</td></tr>"
        //    cattAbilities += "<tr><td>scan</td><td> - </td><td>Scan the local network and show all Chromecasts and their IPs.</td></tr>"
        //    cattAbilities += "<tr><td>seek</td><td> - </td><td>Seek the video to TIME position.</td></tr>"
            cattAbilities += "<tr><td>skip</td><td> - </td><td>Skip to end of content.</td></tr>"
            cattAbilities += "<tr><td>status</td><td> - </td><td>Show some information about the currently-playing video.</td></tr>"
            cattAbilities += "<tr><td>stop</td><td> - </td><td>Stop playing.</td></tr>"
            cattAbilities += "<tr><td>volume</td><td> - </td><td>Set the volume to LVL [0-100].</td></tr>"
            cattAbilities += "<tr><td>volumedown</td><td> - </td><td>Turn down volume by a DELTA increment.</td></tr>"
            cattAbilities += "<tr><td>volumeup</td><td> - </td><td>Turn up volume by a DELTA increment.</td></tr>"
            cattAbilities += "<tr><td>write_config</td><td> - </td><td>Write the name of default Chromecast device to config file.</td></tr>"
            cattAbilities += "</table>"

            paragraph "<b>Cast All The Things Director</b><br>Take control of a Chromecast device using CATT."
            paragraph "${cattAbilities}"

			paragraph "<b>Notes:</b>"
			paragraph "* Vitual Device must use the 'Send to Hub with CATT' Driver"
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" CATT Device")) {
			input "cattDevice", "capability.speechSynthesis", title: "Virtual Device created with 'Send to Hub with CATT' Driver", required: true, multiple: true
		}
        section(getFormat("header-green", "${getImage("Blank")}"+" Google/Nest Device")) {
			input "googleDevice", "capability.speechSynthesis", title: "Device to send the commands to (must be a Chromecast device)", required: true, multiple: true
		}
		section(getFormat("header-green", "${getImage("Blank")}"+" Triggers")) {
			input "myContact", "capability.contactSensor", title: "Select the contact sensor to control the event", required: false, multiple: true, submitOnChange: true
			input "myMotion", "capability.motionSensor", title: "Select the motion sensor to control the event", required: false, multiple: true, submitOnChange: true
			input "mySwitch", "capability.switch", title: "Select the switch to control the event", required: false, multiple: true, submitOnChange: true
            
            if(!myContact && !myMotion && !mySwitch) {
                input "timeToRun", "time", title: "Select time to start the event", required: false, width: 6
                input "timeToStop", "time", title: "Select time to stop the event", required: false, width: 6
            }
            
            if(myContact || myMotion || mySwitch) {
                paragraph "<b>Trigger Options</b>"
                input "triggerDelayEnd", "number", title: "Delay end of trigger by (in seconds)", description: "0-60", range: '1..60', defaultValue: 1, required: true
                input "triggerAutoEnd", "number", title: "Automaticaly end trigger after (in seconds, 0=off)", description: "0-300", range: '0..300', defaultValue: 0, required: true
            }
		}
        section(){
            paragraph getFormat("line")
            paragraph "<b>CATT Director allows you to send a few commands in a sequence. Please only select ONE option from each section.</b>"
        }
		section(getFormat("header-green", "${getImage("Blank")}"+" Do this first")) {
			input(name: "add1", type: "bool", defaultValue: "false", title: "add", description: "", submitOnChange: "true", width: 4)
			input(name: "cast1", type: "bool", defaultValue: "false", title: "cast", description: "", submitOnChange: "true", width: 4)
			input(name: "cast_site1", type: "bool", defaultValue: "false", title: "cast_site", description: "", submitOnChange: "true", width: 4)
			input(name: "clear1", type: "bool", defaultValue: "false", title: "clear", description: "", submitOnChange: "true", width: 4)
			input(name: "ffwd1", type: "bool", defaultValue: "false", title: "ffwd", description: "", submitOnChange: "true", width: 4)
			input(name: "info1", type: "bool", defaultValue: "false", title: "info", description: "", submitOnChange: "true", width: 4)
        	input(name: "pause1", type: "bool", defaultValue: "false", title: "pause", description: "", submitOnChange: "true", width: 4)
			input(name: "play1", type: "bool", defaultValue: "false", title: "play", description: "", submitOnChange: "true", width: 4)
			input(name: "remove1", type: "bool", defaultValue: "false", title: "remove", description: "", submitOnChange: "true", width: 4)
            input(name: "restore1", type: "bool", defaultValue: "false", title: "restore", description: "", submitOnChange: "true", width: 4)
            input(name: "rewind1", type: "bool", defaultValue: "false", title: "rewind", description: "", submitOnChange: "true", width: 4)
            input(name: "save1", type: "bool", defaultValue: "false", title: "save", description: "", submitOnChange: "true", width: 4)
        //    input(name: "scan1", type: "bool", defaultValue: "false", title: "scan", description: "", submitOnChange: "true", width: 4)
        //    input(name: "seek1", type: "bool", defaultValue: "false", title: "seek, description: "", submitOnChange: "true", width: 4)
            input(name: "skip1", type: "bool", defaultValue: "false", title: "skip", description: "", submitOnChange: "true", width: 4)
            input(name: "status1", type: "bool", defaultValue: "false", title: "status", description: "", submitOnChange: "true", width: 4)
            input(name: "stop1", type: "bool", defaultValue: "false", title: "stop", description: "", submitOnChange: "true", width: 4)
            input(name: "volume1", type: "bool", defaultValue: "false", title: "volume", description: "", submitOnChange: "true", width: 4)
            input(name: "volumedown1", type: "bool", defaultValue: "false", title: "volumedown", description: "", submitOnChange: "true", width: 4)
            input(name: "volumeup1", type: "bool", defaultValue: "false", title: "volumeup", description: "", submitOnChange: "true", width: 4)
            input(name: "write_config1", type: "bool", defaultValue: "false", title: "write_config", description: "", submitOnChange: "true", width: 4)
            
            paragraph ""
            if(add1) {
                input "castAdd1", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "https://youtu.be/asKFYkRloOA", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(cast1) {
                input "castURL1", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "https://youtu.be/asKFYkRloOA", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(cast_site1) {
                input "castWebsite1", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "http://www.google.com", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(clear1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(ffwd1) {
                input "castFfwd1", "text", title: "How many seconds to fast forward", required: true, defaultValue: "10", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(info1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(pause1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(play1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(remove1) {
                input "castRemove1", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(restore1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(rewind1) {
                input "castRewind1", "text", title: "How many seconds to rewind", required: true, defaultValue: "10", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(save1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(skip1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(status1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(stop1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(volume1) {
                input "castVolume1", "number", title: "Volume Level (0-100)", description: "0-100", range: '1..100', required: true, width: 10
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(volumedown1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(volumeup1) {
                input "testBtn1", "button", title: "Test", width: 2
            } else
            if(write_config1) {
                input "castWrite_config1", "text", title: "Exact name of device", required: true, defaultValue: "", width: 10
                input "testBtn1", "button", title: "Test", width: 2
            }
        }
        
        section() {
            paragraph "<hr>"
            input "secToPause1to2", "number", title: "How many seconds to pause before sending the second command", required: true, defaultValue: 2
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Do this second")) {
			input(name: "add2", type: "bool", defaultValue: "false", title: "add", description: "", submitOnChange: "true", width: 4)
			input(name: "cast2", type: "bool", defaultValue: "false", title: "cast", description: "", submitOnChange: "true", width: 4)
			input(name: "cast_site2", type: "bool", defaultValue: "false", title: "cast_site", description: "", submitOnChange: "true", width: 4)
			input(name: "clear2", type: "bool", defaultValue: "false", title: "clear", description: "", submitOnChange: "true", width: 4)
			input(name: "ffwd2", type: "bool", defaultValue: "false", title: "ffwd", description: "", submitOnChange: "true", width: 4)
			input(name: "info2", type: "bool", defaultValue: "false", title: "info", description: "", submitOnChange: "true", width: 4)
        	input(name: "pause2", type: "bool", defaultValue: "false", title: "pause", description: "", submitOnChange: "true", width: 4)
			input(name: "play2", type: "bool", defaultValue: "false", title: "play", description: "", submitOnChange: "true", width: 4)
			input(name: "remove2", type: "bool", defaultValue: "false", title: "remove", description: "", submitOnChange: "true", width: 4)
            input(name: "restore2", type: "bool", defaultValue: "false", title: "restore", description: "", submitOnChange: "true", width: 4)
            input(name: "rewind2", type: "bool", defaultValue: "false", title: "rewind", description: "", submitOnChange: "true", width: 4)
            input(name: "save2", type: "bool", defaultValue: "false", title: "save", description: "", submitOnChange: "true", width: 4)
        //    input(name: "scan2", type: "bool", defaultValue: "false", title: "scan", description: "", submitOnChange: "true", width: 4)
        //    input(name: "seek2", type: "bool", defaultValue: "false", title: "seek, description: "", submitOnChange: "true", width: 4)
            input(name: "skip2", type: "bool", defaultValue: "false", title: "skip", description: "", submitOnChange: "true", width: 4)
            input(name: "status2", type: "bool", defaultValue: "false", title: "status", description: "", submitOnChange: "true", width: 4)
            input(name: "stop2", type: "bool", defaultValue: "false", title: "stop", description: "", submitOnChange: "true", width: 4)
            input(name: "volume2", type: "bool", defaultValue: "false", title: "volume", description: "", submitOnChange: "true", width: 4)
            input(name: "volumedown2", type: "bool", defaultValue: "false", title: "volumedown", description: "", submitOnChange: "true", width: 4)
            input(name: "volumeup2", type: "bool", defaultValue: "false", title: "volumeup", description: "", submitOnChange: "true", width: 4)
            input(name: "write_config2", type: "bool", defaultValue: "false", title: "write_config", description: "", submitOnChange: "true", width: 4)
            
            paragraph ""
            if(add2) {
                input "castAdd2", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "https://youtu.be/asKFYkRloOA", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(cast2) {
                input "castURL2", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "https://youtu.be/asKFYkRloOA", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(cast_site2) {
                input "castWebsite2", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "http://www.google.com", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(clear2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(ffwd2) {
                input "castFfwd2", "text", title: "How many seconds to fast forward", required: true, defaultValue: "10", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(info2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(pause2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(play2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(remove2) {
                input "castRemove2", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(restore2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(rewind2) {
                input "castRewind2", "text", title: "How many seconds to rewind", required: true, defaultValue: "10", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(save2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(skip2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(status2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(stop2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(volume2) {
                input "castVolume2", "number", title: "Volume Level (0-100)", description: "0-100", range: '1..100', required: true, width: 10
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(volumedown2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(volumeup2) {
                input "testBtn2", "button", title: "Test", width: 2
            } else
            if(write_config2) {
                input "castWrite_config2", "text", title: "Exact name of device", required: true, defaultValue: "", width: 10
                input "testBtn2", "button", title: "Test", width: 2
            }
        }
        
        section() {
            paragraph "<hr>"
            input "secToPause2to3", "number", title: "How many seconds to pause before sending the third command", required: true, defaultValue: 2
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Do this third")) {
			input(name: "add3", type: "bool", defaultValue: "false", title: "add", description: "", submitOnChange: "true", width: 4)
			input(name: "cast3", type: "bool", defaultValue: "false", title: "cast", description: "", submitOnChange: "true", width: 4)
			input(name: "cast_site3", type: "bool", defaultValue: "false", title: "cast_site", description: "", submitOnChange: "true", width: 4)
			input(name: "clear3", type: "bool", defaultValue: "false", title: "clear", description: "", submitOnChange: "true", width: 4)
			input(name: "ffwd3", type: "bool", defaultValue: "false", title: "ffwd", description: "", submitOnChange: "true", width: 4)
			input(name: "info3", type: "bool", defaultValue: "false", title: "info", description: "", submitOnChange: "true", width: 4)
        	input(name: "pause3", type: "bool", defaultValue: "false", title: "pause", description: "", submitOnChange: "true", width: 4)
			input(name: "play3", type: "bool", defaultValue: "false", title: "play", description: "", submitOnChange: "true", width: 4)
			input(name: "remove3", type: "bool", defaultValue: "false", title: "remove", description: "", submitOnChange: "true", width: 4)
            input(name: "restore3", type: "bool", defaultValue: "false", title: "restore", description: "", submitOnChange: "true", width: 4)
            input(name: "rewind3", type: "bool", defaultValue: "false", title: "rewind", description: "", submitOnChange: "true", width: 4)
            input(name: "save3", type: "bool", defaultValue: "false", title: "save", description: "", submitOnChange: "true", width: 4)
        //    input(name: "scan3", type: "bool", defaultValue: "false", title: "scan", description: "", submitOnChange: "true", width: 4)
        //    input(name: "seek3", type: "bool", defaultValue: "false", title: "seek, description: "", submitOnChange: "true", width: 4)
            input(name: "skip3", type: "bool", defaultValue: "false", title: "skip", description: "", submitOnChange: "true", width: 4)
            input(name: "status3", type: "bool", defaultValue: "false", title: "status", description: "", submitOnChange: "true", width: 4)
            input(name: "stop3", type: "bool", defaultValue: "false", title: "stop", description: "", submitOnChange: "true", width: 4)
            input(name: "volume3", type: "bool", defaultValue: "false", title: "volume", description: "", submitOnChange: "true", width: 4)
            input(name: "volumedown3", type: "bool", defaultValue: "false", title: "volumedown", description: "", submitOnChange: "true", width: 4)
            input(name: "volumeup3", type: "bool", defaultValue: "false", title: "volumeup", description: "", submitOnChange: "true", width: 4)
            input(name: "write_config3", type: "bool", defaultValue: "false", title: "write_config", description: "", submitOnChange: "true", width: 4)
            
            paragraph ""
            if(add3) {
                input "castAdd3", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "https://youtu.be/asKFYkRloOA", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(cast3) {
                input "castURL3", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "https://youtu.be/asKFYkRloOA", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(cast_site3) {
                input "castWebsite3", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "http://www.google.com", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(clear3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(ffwd3) {
                input "castFfwd3", "text", title: "How many seconds to fast forward", required: true, defaultValue: "10", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(info3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(pause3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(play3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(remove3) {
                input "castRemove3", "text", title: "Enter the exact URL including http://", required: true, defaultValue: "", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(restore3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(rewind3) {
                input "castRewind3", "text", title: "How many seconds to rewind", required: true, defaultValue: "10", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(save3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(skip3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(status3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(stop3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(volume3) {
                input "castVolume3", "number", title: "Volume Level (0-100)", description: "0-100", range: '1..100', required: true, width: 10
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(volumedown3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(volumeup3) {
                input "testBtn3", "button", title: "Test", width: 2
            } else
            if(write_config3) {
                input "castWrite_config3", "text", title: "Exact name of device", required: true, defaultValue: "", width: 10
                input "testBtn3", "button", title: "Test", width: 2
            }
        }
        
        section(getFormat("header-green", "${getImage("Blank")}"+" Test")) {
            paragraph "After entering in the options above, click here to test the sequence", width: 8
            input "testBtnSequence", "button", title: "Test the Sequence", width: 4
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
            label title: "Enter a name for this automation", required: false
            input "logEnable", "bool", defaultValue:false, title: "Enable Debug Logging", description: "Enable extra logging for debugging."
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
    unsubscribe()
    if(logEnable) runIn(3600, logsOff)
	initialize()
}

def initialize() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        setDefaults()
        if(myContact) subscribe(myContact, "contact", contactSensorHandler)
        if(myMotion) subscribe(myMotion, "motion", motionSensorHandler)
        if(mySwitch) subscribe(mySwitch, "switch", switchHandler)
        if(timeToRun) schedule(timeToRun, startTimeHandler)
        if(timeToStop) schedule(timeToStop, stopTimeHandler)
    }
}

def contactSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In contactSensorHandler..."
        state.contactStatus = evt.value
        if(state.contactStatus == "open") {
            if(logEnable) log.debug "In contactSensorHandler - open"
            firstHandler()
            if(triggerAutoEnd != 0) runIn(triggerAutoEnd, stopTimeHandler)
        } else {
            if(logEnable) log.debug "In contactSensorHandler - closed"
            runIn(triggerDelayEnd, stopTimeHandler)
        }
    }
}

def motionSensorHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In motionSensorHandler..."
        state.motionStatus = evt.value
        if(state.motionStatus == "active") {
            if(logEnable) log.debug "In motionSensorHandler - active"
            firstHandler()
            if(triggerAutoEnd != 0) runIn(triggerAutoEnd, stopTimeHandler)
        } else {
            if(logEnable) log.debug "In motionSensorHandler - Not active"
            runIn(triggerDelayEnd, stopTimeHandler)
        }
    }
}

def switchHandler(evt) {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In switchHandler..."
        state.switchStatus = evt.value
        if(state.switchStatus == "on") {
            if(logEnable) log.debug "In switchHandler - on"
            firstHandler()
            if(triggerAutoEnd != 0) runIn(triggerAutoEnd, stopTimeHandler)
        } else {
            if(logEnable) log.debug "In switchHandler - off"
            runIn(triggerDelayEnd, stopTimeHandler)
        }
    }
}
						  
def startTimeHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In startTimeHandler..."
        firstHandler()
    }
}

def stopTimeHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        if(logEnable) log.debug "In stopTimeHandler..."
        cattDevice.stop()
    }
}

def firstHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        thePause = secToPause1to2 * 1000
        if(logEnable) log.debug "In firstHandler - pause between commands: ${thePause}"
        if(add1) {
            cattDevice.add(castAdd1)
            pauseExecution(thePause)
        }
        if(cast1) {
            cattDevice.cast(castURL1)
            pauseExecution(thePause)
        }
        if(cast_site1) {
            cattDevice.on(castWebsite1)
            pauseExecution(thePause)
        }
        if(clear1) {
            cattDevice.clear()
            pauseExecution(thePause)
        }
        if(ffwd1) {
            cattDevice.ffwd(castFfwd1)
            pauseExecution(thePause)
        }
        if(info1) {
            cattDevice.info()
            pauseExecution(thePause)
        }
        if(pause1) {
            cattDevice.pause()
            pauseExecution(thePause)
        }
        if(play1) {
            cattDevice.play()
            pauseExecution(thePause)
        }
        if(remove1) {
            cattDevice.remove(castRemove1)
            pauseExecution(thePause)
        }
        if(restore1) {
            cattDevice.restore()
            pauseExecution(thePause)
        }
        if(rewind1) {
            cattDevice.rewind(castRewind1)
            pauseExecution(thePause)
        }
        if(save1) {
            cattDevice.save()
            pauseExecution(thePause)
        }
        if(scan1) {
            cattDevice.scan()
            pauseExecution(thePause)
        }
        if(seek1) {
            cattDevice.seek()
            pauseExecution(thePause)
        }
        if(skip1) {
            cattDevice.skip()
            pauseExecution(thePause)
        }
        if(status1) {
            cattDevice.status()
            pauseExecution(thePause)
        }
        if(stop1) {
            cattDevice.off()
            pauseExecution(thePause)
        }
        if(volume1) {
            cattDevice.volume(castVolume1)
            pauseExecution(thePause)
        }
        if(volumedown1) {
            cattDevice.volumeDown(castVolumeDown1)
            pauseExecution(thePause)
        }
        if(volumeup1) {
            cattDevice.volumeUp(castVolumeUp1)
            pauseExecution(thePause)
        }
        if(write_config1) {
            cattDevice.write_config(castWrite_config1)
            pauseExecution(thePause)
        }
        secondHandler()
    }
}

def secondHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        thePause = secToPause2to3 * 1000
        if(logEnable) log.debug "In secondHandler - pause between commands: ${thePause}"
        if(add2) {
            cattDevice.add(castAdd2)
            pauseExecution(thePause)
        }
        if(cast2) {
            cattDevice.cast(castURL2)
            pauseExecution(thePause)
        }
        if(cast_site2) {
            cattDevice.on(castWebsite2)
            pauseExecution(thePause)
        }
        if(clear2) {
            cattDevice.clear()
            pauseExecution(thePause)
        }
        if(ffwd2) {
            cattDevice.ffwd(castFfwd2)
            pauseExecution(thePause)
        }
        if(info2) {
            cattDevice.info()
            pauseExecution(thePause)
        }
        if(pause2) {
            cattDevice.pause()
            pauseExecution(thePause)
        }
        if(play2) {
            cattDevice.play()
            pauseExecution(thePause)
        }
        if(remove2) {
            cattDevice.remove(castRemove2)
            pauseExecution(thePause)
        }
        if(restore2) {
            cattDevice.restore()
            pauseExecution(thePause)
        }
        if(rewind2) {
            cattDevice.rewind(castRewind2)
            pauseExecution(thePause)
        }
        if(save2) {
            cattDevice.save()
            pauseExecution(thePause)
        }
        if(scan2) {
            cattDevice.scan()
            pauseExecution(thePause)
        }
        if(seek2) {
            cattDevice.seek()
            pauseExecution(thePause)
        }
        if(skip2) {
            cattDevice.skip()
            pauseExecution(thePause)
        }
        if(status2) {
            cattDevice.status()
            pauseExecution(thePause)
        }
        if(stop2) {
            cattDevice.off()
            pauseExecution(thePause)
        }
        if(volume2) {
            cattDevice.volume(castVolume2)
            pauseExecution(thePause)
        }
        if(volumedown2) {
            cattDevice.volumeDown(castVolumeDown2)
            pauseExecution(thePause)
        }
        if(volumeup2) {
            cattDevice.volumeUp(castVolumeUp2)
            pauseExecution(thePause)
        }
        if(write_config2) {
            cattDevice.write_config(castWrite_config2)
            pauseExecution(thePause)
        }
        thirdHandler()
    }
}

def thirdHandler() {
    checkEnableHandler()
    if(pauseApp || state.eSwitch) {
        log.info "${app.label} is Paused or Disabled"
    } else {
        thePause = 1000
        if(logEnable) log.debug "In thirdHandler - pause between commands: ${thePause}"
        if(add3) {
            cattDevice.add(castAdd3)
            pauseExecution(thePause)
        }
        if(cast3) {
            cattDevice.cast(castURL3)
            pauseExecution(thePause)
        }
        if(cast_site3) {
            cattDevice.on(castWebsite3)
            pauseExecution(thePause)
        }
        if(clear3) {
            cattDevice.clear()
            pauseExecution(thePause)
        }
        if(ffwd3) {
            cattDevice.ffwd(castFfwd3)
            pauseExecution(thePause)
        }
        if(info3) {
            cattDevice.info()
            pauseExecution(thePause)
        }
        if(pause3) {
            cattDevice.pause()
            pauseExecution(thePause)
        }
        if(play3) {
            cattDevice.play()
            pauseExecution(thePause)
        }
        if(remove3) {
            cattDevice.remove(castRemove3)
            pauseExecution(thePause)
        }
        if(restore3) {
            cattDevice.restore()
            pauseExecution(thePause)
        }
        if(rewind3) {
            cattDevice.rewind(castRewind3)
            pauseExecution(thePause)
        }
        if(save3) {
            cattDevice.save()
            pauseExecution(thePause)
        }
        if(scan3) {
            cattDevice.scan()
            pauseExecution(thePause)
        }
        if(seek3) {
            cattDevice.seek()
            pauseExecution(thePause)
        }
        if(skip3) {
            cattDevice.skip()
            pauseExecution(thePause)
        }
        if(status3) {
            cattDevice.status()
            pauseExecution(thePause)
        }
        if(stop3) {
            cattDevice.off()
            pauseExecution(thePause)
        }
        if(volume3) {
            cattDevice.volume(castVolume3)
            pauseExecution(thePause)
        }
        if(volumedown3) {
            cattDevice.volumeDown(castVolumeDown3)
            pauseExecution(thePause)
        }
        if(volumeup3) {
            cattDevice.volumeUp(castVolumeUp3)
            pauseExecution(thePause)
        }
        if(write_config3) {
            cattDevice.write_config(castWrite_config3)
            pauseExecution(thePause)
        }	
    }
}

def appButtonHandler(buttonPressed) {
    state.whichButton = buttonPressed
    thePause = 1000
    if(logEnable) log.debug "In testButtonHandler (${state.version}) - Button Pressed: ${state.whichButton}"
    
    try {
        if(state.whichButton == "testBtn1" && add1) cattDevice.add(castAdd1)
        if(state.whichButton == "testBtn1" && cast1) cattDevice.cast(castURL1)
        if(state.whichButton == "testBtn1" && cast_site1) cattDevice.add(castWebsite1)
        if(state.whichButton == "testBtn1" && clear1) cattDevice.clear()
        if(state.whichButton == "testBtn1" && ffwd1) cattDevice.ffwd(castFfwd1)
        if(state.whichButton == "testBtn1" && info1) cattDevice.info()
        if(state.whichButton == "testBtn1" && pause1) cattDevice.pause()
        if(state.whichButton == "testBtn1" && play1) cattDevice.play()
        if(state.whichButton == "testBtn1" && remove1) cattDevice.remove(castRemove1)
        if(state.whichButton == "testBtn1" && restore1) cattDevice.restore()
        if(state.whichButton == "testBtn1" && rewind1) cattDevice.rewind(castRewind1)
        if(state.whichButton == "testBtn1" && save1) cattDevice.save()
        if(state.whichButton == "testBtn1" && scan1) cattDevice.scan()
        if(state.whichButton == "testBtn1" && seek1) cattDevice.seek(castSeek1)
        if(state.whichButton == "testBtn1" && skip1) cattDevice.skip()
        if(state.whichButton == "testBtn1" && status1) cattDevice.status()
        if(state.whichButton == "testBtn1" && stop1) cattDevice.stop()
        if(state.whichButton == "testBtn1" && volume1) cattDevice.volume(castVolume1)
        if(state.whichButton == "testBtn1" && volumedown1) cattDevice.volumedown(castVolumeDown1)
        if(state.whichButton == "testBtn1" && volumeup1) cattDevice.volumeup(castVolumeUp1)
        if(state.whichButton == "testBtn1" && write_config1) cattDevice.write_config(castWrite_config1)
        
        if(state.whichButton == "testBtn2" && add2) cattDevice.add(castAdd2)
        if(state.whichButton == "testBtn2" && cast2) cattDevice.cast(castURL2)
        if(state.whichButton == "testBtn2" && cast_site2) cattDevice.add(castWebsite2)
        if(state.whichButton == "testBtn2" && clear2) cattDevice.clear()
        if(state.whichButton == "testBtn2" && ffwd2) cattDevice.ffwd(castFfwd2)
        if(state.whichButton == "testBtn2" && info2) cattDevice.info()
        if(state.whichButton == "testBtn2" && pause2) cattDevice.pause()
        if(state.whichButton == "testBtn2" && play2) cattDevice.play()
        if(state.whichButton == "testBtn2" && remove2) cattDevice.remove(castRemove2)
        if(state.whichButton == "testBtn2" && restore2) cattDevice.restore()
        if(state.whichButton == "testBtn2" && rewind2) cattDevice.rewind(castRewind2)
        if(state.whichButton == "testBtn2" && save2) cattDevice.save()
        if(state.whichButton == "testBtn2" && scan2) cattDevice.scan()
        if(state.whichButton == "testBtn2" && seek2) cattDevice.seek(castSeek2)
        if(state.whichButton == "testBtn2" && skip2) cattDevice.skip()
        if(state.whichButton == "testBtn2" && status2) cattDevice.status()
        if(state.whichButton == "testBtn2" && stop2) cattDevice.stop()
        if(state.whichButton == "testBtn2" && volume2) cattDevice.volume(castVolume2)
        if(state.whichButton == "testBtn2" && volumedown2) cattDevice.volumedown(castVolumeDown2)
        if(state.whichButton == "testBtn2" && volumeup2) cattDevice.volumeup(castVolumeUp2)
        if(state.whichButton == "testBtn2" && write_config2) cattDevice.write_config(castWrite_config2)
        
        if(state.whichButton == "testBtn3" && add3) cattDevice.add(castAdd3)
        if(state.whichButton == "testBtn3" && cast3) cattDevice.cast(castURL3)
        if(state.whichButton == "testBtn3" && cast_site3) cattDevice.add(castWebsite3)
        if(state.whichButton == "testBtn3" && clear3) cattDevice.clear()
        if(state.whichButton == "testBtn3" && ffwd3) cattDevice.ffwd(castFfwd3)
        if(state.whichButton == "testBtn3" && info3) cattDevice.info()
        if(state.whichButton == "testBtn3" && pause3) cattDevice.pause()
        if(state.whichButton == "testBtn3" && play3) cattDevice.play()
        if(state.whichButton == "testBtn3" && remove3) cattDevice.remove(castRemove3)
        if(state.whichButton == "testBtn3" && restore3) cattDevice.restore()
        if(state.whichButton == "testBtn3" && rewind3) cattDevice.rewind(castRewind3)
        if(state.whichButton == "testBtn3" && save3) cattDevice.save()
        if(state.whichButton == "testBtn3" && scan3) cattDevice.scan()
        if(state.whichButton == "testBtn3" && seek3) cattDevice.seek(castSeek3)
        if(state.whichButton == "testBtn3" && skip3) cattDevice.skip()
        if(state.whichButton == "testBtn3" && status3) cattDevice.status()
        if(state.whichButton == "testBtn3" && stop3) cattDevice.stop()
        if(state.whichButton == "testBtn3" && volume3) cattDevice.volume(castVolume3)
        if(state.whichButton == "testBtn3" && volumedown3) cattDevice.volumedown(castVolumeDown3)
        if(state.whichButton == "testBtn3" && volumeup3) cattDevice.volumeup(castVolumeUp3)
        if(state.whichButton == "testBtn3" && write_config3) cattDevice.write_config(castWrite_config3)
        
	    pauseExecution(thePause)
    } catch (e) { 
        log.warn "CATT Director - ${e}"
    }
      
    try {
        if(state.whichButton == "testBtnSequence") {
            if(logEnable) log.debug "In testButtonHandler - Testing sequence"
	    	firstHandler()
	    	pauseExecution(thePause)
	    }
    } catch (e) {
        log.warn "CATT Director - ${e}"
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
            eSwitch = it.currentValue("switch")
            if(eSwitch == "on") { state.eSwitch = true }
        }
    }
}

def setDefaults(){
	if(logEnable) log.debug "In setDefaults..."
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
