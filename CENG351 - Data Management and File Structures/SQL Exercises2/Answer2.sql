UPDATE Transcript
SET Score = Score + Score/10
WHERE StudentId IN (
SELECT S.StudentId
FROM Student S
WHERE S.Status = 'Senior');
