/*
 * Copyright 2012 Alliance Global Services, Inc. All rights reserved.
 * 
 * Licensed under the General Public License, Version 3.0 (the "License") you
 * may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.gnu.org/licenses/gpl-3.0.txt
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Class: ScrollableFrame
 * 
 * Purpose: Scrolling Text frame implementation. It takes each test scenario and
 * passes to Panel. Panel it self show in within frame.
 */

package com.ags.aft.util;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.Timer;
import org.apache.log4j.Logger;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.exception.AFTException;

public final class ScrollableFrame {
	/**
	 * CONSTANT LOGGER Object Instance
	 */
	private static final Logger LOGGER = Logger
			.getLogger(ScrollableFrame.class);

	/**
	 * class object
	 */
	private static ScrollableFrame scrollableTextFrame;

	/**
	 * Frame window object
	 */
	private JWindow window = null;
	/**
	 * Label object to display text on frame window
	 */
	private JLabel label = null;

	/**
	 * position upto which the scrollable text is being displayed
	 */
	private int currentScrollableTextPosition = 0;
	/**
	 * Maximum frame text length
	 */
	private int frameMaxTextlength = 0;
	/**
	 * Label text being displayed in the frame window
	 */
	private String labeltxt = "";
	/**
	 * Font of the label text
	 */
	private Font labelTextFont = null;
	/**
	 * Current screen width
	 */
	private int screenWidth = 0;
	/**
	 * Frame window/label height
	 */
	private int screenHeight = 0;

	/**
	 * timer to display rotating text
	 */
	private Timer timer = null;
	/**
	 * listener being called by timer to display rotating text
	 */
	private ActionListener activeListener = null;
	/**
	 * scrollable text being displayed in the label/frame window
	 */
	private String scrollableText = "";

	/**
	 * instance for ScrollableFrame
	 * 
	 */
	private ScrollableFrame() {
		super();
	}

	/**
	 * This method returns singleton instance
	 * 
	 * @return ScrollText
	 */
	public static ScrollableFrame getInstance() {
		// Creating singleton object
		if (scrollableTextFrame == null) {
			scrollableTextFrame = new ScrollableFrame();
		}
		return scrollableTextFrame;
	}

	/**
	 * This method initializes Frame
	 * 
	 * @throws AFTException
	 * 
	 */
	public void initializeFrame() throws AFTException {

		// Initialize frame window and label
		window = new JWindow();
		label = new JLabel("test");

		// set label alignments
		label.setVerticalAlignment(JLabel.CENTER);
		label.setHorizontalAlignment(JLabel.RIGHT);

		// add label to window frame
		window.add(label);
		window.pack();

		// get the screen width and height
		getScreenWidth();

		// Set size and location of the window frame based on screen width and
		// height
		window.setSize(screenWidth - 4, 30);
		window.setLocation(2, screenHeight - 70);
		// we want the frame window to be always on top
		window.setAlwaysOnTop(true);

		// set font, font color, text color and size
		setFontnColor();

		// Create listener for timer
		createActiveListener();

		int refreshPeriod = Integer
				.parseInt(ConfigProperties
						.getInstance()
						.getConfigProperty(
								ConfigProperties.SCROLLABLE_TEST_SCENARIO_FRAME_REFRESH_TIME));
		// create the timer object
		timer = new Timer(refreshPeriod, activeListener);
	}

	/**
	 * @throws AFTException
	 */
	private void setFontnColor() throws AFTException {
		int fontSize = Integer
				.parseInt(ConfigProperties
						.getInstance()
						.getConfigProperty(
								ConfigProperties.SCROLLABLE_TEST_SCENARIO_FRAME_FONT_SIZE));
		labelTextFont = new Font("Courier", Font.BOLD, fontSize);
		label.setFont(labelTextFont);
		Color textColor = new Color(200, 90, 10);
		label.setForeground(textColor);
		Color bgColor = new Color(140, 180, 200);
		window.getContentPane().setBackground(bgColor);
	}

	/**
	 * This method sets the screenWidth and screenHeight
	 * 
	 */
	private void getScreenWidth() {
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		for (int i = 0; i < gs.length; i++) {
			DisplayMode displayMode = gs[i].getDisplayMode();
			screenWidth = displayMode.getWidth();
			screenHeight = displayMode.getHeight();
		}

		// LOGGER.debug("ScreenWidth [" + screenWidth + "]");
		// LOGGER.debug("screenHeight [" + screenWidth + "]");
	}

	/**
	 * This method set the font to label.
	 * 
	 * @param text
	 * 
	 */
	private void setFontMetrics(String scrollableText) {
		// get metrics from the graphics
		FontMetrics metrics = label.getFontMetrics(labelTextFont);

		Dimension labelSize = label.getSize();
		// frameMaxTextlength = labelSize.width / (metrics.getWidths()[0] + 1);

		// compute width of scrollable text to compute how many characters can
		// be displayed
		int textWidth = 0;
		int frametextlength = 0;
		for (; frametextlength < scrollableText.length(); frametextlength++) {
			textWidth += metrics.charWidth(scrollableText
					.charAt(frametextlength));
			if (textWidth >= labelSize.width) {
				break;
			}
		}
		if (frametextlength == scrollableText.length()) {
			frameMaxTextlength = frametextlength - 1;
		} else {
			frameMaxTextlength = frametextlength;
		}

		// LOGGER.debug("frameMaxTextlength [" + frameMaxTextlength + "]");
	}

	/**
	 * This method starts frame
	 * 
	 * @param text
	 *            text
	 * 
	 */
	public void start(final String text) {
		EventQueue.invokeLater(new Runnable() {

			public void run() {
				try {
					currentScrollableTextPosition = 0;
					startTimer(text);
					window.setVisible(true);
				} catch (AFTException e) {
					LOGGER.error("Exception::", e);
				}
			}
		});
	}

	/**
	 * This method starts timer
	 * 
	 * @param text
	 *            text
	 * @throws AFTException
	 * 
	 */
	public void startTimer(final String text) throws AFTException {
		scrollableText = text;

		// set font metrics
		setFontMetrics(scrollableText);
		// LOGGER.info("scrollable text [" + scrollableText + "], length ["
		// + scrollableText.length() + "], frameMaxLength ["
		// + frameMaxTextlength + "]");
		currentScrollableTextPosition = (frameMaxTextlength - 20 < scrollableText
				.length() ? frameMaxTextlength - 20 : scrollableText.length());
		labeltxt = scrollableText.substring(0, currentScrollableTextPosition);
		label.setText(labeltxt);

		timer.restart();
	}

	/**
	 * This method creates active list
	 * 
	 */
	private void createActiveListener() {

		activeListener = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent event) {
				if (currentScrollableTextPosition < frameMaxTextlength) {
					if (scrollableText.length() >= (currentScrollableTextPosition + 1)) {
						labeltxt = scrollableText.substring(0,
								currentScrollableTextPosition + 1);
					} else {
						labeltxt = labeltxt + " ";
					}
				} else {
					if (scrollableText.length() > currentScrollableTextPosition) {
						labeltxt = scrollableText.substring(
								currentScrollableTextPosition
										- frameMaxTextlength,
								currentScrollableTextPosition + 1);
						// LOGGER.info("currentScrollableTextPosition ["
						// + currentScrollableTextPosition
						// + "], scrollableText length ["
						// + scrollableText.length() + "]");
					} else {
						labeltxt = labeltxt + " ";
					}
				}
				// LOGGER.debug("cursor [" + currentScrollableTextPosition
				// + "], label [" + labeltxt + "]");

				label.setText(labeltxt);
				if (labeltxt.trim().length() > 0) {
					currentScrollableTextPosition++;
				} else {
					currentScrollableTextPosition = 0;
				}
			}
		};
	}

	/**
	 * This method will stop the timer
	 * 
	 * 
	 */
	public void stop() {
		timer.stop();
		window.setVisible(false);
		labeltxt = "";
		label.setText(labeltxt);
		currentScrollableTextPosition = 0;
	}

	/**
	 * This method disposes window frame
	 * 
	 * 
	 */
	public void disposeFrame() {
		// dispose the window
		window.dispose();
	}
}