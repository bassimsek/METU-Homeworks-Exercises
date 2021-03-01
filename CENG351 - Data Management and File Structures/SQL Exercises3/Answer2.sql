SELECT A.cid, SUM(S.totalAmount)
FROM Account A, ShareOwned S ,Customer C
WHERE A.accountNo = S.accountNo AND C.cid = A.cid AND C.name LIKE '%t' 
GROUP BY A.cid
ORDER BY A.cid;
