package com.barchart.bench.caliper;

import java.util.Map;
import java.util.TreeMap;

public class CaliperUtil {

	public static void log(Map<?, ?> map){
		
		TreeMap<Object, Object> tree = new TreeMap<Object, Object> (map);
		
		for(Map.Entry<Object, Object> entry: tree.entrySet()){
			System.err.println(String.format("%s = %s", entry.getKey(), entry.getValue()));
		}
		
	}
	
}
