package io.github.ag88.embtomcatwebdav.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

/**
 * The Class ImagePanel - JPanel to display a BufferedImage.
 * 
 * JPanel to display a BufferedImage
 * 
 */
public class ImagePanel extends JPanel {
	
	BufferedImage image;
	
	public ImagePanel() {
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, null);
	}

	/**
	 * Instantiates a new image panel.
	 *
	 * @param image the image
	 */
	public ImagePanel(BufferedImage image) {
		super();
		this.image = image;
		setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
	}

	/**
	 * Gets the image.
	 *
	 * @return the image
	 */
	public BufferedImage getImage() {
		return image;
	}


	/**
	 * Sets the image.
	 *
	 * @param image the new image
	 */
	public void setImage(BufferedImage image) {
		this.image = image;
	}
		

}
