package featureSelection.tester.statistics.record.impl;

import common.utils.StringUtils;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.handler.DataValueHandling;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.utils.DBUtils;
import featureSelection.tester.utils.StatisticsUtils;

import java.util.*;

public class PlainRecord4Heuristic extends PlainRecord {

	private Map<String, RecordFieldInfo> titleMap;

	public PlainRecord4Heuristic(Collection<RecordFieldInfo> titles) {
		super(titles);
		titleMap = StatisticsUtils.Title.toMap(getTitles());
	}

	/* ---------------------------------------------------------------------------------- */

	/**
	 *
	 * Set value of a title.
	 *
	 * @see #set(Map, String, DataValueHandling, Object)
	 *
	 * @param <DataValue>
	 *     Type of data value.
	 * @param <DBValue>
	 *     Type of Database value.
	 * @param title
	 * 		The title of the value to be set.
	 * @param valueHandle
	 * 		Implemented {@link DataValueHandling} to handle value and return {@link DataValue}
	 * 		to set.
	 * @param dbValue
	 * 		The value to save into database.
	 * @return true if <code>title</code> exists in {@link #titleMap}.
	 */
	protected <DataValue, DBValue> boolean set(
			String title, DataValueHandling<DataValue> valueHandle, DBValue dbValue
	){
		return this.set(titleMap, title, valueHandle, dbValue);
	}

	protected void load(
			DatasetInfo datasetInfo, int[] attributes,
			int times, ProcedureParameters parameters, String testName,
			Statistics statistics, Map<String, Long> componentTagTimeMap,
			ProcedureContainer<?> container
	){
		String databaseUniqueID = DBUtils.generateUniqueID(datasetInfo.getDatasetName(), testName, parameters);

		Common.loadRecordBasicInfo(this, titleMap, container.id(), databaseUniqueID);
		Common.loadDatasetAlgorithmInfo(this, titleMap, datasetInfo, attributes, statistics);
		Common.loadCommonRuntimeInfo(this, titleMap, times, testName, container.shortName(), parameters, componentTagTimeMap);
		Common.loadCalculationInfo(this, titleMap, parameters);
		loadHeuristicSearchSignificanceCalculationLengths(statistics);
		loadPositiveRegionRemoving(datasetInfo, statistics);
		loadQuickReductTime(datasetInfo, componentTagTimeMap);
		loadQuickReductCore(parameters, statistics);
		loadQuickReductReductResult(statistics);
		Common.loadProcedureInfo(this, titleMap, container);
	}

	/* ---------------------------------------------------------------------------------- */

	@SuppressWarnings("unchecked")
	protected void loadPositiveRegionRemoving(
			DatasetInfo datasetInfo, Statistics statistics
	) {
		final Collection<Integer> removePos =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_INSTANCE_REMOVED);
		final Collection<Integer> removeNeg =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_INSTANCE_REMOVED);
		final Collection<Integer> removedAll = collectRemovedAll(removePos, removeNeg);

		final Collection<Integer> removePosComp =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_POS_COMPACTED_INSTANCE_REMOVED);
		final Collection<Integer> removeNegComp =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_NEG_COMPACTED_UNIVERSE_REMOVED);
		final Collection<Integer> removedAllComp = collectRemovedAll(removePosComp, removeNegComp);

		BasicExecutionInstanceInfo.Builder builder =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER);

		// SIGNIFICANCE_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.SIGNIFICANCE_HISTORY,
				() -> {
					Collection<Number> incrementSig =
							statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
					return incrementSig;
				},
				null
		);
		// SIGNIFICANCE_SUM
		this.set(
				StatisticsConstants.PlainRecordInfo.SIGNIFICANCE_SUM,
				() -> {
					Collection<Number> incrementSig =
							statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
					return StatisticsUtils.numberSum(incrementSig);
				},
				null
		);
		// SIGNIFICANCE_AVERAGE
		this.set(
				StatisticsConstants.PlainRecordInfo.SIGNIFICANCE_AVERAGE,
				() -> {
					Collection<Number> incrementSig =
							statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
					return incrementSig == null ?
							0 :
							StatisticsUtils.numberSum(incrementSig) / incrementSig.size();
				},
				null
		);

		// remove POS(U)：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_POS_REMOVE_NUMBER_HISTORY,
				() -> StringUtils.toString(removePos, 100),
				null
		);
		// Sum(remove POS(U))：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
				() -> StatisticsUtils.intSum(removePos),
				null
		);
		// Eva(remove POS(U))：TOTAL_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION,
				() -> {
					if (removePos != null) {
						Collection<Double> evalEach = new ArrayList<>(removePos.size());
						int insSize = datasetInfo.getUniverseSize(), evalDenominator = 0;
						for (int rm : removePos) {
							evalEach.add((rm + 0.0) / insSize);
							evalDenominator += insSize;
							insSize -= rm;
						}
						return evalDenominator == 0 ?
								0 : StatisticsUtils.intSum(removePos) / (evalDenominator + 0.0);
					} else {
						return 0;
					}
				},
				null
		);

		// remove NEG(U)：TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY,
				() -> StringUtils.toString(removeNeg, 100),
				null
		);
		// Sum(remove NEG(U))：TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
				() -> StatisticsUtils.intSum(removeNeg),
				null
		);
		// Eva(remove NEG(U))：TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION,
				() -> {
					if (removeNeg != null) {
						Collection<Double> evalEach = new ArrayList<>(removeNeg.size());
						int insSize = datasetInfo.getUniverseSize(), evalDenominator = 0;
						for (int rm : removeNeg) {
							evalEach.add((rm + 0.0) / insSize);
							evalDenominator += insSize;
							insSize -= rm;
						}
						return evalDenominator == 0 ?
								0 : StatisticsUtils.intSum(removeNeg) / (evalDenominator + 0.0);
					} else {
						return 0;
					}
				},
				null
		);

		// remove U：TOTAL_UNIVERSE_REMOVE_NUMBER_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_NUMBER_HISTORY,
				() -> StringUtils.toString(removedAll, 100),
				null
		);
		// Sum(remove NEG(U))：TOTAL_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
				() -> removedAll == null ? null : StatisticsUtils.intSum(removedAll),
				null
		);

		// Eva(remove U)：TOTAL_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION,
				() -> {
					Collection<Double> evalEach = new ArrayList<>(removedAll.size());
					int insSize = datasetInfo.getUniverseSize();
					for (int rm : removedAll) {
						evalEach.add((rm + 0.0) / insSize);
						insSize -= rm;
					}
					return StringUtils.numberToString(evalEach, 100, 4);
				},
				null
		);
		// Eva(Sum(remove U))：TOTAL_UNIVERSE_REMOVE_TOTAL_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.TOTAL_UNIVERSE_REMOVE_TOTAL_EVALUATION,
				() -> {
					Collection<Double> evalEach = new ArrayList<>(removedAll.size());
					int insSize = datasetInfo.getUniverseSize(), evalDenominator = 0;
					for (int rm : removedAll) {
						evalEach.add((rm + 0.0) / insSize);
						evalDenominator += insSize;
						insSize -= rm;
					}
					return evalDenominator == 0 ?
							0 : StatisticsUtils.intSum(removedAll) / (evalDenominator + 0.0);
				},
				null
		);
		// remove POS(U/C)：COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_HISTORY,
				() -> removePosComp == null ? null : StringUtils.toString(removePosComp, 100),
				null
		);
		// Sum(remove POS(U/C))：COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
				() -> removePosComp == null ? null : StatisticsUtils.intSum(removePosComp),
				null
		);
		// Eva(remove POS(U/C))：COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_POS_REMOVE_NUMBER_EVALUATION,
				() -> {
					if (removePosComp != null) {
						Collection<Double> evalEach = new ArrayList<>(removePosComp.size());
						int insSize = datasetInfo.getUniverseSize();
						int evalDenominator = 0;
						for (int rm : removePosComp) {
							evalEach.add((rm + 0.0) / insSize);
							evalDenominator += insSize;
							insSize -= rm;
						}
						return evalDenominator == 0 ?
								0 : StatisticsUtils.intSum(removePosComp) / (evalDenominator + 0.0);
					} else {
						return 0;
					}
				},
				null
		);

		// remove NEG(U/C)：COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_HISTORY,
				() -> removeNegComp == null ? null : StringUtils.toString(removeNegComp, 100),
				null
		);
		// Sum(remove NEG(U/C))：COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
				() -> removeNegComp == null ? null : StatisticsUtils.intSum(removeNegComp),
				null
		);
		// Eva(remove NEG(U/C))：COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_NEG_REMOVE_NUMBER_EVALUATION,
				() -> {
					if (removeNegComp != null) {
						Collection<Double> evalEach = new ArrayList<>(removeNegComp.size());
						int insSize = datasetInfo.getUniverseSize(), evalDenominator = 0;
						for (int rm : removeNegComp) {
							evalEach.add((rm + 0.0) / insSize);
							evalDenominator += insSize;
							insSize -= rm;
						}
						return evalDenominator == 0 ?
								0 : StatisticsUtils.intSum(removeNegComp) / (evalDenominator + 0.0);
					} else {
						return 0;
					}
				},
				null
		);

		// remove U/C：COMPACTED_UNIVERSE_REMOVE_NUMBER_HISTORY
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_REMOVE_NUMBER_HISTORY,
				() -> removedAllComp == null ? null : StringUtils.toString(removedAllComp, 100),
				null
		);
		// Sum(remove U/C)：COMPACTED_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_REMOVE_NUMBER_SUM_4_TOTAL_UNIVERSE,
				() -> removedAllComp == null ? null : StatisticsUtils.intSum(removedAllComp),
				null
		);
		// Eva(remove U/C)：COMPACTED_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_REMOVE_INDIVIDUAL_EVALUATION,
				() -> {
					Collection<Double> evalEach = new ArrayList<>(removedAllComp.size());
					int equClassSize = builder.build().getCompressedInstanceNumber();
					if (equClassSize != 0) {
						for (int rm : removedAllComp) {
							evalEach.add((rm + 0.0) / equClassSize);
							equClassSize -= rm;
						}
					}
					return StringUtils.numberToString(evalEach, 100, 4);
				},
				null
		);
		// Eva(Sum(remove U/C))：COMPACTED_UNIVERSE_REMOVE_TOTAL_EVALUATION
		this.set(
				StatisticsConstants.PlainRecordInfo.COMPACTED_UNIVERSE_REMOVE_TOTAL_EVALUATION,
				() -> {
					Collection<Double> evalEach = new ArrayList<>(removedAllComp.size());
					int equClassSize = builder.build().getCompressedInstanceNumber();
					int evalDenominator = 0;
					if (equClassSize != 0) {
						evalDenominator = 0;
						for (int rm : removedAllComp) {
							evalEach.add((rm + 0.0) / equClassSize);
							evalDenominator += equClassSize;
							equClassSize -= rm;
						}
					}
					return evalDenominator == 0 ?
							0 : StatisticsUtils.intSum(removedAllComp) / (evalDenominator + 0.0);
				},
				null
		);
	}

	protected void loadQuickReductTime(
			DatasetInfo datasetInfo, Map<String, Long> componentTagTimeMap
	) {
		// TIME_INIT
		this.set(
				StatisticsConstants.PlainRecordInfo.TIME_INIT,
				() -> TimerUtils.nanoTimeToMillis(datasetInfo.getInitTime()),
				null
		);
		// TIME_COMPRESS
		this.set(
				StatisticsConstants.PlainRecordInfo.TIME_COMPRESS,
				() -> {
					Long compressTime = componentTagTimeMap.get(ComponentTags.TAG_COMPACT);
					return TimerUtils.nanoTimeToMillis(compressTime == null ? 0 : compressTime);
				},
				null
		);
		// TIME_CORE
		this.set(
				StatisticsConstants.PlainRecordInfo.TIME_CORE,
				() ->{
					Long coreTime = componentTagTimeMap.get(ComponentTags.TAG_CORE);
					return TimerUtils.nanoTimeToMillis(coreTime == null ? 0 : coreTime);
				},
				null
		);
		// TIME_RED
		this.set(
				StatisticsConstants.PlainRecordInfo.TIME_RED,
				() -> {
					Long redTime = componentTagTimeMap.get(ComponentTags.TAG_SIG);
					return TimerUtils.nanoTimeToMillis(redTime == null ? 0 : redTime);
				},
				null
		);
		// TIME_INSPECT
		this.set(
				StatisticsConstants.PlainRecordInfo.TIME_INSPECT,
				() -> {
					Long inspectTime = componentTagTimeMap.get(ComponentTags.TAG_CHECK);
					return TimerUtils.nanoTimeToMillis(inspectTime == null ? 0 : inspectTime);
				},
				null
		);
	}

	protected void loadQuickReductCore(
			ProcedureParameters parameters, Statistics statistics
	) {
		// CORE_INCLUDE
		this.set(
				StatisticsConstants.PlainRecordInfo.CORE_INCLUDE,
				() -> {
					Boolean byCore = parameters.get("byCore");
					return byCore != null && byCore;
				},
				null
		);
		// CORE_SIZE
		this.set(
				StatisticsConstants.PlainRecordInfo.CORE_SIZE,
				() -> {
					Integer[] core = statistics.get(StatisticsConstants.Procedure.STATISTIC_CORE_LIST);
					return core != null ? core.length : 0;
				},
				null
		);
		// CORE_LIST
		this.set(
				StatisticsConstants.PlainRecordInfo.CORE_LIST,
				() -> {
					Integer[] core = statistics.get(StatisticsConstants.Procedure.STATISTIC_CORE_LIST);
					if (core != null) Arrays.sort(core);
					return core == null ? new Integer[0] : Arrays.toString(core);
				},
				null
		);
		// STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH
		this.set(
				StatisticsConstants.PlainRecordInfo.CORE_ATTRIBUTE_EXAM_SIZE,
				() -> {
					Object coreExamSize =
							statistics.get(StatisticsConstants.Procedure.STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH);
					return coreExamSize == null ? 0 : coreExamSize;
				},
				null
		);
	}

	protected void loadHeuristicSearchSignificanceCalculationLengths(Statistics statistics) {
		// CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LIST
		this.set(
				StatisticsConstants.PlainRecordInfo.CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LIST,
				() -> {
					@SuppressWarnings("unchecked")
					Collection<Integer> redSigCalAttrs =
							statistics.get(StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH);
					return redSigCalAttrs == null ? "" : redSigCalAttrs;
				},
				null
		);
		// CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LENGTH
		this.set(
				StatisticsConstants.PlainRecordInfo.CALCULATION_HEURISTIC_LENGTH_IN_SEARCH_LENGTH,
				() -> {
					@SuppressWarnings("unchecked")
					Collection<Integer> redSigCalAttrs =
							statistics.get(StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH);
					return redSigCalAttrs == null ? "" : redSigCalAttrs.stream().reduce(Integer::sum).orElse(0);
				},
				null
		);
	}

	protected void loadQuickReductReductResult(Statistics statistics) {
		@SuppressWarnings("unchecked")
		Collection<Integer> redCandidate =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT);
		@SuppressWarnings("unchecked")
		Collection<Integer> red =
				statistics.get(StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT);
		int redundantLength = redCandidate == null ? 0 : redCandidate.size();
		if (red != null && redundantLength != 0) redundantLength = redundantLength - red.size();
		int[] redundant = new int[redundantLength];
		if (redundant.length != 0 && red != null && redCandidate != null) {
			int i = 0;
			for (int attr : redCandidate) if (!red.contains(attr)) redundant[i++] = attr;
		}

		// REDUCT_SIZE
		this.set(
				StatisticsConstants.PlainRecordInfo.REDUCT_SIZE,
				() -> red == null ? (redCandidate == null ? 0 : redCandidate.size()) : red.size(),
				null
		);
		// REDUCT_LIST
		this.set(
				StatisticsConstants.PlainRecordInfo.REDUCT_LIST,
				() -> {
					List<Integer> redList = new ArrayList<>(red != null ? red : redCandidate);
					Collections.sort(redList);
					return redList;
				},
				null
		);
		// REDUNDANT_SIZE_BEFORE_INSPECT
		this.set(
				StatisticsConstants.PlainRecordInfo.REDUNDANT_SIZE_BEFORE_INSPECT,
				() -> redundant.length,
				null
		);
		// REDUNDANT_LIST_BEFORE_INSPECT
		this.set(
				StatisticsConstants.PlainRecordInfo.REDUNDANT_LIST_BEFORE_INSPECT,
				() -> StringUtils.intToString(redundant, 500),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

	private Collection<Integer> collectRemovedAll(
			Collection<Integer> removePos, Collection<Integer> removeNeg
	){
		Collection<Integer> removedAll = new ArrayList<>(Math.max(
				removePos == null ? 0 : removePos.size(),
				removeNeg == null ? 0 : removeNeg.size()
		));

		if ((removePos != null && removePos.size() > 0) ||
				(removeNeg != null && removeNeg.size() > 0)
		) {
			Iterator<Integer> posIterator = null, negIterator = null;
			if (removePos != null) posIterator = removePos.iterator();
			if (removeNeg != null) negIterator = removeNeg.iterator();
			while ((posIterator != null && posIterator.hasNext()) ||
					(negIterator != null && negIterator.hasNext())
			) {
				int pos = posIterator == null ?
						0 : posIterator.hasNext() ? posIterator.next() : 0;
				int neg = negIterator == null ?
						0 : negIterator.hasNext() ? negIterator.next() : 0;
				removedAll.add(pos + neg);
			}
		}
		return removedAll;
	}

	/* ---------------------------------------------------------------------------------- */
}
