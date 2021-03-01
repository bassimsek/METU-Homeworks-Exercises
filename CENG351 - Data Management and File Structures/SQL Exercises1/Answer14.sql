SELECT S.rating, AVG(S.age) as avgage
FROM Sailors S
GROUP BY S.rating
HAVING avgage = (
SELECT MIN(avgage2)
FROM (
SELECT S2.rating , AVG(S2.age) as avgage2
FROM Sailors S2
GROUP BY S2.rating))
ORDER BY S.rating, avgage;
