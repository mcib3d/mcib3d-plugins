package mcib_plugins.processing;

import imageware.Builder;
import imageware.ImageWare;

public class LoG3D
{
  private boolean showKernel = false;

  public LoG3D(boolean paramBoolean)
  {
    this.showKernel = paramBoolean;
  }

  public ImageWare doLoG(ImageWare paramImageWare, double paramDouble1, double paramDouble2)
  {
    if (paramImageWare == null)
      return null;
    ImageWare localImageWare = doLoG_Separable(paramImageWare, paramDouble1, paramDouble2);
    return localImageWare;
  }

  public ImageWare doLoG(ImageWare paramImageWare, double paramDouble1, double paramDouble2, double paramDouble3)
  {
    if (paramImageWare == null)
      return null;
    ImageWare localImageWare = doLoG_Separable(paramImageWare, paramDouble1, paramDouble2, paramDouble3);
    return localImageWare;
  }

  private ImageWare doLoG_NonSeparable(ImageWare paramImageWare, double paramDouble1, double paramDouble2)
  {
    if (paramImageWare == null)
      return null;
    int i = paramImageWare.getSizeX();
    int j = paramImageWare.getSizeY();
    int k = paramImageWare.getSizeZ();
    double[][] arrayOfDouble1 = createKernelLoG_NonSeparable(paramDouble1);
    int m = arrayOfDouble1.length;
    double[][] arrayOfDouble2 = new double[m][m];
    ImageWare localImageWare1 = Builder.create(i, j, 1, paramImageWare.getType());
    ImageWare localImageWare2 = Builder.create(i, j, k, paramImageWare.getType());
    double d2 = 0.0D;
    for (int n = 0; n < k; n++)
    {
      paramImageWare.getXY(0, 0, n, localImageWare1);
      for (int i1 = 0; i1 < i; i1++)
        for (int i2 = 0; i2 < j; i2++)
        {
          double d1 = 0.0D;
          localImageWare1.getNeighborhoodXY(i1, i2, 0, arrayOfDouble2, (byte)2);
          for (int i3 = 0; i3 < m; i3++)
            for (int i4 = 0; i4 < m; i4++)
              d1 += arrayOfDouble2[i3][i4] * arrayOfDouble1[i3][i4];
          localImageWare2.putPixel(i1, i2, n, d1);
        }
    }
    if (this.showKernel)
    {
      ImageWare localImageWare3 = Builder.create(arrayOfDouble1);
      localImageWare3.show("LoG kernel sigma=" + paramDouble1);
    }
    return localImageWare2;
  }

  private ImageWare doLoG_NonSeparable(ImageWare paramImageWare, double paramDouble1, double paramDouble2, double paramDouble3)
  {
    if (paramImageWare == null)
      return null;
    int i = paramImageWare.getSizeX();
    int j = paramImageWare.getSizeY();
    int k = paramImageWare.getSizeZ();
    double[][][] arrayOfDouble1 = createKernelLoG_NonSeparable3(paramDouble1);
    int m = arrayOfDouble1.length;
    double[][][] arrayOfDouble2 = new double[m][m][m];
    ImageWare localImageWare1 = Builder.create(i, j, k, paramImageWare.getType());
    double d2 = 0.0D;
    for (int n = 0; n < k; n++)
      for (int i1 = 0; i1 < i; i1++)
        for (int i2 = 0; i2 < j; i2++)
        {
          double d1 = 0.0D;
          paramImageWare.getNeighborhoodXYZ(i1, i2, n, arrayOfDouble2, (byte)2);
          for (int i3 = 0; i3 < m; i3++)
            for (int i4 = 0; i4 < m; i4++)
              for (int i5 = 0; i5 < m; i5++)
                d1 += arrayOfDouble2[i3][i4][i5] * arrayOfDouble1[i3][i4][i5];
          localImageWare1.putPixel(i1, i2, n, d1);
        }
    if (this.showKernel)
    {
      ImageWare localImageWare2 = Builder.create(arrayOfDouble1);
      localImageWare2.show("LoG kernel sigma=" + paramDouble1);
    }
    return localImageWare1;
  }

  public ImageWare doLoG_Separable(ImageWare paramImageWare, double paramDouble1, double paramDouble2)
  {
    if (paramImageWare == null)
      return null;
    int i = paramImageWare.getSizeX();
    int j = paramImageWare.getSizeY();
    int k = paramImageWare.getSizeZ();
    int m = 0;
    m = paramDouble1 > 0.0D ? m + 1 : m;
    m = paramDouble2 > 0.0D ? m + 1 : m;
    if (m == 0)
      return paramImageWare;
    double d1 = Math.pow(6.283185307179586D, m / 2.0D);
    double d2 = paramDouble1 > 0.0D ? paramDouble1 : 1.0D;
    double d3 = paramDouble2 > 0.0D ? paramDouble2 : 1.0D;
    double d4 = 1.0D / (d1 * d2 * d3);
    double[] arrayOfDouble1 = createKernelLoG_Fact(paramDouble1, d4);
    double[] arrayOfDouble2 = createKernelLoG_Base(paramDouble1);
    double[] arrayOfDouble3 = createKernelLoG_Fact(paramDouble2, d4);
    double[] arrayOfDouble4 = createKernelLoG_Base(paramDouble2);
    ImageWare localImageWare1 = paramImageWare.duplicate();
    ImageWare localImageWare2 = paramImageWare.duplicate();
    Object localObject;
    int i2;
    for (int n = 0; n < k; n++)
    {
      if (paramDouble2 > 0.0D)
      {
        localObject = new double[j];
        double[] arrayOfDouble5 = new double[j];
        for (i2 = 0; i2 < i; i2++)
        {
          localImageWare1.getY(i2, 0, n, (double[])localObject);
          arrayOfDouble5 = convolve((double[])localObject, arrayOfDouble3);
          localImageWare1.putY(i2, 0, n, arrayOfDouble5);
          localImageWare2.getY(i2, 0, n, (double[])localObject);
          arrayOfDouble5 = convolve((double[])localObject, arrayOfDouble4);
          localImageWare2.putY(i2, 0, n, arrayOfDouble5);
        }
      }
      if (paramDouble1 <= 0.0D)
        continue;
      localObject = new double[i];
      double[] arrayOfDouble5 = new double[i];
      for (i2 = 0; i2 < j; i2++)
      {
        localImageWare1.getX(0, i2, n, (double[])localObject);
        arrayOfDouble5 = convolve((double[])localObject, arrayOfDouble2);
        localImageWare1.putX(0, i2, n, arrayOfDouble5);
        localImageWare2.getX(0, i2, n, (double[])localObject);
        arrayOfDouble5 = convolve((double[])localObject, arrayOfDouble1);
        localImageWare2.putX(0, i2, n, arrayOfDouble5);
      }
    }
    localImageWare1.add(localImageWare2);
    if (this.showKernel)
    {
      localObject = new double[arrayOfDouble2.length][arrayOfDouble3.length];
      int i1 = arrayOfDouble1.length;
      i2 = arrayOfDouble3.length;
      for (int i3 = 0; i3 < i1; i3++)
        for (int i4 = 0; i4 < i2; i4++)
          ((double[][])localObject)[i3][i4] = (arrayOfDouble1[i3] * arrayOfDouble4[i4] + arrayOfDouble3[i4] * arrayOfDouble2[i3]);
      ImageWare localImageWare3 = Builder.create((double[][])localObject);
      localImageWare3.show("LoG kernel sigma=" + paramDouble1);
    }
    return (ImageWare)localImageWare1;
  }

  public ImageWare doLoG_Separable(ImageWare paramImageWare, double paramDouble1, double paramDouble2, double paramDouble3)
  {
    if (paramImageWare == null)
      return null;
    int i = paramImageWare.getSizeX();
    int j = paramImageWare.getSizeY();
    int k = paramImageWare.getSizeZ();
    int m = 0;
    m = paramDouble1 > 0.0D ? m + 1 : m;
    m = paramDouble2 > 0.0D ? m + 1 : m;
    m = paramDouble3 > 0.0D ? m + 1 : m;
    if (m == 0)
      return paramImageWare;
    double d1 = Math.pow(6.283185307179586D, m / 2.0D);
    double d2 = paramDouble1 > 0.0D ? paramDouble1 : 1.0D;
    double d3 = paramDouble2 > 0.0D ? paramDouble2 : 1.0D;
    double d4 = paramDouble3 > 0.0D ? paramDouble3 : 1.0D;
    double d5 = 1.0D / (d1 * d2 * d3 * d4);
    double[] arrayOfDouble1 = createKernelLoG_Fact(paramDouble1, d5);
    double[] arrayOfDouble2 = createKernelLoG_Base(paramDouble1);
    double[] arrayOfDouble3 = createKernelLoG_Fact(paramDouble2, d5);
    double[] arrayOfDouble4 = createKernelLoG_Base(paramDouble2);
    double[] arrayOfDouble5 = createKernelLoG_Fact(paramDouble3, d5);
    double[] arrayOfDouble6 = createKernelLoG_Base(paramDouble3);
    ImageWare localImageWare1 = paramImageWare.duplicate();
    ImageWare localImageWare2 = paramImageWare.duplicate();
    ImageWare localImageWare3 = paramImageWare.duplicate();
    Object localObject;
    double[] arrayOfDouble7;
    int i1;
    int i2;
    if (paramDouble3 > 0.0D)
    {
      localObject = new double[k];
      arrayOfDouble7 = new double[k];
      for (i1 = 0; i1 < i; i1++)
        for (i2 = 0; i2 < j; i2++)
        {
          localImageWare1.getZ(i1, i2, 0, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble5);
          localImageWare1.putZ(i1, i2, 0, arrayOfDouble7);
          localImageWare2.getZ(i1, i2, 0, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble6);
          localImageWare2.putZ(i1, i2, 0, arrayOfDouble7);
          localImageWare3.getZ(i1, i2, 0, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble6);
          localImageWare3.putZ(i1, i2, 0, arrayOfDouble7);
        }
    }
    if (paramDouble2 > 0.0D)
    {
      localObject = new double[j];
      arrayOfDouble7 = new double[j];
      for (i1 = 0; i1 < i; i1++)
        for (i2 = 0; i2 < k; i2++)
        {
          localImageWare1.getY(i1, 0, i2, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble4);
          localImageWare1.putY(i1, 0, i2, arrayOfDouble7);
          localImageWare2.getY(i1, 0, i2, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble3);
          localImageWare2.putY(i1, 0, i2, arrayOfDouble7);
          localImageWare3.getY(i1, 0, i2, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble4);
          localImageWare3.putY(i1, 0, i2, arrayOfDouble7);
        }
    }
    if (paramDouble1 > 0.0D)
    {
      localObject = new double[i];
      arrayOfDouble7 = new double[i];
      for (i1 = 0; i1 < j; i1++)
        for (i2 = 0; i2 < k; i2++)
        {
          localImageWare1.getX(0, i1, i2, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble2);
          localImageWare1.putX(0, i1, i2, arrayOfDouble7);
          localImageWare2.getX(0, i1, i2, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble2);
          localImageWare2.putX(0, i1, i2, arrayOfDouble7);
          localImageWare3.getX(0, i1, i2, (double[])localObject);
          arrayOfDouble7 = convolve((double[])localObject, arrayOfDouble1);
          localImageWare3.putX(0, i1, i2, arrayOfDouble7);
        }
    }
    localImageWare1.add(localImageWare2);
    localImageWare1.add(localImageWare3);
    if (this.showKernel)
    {
      localObject = new double[arrayOfDouble2.length][arrayOfDouble3.length][arrayOfDouble5.length];
      int n = arrayOfDouble1.length;
      i1 = arrayOfDouble3.length;
      i2 = arrayOfDouble5.length;
      for (int i3 = 0; i3 < n; i3++)
        for (int i4 = 0; i4 < i1; i4++)
          for (int i5 = 0; i5 < i2; i5++)
            ((double[][][])localObject)[i3][i4][i5] = (arrayOfDouble1[i3] * arrayOfDouble4[i4] * arrayOfDouble6[i5] + arrayOfDouble3[i4] * arrayOfDouble2[i3] * arrayOfDouble6[i5] + arrayOfDouble5[i5] * arrayOfDouble2[i3] * arrayOfDouble4[i4]);
      ImageWare localImageWare4 = Builder.create((double[][][])localObject);
      localImageWare4.show("LoG kernel Sigma=" + paramDouble1);
    }
    return (ImageWare)localImageWare1;
  }

  public double[] createKernelLoG_Fact(double paramDouble1, double paramDouble2)
  {
    if (paramDouble1 <= 0.0D)
    {
      double[] arrayOfDouble1 = new double[1];
      arrayOfDouble1[0] = 1.0D;
      return arrayOfDouble1;
    }
    double d1 = paramDouble1 * paramDouble1;
    double d2 = d1 * d1;
    double d3 = 2.0D * d1;
    int i = (int)Math.round((int)(paramDouble1 * 3.0D) * 2.0D + 1.0D);
    int j = i / 2;
    double[] arrayOfDouble2 = new double[i];
    for (int k = 0; k < i; k++)
    {
      double d4 = (k - j) * (k - j);
      arrayOfDouble2[k] = (paramDouble2 * (d4 / d2 - 1.0D / d1) * Math.exp(-d4 / d3));
    }
    return arrayOfDouble2;
  }

  public double[] createKernelLoG_Base(double paramDouble)
  {
    if (paramDouble <= 0.0D)
    {
      double[] arrayOfDouble1 = new double[1];
      arrayOfDouble1[0] = 1.0D;
      return arrayOfDouble1;
    }
    double d1 = paramDouble * paramDouble;
    double d2 = 2.0D * d1;
    int i = (int)Math.round((int)(paramDouble * 3.0D) * 2.0D + 1.0D);
    int j = i / 2;
    double[] arrayOfDouble2 = new double[i];
    for (int k = 0; k < i; k++)
    {
      double d3 = (k - j) * (k - j);
      arrayOfDouble2[k] = Math.exp(-d3 / d2);
    }
    return arrayOfDouble2;
  }

  private double[][] createKernelLoG_NonSeparable(double paramDouble)
  {
    double d1 = -(1.0D / (3.141592653589793D * Math.pow(paramDouble, 4.0D)));
    double d2 = 2.0D * Math.pow(paramDouble, 2.0D);
    int i = (int)(paramDouble * 6.0D);
    double[][] arrayOfDouble = new double[i][i];
    int j = i / 2;
    for (int k = 0; k < i; k++)
      for (int m = 0; m < i; m++)
      {
        double d3 = (k - j) * (k - j);
        double d4 = (m - j) * (m - j);
        arrayOfDouble[k][m] = (d1 * (1.0D - (d3 + d4) / d2) * Math.exp(-(d3 + d4) / d2));
      }
    return arrayOfDouble;
  }

  private double[][][] createKernelLoG_NonSeparable3(double paramDouble)
  {
    double d1 = -(1.0D / (3.141592653589793D * Math.pow(paramDouble, 4.0D)));
    double d2 = 2.0D * Math.pow(paramDouble, 2.0D);
    int i = (int)(paramDouble * 6.0D);
    double[][][] arrayOfDouble = new double[i][i][i];
    int j = i / 2;
    for (int k = 0; k < i; k++)
      for (int m = 0; m < i; m++)
        for (int n = 0; n < i; n++)
        {
          double d3 = (k - j) * (k - j);
          double d4 = (m - j) * (m - j);
          double d5 = (n - j) * (n - j);
          arrayOfDouble[k][m][n] = (d1 * (1.0D - (d3 + d4 + d5) / d2) * Math.exp(-(d3 + d4 + d5) / d2));
        }
    return arrayOfDouble;
  }

  private double[] convolve(double[] paramArrayOfDouble1, double[] paramArrayOfDouble2)
  {
    int i = paramArrayOfDouble1.length;
    int j = paramArrayOfDouble2.length;
    int k = j / 2;
    double[] arrayOfDouble = new double[i];
    int m = i <= 1 ? 1 : 2 * i - 2;
    for (int i1 = 0; i1 < i; i1++)
    {
      double d = 0.0D;
      for (int i2 = 0; i2 < j; i2++)
      {
        int n = i1 + i2 - k;
        while (n < 0)
          n += m;
        while (n >= i)
        {
          n = m - n;
          n = n < 0 ? -n : n;
        }
        d += paramArrayOfDouble2[i2] * paramArrayOfDouble1[n];
      }
      arrayOfDouble[i1] = d;
    }
    return arrayOfDouble;
  }
}
