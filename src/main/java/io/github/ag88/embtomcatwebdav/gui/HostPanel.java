package io.github.ag88.embtomcatwebdav.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class HostPanel extends JPanel implements ActionListener {

	JTextField tfhost;
	JFormattedTextField tfport;
	
	public HostPanel(String host, int port) {
		setPreferredSize(new Dimension(700, 200));
		creategui(host, port);
	}

	private void creategui(String host, int port) {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		add(Box.createVerticalGlue());
		add(new JLabel("host"));		
		
		ButtonGroup bg1 = new ButtonGroup();
		JRadioButton rb1 = new JRadioButton("localhost - this can only be accessed on this PC/workstation",true);
		rb1.setActionCommand("RBLOCAL");
		rb1.addActionListener(this);
		bg1.add(rb1);
		add(rb1);
		JRadioButton rb2 = new JRadioButton("dynamic - use DHCP configured ip address, "
			+ "note: this run on all network interfaces");
		rb2.setActionCommand("RBDHCP");
		rb2.addActionListener(this);
		bg1.add(rb2);
		add(rb2);
		JRadioButton rb3 = new JRadioButton("fixed specify host:");
		rb3.setActionCommand("RBFIXED");
		rb3.addActionListener(this);
		bg1.add(rb3);
		add(rb3);		
		JPanel l1 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		l1.add(new JLabel("host"));
		tfhost = new JTextField(40);
		
		if(host.equals("localhost")) {
			rb1.setSelected(true);
		} else if (host.equals("0.0.0.0")) {
			rb2.setSelected(true);
		} else
			rb3.setSelected(true);
		tfhost.setText(host);
		l1.add(tfhost);	
		add(l1);		
		JPanel l2 = new JPanel(new FlowLayout(FlowLayout.LEADING));
		l2.add(new JLabel("port"));
		NumberFormat format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(0);
		format.setGroupingUsed(false);
		tfport = new JFormattedTextField(format);
		tfport.setValue(port);
		l2.add(tfport);
		add(l2);
		add(Box.createVerticalGlue());				
		
	}

	public String getHost() {
		return tfhost.getText();
	}
	
	public int getPort() {
		return ((Number)(tfport.getValue())).intValue();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("RBLOCAL")) {
			tfhost.setText("localhost");
		} else if (e.getActionCommand().equals("RBDHCP")) {
			tfhost.setText("0.0.0.0");
		} else if (e.getActionCommand().equals("RBFIXED")) {
			tfhost.requestFocus();
		}
		
	}


}
