#!/usr/bin/env bash

dir="temp"
rm -rf ${dir}
mkdir ${dir}

echo "Compiling sources..."
javac -d ${dir} -cp ./Testers/ImplementorTest.jar src/ru/ifmo/ctddev/tolmachev/implementor/*.java

echo "Creating jar..."
echo "Manifest-Version: 1.0
Main-Class: ru.ifmo.ctddev.tolmachev.implementor.Implementor
Class-Path: ./Testers/ImplementorTest.jar" >> Manifest.txt

jar cfm MyImplementor.jar Manifest.txt -C temp . && echo "MyImplementor.jar created in current directory"

rm -f Manifest.txt
rm -fr ${dir}