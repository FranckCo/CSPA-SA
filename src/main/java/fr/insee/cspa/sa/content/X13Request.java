package fr.insee.cspa.sa.content;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import ec.tss.xml.XmlTsData;
import ec.tss.xml.regression.XmlTsVariables;
import ec.tss.xml.x13.XmlX13Specification;

/**
 * The <code>X13Request</code> class maps the input request to the X13 seasonal adjustment service.
 * 
 * @author Franck Cotton, Guillaume Rateau
 */
@XmlRootElement(name = "X13Request")
@XmlAccessorType(XmlAccessType.FIELD)
public class X13Request {

    @XmlElement(name = "Series")
    private XmlTsData series;

    @XmlElement(name = "PreSpecification")
    private PreSpecificationEnum preSpecification;

    @XmlElement(name = "Specification")
    private XmlX13Specification specification;

    @XmlElement(name = "UserRegressors")
    private XmlTsVariables userRegressors;

    @XmlElement(name = "OutputFilter")
    @XmlList
    private List<String> outputFilter;

    /* ------------------- */
    /* Getters and setters */
    /* ------------------- */

	public XmlTsData getSeries() {
		return series;
	}

	public void setSeries(XmlTsData series) {
		this.series = series;
	}

	public PreSpecificationEnum getPreSpecification() {
		return preSpecification;
	}

	public void setPreSpecification(PreSpecificationEnum preSpecification) {
		this.preSpecification = preSpecification;
	}

	public XmlX13Specification getSpecification() {
		return specification;
	}

	public void setSpecification(XmlX13Specification specification) {
		this.specification = specification;
	}

	public XmlTsVariables getUserRegressors() {
		return userRegressors;
	}

	public void setUserRegressors(XmlTsVariables userRegressors) {
		this.userRegressors = userRegressors;
	}

	public List<String> getOutputFilter() {
		return outputFilter;
	}

	public void setOutputFilter(List<String> outputFilter) {
		this.outputFilter = outputFilter;
	}

}
