package org.example.lsw_proto2.app;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.example.lsw_proto2.io.*;

public abstract class ConsoleGameScene {
    protected final VBox root;
    protected final GUIInputService inputService;
    protected final GUIOutputService outputService;

    public ConsoleGameScene(Node topBar, String bottomBarColor) {
        // -----------------------------------------------------------------------
        // |                              Console                                |
        // -----------------------------------------------------------------------
        TextArea console = new TextArea();
        console.setEditable(false);
        console.setWrapText(true);
        console.setFont(Font.font("Monospaced", 13));
        console.setStyle("""
            -fx-control-inner-background: #1a1a1a;
            -fx-text-fill: #d0d0d0;
            -fx-border-color: transparent;
        """);
        VBox.setVgrow(console, Priority.ALWAYS);

        // -----------------------------------------------------------------------
        // |                                 Input row                           |
        // -----------------------------------------------------------------------
        TextField inputField = new TextField();
        inputField.setPromptText("Type a command and press Enter...");
        inputField.setFont(Font.font("Monospaced", 13));
        inputField.setStyle("""
            -fx-background-color: #2a2a2a;
            -fx-text-fill: #e0e0e0;
            -fx-prompt-text-fill: #555555;
            -fx-border-color: #444444;
            -fx-border-radius: 5;
            -fx-background-radius: 5;
        """);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        Button submitBtn = buildSubmitButton();

        HBox bottomBar = new HBox(8, inputField, submitBtn);
        bottomBar.setAlignment(Pos.CENTER_LEFT);
        bottomBar.setPadding(new Insets(10, 16, 10, 16));
        bottomBar.setStyle("-fx-background-color: " + bottomBarColor + ";");

        // -----------------------------------------------------------------------
        // |                       Submit handler                                |
        // -----------------------------------------------------------------------
        inputService  = new GUIInputService();
        outputService = new GUIOutputService(console);

        Runnable submit = () -> {
            String line = inputField.getText();
            if (line.isBlank()) return;
            inputField.clear();
            console.appendText("> " + line + "\n");
            inputService.submitInput(line);
        };
        submitBtn.setOnAction(e -> submit.run());
        inputField.setOnKeyPressed(e -> {if (e.getCode() == KeyCode.ENTER) submit.run();});

        // allow subclasses to add extra nodes to the bottom bar (i.e, GameScene's back button)
        buildExtraBottomNodes(bottomBar, inputField, submitBtn, console);

        // -----------------------------------------------------------------------
        // |                              Root                                   |
        // -----------------------------------------------------------------------
        root = new VBox(topBar, console, bottomBar);
        root.setStyle("-fx-background-color: #1a1a1a;");
    }

    protected abstract Button buildSubmitButton();

    protected void buildExtraBottomNodes(HBox bottomBar, TextField inputField, Button submitBtn, TextArea console) {}

    public VBox getRoot() {return root;}
}