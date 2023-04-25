package edu.montana.csci.csci468.demo;

import edu.montana.csci.csci468.CatscriptTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CatscriptExecutionPartnerTest extends CatscriptTestBase {

    @Test
    void NestedIfStatement() {
        String program = "var x = %d\n" +
                "var y = %d\n" +
                "if (x == 1) {\n" +
                    "if (y == 2) {\n" +
                        "print(\"x is one and y is two\")" +
                    "}\n" +
                    "else {\n" +
                        "print(\"x is one and y is not two\")" +
                    "}\n" +
                "}\n" +
                "else {\n" +
                    "if (y == 2) {\n" +
                        "print(\"x is not one and y is two\")" +
                    "}\n" +
                    "else {\n" +
                        "print(\"x is not one and y is not two\")" +
                    "}\n" +
                "}\n";

        assertEquals("x is one and y is two\n", executeProgram(String.format(program, 1, 2)));
        assertEquals("x is one and y is not two\n", executeProgram(String.format(program, 1, 1)));
        assertEquals("x is not one and y is two\n", executeProgram(String.format(program, 2, 2)));
        assertEquals("x is not one and y is not two\n", executeProgram(String.format(program, 2, 1)));
    }

    @Test
    void NestedForLoop() {
        String program = "for (i in [1, 2, 3]) {\n" +
                "for (j in [4, 5, 6]) {\n" +
                "print(i * j)\n" +
                "}\n" +
                "}\n";
        assertEquals("4\n5\n6\n8\n10\n12\n12\n15\n18\n", executeProgram(program));
    }


    @Test
    void fibonacciFunction() {
        /*
        function fib(n : int): int {
            if (n < 0) {
                print("Invalid Input")
                return 0
            }
            if (n == 0) {
                return 0
            }
            if (n == 1) {
                return 1
            }
            if (n == 2) {
                return 1
            }

            return fib(n-1) + fib(n-2)
        }
        print(fib(10))
         */
        String program = "function fib(n : int): int {\n" +
                "\tif (n < 0) {\n" +
                "\t\tprint(\"Invalid Input, returning 0\")\n" +
                "\t\treturn 0\n" +
                "\t}\n" +
                "\tif (n == 0) {\n" +
                "\t\treturn 0\n" +
                "\t}\n" +
                "\tif (n == 1) {\n" +
                "\t\treturn 1\n" +
                "\t}\n" +
                "\tif (n == 2) {\n" +
                "\t\treturn 1\n" +
                "\t}\n" +
                "\t\n" +
                "\treturn fib(n-1) + fib(n-2)\n" +
                "}\n" +
                "print(fib(%d))";
        //assertEquals("0\n", compile(String.format(program, 0)));
        //assertEquals("1\n", compile(String.format(program, 1)));
        //assertEquals("1\n", compile(String.format(program, 2)));
        //assertEquals("2\n", compile(String.format(program, 3)));
        //assertEquals("3\n", compile(String.format(program, 4)));
    }

    @Test
    void VariableAssignment(){
        String program = "var x = 0\n" +
                "for (i in [1,2,3]){\n" +
                "x = x + i\n" +
                "print(x)\n" +
                "}\n";
        assertEquals("1\n3\n6\n", executeProgram(program));
    }

    @Test
    void CascadeReturnTest(){
        String program = "function final() : int{return 5}\n" +
                "function fun() : int{return final()}\n" +
                "function foo() : int{return fun()}\n" +
                "print(foo())";
        assertEquals("5\n", executeProgram(program));
    }
    @Test
    void FunctionVarAssignment(){
        String program = "var x = %d\n" +
                "function run(){print(\"pass\")}\n" +
                "function dont(){print(\"fail\")}\n"+
                "function fun(y : int){if(y < 3){run()} else{dont()}}\n" +
                "fun(x)";

        assertEquals("pass\n", executeProgram(String.format(program, 2)));
        assertEquals("fail\n", executeProgram(String.format(program, 5)));
    }

}
