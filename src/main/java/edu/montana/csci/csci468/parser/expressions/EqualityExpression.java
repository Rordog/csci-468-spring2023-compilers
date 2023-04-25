package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import java.util.Objects;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class EqualityExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public EqualityExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
        this.leftHandSide = addChild(leftHandSide);
        this.rightHandSide = addChild(rightHandSide);
        this.operator = operator;
    }

    public Expression getLeftHandSide() {
        return leftHandSide;
    }

    public Expression getRightHandSide() {
        return rightHandSide;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    public boolean isEqual() {
        return operator.getType().equals(TokenType.EQUAL_EQUAL);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
    }

    @Override
    public CatscriptType getType() {
        return CatscriptType.BOOLEAN;
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
        Object left = leftHandSide.evaluate(runtime);
        Object right = rightHandSide.evaluate(runtime);
        if(isEqual()){
            if(leftHandSide.getType() == CatscriptType.INT && rightHandSide.getType() == CatscriptType.INT){
                if(left.equals(right)){
                    return true;
                } else {return false;}
            } else if(leftHandSide.getType() == CatscriptType.BOOLEAN && rightHandSide.getType() == CatscriptType.BOOLEAN){
                if(leftHandSide.getStart().getType().equals(rightHandSide.getStart().getType())){
                    return true;
                } else {return false;}
            } else if(leftHandSide.getType() == CatscriptType.STRING && rightHandSide.getType() == CatscriptType.STRING){
                return null;
            } else if(leftHandSide.getType() == CatscriptType.OBJECT && rightHandSide.getType() == CatscriptType.OBJECT){
                return null;
            } else if(leftHandSide.getType() == CatscriptType.NULL && rightHandSide.getType() == CatscriptType.NULL){
                return true;
            }
        } else {
            if(leftHandSide != rightHandSide){
                return true;
            } else {
                return false;
            }
        }
        return null;
    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        getLeftHandSide().compile(code);
        box(code, getLeftHandSide().getType());
        getRightHandSide().compile(code);
        box(code, getRightHandSide().getType());
        code.addMethodInstruction(Opcodes.INVOKESTATIC, internalNameFor(Objects.class),
                "equals", "(Ljava/lang/Object;Ljava/lang/Object;)Z");
        if(! isEqual()){
            code.pushConstantOntoStack(1);
            code.addInstruction(Opcodes.IXOR);
        }
    }


}
