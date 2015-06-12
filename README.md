# WatchDist
WatchDist is a watch hours (a.k.a. guard duty) distribution application that runs on desktop and can be used in 
military or police bases to create a fair distribution of watch duties.
 
The project uses [**Maven**](https://maven.apache.org/) as build tool, [**Java 8**](http://www.oracle.com/technetwork/java/javase/overview/java8-2100321.html) 
and [**JavaFX 8**](https://docs.oracle.com/javase/8/javafx/api/toc.htm) as UI library, [**SQLite**](https://www.sqlite.org/
) to store the 
data 
and [**ORMLite**](http://ormlite.com/) for mapping 
objects to the 
database. [**Apache POI**](https://poi.apache.org/) is used to export table to Excel file.
  
Other libraries used are [**ControlsFX**](http://fxexperience.com/controlsfx/), Google's ~~guava~~ (replaced by Java 8's [**Stream API**](https://docs.oracle.com/javase/8/docs/api/java/util/stream/package-summary.html)), ~~joda-time~~ 
(replaced by Java 8's [**Clock API**](https://docs.oracle.com/javase/8/docs/api/java/time/Clock.html)), [**slf4j**](http://www.slf4j.org/) and [**logback**](http://logback.qos.ch/).
 
Watch hours and durations are ~~currently fixed~~ adjustable (as 1.0), and their values (score added to the soldier) can be edited.
A soldier's availabe hours can be changed for a week, and a soldier can be marked as sergeant and this soldier will 
have no watches but still be getting score every day.

Distributions including past ones can be viewed and edited, they can be exported to an excel file with a user-edited 
template. 

## Build
The project uses maven as build tool and [Javafx Maven Plugin](https://github.com/javafx-maven-plugin) to create 
runnable JARs/executables.  
Maven should be using [**JDK 8**](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) to be able to build the project. 

To create native installers, in the project root directory, execute the maven goal

`mvn clean jfx:native` 

If you have [Inno Setup](http://www.jrsoftware.org/isinfo.php) executables in the PATH, this command will create an `
.exe` installer for windows.  
If you have [WIX Toolset](http://wixtoolset.org/) executables in PATH, an `.msi` installer will be generated.

These native installers can be found under `<project.dir>/target/jfx/native/`.  
They will contain a bundled JRE with so it will not be necessary to install Java runtimes to the computers which the 
app will run on.

To create runnable JAR, execute the maven goal

`mvn clean jfx:jar` 

This JAR can be found under `<project.dir>/target/jfx/app/`.

## Installation
If a native installer is used, just follow the install steps.  
If a runnable JAR is used, just double-click to the jar or
 use `java -jar <jarname>.jar` (requires JRE 8 installed).

## Usage
Every other screen can be accessed from the main screen. Pretty simple to use.

1. Create/edit the soldiers.  
2. Create/edit the watch points.  
3. Edit the watch values.  
4. Edit the excel template used on export as excel file feature.    
5. Choose a date and perform a distribution.  
6. Edit distribution manually if necessary for fine-tuning.  
7. Approve the distribution.  
8. Export the distribution as excel file if required (to print etc.)  
9. Edit an already approved distribution for changes, it will do the necessary changes on soldier points (scores).  
10. You can reset/export excel template and database from the management screen.  

## History
v1.0: Initial release.

## Credits
Thanks to Engin Erkan and Fatih Öztürk for their help and suggestions.

## License
See [LICENSE](LICENSE).
