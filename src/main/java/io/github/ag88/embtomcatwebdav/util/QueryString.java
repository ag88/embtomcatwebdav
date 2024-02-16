/*
 * 
 */
package io.github.ag88.embtomcatwebdav.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

/**
 * Class QueryString.
 * 
 * This class maintains the parameters in a {@code Map<String, String[]>}.
 * The query string for URL can then be generted via {@link getQueryString}.
 * 
 */
public class QueryString {

	private Log log = LogFactory.getLog(QueryString.class);
	
	Map<String, String[]> params;
	
	public QueryString() {
		params = new TreeMap<String, String[]>();
	}

	public void put(String key, String[] values) {
		params.put(key, values);
	}
	
	public void put(String key, String value) {
		if(params.containsKey(key)) {
			String[] ovalues = params.get(key);
			String[] values;
			values = Arrays.copyOf(ovalues, ovalues.length+1);
			values[values.length-1] = value;
			params.put(key, values);
		} else {
			String[] values = new String[1];
			values[0] = value;
			params.put(key, values);
		}
	}
	
	public void putAll(Map<String, String[]> params) {
		this.params.putAll(params);
	}
	
	public String[] get(String key) {
		return params.get(key);
	}
	
	public boolean containsKey(String key) {
		return params.containsKey(key);
	}
	
	public boolean isEmpty() {
		return params.isEmpty();
	}
	
	public String[] remove(String key) {
		return params.remove(key);
	}
	
	/**
	 * Gets the query string.
	 *
	 * @return the query string
	 */
	public String getQueryString() {
		StringBuilder sb = new StringBuilder(128);
		if(params.isEmpty())
			return "";				
		boolean first = true;		
		sb.append("?");
		for(String k : params.keySet()) {
			for(String v : params.get(k)) {
				if (first)
					first = false;
				else
					sb.append('&');
				sb.append(k);
				sb.append('=');				
				sb.append(encodevalue(v));
			}			
		}				
		return sb.toString();
	}
	
	
	protected String encodevalue(String value) {
		try {			
			return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			log.warn(e);
			return value;
		}		
	}
	
	public void clear() {
		params.clear();
	}
	
	public Map<String, String[]> getParams() {
		return params;
	}

	public void setParams(Map<String, String[]> params) {
		this.params = params;
	}		

}
