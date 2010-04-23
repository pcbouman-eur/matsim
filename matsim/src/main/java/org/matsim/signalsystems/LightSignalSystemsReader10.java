/* *********************************************************************** *
 * project: org.matsim.*
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2008 by the members listed in the COPYING,        *
 *                   LICENSE and WARRANTY file.                            *
 * email           : info at matsim dot org                                *
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *   See also COPYING, LICENSE and WARRANTY file                           *
 *                                                                         *
 * *********************************************************************** */

package org.matsim.signalsystems;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.matsim.core.basic.v01.IdImpl;
import org.matsim.core.utils.io.MatsimJaxbXmlParser;
import org.matsim.jaxb.lightsignalsystems10.ObjectFactory;
import org.matsim.jaxb.lightsignalsystems10.XMLIdRefType;
import org.matsim.jaxb.lightsignalsystems10.XMLLaneType;
import org.matsim.jaxb.lightsignalsystems10.XMLLanesToLinkAssignmentType;
import org.matsim.jaxb.lightsignalsystems10.XMLLightSignalGroupDefinitionType;
import org.matsim.jaxb.lightsignalsystems10.XMLLightSignalSystemDefinitionType;
import org.matsim.jaxb.lightsignalsystems10.XMLLightSignalSystems;
import org.matsim.lanes.Lane;
import org.matsim.lanes.LaneDefinitions;
import org.matsim.lanes.LaneDefinitionsFactory;
import org.matsim.lanes.LanesToLinkAssignment;
import org.matsim.signalsystems.systems.SignalGroupDefinition;
import org.matsim.signalsystems.systems.SignalSystemDefinition;
import org.matsim.signalsystems.systems.SignalSystems;
import org.matsim.signalsystems.systems.SignalSystemsFactory;
import org.xml.sax.SAXException;

/**
 * Reader for the lightSignalSystems_v1.0.xsd file format.
 * @author dgrether
 */
public class LightSignalSystemsReader10 extends MatsimJaxbXmlParser {

	private static final Logger log = Logger
			.getLogger(LightSignalSystemsReader10.class);

	private SignalSystems lightSignalSystems;
	private LaneDefinitions laneDefinitions;

	private SignalSystemsFactory builder;

	private LaneDefinitionsFactory laneBuilder;


	public LightSignalSystemsReader10(LaneDefinitions laneDefs,
			SignalSystems lightSignalSystems, String schemaLocation) {
		super(schemaLocation);
		this.laneDefinitions = laneDefs;
		this.lightSignalSystems = lightSignalSystems;
		this.laneBuilder = this.laneDefinitions.getFactory();
		this.builder = this.lightSignalSystems.getFactory();
	}

	@Override
	public void readFile(final String filename) throws JAXBException, SAXException, ParserConfigurationException, IOException {
		//create jaxb infrastructure
		JAXBContext jc;
		XMLLightSignalSystems xmlLssDefinition;
			jc = JAXBContext
					.newInstance(org.matsim.jaxb.lightsignalsystems10.ObjectFactory.class);
			ObjectFactory fac = new ObjectFactory();
			Unmarshaller u = jc.createUnmarshaller();
			// validate XML file
			super.validateFile(filename, u);
			log.info("starting unmarshalling " + filename);
			InputStream stream = null;
			try {
				stream = new FileInputStream(filename);
				xmlLssDefinition = (XMLLightSignalSystems) u.unmarshal(stream);
			}
			finally {
				try {
					if (stream != null) { stream.close();	}
				} catch (IOException e) {
					log.warn("Could not close stream.", e);
				}
			}

			//convert the parsed xml-instances to basic instances
			LanesToLinkAssignment l2lAssignment;
			Lane lane = null;
			for (XMLLanesToLinkAssignmentType lldef : xmlLssDefinition
					.getLanesToLinkAssignment()) {
				l2lAssignment = laneBuilder.createLanesToLinkAssignment(new IdImpl(lldef
						.getLinkIdRef()));
				for (XMLLaneType laneType : lldef.getLane()) {
					lane = laneBuilder.createLane(new IdImpl(laneType.getId()));
					for (XMLIdRefType toLinkId : laneType.getToLink()) {
						lane.addToLinkId(new IdImpl(toLinkId.getRefId()));
					}
					if (laneType.getRepresentedLanes() == null) {
						laneType.setRepresentedLanes(fac
								.createXMLLaneTypeXMLRepresentedLanes());
					}
					lane.setNumberOfRepresentedLanes(laneType.getRepresentedLanes()
							.getNumber());
					if (laneType.getLength() == null) {
						laneType.setLength(fac.createXMLLaneTypeXMLLength());
					}
					lane.setStartsAtMeterFromLinkEnd(laneType.getLength().getMeter());
					l2lAssignment.addLane(lane);
				}
				this.laneDefinitions.addLanesToLinkAssignment(l2lAssignment);
			}

			SignalSystemDefinition lssdef;
			for (XMLLightSignalSystemDefinitionType xmllssDef : xmlLssDefinition
					.getLightSignalSystemDefinition()) {
				lssdef = builder.createSignalSystemDefinition(new IdImpl(xmllssDef
						.getId()));
				lssdef.setDefaultCycleTime(xmllssDef.getDefaultCirculationTime()
						.getSeconds());
				lssdef.setDefaultInterGreenTime(xmllssDef.getDefaultInterimTime()
						.getSeconds());
				lssdef.setDefaultSynchronizationOffset(xmllssDef
						.getDefaultSyncronizationOffset().getSeconds());
				lightSignalSystems.addSignalSystemDefinition(lssdef);
			}
			// parsing lightSignalGroupDefinitions
			SignalGroupDefinition lsgdef;
			for (XMLLightSignalGroupDefinitionType xmllsgdef : xmlLssDefinition
					.getLightSignalGroupDefinition()) {
				lsgdef = builder.createSignalGroupDefinition(new IdImpl(xmllsgdef
						.getLinkIdRef()), new IdImpl(xmllsgdef.getId()));
				lsgdef.setSignalSystemDefinitionId(new IdImpl(xmllsgdef
						.getLightSignalSystemDefinition().getRefId()));
				for (XMLIdRefType refIds : xmllsgdef.getLane()) {
					lsgdef.addLaneId(new IdImpl(refIds.getRefId()));
				}
				for (XMLIdRefType refIds : xmllsgdef.getToLink()) {
					lsgdef.addToLinkId(new IdImpl(refIds.getRefId()));
				}
				lightSignalSystems.addSignalGroupDefinition(lsgdef);
			}

	}
}
