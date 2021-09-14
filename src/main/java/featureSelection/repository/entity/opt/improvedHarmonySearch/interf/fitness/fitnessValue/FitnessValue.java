package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue;

/**
 * An interface for Harmony Fitness Value in <code>Improved Harmony Search Algorithm</code>.
 * 
 * @author Benjamin_L
 *
 * @param <V>
 * 		The type of fitness value. Implemented {@link Number}.
 */
public interface FitnessValue<V extends Number> {
	V getValue();
}
