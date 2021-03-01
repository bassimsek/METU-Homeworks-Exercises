SELECT B.bid ,COUNT(*) AS count
FROM Boats B, Reserves R
WHERE B.bid = R.bid AND B.color='red'
GROUP BY B.bid
ORDER BY B.bid, count;
