package org.greenblitz.gbEV3.common;

import java.io.IOException;
import java.lang.reflect.Field;

import org.greenblitz.gbEV3.commandbased.Robot;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;

public class GsonMessageAnalyzer<T> implements MessageAnalyzer<T> {

	public static final Gson gson = new Gson();

	private final TypeAdapter<T> mAdapter;
	private final Field[] mClsFields;

	public GsonMessageAnalyzer(Class<T> cls) {
		mAdapter = gson.getAdapter(cls);
		mClsFields = cls.getDeclaredFields();
		for (Field f : mClsFields) 
			if (!f.isAccessible()) f.setAccessible(true);
	}

	@Override
	public boolean test(String msg) {
		try {
			T t = mAdapter.fromJson(msg);
			if (t == null) return false;
			for (Field f : mClsFields) {
				try {
					if (f.get(t) != null) return true;
				} catch (IllegalArgumentException | IllegalAccessException e) {
					Robot.getRobotLogger().error(e);
				}
			}
			return false;
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
