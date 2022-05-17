# COVID-19-Statistics

This is a project for Android written for SDK 24+.  The prupose of this project is to show statistics of COVID-19 cases in specific locations.  Users will be able to select a location and add it to their list of tracked locations.  The main entry shows a summary of global statistics and a list of the user's tracked locations with their individual stats.  

# Motivation
To create an app to show summary COVID-19 data from various places from all over the world.  There were a bunch of desparate apps out there that did some of the features but not all of them.  This app combines the ability to select locations from all over the world and see their COVID-19 information about new and active cases, positivity and fatality rates and hospitalizations.  It also gives you graphing for a 7 day trend and overall across time. 

# Installation
This app is not available in any android store so it must be sideloaded.  There is a setting in Android Security settings to allow installation of apps from unknown sources.  This must be enabled to install.  On most Android devices, if you try to install this app it will prompt you about the setting and give you an option to go directly to the setting and change it. Opening the `.apk` file on an Android device will start the installation process.

# Sources
Locations can be as broad as a country or as specific as a municipality depending on the area's reporting.  This application takes COVID-19 statistics from COVID-19 Statistics API (https://covid-api.com/) which is based on public data by Johns Hopkins CSSE (https://github.com/CSSEGISandData/COVID-19). U.S. statistics taken from COVID Act Now (https://covidactnow.org/)


Color scheme recommendations from:   
https://www.schemecolor.com/coronavirus-covid-19-color-scheme.php 
https://www.seas.harvard.edu/office-communications/brand-style-guide/color-palette  

# Version History

**1.6.5.1** - 2022-05-19  
Changed display on main location stat list to show actual numbers rather than difference between previous and current day.  Arrow indicators will show whether the number is an increase, decrease or no change.  

**1.6.4.1** - 2021-09-23  
Lots of updates under the hood.  Better handling of UI vs. background threads. Added more info to detail view.  Changed data source for US and its locations to use COVID Act Now.  

**1.4.1.1** - 2020-07-31  
Resolved Issues 15 and 17.  Other clean-up tasks.

**1.4.0.0** - 2020-07-14   
Added hospitalization and ICU statistics for US and US States where available. Not all states are reporting either or both statistics. International statistics for hopaitalizatiins and ICU are not yet freely available.

**1.3.1.0** - 2020-06-06  
Resolved issues 11 and 12 

**1.2.4.0** - 2020-05-25  
Added confiramtion when removing a location

**1.2.3.0** - 2020-05-21  
Added pull-down refresh to manually fetch global stats and check for location updates

# License
GPL-3.0 License

https://github.com/gregchak/COVID-19-Statistics/blob/master/LICENSE
