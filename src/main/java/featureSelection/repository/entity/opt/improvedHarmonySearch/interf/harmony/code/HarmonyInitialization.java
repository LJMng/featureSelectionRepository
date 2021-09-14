package featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.code;

import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;

import java.util.Random;

/**
 * Initiate harmonies.
 * 
 * @author Benjamin_L
 *
 * @param <Hmny>
 * 		Type of implemented {@link Harmony}.
 */
public interface HarmonyInitialization<Hmny extends Harmony<?>> {
	/**
	 * Initiate harmonies using the given {@link Random} instance.
	 * 
	 * @param param
	 * 		{@link ReductionParameters}.
	 * @param random
	 * 		{@link Random} instance.
	 * @return {@link Haromony} array with harmonies initiated.
	 */
	Hmny[] init(@SuppressWarnings("rawtypes") ReductionParameters param, Random random);

	void setMinPossibleValueBoundOfHarmonyBit(Number min);
	void setMaxPossibleValueBoundOfHarmonyBit(Number max);
	Number getMinPossibleValueBoundOfHarmonyBit();
	Number getMaxPossibleValueBoundOfHarmonyBit();
}