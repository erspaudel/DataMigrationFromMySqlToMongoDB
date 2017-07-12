package com.ui;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

public class ProgressPageController implements Initializable {

	@FXML
	public Label label;

	public static boolean showLoopMessage = true;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		testProgress();
	}

	public void testProgress() {

		Task<Void> task = new Task<Void>() {
			@Override
			public Void call() {

				while (showLoopMessage) {

					final String labelValue = "Please wait ";

					for (int i = 0; i < 4; i++) {

						final int j = i;

						Platform.runLater(new Runnable() {

							@Override
							public void run() {
								label.setText(labelValue + getDots(j));

							}
						});

						try {
							Thread.sleep(200);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}
				}
				return null;
			}
		};

		Thread th = new Thread(task);
		th.setDaemon(true);
		th.start();

	}

	public String getDots(int count) {

		if (count == 1) {
			return ".";
		} else if (count == 2) {
			return "..";
		} else if (count == 3) {
			return "...";
		} else {
			return "";
		}

	}

}
