JAVA_HOME=/usr/java/jdk1.6.0_06
CONDOR_HOME=/usr/local/condor-7.0.2
JAR=condor-drmaa.jar
CONDOR_INC=$(shell dirname `find ${CONDOR_HOME} -name drmaa.h`)
LDFLAGS=-Wall
CFLAGS=-fPIC -Wall

default: all

build:
	-mkdir build

dist: build
	-mkdir dist

libcondorjdrmaa.so: dist
	@echo "Compiling C code for the JNI layer"
	gcc ${CFLAGS} -I "${CONDOR_INC}" -I "${JAVA_HOME}/include/" -I "${JAVA_HOME}/include/linux/" -c SessionImpl.c -o build/SessionImpl.o
	cp ${CONDOR_INC}/libcondordrmaa.a build/
	pushd build && ar -x libcondordrmaa.a 
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
