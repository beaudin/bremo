package matLib.filter;


import java.util.LinkedList;
import java.util.List;

import bremo.parameter.CasePara;

import Jama.LUDecomposition;
import Jama.Matrix;



/** Class implements the Savitzy-Golay filter.  The algorithm is based on
 * the algorithm used in 
 * 
 * Numerical Methods in C: The art of scientific computing second edition
 * 
 * This filter is used for smoothing data.  The current implementation is
 * a 1-d approach 
 * 
 **/ 
public class SavitzkyGolayFilter{
    double[] coefficients;
    int LEFT;
	int RIGHT;
	int ORDER;
    /**
     * Prepares the filter coefficients
     * 
     * @param left number of places to the left of the origion
     * @param right number of places to the right of the origion
     * @param order of polynomial used to fit data
     * */
    public SavitzkyGolayFilter(int left, int right, int order){
        
        coefficients = SavitzkyGolayCoefficients(left,right,order);
        LEFT = left;
        RIGHT = right;
        ORDER = order;
    }   

    /**
     * Prepares the filter coefficients
     *
     * @param left number of places to the left of the origion
     * @param right number of places to the right of the origion
     * @param order of polynomial used to fit data
     * @param derivative use a SavitzkyGolayFilter to calculate the derivative or order.
     * */
    public SavitzkyGolayFilter(int left, int right, int order, int derivative){

        coefficients = SavitzkyGolayCoefficients(left,right,order, derivative);
        LEFT = left;
        RIGHT = right;
        ORDER = order;
    }
    
    public List<Double> filterData(List<Double> signal){ 
    	
    	 List <Double> Tmp= new LinkedList<Double>();
    	 
    	 Tmp.addAll(signal);
    	 
    	 Double [] tmp = new Double [signal.size()];
		  
		  Tmp.toArray(tmp);
		  
		  tmp = filterData(tmp); 
		  
		  Tmp.clear();
		  
		  for (int i = 0 ;i < tmp.length-1;i++) {
		  
			  Tmp.add(tmp[i]);
		  }
		  
		  return Tmp ;
    	
    }
    
    /**
     * filters the data by assuming the ends are reflected.
     * 
     * @param tmp the array that is going to be filtered.
     * */
    public double[] filterData(double[] tmp){
        int pts = tmp.length;
        Double[] input_value = new Double[pts];
        
    	for(int i=0;i<pts;i++){
    		input_value[i]=tmp[i];
    	}
    	
    	 Double[] ret_value_tmp=this.filterData(input_value);
    	 
    	 double [] ret_value=new double [pts]; 
    	 
    	 for(int i=0;i<pts;i++){
    		 ret_value[i]=ret_value_tmp[i];
     	}    	 
    	  return ret_value;
    }    
    

    /**
     * filters the data by assuming the ends are reflected.
     * 
     * @param tmp the array that is going to be filtered.
     * */
    public Double[] filterData(Double[] tmp){
        int pts = tmp.length;
        Double[] ret_value = new Double[pts];
        
        int j;
        //reflected
        for(j = 0; j<LEFT; j++)
            ret_value[j] = rFilter(tmp, coefficients, j, LEFT);
        
        
        //normal
        for(j = LEFT; j<pts - RIGHT; j++)
            ret_value[j] = filter(tmp, coefficients, j, LEFT);
            
        
        //reflected
        for(j = pts - RIGHT; j<pts; j++)
            ret_value[j] = rFilter(tmp, coefficients, j, LEFT);
        
        return ret_value;
    }
    
    /**
     *  Calculates one point in the data, similar to one step of a
     *  convolution. 
     * 
     * @param data - data that will be convolved
     * @param mask - kernel
     * @param start - position of the 'middle point' on the data
     * @param middle - postion of the center of the kernel on the kernel
     * 
     * */
    public double filter(Double[] data, double[] mask, int start, int middle){
        double out = 0;
        for(int i = 0; i<mask.length; i++)
            out += mask[i]*data[start - middle + i];
            
        return out;
        
    }
    
    
    
    /**
     *  Calculates one point in the data, similar to one step of a
     *  convolution.  This one will wrap the ends of the mask.  Something
     *  like a convolution with reflected boundary conditions.
     * 
     * @param tmp - data that will be convolved
     * @param mask - kernel
     * @param start - position of the 'middle point' on the data
     * @param middle - postion of the center of the kernel on the kernel
     * 
     * */
    public double rFilter(Double[] tmp, double[] mask, int start, int middle){
        double out = 0;
        int dex;
        for(int i = 0; i<mask.length; i++){
            dex = Math.abs(start - middle + 1 +i);
            dex = (dex<tmp.length)?dex: 2*tmp.length - dex - 1;
            out += mask[i]*tmp[dex];
        }
            
        return out;
        
    }
    /**
     * 
     *   Creates the array of coefficients for using a least squares polynomial fit
     *   of the data.  Due to the fact the points are equally spaced 
     *   and the polynomial least squares is linear in the coefficients,
     *   a set of coefficients can be used for every element.
     * 
     *   @param nl - spaces to the left
     *   @param nr - spaces to the right
     *   @param order - the order of the polynomial being used, not that nl+nr+1 must be more than the order of the polynomial
     *   @return a 'time kernel' for convolving in time.
     * */
    
    
    public static double[] SavitzkyGolayCoefficients(int nl, int nr, int order){
        if( nl + nr + 1<= order)
            throw new java.lang.IllegalArgumentException(" The order of polynomial cannot exceed the number of points being used." +
                                                         "If they are equal the input equals the output.");
        int N = nr + nl + 1;
        double[] ret_values = new double[N];
        double[] xvalues = new double[N];
        for(int i = 0; i<N; i++){
            
            xvalues[i] = -nl + i;
            
        }
        
        int counts = 2*order+1;
        double[] moments = new double[counts];
        for(int i = 0; i<counts; i++){
            for(int j = 0; j<N; j++){
                
                moments[i] += Math.pow(xvalues[j],i);
                
            }
            
            moments[i] = moments[i]/N;
        }
        
        
        
        double[][] matrix = new double[order+1][order+1];
        
        for(int i = 0; i<order+1; i++){
            for(int j = 0; j<order+1; j++){
                matrix[i][j] = moments[counts - i - j - 1];
            }
           // System.out.println("");
        }
        
        Matrix A = new Matrix(matrix);
       
        LUDecomposition lu = A.lu();
       
        Matrix x = new Matrix(new double[order+1],order+1);
        Matrix y;
        double[] polynomial;
       
        for(int i = 0; i<N; i++){
            
            for(int j = 0; j<order+1; j++)
                x.set( j , 0, Math.pow(xvalues[i],order - j));
            
            y = lu.solve(x);
            
            polynomial = y.getColumnPackedCopy();
            ret_values[i] = evaluatePolynomial(polynomial, xvalues[nl])/N;
        }
        
        
        return ret_values;
        
    }

    /**
     *
     *   Creates the array of coefficients for using a least squares polynomial fit
     *   of the data.  Due to the fact the points are equally spaced
     *   and the polynomial least squares is linear in the coefficients,
     *   a set of coefficients can be used for every element.
     *
     *   @param nl - spaces to the left
     *   @param nr - spaces to the right
     *   @param order - the order of the polynomial being used, not that nl+nr+1 must be more than the order of the polynomial
     *   @param derivative - returns a set of derivative coefficients
     * */


    public static double[] SavitzkyGolayCoefficients(int nl, int nr, int order, int derivative){
        if( nl + nr + 1<= order)
            throw new java.lang.IllegalArgumentException(" The order of polynomial cannot exceed the number of points being used." +
                                                         "If they are equal the input equals the output.");
        int N = nr + nl + 1;
        double[] ret_values = new double[N];
        double[] xvalues = new double[N];
        for(int i = 0; i<N; i++){

            xvalues[i] = -nl + i;

        }

        int counts = 2*order+1;
        double[] moments = new double[counts];
        for(int i = 0; i<counts; i++){
            for(int j = 0; j<N; j++){

                moments[i] += Math.pow(xvalues[j],i);

            }

            moments[i] = moments[i]/N;
        }



        double[][] matrix = new double[order+1][order+1];

        for(int i = 0; i<order+1; i++){
            for(int j = 0; j<order+1; j++){
                matrix[i][j] = moments[counts - i - j - 1];
            }
            //System.out.println("");
        }

        Matrix A = new Matrix(matrix);

        LUDecomposition lu = A.lu();

        Matrix x = new Matrix(new double[order+1],order+1);
        Matrix y;
        double[] polynomial;

        for(int i = 0; i<N; i++){

            for(int j = 0; j<order+1; j++)
                x.set( j , 0, Math.pow(xvalues[i],order - j));

            y = lu.solve(x);

            polynomial = y.getColumnPackedCopy();
            ret_values[i] = evaluatePolynomial(polynomial, xvalues[nl], derivative)/N;
        }


        return ret_values;

    }
    
    
    /**
     *      Evaluates a polynomial where:
     *      p(x) = poly[0] * x**m + poly[1] * x**(m-1) + ... + poly[m]
     *      
     *  @param poly - double array representation of a polynomial
     *  @param x - the variable that will be evaluated. 
     **/
    public static double evaluatePolynomial(double[] poly, double x){
        
        double val = 0;
        int m = poly.length;
        for(int j = 0; j<m; j++){
            val += Math.pow(x,m-j-1)*poly[j];
        }
        return val;
    }

    /**
     *      Evaluates derivative of polynomial where with:
     *      p(x) = poly[0] * x**m + poly[1] * x**(m-1) + ... + poly[m]
     *
     *  @param poly - double array representation of a polynomial
     *  @param x - the variable that will be evaluated.
     *  @param order - the order of the derivative. (n in description)
     *  @return m*(m-1)*...(m - n)*poly[0]*x**(m-n) + (m-1)*...(m - n - 1)*poly[1] * x**(m-2) ... + n!*poly[m - n]
     **/
    public static double evaluatePolynomial(double[] poly, double x, int order){

        double val = 0;
        int m = poly.length;
        for(int j = 0; j<m-order; j++){
            int pow = m - j - 1; //original power before derivatives
            val += Math.pow(x,pow - order)*poly[j]*pfact(pow,order);
        }
        return val;
    }

    /**
     * Partial factorial,
     * @param m power of x
     * @param n number of derivatives
     *
     * @return m*(m-1)*...(m - n)
     */
    public static double pfact(int m, int n){
        int p = 1;
        for(int i = 0; i<n; i++){
            p*=(m - i);
        }
        return p;
    }
}