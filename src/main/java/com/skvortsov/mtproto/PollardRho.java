package com.skvortsov.mtproto;

/*************************************************************************
 *  Compilation:  javac PollardRho.java
 *  Execution:    java PollardRho N
 *
 *  Factor N using the Pollard-Rho method.
 *
 *  % java PollardRho 44343535354351600000003434353
 *  149
 *  329569479697
 *  903019357561501
 *
 * http://introcs.cs.princeton.edu/java/78crypto/PollardRho.java.html
 *
 *************************************************************************/

//import android.util.Log;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

public class PollardRho {
    private final static BigInteger ZERO = new BigInteger("0");
    private final static BigInteger ONE  = new BigInteger("1");
    private final static BigInteger TWO  = new BigInteger("2");
    private final static SecureRandom random = new SecureRandom();
    private final static String PR = "PollardRho";
    private static SortedMap<BigInteger, Integer> factors = new TreeMap<BigInteger, Integer>();

    static BigInteger rho(BigInteger N) {
        BigInteger divisor;
        BigInteger c  = new BigInteger(N.bitLength(), random);
        BigInteger x  = new BigInteger(N.bitLength(), random);
        BigInteger xx = x;

        // check divisibility by 2
        if (N.mod(TWO).compareTo(ZERO) == 0) return TWO;

        do {
            x  =  x.multiply(x).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            xx = xx.multiply(xx).mod(N).add(c).mod(N);
            divisor = x.subtract(xx).gcd(N);
        } while((divisor.compareTo(ONE)) == 0);

        return divisor;
    }

    public void factor(BigInteger N) {
        if (N.compareTo(ONE) == 0) return;
//        Log.i("AsyncTask", "doInBackground: if (N.compareTo(ONE) == 0) return;");
        if (N.isProbablePrime(20)) {
//            Log.d(PR, N.toString());
            Integer value = factors.get(N);
            if (value == null) {
                factors.put(N, 1);
            } else {
                factors.put(N,  value+1);
            }
            return;
        }
        BigInteger divisor = rho(N);
        factor(divisor);
        factor(N.divide(divisor));
    }

    public void clear()
    {
        factors.clear();
    }

    public String getfactors()
    {
        String ret = new String();

        Set<BigInteger> s = factors.keySet();
        Iterator<BigInteger> i = s.iterator();
        while (i.hasNext()) {
            BigInteger b = i.next();
            ret = ret + b.toString() + " ^ ";
            ret = ret + factors.get(b) + "\n";
        }

        return ret;
    }

    public BigInteger[] getfactors2()
    {

        int j = 0;
        BigInteger[] b1 = new BigInteger[2];

        Set<BigInteger> s = factors.keySet();
        Iterator<BigInteger> i = s.iterator();
        while (i.hasNext()) {
            BigInteger b = i.next();
            b1[j] = b;
            j = j + 1;
        }

        return b1;
    }
}