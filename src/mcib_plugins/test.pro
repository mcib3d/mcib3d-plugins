
/*
* Prolog Source File ${name}
*   created ${date} at ${time}
*   created by ${user}
*/

% fibonacci(0) = 0
% fibonacci(1) = 1
% fibonacci(2) = 1
% fibonacci(n) = fibonacci(n-1) + fibonacci(n-2)

% test chaine 3 objets

EC(a,b).
EC(b,c).
EC(b,a).
EC(c,b).
DR(a,c).
DR(b,c).

chaine(A,B,C):-
	EC(A,B),
	EC(B,C),
	DR(A,C).

	
