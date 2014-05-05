@XmlSchema(namespace = "http://xml.unece.org/cspa", 
        elementFormDefault = XmlNsForm.QUALIFIED, 
        attributeFormDefault = XmlNsForm.UNQUALIFIED, 
        location="",
        xmlns =
{
	@XmlNs(prefix = "cspa", namespaceURI = "http://xml.unece.org/cspa"),
    @XmlNs(prefix = "sa", namespaceURI = "ec/tss.sa"),
    @XmlNs(prefix = "tss", namespaceURI = "ec/tss.core"),
    @XmlNs(prefix = "arima", namespaceURI = "ec/tss.arima"),
    @XmlNs(prefix = "uscb", namespaceURI = "ec/tss.uscb"),
    @XmlNs(prefix = "x13", namespaceURI = "ec/tss.x13"),
    @XmlNs(prefix = "trs", namespaceURI = "ec/tss.tramoseats"),
    @XmlNs(prefix = "ss", namespaceURI = "http://www.sdmx.org/resources/sdmxml/schemas/v2_1/data/structurespecific")
})
package fr.insee.cspa.sa.content;

import javax.xml.bind.annotation.XmlNs;
import javax.xml.bind.annotation.XmlNsForm;
import javax.xml.bind.annotation.XmlSchema;