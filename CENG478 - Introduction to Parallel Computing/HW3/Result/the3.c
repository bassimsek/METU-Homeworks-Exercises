#define matrixDimension 3200
#include <mpi.h>
#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <math.h>



int main(int argc, char *argv[]) {

	int rank,numtasks;
	int i, j, k;
	double frobeniusResult;
	double frobeniusResultOfEachPartition = 0;

	double (*matrixA)[matrixDimension] = malloc(sizeof(double[matrixDimension][matrixDimension]));

	double (*matrixB)[matrixDimension] = malloc(sizeof(double[matrixDimension][matrixDimension]));
    //double (*resultMatrix)[matrixDimension] = malloc(sizeof(double[matrixDimension][matrixDimension]));


	for (i = 0; i<matrixDimension; i++) {
		for (j = 0 ;j<matrixDimension; j++) {
			matrixA[i][j] = ((double)((i+1)+1))/((j+1)+1);
			matrixB[i][j] = (i+1)+1-(((double)((j+1)+1))/3200);
		}
	}


	MPI_Init(NULL, NULL);
	double startTime, endTime, times,largest_time;
	startTime = MPI_Wtime();
	MPI_Comm_size(MPI_COMM_WORLD, &numtasks);
	MPI_Comm_rank(MPI_COMM_WORLD, &rank);

	int intervalForEachProcessor = matrixDimension / numtasks;

	double (*resultOfEachPartition)[matrixDimension] = malloc(sizeof(double[intervalForEachProcessor][matrixDimension]));

	
	for (i=0;i<intervalForEachProcessor;i++) {
		for(j=0;j<matrixDimension;j++) {		
			resultOfEachPartition[i][j]=0;
			for(k=0;k<matrixDimension;k++) {
				resultOfEachPartition[i][j] += matrixA[i+(rank*intervalForEachProcessor)][k] * matrixB[k][j];
			}
		}	
	}


	for(i=0;i<intervalForEachProcessor;i++) {
		for(j=0;j<matrixDimension;j++) {
			frobeniusResultOfEachPartition += (resultOfEachPartition[i][j]*resultOfEachPartition[i][j]);
		}
	}

	MPI_Reduce(&frobeniusResultOfEachPartition, &frobeniusResult, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);

	//MPI_Gather(resultOfEachPartition, matrixDimension*matrixDimension/numtasks, MPI_DOUBLE, resultMatrix, matrixDimension*matrixDimension/numtasks, MPI_DOUBLE, 0, MPI_COMM_WORLD);



	endTime= MPI_Wtime();
	times = endTime-startTime;
	MPI_Reduce(&times, &largest_time, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);

	if(rank == 0) {
		frobeniusResult = sqrt(frobeniusResult);
		printf("Time Consumed = %lf\n",largest_time);
	    printf("Frobenius Norm of result: %lf\n",frobeniusResult);
	   /* printf("Resulting Matrix: \n");
	    for (i = 0; i < matrixDimension; i++) {
	        for (j = 0; j < matrixDimension; j++) {
                printf(" %lf", resultMatrix[i][j]);
	        }
	        printf ("\n");
		}*/
	}

	free(matrixA);
	free(matrixB);
	//free(resultMatrix);
	free(resultOfEachPartition);

	MPI_Finalize();



  return 0;
}
