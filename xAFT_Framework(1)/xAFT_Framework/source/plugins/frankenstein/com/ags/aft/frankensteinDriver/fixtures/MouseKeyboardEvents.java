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
 * Class: MouseKeyboardEvents
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * related to Mouse and Keyboard
 */

package com.ags.aft.frankensteinDriver.fixtures;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.apache.log4j.Logger;

import com.ags.aft.exception.AFTException;

/**
 * The Class MouseKeyboardEvents.
 */
public class MouseKeyboardEvents {

	/**Default wait time**/
	private int sleepTime=1000;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(MouseKeyboardEvents.class);

	private Robot robot;

	private static final int KEY_DELAY_MS = 500;
	public static final String MOUSEACTION_BUTTONPRESS = "BUTTONPRESS";
	public static final String MOUSEACTION_MOUSEMOVE = "MOUSEMOVE";
	public static final String MOUSE_CLICKBUTTON_LEFT = "LEFT";
	public static final String MOUSE_CLICKBUTTON_RIGHT = "RIGHT";
	public static final String MOUSE_CLICKBUTTON_MIDDLE = "CENTER";

	public MouseKeyboardEvents() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			LOGGER.error("Exception::", e);
		}
	}

	/**
	 * Uses Robot library to release the specified key. If ObjectId is
	 * specified, it uses Selenium library to focus on the object before Robot
	 * library is used generate the key event
	 * 
	 * @param objectID
	 *            ObjectId to focus before key events are generated
	 * 
	 * @param keyCode
	 *            Key to press. For special keys, use the following codes: Alt:
	 *            {[ALT]}, Control: {[CTRL]}, Shift: {[SHIFT]}, Esc: {[ESC]}
	 * @throws AFTException
	 */
	public void keyRelease(String objectID, String keyCode) throws AFTException {
		LOGGER.info("Executing command: [keyRelease] with value [" + keyCode
				+ "]");

		char char1=keyCode.charAt(0);

		// Let us create a little delay
		//
		robot.delay(KEY_DELAY_MS);

		// check the keycode. should be specified as {[keycode]} and if
		// matches
		// press the key else raise exception
		if (keyCode.compareToIgnoreCase("{[ALT]}") == 0) {
			LOGGER.trace("Release Alt KEY");
			robot.keyRelease(KeyEvent.VK_ALT);
			LOGGER.debug("Released Alt KEY");
		} else if (keyCode.compareToIgnoreCase("{[CTRL]}") == 0) {
			LOGGER.trace("Release Ctrl KEY");
			robot.keyRelease(KeyEvent.VK_CONTROL);
			LOGGER.debug("Released Ctrl KEY");
		} else if (keyCode.compareToIgnoreCase("{[CAPSLOCK]}") == 0) {
			LOGGER.trace("Release CAPSLOCK KEY");
			// Generating key release event for writing the Keyboard letter
			Toolkit.getDefaultToolkit().setLockingKeyState(
					KeyEvent.VK_CAPS_LOCK, true);
			robot.delay(KEY_DELAY_MS);
			LOGGER.debug("Released CAPSLOCK KEY");
		} else if (keyCode.compareToIgnoreCase("{[SHIFT]}") == 0) {
			LOGGER.trace("Release Shift KEY");
			// Generating key release event for writing the Keyboard letter
			robot.keyRelease(KeyEvent.VK_SHIFT);
			LOGGER.debug("Released Shift KEY");
		} else if (keyCode.compareToIgnoreCase("{[ESC]}") == 0) {
			LOGGER.trace("Release Escape KEY");
			// Generating key release event for writing the Keyboard letter
			robot.keyRelease(KeyEvent.VK_ESCAPE);
			LOGGER.debug("Released Escape KEY");
		} else if (keyCode.compareToIgnoreCase("{[SPACE]}") == 0) {
			LOGGER.debug("Release SPACE KEY");
			robot.keyRelease(KeyEvent.VK_SPACE);
			LOGGER.debug("Released Space KEY");
		}else if (char1 >= 'A' && char1 <= 'Z') {
			robot.keyRelease(KeyEvent.VK_SHIFT);
		}

		switch (char1) {
		case 'a':
		case 'A':
			robot.keyRelease(KeyEvent.VK_A);
			break;
		case 'b':
		case 'B':
			robot.keyRelease(KeyEvent.VK_B);
			break;
		case 'c':
		case 'C':
			robot.keyRelease(KeyEvent.VK_C);
			break;
		case 'd':
		case 'D':
			robot.keyRelease(KeyEvent.VK_D);
			break;
		case 'e':
		case 'E':
			robot.keyRelease(KeyEvent.VK_E);
			break;
		case 'f':
		case 'F':
			robot.keyRelease(KeyEvent.VK_F);
			break;
		case 'g':
		case 'G':
			robot.keyRelease(KeyEvent.VK_G);
			break;
		case 'h':
		case 'H':
			robot.keyRelease(KeyEvent.VK_H);
			break;
		case 'i':
		case 'I':
			robot.keyRelease(KeyEvent.VK_I);
			break;
		case 'j':
		case 'J':
			robot.keyRelease(KeyEvent.VK_J);
			break;
		case 'k':
		case 'K':
			robot.keyRelease(KeyEvent.VK_K);
			break;
		case 'l':
		case 'L':
			robot.keyRelease(KeyEvent.VK_L);
			break;
		case 'm':
		case 'M':
			robot.keyRelease(KeyEvent.VK_M);
			break;
		case 'n':
		case 'N':
			robot.keyRelease(KeyEvent.VK_N);
			break;
		case 'o':
		case 'O':
			robot.keyRelease(KeyEvent.VK_O);
			break;
		case 'p':
		case 'P':
			robot.keyRelease(KeyEvent.VK_P);
			break;
		case 'q':
		case 'Q':
			robot.keyRelease(KeyEvent.VK_Q);
			break;
		case 'r':
		case 'R':
			robot.keyRelease(KeyEvent.VK_R);
			break;
		case 's':
		case 'S':
			robot.keyRelease(KeyEvent.VK_S);
			break;
		case 't':
		case 'T':
			robot.keyRelease(KeyEvent.VK_T);
			break;
		case 'u':
		case 'U':
			robot.keyRelease(KeyEvent.VK_U);
			break;
		case 'v':
		case 'V':
			robot.keyRelease(KeyEvent.VK_V);
			break;
		case 'w':
		case 'W':
			robot.keyRelease(KeyEvent.VK_W);
			break;
		case 'x':
		case 'X':
			robot.keyRelease(KeyEvent.VK_X);
			break;
		case 'y':
		case 'Y':
			robot.keyRelease(KeyEvent.VK_Y);
			break;
		case 'z':
		case 'Z':
			robot.keyRelease(KeyEvent.VK_Z);
			break;

		case '.':
			robot.keyRelease(KeyEvent.VK_PERIOD);
			break;
		case '0':
			robot.keyRelease(KeyEvent.VK_0);
			break;
		case '1':
			robot.keyRelease(KeyEvent.VK_1);
			break;
		case '2':
			robot.keyRelease(KeyEvent.VK_2);
			break;
		case '3':
			robot.keyRelease(KeyEvent.VK_3);
			break;
		case '4':
			robot.keyRelease(KeyEvent.VK_4);
			break;
		case '5':
			robot.keyRelease(KeyEvent.VK_5);
			break;
		case '6':
			robot.keyRelease(KeyEvent.VK_6);
			break;
		case '7':
			robot.keyRelease(KeyEvent.VK_7);
			break;
		case '8':
			robot.keyRelease(KeyEvent.VK_8);
			break;
		case '9':
			robot.keyRelease(KeyEvent.VK_9);
			break;

		case ';':
			robot.keyRelease(KeyEvent.VK_SEMICOLON);
			break;
		case '/':
			robot.keyRelease(KeyEvent.VK_SLASH);
			break;
		case '<':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_COMMA);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '>':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_PERIOD);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case ':':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_SEMICOLON);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '@':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_2);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '_':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyPress(KeyEvent.VK_MINUS);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '{':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_OPEN_BRACKET);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '}':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_CLOSE_BRACKET);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '$':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_4);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '(':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_9);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case ')':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_0);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '#':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_3);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '&':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_7);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '*':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_8);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '!':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_1);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		case '\\':
			robot.keyRelease(KeyEvent.VK_BACK_SLASH);
			break;
		case '-':
			robot.keyRelease(KeyEvent.VK_MINUS);
			break;
		case '+':
			robot.keyPress(KeyEvent.VK_SHIFT);
			robot.keyRelease(KeyEvent.VK_EQUALS);
			robot.keyRelease(KeyEvent.VK_SHIFT);
			break;
		default:
			LOGGER.error("Invalid KEY SEQUENCE specified!!!! Please refer to user documentation for the supported special key sequences.");
		}

		if (char1 >= 'A' && char1 <= 'Z') {
			robot.keyRelease(KeyEvent.VK_SHIFT);
		}

	}

	public void typeKeys(String objectID, String keyCode)
			throws AFTException {
		boolean flag=true;	
		char[] requiredChars=keyCode.toCharArray();
		for(char c:requiredChars){
			if(flag){
				flag=keyPress(objectID, c+"");
				try{
					Thread.sleep(sleepTime);
				}catch(Exception e){
					LOGGER.error("Exception::", e);
				}
				try{
					keyRelease(objectID,c+"");
				}catch(Exception e){

				}
			}
		}

	}


	/**
	 * Uses Robot library to press the specified key on active window at the
	 * location of the cursor. If ObjectId is specified, it uses Selenium
	 * library to focus on the object before Robot library is used generate the
	 * key events
	 * 
	 * @param objectID
	 *            ObjectId to focus before key events are generated
	 * 
	 * @param keyCode
	 *            Key to press. For special keys, use the following codes: Tab:
	 *            {[TAB]}, Enter: {[Enter]}, Esc: {[ESC]}, Space: {[SPACE]},
	 *            CAPS LOCK: {[CAPSLOCK]}, Alt: {[ALT]}, Control: {[CTRL]},
	 *            Shift: {[SHIFT]}, Function 4: {[F4]} Other keys supported
	 *            include: a-z, A-Z, 0-9, ;, /, <, :, >, @, {, $, }, (, #, ), &,
	 *            *, !, \, -, +
	 * @throws AFTException
	 */
	public boolean keyPress(String objectID, String keyCode)
			throws AFTException {

		LOGGER.info("[Frankenstein]Executing command: [keyPress] with value [" + keyCode
				+ "]");
		if(keyCode.equalsIgnoreCase("UP") || keyCode.equalsIgnoreCase("DOWN")|| keyCode.equalsIgnoreCase("LEFT")|| keyCode.equalsIgnoreCase("RIGHT")){
			KeyStroke.getKeyStroke(keyCode.toUpperCase());
		}else{

			try {
				int i = 0;
				while (i < keyCode.length()) {
					LOGGER.info("[Frankenstein]");
					char char1 = keyCode.charAt(i);
					char char2 = '\u0000';
					if ((i + 1) < keyCode.length()) {
						char2 = keyCode.charAt(i + 1);
					}
					if (char1 == '{' && char2 == '[') {
						// we found special key start seq. check if the special
						// key
						// close seq is specified by user or not
						if (((i + 2) >= keyCode.length())
								|| !(keyCode.substring(i + 2).indexOf("]}") > 0)) {
							// oops, did not find the special key close seq. Let
							// us
							// throw an exception and get out of here...
							LOGGER.error("Please specify correct format for keys to be pressed using KeyPress action.");
							throw new AFTException(
									"Please specify correct format for keys to be pressed using KeyPress action.");
						}

						// of, we found that user has specified the special key
						// close
						// seq as well, now let us move forward to extract the
						// special
						// key seq
						
						//keyCode.substring(keyCode.indexOf("{[")+2, keyCode.indexOf("]}"));
						int k = i + 2;
						String subStr = "";
						boolean flag = true;
						while (flag) {
							LOGGER.info("[Frankenstein]get key");
							subStr += keyCode.charAt(k);
							if (((k + 2) < keyCode.length())
									&& (keyCode.charAt(k + 1) == ']' && keyCode
									.charAt(k + 2) == '}')) {
								flag = false;
							} else if ((k + 2) >= keyCode.length()) {
								LOGGER.error("Please specify correct format for keys to be pressed using KeyPress action.");
								throw new AFTException(
										"Please specify correct format for keys to be pressed using KeyPress action.");
							}
							k++;
						}

						// ok, we found a special key seq, let us check if
						// matches
						// one
						// of the supported key seq's and press that key
						LOGGER.trace("Special character [" + subStr + "]");
						if (subStr.compareToIgnoreCase("TAB") == 0) {
							LOGGER.debug("User specififed TAB KEY to click");
							robot.keyPress(KeyEvent.VK_TAB);
							LOGGER.debug("Clicked TAB KEY");
						} else if (subStr.compareToIgnoreCase("CAPSLOCK") == 0) {
							LOGGER.debug("User specififed CAPSLOCK KEY to click");
							Toolkit.getDefaultToolkit().setLockingKeyState(
									KeyEvent.VK_CAPS_LOCK, true);
							robot.delay(KEY_DELAY_MS);
							LOGGER.debug("Clicked CAPSLOCK KEY");
						} else if (subStr.compareToIgnoreCase("ENTER") == 0) {
							LOGGER.debug("User specififed ENTER KEY to click");
							robot.keyPress(KeyEvent.VK_ENTER);
							LOGGER.debug("Clicked ENTER KEY");
							break;
						} else if (subStr.compareToIgnoreCase("ESC") == 0) {
							LOGGER.debug("User specififed ESC KEY to click");
							robot.keyPress(KeyEvent.VK_ESCAPE);
							LOGGER.debug("Clicked ESC KEY");
							break;
						} else if (subStr.compareToIgnoreCase("SPACE") == 0) {
							LOGGER.debug("User specififed SPACE KEY to click");
							robot.keyPress(KeyEvent.VK_SPACE);
							LOGGER.debug("Clicked Space KEY");
							break;
						} else if (subStr.compareToIgnoreCase("ALT") == 0) {
							LOGGER.debug("User specififed ALT KEY to click");
							robot.keyPress(KeyEvent.VK_ALT);
							LOGGER.debug("Clicked ALT KEY");
						} else if (subStr.compareToIgnoreCase("CTRL") == 0) {
							LOGGER.debug("User specififed CTRL KEY to click");
							robot.keyPress(KeyEvent.VK_CONTROL);
							LOGGER.debug("Clicked CTRL KEY");
						} else if (subStr.compareToIgnoreCase("SHIFT") == 0) {
							LOGGER.debug("User specififed SHIFT KEY to click");
							robot.keyPress(KeyEvent.VK_SHIFT);
							LOGGER.debug("Clicked Shift KEY");
						} else if (subStr.compareToIgnoreCase("F4") == 0) {
							LOGGER.debug("User specififed F4 KEY to click");
							robot.keyPress(KeyEvent.VK_F4);
							LOGGER.debug("Clicked F4 KEY");
						} else {
							// oops, the specified special char key seq does not
							// match
							// any of the supported key seq's. Let us throw an
							// exception
							// and get out of here...
							LOGGER.error("Invalid KEY SEQUENCE specified!!!! Please refer to user documentation for the supported special key sequences.");
							throw new AFTException(
									"Invalid KEY SEQUENCE specified!!!! Please refer to user documentation for the supported special key sequences.");
						}

						// now, we need to increment the counter so that it
						// starts
						// searching for next char to press from correct
						// location
						i = k + 2;

					} else {

						LOGGER.trace("Printing character [" + char1 + "]");

						LOGGER.debug("User specififed [" + char1 + "] to click");

						if (char1 >= 'A' && char1 <= 'Z') {
							robot.keyPress(KeyEvent.VK_SHIFT);
						}

						switch (char1) {
						case 'a':
						case 'A':
							robot.keyPress(KeyEvent.VK_A);
							break;
						case 'b':
						case 'B':
							robot.keyPress(KeyEvent.VK_B);
							break;
						case 'c':
						case 'C':
							robot.keyPress(KeyEvent.VK_C);
							break;
						case 'd':
						case 'D':
							robot.keyPress(KeyEvent.VK_D);
							break;
						case 'e':
						case 'E':
							robot.keyPress(KeyEvent.VK_E);
							break;
						case 'f':
						case 'F':
							robot.keyPress(KeyEvent.VK_F);
							break;
						case 'g':
						case 'G':
							robot.keyPress(KeyEvent.VK_G);
							break;
						case 'h':
						case 'H':
							robot.keyPress(KeyEvent.VK_H);
							break;
						case 'i':
						case 'I':
							robot.keyPress(KeyEvent.VK_I);
							break;
						case 'j':
						case 'J':
							robot.keyPress(KeyEvent.VK_J);
							break;
						case 'k':
						case 'K':
							robot.keyPress(KeyEvent.VK_K);
							break;
						case 'l':
						case 'L':
							robot.keyPress(KeyEvent.VK_L);
							break;
						case 'm':
						case 'M':
							robot.keyPress(KeyEvent.VK_M);
							break;
						case 'n':
						case 'N':
							robot.keyPress(KeyEvent.VK_N);
							break;
						case 'o':
						case 'O':
							robot.keyPress(KeyEvent.VK_O);
							break;
						case 'p':
						case 'P':
							robot.keyPress(KeyEvent.VK_P);
							break;
						case 'q':
						case 'Q':
							robot.keyPress(KeyEvent.VK_Q);
							break;
						case 'r':
						case 'R':
							robot.keyPress(KeyEvent.VK_R);
							break;
						case 's':
						case 'S':
							robot.keyPress(KeyEvent.VK_S);
							break;
						case 't':
						case 'T':
							robot.keyPress(KeyEvent.VK_T);
							break;
						case 'u':
						case 'U':
							robot.keyPress(KeyEvent.VK_U);
							break;
						case 'v':
						case 'V':
							robot.keyPress(KeyEvent.VK_V);
							break;
						case 'w':
						case 'W':
							robot.keyPress(KeyEvent.VK_W);
							break;
						case 'x':
						case 'X':
							robot.keyPress(KeyEvent.VK_X);
							break;
						case 'y':
						case 'Y':
							robot.keyPress(KeyEvent.VK_Y);
							break;
						case 'z':
						case 'Z':
							robot.keyPress(KeyEvent.VK_Z);
							break;

						case '0':
							robot.keyPress(KeyEvent.VK_0);
							break;
						case '1':
							robot.keyPress(KeyEvent.VK_1);
							break;
						case '2':
							robot.keyPress(KeyEvent.VK_2);
							break;
						case '3':
							robot.keyPress(KeyEvent.VK_3);
							break;
						case '4':
							robot.keyPress(KeyEvent.VK_4);
							break;
						case '5':
							robot.keyPress(KeyEvent.VK_5);
							break;
						case '6':
							robot.keyPress(KeyEvent.VK_6);
							break;
						case '7':
							robot.keyPress(KeyEvent.VK_7);
							break;
						case '8':
							robot.keyPress(KeyEvent.VK_8);
							break;
						case '9':
							robot.keyPress(KeyEvent.VK_9);
							break;

						case '.':
							robot.keyPress(KeyEvent.VK_PERIOD);
							break;
						case ';':
							robot.keyPress(KeyEvent.VK_SEMICOLON);
							break;
						case '/':
							robot.keyPress(KeyEvent.VK_SLASH);
							break;
						case '<':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_COMMA);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '>':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_PERIOD);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case ':':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_SEMICOLON);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '@':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_2);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '{':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_OPEN_BRACKET);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '}':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_CLOSE_BRACKET);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '$':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_4);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '(':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_9);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case ')':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_0);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '#':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_3);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '&':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_7);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '*':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_8);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '!':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_1);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '_':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyRelease(KeyEvent.VK_MINUS);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						case '\\':
							robot.keyPress(KeyEvent.VK_BACK_SLASH);
							break;
						case '-':
							robot.keyPress(KeyEvent.VK_MINUS);
							break;
						case '+':
							robot.keyPress(KeyEvent.VK_SHIFT);
							robot.keyPress(KeyEvent.VK_EQUALS);
							robot.keyRelease(KeyEvent.VK_SHIFT);
							break;
						default:
							LOGGER.error("Invalid KEY SEQUENCE specified!!!! Please refer to user documentation for the supported special key sequences.");
							throw new AFTException(
									"Invalid KEY SEQUENCE specified!!!! Please refer to user documentation for the supported special key sequences.");
						}

						if (char1 >= 'A' && char1 <= 'Z') {
							robot.keyRelease(KeyEvent.VK_SHIFT);
						}

						LOGGER.debug("Clicked KEY [" + char1 + "]");

						// now, we need to increment the counter so that it
						// starts
						// searching for next char to press from correct
						// location
						i++;
					}

					// create a little delay for Robot to work properly
					robot.delay(KEY_DELAY_MS);
				}
			} catch (Exception e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		}
		return true;
	}

	/**
	 * Uses Robot library to press the specified Mouse Action on active window
	 * at the location of the cursor
	 * 
	 * @param mouseAction
	 *            Mouse action to be performed (BUTTONPRESS or MOUSEMOVE)
	 * @param parameters
	 *            parameters for Mouse Actions
	 * 
	 * @throws AFTException
	 */
	public void mouseEvent(String mouseAction, String parameters)
			throws AFTException {
		LOGGER.info("Executing command: [mouse] with value [" + parameters
				+ "]");

		String[] mouseEvtParams = null;

		// Checking whether mouse event value is passed or not
		if (parameters != null && !parameters.equals("")) {
			mouseEvtParams = parameters.split(",");

			// Let us create a little delay
			//
			robot.delay(KEY_DELAY_MS);

			// eliminating white spaces
			mouseAction.trim();

			if (mouseAction.equals(MOUSEACTION_BUTTONPRESS)) {
				LOGGER.trace("Click the mouse button");

				// Checking if parameters are null
				if (mouseEvtParams.length > 0) {
					// checking the parameter if it left or right click
					// Left=left click
					if (mouseEvtParams[0]
							.compareToIgnoreCase(MOUSE_CLICKBUTTON_LEFT) == 0) {
						robot.mousePress(InputEvent.BUTTON1_MASK);
						robot.mouseRelease(InputEvent.BUTTON1_MASK);
						LOGGER.debug("Clicked mouse left button");
					}
					// Center button click
					else if (mouseEvtParams[0]
							.compareToIgnoreCase(MOUSE_CLICKBUTTON_MIDDLE) == 0) {
						robot.mousePress(InputEvent.BUTTON2_MASK);
						robot.mouseRelease(InputEvent.BUTTON2_MASK);
						LOGGER.debug("Clicked mouse center button");
					} // Right button click
					else if (mouseEvtParams[0]
							.compareToIgnoreCase(MOUSE_CLICKBUTTON_RIGHT) == 0) {
						robot.mousePress(InputEvent.BUTTON3_MASK);
						robot.mouseRelease(InputEvent.BUTTON3_MASK);
						LOGGER.debug("Clicked mouse right button");
					} else {
						String errMsg = "Invalid parameter ["
								+ mouseEvtParams[0]
										+ "] specified for [mouse] action. Please refer to documentation on how to use [mouse] action.";
						LOGGER.error(errMsg);
						throw new AFTException(errMsg);
					}
				} else {
					String errMsg = "Invalid parameters specified for [mouse] action. Please refer to documentation on how to use [mouse] action.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			}
			// This action takes mouse to specified Coordinates
			else if (mouseAction.equals(MOUSEACTION_MOUSEMOVE)) {
				LOGGER.trace("Mouse Action: MOUSEMOVE");
				// Checking values are not null
				if (mouseEvtParams.length >= 2) {
					robot.mouseMove(Integer.parseInt(mouseEvtParams[0]),
							Integer.parseInt(mouseEvtParams[1]));
					LOGGER.debug("Mouse Action: MOUSEMOVE completed successfully");
				} else {
					String errMsg = "Invalid parameters specified for [mouse] action. Please refer to documentation on how to use [mouse] action.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			} else {
				String errMsg = "Invalid mouse action specified. Valid actions BUTTONPRESS and MOUSEMOVE. Please refer to documentation on how to use [mouse] action.";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} else {
			String errMsg = "No parameter passed for [mouse] action. Please refer to documentation on how to use [mouse] action.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
	}
}
