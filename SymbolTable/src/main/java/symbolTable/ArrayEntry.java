package symbolTable;

import lex.TokenType;

public class ArrayEntry extends SymbolTableEntry {

	//Basically copied the given KeywordEntry class file
	//and modified the constructors and made
	//appropriate accessors and print statements.

	//It's pretty straightforward and self-explanatory

	int address;
	int upperBound;
	int lowerBound;

	public ArrayEntry(String name) {
		super(name);
	}

	public ArrayEntry(String name, TokenType type) {
		super(name,type);
	}

	public ArrayEntry(String name, int address, TokenType type, int upperBound, int lowerBound)
	{
		super(name, type);
		this.address = address;
		this.upperBound = upperBound;
		this.lowerBound = lowerBound;
	}

	public boolean isArray() { return true; }

	public TokenType getType() {
		return type;
	}

	public void setType(TokenType keyword) {
		this.type = keyword;
	}

	public void setUpperBound(int ub) {
		this.upperBound = ub;
	}

	public void setLowerBound(int lb) {
		this.lowerBound = lb;
	}

	public int getAddress() {
		return this.address;
	}

	public int getUpperBound() {
		return this.upperBound;
	}

	public int getLowerBound() {
		return this.lowerBound;
	}

	public String getName() {
		return name;
	}

	public void print () {
		
		System.out.println("Array Entry:");
		System.out.println("   Name    : " + this.getName());
		System.out.println("   Type    : " + this.getType());
		System.out.println("  Address  : " + this.getAddress());
		System.out.println("Upper Bound: " + this.getUpperBound());
		System.out.println("Lower Bound: " + this.getLowerBound());
		System.out.println();
	}


}
