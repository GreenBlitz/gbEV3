package org.greenblitz.gbEV3.common;

import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

public class GsonMessageAnalyzer<T> implements MessageAnalyzer<T> {

	public static final Gson gson = new Gson();

	private TypeAdapter<T> mAdapter;

	public GsonMessageAnalyzer(Class<T> cls) {
		mAdapter = gson.getAdapter(cls);
	}

	@Override
	public boolean test(String msg) {
		try {
			T t = mAdapter.fromJson(msg);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public T parse(String msg) {		
		try {
			return mAdapter.fromJson(msg);
		} catch (IOException e) {
			return null;
		}
	}

}
