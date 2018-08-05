package org.greenblitz.gbEV3.common.joystick;

/**
 * Enum describing all the buttons on the XBOX 360 controller.
 * 
 * @author OGBOY
 *
 */
public enum Button {

	/**
	 * The green button marked with the letter A on the XBOX 360 controller.
	 */
	A(0),

	/**
	 * The red button marked with the letter B on the XBOX 360 controller.
	 */
	B(1),

	/**
	 * The blue button marked with the letter X on the XBOX 360 controller.
	 */
	X(2),

	/**
	 * The yellow button marked with the letter Y on the XBOX 360 controller.
	 */
	Y(3),

	/**
	 * The upper bumper on the left corner on the XBOX 360 controller.
	 */
	LB(4),

	/**
	 * The upper bumper on the right corner on the XBOX 360 controller.
	 */
	RB(5),

	/**
	 * The small black button on the left of the central button on the XBOX 360
	 * controller.
	 */
	BACK(6),

	/**
	 * The small black button on the right of the central button on the XBOX 360
	 * controller.
	 */
	START(7),

	/**
	 * The button that is pressed by pushing the left stick in the XBOX 360
	 * controller.
	 */
	LS(8),

	/**
	 * The button that is pressed by pushing the right stick in the XBOX 360
	 * controller.
	 */
	RS(9),

	/**
	 * The button between the left and up arrows on the left side of the XBOX
	 * 360 controller.
	 */
	LEFT_UP(10),

	/**
	 * The upper arrow on the left side on the XBOX 360 controller.
	 */
	UP(11),

	/**
	 * The button between the right and up arrows on the left side of the XBOX
	 * 360 controller.
	 */
	RIGHT_UP(12),

	/**
	 * The right arrow on the left side of the XBOX 360 controller.
	 */
	RIGHT(13),

	/**
	 * The button between the right and down arrows on the left side of the XBOX
	 * 360 controller.
	 */
	RIGHT_DOWN(14),

	/**
	 * The down arrow on the left side of the XBOX 360 controller.
	 */
	DOWN(15),

	/**
	 * The button between the down and left arrow on the left side of the XBOX
	 * 360 controller.
	 */
	DOWN_LEFT(16),

	/**
	 * The left arrow on the left side on the XBOX 360 controller.
	 */
	LEFT(17);

	public final int id;

	private Button(int id) {
		this.id = id;
	}

}
