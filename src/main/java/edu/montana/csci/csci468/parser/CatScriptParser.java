package edu.montana.csci.csci468.parser;

import edu.montana.csci.csci468.parser.expressions.*;
import edu.montana.csci.csci468.parser.statements.*;
import edu.montana.csci.csci468.tokenizer.CatScriptTokenizer;
import edu.montana.csci.csci468.tokenizer.Token;
import edu.montana.csci.csci468.tokenizer.TokenList;
import edu.montana.csci.csci468.tokenizer.TokenType;

import javax.swing.plaf.nimbus.State;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

import java.util.LinkedList;
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

    private Statement parseProgramStatement(){
        Statement functionDefStatement = parseFunctionDefinitionStatement();
        if(functionDefStatement != null){
            return functionDefStatement;
        }
        return parseStatement();
    }

    private Statement parseFunctionDefinitionStatement(){
        if(tokens.match(FUNCTION)){
            FunctionDefinitionStatement def = new FunctionDefinitionStatement();
            def.setStart(tokens.consumeToken());
            Token name = require(IDENTIFIER, def);
            def.setName(name.getStringValue());
            require(LEFT_PAREN, def);
            if(!tokens.match(RIGHT_PAREN)){
                do {
                    Token parameterName = require(IDENTIFIER, def);
                    TypeLiteral typeLiteral = null;
                    if(tokens.matchAndConsume(COLON)){
                        typeLiteral = parseTypeExpression();
                    }
                    def.addParameter(parameterName.getStringValue(), typeLiteral);
                } while(tokens.matchAndConsume(COMMA) && tokens.hasMoreTokens());
            }
            require(RIGHT_PAREN, def);
            TypeLiteral returnType = null;
            if(tokens.matchAndConsume(COLON)){
                returnType = parseTypeExpression();
            }
            def.setType(returnType);
            require(LEFT_BRACE, def);
            this.currentFunctionDefinition = def;
            LinkedList<Statement> body = new LinkedList<>();
            while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                body.add(parseStatement());
            }
            require(RIGHT_BRACE, def);
            def.setBody(body);
            this.currentFunctionDefinition = null;
            return def;
        }
        return null;
    }

    private TypeLiteral parseTypeExpression() {
        TypeLiteral typeLiteral = new TypeLiteral();
        boolean isList = false;
        String type = require(IDENTIFIER, typeLiteral).getStringValue();
        if(type.equals("list")){
            isList = true;
            if(tokens.matchAndConsume(LESS)){
                type = require(IDENTIFIER, typeLiteral).getStringValue();
                require(GREATER, typeLiteral);
            } else {
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            }

        }
        if(type.equals("int")){
            if(isList == true){
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.INT));
            } else {typeLiteral.setType(CatscriptType.INT);}
        } else if(type.equals("string")){
            if(isList == true){
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.STRING));
            } else {typeLiteral.setType(CatscriptType.STRING);}
        } else if(type.equals("bool")){
            if(isList == true){
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.BOOLEAN));
            } else {typeLiteral.setType(CatscriptType.BOOLEAN);}
        } else if(type.equals("object")){
            if(isList == true){
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.OBJECT));
            } else {typeLiteral.setType(CatscriptType.OBJECT);}
        } else if(type.equals("null")){
            if(isList == true){
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.NULL));
            } else {typeLiteral.setType(CatscriptType.NULL);}
        } else if(type.equals("void")){
            if(isList == true){
                typeLiteral.setType(CatscriptType.getListType(CatscriptType.VOID));
            } else {typeLiteral.setType(CatscriptType.VOID);}
        }
        return typeLiteral;
    }

    private Statement parseStatement() {
        Statement printStmt = parsePrintStatement();
        if (printStmt != null) {
            return printStmt;
        }
        Statement forStmt = parseForStatement();
        if (forStmt != null) {
            return forStmt;
        }
        Statement returnStmt = parseReturnStatement();
        if (returnStmt != null) {
            return returnStmt;
        }
        Statement ifStmt = parseIfStatement();
        if (ifStmt != null){
            return ifStmt;
        }
        Statement varStmt = parseVarStatement();
        if (varStmt != null){
            return varStmt;
        }
        Statement assignmentOrFuncCall = parseAssignmentOrFunctionCallStatement();
        if(assignmentOrFuncCall != null){
            return assignmentOrFuncCall;
        }
        return new SyntaxErrorStatement(tokens.consumeToken());
    }

    private Statement parseVarStatement() {
        if(tokens.matchAndConsume(VAR)){
            VariableStatement def = new VariableStatement();
            if(tokens.match(IDENTIFIER)){
                def.setVariableName(tokens.consumeToken().getStringValue());
                if(tokens.matchAndConsume(COLON)){
                    def.setExplicitType(parseTypeExpression().getType());
                }
                if(tokens.matchAndConsume(EQUAL)){
                    def.setExpression(parseExpression());
                } else {def.addError(ErrorType.UNEXPECTED_TOKEN);}
            } else {def.addError(ErrorType.UNEXPECTED_TOKEN);}
            return def;
        } else {return null;}
    }

    private Statement parseIfStatement() {
        if(tokens.matchAndConsume(IF)){
            IfStatement def = new IfStatement();
            if(tokens.matchAndConsume(LEFT_PAREN)){
                Expression condition = parseExpression();
                def.setExpression(condition);
                if(tokens.matchAndConsume(RIGHT_PAREN)){
                    if(tokens.matchAndConsume(LEFT_BRACE)){
                        List<Statement> trueArgs = new ArrayList<>();
                        while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                            Statement statement = parseStatement();
                            trueArgs.add(statement);
                        }
                        if(!tokens.matchAndConsume(RIGHT_BRACE)){
                            def.addError(ErrorType.UNEXPECTED_TOKEN);
                        }
                        def.setTrueStatements(trueArgs);
                        if(tokens.matchAndConsume(ELSE)){
                            Statement ifStmt = parseIfStatement();
                            if (ifStmt != null){
                                return ifStmt;
                            } else {
                                if(tokens.matchAndConsume(LEFT_BRACE)){
                                    List<Statement> elseArgs = new ArrayList<>();
                                    while (!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()) {
                                        Statement statement = parseStatement();
                                        elseArgs.add(statement);
                                    }
                                    if (!tokens.matchAndConsume(RIGHT_BRACE)) {
                                        def.addError(ErrorType.UNEXPECTED_TOKEN);
                                    }
                                    def.setElseStatements(elseArgs);
                                    return def;
                                } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
                            }
                        } else {return def;}
                    } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
                } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
            } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
        } else {
            return null;
        }

    }

    private Statement parseAssignmentOrFunctionCallStatement() {
        if(tokens.match(IDENTIFIER)){
            Token id = tokens.consumeToken();
            if(tokens.match(LEFT_PAREN)){
                return parseFunctionCallStatement(id);
            } else {
                return parseAssignmemntStatement(id);
            }
        } else {
            return null;
        }
    }

    private Statement parseAssignmemntStatement(Token id) {
        if(tokens.match(EQUAL)){
            tokens.consumeToken();
            AssignmentStatement def = new AssignmentStatement();
            def.setVariableName(id.getStringValue());
            def.setExpression(parseExpression());
            return def;
        } else {return null;}
    }

    private Statement parseFunctionCallStatement(Token id) {
        FunctionCallExpression e = parseFunctionCallExpression(id);
        return new FunctionCallStatement(e);
    }

    private FunctionCallExpression parseFunctionCallExpression(Token id) {
        if(tokens.match(LEFT_PAREN)){
            tokens.consumeToken();
            LinkedList<Expression> args = new LinkedList<>();
            if(!tokens.match(RIGHT_PAREN)){
                do {
                    args.add(parseExpression());
                } while(tokens.matchAndConsume(COMMA) && tokens.hasMoreTokens());
            }
            FunctionCallExpression def = new FunctionCallExpression(id.getStringValue(), args);
            if(tokens.match(RIGHT_PAREN)){
                tokens.consumeToken();
            } else {
                def.addError(ErrorType.UNEXPECTED_TOKEN, def);
            }
            return def;
        } else {return null;}
    }

    private Statement parseForStatement() {
        if(tokens.matchAndConsume(FOR)){
            ForStatement def = new ForStatement();
            if(tokens.matchAndConsume(LEFT_PAREN)){
                def.setVariableName(tokens.consumeToken());
            } else {def.addError(ErrorType.UNEXPECTED_TOKEN);}
            if(tokens.matchAndConsume(IN)){
                Expression loop = parseExpression();
                if(tokens.matchAndConsume(RIGHT_PAREN)){
                    if(tokens.matchAndConsume(LEFT_BRACE)){
                        List<Statement> args = new ArrayList<>();
                        while(!tokens.match(RIGHT_BRACE) && tokens.hasMoreTokens()){
                            Statement statement = parseStatement();
                            args.add(statement);
                        }
                        if(!tokens.matchAndConsume(RIGHT_BRACE)){
                            def.addError(ErrorType.UNEXPECTED_TOKEN);
                        }
                        def.setExpression(loop);
                        def.setBody(args);
                        return def;
                    } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
                } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
            } else {def.addError(ErrorType.UNEXPECTED_TOKEN); return def;}
        } else {
            return null;
        }
    }

    private Statement parseReturnStatement() {
        if(this.currentFunctionDefinition != null){
            if(tokens.match(RETURN)){
                ReturnStatement def = new ReturnStatement();
                def.setStart(tokens.consumeToken());
                def.setFunctionDefinition(this.currentFunctionDefinition);
                if(!tokens.match(RIGHT_BRACE)) {
                    Expression ret = parseExpression();
                    def.setExpression(ret);
                }
                return def;
            }
            return null;
        } else {return null;}
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
        if(tokens.matchAndConsume(LEFT_PAREN)){
            ParenthesizedExpression parenthesizedExpression = new ParenthesizedExpression(parseExpression());
            require(RIGHT_PAREN, parenthesizedExpression);
            return parenthesizedExpression;
        }
        Expression lhs = parseFactorExpression();
        while (tokens.match(PLUS, MINUS)) {
            Token operator = tokens.consumeToken();
            final Expression rightHandSide = parseFactorExpression();
            AdditiveExpression additiveExpression = new AdditiveExpression(operator, lhs, rightHandSide);
            additiveExpression.setStart(lhs.getStart());
            additiveExpression.setEnd(rightHandSide.getEnd());
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
        } else if(tokens.matchAndConsume(LEFT_PAREN)){
            return new ParenthesizedExpression(parseExpression());
        } else if(tokens.match(RIGHT_PAREN)){
            return null;
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
                IdentifierExpression identifierExpression = new IdentifierExpression(name);
                return identifierExpression;
            }
        } else if(tokens.match(NULL)){
            NullLiteralExpression nullLiteralExpression = new NullLiteralExpression();
            nullLiteralExpression.setStart(tokens.consumeToken());
            return nullLiteralExpression;
        } else if(tokens.match(TRUE)){
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(true);
            booleanLiteralExpression.setStart(tokens.consumeToken());
            return booleanLiteralExpression;
        } else if(tokens.match(FALSE)){
            BooleanLiteralExpression booleanLiteralExpression = new BooleanLiteralExpression(false);
            booleanLiteralExpression.setStart(tokens.consumeToken());
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
