package com.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import org.bson.Document;

import com.action.ErrorHandler;
import com.action.RefactorConfigFile;
import com.constants.TablesRelations;
import com.file.handling.ConfigWriter;
import com.multithreading.FileToMongoDBThread;
import com.multithreading.MySQLToFileThread;
import com.utils.ConfigUtils;
import com.utils.FileUtils;
import com.utils.JavaFxUtils;
import com.utils.MigratorDocument;
import com.utils.MySqlUtils;
import com.utils.StringUtils;
import com.utils.TestUtils;

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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TablePageController4 implements Initializable {

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
	private static final String ID_FOREIGN_RELATION_VBOX_HBOX = "FOREIGN_RELATION_VBOX_HBOX";
	private static final String ID_CUSTOMIZE_DATA_MODELING_OPTION_HBOX = "CUSTOMIZE_DATA_MODELING";
	private static final String ID_THUMB_RULE_OPTION_VBOX = "THUMB_RULE_OPTION";
	private static final String ID_FOREIGN_COLUMNS_VBOX = "FOREIGN_COLUMNS";

	private static final int FIRST_INDEX = 0;

	private static final int INDEX_RELATION = 0;
	private static final int INDEX_CUSTOMIZE_DATA_MODELING_OPTION = 1;
	private static final int INDEX_THUMB_RULE_OPTION = 2;
	private static final int INDEX_FOREIGN_COLUMNS = 3;

	private static final int INDEX_RELATION_LABEL = 0;
	private static final int INDEX_RELATION_TYPE_COMBOBOX = 1;

	public Map<String, Pane> tempMap = new HashMap<String, Pane>();

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		System.out.println("TablePageController.initialize(): " + ConfigUtils.getDocument());

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
	public void startMigration(ActionEvent event) throws Exception {
		Document document = getConfigurations();
		writeConfigurations(document);
		processConfiguration();
	}

	public Document getConfigurations() throws Exception {
		List<TitledPane> titledPanes = accordion.getPanes();

		Document document = ConfigUtils.getDocument();

		List<Document> tablesDocument = new ArrayList<>();

		for (TitledPane tp : titledPanes) {

			VBox content = (VBox) tp.getContent();

			String tableName = tp.getText();

			Document document_table = new Document();
			document_table.append(ConfigUtils.TABLE_NAME, tableName);

			List childrens = content.getChildren();

			if (childrens.size() == 0) {
				tablesDocument.add(document_table);
				continue;
			}

			if ((childrens.get(INDEX_RELATION) instanceof HBox) && (!ConfigUtils.shouldMigrateAllTables())) {

				/*
				 * Migrate table option
				 */

				HBox migrateHBox = (HBox) childrens.get(FIRST_INDEX);

				if (migrateHBox.getId().endsWith(ID_MIGRATE_TABLE_OPTION_VBOX)) {

					CheckBox checkBox = (CheckBox) migrateHBox.getChildren().get(FIRST_INDEX);

					if (checkBox.isSelected()) {
						continue;
					}

				}

			}

			/*
			 * Foreign Relationship options
			 */

			List<Document> documentRelations = processRelations(childrens);

			document_table.append(ConfigUtils.RELATIONS, documentRelations);

			tablesDocument.add(document_table);
		}

		document.append(ConfigUtils.TABLES, tablesDocument);

		return document;
	}

	private List<Document> processRelations(List childrens) throws SQLException {

		VBox foreignVBox;
		List<Document> documentRelations = new ArrayList<>();

		for (Object object : childrens) {

			System.out.println("\n" + object);

			Document document_relation = null;

			if (object instanceof HBox) {
				// TODO Handle migrate table option

			} else if (object instanceof VBox) {

				document_relation = new Document();

				foreignVBox = (VBox) object;

				List foreignVBoxChildrens = foreignVBox.getChildren();

				String foreignTableName = getForeignTableNameFromId(foreignVBox.getId());
				String tableName = getTableNameFromId(foreignVBox.getId());

				document_relation.append(ConfigUtils.FK_TABLE_NAME, foreignTableName);

				Object childObject = foreignVBoxChildrens.get(INDEX_RELATION);

				System.out.println("\tRELATION TYPE: " + childObject);

				HBox hBoxChild1 = (HBox) childObject;

				/*
				 * Handle the selected relation types
				 */

				List innerChildrens = ((HBox) hBoxChild1).getChildren();

				/*
				 * Relation type combo box
				 */

				ComboBox<TablesRelations> tablelRelationComboBox = (ComboBox<TablesRelations>) innerChildrens
						.get(INDEX_RELATION_TYPE_COMBOBOX);
				TablesRelations tr = (TablesRelations) tablelRelationComboBox.getSelectionModel().getSelectedItem();

				if (tr != null) {
					/*
					 * Customize the relation type
					 */

					int relation = tr.getId();

					document_relation.append(ConfigUtils.FK_IS_AUTOMATIC, false);
					document_relation.append(ConfigUtils.FK_RELATION_CODE, relation);

					System.out.println("\t\t\tRelation: " + relation);

				} else {

					document_relation.append(ConfigUtils.FK_RELATION_CODE,
							getRelationCode(tableName, foreignTableName));
					document_relation.append(ConfigUtils.FK_IS_AUTOMATIC, true);

					/*
					 * Automate the relation type
					 */

				}

				processCustomizationOption(foreignVBoxChildrens);

				processThumbRuleOptions(document_relation, foreignVBoxChildrens);

				processDenormalizedColumns(document_relation, foreignVBoxChildrens);

			}

			documentRelations.add(document_relation);
		}
		return documentRelations;
	}

	private void processCustomizationOption(List foreignVBoxChildrens) {

		if (foreignVBoxChildrens.size() > 1) {

			// Object childObject =
			// foreignVBoxChildrens.get(INDEX_CUSTOMIZE_DATA_MODELING_OPTION);

			// HBox customizeOptionHBox = (HBox) childObject;

			// for (Object object2 :
			// customizeOptionHBox.getChildren()) {
			// System.out.println("\t\t\t" + object2);
			// }

		}
	}

	private void processThumbRuleOptions(Document document_relation, List foreignVBoxChildrens) {
		if (foreignVBoxChildrens.size() > 2) {

			Object childObject = foreignVBoxChildrens.get(INDEX_THUMB_RULE_OPTION);

			VBox thumnRuleOptionVBox = (VBox) childObject;

			for (Object object2 : thumnRuleOptionVBox.getChildren()) {

				RadioButton rb = (RadioButton) object2;
				if (rb.isSelected()) {

					document_relation.append(ConfigUtils.FK_THUMB_RULE_CODE, rb.getId());
					System.out.println("\t\t\tThumb Rule Code: " + rb.getId() + ", Label: " + rb.getText());
				}
			}

		}
	}

	private void processDenormalizedColumns(Document document_relation, List foreignVBoxChildrens) {
		if (foreignVBoxChildrens.size() > 3) {

			Object childObject = foreignVBoxChildrens.get(INDEX_FOREIGN_COLUMNS);

			VBox foreignColumnVBox = (VBox) childObject;

			List<String> denormalizedColumns = new ArrayList<>();

			for (Object object2 : foreignColumnVBox.getChildren()) {

				CheckBox cb = (CheckBox) object2;

				if (cb.isSelected()) {
					System.out.println("\t\t\t" + cb.getText());
					denormalizedColumns.add(cb.getText());
				}
			}

			document_relation.append(ConfigUtils.DENORMALIZATION_COLUMNS, denormalizedColumns);
		}
	}

	private void writeConfigurations(Document document) throws FileNotFoundException, IOException {
		ConfigWriter writer = new ConfigWriter();
		writer.clearFileContent();
		// writer.write(MigratorUtils.getPrettyJson(document.toJson()));
		writer.write(document.toJson());
		writer.close();
		// System.out.println("\n\n\nFINAL DATA: \n\t" +
		// MigratorUtils.getPrettyJson(document.toJson()));
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

			System.out.println("Set ID: " + getId(tableName, fk.getValue(), ID_FOREIGN_RELATION_VBOX));

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

			System.out.println("\t Childrens: " + foreignKeysVBox.getChildren().size());

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

		// if (tr.getId() == ConfigUtils.ONE_to_ONE || tr.getId() ==
		// ConfigUtils.ONE_to_FEW) {
		tempDenormalizationOptionHBox.setVisible(false);
		// }

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

		// Not needed code but not removed due to code refactor
		tempDenormalizationOptionHBox.getChildren().addAll(labelManuallyDemormalize, radioButton1, radioButton2);
		foreignKeysVBox.getChildren().add(INDEX_CUSTOMIZE_DATA_MODELING_OPTION, tempDenormalizationOptionHBox);

		try {
			addManuallyDenormalizeOptions(dbName, tableName, foreignTableName, tr, foreignKeysVBox);
		} catch (SQLException e1) {
			e1.printStackTrace();
		}

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
		rb1.setId(ConfigUtils.RELATION_CODE_One_to_One_1);
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectID of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb2.setToggleGroup(group);
		rb2.setId(ConfigUtils.RELATION_CODE_One_to_One_2);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2);

		return new VBox();
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
		rb1.setId(ConfigUtils.RELATION_CODE_One_to_Few_1);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectIDs of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb2.setToggleGroup(group);
		rb2.setId(ConfigUtils.RELATION_CODE_One_to_Few_2);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2);

		return new VBox();
	}

	public VBox handleOneToManyVBox(String dbName, String tableName, String foreignTableName, VBox foreignKeysVBox) {

		VBox tempEmbedOptionVBox = new VBox();

		ToggleGroup group = new ToggleGroup();

		String id = getId(tableName, foreignTableName, ID_THUMB_RULE_OPTION_VBOX);

		removeTemps(foreignKeysVBox, id);

		tempEmbedOptionVBox.setId(id);

		/*
		 * RadioButton rb1 = new RadioButton( "Reference ObjectIDs of " +
		 * foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		 * rb1.setId(ConfigUtils.RELATION_CODE_One_to_Many_1);
		 * rb1.setToggleGroup(group);
		 * 
		 * RadioButton rb2 = new RadioButton( "Reference ObjectID of " +
		 * tableName.toUpperCase() + " in " + foreignTableName.toUpperCase());
		 * rb2.setId(ConfigUtils.RELATION_CODE_One_to_Many_2);
		 * rb2.setToggleGroup(group);
		 */

		RadioButton rb3 = new RadioButton(
				"Denormalize Many-To-One | " + tableName.toUpperCase() + " -> " + foreignTableName.toUpperCase());
		rb3.setId(ConfigUtils.RELATION_CODE_One_to_Many_3);
		rb3.setToggleGroup(group);

		RadioButton rb4 = new RadioButton(
				"Denormalize One-to-Many | " + foreignTableName.toUpperCase() + " -> " + tableName.toUpperCase());
		rb4.setId(ConfigUtils.RELATION_CODE_One_to_Many_4);
		rb4.setToggleGroup(group);

		tempEmbedOptionVBox.getChildren().addAll(rb3, rb4);

		final String tableNameFinal = tableName;
		final String foreignTableNameFinal = foreignTableName;

		group.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
			public void changed(ObservableValue<? extends Toggle> ov, Toggle old_toggle, Toggle new_toggle) {

				if (group.getSelectedToggle() != null) {

					RadioButton rb = (RadioButton) new_toggle.getToggleGroup().getSelectedToggle();

					// System.out.println("Embed Option: " + rb.getText() + ", "
					// + rb.getId());

					if (StringUtils.equalsIgnoreCase(rb.getId(), ConfigUtils.RELATION_CODE_One_to_Many_3)) {
						addColumns(dbName, tableNameFinal, foreignTableNameFinal, foreignKeysVBox, false);
					} else if (StringUtils.equalsIgnoreCase(rb.getId(), ConfigUtils.RELATION_CODE_One_to_Many_4)) {
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

		// RadioButton rb1 = new RadioButton("Reference ObjectID of " +
		// tableName.toUpperCase() + " in " + foreignTableName.toUpperCase());
		RadioButton rb1 = new RadioButton("Do not denormalize");
		rb1.setId(ConfigUtils.RELATION_CODE_One_to_Squillion_1);
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Denormalize One-to-Squillion | " + foreignTableName.toUpperCase() + " -> " + tableName.toUpperCase());
		rb2.setId(ConfigUtils.RELATION_CODE_One_to_Squillion_2);
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

					if (StringUtils.equalsIgnoreCase(rb.getId(), ConfigUtils.RELATION_CODE_One_to_Squillion_2)) {
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

	public String getForeignTableNameFromId(String id) {

		String[] splittedId = id.split("-");

		if (splittedId.length > 1) {
			return splittedId[1];
		}

		return "";
	}

	public String getTableNameFromId(String id) {

		String[] splittedId = id.split("-");

		if (splittedId.length > 0) {
			return splittedId[0];
		}

		return "";
	}

	public int getRelationCode(String tableName, String foreignTableName) throws SQLException {

		int relationCount = MySqlUtils.getRelationCount(tableName, foreignTableName);

		System.out.println("\tRelation Count: " + relationCount);

		if (relationCount <= ConfigUtils.LIMIT_ONE_to_ONE) {
			return ConfigUtils.ONE_to_ONE;
		} else if (relationCount > ConfigUtils.LIMIT_ONE_to_ONE && relationCount <= ConfigUtils.LIMIT_ONE_to_FEW) {
			return ConfigUtils.ONE_to_FEW;
		} else if (relationCount > ConfigUtils.LIMIT_ONE_to_FEW && relationCount <= ConfigUtils.LIMIT_ONE_to_MANY) {
			return ConfigUtils.ONE_to_MANY;
		}

		return ConfigUtils.ONE_to_SQUILLION;

	}

	public List<Integer> getRandomQueryIds(String tableName, String foreignTableName) throws SQLException {

		String dbName = ConfigUtils.getDbName();

		int minId = MySqlUtils.getMinId(dbName, tableName);
		int maxId = MySqlUtils.getMaxId(dbName, tableName);
		int totalRecords = MySqlUtils.getRowsCount(dbName, tableName);

		double countDouble = (maxId - minId) / totalRecords;
		Double countDouble1 = Math.floor(countDouble);
		int count = countDouble1.intValue();

		List<Integer> ids = new ArrayList<>();

		for (int i = minId; i <= maxId; i += count) {
			ids.add(i);
		}

		return ids;

	}

	public void processConfiguration() throws Exception {

		RefactorConfigFile.refactorConfig();

		List<Document> tables = ConfigUtils.getTables();

		CountDownLatch latch = new CountDownLatch(tables.size());

		for (Document tableDocument : tables) {

			if (tableDocument == null) {
				continue;
			}

			MySQLToFileThread thread = new MySQLToFileThread(tableDocument, latch);
			thread.run();
		}

		latch.await();

		System.out.println("EXPORT COMPLETED");

		File[] directories = new File(FileUtils.FILE_PATH_IN_PROGRESS).listFiles(File::isDirectory);

		latch = new CountDownLatch(directories.length);

		for (File file : directories) {

			System.out.println(file.getAbsolutePath());
			System.err.println("\t" + file.getName().toUpperCase());

			FileToMongoDBThread thread = new FileToMongoDBThread(file, latch);

			thread.start();

			FileUtils.deleteEmptyDirectory(file);
		}

		latch.await();

		System.out.println("DATA MIGRATION COMPLETED!!");
		System.out.println("Waiting for start Error handing...");

		if (TestUtils.ENABLE_THREAD_SLEEP) {

			Thread.sleep(50000);
		}

		ErrorHandler.startErrorHandling();

	}

	public String getOneToOneRelationQuery(Document tableDocument, Document relationDocument, int startLimit,
			int endLimit) throws Exception {

		String thumbRuleCode = relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);

		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);
		String foreignTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);

		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ");
		sb.append(tableName);

		if (StringUtils.isEmpty(thumbRuleCode)) {
			throw new Exception("Relation Code cannot be null");
		}

		switch (thumbRuleCode) {

		case ConfigUtils.RELATION_CODE_One_to_One_1:

			sb.append(" LEFT JOIN ");
			sb.append(foreignTableName);
			sb.append(" ON ");
			sb.append(tableName);
			sb.append(".");
			sb.append(foreignTableName);
			sb.append("_id=");
			sb.append(foreignTableName);
			sb.append(".id");

			break;
		case ConfigUtils.RELATION_CODE_One_to_One_2:

			break;
		}

		sb.append(" ORDER BY ");
		sb.append(tableName);
		sb.append(".id ASC limit ");
		sb.append(startLimit);
		sb.append(",");
		sb.append(endLimit - startLimit);

		return sb.toString();

	}

	public ResultSet getResultSet(String query, int startLimit, int endLimit) throws SQLException {

		StringBuilder builder = new StringBuilder(query);
		builder.append(startLimit);
		builder.append(",");
		builder.append(endLimit - startLimit);

		return MySqlUtils.getStatement().executeQuery(builder.toString());
	}

	public void debug(String message) {
		System.out.println(message);
	}

	public static void main(String[] args) throws Exception {
		new TablePageController4().processConfiguration();
	}
}
