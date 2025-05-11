cd ../src/
JAVAFX=../javafx/lib
javac --module-path $JAVAFX --add-modules javafx.controls,javafx.fxml WasteSimulation/*.java
