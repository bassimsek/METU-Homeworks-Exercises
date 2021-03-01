SELECT A.accountNo
FROM Account A
WHERE NOT EXISTS (
SELECT S.shareID
FROM Share S
WHERE S.priceSell > 10
EXCEPT
SELECT S1.shareID
FROM ShareOwned S1
WHERE S1.accountNo = A.accountNo)
ORDER BY A.accountNo;
