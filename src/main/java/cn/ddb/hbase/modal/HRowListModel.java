package cn.ddb.hbase.modal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.ListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 * 行列表数据模型。
 * @author venia
 */
public class HRowListModel implements ListModel<String> {
	private List<HRow> data;
	private int pageIndex;
	private ArrayList<ListDataListener> listDataListeners = new ArrayList<ListDataListener>();
	
	public HRowListModel() {
		this.data = new ArrayList<HRow>();
		this.pageIndex = 0;
	}
	
	public void setData(List<HRow> data, int pageIndex) {
		if(data != null)
			this.data = data;
		else this.data = new ArrayList<HRow>();
		
		this.pageIndex = pageIndex;
		
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size());
		listDataListeners.forEach((l)->{
			l.contentsChanged(e);
		});
	}
	
	public List<HRow> getData() {
		return this.data;
	}
	
	public int getPageIndex() {
		return pageIndex;
	}

	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}

	public List<HCell> getCellsAt(int index) {
		return data.get(index).getCells();
	}
	
	public HRow getRowAt(int index) {
		return data.get(index);
	}
	
	public void clear() {
		data.clear();
		ListDataEvent e = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, data.size());
		listDataListeners.forEach((l)->{
			l.contentsChanged(e);
		});
	}
	
	public String getLastRowKey() {
		int size = data.size();
		if(size > 0) {
			return data.get(size-1).getRowKey();
		}
		return null;
	}
	
	public String getFirstRowKey() {
		if(data.size() > 0){
			return data.get(0).getRowKey();
		}
		return null;
	}
	
	@Override
	public int getSize() {
		return data.size();
	}

	@Override
	public String getElementAt(int index) {
		return data.get(index).getRowKey();
	}
	
	@Override
	public void addListDataListener(ListDataListener l) {
		listDataListeners.add(l);
	}
	
	@Override
	public void removeListDataListener(ListDataListener l) {
		listDataListeners.remove(l);
	}
} 
