package featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.code;

import java.util.Random;

import common.utils.RandomUtils;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity.BinaryHarmony;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.code.HarmonyInitialization;
import lombok.Data;

/**
 * Initiate Harmony in binary encoding with random binary value at each bit.
 */
@Data
public class BinaryHarmonyInitialization 
	implements HarmonyInitialization<BinaryHarmony>
{
	private Number minPossibleValueBoundOfHarmonyBit, maxPossibleValueBoundOfHarmonyBit;
	
	@Override
	public BinaryHarmony[] init(@SuppressWarnings("rawtypes") ReductionParameters param, Random random) {
		byte[] harmonyValue;
		BinaryHarmony[] harmonies = new BinaryHarmony[param.getGroupSize()];
		for (int i=0; i<harmonies.length; i++) {
			harmonyValue = new byte[param.getHarmonyMemorySize()];
			for (int a=0; a<harmonyValue.length; a++) {
				if (RandomUtils.probability(param.getHarmonyMemoryConsiderationRate(), random)) {
					harmonyValue[a] = (byte) 1;
				}
			}
			harmonies[i] = new BinaryHarmony(harmonyValue);
		}
		return harmonies;
	}
}