# COVID-19-Statistics

This is a project for Android written for SDK 24+.  The prupose of this project is to show statistics of COVID-19 cases in specific locations.  Users will be able to select a location and add it to their list of tracked locations.  The main entry shows a summary of global statistics and a list of the user's tracked locations with their individual stats.  

Locations can be as broad as a country or as specific as a municipality depending on the area's reporting.  This application takes COVID-19 statistics from COVID-19 Statistics API (https://covid-api.com/) which is based on public data by Johns Hopkins CSSE (https://github.com/CSSEGISandData/COVID-19). Hospitalization and ICU stats come from The Covid Tracking Project (https://covidtracking.com/).  


Color scheme recommendations from:   
https://www.schemecolor.com/coronavirus-covid-19-color-scheme.php 
https://www.seas.harvard.edu/office-communications/brand-style-guide/color-palette  

# Version History

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

