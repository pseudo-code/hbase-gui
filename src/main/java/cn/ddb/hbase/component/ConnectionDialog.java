package cn.ddb.hbase.component;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import cn.ddb.hbase.util.SettingsUtil;

/**
 * 显示设置连接的对话框
 * @author venia
 */
public class ConnectionDialog extends JDialog implements MouseListener {
	private static final long serialVersionUID = 1L;
	
	@FunctionalInterface
	public static interface OkButtonListener {
		public void onConfigConfirm(String zookeeper, String port, String znodeParent);
	}
	
	private JLabel jl1;
	private JLabel jl2;
	private JLabel jl3;
	private JTextField address;
	private JTextField port;
	private JTextField znodeParent;
	private JButton ok;
	private JButton cancel;
	
	private OkButtonListener okButtonListener;
	
	
	public ConnectionDialog(JFrame jframe) {
		super(jframe);
		
		jl1 = new JLabel("Zookeeper addr :");
		jl1.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		jl2 = new JLabel("Port :");
		jl2.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		jl3 = new JLabel("ZNode parent :");
		jl3.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		
		address = new JTextField(SettingsUtil.getZookeeperQuarum(), 20);
		port = new JTextField(SettingsUtil.getClientPort(), 20);
		znodeParent = new JTextField(SettingsUtil.getZnodeParent(), 20);
		ok = new JButton("ok");
		ok.setName("ok");
		ok.addMouseListener(this);
		
		cancel = new JButton("cancel");
		cancel.setName("cancel");
		cancel.addMouseListener(this);
		
		JPanel jpanel = new JPanel();
		GroupLayout layout = new GroupLayout(jpanel);
		jpanel.setLayout(layout);
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(jl1).addComponent(jl2).addComponent(jl3).addComponent(ok))
				.addGroup(layout.createParallelGroup(Alignment.LEADING).addComponent(address).addComponent(port).addComponent(znodeParent).addComponent(cancel)));
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(jl1).addComponent(address))
				.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(jl2).addComponent(port))
                .addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(jl3).addComponent(znodeParent))
                .addGap(10)
				.addGroup(layout.createParallelGroup(Alignment.BASELINE).addComponent(ok).addComponent(cancel)));
		
		jpanel.setSize(new Dimension(400, 200));
		jpanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));
		
		setContentPane(jpanel);
		setModal(true);
		setResizable(false);
		setLocationByPlatform(true);
		pack();
	}
	
	public void setOkButtonListener(OkButtonListener o) {
		this.okButtonListener = o;
	}
	
	public OkButtonListener getOkButtonListener() {
		return this.okButtonListener;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() != MouseEvent.BUTTON1) return;
		switch(((JComponent)e.getSource()).getName()){
		case "ok":
			if(okButtonListener != null){
				okButtonListener.onConfigConfirm(address.getText(), port.getText(), znodeParent.getText());
			}
			super.setVisible(false);
			break;
			
		case "cancel": 
			super.setVisible(false);
			break;
			
		default:
			super.setVisible(false);
			break;
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}
}
