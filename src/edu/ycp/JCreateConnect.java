package edu.ycp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import edu.ycp.RoombaStartPacket.StartCommand;

import gnu.io.*;

public class JCreateConnect {
	
	private static final int roombaBaudRate = 57600;
	private OutputStream serialOut;
	private InputStream serialIn;
	private SerialPort serialPort;
	
	public void connect(String portName){
		try {
			CommPortIdentifier portId = CommPortIdentifier.getPortIdentifier(portName);
			
			serialPort = (SerialPort) portId.open("RoombaConnect", 2000);
			
			serialPort.setSerialPortParams(roombaBaudRate, SerialPort.DATABITS_8, 
					SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
			
			serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
			
			serialOut = serialPort.getOutputStream();
			serialIn = serialPort.getInputStream();
			
			
		} catch (NoSuchPortException e) {
			
			System.err.println("Port " + portName + " does not exist on this machine.");
		
		} catch (PortInUseException e) {
			
			System.err.println("Port " + portName + " in use!");
		} catch (UnsupportedCommOperationException e) {
			
			System.err.println("Error: " + e.getMessage());
		} catch (IOException e) {
			
			serialPort.close();
			System.err.println("Error: " + e.getMessage());
		}
		
		
		
	}
	
	public void sendBuffer(ByteBuffer bb){
		try {
			serialOut.write(bb.array());
		} catch (IOException e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public void disconnectRoomba(){
		serialPort.close();
	}
	
	public static void main(String[] args){
		System.out.println("Try connecting to iRobot Create...");
		
		JCreateConnect jc = new JCreateConnect();
		//TODO: just test code. Need to manually change depending on platform.
//		jc.connect("/dev/ttyUSB0");
		// Keyspan serial port on MacOSX 10.7
		jc.connect("/dev/tty.USA19Hfa14P1.1");
		
		System.out.println("...success.");
		
		// send a start command...
		//TODO: need a more elegant structure here!
		ByteBuffer startCommand = RoombaStartPacket.generateCommand(StartCommand.START);
		
		jc.sendBuffer(startCommand);
		
		jc.disconnectRoomba();
		
	}

}
