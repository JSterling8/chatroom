package views;

import java.awt.Color;
import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

/**
 * A JTable extension for easily colouring table rows
 * 
 * @author Jonathan Stering
 *
 */
public class ColoredTable extends JTable {
	private static final long serialVersionUID = -1118415077892936053L;

	private Map<Integer, Color> rowColors = new HashMap<Integer, Color>();

	public ColoredTable(TableModel model) {
		super(model);
	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		Component c = super.prepareRenderer(renderer, row, column);

		if (!isRowSelected(row)) {
			Color color = rowColors.get(row);
			c.setBackground(color == null ? getBackground() : color);
		}

		return c;
	}

	public void setRowColor(int row, Color color) {
		rowColors.put(row, color);
	}
}