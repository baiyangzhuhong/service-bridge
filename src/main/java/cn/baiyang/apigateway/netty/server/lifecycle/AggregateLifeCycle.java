package cn.baiyang.apigateway.netty.server.lifecycle;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AggregateLifeCycle extends AbstractLifeCycle
		implements Destroyable, Dumpable {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractLifeCycle.class);

	private final List<Object> dependentBeans = new CopyOnWriteArrayList<>();

	@Override
	public void destroy() {
		for (Object o : dependentBeans) {
			if (o instanceof Destroyable) {
				((Destroyable) o).destroy();
			}
		}
		dependentBeans.clear();
	}

	@Override
	protected void doStart() throws Exception {
		for (Object o : dependentBeans) {
			if (o instanceof LifeCycle) {
				((LifeCycle) o).start();
			}
		}
		super.doStart();
	}

	@Override
	protected void doStop() throws Exception {
		super.doStop();
		List<Object> reverse = new ArrayList<Object>(dependentBeans);
		Collections.reverse(reverse);
		for (Object o : reverse) {
			if (o instanceof LifeCycle) {
				((LifeCycle) o).stop();
			}
		}
	}

	/* ------------------------------------------------------------ */

	/**
	 * Add an associated bean. The bean will be added to this LifeCycle and if it is also
	 * a {@link LifeCycle} instance, it will be started/stopped. Any beans that are also
	 * {@link Destroyable}, will be destroyed with the server.
	 * @param o the bean object to add
	 */
	public boolean addBean(Object o) {
		if (o == null) {
			return false;
		}
		boolean added = false;
		if (!dependentBeans.contains(o)) {
			dependentBeans.add(o);
			added = true;
		}

		try {
			if (isStarted() && o instanceof LifeCycle) {
				((LifeCycle) o).start();
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return added;
	}

	/* ------------------------------------------------------------ */

	/**
	 * Get dependent beans
	 * @return List of beans.
	 */
	public Collection<Object> getBeans() {
		return dependentBeans;
	}

	/* ------------------------------------------------------------ */

	/**
	 * Get dependent beans of a specific class
	 * @param clazz
	 * @return List of beans.
	 * @see #addBean(Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getBeans(Class<T> clazz) {
		ArrayList<T> beans = new ArrayList<>();
		for (Object o : dependentBeans) {
			if (clazz.isInstance(o)) {
				beans.add((T) o);
			}
		}
		return beans;
	}

	/* ------------------------------------------------------------ */

	/**
	 * Get dependent bean of a specific class. If more than one bean of the type exist,
	 * the first is returned.
	 * @param clazz
	 * @return bean or null
	 * @see #addBean(Object)
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBean(Class<T> clazz) {
		Iterator<?> iter = dependentBeans.iterator();
		T t = null;
		int count = 0;
		while (iter.hasNext()) {
			Object o = iter.next();
			if (clazz.isInstance(o)) {
				count++;
				if (t == null) {
					t = (T) o;
				}
			}
		}
		if (count > 1 && LOG.isDebugEnabled()) {
			LOG.debug("getBean(" + clazz.getName() + ") 1 of " + count);
		}

		return t;
	}

	/**
	 * Remove all associated bean.
	 */
	public void removeBeans() {
		dependentBeans.clear();
	}

	/**
	 * Remove an associated bean.
	 */
	public boolean removeBean(Object o) {
		if (o == null) {
			return false;
		}
		return dependentBeans.remove(o);
	}

	public void dumpStdErr() {
		try {
			dump(System.err, "");
		}
		catch (IOException e) {
			LOG.warn(e.getMessage());
		}
	}

	@Override
	public String dump() {
		return dump(this);
	}

	public static String dump(Dumpable dumpable) {
		StringBuilder b = new StringBuilder();
		try {
			dumpable.dump(b, "");
		}
		catch (IOException e) {
			LOG.warn(e.getMessage());
		}
		return b.toString();
	}

	public void dump(Appendable out) throws IOException {
		dump(out, "");
	}

	protected void dumpThis(Appendable out) throws IOException {
		out.append(String.valueOf(this)).append("\n");
	}

	@Override
	public void dump(Appendable out, String indent) throws IOException {
		dumpThis(out);
		dump(out, indent, dependentBeans);
	}

	public static void dump(Appendable out, String indent, Collection<?>... collections)
			throws IOException {
		if (collections.length == 0) {
			return;
		}
		int size = 0;
		for (Collection<?> c : collections) {
			size += c.size();
		}
		if (size == 0) {
			return;
		}
		int i = 0;
		for (Collection<?> c : collections) {
			for (Object o : c) {
				i++;
				out.append(indent).append(" +- ");

				if (o instanceof Dumpable) {
					((Dumpable) o).dump(out, indent + (i == size ? "    " : " |  "));
				}
				else {
					out.append(String.valueOf(o)).append("\n");
				}
			}

			if (i != size) {
				out.append(indent).append(" |\n");
			}

		}
	}

}
