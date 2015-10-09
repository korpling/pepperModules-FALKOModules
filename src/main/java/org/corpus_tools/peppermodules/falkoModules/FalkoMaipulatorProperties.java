/**
 * Copyright 2009 Humboldt-Universität zu Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.corpus_tools.peppermodules.falkoModules;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

/**
 * Defines the properties to be used for the {@link EXMARaLDAImporter}. 
 * @author Florian Zipser
 *
 */
public class FalkoMaipulatorProperties extends PepperModuleProperties 
{
	public static final String PREFIX="falkomanipulator.";
	
	public static final String PROP_SLAYER_NAME=PREFIX+"sLayerName";
	
	public FalkoMaipulatorProperties()
	{
		this.addProperty(new PepperModuleProperty<String>(PROP_SLAYER_NAME, String.class, "With this property you can set the name of the new created SLayer.", "falko", false));
	}
	
	public String getSLayerName()
	{
		return((String)this.getProperty(PROP_SLAYER_NAME).getValue());
	}
}
