package cfpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static cfpl.TokenType.*;
import static cfpl.TokenType.COLON;

public class TestScanner {
    private final String source;
    private final List<Token> tokens = new ArrayList<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    TestScanner(String source) {
    this.source = source;
    }

    List<Token> scanTokens() {
        while (!isAtEnd()) {
          // We are at the beginning of the next lexeme.
          start = current;
          scanToken();
        }
    
        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(':   addToken(LEFT_PAREN); break;
            case ')':   addToken(RIGHT_PAREN); break;
            case ',':   addToken(COMMA); break;
            case '.':   addToken(DOT); break;
            case '-':   addToken(MINUS); break;
            case '+':   addToken(PLUS); break;
            case ';':   addToken(SEMICOLON); break;
            case ':':   addToken(COLON); break;
            case '&':   addToken(PLUS); break;
            case '*':   
                if(comment()){
                    while (peek() != '\n' && !isAtEnd()) advance();
                }
                else addToken(STAR);
                break;
            case '!':
                        addToken(BANG);
                        break;
            case '=':   addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                        break;
            case '<':   if(match('>')){ 
                            addToken(BANG_EQUAL);
                            break;
                        }
                        addToken(match('=') ? LESS_EQUAL : LESS);
                        break;
            case '>':
                        addToken(match('=') ? GREATER_EQUAL : GREATER);
                        break;
            case '/':
                        addToken(SLASH);
                        break;
            case '\'':  addToken(APOS); break;
            case ' ':
            case '\r':
            case '\t':
                        // Ignore whitespace.
                        break;
              
            case '\n':
                        line++;
                        break;
            case '"':   addToken(DOUBLE_QUOTES);
                        //string(); 
                        break;
            case 'o':
                        if (match('r')) {
                            addToken(OR);
                        }
                        break;
            default:    if (isDigit(c)) {
                            number();
                        }   else if (isAlpha(c)) {
                            identifier();
                        } else {
                            Lexer.error(line, "Unexpected character.");
                        }
                        break;
        }
    }

    private boolean comment() {
        String line[] = source.split("\n");

        boolean flag = false;

        for (String str : line) {
            if(str.startsWith("*")){
                flag = true;
                //System.out.println(str);
            }
        }
        return flag;
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();

        String text = source.substring(start, current);
        TokenType type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private void number() {
        while (isDigit(peek())) advance();
    
        // Look for a fractional part.
        if (peek() == '.' && isDigit(peekNext())) {
          // Consume the "."
          advance();
    
          while (isDigit(peek())) advance();
        }
    
        addToken(NUMBER,
            Double.parseDouble(source.substring(start, current)));
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
    }
    
    if (isAtEnd()) {
        Lexer.error(line, "Unterminated string.");
        return;
    }
    
        // The closing ".
        advance();
    
        // Trim the surrounding quotes.
        String value = source.substring(start + 1, current - 1);
        addToken(STRING, value);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current) != expected) return false;
    
        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return source.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= source.length()) return '\0';
        return source.charAt(current + 1);
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

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private char advance() {
        return source.charAt(current++);
    }
    
    private void addToken(TokenType type) {
        addToken(type, null);
    }
    
    private void addToken(TokenType type, Object literal) {
        String text = source.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private static final Map<String, TokenType> keywords;

    static {
        keywords = new HashMap<>();

        //CFPL Reserved Words
        keywords.put("VAR", VAR);
        keywords.put("AS", AS);
        keywords.put("OUTPUT", OUTPUT);

        //CFPL Block
        keywords.put("START", START);
        keywords.put("STOP", STOP);

        //CFPL Data Types
        keywords.put("INT", INT);
        keywords.put("CHAR", CHAR);
        keywords.put("BOOL", BOOL);
        keywords.put("FLOAT", FLOAT);

        //CFPL Logical Operators
        keywords.put("AND", AND);
        keywords.put("OR", OR);
        keywords.put("NOT", NOT);

        //CFPL Control Structures
        keywords.put("IF", IF);
        keywords.put("ELSE", ELSE);

        keywords.put("class",  CLASS);
        keywords.put("FALSE",  FALSE);
        keywords.put("for",    FOR);
        keywords.put("fun",    FUN);
        keywords.put("nil",    NIL);
        keywords.put("or",     OR);
        keywords.put("print",  PRINT);
        keywords.put("return", RETURN);
        keywords.put("super",  SUPER);
        keywords.put("this",   THIS);
        keywords.put("TRUE",   TRUE);
        keywords.put("WHILE",  WHILE);
    }
    
}
