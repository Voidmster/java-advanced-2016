#!/usr/bin/env bash

MY_SOURCE="./src/ru/ifmo/ctddev/tolmachev/"
KG_SOURCE="./PackageForJavadoc/info/kgeorgiy/java/advanced/"
URL="http://docs.oracle.com/javase/7/docs/api/"

#Implementor
SOURCE_DIR_FOR_HM5="${MY_SOURCE}implementor/*.java ${KG_SOURCE}implementor/*.java"
DOC_DIR_FOR_HM5="JavadocForHM5"

#Iterative parallelism
SOURCE_DIR_FOR_HM6="${MY_SOURCE}concurrent/*.java ${KG_SOURCE}concurrent/*.java"
DOC_DIR_FOR_HM6="JavadocForHM6"

#ParallelMapperImpl
SOURCE_DIR_FOR_HM7="${MY_SOURCE}mapper/*.java ${MY_SOURCE}concurrent/*.java ${KG_SOURCE}mapper/*.java ${KG_SOURCE}concurrent/*.java"
DOC_DIR_FOR_HM7="JavadocForHM7"

#WebCrawler
SOURCE_DIR_FOR_HM8="${MY_SOURCE}crawler/*.java ${KG_SOURCE}crawler/*.java"
DOC_DIR_FOR_HM8="JavadocForHM8"

case "$1" in
    hm5)
    javadoc -linkoffline ${URL} ${URL} -private -d ${DOC_DIR_FOR_HM5} ${SOURCE_DIR_FOR_HM5}
    ;;
    hm6)
    javadoc -linkoffline ${URL} ${URL} -d ${DOC_DIR_FOR_HM6} ${SOURCE_DIR_FOR_HM6}
    ;;
    hm7)
    javadoc -linkoffline ${URL} ${URL} -d ${DOC_DIR_FOR_HM7} ${SOURCE_DIR_FOR_HM7}
    ;;
    hm8)
    javadoc -linkoffline ${URL} ${URL} -d ${DOC_DIR_FOR_HM8} ${SOURCE_DIR_FOR_HM8}
    ;;
    *)
    javadoc -linkoffline ${URL} ${URL} -private -d ${DOC_DIR_FOR_HM5} ${SOURCE_DIR_FOR_HM5}
    javadoc -linkoffline ${URL} ${URL} -d ${DOC_DIR_FOR_HM6} ${SOURCE_DIR_FOR_HM6}
    ;;
    esac


