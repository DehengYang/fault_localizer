package apr.apr.repair.utils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

public class ClassFinder {
//	private URL url;
//	
//	public ClassFinder(String directory) throws IOException{
//		// get url
//		url = new File(directory).toURI().toURL();
//	}
	
//	public ClassFinder(){
//		
//	}
	private Set<String> testMethods = new HashSet<>();
	
	public Set<String> getTestMethods(){
		return testMethods;
	}
	
	public Set<String> getJavaClasses(String path){
		return getJavaClasses(path, "class");
	}
	
	/**
	 * 
	 * @Description: get java classes to intrument
	 * @author apr
	 * @version Mar 17, 2020
	 *
	 * @param path
	 * @param filter
	 * @return
	 */
	public Set<String> getJavaClasses(String path, String filter){
		// refer to: https://www.geeksforgeeks.org/set-in-java/
		Set<String> classes = new HashSet<>();
		
		// get all files
		File directory = new File(path);
		// refer to: https://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtils.html#listFiles(java.io.File,%20java.lang.String[],%20boolean)
		Collection<File> files = FileUtils.listFiles(directory, new String[]{filter}, true);
		
		// get all classes
		for (File file : files){
			//refer to : https://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls
			String relativePath = directory.toURI().relativize(file.toURI()).getPath();
			String className = relativePath.replace("/", ".").substring(0, relativePath.length() - ".class".length()); // .class len = 6
			
			classes.add(className);
		}
		
		return classes;
	}
	
	/**
	 * @Description: get all test classes & methods
	 * @author apr
	 * @version Mar 16, 2020
	 *
	 * @param testPath
	 * @param srcPath
	 * @param deps
	 * @return
	 */
	public Set<String> getTestClasses(String testPath, String srcPath, List<String> deps){
		Set<String> classes = new HashSet<>();
		
		// get urls: must include classpath (testClasses, srcClasses, dependencies)
		List<String> classpath = new ArrayList<>();
		classpath.add(testPath);
		classpath.add(srcPath);
		for (String dep : deps){
			classpath.add(dep);
		}
		URL[] urls = new URL[classpath.size()];
		try {
			int cnt = 0;
			for (String path : classpath){
				urls[cnt] = new File(path).toURI().toURL();
				cnt ++;
			}
//			url = new File(testPath).toURI().toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// get classloader
		// refer to: https://stackoverflow.com/questions/31324558/is-there-a-way-to-initialize-a-single-element-in-an-array-java
//		URL[] urls = {url};
		URLClassLoader classLoader = new URLClassLoader(urls);
		
		// get all files
		File directory = new File(testPath);
		Collection<File> files = FileUtils.listFiles(directory, new String[]{"class"}, true);
		
		// get all classes
		for (File file : files){
			String relativePath = directory.toURI().relativize(file.toURI()).getPath();
			String className = relativePath.replace("/", ".").substring(0, relativePath.length() - ".class".length()); // .class len = 6
			
			// get class
			Class<?> clazz = null;
			try {
				clazz = classLoader.loadClass(className);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// judge if a junit test
//			String name = clazz.getSuperclass().getName(); //NPE
			if (isTest(clazz)){
				classes.add(className);
			}
		}
		try {
			classLoader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return classes;
	}

	/**
	 * @Description: judge if the class is a test class
	 * refer to: 
	 * + http://asjava.com/junit/junit-3-vs-junit-4-comparison/
	 * + https://stackoverflow.com/questions/6685730/the-differences-between-junit-3-and-junit-4
	 * + https://www.vogella.com/tutorials/JUnit/article.html
	 * @author apr
	 * @version Mar 16, 2020
	 *
	 * @param clazz
	 * @return
	 */
	private boolean isTest(Class<?> clazz) {
		// refer to: https://stackoverflow.com/questions/16658031/getmethod-avoiding-parent-classes
		Method[] methods = clazz.getDeclaredMethods();
		
		boolean isTest = false;
		
		for (Method method : methods){
//			if (method.getAnnotations().length > 1){
//				System.out.println("method.getAnnotations() size:" + method.getAnnotations().length);
//			}
			for (Annotation a : method.getAnnotations()){
//				org.junit.Ignore
//				org.junit.Test
//				System.out.println(a.annotationType().getCanonicalName());
//				System.out.println(a.annotationType().getName());
				
				// junit4
				if (a.annotationType().getCanonicalName().equals("org.junit.Test")){
					testMethods.add(clazz.getName() + "#" + method.getName());
//					testMethods.add(clazz.getName() + "#" + method.getName());// test if uniq
					isTest = true;
					break;
//					return true;
				}
			}
			
			// junit3
			if (method.getParameterTypes().length == 0 && method.getName().startsWith("test") && method.getReturnType().equals(Void.TYPE) &&
				Modifier.isPublic(method.getModifiers())){
				testMethods.add(clazz.getName() + "#" + method.getName());
				isTest = true;
//				return true;	
			}
		}
		
		if (isTest) return true;
		return false;
	}
}