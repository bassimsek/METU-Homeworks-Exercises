SELECT S1.sname, S1.age
FROM Sailors S1
WHERE S1.age >= (
SELECT MAX(S2.age)
FROM Sailors S2)
ORDER BY S1.sname, S1.age;
