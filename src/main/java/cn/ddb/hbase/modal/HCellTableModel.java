package cn.ddb.hbase.modal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 * 单元格表格数据模型。
 * @author venia
 */
public class HCellTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private static final String[] columnNames = new String[] { "ColumnFamily", "Qualifier", "Value" };

	private List<HCell> data;

	public HCellTableModel(List<HCell> list) {
		if (list != null) {
			this.data = list;
		} else {
			data = new ArrayList<HCell>();
		}
	}

	public void setData(List<HCell> data) {
		this.data = data;
		fireTableDataChanged();
	}
	
	public List<HCell> getData() {
		return this.data;
	}
	
	@Override
	public int getRowCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		switch (columnIndex) {
		case 0: return data.get(rowIndex).columnFamily;
		case 1: return data.get(rowIndex).qualifier;
		case 2: return data.get(rowIndex).value;
		default: return null;
		}
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		if (col == 2) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		HCell cell = data.get(row);
		cell.setOldValue(cell.getValue());
		cell.setValue(value.toString());
		cell.setChanged(true);
		fireTableCellUpdated(row, col);
	}
	
	/** Revert all the cell that had changed. */
	public void revert() {
		data.forEach(c -> {
			if(c.isChanged()) {
				c.setValue(c.getOldValue());
				c.setOldValue(null);
				c.setChanged(false);
			}
		});
		fireTableDataChanged();
	}
	
	/** Apply all the changes. */
	public void applay() {
		data.forEach(c -> {
			if(c.isChanged()) {
				c.setOldValue(null);
				c.setChanged(false);
			}
		});
	}

	/** Empty this model. */
	public void clear() {
		data.clear();
		fireTableDataChanged();
	}

}