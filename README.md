[![](https://jitpack.io/v/datalogic/androidpos-core.svg)](https://jitpack.io/#datalogic/androidpos-core)
# Description
This project contains the core of the **AndroidPOS** project. AndroidPOS is the porting on Android of the [Datalogic JavaPOS library](https://datalogic.github.io/javapos/overview/). Implements the [UPOS standard](https://www.omg.org/spec/UPOS/) (v 1.14) to allow Android POS/POE/tablet to communicate with Datalogic scanners and scales.<br>
Communication is supported **USB-OEM only, USB-COM is included but it does not work on all devices**.<br>
The Android module :
- allows to listen for scan or weight events.
- allows Firmware comparison and update.
- allows statistics retrieval from device (both UPOS and Avalanche format).
- allows to send commands to devices.

A sample app, is available [here](https://github.com/datalogic/androidPOS-sampleApp).
# How to use the library
## Add dependency
Add in your root build.gradle at the end of repositories:
~~~gradle
    allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
~~~
Add the following dependency to your gradle file:
~~~gradle
    dependencies {
	        implementation 'com.github.datalogic:androidpos-core:0.1.1-alpha'
	}
~~~
## Integrate a scanner
The generic setup for a scanner is the following:
![Scanner setup](/documentation/scanner-setup.PNG)
Integrate a scanner is very easy. The code is almost the same of **JavaPOS**.<br>
### Open the communication
Instantiate a *Scanner* object, open it, and claim the port:
~~~java
Scanner scanner = new Scanner();
scanner.open(logicalName, context);
scanner.claim(requestListener);
~~~
*requestListener* is an interface, responsible for handling the success or the failure of the port claiming.<br>
**The user must explicitly grant a dedicated permission** to allow the application to claim a port.
### Register for scan events
To register for data events, register an *EventListener*, set to false the auto disable, enable data events and device communication:
~~~java
scanner.addEventListener(listener, EventCallback.EventType.Data);
scanner.setAutoDisable(false);
scanner.setDataEventEnabled(true);
scanner.setDeviceEnabled(true);
~~~
All of this is fully UPOS compliant and identical to JavaPOS.
### Close the communication
To close the communication, disable communication and remove data listener, release the port and close the device:
~~~java
Scanner.setDataEventEnabled(false);
Scanner.setAutoDisable(true);
Scanner.removeEventListener(listener, EventCallback.EventType.Data)
Scanner.setDeviceEnabled(false);
scanner.release();
scanner.close();
~~~
All of this is fully UPOS compliant and identical to JavaPOS.
## Integrate a scale
The generic setup for a scale is the following:
![Scale setup](/documentation/scale-setup.PNG)
### Open the communication
Instantiate a *Scale* object, open it, and claim the port:
~~~java
Scale scale = new Scale();
scale.open(logicalName, context);
scale.claim(requestListener);
~~~
*requestListener* is an interface, responsible for handling the success or the failure of the port claiming.<br>
**The user must explicitly grant a dedicated permission** to allow the application to claim a port.
### Get weight
To get weight, enable data events and ask for the weight to the scale:
~~~java
scale.setDataEventEnabled(true);
scale.readWeight(result, timeout);
~~~
All of this is fully UPOS compliant and identical to JavaPOS.
### Close the communication
To close the communication release the port and close the device:
~~~java
scale.release();
scale.close();
~~~
## Configuration
The library can be configured using a configuration file, exactly as JavaPOS and UPOS requirements. The only difference is the format of the file: **json**. In the asset folder of the project an apos.json file must be set. The sample application provides an [example](https://github.com/datalogic/androidPOS-sampleApp/blob/main/app/src/main/assets/apos.json) of the file.
# Tested devices
The following table lists only tests performed with OEM communication.
| Device 			| Lifecycle | Get Weight 	| Receive labels 	| Retrieve statistics 	|Firmware comparison|Firmware upgrade	|
| ---- 				| ---- 		| ---- 			| ---- 				| ---- 					| ---- 				| ---- 				|
| QuickScan QD2590 	| V 		| NS 			| V 				| V 					| V 				| V 				| 
| Gryphon GD4590 	| V 		| NS 			| V 				| V 					| V 				| V 				|
| Gryphon GD4520 	| V 		| NS 			| V 				| V					 	| V 				| V 				|
| PowerScan PD9630	| V*		| NS			| V*				| V*					| V*				| -					|
| PowerScan PD9530	| V			| NS			| V					| V						| V					| V					|
| Magellan 9800i	| V			| V				| V					| V						| V					| -					|

  (*)- requires an external power plug.
# Useful links
- *[Datalogic Javapos](https://datalogic.github.io/javapos/overview/)*
- *[UPOS standard](https://www.omg.org/spec/UPOS/)*
- *[Sample application](https://github.com/datalogic/androidPOS-sampleApp)*