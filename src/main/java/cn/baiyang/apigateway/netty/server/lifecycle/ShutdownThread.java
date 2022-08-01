package cn.baiyang.apigateway.netty.server.lifecycle;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShutdownThread extends Thread {

	private static Logger log = LoggerFactory.getLogger(ShutdownThread.class);

	private static final ShutdownThread SHUTDOWN_THREAD = new ShutdownThread();

	private boolean hooked;

	private final List<LifeCycle> lifeCycles = new CopyOnWriteArrayList<LifeCycle>();

	/**
	 * Default constructor for the singleton
	 * <p>
	 * Registers the instance as shutdown hook with the Java Runtime
	 */
	private ShutdownThread() {
		this.setName("ShutdownThread");
	}

	private synchronized void hook() {
		try {
			if (!hooked)
				Runtime.getRuntime().addShutdownHook(this);
			hooked = true;
		}
		catch (Exception e) {
			log.error(e.getMessage());
			log.info("shutdown already commenced");
		}
	}

	private synchronized void unhook() {
		try {
			hooked = false;
			Runtime.getRuntime().removeShutdownHook(this);
		}
		catch (Exception e) {
			log.error(e.getMessage());
			log.info("shutdown already commenced");
		}
	}

	/**
	 * Returns the instance of the singleton
	 * @return the singleton instance of the {@link ShutdownThread}
	 */
	public static ShutdownThread getInstance() {
		return SHUTDOWN_THREAD;
	}

	public static synchronized void register(LifeCycle... lifeCycles) {
		log.info("register {}", Arrays.asList(lifeCycles));
		SHUTDOWN_THREAD.lifeCycles.addAll(Arrays.asList(lifeCycles));
		if (SHUTDOWN_THREAD.lifeCycles.size() > 0)
			SHUTDOWN_THREAD.hook();
	}

	public static synchronized void register(int index, LifeCycle... lifeCycles) {
		log.info("register {}", Arrays.asList(lifeCycles));
		SHUTDOWN_THREAD.lifeCycles.addAll(index, Arrays.asList(lifeCycles));
		if (SHUTDOWN_THREAD.lifeCycles.size() > 0)
			SHUTDOWN_THREAD.hook();
	}

	public static synchronized void deregister(LifeCycle lifeCycle) {
		log.info("deregister {}", lifeCycle);
		SHUTDOWN_THREAD.lifeCycles.remove(lifeCycle);
		if (SHUTDOWN_THREAD.lifeCycles.size() == 0)
			SHUTDOWN_THREAD.unhook();
	}

	@Override
	public void run() {
		for (LifeCycle lifeCycle : SHUTDOWN_THREAD.lifeCycles) {
			try {
				lifeCycle.stop();
				log.debug("Stopped {}", lifeCycle.toString());
			}
			catch (Exception ex) {
				log.error(ex.getMessage());
			}
		}
	}

}
