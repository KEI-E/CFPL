package cfpl;

public enum TokenType {
    //Data Types
    INT, CHAR, BOOL, FLOAT,

    //Program Structure
    START, STOP, AS, OUTPUT, VAR, COMMENT,  COLON,

    //Operators
    LEFT_PAREN, RIGHT_PAREN, STAR, SLASH, MOD,
    PLUS, MINUS, GREATER, LESS, GREATER_EQUAL, LESS_EQUAL,
    EQUAL_EQUAL, NOT_EQUAL, BANG_EQUAL, DOT, EQUAL,
    DOUBLE_QUOTES, 

    //Logical Operators
    AND, OR, NOT,

    //Unary
    POS, NEG,

    //Boolean
    TRUE, FALSE,

    // Single-character tokens.
    COMMA, SEMICOLON, APOS, BANG, AMPERSAND,

    //LITERALS
    IDENTIFIER, STRING, NUMBER,

    EOF, PRINT, NIL,

    // Keywords.
    CLASS, ELSE,FUN, FOR, IF,
    RETURN, SUPER, THIS, WHILE,

    
  }