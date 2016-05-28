#!/usr/bin/env bash

CLASSPATH="./Jars/*:out/production/Homework"

FLAGS="-cp"

TESTS_PACKAGE="info.kgeorgiy.java.advanced."

SOLUTIONS_PATH="ru.ifmo.ctddev.tolmachev."

case "$1" in
	"hm1")
	ARGS=${TESTS_PACKAGE}"walk.Tester RecursiveWalk "${SOLUTIONS_PATH}"walk.RecursiveWalk"
	CLASSPATH=${CLASSPATH}":./Testers/WalkTest.jar";
	;;
	"hm2")
	ARGS=${TESTS_PACKAGE}"arrayset.Tester NavigableSet "${SOLUTIONS_PATH}"arrayset.ArraySet"
	CLASSPATH=${CLASSPATH}":./Testers/ArraySetTest.jar";
	;;
	"hm3")
	ARGS=${TESTS_PACKAGE}"implementor.Tester class "${SOLUTIONS_PATH}"implementor.Implementor"
	CLASSPATH=${CLASSPATH}":./Testers/ImplementorTest.jar";
	;;
	"hm4")
    ARGS=${TESTS_PACKAGE}"implementor.Tester jar-class "${SOLUTIONS_PATH}"implementor.Implementor"
	CLASSPATH=${CLASSPATH}":./Testers/ImplementorTest.jar";
	;;
	"hm6")
    ARGS=${TESTS_PACKAGE}"concurrent.Tester list "${SOLUTIONS_PATH}"concurrent.IterativeParallelism"
	CLASSPATH=${CLASSPATH}":./Testers/IterativeParallelismTest.jar:./Testers/ParallelMapperTest.jar";
	;;
	"hm7")
    ARGS=${TESTS_PACKAGE}"mapper.Tester list "${SOLUTIONS_PATH}"mapper.ParallelMapperImpl,"${SOLUTIONS_PATH}"concurrent.IterativeParallelism"
	CLASSPATH=${CLASSPATH}":./Testers/ParallelMapperTest.jar";
	;;
	"hm8")
    ARGS=${TESTS_PACKAGE}"crawler.Tester hard "${SOLUTIONS_PATH}"crawler.WebCrawler"
	CLASSPATH=${CLASSPATH}":./Testers/WebCrawlerTest.jar";
	;;
	"hm9")
	case "$2" in
        "server")
        ARGS=${TESTS_PACKAGE}"hello.Tester server "${SOLUTIONS_PATH}"hello.HelloUDPServer"
        CLASSPATH=${CLASSPATH}":./Testers/HelloUDPTest.jar";
        java ${FLAGS} ${CLASSPATH} ${ARGS} $3
        exit
        ;;
        "client")
        ARGS=${TESTS_PACKAGE}"hello.Tester client "${SOLUTIONS_PATH}"hello.HelloUDPClient"
        CLASSPATH=${CLASSPATH}":./Testers/HelloUDPTest.jar";
        java ${FLAGS} ${CLASSPATH} ${ARGS} $3
        exit
        ;;
        *)
        ARGS=${TESTS_PACKAGE}"hello.Tester client "${SOLUTIONS_PATH}"hello.HelloUDPClient"
        CLASSPATH=${CLASSPATH}":./Testers/HelloUDPTest.jar";
        java ${FLAGS} ${CLASSPATH} ${ARGS} $2
        ARGS=${TESTS_PACKAGE}"hello.Tester server "${SOLUTIONS_PATH}"hello.HelloUDPServer"
        CLASSPATH=${CLASSPATH}":./Testers/HelloUDPTest.jar";
        java ${FLAGS} ${CLASSPATH} ${ARGS} $2
        exit
        ;;
        esac

	;;
	*)
	echo "unexpected command" $1
	exit
	;;
	esac
java ${FLAGS} ${CLASSPATH} ${ARGS} $2
