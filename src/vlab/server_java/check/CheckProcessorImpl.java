package vlab.server_java.check;

import org.json.JSONArray;
import org.json.JSONObject;
import rlcp.check.ConditionForChecking;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.check.PreCheckProcessor.PreCheckResult;
import rlcp.server.processor.check.PreCheckResultAwareCheckProcessor;

import java.math.BigDecimal;
import java.util.Arrays;

/**
 * Simple CheckProcessor implementation. Supposed to be changed as needed to provide
 * necessary Check method support.
 */
public class CheckProcessorImpl implements PreCheckResultAwareCheckProcessor<String> {
    @Override
    public CheckingSingleConditionResult checkSingleCondition(ConditionForChecking condition, String instructions, GeneratingResult generatingResult) throws Exception {
        //do check logic here
        BigDecimal points = BigDecimal.valueOf(1.0);
        String comment = "test";

        String code = generatingResult.getCode();
        String generated = generatingResult.getCode();

        JSONObject jsonCode = new JSONObject(code);
        JSONObject jsonGenerate = new JSONObject(generated);

        JSONArray nodes = jsonGenerate.getJSONArray("nodes");
        JSONArray edges = jsonGenerate.getJSONArray("edges");

        JSONArray edgeWeight = jsonGenerate.getJSONArray("edgeWeight");
        JSONArray nodesLevel = jsonCode.getJSONArray("nodesLevel");
        JSONArray nodesValue = jsonCode.getJSONArray("nodesValue");
//        JSONArray neuronsTableData = jsonCode.getJSONArray("neuronsTableData");

        return new CheckingSingleConditionResult(points, comment);
    }

    @Override
    public void setPreCheckResult(PreCheckResult<String> preCheckResult) {}
}
