package org.greenblitz.gbEV3.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lejos.hardware.Button;
import lejos.hardware.Sound;

import org.greenblitz.gbEV3.commandbased.Robot;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

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
		TELEOP, AUTO
	}

	/**
	 * All data related to single joystick hid
	 * 
	 * @author karlo
	 */
	private static class NativeJoystickData {
		public float[] axes;
		public boolean[] povs;
		public boolean[] buttons;
		public boolean isValid;

		public NativeJoystickData(int axesCount, int buttonCount,
				int povsCount, boolean isValid) {
			axes = new float[axesCount];
			povs = new boolean[buttonCount];
			buttons = new boolean[povsCount];
			this.isValid = isValid;
		}

		public NativeJoystickData() {
			this(6, 10, 8, false);
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
	}

	/**
	 * Maximum amount of joysticks to connect
	 */
	public static final int JOYSTICK_COUNT = 2;

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
	private BufferedReader m_stationReader;
	private InputStreamReader m_stationInnerReader;

	private long m_nextJoystickUnpluggedMessageTime = -1;

	private StationDataCache m_cache = new StationDataCache();
	private final MatchSpecificData m_matchInfo;
	private final Object m_cacheMutex = new Object();

	private final Lock m_waitForData = new ReentrantLock();
	private final Condition m_hasDataArrived = m_waitForData.newCondition();
	private int m_currentUpdateCount = 0;

	private volatile boolean m_keepThreadAlive = true;

	private StationAccessor() {
		m_self = getSelfSocket();
		m_station = pollStationConnection();
		m_stationInnerReader = getInnerReader();
		m_stationReader = getReader();
		m_matchInfo = null;// safeJsonAsMatchData();
		Robot.getRobotLogger().config(
				"connection between station and robot successfully set");
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
		synchronized (m_cacheMutex) {
			StationDataCache tmp = safeJsonAsCache();
			if (tmp == null)
				reAttainConnection();
			else
				m_cache = tmp;
		}

		m_hasDataArrived.signalAll();
		m_currentUpdateCount++;
	}

	public void waitForData() {
		waitForData(0);
	}

	public boolean waitForData(long timeout) {
		long startTime = RobotClock.currentTimeMicros();
		m_waitForData.lock();
		try {
			int currentUpdateCount = m_currentUpdateCount;

			while (currentUpdateCount == m_currentUpdateCount) {
				if (timeout > 0) {

					long now = RobotClock.currentTimeMicros();
					if (now < startTime + timeout) {
						boolean signaled = m_hasDataArrived.await(startTime
								+ timeout - now, TimeUnit.MICROSECONDS);

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
	public float getJoystickAxis(int stick, int axis)
			throws IllegalArgumentException {
		if (stick < 0 || stick >= JOYSTICK_COUNT)
			throw new IllegalArgumentException("Joystick index out of range: '"
					+ stick + "', expected 0 - " + (JOYSTICK_COUNT - 1));

		synchronized (m_cacheMutex) {
			if (!m_cache.joystickData[stick].isValid) {
				reportJoystickUnpluggedError("Invalid joystick input on port '"
						+ stick + "', check if it is connected");
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
	 * @param pov
	 *            Requested joystick pov button
	 * @return The state of requested pov button (is it pressed)
	 * @throws IllegalArgumentException
	 *             if {@code stick < 0} or {@code stick >= JOYSTICK_COUNT}
	 */
	public boolean getJoystickPov(int stick, int pov)
			throws IllegalArgumentException {
		if (stick < 0 || stick >= JOYSTICK_COUNT)
			throw new IllegalArgumentException("Joystick index out of range: '"
					+ stick + "', expected 0 - " + (JOYSTICK_COUNT - 1));

		synchronized (m_cacheMutex) {
			if (!m_cache.joystickData[stick].isValid) {
				reportJoystickUnpluggedError("Invalid joystick input on port '"
						+ stick + "', check if it is connected");
				return false;
			} else {
				return m_cache.joystickData[stick].povs[pov];
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
	public boolean getJoystickButton(int stick, int button)
			throws IllegalArgumentException {
		if (stick < 0 || stick >= JOYSTICK_COUNT)
			throw new IllegalArgumentException("Joystick index out of range: '"
					+ stick + "', expected 0 - " + (JOYSTICK_COUNT - 1));

		synchronized (m_cacheMutex) {
			if (!m_cache.joystickData[stick].isValid) {
				reportJoystickUnpluggedError("Invalid joystick input on port '"
						+ stick + "', check if it is connected");
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
			m_stationInnerReader.close();
			m_stationReader.close();
		} catch (IOException e) {
		}
	}

	@Override
	public void run() {
		while (m_keepThreadAlive)
			aquireData();
	}

	public InetSocketAddress getStationConnectionAddress() {
		return (InetSocketAddress) m_station.getRemoteSocketAddress();
	}

	private StationDataCache safeJsonAsCache() {
		String jsonObj = safeGetString();
		if (jsonObj == null)
			return null;
		try {
			StationDataCache ret;
			if ((ret = JSON_CACHE_PARSER.fromJson(jsonObj)) == null) {
				logJsonParseError(jsonObj);
				return null;
			} else {
				return ret;
			}
		} catch (IOException e) {
			logJsonParseError(jsonObj);
			return null;
		}
	}

	private MatchSpecificData safeJsonAsMatchData() {
		String jsonObj = safeGetString();
		if (jsonObj == null)
			return null;

		try {
			MatchSpecificData ret;
			if ((ret = JSON_MATCH_DATA_PARSER.fromJson(jsonObj)) == null) {
				logJsonParseError(jsonObj);
				return null;
			} else {
				return ret;
			}
		} catch (IOException e) {
			logJsonParseError(jsonObj);
			return null;
		}
	}

	private String safeGetString() {
		try {
			return m_stationReader.readLine();
		} catch (SocketTimeoutException e) {
		} catch (IOException e) {
			logSocketError(e);
		}
		return "";
	}

	private void logJsonParseError(String jsonObj) {
		Robot.getRobotLogger().severe("Error parsing " + jsonObj);
	}

	private void logSocketError(IOException e) {
		Robot.getRobotLogger().severe("IOException: " + e.getMessage());
		Sound.buzz();
	}

	private void reportJoystickUnpluggedError(String message) {
		long currentTime = RobotClock.currentTimeMicros();
		if (currentTime > m_nextJoystickUnpluggedMessageTime) {
			Robot.getRobotLogger().warning(message);
			m_nextJoystickUnpluggedMessageTime = currentTime
					+ NEXT_JOYSTICK_UNPLUGGED_MESSAGE_INTERVAL;
		}
	}

	private ServerSocket getSelfSocket() {
		ServerSocket ret = null;
		try {
			ret = new ServerSocket(4590);
			ret.setSoTimeout(SERVER_TIMEOUT);
		} catch (IOException e) {
			Robot.getRobotLogger().severe(
					"could not setup connection between station and robot");
			Robot.exit(9970);
		}
		return ret;
	}

	private Socket getStationSocket() {
		Socket ret = null;
		try {
			Sound.twoBeeps();
			ret = m_self.accept();
			ret.setSoTimeout(STATION_TIMEOUT);
		} catch (SocketTimeoutException e) {
		} catch (IOException e) {
			try {
				if (ret != null) {
					ret.close();
				}
			} catch (IOException e1) {
			}
			Robot.getRobotLogger().severe(
					"could not setup connection between station and robot");
			Robot.exit(9970);
		}
		return ret;
	}

	private InputStreamReader getInnerReader() {
		try {
			return new InputStreamReader(m_station.getInputStream());
		} catch (IOException e) {
			Robot.getRobotLogger().severe(e.toString());
			return null;
		}
	}

	private BufferedReader getReader() {
		return new BufferedReader(m_stationInnerReader);
	}

	private void reAttainConnection() {
		try {
			m_station.close();
			m_stationInnerReader.close();
			m_stationReader.close();
		} catch (IOException e) {
		}
		m_station = pollStationConnection();
		m_stationInnerReader = getInnerReader();
		m_stationReader = getReader();
	}

	private Socket pollStationConnection() {
		Socket ret = getStationSocket();
		while (ret == null && Button.ESCAPE.isUp())
			ret = getStationSocket();
		if (ret == null)
			Robot.exit(-1);
		return ret;
	}
}
