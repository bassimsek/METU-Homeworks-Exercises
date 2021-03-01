DELETE FROM Course
WHERE CourseId NOT IN (
SELECT T.CourseId
FROM Transcript T);