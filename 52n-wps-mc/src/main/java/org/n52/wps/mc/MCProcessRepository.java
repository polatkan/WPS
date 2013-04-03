/**
 * ﻿Copyright (C) 2012
 * by 52 North Initiative for Geospatial Open Source Software GmbH
 *
 * Contact: Andreas Wytzisk
 * 52 North Initiative for Geospatial Open Source Software GmbH
 * Martin-Luther-King-Weg 24
 * 48155 Muenster, Germany
 * info@52north.org
 *
 * This program is free software; you can redistribute and/or modify it under
 * the terms of the GNU General Public License version 2 as published by the
 * Free Software Foundation.
 *
 * This program is distributed WITHOUT ANY WARRANTY; even without the implied
 * WARRANTY OF MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program (see gnu-gpl v2.txt). If not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA or
 * visit the Free Software Foundation web page, http://www.fsf.org.
 */

package org.n52.wps.mc;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.apache.log4j.Logger;
import org.n52.movingcode.runtime.GlobalRepositoryManager;
import org.n52.movingcode.runtime.ProcessorConfig;
import org.n52.movingcode.runtime.coderepository.IMovingCodeRepository;
import org.n52.movingcode.runtime.coderepository.RepositoryChangeListener;
import org.n52.wps.PropertyDocument.Property;
import org.n52.wps.commons.WPSConfig;
import org.n52.wps.server.IAlgorithm;
import org.n52.wps.server.IAlgorithmRepository;

/**
 * 
 * @author Matthias Mueller
 * 
 *         TODO: lazy initialization
 *         TODO: cross-check registered functions with the capabilities of the
 *               ProcessorFactory --> offer only those processes that are
 *               executable in this runtime environment
 * 
 */
public class MCProcessRepository implements IAlgorithmRepository {

	private static final String CONFIG_FILE_NAME = "processors.xml";

	private static final String REPO_FEED_URL_PARAM = "REPOSITORY_FEED_URL";

	private static final String REPO_FOLDER_PARAM = "REPOSITORY_FOLDER";

	private GlobalRepositoryManager rm = GlobalRepositoryManager.getInstance();

	private static Logger logger = Logger.getLogger(MCProcessRepository.class);

	public MCProcessRepository() {
		super();
		configureMCRuntime();

		// check if the repository is active
		if (WPSConfig.getInstance().isRepositoryActive(this.getClass().getCanonicalName())) {

			// get properties to find out which remote repositories we shall invoke
			Property[] propertyArray = WPSConfig.getInstance().getPropertiesForRepositoryClass(this.getClass().getCanonicalName());

			// for each remote repository: add to RepoManager
			for (Property property : propertyArray) {
				if (property.getName().equalsIgnoreCase(REPO_FEED_URL_PARAM) && property.getActive()) {
					// convert to URL, check and register
					try {
						URL repoURL = new URL(property.getStringValue());
						rm.addRepository(repoURL);
						logger.info("Added MovingCode Repository: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (MalformedURLException e) {
						logger.warn("MovingCode Repository is not a valid URL: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						logger.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
								+ property.getStringValue());
					}

				}
			}
			
			// for each remote repository: add to RepoManager
			for (Property property : propertyArray) {
				if (property.getName().equalsIgnoreCase(REPO_FOLDER_PARAM) && property.getActive()) {
					// identify Folder, check and register
					try {
						String repoFolder = property.getStringValue();
						rm.addLocalZipPackageRepository(repoFolder);
						logger.info("Added MovingCode Repository: " + property.getName() + " - "
								+ property.getStringValue());
					}
					catch (Exception e) {
						// catch any unexpected error; if we get here this is probably an indication for a
						// bug/flaw in mc-runtime ...
						logger.error("Error invoking MovingCode Runtime for feed URL : " + property.getName() + " - "
								+ property.getStringValue());
					}

				}
			}

			// add a change listener to the GlobalRepositoryManager rm
			rm.addRepositoryChangeListener(new RepositoryChangeListener() {
				@Override
				public void onRepositoryUpdate(IMovingCodeRepository updatedRepo) {
					logger.info("Moving Code repository content has changed. Capabilities update required.");
					WPSConfig.getInstance().firePropertyChange(
							WPSConfig.WPSCAPABILITIES_SKELETON_PROPERTY_EVENT_NAME);				
				}
			});

		}
		else {
			logger.debug("MCProcessRepository does not contain any processes.");
		}
	}

	@Override
	public Collection<String> getAlgorithmNames() {
		return Arrays.asList(rm.getFunctionIDs());
	}

	@Override
	public IAlgorithm getAlgorithm(String processID) {
		return new MCProcessDelegator(processID);
	}

	@Override
	public ProcessDescriptionType getProcessDescription(String processID) {
		return rm.getProcessDescription(processID);
	}

	@Override
	public boolean containsAlgorithm(String processID) {
		return rm.providesFunction(processID);
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		// we probably do not need any logic here
	}

	// ----------------------------------------------------------------
	// methods and logic for processor configuration
	private static void configureMCRuntime() {
		String configFilePath = WPSConfig.getConfigDir() + CONFIG_FILE_NAME;
		File configFile = new File(configFilePath);
		boolean loaded = ProcessorConfig.getInstance().setConfig(configFile);
		if ( !loaded) {
			logger.error("Could not load processor configuration from " + configFilePath);
		}
	}

}