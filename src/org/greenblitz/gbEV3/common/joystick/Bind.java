package org.greenblitz.gbEV3.common.joystick;

import org.greenblitz.gbEV3.commandbased.Command;

public interface Bind {
	public void executeBind(int stick, int button, Command cmd);
}
