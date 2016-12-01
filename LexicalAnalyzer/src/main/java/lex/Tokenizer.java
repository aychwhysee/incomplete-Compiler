package lex;

import errors.LexicalError;
import symbolTable.KeywordTable;
import symbolTable.SymbolTable;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/*
 */
public class Tokenizer
{
	private CharStream stream = null;

	/** The KeywordTable is a SymbolTable that comes with all of the KeywordEntries
	 *  already inserted.
	 */
	private KeywordTable keywordTable;
	private SymbolTable table;
	private Token lastToken;
	static final int MAX_IDENTIFIER_SIZE = 64;

	public Tokenizer(String filename) throws IOException, LexicalError
	{
		super();
		init(new CharStream(filename));
	}

	/** Used during testing to read files from the classpath. */
	public Tokenizer(URL url) throws IOException, LexicalError
	{
		super();
		init(new CharStream(url));
	}

	public Tokenizer(File file) throws IOException, LexicalError
	{
		super();
		init(new CharStream(file));
	}

	protected void init(CharStream stream)
	{
		this.stream = stream;
		keywordTable = new KeywordTable();
		lastToken = new Token();

	}

	public int getLineNumber()
	{
		return stream.lineNumber();
	}

	public Token getNextToken() throws LexicalError
	{
//		Buffer buffer;
//		buffer.clear();

		// MAYBEFIX Too many Token objects are being created. The methods getNextToken,
		// readIdentifier, readNumber, and readSymbol all create Token objects even
		// though only one of the objects will be used.
		Token token = new Token();
		token.clear();
		char c = stream.currentChar();
		//MAYBE FIX This is not required as the CharStream swallows comments for us.
		//FIXED
		/*if (c == '{') {
			while (!(c == '}')) {
				c = stream.currentChar();
			}
		}*/
		if (c == CharStream.BLANK)
			c = stream.currentChar();


/*		if (Character.isLetter(c))
			token = readIdentifier(c);
		else if (c == stream.EOF) token.setType(TokenType.ENDOFFILE);
		else if (Character.isDigit(c))
			token = readNumber(c);
		else
			token = readSymbol(c);*/
		if (Character.isLetter(c)) {
			token = readIdentifier(c);
		} else if (c == stream.EOF) {
			token.setType(TokenType.ENDOFFILE);
		} else if (Character.isDigit(c)) {
			token = readNumber(c);
		} else {
			token = readSymbol(c);
		}
		lastToken = token;
		return token;
	}

	public boolean isSym(char ch)
	{
		return (ch == '=' ||
				ch == '<' ||
				ch == '>' ||
				ch == '+' ||
				ch == '-' ||
				ch == '*' ||
				ch == '/' ||
				ch == '.' ||
				ch == '(' ||
				ch == ')' ||
				ch == ',' ||
				ch == ':' ||
				ch == ';' ||
				ch == '[' ||
				ch == ']');
	}

	protected Token readIdentifier(char nextChar) throws LexicalError
	{
		Token token = new Token();
		StringBuffer stringBuffer = new StringBuffer();
		while (Character.isDigit(nextChar) || Character.isLetter(nextChar))
		{
			//stringBuffer.addChar(nextChar);
			stringBuffer.append(nextChar);
			nextChar = stream.currentChar();
		}
		if (!(nextChar == CharStream.BLANK))
			stream.pushBack(nextChar);
		if (stringBuffer.toString().length() > MAX_IDENTIFIER_SIZE)
			throw LexicalError.IdentifierTooLong(stringBuffer.toString());
		if (keywordTable.lookup(stringBuffer.toString().toUpperCase()) != null) {
			token.setType(keywordTable.lookup(stringBuffer.toString().toUpperCase()).getType());
		}
		else {
			token.setType(TokenType.IDENTIFIER);
		}
		token.setValue(stringBuffer.toString().toUpperCase());

		return token;
	}

	protected Token readNumber(char nextChar) throws LexicalError {
		Token token = new Token();
		StringBuffer stringBuffer = new StringBuffer();
		while (Character.isDigit(nextChar)) {
			stringBuffer.append(nextChar);
		nextChar = stream.currentChar();
	}
//		if (!(nextChar == CharStream.BLANK))
//			stream.pushBack(nextChar);
		token.setType(TokenType.INTCONSTANT);
		token.setValue(stringBuffer.toString());
		//nextChar = stream.currentChar();

		//FIXED We only care if '.' comes next.
//		if (isSym(nextChar))
//		{
		if (nextChar == '.') {
			char thisChar = nextChar;
			nextChar = stream.currentChar();
//				if (nextChar == '.')
//				{
//					stream.pushBack(thisChar);
//					stream.pushBack(nextChar);
//					token.setType(TokenType.INTCONSTANT);
//					token.setValue(stringBuffer.toString());
//				} else
			if (Character.isDigit(nextChar)) {
				// FIXME Should only accept digits not all non-blank characters.
				while (Character.isDigit(nextChar)) {
					stringBuffer.append(nextChar);
					nextChar = stream.currentChar();
				}
				stream.pushBack(nextChar);
				token.setType(TokenType.REALCONSTANT);
				token.setValue(stringBuffer.toString());
			}
			// FIX If we found a dot not followed by a number or another dot then
			// we've found an ENDMARKER and need to push it back for the next
			// call to getNextToken.
			else {
				stream.pushBack(nextChar);
				stream.pushBack(thisChar);
			}
		}
		//FIXED Similarly, if parsing a real we only care if we encounter 'e' or 'E'
		//} else if (Character.isLetter(nextChar))
		//{
		else if (nextChar == 'e' || nextChar == 'E') {
			nextChar = stream.currentChar();
			char thisChar = nextChar;
			nextChar = stream.currentChar();
			if(thisChar == '-' && Character.isDigit(nextChar)){
				stringBuffer.append(thisChar);
			}
			else {
				stream.pushBack(nextChar);
				nextChar = thisChar;
			}
			if(Character.isDigit(nextChar)) {
				// FIXME Should only accept digits not all non-blank characters.
				while (Character.isDigit(nextChar)) {
					stringBuffer.append(nextChar);
					nextChar = stream.currentChar();
				}
				stream.pushBack(nextChar);
				token.setType(TokenType.REALCONSTANT);
				token.setValue(stringBuffer.toString());
			}
			else {
				stream.pushBack('e');
				stream.pushBack(nextChar);
			}
		} else {
			stream.pushBack(nextChar);
		}
//		} //else stream.pushBack(nextChar);
		/*nextChar = stream.currentChar();
		stream.pushBack(nextChar);*/ //this does nothing, i think
		return token;
	}

	protected Token readSymbol (char nextChar) throws LexicalError
	{
		Token token = new Token();
		StringBuffer stringBuffer = new StringBuffer();
//		while (isSym(nextChar))
//		{
//			stringBuffer.append(nextChar);
//			nextChar = stream.currentChar();
//		}
		//stringBuffer.append(nextChar);
		if (nextChar == '<') {
			nextChar = stream.currentChar();
			if (nextChar == '>') {
				token.setType(TokenType.RELOP);
				token.setValue("<>"); //token.setValue("2");
			} else if (nextChar == '=') {
				token.setType(TokenType.RELOP);
				token.setValue("<="); //token.setValue("5");
			} else {
				//nextChar = stream.currentChar();
				stream.pushBack(nextChar);
				token.setType(TokenType.RELOP);
				token.setValue("<"); //token.setValue("3");
			}
		} else if (nextChar == '>') {
			nextChar = stream.currentChar();
			if (nextChar == '=') {
				token.setType(TokenType.RELOP);
				token.setValue(">="); //token.setValue("5");
			} else {
				stream.pushBack(nextChar);
				token.setType(TokenType.RELOP);
				token.setValue(">"); //token.setValue("4");
			}
		} else if (nextChar == '=') {
			token.setType(TokenType.RELOP);
			token.setValue("="); //token.setValue("1")
		} else if (nextChar == '*') {
			token.setType(TokenType.MULOP);
			token.setValue("*"); //token.setValue("1")
		} else if (nextChar == '/') {
			token.setType(TokenType.MULOP);
			token.setValue("/"); //token.setValue("2")
		} else if (nextChar == '(') {
			token.setType(TokenType.LEFTPAREN);
			token.setValue("(");
		} else if (nextChar == ')') {
			token.setType(TokenType.RIGHTPAREN);
			token.setValue(")");
		} else if (nextChar == ',') {
			token.setType(TokenType.COMMA);
			token.setValue(",");
		} else if (nextChar == ';') {
			token.setType(TokenType.SEMICOLON);
			token.setValue(";");
		} else if (nextChar == '[') {
			token.setType(TokenType.LEFTBRACKET);
			token.setValue("[");
		} else if (nextChar == ']') {
			token.setType(TokenType.RIGHTBRACKET);
			token.setValue("]");
		} else if (nextChar == ':') {
			nextChar = stream.currentChar();
			if (nextChar == '=') {
				token.setType(TokenType.ASSIGNOP);
				token.setValue(":=");
			} else {
				stream.pushBack(nextChar);
				token.setType(TokenType.COLON);
				token.setValue(":");
			}
		} else if (nextChar == '+') {
			if (lastToken.getType() == TokenType.RIGHTPAREN ||
					lastToken.getType() == TokenType.RIGHTBRACKET ||
					lastToken.getType() == TokenType.IDENTIFIER ||
					lastToken.getType() == TokenType.INTCONSTANT ||
					lastToken.getType() == TokenType.REALCONSTANT) {
				token.setType(TokenType.ADDOP);
				token.setValue("+"); //token.setValue("1");
				//FIXED
				token.setOpType(Token.OperatorType.ADD);
			} else {
				//System.out.println("Hellohello!");
				token.setType(TokenType.UNARYPLUS);
				token.setValue("+");
			}
		} else if (nextChar == '-') {
			if (lastToken.getType() == TokenType.RIGHTPAREN ||
					lastToken.getType() == TokenType.RIGHTBRACKET ||
					lastToken.getType() == TokenType.IDENTIFIER ||
					lastToken.getType() == TokenType.INTCONSTANT ||
					lastToken.getType() == TokenType.REALCONSTANT) {
				token.setType(TokenType.ADDOP);
				token.setValue("-"); //token.setValue("2");
				//FIXED
				token.setOpType(Token.OperatorType.SUBTRACT);
			} else {
				nextChar = stream.currentChar();
				if (Character.isDigit(nextChar)) {
					token = readNumber(nextChar);
				} else {
				// FIXED Push back nextChar since it is not consumed.
				stream.pushBack(nextChar);
				token.setType(TokenType.UNARYMINUS);
				token.setValue("-");
				}
			}
		} else if (nextChar == '.') {
			//Token previous = lastToken;
			//Character thisChar = nextChar;
			nextChar = stream.currentChar();
			if (nextChar == '.') {
				token.setType(TokenType.DOUBLEDOT);
				token.setValue("..");
				//stream.pushBack(thisChar);
				//stream.pushBack(nextChar);
			} else {
				//stream.pushBack(nextChar);
				token.setType(TokenType.ENDMARKER);
				token.setValue(".");
				stream.pushBack(nextChar);
			}
		}
		/*if (!(nextChar == CharStream.BLANK))
			stream.pushBack(nextChar);*/

		//String stringy = stringBuffer.toString();
		/*switch(stringy){
//			case "=":
//				token.setType(TokenType.RELOP);
//				token.setValue("="); //token.setValue ("1")
//				break;
//			case "<":
//				token.setType(TokenType.RELOP);
//				token.setValue("<"); //token.setValue ("3")
//				break;
//			case ">":
//				token.setType(TokenType.RELOP);
//				token.setValue(">"); //token.setValue("4")
//				break;
//			case "<>":
//				token.setType(TokenType.RELOP);
//				token.setValue("<>"); //token.setValue("2")
//				break;
//			case "<=":
//				token.setType(TokenType.RELOP);
//				token.setValue("<="); //token.setValue("5")
//				break;
//			case ">=":
//				token.setType(TokenType.RELOP);
//				token.setValue(">="); //token.setValue("5")
//				break;
//			case "+":
//				token.setType(TokenType.UNARYPLUS);
//				token.setValue("+"); //token.setValue("1")?
//				break;
//			case "-":
//				token.setType(TokenType.UNARYMINUS);
//				token.setValue("-"); //token.setValue("2")?
//				break;
//			case "*":
//				token.setType(TokenType.MULOP);
//				token.setValue("*"); //token.setValue("1")
//				break;
//			case "/":
//				token.setType(TokenType.MULOP);
//				token.setValue("/"); //token.setValue("2")
//				break;
*//*			case ".":
				token.setType(TokenType.ENDMARKER);
				token.setValue(".");
				break;*//*
//			case "(":
//				token.setType(TokenType.LEFTPAREN);
//				token.setValue("(");
//				break;
//			case ")":
//				token.setType(TokenType.RIGHTPAREN);
//				token.setValue(")");
//				break;
//			case ",":
//				token.setType(TokenType.COMMA);
//				token.setValue(",");
//				break;
//			case ":":
//				token.setType(TokenType.COLON);
//				token.setValue(":");
//				break;
//			case ";":
//				token.setType(TokenType.SEMICOLON);
//				token.setValue(";");
//				break;
//			case "[":
//				token.setType(TokenType.LEFTBRACKET);
//				token.setValue("[");
//				break;
//			case "]":
//				token.setType(TokenType.RIGHTBRACKET);
//				token.setValue("]");
//				break;
*//*			case "..":
				token.setType(TokenType.DOUBLEDOT);
				token.setValue("..");
				break;*//*
		}*/
		return token;
	}

}
