package cn.ddb.hbase.modal;

/**
 * 单元格
 * 
 * @author venia
 */
public class HCell {

	public String columnFamily;
	public String qualifier;
	public String value;
	public String oldValue;
	public boolean isChanged;
	
	public HCell(String columnFamily, String qualifier, String value) {
		this.columnFamily 	= columnFamily;
		this.qualifier 		= qualifier;
		this.value 			= value;
		this.oldValue 		= null;
		this.isChanged 		= false;
	}
	
	public HCell(HCell target) {
		this.value = target.value;
		this.qualifier = target.qualifier;
		this.columnFamily = target.columnFamily;
		this.oldValue = target.oldValue;
		this.isChanged = target.isChanged;
	}
	
	public String getOldValue() {
		return oldValue;
	}

	public void setOldValue(String oldValue) {
		this.oldValue = oldValue;
	}

	public String getQualifier() {
		return qualifier;
	}
	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}

	public String getColumnFamily() {
		return columnFamily;
	}

	public void setColumnFamily(String columnFamily) {
		this.columnFamily = columnFamily;
	}

	public boolean isChanged() {
		return isChanged;
	}

	public void setChanged(boolean isChanged) {
		this.isChanged = isChanged;
	}
	
}
