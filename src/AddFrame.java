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

public class AddFrame {
	
	String dbUsername;
	Connection con;
	JFrame adderFrame;
	ArrayList<String> fieldNames;
	String tableToAddTo;
	ArrayList<JTextField> inputs;
	JTable table;
	UseFrame uf;
	
	public AddFrame(String username, Connection receivedConnection, ArrayList<String> fields, String correctTable, ArrayList<JTextField> correctInputs, JTable table2, UseFrame uf) {
		this.dbUsername = username;
		this.con = receivedConnection;
		this.fieldNames = fields;
		this.tableToAddTo = correctTable;
		this.inputs = correctInputs;
		this.table = table2;
		this.uf = uf;
	}
	
	public void open() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel();
		
		for(int i = 0; i < this.fieldNames.size(); i++) {
			if(!((this.fieldNames.get(i).equals("RecipeName") && (this.tableToAddTo.equals("Has") || this.tableToAddTo.equals("Uses"))) || this.fieldNames.get(i).equals("ID") || this.fieldNames.get(i).equals("UserID") 
					|| this.fieldNames.get(i).equals("Username") || this.fieldNames.get(i).equals("Author") 
					|| ((tableToAddTo.equals("Steps")|| tableToAddTo.equals("Reviews") || tableToAddTo.equals("Uses") || tableToAddTo.equals("Ingredients")) 
							&& this.fieldNames.get(i).equals("RecipeID")))) {
				JTextField textBox = new JTextField(20);
				textBox.setText(this.fieldNames.get(i));
				panel.add(textBox);
				this.inputs.add(textBox);
			}
		}
		
		JButton completeAdd = new JButton("Complete " + tableToAddTo);
		completeAdd.addActionListener(new CompleteAddActionListener());
		this.adderFrame = frame;
		panel.add(completeAdd);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
	
	
	private class CompleteAddActionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			try {
				CallableStatement cs = null;
				int questionMarkIndex = 2;
				
				switch (tableToAddTo) {
					case "BelongsTo":
						cs = con.prepareCall("{? = call addBelongsTo(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
					
					case "Dish":
						cs = con.prepareCall("{? = call addDish(?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
					
					case "Has":
						if(!uf.checkInTable("Ingredients", String.valueOf(inputs.get(0).getText())))
						{
							uf.addFrame("Ingredients");
							return;
						}
						cs = con.prepareCall("{? = call addHas(?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, table.getValueAt(table.getSelectedRow(), 1).toString());
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, String.valueOf(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
						
					case "Ingredients":
						cs = con.prepareCall("{? = call addIngredients(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
					
					case "Recipe":
						cs = con.prepareCall("{? = call addRecipe(?,?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);

						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0 || i == 1) {
								cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
								questionMarkIndex++;
							}
							else if(i == 3)
							{
								String dishName = inputs.get(i).getText();
								cs.setString(questionMarkIndex, dishName);
								questionMarkIndex++;
								if(!uf.checkInTable("Dish", dishName))
								{
									uf.addFrame("Dish");
									return;
								}
							}
							else if (i == 2 ){
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
						
						cs.setString(6, dbUsername);
						
						break;
					
					case "Reviews":
						cs = con.prepareCall("{? = call addReviews(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, dbUsername);
						questionMarkIndex++;
						cs.setInt(questionMarkIndex, Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString()));
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0) {
								int rating = Integer.parseInt(inputs.get(i).getText());
								if (rating > 5 || rating < 0)
								{
									JOptionPane.showMessageDialog(null, "Rating must be between 0 and 5!");
									return;
								}
								cs.setInt(questionMarkIndex, rating);
								questionMarkIndex++;
							}
							else {
								cs.setString(questionMarkIndex, sanitize(inputs.get(i).getText()));
								questionMarkIndex++;
							}
						}
						break;
					
					case "Steps":
						cs = con.prepareCall("{? = call addSteps(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						System.out.println(inputs.size());
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
						cs.setInt(questionMarkIndex, Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString()));
						questionMarkIndex++;
						
						cs.setString(questionMarkIndex,  dbUsername);
						break;
					
					case "Utensils":
						cs = con.prepareCall("{? = call addUtensils(?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, sanitize(inputs.get(0).getText()));
						break;
					case "Cuisine":
						cs = con.prepareCall("{? = call addCuisine(?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, sanitize(inputs.get(0).getText()));
					case "Uses":
						cs = con.prepareCall("{? = call addUses(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, sanitize(inputs.get(0).getText()));
						questionMarkIndex++;
						cs.setInt(questionMarkIndex, Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString()));
				}
				cs.execute();
				adderFrame.dispose();
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(null, "Something went wrong with the button click");
				e.printStackTrace();
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
	
	
	
}
