package matLib;

/*
*   Interface DerivFunction
*
*   This class provides the interface by which a
*   single ODE may be coded and  supplied to the methods
*   in the class RungeKutta
*
*   WRITTEN BY: Dr Michael Thomas Flanagan
*
*   DATE:	 February 2002
*   UPDATE:  22 June 2003
*
*   DOCUMENTATION:
*   See Michael Thomas Flanagan's Java library on-line web page:
*   http://www.ee.ucl.ac.uk/~mflanaga/java/RungeKutta.html
*   http://www.ee.ucl.ac.uk/~mflanaga/java/
*
*   Copyright (c) April 2004
*
*   PERMISSION TO COPY:
*   Permission to use, copy and modify this software and its documentation for
*   NON-COMMERCIAL purposes is granted, without fee, provided that an acknowledgement
*   to the author, Michael Thomas Flanagan at www.ee.ucl.ac.uk/~mflanaga, appears in all copies.
*
*   Dr Michael Thomas Flanagan makes no representations about the suitability
*   or fitness of the software for any or for a particular purpose.
*   Michael Thomas Flanagan shall not be liable for any damages suffered
*   as a result of using, modifying or distributing this software or its derivatives.
*
***************************************************************************************/
// Interface for RungeKutta class (single ODE)
public interface DglFunction{
        double deriv(double x, double y);
}
