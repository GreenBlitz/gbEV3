package org.greenblitz.gbEV3.common.joystick;

import org.greenblitz.gbEV3.commandbased.Command;
import org.greenblitz.gbEV3.common.StationAccessor;

public enum BindType implements Bind {
	
	WhenPressed{
		
		private boolean pressedLastTime = false;
		
		@Override
		public void executeBind(int stick, int button, Command cmd) {
			StationAccessor station = StationAccessor.getInstance();
			if(station.getJoystickButton(stick, button)){
				if(!pressedLastTime)cmd.start();
				
				// dont run multiple times each press
				pressedLastTime = true;
			} else  
				
				pressedLastTime = false;
				
		}
		
	},
	WhenReleased{
		
		boolean wasPressed = false;

		@Override
		public void executeBind(int stick, int button, Command cmd) {
			StationAccessor station = StationAccessor.getInstance();

			if(wasPressed && !station.getJoystickButton(stick, button)){
				cmd.start();
			}
			
			wasPressed = station.getJoystickButton(stick, button);
		}
		
	},Whileheld{
		
		boolean pressedLastTime = false;

		@Override
		public void executeBind(int stick, int button, Command cmd) {
			StationAccessor station = StationAccessor.getInstance();
			if(station.getJoystickButton(stick, button)){
				if(!pressedLastTime){
					cmd.start();
					pressedLastTime= true;
				}
			} else  {
				if(cmd.isRunning()){
					cmd.terminate(true);
					pressedLastTime = false;
				}
			}
		}
		
	},WhileReleased{
		
		boolean firstReleased = true;
		@Override
		public void executeBind(int stick, int button, Command cmd) {
			StationAccessor station = StationAccessor.getInstance();
			if(!station.getJoystickButton(stick, button)){
				if(firstReleased){
					cmd.start();
					firstReleased = false;
				}
			} else {
				if(cmd.isRunning()){
					cmd.terminate(true);
					firstReleased = true;
				}
			}
		}
	
	};	
	
}
