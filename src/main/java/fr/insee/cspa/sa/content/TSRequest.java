package fr.insee.cspa.sa.content;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;

import ec.tss.xml.XmlTsData;
import ec.tss.xml.regression.XmlTsVariables;
import ec.tss.xml.tramoseats.XmlTramoSeatsSpecification;

@XmlRootElement(name = "TSRequest")
@XmlAccessorType(XmlAccessType.FIELD)
public class TSRequest {

    @XmlElement(name = "Series")
    private XmlTsData series;

    @XmlElement(name = "PreSpecification")
    private PreSpecificationEnum preSpecification;

    @XmlElement(name = "Specification")
    private XmlTramoSeatsSpecification specification;

    @XmlElement(name="UserRegressors")
    private XmlTsVariables userRegressors;

    @XmlElement(name = "OutputFilter")
    @XmlList
    private List<String> outputFilter;

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

	public XmlTramoSeatsSpecification getSpecification() {
		return specification;
	}

	public void setSpecification(XmlTramoSeatsSpecification specification) {
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
