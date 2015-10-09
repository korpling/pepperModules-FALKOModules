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

import java.util.ArrayList;
import java.util.List;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.SSpanningRelation;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SAnnotation;
import org.corpus_tools.salt.core.SLayer;
import org.corpus_tools.salt.core.SNode;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.graph.Relation;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

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
		
		//setting name of module
		this.setName("FALKOManipulator");
		setSupplierContact(URI.createURI("saltnpepper@lists.hu-berlin.de"));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-FALKOModules"));
		setDesc("This manipulator was developed especially for the FALKO Corpus. It creates a SSpan-objects for every SToken object in the document. All annotations for STokens will be duplicated and added to the spans. The annotations of the tokens will be renamed from 'annoName' to 'annoName.'. For example a 'pos'-annotation of SToken-object will be renamed to a 'pos.'-annotation. All spans, tokens and spanning relations will be added to an artificial layer named 'falko'. ");
		setProperties(new FalkoMaipulatorProperties());
	}
	
	/**
	 * Creates a mapper of type {@link PAULA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId)
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
		public DOCUMENT_STATUS mapSDocument() { 
			if (getDocument().getDocumentGraph()== null)
				getDocument().setDocumentGraph(SaltFactory.createSDocumentGraph());
			SDocumentGraph sDocGraph= getDocument().getDocumentGraph();
			if(sDocGraph!= null)
			{//if document contains a document graph
				List<SToken> sTokens= sDocGraph.getTokens();
				List<SToken> emptyTokensAtStart= new ArrayList<SToken>();
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
					{
						//for all tokens do
						sToken= sTokens.get(i);
						List<SRelation<SNode, SNode>> outRelations= sDocGraph.getOutRelations(sToken.getId());
						for (Relation outRelation: outRelations)
						{//find sTextRel
							if (outRelation instanceof STextualRelation)
							{
								sTextRel= (STextualRelation) outRelation;
								break;
							}
						}//find sTextRel
						if (sTextRel!= null)
						{//if textrel exists
							String text= sTextRel.getTarget().getText().substring(sTextRel.getStart(), sTextRel.getEnd());
							if (	(sTextRel.getStart()< sTextRel.getEnd()) &&
									(!" ".equals(text)))
							{//if current token is not an empty token and does not contain only a blank	
								{//create artificial span
									sSpan= SaltFactory.createSSpan();
									sDocGraph.addNode(sSpan);
								}//create artificial span
								{//create an artificial annotation with the overlapped text
									sNewAnno= SaltFactory.createSAnnotation();
									sNewAnno.setName(KW_WORD);
									sNewAnno.setValue(sTextRel.getTarget().getText().substring(sTextRel.getStart(), sTextRel.getEnd()));
									sSpan.addAnnotation(sNewAnno);
								}//create an artificial annotation with the overlapped text
								{//copy all annotations from token to span
									if (	(sToken.getAnnotations()!= null)&&
											(sToken.getAnnotations().size()> 0))
									{//if annotations exist	
										
										for (SAnnotation sAnno: sToken.getAnnotations())
										{//copy annotation and manipulate annotation
											if ("<unknown>".equalsIgnoreCase(sAnno.getValue_STEXT()))
												sAnno.setValue("[unknown]");
												
											sNewAnno= SaltFactory.createSAnnotation();
											sNewAnno.setNamespace(sAnno.getNamespace());
											sNewAnno.setName(sAnno.getName());
											sNewAnno.setValue(sAnno.getValue_STEXT());
											//change the name of the old annotation
											sAnno.setName(sAnno.getName()+ KW_POSTFIX);
											
											sSpan.addAnnotation(sNewAnno);
										}//copy annotation and manipulate annotation
									}//if annotations exist
								}//copy all annotations from token to span
								if (emptyTokensAtStart.size()>= 0)
								{//if there are empty tokens at start, put them to current span
									for (SToken emptyToken: emptyTokensAtStart)
									{
										sSpanRel= SaltFactory.createSSpanningRelation();
										sSpanRel.setTarget(emptyToken);
										sSpanRel.setSource(sSpan);
										sDocGraph.addRelation(sSpanRel);
									}
									//remove all entries from empty token list
									emptyTokensAtStart.clear();
								}//if there are empty tokens at start, put them to current span
							}//if current token is not an empty token and does not contain only a blank
							if (sSpan!= null)
							{//if span is not empty relate token to span via SSpanningRel
								sSpanRel= SaltFactory.createSSpanningRelation();
								sSpanRel.setTarget(sToken);
								sSpanRel.setSource(sSpan);
								sDocGraph.addRelation(sSpanRel);
							}//if span is not empty relate token to span via SSpanningRel
							else
							{//put token to list emptyTokensAtStart
								emptyTokensAtStart.add(sToken);
							}//put token to list emptyTokensAtruStart
						}
					}//if textrel exists
				}//for all tokens do
				{//create an SLayer for all SNodes
					SLayer sLayer= SaltFactory.createSLayer();
					sLayer.setName(((FalkoMaipulatorProperties)getProperties()).getSLayerName());
					boolean addLayer=false;
					{//adding tokens to layer
						for (SToken sToken: sDocGraph.getTokens())
						{
							if (	(sToken.getLayers()== null) ||
									(sToken.getLayers().size()==0))
							{
								sToken.getLayers().add(sLayer);
								addLayer=true;
							}
						}
					}//adding tokens to layer
					{//adding spans to layer
						for (SSpan sSpan: sDocGraph.getSpans())
						{
							if (	(sSpan.getLayers()== null) ||
									(sSpan.getLayers().size()==0))
							{
								sSpan.getLayers().add(sLayer);
								addLayer=true;
							}
						}
					}//adding spans to layer
					{//adding SpanningRelation to layer
						for (SSpanningRelation sSpanRel: sDocGraph.getSpanningRelations())
						{
							if (	(sSpanRel.getLayers()== null) ||
									(sSpanRel.getLayers().size()==0))
							{
								sSpanRel.getLayers().add(sLayer);
								addLayer=true;
							}
						}	
					}//adding SpanningRelation to layer
					
					if (addLayer)
						sDocGraph.getLayers().add(sLayer);
				}//create an SLayer for all SNodes
				
			}//if document contains a document graph
			return(DOCUMENT_STATUS.COMPLETED);
		}
	}
//================================ start: methods used by OSGi
}
