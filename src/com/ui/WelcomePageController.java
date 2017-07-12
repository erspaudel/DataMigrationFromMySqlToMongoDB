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
import com.utils.MongoUtils;
import com.utils.MySqlUtils;
import com.utils.StringUtils;
import com.utils.TestUtils;

import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

/**
 * 
 * @author Sushil Paudel
 *
 */

public class WelcomePageController extends Application {

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
	public Label label_Database;

	@FXML
	public Label label_ConfigRadioButtons;

	@FXML
	public Label label_EmbedDocumentLimit;

	@FXML
	public TextField textField_EmbedLimit;

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
		label_EmbedDocumentLimit.setVisible(true);

		checkBox_BackUp.setVisible(disable);
		checkBox_DefaultMigration.setVisible(disable);
		checkBox_MigrateAll.setVisible(disable);
		comboBox_Database.setVisible(disable);
		textField_EmbedLimit.setVisible(true);
	}

	public void showRadioButtons(boolean show) {
		radio_Continue.setVisible(show);
		radio_ReConfigure.setVisible(show);

		label_ConfigRadioButtons.setVisible(show);
	}

	public void showEmbedDocumentLimit(boolean show) {

		textField_EmbedLimit.setVisible(show);

		label_EmbedDocumentLimit.setVisible(show);
	}

	public void showMainView() throws Exception {
		FXMLLoader loader = new FXMLLoader();
		loader.setLocation(WelcomePageController.class.getResource("WelcomePage.fxml"));
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
		if (StringUtils.isNotEmpty(TestUtils.MIGRATOR_PATH)) {
			showConfigurationOptions(true);
			showEmbedDocumentLimit(true);
		} else {
			showConfigurationOptions(false);
			showEmbedDocumentLimit(false);
		}
		showRadioButtons(false);
	}

	public void showAlert(String message) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setHeaderText(message);
		alert.showAndWait();
	}

	@FXML
	public void nextPage(ActionEvent event) throws Exception {

		if (StringUtils.isNotEmpty(TestUtils.MIGRATOR_PATH)) {
			choosedFile = new File(TestUtils.MIGRATOR_PATH);
		}

		if (choosedFile == null) {

			showAlert("Please select directory!");
			return;
		}

		if (radio_Continue.isSelected()) {
			JavaFxUtils.showStage(getClass(), event, "ProgressPage.fxml");
			processExistingConfiguration();
		}

		if (comboBox_Database.getSelectionModel().getSelectedItem() != null) {

			if (StringUtils.isEmpty(textField_EmbedLimit.getText())) {
				showAlert("Please enter embed document limit!");
				return;
			}

			if (!StringUtils.isNumeric(textField_EmbedLimit.getText(), false)) {
				showAlert("Please enter valid number from 1 to 100 for embed document limit");
				return;
			}

			int limit = Integer.parseInt(textField_EmbedLimit.getText());

			if (limit < 0 || limit > 100) {
				showAlert("Please enter valid number from 1 to 100 for embed document limit");
				return;
			}

			deleteFilesInsideChoosedDirectory();

			writeSettings();
			FileUtils.init(choosedFile.getPath());
			ConfigUtils.init();

			JavaFxUtils.showStage(getClass(), event, "TablePage.fxml");
		} else {
			showAlert("Please select database!");
		}
	}

	public void processExistingConfiguration() {

		Task<Void> task2 = new Task<Void>() {
			@Override
			public Void call() {

				try {
					TablePageController controller = new TablePageController();
					try {
						controller.processConfiguration();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}
		};

		Thread th2 = new Thread(task2);
		th2.setDaemon(true);
		th2.start();

	}

	@FXML
	public void readDocumentation(ActionEvent event) {

		// DirectoryChooser directoryChooser = new DirectoryChooser();
		// File file = directoryChooser.showDialog(primaryStage);

		// File[] directory = file.listFiles();

	}

	@FXML
	public void openDirectoryChooser(ActionEvent event) {

		DirectoryChooser directoryChooser = new DirectoryChooser();
		choosedFile = directoryChooser.showDialog(primaryStage);

		if (choosedFile == null) {
			return;
		}

		deleteFilesInsideChoosedDirectory();

		showRadioButtons(false);
		showConfigurationOptions(true);
		showEmbedDocumentLimit(true);

	}

	public void deleteFilesInsideChoosedDirectory() {
		File[] directory = choosedFile.listFiles();

		button_FileChooser.setText(choosedFile.getPath());

		FileUtils.init(choosedFile.getPath());

		for (File file2 : directory) {

			if (isConfigFile(file2)) {
				showRadioButtons(true);
				showConfigurationOptions(false);
				showEmbedDocumentLimit(false);
				return;
			} else {
				deleteFile(file2);
			}
		}
	}

	public void deleteFile(File file) {

		File[] innerFiles = file.listFiles();

		if (innerFiles != null) {

			for (File file2 : innerFiles) {

				deleteFile(file2);
			}
		}
		file.delete();
	}

	public boolean isConfigFile(File file) {

		if (StringUtils.equalsIgnoreCase(file.getName(), FileUtils.FILE_CONFIG)) {
			try {

				Document document = null;

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

		System.out.println("Writing Configurations ...");

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
		document.append(ConfigUtils.UNIX_TIME, MongoUtils.getUnixTime());
		document.append(ConfigUtils.EMBED_DOCUMENT_LIMIT, textField_EmbedLimit.getText());

		return document.toJson();
	}

	public static void main(String[] args) {
		launch(args);
	}

}
