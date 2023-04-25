package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenType;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.bytecode.ByteCodeGenerator.internalNameFor;

public class AdditiveExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public AdditiveExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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
    public boolean isAdd() {
        return operator.getType() == TokenType.PLUS;
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
        if (getType().equals(CatscriptType.INT)) {
            if (!leftHandSide.getType().equals(CatscriptType.INT)) {
                leftHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
            if (!rightHandSide.getType().equals(CatscriptType.INT)) {
                rightHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        } else if(getType().equals(CatscriptType.STRING)){
            if (!leftHandSide.getType().equals(CatscriptType.STRING) && !leftHandSide.getType().equals(CatscriptType.INT) && !leftHandSide.getType().equals(CatscriptType.NULL)) {
                leftHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
            if (!rightHandSide.getType().equals(CatscriptType.STRING) && !rightHandSide.getType().equals(CatscriptType.INT) && !rightHandSide.getType().equals(CatscriptType.NULL)) {
                rightHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
            }
        }
    }

    @Override
    public CatscriptType getType() {
        if (leftHandSide.getType().equals(CatscriptType.STRING) || rightHandSide.getType().equals(CatscriptType.STRING)) {
            return CatscriptType.STRING;
        } else {
            return CatscriptType.INT;
        }
    }

    @Override
    public String toString() {
        return super.toString() + "[" + operator.getStringValue() + "]";
    }

    //==============================================================
    // Implementation
    //==============================================================

    @Override
    public Object evaluate(CatscriptRuntime runtime) {
         if(getType() == CatscriptType.INT){
            Integer lhsValue = (Integer) leftHandSide.evaluate(runtime);
            Integer rhsValue = (Integer) rightHandSide.evaluate(runtime);
            if (isAdd()) {
                return lhsValue + rhsValue;
            } else {
                return lhsValue - rhsValue;
            }
        } else {
             String lhs;
             String rhs;
             if(leftHandSide.evaluate(runtime) == null){
                 lhs = "null";
             } else {
                 lhs = leftHandSide.evaluate(runtime).toString();
             }
             if(rightHandSide.evaluate(runtime) == null){
                 rhs = "null";
             } else {
                 rhs = rightHandSide.evaluate(runtime).toString();
             }
            return lhs.concat(rhs);
        }


    }

    @Override
    public void transpile(StringBuilder javascript) {
        getLeftHandSide().transpile(javascript);
        javascript.append(operator.getStringValue());
        getRightHandSide().transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        if(getType().equals(CatscriptType.STRING)){
            getLeftHandSide().compile(code);
            box(code, getLeftHandSide().getType());
            if(!getLeftHandSide().getType().equals(CatscriptType.STRING)){
                code.addMethodInstruction(Opcodes.INVOKESTATIC, internalNameFor(String.class),
                        "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
            }
            getRightHandSide().compile(code);
            box(code, getRightHandSide().getType());
            if(!getRightHandSide().getType().equals(CatscriptType.STRING)){
                code.addMethodInstruction(Opcodes.INVOKESTATIC, internalNameFor(String.class),
                        "valueOf", "(Ljava/lang/Object;)Ljava/lang/String;");
            }
            code.addMethodInstruction(Opcodes.INVOKEVIRTUAL, internalNameFor(String.class),
                    "concat", "(Ljava/lang/String;)Ljava/lang/String;");

        } else {
            getLeftHandSide().compile(code);
            getRightHandSide().compile(code);
            if (isAdd()) {
                code.addInstruction(Opcodes.IADD);
            } else {
                code.addInstruction(Opcodes.ISUB);
            }
        }

    }

}
