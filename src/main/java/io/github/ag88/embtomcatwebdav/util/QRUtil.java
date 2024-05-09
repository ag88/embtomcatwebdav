package io.github.ag88.embtomcatwebdav.util;
/* 
 * QR Code generator demo (Java)
 * 
 * Run this command-line program with no arguments. The program creates/overwrites a bunch of
 * PNG and SVG files in the current working directory to demonstrate the creation of QR Codes.
 * 
 * Copyright (c) Project Nayuki. (MIT License)
 * https://www.nayuki.io/page/qr-code-generator-library
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * - The above copyright notice and this permission notice shall be included in
 *   all copies or substantial portions of the Software.
 * - The Software is provided "as is", without warranty of any kind, express or
 *   implied, including but not limited to the warranties of merchantability,
 *   fitness for a particular purpose and noninfringement. In no event shall the
 *   authors or copyright holders be liable for any claim, damages or other
 *   liability, whether in an action of contract, tort or otherwise, arising from,
 *   out of or in connection with the Software or the use or other dealings in the
 *   Software.
 */

import java.awt.image.BufferedImage;
import java.util.Objects;

import io.nayuki.qrcodegen.QrCode;

public class QRUtil {

	public QRUtil() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * QrCode To image.
	 *
	 * @param qr the qr
	 * @param scale the scale
	 * @param border the border
	 * @return the buffered image
	 */
	public BufferedImage toImage(QrCode qr, int scale, int border) {
		return toImage(qr, scale, border, 0xFFFFFF, 0x000000);
	}
	
	
	/**
	 * Returns a raster image depicting the specified QR Code, with
	 * the specified module scale, border modules, and module colors.
	 * <p>For example, scale=10 and border=4 means to pad the QR Code with 4 light border
	 * modules on all four sides, and use 10&#xD7;10 pixels to represent each module.
	 * @param qr the QR Code to render (not {@code null})
	 * @param scale the side length (measured in pixels, must be positive) of each module
	 * @param border the number of border modules to add, which must be non-negative
	 * @param lightColor the color to use for light modules, in 0xRRGGBB format
	 * @param darkColor the color to use for dark modules, in 0xRRGGBB format
	 * @return a new image representing the QR Code, with padding and scaling
	 * @throws NullPointerException if the QR Code is {@code null}
	 * @throws IllegalArgumentException if the scale or border is out of range, or if
	 * {scale, border, size} cause the image dimensions to exceed Integer.MAX_VALUE
	 */
	public BufferedImage toImage(QrCode qr, int scale, int border, int lightColor, int darkColor) {
		Objects.requireNonNull(qr);
		if (scale <= 0 || border < 0)
			throw new IllegalArgumentException("Value out of range");
		if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
			throw new IllegalArgumentException("Scale or border too large");
		
		BufferedImage result = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < result.getHeight(); y++) {
			for (int x = 0; x < result.getWidth(); x++) {
				boolean color = qr.getModule(x / scale - border, y / scale - border);
				result.setRGB(x, y, color ? darkColor : lightColor);
			}
		}
		return result;
	}
}
