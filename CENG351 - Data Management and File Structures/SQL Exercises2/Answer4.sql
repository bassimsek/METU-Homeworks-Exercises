SELECT P.ProfessorId, P.Age
FROM Professor P
WHERE NOT EXISTS (
SELECT T.CourseId
FROM Professor P1, Teaching T
WHERE P1.Name = 'Waylon Smithers' AND P1.ProfessorId = T.ProfessorId
EXCEPT
SELECT T1.CourseId
FROM Teaching T1
WHERE T1.ProfessorId = P.ProfessorId AND P.Name <> 'Waylon Smithers')
ORDER BY P.Age;
