JAVA_HOME=/usr/java/jdk1.6.0_06
CONDOR_HOME=/usr/local/packages/condor
JAR=condor-drmaa.jar
CONDOR_INC=${CONDOR_HOME}/include
LDFLAGS=-Wall
CFLAGS=-fPIC -Wall

default: all

build:
	-mkdir build

dist: build
	-mkdir dist

patch:
	-cp "${CONDOR_HOME}/src/drmaa/libDrmaa.c" build/
	-patch build/libDrmaa.c libDrmaa.c.patch

libcondorjdrmaa.so: dist patch
	@echo "Compiling C code for the JNI layer"
	gcc ${CFLAGS} -I "${CONDOR_INC}" -I "${JAVA_HOME}/include/" -I "${JAVA_HOME}/include/linux/" -c SessionImpl.c -o build/SessionImpl.o
	cp ${CONDOR_HOME}/lib/libcondordrmaa.a build/
	pushd build && ar -x libcondordrmaa.a 
	gcc -DCONDOR_DRMAA_STANDALONE ${CFLAGS} -I "${CONDOR_HOME}/src/drmaa/" -c build/libDrmaa.c -o build/libDrmaa.o
	gcc ${LDFLAGS} -shared build/*.o -o dist/libcondorjdrmaa.so

clean:
	@if [ -d build ]; then rm -rf build; fi
	@if [ -f build.err ]; then rm build.err; fi
	@if [ -f build.log ]; then rm build.log; fi
	@ant clean 

all: libcondorjdrmaa.so build.xml
	@echo "Compiling Java code"
	@ant > build.log 2> build.err
	@echo "================================================================="
	@echo "The ${JAR} and shared library are in the dist directory"
	@echo "================================================================="
