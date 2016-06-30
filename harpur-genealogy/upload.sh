#!/bin/bash
function readfile {
    value=$(<$1)
    echo "$value"
}
udata="{\"name\":\"desmond\",\"roles\": [\"editor\"]}"
encrypted=`./encrypt "$udata"`
curl --data "docid=english/harpur/root&userdata=$encrypted" http://localhost:8096/genealogy/delete
curl --data "docid=english/harpur/root&userdata=$encrypted" http://localhost:8096/genealogy/create
for f in *.json
do
   end=$((${#f} - 5))
   docid="english/harpur/"${f:0:$end}
   json=`readfile $f`
   curl --data "record=$json&docid=$docid" http://localhost:8096/genealogy
done
