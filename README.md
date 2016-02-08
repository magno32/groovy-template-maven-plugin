# Groovy-Template Maven Plugin

This plugin allows Maven to generate sources and resources from available
groovy template files, see: [Template Engines](http://docs.groovy-lang.org/latest/html/documentation/template-engines.html)

## Usage

We are working toward a release and deployment to central.  Until this occurs
you will need to build and install the plugin locally.

Configuration:

```xml
<build>
	<plugins>
	...
		<plugin>
			<groupId>com.summit.opc.jmx</groupId>
			<artifactId>groovy-template-maven-plugin</artifactId>
			<version>1.0-SNAPSHOT</version>
			<executions>
				<execution>
					<goals>
						<goal>generate</goal>
					</goals>
				</execution>
			</executions>
			<configuration>
				<propertiesFiles>
					<file>${project.basedir}/src/gconfigs/config.groovy</file>
				</propertiesFiles>
			</configuration>
		</plugin>
	...
	</plugins>
</build>
```

### Plugin Goals
From `mvn groovy-template:help`

This plugin has 2 goals:

groovy-template:generate
  Generates source code from Groovy Templates

  Available parameters:

    groovyTemplateSources (Default: ${project.basedir}/src/main/gtemplate/)
      Source folder for groovy templates
      Required: Yes
      User property: groovyTemplateDir

    groovyTemplateTestSources (Default: ${basedir}/src/test/gtemplate/)
      Source folder for velocity test templates
      Required: Yes
      User property: testGroovyTemplateDir

    properties
      Properties to go into Groovy templates

    propertiesFiles
      Additional properties files to use. Will be added to the groovy binding.
      These are expected to be in groovy syntax (we use the groovy slurper)

    resourcesOutputDirectory (Default: ${project.build.directory}/generated-resources/)
      Output directory for resources.
      User property: resOutput

    sourcePathRoot (Default: ${project.basedir}/src/)
      Source folder for velocity templates
      Required: Yes
      User property: sourcePathRoot

    sourcesOutputDirectory (Default: ${project.build.directory}/generated-sources/main)
      Output directory for generated sources.
      User property: srcOutput

    testResourcesOutputDirectory (Default: ${project.build.directory}/generated-test-resources/)
      Output directory test resources.
      User property: testResOutput

    testSourcesOutputDirectory (Default: ${project.build.directory}/generated-sources/test)
      Output directory for generated test sources.
      User property: testSrcOutput

##### groovy-template:help
  Display help information on groovy-template-maven-plugin.
  Call mvn groovy-template:help -Ddetail=true -Dgoal=goal-name to display
  parameter details.


