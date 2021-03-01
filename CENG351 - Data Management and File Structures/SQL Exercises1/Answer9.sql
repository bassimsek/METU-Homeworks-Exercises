SELECT S.sname
FROM Sailors S
WHERE NOT EXISTS (
SELECT B2.bid
FROM Boats B2 
EXCEPT 
SELECT R.bid
FROM Reserves R
WHERE R.sid = S.sid)
ORDER BY S.sname;
