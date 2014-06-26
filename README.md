Try-with-resources Zip(Input|Output)Stream check plugin
=======================================================

Example usage.

Add compiler configuration (requires Oracle javac 8):

    <build>
      ...
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.1</version>
        <configuration>
          <fork>true</fork>
          <forceJavacCompilerUse>true</forceJavacCompilerUse>
          <compilerArgs>
            <arg>-processorpath</arg>
            <arg>${settings.localRepository}${file.separator}eu${file.separator}nets${file.separator}distribution${file.separator}try-with-resources-checker${file.separator}1-SNAPSHOT${file.separator}try-with-resources-checker-1-SNAPSHOT.jar</arg>
            <arg>-Xplugin:TryWithCheckPluginHardline</arg>
            <!-- <arg>-Xplugin:TryWithCheckPlugin</arg> --><!-- Will give Warning on compilation instead of Error -->
          </compilerArgs>
          <encoding>UTF-8</encoding>
          <source>1.8</source>
          <target>1.8</target>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>eu.nets.distribution</groupId>
            <artifactId>try-with-resources-checker</artifactId>
            <version>1-SNAPSHOT</version>
          </dependency>
        </dependencies>
      </plugin>
      ...
    </build>

Running "mvn clean compile" for input
    
    public class Demo {
        public static void main(String[] args) {
            ZipInputStream unssafe = new ZipInputStream(null);
        }
    }
    
should give

    [ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project nets-distribution: Compilation failure
    [ERROR] C:\devel\...\src\main\java\eu\nets\distribution\Demo.java:[7,33] error: Use try-with-resources, offending class was java.util.zip.ZipInputStream


