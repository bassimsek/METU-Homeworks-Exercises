SELECT C.CourseName, A.AgeWideness
FROM Course C, 
(SELECT T.CourseId, MAX(S.Age) - MIN(S.Age) AS AgeWideness
FROM Transcript T, Student S
WHERE T.StudentId = S.StudentId
GROUP BY T.CourseId) A
WHERE A.CourseId = C.CourseId
ORDER BY C.CourseName;
