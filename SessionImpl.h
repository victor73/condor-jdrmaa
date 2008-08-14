#include <jni.h>
/* Header for class SessionImpl */

#ifndef _Included_SessionImpl
#define _Included_SessionImpl
#ifdef __cplusplus
extern "C" {
#endif
#undef SessionImpl_SUSPEND
#define SessionImpl_SUSPEND 0L
#undef SessionImpl_RESUME
#define SessionImpl_RESUME 1L
#undef SessionImpl_HOLD
#define SessionImpl_HOLD 2L
#undef SessionImpl_RELEASE
#define SessionImpl_RELEASE 3L
#undef SessionImpl_TERMINATE
#define SessionImpl_TERMINATE 4L
/* Inaccessible static: JOB_IDS_SESSION_ALL */
#undef SessionImpl_TIMEOUT_WAIT_FOREVER
#define SessionImpl_TIMEOUT_WAIT_FOREVER -1LL
#undef SessionImpl_TIMEOUT_NO_WAIT
#define SessionImpl_TIMEOUT_NO_WAIT 0LL
#undef SessionImpl_UNDETERMINED
#define SessionImpl_UNDETERMINED 0L
#undef SessionImpl_QUEUED_ACTIVE
#define SessionImpl_QUEUED_ACTIVE 16L
#undef SessionImpl_SYSTEM_ON_HOLD
#define SessionImpl_SYSTEM_ON_HOLD 17L
#undef SessionImpl_USER_ON_HOLD
#define SessionImpl_USER_ON_HOLD 18L
#undef SessionImpl_USER_SYSTEM_ON_HOLD
#define SessionImpl_USER_SYSTEM_ON_HOLD 19L
#undef SessionImpl_RUNNING
#define SessionImpl_RUNNING 32L
#undef SessionImpl_SYSTEM_SUSPENDED
#define SessionImpl_SYSTEM_SUSPENDED 33L
#undef SessionImpl_USER_SUSPENDED
#define SessionImpl_USER_SUSPENDED 34L
#undef SessionImpl_DONE
#define SessionImpl_DONE 48L
#undef SessionImpl_FAILED
#define SessionImpl_FAILED 64L
/*
 * Class:     SessionImpl
 * Method:    nativeControl
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeControl
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     SessionImpl
 * Method:    nativeExit
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeExit
  (JNIEnv *, jobject);

/*
 * Class:     SessionImpl
 * Method:    nativeGetContact
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_igs_SessionImpl_nativeGetContact
  (JNIEnv *, jobject);

/*
 * Class:     SessionImpl
 * Method:    nativeGetDRMSInfo
 * Signature: ()Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_igs_SessionImpl_nativeGetDRMSInfo
  (JNIEnv *, jobject);

/*
 * Class:     SessionImpl
 * Method:    nativeGetJobProgramStatus
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_net_sf_igs_SessionImpl_nativeGetJobProgramStatus
  (JNIEnv *, jobject, jstring);

/*
 * Class:     SessionImpl
 * Method:    nativeInit
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeInit
  (JNIEnv *, jobject, jstring);

/*
 * Class:     SessionImpl
 * Method:    nativeRunBulkJobs
 * Signature: (IIII)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_sf_igs_SessionImpl_nativeRunBulkJobs
  (JNIEnv *, jobject, jint, jint, jint, jint);

/*
 * Class:     SessionImpl
 * Method:    nativeRunJob
 * Signature: (I)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_net_sf_igs_SessionImpl_nativeRunJob
  (JNIEnv *, jobject, jint);

/*
 * Class:     SessionImpl
 * Method:    nativeSynchronize
 * Signature: ([Ljava/lang/String;JZ)V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeSynchronize
  (JNIEnv *, jobject, jobjectArray, jlong, jboolean);

/*
 * Class:     SessionImpl
 * Method:    nativeWait
 * Signature: (Ljava/lang/String;J)Lcom/sun/grid/drmaa/SGEJobInfo;
 */
JNIEXPORT jobject JNICALL Java_net_sf_igs_SessionImpl_nativeWait
  (JNIEnv *, jobject, jstring, jlong);

/*
 * Class:     SessionImpl
 * Method:    nativeAllocateJobTemplate
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_net_sf_igs_SessionImpl_nativeAllocateJobTemplate
  (JNIEnv *, jobject);

/*
 * Class:     SessionImpl
 * Method:    nativeSetAttributeValue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeSetAttributeValue
  (JNIEnv *, jobject, jint, jstring, jstring);

/*
 * Class:     SessionImpl
 * Method:    nativeSetAttributeValues
 * Signature: (ILjava/lang/String;[Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeSetAttributeValues
  (JNIEnv *, jobject, jint, jstring, jobjectArray);

/*
 * Class:     SessionImpl
 * Method:    nativeGetAttributeNames
 * Signature: (I)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_sf_igs_SessionImpl_nativeGetAttributeNames
  (JNIEnv *, jobject, jint);

/*
 * Class:     SessionImpl
 * Method:    nativeGetAttribute
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_net_sf_igs_SessionImpl_nativeGetAttribute
  (JNIEnv *, jobject, jint, jstring);

/*
 * Class:     SessionImpl
 * Method:    nativeDeleteJobTemplate
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_net_sf_igs_SessionImpl_nativeDeleteJobTemplate
  (JNIEnv *, jobject, jint);

#ifdef __cplusplus
}
#endif
#endif
