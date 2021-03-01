#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <mpi.h>


void quicksort(double a[], int n) {
    if (n <= 1) return;
    double p = a[n/2];
    double *b, *c;
    b=(double*) calloc(n,sizeof(double));
    c=(double*) calloc(n,sizeof(double));
    int i, j = 0, k = 0;
    for (i=0; i < n; i++) {
        if (i == n/2) continue;
        if ( a[i] <= p) b[j++] = a[i];
        else            c[k++] = a[i];
    }
    quicksort(b,j);
    quicksort(c,k);
    for (i=0; i<j; i++) a[i] =b[i];
    a[j] = p;
    for (i= 0; i<k; i++) a[j+1+i] =c[i]; 
}


void getSmallerOnes(double arr[], double temp1[], double temp2[], int n) {
    int i=0;
    int j=0;
    int k=0;
    while (i <n && j<n && k<n) {
        if (arr[i] > temp1[j]) {
            temp2[k] = temp1[j];
            j++;
        } else if (arr[i] < temp1[j]) {
            temp2[k] = arr[i];
            i++;
        } else if (arr[i] == temp1[j]) {
            temp2[k] = arr[i];
            if ((k+1)<n) {
                k++;
                temp2[k] = arr[i];
            }
            i++;
            j++;
        }
        k++;
    }

    for(i =0;i<n;i++) {
        arr[i] = temp2[i]; 
    }

}

void getBiggerOnes(double arr[], double temp1[], double temp2[], int n) {
    int i=n-1;
    int j=n-1;
    int k=n-1;
    while (i >=0 && j>=0 && k>=0) {
        if (arr[i] > temp1[j]) {
            temp2[k] = arr[i];
            i--;
        } else if (arr[i] < temp1[j]) {
            temp2[k] = temp1[j];
            j--;
        } else if (arr[i] == temp1[j]) {
            temp2[k] = arr[i];
            if ((k-1)>=0) {
                k--;
                temp2[k] = arr[i];
            }
            i--;
            j--;
        }
        k--;
    }

    for(i =0;i<n;i++) {
        arr[i] = temp2[i]; 
    }

}



void evenOddSort(double arr[], int n, int currentRank, int processNumber, MPI_Comm comm) {
   MPI_Status status;
   int i;
   double *temp1, *temp2;
   int evenPair,oddPair;   

   
   temp1 = (double*) calloc(n,sizeof(double));
   temp2 = (double*) calloc(n,sizeof(double));

   
   if (currentRank % 2 == 0) {  
      oddPair = currentRank-1;
      evenPair = currentRank + 1;
      if (evenPair == processNumber) {
          evenPair = MPI_PROC_NULL; 
      }  
   } else {  
      oddPair = currentRank + 1;
      if (oddPair == processNumber) {
          oddPair = MPI_PROC_NULL; 
      }                 
      evenPair = currentRank - 1;
   }

   quicksort(arr,n);
    
	
   for (i = 0; i < processNumber; i++) {
        if (i %2 == 0) {
            if (evenPair >= 0) {
                MPI_Sendrecv(arr, n, MPI_DOUBLE, evenPair, 0, temp1, n, MPI_DOUBLE, evenPair, 0, comm, &status);
                if (currentRank %2 == 1) {
                    getBiggerOnes(arr,temp1,temp2,n);
                } else {
                    getSmallerOnes(arr,temp1,temp2,n);
                }
            }
        } else {
            if (oddPair >= 0) {
                MPI_Sendrecv(arr, n, MPI_DOUBLE, oddPair, 0, temp1, n, MPI_DOUBLE, oddPair, 0, comm, &status);
                if (currentRank %2 == 1) {
                    getSmallerOnes(arr,temp1,temp2,n);
                } else {
                    getBiggerOnes(arr,temp1,temp2,n);
                }
            }
        }
   }

  free(temp1);
  free(temp2);
}


int main() {

    FILE *fp;
    fp=fopen("input.txt","r");
    int size, min, max;
    double *arr;
    double *total_list;
    fscanf(fp,"%d",&size);
    fscanf(fp,"%d %d",&min, &max);

    arr=(double*) calloc(size,sizeof(double));
    int i=0;
    int j=0;
    for(;i<size;i++) fscanf(fp,"%lf",&arr[i]);
    total_list = (double*) calloc(size,sizeof(double));


    // Initialize the MPI environment
    MPI_Init(NULL, NULL);

    // Get the number of processes
    int world_size;
    MPI_Comm_size(MPI_COMM_WORLD, &world_size);

    // Get the rank of the process
    int world_rank;
    MPI_Comm_rank(MPI_COMM_WORLD, &world_rank);

    // Get the name of the processor
    char processor_name[MPI_MAX_PROCESSOR_NAME];
    int name_len;
    MPI_Get_processor_name(processor_name, &name_len);

    double start, end, times,largest_time;

	
	start=MPI_Wtime();

    int noOfElementsInEachProcessor = size/world_size;
    double *listOfDoublesForEachProcessor;
    listOfDoublesForEachProcessor = (double*) calloc(noOfElementsInEachProcessor,sizeof(double));

	i=0;
    for(j=world_rank*noOfElementsInEachProcessor;j<((world_rank+1)*noOfElementsInEachProcessor);j++) {
        listOfDoublesForEachProcessor[i] = arr[j];
		i++;
    }
    
	evenOddSort(listOfDoublesForEachProcessor,noOfElementsInEachProcessor,world_rank,world_size,MPI_COMM_WORLD);

    MPI_Gather(listOfDoublesForEachProcessor,noOfElementsInEachProcessor, MPI_DOUBLE, total_list, noOfElementsInEachProcessor, MPI_DOUBLE, 0, MPI_COMM_WORLD); 
	end=MPI_Wtime();
	times = end-start;
	MPI_Reduce(&times, &largest_time, 1, MPI_DOUBLE, MPI_MAX, 0, MPI_COMM_WORLD);

    if(world_rank == 0) {
        printf("Parallel odd-even transposition: %lf\n",largest_time);
        for(i=0;i<size;i++) printf("%lf\n",total_list[i]);
        free(total_list);
    }
    free(listOfDoublesForEachProcessor);
    free(arr);
    fclose(fp);

	
    MPI_Finalize();

    
    
	return 0;
}










