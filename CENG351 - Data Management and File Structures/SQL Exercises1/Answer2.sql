SELECT DISTINCT R.sid
FROM Reserves R, Boats B
WHERE R.bid = B.bid AND B.color = 'red'
UNION
SELECT DISTINCT R.sid
FROM Reserves R, Boats B
WHERE R.bid = B.bid AND B.color = 'green'
ORDER BY R.sid;
