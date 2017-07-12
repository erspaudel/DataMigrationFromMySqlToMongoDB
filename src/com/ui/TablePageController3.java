package com.ui;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import org.bson.Document;

import com.action.RefactorConfigFile;
import com.constants.TablesRelations;
import com.file.handling.ConfigWriter;
import com.utils.ConfigUtils;
import com.utils.JavaFxUtils;
import com.utils.MigratorDocument;
import com.utils.MongoUtils;
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

@SuppressWarnings({ "rawtypes", "unchecked" })
public class TablePageController3 implements Initializable {

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
		readConfiguration();
	}

	public Document getConfigurations() throws IOException {
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

	private List<Document> processRelations(List childrens) {

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

				document_relation.append(ConfigUtils.FK_TABLE_NAME, foreignTableName);

				System.out.println("\t" + foreignTableName);

				Object childObject = foreignVBoxChildrens.get(INDEX_RELATION);

				System.out.println("\t\tRELATION TYPE: " + childObject);

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
		rb1.setId(ConfigUtils.RELATION_CODE_One_to_One_1);
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectID of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb2.setToggleGroup(group);
		rb2.setId(ConfigUtils.RELATION_CODE_One_to_One_2);

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
		rb1.setId(ConfigUtils.RELATION_CODE_One_to_Few_1);

		RadioButton rb2 = new RadioButton(
				"Reference ObjectIDs of " + foreignTableName.toUpperCase() + " in " + tableName.toUpperCase());
		rb2.setToggleGroup(group);
		rb2.setId(ConfigUtils.RELATION_CODE_One_to_Few_2);

		tempEmbedOptionVBox.getChildren().addAll(rb1, rb2);

		return tempEmbedOptionVBox;
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
				"Denormalize Many-to-One | " + foreignTableName.toUpperCase() + " -> " + tableName.toUpperCase());
		rb3.setId(ConfigUtils.RELATION_CODE_One_to_Many_3);
		rb3.setToggleGroup(group);

		RadioButton rb4 = new RadioButton(
				"Denormalize One-To-Many | " + tableName.toUpperCase() + " -> " + foreignTableName.toUpperCase());
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

		RadioButton rb1 = new RadioButton(
				"Reference ObjectID of " + tableName.toUpperCase() + " in " + foreignTableName.toUpperCase());
		rb1.setId(ConfigUtils.RELATION_CODE_One_to_Squillion_1);
		rb1.setToggleGroup(group);

		RadioButton rb2 = new RadioButton(
				"Denormalize Many-to-One | " + foreignTableName.toUpperCase() + " -> " + tableName.toUpperCase());
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

	public String getForeignTableNameFromId(String id) {

		String[] splittedId = id.split("-");

		if (splittedId.length > 1) {
			return splittedId[1];
		}

		return "";
	}

	public void readConfiguration() throws Exception {

		System.out.println("TablePageController.readConfiguration()");

		RefactorConfigFile.refactorConfig();

		List<Document> tables = ConfigUtils.getTables();

		for (Document tableDocument : tables) {

			processTable(tableDocument);
		}
	}

	public void processTable(Document tableDocument) throws Exception {

		Document mainDocument = ConfigUtils.getDocument();

		String dbName = mainDocument.getString(ConfigUtils.DB_NAME);
		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		int startLimit = 0;
		int limit = MySqlUtils.getStaticQueryLimitStart(tableName);
		int endLimit = limit;
		int iteratorLength = MySqlUtils.getStaticIteratorLength(tableName);

		for (int i = 1; i <= iteratorLength; i++) {

			try {
				List<Document> documents = getDocuments(tableDocument, startLimit, endLimit);

				for (Document document : documents) {
					System.err.println("\t\t\t" + document.toJson().toUpperCase());
				}
			} catch (Exception e) {
				continue;
			}

			startLimit = endLimit;
			endLimit += limit;
		}
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

	public List<Document> getDocuments(Document tableDocument, int startLimit, int endLimit) throws Exception {

		List<Document> documents = new ArrayList<>();

		List<Document> relations = (List<Document>) tableDocument.get(ConfigUtils.RELATIONS);

		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		System.out.println("\t " + tableName.toUpperCase());

		for (int i = startLimit; i < endLimit; i++) {

			Document document = getDocumentFromMySql(tableDocument, i);

			System.out.println("\t\t" + document.toJson());

			for (Document relationDocument : relations) {

				if (relationDocument == null) {
					continue;
				}

				System.out.println("\t\t\t" + relationDocument.toJson());

				int relationCode = 0;

				try {

					Object rc = relationDocument.get(ConfigUtils.FK_RELATION_CODE);

					if (rc != null) {
						relationCode = (int) rc;
					}

				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}

				switch (relationCode) {
				case ConfigUtils.ONE_to_ONE:

					processOneToOne(document, relationDocument);

					break;
				case ConfigUtils.ONE_to_FEW:

					processOneToFew(tableName, document, relationDocument);

					break;
				case ConfigUtils.ONE_to_MANY:

					break;
				case ConfigUtils.ONE_to_SQUILLION:

					break;

				}
			}

			documents.add(document);

		}

		return documents;
	}

	public void processOneToOne(Document document, Document relationDocument) throws SQLException {

		String thumbRuleCode = "";

		// thumbRuleCode =
		// relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);

		String fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);

		int id = Integer.parseInt(document.getString(fkTableName + "_id"));

		boolean embed = true;

		switch (thumbRuleCode) {
		case ConfigUtils.RELATION_CODE_One_to_One_1:

			embed = true;

			break;

		case ConfigUtils.RELATION_CODE_One_to_One_2:

			embed = false;

			break;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(fkTableName);
		sb.append(" WHERE id=");
		sb.append(id);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		while (rs.next()) {

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (embed) {
					if (StringUtils.isNotEmpty(value)) {
						document.append(key, value);
					}
				} else {
					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						document.append(fkTableName + "_id", MongoUtils.getObjectId(mySqlId).toString());
					}
				}
			}
		}
	}

	public void processOneToFew(String tableName, Document document, Document relationDocument) throws SQLException {

		System.out.println("TablePageController.processOneToFew()");

		String thumbRuleCode = "";
		String fkTableName = "";
		int id = 0;
		try {
			// Object thumbRuleCodeObject =
			// relationDocument.get(ConfigUtils.FK_THUMB_RULE_CODE);
			// if (thumbRuleCodeObject != null) {
			// thumbRuleCode =
			// relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);
			// }

			Object fkTableNameObject = relationDocument.get(ConfigUtils.FK_TABLE_NAME);
			if (fkTableNameObject != null) {

				fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);
			}

			Object idStr = document.get("id");
			if (idStr != null) {
				id = Integer.parseInt(idStr.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean embedAll = true;

		switch (thumbRuleCode) {
		case ConfigUtils.RELATION_CODE_One_to_Few_1:

			embedAll = true;
			break;
		case ConfigUtils.RELATION_CODE_One_to_Few_2:

			embedAll = false;
			break;
		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(fkTableName);
		sb.append(" WHERE ");
		sb.append(tableName);
		sb.append("_id=");
		sb.append(id);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		List<Document> embedDocuments = new ArrayList<>();
		List<String> referenceDocuments = new ArrayList<>();

		System.out.println("Embed: " + embedAll);

		while (rs.next()) {

			Document doc = new Document();

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (embedAll) {

					if (StringUtils.equalsIgnoreCase(tableName + "_id", key)) {
						continue;
					}

					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						doc.append("_id", MongoUtils.getObjectId(mySqlId).toString());
					}

					if (StringUtils.isNotEmpty(value)) {
						doc.append(key, value);
					}

				} else {
					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						referenceDocuments.add(MongoUtils.getObjectId(mySqlId).toString());
					}

				}

			}

			if (embedAll) {
				embedDocuments.add(doc);
				document.append(fkTableName + "s", embedDocuments);
			} else {
				document.append(fkTableName + "s", referenceDocuments);
			}
		}

	}

	public void processOneToMany(String tableName, Document document, Document relationDocument) throws SQLException {

		String thumbRuleCode = "";
		String fkTableName = "";
		int id = 0;
		try {
			Object thumbRuleCodeObject = relationDocument.get(ConfigUtils.FK_THUMB_RULE_CODE);
			if (thumbRuleCodeObject != null) {
				thumbRuleCode = relationDocument.getString(ConfigUtils.FK_THUMB_RULE_CODE);
			}

			Object fkTableNameObject = relationDocument.get(ConfigUtils.FK_TABLE_NAME);
			if (fkTableNameObject != null) {

				fkTableName = relationDocument.getString(ConfigUtils.FK_TABLE_NAME);
			}

			Object idStr = document.get("id");
			if (idStr != null) {
				id = Integer.parseInt(idStr.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		boolean embedAll = false;

		switch (thumbRuleCode) {
		case ConfigUtils.RELATION_CODE_One_to_Many_1:

			break;
		case ConfigUtils.RELATION_CODE_One_to_Many_2:

			break;
		case ConfigUtils.RELATION_CODE_One_to_Many_3:

			break;
		case ConfigUtils.RELATION_CODE_One_to_Many_4:

			break;

		}

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(fkTableName);
		sb.append(" WHERE ");
		sb.append(tableName);
		sb.append("_id=");
		sb.append(id);

		ResultSet rs = MySqlUtils.getStatement().executeQuery(sb.toString());

		ResultSetMetaData meta = rs.getMetaData();

		List<Document> embedDocuments = new ArrayList<>();
		List<String> referenceDocuments = new ArrayList<>();

		while (rs.next()) {

			Document doc = new Document();

			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (embedAll) {

					if (StringUtils.equalsIgnoreCase(tableName + "_id", key)) {
						continue;
					}

					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						doc.append("_id", MongoUtils.getObjectId(mySqlId).toString());
					}

					if (StringUtils.isNotEmpty(value)) {
						doc.append(key, value);
					}

				} else {
					if (StringUtils.equals("id", key)) {
						int mySqlId = Integer.parseInt(value);
						referenceDocuments.add(MongoUtils.getObjectId(mySqlId).toString());
					}

				}

			}

			if (embedAll) {
				embedDocuments.add(doc);
				document.append(fkTableName + "s", embedDocuments);
			} else {
				document.append(fkTableName + "s", referenceDocuments);
			}
		}

	}

	public Document getDocumentFromMySql(Document tableDocument, int startLimit) throws SQLException {

		String tableName = tableDocument.getString(ConfigUtils.TABLE_NAME);

		StringBuilder sb = new StringBuilder();

		sb.append("SELECT * FROM ");
		sb.append(tableName);
		sb.append(" ORDER BY ");
		sb.append(tableName);
		sb.append(".id ASC limit ");
		sb.append(startLimit);
		sb.append(",");
		sb.append(1);

		return getDocumentByQuery(sb.toString(), true);

	}

	public Document getDocumentByQuery(String query, boolean includeMongoDBId) throws SQLException {

		Document document = new Document();

		ResultSet rs = MySqlUtils.getStatement().executeQuery(query.toString());

		ResultSetMetaData meta = rs.getMetaData();

		while (rs.next()) {

			/*
			 * Get values by column names
			 */
			for (int k = 1; k <= meta.getColumnCount(); k++) {

				String key = meta.getColumnName(k);
				String value = rs.getString(key);

				if (includeMongoDBId) {
					if (StringUtils.equals("id", key)) {
						int id = Integer.parseInt(value);
						document.append("_id", MongoUtils.getObjectId(id).toString());
					}
				}

				if (StringUtils.isNotEmpty(value)) {

					document.append(key, value);
				}

			}

		}

		return document;
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
}
