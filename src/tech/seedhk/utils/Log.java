package tech.seedhk.utils;

import java.io.File;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Log {
	
	
	public static Logger getInstance(Class<?> clazz){
		//System.out.println(Log.class.getResource("/").getPath());
		//System.out.println(Log.class.getResource("").getPath());
		//System.out.println(System.getProperty("user.dir"));
		//System.out.println(Thread.currentThread().getContextClassLoader().getResource("").getPath());
		//System.out.println(Log.class.getClassLoader().getResource("").getPath());
		//System.out.println(ClassLoader.getSystemResource(""));
		
		//InputStream is=Log.class.getClassLoader().getResourceAsStream("/log4j.properties");
		//String filePath=Log.class.getClassLoader().getResource("log4j.properties").getPath();
		String filePath=System.getProperty("user.dir").replace("\\", "/");
		PropertyConfigurator.configure(filePath+File.separator+"log4j.properties");
		Logger log=Logger.getLogger(clazz);
		return log;
	}
	
	
	public static void main(String[] args) {
		
		Logger log=getInstance(Log.class);
		log.info("info");
		log.warn("info");
		log.error("info");
		log.debug("info");
		System.out.println(log.getLevel());
		
	}


}
