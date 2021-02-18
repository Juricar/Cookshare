import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


public class UseFrame {
	
	JFrame useFrame;
	JScrollPane scrollPane;
	JTable table;
	JComboBox<String> selectionMenu;
	JComboBox<String> filterMenu;
	JComboBox<String> sortMenu;
	ArrayList<JTextField> inputs = new ArrayList<JTextField>();
	String[] tableNames = {"Cuisine", "BelongsTo", "Dish", "Has", "Ingredients", "Recipe", "Reviews", "Steps", "User", "Uses", "Utensils"};
	String[] filters = {"All"};
	String[] sortVals = {"None", "Highest Rating"};
	String tableToAddTo;
	String tableToExpand;
	String tableToModify;
	JTextField filterValue;
	int numFields;
	ArrayList<String> fieldNames;
	String dbUsername;
	Connection con;
	JFrame frameToDispose;
	JFrame adderFrame;
	int recipeID;

	public UseFrame(String username, Connection receivedConnection) {
		this.dbUsername = username;
		this.con = receivedConnection;
	}

	public void open() {
		setupUI();
		
		searchTables("Recipe","", (String) sortMenu.getSelectedItem());
		
		setupOnClick();
		
		setupFilters();
	}
	
	public void setupUI()	{
		JFrame frame = new JFrame("Cookshare");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		createMenuBar(frame);
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.CENTER);
		JPanel topPanel = new JPanel();
		panel.add(topPanel, BorderLayout.PAGE_START);
		this.useFrame = frame;
		
		JTable table2 = new JTable(new DefaultTableModel());		
		this.table = table2;
		this.table.setDefaultEditor(Object.class, null);
		this.table.getTableHeader().setReorderingAllowed(false); 
		
		this.scrollPane = new JScrollPane(table);

		
		JComboBox<String> tableList = new JComboBox<>(tableNames);
		tableList.setSelectedIndex(5);
		this.selectionMenu = tableList;
		
		JComboBox<String> filterList = new JComboBox<>(filters);
		filterList.setSelectedIndex(0);
		this.filterMenu = filterList;
		
		JComboBox<String> sortList = new JComboBox<>(sortVals);
		sortList.setSelectedIndex(0);
		this.sortMenu = sortList;
		
		
		this.filterValue = new JTextField(25);
		this.filterValue.setText("Enter Filter Value");
		
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new SearchButtonListener());
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new AddButtonListener());

		topPanel.add(tableList, BorderLayout.NORTH);
		topPanel.add(searchButton, BorderLayout.NORTH);
		topPanel.add(addButton, BorderLayout.NORTH);
		topPanel.add(filterList, BorderLayout.NORTH);
		topPanel.add(sortList, BorderLayout.NORTH);
		topPanel.add(this.filterValue, BorderLayout.NORTH);
		panel.add(this.scrollPane, BorderLayout.SOUTH);
		frame.pack();
		frame.setVisible(true);
	}
	
	void setupOnClick() // This function sets up the on click functionalities for various tables
	{
		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				ArrayList<String> names = new ArrayList<String>();
				ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
				if(event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
					switch(selectionMenu.getSelectedItem().toString()) {
						case "Recipe":
							names.add("View steps");
							names.add("Delete");
							names.add("Add a step");
							names.add("Add a review");
							names.add("View reviews");
							names.add("Edit recipe");
							names.add("Add an ingredient");
							names.add("View ingredients");
							names.add("View utensils");
							names.add("Add a utensil");
							
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									searchTables("Steps", table.getValueAt(table.getSelectedRow(), 0).toString(), (String) sortMenu.getSelectedItem());
									selectionMenu.setSelectedIndex(7);
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									if (dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										try {
											CallableStatement cs = con.prepareCall("{call deleteRecipe(?,?)}");
											cs.setString(1, table.getValueAt(table.getSelectedRow(), 0).toString());
											cs.setString(2, dbUsername);
											cs.execute();
											searchTables("Recipe", "", (String) sortMenu.getSelectedItem());
										} catch (SQLException err) {
											JOptionPane.showMessageDialog(null, "Something went wrong");
										}
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You cannot delete other user's recipes!");
									}
								}
							});
							listeners.add(new ActionListener() {									
								public void actionPerformed (ActionEvent e){
									if (dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										addFrame("Steps");
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You cannot edit other user's recipes!");
									}
								}
									});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									if (!dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										addFrame("Reviews");
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You cannot review your own recipe!");
									}
								}
							});
							listeners.add(new ActionListener(){
								public void actionPerformed (ActionEvent e){
									searchTables("Reviews", table.getValueAt(table.getSelectedRow(), 0).toString(), (String) sortMenu.getSelectedItem());
									selectionMenu.setSelectedIndex(6);
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									if(dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString()))
									{
										int recipeIDToModify = Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString());
										modifyFrame("Recipe", recipeIDToModify);
									}
									else {
										JOptionPane.showMessageDialog(null, "You can't edit another user's recipe!");
									}
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									if(dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										addFrame("Ingredients");
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You can't add ingredients to another user's recipe!");
									}
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									searchTables("Ingredients", table.getValueAt(table.getSelectedRow(), 0).toString(), (String) sortMenu.getSelectedItem());
									selectionMenu.setSelectedIndex(4);
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									filterTables("Utensils", "RecipeID", table.getValueAt(table.getSelectedRow(), 0).toString());
									selectionMenu.setSelectedIndex(10);
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									if(dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										addFrame("Uses");
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You can't add ingredients to another user's recipe!");
									}
								}
							});
							break;
						case "Dish":
							names.add("View Recipes");
							names.add("View Recipes by Dish Type");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									searchTables("Recipe", table.getValueAt(table.getSelectedRow(), 0).toString(), (String) sortMenu.getSelectedItem());
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									searchTables("Recipe", table.getValueAt(table.getSelectedRow(), 2).toString(), (String) sortMenu.getSelectedItem());
									frameToDispose.dispose();
								}
							});
							break;
						case "User":
							names.add("View User's Recipes");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									filterTables("Recipe", "User", table.getValueAt(table.getSelectedRow(), 0).toString());
									frameToDispose.dispose();
								}
							});
							break;
						case "Cuisine":
							names.add("View Dishes");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									filterTables("Dish", "Cuisine", table.getValueAt(table.getSelectedRow(), 0).toString());
									frameToDispose.dispose();
								}
							});
							break;
						case "Steps":
							names.add("Edit Step");
							names.add("Expand step");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									int stepIDToModify = Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString());
									modifyFrame("Steps", stepIDToModify);
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									tableToExpand = "Steps";
									expandFrame(tableToExpand);
								}
							});
							break;
						case "Reviews":
							names.add("Edit Review");
							names.add("Expand Review");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									if(dbUsername.equals(table.getValueAt(table.getSelectedRow(), 0))) modifyFrame("Reviews", 0);
									else JOptionPane.showMessageDialog(null, "You can't edit another user's review!");
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									tableToExpand = "Reviews";
									expandFrame(tableToExpand);
								}
							});
					}
					contextMenu(names, listeners);
				}
				
			}
		});
	}
	
	public void setupFilters()
	{
		ArrayList<String> newFilters = new ArrayList<String>();
		selectionMenu.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				newFilters.clear();
				filterMenu.removeAllItems();
				newFilters.add("All");
				switch(selectionMenu.getSelectedItem().toString())
				{
					case "Dish":
						newFilters.add("Name");
						newFilters.add("Cuisine");
						newFilters.add("Type");
						break;
					case "Recipe":
						newFilters.add("Cuisine");
						newFilters.add("Dietary Type");
						break;
					case "Reviews":
						newFilters.add("Rating greater than");
						newFilters.add("Rating less than");
						newFilters.add("Rating equals");
				}
				for(int i = 0; i < newFilters.size(); i++)
				{
					filterMenu.insertItemAt(newFilters.get(i), i);
				}
			}
	    });
	}
	
	
	public void searchTables(String tableToSearch, String searchParam, String sortParam) {
		try {
			CallableStatement cs = this.con.prepareCall("{call search(?,?,?)}");
			cs.setString(1, tableToSearch);
			cs.setString(2, searchParam);
			cs.setString(3,  sortParam);
			cs.execute();
			
			ResultSet rs = cs.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			parseResults(rs, rsmd);
			
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Something went wrong");
		}
	}
	
	public void parseResults(ResultSet rs, ResultSetMetaData rsmd)
	{
		
		ArrayList<String> names = new ArrayList<String>();
		int columnCount;
		try {
			columnCount = rsmd.getColumnCount();
		
			numFields = columnCount;
		
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setColumnCount(0);
			model.setRowCount(0);
			for(int i = 0; i < columnCount; i++) {
				names.add(rsmd.getColumnName(i+1));
				model.addColumn(rsmd.getColumnName(i+1));
			}
			fieldNames = names;
			
			while(rs.next()) {
				Object[] data = new Object[names.size()];
				for(int j = 0; j < names.size(); j++) {
					data[j] = rs.getString(j+1);
				}
				model.addRow(data);
			}
			
			scrollPane.getViewport ().add (table);
		}
		catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Something went wrong");
		}		
	}
	
	private void contextMenu(ArrayList<String> buttons, ArrayList<ActionListener> listeners) {
		
		JFrame frame = new JFrame("Options");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel();
		
		for (int i = 0; i < buttons.size(); i++) {
			JButton button = new JButton(buttons.get(i));
			button.addActionListener(listeners.get(i));
			panel.add(button);
		}
		
		frame.add(panel);
		frame.pack();
		frameToDispose = frame;
		frame.setVisible(true);
	}
	
	public void filterTables(String tableToFilter, String filterColumn, String filterValue)
	{
		CallableStatement cs;
		try {
			cs = this.con.prepareCall("{call filter(?,?,?)}");
			cs.setString(1, tableToFilter);
			cs.setString(2, filterColumn);
			cs.setString(3, filterValue);
			cs.execute();
			ResultSet rs = cs.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			parseResults(rs, rsmd);
		}
		catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Something went wrong");
		}
	}
	
	public void setupFields(String table)
	{
		try {
			CallableStatement cs = con.prepareCall("{call search(?,?,?)}");
			cs.setString(1, table);
			cs.setString(2, "");
			cs.setString(3, (String) sortMenu.getSelectedItem());
			cs.execute();
			ResultSet rs = cs.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			
			ArrayList<String> names = new ArrayList<String>();
			int columnCount = rsmd.getColumnCount();
			numFields = columnCount;
		
			for(int i = 0; i < columnCount; i++) {
				if(rsmd.getColumnName(i+1).equals("DishID"))
				{
					names.add("DishName");
				}
				else if(rsmd.getColumnName(i+1).equals("UtensilID"))
				{
					names.add("UtensilName");
				}
				else
				{
					names.add(rsmd.getColumnName(i+1));					
				}
			}
			if(table.equals("Dish")) names.add("Cuisine Type");
			fieldNames = names;
			if(table.equals("Ingredients")) names.add("RecipeID");
			fieldNames = names;
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Something went wrong");
		}
	}
	
	public void modifyFrame(String tableToModify, int IDToModify)
	{
		setupFields(tableToModify);
		inputs.clear();
		ModifyFrame frame = new ModifyFrame(this.dbUsername, this.con, this.fieldNames, tableToModify, new ArrayList<JTextField>(), this.table, IDToModify);
		frame.open();
	}
	
	public void expandFrame(String expandThisTable) {
		ExpandFrame frame = new ExpandFrame(expandThisTable, table, con);
		frame.open();
	}
	
	public void addFrame(String tableToAddTo) 
	{
		setupFields(tableToAddTo);
		inputs.clear();
		AddFrame frame = new AddFrame(this.dbUsername, this.con, this.fieldNames, tableToAddTo, new ArrayList<JTextField>(), this.table, this);
		frame.open();
	}
	
	/**
	 * This button is attached to the 'Search' button, and executes the search when the button is hit.
	 * @author juricar
	 *
	 */
	private class SearchButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(String.valueOf(filterMenu.getSelectedItem()).equals("All") || filterMenu.getSelectedItem() == null)
			{
				searchTables(String.valueOf(selectionMenu.getSelectedItem()), "", (String) sortMenu.getSelectedItem());
			}
			else
			{
				filterTables(String.valueOf(selectionMenu.getSelectedItem()), String.valueOf(filterMenu.getSelectedItem()), String.valueOf(filterValue.getText()));
			}
		}
	}
	
	/**
	 * This button is attached to the 'Add' button, and executes the add when the button is hit.
	 * @author mcgeeca
	 *
	 */
	private class AddButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			tableToAddTo = String.valueOf(selectionMenu.getSelectedItem());
			if(tableToAddTo.equals("Steps") || tableToAddTo.equals("Reviews") || tableToAddTo.equals("Ingredients"))
			{
				JOptionPane.showMessageDialog(null, "You must add " + tableToAddTo.toLowerCase() + " through the recipe view");
			}
			else if(tableToAddTo.equals("Belongs To")) {
				JOptionPane.showMessageDialog(null,  "This is not a table in which you can add anything.  This is strictly for the user's view");
			}
			else
			{
				addFrame(tableToAddTo);
			}

		}

	}
	
}
