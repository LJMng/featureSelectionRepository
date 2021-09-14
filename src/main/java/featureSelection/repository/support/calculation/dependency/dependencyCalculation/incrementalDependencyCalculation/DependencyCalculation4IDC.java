package featureSelection.repository.support.calculation.dependency.dependencyCalculation.incrementalDependencyCalculation;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.entity.alg.incrementalDependencyCalculation.HashMapValue;
import featureSelection.repository.support.calculation.alg.dependencyCalculation.incrementalDependencyCalculation.FeatureImportance4IncrementalDependencyCalculation;
import featureSelection.repository.support.calculation.dependency.DefaultDependencyCalculation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DependencyCalculation4IDC
	extends DefaultDependencyCalculation
	implements FeatureImportance4IncrementalDependencyCalculation<Double>
{
	private double dependency;
	@Override
	public Double getResult() {
		return dependency;
	}

	public DependencyCalculation4IDC calculate(
			Collection<Instance> instances, IntegerIterator attributes,
			Object...args
	) {
		// Count the current calculation
		countCalculate(attributes.size());
		// Calculate
		dependency = instances==null || instances.isEmpty()?
						0: dependency(instances, attributes);
		return this;
	}
	
	private static double dependency(
			Collection<Instance> instances, IntegerIterator attributes
	) {
		if (instances.size()==0)	return 0;
		
		// New a HashMap(H)
		Map<IntArrayKey, HashMapValue> map = new HashMap<>();
		// Initiate
		int udv=0;
		// Go through Universes(X)
		int index;
		IntArrayKey key; 
		int[] attrValues;
		HashMapValue mapValue;
		for (Instance universe : instances) {
			// Count the universe(x)
			// Set the key with the given attributes' values : P(x)
			attrValues = new int[attributes.size()];
			attributes.reset();
			while (attributes.hasNext()) {
				index = attributes.currentIndex();
				attrValues[index] = universe.getAttributeValue(attributes.next());
			}
			key = new IntArrayKey(attrValues);
			// Search for the key in map.
			// If no such key
			if ( !map.containsKey(key) ) {
				// create a sub item(h), h.cons=true, h.dec=x.dec, h.count=1
				map.put(key, new HashMapValue(true, universe.getAttributeValue(0)));
				udv++;
			// else
			}else {
				// Get the correspondent sub item(h)
				mapValue = map.get(key);
				// h.count++
				mapValue.add();
				if (mapValue.cons()) {
					// If h.cons is true
					if (mapValue.decisionValue()==universe.getAttributeValue(0)) {
						// If h.dec equals x.dec, udv++
						udv++;
					}else {
						// Else h.cons=false, UDV=UNV-h.count+1
						mapValue.setCons(false);
						udv = udv-mapValue.count()+1;
					}
				}
				map.replace(key, mapValue);
			}
		}
		return (double)udv / (double)instances.size();
	}

	@Override
	public <Item> boolean calculateAble(Item item) {
		return item instanceof Collection;
	}

	@Override
	public Double difference(Double v1, Double v2) {
		return (v1==null?0:v1) - (v2==null?0:v2);
	}
}