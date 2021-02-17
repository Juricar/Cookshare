import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;


public class ModifyFrame {
	String dbUsername;
	Connection con;
	JFrame adderFrame;
	ArrayList<String> fieldNames;
	String tableToAddTo;
	ArrayList<JTextField> inputs;
	JTable table;
	int recipeID;
	
	public ModifyFrame(String username, Connection receivedConnection, ArrayList<String> fields, String correctTable, ArrayList<JTextField> correctInputs, JTable table2, int CorrectRecipeID) {
		this.dbUsername = username;
		this.con = receivedConnection;
		this.fieldNames = fields;
		this.tableToAddTo = correctTable;
		this.inputs = correctInputs;
		this.table = table2;
		this.recipeID = CorrectRecipeID;
	}
	
	public void open() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JPanel panel = new JPanel();
		
		for(int i = 0; i < this.fieldNames.size(); i++) {
			if(!(this.fieldNames.get(i).equals("ID") || this.fieldNames.get(i).equals("UserID") 
					|| this.fieldNames.get(i).equals("Username") || this.fieldNames.get(i).equals("Author") 
					|| ((tableToAddTo.equals("Steps")|| tableToAddTo.equals("Reviews")) && this.fieldNames.get(i).equals("RecipeID")))) {
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
						cs = con.prepareCall("{? = call addDish(?,?, ?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, inputs.get(i).getText());
							questionMarkIndex++;
						}
						break;
					
					case "Has":
						cs = con.prepareCall("{? = call addHas(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
							questionMarkIndex++;
						}
						break;
						
					case "Ingredients":
						cs = con.prepareCall("{? = call addIngredients(?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						for(int i = 0; i < inputs.size(); i++) {
							cs.setString(questionMarkIndex, inputs.get(i).getText());
							questionMarkIndex++;
						}
						break;
					
					case "Recipe":
						cs = con.prepareCall("{? = call addRecipe(?,?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);

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
						cs = con.prepareCall("{? = call addReviews(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, dbUsername);
						questionMarkIndex++;
						cs.setInt(questionMarkIndex, Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString()));
						questionMarkIndex++;
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0) {
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
						cs = con.prepareCall("{? = call addSteps(?,?,?,?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						System.out.println(inputs.size());
						for(int i = 0; i < inputs.size(); i++) {
							if(i == 0) {
								cs.setInt(questionMarkIndex, Integer.parseInt(inputs.get(i).getText()));
								questionMarkIndex++;
							} 
							else {
								cs.setString(questionMarkIndex, inputs.get(i).getText());
								questionMarkIndex++;
							}
							
						}
						cs.setInt(questionMarkIndex, Integer.parseInt(table.getValueAt(table.getSelectedRow(), 0).toString()));
						questionMarkIndex++;
						
						cs.setString(questionMarkIndex,  dbUsername);
						
//						cs.setInt(questionMarkIndex, Integer.parseInt((table.getValueAt(table.getSelectedRow(), 0)).toString()));
//						
//						cs.setString(questionMarkIndex, dbUsername);
						break;
					
					case "Utensils":
						cs = con.prepareCall("{? = call addUtensils(?)}");
						cs.registerOutParameter(1, Types.INTEGER);
						cs.setString(questionMarkIndex, inputs.get(0).getText());
						break;
				}
				cs.execute();
//				System.out.println("Adding Dish complete!");
				adderFrame.dispose();
			} catch (SQLException e) {
				System.out.println("Didn't work again.");
				e.printStackTrace();
			}
			
		}
	}
}
