package edu.montana.csci.csci468.parser.expressions;

import edu.montana.csci.csci468.bytecode.ByteCodeGenerator;
import edu.montana.csci.csci468.eval.CatscriptRuntime;
import edu.montana.csci.csci468.parser.CatscriptType;
import edu.montana.csci.csci468.parser.ErrorType;
import edu.montana.csci.csci468.parser.ParseError;
import edu.montana.csci.csci468.parser.SymbolTable;
import edu.montana.csci.csci468.tokenizer.Token;
import org.eclipse.jetty.websocket.common.OpCode;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class ComparisonExpression extends Expression {

    private final Token operator;
    private final Expression leftHandSide;
    private final Expression rightHandSide;

    public ComparisonExpression(Token operator, Expression leftHandSide, Expression rightHandSide) {
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

    public boolean isLessThan() {
        return operator.getType().equals(LESS);
    }
    public boolean isLessThanOrEqual() {
        return operator.getType().equals(LESS_EQUAL);
    }
    public boolean isGreaterThanOrEqual() {
        return operator.getType().equals(GREATER_EQUAL);
    }
    public boolean isGreater() {
        return operator.getType().equals(GREATER);
    }

    @Override
    public void validate(SymbolTable symbolTable) {
        leftHandSide.validate(symbolTable);
        rightHandSide.validate(symbolTable);
        if (!leftHandSide.getType().equals(CatscriptType.INT)) {
            leftHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
        if (!rightHandSide.getType().equals(CatscriptType.INT)) {
            rightHandSide.addError(ErrorType.INCOMPATIBLE_TYPES);
        }
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
        String left = leftHandSide.evaluate(runtime).toString();
        Integer lhs = Integer.parseInt(left);
        String right = rightHandSide.evaluate(runtime).toString();
        Integer rhs = Integer.parseInt(right);
        if (isGreater()){
            if(lhs > rhs){
                return true;
            } else {
                return false;
            }
        } else if(isGreaterThanOrEqual()){
            if(lhs >= rhs){
                return true;
            } else {
                return false;
            }
        } else if(isLessThan()){
            if(lhs < rhs){
                return true;
            } else {
                return false;
            }
        } else{
            if(lhs <= rhs){
                return true;
            } else {
                return false;
            }
        }

    }

    @Override
    public void transpile(StringBuilder javascript) {
        super.transpile(javascript);
    }

    @Override
    public void compile(ByteCodeGenerator code) {
        getLeftHandSide().compile(code);
        getRightHandSide().compile(code);
        Label setTrue = new Label();
        Label end = new Label();

        if(isLessThanOrEqual()){
            code.addJumpInstruction(Opcodes.IF_ICMPLE, setTrue);
        } else if(isLessThan()){
            code.addJumpInstruction(Opcodes.IF_ICMPLT, setTrue);
        } else if(isGreater()){
            code.addJumpInstruction(Opcodes.IF_ICMPGT, setTrue);
        } else {
            code.addJumpInstruction(Opcodes.IF_ICMPGE, setTrue);
        }
        code.pushConstantOntoStack(false);
        code.addJumpInstruction(Opcodes.GOTO, end);
        code.addLabel(setTrue);
        code.pushConstantOntoStack(true);
        code.addLabel(end);
    }

}
