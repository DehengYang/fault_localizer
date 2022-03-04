debug="-Xdebug -Xrunjdwp:transport=dt_socket,address=8000,server=y"
#debug=""

cd ../
./package.sh

cd /mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6;
            export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF8 -Duser.language=en-US -Duser.country=US -Duser.language=en";
            TZ="America/New_York"; export TZ;
            export PATH="/home/apr/env/jdk1.8.0_202/bin/:$PATH";
            export JAVA_HOME="/home/apr/env/jdk1.8.0_202/bin/";
            time java $debug -Xmx4g -Xms1g -cp /mnt/data/2021_11_multi_chunk_repair/APRConfig/APRConfig/../fl_modules/fault_localizer/versions/gzoltar_localizer-0.0.1-SNAPSHOT-jar-with-dependencies.jar apr.module.fl.main.Main \
                --externalProjPath /mnt/data/2021_11_multi_chunk_repair/APRConfig/APRConfig/../patch_validator/patch_validator/versions/PatchTest-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
                --srcJavaDir /mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/source/ \
                --binJavaDir /mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/build/ \
                --binTestDir /mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/build-tests/ \
                --jvmPath /home/apr/env/jdk1.7.0_80/bin/ \
                --failedTests org.jfree.chart.util.junit.ShapeListTests \
                --dependencies /mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/build/:/mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/build-tests/:/mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/lib/junit.jar:/mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/lib/servlet.jar:/mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6/lib/itext-2.0.6.jar:/mnt/data/2021_11_multi_chunk_repair/APRConfig/datasets/defects4j/framework/projects/lib/junit-4.11.jar \
                --outputDir /mnt/data/2021_11_multi_chunk_repair/APRConfig/APRConfig/../results_defects4j/defects4j_Chart_6/dataset/../gzoltar \
                --workingDir /mnt/data/2021_11_multi_chunk_repair/APRConfig/results_defects4j/defects4j_Chart_6/defects4j_Chart_6;
