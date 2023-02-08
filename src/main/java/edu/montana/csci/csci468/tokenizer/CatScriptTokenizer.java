package edu.montana.csci.csci468.tokenizer;

import static edu.montana.csci.csci468.tokenizer.TokenType.*;

public class  CatScriptTokenizer {

    TokenList tokenList;
    String src;
    int position = 0;
    int line = 1;
    int lineOffset = 0;

    public CatScriptTokenizer(String source) {
        src = source;
        tokenList = new TokenList(this);
        tokenize();
    }

    private void tokenize() {
        consumeWhitespace();
        while (!tokenizationEnd()) {
            scanToken();
            consumeWhitespace();
        }
        tokenList.addToken(EOF, "<EOF>", position, position, line, lineOffset);
    }

    private void scanToken() {
        if(scanNumber()) {
            return;
        }
        if(scanString()) {
            return;
        }
        if(scanIdentifier()) {
            return;
        }
        scanSyntax();
    }

    private boolean scanString() {
        if(peek() == '"') {
            String value = "";
            int start = position;
            takeChar();
            while (peek() != '"' && !tokenizationEnd()) {
                if(peek() == '\n'){
                    line++;
                    lineOffset++;
                }
                if (peek() == '\\') {
                    takeChar();
                    if(peek() == '"'){
                        takeChar();
                    }
                }
                if(!tokenizationEnd()) {
                    value = value.concat(Character.toString(takeChar()));
                }
            }
            if (tokenizationEnd()) {
                tokenList.addToken(ERROR, "Unterminated String", start, position, line, lineOffset);
            } else {
                takeChar();
                tokenList.addToken(STRING, value, start, position, line, lineOffset);
            }

            return true;
        } else {
            return false;
        }
    }

    private boolean scanIdentifier() {
        if( isAlpha(peek())) {
            int start = position;
            while (isAlphaNumeric(peek())) {
                takeChar();
            }
            String value = src.substring(start, position);
            if (KEYWORDS.containsKey(value)) {
                tokenList.addToken(KEYWORDS.get(value), value, start, position, line, lineOffset);
            } else {
                tokenList.addToken(IDENTIFIER, value, start, position, line, lineOffset);
            }
            return true;
        } else {
            return false;
        }
    }

    private boolean scanNumber() {
        if(isDigit(peek())) {
            int start = position;
            while (isDigit(peek())) {
                takeChar();
            }
            tokenList.addToken(INTEGER, src.substring(start, position), start, position, line, lineOffset);
            return true;
        } else {
            return false;
        }
    }

    private void scanSyntax() {
        int start = position;
        if(matchAndConsume('+')) {
            tokenList.addToken(PLUS, "+", start, position, line, lineOffset);
        } else if(matchAndConsume('-')) {
            tokenList.addToken(MINUS, "-", start, position, line, lineOffset);
        } else if(matchAndConsume('/')) {
            if (matchAndConsume('/')) {
                while (peek() != '\n' && !tokenizationEnd()) {
                    takeChar();
                }
            } else {
                tokenList.addToken(SLASH, "/", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('=')) {
            if (matchAndConsume('=')) {
                tokenList.addToken(EQUAL_EQUAL, "==", start, position, line, lineOffset);
            } else {
                tokenList.addToken(EQUAL, "=", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('"')){
            scanString();
        } else if(matchAndConsume('(')){
            tokenList.addToken(LEFT_PAREN, "(", start, position, line, lineOffset);
        } else if(matchAndConsume(')')){
            tokenList.addToken(RIGHT_PAREN, ")", start, position, line, lineOffset);
        } else if(matchAndConsume('{')){
            tokenList.addToken(LEFT_BRACE, "{", start, position, line, lineOffset);
        } else if(matchAndConsume('}')){
            tokenList.addToken(RIGHT_BRACE, "}", start, position, line, lineOffset);
        } else if(matchAndConsume('[')){
            tokenList.addToken(LEFT_BRACKET, "[", start, position, line, lineOffset);
        } else if(matchAndConsume(']')){
            tokenList.addToken(RIGHT_BRACKET, "]", start, position, line, lineOffset);
        } else if(matchAndConsume(':')){
            tokenList.addToken(COLON, ":", start, position, line, lineOffset);
        } else if(matchAndConsume(',')){
            tokenList.addToken(COMMA, ",", start, position, line, lineOffset);
        } else if(matchAndConsume('.')){
            tokenList.addToken(DOT, ".", start, position, line, lineOffset);
        } else if(matchAndConsume('*')){
            tokenList.addToken(STAR, "*", start, position, line, lineOffset);
        } else if(matchAndConsume('!')){
            if(matchAndConsume('=')){
                tokenList.addToken(BANG_EQUAL, "!=", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('<')){
            if(matchAndConsume('=')){
                tokenList.addToken(LESS_EQUAL, "<=", start, position, line, lineOffset);
            } else {
                tokenList.addToken(LESS, "<", start, position, line, lineOffset);
            }
        } else if(matchAndConsume('>')){
            if(matchAndConsume('=')){
                tokenList.addToken(GREATER_EQUAL, ">=", start, position, line, lineOffset);
            } else {
                tokenList.addToken(GREATER, ">", start, position, line, lineOffset);
            }
        } else {
            tokenList.addToken(ERROR, "<Unexpected Token: [" + takeChar() + "]>", start, position, line, lineOffset);
        }
    }

    private void consumeWhitespace() {
        while (!tokenizationEnd()) {
            char c = peek();
            if (c == ' ' || c == '\r' || c == '\t') {
                position++;
                lineOffset++;
                continue;
            } else if (c == '\n') {
                position++;
                lineOffset = 0;
                line++;
                continue;
            }
            break;
        }
    }

    //===============================================================
    // Utility functions
    //===============================================================

    private char peek() {
        if (tokenizationEnd()) return '\0';
        return src.charAt(position);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') ||
                (c >= 'A' && c <= 'Z') ||
                c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char takeChar() {
        char c = src.charAt(position);
        position++;
        lineOffset++;
        return c;
    }

    private boolean tokenizationEnd() {
        return position >= src.length();
    }

    public boolean matchAndConsume(char c) {
        if (peek() == c) {
            takeChar();
            return true;
        }
        return false;
    }

    public TokenList getTokens() {
        return tokenList;
    }

    @Override
    public String toString() {
        if (tokenizationEnd()) {
            return src + "-->[]<--";
        } else {
            return src.substring(0, position) + "-->[" + peek() + "]<--" +
                    ((position == src.length() - 1) ? "" :
                            src.substring(position + 1, src.length() - 1));
        }
    }
}