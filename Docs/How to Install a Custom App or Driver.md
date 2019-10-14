# How to Install an App or Driver from BPTWorld
<b>Design Usage:</b><br>
Basic instructions on installing a custom app from GitHub.<br><br>

<b>New Install: Copying the Code from GitHub</b><br>
* Locate the code that you want to install on GitHub
* Copy the Parent code from GitHub using 1 of 2 methods
  1. Click the 'Raw' button, then use 'ctrl + a' to select all of the code and then 'ctrl + c' to copy it
  2. Click the 'Raw button, then copy the URL by double clicking the URL and then using 'ctrl + c' to copy it
* In Hubitat, select ‘Apps Code’, ‘New App’ and paste in the new code, again there are two ways
  1. Be sure your cursor is active on line 1 in the code, use 'ctrl + v' to paste in the raw code
  2. Click the 'Import' button and paste in the URL copied in the previous step, then click 'Ok' on the warning message
* Be sure to click 'Save' each time you add or replace the code

<i>If the App contains a Child App:</i>
* Follow the same procedure as above to create each child app available

<i>If the App also contains a Custom Driver:</i>
* Follow the same instructions as above to copy the code
* In Hubitat, select ‘Drivers Code’, ‘New Driver’ and paste in the new code, using one of the methods previously described
* Be sure to click 'Save' each time you add or replace the code
* Do this for each Driver available on GitHub

<b>New Install: Loading the App in Hubitat</b><br>
* In Hubitat, go to 'Apps'
* Click ‘Add User Apps’
* Select ‘(the app you just created)’  ie. 'Follow Me'
* Click 'Done', this will install the App and bring you back to the 'Apps' list
* Now just scroll to the new App and click on it to open it

<b>That's it!</b><br>
<i>Don't worry, it gets easier each time you do it!</i>
