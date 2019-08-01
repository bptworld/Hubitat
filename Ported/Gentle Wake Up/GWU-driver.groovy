/**
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
    definition (name: "Gentle Wake Up Controller", namespace: "smartthings", author: "SmartThings") {
        capability "Switch"
        capability "Timed Session"

        attribute "percentComplete", "number"

        command "setPercentComplete", ["number"]
    }

    simulator {
        // TODO: define status and reply messages here
    }
}

// parse events into attributes
def parse(description) {
    log.debug "Parsing '${description}'"
    // TODO: handle 'switch' attribute
    // TODO: handle 'level' attribute
    // TODO: handle 'sessionStatus' attribute
    // TODO: handle 'timeRemaining' attribute

}

// handle commands
def on() {
    log.debug "Executing 'on'"
    startDimming()
}

def off() {
    log.debug "Executing 'off'"
    stopDimming()
}

def setTimeRemaining(percentComplete) {
    log.debug "Executing 'setTimeRemaining' to ${percentComplete}% complete"
    parent.jumpTo(percentComplete)
}

def start() {
    log.debug "Executing 'start'"
    startDimming()
}

def stop() {
    log.debug "Executing 'stop'"
    stopDimming()
}

def pause() {
    log.debug "Executing 'pause'"
    // TODO: handle 'pause' command
}

def cancel() {
    log.debug "Executing 'cancel'"
    stopDimming()
}

def startDimming() {
    log.trace "startDimming"
    log.debug "parent: ${parent}"
    parent.start("controller")
}

def stopDimming() {
    log.trace "stopDimming"
    log.debug "parent: ${parent}"
    parent.stop("controller")
}

def controllerEvent(eventData) {
    sendEvent(eventData)
    if (eventData.name == "sessionStatus") {
    	if (eventData.value == "running") {
            //Set Switch to ON to support Samsung Connect
            sendEvent(name: "switch", value: "on")
    	} else {
            // Set Switch to OFF to support Samsung Connect
            sendEvent(name: "switch", value: "off")
        }
    }
}
