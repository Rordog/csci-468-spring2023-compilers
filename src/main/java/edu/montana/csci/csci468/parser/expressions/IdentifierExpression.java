package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import org.objectweb.asm.Opcodes;

public class IdentifierExpression extends Expression {
    private final String name;
    private Token nameToken;
    private CatscriptType type;

    public IdentifierExpression(Token value) {
        this.name = value.getStringValue();
        nameToken = value;
    }

    public String getName() {
        return name;
    }

    @Override
    public CatscriptType getType() {
        return type;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        CatscriptType type = symbolTable.getSymbolType(getName());
        if (type == null) {
            addError(ErrorType.UNKNOWN_NAME, nameToken);
        } else {
            this.type = type;
        }
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object evaluated = runtime.getValue(name);
        return evaluated;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Integer slot = code.resolveLocalStorageSlotFor(name);
        // inverse of variable statement
        if(slot == null){
            //global variable
            // primitive and non-primitive variables
            // push on a this pointer
            // load field
            //Opcodes.GETFIELD

        } else {
            // local variable
            // primitive and non-primitive
            //Opcodes.ILOAD or Opcodes.ALOAD
        }
    }


}
