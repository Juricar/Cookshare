import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;


public class ModifyFrame {
	String dbUsername;
	Connection con;
	JFrame adderFrame;
	ArrayList<String> fieldNames;
	String tableToModify;
	ArrayList<JTextField> inputs;
	JTable table;
	int IDToModify;
	UseFrame uf;
	
	public ModifyFrame(String username, Connection receivedConnection, ArrayList<String> fields, String correctTable, ArrayList<JTextField> correctInputs, JTable table2, int IDToModify, UseFrame uf) {
		this.dbUsername = username;
		this.con = receivedConnection;
		this.fieldNames = fields;
		this.tableToModify = correctTable;
		this.inputs = correctInputs;
		this.table = table2;
		this.IDToModify = IDToModify;
		this.uf = uf;
	}
	/*
	 This is just the AddFrame's code copied over.  Need to change this eventually. 
	 
	 */
	public void open() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel();
		
		for(int i = 0; i < this.fieldNames.size(); i++) {
			if(!((this.fieldNames.get(i).equals("RecipeName") && (this.tableToModify.equals("Has") || this.tableToModify.equals("Uses"))) || this.fieldNames.get(i).equals("ID") || this.fieldNames.get(i).equals("UserID") 
					|| this.fieldNames.get(i).equals("Username") || this.fieldNames.get(i).equals("Author") 
					|| ((tableToModify.equals("Steps")|| tableToModify.equals("Reviews") || tableToModify.equals("Has")) && this.fieldNames.get(i).equals("RecipeID"))
					|| (tableToModify.equals("Recipe") && this.fieldNames.get(i).equals("Name")))) {
				JTextField textBox = new JTextField(20);
				textBox.setText(this.fieldNames.get(i));
				panel.add(textBox);
				this.inputs.add(textBox);
			}
		}
		
		JButton completeModify = new JButton("Complete Modifying" + tableToModify);
		completeModify.addActionListener(new CompleteModifyActionListener());
		this.adderFrame = frame;
		panel.add(completeModify);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	private class CompleteModifyActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				CallableStatement cs = null;
				int questionMarkIndex = 2;

				switch (tableToModify) {
//					case "BelongsTo":
//						cs = con.prepareCall("{? = call addBelongsTo(?,?)}");
//						cs.registerOutParameter(1, Types.INTEGER);
//						for(int i = 0; i < inputs.size(); i++) {
//							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
//							questionMarkIndex++;
//						}
//						break;
					case "Uses":
						cs = con.prepareCall("{? = call modifyUses(?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(2, table.getValueAt(table.getSelectedRow(), 0).toString());
						cs.setString(3, table.getValueAt(table.getSelectedRow(), 1).toString());
						cs.setString(4, sanitize(inputs.get(0).getText()));
						break;
					case "Has":
						if(!uf.checkInTable("Ingredients", String.valueOf(inputs.get(0).getText())))
						{
							uf.addFrame("Ingredients");
							return;
						}
						cs = con.prepareCall("{? = call modifyHas(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(2, table.getValueAt(table.getSelectedRow(), 0).toString());
						cs.setString(3, table.getValueAt(table.getSelectedRow(), 1).toString());
						cs.setString(4, sanitize(inputs.get(0).getText()));
						cs.setString(5, sanitize(inputs.get(1).getText()));
						break;
					
					case "Recipe":
						cs = con.prepareCall("{? = call modifyRecipe(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setInt(questionMarkIndex, IDToModify);
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0 || i == 2) {
								cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
								questionMarkIndex++;
							}
							else if (i == 1){
								if(inputs.get(i).getText().isEmpty())
								{
									cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
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
						break;
					
					case "Reviews":
						cs = con.prepareCall("{? = call modifyReview(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, dbUsername);
						questionMarkIndex++;
						cs.setInt(questionMarkIndex, Integer.parseInt((table.getValueAt(table.getSelectedRow(), 1)).toString()));
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0) {
								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								questionMarkIndex++;
							}
							else {
								cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
								questionMarkIndex++;
							}
						}
						break;
					
					case "Steps":
						cs = con.prepareCall("{? = call modifySteps(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setInt(questionMarkIndex, IDToModify);
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0) {
								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								questionMarkIndex++;
							} 
							else {
								cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
								questionMarkIndex++;
							}
							
						}
						cs.setString(questionMarkIndex,  dbUsername);
						break;
				}
				cs.execute();
//				System.out.println("Adding Dish complete!");
				adderFrame.dispose();
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Something went wrong with the modify button click");
				e.printStackTrace();
			}
			
		}
	}
	
	private String sanitize(String text) {
		// TODO Auto-generated method stub
		StringBuilder sanitizedString = new StringBuilder();
		for(int i = 0; i < text.length(); i++)
		{
			if(text.charAt(i) == ';' || text.charAt(i) == '@' || text.charAt(i) == '#' || text.charAt(i) == '%')
			{
				sanitizedString.append(' ');
			}
			else
			{
				sanitizedString.append(text.charAt(i));
			}
		}
		return sanitizedString.toString();
	}
}
