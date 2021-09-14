package featureSelection.tester.statistics.record;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RecordFieldInfo {
	private String field;
	private String desc;
	private String dbTable;
	private String dbField;
}