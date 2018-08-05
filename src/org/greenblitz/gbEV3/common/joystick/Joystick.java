package org.greenblitz.gbEV3.common.joystick;

import java.util.LinkedList;
import java.util.List;

import org.greenblitz.gbEV3.commandbased.Command;
import org.greenblitz.gbEV3.common.StationAccessor;
import org.greenblitz.gbEV3.common.Tuple3;

public class Joystick {

	private int port;
	private List<Tuple3<BindType, Integer, Command>> bindTypeList;
	private StationAccessor station;

	/**
	 * Construct a Joystick object.
	 * 
	 * @param port
	 *            the Joystick's port
	 */
	public Joystick(int port) {
		// TODO change port to 0
		this.port = port;
		bindTypeList = new LinkedList<>();
		station = StationAccessor.getInstance();
	}

	/**
	 * Getter for an axis position.
	 * 
	 * @param axis
	 *            - an axis type. see {@link Axis}.
	 * @return a float between -1 and 1 describing the position of the joystick
	 */
	public float getAxis(Axis axis) {
		return station.getJoystickAxis(port, axis.id);
	}

	/**
	 * Getter for a button position.
	 * 
	 * @param button
	 *            - a button type. see {@link Button}.
	 * @return true if the button is pressed.
	 */
	public boolean getButton(Button button) {
		return station.getJoystickButton(port, button.id);
	}

	/**
	 * Binds a command to the press of a button. The command will run every time
	 * the button is pressed (but not repeatedly!)
	 * 
	 * @param b
	 *            - a button type. see {@link Button}.
	 * @param cmd
	 *            - the desired command to bind to the button.
	 */
	public void whenPressed(Button b, Command cmd) {
		addBind(BindType.WhenPressed, b, cmd);
	}

	/**
	 * Binds a command to the release of a button. The command will run every
	 * time the button is released after it was pressed (but not repeatedly!).
	 * 
	 * @param b
	 *            - a button type. see {@link Button}.
	 * @param cmd
	 *            - the desired command to bind to the button.
	 */
	public void whenReleased(Button b, Command cmd) {
		addBind(BindType.WhenReleased, b, cmd);
	}

	/**
	 * Binds a command to the press of a button and ends it on it's release. The
	 * command will run only while the button is pressed.
	 * 
	 * @param b
	 *            - a button type. see {@link Button}.
	 * @param cmd
	 *            - the desired command to bind to the button.
	 */
	public void whileHeld(Button b, Command cmd) {
		addBind(BindType.Whileheld, b, cmd);
	}

	/**
	 * Binds a command to execute while a button is released.
	 * 
	 * @param b
	 *            - a button type. see {@link Button}.
	 * @param cmd
	 *            - the desired command to bind to the button.
	 */
	public void whileReleased(Button b, Command cmd) {
		addBind(BindType.WhileReleased, b, cmd);
	}

	private void addBind(BindType t, Button b, Command cmd) {
		bindTypeList.add(new Tuple3<BindType, Integer, Command>(t, b.id, cmd));
	}

	public void executeMePlease() {
		for (Tuple3<BindType, Integer, Command> boyo : bindTypeList) {
			boyo.first.executeBind(port, boyo.second, boyo.third);
		}
	}
}
