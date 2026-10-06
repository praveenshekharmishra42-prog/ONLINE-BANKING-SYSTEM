package online.banking.system.util;

import java.awt.Component;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import javax.swing.SwingWorker;

/**
 * Runs slow work (database calls) on a BACKGROUND THREAD so windows never freeze.
 *
 *   doInBackground() -> runs on a worker thread      (database query)
 *   done()           -> runs on the Swing UI thread  (update labels/tables)
 *
 * Swing components must only be touched from the UI thread (Event Dispatch
 * Thread); SwingWorker takes care of switching threads for us.
 * <T> is the result type, so one helper works for every kind of result.
 */
public final class BackgroundTask {

    private BackgroundTask() {
    }

    public static <T> void run(Component parent, Callable<T> task, Consumer<T> onSuccess) {
        run(parent, task, onSuccess, null);
    }

    /** onFinally (may be null) always runs afterwards on the UI thread, e.g. to re-enable a button. */
    public static <T> void run(Component parent, Callable<T> task, Consumer<T> onSuccess, Runnable onFinally) {
        new SwingWorker<T, Void>() {
            @Override
            protected T doInBackground() throws Exception {
                return task.call();
            }

            @Override
            protected void done() {
                try {
                    onSuccess.accept(get());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException e) {
                    MessageUtil.error(parent, e.getCause());
                } finally {
                    if (onFinally != null) {
                        onFinally.run();
                    }
                }
            }
        }.execute();
    }
}
