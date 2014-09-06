#!/bin/sh 

files=( *.xml )
for file in "${files[@]}"
do
  filename="${file##*/}"
  filenameWithoutExtension="${filename%.*}"
  echo "converting $filenameWithoutExtension ..."
  ./xml2json  -t xml2json $file --strip_text --strip_namespace --pretty -o "$filenameWithoutExtension".json
done
