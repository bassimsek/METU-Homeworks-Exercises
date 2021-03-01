SELECT P.tid, P.amount, P.time
FROM performedOrders P, Account A2,
(SELECT C.cid
FROM ShareOwned S, Account A, Customer C
WHERE S.AccountNo = A.accountNo AND A.cid = C.cid
GROUP BY C.cid
HAVING SUM(S.totalAmount) > 60) D
WHERE P.type = 'BUY' AND A2.accountNo = P.accountNo AND A2.cid = D.cid
ORDER BY P.tid;
