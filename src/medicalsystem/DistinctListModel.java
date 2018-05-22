package medicalsystem;

import java.util.Collection;
import java.util.LinkedHashSet;

import javax.swing.DefaultListModel;

public class DistinctListModel<E> extends DefaultListModel<E> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -2529197014684370615L;

	public DistinctListModel(Collection<E> data) {
        for (E e: new LinkedHashSet<E>(data)) {
            addElement(e);
        }
    }
}