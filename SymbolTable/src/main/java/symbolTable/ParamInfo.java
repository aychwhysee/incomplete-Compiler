package symbolTable;

import lex.TokenType;

public class ParamInfo {

    TokenType type;
    int upperBound;
    int lowerBound;
    boolean isArray;

    public void setType(TokenType typey) {
        this.type = typey;
    }

    public void setUpperBound(int ub) {
        this.upperBound = ub;
    }

    public void setLowerBound(int lb) {
        this.lowerBound = lb;
    }

    public void setIsArray(boolean iA) {
        this.isArray = iA;
    }

    public TokenType getType() {
        return type;
    }

    public int getUpperBound() {
        return upperBound;
    }

    public int getLowerBound() {
        return lowerBound;
    }

    public boolean getIsArray() {
        return isArray;
    }


}
