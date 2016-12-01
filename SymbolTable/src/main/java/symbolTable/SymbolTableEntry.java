package symbolTable;

import lex.TokenType;

public class SymbolTableEntry {



	String name;
	TokenType type;

	//Defining boolean value isItReserved
	//So we can use it in isReserved()
	public boolean isItReserved = false;
	int numParams;
	boolean isParam;
	int address;
	ParamInfo paramInfo;

	public SymbolTableEntry () {}
	
	public SymbolTableEntry (String name) {
		this.name = name;
	} 
	
	public SymbolTableEntry (String name, TokenType type) {
		this.name = name;
		this.type = type;
	}

	public int getAddress() {
		return -999999999;
	}

	public void setNumParams(int numParams) {
		this.numParams = numParams;
	}

	public int getNumParams() {
		return numParams;
	}

	public void setIsParam(boolean pm) {
		this.isParam = pm;
	}

	public boolean getIsParam() {
		return isParam;
	}

	public ParamInfo getParamInfo() {
		return paramInfo;
	}

	public void setAddress(int addressy) {
		this.address = addressy;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public TokenType getType() {
		return type;
	}
	public void setType(TokenType type) {
		this.type = type;
	}
	public boolean isVariable() { return false; } 
	public boolean isKeyword() { return false; } 
	public boolean isProcedure() { return false; } 
	public boolean isFunction() { return false; } 
	public boolean isFunctionResult() { return false; }  
	public boolean isParameter() { return false; }  
	public boolean isArray() { return false; } 
	public boolean isConstant() { return false; }

	//Modified the given isReserved() to return the above defined boolean
	//instead of always returning false
	public boolean isReserved() { return isItReserved; }

	//Simple function to set the value of whatever
	//isReserved() returns whenever it is called
	public void setReserved(boolean bool) {
		isItReserved = bool;
	}



}
