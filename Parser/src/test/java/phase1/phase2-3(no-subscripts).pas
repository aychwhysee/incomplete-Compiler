program expressionTest (input, output);

 var a, b : integer;
        c : real;
        x : array [0..5] of real;
 begin
   b := a - 4;
   c := ((b + a)/ 2) div (c-b)
 end.
 
{ Output:

CODE
1:  call main, 0
2:  exit
3:  PROCBEGIN main
4:  alloc 21
5:  move 4, _10
6:  sub _1, _10, _9
7:  move _9, _0
8:  add _0, _1, _11
9:  move 2, _15
10:  ltof _15, _12
11:  ltof _11, _13
12:  fdiv _12, _13, _14
13:  ltof _0, _16
14:  fsub _2, _16, _17
15:  ftol _17, _18
16:  div _18, _14, _19
17:  ltof _19, _20
18:  move _20, _2
19:  free 21
20:  PROCEND
}