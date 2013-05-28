jcreate
=======

A Java library that allows you to control an iRobot Create robot platform.

This project provides Java programmers a simple interface to make an iRobot Create "go" using the iRobot Open Interface specification. It depends on the RXTX library for serial communication. This library does NOT implement the whole Open Interface specification. Its primary goal is to allow for quick startup of and easy access to the iRobot Create. Some features are not currently implemented, such as the scripting commands.

This library also serves an educational example of how the Java Concurrency tools can be used to get interesting work done. Furthermore, you can see how to handle serial port communication in a high level language.

This software is licensed under the MIT License. See the license file for details.

### Quick Start ###

First, you need to download RXTX and install it on your Windows, Mac, or Linux box accordingly. Download the jcreate library
and compile in your favorite Java environment. Note: jar binaries for jcreate will be coming soon.

In a new Java class, you can invoke a CreateRobot by instantiating the following Java object:
``CreateRobot optimusPrime = new CreateRobot("/dev/ttyUSB0", CreateMode.FULL);``

jcreate handles all startup and management of your iRobot Create robot! All you need to do is interact with it. For example, 
drive the robot at 100 mm/s and 45 deg/s rotation:

``optimusPrime.go(100, (float)Math.PI/4)``

or, get the current bump sensor status:

``optimusPrime.isBumpRight()``

Let us know how it works for you!
