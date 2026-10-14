package com.wac.autocore.gui.launcher;

import javafx.application.Platform;
import javafx.scene.control.TextArea;
import java.io.OutputStream;
import java.io.PrintStream;

public class ConsoleRedirector extends OutputStream {
    private final TextArea outputArea;

    public ConsoleRedirector(TextArea outputArea) {
        this.outputArea = outputArea;
    }

    @Override
    public void write(int b) {
        // Omvandlar byte för byte till tecken och skickar till UI-tråden
        Platform.runLater(() -> outputArea.appendText(String.valueOf((char) b)));
    }

    public static void redirectTo(TextArea outputArea) {
        PrintStream stream = new PrintStream(new ConsoleRedirector(outputArea), true);
        System.setOut(stream); // Ändrar globala System.out
        System.setErr(stream); // Ändrar globala System.err
    }
}