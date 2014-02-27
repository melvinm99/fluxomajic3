package com.sistemaits.optima.fluxomajic3;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class SimpleCache {

	private static Cache<String, String> test;
	
	static {
		test = CacheBuilder.newBuilder()
				.expireAfterWrite(1, TimeUnit.MINUTES)
				.build();
	}
	
	public static void setTest(String key, String value){
		test.put(key, value);
	}
	
	public static String getTest(String key) {
		return test.getIfPresent(key);
	}
	
}
