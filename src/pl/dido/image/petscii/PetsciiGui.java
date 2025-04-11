package pl.dido.image.petscii;

import java.awt.Canvas;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import pl.dido.image.GuiUtils;
import pl.dido.image.utils.ImageCanvas;

public class PetsciiGui {

	public static JPanel petsciiTab(final PetsciiConfig config) {
		final JPanel petsciiC64 = new JPanel();
		petsciiC64.setLayout(null);
		GuiUtils.addDASControls(petsciiC64, config);

		final JLabel lblConvertLabel = new JLabel("Detection threshold:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 136, 169, 14);
		petsciiC64.add(lblConvertLabel);
		
		final JSlider sldDetect = new JSlider(JSlider.HORIZONTAL, 0, 15, config.nn_threshold);
		sldDetect.setBounds(40, 156, 200, 35);
		sldDetect.setFont(GuiUtils.std);
		sldDetect.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.nn_threshold = source.getValue();
			}
		});
		
		sldDetect.setMajorTickSpacing(2);
		sldDetect.setPaintLabels(true);
		petsciiC64.add(sldDetect);

		final Canvas c64Logo = new ImageCanvas("c64.png");
		c64Logo.setBounds(381, 7, 100, 96);
		
		petsciiC64.add(c64Logo);
		
		GuiUtils.addContrastControls(petsciiC64, config);
		GuiUtils.addColorControls(petsciiC64, config);
		GuiUtils.addFiltersControls(petsciiC64, config);

		return petsciiC64;
	}
}