package symbolTable;

import lex.TokenType;

public class VariableEntry extends SymbolTableEntry {

	//Basically copied the given KeywordEntry class file
	//and modified the constructors and made
	//appropriate accessors and print statements.

	//It's pretty straightforward and self-explanatory

	int address;

	public VariableEntry(String name) {
		super(name);
	}

	public VariableEntry(String name, TokenType type) {
		super(name, type);
	}

	public VariableEntry(String name, int address, TokenType type) {
		super(name, type);
		this.address = address;
	}

	public boolean isVariable() { return true; }

	public int getAddress() {
		return this.address;
	}

	public TokenType getType() {
		return type;
	}

	public void setKey(TokenType keyword) {
		this.type = keyword;
	}

	public String getName() {
		return name;
	}

	public void print () {
		
		System.out.println("Keyword Entry:");
		System.out.println("   Name    : " + this.getName());
		System.out.println("  Address  : " + this.getAddress());
		System.out.println("   Type    : " + this.getType());
		System.out.println();
	}
}
