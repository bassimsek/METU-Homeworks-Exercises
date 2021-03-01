SELECT DISTINCT A.accountNo
FROM Account A,
(SELECT S.accountNo
FROM ShareOwned S, Share S1
WHERE  S.shareID = S1.shareID AND S1.priceBuy >10
GROUP BY S.accountNo
HAVING 1 < COUNT(*)) F
WHERE A.accountNo = F.accountNo
ORDER BY A.accountNo;
