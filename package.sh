cd ~/env
java -version
./change-jdk-version.sh 8
cd -
mvn clean package -DskipTests

[ ! -d "versions" ] && echo "mkdir versions" && mkdir versions

cp target/gzoltar_localizer-0.0.1-SNAPSHOT-jar-with-dependencies.jar versions/
cd ~/env
./change-jdk-version.sh 8
