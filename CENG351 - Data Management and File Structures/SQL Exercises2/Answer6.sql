SELECT C1.CourseName
FROM Course C1
WHERE C1.CourseId IN (

SELECT C.CourseId
FROM Course C, Transcript T
WHERE C.CourseId = T.CourseId
GROUP BY C.CourseId
HAVING AVG(T.Score) > (
SELECT AVG(T1.Score)
FROM Transcript T1
WHERE T1.CourseId = 'ME202'))
ORDER BY C1.CourseName;
