package featureSelection.tester.statistics.record.impl.opt;

import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.handler.DataValueHandling;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.utils.StatisticsUtils;

import java.util.*;

import lombok.Getter;

@Getter
public abstract class PlainRecords4Optimization extends PlainRecord {

	protected Map<String, RecordFieldInfo> titleMap;

	public PlainRecords4Optimization(Collection<RecordFieldInfo> titles) {
		super(titles);
		titleMap = StatisticsUtils.Title.toMap(getTitles());
	}

	/* ---------------------------------------------------------------------------------- */

	/**
	 * Set value of a title.
	 *
	 * @param <DataValue> Type of data value.
	 * @param <DBValue>   Type of Database value.
	 * @param title       The title of the value to be set.
	 * @param valueHandle Implemented {@link DataValueHandling} to handle value and return {@link DataValue}
	 *                    to set.
	 * @param dbValue     The value to save into database.
	 * @return true if <code>title</code> exists in {@link #titleMap}.
	 * @see #set(Map, String, DataValueHandling, Object)
	 */
	protected <DataValue, DBValue> boolean set(
			String title, DataValueHandling<DataValue> valueHandle, DBValue dbValue
	) {
		return this.set(titleMap, title, valueHandle, dbValue);
	}

	/* ---------------------------------------------------------------------------------- */

	protected void loadTimeInfo4(
			PlainRecord record, DatasetInfo datasetInfo,
			Map<String, Long> componentTagTimeMap
	) {
		// TIME_INIT
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.TIME_INIT,
				() -> TimerUtils.nanoTimeToMillis(datasetInfo.getInitTime()),
				null
		);
		// TIME_COMPRESS
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.TIME_COMPRESS,
				() -> {
					Long compressTime = componentTagTimeMap.get(ComponentTags.TAG_COMPACT);
					return TimerUtils.nanoTimeToMillis(compressTime == null ? 0 : compressTime);
				},
				null
		);
		// TIME_RED
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.TIME_RED,
				() -> {
					Long redTime = componentTagTimeMap.get(ComponentTags.TAG_SIG);
					return TimerUtils.nanoTimeToMillis(redTime == null ? 0 : redTime);
				},
				null
		);
		// TIME_INSPECT_DURING
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.TIME_INSPECT_DURING,
				() -> {
					Long inspectTimeDuring = componentTagTimeMap.get(ComponentTags.TAG_INSPECT_DURING);
					return TimerUtils.nanoTimeToMillis(inspectTimeDuring == null ? 0 : inspectTimeDuring);
				},
				null
		);
		// TIME_INSPECT
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.TIME_INSPECT_LAST,
				() -> {
					Long inspectTimeLast = componentTagTimeMap.get(ComponentTags.TAG_INSPECT_LAST);
					return TimerUtils.nanoTimeToMillis(inspectTimeLast == null ? 0 : inspectTimeLast);
				},
				null
		);
		// TIME_SOLUTION_SELECTION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.TIME_SOLUTION_SELECTION,
				() -> {
					Long solutionSelectionTime = componentTagTimeMap.get(ComponentTags.TAG_SOLUTION_SELECTION);
					return solutionSelectionTime == null ?
							"" : TimerUtils.nanoTimeToMillis(solutionSelectionTime);
				},
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
