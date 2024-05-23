package io.github.ag88.embtomcatwebdav.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.security.NoSuchAlgorithmException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.InputVerifier;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.util.DigestPWUtil;

public class PWPanel extends JPanel implements ActionListener {

	/** The log. */
	Log log = LogFactory.getLog(PWPanel.class);

	/** The tfrealm. */
	JTextField tfrealm;

	/** The tfuser. */
	JTextField tfuser;

	/** The pwfpass. */
	JPasswordField pwfpass;
	
	JRadioButton rbhash;
	JRadioButton rbplain;

	
	public enum AuthMode {
		DIGEST, PLAIN
	}

	AuthMode authmode = AuthMode.DIGEST;

	public PWPanel() {
		setPreferredSize(new Dimension(600, 300));
		creategui();
	}

	
	class DigestChListener implements DocumentListener {

		@Override
		public void insertUpdate(DocumentEvent e) {
			if (authmode == AuthMode.DIGEST)
				pwfpass.setText(null);			
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			if (authmode == AuthMode.DIGEST)
				pwfpass.setText(null);						
		}

		@Override
		public void changedUpdate(DocumentEvent e) {
			if (authmode == AuthMode.DIGEST)
				pwfpass.setText(null);			
		}
		
	}

	private void creategui() {

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		addleft(new JLabel("Realm"));
		tfrealm = new JTextField(15);
		addleft(tfrealm);
		DigestChListener chl = new DigestChListener();
		tfrealm.getDocument().addDocumentListener(chl);
		addleft(new JLabel("Username"));
		tfuser = new JTextField(20);
		tfuser.getDocument().addDocumentListener(chl);
		addleft(tfuser);
		addleft(new JLabel("Password"));
		pwfpass = new JPasswordField(15);
		addleft(pwfpass);
		addleft(new JLabel("note that username and password has nothing to do with OS (e.g. Windows/Linux/OS X etc) user and password"));
		addleft(new JLabel("the username and password is only used for connecting to this web/webdav server"));
		JLabel l1 = new JLabel("note: in DIGEST mode, changing username or realm or switching to plain");
		l1.setForeground(Color.MAGENTA);
		addleft(l1);
		l1 = new JLabel("resets/clears the password, and you need to set password again");
		l1.setForeground(Color.MAGENTA);
		addleft(l1);
		ButtonGroup bg = new ButtonGroup();
		JPanel bp = new JPanel();
		bp.setLayout(new BoxLayout(bp, BoxLayout.PAGE_AXIS));
		bp.add(new JLabel("note that for plain authentication, password is stored and transmitted in plain text." +
				"  use DIGEST authentication for security"));
		rbhash = new JRadioButton("DIGEST authentication");
		rbhash.setActionCommand("RBHASH");
		rbhash.addActionListener(this);
		bg.add(rbhash);
		bp.add(rbhash);
		rbplain = new JRadioButton("BASIC authentication");
		rbplain.setActionCommand("RBPLAIN");
		rbplain.addActionListener(this);
		bg.add(rbplain);
		bp.add(rbplain);
		bp.setBorder(BorderFactory.createTitledBorder("Auth mode"));
		addleft(bp);
		if (authmode == AuthMode.DIGEST)
			rbhash.setSelected(true);
		else
			rbplain.setSelected(true);

	}

	/**
	 * Addleft.
	 *
	 * @param o the o
	 */
	private void addleft(JComponent o) {
		o.setAlignmentX(LEFT_ALIGNMENT);
		add(o);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("RBHASH")) {
			authmode = AuthMode.DIGEST;
		} else if (e.getActionCommand().equals("RBPLAIN")) {
			authmode = AuthMode.PLAIN;
			pwfpass.setText(null);
		}

	}


	public String getRealm() {
		return tfrealm.getText();
	}

	public String getUser() {
		return tfuser.getText();
	}

	public String getPasswd() {
		String passwd = new String(pwfpass.getPassword());
		if (authmode == AuthMode.PLAIN)
			return passwd;
		else { //DIGEST
			DigestPWUtil pwutil = new  DigestPWUtil();		
			if (passwd.startsWith("digest(") && passwd.endsWith(")")) {
				return passwd;
			} else {
				try {				
					String epass = pwutil.digestEncodeStoredPw(tfrealm.getText(), tfuser.getText(),
							new String(pwfpass.getPassword()));
					return epass;
				} catch (NoSuchAlgorithmException e1) {
					log.error(e1);
					return null;
				}
			}
				
		}
	}
	
	public String getrawPasswd() {
		return new String(pwfpass.getPassword());
	}
	
	public AuthMode getAuthmode() {
		return authmode;
	}

	public void setRealm(String realm) {
		tfrealm.setText(realm);
	}

	public void setUser(String user) {
		tfuser.setText(user);
	}

	public void setPasswd(String passwd) {
		pwfpass.setText(passwd);
	}
	
	public void setAuthMode(AuthMode mode) {
		authmode = mode;
		if (authmode == AuthMode.DIGEST)
			rbhash.setSelected(true);
		else
			rbplain.setSelected(true);
	}


}
