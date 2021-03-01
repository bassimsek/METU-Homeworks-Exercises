SELECT DISTINCT S.sid
FROM Sailors S, Reserves R
WHERE S.sid = R.sid
ORDER BY s.sid;
