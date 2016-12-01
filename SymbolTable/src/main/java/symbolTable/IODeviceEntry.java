package symbolTable;

import lex.TokenType;

public class IODeviceEntry extends SymbolTableEntry {

	//Basically copied the given KeywordEntry class file
	//and modified the constructors and made
	//appropriate accessors and print statements.

	//It's pretty straightforward and self-explanatory

	public IODeviceEntry(String name) {
		super(name);
	}

/*	public IODeviceEntry(String name, TokenType keyword) {
		super(name, keyword);
	}*/

	//public boolean isKeyword() { return true; }
	
/*
	public TokenType getKey() {
		return type;
	}
*/

/*	public void setKey(TokenType keyword) {
		this.type = keyword;
	}*/

	public void print () {

		System.out.println("Keyword Entry:");
		System.out.println("   Name    : " + this.getName());
		//System.out.println("   Type    : " + this.getKey());
		System.out.println();
	}
}
