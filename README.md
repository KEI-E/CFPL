# CFPL
An interpreter for the CIT-U First Programming Language written in Java.

CFPL is a very simple programming language that allows the programmer to achieve fluency in minutes.  It is a strongly typed programming language. 

From the sample program written below: 

```
* my first program in CFPL
VAR abc, b, c AS INT
VAR x, w_23=’w’ AS CHAR
VAR t=”TRUE” AS BOOL
START
  abc=b=10
  w_23=’a’
  * this is a comment
  OUTPUT: abc & “hi” & b & “#” & w_23 & “[#]”
STOP

```

The console should show:

```
10hi10
a#

```

## Language Grammar

Program Structure:
-	every line contains a single statement
-	all variable declaration is found on top of the program
-	a line that starts with asterisk(*) is considered as a comment and comment can be found in any part of the program
-	executable code should be found inside the START and STOP block
-	all reserved words are in capital letters
-	sharp sign(#) signifies next line or carriage return
-	ampersand(&) serves as a concatenator
-	the square braces([]) are as escape code

Data Types:
1.	``` INT ``` – an ordinary number with no decimal part. It uses 32 bits. It can be positive or negative.
2.	``` CHAR ``` – a single symbol. It uses UNICODE.
3.	``` BOOL ``` – represents the literals true or false.
4.	``` FLOAT ``` – a number with decimal part.  It uses 64 bits.

Operators:
	Arithmetic operators
1.	``` ( ) ```	    - parenthesis
2.	``` *, /, % ```     - multiplication, division, modulo
3.	``` +, - ```	    - addition, subtraction
4.	``` >, < ```	    - greater than, lesser than
5.  ``` >=, <= ```    - greater than or equal to, lesser than or equal to
6.  ``` ==, <>```      - equal, not equal

Logical operators (<BOOL expression><LogicalOperator><BOOL expression>)

1. ``` AND ```  	    - needs the two BOOL expression to be true to result to true, else false
2. ``` OR ``` 	    - if one of the BOOL expressions evaluates to true, returns true, else false
3. ``` NOT ```	    - the reverse value of the BOOL value

Unary operator

1. ``` + ```	    - positive
2. ``` - ```	  	    - negative
