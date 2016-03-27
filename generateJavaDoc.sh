#!/usr/bin/env bash

MY_SOURCE="./ru/ifmo/ctddev/tolmachev/"
KG_SOURCE="./PackageForJavadoc/info/kgeorgiy/java/advanced/"
URL="http://docs.oracle.com/javase/7/docs/api/"

#Implementor
SOURCE_DIR_FOR_HM5="${MY_SOURCE}implementor/*.java ${KG_SOURCE}implementor/*.java"
DOC_DIR_FOR_HM5="JavadocForHM5"

case "$1" in
    *)
    javadoc -linkoffline ${URL} ${URL} -private -d ${DOC_DIR_FOR_HM5} ${SOURCE_DIR_FOR_HM5}
    ;;
    esac


