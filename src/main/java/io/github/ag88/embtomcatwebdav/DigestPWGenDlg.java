package io.github.ag88.embtomcatwebdav;

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

public class DigestPWGenDlg extends JDialog implements ActionListener {
	
	Log log = LogFactory.getLog(DigestPWGenDlg.class);
	
	WebDavServer server;
	
	JTextField tfrealm;
	JTextField tfuser;
	JPasswordField pwfpass;
	JTextField tfout;

	public DigestPWGenDlg(WebDavServer wdav) {
		super((JFrame) null, "Generate DIGEST password");
		setModal(true);
		server = wdav;
		creategui();
	}

	private void creategui() {
		
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(480, 320));
		
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		
		addleft(new JLabel("Realm"));
		tfrealm = new JTextField(15);
		tfrealm.setText(server.getRealm());
		addleft(tfrealm);
		addleft(new JLabel("Username"));
		tfuser = new JTextField(20);
		addleft(tfuser);
		addleft(new JLabel("Password"));
		pwfpass = new JPasswordField(15);
		addleft(pwfpass);
		addleft(new JLabel("Hashed password"));
		tfout = new JTextField(20);
		addleft(tfout);
		StringBuilder sb = new StringBuilder(100);
		sb.append("<html>For DIGEST authentication, this generated hashed password ");
		sb.append(",including the 'digest(xxx)' wrapper text, can be maintained ");
		sb.append("in the password field in the properties config file. ");
		sb.append("Set digest to true as it otherwise defaults to BASIC authentication</html>");				
		JLabel l = new JLabel(sb.toString());
		//l.setSize(400, 80);
		l.setBorder(new LineBorder(Color.BLACK));
		addleft(l);
		
		JPanel p = new JPanel();
		JButton b1 = new JButton("Generate");
		b1.setActionCommand("GEN");
		b1.addActionListener(this);
		p.add(b1);
		addleft(p);
	}
	
	private void addleft(JComponent o) {
		o.setAlignmentX(LEFT_ALIGNMENT);
		getContentPane().add(o);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getActionCommand().equals("GEN")) {
			try {
				String epass = server.digestEncodeStoredPw(tfrealm.getText(), tfuser.getText(), new String(pwfpass.getPassword()));
				tfout.setText(epass);
			} catch (NoSuchAlgorithmException e1) {
				log.error(e1);
			}
		}	
	}

	
}
