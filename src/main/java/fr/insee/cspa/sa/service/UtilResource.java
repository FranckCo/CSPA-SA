package fr.insee.cspa.sa.service;

import java.util.List;

import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;

public class UtilResource {

	// static class
	UtilResource() {}
	
	/**
	 * Creates an <code>InformationSet</code> object by combining three input information sets.
	 * 
	 * @param explicit <code>InformationSet</code> object specified explicitly.
	 * @param implicit <code>InformationSet</code> object corresponding to a predetermined specification.
	 * @param base <code>InformationSet</code> object with default values.
	 */
	static public InformationSet merge(InformationSet explicit, InformationSet implicit, InformationSet base) {
		
		List<Information<Object>> itemsExplicit = explicit.select(Object.class);
		List<Information<Object>> itemsImplicit = implicit.select(Object.class);
		Object itemExplicit, itemImplicit, itemBase;
		
	    for (Information<Object> info : itemsExplicit) {
			itemExplicit = info.value;
			itemImplicit = implicit.search(info.name, Object.class);
			itemBase = (base != null) ? base.search(info.name, Object.class) : null;
			if (itemExplicit instanceof InformationSet && itemImplicit != null && itemImplicit instanceof InformationSet) 
				explicit.set(info.name, merge((InformationSet) itemExplicit, (InformationSet) itemImplicit, (InformationSet) itemBase));
			else if (itemImplicit != null && itemExplicit.equals(itemBase) && !itemImplicit.equals(itemBase)) explicit.set(info.name, itemImplicit);
		}
		for (Information<Object> info : itemsImplicit) if (explicit.search(info.name, Object.class) == null) explicit.add(info.name, implicit.getSubSet(info.name));
		return explicit;
	}
}
