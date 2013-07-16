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

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.osgi.service.component.annotations.Component;

import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.MAPPING_RESULT;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.PepperMapper;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperManipulatorImpl;
import de.hu_berlin.german.korpling.saltnpepper.pepper.pepperModules.impl.PepperMapperImpl;
import de.hu_berlin.german.korpling.saltnpepper.salt.SaltFactory;
import de.hu_berlin.german.korpling.saltnpepper.salt.graph.Edge;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.SaltCommonFactory;
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
public class FALKOManipulator extends PepperManipulatorImpl 
{
	public FALKOManipulator()
	{
		super();
		
		{//setting name of module
			this.name= "FALKOManipulator";
		}//setting name of module
	}
	
	/**
	 * Creates a mapper of type {@link PAULA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(SElementId)}
	 */
	@Override
	public PepperMapper createPepperMapper(SElementId sElementId)
	{
		FalkoMapper mapper= new FalkoMapper();
		return(mapper);
	}
	
	private class FalkoMapper extends PepperMapperImpl{
		private static final String KW_POSTFIX= ".";
		private static final String KW_WORD= "word";
		/**
		 * This method maps a Salt document to a Treetagger document  
		 */
		@Override
		public MAPPING_RESULT mapSDocument() { 
			if (getSDocument().getSDocumentGraph()== null)
				getSDocument().setSDocumentGraph(SaltFactory.eINSTANCE.createSDocumentGraph());
			SDocumentGraph sDocGraph= getSDocument().getSDocumentGraph();
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
			return(MAPPING_RESULT.FINISHED);
		}
	}
//================================ start: methods used by OSGi
}
