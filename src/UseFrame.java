import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;


public class UseFrame {
	
	JFrame useFrame;
	JScrollPane scrollPane;
	JTable table;
	JComboBox<String> selectionMenu;
	JComboBox<String> filterMenu;
	ArrayList<JTextField> inputs = new ArrayList<JTextField>();
	String[] tableNames = {"Cuisine", "BelongsTo", "Dish", "Has", "Ingredients", "Recipe", "Reviews", "Steps", "User", "Uses", "Utensils"};
	String[] filters = {"All"};
	String tableToAddTo;
	String tableToModify;
	JTextField filterValue;
	int numFields;
	ArrayList<String> fieldNames;
	String dbUsername;
	Connection con;
	JFrame frameToDispose;
	JFrame adderFrame;



	public UseFrame(String username, Connection receivedConnection) {
		this.dbUsername = username;
		this.con = receivedConnection;
	}

	public void open() {
		setupUI();
		
		searchTables("BelongsTo","");
		
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
//		this.loginFrame.dispose();
//		if (this.inputFrame != null) {
//			this.inputFrame.dispose();
//			this.inputFrame = null;
//			System.out.println("Cleanup");
//		}
		
		this.useFrame = frame;
		
		JTable table2 = new JTable(new DefaultTableModel());		
		this.table = table2;
		this.table.setDefaultEditor(Object.class, null);
		this.table.getTableHeader().setReorderingAllowed(false); 
		
		this.scrollPane = new JScrollPane(table);

		
		JComboBox<String> tableList = new JComboBox<>(tableNames);
		tableList.setSelectedIndex(1);
		this.selectionMenu = tableList;
		
		JComboBox<String> filterList = new JComboBox<>(filters);
		filterList.setSelectedIndex(0);
		this.filterMenu = filterList;
		
		this.filterValue = new JTextField(25);
		this.filterValue.setText("Enter Filter Value");
		
		JButton searchButton = new JButton("Search");
		searchButton.addActionListener(new SearchButtonListener());
		
		JButton addButton = new JButton("Add");
		addButton.addActionListener(new AddButtonListener());

		topPanel.add(tableList, BorderLayout.WEST);
		topPanel.add(searchButton, BorderLayout.EAST);
		topPanel.add(addButton, BorderLayout.EAST);
		topPanel.add(filterList, BorderLayout.SOUTH);
		topPanel.add(this.filterValue, BorderLayout.SOUTH);
		panel.add(this.scrollPane, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
	
//	/**
//	 * The menu bar (for swapping between insert/update and selection)
//	 * @param frame
//	 */
//	private void createMenuBar(JFrame frame) {
//		JMenuBar menuBar = new JMenuBar();
//		
//		JMenuItem search = new JMenuItem("Search");
//		search.setToolTipText("Search through the Database");
//		search.addActionListener((event) -> open());
//		
//		frame.setJMenuBar(menuBar);
//		
//		
//	}
	
	void setupOnClick() // This function sets up the on click functionalities for various tables
	{
		this.table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				ArrayList<String> names = new ArrayList<String>();
				ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
				if(event.getValueIsAdjusting() && table.getSelectedRow() != -1) {
					switch(selectionMenu.getSelectedItem().toString()) {
						case "Recipe":
							names.add("View Steps");
							names.add("Delete");
							names.add("Add a step");
							names.add("Add a review");
							names.add("View reviews");
							names.add("Edit recipe");
							names.add("Add an ingredient");
							names.add("View Ingredients");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									searchTables("Steps", table.getValueAt(table.getSelectedRow(), 0).toString());
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
											searchTables("Recipe", "");
										} catch (SQLException err) {
											err.printStackTrace();
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
										tableToAddTo = "Steps";
										addFrame(tableToAddTo);
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You cannot edit other user's recipes!");
									}
								}
									});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									if (!dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										tableToAddTo = "Reviews";
										addFrame(tableToAddTo);
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You cannot review your own recipe!");
									}
								}
							});
							listeners.add(new ActionListener(){
								public void actionPerformed (ActionEvent e){
									searchTables("Reviews", table.getValueAt(table.getSelectedRow(), 0).toString());
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
//									searchTables("Reviews", "");
									tableToModify = "Recipe";
									int recipeIDToModify = (int) table.getValueAt(table.getSelectedRow(), 0);
//									modifyFrame(tableToModify, recipeIDToModify);
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									if(dbUsername.equals(table.getValueAt(table.getSelectedRow(), 4).toString())) {
										tableToAddTo = "Ingredients";
										addFrame(tableToAddTo);
										frameToDispose.dispose();
									} else {
										JOptionPane.showMessageDialog(null, "You can't add ingredients to another user's recipe!");
									}
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e) {
									searchTables("Ingredients", table.getValueAt(table.getSelectedRow(), 0).toString());
									selectionMenu.setSelectedIndex(4);
									frameToDispose.dispose();
								}
							});
							
							
							break;
						case "Dish":
							names.add("View Recipes");
							names.add("View Recipes by dish type");
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									searchTables("Recipe", table.getValueAt(table.getSelectedRow(), 0).toString());
									frameToDispose.dispose();
								}
							});
							listeners.add(new ActionListener() {
								public void actionPerformed (ActionEvent e){
									searchTables("Recipe", table.getValueAt(table.getSelectedRow(), 2).toString());
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
					
				}
				for(int i = 0; i < newFilters.size(); i++)
				{
					filterMenu.insertItemAt(newFilters.get(i), i);
				}
			}
	    });
	}
	
	
	public void searchTables(String tableToSearch, String searchParam) {
		try {
			CallableStatement cs = this.con.prepareCall("{call search(?,?)}");
			cs.setString(1, tableToSearch);
			cs.setString(2, searchParam);
			cs.execute();
			
			ResultSet rs = cs.getResultSet();
			ResultSetMetaData rsmd = rs.getMetaData();
			parseResults(rs, rsmd);
			
		} catch (SQLException e) {
			e.printStackTrace();
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
			e.printStackTrace();
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
			e.printStackTrace();
		}
	}
	
	private void addFrame(String tableToAddTo) {
		
		try {
			CallableStatement cs = con.prepareCall("{call search(?,?)}");
			cs.setString(1, tableToAddTo);
			cs.setString(2, "");
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
				else
				{
					names.add(rsmd.getColumnName(i+1));					
				}
			}
			if(tableToAddTo.equals("Dish")) names.add("Cuisine Type");
			fieldNames = names;
			if(tableToAddTo.equals("Ingredients")) names.add("RecipeID");
			fieldNames = names;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		inputs.clear();
		AddFrame frame = new AddFrame(this.dbUsername, this.con, this.fieldNames, this.tableToAddTo, this.inputs, this.table);
		frame.open();
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
//		JPanel panel = new JPanel();
//		
//		for(int i = 0; i < this.fieldNames.size(); i++) {
//			if(!(this.fieldNames.get(i).equals("ID") || this.fieldNames.get(i).equals("UserID") 
//					|| this.fieldNames.get(i).equals("Username") || this.fieldNames.get(i).equals("Author") 
//					|| ((tableToAddTo.equals("Steps")|| tableToAddTo.equals("Reviews")) && this.fieldNames.get(i).equals("RecipeID")))) {
//				JTextField textBox = new JTextField(20);
//				textBox.setText(this.fieldNames.get(i));
//				panel.add(textBox);
//				this.inputs.add(textBox);
//			}
//		}
//		
//		JButton completeAdd = new JButton("Complete " + tableToAddTo);
//		completeAdd.addActionListener(new CompleteAddActionListener());
//		this.adderFrame = frame;
//		panel.add(completeAdd);
//		frame.add(panel);
//		frame.pack();
//		frame.setVisible(true);
		
	}
	
//	private void modifyFrame(String tableToModify, int recipeIDToEdit) {
//		
//		try {
//			CallableStatement cs = con.prepareCall("{call search(?,?)}");
//			cs.setString(1, tableToModify);
//			cs.setString(2, "");
//			cs.execute();
//			ResultSet rs = cs.getResultSet();
//			ResultSetMetaData rsmd = rs.getMetaData();
//			
//			ArrayList<String> names = new ArrayList<String>();
//			int columnCount = rsmd.getColumnCount();
//			numFields = columnCount;
//		
//			for(int i = 0; i < columnCount; i++) {
//				if(rsmd.getColumnName(i+1).equals("DishID"))
//				{
//					names.add("DishName");
//				}
//				else
//				{
//					names.add(rsmd.getColumnName(i+1));					
//				}
//			}
//			if(tableToAddTo.equals("Dish")) names.add("Cuisine Type");
//			fieldNames = names;
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//		
//		inputs.clear();
//		ModifyFrame frame = new ModifyFrame(this.dbUsername, this.con, this.fieldNames, this.tableToAddTo, this.inputs, this.table, recipeIDToEdit);
//		frame.open();
//	}
	/**
	 * This button is attached to the 'Search' button, and executes the search when the button is hit.
	 * @author juricar
	 *
	 */
	private class SearchButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
//			System.out.println(String.valueOf(filterMenu.getSelectedItem()));
			if(String.valueOf(filterMenu.getSelectedItem()).equals("All") /*|| String.valueOf(filterMenu.getSelectedItem()) == null*/)
			{
//				System.out.println("I'm inside if statement");
				searchTables(String.valueOf(selectionMenu.getSelectedItem()), "");
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
			if(tableToAddTo.equals("Steps"))
			{
				JOptionPane.showMessageDialog(null, "You must add steps through the recipe view");
			}
			else if(tableToAddTo.equals("Reviews"))
			{
				JOptionPane.showMessageDialog(null, "You must add reviews through the recipe view");
			}
			else
			{
				addFrame(tableToAddTo);
			}

		}

	}
//	
//	private class CompleteAddActionListener implements ActionListener {
//		@Override
//		public void actionPerformed(ActionEvent arg0) {
//			try {
//				CallableStatement cs = null;
//				int questionMarkIndex = 2;
//
//				switch (tableToAddTo) {
//					case "BelongsTo":
//						cs = con.prepareCall("{? = call addBelongsTo(?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						for(int i = 0; i < inputs.size(); i++) {
//							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//							questionMarkIndex++;
//						}
//						break;
//					
//					case "Dish":
//						cs = con.prepareCall("{? = call addDish(?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						for(int i = 0; i < inputs.size(); i++) {
//							cs.setString(questionMarkIndex, inputs.get(i).getText());
//							questionMarkIndex++;
//						}
//						break;
//					
//					case "Has":
//						cs = con.prepareCall("{? = call addHas(?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						for(int i = 0; i < inputs.size(); i++) {
//							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//							questionMarkIndex++;
//						}
//						break;
//						
//					case "Ingredients":
//						cs = con.prepareCall("{? = call addIngredients(?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						for(int i = 0; i < inputs.size(); i++) {
//							cs.setString(questionMarkIndex, inputs.get(i).getText());
//							questionMarkIndex++;
//						}
//						break;
//					
//					case "Recipe":
//						cs = con.prepareCall("{? = call addRecipe(?,?,?,?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//
//						for(int i = 0; i < inputs.size(); i++) {
////							System.out.println(inputs.get(i).getText());
//							if(i == 0 || i == 1 || i == 3) {
//								cs.setString(questionMarkIndex, inputs.get(i).getText());
//								questionMarkIndex++;
//							}
//							else if (i == 2 ){
//								if(inputs.get(i).getText().isEmpty())
//								{
//									cs.setString(questionMarkIndex, inputs.get(i).getText());
//								}
//								else
//								{
//									cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//								}
//								questionMarkIndex++;
//							}
//							else {
//								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//								questionMarkIndex++;
//							}
//						}
//						
//						cs.setString(6, dbUsername);
//						break;
//					
//					case "Reviews":
//						cs = con.prepareCall("{? = call addReviews(?,?,?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						cs.setString(questionMarkIndex, dbUsername);
//						questionMarkIndex++;
//						cs.setInt(questionMarkIndex, Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString()));
//						questionMarkIndex++;
//						for(int i = 0; i < inputs.size(); i++) {
//							if(i == 0) {
//								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//								questionMarkIndex++;
//							}
//							else {
//								cs.setString(questionMarkIndex, inputs.get(i).getText());
//								questionMarkIndex++;
//							}
//						}
//						break;
//					
//					case "Steps":
//						cs = con.prepareCall("{? = call addSteps(?,?,?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						for(int i = 0; i < inputs.size(); i++) {
//							if(i == 0) {
//								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//								questionMarkIndex++;
//							} 
//							else {
//								cs.setString(questionMarkIndex, inputs.get(i).getText());
//								questionMarkIndex++;
//							}
//						}
//						cs.setInt(questionMarkIndex, Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString()));
//						
//						cs.setString(questionMarkIndex, dbUsername);
//						break;
//					
//					case "Utensils":
//						cs = con.prepareCall("{? = call addUtensils(?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						cs.setString(questionMarkIndex, inputs.get(0).getText());
//						break;
//				}
//				cs.execute();
////				System.out.println("Adding Dish complete!");
//				adderFrame.dispose();
//			} catch (SQLException e) {
//				System.out.println("Didn't work again.");
//				e.printStackTrace();
//			}
//			
//		}
//	}
}
