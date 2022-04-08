package cfpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.jar.Attributes.Name;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import static cfpl.TokenType.*;

class TestParser { private static class ParseError extends RuntimeException {}

    private final List<Token> tokens;
    private int current = 0;
    boolean varDeclared = false;              

    private final Map<String, TokenType> storeDataType = new HashMap<>();
  
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
            for(Stmt.Var var : varStatements){
              statements.add(var);
            }
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
      
        return new Expr.Literal(previous().literal);
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

    private Stmt assignInput(){ // INPUT function

      consume(COLON, "Expect : after INPUT decleration.");
      Stmt statement = null;
      String str;

        try {
          Token name = consume(IDENTIFIER, "Expect variable name.");
          //System.out.println("name: "+name.lexeme); // to be removed
          System.out.println("INPUT:");

          InputStreamReader inputStreamReader = new InputStreamReader(System.in);
          BufferedReader reader = new BufferedReader(inputStreamReader);

          str = reader.readLine();
          
          Object Ob = str;
          //System.out.println("Object: "+Ob); // to be removed
          Expr xpr = new Expr.Literal(Ob);
          statement= new Stmt.Var(name, xpr);
        } catch (IOException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      
      return statement;
    }

    //Transformed into a list for multiple variable declarations
    private List<Stmt.Var> varDeclaration() 
    {
        Token name = consume(IDENTIFIER, "Expect variable name.");
        List<Stmt.Var> statements = new ArrayList<>();
        Expr initializer = null;
        TokenType dataType = null;

        if (match(EQUAL)) 
        {
          if(peek().type == NUMBER && peekAdvance().type == BOOL) throw error(peek(), "Incorrect Data Type");
          if(match(DOUBLE_QUOTES)){ // if "" is found, value is going to be bool
            initializer = assignBool();}
          else if(match(APOS))    // if '' is found, value is going to be char
            initializer = assignChar();
          else
            initializer = expression();
        }

        if(peek().type == COMMA){
          int cr = current;
          do{
            current++;
            //System.out.println("peek:"+peek()+"peekAdvance:"+peekAdvance()); // to be removed
            if(peekAdvance().type == INT) dataType = INT;
            else if(peekAdvance().type == FLOAT) dataType = FLOAT;
            else if(peekAdvance().type == BOOL) dataType = BOOL;
            else if(peekAdvance().type == CHAR) dataType = CHAR;
            
          }while(peek().type != AS);
          current = cr;
          storeDataType.put(name.lexeme, dataType);
          //System.out.println(name.lexeme+":"+storeDataType.get(name.lexeme)); 
          statements.add(new Stmt.Var(name, initializer));
        }
        else{
          consume(AS, "Expect 'AS' after identifier."); //This code runs only when there is a single VAR decleration
          dataType = peek().type;

          if      (peek().type == INT)    consume(INT, "Error");
          else if (peek().type == FLOAT)  consume(FLOAT, "Error");
          else if (peek().type == BOOL)   consume(BOOL, "Error");
          else if (peek().type == CHAR)   consume(CHAR, "Error");
          
          storeDataType.put(name.lexeme, dataType);
          //System.out.println(name.lexeme+":"+storeDataType.get(name.lexeme)); 
          statements.add(new Stmt.Var(name, initializer));
          return statements;
        }

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
          storeDataType.put(name.lexeme, dataType);
          //System.out.println(name.lexeme+":"+storeDataType.get(name.lexeme));
          statements.add(new Stmt.Var(name, initializer));
        }

        consume(AS, "Expect 'AS' after identifier.");

      //  if (match(EQUAL)) {
     //     initializer = expression();
     //   }

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
      Stmt statement = null;
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
          if (match(INPUT)){
              return assignInput();
              }

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
        TokenType dataType = null;
        
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

        if      (match(INT))    dataType = INT;
        else if (match(FLOAT))  dataType = FLOAT;
        else if (match(BOOL))   dataType = BOOL;
        else if (match(CHAR))   dataType = CHAR;
        else throw error(peek(), "Expect <datatype> after 'AS' keyword");  
        //System.out.println("something:"+dataType);// to be removed
        storeDataType.put(name.lexeme, dataType);

        return new Stmt.Var(name, initializer);
    }

    private List<Stmt> block() 
    {
        List<Stmt> statements = new ArrayList<>();

        if (match(VAR)) 
          throw error(peek() ,"'VAR' inside 'START' 'STOP'");
    
        while (!check(STOP) && !isAtEnd()) 
        {
          statements.add(cfplStatement());  

          if (match(VAR)) 
            throw error(peek() ,"'VAR' inside 'START' 'STOP'");
        }
         
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
        //System.out.println("AND");
        Token operator = previous();
        Expr right = equality();
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
    
        while (match(SLASH, STAR, MODULO)) {
          Token operator = previous();
          Expr right = unary();
          expr = new Expr.Binary(expr, operator, right);
        }
    
        return expr;
    }

    private Expr unary() {
        if (match(BANG, MINUS, NOT)) {
          Token operator = previous();
          Expr right = unary();
          return new Expr.Unary(operator, right);
        }
    
        return primary();
    }

    private Expr primary() {
        if (match(FALSE)) return new Expr.Literal(false);
        if (match(TRUE)) return new Expr.Literal(true);
        if (match(NIL)) return new Expr.Literal(null);

        if (match(NUMBER, STRING, BOOL)) {
          //System.out.println("peek: "+previous().type+":"+tokens.get(current-3));
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

        if (match(APOS)){
          //System.out.println("lexme:"+tokens.get(current).lexeme); // to be removed
          Expr expr = new Expr.Literal(tokens.get(current).lexeme);
          consume(IDENTIFIER, "Expect Expression");
          consume(APOS, "Expect ' after expression:");
          return expr;
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

        if(isReservedWord) {
            Lexer.error(token, "Reserved Word can't be used as a variable.");
            //System.out.println(TestScanner.reservedWords().get(token.lexeme));
        }
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
            case BOOL:
              return;
          }
    
          advance();
        }
    }
}


