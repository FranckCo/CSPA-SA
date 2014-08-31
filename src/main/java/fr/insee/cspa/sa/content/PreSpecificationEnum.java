package fr.insee.cspa.sa.content;

import ec.satoolkit.tramoseats.TramoSeatsSpecification;
import ec.satoolkit.x13.X13Specification;

/**
 * The <code>PreSpecificationEnum</code> enumeration lists the identifiers corresponding to the predefined Tramo-Seats or X13 specifications.
 * 
 * @author Franck Cotton, Guillaume Rateau.
 */
public enum PreSpecificationEnum {
	
	RSA0(0), 
	RSA1(1), 
	RSA2(2), 
	RSA3(3), 
	RSA4(4), 
	RSA5(5);
	
	private final int value;

	/** Usual names for the Tramo-Seats predefined specifications */
	private final String[] tsNames = {"RSA0","RSA1","RSA2","RSA3","RSA4","RSA5"};

	/** Usual names for the X13 predefined specifications */
	private final String[] x13Names = {"X11","RSA1","RSA2c","RSA3","RSA4c","RSA5c"};
        
    private PreSpecificationEnum(int value) {
        this.value = value;
    }
    
    /**
     * Gets the predefined Tramo-Seats specification corresponding to this identifier.
     * 
     * @return A <code>TramoSeatsSpecification</code> object.
     */ 
    public TramoSeatsSpecification getTramoSeatsSpecification() {
    	return TramoSeatsSpecification.fromString(tsNames[value]);
    }
    
    /**
     * Gets the predefined related X13 specification specification corresponding to this identifier.
     * 
     * @return A <code>X13Specification</code> object.
     */ 
    public X13Specification getX13Specification() {
    	return X13Specification.fromString(x13Names[value]);
    }
}
