import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Array;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicOptionPaneUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
/**
 * This method handles connection to the database, along with the GUI
 * @author juricar
 *
 */
public class CookshareConnectionService {
	
	private final String connectionURL = "jdbc:sqlserver://${dbServer};databaseName=${dbName};user=${user};password={${pass}}";

	private Connection connection = null;
	private String serverName, databaseName;
	private JFrame loginFrame;
	private JFrame useFrame;
	private JFrame inputFrame;
	private JFrame adderFrame;
	private JTextField userBox;
	private JTextField passBox;
	private JTextField searchBox;
	private JTable table;
	private JComboBox selectionMenu;
	private JComboBox constraintMenu;
	JScrollPane scrollPane;
	private int numFields;
	private ArrayList<String> fieldNames;
	private JPanel inputPanel;
	private ArrayList<JTextField> inputs = new ArrayList<JTextField>();
	private JRadioButton isUpdate;
	private String[] tableNames = {"Cuisine", "BelongsTo", "Dish", "Has", "Ingredients", "Recipe", "Reviews", "Steps", "User", "Uses", "Utensils"};
	private JButton searchButton;
	private JButton addButton;
	private JButton deleteButton;
	private String tableToAddTo;
	String user = "juricar";
	String pass = "Atsknktvegef24035526LCA!";
	private String dbUsername;
	private String dbPassword;
	
	private final String STARTINGTABLE = "People";
	
	private static final Random RANDOM = new SecureRandom();
	private static final Base64.Encoder enc = Base64.getEncoder();
	private static final Base64.Decoder dec = Base64.getDecoder();
	
	/**
	 * A basic constructor for our connection service.
	 * @param serverName the name of the server we are connecting to
	 * @param databaseName our database name
	 */
	public CookshareConnectionService(String serverName, String databaseName) {
		this.serverName = serverName;
		this.databaseName = databaseName;
		this.connect(user,pass);
		this.loginFrame = makeLoginDialog();
	}
	
	/**
	 * This is the method that connects to the database.
	 * @param user username
	 * @param pass password
	 * @return
	 */
	public boolean connect(String user, String pass) {
		
		String connectionString = connectionURL.replace("${dbServer}", serverName).replace("${dbName}", databaseName).replace("${user}", user).replace("{${pass}}", pass);
		//System.out.println(connectionString);
		try {
			Connection c = DriverManager.getConnection(connectionString);
			this.connection = c;
			return true;
			
		}
		catch(SQLException e) {
			System.out.println("Invalid Login Credentials");
			System.out.println(e);
			JOptionPane.showMessageDialog(null, "Invalid Login Credentials");
		}
		return false;	
	}
	
	/**
	 * @return a connection object (our connection)
	 */
	public Connection getConnection() {
		return this.connection;
	}
	
	/**
	 * closes the connection
	 */
	public void closeConnection() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			JOptionPane.showMessageDialog(null, "Failed To Close Connection");
		}
	}	
	
	/**
	 * Opens the 'use' frame, where we can select from and edit our data.
	 */
	public void openUseFrame() {
		if(this.useFrame != null) {
			return;
		}
		JFrame frame = new JFrame("Cookshare");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		createMenuBar(frame);
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.CENTER);
		JPanel topPanel = new JPanel();
		panel.add(topPanel, BorderLayout.PAGE_START);
		this.loginFrame.dispose();
		if (this.inputFrame != null) {
			this.inputFrame.dispose();
			this.inputFrame = null;
			System.out.println("Cleanup");
		}
		this.useFrame = frame;
		
		try {
			CallableStatement cs = getConnection().prepareCall("{call search(?)}");
			cs.setString(1, "BelongsTo");
			cs.execute();
			ResultSet rs = cs.getResultSet();;
			ResultSetMetaData rsmd = rs.getMetaData();
			//System.out.println("Made Query");
			
			JTable table = new JTable(new DefaultTableModel());
			ArrayList<String> names = new ArrayList<String>();
			int columnCount = rsmd.getColumnCount();
			numFields = columnCount;
		
			DefaultTableModel model = (DefaultTableModel) table.getModel();
			model.setColumnCount(0);
			model.setRowCount(0);
			for(int i = 0; i < columnCount; i++) {
				names.add(rsmd.getColumnName(i+1));
				model.addColumn(rsmd.getColumnName(i+1));
				
			}
			fieldNames = names;
			
			int i = 0;
			while(rs.next()) {
				i++;
				Object[] data = new Object[names.size()];
				for(int j = 0; j < names.size(); j++) {
					data[j] = rs.getString(j+1);
				}
				model.addRow(data);
			}
			
			scrollPane = new JScrollPane(table);
			
			this.table = table;
			table.setDefaultEditor(Object.class, null);
			table.getTableHeader().setReorderingAllowed(false); 
			
			JComboBox tableList = new JComboBox(tableNames);
			tableList.setSelectedIndex(1);
			this.selectionMenu = tableList;
			
			JButton searchButton = new JButton("Search");
			searchButton.addActionListener(new SearchButtonListener());
			this.searchButton = searchButton;
			
			JButton addButton = new JButton("Add");
			addButton.addActionListener(new AddButtonListener());
			this.addButton = addButton;
			
			JButton deleteButton = new JButton("Delete");
//			deleteButton.addActionListener(new DeleteButtonListener());
			this.deleteButton = deleteButton;
			
			topPanel.add(tableList, BorderLayout.WEST);
			topPanel.add(searchButton, BorderLayout.EAST);
			topPanel.add(addButton, BorderLayout.EAST);
			topPanel.add(deleteButton, BorderLayout.EAST);
			panel.add(scrollPane, BorderLayout.CENTER);
			frame.pack();
			frame.setVisible(true);
			//System.out.println("Returned");
			return;
		}
		catch(SQLException e) {
			System.out.println(e);
			JOptionPane.showMessageDialog(null, "Failed to open use frame");
		}
		closeConnection();
	}
	
	/**
	 * The menu bar (for swapping between insert/update and selection)
	 * @param frame
	 */
	private void createMenuBar(JFrame frame) {
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Applications");
		
		JMenuItem search = new JMenuItem("Search");
		search.setToolTipText("Search through the Database");
		search.addActionListener((event) -> openUseFrame());
				
		menu.add(search);
		
		menuBar.add(menu);
		
		frame.setJMenuBar(menuBar);
		
		
	}
	
	/**
	 * This method makes the login dialog, where the user enters login credentials to access our database.
	 * @return
	 */
	public JFrame makeLoginDialog() {
		JFrame frame = new JFrame("Cookshare");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		frame.add(panel, BorderLayout.CENTER);
		
		JTextField usernameEntry = new JTextField(25);
		usernameEntry.setText("Username"); //default credentials
		JTextField passwordEntry = new JTextField(25);
		passwordEntry.setText("Password");
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(new LoginActionListener());
		JButton registerButton = new JButton("Register");
		registerButton.addActionListener(new RegisterActionListener());
		
		panel.add(usernameEntry);
		panel.add(passwordEntry);
		panel.add(loginButton);
		panel.add(registerButton);
		
		this.userBox = usernameEntry;
		this.passBox = passwordEntry;
		
		frame.pack();
		frame.setVisible(true);
		return frame;
	}
	
	public byte[] getNewSalt() {
		byte[] salt = new byte[16];
		RANDOM.nextBytes(salt);
		return salt;
	}
	
	public String getStringFromBytes(byte[] data) {
		return enc.encodeToString(data);
	}

	public String hashPassword(byte[] salt, String password) {

		KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
		SecretKeyFactory f;
		byte[] hash = null;
		try {
			f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			hash = f.generateSecret(spec).getEncoded();
		} catch (NoSuchAlgorithmException e) {
			JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
			e.printStackTrace();
		}
		return getStringFromBytes(hash);
	}
	
	/**
	 * This action listener is associated with the connection button, and calls the 
	 * relevant method when the button is hit
	 * @author juricar
	 *
	 */
	private class RegisterActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			dbUsername = userBox.getText();
			dbPassword = passBox.getText();
			byte[] salt = getNewSalt();
			String hPwd = hashPassword(salt, dbPassword);
			Connection con = getConnection();
			try 
			{
				CallableStatement cs = con.prepareCall("{? = call Register(?,?,?)}");
				cs.registerOutParameter(1, Types.INTEGER);
				cs.setString(2, dbUsername);
				cs.setBytes(3, salt);
				cs.setString(4, hPwd);
				cs.execute();
				int returnValue = cs.getInt(1);
				if(returnValue != 0)
				{
					JOptionPane.showMessageDialog(null, "Registration failed");
					return;
				}
			} 
			catch (SQLException e1) 
			{
				e1.printStackTrace();
				JOptionPane.showMessageDialog(null, "Registration failed");
				return;
			}
			openUseFrame();
		}

		private void openUseFrame() {
			CookshareConnectionService.this.openUseFrame();
		}

	}
	
	private class LoginActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			String query = "SELECT PasswordSalt, PasswordHash FROM [Cookshare].[dbo].[User] WHERE Username = ?";
			String salt = null;
			String hPwd = null;
			dbUsername = userBox.getText();
			dbPassword = passBox.getText();
			try 
			{
				PreparedStatement stmt = getConnection().prepareStatement(query);
				stmt.setString(1, dbUsername);
				ResultSet rs = stmt.executeQuery();
				rs.next();
				salt = rs.getString("PasswordSalt");
				hPwd = rs.getString("PasswordHash");
				if(salt == null)
				{
					JOptionPane.showMessageDialog(null, "Login failed");
					return;
				}
				String checkPwd = hashPassword(salt.getBytes(), dbPassword);
				if(!(checkPwd.equals(hPwd)))
				{
					JOptionPane.showMessageDialog(null, "Login failed");
					return;
				}
			} 
			catch (SQLException e1) 
			{
				JOptionPane.showMessageDialog(null, "Login failed");
				e1.printStackTrace();
				return;
			}
			openUseFrame();
		}

		private void openUseFrame() {
			CookshareConnectionService.this.openUseFrame();
		}

	}

	/**
	 * This button is attached to the 'Search' button, and executes the search when the button is hit.
	 * @author juricar
	 *
	 */
	private class SearchButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
//			String query = "SELECT * FROM ";
			try {
//				query += "[" + String.valueOf(selectionMenu.getSelectedItem()) + "]";
				CallableStatement cs = getConnection().prepareCall("{call search(?)}");
				cs.setString(1, String.valueOf(selectionMenu.getSelectedItem()));
				cs.execute();
				ResultSet rs = cs.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				//System.out.println("Made Query");
				
				JTable table = new JTable(new DefaultTableModel());
				ArrayList<String> names = new ArrayList<String>();
				int columnCount = rsmd.getColumnCount();
				numFields = columnCount;
			
				DefaultTableModel model = (DefaultTableModel) table.getModel();
				model.setColumnCount(0);
				model.setRowCount(0);
				for(int i = 0; i < columnCount; i++) {
					names.add(rsmd.getColumnName(i+1));
					model.addColumn(rsmd.getColumnName(i+1));
					
				}
				fieldNames = names;
				
				int i = 0;
				while(rs.next()) {
					i++;
					Object[] data = new Object[names.size()];
					for(int j = 0; j < names.size(); j++) {
						data[j] = rs.getString(j+1);
					}
					model.addRow(data);
				}
				
				scrollPane.getViewport ().add (table);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
//			String query = "SELECT * FROM ";
			try {
//				query += String.valueOf(selectionMenu.getSelectedItem());
//				System.out.println(query);
				CallableStatement cs = getConnection().prepareCall("{call search(?)}");
				cs.setString(1, String.valueOf(selectionMenu.getSelectedItem()));				
				cs.execute();
				ResultSet rs = cs.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				
				
				JTable table = new JTable(new DefaultTableModel());
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
				fieldNames = names;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			addFrame(tableToAddTo);

		}

	}
	
	private class CompleteAddActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				Connection con = getConnection();
				CallableStatement cs = null;
				int questionMarkIndex = 2;

				switch (tableToAddTo) {
					case "BelongsTo":
						cs = getConnection().prepareCall("{? = call addBelongsTo(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
					
					case "Dish":
						cs = getConnection().prepareCall("{? = call addDish(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, inputs.get(i).getText());
							questionMarkIndex++;
						}
						break;
					
					case "Has":
						cs = getConnection().prepareCall("{? = call addHas(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
						
					case "Ingredients":
						cs = getConnection().prepareCall("{? = call addIngredients(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, inputs.get(i).getText());
							questionMarkIndex++;
						}
						break;
					
					case "Recipe":
						cs = getConnection().prepareCall("{? = call addRecipe(?,?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
//						System.out.println(inputs.get(0).getText());
//						System.out.println(inputs.get(1).getText());
//						System.out.println(inputs.get(2).getText());
//						System.out.println(inputs.get(3).getText());

						for(int i = 0; i < inputs.size(); i++) {
//							System.out.println(inputs.get(i).getText());
							if(i == 0 || i == 1 || i == 3) {
								cs.setString(questionMarkIndex, inputs.get(i).getText());
								questionMarkIndex++;
							}
							else if (i == 2 ){
								if(inputs.get(i).getText().isEmpty())
								{
									cs.setString(questionMarkIndex, inputs.get(i).getText());
								}
								else
								{
									cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								}
								questionMarkIndex++;
							}
							else {
								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								questionMarkIndex++;
							}
						}
						
						cs.setString(6, dbUsername);
						break;
					
					case "Reviews":
						cs = getConnection().prepareCall("{? = call addReviews(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, dbUsername);
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0 || i == 1) {
								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								questionMarkIndex++;
							}
							else {
								cs.setString(questionMarkIndex, inputs.get(i).getText());
								questionMarkIndex++;
							}
						}
						break;
					
					case "Steps":
						cs = getConnection().prepareCall("{? = call addSteps(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0 || i == 2) {
								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								questionMarkIndex++;
							} 
							else {
								cs.setString(questionMarkIndex, inputs.get(i).getText());
								questionMarkIndex++;
							}
						}
						cs.setString(questionMarkIndex, dbUsername);
						break;
					
					case "Utensils":
						cs = getConnection().prepareCall("{? = call addUtensils(?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, inputs.get(0).getText());
						break;
				}
				cs.execute();
//				System.out.println("Adding Dish complete!");
				adderFrame.dispose();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				System.out.println("Didn't work again.");
				e.printStackTrace();
			}
			
		}
	}
	
	private void addFrame(String tableToAddTo) {
		
		inputs.clear();
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel panel = new JPanel();
		
		for(int i = 0; i < this.fieldNames.size(); i++) {
			if(!(this.fieldNames.get(i).equals("ID") || this.fieldNames.get(i).equals("UserID") || this.fieldNames.get(i).equals("Username") || this.fieldNames.get(i).equals("Author"))) {
				JTextField textBox = new JTextField(50);
				textBox.setText(this.fieldNames.get(i));
				panel.add(textBox);
				this.inputs.add(textBox);
			}
//			else if(this.fieldNames.get(i).equals("ID") && String.valueOf(selectionMenu.getSelectedItem()).equals("Steps")) {
//				JTextField textBox = new JTextField(50);
//				textBox.setText(this.fieldNames.get(i));
//				panel.add(textBox);
//				this.inputs.add(textBox);
//			}
		}
		
		JButton completeAdd = new JButton("Complete " + tableToAddTo);
		completeAdd.addActionListener(new CompleteAddActionListener());
		this.adderFrame = frame;
		panel.add(completeAdd);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
		
	}
	
	
}