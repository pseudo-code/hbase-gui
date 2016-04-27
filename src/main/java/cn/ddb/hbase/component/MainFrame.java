package cn.ddb.hbase.component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import com.sun.glass.events.KeyEvent;

import cn.ddb.hbase.modal.HCell;
import cn.ddb.hbase.modal.HCellTableModel;
import cn.ddb.hbase.modal.HRow;
import cn.ddb.hbase.modal.HRowListModel;
import cn.ddb.hbase.modal.HTableTreeModel;
import cn.ddb.hbase.modal.HTableTreeNode;

/**
 * DDBHBaseGUI工具主界面，包括菜单、hbase表的树形结构、hbase行的列表、hbase行对应的列以及相应的操作功能按钮。
 * 
 * @author venia
 */
@SuppressWarnings("unused")
public class MainFrame extends JFrame implements MouseListener {

	private static final long serialVersionUID = 1L;
	
	private static final String APP_TITLE			= "DDBHbaseGUI";
	
	// ---------------- action command ---------------------------------
	// menu bar
	private static final String AC_CONNECT 			= "Connect";
	private static final String AC_DISCONNECT 		= "Disconnect";
	private static final String AC_DETAILS			= "Cluster details";
	// operation bar
	private static final String AC_ADD				= "Add";
	private static final String AC_VIEW_ROW			= "View row";
	private static final String AC_DELETE_ROWS 		= "Delete Rows";
	private static final String AC_REFRESH_ROWS 	= "Refresh rows";
	
	private static final String AC_UPDATE_CELLS		= "Update Cells";
	private static final String AC_REVERT_CELLS		= "Revert Cells";
	private static final String AC_REFRESH_CELLS	= "Refresh Cells";
	// popup menu
	private static final String AC_VIEW_TABLE		= "Open table";
	private static final String AC_CREATE_TABLE		= "Create table";
	private static final String AC_DELETE_TABLE		= "Delete table";
	private static final String AC_REFRESH_TABLE	= "Refresh tables";
	// paging par
	private static final String AC_NEXT_ROW_P		= "Next Row Page";
	private static final String AC_PREV_ROW_P		= "Prev Row Page";
	private static final String AC_NEXT_CELL_P		= "Next Cell Page";
	private static final String AC_PREV_CELL_P		= "Prev Cell Page";
	// ---------------- action command end ---------------------------------
	
	// --------------- btn text ----------------------------------------
	// menu bar
	private static final String TXT_CONNECT			= "Connect...";
	private static final String TXT_DISCONNECT		= "Disconnect";
	private static final String TXT_DETAILS			= "Cluster...";
	// operation bar
	private static final String TXT_ADD				= "Add";
	private static final String TXT_VIEW_ROW		= "View";
	private static final String TXT_DELETE_ROWS		= "Delt";
	private static final String TXT_REFRESH_ROWS	= "Refr";
	
	private static final String TXT_UPDATE_CELLS	= "Updt";
	private static final String TXT_REVERT_CELLS	= "Revt";
	private static final String TXT_REFRESH_CELLS	= "Refr";
	// popup menu
	private static final String TXT_VIEW_TABLE		= "Open";
	private static final String TXT_CREATE_TABLE	= "Create...";
	private static final String TXT_DELETE_TABLE	= "Delete";
	private static final String TXT_REFRESH_TABLE	= "Refresh";
	// paging par
	private static final String TXT_NEXT_ROW_P		= "Next >";
	private static final String TXT_PREV_ROW_P		= "< Prev";
	private static final String TXT_NEXT_CELL_P		= "Next >";
	private static final String TXT_PREV_CELL_P		= "< Prev";
	// --------------- btn text end ----------------------------------------
	
	private static final Pattern NUM_PATTERN		= Pattern.compile("^[0-9]*$");
	
	
	/** The listener can used to interact with the MainFrame. */
	// All the action the UI can occur must defines here.
	public static interface MainFrameListener {
		public void onConnect();
		public void onDisconnect();
		public void onClusterDetails();
		
		public void onViewHTable(String tableName, String filteStr);
		public void onCreateHTable();
		public void onDeleteHTable(String tableName);
		public void onRefreshHTable();
		
		public void onAdd(String tableName, String rowKey);
		public void onViewHRow(String tableName, String selectedRowKey, String columnFamily, String qualifier);
		public void onDeleteHRows(String tableName, List<String> needDelete, String startRowKey, String filteStr);
		public void onRefreshHRows(String tableName, String rowKey, String filteStr);
		
		public void onUpdateHCells(String tableName, String rowKey, List<HCell> changedCells);
		public void onRefreshHCells(String tableName, String rowKey, String columnFamily, String qualifier);
		public void onChangeHCellsPage(String tableName, String rowKey, String columnFamily, String qualifier, int pageNum);
		
		public void nextHRowPage(String tableName, String currentRowKey, String filteStr);
		public void prevHRowPage(String tableName, String currentRowKey, String filteStr);
		public void nextHCellPage(String tableName, String rowKey, String columnFamily, String qualifier);
		public void prevHCellPage(String tableName, String rowKey, String columnFamily, String qualifier);
	}
	
	private MainFrameListener listener;
	
	
	private JTree 			htableTree;
	private JList<String> 	hrowList;
	private JTable 			hcellTable;
	
	private JProgressBar 	progressBar;
	private JPopupMenu 		popupMenu;
	
	private JLabel			rowCountLabel;
	private JLabel			cellCountLabel;
	private JLabel			cellPageNumLabel; // for display
	private JTextField		cellPageNumTextField; // for edit
	
	private JTextField		rowKeyFilter;
	private JTextField		cellColumnFamilyFilter;
	private JTextField		cellQualifierFilter;
	
	private AtomicInteger taskCount = new AtomicInteger(0);
	
	private String preSelectedTableName = null;
	private String openedTableName 		= null;
	private String selectedRowKey 		= null;
	
	Timer updateProgressBarTimer = new Timer(200, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			int i = taskCount.get();
			if(i > 0) {
				progressBar.setValue((progressBar.getValue() + 3)%100);
			}
			else {
				progressBar.setValue(0);
				updateProgressBarTimer.stop();
			}
		}
	});

	public MainFrame() {
		super();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle(APP_TITLE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);

		setJMenuBar(createMenuBar());
		getContentPane().add(createMainPane(), BorderLayout.CENTER);
		getContentPane().add(createStatusPanel(), BorderLayout.PAGE_END);
		createPopupMenu();
	}

	// center main pane
	private JComponent createMainPane() {
		// the panel on the left
		JPanel left = new JPanel();
		left.setLayout(new BorderLayout());

		// Click of Right-Button to open the popup menu.
		htableTree = new JTree();
		htableTree.setModel(new HTableTreeModel(new HTableTreeNode("root")));
		htableTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		htableTree.setDragEnabled(false);
		htableTree.setEditable(false);
		htableTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				TreePath selectionPath = htableTree.getSelectionPath();
				HTableTreeNode object = selectionPath == null ? null : (HTableTreeNode)selectionPath.getLastPathComponent();
				if(e.getButton() == MouseEvent.BUTTON3
						&& object != null
						&& object.isLeaf()){
					preSelectedTableName = object.toString();
					popupMenu.show(htableTree, e.getX(), e.getY());
				}
			}
		});
		left.add(htableTree, BorderLayout.CENTER);

		// the panel on the right
		// tool bar
		JButton toolBarAddButton = new JButton(TXT_ADD);
		toolBarAddButton.setName(AC_ADD);
		toolBarAddButton.addMouseListener(this);
		
		JButton toolBarDeleteButton = new JButton(TXT_DELETE_ROWS);
		toolBarDeleteButton.setName(AC_DELETE_ROWS);
		toolBarDeleteButton.addMouseListener(this);
		
		JButton toolBarRefreshRowButton = new JButton(TXT_REFRESH_ROWS);
		toolBarRefreshRowButton.setName(AC_REFRESH_ROWS);
		toolBarRefreshRowButton.addMouseListener(this);
		
		rowKeyFilter = new JTextField();
		rowKeyFilter.setEditable(true);
		rowKeyFilter.setToolTipText("Enter a regex to filter the rows.");
		
		JToolBar toolbar1 = new JToolBar();
		toolbar1.add(toolBarAddButton);
		toolbar1.add(toolBarDeleteButton);
		toolbar1.add(toolBarRefreshRowButton);
		toolbar1.addSeparator();
		toolbar1.add(rowKeyFilter);
		
		JButton toolBarUpdateButton = new JButton(TXT_UPDATE_CELLS);
		toolBarUpdateButton.setName(AC_UPDATE_CELLS);
		toolBarUpdateButton.addMouseListener(this);
		
		JButton toolBarRevertButton = new JButton(TXT_REVERT_CELLS);
		toolBarRevertButton.setName(AC_REVERT_CELLS);
		toolBarRevertButton.addMouseListener(this);
		
		JButton toolBarRefreshCellsButton = new JButton(TXT_REFRESH_CELLS);
		toolBarRefreshCellsButton.setName(AC_REFRESH_CELLS);
		toolBarRefreshCellsButton.addMouseListener(this);
		
		cellColumnFamilyFilter = new JTextField();
		cellColumnFamilyFilter.setEditable(true);
		cellColumnFamilyFilter.setToolTipText("Enter a column family name.");
		cellColumnFamilyFilter.setMaximumSize(new Dimension(120, 30));
		
		cellQualifierFilter = new JTextField();
		cellQualifierFilter.setEditable(true);
		cellQualifierFilter.setToolTipText("Enter a qualifier name.");
		cellQualifierFilter.setMaximumSize(new Dimension(120, 30));
		
		JToolBar toolbar2 = new JToolBar();
		toolbar2.add(toolBarUpdateButton);
		toolbar2.add(toolBarRevertButton);
		toolbar2.add(toolBarRefreshCellsButton);
		toolbar2.addSeparator();
		toolbar2.add(new JLabel("CF:"));
		toolbar2.add(cellColumnFamilyFilter);
		toolbar2.addSeparator();
		toolbar2.add(new JLabel("Qual:"));
		toolbar2.add(cellQualifierFilter);

		// row list
		// Double click of Left-Button to show the cells of the row.
		// Single click of Left-Button to select a row, whick can be obtained in DELETE function.
		hrowList = new JList<String>();
		hrowList.setModel(new HRowListModel());
		hrowList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		hrowList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1
						&& openedTableName != null){
					selectedRowKey = hrowList.getSelectedValue();
					if(listener != null) {
						listener.onViewHRow(openedTableName,
								selectedRowKey,
								getEnteredColumnFamily(),
								getEnteredQualifier());
					}
				}
			}
		});
		JScrollPane rowScroll = new JScrollPane(hrowList,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

		
		rowCountLabel = new JLabel("0");
		
		JButton nextRowPageBtn = new JButton(TXT_NEXT_ROW_P);
		nextRowPageBtn.setName(AC_NEXT_ROW_P);
		nextRowPageBtn.addMouseListener(this);
		
		JButton prevRowPageBtn = new JButton(TXT_PREV_ROW_P);
		prevRowPageBtn.setName(AC_PREV_ROW_P);
		prevRowPageBtn.addMouseListener(this);
		
		JPanel rowPagingBar = new JPanel();
		rowPagingBar.setLayout(new BoxLayout(rowPagingBar, BoxLayout.X_AXIS));
		rowPagingBar.add(new JLabel("Count: "));
		rowPagingBar.add(rowCountLabel);
		rowPagingBar.add(Box.createHorizontalGlue());
		rowPagingBar.add(prevRowPageBtn);
		rowPagingBar.add(nextRowPageBtn);
		
		JPanel rowPanel = new JPanel();
		rowPanel.setLayout(new BorderLayout());
		rowPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		rowPanel.setPreferredSize(new Dimension(240, Short.MAX_VALUE));
		rowPanel.add(toolbar1, BorderLayout.NORTH);
		rowPanel.add(rowScroll, BorderLayout.CENTER);
		rowPanel.add(rowPagingBar, BorderLayout.SOUTH);

		// cell table
		hcellTable = new JTable();
		hcellTable.setFillsViewportHeight(true);
		hcellTable.setModel(new HCellTableModel(null));
		hcellTable.setRowSelectionAllowed(true);
		hcellTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		hcellTable.setColumnSelectionAllowed(false);

		JScrollPane cellScroll = new JScrollPane(hcellTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		cellScroll.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

		cellCountLabel = new JLabel("0");
		
		cellPageNumLabel = new JLabel("0");
		cellPageNumLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if(openedTableName == null || selectedRowKey == null) return;
				cellPageNumLabel.setVisible(false);
				cellPageNumTextField.setText(cellPageNumLabel.getText());
				cellPageNumTextField.setVisible(true);
				cellPageNumTextField.requestFocus();
			}
		});
		
		cellPageNumTextField = new JTextField("0");
		cellPageNumTextField.setMaximumSize(new Dimension(30, 30));
		cellPageNumTextField.setVisible(false);
		cellPageNumTextField.setColumns(2);
		cellPageNumTextField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(java.awt.event.KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					cellPageNumLabel.setVisible(true);
					cellPageNumTextField.setVisible(false);
					if(openedTableName != null && selectedRowKey != null && listener != null) {
						listener.onChangeHCellsPage(openedTableName,
								selectedRowKey,
								getEnteredColumnFamily(),
								getEnteredQualifier(),
								getEnteredPageNum());
					}
				}
			}
		});
		cellPageNumTextField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				cellPageNumLabel.setVisible(true);
				cellPageNumTextField.setVisible(false);
			}
		});
		
		JButton nextCellPageBtn = new JButton(TXT_NEXT_CELL_P);
		nextCellPageBtn.setName(AC_NEXT_CELL_P);
		nextCellPageBtn.addMouseListener(this);
		
		JButton prevCellPageBtn = new JButton(TXT_PREV_CELL_P);
		prevCellPageBtn.setName(AC_PREV_CELL_P);
		prevCellPageBtn.addMouseListener(this);
		
		JPanel cellPagingBar = new JPanel();
		cellPagingBar.setLayout(new BoxLayout(cellPagingBar, BoxLayout.X_AXIS));
		cellPagingBar.add(new JLabel("Page: "));
		cellPagingBar.add(cellPageNumLabel);
		cellPagingBar.add(cellPageNumTextField);
		cellPagingBar.add(Box.createHorizontalStrut(20));
		cellPagingBar.add(new JLabel("Count: "));
		cellPagingBar.add(cellCountLabel);
		cellPagingBar.add(Box.createHorizontalGlue());
		cellPagingBar.add(prevCellPageBtn);
		cellPagingBar.add(nextCellPageBtn);
		
		JPanel cellPanel = new JPanel();
		cellPanel.setLayout(new BorderLayout());
		cellPanel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
		cellPanel.add(toolbar2, BorderLayout.NORTH);
		cellPanel.add(cellScroll, BorderLayout.CENTER);
		cellPanel.add(cellPagingBar, BorderLayout.SOUTH);
		
		
		// combine row list and cell table
		JPanel right = new JPanel();
		BorderLayout dataPanelBorderLayout = new BorderLayout();
		dataPanelBorderLayout.setHgap(1);
		dataPanelBorderLayout.setVgap(1);
		right.setLayout(dataPanelBorderLayout);
		right.add(rowPanel, BorderLayout.LINE_START);
		right.add(cellPanel, BorderLayout.CENTER);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
		splitPane.setResizeWeight(0.2);

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.add(splitPane, BorderLayout.CENTER);
		return mainPanel;
	}
	
	// status panel, show progress and other info
	private JComponent createStatusPanel() {
		JPanel status = new JPanel();
		status.setBorder(BorderFactory.createLineBorder(Color.GRAY));
		status.setLayout(new FlowLayout(FlowLayout.RIGHT));
		status.add(new JLabel("Status"));
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(false);
		status.add(progressBar);
		return status;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JMenuItem menuItem = new JMenuItem(TXT_CONNECT + "...", KeyEvent.VK_C);
		menuItem.setName(AC_CONNECT);
		menuItem.addMouseListener(this);
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem(TXT_DISCONNECT + "...", KeyEvent.VK_D);
		menuItem.setName(AC_DISCONNECT);
		menuItem.addMouseListener(this);
		menu.add(menuItem);
		menu.addSeparator();
		
		menuItem = new JMenuItem(TXT_DETAILS + "...", KeyEvent.VK_E);
		menuItem.setName(AC_DETAILS);
		menuItem.addMouseListener(this);
		menu.add(menuItem);
		menu.addSeparator();
		
		menuBar.add(menu);
		return menuBar;
	}
	
	private JComponent createPopupMenu() {
		popupMenu = new JPopupMenu();
		JMenuItem menuItem = new JMenuItem(TXT_VIEW_TABLE);
		menuItem.setName(AC_VIEW_TABLE);
		menuItem.addMouseListener(this);
		popupMenu.add(menuItem);
		popupMenu.addSeparator();
		
		menuItem = new JMenuItem(TXT_CREATE_TABLE);
		menuItem.setName(AC_CREATE_TABLE);
		menuItem.addMouseListener(this);
		popupMenu.add(menuItem);
		popupMenu.addSeparator();
		
		menuItem = new JMenuItem(TXT_DELETE_TABLE);
		menuItem.setName(AC_DELETE_TABLE);
		menuItem.addMouseListener(this);
		popupMenu.add(menuItem);
		popupMenu.addSeparator();
		
		menuItem = new JMenuItem(TXT_REFRESH_TABLE);
		menuItem.setName(AC_REFRESH_TABLE);
		menuItem.addMouseListener(this);
		popupMenu.add(menuItem);
		popupMenu.addSeparator();
		
		return popupMenu;
	}
	
	/**
	 * 设置表根元素
	 */
	public void setTableRoot(HTableTreeNode root) {
		HTableTreeModel model = (HTableTreeModel)htableTree.getModel();
		model.setRoot(root);
		for(int i=0, count=htableTree.getRowCount(); i<count; i++){
			htableTree.expandRow(i);
		}
	}
	
	/**
	 * 设置行数据
	 * @param row
	 */
	public void setRows(List<HRow> row) {
		clearRowListAndCellTable();
		((HRowListModel)hrowList.getModel()).setData(row, 0);
		rowCountLabel.setText(String.valueOf(row.size()));
	}
	
	public void setCells(List<HCell> cells, int pageNum) {
		((HCellTableModel)hcellTable.getModel()).setData(cells);
		cellCountLabel.setText(String.valueOf(cells.size()));
		cellPageNumLabel.setText(String.valueOf(pageNum));
	}
	
	/** 清空页面数据 */
	public void clear() {
		preSelectedTableName = null;
		openedTableName = null;
		((HTableTreeModel)htableTree.getModel()).clear();
		htableTree.clearSelection();
		
		clearRowListAndCellTable();
	}
	
	public void clearRowList() {
		selectedRowKey = null;
		((HRowListModel)hrowList.getModel()).clear();
		hrowList.clearSelection();
	}
	
	public void clearCellTable() {
		cellPageNumLabel.setText("0");
		((HCellTableModel)hcellTable.getModel()).clear();
		hcellTable.clearSelection();
	}
	
	/** 清空rowkeys和celltable */
	public void clearRowListAndCellTable() {
		clearRowList();
		clearCellTable();
	}
	
	/**
	 * increase working task count and show progressbar
	 */
	public void showProgress() {
		if(taskCount.getAndIncrement() == 0) {
			updateProgressBarTimer.start();
		}
	}
	
	/**
	 * decrease working task count
	 */
	public void stopProgress() {
		taskCount.decrementAndGet();
	}
	
	public MainFrameListener getListener() {
		return listener;
	}

	public void setListener(MainFrameListener listener) {
		this.listener = listener;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if(e.getButton() != MouseEvent.BUTTON1) return;
		switch(((JComponent)e.getSource()).getName()){
		case AC_CONNECT: {
			if(listener != null){
				listener.onConnect();
			}
			break;
		}
		case AC_DISCONNECT: {
			clear();
			if(listener != null){
				listener.onDisconnect();
			}
			break;
		}
		case AC_VIEW_TABLE: {
			if(listener != null && preSelectedTableName != null){
				openedTableName = preSelectedTableName;
				clearRowListAndCellTable();
				listener.onViewHTable(openedTableName, rowKeyFilter.getText());
			}
			break;
		}
		case AC_CREATE_TABLE: {
			if(listener != null) {
				listener.onCreateHTable();
			}
			break;
		}
		case AC_DELETE_TABLE: {
			if(listener != null && preSelectedTableName != null){
				if(preSelectedTableName.equals(openedTableName)) {
					openedTableName = null;
					clearRowListAndCellTable();
				}
				listener.onDeleteHTable(preSelectedTableName);
			}
			break;
		}
		case AC_REFRESH_TABLE: {
			if(listener != null) {
				listener.onRefreshHTable();
			}
			break;
		}
		case AC_ADD: {
			if(openedTableName != null && listener != null){
				listener.onAdd(openedTableName, hrowList.getSelectedValue());
			}
			break;
		}
		case AC_DELETE_ROWS: {
			if(openedTableName == null) return;
			List<String> selectedValuesList = hrowList.getSelectedValuesList();
			if(selectedValuesList == null || selectedValuesList.size() == 0) return;
			if(selectedValuesList.contains(selectedRowKey)) {
				selectedRowKey = null;
				clearCellTable();
			}
			if(listener != null) {
				listener.onDeleteHRows(openedTableName, selectedValuesList,
						((HRowListModel) hrowList.getModel()).getFirstRowKey(), rowKeyFilter.getText());
			}
			break;
		}
		case AC_REFRESH_ROWS: {
			if(listener != null && openedTableName != null) {
				listener.onRefreshHRows(openedTableName,
						((HRowListModel) hrowList.getModel()).getFirstRowKey(),
						rowKeyFilter.getText()); // Get the beginning.
			}
			break;
		}
		case AC_UPDATE_CELLS: {
			if(openedTableName == null || selectedRowKey == null) return;
			HCellTableModel model = (HCellTableModel) hcellTable.getModel();
			if(model.getRowCount() == 0) return;
			List<HCell> changedList = new ArrayList<HCell> ();
			model.getData().forEach(c -> {
				if(c.isChanged()) {
					changedList.add(new HCell(c));
				}
			});
			
			if(changedList.size() == 0) {
				JOptionPane.showMessageDialog(this, "No item changed.", "Error", JOptionPane.ERROR_MESSAGE);
			}
			else if(listener != null && openedTableName != null && selectedRowKey != null) {
				listener.onUpdateHCells(openedTableName, selectedRowKey, changedList);
				model.applay();
			}
			break;
		}
		case AC_REVERT_CELLS: {
			if(openedTableName != null && selectedRowKey != null) ((HCellTableModel) hcellTable.getModel()).revert();
			break;
		}
		case AC_REFRESH_CELLS: {
			if(openedTableName != null && listener != null) {
				listener.onRefreshHCells(openedTableName,
						selectedRowKey,
						getEnteredColumnFamily(),
						getEnteredQualifier());
			}
			break;
		}
		case AC_NEXT_ROW_P: {
			if(listener != null && openedTableName != null) {
				listener.nextHRowPage(openedTableName, ((HRowListModel) hrowList.getModel()).getLastRowKey(), rowKeyFilter.getText());
			}
			break;
		}
		case AC_PREV_ROW_P: {
			if(listener != null && openedTableName != null) {
				listener.prevHRowPage(openedTableName, ((HRowListModel) hrowList.getModel()).getFirstRowKey(), rowKeyFilter.getText());
			}
			break;
		}
		case AC_NEXT_CELL_P: {
			if(listener != null && openedTableName != null && selectedRowKey != null) {
				listener.nextHCellPage(openedTableName, selectedRowKey, getEnteredColumnFamily(), getEnteredQualifier());
			}
			break;
		}
		case AC_PREV_CELL_P: {
			if(listener != null && openedTableName != null && selectedRowKey != null) {
				listener.prevHCellPage(openedTableName, selectedRowKey, getEnteredColumnFamily(), getEnteredQualifier());
			}
			break;
		}
		}
	}
	
	private int getEnteredPageNum() {
		int p = 0;
		try {
			p = Integer.parseInt(cellPageNumTextField.getText());
		} catch (Exception e) { // ignore
		}
		return p;
	}
	
	private String getEnteredQualifier() {
		return cellQualifierFilter.getText();
	}

	private String getEnteredColumnFamily() {
		return cellColumnFamilyFilter.getText();
	}

	@Override
	public void mouseReleased(MouseEvent e) { // ignore
	}

	@Override
	public void mouseEntered(MouseEvent e) { // ignore
	}

	@Override
	public void mouseExited(MouseEvent e) { // ignore
	}
	
	@Override
	public void mouseClicked(MouseEvent e) { // ignore
	}
	
}
