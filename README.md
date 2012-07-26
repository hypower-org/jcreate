jcreate
=======

A Java library that allows you to control an iRobot Create robot platform.

This project provides Java programmers a simple interface to make an iRobot Create "go" 
using the iRobot Open Interface specification. It depends on the RXTX library for serial communication. This library does implement the whole Open Interface specification, as its main goal is to allow for quick startup and easy access. Some features are not currently implemented, such as the scripting commands.

Instead, one can use this library inside of an application that supplies the "smarts" for your iRobot Create. For more details,see the Wiki.