/* *********************************************************************** *
 * project: org.matsim.*
 * MatsimLaneWriter
 *                                                                         *
 * *********************************************************************** *
 *                                                                         *
 * copyright       : (C) 2009 by the members listed in the COPYING,        *
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
package org.matsim.lanes;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.crypto.MarshalException;

import org.apache.log4j.Logger;
import org.matsim.api.core.v01.Id;
import org.matsim.core.utils.io.IOUtils;
import org.matsim.core.utils.io.MatsimJaxbXmlWriter;
import org.matsim.jaxb.lanedefinitions20.ObjectFactory;
import org.matsim.jaxb.lanedefinitions20.XMLIdRefType;
import org.matsim.jaxb.lanedefinitions20.XMLLaneDefinitions;
import org.matsim.jaxb.lanedefinitions20.XMLLaneType;
import org.matsim.jaxb.lanedefinitions20.XMLLanesToLinkAssignmentType;
/**
 * Writer for the http://www.matsim.org/files/dtd/laneDefinitions_v2.0.xsd
 * file format.
 * @author dgrether
 *
 */
public class LaneDefinitionsWriter20 extends MatsimJaxbXmlWriter {

	private static final Logger log = Logger
			.getLogger(LaneDefinitionsWriter20.class);

	private LaneDefinitions laneDefinitions;

	private XMLLaneDefinitions xmlLaneDefinitions;

	/**
	 * Writer for the http://www.matsim.org/files/dtd/laneDefinitions_v2.0.xsd
	 * file format.
	 * @param lanedefs
	 *
	 */
	public LaneDefinitionsWriter20(LaneDefinitions lanedefs) {
		log.info("Using LaneDefinitionWriter20...");
		this.laneDefinitions = lanedefs;
	}

	/**
	 * @see org.matsim.core.utils.io.MatsimJaxbXmlWriter#writeFile(java.lang.String)
	 */
	@Override
	public void writeFile(String filename) {
		log.info("writing to file: " + filename);
  	JAXBContext jc;
		try {
			this.xmlLaneDefinitions = convertBasicToXml();
			jc = JAXBContext.newInstance(org.matsim.jaxb.lanedefinitions20.ObjectFactory.class);
			Marshaller m = jc.createMarshaller();
			super.setMarshallerProperties(MatsimLaneDefinitionsReader.SCHEMALOCATIONV20, m);
			m.marshal(this.xmlLaneDefinitions, IOUtils.getBufferedWriter(filename));
		} catch (JAXBException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MarshalException e) {
			e.printStackTrace();
		}
	}

	private XMLLaneDefinitions convertBasicToXml() throws MarshalException {
		ObjectFactory fac = new ObjectFactory();
		XMLLaneDefinitions xmllaneDefs = fac.createXMLLaneDefinitions();

		for (LanesToLinkAssignment ltla : this.laneDefinitions.getLanesToLinkAssignments().values()) {
			XMLLanesToLinkAssignmentType xmlltla = fac.createXMLLanesToLinkAssignmentType();
			xmlltla.setLinkIdRef(ltla.getLinkId().toString());

			for (Lane bl : ltla.getLanes().values()) {
				XMLLaneType xmllane = fac.createXMLLaneType();
				xmllane.setId(bl.getId().toString());

				if ((bl.getToLinkIds() == null && bl.getToLaneIds() != null) || 
						(bl.getToLinkIds() != null && bl.getToLaneIds() == null)){
					xmllane.setLeadsTo(fac.createXMLLaneTypeXMLLeadsTo());
				}
				else {
					throw new MarshalException("Either at least one toLinkId or (exclusive) one toLaneId must" +
							"be set for a Lane! Cannot write according to XML grammar.");
				}
				
				if (bl.getToLinkIds() != null){
					for (Id id : bl.getToLinkIds()) {
						XMLIdRefType xmlToLink = fac.createXMLIdRefType();
						xmlToLink.setRefId(id.toString());
						xmllane.getLeadsTo().getToLink().add(xmlToLink);
					}
				}
				else if (bl.getToLaneIds() != null){
					for (Id id : bl.getToLaneIds()) {
						XMLIdRefType xmlToLink = fac.createXMLIdRefType();
						xmlToLink.setRefId(id.toString());
						xmllane.getLeadsTo().getToLane().add(xmlToLink);
					}
				}

				XMLLaneType.XMLRepresentedLanes lanes = new XMLLaneType.XMLRepresentedLanes();
				lanes.setNumber(Double.valueOf(bl.getNumberOfRepresentedLanes()));
				xmllane.setRepresentedLanes(lanes);

				XMLLaneType.XMLStartsAt startsAt = new XMLLaneType.XMLStartsAt();
				startsAt.setMeterFromLinkEnd(Double.valueOf(bl.getStartsAtMeterFromLinkEnd()));
				xmllane.setStartsAt(startsAt);

				xmllane.setAlignment(bl.getAlignment());
				
				xmlltla.getLane().add(xmllane);
			}
			xmllaneDefs.getLanesToLinkAssignment().add(xmlltla);
		} //end writing lanesToLinkAssignments

		return xmllaneDefs;
	}

}
