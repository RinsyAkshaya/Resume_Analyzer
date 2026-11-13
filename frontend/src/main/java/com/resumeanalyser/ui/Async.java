package com.resumeanalyser.ui;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/** Runs backend calls off the Swing event thread (SwingWorker) so the UI never freezes. */
public final class Async {

    private Async() { }

    public static <T> void run(Component owner, Callable<T> task, Consumer<T> onSuccess) {
        run(owner, task, onSuccess, ex -> UI.error(owner, ex.getMessage()));
    }

    public static <T> void run(Component owner, Callable<T> task, Consumer<T> onSuccess, Consumer<Exception> onError) {
        if (owner != null) owner.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                if (owner != null) owner.setCursor(Cursor.getDefaultCursor());
                try {
                    onSuccess.accept(get());
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    onError.accept(cause instanceof Exception ? (Exception) cause : e);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }.execute();
    }
}
