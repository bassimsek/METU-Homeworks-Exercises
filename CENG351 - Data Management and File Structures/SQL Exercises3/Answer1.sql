DELETE FROM performedOrders
WHERE accountNo IN (
SELECT A.accountNo
FROM Account A, performedOrders P
WHERE A.accountNo = P.accountNo AND A.cid = 10002);
