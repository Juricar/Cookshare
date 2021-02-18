import java.awt.BorderLayout;
import java.awt.Font;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ExpandFrame {
	
	String currentTable;
	JTable table;
	Connection con;
	
	public ExpandFrame(String tableUsing, JTable table, Connection con) {
		this.currentTable = tableUsing;
		this.table = table;
		this.con = con;
	}
	
	public void open() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setBounds(100, 100, 749, 418);
		JPanel panel = new JPanel();
		panel.setBorder(new EmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(0, 0));
		frame.setContentPane(panel);
		
		JLabel lblNewLabel = new JLabel("New label");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 45));
		panel.add(lblNewLabel, BorderLayout.NORTH);
		
		JLabel lblNewLabel_1 = new JLabel("Rating:");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 25));
		panel.add(lblNewLabel_1, BorderLayout.SOUTH);
		
		JTextArea textArea = new JTextArea();
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 15));
		panel.add(textArea, BorderLayout.CENTER);
		textArea.setLineWrap(true);
		
		if(this.currentTable.equals("Reviews")) {
			String recipeName;
			CallableStatement cs;
			try {
				cs = con.prepareCall("{? = call getRecipeName(?)}");
				int recipeID =  Integer.parseInt((table.getValueAt(table.getSelectedRow(), 1)).toString());
				String reviewCreator =  table.getValueAt(table.getSelectedRow(), 0).toString();
				int stars = Integer.parseInt((table.getValueAt(table.getSelectedRow(), 2)).toString());
				String reviewText = table.getValueAt(table.getSelectedRow(), 3).toString();
				cs.registerOutParameter(1, Types.VARCHAR);
				cs.setInt(2, recipeID);
				cs.execute();
				ResultSet rs = cs.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				rs.next();
				recipeName = rs.getObject("Name").toString();
				
				frame.setTitle("Expanded Review");
				lblNewLabel.setText(recipeName + " Review by " + reviewCreator);
				lblNewLabel_1.setText("Rating: " + stars);
				textArea.setText(reviewText);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		else if(this.currentTable.equals("Steps"))
		{
			String recipeName;
			CallableStatement cs;
			try {
				int recipeID =  Integer.parseInt((table.getValueAt(table.getSelectedRow(), 3)).toString());
				int stepNumber = Integer.parseInt((table.getValueAt(table.getSelectedRow(), 1)).toString());
				String stepText = table.getValueAt(table.getSelectedRow(), 2).toString();
				cs = con.prepareCall("{? = call getRecipeName(?)}");
				cs.registerOutParameter(1, Types.VARCHAR);
				cs.setInt(2, recipeID);
				cs.execute();
				ResultSet rs = cs.getResultSet();
				ResultSetMetaData rsmd = rs.getMetaData();
				rs.next();
				recipeName = rs.getObject("Name").toString();
				
				frame.setTitle("Expanded Step");
				lblNewLabel.setText("Step Number " + stepNumber + " of " + recipeName);
				textArea.setText(stepText);
			}
			catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		frame.setVisible(true);
	}
	
}
