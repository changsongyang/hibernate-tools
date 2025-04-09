/*
 * Hibernate Tools, Tooling for your Hibernate Projects
 *
 * Copyright 2004-2025 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hibernate.tool.ant;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.hibernate.tool.api.java.DefaultJavaPrettyPrinterStrategy;

public class JavaFormatterTask extends Task {
	
	private List<FileSet> fileSets = new ArrayList<FileSet>();
	private boolean failOnError;
	private File configurationFile;
	
	public void addConfiguredFileSet(FileSet fileSet) {
		fileSets.add(fileSet);
	}

	public void setConfigurationFile(File configurationFile) {
		this.configurationFile = configurationFile;
	}
	
	private Properties readConfig(File cfgfile) throws IOException {
		BufferedInputStream stream = null;
		try {
			stream = new BufferedInputStream(new FileInputStream(cfgfile));
			final Properties settings = new Properties();
			settings.load(stream);
			return settings;
		} catch (IOException e) {
			throw e;
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					
				}
			}
		}		
	}

	
	public void execute() throws BuildException {
		
		Map<Object, Object> settings = null;
		if(configurationFile!=null) {
			 try {
				settings = readConfig( configurationFile );
			}
			catch (IOException e) {
				throw new BuildException("Could not read configurationfile " + configurationFile, e);
			}
		}
		
		File[] files = getFiles();
		
		int failed = 0;
	
		if(files.length>0) {
			
			DefaultJavaPrettyPrinterStrategy formatter = new DefaultJavaPrettyPrinterStrategy(settings);
			for (int i = 0; i < files.length; i++) {
				File file = files[i];			
				try {
					boolean ok = formatter.formatFile( file );
					if(!ok) {
						failed++;
						getProject().log(this, "Formatting failed - skipping " + file, Project.MSG_WARN);						
					} else {
						getProject().log(this, "Formatted " + file, Project.MSG_VERBOSE);
					}
				} catch(RuntimeException ee) {
					failed++;
					if(failOnError) {
						throw new BuildException("Java formatting failed on " + file, ee);
					} else {
						getProject().log(this, "Java formatting failed on " + file + ", " + ee.getLocalizedMessage(), Project.MSG_ERR);
					}
				}
			}			
		}
		
		getProject().log( this, "Java formatting of " + files.length + " files completed. Skipped " + failed + " file(s).", Project.MSG_INFO );
		
	}

	private File[] getFiles() {

		List<File> files = new LinkedList<File>();
		for ( Iterator<FileSet> i = fileSets.iterator(); i.hasNext(); ) {

			FileSet fs = i.next();
			DirectoryScanner ds = fs.getDirectoryScanner( getProject() );

			String[] dsFiles = ds.getIncludedFiles();
			for (int j = 0; j < dsFiles.length; j++) {
				File f = new File(dsFiles[j]);
				if ( !f.isFile() ) {
					f = new File( ds.getBasedir(), dsFiles[j] );
				}

				files.add( f );
			}
		}

		return (File[]) files.toArray(new File[files.size()]);
	}

}
