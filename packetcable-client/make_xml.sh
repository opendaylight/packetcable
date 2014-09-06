#!/bin/sh 

files=( *.json )
for file in "${files[@]}"
do
  filename="${file##*/}"
  filenameWithoutExtension="${filename%.*}"
  echo "converting $filenameWithoutExtension ..."
  ./xml2json  -t json2xml $file --strip_text --strip_namespace --pretty -o "$filenameWithoutExtension".xml
done
