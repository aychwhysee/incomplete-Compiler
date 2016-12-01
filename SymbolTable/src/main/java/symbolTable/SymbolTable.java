package symbolTable;

import errors.SymbolTableError;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;

public class SymbolTable {

/*
	Note: I tried to fix my lexical analyzer using the
	provided FIXMEs from the merge so that I could use
	my own, but that didn't go too well. I'll try to
	fix it up if I get some more time to work on it.
*/


	private HashMap<String, SymbolTableEntry> symTable;

	public SymbolTable()
	{
		// Basic constructor
		symTable = new HashMap<String, SymbolTableEntry>();
	}

	public SymbolTable (int size)
	{
		// Constructor with size
		symTable = new HashMap<String, SymbolTableEntry>(size);
	}

	public SymbolTableEntry lookup (String key)
	{
		// Built-in hash functions, self explanatory
		//return symTable.get(key.toUpperCase());
		return symTable.getOrDefault(key.toUpperCase(),null);
	}

	public void insert(SymbolTableEntry entry) throws SymbolTableError
	{
		// First need to check for duplicates
		if (this.lookup(entry.getName().toUpperCase()) != entry) {
			//If not already in, put it in
			symTable.put(entry.getName().toUpperCase(), entry);
			//otherwise throw an error
		} else throw SymbolTableError.DuplicateEntry(entry.toString());

		//symTable.putIfAbsent(entry.getName().toUpperCase(), entry);
	}

	public int size()
	{
		// Built-in hash function, self explanatory
		return symTable.size();
	}

	public void dumpTable ()
	{
		// Print out contents of the symTable
		// keyn=valuen, keyn-1 = valuen-1 etc
		System.out.print(symTable);
	}

	public static void installBuiltins(SymbolTable table) throws SymbolTableError
	{
		//Define them first
		ProcedureEntry main = new ProcedureEntry("MAIN", 0);
		ProcedureEntry read = new ProcedureEntry("READ", 0);
		ProcedureEntry write = new ProcedureEntry("WRITE", 0);
		IODeviceEntry input = new IODeviceEntry("INPUT");
		IODeviceEntry output = new IODeviceEntry("OUTPUT");
		//Set isReserved value to true (see code in SymbolTableEntry)
		main.setReserved(true);
		read.setReserved(true);
		write.setReserved(true);
		//input.setReserved(true);
		//output.setReserved(true);
		//Install them all
		table.insert(main);
		table.insert(read);
		table.insert(write);
		//table.insert(input);
		//table.insert(output);
	}

}
