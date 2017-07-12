package com.utils;

import java.io.IOException;

import com.ui.WelcomePageController;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class JavaFxUtils {

	public static void showStage(Class<?> className, ActionEvent event, String location) throws IOException {
		Scene nextPageScene = getScene(className, location);
		Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
		// stage.setMaximized(true);
		System.out.println("stage: "+stage);
		stage.setScene(nextPageScene);
		stage.show();
	}

	public static Scene getScene(Class<?> className, String location) throws IOException {
		Parent nextPage = FXMLLoader.load(className.getResource(location));
		return new Scene(nextPage);
	}

	public static void showNextPage(String pageName) throws IOException {

		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(WelcomePageController.class.getResource(pageName));
		BorderPane borderPane = (BorderPane) loader.load();
		Scene scene = new Scene(borderPane);

		Stage stage = WelcomePageController.staticStage;
		stage.setScene(scene);
		stage.show();
	}

}
