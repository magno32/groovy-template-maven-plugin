package com.summit.groovy.template.mojo;

import java.io.File;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.*
import org.apache.maven.project.MavenProject
import org.apache.maven.model.Resource;

/*
 * #%L
 * groovy-template-maven-plugin Maven Plugin
 * %%
 * Copyright (C) 2016 Summit Management Systems, Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
/**
 * Generates source code from Groovy Templates
 *
 * @author Justin Smith
 */
@Mojo(name="generate", defaultPhase= LifecyclePhase.GENERATE_SOURCES)
class TemplateMojo extends AbstractMojo{
	
	/**
	 * Properties to go into Groovy templates
	 */
	@Parameter
	private Map properties = [:];
	
	/**
	 * Additional properties files to use.  Will be added to the groovy binding.
	 * These are expected to be in groovy syntax (we use the groovy slurper)
	 */
	@Parameter
	private List<File> propertiesFiles;
	
	/**
     * Source folder for velocity templates
     *
     */
	@Parameter(defaultValue='\${project.basedir}/src/', property="sourcePathRoot", required=true)
    private File sourcePathRoot;
	
	/**
     * Source folder for groovy templates
     */
	@Parameter(defaultValue='\${project.basedir}/src/main/gtemplate/', property="groovyTemplateDir", required=true)
    private File groovyTemplateSources;
	
	/**
     * Source folder for velocity test templates
     */
	@Parameter(defaultValue="\${basedir}/src/test/gtemplate/", property="testGroovyTemplateDir", required=true)
    private File groovyTemplateTestSources;
    /**
     * Output directory for generated sources.
     */
	@Parameter(defaultValue="\${project.build.directory}/generated-sources/main", property="srcOutput")
    private File sourcesOutputDirectory;
    /**
     * Output directory for generated test sources.
     */
	@Parameter(defaultValue="\${project.build.directory}/generated-sources/test", property="testSrcOutput")
    private File testSourcesOutputDirectory;
    /**
     * Output directory for resources.
     */
	@Parameter(defaultValue="\${project.build.directory}/generated-resources/", property="resOutput")
    private File resourcesOutputDirectory;
    /**
     * Output directory test resources.
     */
	@Parameter(defaultValue="\${project.build.directory}/generated-test-resources/",property="testResOutput")
    private File testResourcesOutputDirectory;
	
	/**
	 * Classname for the groovy template engine to use.
	 */ 
	@Parameter(defaultValue="groovy.text.SimpleTemplateEngine",property="templateEngine")
	private String templateEngineClass;
	
    /**
     * @since 1.0
     */
	@Parameter(defaultValue="\${project}", required=true,readonly=true)
    private MavenProject project;
	
	public void execute()
	throws MojoExecutionException {
		if(properties == null){
			getLog().info("Properties is empty")
			properties = [:]
		}
		getLog().info("Properties: " + properties)
		
		def allProps = [:]
		allProps += properties;
		if(propertiesFiles != null){
			for(File f : propertiesFiles){
				if(f.exists()){
					allProps+=new ConfigSlurper().parse(f.toURL())
				}else{
					getLog().warn("$f does not exist...")
				}
			}
		}		
		getLog().debug("All properties: " + allProps)
		
		//Generate Sources
		if(generateSources(groovyTemplateSources,sourcesOutputDirectory,resourcesOutputDirectory, allProps)){
			project.addCompileSourceRoot(sourcesOutputDirectory.toString());
            Resource res = new Resource();
            res.setDirectory(resourcesOutputDirectory.getAbsolutePath());
            project.addResource(res);
		}
		//Generate Test Sources
		if(generateSources(groovyTemplateTestSources,testSourcesOutputDirectory,testResourcesOutputDirectory, allProps)){
			project.addTestCompileSourceRoot(testSourcesOutputDirectory.toString());
            Resource res = new Resource();
            res.setDirectory(testResourcesOutputDirectory.getAbsolutePath());
            project.addTestResource(res);
		}
	}
	
	public boolean generateSources(File templateSources, File sourcesOutput,File resourceDst, Map properties){
		//get source files
		List<File> sourceFiles = getTemplateFiles(templateSources)
		//Generate sources
		writeOutputFiles(templateSources,sourcesOutput,resourceDst,sourceFiles, properties);
		return !sourceFiles.isEmpty()
	}
	
	public List<File> getTemplateFiles(File srcDir){
		getLog().info("Loading sources from: $srcDir")
		getTemplateFiles(srcDir, new ArrayList<File>())
	}
	public List<File> getTemplateFiles(File srcDir, List<File> knownFiles){
		for(File f : srcDir.listFiles()){
			if(f.isFile()){
				getLog().info("Found $f")
				knownFiles.add(f)
			}else if(f.isDirectory()){
				getTemplateFiles(f, knownFiles)
			}
		}
		knownFiles
	}
	
	public void writeOutputFiles(File srcRoot, File dstDir,File resourceDstFile, List<File> templateFiles, Map params){
		getLog().debug("Writing ${templateFiles.size()} output file(s) to $dstDir")
		for(File f : templateFiles){
			writeOutputFile(srcRoot, dstDir, resourceDstFile, f, params);
		}
	}
	public void writeOutputFile(File srcRoot, File dstDir,File resourceDstFile, File templateFile, Map params){
		String relativePath = templateFile.getAbsolutePath().substring(srcRoot.getAbsolutePath().length())
		String[] suffixes = [".gsp",".groovy"]
		for(String suf : suffixes){
			if(relativePath.endsWith(suf)){
				relativePath = relativePath.substring(0, relativePath.length() - suf.length());
				break;
			}
		}
		getLog().debug("Relative Path: $relativePath")
		//Allow override of template engine!
		def engine = Class.forName(templateEngineClass).newInstance();
		def template = engine.createTemplate(templateFile.text).make(params)
		
		//TODO how do we handle this...  It limits us to only generating JAVA code, 
		//everything else is a resource.
		File outFile
		if(relativePath.endsWith("java")){
			outFile = new File(dstDir, relativePath);
		}else{
			outFile = new File(resourceDstFile, relativePath);
		}
		getLog().debug("Destination: $outFile")
		outFile.getParentFile().mkdirs()
		outFile.withWriter{ it << template.toString()}
	}
}

