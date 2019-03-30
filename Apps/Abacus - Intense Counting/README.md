# ABACUS - Intense Counting
<b>Design Usage:</b><br>
Count how many times a Device is triggered. Displays Daily, Weekly, Monthly and Yearly counts!<br><br>

<b>New Install:</b><br>
* Copy the Parent code from GitHub into a ‘New App’ under the ‘Apps Code’ menu then click ‘Done’
* Copy the Child code from GitHub into a second ‘New App’ & also save this
* Go to ‘Apps’
* Click ‘Load New Apps’
* Select ‘Abacus’ under ‘User Apps’
* Click 'Done'
* Go back to Apps and open 'Abacus - Intense Counting'
<br>
You can now create new child apps directly from here.<br><br>
<b>*** HOW TO DISPLAY THE DATA ON A DASHBOARD TILE ***</b>
<b>Design Usage:</b><br>
This driver formats the data to be displayed on Hubitat's Dashboards.<br><br>
<b>New Install:</b><br>
* Copy the Driver code from GitHub into a ‘New driver’ under the ‘Drivers Code’ menu then click ‘Done’<br>
* Go to ‘Devices’ and create a new 'Add Virtual Device'<br>
* Name your new device something like 'Abaucs Intense Counting Tile'<br>
* Come up with a new 'Device Network ID'<br>
* Select ‘Abacus Intense Counting Tile’ under ‘Type’<br>
* Click 'Save Device'<br>
<br><br>
<b>Setup Options:</b><br>
Commands Section<br>
* Ignore everything in this section
<br>
Preferences<br>
* Enable logging - Turns the device logging on and off<br>
* Font Size - Changes the size of the font used on the tile 'bigger number = bigger font'<br>
<br><br>

<b>Usage:</b><br>
Now all you have to do is add this device to one of your dashboards to see the data anytime!<br>
Add a new tile with the following selections
- Pick a device = Abacus Intense Counting
- Pick a template = attribute
- 3rd box = EACH attribute holds 5 lines of data. So mulitple boxes are now necessary. The options are switchDevice1-5, contactDevice1-5, motionDevice1-5 OR thermostatDevice1

<br>
Bryan<br>
@BPTWorld
