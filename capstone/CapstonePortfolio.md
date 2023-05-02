# Section 4: Technical Report

## Introduction

This program is a compiler written in java and creates a new language called Catscript. 
Catscript is a statically typed functional programming language. 
It possesses many of the features present in java, such as for loops, if statements, and return statements. 

## Features

### For Loop
Although built in java, which is known for its three part for loop declaration, the catscript for loop is quite
simple. The grammar for this loop is below.
```
for_statement = 'for', '(', IDENTIFIER, 'in', expression ')', 
                '{', { statement }, '}'
```
This for loop's declaration only has two parts: an identifier variable and an expression to iterate through, most commonly a list.
Every time the loop iterates through the guiding expression, the statements inside will be executed, using the
argument variable if called, x in the example below.

```
for(x in [1, 2, 3]) { 
    print(x) 
}
```



### If Statement
The catscript If statement is straightforward like If statements in other languages. The controlling expression
requires an expression that can return a boolean value. The grammar for the If statement is shown below.
```
if_statement = 'if', '(', expression, ')', '{', 
                    { statement }, 
               '}' 
               [ 'else', ( if_statement | '{', { statement }, '}' ) ]

```
If the controlling expression returns true, the statement inside will execute. Else statements are also usable in 
Catscript. If the controlling expression returns false, the else statement will execute if present. 

In the example below, the controlling expression returns true because the value of y is less than 3. If 
the variable y was greater than or equal 3, the else statement would execute instead and return false.
```
var y = 0
if(y < 3){
    return true
} else {
    return false
}
```
### Variable Statement
Variable statements have three required parts, with an optional type expression. These required pieces are the var
keyword, an identifier or variable name, and the expression that being assigned as the value. THe grammar for this 
is shown below. 
```
variable_statement = 'var', IDENTIFIER, 
     [':', type_expression, ] '=', expression
```
Variables can be set to any of six types: int, bool, object, string, list, and null. If the expression does not match
the type set by the type expression, incompatible type errors will be produced. If there is no type expression
declared, Catscript will automatically assign a type to the variable based on the value assigned.
```
var x : int = 10
var y = false
var z = null
```
### Function Definition Statement
Function declarations require the function keyword, a function name, a parameter list in parentheses, an optional \
return type, and the code that the function will run. The grammar structure is shown below. 
```
function_declaration = 'function', IDENTIFIER, '(', parameter_list, ')' + 
                       [ ':' + type_expression ], '{',  { function_body_statement },  '}'                
```
If the function is not given a return type, it will be assigned the void type and no return value will be expected. 
Functions have the option to use return statements to either return a value or to end the function. The examples
below showcase different forms of functions, displaying a version with a set return type and a function with the void return type.
```
function final() : int { return 5 }
function foo(x, y, z) { print(x) }
```
### Print Statement
The print statement is a simple call that requires the print keyword and an expression within parenthesis. 
It does not require any form of type expression, only a valid expression to print out. 
```
print_statement = 'print', '(', expression, ')'
```
The print statement is able to accept any object value. This can range from strings in the first example below 
to variable values in the second example. Print statements will get the value of its inputted expression before
printing. With the second example, true would be printed out instead of x.
```
print("This is a test")

var x = true
print(x)
```
### Return Statement
A core component in functions is the return statement. Return statements require the return keyword and then an
expression that matches the function's return type. If the return is either unlisted or declared as void, an expression
is not required after the return keyword and instead will end the function and return to where it was called. The 
two examples below demonstrate return statements that have a return type or return void.
```
function foo() : object { return 1 }

function foo2() { 
    print(4)
    return
}
```