SELECT DISTINCT S.sname
FROM Sailors S, Reserves R
WHERE S.sid = R.sid AND R.bid=103
ORDER BY S.sname;
