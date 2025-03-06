@echo off
echo Installing FrizzlenEco to local Maven repository...
call mvn install:install-file -Dfile="C:\Users\Ben\Documents\Development\townai\FrizzlenEco\target\FrizzlenEco-1.0-SNAPSHOT.jar" -DgroupId=org.frizzlenpop -DartifactId=FrizzlenEco -Dversion=1.0-SNAPSHOT -Dpackaging=jar
echo Done!
pause 