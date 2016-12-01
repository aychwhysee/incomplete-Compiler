package symbolTable;

import lex.TokenType;

import java.util.List;

public class FunctionEntry extends SymbolTableEntry {

	//Basically copied the given KeywordEntry class file
	//and modified the constructors and made
	//appropriate accessors and print statements.

	//It's pretty straightforward and self-explanatory

	int numberOfParameters;
	List parameterInfo;
	VariableEntry result;

	public FunctionEntry(String name) {
		super(name);
	}

	public FunctionEntry(String name, int numberOfParameters,
						 List parameterInfo, VariableEntry result) {
		super(name);
		this.numberOfParameters = numberOfParameters;
		this.parameterInfo = parameterInfo;
		this.result = result;
	}

	public boolean isFunction() { return true; }
	
	public TokenType getKey() { 
		return type;
	}

	public int getNumberOfParameters() {
		return this.numberOfParameters;
	}

	public List getParameterInfo() {
		return this.parameterInfo;
	}

	public VariableEntry getResult() {
		return this.result;
	}

	public void setResult(VariableEntry resulty) {
		this.result = resulty;
	}

	public void setKey(TokenType keyword) {
		this.type = keyword;
	}
	
	public void print () {
		
		System.out.println("Keyword Entry:");
		System.out.println("   Name    : " + this.getName());
		System.out.println("No. Params : " + this.getNumberOfParameters());
		System.out.println("Param Info : " + this.getParameterInfo());
		System.out.println("  Result   : " + this.getResult());
		System.out.println();
	}
}
