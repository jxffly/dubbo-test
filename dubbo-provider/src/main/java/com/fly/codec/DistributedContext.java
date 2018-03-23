package com.fly.codec;

import java.util.HashMap;
import java.util.Set;


/**
 *
 * @author nikodu
 *
 */
public final class DistributedContext {
	private static ThreadLocal<HashMap<String, Object>> threadLocal = new ThreadLocal<HashMap<String, Object>>() {
		@Override
		protected HashMap<String, Object> initialValue() {
			return new HashMap<String, Object>();
		}
	};

	public void clear() {
		getThreadLocal().clear();
	}

	public Object get(String name) {
		return getThreadLocal().get(name);
	}

	private HashMap<String, Object> getThreadLocal() {
		return threadLocal.get();
	}

	public Set<String> keySet() {
		return getThreadLocal().keySet();
	}

	public void put(String name, Object value) {
		getThreadLocal().put(name, value);
	}

	public int size() {
		return getThreadLocal().size();
	}
}
