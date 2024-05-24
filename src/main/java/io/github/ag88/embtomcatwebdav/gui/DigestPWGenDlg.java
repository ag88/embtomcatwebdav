/*
 Copyright 2023 Andrew Goh http://github.com/ag88
 
 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package io.github.ag88.embtomcatwebdav.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.TextArea;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.NoSuchAlgorithmException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import io.github.ag88.embtomcatwebdav.WebDavServer;
import io.github.ag88.embtomcatwebdav.gui.WizDlg.Ret;
import io.github.ag88.embtomcatwebdav.util.DigestPWUtil;

// TODO: Auto-generated Javadoc
/**
 * This is a dialog that provide a utility to generate hashed DIGEST authentication passwords
 */
public class DigestPWGenDlg extends JDialog implements ActionListener {
	
	/** The log. */
	Log log = LogFactory.getLog(DigestPWGenDlg.class);
	
	/** The tfrealm. */
	JTextField tfrealm;
	
	/** The tfuser. */
	JTextField tfuser;
	
	/** The pwfpass. */
	JPasswordField pwfpass;
	
	/** The tfout. */
	JTextField tfout;
	
	String realm;
	String user;
	String passwd;

	/**
	 * Instantiates a new digest PW gen dlg.
	 *
	 * @param wdav the wdav
	 */
	public DigestPWGenDlg() {
		super((JFrame) null, "Generate DIGEST password");
		creategui();
	}

	public DigestPWGenDlg(String realm, String user, String passwd) {
		super((JFrame) null, "Generate DIGEST password");
		this.realm = realm;
		this.user = user;
		this.passwd = passwd;
		creategui();
	}
	
	/**
	 * Creategui.
	 */
	private void creategui() {
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(480, 320));
		
		getContentPane().setLayout(new BorderLayout());
		
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
		
		addleft(p, new JLabel("Realm"));
		tfrealm = new JTextField(15);
		if(realm != null)
			tfrealm.setText(realm);
		addleft(p, tfrealm);
		addleft(p, new JLabel("Username"));
		tfuser = new JTextField(20);
		addleft(p, tfuser);
		if(user != null)
			tfrealm.setText(user);
		
		addleft(p, new JLabel("Password"));
		pwfpass = new JPasswordField(15);
		addleft(p, pwfpass);
		if (passwd != null)
			pwfpass.setText(passwd);
		
		addleft(p, new JLabel("Hashed password"));
		tfout = new JTextField(20);
		addleft(p, tfout);
		StringBuilder sb = new StringBuilder(100);
		sb.append("<html>For DIGEST authentication, this generated hashed password ");
		sb.append(",including the 'digest(xxx)' wrapper text, can be maintained ");
		sb.append("in the password field in the properties config file. ");
		sb.append("Set digest to true as it otherwise defaults to BASIC authentication</html>");				
		JLabel l = new JLabel(sb.toString());
		addleft(p, l);
		
		JPanel p1 = new JPanel();
		JButton b1 = new JButton("Generate");
		b1.setActionCommand("GEN");
		b1.addActionListener(this);
		p1.add(b1);
		addleft(p, p1);
		getContentPane().add(p, BorderLayout.CENTER);
	}
	
	/**
	 * Addleft.
	 *
	 * @param o the o
	 */
	private void addleft(JPanel p, JComponent o) {
		o.setAlignmentX(LEFT_ALIGNMENT);
		p.add(o);
	}
	

	public void doModal() {
		
		pack();
		setLocationRelativeTo(getParent());
		setModal(true);
		setVisible(true);

	}

	
	/**
	 * Action performed.
	 *
	 * @param e the e
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("GEN")) {
			try {
				DigestPWUtil pwutil = new DigestPWUtil();
				String epass = pwutil.digestEncodeStoredPw(tfrealm.getText(), tfuser.getText(), 
						new String(pwfpass.getPassword()));
				tfout.setText(epass);
			} catch (NoSuchAlgorithmException e1) {
				log.error(e1);
			}
		}
	}

	
	
	public String getRealm() {
		return tfrealm.getText();
	}
	
	public String getUser() {
		return tfuser.getText();
	}
	
	public String getPasswd() {
		return new String(pwfpass.getPassword());
	}
	
	public String getHashPW() {
		try {
			DigestPWUtil pwutil = new DigestPWUtil();
			String epass = pwutil.digestEncodeStoredPw(tfrealm.getText(), tfuser.getText(), 
					new String(pwfpass.getPassword()));
			return epass;
		} catch (NoSuchAlgorithmException e1) {
			log.error(e1);
			return null;
		}
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

}
