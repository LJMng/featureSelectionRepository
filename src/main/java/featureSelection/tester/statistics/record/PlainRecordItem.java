package featureSelection.tester.statistics.record;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlainRecordItem<DataValue, DBValue> {
	private RecordFieldInfo name;
	private DataValue value;
	private DBValue dbValue;
}
