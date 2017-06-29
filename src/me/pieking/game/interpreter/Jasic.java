package me.pieking.game.interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import me.pieking.game.command.Command;
import me.pieking.game.command.CommandRun;
import me.pieking.game.console.Console;
import me.pieking.game.interpreter.Jasic.NumberValue;

/**
 * ALL CREDIT TO <a href="https://github.com/munificent/jasic/blob/master/com/stuffwithstuff/Jasic.java">https://github.com/munificent/jasic/blob/master/com/stuffwithstuff/Jasic.java</a>.
 * <br><br>
 * 
 * This defines a single class that contains an entire interpreter for a<br>
 * language very similar to the original BASIC. Everything is here (albeit in<br>
 * very simplified form): tokenizing, parsing, and interpretation. The file is<br>
 * organized in phases, with each appearing roughly in the order that they<br>
 * occur when a program is run. You should be able to read this top-down to walk<br>
 * through the entire process of loading and running a program.<br>
 * <br>
 * Jasic language syntax<br>
 * ---------------------<br>
 * <br>
 * Comments start with ' and proceed to the end of the line:<br>
 * <br>
 *     print "hi there" ' this is a comment<br>
 * <br>
 * Numbers and strings are supported. Strings should be in "double quotes", and<br>
 * only positive integers can be parsed (though numbers are double internally).<br>
 * <br>
 * Variables are identified by name which must start with a letter and can<br>
 * contain letters or numbers. Case is significant for names and keywords.<br>
 * <br>
 * Each statement is on its own line. Optionally, a line may have a label before<br>
 * the statement. A label is a name that ends with a colon:<br>
 * <br>
 *     foo:<br>
 * <br>
 * <br>
 * The following statements are supported:<br>
 * <br>
 * <name> = <expression><br>
 *     Evaluates the expression and assigns the result to the given named <br>
 *     variable. All variables are globally scoped.<br>
 *     <br>
 *     pi = (314159 / 10000)<br>
 *     <br>
 * print <expression><br>
 *     Evaluates the expression and prints the result.<br>
 * <br>
 *     print "hello, " + "world"<br>
 * <br>
 * input <name><br>
 *     Reads in a line of input from the user and stores it in the variable with<br>
 *     the given name.<br>
 *     <br>
 *     input guess<br>
 *     <br>
 * goto <label><br>
 *     Jumps to the statement after the label with the given name.<br>
 * <br>
 *     goto loop<br>
 * <br>
 * if <expression> then <label><br>
 *     Evaluates the expression. If it evaluates to a non-zero number, then<br>
 *     jumps to the statement after the given label.<br>
 * <br>
 *     if a < b then dosomething<br>
 * <br>
 * <br>
 * The following expressions are supported:<br>
 * <br>
 * <expression> = <expression><br>
 *     Evaluates to 1 if the two expressions are equal, 0 otherwise.<br>
 * <br>
 * <expression> + <expression><br>
 *     If the left-hand expression is a number, then adds the two expressions,<br>
 *     otherwise concatenates the two strings.<br>
 * <br>
 * <expression> - <expression><br>
 * <expression> * <expression><br>
 * <expression> / <expression><br>
 * <expression> < <expression><br>
 * <expression> > <expression><br>
 *     You can figure it out.<br>
 * <br>
 * <name><br>
 *     A name in an expression simply returns the value of the variable with<br>
 *     that name. If the variable was never set, it defaults to 0.<br>
 * <br>
 * All binary operators have the same precedence. Sorry, I had to cut corners<br>
 * somewhere.<br>
 * <br>
 * To keep things simple, I've omitted some stuff or hacked things a bit. When<br>
 * possible, I'll leave a "HACK" note there explaining what and why. If you<br>
 * make your own interpreter, you'll want to address those.<br>
 * <br>
 * @author Bob Nystrom
 */
public class Jasic {

	public Console cons;
	
    /**
     * Runs the interpreter as a command-line app. Takes one argument: a path
     * to a script file to load and run. The script should contain one
     * statement per line.
     * 
     * @param args Command-line arguments.
     */
//    public static void main(String[] args) {
//        // Just show the usage and quit if a script wasn't provided.
//        if (args.length != 1) {
//            ////System.out.println("Usage: jasic <script>");
//            ////System.out.println("Where <script> is a relative path to a .jas script to run.");
//            return;
//        }
//        
//        // Read the file.
//        String contents = readFile(args[0]);
//        
//        // Run it.
//        Jasic jasic = new Jasic();
//        jasic.interpret(contents);
//    }
    
    public static void run(String code, Console cons, InputStream input, AtomicBoolean cancel){
        Jasic jasic = new Jasic(cons, input);
        jasic.interpret(code, cancel);
    }
    
    public static void runFile(File file, Console cons, InputStream input, AtomicBoolean cancel){
    	String contents = readFile(file);
        Jasic jasic = new Jasic(cons, input);
        jasic.interpret(contents, cancel);
    }
    
    // Tokenizing (lexing) -----------------------------------------------------
    
    /**
     * This function takes a script as a string of characters and chunks it into
     * a sequence of tokens. Each token is a meaningful unit of program, like a
     * variable name, a number, a string, or an operator.
     */
    private static List<Token> tokenize(String source) {
        List<Token> tokens = new ArrayList<Token>();
        
        String token = "";
        TokenizeState state = TokenizeState.DEFAULT;
        
        // Many tokens are a single character, like operators and ().
        String charTokens = "\n=+-*/<>()#";
        TokenType[] tokenTypes = { TokenType.LINE, TokenType.EQUALS,
            TokenType.OPERATOR, TokenType.OPERATOR, TokenType.OPERATOR,
            TokenType.OPERATOR, TokenType.OPERATOR, TokenType.OPERATOR,
            TokenType.LEFT_PAREN, TokenType.RIGHT_PAREN, TokenType.NOTEQUALS
        };
        
        // Scan through the code one character at a time, building up the list
        // of tokens.
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            switch (state) {
            case DEFAULT:
                if (charTokens.indexOf(c) != -1) {
                    tokens.add(new Token(Character.toString(c),
                        tokenTypes[charTokens.indexOf(c)]));
                } else if (c == '{') {
                	token += c;
                    state = TokenizeState.ARRAY_LITERAL;
                } else if (Character.isLetter(c) || c == '[' || c == ']' || c == '_' || c == '?') {
                    token += c;
                    state = TokenizeState.WORD;
                } else if (Character.isDigit(c)) {
                    token += c;
                    state = TokenizeState.NUMBER;
                } else if (c == '"') {
                    state = TokenizeState.STRING;
                } else if (c == '\'') {
                    state = TokenizeState.COMMENT;
                }
                break;
            case ARRAY_LITERAL:
            	//System.out.println(c);
            	token += c;
            	if(c == '}'){
            		//System.out.println("add literal " + token);
            		tokens.add(new Token(token, TokenType.ARRAYLITERAL));
            		token = "";
                    state = TokenizeState.DEFAULT;
                    i--;
            	}
            	break;
            case WORD:
//            	////System.out.println(c);
                if (Character.isLetterOrDigit(c) || c == '[' || c == ']' || c == '{' || c == '}' || c == ',' || c == '_' || c == '?') {
                    token += c;
                } else if (c == ':') {
                    tokens.add(new Token(token, TokenType.LABEL));
                    token = "";
                    state = TokenizeState.DEFAULT;
                } else {
//                	////System.out.println("token = " + token);
                	if(token.startsWith("[") && token.endsWith("]")){
                		tokens.add(new Token(token, TokenType.ARRAY));
                	}else{
                		tokens.add(new Token(token, TokenType.WORD));
                	}
                    token = "";
                    state = TokenizeState.DEFAULT;
                    i--; // Reprocess this character in the default state.
                }
                break;
            case NUMBER:
                // HACK: Negative numbers and floating points aren't supported.
                // To get a negative number, just do 0 - <your number>.
                // To get a floating point, divide.
                if (Character.isDigit(c)) {
                    token += c;
                } else {
                    tokens.add(new Token(token, TokenType.NUMBER));
                    token = "";
                    state = TokenizeState.DEFAULT;
                    i--; // Reprocess this character in the default state.
                }
                break;
            case STRING:
                if (c == '"') {
                    tokens.add(new Token(token, TokenType.STRING));
                    token = "";
                    state = TokenizeState.DEFAULT;
                } else {
                    token += c;
                }
                break;
            case COMMENT:
                if (c == '\n') {
                    state = TokenizeState.DEFAULT;
                }
                break;
            }
        }
        
        // HACK: Silently ignore any in-progress token when we run out of
        // characters. This means that, for example, if a script has a string
        // that's missing the closing ", it will just ditch it.
        return tokens;
    }

    // Token data --------------------------------------------------------------

    /**
     * This defines the different kinds of tokens or meaningful chunks of code
     * that the parser knows how to consume. These let us distinguish, for
     * example, between a string "foo" and a variable named "foo".
     * 
     * HACK: A typical tokenizer would actually have unique token types for
     * each keyword (print, goto, etc.) so that the parser doesn't have to look
     * at the names, but Jasic is a little more crude.
     */
    private enum TokenType {
        WORD, NUMBER, STRING, LABEL, LINE,
        EQUALS, NOTEQUALS, OPERATOR, LEFT_PAREN, RIGHT_PAREN, EOF, ARRAY, ARRAYLITERAL
    }
    
    /**
     * This is a single meaningful chunk of code. It is created by the tokenizer
     * and consumed by the parser.
     */
    private static class Token {
        public Token(String text, TokenType type) {
            this.text = text;
            this.type = type;
        }
        
        public final String text;
        public final TokenType type;
    }
    
    /**
     * This defines the different states the tokenizer can be in while it's
     * scanning through the source code. Tokenizers are state machines, which
     * means the only data they need to store is where they are in the source
     * code and this one "state" or mode value.
     * 
     * One of the main differences between tokenizing and parsing is this
     * regularity. Because the tokenizer stores only this one state value, it
     * can't handle nesting (which would require also storing a number to
     * identify how deeply nested you are). The parser is able to handle that.
     */
    private enum TokenizeState {
        DEFAULT, WORD, NUMBER, STRING, COMMENT, ARRAY_LITERAL
    }

    // Parsing -----------------------------------------------------------------

    /**
     * This defines the Jasic parser. The parser takes in a sequence of tokens
     * and generates an abstract syntax tree. This is the nested data structure
     * that represents the series of statements, and the expressions (which can
     * nest arbitrarily deeply) that they evaluate. In technical terms, what we
     * have is a recursive descent parser, the simplest kind to hand-write.
     *
     * As a side-effect, this phase also stores off the line numbers for each
     * label in the program. It's a bit gross, but it works.
     */
    private class Parser {
    	public Jasic j;
        public Parser(List<Token> tokens, Jasic j) {
            this.tokens = tokens;
            position = 0;
            this.j = j;
        }
        
        /**
         * The top-level function to start parsing. This will keep consuming
         * tokens and routing to the other parse functions for the different
         * grammar syntax until we run out of code to parse.
         * 
         * @param  labels   A map of label names to statement indexes. The
         *                  parser will fill this in as it scans the code.
         * @return          The list of parsed statements.
         */
        public List<Statement> parse(Map<String, Integer> labels) {
            List<Statement> statements = new ArrayList<Statement>();
            
            while (true) {
                // Ignore empty lines.
                while (match(TokenType.LINE));
                
                if (match(TokenType.LABEL)) {
                    // Mark the index of the statement after the label.
                    labels.put(last(1).text, statements.size());
                } else if (match(TokenType.WORD, TokenType.EQUALS)) {
                    String name = last(2).text;
                    Expression value = expression();
                    statements.add(new AssignStatement(name, value));
                } else if (match("print")) {
                    statements.add(new PrintStatement(expression(), j));
                } else if (match("run")) {
                    statements.add(new RunStatement(expression(), j));
                } else if (match("int")) {
                	Expression toRound = expression();
                	String name = consume(TokenType.WORD).text;
                    statements.add(new IntStatement(name, toRound, j));
                } else if (match("remove")) {
                	String name = consume(TokenType.WORD).text;
                	if(get(0).text.equals("?")){
                		consume("?");
                		statements.add(new RemoveStatement(name, j));
                	}else{
                		Expression index = expression();
                		statements.add(new RemoveStatement(name, index, j));
                	}
                } else if (match("rnd")) {
                	Expression ex = expression();
                	String name = consume(TokenType.WORD).text;
                    statements.add(new RndStatement(ex, name, j));
                } else if (match("input")) {
                    statements.add(new InputStatement(consume(TokenType.WORD).text, j));
                } else if (match("goto")) {
                    statements.add(new GotoStatement(consume(TokenType.WORD).text));
                } else if (match("sleep")) {
                    statements.add(new SleepStatement(expression()));
                } else if (match("sub")) {
                    statements.add(new SubStatement(consume(TokenType.WORD).text));
                } else if (match("return")) {
                    statements.add(new ReturnStatement());
                } else if (match("if")) {
                    Expression condition = expression();
                    consume("then");
                    String label = consume(TokenType.WORD).text;
                    statements.add(new IfThenStatement(condition, label));
                } else break; // Unexpected token (likely EOF), so end.
            }
            
            return statements;
        }
        
        // The following functions each represent one grammatical part of the
        // language. If this parsed English, these functions would be named like
        // noun() and verb().
        
        /**
         * Parses a single expression. Recursive descent parsers start with the
         * lowest-precedent term and moves towards higher precedence. For Jasic,
         * binary operators (+, -, etc.) are the lowest.
         * 
         * @return The parsed expression.
         */
        private Expression expression() {
            return operator();
        }
        
        /**
         * Parses a series of binary operator expressions into a single
         * expression. In Jasic, all operators have the same predecence and
         * associate left-to-right. That means it will interpret:
         *    1 + 2 * 3 - 4 / 5
         * like:
         *    ((((1 + 2) * 3) - 4) / 5)
         * 
         * It works by building the expression tree one at a time. So, given
         * this code: 1 + 2 * 3, this will:
         * 
         * 1. Parse (1) as an atomic expression.
         * 2. See the (+) and start a new operator expression.
         * 3. Parse (2) as an atomic expression.
         * 4. Build a (1 + 2) expression and replace (1) with it.
         * 5. See the (*) and start a new operator expression.
         * 6. Parse (3) as an atomic expression.
         * 7. Build a ((1 + 2) * 3) expression and replace (1 + 2) with it.
         * 8. Return the last expression built.
         * 
         * @return The parsed expression.
         */
        private Expression operator() {
            Expression expression = atomic();
            
            // Keep building operator expressions as long as we have operators.
            while (match(TokenType.OPERATOR) || match(TokenType.EQUALS) || match(TokenType.NOTEQUALS)) {
                String operator = last(1).text.charAt(0) + "";
                ////System.out.println("op = " + operator);
                Expression right = atomic();
                ////System.out.println(right);
                expression = new OperatorExpression(expression, operator, right);
            }
            
            return expression;
        }
        
        /**
         * Parses an "atomic" expression. This is the highest level of
         * precedence and contains single literal tokens like 123 and "foo", as
         * well as parenthesized expressions.
         * 
         * @return The parsed expression.
         */
        private Expression atomic() {
            if (match(TokenType.WORD)) {
                return new VariableExpression(last(1).text);
            } else if (match(TokenType.NUMBER)) {
                return new NumberValue(Double.parseDouble(last(1).text));
            } else if (match(TokenType.STRING)) {
                return new StringValue(last(1).text);
            } else if (match(TokenType.ARRAY)) {
                return new ArrayValue(last(1).text);
            } else if (match(TokenType.ARRAYLITERAL)) {
                return new ArrayValue((Object)last(1).text);
            } else if (match(TokenType.LEFT_PAREN)) {
                // The contents of a parenthesized expression can be any
                // expression. This lets us "restart" the precedence cascade
                // so that you can have a lower precedence expression inside
                // the parentheses.
                Expression expression = expression();
                consume(TokenType.RIGHT_PAREN);
                return expression;
            }
            throw new Error("Couldn't parse :(");
        }
        
        // The following functions are the core low-level operations that the
        // grammar parser is built in terms of. They match and consume tokens in
        // the token stream.
        
        /**
         * Consumes the next two tokens if they are the given type (in order).
         * Consumes no tokens if either check fais.
         * 
         * @param  type1 Expected type of the next token.
         * @param  type2 Expected type of the subsequent token.
         * @return       True if tokens were consumed.
         */
        private boolean match(TokenType type1, TokenType type2) {
            if (get(0).type != type1) return false;
            if (get(1).type != type2) return false;
            position += 2;
            return true;
        }
        
        /**
         * Consumes the next token if it's the given type.
         * 
         * @param  type  Expected type of the next token.
         * @return       True if the token was consumed.
         */
        private boolean match(TokenType type) {
            if (get(0).type != type) return false;
            position++;
            return true;
        }
        
        /**
         * Consumes the next token if it's a word token with the given name.
         * 
         * @param  name  Expected name of the next word token.
         * @return       True if the token was consumed.
         */
        private boolean match(String name) {
            if (get(0).type != TokenType.WORD) return false;
            if (!get(0).text.equals(name)) return false;
            position++;
            return true;
        }
        
        /**
         * Consumes the next token if it's the given type. If not, throws an
         * exception. This is for cases where the parser demands a token of a
         * certain type in a certain position, for example a matching ) after
         * an opening (.
         * 
         * @param  type  Expected type of the next token.
         * @return       The consumed token.
         */
        private Token consume(TokenType type) {
            if (get(0).type != type) throw new Error("Expected " + type + ".");
            return tokens.get(position++);
        }
        
        /**
         * Consumes the next token if it's a word with the given name. If not,
         * throws an exception.
         * 
         * @param  name  Expected name of the next word token.
         * @return       The consumed token.
         */
        private Token consume(String name) {
            if (!match(name)) throw new Error("Expected " + name + ".");
            return last(1);
        }

        /**
         * Gets a previously consumed token, indexing backwards. last(1) will
         * be the token just consumed, last(2) the one before that, etc.
         * 
         * @param  offset How far back in the token stream to look.
         * @return        The consumed token.
         */
        private Token last(int offset) {
            return tokens.get(position - offset);
        }
        
        /**
         * Gets an unconsumed token, indexing forward. get(0) will be the next
         * token to be consumed, get(1) the one after that, etc.
         * 
         * @param  offset How far forward in the token stream to look.
         * @return        The yet-to-be-consumed token.
         */
        private Token get(int offset) {
            if (position + offset >= tokens.size()) {
                return new Token("", TokenType.EOF);
            }
            ////System.out.println(tokens.get(position + offset).text);
            return tokens.get(position + offset);
        }
        
        private final List<Token> tokens;
        private int position;
    }
    
    // Abstract syntax tree (AST) ----------------------------------------------

    // These classes define the syntax tree data structures. This is how code is
    // represented internally in a way that's easy for the interpreter to
    // understand.
    //
    // HACK: Unlike most real compilers or interpreters, the logic to execute
    // the code is baked directly into these classes. Typically, it would be
    // separated out so that the AST us just a static data structure.

    /**
     * Base interface for a Jasic statement. The different supported statement
     * types like "print" and "goto" implement this.
     */
    public interface Statement {
        /**
         * Statements implement this to actually perform whatever behavior the
         * statement causes. "print" statements will display text here, "goto"
         * statements will change the current statement, etc.
         * @param cancel 
         */
        void execute(AtomicBoolean cancel);
    }

    /**
     * Base interface for an expression. An expression is like a statement
     * except that it also returns a value when executed. ts do not
     * appear at the top level in Jasic programs, but are used in many
     * statements. For example, the value printed by a "print" statement is an
     * expression. Unlike statements, expressions can nest.
     */
    public interface Expression {
        /**
         * Expression classes implement this to evaluate the expression and
         * return the value.
         * 
         * @return The value of the calculated expression.
         */
        Value evaluate();
    }
    
    /**
     * A "print" statement evaluates an expression, converts the result to a
     * string, and displays it to the user.
     */
    public class PrintStatement implements Statement {
    	Jasic j;
        public PrintStatement(Expression expression, Jasic j) {
            this.expression = expression;
            this.j = j;
        }
        
        public void execute(AtomicBoolean cancel) {
        	if(j.cons != null){
        		j.cons.write(expression.evaluate().toString());
        	}else{
        		////System.out.println(expression.evaluate().toString());
        	}
        }

        private final Expression expression;
    }
    
    /**
     * A "run" statement evaluates an expression, converts the result to a
     * string, and runs it as a console command.
     */
    public class RunStatement implements Statement {
    	Jasic j;
        public RunStatement(Expression expression, Jasic j) {
            this.expression = expression;
            this.j = j;
        }
        
        public void execute(AtomicBoolean cancel) {
        	if(j != null){
        		j.cons.runCommand(expression.evaluate().toString(), false);
        	}else{
        		////System.out.println("run " + expression.evaluate().toString());
        	}
        }

        private final Expression expression;
    }
    
    /**
     * An "input" statement reads input from the user and stores it in a
     * variable.
     */
    public class InputStatement implements Statement {
    	Jasic j;
        public InputStatement(String name, Jasic j) {
            this.name = name;
            this.j = j;
        }
        
        public void execute(AtomicBoolean cancel) {
            try {
            	Command running = j.cons.getRunning();
            	
            	if(running != null){
            		if(running instanceof CommandRun){
            			running.wantsInput = true;
            		}
            	}
            	
                String input = lineIn.readLine();
                
                if(input == null) return;
                
                ////System.out.println(input);
                
                if(running != null){
            		if(running instanceof CommandRun){
            			running.wantsInput = false;
            		}
            	}
                
                // Store it as a number if possible, otherwise use a string.
                try {
                    double value = Double.parseDouble(input);
                    variables.put(name, new NumberValue(value));
                } catch (NumberFormatException e) {
                    variables.put(name, new StringValue(input));
                }
            } catch (IOException e1) {
                // HACK: Just ignore the problem.
            }
        }

        private final String name;
    }
    
    /**
     * A "remove" statement evaluates an expression into a number, 
     * and removes the value at that index in the array with a certain name.
     */
    public class RemoveStatement implements Statement {
    	Jasic j;
    	String name;
    	
    	public RemoveStatement(String name, Expression expression, Jasic j) {
    		
    		this.name = name;
    		this.expression = expression;
    		this.j = j;
    	}
    	
    	public RemoveStatement(String name, Jasic j) {
    		expression = null;
    		this.name = name;
    		this.j = j;
    	}
    	
    	public void execute(AtomicBoolean cancel) {
    		
    		
    		Value eval = expression != null ? expression.evaluate() : new NumberValue(-1);
    		if(!(eval instanceof NumberValue)) return;
    		
    		NumberValue nval = (NumberValue) eval;
    		int index = (int) nval.value;
    		
    		
//        	//System.out.println(name + " " + nval.toString());
    		
    		if(index >= -1){
    			if(variables.containsKey(name)){
    				Value val = variables.get(name);
    				if(val instanceof ArrayValue){
    					ArrayValue ar = (ArrayValue) val;
    					ar.remove(index);
    				}
    			}
    		}
    	}
    	
    	private final Expression expression;
    }
    /**
     * A "int" statement evaluates an expression, converts the result to a
     * double, rounds it down to the nearest whole number, and stores the result in a variable.
     */
    public class IntStatement implements Statement {
    	Jasic j;
    	String name;
    	
    	public IntStatement(String name, Expression expression, Jasic j) {
    		////System.out.println("IntStatement " + name);
    		
    		this.name = name;
    		this.expression = expression;
    		this.j = j;
    	}
    	
    	public void execute(AtomicBoolean cancel) {
    		
    		Object[] arrayInfo = arrayNameAndIndex(name);
        	String arName = (String) arrayInfo[0];
        	int index = (int) arrayInfo[1];
        	
        	Value eval = expression.evaluate();
        	if(!(eval instanceof NumberValue)) return;
        	
        	NumberValue nval = (NumberValue) eval;
        	int casted = (int) nval.value;
        	nval = new NumberValue(casted);
        	
        	////System.out.println(arName + " " + index);
        	
//        	//System.out.println(name + " " + nval.toString());
        	
        	if(arName != null && index >= -1){
        		if(variables.containsKey(arName)){
        			Value val = variables.get(arName);
        			if(val instanceof ArrayValue){
        				ArrayValue ar = (ArrayValue) val;
        				ar.set(index, nval);
        			}
        		}
        	}else{
        		variables.put(name, nval);
        	}
    	}
    	
    	private final Expression expression;
    }
    
    public Random rand = new Random();
    public double lastRand = rand.nextDouble();
    
    /**
     * A "rnd" statement evaluates an expression, converts the result to a
     * double, rounds it down to the nearest whole number, and stores the result in a variable.
     */
    public class RndStatement implements Statement {
    	
    	Jasic j;
    	Expression mode;
    	String name;
    	
    	public RndStatement(Expression mode, String name, Jasic j) {
    		////System.out.println("IntStatement " + name);
    		
    		this.name = name;
    		this.mode = mode;
    		this.j = j;
    	}
    	
    	public void execute(AtomicBoolean cancel) {
    		
    		Object[] arrayInfo = arrayNameAndIndex(name);
        	String arName = (String) arrayInfo[0];
        	int index = (int) arrayInfo[1];
        	
        	double random = 0;
        	
        	int mode = (int)this.mode.evaluate().toNumber();
        	
        	if(mode < 0){
        		Random r = new Random(mode);
        		random = r.nextDouble();
        	}else if(mode == 0){
        		random = lastRand;
        	}else{
        		lastRand = rand.nextDouble();
        		random = lastRand;
        	}
        	
        	NumberValue nval = new NumberValue(random);
        	
//        	//System.out.println(name + " " + nval.toString());
        	
        	if(arName != null && index >= -1){
        		if(variables.containsKey(arName)){
        			Value val = variables.get(arName);
        			if(val instanceof ArrayValue){
        				ArrayValue ar = (ArrayValue) val;
        				ar.set(index, nval);
        			}
        		}
        	}else{
        		variables.put(name, nval);
        	}
    	}
    }

    /**
     * An assignment statement evaluates an expression and stores the result in
     * a variable.
     */
    public class AssignStatement implements Statement {
        public AssignStatement(String name, Expression value) {
            this.name = name;
            this.value = value;
        }
        
        public void execute(AtomicBoolean cancel) {
        	Object[] arrayInfo = arrayNameAndIndex(name);
        	String arName = (String) arrayInfo[0];
        	int index = (int) arrayInfo[1];
        	
        	//System.out.println(name);
        	
        	//System.out.println("array = " + arName);
        	
        	Value eval = value.evaluate();
        	
        	//System.out.println(value);
			
			//System.out.println(eval);
        	
        	if(arName != null && index >= -1){
        		if(variables.containsKey(arName)){
        			Value val = variables.get(arName);
        			if(val instanceof ArrayValue){
        				ArrayValue ar = (ArrayValue) val;
        				
        				//System.out.println(ar);
        				
        				if(eval instanceof ArrayValue){
        					ArrayValue eAr = (ArrayValue) eval;
        					ar.set(index, eAr.clone());
        				}else{
        					ar.set(index, eval);
        				}
        			}
        		}
        	}else{
        		variables.put(name, eval);
        	}
        	
        	//System.out.println();
        }

        private final String name;
        private final Expression value;
    }
    
    public Object[] arrayNameAndIndex(String input){
    	Pattern p = Pattern.compile("(.+?)\\[(.+?)\\]"); // xPos[151] -> (xPos)[(151)]
    	////System.out.println(input);
    	Matcher m = p.matcher(input);
    	String arName = null;
    	String indexS = null;
    	if(m.find()){
        	arName = m.group(1);
        	indexS = m.group(2);
    	}
    	
    	////System.out.println("index = " + indexS);
    	
    	int index = -1;
    	
    	try{
    		index = Integer.parseInt(indexS);
    	}catch(Exception e){
    		try{
    			////System.out.println(indexS + " " + variables.containsKey(indexS));
        		if(indexS == "?"){
        			index = -1;
        		}else if(variables.containsKey(indexS)){
        			Value val = variables.get(indexS);
        			////System.out.println(val);
        			if(val instanceof NumberValue){
        				NumberValue num = (NumberValue) val;
        				index = (int)num.toNumber();
        			}
        		}
        	}catch(Exception e1){
        		e1.printStackTrace();
        	}
    	}
    	
    	
    	return new Object[]{arName, index};
    }
    
    /**
     * A "goto" statement jumps execution to another place in the program.
     */
    public class GotoStatement implements Statement {
        public GotoStatement(String label) {
            this.label = label;
        }
        
        public void execute(AtomicBoolean cancel) {
            if (labels.containsKey(label)) {
            	//System.out.println("goto " + label);
                currentStatement = labels.get(label).intValue();
            }
        }

        private final String label;
    }
    
    /**
     * A "sleep" statement waits a certain amount of time.
     */
    public class SleepStatement implements Statement {
        public SleepStatement(Expression expression) {
            this.expression = expression;
        }
        
        public void execute(AtomicBoolean cancel) {
        	int time = (int) expression.evaluate().toNumber();
            long start = System.currentTimeMillis();
            
            while(System.currentTimeMillis()-start < time || cancel.get()){}
        }

        private final Expression expression;
    }
    
    public static List<Integer> returnTo = new ArrayList<Integer>();
    
    public class SubStatement implements Statement {
    	
        public SubStatement(String label) {
            this.label = label;
        }
        
        public void execute(AtomicBoolean cancel) {
            if (labels.containsKey(label) && returnTo.size() < 100) {
            	//System.out.println("sub " + label);
            	returnTo.add(currentStatement);
                currentStatement = labels.get(label).intValue();
            }
        }

        private final String label;
    }
    
    public class ReturnStatement implements Statement {
    	
        public ReturnStatement() {}
        
        public void execute(AtomicBoolean cancel) {
        	if(!returnTo.isEmpty()){
            	int index = returnTo.size()-1;
            	currentStatement = returnTo.get(index);
            	returnTo.remove(index);
        	}
        }
    }
    
    /**
     * An if then statement jumps execution to another place in the program, but
     * only if an expression evaluates to something other than 0.
     */
    public class IfThenStatement implements Statement {
        public IfThenStatement(Expression condition, String label) {
            this.condition = condition;
            this.label = label;
        }
        
        public void execute(AtomicBoolean cancel) {
            if (labels.containsKey(label)) {
                double value = condition.evaluate().toNumber();
                if (value != 0) {
                    currentStatement = labels.get(label).intValue();
                }
            }
        }

        private final Expression condition;
        private final String label;
    }
    
    /**
     * A variable expression evaluates to the current value stored in that
     * variable.
     */
    public class VariableExpression implements Expression {
        public VariableExpression(String name) {
            this.name = name;
        }
        
        public Value evaluate() {
        	
        	Object[] arrayInfo = arrayNameAndIndex(name);
        	String arName = (String) arrayInfo[0];
        	int index = (int) arrayInfo[1];
        	
        	if(arName != null && index >= -1){
        		if(variables.containsKey(arName)){
        			Value val = variables.get(arName);
        			if(val instanceof ArrayValue){
        				ArrayValue ar = (ArrayValue) val;
        				return ar.get(index);
        			}
        		}
        	}else{
                if (variables.containsKey(name)) {
                    return variables.get(name);
                }
        	}
            return new NumberValue(0);
        }
        
        private final String name;
    }
    
    /**
     * An operator expression evaluates two expressions and then performs some
     * arithmetic operation on the results.
     */
    public class OperatorExpression implements Expression {
        public OperatorExpression(Expression left, String operator,
                                  Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }
        
        public Value evaluate() {
            Value leftVal = left.evaluate();
            Value rightVal = right.evaluate();
            
            switch (operator) {
            case "=":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftVal.toNumber() ==
                                            rightVal.toNumber()) ? 1 : 0);
                } else {
                    return new NumberValue(leftVal.toString().equals(
                                           rightVal.toString()) ? 1 : 0);
                }
            case "#":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftVal.toNumber() ==
                                            rightVal.toNumber()) ? 0 : 1);
                } else {
//                	//System.out.println(left + " -> " + leftVal + " " + right + " -> " + rightVal);
                	
                    return new NumberValue(leftVal.toString().equals(
                                           rightVal.toString()) ? 0 : 1);
                }
            case "+":
                // Addition if the left argument is a number, otherwise do
                // string concatenation.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue(leftVal.toNumber() +
                                           rightVal.toNumber());
                } else {
                    return new StringValue(leftVal.toString() +
                            rightVal.toString());
                }
            case "-":
                return new NumberValue(leftVal.toNumber() -
                        rightVal.toNumber());
            case "*":
                return new NumberValue(leftVal.toNumber() *
                        rightVal.toNumber());
            case "/":
                return new NumberValue(leftVal.toNumber() /
                        rightVal.toNumber());
            case "<":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftVal.toNumber() <
                                            rightVal.toNumber()) ? 1 : 0);
                } else {
                    return new NumberValue((leftVal.toString().compareTo(
                                           rightVal.toString()) < 0) ? 1 : 0);
                }
            case ">":
                // Coerce to the left argument's type, then compare.
                if (leftVal instanceof NumberValue) {
                    return new NumberValue((leftVal.toNumber() >
                                            rightVal.toNumber()) ? 1 : 0);
                } else {
                    return new NumberValue((leftVal.toString().compareTo(
                            rightVal.toString()) > 0) ? 1 : 0);
                }
            }
            throw new Error("Unknown operator.");
        }
        
        private final Expression left;
        private final String operator;
        private final Expression right;
    }
    
    // Value types -------------------------------------------------------------
    
    /**
     * This is the base interface for a value. Values are the data that the
     * interpreter processes. They are what gets stored in variables, printed,
     * and operated on.
     * 
     * There is an implementation of this interface for each of the different
     * primitive types (really just double and string) that Jasic supports.
     * Wrapping them in a single Value interface lets Jasic be dynamically-typed
     * and convert between different representations as needed.
     * 
     * Note that Value extends Expression. This is a bit of a hack, but it lets
     * us use values (which are typically only ever seen by the interpreter and
     * not the parser) as both runtime values, and as object representing
     * literals in code.
     */
    public interface Value extends Expression {
        /**
         * Value types override this to convert themselves to a string
         * representation.
         */
        String toString();
        
        /**
         * Value types override this to convert themselves to a numeric
         * representation.
         */
        double toNumber();
    }
    
    /**
     * A numeric value. Jasic uses doubles internally for all numbers.
     */
    public class NumberValue implements Value {
        public NumberValue(double value) {
            this.value = value;
        }
        
        @Override public String toString() { 
        	
        	if(value == (int)value){
        		return Integer.toString((int)value);
        	}
        	
        	return Double.toString(value);
        }
        public double toNumber() { return value; }
        public Value evaluate() { return this; }

        private final double value;
    }
    
    /**
     * A string value.
     */
    public class StringValue implements Value {
        public StringValue(String value) {
            this.value = value;
        }
        
        @Override public String toString() { return value; }
        
        public double toNumber() { 
        	try{
        		return Double.parseDouble(value); 
        	}catch(NumberFormatException e){
        		return -1;
        	}
        }
        
        public Value evaluate() { return this; }

        private final String value;
    }
    
    /**
     * An array value.
     */
    public class ArrayValue implements Value {
    	public ArrayValue(String initSize) {
    		
    		Value[] val = new Value[0];
    		
    		if(initSize.substring(1, initSize.length()-1).equals("?")){
    			valueL = new ArrayList<Jasic.Value>();
    			value = null;
    		}else{
                try{
                	val = new Value[Integer.parseInt(initSize.substring(1, initSize.length()-1))];
                }catch(NumberFormatException e){}
                
                value = val;
                valueL = null;
    		}
        }
    	
    	public Value get(int index) {
    		if(valueL != null){
    			if(index == -1){
					return valueL.get(valueL.size()-1);
				}else{
					if(index >= 0 && index < valueL.size()) return valueL.get(index);
				}
			}else{
    			if(index >= 0 && index < value.length) return value[index];
    		}
			return new NumberValue(-1);
		}

		public void set(int index, Value val) {
			if(valueL != null){
				if(index == -1){
					valueL.add(val);
				}else{
					if(index >= 0 && index < valueL.size()) valueL.set(index, val);
				}
			}else{
				if(index >= 0 && index < value.length) value[index] = val;
			}
		}
		
		public void remove(int index) {
			if(valueL != null){
				if(index == -1){
					valueL.remove(valueL.size()-1);
				}else{
					if(index >= 0 && index < valueL.size()) valueL.remove(index);
				}
			}else{
				if(index >= 0 && index < value.length) value[index] = null;
			}
		}

		public ArrayValue(Value[] value) {
            this.value = value;
            valueL = null;
        }
        
		/**
		 * SUPER HACK: text must be a String but I already have a constructor with a String
		 */
        public ArrayValue(Object text) {
        	String raw = (String) text;
        	
        	//System.out.println("raw = " + raw);
        	
        	raw = raw.substring(1, raw.length()-1);
        	
        	String[] spl = raw.split(",");
        	
        	value = new Value[spl.length];
        	for(int i = 0; i < spl.length; i++){
        		String s = spl[i];
        		
        		//System.out.println(i + ": " + s);
        		////System.out.println(s + " " + variables.containsKey(s));
        		
        		try{
        			value[i] = new NumberValue(Double.parseDouble(s));
        		}catch(NumberFormatException e){
            		value[i] = new StringValue(s);
        		}
        	}
        	valueL = null;
        	
		}

		@Override public String toString() { 
        	
        	String items = "";
        	
        	if(value != null){
            	for(int i = 0; i < value.length; i++){
            		if(value[i] != null){
            			if(value[i] instanceof StringValue){
            				items += "\"" + value[i].toString() + "\", ";
            			}else{
            				items += value[i].toString() + ", ";
            			}
            		}else{
            			items += "null, ";
            		}
            	}
        	}else{
        		for(int i = 0; i < valueL.size(); i++){
        			Value v = valueL.get(i);
            		if(v != null){
            			if(v instanceof StringValue){
            				items += "\"" + v.toString() + "\", ";
            			}else{
            				items += v.toString() + ", ";
            			}
            		}else{
            			items += "null, ";
            		}
            	}
        	}
        	
        	if(!items.isEmpty()) items = items.substring(0, items.length()-2);
        	
        	return (valueL!=null ? "L" : "" ) + "[" + items + "]"; 
        }
        public double toNumber() { return valueL == null ? value.length : valueL.size(); }
        public Value evaluate() { return this; }
        
        private final Value[] value;
        private final List<Value> valueL;
        
        public ArrayValue clone(){
        	Value[] val = new Value[value.length];
        	
        	for(int i = 0; i < val.length; i++){
        		val[i] = value[i];
        	}
        	
        	return new ArrayValue(val);
        }
        
    }

    // Interpreter -------------------------------------------------------------
    
    /**
     * Constructs a new Jasic instance. The instance stores the global state of
     * the interpreter such as the values of all of the variables and the
     * current statement.
     */
    public Jasic(Console cons, InputStream input) {
        variables = new HashMap<String, Value>();
        labels = new HashMap<String, Integer>();
        
        InputStreamReader converter = new InputStreamReader(input);
        lineIn = new BufferedReader(converter);
        this.cons = cons;
    }

    /**
     * This is where the magic happens. This runs the code through the parsing
     * pipeline to generate the AST. Then it executes each statement. It keeps
     * track of the current line in a member variable that the statement objects
     * have access to. This lets "goto" and "if then" do flow control by simply
     * setting the index of the current statement.
     *
     * In an interpreter that didn't mix the interpretation logic in with the
     * AST node classes, this would be doing a lot more work.
     * 
     * @param source A string containing the source code of a .jas script to
     *               interpret.
     * @param cancel 
     */
    public void interpret(String source, AtomicBoolean cancel) {
        // Tokenize.
        List<Token> tokens = tokenize(source);
        
        // Parse.
        Parser parser = new Parser(tokens, this);
        List<Statement> statements = parser.parse(labels);
        
        // Interpret until we're done.
        currentStatement = 0;
        while (currentStatement < statements.size() && !cancel.get()) {
            int thisStatement = currentStatement;
            currentStatement++;
            Statement s = statements.get(thisStatement);
            try{
            	s.execute(cancel);
            }catch(Exception e){
            	//System.out.println(s);
            	e.printStackTrace();
            	break;
            }
        }
    }
    
    private final Map<String, Value> variables;
    private final Map<String, Integer> labels;
    
    private final BufferedReader lineIn;
    
    private int currentStatement;
    
    // Utility stuff -----------------------------------------------------------
    
    /**
     * Reads the file from the given path and returns its contents as a single
     * string.
     * 
     * @param  path  Path to the text file to read.
     * @return       The contents of the file or null if the load failed.
     * @throws       IOException
     */
    private static String readFile(String path) {
        try {
            FileInputStream stream = new FileInputStream(path);
            
            try {
                InputStreamReader input = new InputStreamReader(stream,
                    Charset.defaultCharset());
                Reader reader = new BufferedReader(input);
                
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                
                while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, read);
                }
                
                // HACK: The parser expects every statement to end in a newline,
                // even the very last one, so we'll just tack one on here in
                // case the file doesn't have one.
                builder.append("\n");
                
                return builder.toString();
            } finally {
                stream.close();
            }
        } catch (IOException ex) {
            return null;
        }
    }
    
    /**
     * Reads the file from the given path and returns its contents as a single
     * string.
     * 
     * @param  file  The text file to read.
     * @return       The contents of the file or null if the load failed.
     * @throws       IOException
     */
    private static String readFile(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            
            try {
                InputStreamReader input = new InputStreamReader(stream,
                    Charset.defaultCharset());
                Reader reader = new BufferedReader(input);
                
                StringBuilder builder = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                
                while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, read);
                }
                
                // HACK: The parser expects every statement to end in a newline,
                // even the very last one, so we'll just tack one on here in
                // case the file doesn't have one.
                builder.append("\n");
                
                return builder.toString();
            } finally {
                stream.close();
            }
        } catch (IOException ex) {
            return null;
        }
    }
}