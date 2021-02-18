import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Base64;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

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
	private JTextField userBox;
	private JTextField passBox;
	JScrollPane scrollPane;
	String user = "juricar";
	String pass = "Atsknktvegef24035526LCA!";
	protected String dbUsername;
	private String dbPassword;
	
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
		this.loginFrame.dispose();
		UseFrame frame = new UseFrame(this.dbUsername, this.connection);
		frame.open();
		return;
	}
	

	/**
	 * This method makes the login dialog, where the user enters login credentials to access our database.
	 * @return
	 */
	public JFrame makeLoginDialog() {
		JFrame frame = new JFrame("Cookshare Login");
		frame.getContentPane().setBackground(Color.BLACK);
		frame.getContentPane().setForeground(Color.WHITE);
		frame.setBounds(100,100,337,343);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JTextField usernameEntry = new JTextField(25);
		usernameEntry.setBounds(109, 106, 135, 45);
		frame.getContentPane().add(usernameEntry);
		usernameEntry.setColumns(10);
		
		JLabel loginUsernameLabel = new JLabel("Username");
		loginUsernameLabel.setForeground(Color.RED);
		loginUsernameLabel.setBackground(Color.BLACK);
		loginUsernameLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
		loginUsernameLabel.setBounds(28, 113, 71, 29);
		frame.getContentPane().add(loginUsernameLabel);
		
		JLabel loginPasswordLabel = new JLabel("Password");
		loginPasswordLabel.setForeground(Color.RED);
		loginPasswordLabel.setBackground(Color.WHITE);
		loginPasswordLabel.setFont(new Font("Times New Roman", Font.BOLD, 15));
		loginPasswordLabel.setBounds(28, 168, 71, 29);
		frame.getContentPane().add(loginPasswordLabel);
		
		JTextField passwordEntry = new JTextField(25);
		passwordEntry.setColumns(10);
		passwordEntry.setBounds(109, 161, 135, 45);
		frame.getContentPane().add(passwordEntry);
		
		JButton loginButton = new JButton("LOGIN");
		loginButton.setFont(new Font("Times New Roman", Font.BOLD, 16));
		loginButton.addActionListener(new LoginActionListener());
		loginButton.setBounds(28, 231, 101, 37);
		frame.getContentPane().add(loginButton);
		
		JButton registerButton = new JButton("REGISTER");
		registerButton.addActionListener(new RegisterActionListener());
		registerButton.setFont(new Font("Times New Roman", Font.BOLD, 16));
		registerButton.setBounds(154, 231, 129, 37);
		frame.getContentPane().add(registerButton);
		
		JLabel loginWelcomeToCookshareLabel = new JLabel("WELCOME TO COOKSHARE!");
		loginWelcomeToCookshareLabel.setFont(new Font("Times New Roman", Font.BOLD, 20));
		loginWelcomeToCookshareLabel.setHorizontalAlignment(SwingConstants.CENTER);
		loginWelcomeToCookshareLabel.setForeground(Color.RED);
		loginWelcomeToCookshareLabel.setBackground(Color.BLACK);
		loginWelcomeToCookshareLabel.setBounds(10, 36, 296, 45);
		frame.getContentPane().add(loginWelcomeToCookshareLabel);

		
		this.userBox = usernameEntry;
		this.passBox = passwordEntry;
		
		frame.setLocationRelativeTo(null);		
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
			if(dbUsername.contains(";") || dbUsername.contains("@") || dbUsername.contains("#") || dbUsername.contains("%") || dbUsername.contains(" "))
			{
				JOptionPane.showMessageDialog(null, "Registration failed, the username must not contain any special characters");
				return;
			}
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
				return;
			}
			openUseFrame();
		}

		private void openUseFrame() {
			CookshareConnectionService.this.openUseFrame();
		}

	}
	
}