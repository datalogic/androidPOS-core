[![](https://jitpack.io/v/datalogic/androidpos-core.svg)](https://jitpack.io/#datalogic/androidpos-core)
# Description
This project contains the core of the AndroidPOS project.

# How to use the library
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
	        implementation 'com.github.datalogic:androidpos-core:0.1-alpha'
	}
~~~