import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
	private ArrayList<JTextField> inputs;
	private JRadioButton isUpdate;
	private String[] tableNames = {"Cuisine", "BelongsTo", "Dish", "Has", "Ingredients", "Recipe", "Reviews", "Steps", "User", "Uses", "Utensils"};
	private JButton searchButton;
	private boolean isGuest;
	
	private final String STARTINGTABLE = "People";
	
	/**
	 * A basic constructor for our connection service.
	 * @param serverName the name of the server we are connecting to
	 * @param databaseName our database name
	 */
	public CookshareConnectionService(String serverName, String databaseName) {
		this.serverName = serverName;
		this.databaseName = databaseName;
		this.loginFrame = makeLoginDialog();
		this.isGuest = true;
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
//			String query = "EXEC "+this.procLookupTables.get(STARTINGTABLE); //The starting info, in there by default.
//			PreparedStatement ps = connection.prepareStatement(query);
			String query = "Select * From Recipe";
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
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
			
			
			
			/*model.addColumn("Username"); 
			model.addColumn("Full Name");
			model.addColumn("Join Date");
			while(rs.next()) {
				i++;
				
				model.addRow(new Object[]{rs.getString("Username"), rs.getString("FullName"), rs.getString("JoinDate")});
				
				//System.out.println(rs.getString("Username") + " : " + rs.getString("Pswd") + " : " +rs.getString("FullName") + " : " +rs.getString("JoinDate"));
			}*/
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
			
			topPanel.add(tableList, BorderLayout.WEST);
			topPanel.add(searchButton, BorderLayout.EAST);
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
		JButton connectButton = new JButton("Connect");
		connectButton.addActionListener(new ConnectActionListener());
		JButton connectGuestButton = new JButton("Connect As Guest");
		connectGuestButton.addActionListener(new ConnectGuestActionListener());
		
		panel.add(usernameEntry);
		panel.add(passwordEntry);
		panel.add(connectButton);
		panel.add(connectGuestButton);
		
		this.userBox = usernameEntry;
		this.passBox = passwordEntry;
		
		frame.pack();
		frame.setVisible(true);
		return frame;
	}
	
	/**
	 * This action listener is associated with the connection button, and calls the 
	 * relevant method when the button is hit
	 * @author juricar
	 *
	 */
	private class ConnectActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean connectionSuccessful = connectFromFrame();
			if(!connectionSuccessful) {
				System.out.println("Failed Connection");
				JOptionPane.showMessageDialog(null, "Failed Connection");
				return;
			}
			CookshareConnectionService.this.setIsGuest(false);
			openUseFrame();
		}

		private void openUseFrame() {
			CookshareConnectionService.this.openUseFrame();
		}

	}
	
	private class ConnectGuestActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			boolean connectionSuccessful = connectGuest();
			if(!connectionSuccessful) {
				System.out.println("Failed Connection");
				JOptionPane.showMessageDialog(null, "Failed Connection");
				return;
			}
			openUseFrame();
		}

		private void openUseFrame() {
			CookshareConnectionService.this.openUseFrame();
		}

	}
	
	/**
	 * This method gets called when the 'connect' button gets hit. Takes info from the user and password
	 * boxes and connects with it.
	 * @return
	 */
	private boolean connectFromFrame() {
		
		String user = this.userBox.getText();
		String pass = this.passBox.getText();
		
		return connect(user, pass);
	}
	
	/**
	 * This just sets our user to a guest when they login without an administrator username and password.
	 * @param b
	 */
	public void setIsGuest(boolean b) {
		this.isGuest = b;		
	}

	private boolean connectGuest() {
		
		String user = "juricar";
		String pass = "Atsknktvegef24035526LCA!";
		
		return connect(user, pass);
	}
	
	/**
	 * This button is attached to the 'Search' button, and executes the search when the button is hit.
	 * @author juricar
	 *
	 */
	private class SearchButtonListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			String query = "SELECT * FROM ";
			try {
				query += String.valueOf(selectionMenu.getSelectedItem());
				CallableStatement cs = getConnection().prepareCall(query);
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
}
