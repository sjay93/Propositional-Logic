import java.io.*;
import java.util.*;

public class CheckTrueFalse {

	public static ArrayList<String> symb = new ArrayList<String>();
	public static ArrayList<String> symb2;
	public static HashMap<String, String> model = new HashMap<String, String>();
	public static boolean entail_reg = false;
	public static String output = "";

	public static int m[][] = new int[4][4];
	public static int s[][] = new int[4][4];
	public static int p[][] = new int[4][4];
	public static int b[][] = new int[4][4];

	public static void main(String[] args) {

		if (args.length != 3) {
			// takes three arguments
			System.out.println("Usage: " + args[0] + " [wumpus-rules-file] [additional-knowledge-file] [input_file]\n");
			exit_function(0);
		}

		// create some buffered IO streams
		String buffer;
		BufferedReader inputStream;
		// create the knowledge base and the statement
		LogicalExpression knowledge_base = new LogicalExpression();
		LogicalExpression statement = new LogicalExpression();

		// open the wumpus_rules.txt
		try {
			inputStream = new BufferedReader(new FileReader(args[0]));

			// load the wumpus rules
			System.out.println("loading the wumpus rules...");
			knowledge_base.setOperator("and");

			while ((buffer = inputStream.readLine()) != null) {
				if (!(buffer.startsWith("#") || (buffer.equals("")))) {
					// the line is not a comment
					LogicalExpression subExpression = readExpression(buffer);
					knowledge_base.setSubexpression(subExpression);
				} else {
					// the line is a comment. do nothing and read the next line
				}
			}

			// close the input file
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + args[0]);
			e.printStackTrace();
			exit_function(0);
		}
		// end reading wumpus rules

		// read the additional knowledge file
		try {
			inputStream = new BufferedReader(new FileReader(args[1]));

			// load the additional knowledge
			System.out.println("loading the additional knowledge...");

			// the connective for knowledge_base is already set. no need to set
			// it again.
			// i might want the LogicalExpression.setConnective() method to
			// check for that
			// knowledge_base.setConnective("and");

			while ((buffer = inputStream.readLine()) != null) {
				if (!(buffer.startsWith("#") || (buffer.equals("")))) {
					LogicalExpression subExpression = readExpression(buffer);
					knowledge_base.setSubexpression(subExpression);
				} else {
					// the line is a comment. do nothing and read the next line
				}
			}

			// close the input file
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + args[1]);
			e.printStackTrace();
			exit_function(0);
		}
		// end reading additional knowledge

		// check for a valid knowledge_base
		if (!valid_expression(knowledge_base)) {
			System.out.println("invalid knowledge base");
			exit_function(0);
		}

		// print the knowledge_base
		knowledge_base.print_expression("\n");

		// read the statement file
		try {
			inputStream = new BufferedReader(new FileReader(args[2]));

			System.out.println("\n\nLoading the statement file...");
			// buffer = inputStream.readLine();

			// actually read the statement file
			// assuming that the statement file is only one line long
			while ((buffer = inputStream.readLine()) != null) {
				if (!buffer.startsWith("#")) {
					// the line is not a comment
					statement = readExpression(buffer);
					break;
				} else {
					// the line is a commend. no nothing and read the next line
				}
			}

			// close the input file
			inputStream.close();

		} catch (Exception e) {
			System.out.println("failed to open " + args[2]);
			e.printStackTrace();
			exit_function(0);
		}
		// end reading the statement file

		// check for a valid statement
		if (!valid_expression(statement)) {
			System.out.println("invalid statement");
			exit_function(0);
		}

		// print the statement
		statement.print_expression("");
		// print a new line
		System.out.println("\n");

		symb2 = symb;
		boolean tt_entai = TT_Entails(knowledge_base, statement, symb, model);
		entail_reg = true;
		boolean tt_entai_neg = TT_Entails(knowledge_base, statement, symb2, model);
		// testing
		if (tt_entai) {
			if (!tt_entai_neg)
				output = "definitely true";
			else
				output = "both true and false";
		} else {
			if (tt_entai_neg)
				output = "definitely false";
			else
				output = "possibly true, possibly false";
		}
		try {
			BufferedWriter o = new BufferedWriter(new FileWriter("result.txt"));
			o.write(output + "\r\n");
			o.close();

		} catch (IOException e) {
			System.out.println("\nProblem writing to the result file!\n" + "Try again.");
			e.printStackTrace();
		}

	} // end of main

	/*
	 * this method reads logical expressions if the next string is a: - '(' =>
	 * then the next 'symbol' is a subexpression - else => it must be a
	 * unique_symbol
	 * 
	 * it returns a logical expression
	 * 
	 * notes: i'm not sure that I need the counter
	 * 
	 */
	public static LogicalExpression readExpression(String input_string) {
		LogicalExpression result = new LogicalExpression();

		// testing
		// System.out.println("readExpression() beginning -"+ input_string
		// +"-");
		// testing
		// System.out.println("\nread_exp");

		// trim the whitespace off
		input_string = input_string.trim();

		if (input_string.startsWith("(")) {
			// its a subexpression

			String symbolString = "";

			// remove the '(' from the input string
			symbolString = input_string.substring(1);
			// symbolString.trim();

			// testing
			// System.out.println("readExpression() without opening paren -"+
			// symbolString + "-");

			if (!symbolString.endsWith(")")) {
				// missing the closing paren - invalid expression
				System.out.println("missing ')' !!! - invalid expression! - readExpression():-" + symbolString);
				exit_function(0);

			} else {
				// remove the last ')'
				// it should be at the end
				symbolString = symbolString.substring(0, (symbolString.length() - 1));
				symbolString.trim();

				// testing
				// System.out.println("readExpression() without closing paren
				// -"+ symbolString + "-");

				// read the connective into the result LogicalExpression object
				symbolString = result.setOperator(symbolString);

				// testing
				// System.out.println("added connective:-" +
				// result.getConnective() + "-: here is the string that is left
				// -" + symbolString + "-:");
				// System.out.println("added connective:->" +
				// result.getConnective() + "<-");
			}

			// read the subexpressions into a vector and call setSubExpressions(
			// Vector );
			result.setSubexpressions(read_subexpressions(symbolString));

		} else {
			// the next symbol must be a unique symbol
			// if the unique symbol is not valid, the setUniqueSymbol will tell
			// us.
			result.setUniqueSymbol(input_string);

			// testing
			// System.out.println(" added:-" + input_string + "-:as a unique
			// symbol: readExpression()" );
		}

		return result;
	}

	/*
	 * this method reads in all of the unique symbols of a subexpression the
	 * only place it is called is by read_expression(String, long)(( the only
	 * read_expression that actually does something ));
	 * 
	 * each string is EITHER: - a unique Symbol - a subexpression - Delineated
	 * by spaces, and paren pairs
	 * 
	 * it returns a vector of logicalExpressions
	 * 
	 * 
	 */

	public static Vector<LogicalExpression> read_subexpressions(String input_string) {

		Vector<LogicalExpression> symbolList = new Vector<LogicalExpression>();
		LogicalExpression newExpression;// = new LogicalExpression();
		String newSymbol = new String();

		// testing
		// System.out.println("reading subexpressions! beginning-" +
		// input_string +"-:");
		// System.out.println("\nread_sub");

		input_string.trim();

		while (input_string.length() > 0) {

			newExpression = new LogicalExpression();

			// testing
			// System.out.println("read subexpression() entered while with
			// input_string.length ->" + input_string.length() +"<-");

			if (input_string.startsWith("(")) {
				// its a subexpression.
				// have readExpression parse it into a LogicalExpression object

				// testing
				// System.out.println("read_subexpression() entered if with: ->"
				// + input_string + "<-");

				// find the matching ')'
				int parenCounter = 1;
				int matchingIndex = 1;
				while ((parenCounter > 0) && (matchingIndex < input_string.length())) {
					if (input_string.charAt(matchingIndex) == '(') {
						parenCounter++;
					} else if (input_string.charAt(matchingIndex) == ')') {
						parenCounter--;
					}
					matchingIndex++;
				}

				// read untill the matching ')' into a new string
				newSymbol = input_string.substring(0, matchingIndex);

				// testing
				// System.out.println( "-----read_subExpression() - calling
				// readExpression with: ->" + newSymbol + "<- matchingIndex is
				// ->" + matchingIndex );

				// pass that string to readExpression,
				newExpression = readExpression(newSymbol);

				// add the LogicalExpression that it returns to the vector
				// symbolList
				symbolList.add(newExpression);

				// trim the logicalExpression from the input_string for further
				// processing
				input_string = input_string.substring(newSymbol.length(), input_string.length());

			} else {
				// its a unique symbol ( if its not, setUniqueSymbol() will tell
				// us )

				// I only want the first symbol, so, create a LogicalExpression
				// object and
				// add the object to the vector

				if (input_string.contains(" ")) {
					// remove the first string from the string
					newSymbol = input_string.substring(0, input_string.indexOf(" "));
					input_string = input_string.substring((newSymbol.length() + 1), input_string.length());

					// testing
					// System.out.println( "read_subExpression: i just read ->"
					// + newSymbol + "<- and i have left ->" + input_string
					// +"<-" );
				} else {
					newSymbol = input_string;
					input_string = "";
				}

				// testing
				// System.out.println( "readSubExpressions() - trying to add -"
				// + newSymbol + "- as a unique symbol with ->" + input_string +
				// "<- left" );

				newExpression.setUniqueSymbol(newSymbol);

				// testing
				// System.out.println("readSubexpression(): added:-" + newSymbol
				// + "-:as a unique symbol. adding it to the vector" );

				symbolList.add(newExpression);

				// testing
				// System.out.println("read_subexpression() - after adding: ->"
				// + newSymbol + "<- i have left ->"+ input_string + "<-");

			}

			// testing
			// System.out.println("read_subExpression() - left to parse ->" +
			// input_string + "<-beforeTrim end of while");

			input_string.trim();

			if (input_string.startsWith(" ")) {
				// remove the leading whitespace
				input_string = input_string.substring(1);
			}

			// testing
			// System.out.println("read_subExpression() - left to parse ->" +
			// input_string + "<-afterTrim with string length-" +
			// input_string.length() + "<- end of while");
		}
		return symbolList;
	}

	/*
	 * this method checks to see if a logical expression is valid or not a valid
	 * expression either: ( this is an XOR ) - is a unique_symbol - has: -- a
	 * connective -- a vector of logical expressions
	 * 
	 */
	public static boolean valid_expression(LogicalExpression expression) {


		if (!(expression.getUniqueSymbol() == null) && (expression.getOperator() == null)) {
			// we have a unique symbol, check to see if its valid
			return valid_symbol(expression.getUniqueSymbol());

			// testing
			// System.out.println("valid_expression method: symbol is not
			// empty!\n");
		}

		// symbol is empty, so
		// check to make sure the connective is valid

		// check for 'if / iff'
		if ((expression.getOperator().equalsIgnoreCase("if"))
				|| (expression.getOperator().equalsIgnoreCase("iff"))) {

			// the connective is either 'if' or 'iff' - so check the number of
			// connectives
			if (expression.getSubexpressions().size() != 2) {
				System.out.println("error: connective \"" + expression.getOperator() + "\" with "
						+ expression.getSubexpressions().size() + " arguments\n");
				return false;
			}
		}
		// end 'if / iff' check

		// check for 'not'
		else if (expression.getOperator().equalsIgnoreCase("not")) {
			// the connective is NOT - there can be only one symbol /
			// subexpression
			if (expression.getSubexpressions().size() != 1) {
				System.out.println("error: connective \"" + expression.getOperator() + "\" with "
						+ expression.getSubexpressions().size() + " arguments\n");
				return false;
			}
		}
		// end check for 'not'

		// check for 'and / or / xor'
		else if ((!expression.getOperator().equalsIgnoreCase("and"))
				&& (!expression.getOperator().equalsIgnoreCase("or"))
				&& (!expression.getOperator().equalsIgnoreCase("xor"))) {
			System.out.println("error: unknown connective " + expression.getOperator() + "\n");
			return false;
		}
		// end check for 'and / or / not'
		// end connective check

		// checks for validity of the logical_expression 'symbols' that go with
		// the connective
		for (Enumeration e = expression.getSubexpressions().elements(); e.hasMoreElements();) {
			LogicalExpression testExpression = (LogicalExpression) e.nextElement();

			// for each subExpression in expression,
			// check to see if the subexpression is valid
			if (!valid_expression(testExpression)) {
				return false;
			}
		}

		// testing
		// System.out.println("The expression is valid");

		// if the method made it here, the expression must be valid
		return true;
	}

	/** this function checks to see if a unique symbol is valid */
	//////////////////// this function should be done and complete
	// originally returned a data type of long.
	// I think this needs to return true /false
	// public long valid_symbol( String symbol ) {
	public static boolean valid_symbol(String symbol) {
		if (symbol == null || (symbol.length() == 0)) {

			// testing
			// System.out.println("String: " + symbol + " is invalid! Symbol is
			// either Null or the length is zero!\n");

			return false;
		}

		for (int counter = 0; counter < symbol.length(); counter++) {
			if ((symbol.charAt(counter) != '_') && (!Character.isLetterOrDigit(symbol.charAt(counter)))) {

				System.out.println("String: " + symbol + " is invalid! Offending character:---" + symbol.charAt(counter)
						+ "---\n");

				return false;
			}
		}

		// the characters of the symbol string are either a letter or a digit or
		// an underscore,
		// return true
		return true;
	}

	private static boolean TT_Entails(LogicalExpression kb, LogicalExpression alpha, ArrayList<String> symbol,
			HashMap<String, String> model) {
		return TT_check_all(kb, alpha, symbol, model);
	}

	private static boolean TT_check_all(LogicalExpression k_base, LogicalExpression alp, ArrayList<String> symbol,
			HashMap<String, String> model1) {
		if (symbol.isEmpty()) {
			if (PL_true(k_base, model1, false))
				return PL_true(alp, model, !entail_reg);
			else
				return true;
		} else {
			String p = symbol.remove(0);
			ArrayList<String> rest = new ArrayList<String>(symbol);
			return TT_check_all(k_base, alp, rest, EXTEND(p, true, model))
					&& TT_check_all(k_base, alp, rest, EXTEND(p, false, model));
		}
	}

	private static boolean PL_true(LogicalExpression know_base, HashMap<String, String> mode, boolean entail_reg) {
		boolean check = know_base.solve(model);
		if (entail_reg)
			return check;
		else
			return !check;
	}

	private static HashMap<String, String> EXTEND(String p, boolean truth_val, HashMap<String, String> mod) {
		mod.put(p, String.valueOf(truth_val));
		return mod;
	}

	public static boolean retrieveValueFromArray(String symbol) {

		String start_sym = null;
		String[] symbo = new String[3];

		symbo = symbol.split("_");
		start_sym = symbo[0];
		int x = Integer.parseInt(symbo[1]);
		int y = Integer.parseInt(symbo[2]);

		if (start_sym.equals("M")) {
			if (m[x][y] == 1) {
				return true;
			} else {
				return false;
			}
		} else if (start_sym.equals("S")) {
			if (s[x][y] == 1) {
				return true;
			} else {
				return false;
			}
		} else if (start_sym.equals("P")) {
			if (p[x][y] == 1) {
				return true;
			} else {
				return false;
			}
		} else if (start_sym.equals("B")) {
			if (b[x][y] == 1) {
				return true;
			} else {
				return false;
			}
		} else {
			System.out.println("Invalid Input format!!");
		}
		return false;
	}

	private static void exit_function(int value) {
		System.out.println("exiting from checkTrueFalse");
		System.exit(value);
	}
}