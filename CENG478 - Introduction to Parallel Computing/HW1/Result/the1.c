#include <mpi.h>
#include <stdio.h>

const int length= 10000000;

double dotProduct(double *vector1, double *vector2, int len, int specifiedRank) {
  int i;
  double result = 0.0;
  for (i=specifiedRank*len; i<((specifiedRank+1)*len); i++) {
    result += vector1[i]*vector2[i];
  }
  return result;
}

double* constructVector1() {
    int i;
    static double vector1[10000000];
    for(i=0; i<length;i++) {
        vector1[i] = 2.0 - (i%20) * 0.1;
    }

    return vector1;
}

double* constructVector2() {
    int i;
    static double vector2[10000000];
    for(i=0; i<length;i++) {
        vector2[i] = 0.1 + (i%20) * 0.1;
    }

    return vector2;
}


int main(int argc, char *argv[]) {
  int rank,numtasks;
  double startTime,endTime;
  double* vector_1;
  double* vector_2;
  double resultOfEachPartition;
  double resultProduct;
 
  MPI_Init(&argc, &argv);
  startTime = MPI_Wtime();
  MPI_Comm_size(MPI_COMM_WORLD, &numtasks);
  MPI_Comm_rank(MPI_COMM_WORLD, &rank);
  
  vector_1 = constructVector1();
  vector_2 = constructVector2();
  int partitionForEachProcessor = length / numtasks;


  resultOfEachPartition = dotProduct(vector_1,vector_2,partitionForEachProcessor,rank);

  MPI_Reduce(&resultOfEachPartition, &resultProduct, 1, MPI_DOUBLE, MPI_SUM, 0, MPI_COMM_WORLD);


  endTime= MPI_Wtime();
  MPI_Finalize();
  if (rank == 0) {
    printf("Result = %f\n", resultProduct);
    printf("Time Consumed = %f\n",endTime-startTime);
  }


  return 0;
}
