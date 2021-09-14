package featureSelection.tester.procedure.basic;

import common.utils.ArrayUtils;
import featureSelection.basic.model.universe.generator.UniverseGeneratorImp;
import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import org.junit.jupiter.api.BeforeAll;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class IncompleteDataTester {
	public static List<Instance> instances;

	private static boolean stringSource = true;
	private static File fromFile = SimpleDataset4Test.FromFile.audiology();

	@BeforeAll
	public static void initiateUniverse(){
		if (!stringSource) {
			UniverseGeneratorImp generator = new UniverseGeneratorImp();
			try {
				generator.setDataSetWithFileByLines(fromFile, ",", -1, "?");
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			instances = generator.universe();
		}else {
			stringIntArray2Universes();
		}
	}

	private static void stringIntArray2Universes() {
		Instance.resetID();

		String missingValue = "?";
		int[] values;
		List<Instance> instanceList = new LinkedList<>();
		for (String[] str : SimpleDataset4Test.sample2) {
			values = new int[str.length];
			values[0] = Integer.valueOf(str[str.length-1].replace(",", ""));
			boolean missV = false;
			for (int i=1; i<values.length; i++) {
				String trimValue = str[i-1].replace(",", "");
				if (missingValue.equals(trimValue)) {
					values[i] = IncompleteInstance.MISSING_VALUE;
					if (!missV)	missV = true;
				}else {
					values[i] = Integer.valueOf(trimValue);
				}
			}
			instanceList.add(missV? new IncompleteInstance(values): new Instance(values));
		}
		instances = instanceList;
	}


	public static int getAttributeLength(){
		return instances.get(0).getAttributeValues().length-1;
	}
	public static int[] getAllConditionalAttributes(){
		return ArrayUtils.initIncrementalValueIntArray(getAttributeLength(), 1, 1);
	}

}
