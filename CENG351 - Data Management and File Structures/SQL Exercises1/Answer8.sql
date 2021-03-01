SELECT *
FROM Sailors S
WHERE S.rating = (SELECT MAX(S2.rating)
FROM Sailors S2)
ORDER BY S.sname, S.rating, S.age;
