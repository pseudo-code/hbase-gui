package cn.ddb.hbase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableMap;
import java.util.stream.Collectors;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.hadoop.hbase.client.Delete;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.client.Scan;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.filter.ColumnPaginationFilter;
import org.apache.hadoop.hbase.filter.CompareFilter;
import org.apache.hadoop.hbase.filter.CompareFilter.CompareOp;
import org.apache.hadoop.hbase.filter.Filter;
import org.apache.hadoop.hbase.filter.FilterList;
import org.apache.hadoop.hbase.filter.PageFilter;
import org.apache.hadoop.hbase.filter.RegexStringComparator;
import org.apache.hadoop.hbase.filter.RowFilter;
import org.apache.hadoop.hbase.util.Bytes;

import cn.ddb.hbase.modal.HCell;
import cn.ddb.hbase.modal.HRow;
import cn.ddb.hbase.modal.HTableTreeNode;

/**
 * hbase相关接口处理以及数据转换。负责封装与hbase的通讯。
 * 
 * @author venia
 */
public class Context {
	
	private static final String STR_ZOOKEEPER_QUORUM 		= "hbase.zookeeper.quorum";
	private static final String STR_ZOOKEEPER_CLIENT_PORT	= "hbase.zookeeper.property.clientPort";
	
	private Configuration configuration;
	
	private Connection connection;
	
	// is connected to hbase or not
	private boolean isConnected = false;
	
	public Context() {
		configuration = HBaseConfiguration.create();
	}
	
	/** 连接到zookeeper */
	public void initConnection(String zookeeperAddr, String port) throws IOException {
		if(zookeeperAddr == null || port == null){
			throw new NullPointerException("zookeeper地址和端口不能为空");
		}
		
		zookeeperAddr	= zookeeperAddr.replace(" ", "");
		port			= port.replace(" ", "");
		
		if(zookeeperAddr.length() == 0 || port.length() == 0){
			throw new NullPointerException("zookeeper地址和端口不能为空");
		}
		
		configuration.set(STR_ZOOKEEPER_QUORUM, zookeeperAddr);
		configuration.set(STR_ZOOKEEPER_CLIENT_PORT, port);
		
		connection = ConnectionFactory.createConnection(configuration);
		isConnected = true;
	}
	
	/** 检查是否已连接 */
	public void checkConnectStat() {
		if(!isConnected) throw new IllegalStateException("未连接到zookeeper");
	}
	
	/** disConnnect */
	public void disConnnect() {
		isConnected = false;
		try {
			connection.close();
			connection = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/** 获取集群状态 */
	public ClusterStatus getClusterStatus() throws IOException {
		checkConnectStat();
		Admin admin = connection.getAdmin();
		ClusterStatus clusterStatus = admin.getClusterStatus();
		admin.close();
		return clusterStatus;
	}
	
	/** 获取所有表的树结构 */
	public HTableTreeNode getTableRoot() throws IOException {
		checkConnectStat();
		HashMap<String, List<String>> map = new HashMap<String, List<String>>();
		
		Admin admin = connection.getAdmin();
		TableName[] tableNames = admin.listTableNames();
		Arrays.asList(tableNames).forEach(tn -> {
			String ns = tn.getNamespaceAsString();
			if(map.containsKey(ns)){
				map.get(ns).add(tn.getNameAsString());
			}
			else {
				List<String> l = new ArrayList<String>();
				l.add(tn.getNameAsString());
				map.put(ns, l);
			}
		});
		
		admin.close();
		
		HTableTreeNode root = new HTableTreeNode(configuration.get(STR_ZOOKEEPER_QUORUM));
		map.forEach((ns, list) -> {
			HTableTreeNode child = new HTableTreeNode(ns);
			list.forEach(s -> child.addChild(new HTableTreeNode(s).setLeaf(true)));
			root.addChild(child);
		});
		return root;
	}
	
	/** 获取表的列簇 */
	public List<String> getColumnFamilies(String tableName) throws IOException {
		checkStringNotEmpty(tableName);
		checkConnectStat();
		
		Table table = connection.getTable(TableName.valueOf(tableName));
		return Arrays.asList(table.getTableDescriptor().getColumnFamilies())
			.stream()
			.map(cfd -> cfd.getNameAsString())
			.collect(Collectors.toList());
	}
	
	/** 获取表的row */
	public List<HRow> scanRows(String tableName, String startRow, long limit, boolean reversed, String regex) throws IOException {
		checkStringNotEmpty(tableName);
		checkConnectStat();
		
		if(limit <= 0) throw new IllegalArgumentException("The param 'limit' should be a integer bigger than 0.");
		
		Scan scan = new Scan();
		FilterList filterList = new FilterList(FilterList.Operator.MUST_PASS_ALL);
		if(regex != null && regex.length() > 0) {
			filterList.addFilter(new RowFilter(CompareOp.EQUAL, new RegexStringComparator(regex)));
		}
		filterList.addFilter(new PageFilter(limit));
		
		scan.setFilter(filterList);
		scan.setMaxResultsPerColumnFamily(1); // 只返回一条数据
		scan.setReversed(reversed);
		if(startRow != null && startRow.length() > 0) scan.setStartRow(startRow.getBytes());
		
		Table table = connection.getTable(TableName.valueOf(tableName));
		ResultScanner scanner = table.getScanner(scan);
		
		List<HRow> rows = new ArrayList<HRow>();
		scanner.forEach((Result r) -> {
			HRow row = new HRow(Bytes.toString(r.getRow()));
			rows.add(row);
		});
		
		if(reversed) Collections.reverse(rows); // in reversed mode, hbase client return the data in a reversed order.
		
		scanner.close();
		table.close();
		return rows;
	}
	
	/** 创建表 */
	public void createTable(String tableName, List<String> columnFamilies) throws IOException {
		if(tableName == null || tableName.length() == 0
				|| columnFamilies == null || columnFamilies.size() == 0)
			throw new IllegalArgumentException("表名和列簇不能为空");
		
		checkConnectStat();
		
		HTableDescriptor table = new HTableDescriptor(TableName.valueOf(tableName));
		columnFamilies.forEach(s -> table.addFamily(new HColumnDescriptor(s)));
		
		Admin admin = connection.getAdmin();
		admin.createTable(table);
		admin.close();
	}

	/** 删除表 */
	public void deleteTable(String tableName) throws IOException {
		checkStringNotEmpty(tableName);
		checkConnectStat();
		
		Admin admin = connection.getAdmin();
		admin.disableTable(TableName.valueOf(tableName));
		admin.deleteTable(TableName.valueOf(tableName));
		admin.close();
	}
	
	/** 新增数据 */
	public void insert(String tableName, String rowKey, List<HCell> cells) throws IOException {
		checkStringNotEmpty(tableName, rowKey);
		if(cells == null) throw new NullPointerException();
		cells.forEach(c -> checkStringNotEmpty(c.getColumnFamily(), c.getQualifier(), c.getValue()));
		checkConnectStat();
		
		Put put = new Put(rowKey.getBytes());
		cells.forEach(c -> put.addColumn(c.getColumnFamily().getBytes(), c.getQualifier().getBytes(), c.getValue().getBytes()));
		Table table = connection.getTable(TableName.valueOf(tableName));
		table.put(put);
		table.close();
	}
	
	/** 更新行 */
	public void updateRow(String tableName, String rowKey, List<HCell> cells) throws IOException {
		checkStringNotEmpty(tableName, rowKey);
		if(cells == null) throw new NullPointerException();
		cells.forEach(c -> checkStringNotEmpty(c.getColumnFamily(), c.getQualifier(), c.getValue()));
		
		checkConnectStat();
		
		Put put = new Put(rowKey.getBytes());
		cells.forEach(c -> put.addColumn(c.getColumnFamily().getBytes(), c.getQualifier().getBytes(), c.getValue().getBytes()));
		Table table = connection.getTable(TableName.valueOf(tableName));
		table.put(put);
		table.close();
	}
	
	/** 删除行 */
	public void deleteRows(String tableName, List<String> rowKeys) throws IOException{
		checkStringNotEmpty(tableName);
		if(rowKeys == null || rowKeys.size() == 0) return; // 没有数据可以进行操作
		checkConnectStat();
		
		List<Delete> deletes = new ArrayList<Delete>();
		rowKeys.forEach(s -> deletes.add(new Delete(s.getBytes())));
		Table table = connection.getTable(TableName.valueOf(tableName));
		table.delete(deletes);
		table.close();
	}
	
	/** 获取一行数据 */
	public List<HCell> getCells(String tableName, String rowKey, String columnFamily, String qualifier, int pageNum, int pageCount) throws IOException {
		checkStringNotEmpty(tableName, rowKey);
		checkConnectStat();
		
		if(pageNum < 0) throw new IllegalArgumentException("The param 'pageNum' should be a integer bigger than or equal 0.");
		if(pageCount <= 0) throw new IllegalArgumentException("The param 'pageCount' should be a integer bigger than 0.");
		
		Table table = connection.getTable(TableName.valueOf(tableName));
		Get get = new Get(rowKey.getBytes());
		get.setFilter(new ColumnPaginationFilter(pageCount, pageCount * pageNum));
		if(columnFamily != null && columnFamily.length() > 0) {
			if(qualifier != null && qualifier.length() > 0) get.addColumn(columnFamily.getBytes(), qualifier.getBytes());
			else get.addFamily(columnFamily.getBytes());
		}
		
		Result result = table.get(get);
		
		final List<HCell> list = new ArrayList<HCell>();
		NavigableMap<byte[], NavigableMap<byte[], byte[]>> noVersionMap = result.getNoVersionMap();
		if(noVersionMap == null) return list;
		noVersionMap.forEach((byte[] fc, NavigableMap<byte[], byte[]> map)->{
			String fcn = Bytes.toString(fc);
			map.forEach((q,v)->{
				HCell item = new HCell(fcn, Bytes.toString(q), Bytes.toString(v));
				list.add(item);
			});
		});
		return list;
	}
	
	/** 删除列，注意：这里不是删除单元格，而是删除单元格对应的列 */
	public void deleteColumn(String tableName, String rowKey, List<HCell> cells) throws IOException {
		checkStringNotEmpty(tableName, rowKey);
		if(cells == null || cells.size() == 0) return;
		cells.forEach(c -> checkStringNotEmpty(c.getColumnFamily(), c.getQualifier()));
		checkConnectStat();
		
		Delete delete = new Delete(rowKey.getBytes());
		cells.stream()
			.map((c) -> new String[]{c.getColumnFamily(), c.getQualifier()})
			.distinct()
			.forEach((String[] strs) -> delete.addColumn(strs[0].getBytes(), strs[1].getBytes()));
		Table table = connection.getTable(TableName.valueOf(tableName));
		table.delete(delete);
	}
	
	// Assert the string is not null and not empty.
	private static void checkStringNotEmpty(String... args) {
		for(String s : args){
			if(s == null || s.length() == 0)
				throw new IllegalArgumentException("IllegalArgumentException");
		}
	}
	
	public static void main(String[] args) throws IOException {
		System.out.println("start:" + new Date());
		Scan scan = new Scan();
        System.out.println("正则获取结尾为5的行");

		Configuration conf = HBaseConfiguration.create();
//        conf.set("hbase.zookeeper.quorum", "master-hadoop,slave1-hadoop,slave2-hadoop");//使用eclipse时必须添加这个，否则无法定位
        conf.set("hbase.zookeeper.quorum", "slave1-hadoop");//使用eclipse时必须添加这个，否则无法定位

        
        conf.set("hbase.zookeeper.property.clientPort", "2181");
//        conf.set("hbase.master", "slave1-hadoop:60000"); 
        Connection connection;
		try {
			connection = ConnectionFactory.createConnection(conf);
			System.out.println("start1:" + new Date());
        Table table = connection.getTable(TableName.valueOf("trafic_rec"));
        Filter filter2 = new RowFilter(CompareFilter.CompareOp.EQUAL,
                        new RegexStringComparator("100000002$"));
        scan.setFilter(filter2);
        ResultScanner scanner2 = table.getScanner(scan);
		System.out.println("start2:" + new Date());
        for (Result res : scanner2) {
                System.out.println(res);
        }
        scanner2.close();
        table.close();
        connection.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("start3:" + new Date());

		
	}
}
