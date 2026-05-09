package com.lpu.smartcli.smart;

import java.util.ArrayList;
import java.util.List;

public class SyntaxHighlighter {

    public static class Token {
        public enum TokenType {
            COMMAND,
            FLAG,
            FILENAME,
            ARGUMENT
        }

        String text;
        TokenType type;

        public Token(String text, TokenType type) {
            this.text = text;
            this.type = type;
        }

        @Override
        public String toString() {
            return text + " [" + type + "]";
        }
    }

    public static List<Token> highlight(String commandString) {
        List<Token> highlightedTokens = new ArrayList<>();
        String[] tokens = commandString.split(" ");

        for (int i = 0; i < tokens.length; i++) {
            Token.TokenType type;

            if (i == 0) {
                type = Token.TokenType.COMMAND;
            } else if (tokens[i].startsWith("-")) {
                type = Token.TokenType.FLAG;
            } else if (tokens[i].contains(".")) {
                type = Token.TokenType.FILENAME;
            } else {
                type = Token.TokenType.ARGUMENT;
            }

            highlightedTokens.add(new Token(tokens[i], type));
        }

        return highlightedTokens;
    }

    public static void main(String[] args) {
        printTokens(highlight("git commit -m hello.txt"));
        printTokens(highlight("create notes.txt"));
        printTokens(highlight("write report.txt This is content"));
    }

    private static void printTokens(List<Token> tokens) {
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
