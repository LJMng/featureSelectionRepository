package featureSelection.tester.statistics.record.impl.opt;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.info.ExecutionInfoStorage;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class PlainRecords4OptSummary
		extends PlainRecords4Optimization
{
	public PlainRecords4OptSummary(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	protected abstract void load(
			DatasetInfo datasetInfo, int[] attributes,
			ProcedureParameters parameters, String testName,
			Statistics statistics,
			ExecutionInfoStorage<OptimizationReduct> executionInfoStorage
	);

	/* ---------------------------------------------------------------------------------- */

	protected void initReductInfo4Summary(
			int times,
			Map<IntArrayKey, Map<IntArrayKey, Collection<OptimizationReduct>>> redMap
	) {
		/* ----------------------------------------------------------------------------------- */
		int reductNumber = redMap.entrySet().stream().mapToInt(entry -> entry.getValue().size()).sum();
		int codeNumber = redMap.values().stream().mapToInt(
							map -> map.values().stream().mapToInt(Collection::size).sum()
						).sum();
		final Collection<Integer> candidateAttributeLength =
				redMap.values().stream().flatMap(
						map -> map.values().stream().flatMap(
								collection -> collection.stream().map(
										optRed -> optRed.getRedsCodingBeforeInspection().getAttributes().length
								)
						)
				).collect(Collectors.toList());
		final Collection<Integer> reductAttributeLength =
				redMap.values().stream().flatMap(
						map -> map.keySet().stream().map(reductKey -> reductKey.key().length)
				).collect(Collectors.toList());
		/* ----------------------------------------------------------------------------------- */
		// RUN_TIME_ID
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.RUN_TIME_ID,
				() -> times,
				null
		);
		// REDUCT_ID
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_ID,
				() -> "-",
				null
		);
		// REDUCT_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_NUMBER,
				() -> reductNumber,
				null
		);
		// REDUCT_DISTINCT_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_DISTINCT_NUMBER,
				() -> redMap.values().stream().flatMap(map -> map.keySet().stream()).distinct().count(),
				null
		);
		// REDUCT_CODE_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CODE_NUMBER,
				() -> codeNumber,
				null
		);
		/* ----------------------------------------------------------------------------------- */
		// REDUCT_NO
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_NO,
				() -> "-",
				null
		);
		// SUM_REDUCT_CANDIDATE_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_NUMBER,
				() -> codeNumber,
				null
		);
		// SUM_REDUCT_CANDIDATE_REDUNDANT_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_CANDIDATE_HAS_REDUNDANT_NUMBER,
				() -> {
					long candidateRedundantNum =
							redMap.values().stream().mapToLong(
									map -> map.values().stream().mapToLong(
											item -> item.stream().filter(optRed -> optRed.countRedundant() > 0).count()
									).sum()
							).sum();
					return candidateRedundantNum;
				},
				null
		);
		/* ----------------------------------------------------------------------------------- */
		// MIN_CANDIDATE_ATTRIBUTE_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.MIN_CANDIDATE_ATTRIBUTE_LENGTH,
				() -> candidateAttributeLength.stream().min(Integer::compare).orElse(0),
				null
		);
		// MAX_CANDIDATE_ATTRIBUTE_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.MAX_CANDIDATE_ATTRIBUTE_LENGTH,
				() -> candidateAttributeLength.stream().max(Integer::compare).orElse(0),
				null
		);
		// AVG_CANDIDATE_ATTRIBUTE_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.AVG_CANDIDATE_ATTRIBUTE_LENGTH,
				() -> candidateAttributeLength.stream().reduce(Integer::sum).orElse(0)
						/ (double) candidateAttributeLength.size(),
				null
		);
		// SUM_CANDIDATE_ATTRIBUTE_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_CANDIDATE_ATTRIBUTE_LENGTH,
				() -> candidateAttributeLength.stream().reduce(Integer::sum).orElse(0),
				null
		);

		// MIN_REDUCT_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.MIN_REDUCT_LENGTH,
				() -> reductAttributeLength.stream().min(Integer::compare),
				null
		);
		// MAX_REDUCT_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.MAX_REDUCT_LENGTH,
				() -> reductAttributeLength.stream().min(Integer::compare),
				null
		);
		// AVG_REDUCT_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.AVG_REDUCT_LENGTH,
				() -> reductAttributeLength.stream().reduce(Integer::sum).orElse(0)
						/ (double) reductAttributeLength.size(),
				null
		);
		// SUM_REDUCT_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.SUM_REDUCT_LENGTH,
				() -> reductAttributeLength.stream().reduce(Integer::sum).orElse(null),
				null
		);
		/* ----------------------------------------------------------------------------------- */
		// REDUCT_CANDIDATE_REDUNDANT_ATTRIBUTE_PROPORTION
		//	冗余属性总数占解属性总数比例
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_REDUNDANT_ATTRIBUTE_PROPORTION,
				() -> {
					int candidateRedundantNum =
							redMap.values().stream().flatMap(
									map -> map.values().stream().flatMap(
											collection -> collection.stream().map(OptimizationReduct::countRedundant)
									)
							).mapToInt(v -> v)
									.sum();
					return candidateRedundantNum
							/ (double) candidateAttributeLength.stream().reduce(Integer::sum).orElse(1);
				},
				null
		);
		/* ----------------------------------------------------------------------------------- */
		// LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.LIST_REDUCT_CANDIDATE_CODES_WITH_REDUNDANCY,
				() -> "-",
				null
		);
		// REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODE_LENGTH_WITHOUT_REDUNDANCY,
				() -> "-",
				null
		);
		// REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Common.REDUCT_CANDIDATE_CODES_WITHOUT_REDUNDANCY,
				() -> "-",
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

	protected void loadCalculationInfo4Summary(
			PlainRecord record, FeatureImportance<?>[] calculations
	) {
		// CALCULATION_TIMES
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.CALCULATION_TIMES,
				() -> {
					int notNullCalculation = 0;
					long calTimes = 0;
					for (FeatureImportance<?> calculation : calculations) {
						if (calculation != null) {
							notNullCalculation++;
							calTimes += calculation.getCalculationTimes();
						}
					}
					return calTimes / (double) notNullCalculation;
				},
				null
		);
		// CALCULATION_ATTR_LEN
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.CALCULATION_ATTR_LEN,
				() -> {
					int notNullCalculation = 0;
					long calAttrLen = 0;
					for (FeatureImportance<?> calculation : calculations) {
						if (calculation != null) {
							notNullCalculation++;
							calAttrLen += calculation.getCalculationAttributeLength();
						}
					}
					return calAttrLen / (double) notNullCalculation;
				},
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */


}
