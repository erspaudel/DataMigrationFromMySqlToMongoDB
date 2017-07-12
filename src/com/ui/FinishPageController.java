package com.ui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import com.utils.JavaFxUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

public class FinishPageController implements Initializable {

	@FXML
	public void previousPage(ActionEvent event) throws IOException {
		System.out.println("FinishPageController.previousPage()");
		JavaFxUtils.showStage(getClass(), event, "WelcomePage.fxml");
	}

	@FXML
	public void exit(ActionEvent event) throws IOException {
		System.out.println("FinishPageController.exit()");
		System.exit(0);
	}

	public static void main(String[] args) {
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}

}
