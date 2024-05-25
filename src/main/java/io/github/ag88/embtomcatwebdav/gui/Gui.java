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
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.prefs.Preferences;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
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
import io.github.ag88.embtomcatwebdav.opt.OptFactory;
import io.github.ag88.embtomcatwebdav.util.QRUtil;
import io.nayuki.qrcodegen.QrCode;

/**
 * Class Gui - the QR Code Gui
 * 
 */
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
		if(!(Boolean)OptFactory.getInstance().getOpt("systray").getValue())
			systraysup = false;

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
		mFile.add(Util.addmenuitem("Stop server and exit", "EXIT", KeyEvent.VK_S, this));
		mbar.add(mFile);
		JMenu mView = new JMenu("Setup");
		mView.setMnemonic(KeyEvent.VK_S);
		mView.add(Util.addmenuitem("View Config", "VIEWC", KeyEvent.VK_V, this));
		mView.addSeparator();
		mView.add(Util.addmenuitem("Paths", "PATH", KeyEvent.VK_P, this));
		mView.add(Util.addmenuitem("Authentication", "AUTH", KeyEvent.VK_A, this));
		mView.add(Util.addmenuitem("Host", "HOST", KeyEvent.VK_H, this));
		mView.add(Util.addmenuitem("Urlprefix, allowlinking", "URLPREFIX", KeyEvent.VK_U, this));
		mView.add(Util.addmenuitem("Access log", "ACLOG", KeyEvent.VK_C, this));
		mView.add(Util.addmenuitem("Other", "OTHER", KeyEvent.VK_R, this));
		mbar.add(mView);
		JMenu mAbout = new JMenu("About");
		mView.setMnemonic(KeyEvent.VK_A);
		mAbout.add(Util.addmenuitem("About", "ABOUT", KeyEvent.VK_U, this));
		mbar.add(mAbout);

		setJMenuBar(mbar);

		getContentPane().setLayout(new BorderLayout());

		JToolBar mToolbar = new JToolBar();
		mToolbar.add(Util.makeNavigationButton("stopsign.png", "EXIT", "Stop server and exit", "Stop server and exit", this));
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
			if (url != null)
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

	/**
	 * Gen QR
	 *
	 * @param text the text
	 * @return the buffered image
	 */
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

	private void dooptselpath() {
		String path = (String) OptFactory.getInstance().getOpt("path").getValue();
		String basedir = (String) OptFactory.getInstance().getOpt("basedir").getValue();

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		FileSelPanel fspp = new FileSelPanel("Select directory/folder to serve:");
		fspp.setChooser(path, JFileChooser.DIRECTORIES_ONLY);
		Util.addleft(p, fspp);

		Util.addleft(p, new JLabel(
				"Tomcat requires a work folder while running." + "  It is recommended to leave it as the default"));
		FileSelPanel fspb = new FileSelPanel("Select tomcat work directory:");
		fspb.setChooser(basedir, JFileChooser.DIRECTORIES_ONLY);
		Util.addleft(p, fspb);
		Util.addleft(p, new JLabel("note requires restart of server"));

		int ret = JOptionPane.showConfirmDialog(this, p, "Select folders", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			path = fspp.getSelFile();
			OptFactory.getInstance().getOpt("path").setValue(path);
			basedir = fspb.getSelFile();
			OptFactory.getInstance().getOpt("basedir").setValue(basedir);
			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
			App.getInstance().restartserver(true);
			dispose();
		}

	}

	private void doopthost() {
		String host = (String) OptFactory.getInstance().getOpt("host").getValue();
		int port = (int) OptFactory.getInstance().getOpt("port").getValue();

		HostPanel ph = new HostPanel(host, port);

		Util.addleft(ph, new JLabel("note requires restart of server"));

		int ret = JOptionPane.showConfirmDialog(this, ph, "set host and port", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {

			host = ph.getHost();
			port = ph.getPort();
			OptFactory.getInstance().getOpt("host").setValue(host);
			OptFactory.getInstance().getOpt("port").setValue(new Integer(port));

			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
			App.getInstance().restartserver(true);
			dispose();
		}
	}

	private void dooptauth() {
		String realm = ((String) OptFactory.getInstance().getOpt("realm").getValue());
		String user = ((String) OptFactory.getInstance().getOpt("user").getValue());
		String passwd = ((String) OptFactory.getInstance().getOpt("password").getValue());
		boolean digest = ((Boolean) OptFactory.getInstance().getOpt("digest").getValue());

		PWPanel pwp = new PWPanel();
		pwp.setAuthMode(digest ? PWPanel.AuthMode.DIGEST : PWPanel.AuthMode.PLAIN);
		pwp.setRealm(realm);
		pwp.setUser(user);
		pwp.setPasswd(passwd);
		Util.addleft(pwp, new JLabel("note requires restart of server"));

		int ret = JOptionPane.showConfirmDialog(this, pwp, "Setup user/realm/password", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			digest = pwp.getAuthmode() == PWPanel.AuthMode.DIGEST ? true : false;
			realm = pwp.getRealm();
			user = pwp.getUser();
			passwd = pwp.getPasswd();
			OptFactory.getInstance().getOpt("digest").setValue(new Boolean(digest));
			OptFactory.getInstance().getOpt("realm").setValue(realm);
			OptFactory.getInstance().getOpt("user").setValue(user);
			OptFactory.getInstance().getOpt("password").setValue(passwd);

			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
			App.getInstance().restartserver(true);
			dispose();
		}

	}

	private void dourlprefix() {
		String urlprefix = ((String) OptFactory.getInstance().getOpt("urlprefix").getValue());
		boolean allowlink = ((Boolean) OptFactory.getInstance().getOpt("allowlinking").getValue());

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(new JLabel(OptFactory.getInstance().getOpt("urlprefix").getDescription()));
		JPanel l1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		l1.add(new JLabel("urlprefix"));
		JTextField tfurlprefix = new JTextField(20);
		tfurlprefix.setText(urlprefix);
		l1.add(tfurlprefix);
		p.add(l1);
		
		p.add(new JLabel(OptFactory.getInstance().getOpt("allowlinking").getDescription()));
		JCheckBox cballowlink = new JCheckBox("allowlinking");
		cballowlink.setSelected(allowlink);
		p.add(cballowlink);
		Util.addleft(p, new JLabel("note requires restart of server"));

		int ret = JOptionPane.showConfirmDialog(this, p, "Setup urlprefix, allowlinking", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			urlprefix = tfurlprefix.getText();
			allowlink = cballowlink.isSelected();

			OptFactory.getInstance().getOpt("urlprefix").setValue(urlprefix);
			OptFactory.getInstance().getOpt("allowlinking").setValue(new Boolean(allowlink));
			
			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
			App.getInstance().restartserver(true);
			dispose();
		}

	}
	
	private void doaccesslog() {
		boolean accesslog = ((Boolean) OptFactory.getInstance().getOpt("accesslog").getValue());
		String logdir = ((String) OptFactory.getInstance().getOpt("accesslog.dir").getValue());
		boolean logrot = ((Boolean) OptFactory.getInstance().getOpt("accesslog.rot").getValue());
		int logdays = ((Integer) OptFactory.getInstance().getOpt("accesslog.days").getValue());
		

		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));

		p.add(new JLabel(OptFactory.getInstance().getOpt("accesslog").getDescription()));
		JCheckBox cbaccesslog = new JCheckBox("accesslog");
		cbaccesslog.setSelected(accesslog);
		p.add(cbaccesslog);
		
		p.add(new JLabel(OptFactory.getInstance().getOpt("accesslog.dir").getDescription()));
		FileSelPanel fspp = new FileSelPanel("Select directory/folder for accesslog:");
		fspp.setChooser(logdir, JFileChooser.DIRECTORIES_ONLY);
		Util.addleft(p, fspp);		
		
		p.add(new JLabel(OptFactory.getInstance().getOpt("accesslog.rot").getDescription()));
		JCheckBox cblogrot = new JCheckBox("accesslog.rot");
		cblogrot.setSelected(logrot);
		p.add(cblogrot);
		
		p.add(new JLabel(OptFactory.getInstance().getOpt("accesslog.days").getDescription()));
		JPanel l1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
		l1.add(new JLabel("log rotate days"));
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(0);
		format.setGroupingUsed(false);
		JFormattedTextField ftfdays = new JFormattedTextField(format);		
		ftfdays.setValue(logdays);
		ftfdays.setColumns(5);
		l1.add(ftfdays);
		p.add(l1);
		
		Util.addleft(p, new JLabel("note requires restart of server"));

		int ret = JOptionPane.showConfirmDialog(this, p, "Setup urlprefix, allowlinking", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			accesslog = cbaccesslog.isSelected();
			logdir = fspp.getSelFile();
			logrot = cblogrot.isSelected();
			logdays = ((Number)ftfdays.getValue()).intValue();

			OptFactory.getInstance().getOpt("accesslog").setValue(Boolean.valueOf(accesslog));
			OptFactory.getInstance().getOpt("accesslog.dir").setValue(logdir);
			OptFactory.getInstance().getOpt("accesslog.rot").setValue(Boolean.valueOf(logrot));
			OptFactory.getInstance().getOpt("accesslog.days").setValue(Integer.valueOf(logdays));

			
			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
			App.getInstance().restartserver(true);
			dispose();
		}
		
	}

	private void doother() {	
		boolean quiet = ((Boolean) OptFactory.getInstance().getOpt("quiet").getValue());
		boolean chkupdates = ((Boolean) OptFactory.getInstance().getOpt("checkupdates").getValue());
		boolean bgui = ((Boolean) OptFactory.getInstance().getOpt("gui").getValue());
		boolean bsystray = ((Boolean) OptFactory.getInstance().getOpt("systray").getValue());
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
				
		p.add(new JLabel(OptFactory.getInstance().getOpt("quiet").getDescription()));
		JCheckBox cbquiet = new JCheckBox("quiet");
		cbquiet.setSelected(quiet);
		p.add(cbquiet);
		
		p.add(new JLabel(OptFactory.getInstance().getOpt("checkupdates").getDescription()));
		JCheckBox cbchkupdates = new JCheckBox("checkupdates");
		cbchkupdates.setSelected(chkupdates);
		p.add(cbchkupdates);

		p.add(new JLabel(OptFactory.getInstance().getOpt("gui").getDescription()));
		JCheckBox cbgui = new JCheckBox("gui");
		cbgui.setSelected(bgui);
		p.add(cbgui);
		
		p.add(new JLabel(OptFactory.getInstance().getOpt("systray").getDescription()));
		JCheckBox cbsystray = new JCheckBox("systray");
		cbsystray.setSelected(bsystray);
		p.add(cbsystray);

		
		Util.addleft(p, new JLabel("note requires restart of server"));

		int ret = JOptionPane.showConfirmDialog(this, p, "Setup urlprefix, allowlinking", JOptionPane.OK_CANCEL_OPTION);
		if (ret == JOptionPane.OK_OPTION) {
			quiet = cbquiet.isSelected();
			chkupdates = cbchkupdates.isSelected();
			bgui = cbgui.isSelected();
			bsystray = cbsystray.isSelected();
			
			OptFactory.getInstance().getOpt("quiet").setValue(new Boolean(quiet));
			OptFactory.getInstance().getOpt("checkupdates").setValue(new Boolean(chkupdates));
			OptFactory.getInstance().getOpt("gui").setValue(new Boolean(bgui));
			OptFactory.getInstance().getOpt("systray").setValue(new Boolean(bsystray));
			
			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
			App.getInstance().restartserver(true);
			dispose();
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
		if (e.getActionCommand().equals("PATH")) {
			dooptselpath();
		} else if (e.getActionCommand().equals("AUTH")) {
			dooptauth();
		} else if (e.getActionCommand().equals("HOST")) {
			doopthost();
		} else if (e.getActionCommand().equals("URLPREFIX")) {
			dourlprefix();
		} else if (e.getActionCommand().equals("ACLOG")) {
			doaccesslog();
		} else if (e.getActionCommand().equals("OTHER")) {
			doother();
		} else if (e.getActionCommand().equals("VIEWC")) {
			ListOptsPanel p = new ListOptsPanel();
			JOptionPane.showMessageDialog(this,p,"current config",JOptionPane.INFORMATION_MESSAGE);
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
		String url = geturl(alias);
		if (url != null) {
			BufferedImage image = genQR(url);
			pi.setImage(image);
			tfurl.setText(url.toString());
			pi.invalidate();
			pi.repaint();
		}
	}

	private void doabout() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		p.add(Box.createVerticalGlue());
		Calendar.getInstance();
		p.add(new JLabel("Copyright (C) Andrew Goh ".concat(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))));
		// p.add(new JLabel("MIT Licensed"));
		String url = "https://github.com/ag88/embtomcatwebdav";
		p.add(Util.URLJLabel(url, url));
		p.add(Box.createVerticalGlue());

		Tomcat tomcat = App.getInstance().getWdav().getTomcat();
		int port = tomcat.getConnector().getPort();

		String urlprefix = (String) OptFactory.getInstance().getOpt("urlprefix").getValue();
		boolean secure = tomcat.getConnector().getSecure();

		String hostname = tomcat.getHost().getName();		
		if (!hostname.equals("0.0.0.0")) {
			try {
				p.add(new JLabel("The embtomcat webdav/web service is accessible at:"));
				String davurl = new URL(secure ? "https" : "http", hostname, port, urlprefix).toString();
				p.add(Util.URLJLabel(davurl, davurl));
				p.add(Box.createVerticalGlue());
				url = new URL(secure ? "https" : "http", hostname, port, "/res/attrib/attribution.html").toString();
				p.add(Util.URLJLabel(url, "Attributions"));				
			} catch (MalformedURLException e) {
			}					
		} else {			
			if (jlalias != null) {
				int i = jlalias.getSelectionModel().getMinSelectionIndex();
				hostname = (String) jlalias.getModel().getElementAt(i);
				try {
					p.add(new JLabel("The embtomcat webdav/web service is accessible at:"));
					String davurl = new URL(secure ? "https" : "http", hostname, port, urlprefix).toString();
					p.add(Util.URLJLabel(davurl, davurl));
					p.add(Box.createVerticalGlue());
					url = new URL(secure ? "https" : "http", hostname, port, "/res/attrib/attribution.html").toString();
					p.add(Util.URLJLabel(url, "Attributions"));				
				} catch (MalformedURLException e) {
				}
			}			
		}
		
        URL spurl = App.class.getResource("/resources/sponsor.htm");
		if (spurl != null) {
			try {
				url = new URL(secure ? "https" : "http", hostname, port, "/res/sponsor.htm").toString();
				p.add(Box.createVerticalGlue());
				p.add(Util.URLJLabel(url, "Sponsor this project"));
			} catch (MalformedURLException e) {
			}
		}
		
		p.add(Box.createVerticalGlue());
		p.setPreferredSize(new Dimension(400,150));
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
