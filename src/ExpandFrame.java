import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class ExpandFrame {
	
	String recipeName;
	String reviewCreator;
	String currentTable;
	int stars;
	
	public ExpandFrame(String tableUsing, String nameOfRecipe, String creatorOfReview, int rating) {
		this.currentTable = tableUsing;
		this.recipeName = nameOfRecipe;
		this.reviewCreator = creatorOfReview;
		this.stars = rating;
	}
	
	public void open() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
		
		if(this.currentTable.equals("Reviews")) {
			frame.setTitle("Expanded Review");
			lblNewLabel.setText(this.recipeName + " Review by " + this.reviewCreator);
			lblNewLabel_1.setText("Rating: " + this.stars);
		}
		frame.setVisible(true);
	}
	
}
