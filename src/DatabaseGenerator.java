import java.util.ArrayList;
import java.util.Random;
import java.io.*;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.*;

import javax.swing.JFrame;

public class DatabaseGenerator {
	
	private Random rand;
	private int dishNum;
	private ArrayList<Integer> ingredientNums;
	
	public static void main(String[] args) {
		new DatabaseGenerator();
	}
	
	public DatabaseGenerator() {
		this.rand = new Random();
		GenerateDBText();
	}
	
	public void GenerateDBText() {
		try {
			FileWriter wr = new FileWriter("C:\\Users\\juric\\cookdb.txt");
			for (int i = 0; i < 50; i++) {
				String dish = DishGen();
				wr.write("Recipe:~" + RecipeNameGen() + " " + dish + "\n");
				wr.write("Cuisine:~" + CuisineGen() + "\n");
				wr.write("Dish:~" + dish + "\n");
				wr.write("DishType:~" + DishTypeGen() + "\n");
				
				ArrayList<String> ing = IngredientGen();
				ArrayList<String> ingTypes = IngredientTypeGen();
				String ingResults = "";
				for (int j = 0; j < ing.size(); j++) {
					ingResults += "~" + ing.get(j) + "~" + ingTypes.get(j);
				}
				ingResults += "\n";
				
				wr.write("Ingredients:" + ingResults);
				wr.write("Utensil:~" + UtensilGen() + "\n");
				
				ArrayList<String> steps = StepsGen();
				String stepResults = "";
				for (int j = 0; j < steps.size(); j++) {
					stepResults += "~" + steps.get(j);
				}
				stepResults += "\n";
				
				wr.write("Steps:" + stepResults);
				wr.write("Diet:~" + DietGen() + "\n");
				wr.write("Calories:~" + String.valueOf(this.rand.nextInt(951) + 50) + "\n");
				wr.write("---\n");
			}
			wr.write("***");
			wr.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String CuisineGen() {
		int num = this.rand.nextInt(6);
		String val = "";
		switch(num) {
		case 0: val = "American"; break;
		case 1: val = "Indian"; break;
		case 2: val = "Italian"; break;
		case 3: val = "Mexican"; break;
		case 4: val = "European"; break;
		case 5: val = "Other"; break;
		}
		return val;
	}
	
	public String DishGen() {
		int num = this.rand.nextInt(13);
		this.dishNum = num;
		String val = "";
		switch(num) {
		case 0: val = "Cheese Pizza"; break;
		case 1: val = "Blueberry Muffin"; break;
		case 2: val = "Grilled Chicken"; break;
		case 3: val = "Tacos"; break;
		case 4: val = "Butter Chicken"; break;
		case 5: val = "Ice Cream"; break;
		case 6: val = "Chocolate Cake"; break;
		case 7: val = "Banana Bread"; break;
		case 8: val = "Omelette"; break;
		case 9: val = "Fried Chicken"; break;
		case 10: val = "Pork Chops"; break;
		case 11: val = "Pepperoni Pizza"; break;
		case 12: val = "Donuts"; break;
		}
		return val;
	}
	
	public String DishTypeGen() {
		String val = "";
		switch(this.dishNum) {
		case 0: val = "Pizza"; break;
		case 1: val = "Muffin"; break;
		case 2: val = "Chicken"; break;
		case 3: val = "Unknown"; break;
		case 4: val = "Chicken"; break;
		case 5: val = "Dessert"; break;
		case 6: val = "Cake"; break;
		case 7: val = "Dessert"; break;
		case 8: val = "Breakfast"; break;
		case 9: val = "Chicken"; break;
		case 10: val = "Pork"; break;
		case 11: val = "Pizza"; break;
		case 12: val = "Dessert"; break;
		}
		return val;
	}
	
	public ArrayList<String> IngredientGen() {
		int loop = this.rand.nextInt(6) + 1;
		int num = 0;
		ArrayList<String> vals = new ArrayList<String>();
		this.ingredientNums = new ArrayList<Integer>();
		for (int i = 0; i < loop; i++) {
			num = this.rand.nextInt(16);
			if (!this.ingredientNums.contains(num)) {
				switch(num) {
				case 0: vals.add("Carrot"); break;
				case 1: vals.add("Spoon"); break;
				case 2: vals.add("Apple"); break;
				case 3: vals.add("Broccoli"); break;
				case 4: vals.add("Green Beans"); break;
				case 5: vals.add("Butter"); break;
				case 6: vals.add("Paprika"); break;
				case 7: vals.add("Onion Powder"); break;
				case 8: vals.add("Salt"); break;
				case 9: vals.add("Black Pepper"); break;
				case 10: vals.add("Lemon"); break;
				case 11: vals.add("Chicken Stock"); break;
				case 12: vals.add("Sriracha"); break;
				case 13: vals.add("Sour Cream"); break;
				case 14: vals.add("Salmon"); break;
				case 15: vals.add("Olive Oil"); break;
				}
				this.ingredientNums.add(num);
			}			
		}
		return vals;
	}
	
	public ArrayList<String> IngredientTypeGen() {
		int num = 0;
		ArrayList<String> vals = new ArrayList<String>();
		for (int i = 0; i < this.ingredientNums.size(); i++) {
			num = this.ingredientNums.get(i);
			switch(num) {
			case 0: vals.add("Vegetable"); break;
			case 1: vals.add("Utensil"); break;
			case 2: vals.add("Fruit"); break;
			case 3: vals.add("Vegetable"); break;
			case 4: vals.add("Vegetable"); break;
			case 5: vals.add("Dairy"); break;
			case 6: vals.add("Spice"); break;
			case 7: vals.add("Seasoning"); break;
			case 8: vals.add("Seasoning"); break;
			case 9: vals.add("Seasoning"); break;
			case 10: vals.add("Fruit"); break;
			case 11: vals.add("Stock/Broth"); break;
			case 12: vals.add("Hot Sauce"); break;
			case 13: vals.add("Dairy"); break;
			case 14: vals.add("Fish"); break;
			case 15: vals.add("Oil"); break;
			}		
		}
		return vals;
	}
	
	public String UtensilGen() {
		int num = this.rand.nextInt(4);
		String val = "";
		switch(num) {
		case 0: val = "Frying Pan"; break;
		case 1: val = "Pot"; break;
		case 2: val = "Glass Dish"; break;
		case 3: val = "Grill"; break;
		}
		return val;
	}
	
	public ArrayList<String> StepsGen() {
		int loop = this.rand.nextInt(4) + 1;
		int num = 0;
		ArrayList<String> vals = new ArrayList<String>();
		ArrayList<Integer> added = new ArrayList<Integer>();
		for (int i = 0; i < loop; i++) {
			num = this.rand.nextInt(16);
			if (!added.contains(num)) {
				switch(num) {
				case 0: vals.add("Preheat oven to " + String.valueOf(this.rand.nextInt(250) + 150) + "."); break;
				case 1: vals.add("Place in oven."); break;
				case 2: vals.add("Let cook for " + String.valueOf(this.rand.nextInt(25) + 5) + " minutes."); break;
				case 3: vals.add("Place in freezer."); break;
				case 4: vals.add("Remove from freezer."); break;
				case 5: vals.add("Beat eggs until all yolks are broken."); break;
				case 6: vals.add("Place in blender."); break;
				case 7: vals.add("Garnish and serve."); break;
				case 8: vals.add("Lightly season with salt."); break;
				case 9: vals.add("Cook in pan until golden-brown."); break;
				case 10: vals.add("Contemplate life choices."); break;
				case 11: vals.add("Add remaining cheese."); break;
				case 12: vals.add("Add pepper to taste."); break;
				case 13: vals.add("Soak in oil."); break;
				case 14: vals.add("Flambe."); break;
				case 15: vals.add("Defrost."); break;
				}
				added.add(num);
			}			
		}
		return vals;
	}
	
	public String RecipeNameGen() {
		int num = this.rand.nextInt(21);
		String val = "";
		switch(num) {
		case 0: val = "Cheesy"; break;
		case 1: val = "Delicious"; break;
		case 2: val = "Cold"; break;
		case 3: val = "Bombastic"; break;
		case 4: val = "Garden-Fresh"; break;
		case 5: val = "Raw"; break;
		case 6: val = "Burnt"; break;
		case 7: val = "Satisfying"; break;
		case 8: val = "Savory"; break;
		case 9: val = "Grilled"; break;
		case 10: val = "Tender"; break;
		case 11: val = "Medium-well"; break;
		case 12: val = "Salivating"; break;
		case 13: val = "Boring"; break;
		case 14: val = "Salty"; break;
		case 15: val = "Spicy"; break;
		case 16: val = "Over-easy"; break;
		case 17: val = "Crusty"; break;
		case 18: val = "Cool-ranch-flavored"; break;
		case 19: val = "Juicy"; break;
		case 20: val = "Stuffed"; break;
		}
		return val;
	}
	
	public String DietGen() {
		int num = this.rand.nextInt(4);
		String val = "";
		switch(num) {
		case 0: val = "Vegan"; break;
		case 1: val = "Vegetarian"; break;
		case 2: val = "Liquid"; break;
		case 3: val = "Non-Vegetarian"; break;

		}
		return val;
	}
	
}