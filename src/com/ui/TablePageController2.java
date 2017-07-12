package com.ui;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.bson.Document;

import com.constants.TablesRelations;
import com.file.handling.ConfigWriter;
import com.utils.ConfigUtils;
import com.utils.JavaFxUtils;
import com.utils.MigratorDocument;
import com.utils.MySqlUtils;
import com.utils.StringUtils;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Accordion;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ScrollPane.ScrollBarPolicy;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

public class TablePageController2 implements Initializable {

	@FXML
	public Accordion accordion;

	@FXML
	public ScrollPane scrollPane;

	@FXML
	public BorderPane borderPane;

	public MigratorDocument migratorDocument;

	private static final String ID_MAIN_VBOX = "MAIN_VBOX";
	private static final String ID_MIGRATE_TABLE_OPTION_VBOX = "MIGRATE_TABLE";
	private static final String ID_FOREIGN_RELATION_VBOX = "FOREIGN_RELATION";
	private static final String ID_FOREIGN_RELATION_VBOX_HBOX = "FOREIGN_RELATION_HBOX";
	private static final String ID_CUSTOMIZE_DATA_MODELING_OPTION_HBOX = "CUSTOMIZE_DATA_MODELING";
	private static final String ID_THUMB_RULE_OPTION_VBOX = "THUMB_RULE_OPTION";
	private static final String ID_FOREIGN_COLUMNS_VBOX = "FOREIGN_COLUMNS";

	private static final int FIRST_INDEX = 0;
	private static final int SECOND_INDEX = 1;

	private static final int INDEX_RELATION = 0;
	private static final int INDEX_CUSTOMIZE_DATA_MODELING_OPTION = 1;
	private static final int INDEX_THUMB_RULE_OPTION = 2;
	private static final int INDEX_FOREIGN_COLUMNS = 3;

	private static final int INDEX_RELATION_LABEL = 0;
	private static final int INDEX_RELATION_TYPE_COMBOBOX = 1;

	private static final String TABLE_NAME = "table_name";
	private static final String RELATIONS = "relations";

	public Map<String, Pane> tempMap = new HashMap<String, Pane>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		migratorDocument = ConfigWriter.getDocument();
		if (migratorDocument.getDbName() != null) {

			loadTablesNames(migratorDocument.getDbName());
		}
	}

	@FXML
	public void previousPage(ActionEvent event) throws IOException {

		JavaFxUtils.showStage(getClass(), event, "WelcomePage.fxml");
	}

	@FXML
	public void startMigration(ActionEvent event) throws IOException {

		List<TitledPane> titledPanes = accordion.getPanes();

		Document document = ConfigUtils.getDocument();

		for (TitledPane tp : titledPanes) {

			VBox content = (VBox) tp.getContent();

			String tableName = tp.getText();

			Document document_table = new Document();
			document_table.append(TABLE_NAME, tableName);

			List childrens = content.getChildren();

			if (childrens.size() == 0) {
				continue;
			}

			if ((childrens.get(FIRST_INDEX) instanceof HBox) && (!ConfigUtils.shouldMigrateAllTables())) {

				/*
				 * Migrate table option
				 */

				HBox migrateHBox = (HBox) childrens.get(FIRST_INDEX);

				if (migrateHBox.getId().endsWith(ID_MIGRATE_TABLE_OPTION_VBOX)) {

				}
			}

			/*
			 * Foreign Relationship options
			 */

			VBox foreignVBox = null;

			if (childrens.get(FIRST_INDEX) instanceof VBox) {

				foreignVBox = (VBox) childrens.get(FIRST_INDEX);
			} else if (childrens.get(SECOND_INDEX) instanceof VBox) {

				foreignVBox = (VBox) childrens.get(SECOND_INDEX);
			}

			System.out.println("ID: "+foreignVBox.getId());
			
			List foreignVBoxChildrens = foreignVBox.getChildren();

			System.out.println("Table Nmae: " + tableName + ", child size: " + foreignVBoxChildrens.size()+", loop counter: "+getLoopLimit(foreignVBoxChildrens));

			// for (Object object : foreignVBoxChildrens) {

			for (int i = 0; i < foreignVBoxChildrens.size(); i++) {

				HBox hBoxChild1 = (HBox) foreignVBoxChildrens.get(i);

				// if (hBoxChild1.getId().endsWith(ID_FOREIGN_RELATION_VBOX)) {
				//
				// }

				List innerChildrens = ((HBox) hBoxChild1).getChildren();

				String foreignTableName = ((Label) innerChildrens.get(INDEX_RELATION_LABEL)).getText();

				System.out.println("tablename: " + foreignTableName);

				/*
				 * Relation type combo box
				 */

				System.out.println("child: " + innerChildrens.get(INDEX_RELATION_TYPE_COMBOBOX));

				ComboBox<TablesRelations> tablelRelationComboBox = (ComboBox<TablesRelations>) innerChildrens
						.get(INDEX_RELATION_TYPE_COMBOBOX);
				TablesRelations tr = (TablesRelations) tablelRelationComboBox.getSelectionModel().getSelectedItem();

				Document document_table_fk = new Document();

				if (tr != null) {
					/*
					 * Customize the relation type
					 */

					String relation = tr.getValue();

					document_table_fk.append("fk", foreignTableName);

					// return document.toJson();

				} else {

					/*
					 * Automate the relation type
					 */

				}

				document_table.append(RELATIONS, document_table_fk);
			}

			document.append(RELATIONS, document_table);

			/*
			 * else if (object2 instanceof RadioButton) {
			 * 
			 * RadioButton rb = (RadioButton) object2;
			 * 
			 * if (StringUtils.equalsIgnoreCase(rb.getText(), "Yes") &&
			 * rb.isSelected()) {
			 * 
			 * 
			 * Customize data modeling radio button
			 * 
			 * 
			 * } else {
			 * 
			 * 
			 * Automate the data modeling
			 * 
			 * 
			 * } }
			 */
		}

		ConfigWriter writer = new ConfigWriter();
		writer.write(document.toJson());

	}

	public void loadTablesNames(String dbName) {

		accordion.getPanes().clear();

		try {
			List<String> tableNames = MySqlUtils.getAllTableNames(dbName, true);

			for (String string : tableNames) {

				TitledPane sizeTpane = getTitledPane(dbName, string);

				accordion.getPanes().add(sizeTpane);
			}
			scrollPane.setVbarPolicy(ScrollBarPolicy.ALWAYS);
			scrollPane.setContent(accordion);

		} catch (Exception ex) {

		}
	}

	public TitledPane getTitledPane(String dbName, String tableName) throws SQLException {

		VBox vBox = new VBox(10);
		vBox.setId(tableName + "-" + ID_MAIN_VBOX);
		TitledPane titledPane = new TitledPane(tableName, vBox);

		vBox.getChildren().addAll(getHeader(tableName));

		if (migratorDocument.isDefaultMigration()) {
			return titledPane;
		}

		addTableRelations(dbName, tableName, vBox);

		return titledPane;
	}

	public void addTableRelations(String dbName, String tableName, VBox vBox) throws SQLException {

		Map<String, String> foreignKeys = MySqlUtils.getForeignKeysMap(tableName);

		if (foreignKeys.size() > 0) {
			Separator separator = new Separator();
			vBox.getChildren().add(separator);
		}

		for (Map.Entry<String, String> fk : foreignKeys.entrySet()) {

			VBox foreignKeysVBox = new VBox();
			foreignKeysVBox.setId(getId(tableName, fk.getValue(), ID_FOREIGN_RELATION_VBOX));

			Label label = new Label(fk.getValue().toUpperCase());
			ComboBox<TablesRelations> combo = getRelationshipComboBox();

			/*
			 * Relationship and foreign keys
			 */
			HBox hBox = new HBox(10);
			hBox.setId(getId(tableName, fk.getValue(), ID_FOREIGN_RELATION_VBOX_HBOX));

			hBox.getChildren().add(INDEX_RELATION_LABEL, label);
			hBox.getChildren().add(INDEX_RELATION_TYPE_COMBOBOX, combo);

			foreignKeysVBox.getChildren().add(INDEX_RELATION, hBox);

			combo.setOnAction((e) -> {

				TablesRelations tr = combo.getSelectionModel().getSelectedItem();
				addDenormalizationOptions(dbName, tableName, fk.getValue(), tr, foreignKeysVBox);

			});

			// VBox.setMargin(pane, new Insets(15));
			vBox.getChildren().add(foreignKeysVBox);

		}
	}

	public void addDenormalizationOptions(String dbName, String tableName, String foreignTableName, TablesRelations tr,
			VBox foreignKeysVBox) {

		String id = getId(tableName, foreignTableName, ID_CUSTOMIZE_DATA_MODELING_OPTION_HBOX);

		System.out.println("id: " + id);

		removeAllTemps(foreignKeysVBox, tableName, foreignTableName);

		HBox tempDenormalizationOptionHBox = new HBox();
		tempDenormalizationOptionHBox.setId(id);

		Label labelManuallyDemormalize = new Label("Customize Data Modelling?");

		ToggleGroup group = new ToggleGroup();

		RadioButton radioButton1 = new RadioButton("Yes");
		radioButton1.setToggleGroup(group);

		RadioButton radioButton2 = new RadioButton("No");
		radioButton2.setToggleGroup(group);

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

				if (group.getSelectedToggle() != null) {

					RadioButton rb = (RadioButton) new_toggle.getToggleGroup().getSelectedToggle();

					if (getBooleanValue(rb.getText())) {
						try {
							addManuallyDenormalizeOptions(dbName, tableName, foreignTableName, tr, foreignKeysVBox);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					} else {

						String removeId = getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX);
						removeTemps(foreignKeysVBox, removeId);

						removeId = getId(tableName, foreignTableName, ID_FOREIGN_COLUMNS_VBOX);
						removeTemps(foreignKeysVBox, removeId);
					}

				}

			}
		});

		tempDenormalizationOptionHBox.getChildren().addAll(labelManuallyDemormalize, radioButton1, radioButton2);

		foreignKeysVBox.getChildren().add(INDEX_CUSTOMIZE_DATA_MODELING_OPTION, tempDenormalizationOptionHBox);

	}

	public void addManuallyDenormalizeOptions(String dbName, String tableName, String foreignTableName,
			TablesRelations tr, VBox foreignKeysVBox) throws SQLException {

		// removeEmbedTemps(foreignKeysVBox);
		// removeForeignColumnsTemps(foreignKeysVBox);

		VBox tempEmbedOptionVBox = null;

		if (tr.getId() == TablesRelations.ONE_TO_ONE.getId()) {

			tempEmbedOptionVBox = handleOneToOneVBox(dbName, tableName, foreignTableName, foreignKeysVBox);

		} else if (tr.getId() == TablesRelations.ONE_TO_FEW.getId()) {

			tempEmbedOptionVBox = handleOneToFewVBox(dbName, tableName, foreignTableName, foreignKeysVBox);

		} else if (tr.getId() == TablesRelations.ONE_TO_MANY.getId()) {

			tempEmbedOptionVBox = handleOneToManyVBox(dbName, tableName, foreignTableName, foreignKeysVBox);
		} else if (tr.getId() == TablesRelations.ONE_TO_SQUILLION.getId()) {

			tempEmbedOptionVBox = handleOneToSquillionsBox(dbName, tableName, foreignTableName, foreignKeysVBox);
		}

		foreignKeysVBox.getChildren().add(INDEX_THUMB_RULE_OPTION, tempEmbedOptionVBox);

	}

	public VBox handleOneToOneVBox(String dbName, String tableName, String foreignTableName, VBox foreignKeysVBox) {

		VBox tempEmbedOptionVBox = new VBox();

		String id = getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX);

		removeTemps(foreignKeysVBox, id);

		tempEmbedOptionVBox.setId(id);

		ToggleGroup group = new ToggleGroup();

		RadioButton rb1 = new RadioButton("Embed " + foreignTableName.toUpperCase() + " to " + tableName.toUpperCase());
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectID of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb2.setToggleGroup(group);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2);

		return tempEmbedOptionVBox;
	}

	public VBox handleOneToFewVBox(String dbName, String tableName, String foreignTableName, VBox foreignKeysVBox) {

		VBox tempEmbedOptionVBox = new VBox();

		String id = getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX);

		removeTemps(foreignKeysVBox, id);

		tempEmbedOptionVBox.setId(id);

		ToggleGroup group = new ToggleGroup();

		RadioButton rb1 = new RadioButton(
				"Embed all " + foreignTableName.toUpperCase() + " columns to " + tableName.toUpperCase());
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectIDs of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb2.setToggleGroup(group);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2);

		return tempEmbedOptionVBox;
	}

	public VBox handleOneToManyVBox(String dbName, String tableName, String foreignTableName, VBox foreignKeysVBox) {

		VBox tempEmbedOptionVBox = new VBox();

		ToggleGroup group = new ToggleGroup();

		String id = getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX);

		removeTemps(foreignKeysVBox, id);

		tempEmbedOptionVBox.setId(id);

		RadioButton rb1 = new RadioButton(
				"Reference ObjectIDs of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb1.setId("One");
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectID of " + tableName.toUpperCase() + " in " + foreignTableName.toUpperCase());
		rb2.setId("Two");
		rb2.setToggleGroup(group);

		RadioButton rb3 = new RadioButton(
				"Denormalize Many-to-One | " + foreignTableName.toUpperCase() + " -> " + tableName.toUpperCase());
		rb3.setId("Three");
		rb3.setToggleGroup(group);

		RadioButton rb4 = new RadioButton(
				"Denormalize One-To-Many | " + tableName.toUpperCase() + " -> " + foreignTableName.toUpperCase());
		rb4.setId("Four");
		rb4.setToggleGroup(group);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2, rb3, rb4);

		final String tableNameFinal = tableName;
		final String foreignTableNameFinal = foreignTableName;

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

				if (group.getSelectedToggle() != null) {

					RadioButton rb = (RadioButton) new_toggle.getToggleGroup().getSelectedToggle();

					// System.out.println("Embed Option: " + rb.getText() + ", "
					// + rb.getId());

					if (StringUtils.equalsIgnoreCase(rb.getId(), "Three")) {
						addColumns(dbName, tableNameFinal, foreignTableNameFinal, foreignKeysVBox, false);
					} else if (StringUtils.equalsIgnoreCase(rb.getId(), "Four")) {
						addColumns(dbName, tableNameFinal, foreignTableNameFinal, foreignKeysVBox, true);
					} else {
						removeForeignColumnVBox(foreignKeysVBox, tableNameFinal, foreignTableNameFinal);
					}

				}

			}
		});

		return tempEmbedOptionVBox;
	}

	public VBox handleOneToSquillionsBox(String dbName, String tableName, String foreignTableName,
			VBox foreignKeysVBox) {

		VBox tempEmbedOptionVBox = new VBox();

		ToggleGroup group = new ToggleGroup();

		String id = getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX);

		removeTemps(foreignKeysVBox, id);

		tempEmbedOptionVBox.setId(id);

		RadioButton rb1 = new RadioButton(
				"Reference ObjectID of " + tableName.toUpperCase() + " in " + foreignTableName.toUpperCase());
		rb1.setId("One");
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Denormalize Many-to-One | " + foreignTableName.toUpperCase() + " -> " + tableName.toUpperCase());
		rb2.setId("Two");
		rb2.setToggleGroup(group);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2);

		final String tableNameFinal = tableName;
		final String foreignTableNameFinal = foreignTableName;

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

				if (group.getSelectedToggle() != null) {

					RadioButton rb = (RadioButton) new_toggle.getToggleGroup().getSelectedToggle();

					// System.out.println("Embed Option: " + rb.getText() + ", "
					// + rb.getId());

					if (StringUtils.equalsIgnoreCase(rb.getId(), "Two")) {
						addColumns(dbName, tableNameFinal, foreignTableNameFinal, foreignKeysVBox, true);
					} else {
						removeForeignColumnVBox(foreignKeysVBox, tableNameFinal, foreignTableNameFinal);
					}

				}

			}
		});

		return tempEmbedOptionVBox;
	}

	public void addColumns(String dbName, String tableName, String foreignTableName, VBox foreignKeysVBox,
			boolean useForeignKey) {

		// removeForeignColumnsTemps(foreignKeysVBox);

		List<String> foreignTableColumns = null;
		try {

			String useTable = tableName;

			if (useForeignKey) {
				useTable = foreignTableName;
			}

			foreignTableColumns = MySqlUtils.getColumns(dbName, useTable);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		VBox tempForeignColumnsVBox = new VBox();
		tempForeignColumnsVBox.setStyle("-fx-background-color: #ccc;");

		String id = getId(tableName, foreignTableName, ID_FOREIGN_COLUMNS_VBOX);
		removeTemps(foreignKeysVBox, id);
		tempForeignColumnsVBox.setId(id);

		CheckBox cb = new CheckBox("<OBJECTID>");
		tempForeignColumnsVBox.getChildren().add(cb);

		for (String column : foreignTableColumns) {

			cb = new CheckBox(column);
			tempForeignColumnsVBox.getChildren().add(cb);
		}

		foreignKeysVBox.getChildren().add(INDEX_FOREIGN_COLUMNS, tempForeignColumnsVBox);
	}

	public void removeAllTemps(VBox foreignKeysVBox, String tableName, String foreignTableName) {

		removeTemps(foreignKeysVBox, getId(tableName, foreignTableName, ID_CUSTOMIZE_DATA_MODELING_OPTION_HBOX));
		removeTemps(foreignKeysVBox, getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX));

		removeForeignColumnVBox(foreignKeysVBox, tableName, foreignTableName);
	}

	public void removeForeignColumnVBox(VBox foreignKeysVBox, String tableName, String foreignTableName) {
		removeTemps(foreignKeysVBox, getId(tableName, foreignTableName, ID_FOREIGN_COLUMNS_VBOX));
	}

	public void removeTemps(VBox foreignKeysVBox, String id) {

		id = getFindId(id);

		Node node = getNodeById(foreignKeysVBox, id);

		if (node != null) {

			foreignKeysVBox.getChildren().remove(foreignKeysVBox.lookup(id));
		}

	}

	public Node getNodeById(VBox foreignKeysVBox, String id) {

		return foreignKeysVBox.lookup(id);
	}

	public List<HBox> getHeader(String tableName) {

		List<HBox> headers = new ArrayList<>();

		if (!migratorDocument.shouldMigrateAllTables()) {

			HBox hBox = new HBox(10);
			hBox.setId(tableName + "-" + ID_MIGRATE_TABLE_OPTION_VBOX);
			CheckBox checkBox = new CheckBox("Do NOT Migrate- " + tableName.toUpperCase());

			hBox.getChildren().addAll(checkBox);

			headers.add(hBox);
		}

		return headers;
	}

	public ComboBox<TablesRelations> getRelationshipComboBox() {

		TablesRelations[] relations = TablesRelations.values();
		ComboBox<TablesRelations> relationalComboBox = new ComboBox<>();
		relationalComboBox.getItems().addAll(relations);

		return relationalComboBox;
	}

	public String getId(String tableName, String foreignTableName, String identifier) {

		return tableName + "-" + foreignTableName + "-" + identifier;
	}

	public String getFindId(String id) {

		return "#" + id;
	}

	public boolean getBooleanValue(String value) {

		if (StringUtils.isEmpty(value)) {
			return false;
		}

		if (StringUtils.equalsIgnoreCase("yes", value)) {
			return true;
		} else if (StringUtils.equalsIgnoreCase("no", value)) {
			return false;
		}

		return false;
	}

	public int getLoopLimit(List foreignVBoxChildrens) {

		int count = 0;

		for (Object object : foreignVBoxChildrens) {

			if (!(object instanceof HBox)) {
				continue;
			}
			HBox hBoxChild1 = (HBox) object;

			if (hBoxChild1.getId() != null && hBoxChild1.getId().endsWith(ID_FOREIGN_RELATION_VBOX_HBOX)) {
				count++;
			}
		}

		return count;
	}

}
