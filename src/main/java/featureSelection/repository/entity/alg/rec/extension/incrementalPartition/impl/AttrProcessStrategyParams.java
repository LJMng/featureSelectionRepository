package featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl;

import java.util.HashMap;
import java.util.Map;

import lombok.Setter;

/**
 * Full name: AttributeProcessStrategyParameters (Attribute Process Strategy Parameters)
 * <p>
 * An entity for containing parameters for Attribute processing.
 * 
 * @author Benjamin_L
 */
public class AttrProcessStrategyParams {
	@Setter private Map<String, Object> parameters;
	
	public <V> AttrProcessStrategyParams set(String key, V value) {
		if (parameters==null)	parameters=new HashMap<>();
		parameters.put(key, value);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public <V> V get(String key){
		return (V) parameters.get(key);
	}
}
