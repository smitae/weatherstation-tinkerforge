import com.tinkerforge.IPConnection;
import com.tinkerforge.BrickletAmbientLight;
import com.tinkerforge.BrickletAmbientLightV2;
import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletBarometer;

import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.io.*;
import java.util.*;

class WeatherListener_Inside implements IPConnection.EnumerateListener,
                                 IPConnection.ConnectedListener,
                                 BrickletAmbientLight.IlluminanceListener,
                                 BrickletAmbientLightV2.IlluminanceListener,
                                 BrickletHumidity.HumidityListener,
                                 BrickletBarometer.AirPressureListener {
	private IPConnection ipcon = null;
	private BrickletAmbientLight brickletAmbientLight = null;
	private BrickletAmbientLightV2 brickletAmbientLightV2 = null;
	private BrickletHumidity brickletHumidity = null;
	private BrickletBarometer brickletBarometer = null;

	public WeatherListener_Inside(IPConnection ipcon) {
		this.ipcon = ipcon;
	}

	
	public void illuminance(int illuminance) {
			// Write to file
			String illuminanceValue = Integer.toString(illuminance);
			writetoFile("Illumina" , illuminanceValue);
    }

	
	public void illuminance(long illuminance) {
			// Write to file
			String illuminanceValue = Long.toString(illuminance);
			writetoFile("Illumina" , illuminanceValue);	
    }

	
	public void humidity(int humidity) {
			// Write to file
			String humidityValue = Integer.toString(humidity);
			writetoFile("Humidity_Inside" , humidityValue);	
    }

	
	public void airPressure(int airPressure) {
			// Write to file
			String airPressureValue = Integer.toString(airPressure);
			writetoFile("Airpressure" , airPressureValue);
						
			// Temperature handling on barometer bricklet	
			if(brickletBarometer != null) {
				int temperature;
				try {
					temperature = brickletBarometer.getChipTemperature();
				} catch(com.tinkerforge.TinkerforgeException e) {
					System.out.println("Could not get temperature: " + e);
					return;
				}
				// Write to file
				String airTemperatureValue = Integer.toString(temperature);
				writetoFile("Temperature_Inside" , airTemperatureValue);	
			}
	}

	
	public void enumerate(String uid, String connectedUid, char position,
	                      short[] hardwareVersion, short[] firmwareVersion,
	                      int deviceIdentifier, short enumerationType) {
		if(enumerationType == IPConnection.ENUMERATION_TYPE_CONNECTED ||
		   enumerationType == IPConnection.ENUMERATION_TYPE_AVAILABLE) {
			if(deviceIdentifier == BrickletAmbientLight.DEVICE_IDENTIFIER) {
				try {
					brickletAmbientLight = new BrickletAmbientLight(uid, ipcon);
					brickletAmbientLight.setIlluminanceCallbackPeriod(30000);
					brickletAmbientLight.addIlluminanceListener(this);
					System.out.println("Ambient Light initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletAmbientLight = null;
					System.out.println("Ambient Light init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletAmbientLightV2.DEVICE_IDENTIFIER) {
				try {
					brickletAmbientLightV2 = new BrickletAmbientLightV2(uid, ipcon);
					brickletAmbientLightV2.setConfiguration(BrickletAmbientLightV2.ILLUMINANCE_RANGE_64000LUX,
					                                        BrickletAmbientLightV2.INTEGRATION_TIME_200MS);
					brickletAmbientLightV2.setIlluminanceCallbackPeriod(30000);
					brickletAmbientLightV2.addIlluminanceListener(this);
					System.out.println("Ambient Light 2.0 initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletAmbientLightV2 = null;
					System.out.println("Ambient Light 2.0 init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletHumidity.DEVICE_IDENTIFIER) {
				try {
					brickletHumidity = new BrickletHumidity(uid, ipcon);
					brickletHumidity.setHumidityCallbackPeriod(30000);
					brickletHumidity.addHumidityListener(this);
					System.out.println("Humidity initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletHumidity = null;
					System.out.println("Humidity Inside init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletBarometer.DEVICE_IDENTIFIER) {
				try {
					brickletBarometer = new BrickletBarometer(uid, ipcon);
					brickletBarometer.setAirPressureCallbackPeriod(30000);
					brickletBarometer.addAirPressureListener(this);
					System.out.println("Barometer initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletBarometer = null;
					System.out.println("Barometer Inside init failed: " + e);
				}
			}
		}
	}

	
	public void connected(short connectedReason) {
		if(connectedReason == IPConnection.CONNECT_REASON_AUTO_RECONNECT) {
			System.out.println("Auto Reconnect");

			while(true) {
				try {
					ipcon.enumerate();
					break;
				} catch(com.tinkerforge.NotConnectedException e) {
				}

				try {
					Thread.sleep(10000);
				} catch(InterruptedException ei) {
				}
			}
		}
	}
	
	
	public void writetoFile( String property, String value ) {
		
		//Prepare log-line
		String text = property + " " + value;
		
		// Get current date and time
		Date date = new Date();
		
        //getTime() returns current time in milliseconds
	    long time = date.getTime();
	    
        //Passed the milliseconds to constructor of Timestamp class 
	    Timestamp timestamp = new Timestamp(time);
		
		//Combine date and time with property and value
		String log_text = timestamp + " " + text+"\n";
		
		//Convert string to byte array
		byte data[] = log_text.getBytes();
		
		//Set log-file name
		String logfileName = property + ".log";
		
		//Set log-dir name
		//String logfileDir = "./"; 					// Testing only
		String logfileDir = "/appl/weerstation/data/";		// Production only
		
		Path p = Paths.get(logfileDir+logfileName);
		try (OutputStream out = new BufferedOutputStream(
				Files.newOutputStream(p, CREATE, APPEND))) {
				out.write(data, 0, data.length);
		} catch (IOException x) {
			System.err.println(x);
		}
	}
}


public class WeerStation_Inside {
	private static final String HOST = "192.168.2.197";
	private static final int PORT = 4223;
	private static IPConnection ipcon = null;
	private static WeatherListener_Inside weatherListener = null;

	public static void main(String args[]) {
		ipcon = new IPConnection();

		while(true) {
			try {
				ipcon.connect(HOST, PORT);
				break;
			} catch(java.net.UnknownHostException e) {
			} catch(java.io.IOException e) {
			} catch(com.tinkerforge.AlreadyConnectedException e) {
			}

			try {
				Thread.sleep(10000);
			} catch(InterruptedException ei) {
			}
		}

		weatherListener = new WeatherListener_Inside(ipcon);
		ipcon.addEnumerateListener(weatherListener);
		ipcon.addConnectedListener(weatherListener);

		while(true) {
			try {
				ipcon.enumerate();
				break;
			} catch(com.tinkerforge.NotConnectedException e) {
			}

			try {
				Thread.sleep(10000);
			} catch(InterruptedException ei) {
			}
		}

		try {
			while (true){
				Thread.sleep(10000);
			}
		} catch(InterruptedException e) {
		}
 
		try {
			ipcon.disconnect();
		} catch(com.tinkerforge.NotConnectedException e) {
		}
	}
}
