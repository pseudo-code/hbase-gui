package cn.ddb.hbase.modal;

import java.util.ArrayList;
import java.util.List;

/**
 * 表树节点。
 * @author venia
 */
public class HTableTreeNode {

	private String name;
	
	private boolean isLeaf = false;

	private List<HTableTreeNode> children = new ArrayList<HTableTreeNode>();
	
	
	public HTableTreeNode(String name){
		this.name = name;
	}

	public boolean isLeaf() {
		return isLeaf;
	}

	public HTableTreeNode setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
		return this;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void addChild(HTableTreeNode child) {
		this.children.add(child);
	}
	
	public void clear(){
		this.children.clear();
	}
	
	@Override
	public String toString() {
		return name;
	}
	
	public int getChildCount() {
		return children.size();
	}
	
    public HTableTreeNode getChildAt(int i) {
        return children.get(i);
    }
    
    public int getIndexOfChild(Object kid) {
        return children.indexOf(kid);
    }

    public List<HTableTreeNode> getChildren() {
    	return new ArrayList<HTableTreeNode>(children);
    }
}
