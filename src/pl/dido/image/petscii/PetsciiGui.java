package pl.dido.image.petscii;

import java.awt.Canvas;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
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
		GuiUtils.addDASControls(petsciiC64, config, new boolean[] { true, true, true, true, false, false, false, false, false });

		final JLabel lblConvertLabel = new JLabel("lowpass threshold:");
		lblConvertLabel.setFont(GuiUtils.bold);
		lblConvertLabel.setBounds(20, 136, 169, 14);
		petsciiC64.add(lblConvertLabel);
		
		final JSlider sldDetect = new JSlider(JSlider.HORIZONTAL, 0, 4, (int)config.lowpass_gain);
		sldDetect.setBounds(40, 156, 100, 35);
		sldDetect.setFont(GuiUtils.std);
		sldDetect.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent e) {
				final JSlider source = (JSlider) e.getSource();

				if (!source.getValueIsAdjusting())
					config.lowpass_gain = 1.2f * source.getValue();
			}
		});
		
		sldDetect.setMajorTickSpacing(2);
		sldDetect.setPaintLabels(true);
		petsciiC64.add(sldDetect);
		
		final JCheckBox chckbxDenoiseCheckBox = new JCheckBox("denoising filter");
		chckbxDenoiseCheckBox.setToolTipText("Neural net denoise filter (autoencoder)");
		chckbxDenoiseCheckBox.setFont(GuiUtils.std);
		chckbxDenoiseCheckBox.setBounds(150, 156, 150, 20);
		chckbxDenoiseCheckBox.setSelected(config.denoise);

		chckbxDenoiseCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				config.denoise = !config.denoise;
			}
		});

		petsciiC64.add(chckbxDenoiseCheckBox);

		final Canvas c64Logo = new ImageCanvas("c64.png");
		c64Logo.setBounds(381, 7, 100, 96);
		
		petsciiC64.add(c64Logo);
		
		GuiUtils.addContrastControls(petsciiC64, config);
		GuiUtils.addColorControls(petsciiC64, config);
		GuiUtils.addFiltersControls(petsciiC64, config);

		return petsciiC64;
	}
}