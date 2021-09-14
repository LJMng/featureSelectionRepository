package featureSelection.tester.procedure.basic;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class SimpleDataset4Test {
	public static List<String[]> sample1 =
			Arrays.asList(
					"1 1 1 1 0".split(" "),	//1
					"2 2 1 2 1".split(" "),	//2
					"1 1 1 1 0".split(" "),	//3
					"2 3 3 2 0".split(" "),	//4
					"2 2 1 2 1".split(" "),	//5
					"3 1 1 2 0".split(" "),	//6
					"1 2 2 3 2".split(" "),	//7
					"2 3 2 1 3".split(" "),	//8
					"3 1 1 2 1".split(" "),	//9
					"1 2 2 3 2".split(" "),	//10
					"3 1 1 2 1".split(" "),	//11
					"2 3 2 1 3".split(" "),	//12
					"4 3 2 4 1".split(" "),	//13
					"1 2 2 3 3".split(" "),	//14
					"4 3 2 4 2".split(" ")	//15*/
			);

	public static List<String[]> sample2 =
			Arrays.asList(
					"1 1 1 1 0".split(" "),	//1
					"2 2 1 1 0".split(" "),	//2
					"1 1 ? 1 0".split(" "),	//3
					"2 3 1 3 0".split(" "),	//4
					"2 ? 1 1 1".split(" "),	//5
					"? 1 2 1 0".split(" "),	//6
					"2 2 2 2 2".split(" "),	//7
					"2 ? 2 2 3".split(" "),	//8
					"3 1 2 1 1".split(" "),	//9
					"2 2 2 2 2".split(" "),	//10
					"? ? 2 1 1".split(" "),	//11
					"2 ? 2 2 3".split(" "),	//12
					"4 3 4 2 1".split(" "),	//13
					"2 2 2 2 2".split(" "),	//14
					"4 3 4 2 2".split(" ")	//15
			);


	public static class FromFile{

		public static String dataFileBasicPath = "G:/UCI Datasets";

		public static File wine(){
			return new File(dataFileBasicPath, "/S/wine-D.data");
		}

		public static File audiology(){
			return new File(dataFileBasicPath, "/S/Audiology/audiology.standardized.csv");
		}
	}

}
