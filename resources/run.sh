java -jar server_installer.jar $1

newestDir=`ls -ltr servers/ | grep '^d' | tail -1 | awk '{print $9}'`
cd servers/$newestDir/
chmod +x run.sh
bash run.sh
