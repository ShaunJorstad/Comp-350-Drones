/**
 * Cyan Team
 * Author: Shaun Jorstad
 * This singleton class is is used to create a functional back button.
 * <p>
 * Member variables
 * -----------------
 * - instance       Maintains a single instance to the class. This allows the controllers to call .getInstance() on the singleton object
 * - sceneStack     Stack of Scenes the user navigates through. When back arrow is pressed the last scene is popped off and navigated too
 * - scenes         List of Scenes in the app. used for error checking
 * - SCALE          scaling factor of the UI. accomodates screens of different sizes and densities.
 */

package gui;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Arrays;


public class Navigation {
    private static Navigation instance = null;
    public static ArrayList<String> sceneStack;
    private static ArrayList<String> scenes;

    private static double SCALE = 1.6;

    private Navigation() {
        sceneStack = new ArrayList<>();
        scenes = new ArrayList<>();
        String[] SCENES = {"Splash", "Results", "FoodItems", "MealItems", "OrderDistribution", "Map", "Drone"};
        scenes.addAll(Arrays.asList(SCENES));
    }

    /**
     * pushes scene onto the scene stack
     *
     * @param scene name of scene
     */
    public static void pushScene(String scene) {
        if (instance == null) {
            instance = new Navigation();
            System.out.println("constructing navigation");
        }
        if (scenes.contains(scene)) {
            sceneStack.add(scene);
        }
    }

    /**
     * Pops the last scene off of the stack
     *
     * @return name of last scene or null
     */
    public static String popScene() {
        if (sceneStack.isEmpty()) {
            return null;
        }
        return sceneStack.remove(sceneStack.size() - 1);
    }

    /**
     * Checks if scene stack is empty
     * Will be used when loading the image of the backbutton
     *
     * @return true if empty, false otherwise
     */
    public static boolean isEmpty() {
        if (instance == null) {
            instance = new Navigation();
        }
        return sceneStack.isEmpty();
    }

    /**
     * Scales the input value based upon the scale factor
     *
     * @param initial value to be scaled
     * @return scaled value
     */
    public static int scale(int initial) {
        return (int) (initial * SCALE);
    }

    /**
     * Scales the input value based upon the scale factor
     *
     * @param initial value to be scaled
     * @return scaled value
     */
    public static double scale(double initial) {
        return initial * SCALE;
    }

    /**
     * Inflates a scene (navigating to a new screen)
     *
     * @param root      fxml document being inflated
     * @param nextScene name of the scene being inflated
     * @param stage     current stage of the application
     */
    public static void inflateScene(Parent root, String nextScene, Stage stage) {
        Scene splashScene = new Scene(root, scale(1000), scale(700));
        splashScene.getStylesheets().add("gui/CSS/" + nextScene + ".css");
        splashScene.getStylesheets().add("gui/CSS/Settings.css");
        splashScene.getStylesheets().add("gui/CSS/Navigation.css");
        stage.setScene(splashScene);
    }

    /**
     * returns instance of this singleton class. Constructs one if it hasn't been constructed yet.
     *
     * @return singleton instance
     */
    public static Navigation getInstance() {
        if (instance == null) {
            instance = new Navigation();
        }
        return instance;
    }
}