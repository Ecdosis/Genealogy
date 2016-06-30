#!/bin/bash
if [ ! -d genealogy ]; then
  mkdir genealogy
  if [ $? -ne 0 ] ; then
    echo "couldn't create genealogy directory"
    exit
  fi
fi
if [ ! -d genealogy/WEB-INF ]; then
  mkdir genealogy/WEB-INF
  if [ $? -ne 0 ] ; then
    echo "couldn't create genealogy/WEB-INF directory"
    exit
  fi
fi
if [ ! -d genealogy/WEB-INF/lib ]; then
  mkdir genealogy/WEB-INF/lib
  if [ $? -ne 0 ] ; then
    echo "couldn't create genealogy/WEB-INF/lib directory"
    exit
  fi
fi
rm -f genealogy/WEB-INF/lib/*.jar
cp dist/Genealogy.jar genealogy/WEB-INF/lib/
cp web.xml genealogy/WEB-INF/
jar cf genealogy.war -C genealogy WEB-INF 
echo "NB: you MUST copy the contents of tomcat-bin to \$tomcat_home/bin"
