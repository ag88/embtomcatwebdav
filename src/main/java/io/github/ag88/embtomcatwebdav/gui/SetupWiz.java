package io.github.ag88.embtomcatwebdav.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.LayoutManager2;
import java.awt.TextField;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.App;
import io.github.ag88.embtomcatwebdav.opt.OptFactory;

public class SetupWiz {
	
	Log log = LogFactory.getLog(SetupWiz.class);

	enum DlgSteps {
		INIT,
		HOSTPORT,
		USERPASSWD,
		FOLDERS,
		SAVEDONE,
		END
	};

	
	public SetupWiz() {
		init();
	}
	
	public void init() {
	}
	
	public void dosetup(boolean init) {
		if(!checkandcreatedir(init)) return;
		dodlgs();
	}
	
	
	
	private void dodlgs() {
		DlgSteps state = DlgSteps.INIT;
		
		while(state != DlgSteps.END) {
			if(state == DlgSteps.INIT ||
				state == DlgSteps.HOSTPORT) {
				state = DlgSteps.HOSTPORT;
				WizDlg.Ret ret = hostportdlg();
				if (ret == WizDlg.Ret.NEXT)
					state = DlgSteps.USERPASSWD;
				else if (ret == WizDlg.Ret.BACK)
					state = DlgSteps.INIT;
				else if (ret == null)
					System.exit(0);
			} else if(state == DlgSteps.USERPASSWD) {
				WizDlg.Ret ret = userpasswddlg();
				if (ret == WizDlg.Ret.NEXT)
					state = DlgSteps.FOLDERS;
				else if (ret == WizDlg.Ret.BACK)
					state = DlgSteps.HOSTPORT;
				else if (ret == null)
					System.exit(0);
			} else if(state == DlgSteps.FOLDERS) {
				WizDlg.Ret ret = selfoldersdlg();
				if (ret == WizDlg.Ret.NEXT)
					state = DlgSteps.SAVEDONE;
				else if (ret == WizDlg.Ret.BACK)
					state = DlgSteps.USERPASSWD;
				else if (ret == null)
					System.exit(0);
			} else if(state == DlgSteps.SAVEDONE) {
				WizDlg.Ret ret = savedonedlg();
				if (ret == WizDlg.Ret.NEXT)
					state = DlgSteps.END;
				else if (ret == WizDlg.Ret.BACK)
					state = DlgSteps.FOLDERS;
				else if (ret == null)
					System.exit(0);
			}

		}
		
	}

	public WizDlg.Ret hostportdlg() {
		String host = (String) OptFactory.getInstance().getOpt("host").getValue();
		int port = (int) OptFactory.getInstance().getOpt("port").getValue();		
		
		HostPanel ph = new HostPanel(host, port);
		
		//JOptionPane.showConfirmDialog(null, p, "set host and port", JOptionPane.OK_CANCEL_OPTION);
		WizDlg wd = new WizDlg(ph, "set host and port");
		WizDlg.Ret ret = wd.doModal();
		host = ph.getHost();
		port = ph.getPort();
		OptFactory.getInstance().getOpt("host").setValue(host);
		OptFactory.getInstance().getOpt("port").setValue(new Integer(port));
		
		log.info(host);
		log.info(port);
		
		return ret;
	}
	
	
	public WizDlg.Ret userpasswddlg() {		
		String realm = ((String) OptFactory.getInstance().getOpt("realm").getValue());
		String user = ((String) OptFactory.getInstance().getOpt("user").getValue());
		String passwd = ((String) OptFactory.getInstance().getOpt("password").getValue());
		boolean digest = ((Boolean) OptFactory.getInstance().getOpt("digest").getValue());
		
		PWPanel pwp = new PWPanel();
		pwp.setAuthMode(digest ? PWPanel.AuthMode.DIGEST : PWPanel.AuthMode.PLAIN);
		pwp.setRealm(realm);
		pwp.setUser(user);
		pwp.setPasswd(passwd);
		
		WizDlg dlg = new WizDlg(pwp, "Setup user/realm/password");
		dlg.pack();
		WizDlg.Ret ret = dlg.doModal();
		digest = pwp.getAuthmode() == PWPanel.AuthMode.DIGEST ? true : false;
		realm = pwp.getRealm();
		user = pwp.getUser();
		passwd = pwp.getPasswd();
		OptFactory.getInstance().getOpt("digest").setValue(new Boolean(digest));
		OptFactory.getInstance().getOpt("realm").setValue(realm);
		OptFactory.getInstance().getOpt("user").setValue(user);
		OptFactory.getInstance().getOpt("password").setValue(passwd);

		log.info(realm);
		log.info(user);
		log.info(passwd);
		log.info(pwp.getAuthmode().name());
		return ret;
	}
	
	
	public WizDlg.Ret selfoldersdlg() {
		String path = (String) OptFactory.getInstance().getOpt("path").getValue();
		String basedir = (String) OptFactory.getInstance().getOpt("basedir").getValue();
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		FileSelPanel fspp = new FileSelPanel("Select directory/folder to serve:");		
		fspp.setChooser(path, JFileChooser.DIRECTORIES_ONLY);
		addleft(p, fspp);

		addleft(p, new JLabel("Tomcat requires a work folder while running."
				+ "  It is recommended to leave it as the default"));
		FileSelPanel fspb = new FileSelPanel("Select tomcat work directory:");
		fspb.setChooser(basedir, JFileChooser.DIRECTORIES_ONLY);
		addleft(p, fspb);
		WizDlg wd = new WizDlg(p, "Select folders");
		WizDlg.Ret ret = wd.doModal();
		path = fspp.getSelFile();
		OptFactory.getInstance().getOpt("path").setValue(path);
		log.info(path);
		basedir = fspb.getSelFile();
		OptFactory.getInstance().getOpt("basedir").setValue(basedir);
		log.info(basedir);
		return ret;

	}
	
	public WizDlg.Ret savedonedlg() {		
		ListOptsPanel op = new ListOptsPanel();
		WizDlg wd = new WizDlg(op, "Configs");
		wd.setbtnNextLabel("Save and launch");
		WizDlg.Ret ret = wd.doModal();
		if( ret == WizDlg.Ret.NEXT) {
			OptFactory.getInstance().genconfigfile(App.getInstance().getconfigfile(), true);
		}
		return ret;
	}
	
	public boolean checkandcreatedir(boolean init) {
		String configdir = App.getInstance().getConfigdir();
		String datadir = App.getInstance().getDatadir();
		
		if (configdir == null || configdir.equals("")) {
			log.error("configdir is null or empty ");
			return false;			
		}
		
		if (datadir == null || datadir.equals("")) {
			log.error("appdatadir is null or empty ");
			return false;			
		}
		
		if (!Files.exists(Paths.get(configdir))) {
			try {
				Files.createDirectories(Paths.get(configdir));
			} catch (IOException e) {
				log.error("unable to create configdir ".concat(configdir));
				return false;
			}
		} 
		
		
		if (!Files.exists(Paths.get(datadir))) {
			try {
				Files.createDirectories(Paths.get(datadir));
				OptFactory.getInstance().getOpt("basedir").setDefaultval(datadir);
				OptFactory.getInstance().getOpt("basedir").setValue(datadir);
			} catch (IOException e) {
				log.error("unable to create appdatadir ".concat(datadir));
				return false;
			}
		}
		
		if (init) {
			OptFactory.getInstance().getOpt("basedir").setDefaultval(datadir);
			OptFactory.getInstance().getOpt("basedir").setValue(datadir);
			OptFactory.getInstance().getOpt("host").setValue("0.0.0.0");			
			OptFactory.getInstance().getOpt("digest").setValue(Boolean.valueOf(true));
			OptFactory.getInstance().getOpt("accesslog.dir").setValue(datadir);			
		}
		
		return true;
	}
	
	private void addleft(JComponent p, JComponent o) {
		o.setAlignmentX(Box.LEFT_ALIGNMENT);
		p.add(o);
	}

}
