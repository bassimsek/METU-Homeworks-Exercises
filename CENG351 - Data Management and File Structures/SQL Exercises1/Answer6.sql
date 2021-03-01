SELECT *
FROM Sailors S1
WHERE EXISTS (
SELECT *
FROM Sailors S2
WHERE S1.rating > S2.rating AND S2.sname = 'Horatio')
ORDER BY S1.sid, S1.sname, S1.rating, S1.age;
