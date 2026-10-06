package online.banking.system.util;

import java.util.List;
import java.util.function.Function;
import javax.swing.table.DefaultTableModel;

/**
 * Converts a List of ANY type into a read-only JTable model.
 * <T> makes this a GENERIC METHOD: it works for List<Transaction>,
 * List<AccountSummary>, etc. The mapper says how one object becomes one row.
 */
public final class TableModelBuilder {

    private TableModelBuilder() {
    }

    public static <T> DefaultTableModel build(String[] columns, List<T> rows,
                                              Function<T, Object[]> mapper) {
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (T item : rows) {
            model.addRow(mapper.apply(item));
        }
        return model;
    }
}
