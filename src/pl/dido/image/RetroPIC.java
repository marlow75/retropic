package pl.dido.image;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import pl.dido.image.amiga.Amiga1200Config;
import pl.dido.image.amiga.Amiga1200Gui;
import pl.dido.image.amiga.Amiga1200Renderer;
import pl.dido.image.amiga.Amiga500Config;
import pl.dido.image.amiga.Amiga500Gui;
import pl.dido.image.amiga.Amiga500Renderer;
import pl.dido.image.atari.STConfig;
import pl.dido.image.atari.STGui;
import pl.dido.image.atari.STRenderer;
import pl.dido.image.c64.C64Config;
import pl.dido.image.c64.C64Gui;
import pl.dido.image.c64.C64Renderer;
import pl.dido.image.cpc.CPCConfig;
import pl.dido.image.cpc.CPCGui;
import pl.dido.image.cpc.CPCRenderer;
import pl.dido.image.utils.Utils;
import pl.dido.image.zx.ZXConfig;
import pl.dido.image.zx.ZXGui;
import pl.dido.image.zx.ZXSpectrumRenderer;

public class RetroPIC {

	protected JFrame frame;

	protected C64Config c64Config = new C64Config();
	protected ZXConfig zxConfig = new ZXConfig();

	protected CPCConfig cpcConfig = new CPCConfig();
	protected STConfig stConfig = new STConfig();

	protected Amiga500Config amiga500Config = new Amiga500Config();
	protected Amiga1200Config amiga1200Config = new Amiga1200Config();

	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					final RetroPIC window = new RetroPIC();
					window.frame.setVisible(true);
					window.frame.setLocationRelativeTo(null);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public RetroPIC() {
		frame = new JFrame("RetroPIC");
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.getResourceAsURL("retro.png")));
		frame.setResizable(false);
		frame.setBounds(0, 0, 510, 460);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(new Font("Tahoma", Font.PLAIN, 16));

		final Button btnLoad = new Button("Load file...");

		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("Commodore 64", null, C64Gui.c64Tab(c64Config), null);
		tabbedPane.addTab("ZX Spectrum 48/+", null, ZXGui.zxTab(zxConfig), null);
		tabbedPane.addTab("Amstrad CPC", null, CPCGui.cpcTab(cpcConfig), null);
		tabbedPane.addTab("Atari ST", null, STGui.stTab(stConfig), null);
		tabbedPane.addTab("Amiga 500", null, Amiga500Gui.amigaTab(amiga500Config), null);
		tabbedPane.addTab("Amiga 1200", null, Amiga1200Gui.amigaTab(amiga1200Config), null);
		tabbedPane.addTab("About", null, AboutGui.aboutTab(), null);
		
		tabbedPane.addChangeListener(new ChangeListener() {
			public void stateChanged(final ChangeEvent changeEvent) {
				final JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
				final int index = sourceTabbedPane.getSelectedIndex();
				btnLoad.setVisible(!"About".equals(tabbedPane.getTitleAt(index)));
			}
		});

		btnLoad.setBackground(new Color(0, 128, 128));
		btnLoad.setFont(new Font("Dialog", Font.BOLD, 12));
		btnLoad.setForeground(new Color(255, 255, 255));
		btnLoad.setPreferredSize(new Dimension(143, 34));
		btnLoad.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser fc = new JFileChooser(Config.default_path);
				final FileFilter filter = new FileNameExtensionFilter("Choose picture", "jpg");

				fc.setFileFilter(filter);
				final int returnVal = fc.showOpenDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					loadImage(fc.getSelectedFile(), tabbedPane.getSelectedIndex());
					Config.default_path = fc.getSelectedFile().getAbsolutePath();
				}
			}
		});

		final Button btnClose = new Button("Close");
		btnClose.setBackground(new Color(128, 0, 64));
		btnClose.setForeground(SystemColor.text);
		btnClose.setPreferredSize(new Dimension(67, 34));

		final JPanel buttonsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.LINE_AXIS));
		buttonsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		buttonsPanel.add(Box.createHorizontalGlue());
		buttonsPanel.add(btnLoad);
		buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonsPanel.add(btnClose);

		frame.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

		btnClose.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});

		new DropTarget(frame, new DropTargetListener() {
			
			@Override
			public void drop(final DropTargetDropEvent event) {
				event.acceptDrop(DnDConstants.ACTION_COPY);
				
				final Transferable transferable = event.getTransferable();
				final DataFlavor[] flavors = transferable.getTransferDataFlavors();

				// Loop through the flavors - dragged objects
				for (final DataFlavor flavor : flavors) {

					try {
						if (flavor.isFlavorJavaFileListType()) {
							@SuppressWarnings("unchecked")
							final List<File> files = (List<File>) transferable.getTransferData(flavor);
							loadImage(files.get(0), tabbedPane.getSelectedIndex());
						}
					} catch (final Exception e) {
						// nothing
					}
				}

				event.dropComplete(true);
			}

			@Override
			public void dragEnter(final DropTargetDragEvent dtde) {
				// nothing		
			}

			@Override
			public void dragExit(final DropTargetEvent dte) {
				// nothing		
			}

			@Override
			public void dragOver(final DropTargetDragEvent dtde) {
				// nothing		
			}

			@Override
			public void dropActionChanged(final DropTargetDragEvent dtde) {
				// nothing		
			}		
		});
	}

	public void loadImage(final File selectedFile, final int selectedTab) {
		try {
			final BufferedImage img = ImageIO.read(selectedFile);

			switch (img.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
			case BufferedImage.TYPE_INT_RGB:
				switch (selectedTab) {
				case 0:
					new C64Renderer(img, selectedFile.getName(), c64Config).start();
					break;
				case 1:
					new ZXSpectrumRenderer(img, selectedFile.getName(), zxConfig).start();
					break;
				case 2:
					new CPCRenderer(img, selectedFile.getName(), cpcConfig).start();
					break;
				case 3:
					new STRenderer(img, selectedFile.getName(), stConfig).start();
					break;
				case 4:
					new Amiga500Renderer(img, selectedFile.getName(), amiga500Config).start();
					break;
				case 5:
					new Amiga1200Renderer(img, selectedFile.getName(), amiga1200Config).start();
					break;
				}
				break;
			default:
				JOptionPane.showMessageDialog(null, "ERROR", "Unsupported pixel format !!!", JOptionPane.ERROR_MESSAGE);
			}

		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, "ERROR", "Can't read selected file !!!", JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
		}
	}
}