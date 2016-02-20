//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2012.12.18 at 02:24:41 PM CET 
//


package playground.gregor.sim2d_v4.io.jaxb.xlink;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the playground.gregor.sim2d_v4.io.jaxb.xlink package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Resource_QNAME = new QName("http://www.w3.org/1999/xlink", "resource");
    private final static QName _Title_QNAME = new QName("http://www.w3.org/1999/xlink", "title");
    private final static QName _Locator_QNAME = new QName("http://www.w3.org/1999/xlink", "locator");
    private final static QName _Arc_QNAME = new QName("http://www.w3.org/1999/xlink", "arc");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: playground.gregor.sim2d_v4.io.jaxb.xlink
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link XMLResourceType }
     * 
     */
    public XMLResourceType createXMLResourceType() {
        return new XMLResourceType();
    }

    /**
     * Create an instance of {@link XMLTitleEltType }
     * 
     */
    public XMLTitleEltType createXMLTitleEltType() {
        return new XMLTitleEltType();
    }

    /**
     * Create an instance of {@link XMLExtended }
     * 
     */
    public XMLExtended createXMLExtended() {
        return new XMLExtended();
    }

    /**
     * Create an instance of {@link XMLLocatorType }
     * 
     */
    public XMLLocatorType createXMLLocatorType() {
        return new XMLLocatorType();
    }

    /**
     * Create an instance of {@link XMLSimple }
     * 
     */
    public XMLSimple createXMLSimple() {
        return new XMLSimple();
    }

    /**
     * Create an instance of {@link XMLArcType }
     * 
     */
    public XMLArcType createXMLArcType() {
        return new XMLArcType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLResourceType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "resource")
    public JAXBElement<XMLResourceType> createResource(XMLResourceType value) {
        return new JAXBElement<XMLResourceType>(_Resource_QNAME, XMLResourceType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLTitleEltType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "title")
    public JAXBElement<XMLTitleEltType> createTitle(XMLTitleEltType value) {
        return new JAXBElement<XMLTitleEltType>(_Title_QNAME, XMLTitleEltType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLLocatorType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "locator")
    public JAXBElement<XMLLocatorType> createLocator(XMLLocatorType value) {
        return new JAXBElement<XMLLocatorType>(_Locator_QNAME, XMLLocatorType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link XMLArcType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.w3.org/1999/xlink", name = "arc")
    public JAXBElement<XMLArcType> createArc(XMLArcType value) {
        return new JAXBElement<XMLArcType>(_Arc_QNAME, XMLArcType.class, null, value);
    }

}