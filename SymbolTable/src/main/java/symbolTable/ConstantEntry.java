package symbolTable;

import lex.TokenType;

public class ConstantEntry extends SymbolTableEntry {

	//Basically copied the given KeywordEntry class file
	//and modified the constructors and made
	//appropriate accessors and print statements.

	//It's pretty straightforward and self-explanatory

	public ConstantEntry(String name) {
		super(name);
	}

	public ConstantEntry(String name, TokenType type) {
		super(name, type);
	}

	public boolean isConstant() { return true; }
	
	public TokenType getType() {
		return type;
	}

	public void setKey(TokenType keyword) {
		this.type = keyword;
	}
	
	public void print () {
		
		System.out.println("Keyword Entry:");
		System.out.println("   Name    : " + this.getName());
		System.out.println("   Type    : " + this.getType());
		System.out.println();
	}
}
