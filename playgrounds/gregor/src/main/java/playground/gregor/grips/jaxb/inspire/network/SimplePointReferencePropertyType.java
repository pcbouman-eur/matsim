//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2011.08.04 at 02:05:46 PM CEST 
//


package playground.gregor.grips.jaxb.inspire.network;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for SimplePointReferencePropertyType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SimplePointReferencePropertyType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{urn:x-inspire:specification:gmlas:Network:3.2}SimplePointReference"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SimplePointReferencePropertyType", propOrder = {
    "simplePointReference"
})
public class SimplePointReferencePropertyType {

    @XmlElement(name = "SimplePointReference", required = true)
    protected SimplePointReferenceType simplePointReference;

    /**
     * Gets the value of the simplePointReference property.
     * 
     * @return
     *     possible object is
     *     {@link SimplePointReferenceType }
     *     
     */
    public SimplePointReferenceType getSimplePointReference() {
        return simplePointReference;
    }

    /**
     * Sets the value of the simplePointReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link SimplePointReferenceType }
     *     
     */
    public void setSimplePointReference(SimplePointReferenceType value) {
        this.simplePointReference = value;
    }

    public boolean isSetSimplePointReference() {
        return (this.simplePointReference!= null);
    }

}