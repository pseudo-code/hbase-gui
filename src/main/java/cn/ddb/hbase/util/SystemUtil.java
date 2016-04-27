package cn.ddb.hbase.util;

public class SystemUtil {

	// Get the system directory where the program located.
	public static final String getCurrentWorkingDir() {
		return System.getProperty("user.dir");
	}
	
	public static void main(String[] args) {
		System.out.println(getCurrentWorkingDir());
	}
	
}
