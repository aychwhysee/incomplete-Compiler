/*
 * Copyright 2016 Vassar College
 * All rights reserverd.
 */

package parser;


import errors.LexicalError;
import errors.SemanticError;
import errors.SymbolTableError;
import errors.SyntaxError;
import grammar.GrammarSymbol;
//import jdk.nashorn.internal.parser.TokenType;
import grammar.NonTerminal;
import grammar.SemanticAction;
import jdk.nashorn.internal.ir.Symbol;
import lex.TokenType;
import lex.Token;
import lex.Tokenizer;

import semanticActions.SemanticActions;
import symbolTable.SymbolTableEntry;

import java.io.File;
import java.io.IOException;
import java.util.Stack;

//haha
public class Parser
{
	private static final int ERROR = 999;

	private ParseTable parsetable = new ParseTable();
	private RHSTable rhsTable = new RHSTable();

	//a stack of GrammarSymbols called parseStack.
	private Stack<GrammarSymbol> parseStack = new Stack<GrammarSymbol>();

	//boolean for errors
	private boolean foolean = false;

	//boolean to turn on/turn off dump_stack function
	// true = on, false = off
	private boolean useDumpStack = false;

	private Tokenizer tokenizer;

	private SemanticActions actions;


	public Parser (String filename) throws IOException, LexicalError, SymbolTableError
	{
		this(new File(filename));
	}

	public Parser(File file) throws IOException, LexicalError, SymbolTableError
	{
		tokenizer = new Tokenizer(file);
		actions = new SemanticActions(tokenizer);
	}

	public void parse () throws SyntaxError, LexicalError, SemanticError, SymbolTableError {
		// TODO Write a parser.
		//SemanticActions actions = new SemanticActions(tokenizer);
		// Current token = GET Next Token (from Lexical Analyzer)
		foolean = false;
		Token currentToken = tokenizer.getNextToken();
		Token previous = new Token();
		// set parse stack to empty stack;
		parseStack.clear();
		//Push ENDOFFILE and Start symbol ON parse stack
		parseStack.push(TokenType.ENDOFFILE);
		parseStack.push(NonTerminal.Goal); //start symbol?
		// WHILE parse stack not empty
		while (!parseStack.isEmpty()
				&& !foolean
				) { //foolean will be for errors
			//set Predicted to POP(Parse stack)
			if (useDumpStack) {
				dump_stack();
				System.out.println(currentToken);
			}
			GrammarSymbol predicted = parseStack.pop();
			//IF Predicted is a Token class
			if (predicted.isToken()) {
				//Try a match move:
				//If predicted = current token
				if (predicted == currentToken.getType()) {
					// Matched, move on
					previous = currentToken;
					currentToken = tokenizer.getNextToken();
				//else if predicted is not equal to current token,
				} else {
					//ERROR "expecting x, y found"
					// x is Predicted; y is current token
					// set error bool to true so program stops
					System.out.println("Expecting " + predicted + ", "
										+ currentToken + " found, on line " +
															//Probably not the way i want to get the line...
															(tokenizer.getLineNumber()-1));
					foolean = true;

					//The next two lines were a simple attempt at moving on after an error
					//to show more errors. Obviously didn't work
					//currentToken = tokenizer.getNextToken();
					//parseStack.pop();
				}
			//else if predicted is a non-terminal
			} else if (predicted.isNonTerminal()){
				//If the entry we get at the current indexes in parseTable is 999, we have an error.
				int entry = parsetable.getEntry(currentToken.getType(), (NonTerminal) predicted);
				if (entry == 999) {
					//ERROR "unexpected:" current token
					// set error bool to true so program stops
					System.out.println("Unexpected: " + currentToken
														//Probably not the way to get the correct line...
										+ " on line " + (tokenizer.getLineNumber()-1));
					foolean = true;

					//The next two lines were a simple attempt at moving on after an error
					//to show more errors. Obviously didn't work
					//currentToken = tokenizer.getNextToken();
					//parseStack.pop();
				//else if we have a negative value, do nothing/ignore it
				} else if (entry < 0);
				//else do the following
				else {
					//for easy calling later
					GrammarSymbol[] rhs = rhsTable.getRule(entry);
					//iterate from end of array and push the GrammarSymbols onto parse stack
					for (int i = (rhs.length -1); i >= 0; i--) {
						parseStack.push(rhs[i]);
					}
				}
			}
			else if (predicted.isAction()) {
				SemanticAction action = (SemanticAction) predicted;
				actions.execute(action.getIndex(),previous);
			}
		}
	}

	public SymbolTableEntry lookup(String name) {
		return actions.lookup(name);
	}


	public boolean error (){
		return foolean;
	} //we're calling in "foolean" for...reasons :)

	//Prints the current contents of the parse stack when called
	public void dump_stack() {
		//For each GrammarSymbol gs in parseStack
		for (GrammarSymbol gs : parseStack) {
			//Print out gs
			System.out.print(gs + " | ");
			//tempStack.peek();
			//System.out.println();

		}
		//new line for each new stack printed
		System.out.println();
	}
}

