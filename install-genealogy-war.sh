#!/bin/bash
service tomcat7 stop
cp genealogy.war /var/lib/tomcat7/webapps/
rm -rf /var/lib/tomcat7/webapps/genealogy
rm -rf /var/lib/tomcat7/work/Catalina/localhost/
service tomcat7 start
