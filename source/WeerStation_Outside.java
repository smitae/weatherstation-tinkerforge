import com.tinkerforge.IPConnection;
import com.tinkerforge.BrickletHumidity;
import com.tinkerforge.BrickletTemperature;
import com.tinkerforge.BrickletSoundIntensity;
import com.tinkerforge.BrickletUVLight;

import static java.nio.file.StandardOpenOption.*;
import java.nio.file.*;
import java.sql.Timestamp;
import java.io.*;
import java.util.*;

class WeatherListener_Outside implements IPConnection.EnumerateListener,
                                 IPConnection.ConnectedListener,
                                 BrickletHumidity.HumidityListener,
				 BrickletUVLight.UVLightListener,
                                 BrickletTemperature.TemperatureListener,
				 BrickletSoundIntensity.IntensityListener {
	private IPConnection ipcon = null;
	private BrickletHumidity brickletHumidity = null;
	private BrickletUVLight brickletUVLight = null;
	private BrickletTemperature brickletTemperature = null;
	private BrickletSoundIntensity brickletIntensity = null;

	public WeatherListener_Outside(IPConnection ipcon) {
		this.ipcon = ipcon;
	}

	
	public void humidity(int humidity) {
		// Write to file
		String humidityValue = Integer.toString(humidity);
		writetoFile("Humidity_Outside" , humidityValue);	
     	}

	
	public void uvLight(long uvLight) {
		// Write to file
		String uvlightValue = Long.toString(uvLight);
		writetoFile("UV_light_Outside" , uvlightValue);	
     	}

	public void temperature(short temperature) {
		// Write to file
		String temperatureValue = Integer.toString(temperature);
		writetoFile("Temperature_Outside" , temperatureValue);	
    	}


	public void intensity(int intensity) {
		// Write to file
		String intensityValue = Integer.toString(intensity);
		writetoFile("Sound_Intensity_Outside" , intensityValue);
	}


	
	public void enumerate(String uid, String connectedUid, char position,
	                      short[] hardwareVersion, short[] firmwareVersion,
	                      int deviceIdentifier, short enumerationType) {
		if(enumerationType == IPConnection.ENUMERATION_TYPE_CONNECTED ||
		   enumerationType == IPConnection.ENUMERATION_TYPE_AVAILABLE) {
			if(deviceIdentifier == BrickletHumidity.DEVICE_IDENTIFIER) {
				try {
					brickletHumidity = new BrickletHumidity(uid, ipcon);
					brickletHumidity.setHumidityCallbackPeriod(30000);
					brickletHumidity.addHumidityListener(this);
					System.out.println("Humidity Outside initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletHumidity = null;
					System.out.println("Humidity Outside init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletUVLight.DEVICE_IDENTIFIER) {
                               try {
                                        brickletUVLight = new BrickletUVLight(uid, ipcon);
                                        brickletUVLight.setUVLightCallbackPeriod(30000);
                                        brickletUVLight.addUVLightListener(this);
                                        System.out.println("UV-Light initialized");
                                } catch(com.tinkerforge.TinkerforgeException e) {
                                        brickletUVLight = null;
                                        System.out.println("UV-Light init failed: " + e);
                                }
			} else if(deviceIdentifier == BrickletSoundIntensity.DEVICE_IDENTIFIER) {
				try {
					brickletIntensity = new BrickletSoundIntensity(uid, ipcon);
					brickletIntensity.setIntensityCallbackPeriod(30000);
					brickletIntensity.addIntensityListener(this);
					System.out.println("Sound Intensity initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletIntensity = null;
					System.out.println("Sound Intensity init failed: " + e);
				}
			} else if(deviceIdentifier == BrickletTemperature.DEVICE_IDENTIFIER) {
				try {
					brickletTemperature = new BrickletTemperature(uid, ipcon);
					brickletTemperature.setTemperatureCallbackPeriod(30000);
					brickletTemperature.addTemperatureListener(this);
					System.out.println("Temperature Outside initialized");
				} catch(com.tinkerforge.TinkerforgeException e) {
					brickletTemperature = null;
					System.out.println("Temperature Outside init failed: " + e);
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
		//String logfileDir = "./"; 				// Testing only
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


public class WeerStation_Outside {
	private static final String HOST = "192.168.2.198";
	private static final int PORT = 4223;
	private static IPConnection ipcon = null;
	private static WeatherListener_Outside weatherListener = null;

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
				Thread.sleep(1000);
			} catch(InterruptedException ei) {
			}
		}

		weatherListener = new WeatherListener_Outside(ipcon);
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
