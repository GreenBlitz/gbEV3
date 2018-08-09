package org.greenblitz.gbEV3.common;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import lejos.hardware.Button;
import lejos.hardware.Sound;

import org.greenblitz.gbEV3.commandbased.Robot;

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

		@Override
		public String toString() {
			return "NativeJoystickData [axes=" + Arrays.toString(axes)
					+ ", buttons=" + Arrays.toString(buttons) + ", isValid="
					+ isValid + "]";
		}

	}

	public static final class InitialMatchData {
		public String eventName;
		public String gameMessage;
		public Alliance alliance;
		public String ip;

		@Override
		public String toString() {
			return "InitialMatchData [eventName=" + eventName
					+ ", gameMessage=" + gameMessage + ", alliance=" + alliance
					+ ", ip=" + ip + "]";
		}
	}

	public static final class PeriodicMatchData {
		public static final int JOYSTICK_COUNT = 2;

		public NativeJoystickData[] joysticksData;
		public boolean isEnabled;
		public GameType gameType;

		@Override
		public String toString() {
			return "PeriodicMatchData [joysticksData="
					+ Arrays.toString(joysticksData) + ", isEnabled="
					+ isEnabled + ", gameType=" + gameType + "]";
		}

	}

	public static final int DEFAULT_PORT = 4444;

	public static final char CONNECTION_ENDED = (char) (byte) -1;

	public static final int CONNECT_TIMEOUT = 5000;

	private static StationAccessor instance;
	private static Object instanceLock = new Object();

	private GsonMessageAnalyzer<InitialMatchData> mInitialDataAnalyzer = new GsonMessageAnalyzer<>(
			InitialMatchData.class);
	private GsonMessageAnalyzer<PeriodicMatchData> mPeriodicDataAnalyzer = new GsonMessageAnalyzer<>(
			PeriodicMatchData.class);

	private AtomicReference<InitialMatchData> mInitialDataCache = new AtomicReference<InitialMatchData>(
			null);

	private AtomicReference<PeriodicMatchData> mPeriodicDataCache = new AtomicReference<PeriodicMatchData>(
			null);

	private AtomicReference<Integer> mCurrentUpdateCount = new AtomicReference<>(
			0);

	private ReentrantLock mLock = new ReentrantLock();
	private Condition mHasDataArrived = mLock.newCondition();

	private Scanner mStationReader;
	private PrintStream mStationWriter;

	private Socket mStationSocket;

	private StationAccessor(int port) {
		Robot.getRobotLogger().info("Connecting to the Driver Station...");
		Socket station = new Socket();
		try {
			Sound.twoBeeps();
			station.connect(
					new InetSocketAddress(InetAddress.getLoopbackAddress(),
							port), CONNECT_TIMEOUT);
		} catch (ConnectException | SocketTimeoutException e) {
			if (e.getMessage() == "Connection refused")
				Robot.getRobotLogger().fatal(
						"no active station was found on the local host", e);
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(e);
		} finally {
			if (!station.isConnected()) {
				Robot.getRobotLogger().fatal(
						"Connection to Driver Station could not be made");
				Robot.exit(9971, Thread.currentThread().getStackTrace()[0]);
			}
		}
		Sound.beepSequenceUp();
		Robot.getRobotLogger().info(
				"Successfully connected to the Driver Station");
		init(station);
	}

	public static void init() {
		synchronized (instanceLock) {
			if (instance == null)
				instance = new StationAccessor(DEFAULT_PORT);
			instance.start();
		}
	}

	public static StationAccessor getInstance() {
		return instance;
	}

	@Override
	public void run() {
		while (Button.ESCAPE.isUp()) {
			aquireData();
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

	private void init(Socket stationSocket) {
		mStationSocket = stationSocket;
		mStationReader = getStationReader(mStationSocket);
		mStationWriter = getStationWriter(mStationSocket);
	}

	public void close() {
		try {
			mStationSocket.close();
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"an error occured while closing station connection", e);
			Robot.exit(9975, Thread.currentThread().getStackTrace()[0]);
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
			Robot.exit(9973, Thread.currentThread().getStackTrace()[0]);
			return;
		}

		if (mInitialDataAnalyzer.test(msg)) {
			if (!mInitialDataCache.compareAndSet(null,
					mInitialDataAnalyzer.parse(msg)))
				Robot.getRobotLogger()
						.warn("an attempt was made to change the initial data cache after it was created");
			else
				Robot.getRobotLogger().debug(
						"accepted initial match data: " + mInitialDataCache);
			return;
		}

		if (mPeriodicDataAnalyzer.test(msg)) {
			managePeriodicDataCache(mPeriodicDataAnalyzer.parse(msg));
			mLock.lock();
			try {
				mCurrentUpdateCount.set(mCurrentUpdateCount.get() + 1);
				mHasDataArrived.signalAll();
			} finally {
				mLock.unlock();
			}
			return;
		}

		Robot.getRobotLogger().fatal("unparseable message received: " + msg);
	}

	private void managePeriodicDataCache(PeriodicMatchData newData) {
		if (!isPeriodicDataAvailable())
			mPeriodicDataCache.set(newData);

		StringBuilder enable = new StringBuilder();
		StringBuilder gameType = new StringBuilder();

		if (newData.isEnabled != mPeriodicDataCache.get().isEnabled)
			enable.append("robot is now "
					+ ((newData.isEnabled) ? "enabled!" : "disabled!"));

		if (newData.gameType != mPeriodicDataCache.get().gameType)
			gameType.append("the game has now entered " + newData.gameType
					+ " stage!");

		if (enable.length() != 0)
			Robot.getRobotLogger().info(enable.toString());

		if (gameType.length() != 0)
			Robot.getRobotLogger().info(gameType.toString());

		mPeriodicDataCache.set(newData);
	}

	private Scanner getStationReader(Socket socket) {
		try {
			return new Scanner(socket.getInputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"error while creating scanner from station", e);
			Robot.exit(9971, Thread.currentThread().getStackTrace()[0]);
			return null;
		}
	}

	private PrintStream getStationWriter(Socket socket) {
		try {
			return new PrintStream(socket.getOutputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().fatal(
					"error while creating stream to station", e);
			Robot.exit(9972, Thread.currentThread().getStackTrace()[0]);
			return null;
		}
	}
}
