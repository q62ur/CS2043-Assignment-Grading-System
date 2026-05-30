import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class UI extends Application {

    private Coordinator coordinator = new Coordinator();
    private TextField statusBox;

    // “Global” folders chosen by the user
    private String currentRootFolder = "";
    private String currentResultsFolder = "";

    // Text of the last execution (what we show in the Execute TS popup)
    private String lastExecutionText = "";

    @Override
    public void start(Stage stage) {

        // v1: we work with a single test suite only
        coordinator.createTestSuite("Main Test Suite");

        statusBox = new TextField();
        statusBox.setEditable(false);
        statusBox.setPromptText("status of last action will show here");

        // -----------------------------
        // Version 1 buttons
        // -----------------------------
        Button btnCreateTC = new Button("Create Test Case (manual)");
        Button btnLoadTC   = new Button("Load Test Case from file");
        Button btnAddToTS  = new Button("Add Test Case to Suite");
        Button btnExecTS   = new Button("Execute Test Suite");

        btnCreateTC.setOnAction(e -> openPopup("create", stage));
        btnLoadTC.setOnAction(e -> openPopup("load", stage));
        btnAddToTS.setOnAction(e -> openPopup("add", stage));
        btnExecTS.setOnAction(e -> openPopup("exec", stage));

        // -----------------------------
        // Version 2 buttons
        // -----------------------------
        Button btnLoadResults  = new Button("Load Results");
        Button btnCompareFiles = new Button("Compare Result Files");

        btnLoadResults.setOnAction(e -> openLoadResultsPopup(stage));
        btnCompareFiles.setOnAction(e -> openCompareResultsPopup(stage));

        // -----------------------------
        // Folder-setting buttons (bottom-left)
        // -----------------------------
        Button btnSetRootFolder    = new Button("Set Root Folder");
        Button btnSetResultsFolder = new Button("Set Results Folder");

        btnSetRootFolder.setOnAction(e -> openSetFolderPopup(stage, true));
        btnSetResultsFolder.setOnAction(e -> openSetFolderPopup(stage, false));

        HBox rowV1 = new HBox(15, btnCreateTC, btnLoadTC, btnAddToTS, btnExecTS);
        rowV1.setStyle("-fx-padding: 10;");
        rowV1.setAlignment(Pos.CENTER);

        HBox rowV2 = new HBox(15, btnLoadResults, btnCompareFiles);
        rowV2.setStyle("-fx-padding: 10;");
        rowV2.setAlignment(Pos.CENTER);

        HBox rowFolders = new HBox(10, btnSetRootFolder, btnSetResultsFolder);
        rowFolders.setStyle("-fx-padding: 10;");
        rowFolders.setAlignment(Pos.CENTER_LEFT);   // bottom-left

        VBox root = new VBox(10, statusBox, rowV1, rowV2, rowFolders);
        root.setStyle("-fx-padding: 20;");

        stage.setScene(new Scene(root, 900, 320));
        stage.setTitle("CS2043(FA2025) - Group 13 - Submission #4");
        stage.show();
    }

    /**
     * Popup for create / load / add / execute test suite (V1 actions).
     */
    private void openPopup(String mode, Stage owner) {

        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 10;");

        TextArea a = new TextArea();
        TextArea b = new TextArea();
        TextArea c = new TextArea();
        TextField f1 = new TextField();
        TextArea resultArea = new TextArea();

        Button btnEnter = new Button("Enter");
        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        switch (mode) {

            case "create":
                popup.setTitle("Create Test Case");
                a.setPromptText("Title");
                a.setPrefRowCount(1);
                b.setPromptText("Input");
                c.setPromptText("Expected Output");

                infoLabel.setText("Enter title, input, and expected output above (without quotations).");
                HBox bottomCreate = new HBox(10, btnEnter, infoLabel);
                bottomCreate.setAlignment(Pos.CENTER_LEFT);

                box.getChildren().addAll(a, b, c, bottomCreate);

                btnEnter.setOnAction(e -> {
                    String title = a.getText().trim();
                    if (!title.isEmpty()) {
                        coordinator.createTestCase(title, b.getText(), c.getText());
                        statusBox.setText("Test Case " + title + ".txt Created!");
                    }
                    popup.close();
                });
                break;

            case "load":
                popup.setTitle("Load Test Case from File");
                f1.setPromptText("full path to test case file");

                infoLabel.setText("Enter full path to test case file above (without quotations).");
                HBox bottomLoad = new HBox(10, btnEnter, infoLabel);
                bottomLoad.setAlignment(Pos.CENTER_LEFT);

                box.getChildren().addAll(f1, bottomLoad);

                btnEnter.setOnAction(e -> {
                    String filename = f1.getText().trim();
                    if (filename.isEmpty()) {
                        statusBox.setText("Filename empty, nothing loaded.");
                        return;
                    }

                    TestCase loaded = coordinator.getTestCase(filename);
                    if (loaded != null) {
                        statusBox.setText("Test Case " + loaded.getTitle() + ".txt Loaded!");
                    } else {
                        statusBox.setText("Failed to load Test Case.");
                    }
                    popup.close();
                });
                break;

            case "add":
                popup.setTitle("Add Test Case to Suite");
                f1.setPromptText("Test Case title");

                infoLabel.setText("Enter the Test Case title above (without quotations).");
                HBox bottomAdd = new HBox(10, btnEnter, infoLabel);
                bottomAdd.setAlignment(Pos.CENTER_LEFT);

                box.getChildren().addAll(f1, bottomAdd);

                btnEnter.setOnAction(e -> {
                    String tcTitle = f1.getText().trim();
                    if (!tcTitle.isEmpty()) {
                        coordinator.addTestCaseToSuite(tcTitle);
                        statusBox.setText("Added " + tcTitle + " to Test Suite.");
                    }
                    popup.close();
                });
                break;

            case "exec":
                popup.setTitle("Execute Test Suite");

                resultArea.setEditable(false);
                resultArea.setPrefHeight(200);

                Button btnSaveResults = new Button("Save Results");
                btnSaveResults.setVisible(false);   // only visible after execution

                updateExecInfoLabel(infoLabel);

                HBox bottomExec = new HBox(10, btnEnter, btnSaveResults, infoLabel);
                bottomExec.setAlignment(Pos.CENTER_LEFT);

                box.getChildren().addAll(bottomExec, resultArea);

                btnEnter.setOnAction(e -> {
                    if (currentRootFolder == null || currentRootFolder.trim().isEmpty()) {
                        statusBox.setText("Please set a root folder using 'Set Root Folder' first.");
                        resultArea.setText("No root folder set.\nUse the 'Set Root Folder' button on the main window.");
                        return;
                    }

                    // suite name argument is ignored by ListOfTestSuites.search in v1
                    String r = coordinator.executeTestSuite("ignored", currentRootFolder);
                    resultArea.setText(r);
                    statusBox.setText("Executed Test Suite using root folder:\n" + currentRootFolder);

                    // remember last execution text so we can save it
                    lastExecutionText = r;

                    btnSaveResults.setVisible(true);
                });

                // SAVE RESULTS: UI-only text save (no changes to Coordinator)
                btnSaveResults.setOnAction(e -> saveLastExecutionToFile());

                break;
        }

        popup.setScene(new Scene(box, 500, (mode.equals("exec") ? 350 : 220)));
        popup.showAndWait();
    }

    /**
     * Save lastExecutionText into a .txt file in the results folder.
     */
    private void saveLastExecutionToFile() {
        if (currentRootFolder == null || currentRootFolder.trim().isEmpty()) {
            statusBox.setText("Cannot save results: root folder is not set.");
            return;
        }
        if (currentResultsFolder == null || currentResultsFolder.trim().isEmpty()) {
            statusBox.setText("Cannot save results: results folder is not set.");
            return;
        }
        if (lastExecutionText == null || lastExecutionText.trim().isEmpty()) {
            statusBox.setText("Cannot save results: no execution output available (run the test suite first).");
            return;
        }

        try {
            File resultsDir = new File(currentResultsFolder);
            if (!resultsDir.exists()) {
                resultsDir.mkdirs();
            }

            // simple naming: results_<rootFolderName>_<timestamp>.txt
            String rootName = new File(currentRootFolder).getName();
            long now = System.currentTimeMillis();
            String fileName = "results_" + rootName + "_" + now + ".txt";

            File outFile = new File(resultsDir, fileName);

            try (PrintWriter pw = new PrintWriter(outFile)) {
                pw.print(lastExecutionText);
            }

            statusBox.setText("Results saved to:\n" + outFile.getAbsolutePath());
        } catch (Exception ex) {
            statusBox.setText("Failed to save results: " + ex.getMessage());
        }
    }

    /**
     * Popup to set either the root folder or the results folder.
     */
    private void openSetFolderPopup(Stage owner, boolean isRoot) {

        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 10;");

        TextField pathField = new TextField();
        Button btnEnter = new Button("Enter");
        Label infoLabel = new Label();
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        if (isRoot) {
            popup.setTitle("Set Root Folder");
            pathField.setPromptText("root folder path (where student subfolders are)");
            infoLabel.setText("Enter the root folder path above (without quotations).");
        } else {
            popup.setTitle("Set Results Folder");
            pathField.setPromptText("results folder path (where result files are stored)");
            infoLabel.setText("Enter the results folder path above (without quotations).");
        }

        HBox bottom = new HBox(10, btnEnter, infoLabel);
        bottom.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(pathField, bottom);

        btnEnter.setOnAction(e -> {
            String path = pathField.getText().trim();
            if (path.isEmpty()) {
                statusBox.setText("Path is empty. No folder set.");
            } else {
                if (isRoot) {
                    currentRootFolder = path;
                    statusBox.setText("Root folder set to:\n" + currentRootFolder);
                } else {
                    currentResultsFolder = path;
                    statusBox.setText("Results folder set to:\n" + currentResultsFolder);
                }
            }
            popup.close();
        });

        popup.setScene(new Scene(box, 550, 150));
        popup.showAndWait();
    }

    /**
     * Popup for "Load Results".
     * Uses the currentResultsFolder (set from main window) and lists .txt files there.
     */
    private void openLoadResultsPopup(Stage owner) {

        if (currentResultsFolder == null || currentResultsFolder.trim().isEmpty()) {
            statusBox.setText("Please set a results folder using 'Set Results Folder' first.");
            return;
        }

        File dir = new File(currentResultsFolder);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));

        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 10;");

        Label infoLabel = new Label("Select a results file below and click Enter to view it.");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        ListView<String> fileListView = new ListView<>();
        TextArea resultArea = new TextArea();
        resultArea.setEditable(false);
        resultArea.setPrefHeight(200);

        Button btnEnter = new Button("Enter");

        if (files != null && files.length > 0) {
            for (File f : files) {
                fileListView.getItems().add(f.getName());
            }
        } else {
            fileListView.getItems().add("(No .txt result files found in this folder)");
            fileListView.setDisable(true);
            btnEnter.setDisable(true);
        }

        HBox bottom = new HBox(10, btnEnter, infoLabel);
        bottom.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(fileListView, bottom, resultArea);

        btnEnter.setOnAction(e -> {
            String selectedName = fileListView.getSelectionModel().getSelectedItem();
            if (selectedName == null || selectedName.startsWith("(")) {
                statusBox.setText("Please select a valid results file.");
                return;
            }

            File selectedFile = new File(currentResultsFolder, selectedName);

            try {
                String content = Files.readString(selectedFile.toPath());
                resultArea.setText(content);
                statusBox.setText("Loaded results file: " + selectedName);
            } catch (Exception ex) {
                resultArea.setText("Failed to read file:\n" + ex.getMessage());
                statusBox.setText("Error loading results file.");
            }
        });

        popup.setTitle("Load Results");
        popup.setScene(new Scene(box, 600, 380));
        popup.showAndWait();
    }

    /**
     * Popup for "Compare Result Files".
     * Shows all .txt files with checkboxes; user must pick exactly 2.
     */
    private void openCompareResultsPopup(Stage owner) {

        if (currentResultsFolder == null || currentResultsFolder.trim().isEmpty()) {
            statusBox.setText("Please set a results folder using 'Set Results Folder' first.");
            return;
        }

        File dir = new File(currentResultsFolder);
        File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));

        Stage popup = new Stage();
        popup.initOwner(owner);
        popup.initModality(Modality.APPLICATION_MODAL);

        VBox box = new VBox(10);
        box.setStyle("-fx-padding: 10;");

        Label infoLabel = new Label("Select exactly TWO results files using the checkboxes below, then click Enter to compare.");
        infoLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        VBox checkBoxContainer = new VBox(5);
        ScrollPane scrollPane = new ScrollPane(checkBoxContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(180);

        Button btnEnter = new Button("Enter");
        TextArea comparisonArea = new TextArea();
        comparisonArea.setEditable(false);
        comparisonArea.setPrefHeight(200);

        if (files != null && files.length > 0) {
            for (File f : files) {
                CheckBox cb = new CheckBox(f.getName());
                checkBoxContainer.getChildren().add(cb);
            }
        } else {
            checkBoxContainer.getChildren().add(new Label("(No .txt result files found in this folder)"));
            btnEnter.setDisable(true);
        }

        HBox bottom = new HBox(10, btnEnter, infoLabel);
        bottom.setAlignment(Pos.CENTER_LEFT);

        box.getChildren().addAll(scrollPane, bottom, comparisonArea);

        btnEnter.setOnAction(e -> {
            List<String> selectedNames = new ArrayList<>();
            for (javafx.scene.Node n : checkBoxContainer.getChildren()) {
                if (n instanceof CheckBox cb && cb.isSelected()) {
                    selectedNames.add(cb.getText());
                }
            }

            if (selectedNames.size() != 2) {
                statusBox.setText("Please select exactly TWO result files to compare.");
                comparisonArea.setText("You must select exactly two result files (no more, no less).");
                return;
            }

            File file1 = new File(currentResultsFolder, selectedNames.get(0));
            File file2 = new File(currentResultsFolder, selectedNames.get(1));

            try {
                String content1 = Files.readString(file1.toPath());
                String content2 = Files.readString(file2.toPath());

                comparisonArea.setText(
                        "===== " + file1.getName() + " =====\n" +
                        content1 +
                        "\n\n===== " + file2.getName() + " =====\n" +
                        content2
                );
                statusBox.setText("Compared result files: " +
                        selectedNames.get(0) + " and " + selectedNames.get(1));
            } catch (Exception ex) {
                comparisonArea.setText("Error reading one of the files:\n" + ex.getMessage());
                statusBox.setText("Error comparing files.");
            }
        });

        popup.setTitle("Compare Result Files");
        popup.setScene(new Scene(box, 650, 430));
        popup.showAndWait();
    }

    /**
     * Helper to update the exec info label text based on currentRootFolder.
     */
    private void updateExecInfoLabel(Label infoLabel) {
        if (currentRootFolder == null || currentRootFolder.trim().isEmpty()) {
            infoLabel.setText("No root folder set. Use 'Set Root Folder' on the main window (without quotations).");
        } else {
            infoLabel.setText("Executing using root folder:\n" + currentRootFolder + "  (set without quotations).");
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
