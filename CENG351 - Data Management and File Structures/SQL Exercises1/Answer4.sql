SELECT DISTINCT S.sname
FROM Sailors S
WHERE S.sid IN (
SELECT DISTINCT R.sid
FROM Reserves R, Boats B
WHERE R.bid = B.bid AND B.color = 'red'
INTERSECT
SELECT DISTINCT R.sid
FROM Reserves R, Boats B
WHERE R.bid = B.bid AND B.color = 'green')
ORDER BY S.sname;
