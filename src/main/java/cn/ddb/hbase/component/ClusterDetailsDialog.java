package cn.ddb.hbase.component;

import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.hadoop.hbase.ClusterStatus;

/**
 * 集群状态对话框。
 * @author venia
 */
public class ClusterDetailsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	
	public ClusterDetailsDialog(JFrame jframe) {
		super(jframe);
		setModal(true);
		getContentPane().setLayout(new GridLayout(0, 2));
		
	}

	public void init(ClusterStatus clusterStatus) {
		// TODO Auto-generated method stub
		
	}

}
