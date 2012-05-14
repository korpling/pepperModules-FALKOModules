/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
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
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.falkoModules;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.log.LogService;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperExceptions.PepperModuleException;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperManipulator;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperManipulatorImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sCorpusStructure.SDocument;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SDocumentGraph;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpanningRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STextualRelation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SToken;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SAnnotation;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SElementId;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SLayer;

/**
 * This manipulator was developed especially for the FALKO Corpus.
 * It creates a SSpan-objects for every SToken object in the document. All annotations for STokens will be duplicated and added to the spans. 
 * The annotations of the tokens will be renamed from "annoName" to "annoName."
 * For example a "pos"-annotation of SToken-object will be renamedto a "pos."-annotation.
 * All spans, tokens and spanning relations will be added to an artificial layer named "falko".
 * @author Florian Zipser
 * @version 1.0
 *
 */
@Component(name="FALKOManipulatorComponent", factory="PepperManipulatorComponentFactory")
@Service(value=PepperManipulator.class)
public class FALKOManipulator extends PepperManipulatorImpl 
{
	public FALKOManipulator()
	{
		super();
		
		{//setting name of module
			this.name= "FALKOManipulator";
		}//setting name of module
		
		{//for testing the symbolic name has to be set without osgi
			if (	(this.getSymbolicName()==  null) ||
					(this.getSymbolicName().isEmpty()))
				this.setSymbolicName("de.hu_berlin.german.korpling.saltnpepper.pepperModules.FALKOModules");
		}//for testing the symbolic name has to be set without osgi
		
		{//just for logging: to say, that the current module has been loaded
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is created...");
		}//just for logging: to say, that the current module has been loaded
	}
	
	private static final String KW_POSTFIX= ".";
	private static final String KW_WORD= "word";
	/**
	 * This method is called by method start() of superclass PepperManipulator, if the method was not overriden
	 * by the current class. If this is not the case, this method will be called for every document which has
	 * to be processed.
	 * @param sElementId the id value for the current document or corpus to process  
	 */
	@Override
	public void start(SElementId sElementId) throws PepperModuleException 
	{
		if (	(sElementId!= null) &&
				(sElementId.getSIdentifiableElement()!= null) &&
				((sElementId.getSIdentifiableElement() instanceof SDocument)))
		{//only if given sElementId belongs to an object of type SDocument or SCorpus	
			SDocumentGraph sDocGraph= ((SDocument)sElementId.getSIdentifiableElement()).getSDocumentGraph();
			if(sDocGraph!= null)
			{//if document contains a document graph
				EList<SToken> sTokens= sDocGraph.getSTokens();
				EList<SToken> emptyTokensAtStart= new BasicEList<SToken>();
				if (	(sTokens!= null) &&
						(sTokens.size()> 0))
				{		
					SToken sToken= null;
					STextualRelation sTextRel= null;
					//stores the span object, to which the tokens has to be add
					SSpan sSpan= null;
					//stores the artificial relation between token and artificial span
					SSpanningRelation sSpanRel= null;
					//stores the new annotation taken from token set to span
					SAnnotation sNewAnno= null;
					for (int i=0; i < sTokens.size(); i++)
					{//for all tokens do
						sToken= sTokens.get(i);
						EList<Edge> outEdges= sDocGraph.getOutEdges(sToken.getSId());
						for (Edge outEdge: outEdges)
						{//find sTextRel
							if (outEdge instanceof STextualRelation)
							{
								sTextRel= (STextualRelation) outEdge;
								break;
							}
						}//find sTextRel
						if (sTextRel!= null)
						{//if textrel exists
							String text= sTextRel.getSTextualDS().getSText().substring(sTextRel.getSStart(), sTextRel.getSEnd());
							if (	(sTextRel.getSStart()< sTextRel.getSEnd()) &&
									(!" ".equals(text)))
							{//if current token is not an empty token and does not contain only a blank	
								{//create artificial span
									sSpan= SaltCommonFactory.eINSTANCE.createSSpan();
									sDocGraph.addSNode(sSpan);
								}//create artificial span
								{//create an artificial annotation with the overlapped text
									sNewAnno= SaltCommonFactory.eINSTANCE.createSAnnotation();
									sNewAnno.setSName(KW_WORD);
									sNewAnno.setSValue(sTextRel.getSTextualDS().getSText().substring(sTextRel.getSStart(), sTextRel.getSEnd()));
									sSpan.addSAnnotation(sNewAnno);
								}//create an artificial annotation with the overlapped text
								{//copy all annotations from token to span
									if (	(sToken.getSAnnotations()!= null)&&
											(sToken.getSAnnotations().size()> 0))
									{//if annotations exist	
										
										for (SAnnotation sAnno: sToken.getSAnnotations())
										{//copy annotation and manipulate annotation
											if ("<unknown>".equalsIgnoreCase(sAnno.getSValueSTEXT()))
												sAnno.setSValue("[unknown]");
												
											sNewAnno= SaltCommonFactory.eINSTANCE.createSAnnotation();
											sNewAnno.setSNS(sAnno.getSNS());
											sNewAnno.setSName(sAnno.getSName());
											sNewAnno.setSValue(sAnno.getSValueSTEXT());
											//change the name of the old annotation
											sAnno.setSName(sAnno.getSName()+ KW_POSTFIX);
											
											sSpan.addSAnnotation(sNewAnno);
										}//copy annotation and manipulate annotation
									}//if annotations exist
								}//copy all annotations from token to span
								if (emptyTokensAtStart.size()>= 0)
								{//if there are empty tokens at start, put them to current span
									for (SToken emptyToken: emptyTokensAtStart)
									{
										sSpanRel= SaltCommonFactory.eINSTANCE.createSSpanningRelation();
										sSpanRel.setSToken(emptyToken);
										sSpanRel.setSSpan(sSpan);
										sDocGraph.addSRelation(sSpanRel);
									}
									//remove all entries from empty token list
									emptyTokensAtStart.clear();
								}//if there are empty tokens at start, put them to current span
							}//if current token is not an empty token and does not contain only a blank
							if (sSpan!= null)
							{//if span is not empty relate token to span via SSpanningRel
								sSpanRel= SaltCommonFactory.eINSTANCE.createSSpanningRelation();
								sSpanRel.setSToken(sToken);
								sSpanRel.setSSpan(sSpan);
								sDocGraph.addSRelation(sSpanRel);
							}//if span is not empty relate token to span via SSpanningRel
							else
							{//put token to list emptyTokensAtStart
								emptyTokensAtStart.add(sToken);
							}//put token to list emptyTokensAtruStart
						}
					}//if textrel exists
				}//for all tokens do
				{//create an SLayer for all SNodes
					SLayer sLayer= SaltCommonFactory.eINSTANCE.createSLayer();
					sLayer.setSName("falko");
					boolean addLayer=false;
					{//adding tokens to layer
						for (SToken sToken: sDocGraph.getSTokens())
						{
							if (	(sToken.getSLayers()== null) ||
									(sToken.getSLayers().size()==0))
							{
								sLayer.getSNodes().add(sToken);
								addLayer=true;
							}
						}
					}//adding tokens to layer
					{//adding spans to layer
						for (SSpan sSpan: sDocGraph.getSSpans())
						{
							if (	(sSpan.getSLayers()== null) ||
									(sSpan.getSLayers().size()==0))
							{
								sLayer.getSNodes().add(sSpan);
								addLayer=true;
							}
						}
					}//adding spans to layer
					{//adding SpanningRelation to layer
						for (SSpanningRelation sSpanRel: sDocGraph.getSSpanningRelations())
						{
							if (	(sSpanRel.getSLayers()== null) ||
									(sSpanRel.getSLayers().size()==0))
							{
								sLayer.getSRelations().add(sSpanRel);
								addLayer=true;
							}
						}	
					}//adding SpanningRelation to layer
					
					if (addLayer)
						sDocGraph.getSLayers().add(sLayer);
				}//create an SLayer for all SNodes
				
			}//if document contains a document graph
		}//only if given sElementId belongs to an object of type SDocument or SCorpus
		
	}
	
//================================ start: methods used by OSGi
	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets activated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void activate(ComponentContext componentContext) 
	{
		this.setSymbolicName(componentContext.getBundleContext().getBundle().getSymbolicName());
		{//just for logging: to say, that the current module has been activated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is activated...");
		}//just for logging: to say, that the current module has been activated
	}

	/**
	 * This method is called by the OSGi framework, when a component with this class as class-entry
	 * gets deactivated.
	 * @param componentContext OSGi-context of the current component
	 */
	protected void deactivate(ComponentContext componentContext) 
	{
		{//just for logging: to say, that the current module has been deactivated
			if (this.getLogService()!= null)
				this.getLogService().log(LogService.LOG_DEBUG,this.getName()+" is deactivated...");
		}	
	}
//================================ start: methods used by OSGi
}
