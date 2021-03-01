SELECT DISTINCT C.cid
FROM Customer C, Account A, performedOrders P
WHERE C.cid = A.cid AND A.accountNo = P.accountNo AND P.type = 'BUY' AND P.time < '2019-11-07'
EXCEPT
SELECT DISTINCT C.cid
FROM Customer C, Account A, performedOrders P
WHERE C.cid = A.cid AND A.accountNo = P.accountNo AND P.type = 'SELL' AND P.time < '2019-11-07'
ORDER BY C.cid;
