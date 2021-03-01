/********************************************************
 * Kernels to be optimized for the Metu Ceng Performance Lab
 ********************************************************/

#include <stdio.h>
#include <stdlib.h>
#include "defs.h"

/* 
 * Please fill in the following team struct 
 */
team_t team = {
    "Team",                     /* Team name */

    "Orçun Başşimşek",             /* First member full name */
    "e2098804",                 /* First member id */

    "Doğancan Akkuyu",                         /* Second member full name (leave blank if none) */
    "e2098705",                         /* Second member id (leave blank if none) */

    "",                         /* Third member full name (leave blank if none) */
    ""                          /* Third member id (leave blank if none) */
};

/****************
 * BOKEH KERNEL *
 ****************/

/****************************************************************
 * Various typedefs and helper functions for the bokeh function
 * You may modify these any way you like.                       
 ****************************************************************/

/* A struct used to compute averaged pixel value */
typedef struct {
    int red;
    int green;
    int blue;
    int num;
} pixel_sum;

/* Compute min and max of two integers, respectively */
static int min(int a, int b) { return (a < b ? a : b); }
static int max(int a, int b) { return (a > b ? a : b); }

/* 
 * initialize_pixel_sum - Initializes all fields of sum to 0 
 */
static void initialize_pixel_sum(pixel_sum *sum) 
{
    sum->red = sum->green = sum->blue = 0;
    sum->num = 0;
    return;
}

/* 
 * accumulate_sum - Accumulates field values of p in corresponding 
 * fields of sum 
 */
static void accumulate_sum(pixel_sum *sum, pixel p) 
{
    sum->red += (int) p.red;
    sum->green += (int) p.green;
    sum->blue += (int) p.blue;
    sum->num++;
    return;
}

/* 
 * assign_sum_to_pixel - Computes averaged pixel value in current_pixel 
 */
static void assign_sum_to_pixel(pixel *current_pixel, pixel_sum sum) 
{
    current_pixel->red = (unsigned short) (sum.red/sum.num);
    current_pixel->green = (unsigned short) (sum.green/sum.num);
    current_pixel->blue = (unsigned short) (sum.blue/sum.num);
    return;
}

/* 
 * avg - Returns averaged pixel value at (i,j) 
 */
static pixel avg(int dim, int i, int j, pixel *src) 
{
    int ii, jj;
    pixel_sum sum;
    pixel current_pixel;

    initialize_pixel_sum(&sum);
    for(ii = max(i-1, 0); ii <= min(i+1, dim-1); ii++) 
    for(jj = max(j-1, 0); jj <= min(j+1, dim-1); jj++) 
        accumulate_sum(&sum, src[RIDX(ii, jj, dim)]);

    assign_sum_to_pixel(&current_pixel, sum);
    return current_pixel;
}

/*******************************************************
 * Your different versions of the bokeh kernel go here 
 *******************************************************/

/* 
 * naive_bokeh - The naive baseline version of bokeh effect with filter
 */
char naive_bokeh_descr[] = "naive_bokeh: Naive baseline bokeh with filter";
void naive_bokeh(int dim, pixel *src, short *flt, pixel *dst) {
  
    int i, j;

    for(i = 0; i < dim; i++) {
        for(j = 0; j < dim; j++) {
            if ( !flt[RIDX(i, j, dim)] )
                dst[RIDX(i, j, dim)] = avg(dim, i, j, src);
            else
                dst[RIDX(i, j, dim)] = src[RIDX(i, j, dim)];
        }
    }
}

/* 
 * bokeh - Your current working version of bokeh
 * IMPORTANT: This is the version you will be graded on
 */
char bokeh_descr[] = "bokeh: Current working version";
void bokeh(int dim, pixel *src, short *flt, pixel *dst) 
{
    int rgbSize = dim*dim;
    
    int *red = malloc(rgbSize * sizeof(int));
    int *green = malloc(rgbSize * sizeof(int));
    int *blue = malloc(rgbSize * sizeof(int));

    int i,j;
    int end = dim-1;


    // Firstly, we need to construct red, green, blue arrays according to neighbour values of specific pixel by using row-major order.


    // first row - first element

    red[0] = src[0].red + src[1].red;
    green[0] = src[0].green + src[1].green; 
    blue[0] = src[0].blue + src[1].blue;



    // first row's middle elements up to last element

    for(i = 1; i< end ; i++) {
        red[i] = src[i-1].red + src[i].red + src[i+1].red;
        green[i] = src[i-1].green + src[i].green + src[i+1].green;
        blue[i] = src[i-1].blue + src[i].blue + src[i+1].blue;
    }


    // first row - last element

    red[end] = src[end-1].red + src[end].red;
    green[end] = src[end-1].green + src[end].green;
    blue[end] = src[end-1].blue + src[end].blue;





    // middle rows

    for(i=1; i<end; i++) {
        // middle rows - first element
        red[i*dim] = src[i*dim].red + src[i*dim+1].red;
        green[i*dim] = src[i*dim].green + src[i*dim+1].green; 
        blue[i*dim] = src[i*dim].blue + src[i*dim+1].blue;

        // middle rows' other elements up to last element
        for(j=1; j<end; j++) {
            red[i*dim+j] = src[i*dim+j-1].red + src[i*dim+j].red + src[i*dim+j+1].red;
            green[i*dim+j] = src[i*dim+j-1].green + src[i*dim+j].green + src[i*dim+j+1].green;
            blue[i*dim+j] = src[i*dim+j-1].blue + src[i*dim+j].blue + src[i*dim+j+1].blue;
        }

        // middle rows- last element
        red[i*dim+end] = src[i*dim+end-1].red + src[i*dim+end].red;
        green[i*dim+end] = src[i*dim+end-1].green + src[i*dim+end].green;
        blue[i*dim+end] = src[i*dim+end-1].blue + src[i*dim+end].blue;
    }




    // last row - first element

    red[dim*end] = src[dim*end].red + src[dim*end+1].red;
    green[dim*end] = src[dim*end].green + src[dim*end+1].green; 
    blue[dim*end] = src[dim*end].blue + src[dim*end+1].blue;



    // last row's middle elements up to last element

    for(i = 1; i< end ; i++) {
        red[dim*end+i] = src[dim*end+i-1].red + src[dim*end+i].red + src[dim*end+i+1].red;
        green[dim*end+i] = src[dim*end+i-1].green + src[dim*end+i].green + src[dim*end+i+1].green;
        blue[dim*end+i] = src[dim*end+i-1].blue + src[dim*end+i].blue + src[dim*end+i+1].blue;
    }


    // last row - last element

    red[dim*dim-1] = src[dim*dim-2].red + src[dim*dim-1].red;
    green[dim*dim-1] = src[dim*dim-2].green + src[dim*dim-1].green;
    blue[dim*dim-1] = src[dim*dim-2].blue + src[dim*dim-1].blue;



    // Secondly, we need to construct dst array according to negihbour pixel's values by using row major-order again.

    // first row - first element
    dst[0].red = (unsigned short) ((red[0] + red[dim])/4);
    dst[0].green = (unsigned short) ((green[0] + green[dim])/4);
    dst[0].blue = (unsigned short) ((blue[0] + blue[dim])/4);


    // first row's middle elements up to last element
    for(i=1; i< end; i++) {
        dst[i].red = (unsigned short) ((red[i] + red[i+dim])/6);
        dst[i].green = (unsigned short) ((green[i] + green[i+dim])/6);
        dst[i].blue = (unsigned short) ((blue[i] + blue[i+dim])/6);
    }


    // first row - last element
    dst[end].red = (unsigned short) ((red[end] + red[end+dim])/4);
    dst[end].green = (unsigned short) ((green[end] + green[end+dim])/4);
    dst[end].blue = (unsigned short) ((blue[end] + blue[end+dim])/4);



    // middle rows
    for(i= 1 ; i<end; i++) {
        // first element of middle rows
        dst[i*dim].red = (unsigned short) ((red[i*dim-dim] + red[i*dim] + red[i*dim+dim])/6);
        dst[i*dim].green = (unsigned short) ((green[i*dim-dim] + green[i*dim] + green[i*dim+dim])/6);
        dst[i*dim].blue = (unsigned short) ((blue[i*dim-dim] + blue[i*dim] + blue[i*dim+dim])/6);


        // middle rows' middle elements from second up to last element
        for(j=1; j<end; j++) {
            dst[i*dim+j].red = (unsigned short) ((red[i*dim+j-dim] + red[i*dim+j] + red[i*dim+j+dim])/9);
            dst[i*dim+j].green = (unsigned short) ((green[i*dim+j-dim] + green[i*dim+j] + green[i*dim+j+dim])/9);
            dst[i*dim+j].blue = (unsigned short) ((blue[i*dim+j-dim] + blue[i*dim+j] + blue[i*dim+j+dim])/9);

        }

        // middle rows' last element
        dst[i*dim+end].red = (unsigned short) ((red[i*dim+end-dim] + red[i*dim+end] + red[i*dim+end+dim])/6);
        dst[i*dim+end].green = (unsigned short) ((green[i*dim+end-dim] + green[i*dim+end] + green[i*dim+end+dim])/6);
        dst[i*dim+end].blue = (unsigned short) ((blue[i*dim+end-dim] + blue[i*dim+end] + blue[i*dim+end+dim])/6);

    }



    // last row - first element
    dst[dim*end].red = (unsigned short) ((red[dim*end-dim] + red[dim*end])/4);
    dst[dim*end].green = (unsigned short) ((green[dim*end-dim] + green[dim*end])/4);
    dst[dim*end].blue = (unsigned short) ((blue[dim*end-dim] + blue[dim*end])/4);

    // last row's middle elements from second up to last element 
    for(i=1; i<end; i++) {
        dst[dim*end+i].red = (unsigned short) ((red[dim*end+i-dim] + red[dim*end+i])/6);
        dst[dim*end+i].green = (unsigned short) ((green[dim*end+i-dim] + green[dim*end+i])/6);
        dst[dim*end+i].blue = (unsigned short) ((blue[dim*end+i-dim] + blue[dim*end+i])/6);
    }


    // last row - last element
    dst[dim*dim-1].red = (unsigned short) ((red[dim*dim-1-dim] + red[dim*dim-1])/4);
    dst[dim*dim-1].green = (unsigned short) ((green[dim*dim-1-dim] + green[dim*dim-1])/4);
    dst[dim*dim-1].blue = (unsigned short) ((blue[dim*dim-1-dim] + blue[dim*dim-1])/4);


    // free pointers
    free(red);
    free(green);
    free(blue);


    // Lastly, check filter condition, If the filter of that pixel is 1, just copy the pixel from source to destination.
    for(i = 0; i < dim; i++) {
        for(j = 0; j < dim; j++) {
            if (flt[RIDX(i, j, dim)] )
                dst[RIDX(i, j, dim)] = src[RIDX(i, j, dim)];
        }
    }  
}




/*********************************************************************
 * register_bokeh_functions - Register all of your different versions
 *     of the bokeh kernel with the driver by calling the
 *     add_bokeh_function() for each test function. When you run the
 *     driver program, it will test and report the performance of each
 *     registered test function.  
 *********************************************************************/

void register_bokeh_functions() 
{
    add_bokeh_function(&naive_bokeh, naive_bokeh_descr);   
    add_bokeh_function(&bokeh, bokeh_descr);
    /* ... Register additional test functions here */
}

/***************************
 * HADAMARD PRODUCT KERNEL *
 ***************************/

/******************************************************
 * Your different versions of the hadamard product functions go here
 ******************************************************/

/* 
 * naive_hadamard - The naive baseline version of hadamard product of two matrices
 */
char naive_hadamard_descr[] = "naive_hadamard The naive baseline version of hadamard product of two matrices";
void naive_hadamard(int dim, int *src1, int *src2, int *dst) {
  
    int i, j;

    for(i = 0; i < dim; i++)
        for(j = 0; j < dim; j++) 
            dst[RIDX(i, j, dim)] = src1[RIDX(i, j, dim)] * src2[RIDX(i, j, dim)];
}

/* 
 * hadamard - Your current working version of hadamard product
 * IMPORTANT: This is the version you will be graded on
 */
char hadamard_descr[] = "hadamard: Current working version";
void hadamard(int dim, int *src1, int *src2, int *dst) 
{
    
    int i, j;

    for(i = 0; i < dim; i++)
        for(j = 0; j < dim; j+=32) {
            dst[RIDX(i, j, dim)] = src1[RIDX(i, j, dim)] * src2[RIDX(i, j, dim)];
            dst[RIDX(i, j+1, dim)] = src1[RIDX(i, j+1, dim)] * src2[RIDX(i, j+1, dim)];
            dst[RIDX(i, j+2, dim)] = src1[RIDX(i, j+2, dim)] * src2[RIDX(i, j+2, dim)];
            dst[RIDX(i, j+3, dim)] = src1[RIDX(i, j+3, dim)] * src2[RIDX(i, j+3, dim)];
            dst[RIDX(i, j+4, dim)] = src1[RIDX(i, j+4, dim)] * src2[RIDX(i, j+4, dim)];
            dst[RIDX(i, j+5, dim)] = src1[RIDX(i, j+5, dim)] * src2[RIDX(i, j+5, dim)];
            dst[RIDX(i, j+6, dim)] = src1[RIDX(i, j+6, dim)] * src2[RIDX(i, j+6, dim)];
            dst[RIDX(i, j+7, dim)] = src1[RIDX(i, j+7, dim)] * src2[RIDX(i, j+7, dim)];
            dst[RIDX(i, j+8, dim)] = src1[RIDX(i, j+8, dim)] * src2[RIDX(i, j+8, dim)];
            dst[RIDX(i, j+9, dim)] = src1[RIDX(i, j+9, dim)] * src2[RIDX(i, j+9, dim)];
            dst[RIDX(i, j+10, dim)] = src1[RIDX(i, j+10, dim)] * src2[RIDX(i, j+10, dim)];
            dst[RIDX(i, j+11, dim)] = src1[RIDX(i, j+11, dim)] * src2[RIDX(i, j+11, dim)];
            dst[RIDX(i, j+12, dim)] = src1[RIDX(i, j+12, dim)] * src2[RIDX(i, j+12, dim)];
            dst[RIDX(i, j+13, dim)] = src1[RIDX(i, j+13, dim)] * src2[RIDX(i, j+13, dim)];
            dst[RIDX(i, j+14, dim)] = src1[RIDX(i, j+14, dim)] * src2[RIDX(i, j+14, dim)];
            dst[RIDX(i, j+15, dim)] = src1[RIDX(i, j+15, dim)] * src2[RIDX(i, j+15, dim)];
            dst[RIDX(i, j+16, dim)] = src1[RIDX(i, j+16, dim)] * src2[RIDX(i, j+16, dim)];
            dst[RIDX(i, j+17, dim)] = src1[RIDX(i, j+17, dim)] * src2[RIDX(i, j+17, dim)];
            dst[RIDX(i, j+18, dim)] = src1[RIDX(i, j+18, dim)] * src2[RIDX(i, j+18, dim)];
            dst[RIDX(i, j+19, dim)] = src1[RIDX(i, j+19, dim)] * src2[RIDX(i, j+19, dim)];
            dst[RIDX(i, j+20, dim)] = src1[RIDX(i, j+20, dim)] * src2[RIDX(i, j+20, dim)];
            dst[RIDX(i, j+21, dim)] = src1[RIDX(i, j+21, dim)] * src2[RIDX(i, j+21, dim)];
            dst[RIDX(i, j+22, dim)] = src1[RIDX(i, j+22, dim)] * src2[RIDX(i, j+22, dim)];
            dst[RIDX(i, j+23, dim)] = src1[RIDX(i, j+23, dim)] * src2[RIDX(i, j+23, dim)];
            dst[RIDX(i, j+24, dim)] = src1[RIDX(i, j+24, dim)] * src2[RIDX(i, j+24, dim)];
            dst[RIDX(i, j+25, dim)] = src1[RIDX(i, j+25, dim)] * src2[RIDX(i, j+25, dim)];
            dst[RIDX(i, j+26, dim)] = src1[RIDX(i, j+26, dim)] * src2[RIDX(i, j+26, dim)];
            dst[RIDX(i, j+27, dim)] = src1[RIDX(i, j+27, dim)] * src2[RIDX(i, j+27, dim)];
            dst[RIDX(i, j+28, dim)] = src1[RIDX(i, j+28, dim)] * src2[RIDX(i, j+28, dim)];
            dst[RIDX(i, j+29, dim)] = src1[RIDX(i, j+29, dim)] * src2[RIDX(i, j+29, dim)];
            dst[RIDX(i, j+30, dim)] = src1[RIDX(i, j+30, dim)] * src2[RIDX(i, j+30, dim)];
            dst[RIDX(i, j+31, dim)] = src1[RIDX(i, j+31, dim)] * src2[RIDX(i, j+31, dim)];
        }
}
/*********************************************************************
 * register_hadamard_functions - Register all of your different versions
 *     of the hadamard kernel with the driver by calling the
 *     add_hadamard_function() for each test function. When you run the
 *     driver program, it will test and report the performance of each
 *     registered test function.  
 *********************************************************************/

void register_hadamard_functions() 
{
    add_hadamard_function(&naive_hadamard, naive_hadamard_descr);   
    add_hadamard_function(&hadamard, hadamard_descr);   
    /* ... Register additional test functions here */
}

