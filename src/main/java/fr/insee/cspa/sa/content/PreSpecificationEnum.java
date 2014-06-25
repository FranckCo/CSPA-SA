package fr.insee.cspa.sa.content;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;

public enum PreSpecificationEnum {
	
	RSA0(0), 
	RSA1(1), 
	RSA2(2), 
	RSA3(3), 
	RSA4(4), 
	RSA5(5);
	
	private final int value;

	private final String[] tsnames = {"RSA0","RSA1","RSA2","RSA3","RSA4","RSA5"};
	private final String[] x13names = {"X11","RSA1","RSA2c","RSA3","RSA4c","RSA5c"};
        
    private PreSpecificationEnum(int value) {
        this.value = value;
    }
    
    /**
     * Get the related tramoseats specification
     */ 
    public TramoSeatsSpecification getTramoSeatsSpecification() {
    	return TramoSeatsSpecification.fromString(tsnames[value]);
    }
    
    /**
     * Get the related X13 specification
     */ 
    public X13Specification getX13Specification() {
    	return X13Specification.fromString(x13names[value]);
    }
}
