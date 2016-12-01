package semanticActions;

import java.lang.*;

import com.sun.org.apache.xpath.internal.operations.Variable;
import errors.*;

import java.util.*;

import jdk.nashorn.internal.ir.Symbol;
import lex.*;
import sun.security.krb5.internal.crypto.EType;
import symbolTable.*;

public class SemanticActions {
    //hello
    private Stack<Object> semanticStack;
    private boolean insert;
    private boolean isArray;
    private boolean global;
    private int globalMemory;
    private int localMemory;

    private SymbolTable globalTable;
    private SymbolTable localTable;


    private SymbolTable constantTable;

    private Quadruples quadruples;
    private int globalStore;
    private int localStore;
    private SymbolTableEntry currentFunction;

    private int tableSize = 97;
    private boolean isParam;
    private SymbolTableEntry nullEntry = null;
    private Tokenizer tokenizer;
    private Stack<Integer> paramCount;
    private Stack<ParamInfo> nextParam;

    //true to print stack in output window
    private boolean useDumpStack = true;
    private boolean useDumpTable = false;

    public enum Etype {ARITHMETIC, RELATIONAL}

    public SemanticActions(Tokenizer tokenizer) throws SymbolTableError {
        semanticStack = new Stack<Object>();
        //insert was false before, but instructions on website say insert/search = insert so..
        insert = true;
        isArray = false;
        isParam = false;
        global = true;
        globalMemory = 0;
        localMemory = 0;
        globalTable = new SymbolTable(tableSize);
        constantTable = new SymbolTable(tableSize);
        installBuiltins(globalTable);
        this.tokenizer = tokenizer;
        this.quadruples = new Quadruples();
        globalStore = 0;
        localStore = 0;
        currentFunction = nullEntry;
        paramCount = new Stack<Integer>();
        nextParam = new Stack<ParamInfo>();
    }

    public void execute(int actionNumber, Token token) throws SemanticError, SymbolTableError {

        debug("calling action : " + actionNumber + " with token " + token.getType() + " with value " + token.getValue());
        //System.out.println("Hello! Execute has been called");
        //Dump Table use for debugging
        if (useDumpTable) {
            if (global) {
                globalTable.dumpTable();
                System.out.println();
            } else {
                localTable.dumpTable();
                System.out.println();
            }
        }
        //Dump Stack use for debugging
        if (useDumpStack) {
            dump_semanticStack();
        }
        switch (actionNumber) {
            case 1:
                //INSERT/SEARCH = INSERT
                insert = true;
                //for debugging
                //System.out.println(insert);
                break;
            case 2:
                //INSERT/SEARCH = SEARCH
                insert = false;
                break;
            case 3:
                // TYP = pop type
                TokenType typ = (TokenType) semanticStack.pop();
                //If array/simple = array
                if (isArray) {
                    //Set up some stuff
                    Token ub = (Token) semanticStack.pop();
                    Token lb = (Token) semanticStack.pop();
                    //Get the int values of UB and LB
                    int ubi = Integer.parseInt(ub.getValue());
                    int lbi = Integer.parseInt(lb.getValue());
                    //Set msize to be UB - LB + 1
                    int msize = ubi - lbi + 1;
                    //Boolean set up to see if we've looked
                    //far enough into the stack, used below in while
                    boolean enough = false;
                    //Go through stack while its not empty and we
                    //haven't found enough/what we want
                    while (!(semanticStack.isEmpty()) && !enough) {
                        //Pop item, save it
                        Object itemy = semanticStack.pop();
                        //Check if it's a Token
                        if (itemy instanceof Token) {
                            //If global/local = global
                            if (global) {
                                //Insert id in globalTable as an ArrayEntry
                                //With the below parameters in constructor
                                ArrayEntry id = new ArrayEntry(((Token) itemy).getValue(), globalMemory, typ, ubi, lbi);
                                SymbolTableEntry id2 = this.lookup(((Token) itemy).getValue());
                                if (insert) {
                                    if (id2 == null) {
                                        globalTable.insert(id);
                                    } else if (id2.isReserved()) {
                                        throw SemanticError.ReservedName(((Token) itemy).getValue(), tokenizer.getLineNumber());
                                    } else {
                                        throw SemanticError.MultipleDeclaration(((Token) itemy).getValue(), tokenizer.getLineNumber());
                                    }
                                } else {
                                    //globalTable.insert(id);
                                }
                                //globalTable.insert(id);
                                //Increase globalMemory by msize
                                globalMemory = globalMemory + msize;
                                //If global/local = local
                            } else {
                                //Insert id in localTable as an ArrayEntry
                                //With the below parameters in constructor
                                // FOR LOCAL STUFF: Maybe need to copy/paste the above error throwing stuff
                                //                  for the future when we implement local stuff
                                ArrayEntry id = new ArrayEntry(((Token) itemy).getValue(), localMemory, typ, ubi, lbi);
                                localTable.insert(id);
                                //Increase localMemory by msize
                                localMemory = localMemory + msize;
                            }
                            //If item is not a token, just push it back on
                            //and break out of the while loop. Enough.
                        } else {
                            semanticStack.push(itemy);
                            enough = true;
                        }
                    }
                    //If array/simple = simple
                } else {
                    //Same as above, set up a boolean for the while loop
                    boolean enough = false;
                    //This while loop is almost identical to the above, except we're
                    //inserting as VariableEntry rather than ArrayEntry, and only
                    //incrementing the addresses by 1.
                    while (!(semanticStack.isEmpty()) && !enough) {
                        Object itemy = semanticStack.pop();
                        if (itemy instanceof Token) {
                            if (global) {
                                VariableEntry id = new VariableEntry(((Token) itemy).getValue(), globalMemory, typ);
                                SymbolTableEntry id2 = this.lookup(((Token) itemy).getValue());
                                if (insert) {
                                    if (id2 == null) {
                                        globalTable.insert(id);
                                    } else if (id2.isReserved()) {
                                        throw SemanticError.ReservedName(((Token) itemy).getValue(), tokenizer.getLineNumber());
                                    } else {
                                        throw SemanticError.MultipleDeclaration(((Token) itemy).getValue(), tokenizer.getLineNumber());
                                    }
                                } else {
                                    //globalTable.insert(id);
                                }
                                //globalTable.insert(id);
                                globalMemory = globalMemory + 1;
                                //System.out.println(globalTable.lookup(id.getName()).getName());
                            } else {
                                // FOR LOCAL STUFF: Maybe need to copy/paste the above error throwing stuff
                                //                  for the future when we implement local stuff
                                VariableEntry id = new VariableEntry(((Token) itemy).getValue(), localMemory, typ);
                                localTable.insert(id);
                                localMemory = localMemory + 1;
                            }
                        } else {
                            semanticStack.push(itemy);
                            enough = true;
                        }
                    }
                }
                //Array/simple = simple
                isArray = false;
                System.out.println(isArray);
                //For debugging
                //globalTable.dumpTable();
                break;
            case 4:
                //push TYPE
                semanticStack.push(token.getType());
                break;
            case 5:
                // INSERT/SEARCH = SEARCH
                insert = false;
//                generate("alloc", Integer.toString(localMemory));
//                generate("PROCBEGIN");
                SymbolTableEntry id5 = (SymbolTableEntry) semanticStack.pop();
                generate("PROCBEGIN", id5);
                localStore = quadruples.getNextQuad();
                generate("alloc", "_");
                break;
            case 6:
                //ARRAY/SIMPLE = ARRAY
                isArray = true;
                System.out.println(isArray);
                break;
            case 7:
                //push CONSTANT
                semanticStack.push(token);
                break;
            case 9:
                //New/updated from website:
//				#9   : insert the top two ids on semantic stack in the symbol table (IODevice entry), mark as RESTRICTED
//			    : insert the bottom-most id in the symbol table (Procedure entry, with number of parameters = 0), mark as RESTRICTED
//				: pop ids
//				: INSERT/SEARCH = SEARCH
                //Set up
                Token id1 = (Token) semanticStack.pop();
                Token id2 = (Token) semanticStack.pop();
                Token id3 = (Token) semanticStack.pop();
                // Top two ids on stack as IODeviceEntries
                IODeviceEntry iod1 = new IODeviceEntry(id1.getValue());
                IODeviceEntry iod2 = new IODeviceEntry(id2.getValue());
                // Bottom-most ID as ProcedureEntry
                ProcedureEntry pe = new ProcedureEntry(id3.getValue(), 0);
                //If global/local = global
                if (global) {
                    //Insert the above defined things
                    globalTable.insert(iod1);
                    globalTable.insert(iod2);
                    globalTable.insert(pe);
                    iod1.setReserved(true);
                    iod2.setReserved(true);
                    pe.setReserved(true);
                } else {
                    // If global/local = local
                    localTable.insert(iod1);
                    localTable.insert(iod2);
                    localTable.insert(pe);
                    iod1.setReserved(true);
                    iod2.setReserved(true);
                    pe.setReserved(true);
                }
                //Insert/Search = search
                insert = false;
                //for debugging
                //System.out.println(insert);
                //GEN
                generate("call", "main", "0");
                generate("exit");
                break;
            case 11:
                // GLOBAL/LOCAL = GLOBAL
                global = true;
                localTable = new SymbolTable();
                currentFunction = nullEntry;
                // fill in quadruple at location localstore with value localmem
                backPatch(localStore, localMemory);
                generate("free", Integer.toString(localMemory));
                generate("PROCEND");
                break;
            case 13:
                //push ID
                //token.getValue() wouldn't help us a lot
                //because its just a string...so push entire token
                semanticStack.push(token);
                break;
            case 15:
                FunctionEntry fe15 = new FunctionEntry(token.getValue());
                // insert id in symbol table as function entry
                if (global) {
                    globalTable.insert(fe15);
                } else {
                    localTable.insert(fe15);
                }
                // push id
                semanticStack.push(fe15);
                // create
                VariableEntry funny = create(fe15.getName(), TokenType.INTEGER);
                // id.result = the above created thing
                fe15.setResult(funny);
                // GLOBAL/LOCAL = LOCAL
                global = false;
                localMemory = 0;
                break;
            case 16:
                // TODO: Check this
                // Pop type
                Token token16 = (Token) semanticStack.pop();
                TokenType type16 = token16.getType();
                // pop id
                SymbolTableEntry id16 = (SymbolTableEntry) semanticStack.pop();
                // set type
                id16.setType(type16);
                currentFunction = id16;
                break;
            case 17:
                ProcedureEntry pe17 = new ProcedureEntry(token.getValue());
                // insert id in symboltable as procedureEntry
                if (global) {
                    globalTable.insert(pe17);
                } else {
                    localTable.insert(pe17);
                }
                // push id
                semanticStack.push(pe17);
                // GLOBAL/LOCAL = LOCAL
                global = false;
                localMemory = 0;
                break;
            case 19:
                paramCount.push(0);
                break;
            case 20:
                SymbolTableEntry id20 = (SymbolTableEntry) semanticStack.peek();
                int paramcount20 = paramCount.pop();
                id20.setNumParams(paramcount20);
                break;
            case 21:
                // TODO: Look at this again lol, check stack order?
                // Pop id
                Token id21 = (Token) semanticStack.pop();
                TokenType type21 = id21.getType();

                int paramCount21 = paramCount.pop();
                // Giant error numbers for debugging purposes
                int ub21 = -99999999;
                int lb21 = -99999999;
                if (isArray) {
                    Token ubt21 = (Token) semanticStack.pop();
                    Token lbt21 = (Token) semanticStack.pop();
                    ub21 = Integer.parseInt(ubt21.getValue());
                    lb21 = Integer.parseInt(lbt21.getValue());
                }
                // Need this to add new elt to id.paraminfo
                LinkedList<ParamInfo> paramInfoL21 = new LinkedList();
                Token id21_1 = (Token) semanticStack.peek();
                // For each id
                while(id21_1.getType() == TokenType.IDENTIFIER) {
                    // Clear paraminfo
                    ParamInfo paramInfo21 = new ParamInfo();
                    // if ARRAY
                    if (isArray) {
                        ArrayEntry ae21 = new ArrayEntry(id21_1.getValue(),type21);
                        ae21.setIsParam(true);
                        ae21.setUpperBound(ub21);
                        ae21.setLowerBound(lb21);
                        paramInfo21.setUpperBound(ub21);
                        paramInfo21.setLowerBound(lb21);
                        paramInfo21.setIsArray(true);
                        if (global) {
                            globalTable.insert(ae21);
                        } else {
                            localTable.insert(ae21);
                        }
                    } else {
                        VariableEntry ve21 = new VariableEntry(id21_1.getValue(), type21);
                        ve21.setIsParam(true);
                        paramInfo21.setIsArray(false);
                        if (global) {
                            globalTable.insert(ve21);
                        } else {
                            localTable.insert(ve21);
                        }
                    }
                    SymbolTableEntry ste21 = this.lookup(id21_1.getValue());
                    ste21.setAddress(localMemory);
                    localMemory++;
                    ste21.setType(type21);
                    paramInfo21.setType(type21);
                    paramCount21++;
                    //add elt to id.paraminfo
                    paramInfoL21.add(paramInfo21);
                    // Pop the id off the stack, get new id
                    // unless there are no more, then break while loop
                    semanticStack.pop();
                    if (semanticStack.peek() instanceof Token) {
                        id21_1 = (Token) semanticStack.peek();
                    } else {
                        break;
                    }
                }
                // Array/Simple = simple
                isArray = false;
                break;
            case 22:
                // Pop etype
                Etype etyp22 = (Etype) semanticStack.pop();
                // if its not relational, throw error
                if (etyp22 != Etype.RELATIONAL) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                // need to access etrue, so pop efalse off
                // peek to get etrue, push efalse back on to not disturb stack
                LinkedList efalse22 = (LinkedList) semanticStack.pop();
                LinkedList etrue22 = (LinkedList) semanticStack.peek();
                semanticStack.push(efalse22);
                // backpatch with etrue
                backPatch(etrue22, quadruples.getNextQuad());
                break;
            case 24:
                // push BEGINLOOP
                int beginloop24 = quadruples.getNextQuad();
                semanticStack.push(beginloop24);
                break;
            case 25:
                // Pop etype
                Etype etyp25 = (Etype) semanticStack.pop();
                // If its not relational, throw error
                if (etyp25 != Etype.RELATIONAL) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                // need to access etrue, so pop efalse off
                // peek to get etrue, push efalse back on to not disturb stack
                LinkedList efalse25 = (LinkedList) semanticStack.pop();
                LinkedList etrue25 = (LinkedList) semanticStack.peek();
                semanticStack.push(efalse25);
                // backpatch with etrue
                backPatch(etrue25, quadruples.getNextQuad());
                break;
            case 26:
                // Pop efalse, etrue, and beginloop
                LinkedList efalse26 = (LinkedList) semanticStack.pop();
                LinkedList etrue26 = (LinkedList) semanticStack.pop();
                int beginloop26 = (int) semanticStack.pop();
                // generate with beginloop
                generate("goto", Integer.toString(beginloop26));
                // backpatch with efalse
                backPatch(efalse26, quadruples.getNextQuad());
                break;
            case 27:
                // Pop skip_else, peek efalse
                LinkedList skipElse27 = makeList(quadruples.getNextQuad());
                LinkedList efalse27 = (LinkedList) semanticStack.peek();
                // backpatch with efalse and nextquad
                backPatch(efalse27, quadruples.getNextQuad());
                // push skip_else back on to stack to not disturb the stack
                semanticStack.push(skipElse27);
                // generate goto _
                generate("goto", "_");
                break;
            case 28:
                // Pop skip_else, efalse, and etrue
                LinkedList skipElse28 = (LinkedList) semanticStack.pop();
                LinkedList efalse28 = (LinkedList) semanticStack.pop();
                LinkedList etrue28 = (LinkedList) semanticStack.pop();
                // backpatch skip_else and nextquad
                backPatch(skipElse28, quadruples.getNextQuad());
                break;
            case 29:
                // Pop efalse, etrue
                LinkedList efalse29 = (LinkedList) semanticStack.pop();
                LinkedList etrue29 = (LinkedList) semanticStack.pop();
                // backpatch with efalse and nextquad
                backPatch(efalse29, quadruples.getNextQuad());
                break;
            case 30:
                //lookup id in symbol table
                SymbolTableEntry entry30 = this.lookup(token.getValue());
                //if not found, throw error
                if (entry30 == null) {
                    throw SemanticError.UndeclaredVariable(token.getValue(), tokenizer.getLineNumber());
                } else {
                    // push id
                    semanticStack.push(entry30);
                }
                // push ETYPE(ARITHMETIC)
                semanticStack.push(Etype.ARITHMETIC);
                break;
            case 31:
                // pop ETYPE
                Etype etypeTk = (Etype) semanticStack.pop();
                // if ETYPE is not ARITHMETIC, throw error
                if (etypeTk != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                //System.out.println("TOP OF STACK: " + semanticStack.peek());
                // Pop id1, offset, id2
                SymbolTableEntry id22 = (SymbolTableEntry) semanticStack.pop();
                SymbolTableEntry offset31 = (SymbolTableEntry) semanticStack.pop();
//                if (semanticStack.peek() instanceof Etype) {
//                    semanticStack.pop();
//                }
                SymbolTableEntry id11 = (SymbolTableEntry) semanticStack.pop();
                // if TYPECHECK(id1, id2) = 3, throw an error
                if (typeCheck(id11, id22) == 3) {
                    throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
                    // if TYPECHECK(id1, id2) = 2
                } else if (typeCheck(id11, id22) == 2) {
                    // create new variable entry "temp" with tokentype real
                    VariableEntry temp31 = create("TEMP", TokenType.REAL);
                    // generate code with that temp
                    generate("ltof", id22, temp31);
                    // check if offset is null. if it is, generate a move
                    if (offset31 == nullEntry) {
                        generate("move", temp31, id11);
                        // otherwise generate a stor
                    } else {
                        generate("stor", temp31, offset31, id11);
                    }
                    // if TYPECHECK(id1, id2) is not 3 or 2
                } else {
                    // check if offset is null. if it is, generate a move
                    if (offset31 == null) {
                        generate("move", id22, id11);
                        // else generate stor
                    } else {
                        generate("stor", id22, offset31, id11);
                    }
                }
                break;
            case 32:
                // pop ETYPE
                etypeTk = (Etype) semanticStack.pop();
                if (etypeTk != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                // if next thing is not an array, throw error
                if (!(((SymbolTableEntry) semanticStack.peek()).isArray())) {
                    throw SemanticError.NotArray(tokenizer.getLineNumber());
                }
                break;
            case 33:
                // Pop ETYPE
                etypeTk = (Etype) semanticStack.pop();
                // If ETYPE is not arithmetic, throw error
                if (etypeTk != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                // Pop id
                SymbolTableEntry id = (SymbolTableEntry) semanticStack.pop();
                // If id's type is not INTEGER, throw an error
                if (id.getType() != TokenType.INTEGER) {
                    //TODO check if correct error
                    throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
                }
                // Get the id on bottom of the stack
                // TODO: Investigate - this is throwing errors
                ArrayEntry ae33 = (ArrayEntry) semanticStack.get(0);
                // Create a variable "temp" with type INTEGER
                VariableEntry temp33 = create("TEMP", TokenType.INTEGER);
                // Generate with the above
                generate("sub", id, ae33.getLowerBound(), temp33);
                // Push "temp"
                semanticStack.push(temp33);
                break;
            case 34:
                //pop etype
                Etype etyp34 = (Etype) semanticStack.pop();
                // if id on stack is a function, call action 52
                if (((SymbolTableEntry) semanticStack.peek()).isFunction()) {
                    // TODO: Check if pushing the etype back on is necessary
                    //semanticStack.push(etyp34);
                    execute(52, token);
                    //else push null offset
                } else {
                    semanticStack.push(nullEntry);
                }
                break;
            case 35:
                paramCount.push(0);
                //Need to access id, so pop etype, peek, and push etype back on
                Etype etyp35 = (Etype) semanticStack.pop();
                // TODO: Is it a STE? or PE?
                SymbolTableEntry id35 = (SymbolTableEntry) semanticStack.peek();
                semanticStack.push(etyp35);
                nextParam.push(id35.getParamInfo());
                break;
            case 36:
                //pop etype
                Etype etyp36 = (Etype) semanticStack.pop();
                // pop id
                ProcedureEntry id36 = (ProcedureEntry) semanticStack.pop();
                if (id36.getNumberOfParameters() != 0) {
                    throw SemanticError.WrongNumParameters(id36.getName(),tokenizer.getLineNumber());
                }
                generate("call", id36, Integer.toString(0));
                break;
            case 37:
                // Pop Etype
                Etype etyp37 = (Etype) semanticStack.pop();
                if (etyp37 != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                SymbolTableEntry id37 = (SymbolTableEntry) semanticStack.peek();
                if (!(id37.isVariable() || id37.isConstant() || id37.isFunctionResult() || id37.isArray())) {
                    throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
                }
                // Increment paramcount.top
                int paramtop = paramCount.pop();
                paramtop++;
                paramCount.push(paramtop);
                SymbolTableEntry procOrFun = (SymbolTableEntry) semanticStack.get(0);
                if (procOrFun.getName().toUpperCase() != "READ" || procOrFun.getName().toString() != "WRITE") {
                    if (paramCount.peek() > procOrFun.getNumParams()) {
                        throw SemanticError.WrongNumParameters(procOrFun.getName(), tokenizer.getLineNumber());
                    }
                    if (id37.getType() != nextParam.peek().getType()) {
                        throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
                    }
                    if (nextParam.peek().getIsArray()) {
                        if (((ArrayEntry)id37).getLowerBound() != nextParam.peek().getLowerBound() ||
                                ((ArrayEntry)id37).getUpperBound() != nextParam.peek().getUpperBound()) {
                            throw SemanticError.BadArrayBounds(tokenizer.getLineNumber());
                        }
                    }
                    // TODO: Increment nextParam? How
                    nextParam.pop();
                }
                break;
            case 38:
                // pop etype
                Etype etyp38 = (Etype) semanticStack.pop();
                // if its not arithmetic, throw error
                if (etyp38 != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                //push token
                semanticStack.push(token);
                break;
            case 39:
                //pop etype
                Etype etyp39 = (Etype) semanticStack.pop();
                // if its not arithmetic, throw error
                if (etyp39 != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                SymbolTableEntry id39_2 = (SymbolTableEntry) semanticStack.pop();
                Token opry39 = (Token) semanticStack.pop();
                SymbolTableEntry id39_1 = (SymbolTableEntry) semanticStack.pop();
                if (typeCheck(id39_1, id39_2) == 2) {
                    VariableEntry temp39 = create("TEMP1", TokenType.REAL);
                    generate("ltof", id39_2, temp39);
                    generate(opry39.getStarString(), id39_1, temp39, "_"); //*** replaced by blt, ble, bgt, etc.
                } else if (typeCheck(id39_1, id39_2) == 3) {
                    VariableEntry temp39 = create("TEMP1", TokenType.REAL);
                    generate("ltof", id39_1, temp39);
                    generate(opry39.getStarString(), temp39, id39_2, "_");
                } else {
                    generate(opry39.getStarString(), id39_1, id39_2, "_");
                }
                generate("goto", "_");
                LinkedList<Integer> e_true = makeList(quadruples.getNextQuad() - 2);
                LinkedList<Integer> e_false = makeList(quadruples.getNextQuad() - 1);
                semanticStack.push(e_true);
                semanticStack.push(e_false);
                semanticStack.push(Etype.RELATIONAL);
                break;
            case 40:
                //push sign
                semanticStack.push(token);
                break;
            case 41:
                // pop etype
                Etype etyp41 = (Etype) semanticStack.pop();
                if (etyp41 != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                // Pop sign
                Token tok41 = (Token) semanticStack.pop();
                if (tok41.getType() == TokenType.UNARYMINUS) {
                    VariableEntry temp41 = create("TEMP", TokenType.UNARYMINUS);
                    // Pop id
                    SymbolTableEntry id41 = (SymbolTableEntry) semanticStack.pop();
                    if (id41.getType() == TokenType.INTEGER) {
                        generate("uminus", id41, temp41);
                    } else {
                        generate("fuminus", id41, temp41);
                    }
                    // Push temp
                    semanticStack.push(temp41);
                } else {
                    // Push etype(ARITHMETIC)
                    semanticStack.push(Etype.ARITHMETIC);
                }
                break;
            case 42:
                // check ETYPE = ARITHMETIC
                // Pop the Etype off the stack
                Etype typ42 = (Etype) semanticStack.pop();
                if (token.getOpType() == Token.OperatorType.OR) {
                    if (typ42 != Etype.RELATIONAL) {
                        throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                    }
                    LinkedList efalse42 = (LinkedList) semanticStack.peek();
                    backPatch(efalse42, quadruples.getNextQuad());
                } else {
                    if (typ42 != Etype.ARITHMETIC) {
                        throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                    }
                }
                semanticStack.push(token);
                break;
            case 43:
                // Pop ETYPE
                Etype etypeT = (Etype) semanticStack.pop();
                if (etypeT == Etype.RELATIONAL) {
                    if (token.getOpType() == Token.OperatorType.OR) {
                        LinkedList e2false = (LinkedList) semanticStack.pop();
                        LinkedList e2true = (LinkedList) semanticStack.pop();
                        Token opry43 = (Token) semanticStack.pop();
                        LinkedList e1false = (LinkedList) semanticStack.pop();
                        LinkedList e1true = (LinkedList) semanticStack.pop();
                        LinkedList etrue = merge(e1true, e2true);
                        LinkedList efalse = e2false;
                        semanticStack.push(etrue);
                        semanticStack.push(efalse);
                        semanticStack.push(Etype.RELATIONAL);
                    }
                } else {
                    // If ETYPE is not ARITHMETIC, throw error
                    if (etypeT != Etype.ARITHMETIC) {
                        throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                    }
                    // Pop id1, operator, id2
                    SymbolTableEntry idTwo = (SymbolTableEntry) semanticStack.pop();
                    Token opry = (Token) semanticStack.pop(); //pop operator?
                    SymbolTableEntry idOne = (SymbolTableEntry) semanticStack.pop();
                    // If typecheck returns 0
                    if (typeCheck(idOne, idTwo) == 0) {
                        // create variable entry, "temp" with type INTEGER
                        VariableEntry temp43 = create("TEMP", TokenType.INTEGER);
                        // generate
                        generate(opry.getStarString(), idOne, idTwo, temp43);
                        // push result variable
                        semanticStack.push(temp43);
                        // if typecheck returns 1
                    } else if (typeCheck(idOne, idTwo) == 1) {
                        // create variable entry "temp" with type REAL
                        VariableEntry temp43 = create("TEMP", TokenType.REAL);
                        // generate
                        generate(("f" + opry.getStarString()), idOne, idTwo, temp43);
                        // push result variable
                        semanticStack.push(temp43);
                        // if typecheck returns 2
                    } else if (typeCheck(idOne, idTwo) == 2) {
                        // create two variable entrys "temp1" and "temp2" and generate
                        VariableEntry temp431 = create("TEMP1", TokenType.REAL);
                        generate("ltof", idTwo, temp431);
                        VariableEntry temp432 = create("TEMP2", TokenType.REAL);
                        generate(("f" + opry.getStarString()), idOne, temp431, temp432);
                        // TODO: Check if this is correct
                        // push result variable..?
                        semanticStack.push(temp432);
                        // if typecheck returns 3
                    } else if (typeCheck(idOne, idTwo) == 3) {
                        // create two variable entrys "temp1" and "temp2" and generate
                        VariableEntry temp431 = create("TEMP1", TokenType.REAL);
                        generate("ltof", idOne, temp431);
                        VariableEntry temp432 = create("TEMP2", TokenType.REAL);
                        generate(("f" + opry.getStarString()), temp431, idTwo, temp432);
                        // TODO: Check if this is correct
                        // push result variable..?
                        semanticStack.push(temp432);
                    } else {
                    }
                    // push ETYPE(ARITHMETIC)
                    semanticStack.push(Etype.ARITHMETIC);
                }
                break;
            case 44:
                // Pop ETYPE
                Etype etypeTok = (Etype) semanticStack.pop();
                // If its relational
                if (etypeTok == Etype.RELATIONAL) {
                    // if operator = AND
                    if (token.getOpType() == Token.OperatorType.AND) {
                        // need to access etrue, so pop efalse, peek etrue, push efalse back on
                        LinkedList efalse44 = (LinkedList) semanticStack.pop();
                        LinkedList etrue44 = (LinkedList) semanticStack.peek();
                        semanticStack.push(efalse44);
                        // backpatch with etrue and nextquad
                        backPatch(etrue44, quadruples.getNextQuad());
                    }
                }
                // Push operator
                semanticStack.push(token);
                break;
            case 45:
                // Pop ETYPE
                etypeTok = (Etype) semanticStack.pop();
                if (token.getOpType() == Token.OperatorType.AND) {
                    if (etypeTok != Etype.RELATIONAL) {
                        throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                    }
                    LinkedList e2false = (LinkedList) semanticStack.pop();
                    LinkedList e2true = (LinkedList) semanticStack.pop();
                    Token opry43 = (Token) semanticStack.pop();
                    LinkedList e1false = (LinkedList) semanticStack.pop();
                    LinkedList e1true = (LinkedList) semanticStack.pop();
                    LinkedList etrue = e2true;
                    LinkedList efalse = merge(e1false, e2false);
                    semanticStack.push(etrue);
                    semanticStack.push(efalse);
                    semanticStack.push(Etype.RELATIONAL);
                } else {
                    // If ETYPE is not ARITHMETIC, throw error
                    if (etypeTok != Etype.ARITHMETIC) {
                        throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                    }
                    // Pop id1, operator, id2
                    SymbolTableEntry idTwo = (SymbolTableEntry) semanticStack.pop();
                    Token opry = (Token) semanticStack.pop(); //pop operator?
                    SymbolTableEntry idOne = (SymbolTableEntry) semanticStack.pop();
                    if (typeCheck(idOne, idTwo) != 0 && opry.getOpType() == Token.OperatorType.MOD) {
                        throw SemanticError.BadMODoperands(tokenizer.getLineNumber());
                    }
                    if (typeCheck(idOne, idTwo) == 0) {
                        //System.out.println(token);
                        //System.out.println(token.getOpType());
                        if (token.getOpType() == Token.OperatorType.MOD) {
                            VariableEntry temp451 = create("TEMP1", TokenType.INTEGER);
                            generate("move", idOne, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.INTEGER);
                            generate("move", temp451, temp452);
                            generate("sub", temp452, idTwo, temp451);
                            generate("bge", temp451, idTwo, Integer.toString(quadruples.getNextQuad() - 2));
                        } else if (opry.getOpType() == Token.OperatorType.DIVIDE) {
                            // Create, generate, and push result variable
                            VariableEntry temp451 = create("TEMP1", TokenType.REAL);
                            generate("ltof", idOne, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.REAL);
                            generate("ltof", idTwo, temp452);
                            VariableEntry temp453 = create("TEMP3", TokenType.REAL);
                            generate("fdiv", temp451, temp452, temp453);
                            semanticStack.push(temp453);
                        } else {
                            // Create, generate, and push result variable
                            VariableEntry temp45 = create("TEMP", TokenType.INTEGER);
                            generate(opry.getStarString(), idOne, idTwo, temp45);
                            semanticStack.push(temp45);
                        }
                    } else if (typeCheck(idOne, idTwo) == 1) {
                        // Create, generate, and push result variable
                        if (opry.getOpType() == Token.OperatorType.INTEGERDIVIDE) {
                            VariableEntry temp451 = create("TEMP1", TokenType.INTEGER);
                            generate("ltof", idOne, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.INTEGER);
                            generate("ltof", idTwo, temp452);
                            VariableEntry temp453 = create("TEMP3", TokenType.INTEGER);
                            generate("fdiv", temp451, temp452, temp453);
                            semanticStack.push(temp453);
                        } else {
                            // Create, generate, and push result variable
                            VariableEntry temp45 = create("TEMP", TokenType.REAL);
                            generate(("f" + opry.getStarString()), idOne, idTwo, temp45);
                            semanticStack.push(temp45);
                        }
                    } else if (typeCheck(idOne, idTwo) == 2) {
                        // Create, generate, and push result variable
                        if (opry.getOpType() == Token.OperatorType.INTEGERDIVIDE) {
                            VariableEntry temp451 = create("TEMP1", TokenType.INTEGER);
                            generate("ftol", idOne, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.INTEGER);
                            generate("div", temp451, idOne, temp452);
                            semanticStack.push(temp452);
                        } else {
                            // Create, generate, and push result variable
                            VariableEntry temp451 = create("TEMP1", TokenType.REAL);
                            generate("ltof", idTwo, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.REAL);
                            generate(("f" + opry.getStarString()), idOne, temp451, temp452);
                            semanticStack.push(temp452);
                        }
                    } else if (typeCheck(idOne, idTwo) == 3) {
                        // Create, generate, and push result variable
                        if (opry.getOpType() == Token.OperatorType.INTEGERDIVIDE) {
                            VariableEntry temp451 = create("TEMP1", TokenType.INTEGER);
                            generate("ftol", idTwo, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.INTEGER);
                            generate("div", idOne, temp451, temp452);
                            semanticStack.push(temp452);
                        } else {
                            // Create, generate, and push result variable
                            VariableEntry temp451 = create("TEMP1", TokenType.REAL);
                            generate("ltof", idOne, temp451);
                            VariableEntry temp452 = create("TEMP2", TokenType.REAL);
                            generate(("f" + opry.getStarString()), temp451, idTwo, temp452);
                            semanticStack.push(temp452);
                        }
                    } else {
                    }
                    // Push ETYPE(ARITHMETIC)
                    semanticStack.push(Etype.ARITHMETIC);
                }
                break;
            case 46:
                // TODO: Confirm this is right
                // TODO: 46 is messing things up. Not quite sure where...
                // First assign a variable to be nothing for now
                SymbolTableEntry entry46 = null;
                // If token is an identifier
                if (token.getType() == TokenType.IDENTIFIER) {
                    // We'll give the above variable the lookup of token
                    entry46 = this.lookup(token.getValue());
                    // If that value returns null, we didn't find token - throw and error
                    if (entry46 == null) {
                        throw SemanticError.UndeclaredVariable(token.getValue(), tokenizer.getLineNumber());
                    } else {
                        //otherwise, we want to push that value on.
                        semanticStack.push(entry46);
                    }
                    // if token is a constant
                }  if (token.getType() == TokenType.INTCONSTANT
                        || token.getType() == TokenType.REALCONSTANT) {
                    // Do the same thing - give the variable the value of lookup
                    entry46 = constantTable.lookup(token.getValue());
                    // if that value is null, we didn't find token
                    if (entry46 == nullEntry) {
                        //We make a new ConstantEntry respectively to the type of constant token is
                        //and insert + push
                        if (token.getType() == TokenType.INTCONSTANT) {
                            ConstantEntry constantentry46 = new ConstantEntry(token.getValue(), TokenType.INTEGER);
                            constantTable.insert(constantentry46);
                            semanticStack.push(constantentry46);
                        } else {
                            ConstantEntry constantentry46 = new ConstantEntry(token.getValue(), TokenType.REAL);
                            constantTable.insert(constantentry46);
                            semanticStack.push(constantentry46);
                        }
                    } else
                    semanticStack.push(entry46);
                }
                // Push ETYPE(ARITHMETIC)
                semanticStack.push(Etype.ARITHMETIC);
                break;
            case 47:
                // "parts of action...47, which deals with NOT"
                Etype etype47 = (Etype) semanticStack.pop();
                if (etype47 != Etype.RELATIONAL) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                LinkedList efalse47 = (LinkedList) semanticStack.pop();
                LinkedList etrue47 = (LinkedList) semanticStack.pop();
                LinkedList tempy47 = new LinkedList();
                tempy47 = efalse47;
                efalse47 = etrue47;
                etrue47 = tempy47;
                semanticStack.push(etrue47);
                semanticStack.push(efalse47);
                semanticStack.push(Etype.RELATIONAL);
                break;
            case 48:
                // Pop offset
                SymbolTableEntry offset = (SymbolTableEntry) semanticStack.pop();
                // If offset is not null
                if (offset != nullEntry) {
                    // Check if it's a function. If it is, call action 52.
                    if (offset.isFunction()) {
                        execute(52, token);
                        // else
                    } else {
                        // Pop offset, ETYPE and id
                        //semanticStack.pop();
                        Etype typy = (Etype) semanticStack.pop();
                        SymbolTableEntry id48 = (SymbolTableEntry) semanticStack.pop();
                        // Create a variable entry with id's type
                        VariableEntry temp48 = create("TEMP", id48.getType());
                        // Gen
                        generate("load", id48, offset, temp48);
                        // Push the variable entry
                        semanticStack.push(temp48);
                    }
                }
                // Push ETYPE(ARITHMETIC)
                semanticStack.push(Etype.ARITHMETIC);
                break;
            case 49:
                // need to access as deep as id, pop and push etype back on
                // since we're not really popping anything here
                Etype etyp49 = (Etype) semanticStack.pop();
                SymbolTableEntry id49 = (SymbolTableEntry) semanticStack.peek();
                semanticStack.push(etyp49);
                if (etyp49 != Etype.ARITHMETIC) {
                    throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
                }
                if (!(id49).isFunction()) {
                    // TODO: Check if correct error thrown
                    throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
                }
                // push new elt on paramcount, paramcount top = 0
                paramCount.push(0);
                nextParam.push(id49.getParamInfo());
                break;
            case 50:
                SymbolTableEntry id50 = (SymbolTableEntry) semanticStack.peek();
                Stack<SymbolTableEntry> reverseStack = new Stack<SymbolTableEntry>();
                // Fill a stack to reverse order of semanticStack since we need to go
                // from bottom to top - only want ids
                while (true) {
                    reverseStack.push(id50);
                    semanticStack.pop();
                    if(semanticStack.peek() instanceof SymbolTableEntry) {
                        id50 = (SymbolTableEntry) semanticStack.peek();
                    } else {
                        break;
                    }
                }
                // For each id on stack: pop id, generate(param id), local mem +1
                while (!(reverseStack.isEmpty())) {
                    SymbolTableEntry id50_1 = reverseStack.pop();
                    generate("param", id50_1);
                    localMemory++;
                }
                //pop paramcount.top
                int paramcount50 = paramCount.pop();
                // pop etype, id
                Etype etyp50 = (Etype) semanticStack.pop();
                SymbolTableEntry id50_2 = (SymbolTableEntry) semanticStack.pop();
                if (paramcount50 > id50_2.getNumParams()) {
                    throw SemanticError.WrongNumParameters(id50_2.getName(),tokenizer.getLineNumber());
                }
                // Pop nextparam.top
                nextParam.pop();
                generate("call", id50_2, Integer.toString(paramcount50));
                VariableEntry temp50 = create("TEMP", id50_2.getType());
                generate("move", ((FunctionEntry) id50_2).getResult(), temp50);
                semanticStack.push(temp50);
                semanticStack.push(Etype.ARITHMETIC);
                break;
            case 51:
                //TODO: Check pop order of stack (etype + id)
                // Pop Etype + id
                Etype etyp51 = (Etype) semanticStack.pop();
                SymbolTableEntry id51 = (SymbolTableEntry) semanticStack.pop();
                if (id51.getName().toUpperCase() == "READ") {
                    semanticAction51read(token);
                } else if (id51.getName().toUpperCase() == "WRITE") {
                    semanticAction51write(token);
                } else {
                    if (paramCount.peek() != id51.getNumParams()) {
                        throw SemanticError.WrongNumParameters(id51.getName(),tokenizer.getLineNumber());
                    }
                    // So we can go bottom to top
                    Stack<SymbolTableEntry> reverseStack51 = new Stack();
                    SymbolTableEntry id51_1 = (SymbolTableEntry) semanticStack.peek();
                    while (true) {
                        reverseStack51.push(id51_1);
                        semanticStack.pop();
                        if (semanticStack.peek() instanceof SymbolTableEntry) {
                            id51_1 = (SymbolTableEntry) semanticStack.peek();
                        } else {
                            break;
                        }
                    }
                    // For each id on stack (in reverse order)
                    while (!reverseStack51.isEmpty()) {
                        // Pop id
                        SymbolTableEntry id51_2 = (SymbolTableEntry) semanticStack.pop();
                        generate("param", id51_2);
                        localMemory++;
                    }
                    generate("call", id51, paramCount.pop().toString());
                }
                break;
            case 52:
                // Pop etype, id
                Etype etyp52 = (Etype) semanticStack.pop();
                SymbolTableEntry id52 = (SymbolTableEntry) semanticStack.pop();
                if (!(id52.isFunction())) {
                    // TODO: Check if correct error thrown
                    throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
                }
                if (id52.getNumParams() > 0) {
                    throw SemanticError.WrongNumParameters(id52.getName(),tokenizer.getLineNumber());
                }
                generate("call", id52, Integer.toString(0));
                VariableEntry temp52 = create("TEMP", id52.getType());
                generate("move", ((FunctionEntry) id52).getResult(), temp52);
                semanticStack.push(temp52);
                semanticStack.push(Etype.ARITHMETIC);
                break;
            case 53:
                Etype etyp53 = (Etype) semanticStack.pop();
                SymbolTableEntry id53 = (SymbolTableEntry) semanticStack.pop();
                if (id53.isFunction()) {
                    //if id != currentfunction, error
                    if (id53 != currentFunction) {
                        throw SemanticError.IllegalProcedureCall(id53.getName(), tokenizer.getLineNumber());
                    } else {
                        // push id.result (ie. $$function-name)
                        semanticStack.push(((FunctionEntry) id53).getResult());
                        // push etype(arithmetic)
                        semanticStack.push(Etype.ARITHMETIC);
                    }
                } else {
                    // Since we popped two things earlier, we need to push them
                    // back on if id53 is not a function.
                    semanticStack.push(id53);
                    semanticStack.push(etyp53);
                }
                break;
            case 54:
                // TODO: Check if right error thrown
                // if top of stack is not a procedure, throw an error
                if (!(((SymbolTableEntry) semanticStack.peek()).isProcedure())) {
                    throw SemanticError.IllegalProcedureCall(((SymbolTableEntry) semanticStack.peek()).getName(),
                            tokenizer.getLineNumber());
                }
                break;
            case 55:
                // backpatch(global_store,global_mem)
                backPatch(globalStore, globalMemory);
                // gen(free global_mem)
                generate("free", globalMemory);
                // gen (procend)
                generate("PROCEND");
                break;
            case 56:
                // gen (procbegin main)
                generate("PROCBEGIN", "main");
                // global_store = nextquad
                globalStore = quadruples.getNextQuad();
                // gen (alloc, _)
                generate("alloc", "_");
                break;
            case 57:
                ConstantEntry ce57 = (ConstantEntry) constantTable.lookup(token.getValue());
                if (ce57 == null) {
                    ConstantEntry newEntry57 = new ConstantEntry(token.getValue(), TokenType.INTEGER);
//                    ConstantEntry newEntry57 = token.getEntry();
//                    newEntry57.setType(TokenType.INTEGER);
                    constantTable.insert(newEntry57);
                }
                break;
            case 58:
                ConstantEntry ce58 = (ConstantEntry) constantTable.lookup(token.getValue());
                if (ce58 == null) {
                    ConstantEntry newEntry58 = new ConstantEntry(token.getValue(), TokenType.INTEGER);
//                    ConstantEntry newEntry58 = token.getEntry();
//                    newEntry58.setType(TokenType.REAL);
                    constantTable.insert(newEntry58);
                }
                break;
            default:
                // TODO Eventually (i.e. final project) this should throw an exception.
                debug("Action " + actionNumber + " not yet implemented.");
                //throw SemanticError.????
        }
        quadruples.print();
    }

    public void semanticAction51read(Token token) throws SemanticError, SymbolTableError  {
        // TODO: Check pop/stack order (etype, id)
        Etype etyp51r = (Etype) semanticStack.pop();
        SymbolTableEntry id51r = (SymbolTableEntry) semanticStack.peek();
        Stack<SymbolTableEntry> reverseStack51r = new Stack();
        while (true) {
            reverseStack51r.push(id51r);
            semanticStack.pop();
            if (semanticStack.peek() instanceof SymbolTableEntry) {
                id51r = (SymbolTableEntry) semanticStack.peek();
            } else {
                break;
            }
        }
        while (!reverseStack51r.isEmpty()) {
            SymbolTableEntry ste51r = reverseStack51r.pop();
            if (ste51r.getType() == TokenType.REAL) {
                generate("finp", ste51r);
            } else {
                generate("inp", ste51r);
            }
        }
        paramCount.pop();
    }

    public void semanticAction51write(Token token) throws SemanticError, SymbolTableError {
        // TODO: Check pop/stack order (etype, id)
        Etype etyp51w = (Etype) semanticStack.pop();
        SymbolTableEntry id51w = (SymbolTableEntry) semanticStack.peek();
        Stack<SymbolTableEntry> reverseStack51w = new Stack();
        while (true) {
            reverseStack51w.push(id51w);
            semanticStack.pop();
            if (semanticStack.peek() instanceof SymbolTableEntry) {
                id51w = (SymbolTableEntry) semanticStack.peek();
            } else {
                break;
            }
        }
        while (!reverseStack51w.isEmpty()) {
            SymbolTableEntry ste51w = reverseStack51w.pop();
            generate("print", ste51w.getName() + " = ");
            if (ste51w.getType() == TokenType.REAL) {
                generate("foutp", ste51w);
            } else {
                generate("outp", ste51w);
            }
            generate("newl");
        }
        paramCount.pop();
    }

    public SymbolTableEntry lookup(String name) {
        if (global) {
            return globalTable.lookup(name);
        } else {
            return localTable.lookup(name);
        }
        //return globalTable.lookup(name);
    }

    public ConstantEntry lookupConstant(Token token) {
        return (ConstantEntry) constantTable.lookup(token.getValue());
    }

    private void installBuiltins(SymbolTable table) throws SymbolTableError {
        SymbolTable.installBuiltins(table);
    }

    public void dump_semanticStack() {
        //For each GrammarSymbol gs in parseStack
        for (Object obj : semanticStack) {
            //Print out gs
            System.out.print(obj + " | ");
        }
        //new line for each new stack printed
        System.out.println();
    }

    public void generate(String tviCode) throws SemanticError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = quad[2] = quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, String operand1) throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = operand1;
        quad[2] = quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1) throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = generateHelp(operand2);
        quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1, String operand2)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = operand2;
        quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, String operand1, SymbolTableEntry operand2)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = operand1;
        quad[2] = generateHelp(operand2);
        quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, String operand1, String operand2) throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = operand1;
        quad[2] = operand2;
        quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, int operand1) throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = Integer.toString(operand1);
        quad[2] = quad[3] = null;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2,
                         SymbolTableEntry operand3) throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = generateHelp(operand2);
        quad[3] = generateHelp(operand3);
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, String operand1, String operand2, SymbolTableEntry operand3)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = operand1;
        quad[2] = operand2;
        quad[3] = generateHelp(operand3);
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, String operand1, SymbolTableEntry operand2, SymbolTableEntry operand3)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = operand1;
        quad[2] = generateHelp(operand2);
        quad[3] = generateHelp(operand3);
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1, String operand2, SymbolTableEntry operand3)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = operand2;
        quad[3] = generateHelp(operand3);
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2, String operand3)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = generateHelp(operand2);
        quad[3] = operand3;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, String operand1, SymbolTableEntry operand2, String operand3)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = operand1;
        quad[2] = generateHelp(operand2);
        quad[3] = operand3;
        quadruples.addQuad(quad);
    }

    public void generate(String tviCode, SymbolTableEntry operand1, int operand2, SymbolTableEntry operand3)
            throws SemanticError, SymbolTableError {
        String[] quad = new String[4];
        quad[0] = tviCode;
        quad[1] = generateHelp(operand1);
        quad[2] = Integer.toString(operand2);
        quad[3] = generateHelp(operand3);
        quadruples.addQuad(quad);
    }

    public String generateHelp(SymbolTableEntry id) throws SymbolTableError, SemanticError {
        VariableEntry ve;
        int idAddress;
        String quadStr;
        if (id.isConstant()) {
            ve = create(id.getName(), id.getType());
            generate("move " + id.getName(), ve);
            idAddress = Math.abs(ve.getAddress());
        } else {
            idAddress = Math.abs(id.getAddress());
            //idAddress = Math.abs()
        }
        if (isParam) {
            quadStr = "^%";
        } else if (global) {
            quadStr = "_";
        } else {
            quadStr = "%";
        }
        quadStr = quadStr + Integer.toString(idAddress);
        return quadStr;
    }

    public void backPatch(int p, int i) throws SemanticError, SymbolTableError {
        quadruples.setField(p, 1, Integer.toString(i));
    }

    public void backPatch(LinkedList p, int i) throws SemanticError, SymbolTableError {
        for (Object val : p) {
            int ip = (int) val;
            String[] currentQuad = quadruples.getQuad(ip);
            int quadLength = currentQuad.length;
            for (int x = 1; x < quadLength; x++) {
                quadruples.setField(ip, x, Integer.toString(i));
            }
        }
    }

    public VariableEntry create(String name, TokenType type) throws SymbolTableError {
        VariableEntry entry = new VariableEntry(name, -globalMemory, type);
        globalTable.insert(entry);
        globalMemory = globalMemory + 1;
        return entry;
    }

    public int typeCheck(Token id1, Token id2) {
        TokenType type1 = id1.getType();
        TokenType type2 = id2.getType();
        TokenType really = TokenType.REAL;
        TokenType inty = TokenType.INTEGER;
        if (type1 == inty && type2 == inty) {
            return 0;
        } else if (type1 == really && type2 == really) {
            return 1;
        } else if (type1 == really && type2 == inty) {
            return 2;
        } else {
            return 3;
        }
    }

    public int typeCheck(SymbolTableEntry id1, SymbolTableEntry id2) {
        TokenType type1 = id1.getType();
        TokenType type2 = id2.getType();
        TokenType really = TokenType.REAL;
        TokenType inty = TokenType.INTEGER;
        if (type1 == inty && type2 == inty) {
            return 0;
        } else if (type1 == really && type2 == really) {
            return 1;
        } else if (type1 == really && type2 == inty) {
            return 2;
        } else {
            return 3;
        }
    }

    //  makelist, which creates a new list of integers that represent the targets of
    //                 quadruples on E.true and E.false lists.
    public LinkedList makeList(int i) {
        LinkedList listy = new LinkedList();
        listy.add(i);
        return listy;
    }

//    MERGE(p1,p2)
//    Concatenates the lists pointed to by p1 and p2, returns a pointer to the
//    concatenated list.
    public LinkedList merge(LinkedList p1, LinkedList p2) {
        LinkedList p3 = new LinkedList();
        p3.addAll(p1);
        p3.addAll(p2);
        return p3;
    }

    private void debug(String message) {
        // TODO Uncomment the following line to enable debug output.
        System.out.println(message);
    }

}