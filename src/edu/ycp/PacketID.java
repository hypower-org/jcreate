/*
 Copyright (c) 2013 - York College of Pennsylvania, Patrick J. Martin
 The MIT License
 See license.txt for details. 
*/


package edu.ycp;

/**
 * This enum organizes the possible packet IDs that the iRobot
 * create could return.
 * 
 * @author pjmartin
 *
 */
public enum PacketID {

	BUMP_AND_WHEEL((byte) 7),
	WALL((byte) 8),
	CLIFF_LEFT((byte) 9),
	CLIFF_FRONT_LEFT((byte) 10),
	CLIFF_FRONT_RIGHT((byte) 11),
	CLIFF_RIGHT((byte) 12),
	VIRTUAL_WALL((byte) 13),
	DRIVER_OVERCURRENTS((byte) 14),
	UNUSED_BYTE1((byte) 15),
	UNUSED_BYTE2((byte) 16),
	IR_BYTE((byte) 17),
	BUTTONS((byte) 18),
	DISTANCE((byte) 19),
	ANGLE((byte) 20),
	CHARGING_STATE((byte) 21),
	VOLTAGE((byte) 22),
	CURRENT((byte) 23),
	BATT_TEMP((byte) 24),
	BATT_CHARGE((byte) 25),
	BATT_CAP((byte) 26),
	WALL_SIGNAL((byte) 27),
	CLIFF_LEFT_SIGNAL((byte) 28),
	CLIFF_FRONT_LEFT_SIGNAL((byte) 29),
	CLIFF_FRONT_RIGHT_SIGNAL((byte) 30),
	CLIFF_RIGHT_SIGNAL((byte) 31),
	CARGO_BAY_DIN((byte) 32),
	CARGO_BAY_ANIN((byte) 33),
	CHARGE_SRC_AVAIL((byte) 34),
	OI_MODE((byte) 35),
	SONG_NUM((byte) 36),
	SONG_PLAYING((byte) 37),
	NUM_STREAM_PKTS((byte) 38),
	REQ_VEL((byte) 39),
	REQ_RADIUS((byte) 40),
	REQ_RIGHT_VEL((byte) 41),
	REQ_LEFT_VEL((byte) 42)
	;
	
	private final byte packetID;
	
	PacketID(byte id){
		this.packetID = id;
	}

	public byte getPacketID() {
		return packetID;
	}
	
	
	
}
