package org.pcmm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads the PCMM Properties file.
 * 
 */
public class PCMMProperties implements PCMMConstants {

	private static Properties properties;
	private static Logger logger = LoggerFactory.getLogger(PCMMProperties.class);

	static {
		try {
			InputStream in = PCMMProperties.class.getClassLoader().getResourceAsStream("pcmm.properties");
			properties = new Properties();
			properties.load(in);
			in.close();
		} catch (IOException ie) {
			logger.error(ie.getMessage());
		}
	}

	protected static String get(String key) {
		return properties.getProperty(key);
	}

	@SuppressWarnings("unchecked")
	public static <T> T get(String key, Class<T> type, Object _default) {
		String prop = get(key);
		if (prop != null && !prop.isEmpty()) {
			if (Boolean.class.isAssignableFrom(type))
				return (T) Boolean.valueOf(prop);
			else if (Integer.class.isAssignableFrom(type))
				return (T) Integer.valueOf(prop);
			else if (Short.class.isAssignableFrom(type))
				return (T) Short.valueOf(prop);
			if (Float.class.isAssignableFrom(type))
				return (T) Float.valueOf(prop);
			if (Long.class.isAssignableFrom(type))
				return (T) Long.valueOf(prop);
			if (Double.class.isAssignableFrom(type))
				return (T) Double.valueOf(prop);
			else if (String.class.isAssignableFrom(type))
				return (T) prop;
		}
		return (T) _default;
	}

	public static <T> T get(String key, Class<T> type) {
		return get(key, type, null);
	}

}
