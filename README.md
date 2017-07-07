# weatherstation-tinkerforge
TinkerForge based weatherstation with multiple options.

Will be used as a IoT learning project which should run on OpenShift Origin.
With lots of pods for AMQ, Database(not sure if I want a DB), Multiple Pods running multiple sensor processes, website for presenting the data. Maybe some Apache Camel/Red Hat Fuse routes to do some intersting stuff with the data.
Also trying to use data from LoRa nodes (when I finally have my Gateway up-and-running).

Here you can find the details of the Hardware used www.tinkerforge.com also the pre-compiled Tinkerforge.jar and other code can be found here. Very nice stuff for experimenting with IoT, focus on development not on electronics.

First try with GitHub and Java. Very simple program to run my 2 TinkerForge based environmental sensor setup.

Setup 1; WeatherStation_Inside.
TinkerForge Weatherstation with Humidity, Barometer(Air-pressure + Temperature) Ambient light and LCD screen.

Setup 2; WeatherStation_Outside.
TinkerForge Sensorstation with UV-Light, Temperature, Humidity and Sound sensor.

All data read is writen to Output files, one for each sensor used.
Output dir is hardcoded - /appl/weerstation/data/.. (Line 153-Outside / 169-Inside in the source code).
Also IP-adres to connect to the sensor array is hardcoded (Line 183-Outside / 167-Inside in the source code).

build;
javac -cp Tinkerforge.jar:. Weerstation_Inside.java.
javac -cp Tinkerforge.jar:. Weerstation_Outside.java.

After that copy the Tinkerforge.jar to a folder and run it from there with;
java -cp Tinkerforge.jar:. Weerstation_Inside.
java -cp Tinkerforge.jar:. Weerstation_Outside.


Things To-Do (in random order and sure to change);
- Put the Data as an MQTT message on a queue using AMQ as the Broker.
- Use parameters for IP-adres, output folder.
- Make it flexible enough to select the sensors attached during the build.
- Create Dockerfile. Run the Java process in a Docker container.
- Create OpenShift Origin project.
  - Pods based on Docker-files/containers?
  - Set it up in Openshift, using GIT, build it from source or something like that.
- Use Fuse to do some stuff with the MQTT messages.

