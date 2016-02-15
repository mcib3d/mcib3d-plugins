package mcib_plugins.analysis;

//=====================================================
//      Name:           GLCM_Texture
//      Project:         Gray Level Correlation Matrix Texture Analyzer
//      Version:         0.4
//
//      Author:           Julio E. Cabrera
//      Date:             06/10/05
//      Comment:       Calculates texture features based in Gray Level Correlation Matrices
//			   Changes since 0.1 version: The normalization constant (R in Haralick's paper, pixelcounter here)
//			   now takes in account the fact that for each pair of pixel you take in account there are two entries to the 
//			   grey level co-ocurrence matrix
//	 		   Changes were made also in the Correlation parameter. Now this parameter is calculated according to Walker's paper
//=====================================================
//===========imports===================================
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.plugin.Duplicator;
import ij.process.StackConverter;

//===========source====================================
public class GLCMTexture {

    int step = 1;
    int dir = 0;
    ImagePlus imp;
    double[][] glcm;
    public boolean verbose = false;

    public GLCMTexture(ImagePlus imp2, int dir, int step) {
        if (imp2.getBitDepth() != 8) {
            if (verbose) {
                IJ.log("converting to 8-bits");
            }
            Duplicator dup = new Duplicator();
            imp = dup.run(imp2);
            StackConverter sc = new StackConverter(imp);
            sc.convertToGray8();
        } else {
            imp = imp2;
        }

        this.dir = dir;
        this.step = step;
        this.computeMatrix();
    }

    private void computeMatrix() {

// This part get al the pixel values into the pixel [ ] array via the Image Processor

        ImageStack ip = imp.getStack();

//}



// The variable a holds the value of the pixel where the Image Processor is sitting its attention
// The variable b holds the value of the pixel which is the neighbor to the  pixel where the Image Processor is sitting its attention

        int a, b1, b2;
        double pixelCounter = 0;



//====================================================================================================
// This part computes the Gray Level Correlation Matrix based in the step selected by the user

        glcm = new double[256][256];


        for (int z = step; z < ip.getSize() - step; z++) {
            for (int y = step; y < ip.getHeight() - step; y++) {
                for (int x = step; x < ip.getWidth() - step; x++) {
                    switch (dir) {
                        case 0:
                            b1 = (int) ip.getVoxel(x + step, y, z);
                            b2 = (int) ip.getVoxel(x - step, y, z);
                            break;
                        case 1:
                            b1 = (int) ip.getVoxel(x, y + step, z);
                            b2 = (int) ip.getVoxel(x, y - step, z);
                            break;
                        case 2:
                            b1 = (int) ip.getVoxel(x, y, z + step);
                            b2 = (int) ip.getVoxel(x, y, z - step);
                            break;
                        default:
                            b1 = 0;
                            b2 = 0;
                            break;
                    }
                    a = (int) ip.getVoxel(x, y, z);
                    glcm[a][b1] += 1;
                    glcm[b1][a] += 1;
                    glcm[a][b2] += 1;
                    glcm[b2][a] += 1;
                    pixelCounter += 4;
                }
            }
        }
//=====================================================================================================

// This part divides each member of the glcm matrix by the number of pixels. The number of pixels was stored in the pixelCounter variable
// The number of pixels is used as a normalizing constant


        for (a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                glcm[a][b] = (glcm[a][b]) / (pixelCounter);
            }
        }

    }
//=====================================================================================================
// This part calculates the angular second moment; the value is stored in asm

    public double getASM() {
        double asm = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                asm = asm + (glcm[a][b] * glcm[a][b]);
            }
        }
        return asm;
    }
//=====================================================================================================
// This part calculates the contrast; the value is stored in contrast

    public double getContrast() {
        double contrast = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                contrast = contrast + (a - b) * (a - b) * (glcm[a][b]);
            }
        }
        return contrast;
    }
//=====================================================================================================
//This part calculates the correlation; the value is stored in correlation
// px []  and py [] are arrays to calculate the correlation
// meanx and meany are variables  to calculate the correlation
//  stdevx and stdevy are variables to calculate the correlation

    public double getCorrelation() {

//First step in the calculations will be to calculate px [] and py []
        double correlation = 0.0;
        double px = 0;
        double py = 0;
        double meanx = 0.0;
        double meany = 0.0;
        double stdevx = 0.0;
        double stdevy = 0.0;

        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                px = px + a * glcm[a][b];
                py = py + b * glcm[a][b];

            }
        }
// Now calculate the standard deviations
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                stdevx = stdevx + (a - px) * (a - px) * glcm[a][b];
                stdevy = stdevy + (b - py) * (b - py) * glcm[a][b];
            }
        }
// Now finally calculate the correlation parameter

        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                correlation = correlation + ((a - px) * (b - py) * glcm[a][b] / (stdevx * stdevy));
            }
        }

        return correlation;
    }
//===============================================================================================
// This part calculates the inverse difference moment

    public double getIDM() {
        double IDM = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                IDM = IDM + (glcm[a][b] / (1 + (a - b) * (a - b)));
            }
        }
        return IDM;

    }
//===============================================================================================
// This part calculates the entropy

    public double getEntropy() {
        double entropy = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                if (glcm[a][b] == 0) {
                } else {
                    entropy = entropy - (glcm[a][b] * (Math.log(glcm[a][b])));
                }
            }
        }
        return entropy;

    }

    public double getSum() {
        double suma = 0.0;
        for (int a = 0; a < 256; a++) {
            for (int b = 0; b < 256; b++) {
                suma = suma + glcm[a][b];
            }
        }
        return suma;
    }
}
