program expressionTest (input, output);
 var a, b : integer;
        c : real;
        x : array [0..5] of real;
 begin
   b := a * 4;
   c := (b + a)/ 2
    
 end.
 
{Output :
 
CODE
1:  call main, 0
2:  exit
3:  PROCBEGIN main
4:  alloc 16
5:  move 4, _10
6:  mul _10, _1, _9
7:  move _9, _0
8:  add _0, _1, _11
9:  move 2, _15
10:  ltof _15, _12
11:  ltof _11, _13
12:  fdiv _12, _13, _14
13:  move _14, _2
14:  free 16
15:  PROCEND
}