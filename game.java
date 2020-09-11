/*
Main file for the game
Creator: Joseph Myc
Last edited: 11/09/2020
*/

import java.util.HashMap;
import java.util.ArrayList;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.scene.control.*; 
import javafx.scene.layout.*; 
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.text.Text.*; 
import javafx.scene.paint.*; 
import javafx.scene.text.*; 

import javafx.animation.AnimationTimer;

import javafx.application.Application;

import javafx.geometry.Point2D;
import javafx.geometry.Pos;

import javafx.stage.Stage;

import javafx.collections.*;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;


public class game extends Application {
    //implements KeyListener, ActionListener, MouseListener, EventHandler<ActionEvent>
    
    //Hashmap that stores all the currently pressed buttons.
    private HashMap<KeyCode, Boolean> pressedKeysHM = new HashMap<KeyCode, Boolean>();

    private ArrayList<Node> platforms = new ArrayList<Node>();

    private Pane appRoot = new Pane(); //The main window root
    private Pane gameRoot = new Pane(); //game entities (scrollable)
    private Pane uiRoot = new Pane(); //not scrollable

    private Scene menuScene;
    private Scene lvlSelScene;

    private int PLATFORM_WIDTH = 60;
    private int PLAYER_WIDTH = 40;

    private Node player;
    private Point2D playerVelocity = new Point2D(0, 0);
    private Boolean canJump = true;

    private int levelWidth;
    private int levelHeight;

    private void initContent(int levelNum) {
        Rectangle background = new Rectangle (1200, 600);
        levelWidth = levelData.LEVEL1[levelNum][0].length() * PLATFORM_WIDTH;
        levelHeight = levelData.LEVEL1[levelNum].length * PLATFORM_WIDTH;

        //parse the level data
        for (int i=0; i < levelData.LEVEL1[levelNum].length; i++) {
            String currLine = levelData.LEVEL1[levelNum][i];
            
            for (int i2=0; i2 < currLine.length(); i2++){
                
                switch (currLine.charAt(i2)){
                    case '0':
                        break;
                    case '1':
                        Node platform = createEntity(i2*PLATFORM_WIDTH, i*PLATFORM_WIDTH, PLATFORM_WIDTH, PLATFORM_WIDTH, Color.RED); 
                        platforms.add(platform);
                        break;
                }
                // if (currLine.charAt(i2) == '1'){
                //     Node platform = createEntity(i2*50, i*50, 50, 50, Color.RED); 
                //     platforms.add(platform);
                //     break;
                // } else if (currLine.charAt(i2) == '0') {
                //     break;
                // }
            } 
        }

        player = createEntity(0, 500, PLAYER_WIDTH, PLAYER_WIDTH, Color.YELLOW);

        player.translateXProperty().addListener((obs, oldValue, newValue) -> {
            int offset = newValue.intValue();

            if (offset > 640 && offset < levelWidth - 640) {
                gameRoot.setLayoutX(-(offset - 640));
            }
        });

        player.translateYProperty().addListener((obs, oldValue, newValue) -> {
            int offset = newValue.intValue();

            if (offset > 225 && offset < levelHeight - 225) {
                gameRoot.setLayoutY(-(offset - 225));
            }
        });

        appRoot.getChildren().addAll(background, gameRoot, uiRoot);
    }

    private void update() {
        if (isPressed(KeyCode.W) && player.getTranslateY() >=5 ) {
            jumpPlayer();
        }
        if (isPressed(KeyCode.A) && player.getTranslateX() >= 5) {
            movePlayerX(-5);
        }
        if (isPressed(KeyCode.D) && player.getTranslateX() + PLAYER_WIDTH <= levelWidth- 5) {
            movePlayerX(5);
        }
        if (playerVelocity.getY() < 10) {
            playerVelocity = playerVelocity.add(0, 1);
        }

        movePlayerY((int)playerVelocity.getY());
    }

    private void jumpPlayer() {
        if (canJump) {
            playerVelocity = playerVelocity.add(0, -30);
            canJump = false;
        }
    }

    private boolean isPressed (KeyCode searchKey) {
        return pressedKeysHM.getOrDefault(searchKey, false);
    }

    private void movePlayerX (int xVel) {
        boolean moveRight = xVel > 0;

        for (int i=0; i<Math.abs(xVel); i++){
            for (Node currPlatform : platforms) {
                if (player.getBoundsInParent().intersects(currPlatform.getBoundsInParent())) {
                    if (moveRight) {

                        //Collision on the right side of the player
                        if (player.getTranslateX()+PLAYER_WIDTH == currPlatform.getTranslateX()) {
                            return;
                        }
                    } else {

                        //Collision of the left side of the player
                        if (player.getTranslateX() == currPlatform.getTranslateX()+PLATFORM_WIDTH) {
                            return;
                        }
                    }
                } 
            }

            //Move the player one unit in the current direction
            if (moveRight){
                player.setTranslateX(player.getTranslateX() + 1);
            } else {
                player.setTranslateX(player.getTranslateX() - 1);
            }
            
        }
    }

    private void movePlayerY (int yVel) {
        boolean moveDown = yVel > 0;

        for (int i=0; i<Math.abs(yVel); i++){
            for (Node currPlatform : platforms) {
                if (player.getBoundsInParent().intersects(currPlatform.getBoundsInParent())) {
                    if (moveDown) {

                        //Player is touching the ground
                        if (player.getTranslateY()+PLAYER_WIDTH == currPlatform.getTranslateY()) {
                            player.setTranslateY(player.getTranslateY() - 1);
                            canJump = true;
                            return;
                        }
                    } else {
                        //Player is touching the ceiling
                        if (player.getTranslateY() == currPlatform.getTranslateY()+PLATFORM_WIDTH) {
                            player.setTranslateY(player.getTranslateY() + 1);
                            return;
                        }
                    }
                } 
            }

            //Move the player one unit in the current direction
            if (moveDown){
                player.setTranslateY(player.getTranslateY() + 1);
            } else {
                player.setTranslateY(player.getTranslateY() - 1);
            }
            
            //respawn and reset camera if player falls off
            if (player.getTranslateY() > levelHeight+200){
                player.setTranslateX(0);
                player.setTranslateY(500);
                gameRoot.setLayoutX(0);
                gameRoot.setLayoutX(500);
            }
            
        }
    }

    private Node createEntity(int x, int y, int width, int height, Color color) {
        Rectangle entity = new Rectangle (width, height);
        entity.setTranslateX(x);
        entity.setTranslateY(y);
        entity.setFill(color);

        gameRoot.getChildren().add(entity);
        return entity;
    }

    public void loadLevelSelect(Stage mainStage){
        //Create Level Select Scene
        Group lvlSelRoot = new Group();
        lvlSelScene = new Scene(lvlSelRoot);

        //Create back button
        Button backBut = new Button("Back");
        backBut.setPrefSize(200, 50);
        backBut.setStyle("-fx-font-family: \"Papyrus\"; -fx-font-size: 2em;");
        //Set button action listeners
        backBut.setOnAction(actionEvent ->  {
            // Switch to main menu scene
            setMenu(mainStage);

        });

        //Create menuButton for level selection
        MenuButton lvlMB = new MenuButton("Level Select");
        lvlMB.setPrefSize(200, 50);
        MenuItem currItem;
        int lvlCount = levelData.LEVEL1.length;
        int i = 0;
        String ia = "";
        for (i=0; i<lvlCount; i++){
            ia = Integer.toString(i+1);
            currItem = new MenuItem(Integer.toString(i+1));
            //Change menubutton text to the selected item when the item is selected
            currItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    //lvlMB.setText(ia);
                    lvlMB.setText(((MenuItem)e.getSource()).getText());
                    loadLevel(mainStage, ((MenuItem)e.getSource()).getText());

                }
            });
            // currItem.setOnAction(actionEvent ->  {
            //     lvlMB.setText(((MenuItem)e.getSource()).getText());
            // });
            lvlMB.getItems().add(currItem);
            
        }

        HBox lvlVBox = new HBox(10, backBut, lvlMB);
        lvlVBox.setPrefSize(mainStage.getWidth(), mainStage.getHeight());
        lvlVBox.setAlignment(Pos.TOP_CENTER);
        lvlVBox.setStyle("-fx-padding: 100px; -fx-font-family: \"Papyrus\"; -fx-font-size: 2em;");

        //Add VBox to lvl select scene
        lvlSelRoot.getChildren().add(lvlVBox);   
        
    }

    public void setLevelSelect(Stage mainStage){
        //Set level select scene to stage
        mainStage.setScene(lvlSelScene);
    }

    public void loadMenu(Stage mainStage){

        //Create Menu Scene
        Group menuRoot = new Group();
        menuScene = new Scene(menuRoot);

        //Create menu buttons
        Button playBut = new Button("Play");
        Button optionsBut = new Button("Options");
        Button quitBut = new Button("Quit");

        //Set button size
        playBut.setPrefSize(200, 50);
        optionsBut.setPrefSize(200, 50);
        quitBut.setPrefSize(200, 50);

        //Button Text Formatting
        playBut.setStyle("-fx-font-family: \"Comic Sans MS\"; -fx-font-size: 2em;");
        optionsBut.setStyle("-fx-font-family: \"Comic Sans MS\"; -fx-font-size: 2em;");
        quitBut.setStyle("-fx-font-family: \"Comic Sans MS\"; -fx-font-size: 2em;");

        //Create Vbox container to hold the buttons
        VBox menuVBox = new VBox(10, playBut, optionsBut, quitBut);
        menuVBox.setPrefSize(mainStage.getWidth(), mainStage.getHeight());
        menuVBox.setAlignment(Pos.TOP_CENTER);
        menuVBox.setStyle("-fx-padding: 100px;");

        //Add VBox to menu scene
        menuRoot.getChildren().add(menuVBox);       

        //Set button action listeners
        playBut.setOnAction(actionEvent ->  {
            // Load level selection
            setLevelSelect(mainStage);

        });
        optionsBut.setOnAction(actionEvent ->  {
            //... do something in here.    
        });
        quitBut.setOnAction(actionEvent ->  {
            //YOU MUST DIE    
            mainStage.close();
        });

    }

    public void setMenu(Stage mainStage){
        //Set menu scene to stage
        mainStage.setScene(menuScene);
    }

    public void loadLevel(Stage mainStage, String levelNum){
        initContent(Integer.parseInt(levelNum)-1);
        System.out.println(levelNum);

        //Create scene
        Scene mainScene = new Scene(appRoot);
        mainScene.setOnKeyPressed(event -> pressedKeysHM.put(event.getCode(), true));
        mainScene.setOnKeyReleased(event -> pressedKeysHM.put(event.getCode(), false));

        //Add scene to stage
        mainStage.setScene(mainScene);
        mainStage.show();

        //timer to track the time
        AnimationTimer timer = new AnimationTimer() {
            
            @Override
            public void handle (long now){
                update();
            }
        };
        timer.start();
    }

    @Override
    public void start(Stage mainStage) throws Exception{

        

        //Create main window
        mainStage.setTitle("Game");
        mainStage.setX(0);
        mainStage.setY(0);
        mainStage.setMaximized(true);
        mainStage.show();

        //Load Main Menu
        loadMenu(mainStage);
        loadLevelSelect(mainStage);
        setMenu(mainStage);

        
        // //Create scene
        // Scene mainScene = new Scene(appRoot);
        // mainScene.setOnKeyPressed(event -> pressedKeysHM.put(event.getCode(), true));
        // mainScene.setOnKeyReleased(event -> pressedKeysHM.put(event.getCode(), false));

        // //Add scene to stage
        // mainStage.setScene(mainScene);
        // mainStage.show();

        // //timer to track the time
        // AnimationTimer timer = new AnimationTimer() {
            
        //     @Override
        //     public void handle (long now){
        //         update();
        //     }
        // };
        // timer.start();

        
    }
    
    //Call start method
    public static void main(String[] args){
    
        System.out.println("Hello World");
        launch(args); //javafx call to start the application (calls the start function)
        
    }

    
    

    // @Override
    // public void handle(ActionEvent inputActionEvent){
        
    // }

}