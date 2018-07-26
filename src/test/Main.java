package test;

import org.greenblitz.gbEV3.commandbased.Scheduler;

public class Main {
	public static void main(String[] args) {
		Scheduler.getInstance().registerSubsystem(IntegerSubsystem.getInstance());
		int count = 0;
		while(count++ < 1000) Scheduler.getInstance().run();
		System.out.println(IntegerSubsystem.getInstance().getValue());
	}
}
