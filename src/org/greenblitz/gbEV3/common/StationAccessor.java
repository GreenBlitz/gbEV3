package org.greenblitz.gbEV3.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.IllegalBlockingModeException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

public final class StationAccessor {

	public enum Alliance {
		RED1(1), RED2(2), RED3(3),
		BLUE1(1), BLUE2(2), BLUE3(3),
		NONE(0);
		
		public final int position;
		
		private Alliance(int position) {
			this.position = position;
		}
		
		public static boolean isRedAlliance(Alliance a) {
			return a == RED1 || a == RED2 || a == RED3;
		}
		
		public static boolean isBlueAlliance(Alliance a) {
			return a == BLUE1 || a == BLUE2 || a == BLUE3;
		}
	}

	public enum GameType {
		TELEOP, AUTO, INVALID
	}

	private static class NativeJoystickData {
		public float[] axes;
		public boolean[] povs;
		public boolean[] buttons;

		public NativeJoystickData(int axesCount, int buttonCount, int povsCount) {
			axes = new float[axesCount];
			povs = new boolean[buttonCount];
			buttons = new boolean[povsCount];
		}

		public NativeJoystickData() {
			this(6, 10, 8);
		}
	}
	
	private static class MatchSpecificData {
		public final String eventName;
		public final String gameSpecificMessage;
		public final int matchNumber;
		public final GameType matchType;		
		
		public MatchSpecificData(String eventName, String gameSpecificMessage, int matchNumber, GameType matchType) {
			this.eventName = eventName;
			this.gameSpecificMessage = gameSpecificMessage;
			this.matchNumber = matchNumber;
			this.matchType = matchType;		
		}
		
		public MatchSpecificData(String gameSpecificMessage, int matchNumber, GameType matchType) {
			this("Treasure Island", gameSpecificMessage, matchNumber, matchType);
		}
		
		public MatchSpecificData(String gameSpecificMessage, int matchNumber) {
			this(gameSpecificMessage, matchNumber, GameType.INVALID);
		}
	}

	private static class StationDataCache {
		public NativeJoystickData[] joystickData;
		public Alliance alliance;
		public GameType gameType;
		public boolean isEnabled;

		public StationDataCache(int joystickCount) {
			joystickData = new NativeJoystickData[joystickCount];
			for (int i = 0; i < joystickData.length; i++) {
				joystickData[i] = new NativeJoystickData();
			}
			alliance = Alliance.NONE;
			gameType = GameType.INVALID;
		}

		public StationDataCache() {
			this(JOYSTICK_COUNT);
		}
	}

	public static final int JOYSTICK_COUNT = 2;
	
	private static final long NEXT_JOYSTICK_UNPLUGGED_MESSAGE_TIME = 1000;
	private static final String REQUIRED_JOYSTICK_UNPLUGGED_MESSAGE = "Joystick unplugged: ";

	private static final int SOCKET_TIMEOUT = 500;	
	
	private static final Gson GSON_OBJECT = new Gson();
	private static final TypeAdapter<StationDataCache> JSON_CACHE_PARSER =
			GSON_OBJECT.getAdapter(StationDataCache.class);
	private static final TypeAdapter<MatchSpecificData> JSON_MATCH_DATA_PARSER = 
			GSON_OBJECT.getAdapter(MatchSpecificData.class);
	
	
	private static final StationAccessor instance = new StationAccessor();
	
	
	private final ServerSocket SELF;
	private final Socket STATION;
	private final BufferedReader STATION_READER;
	private final InputStreamReader STATION_INNER_READER;
	private final PrintStream STATION_WRITER;
	
	private long m_lastJoystickUnpluggedMessageTime = -1;
	private StationDataCache m_cache = new StationDataCache();
	private final MatchSpecificData m_matchInfo;
	private final Object m_cacheMutex = new Object();

	private final Lock m_waitForData = new ReentrantLock();
	private final Condition m_hasDataArrived = m_waitForData.newCondition();
	private int m_currentUpdateCount = 0;

	private StationAccessor() {
		try {
			SELF = new ServerSocket(4590);
			SELF.setSoTimeout(SOCKET_TIMEOUT);
			STATION = SELF.accept();
			STATION_INNER_READER = new InputStreamReader(STATION.getInputStream());
			STATION_READER = new BufferedReader(STATION_INNER_READER);
			STATION_WRITER = new PrintStream(STATION.getOutputStream());
		} catch (IOException | SecurityException | IllegalBlockingModeException e) {
			System.exit(9970);
			throw new RuntimeException();
		}
		m_matchInfo = safeJsonAsMatchData();
	}

	public static StationAccessor getInstance() {
		return instance;
	}

	public boolean isDisabled() {
		synchronized (m_cacheMutex) {
			return !m_cache.isEnabled;
		}
	}

	public boolean isEnabled() {
		synchronized (m_cacheMutex) {
			return m_cache.isEnabled;
		}
	}

	private void aquireData() {
		synchronized (m_cacheMutex) {
			m_cache = safeJsonAsCache();
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

	public GameType getCurrentGameType() {
		synchronized (m_cacheMutex) {
			return m_cache.gameType;
		}
	}

	public Alliance getAlliance() {
		synchronized (m_cacheMutex) {
			return m_cache.alliance;
		}
	}
	
	private StationDataCache safeJsonAsCache() {
		String jsonObj = safeGetString();
		
		try {
			StationDataCache ret;
			if ((ret = JSON_CACHE_PARSER.fromJson(jsonObj)) == null) {
				logJsonParseError(jsonObj);
				System.exit(9971);
				return null;
			} else {
				return ret;
			}
		} catch (IOException e) {
			logJsonParseError(jsonObj);
			System.exit(9971);
			return null;
		}
	}
	
	private MatchSpecificData safeJsonAsMatchData() {
		String jsonObj = safeGetString();
		
		try {
			MatchSpecificData ret;
			if ((ret = JSON_MATCH_DATA_PARSER.fromJson(jsonObj)) == null) {
				logJsonParseError(jsonObj);
				System.exit(9971);
				return null;
			} else {
				return ret;
			}
		} catch (IOException e) {
			logJsonParseError(jsonObj);
			System.exit(9971);
			return null;
		}
	}
	
	private String safeGetString() {
		try {
			return STATION_READER.readLine();
		} catch (IOException e) {
			logSocketError(e);
			System.exit(9970);
			return "";
		}
	}
	
	private void logJsonParseError(String jsonObj) {
		// TODO: implement this after merge
	}
	
	private void logSocketError(IOException e) {
		// TODO: implement this after merge
	}
}
