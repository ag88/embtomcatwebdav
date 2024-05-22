package io.github.ag88.embtomcatwebdav.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.App;
import io.github.ag88.embtomcatwebdav.ImagePanel;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.util.QRUtil;
import io.nayuki.qrcodegen.QrCode;

public class Gui extends JFrame implements ActionListener, WindowListener, ListSelectionListener {

	Log log = LogFactory.getLog(Gui.class);

	ImagePanel pi;
	JTextField tfurl;
	JList<String> jlalias;
	JLabel jlmsg;

	boolean systraysup = false;

	public Gui() throws HeadlessException {
		setTitle("embtomcatwebdav");

		systraysup = SystemTray.isSupported();

		if (systraysup)
			setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		else
			setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		setPreferredSize(new Dimension(800, 600));

		creategui();
	}

	private void creategui() {

		addWindowListener(this);

		JMenuBar mbar = new JMenuBar();
		JMenu mFile = new JMenu("File");
		mFile.setMnemonic(KeyEvent.VK_F);
		mFile.add(Util.addmenuitem("Close", "CLOSE", KeyEvent.VK_C, this));
		mFile.add(Util.addmenuitem("Exit", "EXIT", KeyEvent.VK_E, this));
		mbar.add(mFile);
		JMenu mView = new JMenu("View");
		mView.setMnemonic(KeyEvent.VK_V);
		mView.add(Util.addmenuitem("Home", "HOME", KeyEvent.VK_H, this));
		mbar.add(mView);
		JMenu mAbout = new JMenu("About");
		mView.setMnemonic(KeyEvent.VK_A);
		mAbout.add(Util.addmenuitem("About", "ABOUT", KeyEvent.VK_U, this));
		mbar.add(mAbout);

		setJMenuBar(mbar);

		getContentPane().setLayout(new BorderLayout());

		JToolBar mToolbar = new JToolBar();
		mToolbar.add(Util.makeNavigationButton("Home24.gif", "HOME", "Open", "Open", this));
		getContentPane().add(mToolbar, BorderLayout.NORTH);

		Tomcat tomcat = App.getInstance().getWdav().getTomcat();
		String hostname = tomcat.getHost().getName();
		if (!hostname.equals("0.0.0.0")) {

			BufferedImage image;
			String url = geturl(hostname);
			if (url != null)
				image = genQR(url);
			else
				image = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
			pi = new ImagePanel(image);
			pi.setAlignmentX(LEFT_ALIGNMENT);
			p.add(pi);
			JPanel l = new JPanel(new FlowLayout(FlowLayout.LEADING));
			l.setAlignmentX(LEFT_ALIGNMENT);
			l.add(new JLabel("url:"));
			// JTextField tfurl = new JTextField(40);
			// tfurl.setText(url.toString());
			// l.add(tfurl);
			if (url != null)
				l.add(Util.URLJLabel(url, url));			
			p.add(l);
			JButton btncopy = new JButton("copy URL to clipboard");
			btncopy.setAlignmentX(LEFT_ALIGNMENT);
			btncopy.setActionCommand("URLCLP");
			btncopy.addActionListener(this);
			p.add(btncopy);
			p.add(Box.createVerticalGlue());
			getContentPane().add(p, BorderLayout.CENTER);

		} else {
			// all interfaces
			String[] aliases = App.getInstance().getWdav().getTomcat().getHost().findAliases();

			int isel = 0;
			Preferences pref = App.getInstance().getPreferences();
			isel = pref.getInt("gui.aliaslist.sel", -1);
			if (isel < 0) {
				for (int i = 0; i < aliases.length; i++) {
					if (aliases[i].equals("localhost") || aliases[i].equals("127.0.0.1"))
						continue;
					else {
						isel = i;
						break;
					}
				}
			}
			String alias = aliases[isel];

			jlalias = new JList<String>(aliases);
			jlalias.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jlalias.setSelectedIndex(isel);

			String url = geturl(alias);
			BufferedImage image;
			if (url != null)
				image = genQR(url);
			else
				image = new BufferedImage(100, 100, BufferedImage.TYPE_BYTE_GRAY);
			JPanel p = new JPanel();
			p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
			pi = new ImagePanel(image);
			p.add(pi);
			JPanel l = new JPanel(new FlowLayout(FlowLayout.LEADING));
			l.add(new JLabel("url:"));
			tfurl = new JTextField(40);
			if( url != null)
				tfurl.setText(url);
			l.add(tfurl);
			// l.add(Util.URLJLabel(url.toString(), url.toString()));
			p.add(l);
			JScrollPane jsp = new JScrollPane(jlalias);
			p.add(jsp);
			getContentPane().add(p, BorderLayout.CENTER);

			jlalias.addListSelectionListener(this);

		}
		jlmsg = new JLabel();

		getContentPane().add(jlmsg, BorderLayout.SOUTH);

	}

	public String geturl(String hostname) {
		Tomcat tomcat = App.getInstance().getWdav().getTomcat();
		int port = tomcat.getConnector().getPort();

		String urlprefix = (String) OptFactory.getInstance().getOpt("urlprefix").getValue();
		boolean secure = tomcat.getConnector().getSecure();

		try {
			URL url = new URL(secure ? "https" : "http", hostname, port, urlprefix);
			return url.toString();
		} catch (MalformedURLException e) {
			log.error("invalid url: " + (secure ? "https://" : "http://") + hostname + ':' + port + '/' + urlprefix);
			return null;
		}
	}

	public BufferedImage genQR(String text) {
		// Simple operation
		QrCode qr = QrCode.encodeText(text, QrCode.Ecc.LOW);
		QRUtil util = new QRUtil();
		BufferedImage img = util.toImage(qr, 6, 8); // See QrCodeGeneratorDemo

		/*
		 * // Manual operation List<QrSegment> segs =
		 * QrSegment.makeSegments("3141592653589793238462643383"); QrCode qr1 =
		 * QrCode.encodeSegments(segs, QrCode.Ecc.HIGH, 5, 5, 2, false); for (int y = 0;
		 * y < qr1.size; y++) { for (int x = 0; x < qr1.size; x++) { (... paint
		 * qr1.getModule(x, y) ...) } }
		 */
		return img;
	}

	private void dourlclip() {
		Tomcat tomcat = App.getInstance().getWdav().getTomcat();
		String hostname = tomcat.getHost().getName();
		String url = geturl(hostname);
		if (url != null) {
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			StringSelection stringSelection = new StringSelection(url);
			clipboard.setContents(stringSelection, null);
			jlmsg.setText("url copied to clipboard");
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
					}
					jlmsg.setText("");
				}				
			});
			t.start();
		}
	}

	public void doshutdown() {
		int ret = JOptionPane.showConfirmDialog(this, "Stop embtomcatwebdave server?", "stop server",
				JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			try {
				App.getInstance().getWdav().getTomcat().stop();
			} catch (LifecycleException e1) {
				log.error(e1);
				System.exit(1);
			}
			if (SystemTray.isSupported()) {
				TrayIcon[] trayicons = SystemTray.getSystemTray().getTrayIcons();
				for (TrayIcon icon : trayicons)
					SystemTray.getSystemTray().remove(icon);
			}
			dispose();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("HOME")) {

		} else if (e.getActionCommand().equals("URLCLP")) {
			dourlclip();
		} else if (e.getActionCommand().equals("EXIT")) {
			doshutdown();
		} else if (e.getActionCommand().equals("CLOSE")) {
			if (systraysup)
				dispose();
			else
				setExtendedState(JFrame.ICONIFIED);
		} else if (e.getActionCommand().equals("ABOUT")) {
			doabout();
		}
	}

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// int i = e.getFirstIndex();
		if (e.getValueIsAdjusting())
			return;
		int i = jlalias.getSelectionModel().getMinSelectionIndex();

		String alias = (String) jlalias.getModel().getElementAt(i);
		Tomcat tomcat = App.getInstance().getWdav().getTomcat();
		int port = tomcat.getConnector().getPort();
		String urlprefix = (String) OptFactory.getInstance().getOpt("urlprefix").getValue();
		boolean secure = tomcat.getConnector().getSecure();
		String hostname = tomcat.getHost().getName();
		try {
			URL url = new URL(secure ? "https" : "http", alias, port, urlprefix);
			BufferedImage image = genQR(url.toString());
			pi.setImage(image);
			tfurl.setText(url.toString());
			pi.invalidate();
			pi.repaint();
		} catch (MalformedURLException e1) {
			log.error("invalid url: " + (secure ? "https://" : "http://") + hostname + ':' + port + '/' + urlprefix);
		}

	}

	private void doabout() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(Box.createVerticalStrut(100));
		p.add(new JLabel("Copyright (C) Andrew Goh 2023"));
		// p.add(new JLabel("MIT Licensed"));
		String url = "https://github.com/ag88/embtomcatwebdav";
		p.add(Util.URLJLabel(url, url));
		JOptionPane.showMessageDialog(this, p);
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		if (jlalias != null) {
			int sel = jlalias.getSelectionModel().getMinSelectionIndex();
			if (sel >= 0) {
				Preferences pref = App.getInstance().getPreferences();
				pref.putInt("gui.aliaslist.sel", sel);
			}
		}

		if (!systraysup)
			setExtendedState(JFrame.ICONIFIED);

	}

	@Override
	public void windowClosed(WindowEvent e) {

	}

	@Override
	public void windowIconified(WindowEvent e) {

	}

	@Override
	public void windowDeiconified(WindowEvent e) {

	}

	@Override
	public void windowActivated(WindowEvent e) {

	}

	@Override
	public void windowDeactivated(WindowEvent e) {

	}

}
