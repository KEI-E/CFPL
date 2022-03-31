package cfpl;

import java.util.ArrayList;
import java.util.List;

import static cfpl.TokenType.*;

class TestParser { private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    boolean varDeclared = false;              
  
    TestParser(List<Token> tokens) {
      this.tokens = tokens;
    }

    List<Stmt> parse() 
    {
        List<Stmt> statements = new ArrayList<>();

        while (!isAtEnd()) 
        {
          //Created a list of declarations in for many declarations
          List<Stmt> declarations = declaration();

          if(declarations != null && declarations.size() > 0)
            statements.addAll(declarations);
        }
    
        return statements; 
    }

    private Expr expression() {
        return assignment();
    }

    //Made the function into a list for many declaration
    private List<Stmt> declaration()
    { // Declare the starting functions
      List<Stmt> statements = new ArrayList<>();

        try 
        {
          if (match(VAR)) 
          {
            List<Stmt.Var> varStatements = varDeclaration(); //for several var declarations
            
            //getting all var declarations
            for(Stmt.Var var : varStatements)
              statements.add(var);
          }
          else 
            statements.add(statement());
    
          return statements;
        } catch (ParseError error) {
          synchronize();
          return null;
        }
    }

    private Stmt statement() 
    { 
      /*
      The conditions below have been moved to cfpl statements
      // Recursion for primary functions
        //if (match(STAR)) synchronize();
        //if (match(IF)) return ifStatement();
       // if (match(WHILE)) return whileStatement();
        
       // else if (match(OUTPUT) && match(COLON)) return printStatement();
       */
        
        if (match(START)) 
          return new Stmt.Block(block()); // creates START and STOP block

        return expressionStatement();
    }

    private Stmt ifStatement() { // IF Statement
      consume(LEFT_PAREN, "Expect '(' after 'if'.");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after if condition."); 
  
      Stmt thenBranch = statement();
      Stmt elseBranch = null;
      if (match(ELSE)) {
        elseBranch = statement();
      }
  
      return new Stmt.If(condition, thenBranch, elseBranch);
    }

    private Stmt printStatement() { // OUTPUT:
        Expr value = expression();
        return new Stmt.Print(value);
    }

    private Expr assignBool(){ // funtion to assign BOOL
      Expr exp = null;

      if(match(TRUE)) exp = new Expr.Literal(TRUE);
      else if(match(FALSE)) exp = new Expr.Literal(FALSE);
      
      if(!match(DOUBLE_QUOTES)) throw error(previous() ,"Missing \" in decleration");

      if(peekAdvance().type != BOOL) 
      throw error(peekAdvance(), "Invalid DataType for Value");

      return exp;
    }

    private Expr assignChar(){ // function to assign CHAR
      Expr exp = null;
      exp = new Expr.Literal(tokens.get(current).lexeme);
      current = current + 1;
      if(!match(APOS)) throw error(peek() ,"Missing ' in decleration");
      if(peekAdvance().type != CHAR) 
      throw error(peekAdvance(), "Invalid DataType for Value");
      return exp;
    }

    //Transformed into a list for multiple variable declarations
    private List<Stmt.Var> varDeclaration() 
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        List<Stmt.Var> statements = new ArrayList<>();
        Expr initializer = null;

        if (match(EQUAL)) 
        {
          if(match(DOUBLE_QUOTES)) // if "" is found, value is going to be bool
            initializer = assignBool();
          else if(match(APOS))    // if '' is found, value is going to be char
            initializer = assignChar();
          else
            initializer = expression();
        }

        statements.add(new Stmt.Var(name, initializer));

        //for variables declared with comma
        while(match(COMMA))
        {
          name = consume(IDENTIFIER, "Expect variable name.");
          initializer = null;

          if (match(EQUAL)) 
          {
            if(match(DOUBLE_QUOTES)) // if "" is found, value is going to be bool
              initializer = assignBool();
            else if(match(APOS))    // if '' is found, value is going to be char
              initializer = assignChar();
            else
              initializer = expression();
          }

          statements.add(new Stmt.Var(name, initializer));
        }

        consume(AS, "Expect 'AS' after identifier.");

        if (match(EQUAL)) {
          initializer = expression();
        }

        if(!match(INT,FLOAT,CHAR,BOOL))
            throw error(peek(), "Expect <datatype> after 'AS' keyword");  
        
        return statements;
    }

    //several re-assignments inside START STOP
    private List<Stmt> assInsideBlock() 
    {
        List<Stmt> statements = new ArrayList<>();
        Expr initializer = null;

        if (match(EQUAL)) 
        {
          if(match(DOUBLE_QUOTES)) // if "" is found, value is going to be bool
            initializer = assignBool();
          else if(match(APOS))    // if '' is found, value is going to be char
            initializer = assignChar();
          else
            initializer = expression();
        }

        //for many reassignments declared with comma
        while(match(COMMA))
        {
          initializer = null;

          if (match(EQUAL)) 
          {
            if(match(DOUBLE_QUOTES)) // if "" is found, value is going to be bool
              initializer = assignBool();
            else if(match(APOS))    // if '' is found, value is going to be char
              initializer = assignChar();
            else
              initializer = expression();
          }
        }

        if (match(EQUAL)) 
          initializer = expression();
        
        return statements;
    }

    private Stmt whileStatement() { // WHILE Statement
      consume(LEFT_PAREN, "Expect '(' after 'while'.");
      Expr condition = expression();
      consume(RIGHT_PAREN, "Expect ')' after condition.");
      Stmt body = statement();
  
      return new Stmt.While(condition, body);
    }

    private Stmt expressionStatement() 
    {
      Expr expr = expression();
        return new Stmt.Expression(expr);
    }

    //cfpl format
    private Stmt cfplStatement() 
    {
        try 
        {
          if(match(COMMA))
            return new Stmt.Block(assInsideBlock());
          if (match(OUTPUT) && match(COLON)) 
            return printStatement();
          if (match(VAR))
              return snglVarDeclaration();
          if (match(IF)) 
              return ifStatement();  
          if (match(WHILE)) 
              return whileStatement();
          if (match(START))
              return new Stmt.Block(block());

          return expressionStatement();
      } 
      catch (ParseError error) 
      {
          synchronize();
          return null;
      }
        
    }

    //for single declaration
    private Stmt snglVarDeclaration() 
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");
       // List<Stmt.Var> statements = new ArrayList<>();
        Expr initializer = null;

        if (match(EQUAL)) 
        {
          if(match(DOUBLE_QUOTES)) // if "" is found, value is going to be bool
            initializer = assignBool();
          else if(match(APOS))    // if '' is found, value is going to be char
            initializer = assignChar();
          else
            initializer = expression();
        }

       // statements.add(new Stmt.Var(name, initializer));

        consume(AS, "Expect 'AS' after identifier.");

        if (match(EQUAL)) {
          initializer = expression();
        }

        if(!match(INT,FLOAT,CHAR,BOOL))
            throw error(peek(), "Expect <datatype> after 'AS' keyword");  
        
        return new Stmt.Var(name, initializer);
    }

    private List<Stmt> block() 
    {
        List<Stmt> statements = new ArrayList<>();

        if (match(VAR)) 
          throw error(peek() ,"'VAR' inside 'START' 'STOP'");
    
        while (!check(STOP) && !isAtEnd()) 
          statements.add(cfplStatement());  
          //statements.add(delcaration());

          consume(STOP, "Expect STOP after block.");

          if(match(VAR))
            throw error(peek() ,"'VAR' found outside 'START' 'STOP'.");

        return statements;
    }

    private Expr assignment() {
        Expr expr = or();
    
        if (match(EQUAL)) {
          Token equals = previous();
          Expr value = assignment();
    
          if (expr instanceof Expr.Variable) {
            Token name = ((Expr.Variable)expr).name;
            return new Expr.Assign(name, value);
          }
    
          error(equals, "Invalid assignment target."); 
        }
    
        return expr;
    }

    private Expr or() { // OR Operator
      Expr expr = and();
  
      while (match(OR)) {
        Token operator = previous();
        Expr right = and();
        expr = new Expr.Logical(expr, operator, right);
      }
  
      return expr;
    }

    private Expr and() { // AND Operator
      Expr expr = equality();
  
      while (match(AND)) {
        Token operator = previous();
        Expr right = equality();
        expr = new Expr.Logical(expr, operator, right);
      }
  
      return expr;
    }

    private Expr not() { // NOT Operator
      Expr expr = unary();

      while (match(NOT)) {
        System.out.println("NOT ");
        Token operator = previous();
        Expr right = unary();
        expr = new Expr.Logical(expr, operator, right);
      }
      
      return expr;
    }

    private Expr equality() {
        Expr expr = comparison();
    
        while (match(BANG_EQUAL, EQUAL_EQUAL)) {
          Token operator = previous();
          Expr right = comparison();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr comparison() {
        Expr expr = term();
    
        while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
          Token operator = previous();
          Expr right = term();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr term() {
        Expr expr = factor();
    
        while (match(MINUS, PLUS)) {
          Token operator = previous();
          Expr right = factor();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr factor() {
        Expr expr = unary();
    
        while (match(SLASH, STAR)) {
          Token operator = previous();
          Expr right = unary();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr unary() {
        if (match(NOT, MINUS)) {
          Token operator = previous();
          Expr right = unary();
          return new Expr.Unary(operator, right);
        }
    
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(FALSE);
        if (match(TRUE)) return new Expr.Literal(TRUE);
        if (match(NIL)) return new Expr.Literal(null);
    
        if (match(NUMBER, STRING)) {
          return new Expr.Literal(previous().literal);
        }

        if (match(IDENTIFIER)) {
            return new Expr.Variable(previous());
        }
    
        if (match(LEFT_PAREN)) {
          Expr expr = expression();
          consume(RIGHT_PAREN, "Expect ')' after expression.");
          return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    private boolean match(TokenType... types) {
        for (TokenType type : types) {
          if (check(type)) {
            advance();
            return true;
          }
        }
    
        return false;
    }

    private Token consume(TokenType type, String message) {
        if (check(type)) return advance();
    
        throw error(peek(), message);
    }

    private boolean check(TokenType type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }
    
    private boolean isAtEnd() {
        return peek().type == EOF;
    }
    
    private Token peek() { //peeks at current token
        return tokens.get(current);
    }
    
    private Token peekAdvance() { //peeks at the next token
      return tokens.get(current + 1);
    }

    private Token previous() {
        return tokens.get(current - 1);
    }

    private ParseError error(Token token, String message) {
        boolean isReservedWord = TestScanner.reservedWords().get(token.lexeme) != null;

        if(isReservedWord && !varDeclared)
            if(!varDeclared)
                Lexer.error(token, "Reserved Word can't be used as a variable.");
            else
                Lexer.error(token, message);
        Lexer.error(token, message);
        return new ParseError();
    }

    private void synchronize() {
        advance();
    
        while (!isAtEnd()) {
          if (previous().type == STOP) return;
    
          switch (peek().type) {
            case CLASS:
            case FUN:
            case VAR:
            case FOR:
            case IF:
            case WHILE:
            case OUTPUT:
            case RETURN:
            case ELSE:
              return;
          }
    
          advance();
        }
    }
}


