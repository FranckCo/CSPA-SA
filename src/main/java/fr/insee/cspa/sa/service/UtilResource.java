package fr.insee.cspa.sa.service;

import java.util.List;

import ec.tstoolkit.information.Information;
import ec.tstoolkit.information.InformationSet;

public class UtilResource {

	// static class
	UtilResource() {}
	
	/**
	 * Merge InformationSet
	 * @param a : specified InformationSet
	 * @param b : model InformationSet
	 * @param c : default InformationSet
	 */
	static public InformationSet merge(InformationSet a, InformationSet b, InformationSet c) {
		
		List<Information<Object>> itemsA = a.select(Object.class);
		List<Information<Object>> itemsB = b.select(Object.class);
		Object itemA, itemB, itemC;
		
	    for (Information<Object> info : itemsA) {
			itemA = info.value;
			itemB = b.search(info.name, Object.class);
			itemC = (c != null) ? c.search(info.name, Object.class) : null;
			if (itemA instanceof InformationSet && itemB != null && itemB instanceof InformationSet) 
				a.set(info.name, merge((InformationSet)itemA, (InformationSet) itemB, (InformationSet) itemC));
			else if (itemB != null && itemA.equals(itemC) && !itemB.equals(itemC)) a.set(info.name, itemB);
		}
		for (Information<Object> info : itemsB) if (a.search(info.name,Object.class) == null) a.add(info.name, b.getSubSet(info.name));
		return a;
	}
}
