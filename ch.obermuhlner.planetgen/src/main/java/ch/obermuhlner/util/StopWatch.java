package ch.obermuhlner.util;

public class StopWatch {

	private static final double MILLISECONDS_PER_NANOSECOND = 1000 * 1000;

	private long startTime;

	private long stopTime;

	private State state = State.INITIAL;

	/**
	 * Constructs a {@link StopWatch}.
	 * 
	 * <p>Automatically {@link #start() starts} the stop watch.</p>
	 */
	public StopWatch() {
		this(true);
	}

	/**
	 * Constructs a {@link StopWatch}.
	 * 
	 * @param theAutoStart <code>true</code> to {@link #start() start} the watch automatically, <code>false</code> to not start it
	 */
	public StopWatch(boolean theAutoStart) {
		if (theAutoStart) {
			start();
		}
	}

	/**
	 * Starts the stop watch.
	 * 
	 * @return this StopWatch to chain calls.
	 */
	public StopWatch start() {
		state = State.STARTED;
		startTime = System.nanoTime();

		return this;
	}

	/**
	 * Resumes the stop watch.
	 * 
	 * @return this StopWatch to chain calls.
	 */
	public StopWatch resume() {
		if (state == State.STARTED) {
			throw new IllegalStateException("StopWatch already started");
		}
		startTime = System.nanoTime() - getElapsedNanoseconds();
		state = State.STARTED;
		return this;
	}

	/**
	 * Stops the stop watch.
	 * 
	 * @return this StopWatch to chain calls.
	 */
	public StopWatch stop() {
		if (state != State.STARTED) {
			throw new IllegalStateException("StopWatch not started");
		}

		state = State.STOPPED;
		stopTime = System.nanoTime();

		return this;
	}

	/**
	 * Returns the elapsed nanoseconds.
	 * 
	 * <p>If the stop watch has been started (but not stopped), this will return the elapsed time since starting.
	 * If called again, this will return a different elapsed time.</p>
	 * 
	 * <p>If the stop watch has been started and stopped, this will return the elapsed time between start and stop.
	 * If called again, this will return the same elapsed time.</p>
	 * 
	 * @return the elapsed nanoseconds
	 */
	public long getElapsedNanoseconds() {
		switch (state) {
			case INITIAL:
				return 0;

			case STARTED:
				return System.nanoTime() - startTime;

			case STOPPED:
				return stopTime - startTime;
		}

		return 0;
	}

	/**
	 * Returns the elapsed milliseconds.
	 * 
	 * <p>If the stop watch has been started (but not stopped), this will return the elapsed time since starting.
	 * If called again, this will return a different elapsed time.</p>
	 * 
	 * <p>If the stop watch has been started and stopped, this will return the elapsed time between start and stop.
	 * If called again, this will return the same elapsed time.</p>
	 * 
	 * @return the elapsed milliseconds
	 */
	public double getElapsedMilliseconds() {
		return getElapsedNanoseconds() / MILLISECONDS_PER_NANOSECOND;
	}

	/**
	 * Returns the elapsed seconds.
	 * 
	 * <p>If the stop watch has been started (but not stopped), this will return the elapsed time since starting.
	 * If called again, this will return a different elapsed time.</p>
	 * 
	 * <p>If the stop watch has been started and stopped, this will return the elapsed time between start and stop.
	 * If called again, this will return the same elapsed time.</p>
	 * 
	 * @return the elapsed milliseconds
	 */
	public double getElapsedSeconds() {
		return getElapsedMilliseconds() / 1000.0;
	}

	@Override
	public String toString() {
		return getElapsedMilliseconds() + " ms";
	}

	private enum State {
		INITIAL,
		STARTED,
		STOPPED
	}
}
