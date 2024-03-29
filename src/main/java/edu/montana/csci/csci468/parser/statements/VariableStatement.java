package edu.montana.csci.csci468.parser.statements;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.*;
import edu.montana.csci.csci468.parser.expressions.*;
import org.objectweb.asm.Opcodes;

public class VariableStatement extends Statement {
    private Expression expression;
    private String variableName;
    private CatscriptType explicitType;
    private CatscriptType type;

    public Expression getExpression() {
        return expression;
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public void setExpression(Expression parseExpression) {
        this.expression = addChild(parseExpression);
    }

    public void setExplicitType(CatscriptType type) {
        this.explicitType = type;
    }

    public CatscriptType getExplicitType() {
        return explicitType;
    }

    public boolean isGlobal() {
        return getParent() instanceof CatScriptProgram;
    }

    @Override
    public void validate(SymbolTable symbolTable)  {
        expression.validate(symbolTable);
        if (symbolTable.hasSymbol(variableName)) {
            addError(ErrorType.DUPLICATE_NAME, expression.getStart());
        } else {
            if(explicitType != null){
                if(expression.getClass() == ListLiteralExpression.class){
                    boolean compatible = true;
                    CatscriptType.ListType listType = (CatscriptType.ListType) explicitType;
                    CatscriptType componentType =  listType.getComponentType();
                    for (int i = 0; i < expression.getChildren().size(); i++){
                        ParseElement ind = expression.getChildren().get(i);
                        if(componentType == CatscriptType.BOOLEAN){
                            if(ind.getClass() != BooleanLiteralExpression.class){
                                compatible = false;
                            }
                        } else if(componentType == CatscriptType.INT){
                            if(ind.getClass() != IntegerLiteralExpression.class){
                                compatible = false;
                            }
                        } else if(componentType == CatscriptType.STRING){
                            if(ind.getClass() != StringLiteralExpression.class){
                                compatible = false;
                            }
                        }
                    }
                    if(compatible == false){
                        addError(ErrorType.INCOMPATIBLE_TYPES, expression.getStart());
                    }
                } else if(explicitType == CatscriptType.OBJECT){
                    type = explicitType;
                } else if(expression.getType() != explicitType){
                    addError(ErrorType.INCOMPATIBLE_TYPES, expression.getStart());
                } else {type = explicitType;}
            } else {
                type = expression.getType();
            }
            symbolTable.registerSymbol(variableName, type);
        }
    }

    public CatscriptType getType() {
        return type;
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
        if(isGlobal()){
            if(getType().equals(CatscriptType.INT) || getType().equals(CatscriptType.BOOLEAN)){
                code.addField(variableName, "I");
                code.addVarInstruction(Opcodes.ALOAD, 0);
                expression.compile(code);
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, "I", code.getProgramInternalName());
            } else {
                String s = "L" + ByteCodeGenerator.internalNameFor(getType().getJavaType()) + ";";
                code.addField(variableName, s);
                code.addVarInstruction(Opcodes.ALOAD, 0);
                expression.compile(code);
                code.addFieldInstruction(Opcodes.PUTFIELD, variableName, s, code.getProgramInternalName());

            }
        } else {
            Integer slotNumber = code.createLocalStorageSlotFor(variableName);
            if(getType().equals(CatscriptType.INT) || getType().equals(CatscriptType.BOOLEAN)){
                expression.compile(code);
                code.addVarInstruction(Opcodes.ISTORE, slotNumber);
            } else {
                expression.compile(code);
                code.addVarInstruction(Opcodes.ASTORE, slotNumber);
            }
        }
    }
}
