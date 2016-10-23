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
 * Class: CommandFixtures
 * 
 * Purpose: This class implements methods that allows users to perform actions
 * on the UI objects like click,type, select, remove
 */

package com.ags.aft.fixtures.common;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.ags.aft.common.DatabaseUtil;
import com.ags.aft.common.RuntimeProperties;
import com.ags.aft.config.ConfigProperties;
import com.ags.aft.constants.Constants;
import com.ags.aft.constants.SystemVariables;
import com.ags.aft.exception.AFTException;
import com.ags.aft.imports.ExcelRead;
import com.ags.aft.main.Parser;
import com.ags.aft.runners.TestStepRunner;
import com.ags.aft.testObjects.TestScenario;
import com.ags.aft.util.Helper;
import com.ags.aft.util.Variable;

/**
 * The Class CommandFixtures.
 * 
 */
public class CommandFixtures {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger
			.getLogger(CommandFixtures.class);

	public static final int PARAMETER_LIST = 3;
	public static final int SHEETNAMEPARAM = 0;
	public static final int COLDETAILSPARAM = 1;
	public static final int ROWDETAILSPARAM = 2;
	public static final int ROWDETAILSIDX = 4;
	public static final int COLUMNDETAILSIDX = 7;
	private final int maxRows = Constants.MAXARRAYROWCOUNT;
	private final int maxColumns = Constants.MAXARRAYCOLUMNCOUNT;

	/**
	 * Instantiates a new AFT command fixtures.
	 * 
	 */
	public CommandFixtures() {
	}

	/**
	 * Gets the operand.
	 * 
	 * @param position
	 *            the position
	 * @param expression
	 *            the expression
	 * @param operator
	 *            the operator
	 * @return the operand
	 */
	String getOperand(String position, String expression, String operator) {
		// Split the expression based on operator to extract left and right
		// operands
		String[] operands = null;
		String operand = "";
		int index;
		operands = expression.split(operator);

		if (operands.length != 0 || expression.length() == operator.length()) {

			if (position.equalsIgnoreCase("left")) {
				index = 0;
			} else {
				index = 1;
			}

			// first let us check how many elements are present in operands
			// array
			if ((operands.length > index)
					&& (operands[index] != null && operands[index].length() > 0)) {
				// if we have the right set of elements, let us check if they
				// are not null and length is > 0
				// Ideally we should not mess around with what user has
				// specified. Not sure why this was added but let us comment
				// it out for now
				// operands[index] = operands[index].trim();

				// let us check now to see if user enclosed the string in
				// double quotes and remove it
				if (operands[index].charAt(0) == '"'
						&& operands[index].charAt(operands[index].length() - 1) == '"') {
					operands[index] = operands[index].substring(1,
							operands[index].length() - 1);
				}
				operand = operands[index];
			}
		}
		return operand;
	}

	/**
	 * This method supports the conditional statements sourced through the test
	 * suite and the actions will be called based on the return value
	 * (true/false).
	 * 
	 * @param expression
	 *            the expression
	 * @param testStepRunner
	 *            Test step runner object
	 * @param parser
	 *            parser object
	 * @throws AFTException
	 *             the application exception
	 */
	public void ifThenElse(String expression, TestStepRunner testStepRunner,
			Parser parser) throws AFTException {

		LOGGER.debug("Executing [ifThenElse] with value [" + expression + "]");
		String description = "";
		String expressionArray[];
		String[] parsedStr = null;
		String annotationName = null;
		String annotationOrgValue = null;
		String annotationParsedValue = null;
		String annotationString = null;
		StringBuffer strBuff = new StringBuffer();
		try {
			String[] expressions = expression.split("\\?");

			// Validate if user had specified invalid ifThenElse Expression
			if (expressions.length != 2) {
				String errMessage = "User had Specified Invalid ifThenElse expression";
				LOGGER.error(errMessage);
				throw new AFTException(errMessage);
			}
			// split the conditions based on the ":" symbol
			String[] action = expressions[1].split(":");
			boolean condition = false;
			String expr = expressions[0].substring(1,
					expressions[0].length() - 1);
			// split the expression by "))" to support for multiple expressions
			String exps[] = expr.split("\\)\\)");
			// if there are multiple conditions proceed with the following
			// logic.
			if (exps.length > 1) {
				int count = 0;
				for (String logicalExp : exps) {
					// this logic will validate one by one expression
					// continuously.
					// first it will evaluate first expression and stores the
					// result in string buffer and then evaluates second
					// expression.and stores the result in buffer.
					// and send this two results statuses to processCondition
					// method to get the result.After getting the result it will
					// delete previous data in buffer and the current result in
					// buffer.This process continues till expression is
					// evaluated.
					if (logicalExp.startsWith("&&")) {
						strBuff.append("&&");
						logicalExp = logicalExp.substring(2,
								logicalExp.length());
					} else if (logicalExp.startsWith("||")) {
						strBuff.append("||");
						logicalExp = logicalExp.substring(2,
								logicalExp.length());
					}
					expressionArray = logicalExp.split("\\)");
					// evaluate the expression and get the result status as
					// true/false.
					condition = processExpression(expressionArray,
							testStepRunner, parser);
					strBuff.append(condition);
					count++;
					if (count == 2) {
						// process two boolean conditions and add the result to
						// string buffer.
						condition = processCondition(strBuff.toString());
						count = 1;
						// delete the existing content
						strBuff.delete(0, strBuff.length());
						// add the current result status.
						strBuff.append(condition);
					}
				}
			} else {
				// if there is only single condition proceed with the following
				// logic.
				expressionArray = expressions[0].split("\\)");
				// evaluate the expression and get the result status as
				// true/false.
				condition = processExpression(expressionArray, testStepRunner,
						parser);
			}
			// }

			// Call callToAnnotation() method to handle the appropriate
			// annotation
			// called in the conditional statement

			if (condition) {
				// Let us process actions/annotations specified in true
				// condition
				LOGGER.debug("Expression [" + condition
						+ "] evaluates to TRUE. Processing annotation ["
						+ action[0] + "]");
				annotationString = action[0];
				description = "Condition is TRUE, Processing annotation ["
						+ action[0] + "]";
			} else {
				// Let us process actions/annotations specified in false
				// condition
				LOGGER.debug("Expression [" + condition
						+ "] evaluates to FALSE. Processing annotation ["
						+ action[1] + "]");
				annotationString = action[1];
				description = "Condition is FALSE, Processing annotation ["
						+ action[1] + "]";
			}

			if (annotationString.contains("=")) {
				parsedStr = annotationString.split("=");
				if (parsedStr.length < 2) {
					String errMsg = "Invalid annotation call ["
							+ action
							+ "]. Please check documentation for more details on how to use annotations.";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}

				annotationName = parsedStr[0].trim();
				annotationOrgValue = parsedStr[1].trim();
			} else {
				annotationName = annotationString.trim();
				annotationOrgValue = null;
			}

			// parse the value string to substitute values for variables and
			// test
			// data
			if (annotationOrgValue != null) {
				annotationParsedValue = Helper.getInstance()
						.getActionValue(testStepRunner.getTestSuiteRunner(),
								annotationOrgValue);
			}
			String annotationDesc = "Calling Annotation [" + annotationName
					+ "], original parameter value [" + annotationOrgValue
					+ "], parsed parameter value [" + annotationParsedValue
					+ "]";
			LOGGER.debug(annotationDesc);

			parser.updateTestStepDescription(testStepRunner, description,
					expression);
			parser.callToAnnotation(testStepRunner, annotationName,
					annotationOrgValue, annotationParsedValue, true, null);
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * It process the single expression.
	 * 
	 * @param expression
	 *            the expression
	 * @param result
	 *            the result
	 * @return boolean
	 * @throws AFTException
	 */
	private boolean processExpression(String[] exps,
			TestStepRunner testStepRunner, Parser parser) throws AFTException {
		StringBuffer strBuff = new StringBuffer();
		int count = 0;
		boolean condition = false;
		for (String logicalExpression : exps) {
			// if expression contains "(" then remove the "{" from expression.
			if (logicalExpression.contains("(")) {
				logicalExpression = logicalExpression.replaceAll("\\(", "");
			}
			if (logicalExpression.startsWith("&&")) {
				strBuff.append("&&");
				logicalExpression = logicalExpression.substring(2,
						logicalExpression.length());
			} else if (logicalExpression.startsWith("||")) {
				strBuff.append("||");
				logicalExpression = logicalExpression.substring(2,
						logicalExpression.length());
			}
			// evaluate the expression and get the result status.
			condition = ifThenElseEvaluation(logicalExpression, testStepRunner,
					parser);
			strBuff.append(condition);
			count++;
			if (count == 2) {
				// process the two boolean conditions
				condition = processCondition(strBuff.toString());
				count = 1;
				strBuff.delete(0, strBuff.length());
				strBuff.append(condition);
			}
		}
		return condition;
	}

	/**
	 * It process the two boolean conditions
	 * 
	 * @param expression
	 *            the expression
	 * @return boolean
	 */
	private boolean processCondition(String expression) {
		LOGGER.debug("Executing [processCondition] with expression ["
				+ expression + "]");
		boolean resultStatus = false;
		// trimming expression to remove extra spaces
		String parsedExpression = expression;
		parsedExpression = parsedExpression.trim();
		if (parsedExpression.contains("&&")) {
			String subExpression = parsedExpression.substring(0,
					parsedExpression.indexOf("&&"));
			resultStatus = (Boolean.parseBoolean(subExpression.trim()) && Boolean
					.parseBoolean(parsedExpression.substring(
							parsedExpression.indexOf("&&") + 2,
							parsedExpression.length())));
		} else if (parsedExpression.contains("||")) {
			String subExpression = parsedExpression.substring(0,
					parsedExpression.indexOf("||"));
			resultStatus = (Boolean.parseBoolean(subExpression.trim()) || Boolean
					.parseBoolean(parsedExpression.substring(
							parsedExpression.indexOf("||") + 2,
							parsedExpression.length())));
		} else {
			resultStatus = Boolean.parseBoolean(parsedExpression.trim());
		}

		LOGGER.debug("Executing [processCondition] with expression ["
				+ expression + "] evaluates to: " + resultStatus);

		return resultStatus;
	}

	/**
	 * This method supports the conditional statements sourced through the test
	 * suite and the return boolean value.
	 * 
	 * @param expression
	 *            the expression
	 * @return true, if successful
	 * @throws AFTException
	 */
	private static boolean ifThenElseEvaluation(String expression,
			TestStepRunner testStepRunner, Parser parser) throws AFTException {

		LOGGER.debug("Executing [ifThenElseEvaluation] with value ["
				+ expression + "]");

		String[] operands = null;
		boolean condition = false;
		if (expression.equals("=")) {
			LOGGER.debug("Left Operands and Right Operand are specified empty");
			LOGGER.debug("Returning the condition as TRUE");
			condition = true;
			return condition;
		}

		if (expression.contains("^")) {
			LOGGER.info("User has specified AFT action as ifThenElse condition");

			String[] condStatement = expression.split("\\^");
			if (condStatement.length != 3) {
				String errMessage = "Condition Specified for ifThenElse action is not Valid."
						+ "Please refere wiki for ifThenElse usage";
				LOGGER.error(errMessage);
				throw new AFTException(errMessage);
			}
			String aftAction = condStatement[0];
			// Restrict user to specify only verify actions
			if (aftAction.toLowerCase().startsWith("verify")) {
				LOGGER.info("User had specified [" + aftAction
						+ "] as ifThenElse Condition");
				String elementName = condStatement[1];
				String elementValue = condStatement[2];

				String parsedElementValue = "";
				parsedElementValue = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), elementValue);

				// String actionResponse = "";
				// Execute the action
				try {
					parser.callToAction(testStepRunner, aftAction, elementName,
							elementValue, parsedElementValue, null, null);
					condition = true;
					// actionResponse = "true";
					LOGGER.info("AFT Validation action evaluated to True");
				} catch (Exception e) {
					// actionResponse = "false";
					condition = false;
					LOGGER.info("AFT Validation action evaluated to False");
				}
				// logicalExpression = actionResponse + "=" + subExpression[1];
			} else {
				String errMessage = "User had specified [ " + aftAction
						+ "] as ifThenElse Condition."
						+ "Any action other than Validation "
						+ "action is not allowed in the ifThenElse condition";
				LOGGER.error(errMessage);
				throw new AFTException(errMessage);
			}
		} // Check if user has specified "not equal to operator" (!= OR <>)
		else if (expression.contains("!=") || expression.contains("<>")) {

			LOGGER.trace("User has specified 'not equal to operator' (!= OR <>)");

			// Split the expression based on operator to extract left and right
			// operands
			if (expression.contains("!=")) {
				operands = expression.split("!=");
			} else {
				operands = expression.split("<>");
			}
			String leftOperand = "";
			String rightOperand = "";
			if (operands.length != 0) {
				if (operands[0].length() > 0) {
					operands[0] = operands[0].trim();
					if (operands[0].charAt(0) == '"'
							&& operands[0].charAt(operands[0].length() - 1) == '"') {
						operands[0] = operands[0].substring(1,
								operands[0].length() - 1);
					}
					leftOperand = operands[0];
				}

				try {
					if (operands[1].length() > 0) {
						operands[1] = operands[1].trim();
						if (operands[1].charAt(0) == '"'
								&& operands[1].charAt(operands[1].length() - 1) == '"') {
							operands[1] = operands[1].substring(1,
									operands[1].length() - 1);
						}
						rightOperand = operands[1];
					}
				} catch (Exception e) {
					LOGGER.info("User has specified one of the operand as empty");
					rightOperand = "";
				}
			} else {
				LOGGER.info("User has specified empty values for comparision");
				leftOperand = "";
				rightOperand = "";
			}

			LOGGER.trace("Left operand is [" + leftOperand
					+ "], right operand is [" + rightOperand + "]");

			// We need to check if both operands eval to a number or not. If
			// they eval to a number, we should do numeric comparison, else
			// string comparison
			try {
				if (Double.parseDouble(leftOperand) != Double
						.parseDouble(rightOperand)) {
					condition = true;
				} else {
					condition = false;
				}

			} catch (NumberFormatException e) {
				// ah, atleast one of the operands is not numeric. Let us do
				// string comparison and return results...
				if (leftOperand.compareToIgnoreCase(rightOperand) != 0) {
					condition = true;
				} else {
					condition = false;
				}
			}

			// Check if user has specified "less than equal to operator" (<=)
		} else if (expression.contains("<=")) {

			LOGGER.trace("User has specified 'less than equal to operator' (<=)");

			// Split the expression based on operator to extract left and right
			// operands
			operands = expression.split("<=");

			String leftOperand = "";
			if (operands[0].length() > 0) {
				operands[0] = operands[0].trim();
				if (operands[0].charAt(0) == '"'
						&& operands[0].charAt(operands[0].length() - 1) == '"') {
					operands[0] = operands[0].substring(1,
							operands[0].length() - 1);
				}
				leftOperand = operands[0];
			}

			String rightOperand = "";
			try {
				if (operands[1].length() > 0) {
					operands[1] = operands[1].trim();
					if (operands[1].charAt(0) == '"'
							&& operands[1].charAt(operands[1].length() - 1) == '"') {
						operands[1] = operands[1].substring(1,
								operands[1].length() - 1);
					}
					rightOperand = operands[1];
				}
			} catch (Exception e) {
				rightOperand = "";
			}

			LOGGER.trace("Left operand is [" + leftOperand
					+ "], right operand is [" + rightOperand + "]");

			if (Double.parseDouble(leftOperand) <= Double
					.parseDouble(rightOperand)) {
				condition = true;
			} else {
				condition = false;
			}
		} else if (expression.contains(">=")) {

			LOGGER.trace("User has specified 'greater than equal to operator' (>=)");

			// Split the expression based on operator to extract left and right
			// operands
			operands = expression.split(">=");

			String leftOperand = "";
			if (operands[0].length() > 0) {
				operands[0] = operands[0].trim();
				if (operands[0].charAt(0) == '"'
						&& operands[0].charAt(operands[0].length() - 1) == '"') {
					operands[0] = operands[0].substring(1,
							operands[0].length() - 1);
				}
				leftOperand = operands[0];
			}

			String rightOperand = "";
			try {
				if (operands[1].length() > 0) {
					operands[1] = operands[1].trim();
					if (operands[1].charAt(0) == '"'
							&& operands[1].charAt(operands[1].length() - 1) == '"') {
						operands[1] = operands[1].substring(1,
								operands[1].length() - 1);
					}
					rightOperand = operands[1];
				}
			} catch (Exception e) {
				rightOperand = "";
			}
			LOGGER.trace("Left operand is [" + leftOperand
					+ "], right operand is [" + rightOperand + "]");

			if (Double.parseDouble(leftOperand) >= Double
					.parseDouble(rightOperand)) {
				condition = true;
			} else {
				condition = false;
			}
		} else if (expression.contains("=")) {
			LOGGER.trace("User has specified 'equal to' (=) operator");
			// Split the expression based on operator to extract left and right
			// operands
			operands = expression.split("=");
			String leftOperand = "";
			if (operands[0].length() > 0) {
				operands[0] = operands[0].trim();
				if (operands[0].charAt(0) == '"'
						&& operands[0].charAt(operands[0].length() - 1) == '"') {
					operands[0] = operands[0].substring(1,
							operands[0].length() - 1);
				}
				leftOperand = operands[0];
			}

			String rightOperand = "";
			try {
				if (operands[1].length() > 0) {
					operands[1] = operands[1].trim();
					if (operands[1].charAt(0) == '"'
							&& operands[1].charAt(operands[1].length() - 1) == '"') {
						operands[1] = operands[1].substring(1,
								operands[1].length() - 1);
					}
					rightOperand = operands[1];
				}
			} catch (Exception e) {
				rightOperand = "";
			}

			LOGGER.trace("Left operand is [" + leftOperand
					+ "], right operand is [" + rightOperand + "]");

			// We need to check if both operands eval to a number or not. If
			// they eval to a number, we should do numeric comparison, else
			// string comparison
			try {
				if (Double.parseDouble(leftOperand) == Double
						.parseDouble(rightOperand)) {
					condition = true;
				} else {
					condition = false;
				}

			} catch (NumberFormatException e) {
				// ah, atleast one of the operands is not numeric. Let us do
				// string comparison and return results...
				if (leftOperand.compareToIgnoreCase(rightOperand) == 0) {
					condition = true;
				} else {
					condition = false;
				}
			}
		} else if (expression.contains("<")) {

			LOGGER.trace("User has specified 'less than operator' (<)");

			// Split the expression based on operator to extract left and right
			// operands
			operands = expression.split("<");

			String leftOperand = "";
			if (operands[0].length() > 0) {
				operands[0] = operands[0].trim();
				if (operands[0].charAt(0) == '"'
						&& operands[0].charAt(operands[0].length() - 1) == '"') {
					operands[0] = operands[0].substring(1,
							operands[0].length() - 1);
				}
				leftOperand = operands[0];
			}

			String rightOperand = "";
			try {
				if (operands[1].length() > 0) {
					operands[1] = operands[1].trim();
					if (operands[1].charAt(0) == '"'
							&& operands[1].charAt(operands[1].length() - 1) == '"') {
						operands[1] = operands[1].substring(1,
								operands[1].length() - 1);
					}
					rightOperand = operands[1];
				}
			} catch (Exception e) {
				rightOperand = "";
			}
			LOGGER.trace("Left operand is [" + leftOperand
					+ "], right operand is [" + rightOperand + "]");

			if (Double.parseDouble(leftOperand) < Double
					.parseDouble(rightOperand)) {
				condition = true;
			} else {
				condition = false;
			}
		} else if (expression.contains(">")) {

			LOGGER.trace("User has specified 'greater than operator' (>)");

			// Split the expression based on operator to extract left and right
			// operands
			operands = expression.split(">");

			String leftOperand = "";
			if (operands[0].length() > 0) {
				operands[0] = operands[0].trim();
				if (operands[0].charAt(0) == '"'
						&& operands[0].charAt(operands[0].length() - 1) == '"') {
					operands[0] = operands[0].substring(1,
							operands[0].length() - 1);
				}
				leftOperand = operands[0];
			}

			String rightOperand = "";
			try {
				if (operands[1].length() > 0) {
					operands[1] = operands[1].trim();
					if (operands[1].charAt(0) == '"'
							&& operands[1].charAt(operands[1].length() - 1) == '"') {
						operands[1] = operands[1].substring(1,
								operands[1].length() - 1);
					}
					rightOperand = operands[1];
				}
			} catch (Exception e) {
				rightOperand = "";
			}

			LOGGER.trace("Left operand is [" + leftOperand
					+ "], right operand is [" + rightOperand + "]");

			if (Double.parseDouble(leftOperand) > Double
					.parseDouble(rightOperand)) {
				condition = true;
			} else {
				condition = false;
			}
		}

		return condition;
	}

	/**
	 * Copy data. from test data to dynamic variable from dynamic variable to
	 * dynamic variable from dynamic variable/test data to test data from static
	 * text to dynamic variable
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param value
	 *            the value
	 * @throws AFTException
	 *             the application exception
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 * @throws InstantiationException
	 *             the instantiation exception
	 * @throws IllegalAccessException
	 *             the illegal access exception
	 */
	public void copyData(TestStepRunner testStepRunner, String value)
			throws AFTException, IOException, InstantiationException,
			IllegalAccessException {

		LOGGER.debug("Executing [copyData] with value [" + value + "]");

		String testDataStartVarIdentifier = Constants.TESTDATASTARTVARIABLEIDENTIFIER;
		String testDataEndVarIdentifier = Constants.TESTDATAENDVARIABLEIDENTIFIER;
		String dynamicVarDelimiter = Constants.DYNAMICVARIABLEDELIMITER;

		try {
			List<String> arrParam = Helper.getInstance()
					.parseActionParameterList(
							testStepRunner.getTestSuiteRunner(), value, false);
			if (arrParam.size() > 2) {
				String errMsg = "User specified invalid # of parameters ["
						+ value
						+ "] with [copyData] action. Please refer to technical documentation on how to use [copyData] command";
				LOGGER.error(errMsg);

				throw new AFTException(errMsg);
			}

			if ((arrParam.size() < 2) || (arrParam.get(0).trim().length() <= 0)
					|| (arrParam.get(1).trim().length() <= 0)) {
				String errMsg = "Invalid value ["
						+ value
						+ "] specified. Could not retrieve source and target to complete [copyData] action";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}

			String sourceValue = arrParam.get(0).trim();
			String targetValue = arrParam.get(1).trim();
			String extractedActualValue;

			// Find if the first parameter is a variable or test data
			// identifier,
			// then replace with its actual value
			if ((sourceValue.startsWith(testDataStartVarIdentifier) && sourceValue
					.endsWith(testDataEndVarIdentifier))
					|| (sourceValue.startsWith(dynamicVarDelimiter) && sourceValue
							.endsWith(dynamicVarDelimiter))) {
				LOGGER.trace("Source value ["
						+ sourceValue
						+ "] is a variable or test data identifier. Replacing it with actual value");
				extractedActualValue = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(), sourceValue);
				LOGGER.debug("Source value ["
						+ sourceValue
						+ "] is a variable or test data identifier. Actual value is ["
						+ extractedActualValue + "]");
			} else {
				LOGGER.trace("Source value " + sourceValue
						+ " is a static value. Extracting the actual value");
				extractedActualValue = sourceValue.substring(2,
						sourceValue.length() - 2);
				extractedActualValue = Helper.getInstance().getActionValue(
						testStepRunner.getTestSuiteRunner(),
						extractedActualValue);
				LOGGER.debug("Source value " + sourceValue
						+ " is a static value. Extracted value is ["
						+ extractedActualValue + "]");
			}

			// Is targetvalue a variable
			if (targetValue.startsWith(dynamicVarDelimiter)
					&& targetValue.endsWith(dynamicVarDelimiter)) {
				Variable.getInstance().setVariableValue(
						testStepRunner.getTestSuiteRunner(), "CopyData",
						targetValue, false, extractedActualValue);
				LOGGER.info("Copied value [" + extractedActualValue
						+ "] to dynamic variable [" + targetValue + "]");
			}

			// Is targetvalue a test data identifier
			else if (targetValue.startsWith(testDataStartVarIdentifier)
					&& targetValue.endsWith(testDataEndVarIdentifier)) {

				String testDataParameters = targetValue.replace(".", "~")
						.replace("${", "").replace("}$", "");
				String[] sheetNameColumnNameRowNum = testDataParameters
						.split("~");

				// Replace single quotes (') to be able to parse clean sheetname
				// and column header names
				//
				for (int i = 0; i < sheetNameColumnNameRowNum.length; i++) {
					if ((sheetNameColumnNameRowNum[i].charAt(0) == '\'')
							&& (sheetNameColumnNameRowNum[i]
									.charAt(sheetNameColumnNameRowNum[i]
											.length() - 1) == '\'')) {
						sheetNameColumnNameRowNum[i] = sheetNameColumnNameRowNum[i]
								.substring(
										1,
										sheetNameColumnNameRowNum[i].length() - 1);
					}
				}
				String sheetName = sheetNameColumnNameRowNum[0].toLowerCase();
				String columnName = sheetNameColumnNameRowNum[1];

				int testDataRowId = -1;
				if (sheetNameColumnNameRowNum.length > 2) {
					// looks like user specified the rowid along
					// with test data identifier, let us use it
					// instead
					String strRowId = Helper.getInstance().getActionValue(
							testStepRunner.getTestSuiteRunner(),
							sheetNameColumnNameRowNum[2]);
					LOGGER.debug("user has specified test data row id ["
							+ strRowId
							+ "] along with test data identified. Using the specified rowid");
					testDataRowId = Integer.parseInt(strRowId);
				} else {
					// oops, user did not specify the rowid along
					// with test data identifier, let us pick the
					// rowid from the one maintained by system
					testDataRowId = testStepRunner.getTestSuiteRunner()
							.getTestDataCurrentRowId(
									sheetNameColumnNameRowNum[0].toLowerCase());
				}
				LOGGER.debug("Test Data RowId is [" + testDataRowId + "]");

				LOGGER.debug("Copying data to Test Data identifier sheet ["
						+ sheetName + "], column name [" + columnName
						+ "], row number [" + testDataRowId + "]");

				testStepRunner
						.getTestSuiteRunner()
						.getTestDataReader()
						.writeToExcel(sheetName, columnName,
								Integer.toString(testDataRowId),
								extractedActualValue);

				LOGGER.info("Copied value [" + extractedActualValue
						+ "] to test data identifier [" + sheetName + '.'
						+ columnName + '.' + testDataRowId + "]");
			} else {
				String errMsg = "Invalid target ["
						+ targetValue
						+ "] specified for [copyData]. Please refer to documentation on how to use [copyData] action";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Gets the operand max decimal places.
	 * 
	 * @param operands
	 *            the operands
	 * @return the operand max decimal places
	 */
	private int getOperandMaxDecimalPlaces(String[] operands) {
		int decimalPlaces = 0;
		for (int i = 0; i < operands.length; i++) {
			if (operands[i].indexOf('.') > 0) {
				int index = operands[i].indexOf('.') + 1;
				if ((operands[i].length() - index) > decimalPlaces) {
					decimalPlaces = operands[i].length() - index;
				}
			}
		}

		return decimalPlaces;
	}

	/**
	 * Checks if is operand decision.
	 * 
	 * @param operands
	 *            the operands
	 * @return true, if is operand decision
	 */
	@SuppressWarnings("unused")
	private boolean isOperandDecision(String[] operands) {
		boolean isDecimal = false;
		for (int i = 0; i < operands.length; i++) {
			double dblValue = Double.parseDouble(operands[i]);
			int intValue = (int) dblValue;

			if ((dblValue - intValue) > 0) {
				isDecimal = true;
				break;
			}
		}

		return isDecimal;
	}

	/**
	 * operatorAdd command: Adds the given number of values and stores in the
	 * specified element.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable name to store result
	 * @param value
	 *            : operands to be added
	 * @return added value
	 * @throws AFTException
	 *             the application exception
	 */
	public String operatorAdd(TestStepRunner testStepRunner,
			String returnVariable, String value) throws AFTException {
		String[] operands = null;
		double result = 0;
		String returnValue = "";
		boolean isDecimal = false;
		DecimalFormat df = null;
		// Checking whether values are passed or not
		if (value != null && !value.equals("")) {
			operands = value.split(",");
			for (String operand : operands) {
				if (operand.indexOf('.') > 0) {
					isDecimal = true;
					break;
				}
			}
			if (isDecimal) {
				int decimalplaces = getOperandMaxDecimalPlaces(operands);
				String decimalFormat = "#.";
				for (int i = 0; i < decimalplaces; i++) {
					decimalFormat += "0";
				}
				df = new DecimalFormat(decimalFormat);
			}

			try {
				result = Double.parseDouble(operands[0]);
				String prevValue;
				for (int i = 1; i < operands.length; i++) {
					if (isDecimal) {
						prevValue = df.format(result);
					} else {
						prevValue = Integer.toString((int) result);
					}
					result = result + Double.parseDouble(operands[i]);

					if (isDecimal) {
						returnValue = df.format(result);
					} else {
						returnValue = Integer.toString((int) result);
					}

					LOGGER.debug("Operator+, Operand [" + prevValue + " + "
							+ operands[i] + "], Result is [" + returnValue
							+ "]");
				}

				/*
				 * if element name contains dynamic variable delimiter set the
				 * return value to the dynamic variable.
				 */
				if (returnVariable != null) {
					LOGGER.debug("Assigning result [" + returnValue
							+ "] to user variable [" + returnVariable + "]");
					Variable.getInstance().setVariableValue(
							testStepRunner.getTestSuiteRunner(), "operatorAdd",
							returnVariable, false, returnValue);
				}

			} catch (NumberFormatException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} catch (ArithmeticException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		} else {
			String errMsg = "No values are passed for operator+ action. Please refer to documentation on how to use operator+ action.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
		return returnValue;
	} // End of operator+ action

	/**
	 * operatorSub command: Subtracts the given number of values and stores in
	 * the specified element.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable name to store result
	 * @param value
	 *            : operands to be subtracted
	 * @return subracted value
	 * @throws AFTException
	 *             the application exception
	 */
	public String operatorSub(TestStepRunner testStepRunner,
			String returnVariable, String value) throws AFTException {
		String[] operands = null;
		double result = 0;
		String returnValue = "";
		boolean isDecimal = false;
		DecimalFormat df = null;
		// Checking whether values are passed or not
		if (value != null && !value.equals("")) {
			operands = value.split(",");
			for (String operand : operands) {
				if (operand.indexOf('.') > 0) {
					isDecimal = true;
					break;
				}
			}
			if (isDecimal) {
				int decimalplaces = getOperandMaxDecimalPlaces(operands);
				String decimalFormat = "#.";
				for (int i = 0; i < decimalplaces; i++) {
					decimalFormat += "0";
				}
				df = new DecimalFormat(decimalFormat);
			}
			try {
				result = Double.parseDouble(operands[0]);
				String prevValue;
				for (int i = 1; i < operands.length; i++) {
					if (isDecimal) {
						prevValue = df.format(result);
					} else {
						prevValue = Integer.toString((int) result);
					}
					result = result - Double.parseDouble(operands[i]);

					if (isDecimal) {
						returnValue = df.format(result);
					} else {
						returnValue = Integer.toString((int) result);
					}
					LOGGER.debug("Operator-, Operand [" + prevValue + " - "
							+ operands[i] + "], Result is [" + returnValue
							+ "]");
				}

				/*
				 * if element name contains dynamic variable delimiter set the
				 * return value to the dynamic variable.
				 */
				if (returnVariable != null) {
					LOGGER.debug("Assigning result [" + returnValue
							+ "] to user variable [" + returnVariable + "]");
					Variable.getInstance().setVariableValue(
							testStepRunner.getTestSuiteRunner(), "operatorSub",
							returnVariable, false, returnValue);
				}

			} catch (NumberFormatException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} catch (ArithmeticException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		} else {
			String errMsg = "No values are passed for operator- action. Please refer to documentation on how to use operator- action.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}
		return returnValue;
	} // End of operator- action

	/**
	 * operatorMul command: Multiplies the given number of values and stores in
	 * the specified element.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable name to store result
	 * @param value
	 *            : operands to be multiplied
	 * @return multiplied value
	 * @throws AFTException
	 *             the application exception
	 */
	public String operatorMul(TestStepRunner testStepRunner,
			String returnVariable, String value) throws AFTException {
		String[] operands = null;
		double result = 0;
		String returnValue = "";

		boolean isDecimal = false;
		DecimalFormat df = null;
		// Checking whether values are passed or not
		if (value != null && !value.equals("")) {
			operands = value.split(",");
			for (String operand : operands) {
				if (operand.indexOf('.') > 0) {
					isDecimal = true;
					break;
				}
			}
			if (isDecimal) {
				int decimalplaces = getOperandMaxDecimalPlaces(operands);
				String decimalFormat = "#.";
				for (int i = 0; i < decimalplaces; i++) {
					decimalFormat += "0";
				}
				df = new DecimalFormat(decimalFormat);
			}
			try {
				result = Double.parseDouble(operands[0]);
				String prevValue;
				for (int i = 1; i < operands.length; i++) {
					if (isDecimal) {
						prevValue = df.format(result);
					} else {
						prevValue = Integer.toString((int) result);
					}
					result = result * Double.parseDouble(operands[i]);

					if (isDecimal && result != 0) {
						returnValue = df.format(result);
					} else {
						returnValue = Integer.toString((int) result);
					}
					LOGGER.debug("Operator*, Operand [" + prevValue + " X "
							+ operands[i] + "], Result is [" + returnValue
							+ "]");
				}

				/*
				 * if element name contains dynamic variable delimiter set the
				 * return value to the dynamic variable.
				 */
				if (returnVariable != null) {
					LOGGER.debug("Assigning result [" + returnValue
							+ "] to user variable [" + returnVariable + "]");
					Variable.getInstance().setVariableValue(
							testStepRunner.getTestSuiteRunner(), "operatorMul",
							returnVariable, false, returnValue);
				}

			} catch (NumberFormatException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} catch (ArithmeticException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		} else {
			String errMsg = "No values are passed for operator* action. Please refer to documentation on how to use operator* action.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}

		return returnValue;
	} // End of operator* action

	/**
	 * operatorDiv command: Divides the given number of values and stores in the
	 * specified element.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param returnVariable
	 *            variable name to store result
	 * @param value
	 *            : operands to be divided
	 * @return result of division
	 * @throws AFTException
	 *             the application exception
	 */
	public String operatorDiv(TestStepRunner testStepRunner,
			String returnVariable, String value) throws AFTException {
		String[] operands = null;
		double result = 0;
		String returnValue = "";
		boolean isDecimal = false;
		DecimalFormat df = null;
		// Checking whether values are passed or not
		if (value != null && !value.equals("")) {
			operands = value.split(",");
			for (String operand : operands) {
				if (operand.indexOf('.') > 0) {
					isDecimal = true;
					break;
				}
			}
			if (isDecimal) {
				int decimalplaces = getOperandMaxDecimalPlaces(operands);
				String decimalFormat = "#.";
				for (int i = 0; i < decimalplaces; i++) {
					decimalFormat += "0";
				}
				df = new DecimalFormat(decimalFormat);
			}
			try {
				result = Double.parseDouble(operands[0]);
				String prevValue;
				for (int i = 1; i < operands.length; i++) {
					if (isDecimal) {
						prevValue = df.format(result);
					} else {
						prevValue = Integer.toString((int) result);
					}
					result = result / Double.parseDouble(operands[i]);

					if (isDecimal && result != 0) {
						returnValue = df.format(result);
					} else {
						returnValue = Integer.toString((int) result);
					}
					LOGGER.debug("Operator/, Operand [" + prevValue + " / "
							+ operands[i] + "], Result is [" + returnValue
							+ "]");
				}
				/*
				 * if element name contains dynamic variable delimiter set the
				 * return value to the dynamic variable.
				 */
				if (returnVariable != null) {
					LOGGER.debug("Assigning result [" + returnValue
							+ "] to user variable [" + returnVariable + "]");
					Variable.getInstance().setVariableValue(
							testStepRunner.getTestSuiteRunner(), "operatorDiv",
							returnVariable, false, returnValue);
				}

			} catch (NumberFormatException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			} catch (ArithmeticException e) {
				LOGGER.error("Exception::", e);
				throw new AFTException(e);
			}
		} else {
			String errMsg = "No values are passed for operator/ action. Please refer to documentation on how to use operator/ action.";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}

		return returnValue;
	} // End of operator/ action

	/**
	 * Function to run action Reusable Business Scenario.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param businessScenarioId
	 *            reusable business scenario to execute
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void executeReusableTestScenario(TestStepRunner testStepRunner,
			String businessScenarioId) throws AFTException {

		String curResuableBussScenarioId = "";
		String curResuableBussScenarioDesc = "";

		LOGGER.info("Executing action [executeReusableTestScenario] for test scenario ["
				+ businessScenarioId + "]");

		if ((testStepRunner.getTestSuiteRunner()
				.getReusableTestSuiteScenarios() == null)
		/*
		 * || (testStepRunner.getTestSuiteRunner() .getReusableTestSuiteSteps()
		 * == null)
		 */
		) {
			String errMsg = "Reusable test suite has not been specified OR could not be loaded. Please check your configuration file and logs";
			LOGGER.error(errMsg);
			throw new AFTException(errMsg);
		}

		try {
			// Let us save the current value of these two system variables so
			// that we can restore them after execution of the reusable scenario
			curResuableBussScenarioId = Helper.getInstance().getActionValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_CURBUSINESSSCENARIOID));
			curResuableBussScenarioDesc = Helper
					.getInstance()
					.getActionValue(
							Variable.getInstance()
									.generateSysVarName(
											SystemVariables.AFT_CURRESBUSINESSSCENARIODESCRIPTION));

			// To call executeSingleBusinessScenario for reusable scenario
			LOGGER.info("Starting execution of business scenario id ["
					+ businessScenarioId
					+ "] and associated test steps in test suite file ["
					+ testStepRunner.getTestSuiteRunner()
							.getReusableTestSuitePath() + "]");
			TestScenario testScenario = testStepRunner.getTestSuiteRunner()
					.getReusableTestScenario(businessScenarioId);
			if (testScenario != null) {
				testStepRunner.getTestSuiteRunner()
						.executeSingleBusinessScenario(testScenario, true);
			} else {
				String errMsg = "Invalid reusable scenario ["
						+ businessScenarioId + "] .";
				LOGGER.error(errMsg);
				throw new AFTException(errMsg);
			}
		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		} finally {
			// Now let us restore the value back
			Variable.getInstance().setVariableValue(
					Variable.getInstance().generateSysVarName(
							SystemVariables.AFT_CURRESBUSINESSSCENARIOID),
					true, curResuableBussScenarioId);

			Variable.getInstance()
					.setVariableValue(
							Variable.getInstance()
									.generateSysVarName(
											SystemVariables.AFT_CURRESBUSINESSSCENARIODESCRIPTION),
							true, curResuableBussScenarioDesc);
		}
	}

	/**
	 * Function to return Test Data Row Count.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param sheetValue
	 *            the sheet value
	 * @throws AFTException
	 *             the aFT exception
	 */
	public void getTestDataRowCount(TestStepRunner testStepRunner,
			String sheetValue) throws AFTException {

		LOGGER.info("Executing action [getTestDataRowCount] for sheet ["
				+ sheetValue + "]");
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		String columnHeader = "";
		int rowCount = 0;
		// Annotation for getting row count for a given sheetname and column
		String[] values = sheetValue.split(",");
		String sheetName = values[0].trim().toLowerCase();
		if (values.length == 2) {
			columnHeader = values[1].trim();
		}
		if (isFileSystemRequest) {
			// get the row count for the given sheet and column header
			rowCount = testStepRunner
					.getTestSuiteRunner()
					.getTestDataReader()
					.getRowCount(
							testStepRunner.getTestSuiteRunner()
									.getTestDataReader().getTargetHandle(),
							sheetName, columnHeader);
		} else {
			String name = sheetName.toLowerCase();
			name = new StringBuffer(name.length())
					.append(Character.toTitleCase(name.charAt(0)))
					.append(name.substring(1)).toString();
			String projectId = RuntimeProperties.getInstance().getProjectId();
			StringBuffer tableName = new StringBuffer();
			tableName.append(Constants.TABLEPREFIX).append(projectId)
					.append("_").append(name);
			rowCount = DatabaseUtil.getInstance().getTestDataRowCount(
					tableName.toString());
		}

		// convert the rowcount to String
		String value = Integer.toString(rowCount);

		LOGGER.info("Set total test data rows as [" + value + "] for sheet ["
				+ sheetName + "], ColumnHeader [" + columnHeader + "]");

		// Set the system variable AFT_TestDataRowCount
		Variable.getInstance().setVariableValue(
				Variable.getInstance().generateSysVarName(
						SystemVariables.AFT_TESTDATAROWCOUNT), true, value);
	}

	/**
	 * Function to get row number based on specific text in the specified column
	 * using column header.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param parameterList
	 *            parameter list containing sheet name, column header, text to
	 *            search for and start row id (optional)
	 * @return row number if text found in specified column or -1
	 * @throws AFTException
	 *             the aFT exception
	 */
	public int getTestDataRowNumber(TestStepRunner testStepRunner,
			String parameterList) throws AFTException {
		int retVal = -1;
		boolean isFileSystemRequest = Helper.getInstance()
				.isFileSystemRequest();

		String startRowId, searchText, columnHeader, sheetName = "";
		int testDataRowNumber = 0;

		try {
			List<String> paramArray = Helper.getInstance()
					.parseActionParameterList(
							testStepRunner.getTestSuiteRunner(), parameterList,
							true);
			sheetName = paramArray.get(0).toLowerCase();
			columnHeader = paramArray.get(1);
			searchText = paramArray.get(2);
			if (isFileSystemRequest) {
				LOGGER.info("Executing action [getTestDataRowNumber] for sheet ["
						+ parameterList + "]");
				if (paramArray.size() == 3) {
					// get the row count for the given sheet and column header
					int rowCount = testStepRunner
							.getTestSuiteRunner()
							.getTestDataReader()
							.getRowCount(
									testStepRunner.getTestSuiteRunner()
											.getTestDataReader()
											.getTargetHandle(), sheetName,
									columnHeader);
					for (testDataRowNumber = 1; testDataRowNumber <= rowCount; testDataRowNumber++) {
						String cellValue = testStepRunner
								.getTestSuiteRunner()
								.getTestDataReader()
								.getColumnData(sheetName, testDataRowNumber,
										columnHeader);
						if (cellValue.equalsIgnoreCase(searchText)) {
							retVal = testDataRowNumber;
							break;
						}
					}
				} else if (paramArray.size() == 4) {

					startRowId = paramArray.get(3);

					// get the row count for the given sheet and column header
					int rowCount = testStepRunner
							.getTestSuiteRunner()
							.getTestDataReader()
							.getRowCount(
									testStepRunner.getTestSuiteRunner()
											.getTestDataReader()
											.getTargetHandle(), sheetName,
									columnHeader);
					int initialRowId = Integer.parseInt(startRowId);
					for (testDataRowNumber = initialRowId; testDataRowNumber <= rowCount; testDataRowNumber++) {
						String cellValue = testStepRunner
								.getTestSuiteRunner()
								.getTestDataReader()
								.getColumnData(sheetName, testDataRowNumber,
										columnHeader);
						if (cellValue.equalsIgnoreCase(searchText)) {
							retVal = testDataRowNumber;
							break;
						}
					}
				} else {
					String errMsg = "User has specified invalid number of parameters. User must specify atleast sheetName, ColumnHeader, search text in this sequence."
							+ " Optionally user can also specify start Row Id. Please go through xAFT documentation for more details";
					LOGGER.error(errMsg);
					throw new AFTException(errMsg);
				}
			} else {
				LOGGER.info("Executing action [getTestDataRowNumber] for table ["
						+ parameterList + "]");
				String name = sheetName.toLowerCase();
				name = new StringBuffer(name.length())
						.append(Character.toTitleCase(name.charAt(0)))
						.append(name.substring(1)).toString();
				String projectId = RuntimeProperties.getInstance()
						.getProjectId();
				StringBuffer tableName = new StringBuffer();
				tableName.append(Constants.TABLEPREFIX).append(projectId)
						.append("_").append(name);
				boolean flag = false;
				if (paramArray.size() == 4) {
					flag = true;
				}
				retVal = DatabaseUtil.getInstance().getTestDataRowNumber(
						tableName.toString(), columnHeader, searchText, flag,
						paramArray);
			}
		} catch (IndexOutOfBoundsException io) {
			LOGGER.error("Exception::", io);
			throw new AFTException(io);
		}

		return retVal;
	}

	/**
	 * Function to run action getCurrentTestDataRow.
	 * 
	 * @param testStepRunner
	 *            test step runner object
	 * @param sheetValue
	 *            sheet name
	 * @return current test data row number
	 * @throws AFTException
	 *             the aFT exception
	 */
	public String getCurrentTestDataRow(TestStepRunner testStepRunner,
			String sheetValue) throws AFTException {
		LOGGER.info("Executing action [getCurrentTestDataRow] with sheet name ["
				+ sheetValue.toLowerCase() + "]");

		String value = "";
		try {
			// Get the current test data row value...
			int testDataRowId = testStepRunner.getTestSuiteRunner()
					.getTestDataCurrentRowId(sheetValue.toLowerCase());

			// convert the rowcount to String
			value = Integer.toString(testDataRowId);

			LOGGER.info("Current test data rowid is [" + value
					+ "] for sheet [" + sheetValue.toLowerCase() + "]");
		} catch (Exception e) {
			LOGGER.error("Exception:: ", e);
			throw new AFTException(e);
		}

		return value;
	}

	/**
	 * Log test result in Test Case Management Tool.
	 * 
	 * @throws AFTException
	 *             the application exception
	 */
	public void logTestExecutionResults() throws AFTException {

		if (ConfigProperties.getInstance()
				.getConfigProperty(ConfigProperties.TCM_INTEGRATION)
				.equalsIgnoreCase("YES")) {
			Helper.getInstance().logTestExecutionResults();
		} else {
			LOGGER.debug("TCM integration is not enabled. "
					+ "Test results will not be logged into TCM tool. Continuing test execution...");
		}

	}

	/**
	 * Reads the data into an array.
	 * 
	 * @param testStepRunner
	 *            the testStepRunner
	 * @param userReturnVariable
	 *            the userReturnVariable
	 * @param parameterList
	 *            parameterList
	 * @param parsedList
	 *            parsedList
	 * @return the column index
	 * @throws AFTException
	 *             the application exception
	 */
	public int readDataToArray(TestStepRunner testStepRunner,
			String userReturnVariable, String parameterList, String parsedList)
			throws AFTException {

		String errorMessage;
		String escapeChar = "\\";

		// if user did not pass return variable
		if (userReturnVariable.isEmpty()
				|| userReturnVariable.equalsIgnoreCase("novalue")) {
			errorMessage = "No return variable passed."
					+ " Please refer to wiki on [excelTableDataToArray] usage.";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		// split parameterList
		String[] splitParams = parsedList.trim().split(
				escapeChar + Constants.ATTRIBUTESDELIMITER);

		// if user did not pass all parameters
		if (splitParams.length != PARAMETER_LIST) {
			errorMessage = "Insufficient parameters passed."
					+ " Please refer to wiki on [excelTableDataToArray] usage.";
			LOGGER.error(errorMessage);
			throw new AFTException(errorMessage);
		}

		try {

			String pipeSymbol = "|";
			String carrotSymbol = "^";
			String commaSymbol = ",";
			String hiphenSymbol = "-";
			String strConstAll = "all";
			String strConstZeroNum = "0";
			int minRowNum = 1;

			String headerName = null;
			int loopCounter = 0;
			int firstRowNum = 0;
			int returnValue = 0;
			ExcelRead excelReader = null;
			String[][] arrayTableData = null;
			String[] multipleColumnHeaders = null;
			List<String> columnHeaders = null;
			boolean isFileSystemRequest = Helper.getInstance()
					.isFileSystemRequest();
			StringBuffer tableName = null;
			// read sheet name
			String sheetName = splitParams[SHEETNAMEPARAM].toLowerCase();
			LOGGER.info("User passed sheet name as [" + sheetName + "]");

			// get column header names
			LOGGER.trace("Getting column headers");
			if (isFileSystemRequest) {
				excelReader = testStepRunner.getTestSuiteRunner()
						.getTestDataReader();
				columnHeaders = excelReader.getColumnNames(
						excelReader.getTargetHandle(), sheetName);
			} else {
				String name = sheetName.toLowerCase();
				name = new StringBuffer(name.length())
						.append(Character.toTitleCase(name.charAt(0)))
						.append(name.substring(1)).toString();
				String projectId = RuntimeProperties.getInstance()
						.getProjectId();
				tableName = new StringBuffer();
				tableName.append(Constants.TABLEPREFIX).append(projectId)
						.append("_").append(name);
				columnHeaders = DatabaseUtil.getInstance().getColumnNames(
						tableName.toString());
			}

			String testDataColumnDelimiter = ConfigProperties.getInstance()
					.getConfigProperty(
							ConfigProperties.TEST_DATA_COLUMN_DELIMITER);

			String escaptedTestDataColumnDelimiter = testDataColumnDelimiter;

			// For Pipe/Carrot, add a escape character so that
			// split fn could work correctly
			if (testDataColumnDelimiter.equalsIgnoreCase(pipeSymbol)
					|| testDataColumnDelimiter.equalsIgnoreCase(carrotSymbol)) {
				escaptedTestDataColumnDelimiter = escapeChar
						+ testDataColumnDelimiter;
			}

			arrayTableData = new String[maxRows][maxColumns];

			int colIndex = 0;

			// store the header names
			for (loopCounter = 0; loopCounter < maxColumns; loopCounter++) {
				try {
					headerName = columnHeaders.get(loopCounter);
				} catch (IndexOutOfBoundsException iobe) {
					break;
				}
				// if header has multiple header names
				if (StringUtils.isNotBlank(testDataColumnDelimiter)
						&& headerName.contains(testDataColumnDelimiter)) {
					multipleColumnHeaders = headerName.trim().split(
							escaptedTestDataColumnDelimiter);
					for (int j = 0; j < multipleColumnHeaders.length; j++) {
						arrayTableData[firstRowNum][colIndex] = multipleColumnHeaders[j];
						colIndex++;
					}
				} else {
					arrayTableData[firstRowNum][colIndex] = headerName;
					colIndex++;
				}

			}

			int excelColCount = colIndex;
			LOGGER.trace("Total number of columns in excel sheet ["
					+ excelColCount + "]");

			// parse user reqd column details
			String columnDetails = splitParams[COLDETAILSPARAM];

			String reqdColumns = columnDetails.substring(COLUMNDETAILSIDX,
					columnDetails.length() - 1);

			errorMessage = "Column number cannot be less than 1.";

			if (reqdColumns.equals(strConstZeroNum)) {
				LOGGER.warn(errorMessage);
				return returnValue;
			}

			int userReqdColCount = 0;
			int[] colNumbers = new int[maxColumns];

			LOGGER.info("User wants column [" + reqdColumns + "] to be read");

			// user wants all the columns data
			if (reqdColumns.trim().equalsIgnoreCase(strConstAll)) {
				for (loopCounter = 0; loopCounter < excelColCount; loopCounter++) {
					colNumbers[loopCounter] = loopCounter + 1;
					userReqdColCount++;
				}

				// user wants multiple columns data - comma seperated
			} else if (reqdColumns.trim().contains(commaSymbol)) {
				String[] parseColNumbers = reqdColumns.split(commaSymbol);
				String currentColumn;
				for (loopCounter = 0; loopCounter < parseColNumbers.length; loopCounter++) {
					currentColumn = parseColNumbers[loopCounter];
					if (currentColumn.equals(strConstZeroNum)) {
						LOGGER.warn(errorMessage);
						return returnValue;
					}
					colNumbers[loopCounter] = Integer.parseInt(currentColumn);
					userReqdColCount++;
				}

				// user wants a multiple columns data - hiffen seperated
			} else if (reqdColumns.trim().contains(hiphenSymbol)) {
				String[] parseColNumbers = reqdColumns.split(hiphenSymbol);
				int startingCol = Integer.parseInt(parseColNumbers[0]);
				if (startingCol < minRowNum) {
					LOGGER.warn(errorMessage);
					return returnValue;
				}
				int endingCol = Integer.parseInt(parseColNumbers[1]);
				if (endingCol < minRowNum) {
					LOGGER.warn(errorMessage);
					return returnValue;
				} else if (endingCol < startingCol) {
					LOGGER.warn("Invalid range of column numbers");
					return returnValue;
				}
				for (loopCounter = 0; loopCounter <= endingCol - startingCol; loopCounter++) {
					colNumbers[loopCounter] = startingCol + loopCounter;
					userReqdColCount++;
				}

				// user wants a single column data
			} else {
				colNumbers[0] = Integer.parseInt(reqdColumns);
				userReqdColCount++;
			}

			// parse row details
			String rowDetails = splitParams[ROWDETAILSPARAM];

			String reqdRows = rowDetails.substring(ROWDETAILSIDX,
					rowDetails.length() - 1);

			errorMessage = "Row number cannot be less than 1.";

			if (reqdRows.equals(strConstZeroNum)) {
				LOGGER.warn(errorMessage);
				return returnValue;
			}

			int excelRowCount = 0;
			int userReqdRowCount = 0;
			int[] rowNumbers = new int[maxRows];

			LOGGER.trace("Getting table row count");
			if (isFileSystemRequest) {
				excelRowCount = excelReader.getRowCount(
						excelReader.getTargetHandle(), sheetName);
				excelRowCount = excelRowCount - 1;
			} else {
				excelRowCount = DatabaseUtil.getInstance().getTestDataRowCount(
						tableName.toString());
			}

			LOGGER.trace("Total number of rows in excel sheet ["
					+ excelRowCount + "]");

			LOGGER.info("User wants row [" + reqdRows + "] to be read");

			// user wants all the rows data
			if (reqdRows.trim().equalsIgnoreCase(strConstAll)) {
				for (loopCounter = 0; loopCounter < excelRowCount; loopCounter++) {
					rowNumbers[loopCounter] = loopCounter + 1;
					userReqdRowCount++;
				}

				// user wants multiple rows data - comma seperated
			} else if (reqdRows.trim().contains(commaSymbol)) {
				String[] parseRowNumbers = reqdColumns.split(commaSymbol);
				String currentRow;
				for (loopCounter = 0; loopCounter < parseRowNumbers.length; loopCounter++) {
					currentRow = parseRowNumbers[loopCounter];

					if (currentRow.equals(strConstZeroNum)) {
						LOGGER.warn(errorMessage);
						return returnValue;
					}

					rowNumbers[loopCounter] = Integer.parseInt(currentRow);
					userReqdRowCount++;
				}

				// user wants a multiple rows data - hiffen seperated
			} else if (reqdRows.trim().contains(hiphenSymbol)) {
				String[] parseRowNumbers = reqdRows.split(hiphenSymbol);
				int startingRow = Integer.parseInt(parseRowNumbers[0]);
				if (startingRow < minRowNum) {
					LOGGER.warn(errorMessage);
					return returnValue;
				}
				int endingRow = Integer.parseInt(parseRowNumbers[1]);
				if (endingRow < minRowNum) {
					LOGGER.warn(errorMessage);
					return returnValue;
				} else if (endingRow < startingRow) {
					LOGGER.warn("Invalid range of row numbers");
					return returnValue;
				}
				for (loopCounter = 0; loopCounter <= endingRow - startingRow; loopCounter++) {
					rowNumbers[loopCounter] = startingRow + loopCounter;
					userReqdRowCount++;
				}

				// user wants a single row data
			} else {

				rowNumbers[0] = Integer.parseInt(reqdRows);
				userReqdRowCount++;
			}

			int excelRowNum, excelColNum;

			// store the cells data
			for (loopCounter = 0; loopCounter < userReqdRowCount; loopCounter++) {

				excelRowNum = rowNumbers[loopCounter];
				// cannot store more than maxRows rows
				if (excelRowNum >= maxRows) {
					LOGGER.warn("Only the first " + maxRows
							+ " rows data can be stored.");
					break;
				}

				for (int colCounter = 0; colCounter < userReqdColCount; colCounter++) {
					excelColNum = colNumbers[colCounter];
					// cannot store more than maxColumns columns
					if (excelColNum >= maxColumns) {
						LOGGER.warn("Only the first " + maxColumns
								+ " columns data can be stored.");
						break;
					}
					headerName = arrayTableData[0][excelColNum - 1];
					if (isFileSystemRequest) {
						arrayTableData[excelRowNum][excelColNum] = excelReader
								.getColumnData(sheetName, excelRowNum,
										headerName);
					} else {
						arrayTableData[excelRowNum][excelColNum] = DatabaseUtil
								.getInstance().getTestDataValueFromDB(
										tableName.toString(), headerName,
										excelRowNum);
					}
				}

			}

			LOGGER.info("[readExcelDataToArray] action execution completed.");

			// store arrayTableData into returnVariable
			LOGGER.info("Storing excel data[array] in user passed return variable ["
					+ userReturnVariable + "]");
			Variable.getInstance().setArrayVariableValue(
					testStepRunner.getTestSuiteRunner(),
					Constants.EXCELARRAYTYPE, userReturnVariable,
					arrayTableData);

			// return the number of rows retrieved
			return userReqdRowCount;

		} catch (Exception e) {
			LOGGER.error("Exception::", e);
			throw new AFTException(e);
		}
	}

	/**
	 * Takes the screenshot of a specific element
	 * 
	 * @param testStepRunner
	 *            the testStepRunner
	 * @param elementName
	 *            elementName
	 * @param reportTestStep
	 *            reportTestStep
	 * @param testStepData
	 *            testStepData
	 * @throws AFTException
	 *             the application exception
	 */
	/*
	 * public void getElementScreenshot(TestStepRunner testStepRunner, String
	 * elementName, TestStep reportTestStep, TestStep testStepData) throws
	 * AFTException { WebElement webElement = null; if
	 * (EngineManager.getInstance().getCurrentExecutionEngine() != null) {
	 * String objectLocator = EngineManager.getInstance()
	 * .getCurrentExecutionEngine().getObjectId(elementName); RepositoryObject
	 * repositoryObject = ObjectRepositoryManager
	 * .getInstance().getObject(elementName); if (objectLocator.isEmpty()) {
	 * LOGGER.error("Element [" + elementName +
	 * "] not found in Object Repository"); throw new AFTException("Element [" +
	 * elementName + "] not found in Object Repository"); } else { // screen
	 * capture for specific element webElement =
	 * UIFixtureUtils.getInstance().findElement( repositoryObject,
	 * objectLocator); if (reportTestStep != null && reportTestStep.getAction()
	 * != null) { testStepRunner .captureScreenShot(reportTestStep,
	 * testStepRunner .getTestSuiteRunner()
	 * .getCurrentBusinessScenarioTestStepCount(), testStepData.getStepType(),
	 * false, true, webElement); } } } }
	 */
}
