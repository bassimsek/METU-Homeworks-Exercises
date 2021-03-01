SELECT S.StudentId, COUNT(T.CourseId)
FROM Student S, Transcript T
WHERE S.StudentId = T.StudentId AND S.Address LIKE '%Springfield%'
GROUP BY S.StudentId
ORDER BY S.StudentId;
