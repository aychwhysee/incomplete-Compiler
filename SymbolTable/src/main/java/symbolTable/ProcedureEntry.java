package symbolTable;

import lex.TokenType;

import java.util.List;

public class ProcedureEntry extends SymbolTableEntry {

	//Basically copied the given KeywordEntry class file
	//and modified the constructors and made
	//appropriate accessors and print statements.

	//It's pretty straightforward and self-explanatory

	int numberOfParameters;
	List parameterInfo;

	public ProcedureEntry(String name) {
		super(name);
	}

	//Made this constructor to only take in two of the three
	//things to make it easier in SymbolTable
	public ProcedureEntry(String name, int numberOfParameters)
	{
		super(name);
		this.numberOfParameters = numberOfParameters;
	}

	public ProcedureEntry(String name, int numberOfParameters,
						  List parameterInfo) {
		super(name);
		this.numberOfParameters = numberOfParameters;
		this.parameterInfo = parameterInfo;
	}



	public boolean isProcedure() { return true; }
	
	public TokenType getKey() { 
		return type;
	}

	public int getNumberOfParameters() {
		return this.numberOfParameters;
	}

	public List getParameterInfo() {
		return this.parameterInfo;
	}

	public void setKey(TokenType keyword) {
		this.type = keyword;
	}
	
	public void print () {
		
		System.out.println("Keyword Entry:");
		System.out.println("   Name    : " + this.getName());
		System.out.println("No. Params : " + this.getNumberOfParameters());
		System.out.println("Param info : " + this.getParameterInfo());
		System.out.println();
	}


}
