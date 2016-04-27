package cn.ddb.hbase.util;

import java.math.BigInteger;

public class KeyUtil {

	public static String getNextKey(String rowkey) {
		if(rowkey == null || rowkey.length() == 0) return null;
		BigInteger b = new BigInteger(rowkey.getBytes());
		return new String(b.add(BigInteger.ONE).toByteArray());
	}
	
	public static String getPreviousKey(String rowkey) {
		if(rowkey == null || rowkey.length() == 0) return null;
		BigInteger b = new BigInteger(rowkey.getBytes());
		return new String(b.add(BigInteger.ONE.negate()).toByteArray());
	}
	
}
