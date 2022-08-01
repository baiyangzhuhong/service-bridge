package cn.baiyang.apigateway.netty.server.lifecycle;

import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractLifeCycle implements LifeCycle {

	private static final Logger log = LoggerFactory.getLogger(AbstractLifeCycle.class);

	private static final String STOPPED = "STOPPED";

	private static final String FAILED = "FAILED";

	private static final String STARTING = "STARTING";

	private static final String STARTED = "STARTED";

	private static final String STOPPING = "STOPPING";

	public static final String RUNNING = "RUNNING";

	private final Object lock = new Object();

	private final int failed = -1, stopped = 0, starting = 1, started = 2, stopping = 3;

	private volatile int state = stopped;

	private final CopyOnWriteArrayList<LifeCycle.Listener> listeners = new CopyOnWriteArrayList<LifeCycle.Listener>();

	private boolean synchronizeStart = true;

	public void setSynchronizeStart(boolean synchronizeStart) {
		this.synchronizeStart = synchronizeStart;
	}

	protected void doStart() throws Exception {
		ShutdownThread.register(this);
	}

	protected void doStop() throws Exception {
		log.error("shutdown deregister {}", this.toString());
		ShutdownThread.deregister(this);
	}

	@Override
	public final void start() throws Exception {
		if (synchronizeStart) {
			synchronized (lock) {
				innerStart();
			}
		}
		else {
			innerStart();
		}
	}

	private void innerStart() throws Exception {
		try {
			if (state == started || state == starting) {
				log.warn("is started");
				return;
			}
			setStarting();
			doStart();
			setStarted();
		}
		catch (Exception e) {
			setFailed(e);
			throw e;
		}
	}

	@Override
	public final void stop() throws Exception {
		if (synchronizeStart) {
			synchronized (lock) {
				innerStop();
			}
		}
		else {
			innerStop();
		}

	}

	private void innerStop() throws Exception {
		try {
			if (state == stopping || state == stopped) {
				return;
			}
			setStopping();
			doStop();
			setStopped();
		}
		catch (Exception e) {
			setFailed(e);
			throw e;
		}
	}

	@Override
	public boolean isRunning() {
		final int state = this.state;

		return state == started || state == starting;
	}

	@Override
	public boolean isStarted() {
		return state == started;
	}

	@Override
	public boolean isStarting() {
		return state == starting;
	}

	@Override
	public boolean isStopping() {
		return state == stopping;
	}

	@Override
	public boolean isStopped() {
		return state == stopped;
	}

	@Override
	public boolean isFailed() {
		return state == failed;
	}

	@Override
	public void addLifeCycleListener(LifeCycle.Listener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeLifeCycleListener(LifeCycle.Listener listener) {
		listeners.remove(listener);
	}

	private String getState() {
		switch (state) {
		case failed:
			return FAILED;
		case starting:
			return STARTING;
		case started:
			return STARTED;
		case stopping:
			return STOPPING;
		case stopped:
			return STOPPED;
		default:
			return null;
		}
	}

	public static String getState(LifeCycle lc) {
		if (lc.isStarting()) {
			return STARTING;
		}
		if (lc.isStarted()) {
			return STARTED;
		}
		if (lc.isStopping()) {
			return STOPPING;
		}
		if (lc.isStopped()) {
			return STOPPED;
		}
		return FAILED;
	}

	private void setStarted() {
		log.info("{} {} ", STARTED, this.toString());
		state = started;
		for (Listener listener : listeners) {
			listener.lifeCycleStarted(this);
		}
	}

	private void setStarting() {
		log.info("{} {}", STARTING, this.toString());
		state = starting;
		for (Listener listener : listeners) {
			listener.lifeCycleStarting(this);
		}
	}

	private void setStopping() {
		log.info("{} {}", STOPPING, this.toString());
		state = stopping;
		for (Listener listener : listeners) {
			listener.lifeCycleStopping(this);
		}
	}

	private void setStopped() {
		log.info("{} {}", STOPPED, this.toString());
		state = stopped;
		for (Listener listener : listeners) {
			listener.lifeCycleStopped(this);
		}
	}

	private void setFailed(Throwable th) {
		log.warn("{} {} : {}", FAILED, this, th);
		state = failed;
		for (Listener listener : listeners) {
			listener.lifeCycleFailure(this, th);
		}
	}

	public static abstract class AbstractLifeCycleListener implements LifeCycle.Listener {

		@Override
		public void lifeCycleFailure(LifeCycle event, Throwable cause) {
		}

		@Override
		public void lifeCycleStarted(LifeCycle event) {
		}

		@Override
		public void lifeCycleStarting(LifeCycle event) {
		}

		@Override
		public void lifeCycleStopped(LifeCycle event) {
		}

		@Override
		public void lifeCycleStopping(LifeCycle event) {
		}

	}

	@Override
	public String toString() {
		return super.toString() + "#" + getState();
	}

}
