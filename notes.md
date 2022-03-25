# RetrieveStatistics function
JavaPOS **retrievetatistics** function generate a text file called *"scanner_info.txt"*. Due to permissions, it is better to not save files in a library under Android. The content of this file is put at index 1 of the statisticsBuffer passed as input. In such a way the developer can save the device where it is needed, handling permissions. This file is used in JavaPOS for Avalanche integration too. In the first version of AndroiPOS the integration with Avalanche is not supported. It is not clear if it is possible at all.

After a claim the retrieve function may be called. In AndroidPOS no file will be generated.

# WMI
WMI is not supported by Android, so it is not in plan to support it.

# JMX
JMX has not been ported into Android. It is a stadard functionality for JavaSE but not supported in Java for Android. It is not possible to port this functionality.

# Avalanche
There is a mobile version of Avalanche but it is not clear if it fits our needs.

# Logging
It is a very bad idea to log in production in Andorid. Both for performance and security reasons. Furthermore logging is not a responsability of a library: a library throws exceptions, the external application, catching exception, can log, if needed.

# RS232 support
At the moment no device with native RS232 support has been found. Furthermore Android has no interface for this kind of transport. Even if a device with a RS232 can be found, it will be extremely difficoult to create a general driver for it.

# XML configuration file
Android has very few working libraries for XML parsing. AndroidPOS will use a JSON configuration file.

# Direct IO image capturing
Due to permission handling, it is not wise to save public files in Android. There are two Direct IO commands for Serial scanners to save images. These commands in JavaPOS creates files. In Android it would lead to many issues. Since these calls are synchronous, my proposal is to handle the content of the image as an output of the function, avoiding any file creation.

# Direct IO logs files
Due to permission handling, it is not wise to save public files in Android. There are two direct IO commands to extract logs from a USBScanner. JavaPOS create a file for these logs, and returns the content of this files as a string too. AndroidPOS will only return the string content of the file.