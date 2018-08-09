package org.greenblitz.gbEV3.common.joystick;

public enum Axis {
	/**
	 * Left stick's X axis.
	 */
	LX(1),
	
	/**
	 * Left stick's Y axis.
	 */
	LY(0),
	
	/**
	 * Right stick's X axis.
	 */
	RX(3),
	
	/**
	 * Right stick's Y axis.
	 */
	RY(2);
	
	public final int id;
	
	private Axis(int id){
		this.id = id;
	}
}
