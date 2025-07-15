package pl.dido.image.utils;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JList;

public class JExComboBox extends JComboBox<String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6631304195874470401L;
	private boolean firingActionEvent = false;
	
	private boolean enabled[];
	private int oldIndex;

	public JExComboBox(final String[] items, final boolean enabled[]) {
		super(items);

		this.enabled = enabled;
		this.setRenderer(new DefaultListCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getListCellRendererComponent(final JList<?> list, Object value, int index,
					boolean isSelected, boolean cellHasFocus) {
				final Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (index >= 0 && !enabled[index])
					c.setForeground(Color.GRAY);
				else
					c.setForeground(Color.BLACK);

				return c;
			}
		});
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void fireActionEvent() {
		final int idx = getSelectedIndex();
		if (idx >= 0 && !enabled[idx]) {
			setSelectedIndex(oldIndex);
			return;
		}
		
		oldIndex = idx;
		if (!firingActionEvent) {
			// Set flag to ensure that an infinite loop is not created
			firingActionEvent = true;
			ActionEvent e = null;
			
			// Guaranteed to return a non-null array
			Object[] listeners = listenerList.getListenerList();
			long mostRecentEventTime = EventQueue.getMostRecentEventTime();
			int modifiers = 0;
			
			AWTEvent currentEvent = EventQueue.getCurrentEvent();
			if (currentEvent instanceof InputEvent)
				modifiers = ((InputEvent) currentEvent).getModifiers();
			else if (currentEvent instanceof ActionEvent)
				modifiers = ((ActionEvent) currentEvent).getModifiers();
			
			try {
				for (int i = listeners.length - 2; i >= 0; i -= 2) {
					if (listeners[i] == ActionListener.class) {
						if (e == null)
							e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getActionCommand(),
									mostRecentEventTime, modifiers);
						((ActionListener) listeners[i + 1]).actionPerformed(e);
					}
				}
			} finally {
				firingActionEvent = false;
			}
		}
	}
}
