SELECT *
FROM Sailors S
WHERE S.rating > (
SELECT MAX(S2.rating)
FROM Sailors S2
WHERE S2.sname = 'Horatio')
ORDER BY S.sid, S.sname, S.rating, S.age;
