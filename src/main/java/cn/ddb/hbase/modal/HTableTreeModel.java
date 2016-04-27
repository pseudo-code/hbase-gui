package cn.ddb.hbase.modal;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/**
 * 表树数据模型。
 * @author venia
 */
public class HTableTreeModel implements TreeModel {
	
	HTableTreeNode root;
	
	List<TreeModelListener> treeModelListeners = new ArrayList<TreeModelListener>();
	
	public HTableTreeModel(HTableTreeNode root) {
		this.root = root;
	}

	@Override
	public Object getRoot() {
		return this.root;
	}
	
	public void setRoot(HTableTreeNode root) {
		HTableTreeNode old = this.root;
		this.root = root;
		TreeModelEvent e = new TreeModelEvent(this, new Object[] {old});
		treeModelListeners.forEach(l -> {
			l.treeStructureChanged(e);
		});
	}
	
	public void clear() {
		setRoot(new HTableTreeNode("root"));
	}

	@Override
	public Object getChild(Object parent, int index) {
		return ((HTableTreeNode)parent).getChildAt(index);
	}

	@Override
	public int getChildCount(Object parent) {
		return ((HTableTreeNode)parent).getChildCount();
	}

	@Override
	public boolean isLeaf(Object node) {
		return ((HTableTreeNode) node).isLeaf();
	}

	@Override
	public void valueForPathChanged(TreePath path, Object newValue) {
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		return ((HTableTreeNode)parent).getIndexOfChild(child);
	}

	@Override
	public void addTreeModelListener(TreeModelListener l) {
		treeModelListeners.add(l);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener l) {
		treeModelListeners.remove(l);
	}

}
