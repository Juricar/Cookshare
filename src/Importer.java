import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.*;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Importer {

	private String file;
	private CookshareConnectionService db;
	private Connection c;
	private final String dbName = "Cookshare4";
	private final String serverName = "titan.csse.rose-hulman.edu";
	
	public static void main(String[] args) {
		new Importer();
	}
	
	public Importer() {
		this.file = "src/cookdb.txt";
		db = new CookshareConnectionService(serverName, dbName);
		db.connect("juricar", "Atsknktvegef24035526LCA!");
		c = db.getConnection();
		ArrayList<CookBookEntry> entries = parseGames(this.file);
		createDefaultUser();
		for (CookBookEntry cb : entries) {
			try 
			{
				CallableStatement cs = c.prepareCall("{? = call importData(?,?,?,?,?,?,?)}");
				cs.registerOutParameter(1, Types.INTEGER);
				cs.setString(2, cb.recipeName);
				cs.setString(3, cb.cuisine);
				cs.setString(4, cb.dish);
				cs.setString(5, cb.dishType);
				cs.setString(6, cb.utensil);
				cs.setString(7, cb.diet);
				cs.setString(8, cb.calories);
				cs.execute();
				int recipeID = cs.getInt(1);
				System.out.println(recipeID);
				for (int i = 0; i < cb.ingredients.size(); i++) {
					cs = c.prepareCall("{? = call addIngredients(?,?,?)}");
					cs.registerOutParameter(1, Types.INTEGER);
					cs.setString(2, cb.ingredients.get(i));
					cs.setString(3, cb.ingredientsTypes.get(i));
					cs.setInt(4, recipeID);
					cs.execute();
				}
				for (int j = 0; j < cb.steps.size(); j++) {
					cs = c.prepareCall("{? = call addSteps(?,?,?,?)}");
					cs.registerOutParameter(1, Types.INTEGER);
					cs.setInt(2, (j + 1));
					cs.setString(3, cb.steps.get(j));
					cs.setInt(4, recipeID);
					cs.setString(5, "Default");
					cs.execute();
				}
			} 
			catch (SQLException e1) 
			{
				e1.printStackTrace();
				return;
			}
		}
		db.closeConnection();
	}
	
	private void createDefaultUser() {
		try 
		{
			byte[] salt = db.getNewSalt();
			String hPwd = db.hashPassword(salt, "Password123");
			CallableStatement cs = c.prepareCall("{? = call Register(?,?,?)}");
			cs.registerOutParameter(1, Types.INTEGER);
			cs.setString(2, "Default");
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
			return;
		}
	}

	private ArrayList<CookBookEntry> parseGames(String file) {
		BufferedReader in = null;
		String line = "";
		ArrayList<CookBookEntry> cbs = new ArrayList<CookBookEntry>();
		CookBookEntry cb = new CookBookEntry();
		try {
			in = new BufferedReader(new FileReader(file));
			line = in.readLine();
			String[] stream = line.split("~");
			while(line != null) {
				stream = line.split("~");
				switch(stream[0]) {
				case "Recipe:": cb.recipeName = stream[1]; break;
				case "Cuisine:": cb.cuisine = stream[1]; break;
				case "Dish:": cb.dish = stream[1]; break;
				case "DishType:": cb.dishType = stream[1]; break;
				case "Ingredients:":
					ArrayList<String> ingResults = new ArrayList<String>();
					ArrayList<String> ingTypesResults = new ArrayList<String>();
					for (int i = 1; i < stream.length; i++) {
						ingResults.add(stream[i]);
						i++;
						ingTypesResults.add(stream[i]);
					}
					cb.ingredients = ingResults;
					cb.ingredientsTypes = ingTypesResults;
					break;
				case "Utensil:": cb.utensil = stream[1]; break;
				case "Steps:": 
					ArrayList<String> stepsResult = new ArrayList<String>();
					for (int i = 1; i < stream.length; i++) {
						stepsResult.add(stream[i]);
					}
					cb.steps = stepsResult;
					break;
				case "Diet:": cb.diet = stream[1]; break;
				case "Calories:": cb.calories = stream[1]; break;
				case "---": cbs.add(cb); cb = new CookBookEntry(); break;
				}
				line = in.readLine();
			}
		}catch(FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return cbs;
	}
	
	class CookBookEntry {
		String recipeName;
		String cuisine;
		String dish;
		String dishType;
		ArrayList<String> ingredients;
		ArrayList<String> ingredientsTypes;
		String utensil;
		ArrayList<String> steps;
		String diet;
		String calories;
		
		CookBookEntry(){
		}
	}	
}
