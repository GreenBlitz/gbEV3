package org.greenblitz.gbEV3.common;

import java.io.EOFException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import org.greenblitz.gbEV3.commandbased.Robot;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

import lejos.hardware.Button;
import lejos.hardware.Sound;

public final class StationAccessor extends Thread {

	/**
	 * The alliance and position of a robot in the arena
	 * 
	 * @author karlo
	 */
	public enum Alliance {
		RED1(1), RED2(2), BLUE1(1), BLUE2(2), NONE(0);

		public final int position;

		private Alliance(int position) {
			this.position = position;
		}

		public static boolean isRedAlliance(Alliance a) {
			return a == RED1 || a == RED2;
		}

		public static boolean isBlueAlliance(Alliance a) {
			return a == BLUE1 || a == BLUE2;
		}
	}

	/**
	 * Type of the current played game; this may change during match
	 * 
	 * @author karlo
	 */
	public enum GameType {
		TELEOP, AUTO, INVALID
	}

	/**
	 * All data related to single joystick hid
	 * 
	 * @author karlo
	 */
	private static class NativeJoystickData {
		public float[] axes;
		public boolean[] buttons;
		public boolean isValid;

		public NativeJoystickData(int axesCount, int buttonCount, boolean isValid) {
			axes = new float[axesCount];
			buttons = new boolean[buttonCount];
			this.isValid = isValid;
		}

		public NativeJoystickData() {
			this(6, 18, false);
		}

		@Override
		public String toString() {
			return "NativeJoystickData [axes=" + Arrays.toString(axes) + ", buttons=" + Arrays.toString(buttons)
					+ ", isValid=" + isValid + "]";
		}

	}

	/**
	 * Data unique to each match
	 * 
	 * @author karlo
	 */
	private static class MatchSpecificData {
		public String eventName;
		public String gameSpecificMessage;
		public Alliance alliance;

		@Override
		public String toString() {
			return "MatchSpecificData[event=" + eventName + ", message=" + gameSpecificMessage + ", alliance="
					+ alliance + "]";
		}
	}

	/**
	 * Every piece of data available from the station
	 * 
	 * @author karlo
	 */
	private static class StationDataCache {
		public NativeJoystickData[] joystickData;
		public GameType gameType;
		public boolean isEnabled;

		public StationDataCache(int joystickCount) {
			joystickData = new NativeJoystickData[joystickCount];
			for (int i = 0; i < joystickData.length; i++) {
				joystickData[i] = new NativeJoystickData();
			}
		}

		public StationDataCache() {
			this(JOYSTICK_COUNT);
		}

		@Override
		public String toString() {
			return "StationDataCache[joystickData=" + Arrays.toString(joystickData) + ", gameType=" + gameType
					+ ", isEnabled=" + isEnabled + "]";
		}
	}

	/**
	 * Maximum amount of joysticks to connect
	 */
	public static final int JOYSTICK_COUNT = 1;

	private static final long NEXT_JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL = 1000;

	private static final Object instancelock = new Object();

	private static final int SERVER_TIMEOUT = 2000;
	private static final int STATION_TIMEOUT = 20;

	private static final Gson GSON_OBJECT = new Gson();
	private static final TypeAdapter<StationDataCache> JSON_CACHE_PARSER = GSON_OBJECT
			.getAdapter(StationDataCache.class);
	private static final TypeAdapter<MatchSpecificData> JSON_MATCH_DATA_PARSER = GSON_OBJECT
			.getAdapter(MatchSpecificData.class);

	private static StationAccessor instance;

	private final ServerSocket m_self;
	private Socket m_station;
	private Scanner m_stationReader;
	private PrintStream m_stationWriter;

	private long m_nextJoystickUnpluggedMessageTime = -1;

	private StationDataCache m_cache = new StationDataCache();
	private final MatchSpecificData m_matchInfo;
	private final Object m_cacheMutex = new Object();

	private final Lock m_waitForData = new ReentrantLock();
	private final Condition m_hasDataArrived = m_waitForData.newCondition();
	private int m_currentUpdateCount = 0;

	private volatile boolean m_keepThreadAlive = true;

	private StationAccessor() {
		Robot.getRobotLogger().config("starting StationAccessor; connecting to remote station");
		m_self = getSelfSocket();
		m_station = pollStationConnection();
		m_stationReader = getReader();
		m_stationWriter = getStationWriter();
		m_matchInfo = waitForMatchData();
		Robot.getRobotLogger().config("connection between station and robot created successfully");
		Robot.getRobotLogger().config("match info recived: " + m_matchInfo);
	}

	public static StationAccessor getInstance() {
		synchronized (instancelock) {
			if (instance == null)
				instance = new StationAccessor();

			return instance;
		}
	}

	public static void init() {
		synchronized (instancelock) {
			if (instance == null)
				instance = new StationAccessor();

			instance.start();
		}
	}

	/**
	 * @return Is the robot in disabled mode
	 */
	public boolean isDisabled() {
		synchronized (m_cacheMutex) {
			return !m_cache.isEnabled;
		}
	}

	/**
	 * @return Is the robot in disabled mode
	 */
	public boolean isEnabled() {
		synchronized (m_cacheMutex) {
			return m_cache.isEnabled;
		}
	}

	private void aquireData() {
		m_waitForData.lock();
		try {
			synchronized (m_cacheMutex) {
				StationDataCache tmp = safeJsonAsCache();
				if (tmp != null) {
					StringBuilder sb = new StringBuilder();
					if (tmp.gameType != m_cache.gameType)
						sb.append("game type changed: '" + m_cache.gameType + "' to '" + tmp.gameType + "'");
					if (tmp.isEnabled != m_cache.isEnabled) {
						if (sb.length() != 0) sb.append(", ");
						sb.append("robot is now ").append(tmp.isEnabled ? "enabled" : "disabled");
					}
					if (sb.length() != 0)
						Robot.getRobotLogger().finer(sb.toString());	
					m_cache = tmp;
				}
			}

			m_hasDataArrived.signalAll();
			m_currentUpdateCount++;
		} catch (Throwable t) {
			Robot.getRobotLogger().log(Level.SEVERE, "Error aquire data", t);
		} finally {
			m_waitForData.unlock();
		}
	}

	public void waitForData() {
		waitForData(0);
	}

	public boolean waitForData(long timeout) {
		m_waitForData.lock();
		try {
			long startTime = RobotClock.currentTimeMicros();

			int currentUpdateCount = m_currentUpdateCount;

			while (currentUpdateCount == m_currentUpdateCount) {
				if (timeout > 0) {

					long now = RobotClock.currentTimeMicros();
					if (now < startTime + timeout) {
						boolean signaled = m_hasDataArrived.await(startTime + timeout - now, TimeUnit.MICROSECONDS);

						if (!signaled)
							return false;
					} else {
						return false;
					}
				} else {
					m_hasDataArrived.await();
				}
			}

			return true;
		} catch (InterruptedException ex) {
			return false;
		} finally {
			m_waitForData.unlock();
		}
	}

	public boolean isAutonomous() {
		synchronized (m_cacheMutex) {
			return m_cache.gameType == GameType.AUTO;
		}
	}

	public boolean isTeleop() {
		synchronized (m_cacheMutex) {
			return m_cache.gameType == GameType.TELEOP;
		}
	}

	public GameType getGameType() {
		synchronized (m_cacheMutex) {
			return m_cache.gameType;
		}
	}

	public Alliance getAlliance() {
		synchronized (m_cacheMutex) {
			return m_matchInfo.alliance;
		}
	}

	/**
	 * Returns the value of the requested axis at the requested joystick.
	 * Normalized between 0-1 for triggers and between -1 to 1 otherwise.
	 * 
	 * @param stick
	 *            Requested joystick index
	 * @param axis
	 *            Requested joystick button
	 * @return The value of requested axis
	 * @throws IllegalArgumentException
	 *             if {@code stick < 0} or {@code stick >= JOYSTICK_COUNT}
	 */
	public float getJoystickAxis(int stick, int axis) throws IllegalArgumentException {
		if (stick < 0 || stick >= JOYSTICK_COUNT)
			throw new IllegalArgumentException(
					"Joystick index out of range: '" + stick + "', expected 0 - " + (JOYSTICK_COUNT - 1));

		synchronized (m_cacheMutex) {
			if (!m_cache.joystickData[stick].isValid) {
				reportJoystickUnpluggedError(
						"Invalid joystick input on port '" + stick + "', check if it is connected");
				return 0;
			} else {
				return m_cache.joystickData[stick].axes[axis];
			}
		}
	}

	/**
	 * 
	 * @param stick
	 *            Requested joystick index
	 * @param button
	 *            Requested joystick button
	 * @return The state of requested button (is it pressed)
	 * @throws IllegalArgumentException
	 *             if {@code stick < 0} or {@code stick >= JOYSTICK_COUNT}
	 */
	public boolean getJoystickButton(int stick, int button) throws IllegalArgumentException {
		if (stick < 0 || stick >= JOYSTICK_COUNT)
			throw new IllegalArgumentException(
					"Joystick index out of range: '" + stick + "', expected 0 - " + (JOYSTICK_COUNT - 1));

		synchronized (m_cacheMutex) {
			if (!m_cache.joystickData[stick].isValid) {
				reportJoystickUnpluggedError(
						"Invalid joystick input on port '" + stick + "', check if it is connected");
				return false;
			} else {
				return m_cache.joystickData[stick].buttons[button];
			}
		}
	}

	public String getGameSpecificMessage() {
		return m_matchInfo.gameSpecificMessage;
	}

	public String getEventName() {
		return m_matchInfo.eventName;
	}

	/**
	 * Stop the station from updating data
	 */
	public void release() {
		m_keepThreadAlive = false;
		try {
			m_self.close();
			m_station.close();
			m_stationReader.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void run() {
		Robot.getRobotLogger().fine("Running StationAccesor thread");
		while (m_keepThreadAlive)
			aquireData();
	}

	public InetSocketAddress getStationConnectionAddress() {
		return (InetSocketAddress) m_station.getRemoteSocketAddress();
	}

	public void send(String msg) {
		m_stationWriter.println(msg);
	}

	public void flush() {
		m_stationWriter.flush();
	}

	public PrintStream getStationStream() {
		return m_stationWriter;
	}

	private StationDataCache safeJsonAsCache() {
		String jsonObj = safeGetString();
		if (jsonObj == "")
			return null;
		try {
			StationDataCache ret;
			if ((ret = JSON_CACHE_PARSER.fromJson(jsonObj)) == null) {
				Robot.getRobotLogger().severe("gson parsing error: unknown json layout");
				return null;
			} else {
				return ret;
			}
		} catch (IOException e) {
			Robot.getRobotLogger().severe("GSON Error: eof encounterd while parsing" + jsonObj);
			return null;
		}
	}

	private MatchSpecificData safeJsonAsMatchData() {
		String jsonObj = safeGetString();
		if (jsonObj == null)
			return null;

		try {
			return JSON_MATCH_DATA_PARSER.fromJson(jsonObj);
		} catch (EOFException e) {
			Robot.getRobotLogger().warning("eof detected while aquiring string");
			return null;
		} catch (IOException e) {
			Robot.getRobotLogger().log(Level.SEVERE, "GSON 	Error: ", e);
			Robot.exit(9970);
			return null;
		}
	}

	private MatchSpecificData waitForMatchData() {
		MatchSpecificData ret;
		while (m_stationReader.hasNext() && Button.ESCAPE.isUp()) {
			if ((ret = safeJsonAsMatchData()) != null) {
				return ret;
			}
		}
		return null;
	}

	private String safeGetString(long timeout) {
		long t0 = System.currentTimeMillis();
		if (timeout == -1)
			while (!m_stationReader.hasNext() && Button.ESCAPE.isUp()) {}
		else
			while (!m_stationReader.hasNext() && Button.ESCAPE.isUp() && System.currentTimeMillis() - t0 < timeout) {}
		
		if (!m_stationReader.hasNext())
			return "";

		String ret = m_stationReader.next();
		if (ret.indexOf((char) -1) != -1) {
			Robot.getRobotLogger().severe("connection to remote station lost");
			Robot.exit(9971);
			return "";
		}
		Robot.getRobotLogger().finest(ret);
		return ret;
	}

	private String safeGetString() {
		return safeGetString(-1);
	}

	private void reportJoystickUnpluggedError(String message) {
		long currentTime = RobotClock.currentTimeMicros();
		if (currentTime > m_nextJoystickUnpluggedMessageTime) {
			Robot.getRobotLogger().warning(message);
			m_nextJoystickUnpluggedMessageTime = currentTime + NEXT_JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL;
		}
	}

	private ServerSocket getSelfSocket() {
		ServerSocket ret = null;
		try {
			ret = new ServerSocket(4444);
			ret.setSoTimeout(SERVER_TIMEOUT);
		} catch (IOException e) {
			Robot.getRobotLogger().severe("could not setup connection between station and robot");
			Robot.exit(9970);
		}
		return ret;
	}

	private Socket getStationSocket() {
		Socket ret = null;
		try {
			Robot.getRobotLogger().config("trying to connect to station... ");
			Sound.twoBeeps();
			ret = m_self.accept();
			ret.setSoTimeout(STATION_TIMEOUT);
			Sound.beepSequenceUp();
		} catch (SocketTimeoutException e) {
			Robot.getRobotLogger().info("cannot connect to station");
		} catch (IOException e) {
			try {
				if (ret != null) {
					ret.close();
				}
			} catch (IOException e1) {
			}
			Robot.getRobotLogger().severe("could not setup connection between station and robot");
			Robot.exit(9970);
		}
		return ret;
	}

	private Scanner getReader() {
		try {
			return new Scanner(m_station.getInputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().severe(e.toString());
			Sound.buzz();
			return null;
		}
	}

	private PrintStream getStationWriter() {
		try {
			return new PrintStream(m_station.getOutputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().severe(e.toString());
			Sound.buzz();
			return null;
		}
	}

	private Socket pollStationConnection() {
		Robot.getRobotLogger().config("waiting for connection request from station");
		Socket ret = getStationSocket();
		while (ret == null && Button.ESCAPE.isUp())
			ret = getStationSocket();
		if (ret == null)
			Robot.exit(9970);
		return ret;
	}
}
