package cn.ddb.hbase;

import java.io.IOException;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.hadoop.hbase.ClusterStatus;
import org.apache.hadoop.hbase.regionserver.NoSuchColumnFamilyException;

import cn.ddb.hbase.component.ClusterDetailsDialog;
import cn.ddb.hbase.component.ConnectionDialog;
import cn.ddb.hbase.component.AddRowOrColumnDialog;
import cn.ddb.hbase.component.CreateTableDialog;
import cn.ddb.hbase.component.MainFrame;
import cn.ddb.hbase.component.MainFrame.MainFrameListener;
import cn.ddb.hbase.modal.HCell;
import cn.ddb.hbase.modal.HRow;
import cn.ddb.hbase.modal.HTableTreeNode;
import cn.ddb.hbase.util.KeyUtil;
import cn.ddb.hbase.util.SettingsUtil;

/**
 * 应用主入口，同时负责controller相关。
 * 
 * @author venia
 */
public class App {

	private MainFrame mainFrame;
	private Context context;
	private ConnectionDialog connectionDialog;
	private CreateTableDialog createTableDialog;
	private AddRowOrColumnDialog createRowDialog;
	private ClusterDetailsDialog clusterDetailsDialog;
	
	private int	cellPageNum		= 0;
	private int cellPageLimit	= SettingsUtil.getCellLimit();
	private int rowPageLimit	= SettingsUtil.getRowLimit();
	
	public void initUi() {
		mainFrame			= new MainFrame();
		context 			= new Context();
		connectionDialog 	= new ConnectionDialog(mainFrame);
		createTableDialog 	= new CreateTableDialog(mainFrame);
		createRowDialog		= new AddRowOrColumnDialog(mainFrame);
		clusterDetailsDialog= new ClusterDetailsDialog(mainFrame);
		
		mainFrame.setListener(new MainFrameListener() {
			@Override
			public void onConnect() {
				connectionDialog.setVisible(true);
			}
			
			@Override
			public void onDisconnect() {
				context.disConnnect();
			}
			
			@Override
			public void onClusterDetails() {
				// TODO
				try {
					ClusterStatus clusterStatus = context.getClusterStatus();
					clusterDetailsDialog.init(clusterStatus);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			@Override
			public void onCreateHTable() {
				createTableDialog.setVisible(true);
			}
			@Override
			public void onViewHTable(String tableName, String filteStr) {
				mainFrame.showProgress();
				new GetTableRowsWorker(tableName, null, false, rowPageLimit, filteStr).execute();
			}
			
			@Override
			public void onDeleteHTable(String tableName) {
				try {
					context.deleteTable(tableName);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				mainFrame.showProgress();
				new GetTableBootWorker().execute();
			}
			
			@Override
			public void onRefreshHTable() {
				mainFrame.showProgress();
				new GetTableBootWorker().execute();
				
			}
			
			@Override
			public void onRefreshHRows(String tableName, String rowKey, String filteStr) {
				mainFrame.showProgress();
				new GetTableRowsWorker(tableName, rowKey, false, rowPageLimit, filteStr).execute();
			}
			
			@Override
			public void onDeleteHRows(String tableName, List<String> rowKeys, String rowKey, String filteStr) {
				try {
					context.deleteRows(tableName, rowKeys);
				} catch (Exception e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				mainFrame.clearRowListAndCellTable();
				mainFrame.showProgress();
				new GetTableRowsWorker(tableName, rowKey, false, rowPageLimit, filteStr).execute();
			}
			
			@Override
			public void onAdd(String tableName, String rowKey) {
				List<String> columnFamilies;
				try {
					columnFamilies = context.getColumnFamilies(tableName);
					createRowDialog.init(tableName, columnFamilies, rowKey);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			@Override
			public void onUpdateHCells(String tableName, String rowKey, List<HCell> changedCells) {
				try {
					context.updateRow(tableName, rowKey, changedCells);
				} catch (IOException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}

			@Override
			public void nextHRowPage(String tableName, String currentRowKey, String filteStr) {
				mainFrame.showProgress();
				new GetTableRowsWorker(tableName, KeyUtil.getNextKey(currentRowKey), false, rowPageLimit, filteStr).execute();
			}

			@Override
			public void prevHRowPage(String tableName, String currentRowKey, String filteStr) {
				mainFrame.showProgress();
				new GetTableRowsWorker(tableName, KeyUtil.getPreviousKey(currentRowKey), true, rowPageLimit, filteStr).execute();
			}

			@Override
			public void onViewHRow(String tableName, String rowKey, String columnFamily, String qualifier) {
				cellPageNum = 0;
				mainFrame.showProgress();
				new GetRowCellsWorker(tableName, rowKey, columnFamily, qualifier, cellPageNum, cellPageLimit).execute();
			}
			
			@Override
			public void onRefreshHCells(String tableName, String rowKey, String columnFamily, String qualifier) {
				mainFrame.showProgress();
				new GetRowCellsWorker(tableName, rowKey, columnFamily, qualifier, cellPageNum, cellPageLimit).execute();
			}
			
			@Override
			public void nextHCellPage(String tableName, String rowKey, String columnFamily, String qualifier) {
				mainFrame.showProgress();
				cellPageNum++;
				new GetRowCellsWorker(tableName, rowKey, columnFamily, qualifier, cellPageNum, cellPageLimit).execute();
			}

			@Override
			public void prevHCellPage(String tableName, String rowKey, String columnFamily, String qualifier) {
				if(cellPageNum <= 0) return;
				cellPageNum--;
				mainFrame.showProgress();
				new GetRowCellsWorker(tableName, rowKey, columnFamily, qualifier, cellPageNum, cellPageLimit).execute();
			}

			@Override
			public void onChangeHCellsPage(String tableName, String rowKey, String columnFamily, String qualifier,
					int pageNum) {
				cellPageNum = pageNum>=0 ? pageNum : 0;
				mainFrame.showProgress();
				new GetRowCellsWorker(tableName, rowKey, columnFamily, qualifier, cellPageNum, cellPageLimit).execute();
			}
		});
		
		connectionDialog.setOkButtonListener((zookeeper, port)->{
			try {
				context.initConnection(zookeeper, port);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			mainFrame.showProgress();
			new GetTableBootWorker().execute();
		});
		
		createTableDialog.setOnOkButtonListener((tn, list) -> {
			try {
				context.createTable(tn, list);
				new GetTableBootWorker().execute();
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
		});
		
		createRowDialog.setOnOkButtonListener((String tableName, String rowKey, List<HCell> cells) -> {
			try {
				context.insert(tableName, rowKey, cells);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
		});
		
		mainFrame.pack();
		mainFrame.setVisible(true);
		
	}
	
	private class GetTableBootWorker extends SwingWorker<Object, Object> {
		
		public GetTableBootWorker(){
			super();
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			return context.getTableRoot();
		}
		
		@Override
		protected void done() {
			super.done();
			try {
				HTableTreeNode tableRoot = (HTableTreeNode) get(); // May raise a exception if doInBackground throws one.
				mainFrame.setTableRoot(tableRoot);
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				mainFrame.stopProgress();
			}
		}
	}
	
	private class GetTableRowsWorker extends SwingWorker<Object, Object> {
		
		String tableName;
		String startRow;
		String filteStr;
		boolean reversed = false;
		int limit;
		
		public GetTableRowsWorker(String tableName, String startRow, boolean reversed, int limit, String filteStr) {
			super();
			this.tableName	= tableName;
			this.startRow	= startRow;
			this.reversed	= reversed;
			this.limit		= limit;
			this.filteStr	= filteStr;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			return context.scanRows(tableName, startRow, limit, reversed, filteStr);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void done() {
			super.done();
			try {
				mainFrame.setRows((List<HRow>) get()); // May raise a exception if doInBackground throws one.
			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				mainFrame.stopProgress();
			}
		}
	}
	
	private class GetRowCellsWorker extends SwingWorker<Object, Object> {
		
		String tableName;
		String rowKey;
		String columnFamily;
		String qualifier;
		int pageNum;
		int pageCount;
		
		public GetRowCellsWorker(String tableName, String rowKey, String columnFamily, String qualifier, int pageNum, int pageCount) {
			super();
			this.tableName		= tableName;
			this.rowKey			= rowKey;
			this.pageNum		= pageNum;
			this.pageCount		= pageCount;
			this.columnFamily	= columnFamily;
			this.qualifier		= qualifier;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			return context.getCells(tableName, rowKey, columnFamily, qualifier, pageNum, pageCount);
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected void done() {
			super.done();
			try {
				mainFrame.setCells((List<HCell>) get(), pageNum); // May raise a exception if doInBackground throws one.
			} catch (Exception e) {
				e.printStackTrace();
				if(e.getCause() instanceof NoSuchColumnFamilyException)
					JOptionPane.showMessageDialog(mainFrame, "Column Family not exists.", "Error", JOptionPane.ERROR_MESSAGE);
				else
					JOptionPane.showMessageDialog(mainFrame, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
			} finally {
				mainFrame.stopProgress();
			}
		}
	}
	
	
	// The main method of this app.
	public static void main(String[] args) {
		App app = new App();
		SwingUtilities.invokeLater(() -> {
			app.initUi();
		});
	}
}
