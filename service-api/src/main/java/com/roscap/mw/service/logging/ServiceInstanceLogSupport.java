package com.roscap.mw.service.logging;

import java.io.IOException;
import java.util.UUID;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * 
 * 
 * 
 * @author is.zharinov
 *
 */
public class ServiceInstanceLogSupport {
	/**
	 * 
	 * @param instanceId
	 */
	public static void prepareLog(UUID instanceId) {
		try {
			FileAppender fa =
					new FileAppender(new PatternLayout("%d{ABSOLUTE} %-5p [%c{1}] %m%n"), instanceId.toString() + ".log", false);
			fa.setName(instanceId.toString());
			fa.setThreshold(Level.DEBUG);
			fa.setAppend(true);
			fa.setEncoding("UTF-8");
			fa.activateOptions();
			
			Logger.getRootLogger().addAppender(fa);
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.err);
		}
	}
}
