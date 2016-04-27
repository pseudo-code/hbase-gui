package cn.ddb.hbase.modal;

import java.util.ArrayList;
import java.util.List;

/**
 * @author venia
 */
public class HRow {

	private String rowKey;
	private List<HCell> cells = new ArrayList<HCell>();
	
	public HRow(String rowKey) {
		if(rowKey == null) throw new NullPointerException();
		this.rowKey = rowKey;
	}
	
	public String getRowKey() {
		return rowKey;
	}
	public void setRowKey(String rowKey) {
		this.rowKey = rowKey;
	}
	public List<HCell> getCells() {
		return cells;
	}
	public void setCells(List<HCell> cells) {
		this.cells = cells;
	}
}
