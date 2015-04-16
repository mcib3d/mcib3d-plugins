ec(a,b).
ec(b,c).
ec(b,a).
ec(c,b).
ec(b,c).
ec(c,d).
ec(d,c).
dr(a,c).
dr(c,a).
dr(b,d).
dr(d,b).
dr(a,d).
dr(d,a).

chaine_ec(A,B,C):-
	ec(A,B),
	ec(B,C),
	dr(A,C).
		
chaine_po(A,B,C):-
	po(A,B),
	po(B,C),
	dr(A,C).

		
		

		
	
