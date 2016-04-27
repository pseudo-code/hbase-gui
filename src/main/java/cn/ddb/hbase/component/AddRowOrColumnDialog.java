package cn.ddb.hbase.component;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.BevelBorder;

import cn.ddb.hbase.modal.HCell;

/**
 * 添加数据的对话框，列簇由表决定，行键唯一，列簇下可以拥有多个 （标识符-值） 对。
 * @author venia
 */
public class AddRowOrColumnDialog extends JDialog {
	
	private static final long serialVersionUID = 1L;
	
	@FunctionalInterface
	public static interface OnOkButtonListener {
		public void onConfirm(String tableName, String rowKey, List<HCell> cells);
	}
	
	private String tableName;
	private JPanel verticalPanel;
	
	private JButton okButton;
	private JButton cancelButton;
	
	private JTextField rowKeyTextField;
	private Map<String, List<JTextField[]>> qvMap;
	
	private OnOkButtonListener onOkButtonListener;
	
	private Dimension defaultSize;
	
	public AddRowOrColumnDialog(JFrame jframe) {
		super(jframe);
		
		verticalPanel = new JPanel();
		verticalPanel.setLayout(new VFlowLayout());
		verticalPanel.setBorder(BorderFactory.createEmptyBorder(5,5,20,5));

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		defaultSize = new Dimension((int)screenSize.getWidth() / 2, (int)screenSize.getHeight() * 2 /3);
		
		JScrollPane scroll = new JScrollPane(verticalPanel);
		scroll.setPreferredSize(defaultSize);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		setLayout(new BorderLayout());
		getContentPane().add(scroll, BorderLayout.CENTER);
		getContentPane().add(createBottomComponent(), BorderLayout.PAGE_END);
		setModal(true);
		setLocationByPlatform(true);
		setResizable(false);
		pack();
	}
	
	public void init(String tableName, List<String> columnFamilies, String rowKey) {
		this.tableName = tableName;
		qvMap = new HashMap<>();
		
		verticalPanel.removeAll();
		verticalPanel.add(createTableNameComponent(tableName));
		verticalPanel.add(createRowKeyComponent(rowKey));
		
		columnFamilies.forEach(s -> {
			verticalPanel.add(createColumnFamiliesComponent(s));
		});
		
		pack();
		
		setVisible(true);
	}
	
	private Component createBottomComponent() {
		okButton = new JButton("OK");
		okButton.addActionListener((e) -> {
			if(onOkButtonListener != null){
				String rowKey = rowKeyTextField.getText().trim();
				List<HCell> cells = new ArrayList<>();
				qvMap.forEach((cf, list) -> {
					list.forEach((array) -> {
						String qualifier = array[0].getText().trim();
						String value = array[1].getText().trim();
						if(qualifier.length() > 0 && value.length() > 0)
							cells.add(new HCell(cf, qualifier, value));
						
					});
				});
				onOkButtonListener.onConfirm(tableName, rowKey, cells);
				setVisible(false);
			}
		});
		
		cancelButton = new JButton("Canel");
		cancelButton.addActionListener((e) -> {
			setVisible(false);
		});
		
		JPanel jpanel = new JPanel();
		jpanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.X_AXIS));
		jpanel.add(Box.createHorizontalGlue());
		jpanel.add(okButton);
		jpanel.add(Box.createHorizontalStrut(20));
		jpanel.add(cancelButton);
		jpanel.add(Box.createHorizontalStrut(20));
		return jpanel;
	}

	private JComponent createTableNameComponent(String tableName) {
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.X_AXIS));
		jpanel.add(new JLabel("Table:"));
		jpanel.add(Box.createRigidArea(new Dimension(10, 0)));
		jpanel.add(new JLabel(tableName));
		jpanel.setPreferredSize(new Dimension(400, 28));
		return jpanel;
	}
	
	private JComponent createRowKeyComponent(String rowKey){
		rowKeyTextField = new JTextField(rowKey);
		rowKeyTextField.setName("rowkeytextfield");
		rowKeyTextField.setPreferredSize(new Dimension(200, 28));
		
		JPanel jpanel = new JPanel();
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.X_AXIS));
		jpanel.add(new JLabel("Row key:"));
		jpanel.add(Box.createRigidArea(new Dimension(10, 0)));
		jpanel.add(rowKeyTextField);
		jpanel.setPreferredSize(new Dimension(400, 28));
		return jpanel;
	}
	
	private JComponent createColumnFamiliesComponent(String cf) {
		JPanel jpanel 		= new JPanel(); // the container
		JPanel head 		= new JPanel(); // the column family name panel
		JButton addButton 	= new JButton("+"); // add button
		JButton minusButton = new JButton("-"); // delte button
		JLabel cfl 			= new JLabel("ColumnFamily: " + cf);
		
		addButton.setSize(new Dimension(20, 20));
		addButton.addActionListener((e) -> {
			jpanel.add(createQVComponent(cf));
			pack();
		});
		minusButton.setSize(new Dimension(20, 20));
		minusButton.addActionListener((e) -> {
			if(jpanel.getComponentCount() > 6) {// There are six components added by default.
				jpanel.remove(jpanel.getComponentCount()-1);
				qvMap.get(cf).remove(qvMap.get(cf).size()-1);
			}
			pack();
		});
		
		head.setLayout(new BoxLayout(head, BoxLayout.X_AXIS));
		head.add(cfl);
		head.add(Box.createHorizontalGlue());
		head.add(addButton);
		head.add(minusButton);
		
		jpanel.setLayout(new BoxLayout(jpanel, BoxLayout.Y_AXIS));
		jpanel.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		jpanel.add(Box.createRigidArea(new Dimension(0, 5))); // first component
		jpanel.add(head); // second
		jpanel.add(Box.createRigidArea(new Dimension(0, 5))); // thired
		jpanel.add(new JSeparator()); // fourth
		jpanel.add(Box.createRigidArea(new Dimension(0, 5))); // fifth 
		jpanel.add(createQVComponent(cf)); // sixth
		return jpanel;
	}
	
	private JComponent createQVComponent(String cf) {
		JPanel qvPanel = new JPanel();
		JLabel ql = new JLabel("q:");
		JLabel vl = new JLabel("v:");
		JTextField qtx = new JTextField(null, 15);
		JTextField vtx = new JTextField(null, 15);
		
		qvPanel.setSize(new Dimension(400, 24));
		qvPanel.setLayout(new BoxLayout(qvPanel, BoxLayout.X_AXIS));
		qvPanel.add(ql);
		qvPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		qvPanel.add(qtx);
		qvPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		qvPanel.add(vl);
		qvPanel.add(Box.createRigidArea(new Dimension(2, 0)));
		qvPanel.add(vtx);
		
		if(!qvMap.containsKey(cf)) {
			qvMap.put(cf, new ArrayList<>());
		}
		qvMap.get(cf).add(new JTextField[]{qtx, vtx});
		
		return qvPanel;
	}
	
	public OnOkButtonListener getOnOkButtonListener() {
		return onOkButtonListener;
	}

	public void setOnOkButtonListener(OnOkButtonListener onOkButtonListener) {
		this.onOkButtonListener = onOkButtonListener;
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame jframe = new JFrame();
			AddRowOrColumnDialog dialog = new AddRowOrColumnDialog(jframe);
			List<String> cf = new ArrayList<String> ();
			cf.add("1");
			cf.add("2");
			jframe.pack();
			jframe.setVisible(true);
			dialog.setOnOkButtonListener((String tableName, String rowKey, List<HCell> cells) -> {
				cells.forEach(c -> {
					System.out.println(c.getColumnFamily() + " " + c.getQualifier() + " " + c.getValue());
				});
			});
			dialog.init("table1", cf, null);
		});
	}
}
