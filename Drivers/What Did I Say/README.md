# What Did I Say
<b>Design Usage:</b><br>
This driver formats Speech data to be displayed on Hubitat's Dashboards.<br><br>

<b>New Install:</b><br>
* Copy the Driver code from GitHub into a ‘New driver’ under the ‘Drivers Code’ menu then click ‘Done’
* Go to ‘Devices’ and create a new 'Add Virtual Device'
* Name your new device something like 'What Did You Say'
* Come up with a new 'Device Network ID'
* Select ‘What Did You Say’ under ‘Type’
* Click 'Save Device'
<br><br>

<b>Setup Options:</b><br>
Commands Section<br>
* Ignore everything in this section
<br>
Preferences<br>
* Enable logging - Turns the device logging on and off<br>
* Font Size - Changes the size of the font used on the tile 'bigger number = bigger font'<br>
* How many lines to display - Only two options here - 5 and 10<br>
* Time Selection - Off for 24h - On for 12h<br>
* Clear All Speech Data - Clears out any stored data in the speech device<br>
<br><br>

<b>Usage:</b><br>
In any app that has a speech option, simply select the 'What Did I Say' device in addition to your normally selected speech device.  Whatever is sent to your speech device will also be sent to this device.
<br><br>
Note: if you find an app that doesn't work with this driver, please drop me a message and I'll see what I can do!
<br><br>
Now all you have to do is add this device to one of your dashboards to see what was said!<br>
Add a new tile with the following selections
- Pick a device = What Did I Say
- Pick a template = attribute
- 3rd box = whatDidISay

If you are also using the Follow Me app, the following attributes will also be available.
- speakerStatus1-5

<br><br>
Bryan<br>
@BPTWorld
