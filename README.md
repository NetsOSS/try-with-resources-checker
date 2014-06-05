Try-with-resources check plugin
===============================

Example usage:

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
            <arg>-Xplugin:TryWithCheckPlugin</arg>
            <!-- <arg>-Xplugin:TryWithCheckPluginHardline</arg> --><!-- Will give ERROR on compilation instead of warning -->
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
     