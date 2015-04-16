#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include "filters3d_.h"

/*
 * Class:     filters3d_
 * Method:    jniMean3D
 * Signature: ([[BIIIIII)[[B
 */

void createKernelEllipsoid(jbyte* ker, int vx, int vy, int vz);

JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMean3D
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[B");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }

    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jbyteArray oneDim[p];
    jbyte * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetByteArrayElements(env, oneDim[i], 0);
    }


    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;
    //int nbp = s*p;

    //int ite = rx;

    //unsigned short* tmpmin;
    //unsigned short* tmpmin2;

    jbyte* tmp = (jbyte*) malloc(w * h * sizeof (jbyte));
    jbyteArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);

    for (i = 0; i < p; i++) {
        printf("3D jni mean filter : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewByteArray(env, w * h);
        int idx = 0;

        for (j = 0; j < s; j++) {
            sum = 0;
            c = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                sum += (element[i + z][j + x + w * y]&0xff);
                                c++;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmp[j] = (sum) / c;
        }
        (*env)->SetByteArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }

    free(tmp);
    free(ker);

    return result;
}

/*
 * Class:     filters3d_
 * Method:    jniMedian3D
 * Signature: ([[BIIIIII)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMedian3D
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    //printf("3D jni median filter...\n");

    jclass intArrCls = (*env)->FindClass(env, "[B");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jbyteArray oneDim[p];
    jbyte * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetByteArrayElements(env, oneDim[i], 0);
    }

    //int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    //int nbp = s*p;
   // int ite = rx;
    int j;

    //unsigned short* tmpmin;
    //unsigned short* tmpmin2;

    jbyte* tmp = (jbyte*) malloc(w * h * sizeof (jbyte));
    jbyteArray iarr;
    unsigned short* pix = malloc((2 * rx + 1)*(2 * ry + 1)*(2 * rz + 1) * sizeof (unsigned short));

    int nValues;
    int nv1b2;
    int ii;
    int jj;
    int ll;
    int mm;
    int med;
    int dum;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;

    for (i = 0; i < p; i++) {
        printf("3D jni median filter : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewByteArray(env, w * h);
        for (j = 0; j < s; j++) {
            c = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + w * y + x >= 0) && (j + w * y + x < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                pix[c] = (element[i + z][j + y * w + x]&0xff);
                                c++;
                            }
                        }
                        idx++;
                    }
                }
            }

            //  Find median. Modified algorithm according to
            //  http://www.geocities.com/zabrodskyvlada/3alg.html Contributed by HeinzKlar.
            //  (copied form ij.plugin.filter.rankfilters)
             // PB with even values ??
            nValues = c;
            nv1b2 = (nValues - 1) / 2;
            ll = 0;
            mm = nValues - 1;
            med = pix[nv1b2];

            while (ll < mm) {
                ii = ll;
                jj = mm;
                do {
                    while (pix[ii] < med) {
                        ii++;
                    }
                    while (med < pix[jj]) {
                        jj--;
                    }
                    dum = pix[jj];
                    pix[jj] = pix[ii];
                    pix[ii] = dum;
                    ii++;
                    jj--;
                } while ((jj >= nv1b2) && (ii <= nv1b2));
                if (jj < nv1b2) {
                    ll = ii;
                }
                if (nv1b2 < ii) {
                    mm = jj;
                }
                med = pix[nv1b2];
            }
            tmp[j] = med;
        }
        (*env)->SetByteArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }


    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);
    free(pix);

    return result;
}

/*
 * Class:     filters3d_
 * Method:    jniMinimum3D
 * Signature: ([[BIIIIII)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMinimum3D
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[B");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jbyteArray oneDim[p];
    jbyte * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetByteArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;

  //  int nbp = s*p;
  //  int p1, p2, p3, p4, p5, p6;

    jbyte* tmp = (jbyte*) malloc(w * h * sizeof (jbyte));
    jbyteArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;

    for (i = 0; i < p; i++) {
        printf("3D jni min filter : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewByteArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 1000000;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = element[i + z][j + x + w * y]&0xff;
                                if (c < sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmp[j] = sum;
        }
        (*env)->SetByteArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    free(tmp);
    free(ker);

    return result;
}

/*
 * Class:     filters3d_
 * Method:    jniMaximum3D
 * Signature: ([[BIIIIII)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMaximum3D
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;
    int s = w*h;
   // int nbp = s*p;

    jclass intArrCls = (*env)->FindClass(env, "[B");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jbyteArray oneDim[p];
    jbyte * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetByteArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int j;

    jbyte* tmp = (jbyte*) malloc(w * h * sizeof (jbyte));
    jbyteArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;

    for (i = 0; i < p; i++) {
        printf("3D jni max filter : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewByteArray(env, s);
        for (j = 0; j < s; j++) {
            sum = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = element[i + z][j + x + w * y]&0xff;
                                if (c > sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmp[j] = sum;
        }
        (*env)->SetByteArrayRegion(env, iarr, 0, s, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);

    return result;
}
/*
 * Class:     filters3d_
 * Method:    jniMaximumLocal3D
 * Signature: ([[BIIIIII)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMaximumLocal3D
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;
    int s = w*h;
   // int nbp = s*p;

    jclass intArrCls = (*env)->FindClass(env, "[B");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jbyteArray oneDim[p];
    jbyte * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetByteArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int j;

    jbyte* tmp = (jbyte*) malloc(w * h * sizeof (jbyte));
    jbyteArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;
    int pix;

    for (i = 0; i < p; i++) {
        printf("3D jni maxlocal : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewByteArray(env, s);
        for (j = 0; j < s; j++) {
            sum = 0;
            idx = 0;
            pix=element[i][j]&0xff;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = element[i + z][j + x + w * y]&0xff;
                                if (c > sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            if(pix==sum) tmp[j] = sum;
            else tmp[j]=0;
        }
        (*env)->SetByteArrayRegion(env, iarr, 0, s, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);

    return result;
}

/*
 * Class:     filters3d_
 * Method:    jniTopHat3D
 * Signature: ([[BIIIIII)[[B
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniTopHat3D
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;
    // printf("topHat 0 rx=%d ry=%d rz=%d \n",rx,ry,rz);

    jclass intArrCls = (*env)->FindClass(env, "[B");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jbyteArray oneDim[p];
    jbyte * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetByteArrayElements(env, oneDim[i], 0);
    }

    // printf("topHat 1 rx=%d ry=%d rz=%d \n",rx,ry,rz);
    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;

    int nbp = s*p;

    jbyte* tmpmin = (jbyte*) malloc(w * h * p * sizeof (jbyte));
    jbyteArray iarr;

    // printf("topHat 1-1 rx=%d ry=%d rz=%d \n",rx,ry,rz);


    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    // printf("topHat 1-2 rx=%d ry=%d rz=%d \n",rx,ry,rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;

    // printf("topHat 2 rx=%d ry=%d rz=%d \n",rx,ry,rz);

    for (i = 0; i < p; i++) {
        printf("3D jni min filter : %d %% completed\n", ((i + 1)*100) / p);
        //iarr = (*env)->NewByteArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 1000000;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = element[i + z][j + x + w * y]&0xff;
                                if (c < sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmpmin[i * s + j] = sum;
        }
    }

    //  printf("topHat 3\n");

    // MAX FILTER
    jbyte* tmpminmax = (jbyte*) malloc(w * h * sizeof (jbyte));
    int offset = 0;
    // printf("topHat 4 p=%d s=%d rx=%d ry=%d rz=%d\n",p,s,rx,ry,rz);
    for (i = 0; i < p; i++) {
        printf("3D jni max filter : %d %% completed\n", ((i + 1)*100) / p);
        iarr = (*env)->NewByteArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        offset = (i + z) * s + j + x + w * y;
                        if ((offset >= 0) && (offset < nbp)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = tmpmin[offset];
                                if (c > sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmpminmax[j] = sum;
        }
        (*env)->SetByteArrayRegion(env, iarr, 0, s, tmpminmax);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }

    free(tmpmin);
    free(tmpminmax);
    free(ker);

    return result;
}
/********************************************************
 ************************* 16 bits
 ***********************************************************/

/*
 * Class:     filters3d_
 * Method:    jniMean3D_16
 * Signature: ([[SIIIIII)[[S
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMean3D_116
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[S");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }

    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jshortArray oneDim[p];
    jshort * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetShortArrayElements(env, oneDim[i], 0);
    }


    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;
    //int nbp = s*p;

    //int ite = rx;

   // unsigned short* tmpmin;
   // unsigned short* tmpmin2;

    jshort* tmp = (jshort*) malloc(w * h * sizeof (jshort));
    jshortArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;


    for (i = 0; i < p; i++) {
        printf("3D jni mean filter : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewShortArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 0;
            c = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                sum += (element[i + z][j + x + w * y]&0xffff);
                                c++;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmp[j] = (sum) / c;
        }
        (*env)->SetShortArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);

    return result;
}

JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMinimum3D_116
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[S");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jshortArray oneDim[p];
    jshort * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetShortArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;

    jshort* tmp = (jshort*) malloc(w * h * sizeof (jshort));
    jshortArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;

    for (i = 0; i < p; i++) {
        printf("3D jni min filter 16 : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewShortArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 1000000;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = (element[i + z][j + x + w * y]&0xffff);
                                if (c < sum) sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmp[j] = sum;
        }
        (*env)->SetShortArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);

    return result;
}

JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMaximum3D_116
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[S");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jshortArray oneDim[p];
    jshort * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetShortArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;

    jshort* tmp = (jshort*) malloc(w * h * sizeof (jshort));
    jshortArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;

    for (i = 0; i < p; i++) {
        printf("3D jni max filter 16 : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewShortArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = (element[i + z][j + x + w * y]&0xffff);
                                if (c > sum) sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmp[j] = sum;
        }
        (*env)->SetShortArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);

    return result;
}
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMaximumLocal3D_116
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[S");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jshortArray oneDim[p];
    jshort * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetShortArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;

    jshort* tmp = (jshort*) malloc(w * h * sizeof (jshort));
    jshortArray iarr;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;
    int pix;

    for (i = 0; i < p; i++) {
        printf("3D jni maxlocal 16 : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewShortArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 0;
            idx = 0;
            pix = element[i][j]&0xffff;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = (element[i + z][j + x + w * y]&0xffff);
                                if (c > sum) sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
           if(pix == sum) tmp[j] = sum; else tmp[j]=0;
        }
        (*env)->SetShortArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);

    return result;
}

/*
 * Class:     filters3d_
 * Method:    jniMedian3D
 * Signature: ([[SIIIIII)[[S
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniMedian3D_116
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    //printf("3D jni median filter...\n");

    jclass intArrCls = (*env)->FindClass(env, "[S");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jshortArray oneDim[p];
    jshort * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetShortArrayElements(env, oneDim[i], 0);
    }

  //  int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    //int nbp = s*p;
    //int ite = rx;
    int j;

    //unsigned short* tmpmin;
    //unsigned short* tmpmin2;

    jshort* tmp = (jshort*) malloc(w * h * sizeof (jshort));
    jshortArray iarr;
    int* pix = malloc(((2 * rx + 1)*(2 * ry + 1)*(2 * rz + 1)) * sizeof (int));

    int nValues;
    int nv1b2;
    int ii;
    int jj;
    int ll;
    int mm;
    int med;
    int dum;

    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    //printf("creating kernel\n");
    createKernelEllipsoid(ker, rx, ry, rz);
    //printf("creating kernel done\n");
    //for(j=0;j<(2*rx+1)*(2*ry+1)*(2*rz+1);j++) printf("test %d %d \n",j,ker[j]);
    int idx;


    for (i = 0; i < p; i++) {
        printf("3D jni median filter : %d %% completed \n", ((i + 1)*100) / p);
        iarr = (*env)->NewShortArray(env, w * h);
        for (j = 0; j < s; j++) {
            c = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + w * y + x >= 0) && (j + w * y + x < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                pix[c] = (element[i + z][j + y * w + x]&0xffff);
                                c++;
                            }
                        }
                        idx++;
                    }
                }
            }

            //  Find median. Modified algorithm according to
            //  http://www.geocities.com/zabrodskyvlada/3alg.html Contributed by HeinzKlar.
            //  (copied form ij.plugin.filter.rankfilters)
            nValues = c;
            nv1b2 = (nValues - 1) / 2;
            ll = 0;
            mm = nValues - 1;
            med = pix[nv1b2];

            while (ll < mm) {
                ii = ll;
                jj = mm;
                do {
                    while (pix[ii] < med) {
                        ii++;
                    }
                    while (med < pix[jj]) {
                        jj--;
                    }
                    dum = pix[jj];
                    pix[jj] = pix[ii];
                    pix[ii] = dum;
                    ii++;
                    jj--;
                } while ((jj >= nv1b2) && (ii <= nv1b2));
                if (jj < nv1b2) {
                    ll = ii;
                }
                if (nv1b2 < ii) {
                    mm = jj;
                }
                med = pix[nv1b2];
            }
            tmp[j] = med;
        }
        (*env)->SetShortArrayRegion(env, iarr, 0, w*h, tmp);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }


    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }
    free(tmp);
    free(ker);
    free(pix);

    return result;
}

/*
 * Class:     filters3d_
 * Method:    jniTopHat3D
 * Signature: ([[SIIIIII)[[S
 */
JNIEXPORT jobjectArray JNICALL Java_filters3d_1_jniTopHat3D_116
(JNIEnv * env, jobject obj, jobjectArray tab, jint w, jint h, jint p, jint rx, jint ry, jint rz) {
    int x, y, z;
    jobjectArray result;
    int i;

    jclass intArrCls = (*env)->FindClass(env, "[S");
    if (intArrCls == NULL) {
        printf("Exception 0\n");
        return NULL; /* exception thrown */
    }
    result = (*env)->NewObjectArray(env, p, intArrCls, NULL);
    if (result == NULL) {
        printf("Exception 1\n");
        return NULL; /* out of memory error thrown */
    }

    jshortArray oneDim[p];
    jshort * element[p];
    for (i = 0; i < p; i++) {
        oneDim[i] = (*env)->GetObjectArrayElement(env, tab, i);
        element[i] = (*env)->GetShortArrayElements(env, oneDim[i], 0);
    }

    int sum = 0;
    int c = 0;
    //float rx2 = (rx != 0) ? rx * rx : 1;
    //float ry2 = (ry != 0) ? ry * ry : 1;
    //float rz2 = (rz != 0) ? rz * rz : 1;
    //float dist;
    int s = w*h;
    int j;

    int nbp = s*p;

    jshort* tmpmin = (jshort*) malloc(w * h * p * sizeof (jshort));
    jshortArray iarr;



    jbyte* ker = (jbyte *) malloc((2 * rx + 1) * (2 * ry + 1) * (2 * rz + 1) * sizeof (jbyte));
    createKernelEllipsoid(ker, rx, ry, rz);
    int idx;


    for (i = 0; i < p; i++) {
        printf("3D jni min filter : %d %% completed\n", ((i + 1)*100) / p);
        //iarr = (*env)->NewByteArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 1000000;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        if ((i + z >= 0) && (i + z < p) && (j + x + w * y >= 0) && (j + x + w * y < s)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = element[i + z][j + x + w * y]&0xffff;
                                if (c < sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmpmin[i * s + j] = sum;
        }
    }


    // MAX FILTER
    jshort* tmpminmax = (jshort*) malloc(w * h * sizeof (jshort));
    int offset = 0;
    for (i = 0; i < p; i++) {
        printf("3D jni max filter : %d %% completed\n", ((i + 1)*100) / p);
        iarr = (*env)->NewShortArray(env, w * h);
        for (j = 0; j < s; j++) {
            sum = 0;
            idx = 0;
            for (z = -rz; z <= rz; z++) {
                for (y = -ry; y <= ry; y++) {
                    for (x = -rx; x <= rx; x++) {
                        offset = (i + z) * s + j + x + w * y;
                        if ((offset >= 0) && (offset < nbp)) {
                            //dist = (x * x) / rx2 + (y * y) / ry2 + (z * z) / rz2;
                            if (ker[idx]) {
                                c = tmpmin[offset];
                                if (c > sum)
                                    sum = c;
                            }
                        }
                        idx++;
                    }
                }
            }
            tmpminmax[j] = sum;
        }
        (*env)->SetShortArrayRegion(env, iarr, 0, s, tmpminmax);
        (*env)->SetObjectArrayElement(env, result, i, iarr);
        (*env)->DeleteLocalRef(env, iarr);
    }

    for (i = 0; i < p; i++) {
        (*env)->DeleteLocalRef(env, oneDim[i]);
    }

    free(tmpmin);
    free(tmpminmax);
    free(ker);

    return result;
}

void createKernelEllipsoid(jbyte* ker, int vx, int vy, int vz) {
    double dist = 0;

    double rx2 = vx * vx;
    double ry2 = vy * vy;
    double rz2 = vz * vz;

    if (rx2 != 0) {
        rx2 = 1.0 / rx2;
    } else {
        rx2 = 0;
    }
    if (ry2 != 0) {
        ry2 = 1.0 / ry2;
    } else {
        ry2 = 0;
    }
    if (rz2 != 0) {
        rz2 = 1.0 / rz2;
    } else {
        rz2 = 0;
    }

    int idx = 0;
    int k, j, i;
    for (k = -vz; k <= vz; k++) {
        for (j = -vy; j <= vy; j++) {
            for (i = -vx; i <= vx; i++) {
                dist = ((double) (i * i)) * rx2 + ((double) (j * j)) * ry2 + ((double) (k * k)) * rz2;
                if (dist <= 1.0) {
                    ker[idx] = 1;
                } else {
                    ker[idx] = 0;
                }
                idx++;
            }
        }
    }
}


