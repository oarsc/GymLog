package org.scp.gymlog.util.json;

import org.json.JSONException;

@FunctionalInterface
public interface JsonConsumer<T> {
	void accept(T t) throws JSONException;
}
