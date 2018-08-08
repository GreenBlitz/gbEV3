package org.greenblitz.gbEV3.common;

import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.greenblitz.gbEV3.commandbased.Robot;

import lejos.hardware.Button;

public final class StationAccessor extends Thread {
	public enum GameType {
		AUTO, TELEOP, INVALID
	}

	public enum Alliance {
		RED1(1), RED2(2), BLUE1(1), BLUE2(2), NONE(-1);

		public final int position;

		private Alliance(int position) {
			this.position = position;
		}

		public boolean isRedAlliance() {
			return this == RED1 || this == RED2;
		}

		public boolean isBlueAlliance() {
			return this == BLUE1 || this == BLUE2;
		}
	}

	public static final class NativeJoystickData {
		public static final int AXES_COUNT = 6;
		public static final int BUTTON_COUNT = 18;

		public float[] axes;
		public boolean[] buttons;
		public boolean isValid;

		public NativeJoystickData() {
			axes = new float[AXES_COUNT];
			buttons = new boolean[BUTTON_COUNT];
			isValid = true;
		}
	}

	public static final class InitialMatchData {
		public String eventName;
		public String gameMessage;
		public Alliance alliance;
		public String ip;
	}

	public static final class PeriodicMatchData {
		public static final int JOYSTICK_COUNT = 2;

		public NativeJoystickData[] joysticksData;
		public boolean isEnabled;
		public GameType gameType;
	}

	public static final int DEFAULT_PORT = 4444;

	public static final char CONNECTION_ENDED = (char) (byte) -1;

	private static StationAccessor instance;
	private static Object instanceLock = new Object();

	private GsonMessageAnalyzer<InitialMatchData> mInitialDataAnalyzer = new GsonMessageAnalyzer<>(
			InitialMatchData.class);
	private GsonMessageAnalyzer<PeriodicMatchData> mPeriodicDataAnalyzer = new GsonMessageAnalyzer<>(
			PeriodicMatchData.class);

	/*
	 * private AtomicReference<InitialMatchData> mInitialDataCache = new
	 * AtomicReference<InitialMatchData>(null);
	 * 
	 * private AtomicReference<PeriodicMatchData> mPeriodicDataCache = new
	 * AtomicReference<PeriodicMatchData>( null);
	 */

	private AtomicReference<InitialMatchData> mInitialDataCache = new AtomicReference<InitialMatchData>(
			new InitialMatchData());

	private AtomicReference<PeriodicMatchData> mPeriodicDataCache = new AtomicReference<PeriodicMatchData>(
			new PeriodicMatchData());

	{
		mInitialDataCache.get().alliance = Alliance.NONE;
		mInitialDataCache.get().ip = "10.0.1.1";
		mPeriodicDataCache.get().gameType = GameType.TELEOP;
		mPeriodicDataCache.get().isEnabled = true;
		mPeriodicDataCache.get().joysticksData = new NativeJoystickData[2];
		mPeriodicDataCache.get().joysticksData[0] = new NativeJoystickData();
	}

	private AtomicReference<Integer> mCurrentUpdateCount = new AtomicReference<>(
			0);

	private ReentrantLock mLock = new ReentrantLock();
	private Condition mHasDataArrived = mLock.newCondition();

	private Scanner mStationReader;
	private PrintStream mStationWriter;

	private Socket mStationSocket;

	private StationAccessor() {
		init(DEFAULT_PORT);
	}

	public static void init() {
		synchronized (instanceLock) {
			if (instance == null)
				instance = new StationAccessor();
		}
	}

	public static StationAccessor getInstance() {
		synchronized (instanceLock) {
			if (instance == null)
				instance = new StationAccessor();

			return instance;
		}
	}

	@Override
	public void run() {
		while (Button.ESCAPE.isUp()) {
			// aquireData();
		}
	}

	public Scanner getStationReader() {
		return mStationReader;
	}

	public PrintStream getStationWriter() {
		return mStationWriter;
	}

	public boolean isInitialDataAvailable() {
		return mInitialDataCache.get() != null;
	}

	public boolean isPeriodicDataAvailable() {
		return mPeriodicDataCache.get() != null;
	}

	public boolean isEnabled() {
		if (!isPeriodicDataAvailable())
			return false;

		return mPeriodicDataCache.get().isEnabled;

	}

	public boolean isDisabled() {
		return !isEnabled();
	}

	public boolean isTeleoperated() {
		if (!isPeriodicDataAvailable())
			return false;

		return mPeriodicDataCache.get().gameType == GameType.TELEOP;
	}

	public boolean isAutonomous() {

		if (!isPeriodicDataAvailable())
			return false;

		return mPeriodicDataCache.get().gameType == GameType.AUTO;

	}

	public GameType getCurrentGameType() {
		if (!isPeriodicDataAvailable())
			return null;

		return mPeriodicDataCache.get().gameType;
	}

	public Alliance getAlliance() {
		if (!isInitialDataAvailable())
			return null;

		return mInitialDataCache.get().alliance;
	}

	public String getEventName() {
		if (!isInitialDataAvailable())
			return null;

		return mInitialDataCache.get().eventName;
	}

	public String getGameMessage() {
		if (!isInitialDataAvailable())
			return null;

		return mInitialDataCache.get().gameMessage;
	}

	public String getRemoteIp() {
		if (!isInitialDataAvailable())
			return null;

		return mInitialDataCache.get().ip;
	}

	public float getJoystickAxis(int stick, int port) {
		if (stick < 0 || stick >= PeriodicMatchData.JOYSTICK_COUNT)
			throw new IllegalArgumentException("no stick at index '" + stick
					+ "'");

		if (port < 0 || port >= NativeJoystickData.AXES_COUNT)
			throw new IllegalArgumentException("no axis at index '" + stick
					+ "'");

		return mPeriodicDataCache.get().joysticksData[stick].axes[port];
	}

	public boolean getJoystickButton(int stick, int port) {
		if (stick < 0 || stick >= PeriodicMatchData.JOYSTICK_COUNT)
			throw new IllegalArgumentException("no stick at index '" + stick
					+ "'");

		if (port < 0 || port >= NativeJoystickData.AXES_COUNT)
			throw new IllegalArgumentException("no button at index '" + stick
					+ "'");

		return mPeriodicDataCache.get().joysticksData[stick].buttons[port];
	}

	private void init(int port) {
		/*
		 * mStationSocket = getConnection(port); mStationReader =
		 * getStationReader(mStationSocket); mStationWriter =
		 * getStationWriter(mStationSocket);
		 */
	}

	public void close() {
		try {
			mStationSocket.close();
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"an error occured while closing station connection", e);
			Robot.exit(9975);
		}
	}

	public boolean waitForData() {
		return waitForData(0);
	}

	public boolean waitForData(long timeout) {
		mLock.lock();
		try {
			if (timeout > 0) {
				long startTime = System.currentTimeMillis();
				int currentUpdateCount = mCurrentUpdateCount.get();
				long currentTime;

				while (currentUpdateCount == mCurrentUpdateCount.get()) {
					if ((currentTime = System.currentTimeMillis()) - startTime > timeout) {
						return false;
					}
					boolean signaled = mHasDataArrived.await(timeout
							+ startTime - currentTime, TimeUnit.MILLISECONDS);
					if (!signaled)
						return false;
				}
			} else {
				mHasDataArrived.await();
			}
			return true;
		} catch (InterruptedException e) {
			return false;
		} finally {
			mLock.unlock();
		}
	}

	private void aquireData() {

		if (!mStationReader.hasNext())
			return;

		String msg = mStationReader.next();

		if (msg.contains(new StringBuilder().append(CONNECTION_ENDED))) {
			Robot.getRobotLogger().fatal(
					"connection to station lost; robot is closing");
			Robot.exit(9973);
			return;
		}

		if (mInitialDataAnalyzer.test(msg)) {
			if (!mInitialDataCache.compareAndSet(null,
					mInitialDataAnalyzer.parse(msg)))
				Robot.getRobotLogger()
						.warn("an attempt was made to change the initial data cache after it was created");
			return;
		}

		if (mPeriodicDataAnalyzer.test(msg)) {
			mPeriodicDataCache.set(mPeriodicDataAnalyzer.parse(msg));
			mLock.lock();
			try {
				mHasDataArrived.signalAll();
				mCurrentUpdateCount.set(mCurrentUpdateCount.get() + 1);
			} finally {
				mLock.unlock();
			}
			return;
		}

		Robot.getRobotLogger().fatal("unparseable message received: " + msg);
	}

	private Socket getConnection(int port) {
		try {
			return new Socket(InetAddress.getLoopbackAddress(), port);
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"error while creating connection station", e);
			System.exit(9970);
			return null;
		}
	}

	private Scanner getStationReader(Socket socket) {
		try {
			return new Scanner(socket.getInputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"error while creating scanner from station", e);
			Robot.exit(9971);
			return null;
		}
	}

	private PrintStream getStationWriter(Socket socket) {
		try {
			return new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"error while creating stream to station", e);
			Robot.exit(9972);
			return null;
		}
	}
}
