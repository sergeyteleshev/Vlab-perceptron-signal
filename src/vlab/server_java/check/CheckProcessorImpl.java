package vlab.server_java.check;

import org.json.JSONArray;
import org.json.JSONObject;
import rlcp.check.ConditionForChecking;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.check.PreCheckProcessor.PreCheckResult;
import rlcp.server.processor.check.PreCheckResultAwareCheckProcessor;
import vlab.server_java.Consts;

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

        JSONObject jsonCode = new JSONObject(code);
        JSONObject jsonInstructions = new JSONObject(instructions);

        JSONArray nodes = jsonCode.getJSONArray("nodes");
        JSONArray edges = jsonCode.getJSONArray("edges");

        JSONArray edgeWeight = jsonCode.getJSONArray("edgeWeight");
        JSONArray nodesLevel = jsonCode.getJSONArray("nodesLevel");
        JSONArray nodesValue = jsonCode.getJSONArray("nodesValue");
        JSONArray neuronsTableData = jsonInstructions.getJSONArray("neuronsTableData");
//        double error = jsonInstructions.getDouble("error");

        //нашли значение всех выходных сигналов нейронов
        for(int i = Consts.inputNeuronsAmount; i < nodes.length(); i++)
        {
            double currentNodeValue = 0;

            for(int j = 0; j < edges.getJSONArray(i).length(); j++)
            {
                if(edges.getJSONArray(j).getInt(i) == 1)
                {
                    currentNodeValue += nodesValue.getDouble(j) * edgeWeight.getJSONArray(j).getDouble(i);
                }
            }

            currentNodeValue = getSigmoidValue(currentNodeValue);
            currentNodeValue = (double) Math.round(currentNodeValue * 100) / 100;
            nodesValue.put(i, currentNodeValue);
        }

        for(int i = 0; i < neuronsTableData.length(); i++)
        {
            String nodeId = neuronsTableData.getJSONObject(i).getString("nodeId");
            String nodeFormula = neuronsTableData.getJSONObject(i).getString("neuronInputSignalFormula");
            double nodeOutputValue = neuronsTableData.getJSONObject(i).getDouble("neuronOutputSignalValue");
            JSONArray nodeSection = neuronsTableData.getJSONObject(i).getJSONArray("nodeSection");
        }

        return new CheckingSingleConditionResult(points, comment);
    }

    private double getSigmoidValue(double x)
    {
        return (1 / (1 + Math.exp(-x)));
    }

    private double getHiperbolicTangensValue(double x)
    {
        return (2 / (1 + Math.exp(-2 * x)));
    }

    @Override
    public void setPreCheckResult(PreCheckResult<String> preCheckResult) {}
}
