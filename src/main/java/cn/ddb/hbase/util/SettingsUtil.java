package cn.ddb.hbase.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class SettingsUtil {
	
	public static final String SETTING_FILE_NAME		= "setting.properties";

	public static final String ROW_LIMIT				= "row_limit";
	public static final String CELL_LIMIT				= "cell_limit";
	public static final String ZOOKEEPER_QUORUM			= "zookeeper_quorum";
	public static final String CLIENT_PORT				= "client_port";
	public static final String ZNODE_PARENT				= "znode_parent";
	
	public static final int DEFAULT_ROW_LIMIT			= 1000;
	public static final int DEFAULT_CELL_LIMIT			= 1000;
	
	public static final Properties properties 			= new Properties();
	public static boolean isLoaded						= false;
	
	public static void load() throws FileNotFoundException, IOException {
		synchronized (properties) {
			isLoaded = true;
			File file = new File(SystemUtil.getCurrentWorkingDir() + File.separator + SETTING_FILE_NAME);
			if(!file.exists()) return;
			
			properties.clear();
			properties.load(new FileInputStream(file));
		}
	}
	
	private static void checkLoad() {
		synchronized (properties) {
			if(isLoaded) return;
			else {
				try {
					load();
				} catch (Exception e) { // ignore
				}
			}
		}
	}
	
	public static int getRowLimit() {
		checkLoad();
		String rl = properties.getProperty(ROW_LIMIT);
		try {
			int int1 = Integer.parseInt(rl);
			return int1 > 0 ? int1 : DEFAULT_ROW_LIMIT;
		} catch (Exception e) { // ignore
		}
		return DEFAULT_ROW_LIMIT;
	}
	
	public static int getCellLimit() {
		checkLoad();
		String cl = properties.getProperty(CELL_LIMIT);
		try {
			int int1 = Integer.parseInt(cl);
			return int1 > 0 ? int1 : DEFAULT_CELL_LIMIT;
		} catch (Exception e) { // ignore
		}
		return DEFAULT_CELL_LIMIT;
	}
	
	public static String getZookeeperQuarum() {
		checkLoad();
		return properties.getProperty(ZOOKEEPER_QUORUM);
	}
	
	public static String getClientPort() {
		checkLoad();
		return properties.getProperty(CLIENT_PORT);
	}

	public static String getZnodeParent() {
		checkLoad();
		return properties.getProperty(ZNODE_PARENT);
	}

	
	private SettingsUtil() {} // No instant.
	
	
	public static void main(String[] args) {
		System.out.println(SettingsUtil.getCellLimit());
		System.out.println(SettingsUtil.getRowLimit());
		System.out.println(SettingsUtil.getZookeeperQuarum());
		System.out.println(SettingsUtil.getClientPort());
	}
}
