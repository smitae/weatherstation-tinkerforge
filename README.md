# weatherstation-tinkerforge
TinkerForge based weatherstation with multiple options

Here you can find the details www.tinkerforge.com also the pre-compiled Tinkerforge.jar and other code can be found here.

First try with Git and Java. Very simple program to run my 2 TinkerForge based environmental sensor setup.

Setup 1; WeatherStation_Inside
TinkerForge Weatherstation with Humidity, Barometer(Air-pressure + Temperature) Ambient light and LCD screen.

Setup 2; WeatherStation_Outside
TinkerForge Sensorstation with UV-Light, Temperature, Humidity and Sound sensor.

Output dir is hardcoded - /appl/weerstation/data/.. (Line 153-Outside / 169-Inside in the source code)
Also IP-adres to connect to the sensor array is hardcoded (Line 183-Outside / 123-Inside in the source code)

build;
javac -cp Tinkerforge.jar:. Weerstation_Inside.java
javac -cp Tinkerforge.jar:. Weerstation_Outside.java

After that copy the Tinkerforge.jar to a folder and run it from there with;
java -cp Tinkerforge.jar:. Weerstation_Inside
java -cp Tinkerforge.jar:. Weerstation_Outside


