import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.text.*;

public class Elements {
	
	public static Label makeRobot() {
		Label robot = new Label();
        robot.setMinSize(30,30);
        robot.setStyle("-fx-background-color:linear-gradient(to right, #EDEDDE, #BDBDBD); -fx-background-radius:30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
        return robot;
	}
	
	public static Label makeCarryingRobot() {
		Label robot = new Label();
        robot.setMinSize(30,30);
        robot.setStyle("-fx-background-color:linear-gradient(to right, #00ABCC, #CCFEAA); -fx-background-radius:30; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
        return robot;
	}
	
	public static Label makeSpace() {
		Label space = new Label();
        space.setMinSize(30, 30);

        return space;
	}
	
	public static Label makeWall() {
		Label wall = new Label();
        wall.setMinSize(30, 30);
        wall.setStyle("-fx-background-color: linear-gradient(to left, #FFABAE, #D7A4A4); -fx-background-radius: 2; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
        return wall;
	}
	
	public static Label makeBin(String color) {
		Label bin = new Label();
        bin.setMinSize(25, 25);
        bin.setStyle("-fx-background-color:" + "linear-gradient(to top," + color + ",#BBBBBB); -fx-background-radius:15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
        
        return bin;

	}
	
	public static Label makeTrash() {
		Label trash = new Label();
        trash.setMinSize(25, 25);
        trash.setStyle("-fx-background-color:" + "linear-gradient(to top, #000000,#CCDDEE); -fx-background-radius:15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0)");
        
        return trash;
	}
	
	public static Label makeBackGround(int width, int height) {
		Label backGround = new Label();
    	backGround.setMinSize(width, height);
        backGround.setStyle("-fx-background-color: linear-gradient(to right, #eecda3, #ef629f)");
        
        return backGround;
	}
	
	
	public static Text makeText(String txt) {
		Text text = new Text(10,50, txt);
		text.setUnderline(true);
		return text;
	}
	
	public static GridPane makeKeyPane() {
		
		GridPane grid = new GridPane();
		grid.setMinSize(500, 500);
		
		grid.add(makeTrash(), 0, 0);
		grid.add(makeText("TRASH"), 1, 0);
		
		grid.add(makeBin("green"), 0, 1);
		grid.add(makeBin("red"), 1, 1);
		grid.add(makeBin("blue"), 2, 1);
		grid.add(makeBin("yellow"), 3, 1);
		grid.add(makeText("BIN "), 4, 1);
		
		grid.add(makeWall(), 0, 2);
		grid.add(makeText("WALL"), 1, 2);
		
		
		grid.add(makeRobot(), 0, 3);
		grid.add(makeText("ROBOT"), 1, 3);
		

		grid.add(makeCarryingRobot(), 0, 5);
		grid.add(makeText("CARRYING ROBOT"), 1, 5);
		grid.setVgap(10);
		grid.setHgap(10);
		
		return grid;
	}
	
	public static GridPane makeMaizePane(int width, int height, char[][] maize) {
		GridPane maizePane = new GridPane();
		maizePane.setMinSize(width, height);
		
		maizePane.setVgap(5);
		maizePane.setHgap(5);
		
		
        for (int row = 0; row < maize.length; row++) {
            for (int col = 0; col < maize[0].length; col++) {
            	switch(maize[row][col]) {
            	case '#': maizePane.add(makeWall(), col, row);
            	break;
            	case '.': maizePane.add(makeSpace(), col, row);
            	break;
            	case 'R': maizePane.add(makeRobot(), col, row);
            	break;
            	case 'O' : maizePane.add(makeCarryingRobot(), col, row);
            	break;
            	case 'S':  maizePane.add(makeBin("red"), col, row); //plastic bin
            	break;
            	case 'P': maizePane.add(makeBin("blue"), col, row); //paper bin
            	break;
            	case 'M': maizePane.add(makeBin("yellow"), col,row); //metal bin
            	break;
            	case 'G': maizePane.add(makeBin("white"), col, row); //glass bin
            	break;
            	case '?':
            	case 's':
            	case 'p':
            	case 'm':
            	case 'g':
            		maizePane.add(makeTrash(), col, row);
            	break;
            	};
                
            }
        }

		
		return maizePane;
	}
}
