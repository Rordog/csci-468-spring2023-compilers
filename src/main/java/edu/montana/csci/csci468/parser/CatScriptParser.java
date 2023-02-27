package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;
import java.util.List;
import java.util.ArrayList;

public class CatScriptParser {

    private TokenList tokens;
    private FunctionDefinitionStatement currentFunctionDefinition;

    public CatScriptProgram parse(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();

        // first parse an expression
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = null;
        try {
            expression = parseExpression();
        } catch(RuntimeException re) {
            // ignore :)
        }
        if (expression == null || tokens.hasMoreTokens()) {
            tokens.reset();
            while (tokens.hasMoreTokens()) {
                program.addStatement(parseProgramStatement());
            }
        } else {
            program.setExpression(expression);
        }

        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    public CatScriptProgram parseAsExpression(String source) {
        tokens = new CatScriptTokenizer(source).getTokens();
        CatScriptProgram program = new CatScriptProgram();
        program.setStart(tokens.getCurrentToken());
        Expression expression = parseExpression();
        program.setExpression(expression);
        program.setEnd(tokens.getCurrentToken());
        return program;
    }

    //============================================================
    //  Statements
    //============================================================

    private Statement parseProgramStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parsePrintStatement() {
        if (tokens.match(PRINT)) {

            PrintStatement printStatement = new PrintStatement();
            printStatement.setStart(tokens.consumeToken());

            require(LEFT_PAREN, printStatement);
            printStatement.setExpression(parseExpression());
            printStatement.setEnd(require(RIGHT_PAREN, printStatement));

            return printStatement;
        } else {
            return null;
        }
    }

    //============================================================
    //  Expressions
    //============================================================

    private Expression parseExpression() {
        return parseEqualityExpression();
    }

    private Expression parseEqualityExpression() {
        Expression lhs = parseComparisonExpression();
        if (tokens.match(EQUAL_EQUAL,  BANG_EQUAL)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseComparisonExpression();
            EqualityExpression equalityExpression = new EqualityExpression(token, lhs, rhs);
            equalityExpression.setStart(token);
            equalityExpression.setEnd(rhs.getEnd());
            return equalityExpression;
        } else {
            return lhs;
        }
    }
    private Expression parseComparisonExpression() {
        Expression lhs = parseAdditiveExpression();
        if (tokens.match(LESS, LESS_EQUAL, GREATER, GREATER_EQUAL)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseAdditiveExpression();
            ComparisonExpression comparisonExpression = new ComparisonExpression(token, lhs, rhs);
            comparisonExpression.setStart(token);
            comparisonExpression.setEnd(rhs.getEnd());
            return comparisonExpression;
        } else {
            return lhs;
        }
    }
    private Expression parseAdditiveExpression() {
        boolean hasParen = false;
        if(tokens.match(LEFT_PAREN)){
            tokens.consumeToken();
            hasParen = true;
        }
        Expression lhs = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, lhs, rightHandSide);
            additiveExpression.setStart(lhs.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
            if(tokens.match(RIGHT_PAREN) && hasParen == true){
                tokens.consumeToken();
                ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(additiveExpression);
                return parenthesizedExpression;
            }
            lhs = additiveExpression;
        }
        return lhs;
    }

    private Expression parseUnaryExpression() {
        if (tokens.match(MINUS, NOT)) {
            Token token = tokens.consumeToken();
            Expression rhs = parseUnaryExpression();
            UnaryExpression unaryExpression = new UnaryExpression(token, rhs);
            unaryExpression.setStart(token);
            unaryExpression.setEnd(rhs.getEnd());
            return unaryExpression;
        } else {
            return parsePrimaryExpression();
        }
    }

    private Expression parseFactorExpression() {
        Expression lhs = parseUnaryExpression();
        while (tokens.match(SLASH, STAR)) {
            Token operator = tokens.consumeToken();
            final Expression rhs = parseUnaryExpression();
            FactorExpression factorExpression = new FactorExpression(operator, lhs, rhs);
            factorExpression.setStart(lhs.getStart());
            factorExpression.setEnd(rhs.getEnd());
            lhs = factorExpression;
        }
        return lhs;
    }


    private Expression parsePrimaryExpression() {
        if (tokens.match(INTEGER)) {
            Token integerToken = tokens.consumeToken();
            IntegerLiteralExpression integerExpression = new IntegerLiteralExpression(integerToken.getStringValue());
            integerExpression.setToken(integerToken);
            return integerExpression;
        } else if(tokens.match(STRING)){
            Token string = tokens.consumeToken();
            StringLiteralExpression stringLiteralExpression = new StringLiteralExpression(string.getStringValue());
            return stringLiteralExpression;
        } else if(tokens.match(LEFT_PAREN)){
            return parseExpression();
        } else if(tokens.match(LEFT_BRACKET)){
            return parseListLiteral();
        } else if(tokens.match(IDENTIFIER)){
            Token name = tokens.consumeToken();

            if(tokens.matchAndConsume(LEFT_PAREN)){
                List<Expression> arguments = new ArrayList<Expression>();
                if(tokens.match(RIGHT_PAREN)){
                    tokens.consumeToken();
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(name.getStringValue(), arguments);
                    return functionCallExpression;
                } else {
                    do {
                        arguments.add(parseExpression());
                    } while (tokens.matchAndConsume(COMMA));
                    FunctionCallExpression functionCallExpression = new FunctionCallExpression(name.getStringValue(), arguments);
                    if(tokens.match(RIGHT_PAREN)){
                        tokens.consumeToken();
                        return functionCallExpression;
                    } else {
                        functionCallExpression.addError(ErrorType.UNTERMINATED_ARG_LIST);
                        return functionCallExpression;
                    }
                }

            } else {
                IdentifierExpression identifierExpression = new IdentifierExpression(name.getStringValue());
                return identifierExpression;
            }

        } else if(tokens.match(NULL)){
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            return nullLiteralExpression;
        } else if(tokens.match(TRUE)){
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(true);
            return booleanLiteralExpression;
        } else if(tokens.match(FALSE)){
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(false);
            return booleanLiteralExpression;
        } else {
            SyntaxErrorExpression syntaxErrorExpression = new SyntaxErrorExpression(tokens.consumeToken());
            return syntaxErrorExpression;
        }
    }

    private Expression parseListLiteral(){
        if(tokens.match(LEFT_BRACKET)){
            tokens.consumeToken();
            List<Expression> list = new ArrayList<Expression>();
            if(tokens.match(RIGHT_BRACKET)) {
                tokens.consumeToken();
                ListLiteralExpression listLiteralExpression = new ListLiteralExpression(list);
                return listLiteralExpression;
            }
            do {
                list.add(parseExpression());
            } while (tokens.matchAndConsume(COMMA));
            ListLiteralExpression listLiteralExpression = new ListLiteralExpression(list);
            if(tokens.match(RIGHT_BRACKET)){
                tokens.consumeToken();
                return listLiteralExpression;
            } else {
                listLiteralExpression.addError(ErrorType.UNTERMINATED_LIST);
                return listLiteralExpression;
            }


        } else {
            return null;
        }
    }
    //============================================================
    //  Parse Helpers
    //============================================================
    private Token require(TokenType type, ParseElement elt) {
        return require(type, elt, ErrorType.UNEXPECTED_TOKEN);
    }

    private Token require(TokenType type, ParseElement elt, ErrorType msg) {
        if(tokens.match(type)){
            return tokens.consumeToken();
        } else {
            elt.addError(msg, tokens.getCurrentToken());
            return tokens.getCurrentToken();
        }
    }

}
