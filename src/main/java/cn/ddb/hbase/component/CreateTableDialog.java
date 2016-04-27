package cn.ddb.hbase.component;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * 创建表的对话框，需要指定列簇。
 * @author venia
 */
public class CreateTableDialog extends JDialog implements MouseListener {

	private static final long serialVersionUID = 1L;
	
	private static final String DEFAULT_STR_TABLE_NAME 		= "tablename1";
	private static final String DEFAULT_STR_COLUMN_FAMILIES = "cf1 cf2 cf3";
	
	@FunctionalInterface
	public static interface OnOkButtonListener {
		public void onClick(String tableName, List<String> columnFamilies);
	}
	
	public OnOkButtonListener onOkButtonListener;
	
	public OnOkButtonListener getOnOkButtonListener() {
		return onOkButtonListener;
	}

	public void setOnOkButtonListener(OnOkButtonListener onOkButtonListener) {
		this.onOkButtonListener = onOkButtonListener;
	}
	
	private JTextField jtf1;
	private JTextField jtf2;

	public CreateTableDialog(JFrame jframe) {
		super(jframe);
		
		JPanel jpanel = new JPanel();
		GroupLayout layout = new GroupLayout(jpanel);
		jpanel.setLayout(layout);
		
		JLabel jl1 = new JLabel("Table name:");
		JLabel jl2 = new JLabel("Column Families:");
		
		jtf1 = new JTextField(DEFAULT_STR_TABLE_NAME, 50);
		jtf2 = new JTextField(DEFAULT_STR_COLUMN_FAMILIES, 50);
		
		JButton confirm = new JButton("Confirm");
		confirm.setName(confirm.getText());
		confirm.addMouseListener(this);
		
		JButton cancel	= new JButton("Cancel");
		cancel.setName(cancel.getText());
		cancel.addMouseListener(this);
		
		layout.setHorizontalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(jl1).addComponent(jl2).addComponent(confirm))
				.addGap(10)
				.addGroup(layout.createParallelGroup().addComponent(jtf1).addComponent(jtf2).addComponent(cancel)));
		
		layout.setVerticalGroup(layout.createSequentialGroup()
				.addGroup(layout.createParallelGroup().addComponent(jl1).addComponent(jtf1))
				.addGap(5)
				.addGroup(layout.createParallelGroup().addComponent(jl2).addComponent(jtf2))
				.addGap(20)
				.addGroup(layout.createParallelGroup().addComponent(confirm).addComponent(cancel)));
		
		jpanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 10));

		setModal(true);
		setLocationByPlatform(true);
		setContentPane(jpanel);
		setResizable(false);
		pack();
	}

	@Override
	public void setVisible(boolean isVisible) {
		if(isVisible) {
			jtf1.setText(DEFAULT_STR_TABLE_NAME);
			jtf2.setText(DEFAULT_STR_COLUMN_FAMILIES);
		}
		super.setVisible(isVisible);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() != MouseEvent.BUTTON1) return;
		switch(((JComponent)e.getSource()).getName()) {
		case "Confirm": {
			this.setVisible(false);
			if(onOkButtonListener != null) {
				String[] split = jtf2.getText().trim().split("\\s|,|;");
				onOkButtonListener.onClick(jtf1.getText().trim(),
						Arrays.asList(split)
							.stream()
							.filter(s -> s.length() > 0)
							.collect(Collectors.toList()));
			}
			break;
		}
		case "Cancel": {
			this.setVisible(false);
			break;
		}
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
