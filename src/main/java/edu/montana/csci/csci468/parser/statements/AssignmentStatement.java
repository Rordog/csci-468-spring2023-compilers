package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.parser.expressions.Expression;
import org.objectweb.asm.Opcodes;

public class AssignmentStatement extends Statement {
    private Expression expression;
    private String variableName;

    public Expression getExpression() {
        return expression;
    }

    public void setExpression(Expression expression) {
        this.expression = addChild(expression);
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        expression.validate(symbolTable);
        CatscriptType symbolType = symbolTable.getSymbolType(getVariableName());
        if (symbolType == null) {
            addError(ErrorType.UNKNOWN_NAME);
        } else {
            if(symbolType != expression.getType()){
                addError(ErrorType.INCOMPATIBLE_TYPES, expression.getStart());
            }
        }
    }

    //==============================================================
    // Implementation
    //==============================================================
    @Override
    public void execute(CatscriptRuntime runtime) {
        Object evaluate = expression.evaluate(runtime);
        runtime.setValue(variableName, evaluate);
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        Integer slot = code.resolveLocalStorageSlotFor(variableName);
        if(slot == null){
            code.addVarInstruction(Opcodes.ALOAD, 0);
            if(expression.getType().equals(CatscriptType.INT) || expression.getType().equals(CatscriptType.BOOLEAN)){
                expression.compile(code);
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, "I", code.getProgramInternalName());
            } else {
                String s = "L" + ByteCodeGenerator.internalNameFor(expression.getType().getJavaType()) + ";";
                expression.compile(code);
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, s, code.getProgramInternalName());
            }
        } else {
            Integer slotNumber = code.resolveLocalStorageSlotFor(variableName);
            if(expression.getType().equals(CatscriptType.INT) || expression.getType().equals(CatscriptType.BOOLEAN)){
                expression.compile(code);
                code.addVarInstruction(Opcodes.ISTORE, slotNumber);
            } else {
                expression.compile(code);
                code.addVarInstruction(Opcodes.ASTORE, slotNumber);
            }
        }
    }
    /*
    int x;
        x = 12;
     // access flags 0x9
  public static main([Ljava/lang/String;)V
   L0
    LINENUMBER 19 L0
    BIPUSH 12
    ISTORE 1
   L1
    LINENUMBER 20 L1
    RETURN
   L2
    LOCALVARIABLE args [Ljava/lang/String; L0 L2 0
    LOCALVARIABLE x I L1 L2 1
    MAXSTACK = 1
    MAXLOCALS = 2
}
    var x = 2
    x = 1
    L0
    LINENUMBER 22 L0
    ICONST_2
    ISTORE 1
   L1
    LINENUMBER 23 L1
    ICONST_1
    ISTORE 1
   L2
    LINENUMBER 24 L2
    RETURN
   L3
    LOCALVARIABLE args [Ljava/lang/String; L0 L3 0
    LOCALVARIABLE x I L1 L3 1
    MAXSTACK = 1
    MAXLOCALS = 2
     */
}
