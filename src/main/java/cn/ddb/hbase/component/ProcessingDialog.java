package cn.ddb.hbase.component;

import java.awt.BorderLayout;
import java.awt.Dialog;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

@Deprecated
public class ProcessingDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;

	public ProcessingDialog(JFrame jframe) {
		super(jframe);
		
		JLabel jLabel = new JLabel("Processing, wait...");
		jLabel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BorderLayout());
		jpanel.add(jLabel, BorderLayout.CENTER);
		jpanel.setBorder(BorderFactory.createDashedBorder(null));
		
		// No title bar.
		dispose();
		setUndecorated(true);
		setModal(false);
		setContentPane(jpanel);
		setResizable(false);
		setLocationRelativeTo(jframe);
		pack();
		
	}

}
