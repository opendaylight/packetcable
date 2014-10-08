#!/bin/bash
OLD="@header@"

DPATH="*.java"
BPATH="bakup"
TFILE="/tmp/out.tmp.$$"
[ ! -d $BPATH ] && mkdir -p $BPATH || :
for f in `egrep -lir --include=*.java "@header@" .`
do
  if [ -f $f -a -r $f ]; then
   echo "$f"
   #/bin/cp -f $f $BPATH
  sed '/@header@/{                                                                      
    s/@header@//g
    r docs/copyright.txt
}' "$f" > $TFILE && mv $TFILE "$f"
  else
   echo "Error: Cannot read $f"
  fi
done
/bin/rm $TFILE
