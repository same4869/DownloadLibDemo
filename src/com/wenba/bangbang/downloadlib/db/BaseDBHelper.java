package com.wenba.bangbang.downloadlib.db;

import java.util.List;

import android.os.UserManager;

/**
 * 
 * @Author:Lijj
 * @Date:2014-5-16上午10:49:15
 * @Todo:TODO
 */
public abstract class BaseDBHelper<T> {
	protected String getUserId() {
		String userId = "12345";//UserManager.getCurUserId();
		if (userId == null) {
			return null;
		}
		return userId;
	}

	public abstract String getTable();

	public abstract void save(T obj);

	public abstract void update(T obj);

	public abstract void delete(String id);

	public abstract void deleteAll();

	public abstract T find(String id);

	public abstract int getCount();

	public abstract List<T> getAllData();
}
