package io.github.ag88.embtomcatwebdav.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

public class WizDlg extends JDialog implements ActionListener {
	
	Log log = LogFactory.getLog(WizDlg.class);
	
	public static enum Ret {
		NEXT,
		BACK
	};
	
	JButton btnBack;
	JButton btnNext;
	
	public Ret ret;

	public WizDlg(JComponent o, String title) {
		setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(800,600));
		creategui(o);
	}

	public WizDlg(Frame owner, JComponent o, String title) {
		super(owner);
		setTitle(title);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setPreferredSize(new Dimension(800,600));
		creategui(o);
	}

	private void creategui(JComponent obj) {
		getContentPane().setLayout(new BorderLayout());
				
		add(obj, BorderLayout.CENTER);
		
		JPanel p = new JPanel();
		btnBack = new JButton("Back");
		btnBack.setActionCommand("BACK");
		btnBack.addActionListener(this);
		p.add(btnBack);
		btnNext = new JButton("Next");
		btnNext.setActionCommand("NEXT");
		btnNext.addActionListener(this);
		p.add(btnNext);
		add(p, BorderLayout.SOUTH);
				
		Dimension d1 = obj.getPreferredSize();
		Dimension d2 = p.getPreferredSize();
		d1 = new Dimension(d1.width > 800 ? d1.width : 800, d1.height + d2.height);
		setPreferredSize(d1);
	}
	
	public Ret doModal() {
		
		pack();
		setLocationRelativeTo(getParent());
		setModal(true);
		setVisible(true);
		
		return ret;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if(e.getActionCommand().equals("NEXT")) {
			ret = Ret.NEXT;
			setVisible(false);
			dispose();
		} else if(e.getActionCommand().equals("BACK")) {
			ret = Ret.BACK;
			setVisible(false);
			dispose();			
		}
		
	}

	public void setbtnNextLabel(String label) {
		btnNext.setText(label);
	}
	
	public void setbtnBackLabel(String label) {
		btnBack.setText(label);
	}
}
