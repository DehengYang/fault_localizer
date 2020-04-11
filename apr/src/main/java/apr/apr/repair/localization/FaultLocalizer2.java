/**
 * 
 */
package apr.apr.repair.localization;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import apr.apr.repair.utils.CmdUtil;
import apr.apr.repair.utils.FileUtil;
import apr.apr.repair.utils.Pair;


/**
 * support GZoltar 1.7.3
 * @author apr
 * @version Apr 1, 2020
 *
 */
public class FaultLocalizer2 {
	final static Logger logger = LoggerFactory.getLogger(FaultLocalizer2.class);
	
	// parameters we need
	private String data_dir; //to save fl results
	private String bug_dir;
	private String test_classpath;
	private String test_classes_dir;
	private String src_classes_dir;
	private String src_classes_file;
	private String all_tests_file;
	private String junit_jar;
	
	private String unitTestsPath = null;
	
	private List<String> failedMethods = new ArrayList<>();;
	
	// test:
	// 1) other benchmark bugs
	
	public FaultLocalizer2(){
		data_dir = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/FL";
//		data_dir = new File(FileUtil.buggylocDir).getAbsolutePath() + "/FL"; // direcory
//		bug_dir = FileUtil.bugDir;
		
		//test_classpath = FileUtil.dependencies;
		test_classpath = "";
		for (String dep : FileUtil.depsList){
			test_classpath += dep + ":";
		}
		
		test_classes_dir = FileUtil.binTestDir;
		src_classes_dir = FileUtil.binJavaDir;
		src_classes_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/srcClasses.txt";
		all_tests_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/testClasses.txt";
//		junit_jar = FileUtil.junitJar;
	}
	
	public FaultLocalizer2(String unitTestsPath){
		data_dir = new File(FileUtil.buggylocDir).getAbsolutePath() + "/" + FileUtil.toolName + "/secondFL";
//		data_dir = new File(FileUtil.buggylocDir).getAbsolutePath() + "/FL"; // direcory
//		bug_dir = FileUtil.bugDir;
		
		//test_classpath = FileUtil.dependencies;
		test_classpath = "";
		for (String dep : FileUtil.depsList){
			test_classpath += dep + ":";
		}
		
		test_classes_dir = FileUtil.binTestDir;
		src_classes_dir = FileUtil.binJavaDir;
		src_classes_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/srcClasses.txt";
		all_tests_file = new File(FileUtil.buggylocDir).getAbsolutePath() + "/testClasses.txt";
		
		this.unitTestsPath = unitTestsPath;
//		junit_jar = FileUtil.junitJar;
	}
	
	/**
	 * @Description get fl results 
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void localize(){
		// debug
//		test_classpath = "/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.9/target/test-classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-common/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.8/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.10/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.11/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.12/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-2.0/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-2.1/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-2.2/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod/target/classes:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-common/2.23.0-SNAPSHOT/webservices.rest-omod-common-2.23.0-SNAPSHOT.jar:/home/apr/env/mavenDownload/joda-time/joda-time/2.9.2/joda-time-2.9.2.jar:/home/apr/env/mavenDownload/org/atteo/evo-inflector/1.2.1/evo-inflector-1.2.1.jar:/home/apr/env/mavenDownload/io/swagger/swagger-core/1.5.13/swagger-core-1.5.13.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/dataformat/jackson-dataformat-yaml/2.8.5/jackson-dataformat-yaml-2.8.5.jar:/home/apr/env/mavenDownload/org/yaml/snakeyaml/1.17/snakeyaml-1.17.jar:/home/apr/env/mavenDownload/io/swagger/swagger-models/1.5.13/swagger-models-1.5.13.jar:/home/apr/env/mavenDownload/io/swagger/swagger-annotations/1.5.13/swagger-annotations-1.5.13.jar:/home/apr/env/mavenDownload/com/google/guava/guava/20.0/guava-20.0.jar:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-common/2.23.0-SNAPSHOT/webservices.rest-omod-common-2.23.0-SNAPSHOT-tests.jar:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-1.8/2.23.0-SNAPSHOT/webservices.rest-omod-1.8-2.23.0-SNAPSHOT.jar:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-1.8/2.23.0-SNAPSHOT/webservices.rest-omod-1.8-2.23.0-SNAPSHOT-tests.jar:/home/apr/env/mavenDownload/org/openmrs/api/openmrs-api/1.9.10/openmrs-api-1.9.10.jar:/home/apr/env/mavenDownload/commons-collections/commons-collections/3.2/commons-collections-3.2.jar:/home/apr/env/mavenDownload/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/home/apr/env/mavenDownload/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:/home/apr/env/mavenDownload/commons-io/commons-io/1.4/commons-io-1.4.jar:/home/apr/env/mavenDownload/org/azeckoski/reflectutils/0.9.14/reflectutils-0.9.14.jar:/home/apr/env/mavenDownload/org/apache/velocity/velocity/1.6.2/velocity-1.6.2.jar:/home/apr/env/mavenDownload/commons-lang/commons-lang/2.4/commons-lang-2.4.jar:/home/apr/env/mavenDownload/log4j/log4j/1.2.15/log4j-1.2.15.jar:/home/apr/env/mavenDownload/org/springframework/spring-core/3.0.5.RELEASE/spring-core-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-asm/3.0.5.RELEASE/spring-asm-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-beans/3.0.5.RELEASE/spring-beans-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-context/3.0.5.RELEASE/spring-context-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-expression/3.0.5.RELEASE/spring-expression-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-aop/3.0.5.RELEASE/spring-aop-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/aopalliance/aopalliance/1.0/aopalliance-1.0.jar:/home/apr/env/mavenDownload/org/springframework/spring-orm/3.0.5.RELEASE/spring-orm-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-tx/3.0.5.RELEASE/spring-tx-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-jdbc/3.0.5.RELEASE/spring-jdbc-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/antlr/antlr-runtime/3.4/antlr-runtime-3.4.jar:/home/apr/env/mavenDownload/org/antlr/stringtemplate/3.2.1/stringtemplate-3.2.1.jar:/home/apr/env/mavenDownload/antlr/antlr/2.7.7/antlr-2.7.7.jar:/home/apr/env/mavenDownload/asm/asm-commons/2.2.3/asm-commons-2.2.3.jar:/home/apr/env/mavenDownload/asm/asm-tree/2.2.3/asm-tree-2.2.3.jar:/home/apr/env/mavenDownload/asm/asm/2.2.3/asm-2.2.3.jar:/home/apr/env/mavenDownload/asm/asm-util/2.2.3/asm-util-2.2.3.jar:/home/apr/env/mavenDownload/cglib/cglib-nodep/2.2/cglib-nodep-2.2.jar:/home/apr/env/mavenDownload/ca/uhn/hapi/hapi/0.5/hapi-0.5.jar:/home/apr/env/mavenDownload/org/openmrs/simpleframework/simple-xml/1.6.1-mod/simple-xml-1.6.1-mod.jar:/home/apr/env/mavenDownload/stax/stax/1.2.0/stax-1.2.0.jar:/home/apr/env/mavenDownload/stax/stax-api/1.0.1/stax-api-1.0.1.jar:/home/apr/env/mavenDownload/javassist/javassist/3.12.1.GA/javassist-3.12.1.GA.jar:/home/apr/env/mavenDownload/org/openmrs/hibernate/hibernate-core/3.6.5.Final-mod/hibernate-core-3.6.5.Final-mod.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-commons-annotations/3.2.0.Final/hibernate-commons-annotations-3.2.0.Final.jar:/home/apr/env/mavenDownload/org/hibernate/javax/persistence/hibernate-jpa-2.0-api/1.0.0.Final/hibernate-jpa-2.0-api-1.0.0.Final.jar:/home/apr/env/mavenDownload/javax/transaction/jta/1.1/jta-1.1.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-c3p0/3.6.0.Final/hibernate-c3p0-3.6.0.Final.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-ehcache/3.6.0.Final/hibernate-ehcache-3.6.0.Final.jar:/home/apr/env/mavenDownload/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar:/home/apr/env/mavenDownload/c3p0/c3p0/0.9.1/c3p0-0.9.1.jar:/home/apr/env/mavenDownload/net/sf/ehcache/ehcache-core/2.2.0/ehcache-core-2.2.0.jar:/home/apr/env/mavenDownload/org/slf4j/slf4j-api/1.6.0/slf4j-api-1.6.0.jar:/home/apr/env/mavenDownload/org/slf4j/jcl-over-slf4j/1.6.0/jcl-over-slf4j-1.6.0.jar:/home/apr/env/mavenDownload/org/slf4j/slf4j-log4j12/1.6.0/slf4j-log4j12-1.6.0.jar:/home/apr/env/mavenDownload/com/thoughtworks/xstream/xstream/1.4.3/xstream-1.4.3.jar:/home/apr/env/mavenDownload/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar:/home/apr/env/mavenDownload/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:/home/apr/env/mavenDownload/javax/mail/mail/1.4.1/mail-1.4.1.jar:/home/apr/env/mavenDownload/javax/activation/activation/1.1/activation-1.1.jar:/home/apr/env/mavenDownload/org/liquibase/liquibase-core/2.0.5/liquibase-core-2.0.5.jar:/home/apr/env/mavenDownload/org/openmrs/liquibase/ext/modify-column/2.0.2/modify-column-2.0.2.jar:/home/apr/env/mavenDownload/org/openmrs/liquibase/ext/identity-insert/1.2.1/identity-insert-1.2.1.jar:/home/apr/env/mavenDownload/org/openmrs/liquibase/ext/type-converter/1.0.1/type-converter-1.0.1.jar:/home/apr/env/mavenDownload/xerces/xercesImpl/2.8.0/xercesImpl-2.8.0.jar:/home/apr/env/mavenDownload/xml-apis/xml-apis/1.3.03/xml-apis-1.3.03.jar:/home/apr/env/mavenDownload/xml-resolver/xml-resolver/1.1/xml-resolver-1.1.jar:/home/apr/env/mavenDownload/javax/validation/validation-api/1.0.0.GA/validation-api-1.0.0.GA.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-validator/4.2.0.Final/hibernate-validator-4.2.0.Final.jar:/home/apr/env/mavenDownload/org/openmrs/api/openmrs-api/1.9.10/openmrs-api-1.9.10-tests.jar:/home/apr/env/mavenDownload/org/openmrs/web/openmrs-web/1.9.10/openmrs-web-1.9.10.jar:/home/apr/env/mavenDownload/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar:/home/apr/env/mavenDownload/javax/servlet/jsp-api/2.0/jsp-api-2.0.jar:/home/apr/env/mavenDownload/javax/servlet/jstl/1.1.2/jstl-1.1.2.jar:/home/apr/env/mavenDownload/org/openmrs/directwebremoting/dwr/2.0.5-mod/dwr-2.0.5-mod.jar:/home/apr/env/mavenDownload/commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.jar:/home/apr/env/mavenDownload/net/sf/saxon/saxon/8.7/saxon-8.7.jar:/home/apr/env/mavenDownload/net/sf/saxon/saxon-dom/8.7/saxon-dom-8.7.jar:/home/apr/env/mavenDownload/org/springframework/spring-web/3.0.5.RELEASE/spring-web-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-webmvc/3.0.5.RELEASE/spring-webmvc-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-context-support/3.0.5.RELEASE/spring-context-support-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-oxm/3.0.5.RELEASE/spring-oxm-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/jfree/jfreechart/1.0.12/jfreechart-1.0.12.jar:/home/apr/env/mavenDownload/jfree/jcommon/1.0.15/jcommon-1.0.15.jar:/home/apr/env/mavenDownload/mysql/mysql-connector-java/5.1.28/mysql-connector-java-5.1.28.jar:/home/apr/env/mavenDownload/postgresql/postgresql/9.0-801.jdbc4/postgresql-9.0-801.jdbc4.jar:/home/apr/env/mavenDownload/net/sourceforge/jtds/jtds/1.2.4/jtds-1.2.4.jar:/home/apr/env/mavenDownload/taglibs/request/1.0.1/request-1.0.1.jar:/home/apr/env/mavenDownload/taglibs/response/1.0.1/response-1.0.1.jar:/home/apr/env/mavenDownload/taglibs/standard/1.1.2/standard-1.1.2.jar:/home/apr/env/mavenDownload/taglibs/page/1.0.1/page-1.0.1.jar:/home/apr/env/mavenDownload/org/codehaus/jackson/jackson-core-asl/1.5.0/jackson-core-asl-1.5.0.jar:/home/apr/env/mavenDownload/org/codehaus/jackson/jackson-mapper-asl/1.5.0/jackson-mapper-asl-1.5.0.jar:/home/apr/env/mavenDownload/org/apache/velocity/velocity-tools/2.0/velocity-tools-2.0.jar:/home/apr/env/mavenDownload/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:/home/apr/env/mavenDownload/commons-chain/commons-chain/1.1/commons-chain-1.1.jar:/home/apr/env/mavenDownload/commons-validator/commons-validator/1.3.1/commons-validator-1.3.1.jar:/home/apr/env/mavenDownload/oro/oro/2.0.8/oro-2.0.8.jar:/home/apr/env/mavenDownload/sslext/sslext/1.2-0/sslext-1.2-0.jar:/home/apr/env/mavenDownload/org/apache/struts/struts-core/1.3.8/struts-core-1.3.8.jar:/home/apr/env/mavenDownload/org/apache/struts/struts-taglib/1.3.8/struts-taglib-1.3.8.jar:/home/apr/env/mavenDownload/org/apache/struts/struts-tiles/1.3.8/struts-tiles-1.3.8.jar:/home/apr/env/mavenDownload/org/openmrs/web/openmrs-web/1.9.10/openmrs-web-1.9.10-tests.jar:/home/apr/env/mavenDownload/org/openmrs/test/openmrs-test/1.9.10/openmrs-test-1.9.10.pom:/home/apr/env/mavenDownload/org/springframework/spring-test/3.0.5.RELEASE/spring-test-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/com/h2database/h2/1.2.135/h2-1.2.135.jar:/home/apr/env/mavenDownload/org/databene/databene-benerator/0.5.9/databene-benerator-0.5.9.jar:/home/apr/env/mavenDownload/org/databene/databene-webdecs/0.4.9/databene-webdecs-0.4.9.jar:/home/apr/env/mavenDownload/org/apache/poi/poi/3.5-beta5/poi-3.5-beta5.jar:/home/apr/env/mavenDownload/org/freemarker/freemarker/2.3.9/freemarker-2.3.9.jar:/home/apr/env/mavenDownload/org/databene/databene-commons/0.4.9/databene-commons-0.4.9.jar:/home/apr/env/mavenDownload/org/databene/databene-gui/0.1.9/databene-gui-0.1.9.jar:/home/apr/env/mavenDownload/org/apache/derby/derbyclient/10.4.2.0/derbyclient-10.4.2.0.jar:/home/apr/env/mavenDownload/org/dbunit/dbunit/2.4.7/dbunit-2.4.7.jar:/home/apr/env/mavenDownload/xmlunit/xmlunit/1.3/xmlunit-1.3.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/core/jackson-databind/2.5.4/jackson-databind-2.5.4.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/core/jackson-annotations/2.5.0/jackson-annotations-2.5.0.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/core/jackson-core/2.5.4/jackson-core-2.5.4.jar:/home/apr/env/mavenDownload/commons-codec/commons-codec/1.5/commons-codec-1.5.jar:/home/apr/env/mavenDownload/junit/junit/4.11/junit-4.11.jar:/home/apr/env/mavenDownload/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/apr/env/mavenDownload/org/hamcrest/hamcrest-library/1.3/hamcrest-library-1.3.jar:/home/apr/env/mavenDownload/org/mockito/mockito-core/1.9.5/mockito-core-1.9.5.jar:/home/apr/env/mavenDownload/org/objenesis/objenesis/1.0/objenesis-1.0.jar";
		
		//		test_classpath = test_classes_dir + ":/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-common/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.8/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.10/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.11/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-1.12/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-2.0/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-2.1/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod-2.2/target/classes:/mnt/benchmarks/repairDir/Kali_Bears_openmrs-openmrs-module-webservices.rest_455565885-458312291/omod/target/classes:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-common/2.23.0-SNAPSHOT/webservices.rest-omod-common-2.23.0-SNAPSHOT.jar:/home/apr/env/mavenDownload/joda-time/joda-time/2.9.2/joda-time-2.9.2.jar:/home/apr/env/mavenDownload/org/atteo/evo-inflector/1.2.1/evo-inflector-1.2.1.jar:/home/apr/env/mavenDownload/io/swagger/swagger-core/1.5.13/swagger-core-1.5.13.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/dataformat/jackson-dataformat-yaml/2.8.5/jackson-dataformat-yaml-2.8.5.jar:/home/apr/env/mavenDownload/org/yaml/snakeyaml/1.17/snakeyaml-1.17.jar:/home/apr/env/mavenDownload/io/swagger/swagger-models/1.5.13/swagger-models-1.5.13.jar:/home/apr/env/mavenDownload/io/swagger/swagger-annotations/1.5.13/swagger-annotations-1.5.13.jar:/home/apr/env/mavenDownload/com/google/guava/guava/20.0/guava-20.0.jar:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-common/2.23.0-SNAPSHOT/webservices.rest-omod-common-2.23.0-SNAPSHOT-tests.jar:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-1.8/2.23.0-SNAPSHOT/webservices.rest-omod-1.8-2.23.0-SNAPSHOT.jar:/home/apr/env/mavenDownload/org/openmrs/module/webservices.rest-omod-1.8/2.23.0-SNAPSHOT/webservices.rest-omod-1.8-2.23.0-SNAPSHOT-tests.jar:/home/apr/env/mavenDownload/org/openmrs/api/openmrs-api/1.9.10/openmrs-api-1.9.10.jar:/home/apr/env/mavenDownload/commons-collections/commons-collections/3.2/commons-collections-3.2.jar:/home/apr/env/mavenDownload/org/apache/commons/commons-lang3/3.1/commons-lang3-3.1.jar:/home/apr/env/mavenDownload/commons-beanutils/commons-beanutils/1.7.0/commons-beanutils-1.7.0.jar:/home/apr/env/mavenDownload/commons-io/commons-io/1.4/commons-io-1.4.jar:/home/apr/env/mavenDownload/org/azeckoski/reflectutils/0.9.14/reflectutils-0.9.14.jar:/home/apr/env/mavenDownload/org/apache/velocity/velocity/1.6.2/velocity-1.6.2.jar:/home/apr/env/mavenDownload/commons-lang/commons-lang/2.4/commons-lang-2.4.jar:/home/apr/env/mavenDownload/log4j/log4j/1.2.15/log4j-1.2.15.jar:/home/apr/env/mavenDownload/org/springframework/spring-core/3.0.5.RELEASE/spring-core-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-asm/3.0.5.RELEASE/spring-asm-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-beans/3.0.5.RELEASE/spring-beans-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-context/3.0.5.RELEASE/spring-context-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-expression/3.0.5.RELEASE/spring-expression-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-aop/3.0.5.RELEASE/spring-aop-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/aopalliance/aopalliance/1.0/aopalliance-1.0.jar:/home/apr/env/mavenDownload/org/springframework/spring-orm/3.0.5.RELEASE/spring-orm-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-tx/3.0.5.RELEASE/spring-tx-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-jdbc/3.0.5.RELEASE/spring-jdbc-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/antlr/antlr-runtime/3.4/antlr-runtime-3.4.jar:/home/apr/env/mavenDownload/org/antlr/stringtemplate/3.2.1/stringtemplate-3.2.1.jar:/home/apr/env/mavenDownload/antlr/antlr/2.7.7/antlr-2.7.7.jar:/home/apr/env/mavenDownload/asm/asm-commons/2.2.3/asm-commons-2.2.3.jar:/home/apr/env/mavenDownload/asm/asm-tree/2.2.3/asm-tree-2.2.3.jar:/home/apr/env/mavenDownload/asm/asm/2.2.3/asm-2.2.3.jar:/home/apr/env/mavenDownload/asm/asm-util/2.2.3/asm-util-2.2.3.jar:/home/apr/env/mavenDownload/cglib/cglib-nodep/2.2/cglib-nodep-2.2.jar:/home/apr/env/mavenDownload/ca/uhn/hapi/hapi/0.5/hapi-0.5.jar:/home/apr/env/mavenDownload/org/openmrs/simpleframework/simple-xml/1.6.1-mod/simple-xml-1.6.1-mod.jar:/home/apr/env/mavenDownload/stax/stax/1.2.0/stax-1.2.0.jar:/home/apr/env/mavenDownload/stax/stax-api/1.0.1/stax-api-1.0.1.jar:/home/apr/env/mavenDownload/javassist/javassist/3.12.1.GA/javassist-3.12.1.GA.jar:/home/apr/env/mavenDownload/org/openmrs/hibernate/hibernate-core/3.6.5.Final-mod/hibernate-core-3.6.5.Final-mod.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-commons-annotations/3.2.0.Final/hibernate-commons-annotations-3.2.0.Final.jar:/home/apr/env/mavenDownload/org/hibernate/javax/persistence/hibernate-jpa-2.0-api/1.0.0.Final/hibernate-jpa-2.0-api-1.0.0.Final.jar:/home/apr/env/mavenDownload/javax/transaction/jta/1.1/jta-1.1.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-c3p0/3.6.0.Final/hibernate-c3p0-3.6.0.Final.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-ehcache/3.6.0.Final/hibernate-ehcache-3.6.0.Final.jar:/home/apr/env/mavenDownload/dom4j/dom4j/1.6.1/dom4j-1.6.1.jar:/home/apr/env/mavenDownload/c3p0/c3p0/0.9.1/c3p0-0.9.1.jar:/home/apr/env/mavenDownload/net/sf/ehcache/ehcache-core/2.2.0/ehcache-core-2.2.0.jar:/home/apr/env/mavenDownload/org/slf4j/slf4j-api/1.6.0/slf4j-api-1.6.0.jar:/home/apr/env/mavenDownload/org/slf4j/jcl-over-slf4j/1.6.0/jcl-over-slf4j-1.6.0.jar:/home/apr/env/mavenDownload/org/slf4j/slf4j-log4j12/1.6.0/slf4j-log4j12-1.6.0.jar:/home/apr/env/mavenDownload/com/thoughtworks/xstream/xstream/1.4.3/xstream-1.4.3.jar:/home/apr/env/mavenDownload/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar:/home/apr/env/mavenDownload/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:/home/apr/env/mavenDownload/javax/mail/mail/1.4.1/mail-1.4.1.jar:/home/apr/env/mavenDownload/javax/activation/activation/1.1/activation-1.1.jar:/home/apr/env/mavenDownload/org/liquibase/liquibase-core/2.0.5/liquibase-core-2.0.5.jar:/home/apr/env/mavenDownload/org/openmrs/liquibase/ext/modify-column/2.0.2/modify-column-2.0.2.jar:/home/apr/env/mavenDownload/org/openmrs/liquibase/ext/identity-insert/1.2.1/identity-insert-1.2.1.jar:/home/apr/env/mavenDownload/org/openmrs/liquibase/ext/type-converter/1.0.1/type-converter-1.0.1.jar:/home/apr/env/mavenDownload/xerces/xercesImpl/2.8.0/xercesImpl-2.8.0.jar:/home/apr/env/mavenDownload/xml-apis/xml-apis/1.3.03/xml-apis-1.3.03.jar:/home/apr/env/mavenDownload/xml-resolver/xml-resolver/1.1/xml-resolver-1.1.jar:/home/apr/env/mavenDownload/javax/validation/validation-api/1.0.0.GA/validation-api-1.0.0.GA.jar:/home/apr/env/mavenDownload/org/hibernate/hibernate-validator/4.2.0.Final/hibernate-validator-4.2.0.Final.jar:/home/apr/env/mavenDownload/org/openmrs/api/openmrs-api/1.9.10/openmrs-api-1.9.10-tests.jar:/home/apr/env/mavenDownload/org/openmrs/web/openmrs-web/1.9.10/openmrs-web-1.9.10.jar:/home/apr/env/mavenDownload/javax/servlet/servlet-api/2.5/servlet-api-2.5.jar:/home/apr/env/mavenDownload/javax/servlet/jsp-api/2.0/jsp-api-2.0.jar:/home/apr/env/mavenDownload/javax/servlet/jstl/1.1.2/jstl-1.1.2.jar:/home/apr/env/mavenDownload/org/openmrs/directwebremoting/dwr/2.0.5-mod/dwr-2.0.5-mod.jar:/home/apr/env/mavenDownload/commons-fileupload/commons-fileupload/1.2.1/commons-fileupload-1.2.1.jar:/home/apr/env/mavenDownload/net/sf/saxon/saxon/8.7/saxon-8.7.jar:/home/apr/env/mavenDownload/net/sf/saxon/saxon-dom/8.7/saxon-dom-8.7.jar:/home/apr/env/mavenDownload/org/springframework/spring-web/3.0.5.RELEASE/spring-web-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-webmvc/3.0.5.RELEASE/spring-webmvc-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-context-support/3.0.5.RELEASE/spring-context-support-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/org/springframework/spring-oxm/3.0.5.RELEASE/spring-oxm-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/jfree/jfreechart/1.0.12/jfreechart-1.0.12.jar:/home/apr/env/mavenDownload/jfree/jcommon/1.0.15/jcommon-1.0.15.jar:/home/apr/env/mavenDownload/mysql/mysql-connector-java/5.1.28/mysql-connector-java-5.1.28.jar:/home/apr/env/mavenDownload/postgresql/postgresql/9.0-801.jdbc4/postgresql-9.0-801.jdbc4.jar:/home/apr/env/mavenDownload/net/sourceforge/jtds/jtds/1.2.4/jtds-1.2.4.jar:/home/apr/env/mavenDownload/taglibs/request/1.0.1/request-1.0.1.jar:/home/apr/env/mavenDownload/taglibs/response/1.0.1/response-1.0.1.jar:/home/apr/env/mavenDownload/taglibs/standard/1.1.2/standard-1.1.2.jar:/home/apr/env/mavenDownload/taglibs/page/1.0.1/page-1.0.1.jar:/home/apr/env/mavenDownload/org/codehaus/jackson/jackson-core-asl/1.5.0/jackson-core-asl-1.5.0.jar:/home/apr/env/mavenDownload/org/codehaus/jackson/jackson-mapper-asl/1.5.0/jackson-mapper-asl-1.5.0.jar:/home/apr/env/mavenDownload/org/apache/velocity/velocity-tools/2.0/velocity-tools-2.0.jar:/home/apr/env/mavenDownload/commons-digester/commons-digester/1.8/commons-digester-1.8.jar:/home/apr/env/mavenDownload/commons-chain/commons-chain/1.1/commons-chain-1.1.jar:/home/apr/env/mavenDownload/commons-validator/commons-validator/1.3.1/commons-validator-1.3.1.jar:/home/apr/env/mavenDownload/oro/oro/2.0.8/oro-2.0.8.jar:/home/apr/env/mavenDownload/sslext/sslext/1.2-0/sslext-1.2-0.jar:/home/apr/env/mavenDownload/org/apache/struts/struts-core/1.3.8/struts-core-1.3.8.jar:/home/apr/env/mavenDownload/org/apache/struts/struts-taglib/1.3.8/struts-taglib-1.3.8.jar:/home/apr/env/mavenDownload/org/apache/struts/struts-tiles/1.3.8/struts-tiles-1.3.8.jar:/home/apr/env/mavenDownload/org/openmrs/web/openmrs-web/1.9.10/openmrs-web-1.9.10-tests.jar:/home/apr/env/mavenDownload/org/openmrs/test/openmrs-test/1.9.10/openmrs-test-1.9.10.pom:/home/apr/env/mavenDownload/org/springframework/spring-test/3.0.5.RELEASE/spring-test-3.0.5.RELEASE.jar:/home/apr/env/mavenDownload/com/h2database/h2/1.2.135/h2-1.2.135.jar:/home/apr/env/mavenDownload/org/databene/databene-benerator/0.5.9/databene-benerator-0.5.9.jar:/home/apr/env/mavenDownload/org/databene/databene-webdecs/0.4.9/databene-webdecs-0.4.9.jar:/home/apr/env/mavenDownload/org/apache/poi/poi/3.5-beta5/poi-3.5-beta5.jar:/home/apr/env/mavenDownload/org/freemarker/freemarker/2.3.9/freemarker-2.3.9.jar:/home/apr/env/mavenDownload/org/databene/databene-commons/0.4.9/databene-commons-0.4.9.jar:/home/apr/env/mavenDownload/org/databene/databene-gui/0.1.9/databene-gui-0.1.9.jar:/home/apr/env/mavenDownload/org/apache/derby/derbyclient/10.4.2.0/derbyclient-10.4.2.0.jar:/home/apr/env/mavenDownload/org/dbunit/dbunit/2.4.7/dbunit-2.4.7.jar:/home/apr/env/mavenDownload/xmlunit/xmlunit/1.3/xmlunit-1.3.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/core/jackson-databind/2.5.4/jackson-databind-2.5.4.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/core/jackson-annotations/2.5.0/jackson-annotations-2.5.0.jar:/home/apr/env/mavenDownload/com/fasterxml/jackson/core/jackson-core/2.5.4/jackson-core-2.5.4.jar:/home/apr/env/mavenDownload/commons-codec/commons-codec/1.5/commons-codec-1.5.jar:/home/apr/env/mavenDownload/junit/junit/4.11/junit-4.11.jar:/home/apr/env/mavenDownload/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/apr/env/mavenDownload/org/hamcrest/hamcrest-library/1.3/hamcrest-library-1.3.jar:/home/apr/env/mavenDownload/org/mockito/mockito-core/1.9.5/mockito-core-1.9.5.jar:/home/apr/env/mavenDownload/org/objenesis/objenesis/1.0/objenesis-1.0.jar";
//		String cmd = FileUtil.gzoltarDir + "/runGZoltar.sh" + " " + data_dir + " " + bug_dir + " " + test_classpath + " " + test_classes_dir + " "
//				+ src_classes_dir + " " + src_classes_file + " " + all_tests_file + " " + " >/dev/null 2>&1";
		// junit_jar + 
		
		String cmd = FileUtil.gzoltarDir + "/runGZoltar.sh" + " " + data_dir + " " + test_classpath + " " + test_classes_dir + " "
				+ src_classes_dir + " " + src_classes_file + " " + all_tests_file + " " + " >/dev/null 2>&1";
		
		if (unitTestsPath != null && new File(unitTestsPath).exists()){
			cmd = FileUtil.gzoltarDir + "/runGZoltar.sh" + " " + data_dir + " " + test_classpath + " " + test_classes_dir + " "
					+ src_classes_dir + " " + src_classes_file + " " + all_tests_file + " " + unitTestsPath + " >/dev/null 2>&1";
		}
		
		logger.debug("cmd: {}", cmd);
		
		long startTime = System.currentTimeMillis();
		
		CmdUtil.runCmdNoOutput(cmd);
		
		FileUtil.writeToFile(data_dir + "/fl.log", String.format("[localize] time cost of running gzoltar v1.7.3 shell script: %s\n", FileUtil.countTime(startTime)));
	}
	
	/**
	 * @Description copy from  logFL(boolean simplify). This is mainly to run FileUtil.readMatrixFile to get some info.
	 * @author apr
	 * @version Apr 10, 2020
	 *
	 */
	public void getCoveredStmtsInfo(){
		long startTime = System.currentTimeMillis();
		
		String flResultDir = data_dir + "/sfl/txt";
		List<SuspiciousLocation> slSpecList;
		List<String> testsList = FileUtil.readTestFile(flResultDir + "/tests.csv");
		List<Pair<List<Integer>, String>> matrixList;
		
		// simplify
		String cmdSimplify = String.format("cp %s/matrix_simplify.py %s && cd %s && python3.6 matrix_simplify.py", FileUtil.gzoltarDir, data_dir, data_dir);
		CmdUtil.runCmd(cmdSimplify);
		slSpecList = FileUtil.readStmtFile(flResultDir + "/spectra.faulty.csv");
		matrixList = FileUtil.readMatrixFile(flResultDir + "/filtered_matrix.txt", slSpecList.size(), testsList);
		
		FileUtil.writeToFile(data_dir + "/fl.log", String.format("[getCoveredStmtsInfo] time cost of getCoveredStmtsInfo (simplify & collect test info): %s\n", FileUtil.countTime(startTime)));
	}
	
	/**
	 * @Description get changed fl.
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void logFL(boolean simplify){
		String flResultDir = data_dir + "/sfl/txt";
		
		List<SuspiciousLocation> slSpecList;
		List<Pair<List<Integer>, String>> matrixList;
		
		List<String> testsList = FileUtil.readTestFile(flResultDir + "/tests.csv");
		
		// simplify
		if (simplify){
			String cmdSimplify = String.format("cp %s/matrix_simplify.py %s && cd %s && python3.6 matrix_simplify.py", FileUtil.gzoltarDir, data_dir, data_dir);
			String output = CmdUtil.runCmd(cmdSimplify);
			slSpecList = FileUtil.readStmtFile(flResultDir + "/spectra.faulty.csv");
			matrixList = FileUtil.readMatrixFile(flResultDir + "/filtered_matrix.txt", slSpecList.size(), testsList);
		}else{
			// read spectra
			slSpecList = FileUtil.readStmtFile(flResultDir + "/spectra.csv");
			matrixList = FileUtil.readMatrixFile(flResultDir + "/matrix.txt", slSpecList.size(), testsList);
		}
		
//		List<String> testsList = FileUtil.readTestFile(data_dir + "/unit_tests.txt");
//		List<String> testsList = FileUtil.readTestFile(flResultDir + "/tests.csv");
		
		int totalPassedTests = FileUtil.totalPassedTests;
		int totalFailedTests = FileUtil.totalFailedTests;
		
		// check equality
		if (testsList.size() != (totalPassedTests + totalFailedTests)){
			String str = String.format("testsList.size(): %d, totalPassedTests: %d, totalFailedTests: %d. They are not consistent. EXIT now.\n", testsList.size(), totalPassedTests, totalFailedTests);
			FileUtil.writeToFile(str);
			logger.error(str);
			System.exit(0);
		}
		
		// check equality
		
		List<SuspiciousLocation> slList = new ArrayList<>();
		//for (SourceLocation sl : slList){
		for (int i = 0; i < slSpecList.size(); i++){
			SuspiciousLocation sl = slSpecList.get(i);
			int executedPassedCount = 0;
			int executedFailedCount = 0;
			for (Pair<List<Integer>, String> pair : matrixList){
				if (pair.getLeft().contains(i)){
					if (pair.getRight().equals("+")){
						executedPassedCount += 1;
					}else{
						executedFailedCount += 1;
					}
				}
			}
			
			slList.add(new SuspiciousLocation(sl.getClassName(), 
					sl.getLineNo(), executedPassedCount, executedFailedCount,
					totalPassedTests, totalFailedTests));
			
		}
		Collections.sort(slList, new Comparator<SuspiciousLocation>(){
			@Override
			public int compare(final SuspiciousLocation o1, final SuspiciousLocation o2){
				return Double.compare(o2.getSuspValue(), o1.getSuspValue());
			}
		});
		
		// write to file.
		String writePath = flResultDir + "/fl.txt";
		FileUtil.writeToFile(writePath, "", false);
		for (SuspiciousLocation sl : slList){
			// debug
//			String line = sl.getClassName() + ":" + sl.getLineNo() + ";" + sl.getSuspValue() + String.format(" %d %", args)"\n";
			String line = sl.getClassName() + ":" + sl.getLineNo() + ";" + sl.getSuspValue() + "\n";
			FileUtil.writeToFile(writePath, line);
		}
		
		changeFL(slList);
		
//		for (int i = 0; i < matrixList.size(); i++){
//			boolean testResult;
//			if(matrixList.get(i).getRight().equals("+")){
//				testResult = true;
//			}else{
//				testResult = false;
//			}
//			List<Integer> coveredStmtIndexList = matrixList.get(i).getLeft();
//			
//			TestResultImpl test = new TestResultImpl(TestCase.from(this.testsList.get(i)), testResult);
//			
//			for(int index : coveredStmtIndexList){
//				SourceLocation sl = slList.get(index);
//				
//				if (!results.containsKey(sl)) {
//					results.put(sl, new ArrayList<fr.inria.lille.localization.TestResult>());
//				}
//				results.get(sl).add(test);
//			}
//		}
//		
//		LinkedHashMap<SourceLocation, List<fr.inria.lille.localization.TestResult>> map = new LinkedHashMap<>();
//		for (StatementSourceLocation ssl : sslList){
//			map.put(ssl.getLocation(), results.get(ssl.getLocation()));
//		}
	}
	
	/**
	 * @Description parse the original matrix file
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 */
	public void logFL(){
		long startTime = System.currentTimeMillis();
		
		// dir containing fl results files
		String flResultDir = data_dir + "/sfl/txt";
		
		// get all tests
		List<String> testsList = FileUtil.readTestFile(flResultDir + "/tests.csv");
		
		// get all stmts from the spectra file
		List<SuspiciousLocation> slSpecList = FileUtil.readStmtFile(flResultDir + "/spectra.csv");
		
		// parse matrix file
		List<SuspiciousLocation> slList = FileUtil.parseMatrixFile(flResultDir + "/matrix.txt", slSpecList, testsList, this.failedMethods);

		Collections.sort(slList, new Comparator<SuspiciousLocation>(){
			@Override
			public int compare(final SuspiciousLocation o1, final SuspiciousLocation o2){
				return Double.compare(o2.getSuspValue(), o1.getSuspValue());
			}
		});
		
		// write to file.
		String writePath = flResultDir + "/fl.txt";
		FileUtil.writeToFile(writePath, "", false);
		// write to file. (with details)
		String detailFLPath = flResultDir + "/fl_details.txt";
		FileUtil.writeToFile(detailFLPath, "", false);
		for (SuspiciousLocation sl : slList){
			String line = sl.getClassName() + ":" + sl.getLineNo() + ";" + sl.getSuspValue() + "\n";
			FileUtil.writeToFile(writePath, line);
			
			List<Integer> coveredTestIndexList = sl.getCoveredTestIndexList();
			line = sl.getClassName() + ":" + sl.getLineNo() + ";" + sl.getSuspValue() + " (" + coveredTestIndexList.size() + ")" + coveredTestIndexList.toString() + "\n";
			FileUtil.writeToFile(detailFLPath, line);
		}
		
		changeFL(slList);
		
		FileUtil.writeToFile(String.format("[logFL] time cost of parsing gzoltar result files, calculating suspValues, and changing fl: %s\n", FileUtil.countTime(startTime)));
	}
	
	/** @Description  find buggy locs and move them into top positions
	 * @author apr
	 * @version Apr 2, 2020
	 *
	 * @param suspList
	 */
	private void changeFL(List<SuspiciousLocation> suspList) {
		List <SuspiciousLocation> buggyLocs = FileUtil.readBuggylocFile(FileUtil.buggylocPath);
		List <Integer> buggyLocIndex = new ArrayList<>();
		
		List<SuspiciousLocation> suspListBackup = new ArrayList<>();
		suspListBackup.addAll(suspList);
		
		List<SuspiciousLocation> changedSuspList = new ArrayList<>();
		
		for (SuspiciousLocation sl : buggyLocs){
			int index = suspList.indexOf(sl);
			if (index >= 0){
				buggyLocIndex.add(index);
//				repairLocs.add(se);
				FileUtil.writeToFile(data_dir + "/fl.log", String.format("[changeFL] buggy location: %s is localized, its rank index is: %d, suspiciousness: %s\n", sl.toString(), index, suspList.get(index).getSuspValue()));
			}else{
				FileUtil.writeToFile(data_dir + "/fl.log", String.format("[changeFL] buggy location: %s is not localized.\n", sl.toString()));
			}
		}
		
		Collections.sort(buggyLocIndex);
		
		// firstly add buggy locs
		for (int index : buggyLocIndex){
			changedSuspList.add(suspListBackup.get(index));
		}
		
		suspListBackup.removeAll(buggyLocs);
		changedSuspList.addAll(suspListBackup);
		
		String changedFlPath = data_dir + "/sfl/txt/fl_changed.txt";
		FileUtil.writeToFile(changedFlPath, "",false);
		for (SuspiciousLocation sl : changedSuspList){
			FileUtil.writeToFile(changedFlPath, sl.toString() + "\n");
		}
//		logger.debug("bp here");
	}
	
	public List<String> getFailedMethods() {
		return failedMethods;
	}

	public void setFailedMethods(List<String> failedMethods) {
		this.failedMethods = failedMethods;
	}

	public String getData_dir() {
		return data_dir;
	}

	public void setData_dir(String data_dir) {
		this.data_dir = data_dir;
	}


	public String getBug_dir() {
		return bug_dir;
	}


	public void setBug_dir(String bug_dir) {
		this.bug_dir = bug_dir;
	}


	public String getTest_classpath() {
		return test_classpath;
	}


	public void setTest_classpath(String test_classpath) {
		this.test_classpath = test_classpath;
	}


	public String getTest_classes_dir() {
		return test_classes_dir;
	}


	public void setTest_classes_dir(String test_classes_dir) {
		this.test_classes_dir = test_classes_dir;
	}


	public String getSrc_classes_dir() {
		return src_classes_dir;
	}


	public void setSrc_classes_dir(String src_classes_dir) {
		this.src_classes_dir = src_classes_dir;
	}


	public String getSrc_classes_file() {
		return src_classes_file;
	}


	public void setSrc_classes_file(String src_classes_file) {
		this.src_classes_file = src_classes_file;
	}


	public String getAll_tests_file() {
		return all_tests_file;
	}


	public void setAll_tests_file(String all_tests_file) {
		this.all_tests_file = all_tests_file;
	}


	public String getJunit_jar() {
		return junit_jar;
	}


	public void setJunit_jar(String junit_jar) {
		this.junit_jar = junit_jar;
	}

	public String getUnitTestsPath() {
		return unitTestsPath;
	}

	public void setUnitTestsPath(String unitTestsPath) {
		this.unitTestsPath = unitTestsPath;
	}
	
}
