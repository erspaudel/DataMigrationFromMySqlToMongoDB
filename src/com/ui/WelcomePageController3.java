package com.ui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.bson.Document;

import com.file.handling.ConfigWriter;
import com.utils.ConfigUtils;
import com.utils.FileUtils;
import com.utils.JavaFxUtils;
import com.utils.MySqlUtils;
import com.utils.StringUtils;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

public class WelcomePageController3 extends Application {

	public static Stage staticStage;
	public Stage primaryStage;
	public BorderPane borderPane;

	@FXML
	public ComboBox<String> comboBox_Database;

	@FXML
	public CheckBox checkBox_MigrateAll;

	@FXML
	public CheckBox checkBox_DefaultMigration;

	@FXML
	public CheckBox checkBox_BackUp;

	@FXML
	public Label label_MigrateAll;

	@FXML
	public Label label_DefaultMigration;

	@FXML
	public Label label_BackUp;

	@FXML
	public Label label_Message;

	@FXML
	public Label label_Database;

	@FXML
	public Label label_ConfigRadioButtons;

	@FXML
	public RadioButton radio_Continue;

	@FXML
	public RadioButton radio_ReConfigure;

	@FXML
	public Button button_FileChooser;

	public File choosedFile;

	@Override
	public void start(Stage primaryStage) throws Exception {

		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("MySql To MongoDB");
		staticStage = primaryStage;
		// this.primaryStage.setMaximized(true);

		showMainView();
	}

	public void showConfigurationOptions(boolean disable) {

		label_BackUp.setVisible(disable);
		label_DefaultMigration.setVisible(disable);
		label_MigrateAll.setVisible(disable);
		label_Database.setVisible(disable);

		checkBox_BackUp.setVisible(disable);
		checkBox_DefaultMigration.setVisible(disable);
		checkBox_MigrateAll.setVisible(disable);
		comboBox_Database.setVisible(disable);
	}

	public void showRadioButtons(boolean show) {
		radio_Continue.setVisible(show);
		radio_ReConfigure.setVisible(show);

		label_ConfigRadioButtons.setVisible(show);
	}

	public void showMainView() throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(WelcomePageController3.class.getResource("WelcomePage.fxml"));
		borderPane = (BorderPane) loader.load();
		Scene scene = new Scene(borderPane);
		this.primaryStage.setScene(scene);
		this.primaryStage.show();
	}

	@FXML
	public void initialize() {
		try {
			String[] dBNames = MySqlUtils.getAllDatabase();
			comboBox_Database.getItems().addAll(dBNames);
		} catch (Exception e) {
			e.printStackTrace();
		}

		handleRadioButtons();
		showRadioButtons(false);
		showConfigurationOptions(false);
	}

	@FXML
	public void nextPage(ActionEvent event) throws Exception {

		if (choosedFile == null) {
			label_Message.setText("Please select directory!");
			label_Message.setVisible(true);
			return;
		}

		if (radio_Continue.isSelected()) {

			JavaFxUtils.showStage(getClass(), event, "ProgressPage.fxml");

			Thread.sleep(1000);
			
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					try {
						TablePageController controller = new TablePageController();
						try {
							// controller.processConfiguration();
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			});

		}

		if (comboBox_Database.getSelectionModel().getSelectedItem() != null) {
			writeSettings();
			FileUtils.init(choosedFile.getPath());
			ConfigUtils.init();
			JavaFxUtils.showStage(getClass(), event, "TablePage.fxml");
		} else {
			label_Message.setText("Please select database!");
			label_Message.setVisible(true);
		}
	}

	@FXML
	public void readDocumentation(ActionEvent event) {

		DirectoryChooser directoryChooser = new DirectoryChooser();
		File file = directoryChooser.showDialog(primaryStage);

		System.out.println("file: " + file.getAbsolutePath());

		System.out.println("is dir: " + file.isDirectory());

		File[] directory = file.listFiles();

		for (File file2 : directory) {
			System.out.println(file2.getName());
		}

	}

	@FXML
	public void openDirectoryChooser(ActionEvent event) {

		DirectoryChooser directoryChooser = new DirectoryChooser();
		choosedFile = directoryChooser.showDialog(primaryStage);

		if (choosedFile == null) {
			return;
		}

		File[] directory = choosedFile.listFiles();

		button_FileChooser.setText(choosedFile.getPath());

		FileUtils.init(choosedFile.getPath());

		for (File file2 : directory) {

			System.out.println("WelcomePageController.openDirectoryChooser(): " + file2.getName());

			System.out.println(isConfigFile(file2));

			if (isConfigFile(file2)) {
				showRadioButtons(true);
				showConfigurationOptions(false);
				return;
			}
		}

		showRadioButtons(false);
		showConfigurationOptions(true);

	}

	public boolean isConfigFile(File file) {

		if (StringUtils.equalsIgnoreCase(file.getName(), FileUtils.FILE_CONFIG)) {
			try {

				Document document = null;

				System.out.println("file: " + FileUtils.FILE_PATH_CONFIG);

				try {

					FileReader fileReader = new FileReader(FileUtils.FILE_PATH_CONFIG);
					BufferedReader bufferedReader = new BufferedReader(fileReader);
					String val = null;

					while ((val = bufferedReader.readLine()) != null) {
						document = Document.parse(val);
					}

					bufferedReader.close();
				} catch (Exception ex) {

				}

				System.out.println(document);

				boolean isRefactored = document.getBoolean(ConfigUtils.IS_REFACTORED);

				return isRefactored;
			} catch (Exception ex) {
				return false;
			}
		}
		return false;
	}

	public void handleRadioButtons() {

		ToggleGroup group = new ToggleGroup();
		radio_Continue.setToggleGroup(group);
		radio_ReConfigure.setToggleGroup(group);

		radio_Continue.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				showConfigurationOptions(false);
			}
		});

		radio_ReConfigure.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				showConfigurationOptions(true);
			}
		});
	}

	public void writeSettings() throws IOException {

		String path = choosedFile.getPath() + File.separator + FileUtils.FILE_CONFIG;

		ConfigWriter writer = new ConfigWriter(path);
		writer.clearFileContent(path);
		String data = getData();
		writer.write(data, path);
		writer.close();
	}

	public String getData() {

		Document document = new Document();
		document.append(ConfigUtils.DB_NAME, comboBox_Database.getSelectionModel().getSelectedItem());
		document.append(ConfigUtils.DB_MIGRATE_ALL_TABLES, checkBox_MigrateAll.isSelected());
		document.append(ConfigUtils.DB_DEFAULT_MIGRATION, checkBox_DefaultMigration.isSelected());
		document.append(ConfigUtils.DB_BACKUP_FILES, checkBox_BackUp.isSelected());
		document.append(ConfigUtils.MIGRATION_DIRECTORY, choosedFile.getAbsolutePath());

		return document.toJson();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
