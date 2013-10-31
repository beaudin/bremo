package numal;

import numal.*;

public class FFT extends Object {


public static void cfftp(double a[][], int n)
{
  int mp,m,ic,id,ill,ird,icc,iss,ick,isk,isf,iap,kd2,ibp,k,
      iam,ibm,mm1,i,ja,kt,ka,ita,itb,idm1,ikt,im,jj,j,kb,kn,
      jf,ktp,icf,l,mm,kf,isp,k0,k1,k2,k3,ikb,ija,ii,jk,kh;
  int iwk[] = new int[6*n+150];
  boolean l1,more,outloop,inloop;
  boolean ll[] = new boolean[6*n+150];
  double wk[] = new double[6*n+150];
  double ak2[] = new double[3];
  double cm,sm,c1,c2,c3,s1,s2,s3,c30,rad,a0,a1,a4,b4,a2,a3,
          b0,b1,b2,b3,zero,half,one,two;
  rad=2.0*Math.PI;
  c30=0.8660254037844386;
  zero=0.0;
  half=0.5;
  one=1.0;
  two=2.0;
  cm=sm=c2=s2=c3=s3=0.0;
  k2=0;
  if (n == 1) return;
  k = n;
  m = 0;
  j = 2;
  jj = 4;
  jf = 0;
  iwk[1] = 1;
  while (true) {
    i = k/jj;
    if (i*jj == k) {
      m++;
      iwk[m+1] = j;
      k = i;
    } else {
      j += 2;
      if (j == 4) j = 3;
      jj = j * j;
      if (jj > k) break;
    }
  }
  kt = m;
  j = 2;
  while (true) {
    i = k / j;
    if (i*j == k) {
      m++;
      iwk[m+1] = j;
      k = i;
    } else {
      j = j + 1;
      if (j != 3) {
        j++;
        if (j > k) break;
      }
    }
  }
  k = iwk[m+1];
  if (iwk[kt+1] > iwk[m+1]) k = iwk[kt+1];
  if (kt > 0) {
    ktp = kt + 2;
    for (i=1; i<=kt; i++) {
      j = ktp - i;
      m++;
      iwk[m+1] = iwk[j];
    }
  }
  mp = m+1;
  ic = mp+1;
  id = ic+mp;
  ill = id+mp;
  ird = ill+mp+1;
  icc = ird+mp;
  iss = icc+mp;
  ick = iss+mp;
  isk = ick+k;
  icf = isk+k;
  isf = icf+k;
  iap = isf+k;
  kd2 = (k-1) / 2 + 1;
  ibp = iap + kd2;
  iam = ibp + kd2;
  ibm = iam + kd2;
  mm1 = m-1;
  i = 1;
  do {
    l = mp - i;
    j = ic - i;
    ll[ill+l] = (iwk[j-1] + iwk[j]) == 4;
    if (ll[ill+l]) {
      i++;
      l--;
      ll[ill+l] = false;
    }
    i++;
  } while (i <= mm1);
  ll[ill+1] = false;
  ll[ill+mp] = false;
  iwk[ic] = 1;
  iwk[id] = n;
  for (j=1; j<=m; j++) {
    k = iwk[j+1];
    iwk[ic+j] = iwk[ic+j-1] * k;
    iwk[id+j] = iwk[id+j-1] / k;
    wk[ird+j] = rad/iwk[ic+j];
    c1 = rad/k;
    if (k > 2) {
      wk[icc+j] = Math.cos(c1);
      wk[iss+j] = Math.sin(c1);
    }
  }
  mm = m;
  if (ll[ill+m]) mm = m - 1;
  if (mm > 1) {
    sm = iwk[ic+mm-2] * wk[ird+m];
    cm = Math.cos(sm);
    sm = Math.sin(sm);
  }
  kb = 0;
  kn = n;
  jj = 0;
  i = 1;
  c1 = one;
  s1 = zero;
  l1 = true;
  outloop: for (;;) {
    if (ll[ill+i+1]) {
      kf = 4;
      i++;
    } else
      kf = iwk[i+1];
    isp = iwk[id+i];
    if (!l1) {
      s1 = jj * wk[ird+i];
      c1 = Math.cos(s1);
      s1 = Math.sin(s1);
    }
    inloop: for (;;) {
      if (kf <= 4) {
        switch (kf) {
          case 1:case 2:
            k0 = kb + isp;
            k2 = k0 + isp;
            if (!l1) {
              while (true) {
                k0--;
                if (k0 < kb) {
                  if ( i < mm ) {
                    i++;
                    continue outloop;
                  }
                  i = mm;
                  l1 = false;
                  kb = iwk[id+i-1] + kb;
                  if (kb >= kn) break outloop;
                  more=true;
                  while (more) {
                    jj = iwk[ic+i-2] + jj;
                    if (jj < iwk[ic+i-1]) {
                      more=false;
                    } else {
                      i--;
                      jj -= iwk[ic+i];
                    }
                  }
                  if (i == mm) {
                    c2 = c1;
                    c1 = cm * c1 - sm * s1;
                    s1 = sm * c2 + cm * s1;
                    continue inloop;
                  } else { 
                    if (ll[ill+i]) i++;
                    continue outloop;
                  }
                }
                k2--;
                a4 = a[1][k2+1];
                b4 = a[2][k2+1];
                a0 = a4*c1-b4*s1;
                b0 = a4*s1+b4*c1;
                a[1][k2+1] = a[1][k0+1]-a0;
                a[2][k2+1] = a[2][k0+1]-b0;
                a[1][k0+1] = a[1][k0+1]+a0;
                a[2][k0+1] = a[2][k0+1]+b0;
              }
            }
            while (true) {
              k0--;
              if (k0 < kb) {
                if ( i < mm ) {
                  i++;
                  continue outloop;
                }
                i = mm;
                l1 = false;
                kb = iwk[id+i-1] + kb;
                if (kb >= kn) break outloop;
                more=true;
                while (more) {
                  jj = iwk[ic+i-2] + jj;
                  if (jj < iwk[ic+i-1]) {
                    more=false;
                  } else {
                    i--;
                    jj -= iwk[ic+i];
                  }
                }
                if (i == mm) {
                  c2 = c1;
                  c1 = cm * c1 - sm * s1;
                  s1 = sm * c2 + cm * s1;
                  continue inloop;
                } else { 
                  if (ll[ill+i]) i++;
                  continue outloop;
                }
              }
              k2--;
              ak2[1] = a[1][k2+1];
              ak2[2] = a[2][k2+1];
              a[1][k2+1] = a[1][k0+1]-ak2[1];
              a[2][k2+1] = a[2][k0+1]-ak2[2];
              a[1][k0+1] = a[1][k0+1]+ak2[1];
              a[2][k0+1] = a[2][k0+1]+ak2[2];
            }

          case 3:
            if (!l1) {
              c2 = c1 * c1 - s1 * s1;
              s2 = two * c1 * s1;
            }
            ja = kb + isp - 1;
            ka = ja + kb;
            ikb = kb+1;
            ija = ja+1;
            for (ii=ikb; ii<=ija; ii++) {
              k0 = ka - ii + 1;
              k1 = k0 + isp;
              k2 = k1 + isp;
              a0 = a[1][k0+1];
              b0 = a[2][k0+1];
              if (l1) {
                a1 = a[1][k1+1];
                b1 = a[2][k1+1];
                a2 = a[1][k2+1];
                b2 = a[2][k2+1];
              } else {
                a4 = a[1][k1+1];
                b4 = a[2][k1+1];
                a1 = a4*c1-b4*s1;
                b1 = a4*s1+b4*c1;
                a4 = a[1][k2+1];
                b4 = a[2][k2+1];
                a2 = a4*c2-b4*s2;
                b2 = a4*s2+b4*c2;
              }
              a[1][k0+1] = a0+a1+a2;
              a[2][k0+1] = b0+b1+b2;
              a0 = -half * (a1+a2) + a0;
              a1 = (a1-a2) * c30;
              b0 = -half * (b1+b2) + b0;
              b1 = (b1-b2) * c30;
              a[1][k1+1] = a0-b1;
              a[2][k1+1] = b0+a1;
              a[1][k2+1] = a0+b1;
              a[2][k2+1] = b0-a1;
            }
            if ( i < mm ) {
              i++;
              continue outloop;
            }
            i = mm;
            l1 = false;
            kb = iwk[id+i-1] + kb;
            if (kb >= kn) break outloop;
            more=true;
            while (more) {
              jj = iwk[ic+i-2] + jj;
              if (jj < iwk[ic+i-1]) {
                more=false;
              } else {
                i--;
                jj -= iwk[ic+i];
              }
            }
            if (i == mm) {
              c2 = c1;
              c1 = cm * c1 - sm * s1;
              s1 = sm * c2 + cm * s1;
              continue inloop;
            } else { 
              if (ll[ill+i]) i++;
              continue outloop;
            }

          case 4:
            if (!l1) {
              c2 = c1 * c1 - s1 * s1;
              s2 = two * c1 * s1;
              c3 = c1 * c2 - s1 * s2;
              s3 = s1 * c2 + c1 * s2;
            }
            ja = kb + isp - 1;
            ka = ja + kb;
            ikb = kb+1;
            ija = ja+1;
            for (ii = ikb; ii<=ija; ii++) {
              k0 = ka - ii + 1;
              k1 = k0 + isp;
              k2 = k1 + isp;
              k3 = k2 + isp;
              a0 = a[1][k0+1];
              b0 = a[2][k0+1];
              if (l1) {
                a1 = a[1][k1+1];
                b1 = a[2][k1+1];
                a2 = a[1][k2+1];
                b2 = a[2][k2+1];
                a3 = a[1][k3+1];
                b3 = a[2][k3+1];
              } else {
                a4 = a[1][k1+1];
                b4 = a[2][k1+1];
                a1 = a4*c1-b4*s1;
                b1 = a4*s1+b4*c1;
                a4 = a[1][k2+1];
                b4 = a[2][k2+1];
                a2 = a4*c2-b4*s2;
                b2 = a4*s2+b4*c2;
                a4 = a[1][k3+1];
                b4 = a[2][k3+1];
                a3 = a4*c3-b4*s3;
                b3 = a4*s3+b4*c3;
              }
              a[1][k0+1] = a0+a2+a1+a3;
              a[2][k0+1] = b0+b1+b2+b3;
              a[1][k1+1] = a0+a2-a1-a3;
              a[2][k1+1] = b0+b2-b1-b3;
              a[1][k2+1] = a0-a2-b1+b3;
              a[2][k2+1] = b0-b2+a1-a3;
              a[1][k3+1] = a0-a2+b1-b3;
              a[2][k3+1] = b0-b2-a1+a3;
            }

            if ( i < mm ) {
              i++;
              continue outloop;
            }
            i = mm;
            l1 = false;
            kb += iwk[id+i-1];
            if (kb >= kn) break outloop;
            more=true;
            while (more) {
              jj = iwk[ic+i-2] + jj;
              if (jj < iwk[ic+i-1]) {
                more=false;
              } else {
                i--;
                jj -= iwk[ic+i];
              }
            }
            if (i == mm) {
              c2 = c1;
              c1 = cm * c1 - sm * s1;
              s1 = sm * c2 + cm * s1;
              continue inloop;
            } else { 
              if (ll[ill+i]) i++;
              continue outloop;
            }

        }  // end switch
      }
      jk = kf - 1;
      kh = jk/2;
      k3 = iwk[id+i-1];
      k0 = kb + isp;
      if (!l1) {
        k = jk - 1;
        wk[icf+1] = c1;
        wk[isf+1] = s1;
        for (j=1; j<=k; j++) {
          wk[icf+j+1] = wk[icf+j] * c1 - wk[isf+j] * s1;
          wk[isf+j+1] = wk[icf+j] * s1 + wk[isf+j] * c1;
        }
      }
      if (kf != jf) {
        c2 = wk[icc+i];
        wk[ick+1] = c2;
        wk[ick+jk] = c2;
        s2 = wk[iss+i];
        wk[isk+1] = s2;
        wk[isk+jk] = -s2;
        for (j = 1; j<=kh; j++) {
          k = jk - j;
          wk[ick+k] = wk[ick+j] * c2 - wk[isk+j] * s2;
          wk[ick+j+1] = wk[ick+k];
          wk[isk+j+1] = wk[ick+j] * s2 + wk[isk+j] * c2;
          wk[isk+k] = -wk[isk+j+1];
        }
      }
      do {
        k0--;
        k1 = k0;
        k2 = k0 + k3;
        a0 = a[1][k0+1];
        b0 = a[2][k0+1];
        a3 = a0;
        b3 = b0;
        for (j = 1; j<=kh; j++) {
          k1 += isp;
          k2 -= isp;
          if (l1) {
            a1 = a[1][k1+1];
            b1 = a[2][k1+1];
            a2 = a[1][k2+1];
            b2 = a[2][k2+1];
          } else {
            k = kf - j;
            a4 = a[1][k1+1];
            b4 = a[2][k1+1];
            a1 = a4*wk[icf+j]-b4*wk[isf+j];
            b1 = a4*wk[isf+j]+b4*wk[icf+j];
            a4 = a[1][k2+1];
            b4 = a[2][k2+1];
            a2 = a4*wk[icf+k]-b4*wk[isf+k];
            b2 = a4*wk[isf+k]+b4*wk[icf+k];
          }
          wk[iap+j] = a1 + a2;
          wk[iam+j] = a1 - a2;
          wk[ibp+j] = b1 + b2;
          wk[ibm+j] = b1 - b2;
          a3 += a1 + a2;
          b3 += b1 + b2;
        }
        a[1][k0+1] = a3;
        a[2][k0+1] = b3;
        k1 = k0;
        k2 = k0 + k3;
        for (j=1; j<=kh; j++) {
          k1 += isp;
          k2 -= isp;
          jk = j;
          a1 = a0;
          b1 = b0;
          a2 = zero;
          b2 = zero;
          for (k=1; k<=kh; k++) {
            a1 += wk[iap+k] * wk[ick+jk];
            a2 += wk[iam+k] * wk[isk+jk];
            b1 += wk[ibp+k] * wk[ick+jk];
            b2 += wk[ibm+k] * wk[isk+jk];
            jk += j;
            if (jk >= kf) jk -= kf;
          }
          a[1][k1+1] = a1-b2;
          a[2][k1+1] = b1+a2;
          a[1][k2+1] = a1+b2;
          a[2][k2+1] = b1-a2;
        }
      } while (k0 > kb);

      jf = kf;
      if ( i < mm ) {
        i++;
        continue outloop;
      }
      i = mm;
      l1 = false;
      kb += iwk[id+i-1];
      if (kb >= kn) break outloop;
      more=true;
      while (more) {
        jj = iwk[ic+i-2] + jj;
        if (jj < iwk[ic+i-1]) {
          more=false;
        } else {
          i--;
          jj -= iwk[ic+i];
        }
      }
      if (i == mm) {
        c2 = c1;
        c1 = cm * c1 - sm * s1;
        s1 = sm * c2 + cm * s1;
        continue inloop;
      } else { 
        if (ll[ill+i]) i++;
        continue outloop;
      }
    }  //  inloop
  }   //  outloop

  i = 1;
  ja = kt - 1;
  ka = ja + 1;
  if (ja >= 1) {
    for (ii=1; ii<=ja; ii++) {
      j = ka - ii;
      iwk[j+1]--;
      i = iwk[j+1] + i;
    }
  }
  if (kt > 0) {
    j = 1;
    i = 0;
    kb = 0;
    while (true) {
      k2 = iwk[id+j] + kb;
      k3 = k2;
      jj = iwk[ic+j-1];
      jk = jj;
      k0 = kb + jj;
      isp = iwk[ic+j] - jj;
      while (true) {
        k = k0 + jj;
        do {
          a4 = a[1][k0+1];
          b4 = a[2][k0+1];
          a[1][k0+1] = a[1][k2+1];
          a[2][k0+1] = a[2][k2+1];
          a[1][k2+1] = a4;
          a[2][k2+1] = b4;
          k0++;
          k2++;
        } while (k0 < k);
        k0 += isp;
        k2 += isp;
        if (k0 >= k3) {
          if (k0 >= k3 + isp) {
            k3 += iwk[id+j];
            if (k3 - kb >= iwk[id+j-1]) break;
            k2 = k3 + jk;
            jk += jj;
            k0 = k3 - iwk[id+j] + jk;
          } else
            k0 = k0 - iwk[id+j] + jj;
        }
      }
      if (j < kt) {
        k = iwk[j+1] + i;
        j++;
        do {
          i = i + 1;
          iwk[ill+i] = j;
        } while (i < k);
      } else {
        kb = k3;
        if (i > 0) {
          j = iwk[ill+i];
          i--;
        } else {
          if (kb >= n) break;
          j = 1;
        }
      }
    }
  }
  jk = iwk[ic+kt];
  isp = iwk[id+kt];
  m -= kt;
  kb = isp/jk-2;
  if (kt >= m-1 ) return;
  ita = ill+kb+1;
  itb = ita+jk;
  idm1 = id-1;
  ikt = kt+1;
  im = m+1;
  for (j=ikt; j<=im; j++)
    iwk[idm1+j] = iwk[idm1+j]/jk;
  jj = 0;
  for (j = 1; j<=kb; j++) {
    k = kt;
    while (true) {
      jj += iwk[id+k+1];
      if (jj < iwk[id+k]) break;
      jj -= iwk[id+k];
      k++;
    }
    iwk[ill+j] = jj;
    if (jj == j) iwk[ill+j] = -j;
  }
  for (j=1; j<=kb; j++) {
    if (iwk[ill+j] > 0) {
      k2 = j;
      while (true) {
        k2 = Math.abs(iwk[ill+k2]);
        if (k2 == j) break;
        iwk[ill+k2] = -iwk[ill+k2];
      }
    }
  }
  i = 0;
  j = 0;
  kb = 0;
  kn = n;
  while (true) {
    j++;
    if (iwk[ill+j] >= 0) {
      k = iwk[ill+j];
      k0 = jk * k + kb;
      do {
        a4 = a[1][k0+i+1];
        b4 = a[2][k0+i+1];
        wk[ita+i] = a4;
        wk[itb+i] = b4;
        i = i + 1;
      } while (i < jk);
      i = 0;
      do {
        k = -iwk[ill+k];
        jj = k0;
        k0 = jk * k + kb;
        do {
          a[1][jj+i+1] = a[1][k0+i+1];
          a[2][jj+i+1] = a[2][k0+i+1];
          i++;
        } while (i < jk);
        i = 0;
      } while (k != j);
      do {
        a[1][k0+i+1] = wk[ita+i];
        a[2][k0+i+1] = wk[itb+i];
        i++;
      } while (i < jk);
      i = 0;
      if (j >= k2) {
        j = 0;
        kb += isp;
        if (kb >= kn) break;
      }
    }
  }
}


public static void orderf(double a[][], int m)
{
  int i,mp,k,j,jj,jk,n2,n4,n8,lm,nn;
  int iwk[] = new int[m+2];
  double temp[] = new double[3];

  if (m <= 1) return;
  n8=0;
  mp = m+1;
  jj = 1;
  iwk[1] = 1;
  for (i = 2; i<=mp; i++)
    iwk[i] = iwk[i-1] * 2;
  n4 = iwk[mp-2];
  if (m > 2) n8 = iwk[mp-3];
  n2 = iwk[mp-1];
  lm = n2;
  nn = iwk[mp]+1;
  mp -= 4;
  j = 2;
  do {
    jk = jj + n2;
    temp[1] = a[1][j];
    temp[2] = a[2][j];
    a[1][j] = a[1][jk];
    a[2][j] = a[2][jk];
    a[1][jk] = temp[1];
    a[2][jk] = temp[2];
    j++;
    if (jj <= n4) {
        jj += n4;
    } else {
      jj -= n4;
      if (jj <= n8) {
        jj += n8;
      } else {
        jj -= n8;
        k = mp;
        while (iwk[k] < jj) {
          jj -= iwk[k];
          k--;
        }
        jj += iwk[k];
      }
    }
    if (jj > j) {
      k = nn - j;
      jk = nn - jj;
      temp[1] = a[1][j];
      temp[2] = a[2][j];
      a[1][j] = a[1][jj];
      a[2][j] = a[2][jj];
      a[1][jj] = temp[1];
      a[2][jj] = temp[2];
      temp[1] = a[1][k];
      temp[2] = a[2][k];
      a[1][k] = a[1][jk];
      a[2][k] = a[2][jk];
      a[1][jk] = temp[1];
      a[2][jk] = temp[2];
    }
    j++;
  } while (j <= lm);
}


public static void cfft2p(double a[][], int m)
{
  int i,mp,k,j,jj,mm,n,kb,mk,kn,k0,k1,k2,k3,isp,jsp;
  int iwk[] = new int[m+2];
  double ak2[] = new double[3];
  double zero,one,rad,c1,c2,c3,s1,s2,s3,ck,sk,sq,a0,a1,a2,a3,
          b0,b1,b2,b3,temp;

  sq=0.7071067811865475;
  sk=0.3826834323650898;
  ck=0.9238795325112868;
  zero=0.0;
  one=1.0;
  c2=c3=s2=s3=0.0;
  k3=0;
  mp = m+1;
  n = (int)Math.pow(2,m);
  iwk[1] = 1;
  mm = (m/2)*2;
  kn = n+1;
  for (i=2;i<=mp;i++)
    iwk[i] = iwk[i-1]+iwk[i-1];
  rad = (2.0*Math.PI)/n;
  mk = m - 4;
  kb = 1;
  if (mm != m) {
    k2 = kn;
    k0 = iwk[mm+1] + kb;
    do {
      k2--;
      k0--;
      ak2[1] = a[1][k2];
      ak2[2] = a[2][k2];
      a[1][k2] = a[1][k0] - ak2[1];
      a[2][k2] = a[2][k0] - ak2[2];
      a[1][k0] = a[1][k0] + ak2[1];
      a[2][k0] = a[2][k0] + ak2[2];
    } while (k0 > kb);
  }
  c1 = one;
  s1 = zero;
  jj = 0;
  k = mm - 1;
  j = 4;
  if (k < 1) return;
  isp = iwk[k];
  if (jj != 0) {
    c2 = jj * isp * rad;
    c1 = Math.cos(c2);
    s1 = Math.sin(c2);
    c2 = c1 * c1 - s1 * s1;
    s2 = c1 * (s1 + s1);
    c3 = c2 * c1 - s2 * s1;
    s3 = c2 * s1 + s2 * c1;
  }
  while (true) {
    jsp = isp + kb;
    for (i=1;i<=isp;i++) {
      k0 = jsp - i;
      k1 = k0 + isp;
      k2 = k1 + isp;
      k3 = k2 + isp;
      a0 = a[1][k0];
      b0 = a[2][k0];
      a1 = a[1][k1];
      b1 = a[2][k1];
      a2 = a[1][k2];
      b2 = a[2][k2];
      a3 = a[1][k3];
      b3 = a[2][k3];
      if (s1 != zero) {
        temp = a1;
        a1 = a1 * c1 - b1 * s1;
        b1 = temp * s1 + b1 * c1;
        temp = a2;
        a2 = a2 * c2 - b2 * s2;
        b2 = temp * s2 + b2 * c2;
        temp = a3;
        a3 = a3 * c3 - b3 * s3;
        b3 = temp * s3 + b3 * c3;
      }
      temp = a0 + a2;
      a2 = a0 - a2;
      a0 = temp;
      temp = a1 + a3;
      a3 = a1 - a3;
      a1 = temp;
      temp = b0 + b2;
      b2 = b0 - b2;
      b0 = temp;
      temp = b1 + b3;
      b3 = b1 - b3;
      b1 = temp;
      a[1][k0] = a0+a1;
      a[2][k0] = b0+b1;
      a[1][k1] = a0-a1;
      a[2][k1] = b0-b1;
      a[1][k2] = a2-b3;
      a[2][k2] = b2+a3;
      a[1][k3] = a2+b3;
      a[2][k3] = b2-a3;
    }
    if (k > 1) {
      k -= 2;
      isp = iwk[k];
      if (jj != 0) {
        c2 = jj * isp * rad;
        c1 = Math.cos(c2);
        s1 = Math.sin(c2);
        c2 = c1 * c1 - s1 * s1;
        s2 = c1 * (s1 + s1);
        c3 = c2 * c1 - s2 * s1;
        s3 = c2 * s1 + s2 * c1;
      }
    } else {
      kb = k3 + isp;
      if (kn <= kb) return;
      if (j == 1) {
        k = 3;
        j = mk;
        while (true) {
          if (iwk[j] > jj) break;
          jj -= iwk[j];
          j--;
          if (iwk[j] > jj) break;
          jj -= iwk[j];
          j--;
          k += 2;
        }
        jj += iwk[j];
        j = 4;
        isp = iwk[k];
        if (jj != 0) {
          c2 = jj * isp * rad;
          c1 = Math.cos(c2);
          s1 = Math.sin(c2);
          c2 = c1 * c1 - s1 * s1;
          s2 = c1 * (s1 + s1);
          c3 = c2 * c1 - s2 * s1;
          s3 = c2 * s1 + s2 * c1;
        }
      } else {
        j--;
        c2 = c1;
        if (j == 2) {
          c1 = c1 * ck + s1 * sk;
          s1 = s1 * ck - c2 * sk;
        } else {
          c1 = (c1 - s1) * sq;
          s1 = (c2 + s1) * sq;
        }
        c2 = c1 * c1 - s1 * s1;
        s2 = c1 * (s1 + s1);
        c3 = c2 * c1 - s2 * s1;
        s3 = c2 * s1 + s2 * c1;
      }
    }
  }
}


public static void cfft2r(double a[][], int m)
{
  int i,mp1,k,j,jj,n,kb,kn,ks,mk,k0,k1,k2,k3,k4,nt,isp;
  int iwk[] = new int[m+2];
  double ak2[] = new double[3];
  double rad,c1,c2,c3,s1,s2,s3,ck,sk,sq,a0,a1,a2,a3,
          b0,b1,b2,b3,zero,one,pie,tr,ti,temp;

  sq=0.7071067811865475;
  sk=0.3826834323650898;
  ck=0.9238795325112867;
  zero=0.0;
  one=1.0;
  if (m == 0) return;
  n = (int)Math.pow(2,m);
  c2=c3=s2=s3=0.0;
  mp1 = m + 1;
  iwk[1] = 1;
  kn = 0;
  k4 = 4;
  mk = m - 3;
  ks = 1;
  for (i=2; i<=mp1; i++) {
    ks = ks + ks;
    iwk[i] = ks;
  }
  rad = Math.PI / ks;
  do {
    kb = kn + k4;
    kn += ks;
    if (m != 1) {
      jj = 0;
      k = 1;
      j = mk;
      nt = 3;
      c1 = one;
      isp = iwk[k];
      s1 = zero;
      if (jj != 0) {
        c2 = jj * isp * rad;
        c1 = Math.cos(c2);
        s1 = Math.sin(c2);
        c2 = c1 * c1 - s1 * s1;
        s2 = 2 * c1 * s1;
        c3 = c1 * c2 - s1 * s2;
        s3 = s1 * c2 + c1 * s2;
      }
      k3 = kb - isp + 1;
      while (true) {
        k2 = k3 - isp;
        k1 = k2 - isp;
        k0 = k1 - isp;
        a0 = a[1][k0];
        b0 = a[2][k0];
        a1 = a[1][k1];
        b1 = a[2][k1];
        a2 = a[1][k2];
        b2 = a[2][k2];
        a3 = a[1][k3];
        b3 = a[2][k3];
        temp = a0 + a1;
        a1 = a0 - a1;
        a0 = temp;
        temp = a2 + a3;
        a3 = a2 - a3;
        a2 = temp;
        temp = b0 + b1;
        b1 = b0 - b1;
        b0 = temp;
        temp = b2 + b3;
        b3 = b2 - b3;
        b2 = temp;
        a[1][k0] = a0+a2;
        a[2][k0] = b0+b2;
        if (s1 == zero) {
          a[1][k1] = a1-b3;
          a[2][k1] = b1+a3;
          a[1][k2] = a0-a2;
          a[2][k2] = b0-b2;
          a[1][k3] = a1+b3;
          a[2][k3] = b1-a3;
        } else {
          tr = a1 - b3;
          ti = b1 + a3;
          a[1][k1] = tr*c1-ti*s1;
          a[2][k1] = tr*s1+ti*c1;
          tr = a0 - a2;
          ti = b0 - b2;
          a[1][k2] = tr*c2-ti*s2;
          a[2][k2] = tr*s2+ti*c2;
          tr = a1 + b3;
          ti = b1 - a3;
          a[1][k3] = tr*c3-ti*s3;
          a[2][k3] = tr*s3+ti*c3;
        }
        k3++;
        if (k3 > kb) {
          nt--;
          if (nt >= 0) {
            c2 = c1;
            if (nt != 1) {
              c1 = (c1 - s1) * sq;
              s1 = (c2 + s1) * sq;
            } else {
              c1 = c1 * ck + s1 * sk;
              s1 = s1 * ck - c2 * sk;
            }
            kb += k4;
            if (kb <= kn) {
              c2 = c1 * c1 - s1 * s1;
              s2 = 2 * c1 * s1;
              c3 = c1 * c2 - s1 * s2;
              s3 = s1 * c2 + c1 * s2;
              k3 = kb - isp + 1;    
            } else
              break;
          } else {
            if (nt == -1) {
              k = 3;
              isp = iwk[k];
              s1 = zero;
              if (jj != 0) {
                c2 = jj * isp * rad;
                c1 = Math.cos(c2);
                s1 = Math.sin(c2);
                c2 = c1 * c1 - s1 * s1;
                s2 = 2 * c1 * s1;
                c3 = c1 * c2 - s1 * s2;
                s3 = s1 * c2 + c1 * s2;
              }
              k3 = kb - isp + 1;
            } else {
              if (iwk[j] <= jj) {
                jj -= iwk[j];
                j--;
                if (iwk[j] <= jj) {
                  jj -= iwk[j];
                  j--;
                  k += 2;
                } else {
                  jj += iwk[j];
                  j = mk;
                }
              } else {
                jj += iwk[j];
                j = mk;
              }
              if (j < mk) {
                isp = iwk[k];
                s1 = zero;
                if (jj != 0) {
                  c2 = jj * isp * rad;
                  c1 = Math.cos(c2);
                  s1 = Math.sin(c2);
                  c2 = c1 * c1 - s1 * s1;
                  s2 = 2 * c1 * s1;
                  c3 = c1 * c2 - s1 * s2;
                  s3 = s1 * c2 + c1 * s2;
                }
                k3 = kb - isp + 1;
              } else {
                k = 1;
                nt = 3;
                kb += k4;
                if (kb <= kn) {
                  isp = iwk[k];
                  s1 = zero;
                  if (jj != 0) {
                    c2 = jj * isp * rad;
                    c1 = Math.cos(c2);
                    s1 = Math.sin(c2);
                    c2 = c1 * c1 - s1 * s1;
                    s2 = 2 * c1 * s1;
                    c3 = c1 * c2 - s1 * s2;
                    s3 = s1 * c2 + c1 * s2;
                  }
                  k3 = kb - isp + 1;
                } else
                  break;
              }
            }
          }
        }
      }
    }
    k = (m/2) * 2;
    if (k != m) {
      k2 = kn + 1;
      j = kn - iwk[k+1] + 1;
      k0 = j;
      do {
        k2--;
        k0--;
        ak2[1] = a[1][k2];
        ak2[2] = a[2][k2];
        a[1][k2] = a[1][k0] - ak2[1];
        a[2][k2] = a[2][k0] - ak2[2];
        a[1][k0] = a[1][k0] + ak2[1];
        a[2][k0] = a[2][k0] + ak2[2];
      } while (k2 > j);
    }
  } while (kn < n);
}


public static void rfftr(double a[], double gamn[], int n)
{
  int i,k,nd2,nd4,mtwo,imax,m,np2,nmk;
  double zero,one,theta,tp,half,ai,ar;
  double s1[] = new double[3];
  double ximag[] = new double[3];
  double beta[] = new double[3];
  double gam[] = new double[3];
  double alph[] = new double[3];
  double a1[][] = new double[3][(n/2)+1];

  zero=0.0;
  half=0.5;
  one=1.0;
  imax=24;
  nd2 = n/2;
  for (i=1; i<=nd2; i++) {
    a1[1][i]=a[2*i-1];
    a1[2][i]=a[2*i];
  }
  if (n == 2) {
    ar = a1[1][1];
    ai = a1[2][1];
    theta = ar;
    tp = ai;
    gamn[1] = theta-tp;
    gamn[2] = zero;
    a[1] = theta+tp;
    a[2] = zero;
    return;
  }
  gam[1] = zero;
  gam[2] = zero;
  for (i=1; i<=nd2; i++) {
    gam[1] += a1[1][i];
    gam[2] += a1[2][i];
  }
  tp = gam[1]-gam[2];
  gam[1] = tp;
  gam[2] = zero;
  mtwo = 2;
  m = 1;
  for (i=1; i<=imax; i++) {
    if (nd2 <= mtwo) break;
    mtwo = mtwo+mtwo;
    m++;
  }
  if (nd2 != mtwo) {
    cfftp(a1,nd2);
  } else {
    cfft2p(a1,m);
    orderf(a1,m);
  }
  alph[1] = a1[1][1];
  alph[2] = a1[2][1];
  a1[1][1] = alph[1] + alph[2];
  a1[2][1] = zero;
  nd4 = (nd2+1)/2;
  if (nd4 >= 2) {
    np2 = nd2 + 2;
    theta = Math.PI/nd2;
    tp = theta;
    ximag[1] = zero;
    ximag[2] = one;
    for (k=2; k<=nd4; k++) {
      nmk = np2 - k;
      s1[1] = a1[1][nmk];
      s1[2] = -a1[2][nmk];
      alph[1] = a1[1][k] + s1[1];
      alph[2] = a1[2][k] + s1[2];
      beta[1] = ximag[1]*(s1[1]-a1[1][k]) - ximag[2]*(s1[2]-a1[2][k]);
      beta[2] = ximag[1]*(s1[2]-a1[2][k]) + ximag[2]*(s1[1]-a1[1][k]);
      s1[1] = Math.cos(theta);
      s1[2] = Math.sin(theta);
      a1[1][k] = (alph[1] + (beta[1]*s1[1] - beta[2]*s1[2])) * half;
      a1[2][k] = (alph[2] + (beta[1]*s1[2] + beta[2]*s1[1])) * half;
      a1[1][nmk] = (alph[1] - (beta[1]*s1[1] - beta[2]*s1[2])) * half;
      a1[2][nmk] = -(alph[2] - (beta[1]*s1[2] + beta[2]*s1[1])) * half;
      theta += tp;
    }
  }
  gamn[1] = gam[1];
  gamn[2] = gam[2];
  for (i=1; i<=n; i+=2) {
    k = i/2 + 1;
    a[i] = a1[1][k];
    a[i+1] = a1[2][k];
  }
}


public static boolean powsp(double x[], double y[], int n, int l,
        boolean crossp, double psx[], double psy[], double xps[])
{
  int m,lp1,ld2,nf,k,i,j,km1sl,lpk,ipnf,jm1;
  double pi2,pii,xm,xsave,ysave,phase,xim1,c1,c2;
  double wk[] = new double[(l/2)+1];
  double gamn[] = new double[3];
  double hamn[] = new double[3];
  double tmp[] = new double[l+1];
    
  if ((n/l)*l != n)
    return (false);
  m = n / l;
  lp1 = l + 1;
  ld2 = l / 2;
  nf = ld2 + 1;
  for (i=1; i<=nf; i++)
    psx[i] = 0.0;
  if (crossp)
    for (i=1; i<=nf; i++) {
      psy[i] = 0.0;
      xps[i] = 0.0;
      xps[i+nf] = 0.0;
    }
  c1 = 0.5 * (l-1);
  c2 = 2.0 / (l+1);
  for (i=1; i<=ld2; i++) {
    xim1 = i - 1;
    wk[i] = 1.0 + c2*(xim1-c1);
  }
  for (k=1; k<=m; k++) {
    km1sl = (k-1) * l;
    lpk = lp1 + km1sl;
    for (i=1; i<=ld2; i++) {
      j = i + km1sl;
      x[j] = x[j] * wk[i];
      j = lpk - i;
      x[j] = x[j] * wk[i];
    }
    for (i=1; i<=l; i++)
      tmp[i] = x[km1sl+i];
    rfftr(tmp,gamn,l);
    for (i=1; i<=l; i++)
      x[km1sl+i] = tmp[i];
    for (i=1; i<=ld2; i++) {
      j = km1sl + i + i;
      psx[i] = psx[i] + Math.pow(x[j-1],2) + Math.pow(x[j],2);
    }
    psx[nf] = psx[nf] + Math.pow(gamn[1],2);
    if (crossp) {
      for (i=1; i<=ld2; i++) {
        j = i + km1sl;
        y[j] *= wk[i];
        j = lpk - i;
        y[j] *= wk[i];
      }
      for (i=1; i<=l; i++)
        tmp[i] = y[km1sl+i];
      rfftr (tmp,hamn,l);
      for (i=1; i<=l; i++)
        y[km1sl+i] = tmp[i];      
      for (i=1; i<=ld2; i++) {
        ipnf = i + nf;
        j = km1sl + i + i;
        jm1 = j - 1;
        psy[i] = psy[i] + Math.pow(y[jm1],2) + Math.pow(y[j],2);
        xps[i] = xps[i] + x[jm1]*y[jm1] + x[j]*y[j];
        xps[ipnf] = xps[ipnf] + x[j]*y[jm1] - x[jm1]*y[j];
      }
      psy[nf] += Math.pow(hamn[1],2);
      xps[nf] += gamn[1]*hamn[1];
    }
  }
  xm = 3.0 / n;
  for (i=1; i<=nf; i++)
    psx[i] *= xm;
  if (!crossp) return (true);
  xps[nf+nf] = 0.0;
  pi2 = Math.PI + Math.PI;
  pii = 1.0 / pi2;
  for (i=1; i<=nf; i++) {
    ipnf = i + nf;
    psy[i] *= xm;
    xsave = xps[i] * xm;
    ysave = xps[ipnf] * xm;
    xps[i] = xsave*xsave + ysave*ysave;
    if ((xsave == 0.0) && (ysave == 0.0)) {
      xps[ipnf] = 0.0;
    } else {
      phase = Math.atan2(ysave,xsave);
      if (phase < 0.0) phase += pi2;
      xps[ipnf] = phase * pii;
    }
  }
  return (true);
}


public static void timser(double w[], int n, int k, int l,
        int mode, double mean[], double var[], double alpha[],
        double beta[], double gamma[])
{
  boolean contd;
  int i,j,im,iflag,j1,j2,kend,k0,j1mk;
  double temp2,zero,temp,temp1;
  double wkarea[] = new double[l+1];

  zero=0.0;
  if (mode != 1) {
    iflag = 0;
    contd=true;
    if (k > 0) {
      for (i=1; i<=k; i++)
        alpha[i] = zero;
      if (mode < 4)
        contd=false;
      else
        for (i=1; i<=k; i++)
          beta[i] = zero;
    }
    if (contd) {
      if (l > 0)
        for (i=1; i<=l; i++)
          gamma[i] = zero;
    }
    if ((mode/2)*2 != mode)  var[0] = zero;
    contd=true;
    for (i=2; i<=n; i++)
      if (w[i] != w[i-1]) {
        contd=false;
        break;
      }
    if (contd) {
      iflag = 1;
      mean[0] = w[1];
    }
    if (iflag == 1) return;
  }
  im = (mode/2)*2-mode;
  if (im != 0) {
    temp = 0.0;
    for (i=1; i<=n; i++)
      temp += w[i];
    mean[0] = temp/n;
    temp = 0.0;
    for (i=1; i<=n; i++)
      temp += (w[i]-mean[0])*(w[i]-mean[0]);
    var[0] = temp/n;
    if (mode == 1) return;
  }
  for (j=1; j<=k; j++) {
    kend = n-j;
    temp=0.0;
    for (i=1; i<=kend; i++)
      temp += (w[i]-mean[0])*(w[i+j]-mean[0]);
    alpha[j] = temp/n;
  }
  if (mode < 4) return;
  for (j=1; j<=k; j++)
    beta[j] = alpha[j]/var[0];
  if (mode < 6) return;
  gamma[1] = beta[1];
  for (j=2; j<=l; j++) {
    j1 = j-1;
    wkarea[j1] = gamma[j1];
    j2 = j1/2;
    if (j != 2) {
      for (k0=1; k0<=j2; k0++) {
        j1mk = j1-k0;
        temp2 = wkarea[k0]-gamma[j1]*wkarea[j1mk];
        wkarea[j1mk] -= gamma[j1]*wkarea[k0];
        wkarea[k0] = temp2;
      }
    }
    temp = 0.0;
    temp1 = 0.0;
    for (i=1; i<=j1; i++) {
      temp += beta[j-i]*wkarea[i];
      temp1 += beta[i]*wkarea[i];
    }
    gamma[j] = (beta[j]-temp)/(1.0-temp1);
  }
}


public static int timspc(double x[], double y[], int n, int m,
        double deltat, boolean dtrend, boolean whiten,
        double whfact, int mode, boolean cross, double meanx[],
        double varx[], double meany[], double vary[],
        double aucvx[], double aucvy[], double freq[],
        double powspx[], double powspy[], double croscv[],
        double recrsp[], double imcrsp[], double amcrsp[],
        double phcrsp[], double amtfxy[], double amtfyx[],
        double coher[])
{
  int i,j,nm1,nsq,mp1,mm1,k,mp1mi,nim1,nmi,mp1pi,mp1pj,
      mp1mj,im1,result;
  double piom,dtopi,dtsq,ndtsq,st,fact,sum,ksixd,ksiyd,
          meansq,fact1,term,st1,sst,c1,c2,c3,f,g,hfpg,hfmg,im1pi,
          sum1,e,sst1,tpi,st2,ps,c;
  boolean ncross,pslto;
  double duma[] = new double[2];
  double tmp[] = new double[1];

  result=-1;
  if (!cross) result=0;
  if (n < 4) return (1);
  if (m >= n) return (2);
  if (m < 3) return (3);
  if (deltat <= 0.0) return (4);
  if (whiten) {
    if (whfact*whfact >= 1.0) return (5);
    if (cross && (mode == 3)) return (6);
  }
  st=meansq=st1=st2=term=ps=ksiyd=ksixd=0.0;
  pslto=true;
  nm1=n-1;
  nsq=n*n;
  mp1=m+1;
  mm1=m-1;
  ncross = !cross;
  piom=Math.PI/m;
  dtopi=deltat/Math.PI;
  dtsq=deltat*deltat;
  ndtsq=nsq*dtsq;
  for (i=1; i<=mp1; i++) {
    if (powspx[i] <= 0.0) result=7;
    if (powspy[i] <= 0.0) result=8;
  }
  if (!cross) pslto=false;
  if (whiten) {
    if (ncross) st=whfact*x[n]-x[1];
    if (cross)  st=whfact*y[n]-y[1];
    for (i=1; i<=nm1; i++) {
      if (ncross) x[i]=x[i+1]-whfact*x[i];
      if (cross)  y[i]=y[i+1]-whfact*y[i];
    }
    if (ncross) x[n]=st;
    if (cross)  y[n]=st;
  }
  if (ncross) timser(x,n,0,0,1,meanx,varx,duma,duma,duma);
  if (cross)  timser(y,n,0,0,1,meany,vary,duma,duma,duma);
  if (dtrend && ncross) varx[0] += meanx[0]*meanx[0];
  if (dtrend && cross)  vary[0] += meany[0]*meany[0];
  if (!dtrend)
    for (i=1; i<=n; i++) {
      if (ncross) x[i] -= meanx[0];
      if (cross)  y[i] -= meany[0];
    }
  st=0.0;
  tmp[0]=0.0;
  if (ncross) timser(x,n,m,0,2,tmp,tmp,aucvx,duma,duma);
  if (cross)  timser(y,n,m,0,2,tmp,tmp,aucvy,duma,duma);
  st=tmp[0];
  for (i=1; i<=m; i++) {
    fact=(double)(n)/(double)(n-i);
    if (ncross) aucvx[i] *= fact;
    if (cross)  aucvy[i] *= fact;
  }
  if (dtrend) {
    k=(n+2)/3;
    sum=0.0;
    for (i=1; i<=k; i++) {
      if (ncross) sum += x[i+n-k]-x[i];
      if (cross)  sum += y[i+n-k]-y[i];
    }
    sum /= (k*(n-k)*deltat);
    if (ncross) ksixd=sum;
    if (cross)  ksiyd=sum;
    sum=sum*sum/12.0;
    if (ncross) meansq=meanx[0]*meanx[0];
    if (cross)  meansq=meany[0]*meany[0];
    if (ncross) varx[0]=varx[0]-meansq-ndtsq*sum;
    if (cross)  vary[0]=vary[0]-meansq-ndtsq*sum;
    fact=dtsq*sum;
    for (i=1; i<=m; i++) {
      fact1=n*(n-2*i)-2*i*i;
      term=meansq+fact*fact1;
      if (ncross) aucvx[i] -= term;
      if (cross)  aucvy[i] -= term;
    }
  }
  if (mode != 1) {
    if (!cross) {
      fact=0.5/(m*deltat);
      for (i=1; i<=mp1; i++)
        freq[i]=(i-1)*fact;
    }
    c=2.0*dtopi;
    if (ncross) st1=0.5*varx[0];
    if (cross)  st1=0.5*vary[0];
    if (ncross) st2=0.5*aucvx[m];
    if (cross)  st2=0.5*aucvy[m];
    for (i=1; i<=mp1; i++) {
      sum=st1;
      fact=(i-1)*piom;
      for (j=1; j<=mm1; j++) {
        if (ncross) term=aucvx[j];
        if (cross)  term=aucvy[j];
        sum += term*Math.cos(j*fact);
      }
      sum += st2*Math.cos((i-1)*Math.PI);
      if (ncross) powspx[i]=c*sum;
      if (cross)  powspy[i]=c*sum;
    }
    if (ncross) st=powspx[1];
    if (cross)  st=powspy[1];
    if (ncross) powspx[1]=0.54*st+0.46*powspx[2];
    if (cross)  powspy[1]=0.54*st+0.46*powspy[2];
    for (i=2; i<=m; i++) {
      if (ncross) st1=powspx[i];
      if (cross)  st1=powspy[i];
      if (ncross) powspx[i]=0.23*(st+powspx[i+1])+0.54*st1;
      if (cross)  powspy[i]=0.23*(st+powspy[i+1])+0.54*st1;
      st=st1;
    }
    if (ncross) powspx[mp1]=0.54*powspx[mp1]+0.46*st;
    if (cross)  powspy[mp1]=0.54*powspy[mp1]+0.46*st;
    if (whiten) {
      st=1.0+whfact*whfact;
      st1=2.0*whfact;
      for (i=1; i<=mp1; i++) {
        sst=st-st1*Math.cos((i-1)*piom);
        if (ncross) powspx[i] /= sst;
        if (cross)  powspy[i] /= sst;
      }
    }
    for (i=1; i<=mp1; i++) {
      if (ncross) ps=powspx[i];
      if (cross)  ps=powspy[i];
      if (ps <= 0.0) {
        if (ncross) result=7;
        if (cross)  result=8;
        if (ncross) powspx[i]=0.0;
        if (cross)  powspy[i]=0.0;
        pslto=true;
      }
    }
  }
  if (ncross) return (result);
  for (i=1; i<=mp1; i++) {
    mp1mi=mp1-i;
    sum=0.0;
    nim1=n-mp1mi;
    for (j=1; j<=nim1; j++)
      sum += x[j+mp1mi]*y[j];
    croscv[i]=sum/nim1;
  }
  for (i=1; i<=m; i++) {
    nmi=n-i;
    sum=0.0;
    for (j=1; j<=nmi; j++)
      sum += x[j]*y[j+i];
    croscv[i+mp1]=sum/nmi;
  }
  if (dtrend) {
    c1=meanx[0]*meany[0];
    c2=0.5*(meanx[0]*ksiyd-varx[0]*ksixd);
    c3=ksixd*ksiyd/12.0;
    for (i=1; i<=m; i++) {
      fact=dtsq*(n*(n-2*i)-2*i*i);
      mp1pi=mp1+i;
      mp1mi=mp1-i;
      st=-c1-fact*c3;
      st1=i*c2;
      croscv[mp1pi] += st-st1;
      croscv[mp1mi] += st+st1;
    }
    croscv[mp1]=croscv[mp1]-c1-c3*ndtsq;
  }
  if (mode == 1) return (result);
  f=croscv[2*m+1];
  g=croscv[1];
  hfpg=0.5*(f+g);
  hfmg=0.5*(f-g);
  for (i=1; i<=mp1; i++) {
    im1=i-1;
    fact=im1*piom;
    im1pi=m*fact;
    sum=croscv[mp1];
    sum1=0.0;
    for (j=1; j<=mm1; j++) {
      mp1pj=mp1+j;
      mp1mj=mp1-j;
      e=j*fact;
      f=croscv[mp1pj];
      g=croscv[mp1mj];
      sum += (f+g)*Math.cos(e);
      sum1 += (f-g)*Math.sin(e);
    }
    recrsp[i]=dtopi*(sum+hfpg*Math.cos(im1pi));
    imcrsp[i]=dtopi*(sum1+hfmg*Math.sin(im1pi));
  }
  st=recrsp[1];
  st1=imcrsp[1];
  recrsp[1]=0.54*st+0.46*recrsp[2];
  imcrsp[1]=0.54*st1+0.46*imcrsp[2];
  for (i=2; i<=m; i++) {
    sst=recrsp[i];
    sst1=imcrsp[i];
    recrsp[i]=0.54*sst+0.23*(st+recrsp[i+1]);
    imcrsp[i]=0.54*sst1+0.23*(st1+imcrsp[i+1]);
    st=sst;
    st1=sst1;
  }
  recrsp[mp1]=0.54*recrsp[mp1]+0.46*st;
  imcrsp[mp1]=0.54*imcrsp[mp1]+0.46*st1;
  if (mode == 2) return (result);
  tpi=2.0*Math.PI;
  for (i=1; i<=mp1; i++) {
    st=recrsp[i];
    st1=imcrsp[i];
    amcrsp[i]=Math.sqrt(st*st+st1*st1);
    if (st == 0.0) {
      if (st1 >= 0.0) phcrsp[i]=0.25;
      if (st1 < 0.0) phcrsp[i]=0.75;
    } else {
      st1=Math.atan(st1/st);
      if (st < 0.0) st1 += Math.PI;
      st1 /= tpi;
      if (st1 < 0.0) st1 += 1.0;
      phcrsp[i]=st1;
    }
  }
  if (pslto) return (result);
  for (i=1; i<=mp1; i++) {
    st1=powspx[i];
    sst=powspy[i];
    st=amcrsp[i];
    amtfxy[i]=st/st1;
    amtfyx[i]=st/sst;
    st=st*st/(st1*sst);
    if (st > 1.0) {
      st=1.0;
      result=9;
    }
    coher[i]=st;
  }
  return (result);
}

}
