
Lighting Effects Child
chat_bubble_outline
more_vert
 
Dashboards
Devices
Apps
Settings
Advanced
codeApps Code
codeDrivers Code
System Events
Logs
Lighting Effects Child Import       HelpDeleteOAuthSave
501
        "color02": [hue: parent.msgColor02Hue, saturation: parent.msgColor02Sat, level: parent.msgColor02Lev],
502
        "color03": [hue: parent.msgColor03Hue, saturation: parent.msgColor03Sat, level: parent.msgColor03Lev],
503
        "color04": [hue: parent.msgColor04Hue, saturation: parent.msgColor04Sat, level: parent.msgColor04Lev],
504
        "color05": [hue: parent.msgColor05Hue, saturation: parent.msgColor05Sat, level: parent.msgColor05Lev],
505
        "color06": [hue: parent.msgColor06Hue, saturation: parent.msgColor06Sat, level: parent.msgColor06Lev],
506
        "color07": [hue: parent.msgColor07Hue, saturation: parent.msgColor07Sat, level: parent.msgColor07Lev],
507
        "color08": [hue: parent.msgColor08Hue, saturation: parent.msgColor08Sat, level: parent.msgColor08Lev],
508
        "color09": [hue: parent.msgColor09Hue, saturation: parent.msgColor09Sat, level: parent.msgColor09Lev],
509
        "color10": [hue: parent.msgColor10Hue, saturation: parent.msgColor10Sat, level: parent.msgColor10Lev],
510
        "color11": [hue: parent.msgColor11Hue, saturation: parent.msgColor11Sat, level: parent.msgColor11Lev],
511
        "color12": [hue: parent.msgColor12Hue, saturation: parent.msgColor12Sat, level: parent.msgColor12Lev],
512
        "color13": [hue: parent.msgColor13Hue, saturation: parent.msgColor13Sat, level: parent.msgColor13Lev],
513
        "color14": [hue: parent.msgColor14Hue, saturation: parent.msgColor14Sat, level: parent.msgColor14Lev],
514
        "color15": [hue: parent.msgColor15Hue, saturation: parent.msgColor15Sat, level: parent.msgColor15Lev],
515
    ]
516
    if((level > 100) || (level < 1)) level=100
517
    def newcolor = colorPallet."${color}"
518
    if(logEnable) log.debug "${color} = ${newcolor}"
519
    newcolor.level = level                  
520
    lights*.setColor(newcolor)
521
    if(logEnable) log.debug "Setting Color = ${color} on: ${lights}"        
522
    }
523
}
524
​
525
// ********** Normal Stuff **********
526
​
527
def setDefaults(){                                  
528
  if(logEnable) log.debug "Initialising defaults..."
529
    if(pauseApp == null){pauseApp = false}
530
    if(logEnable == null){logEnable = false}
531
}
532
​
533
def getImage(type) {                                    // Modified from @Stephack Code
534
    def loc = "<img src=https://raw.githubusercontent.com/bptworld/Hubitat/master/resources/images/"
535
    if(type == "Blank") return "${loc}blank.png height=40 width=5}>"
536
}
537
​
538
def getFormat(type, myText=""){                         // Modified from @Stephack Code
539
    if(type == "header-green") return "<div style='color:#ffffff;font-weight: bold;background-color:#81BC00;border: 1px solid;box-shadow: 2px 3px #A9A9A9'>${myText}</div>"
540
    if(type == "line") return "\n<hr style='background-color:#1A77C9; height: 1px; border: 0;'></hr>"
541
    if(type == "title") return "<div style='color:blue;font-weight: bold'>${myText}</div>"
542
}
543
​
544
def display() {
545
    section() {
546
        paragraph getFormat("line")
547
        input "pauseApp", "bool", title: "Pause App", required: true, submitOnChange: true, defaultValue: false
548
        if(pauseApp) {paragraph "<font color='red'>App is Paused</font>"}
Terms of Service
Documentation
Community
Support
Copyright 2019 Hubitat, Inc.
