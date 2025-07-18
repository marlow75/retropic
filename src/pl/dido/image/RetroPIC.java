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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import pl.dido.image.amiga.Amiga1200Runner;
import pl.dido.image.amiga.Amiga500Config;
import pl.dido.image.amiga.Amiga500Gui;
import pl.dido.image.amiga.Amiga500Renderer;
import pl.dido.image.amiga.Amiga500Runner;
import pl.dido.image.atari.STConfig;
import pl.dido.image.atari.STGui;
import pl.dido.image.atari.STRenderer;
import pl.dido.image.atari.STRunner;
import pl.dido.image.c64.C64Config;
import pl.dido.image.c64.C64ExtraConfig;
import pl.dido.image.c64.C64ExtraGui;
import pl.dido.image.c64.C64ExtraRenderer;
import pl.dido.image.c64.C64ExtraRunner;
import pl.dido.image.c64.C64Gui;
import pl.dido.image.c64.C64Renderer;
import pl.dido.image.c64.C64Runner;
import pl.dido.image.cpc.CPCConfig;
import pl.dido.image.cpc.CPCGui;
import pl.dido.image.cpc.CPCRenderer;
import pl.dido.image.cpc.CPCRunner;
import pl.dido.image.pc.PCGui;
import pl.dido.image.pc.PCRenderer;
import pl.dido.image.pc.PCRunner;
import pl.dido.image.pc.PCConfig;
import pl.dido.image.petscii.PetsciiConfig;
import pl.dido.image.petscii.PetsciiGui;
import pl.dido.image.petscii.PetsciiRenderer;
import pl.dido.image.petscii.PetsciiRunner;
import pl.dido.image.plus4.Plus4Config;
import pl.dido.image.plus4.Plus4ExtraConfig;
import pl.dido.image.plus4.Plus4ExtraGui;
import pl.dido.image.plus4.Plus4ExtraRenderer;
import pl.dido.image.plus4.Plus4ExtraRunner;
import pl.dido.image.plus4.Plus4Gui;
import pl.dido.image.plus4.Plus4Renderer;
import pl.dido.image.plus4.Plus4Runner;
import pl.dido.image.utils.Gfx;
import pl.dido.image.utils.Utils;
import pl.dido.image.vic20.Vic20Config;
import pl.dido.image.vic20.Vic20Gui;
import pl.dido.image.vic20.Vic20Renderer;
import pl.dido.image.vic20.Vic20Runner;
import pl.dido.image.zx.ZXConfig;
import pl.dido.image.zx.ZXGui;
import pl.dido.image.zx.ZXRenderer;
import pl.dido.image.zx.ZXRunner;

public class RetroPIC {

	protected JFrame frame;
	protected String default_path;

	protected PetsciiConfig petsciiConfig;
	protected C64ExtraConfig c64ExtraConfig;

	protected C64Config c64Config;
	protected ZXConfig zxConfig;

	protected CPCConfig cpcConfig;
	protected STConfig stConfig;
	
	protected PCConfig cgaConfig;
	protected Vic20Config vic20Config;
	
	protected Plus4Config plus4Config;
	protected Plus4ExtraConfig plus4ExtraConfig;

	protected Amiga500Config amiga500Config;
	protected Amiga1200Config amiga1200Config;

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
		petsciiConfig = new PetsciiConfig();
		c64ExtraConfig = new C64ExtraConfig();

		c64Config = new C64Config();
		zxConfig = new ZXConfig();

		cpcConfig = new CPCConfig();
		stConfig = new STConfig();
		
		plus4Config = new Plus4Config();
		plus4ExtraConfig = new Plus4ExtraConfig();

		amiga500Config = new Amiga500Config();
		amiga1200Config = new Amiga1200Config();
		
		cgaConfig = new PCConfig();
		vic20Config = new Vic20Config();

		frame = new JFrame("RetroPIC");
		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(Utils.getResourceAsURL("retro.png")));
		frame.setResizable(false);
		frame.setBounds(0, 0, 510, 550);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setFont(GuiUtils.title);

		final Button btnLoad = new Button("Load file...");

		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		tabbedPane.addTab("Commodore 64", null, C64Gui.c64Tab(c64Config), null);
		tabbedPane.addTab("C64 PETSCII", null, PetsciiGui.petsciiTab(petsciiConfig), null);
		tabbedPane.addTab("Commodore Plus4", null, Plus4Gui.plus4Tab(plus4Config), null);
		tabbedPane.addTab("Commodore VIC-20", null, Vic20Gui.vic20Tab(vic20Config), null);
		tabbedPane.addTab("ZX 48/+", null, ZXGui.zxTab(zxConfig), null);
		tabbedPane.addTab("Amstrad CPC", null, CPCGui.cpcTab(cpcConfig), null);
		tabbedPane.addTab("Atari ST", null, STGui.stTab(stConfig), null);
		tabbedPane.addTab("Amiga 500", null, Amiga500Gui.amigaTab(amiga500Config), null);
		tabbedPane.addTab("Amiga 1200", null, Amiga1200Gui.amigaTab(amiga1200Config), null);
		tabbedPane.addTab("Commodore 64 extra", null, C64ExtraGui.c64Extra(c64ExtraConfig), null);
		tabbedPane.addTab("Commodore Plus4 extra", null, Plus4ExtraGui.plus4ExtraTab(plus4ExtraConfig), null);
		tabbedPane.addTab("PC ASCII", null, PCGui.pcTab(cgaConfig), null);
		tabbedPane.addTab("About", null, AboutGui.aboutTab("aboutRetroPIC.htm"), null);

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
				final JFileChooser fc = new JFileChooser(default_path);
				final FileFilter filter = new FileNameExtensionFilter("Choose picture", "jpg");

				fc.setFileFilter(filter);
				final int returnVal = fc.showOpenDialog(frame);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					loadImage(fc.getSelectedFile(), tabbedPane.getSelectedIndex());
					default_path = fc.getSelectedFile().getAbsolutePath();
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
		
		frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(final ComponentEvent e) {
                frame.repaint();
            }
        });

		new DropTarget(frame, new DropTargetListener() {
			@Override
			public void drop(final DropTargetDropEvent event) {
				event.acceptDrop(DnDConstants.ACTION_COPY);

				final Transferable transferable = event.getTransferable();
				final DataFlavor[] flavors = transferable.getTransferDataFlavors();

				// loop through the flavors - dragged objects
				for (final DataFlavor flavor : flavors) {
					try {
						if (flavor.isFlavorJavaFileListType()) {
							@SuppressWarnings("unchecked")
							final List<File> files = (List<File>) transferable.getTransferData(flavor);
							loadImage(files.get(0), tabbedPane.getSelectedIndex());
							default_path = files.get(0).getAbsolutePath();
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
			BufferedImage image = ImageIO.read(selectedFile);
			
			switch (image.getType()) {
			case BufferedImage.TYPE_3BYTE_BGR:
				break;
			case BufferedImage.TYPE_BYTE_GRAY:
				image = Gfx.grey2BGR(image);
				break;
			case BufferedImage.TYPE_INT_RGB:
				image = Gfx.rgb2BGR(image);
				break;
			default: 
				throw new IOException("Unsupported pixel format !!!");
			}
			
			final String fileName = selectedFile.getName();

			switch (selectedTab) {
			case 0:
				new Thread(new C64Runner(new C64Renderer(image, c64Config), fileName)).start();
				break;
			case 1:
				new Thread(new PetsciiRunner(new PetsciiRenderer(image, petsciiConfig), fileName)).start();
				break;
			case 2:
				new Thread(new Plus4Runner(new Plus4Renderer(image, plus4Config), fileName)).start();
				break;
			case 3:
				new Thread(new Vic20Runner(new Vic20Renderer(image, vic20Config), fileName)).start();
				break;
			case 4:
				new Thread(new ZXRunner(new ZXRenderer(image, zxConfig), fileName)).start();
				break;
			case 5:
				new Thread(new CPCRunner(new CPCRenderer(image, cpcConfig), fileName)).start();
				break;
			case 6:
				new Thread(new STRunner(new STRenderer(image, stConfig), fileName)).start();
				break;
			case 7:
				new Thread(new Amiga500Runner(new Amiga500Renderer(image, amiga500Config), fileName)).start();
				break;
			case 8:
				new Thread(new Amiga1200Runner(new Amiga1200Renderer(image, amiga1200Config), fileName)).start();
				break;
			case 9:
				new Thread(new C64ExtraRunner(new C64ExtraRenderer(image, c64ExtraConfig), fileName)).start();
				break;
			case 10:
				new Thread(new Plus4ExtraRunner(new Plus4ExtraRenderer(image, plus4ExtraConfig), fileName)).start();
				break;
			case 11:
				new Thread(new PCRunner(new PCRenderer(image, cgaConfig), fileName)).start();
				break;
			}

		} catch (final IOException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Can't read selected file !!!", JOptionPane.ERROR_MESSAGE);
		}
	}
}