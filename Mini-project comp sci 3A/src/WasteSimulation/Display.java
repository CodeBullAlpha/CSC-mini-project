import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;


public class Display {
	public static final int MIN_WIDTH = 1500;
	public static final int MIN_HEIGHT = 1200;

	private char[][] maize;

	private Scene currentScene;
	private StackPane container;
	private GridPane keyPane;
	private GridPane maizePane;
	private Label background;
	
	public Display(char[][] maize) {
		this.maize = maize;
		setupElements();
	}
	

	public void updateMaize(char[][] newMaize) {
		maize = newMaize;
		setupElements();
	}
	
	
	public void setupElements() {
		container = new StackPane();
		container.setMinSize(MIN_WIDTH, MIN_HEIGHT);
	
		background = Elements.makeBackGround(2048, 2048);
		background.setAlignment(Pos.CENTER);
		
		GridPane keyPane = Elements.makeKeyPane();
		keyPane.setAlignment(Pos.CENTER_RIGHT);
	
		maizePane = new GridPane();
		maizePane.setAlignment(Pos.TOP_LEFT);

		maizePane = Elements.makeMaizePane(1024, 1024, maize);
		maizePane.setAlignment(Pos.BOTTOM_LEFT);
	
    
		container.getChildren().add(background);
		container.getChildren().add(maizePane);
		container.getChildren().add(keyPane);
		
		currentScene = new Scene(container, MIN_WIDTH, MIN_HEIGHT);
	}
	

	public Scene getCurrentScene() {
		return currentScene;
	}
	
}
