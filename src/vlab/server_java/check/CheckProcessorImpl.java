package vlab.server_java.check;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rlcp.check.ConditionForChecking;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.check.PreCheckProcessor.PreCheckResult;
import rlcp.server.processor.check.PreCheckResultAwareCheckProcessor;
import vlab.server_java.Consts;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.math.BigDecimal;
import java.util.*;

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

//        double error = jsonInstructions.getDouble("error");
        JSONArray edgeWeight = jsonCode.getJSONArray("edgeWeight");
        JSONArray nodesLevel = jsonCode.getJSONArray("nodesLevel");
        JSONArray nodesValue = jsonCode.getJSONArray("nodesValue");
        JSONArray neuronsTableData = jsonInstructions.getJSONArray("neuronsTableData");
        JSONArray checkNeuronsTableData = generateRightAnswer(nodes, edges, nodesValue, edgeWeight);

        JSONArray serverAnswer = generateRightAnswer(nodes, edges, nodesValue, edgeWeight);


        return new CheckingSingleConditionResult(points, serverAnswer.toString());
    }


    //считает только для выходного нейрона. метод подсчёта NSE
    private static double countError(double outputNeuronValue)
    {
        return Math.pow(1 - outputNeuronValue, 2) / 1;
    }

    private static JSONArray sortJsonArrays(String jsonArrStr, String KEY_NAME)
    {
        JSONArray jsonArr = new JSONArray(jsonArrStr);
        JSONArray sortedJsonArray = new JSONArray();

        List<JSONObject> jsonValues = new ArrayList<JSONObject>();
        for (int i = 0; i < jsonArr.length(); i++) {
            jsonValues.add(jsonArr.getJSONObject(i));
        }
        Collections.sort( jsonValues, new Comparator<JSONObject>() {
            //You can change "Name" with "ID" if you want to sort by ID

            @Override
            public int compare(JSONObject a, JSONObject b) {
                String valA = new String();
                String valB = new String();

                try {
                    valA = (String) a.get(KEY_NAME);
                    valB = (String) b.get(KEY_NAME);
                }
                catch (JSONException e) {
                    //do something
                }

                return valA.compareTo(valB);
                //if you want to change the sort order, simply use the following:
                //return -valA.compareTo(valB);
            }
        });

        for (int i = 0; i < jsonArr.length(); i++) {
            sortedJsonArray.put(jsonValues.get(i));
        }

        return sortedJsonArray;
    }

    private static boolean compareArrays(Integer[] arr1, Integer[] arr2) {
        if (arr1.length != arr2.length)
        {
            return false;
        }

        Arrays.sort(arr1);
        Arrays.sort(arr2);
        JSONArray test = new JSONArray();
        return Arrays.equals(arr1, arr2);
    }

    private boolean compareNumberJsonArray(JSONArray serverData, JSONArray clientData, String comparingField)
    {
        int amountOfMistakes = 0;
        StringBuilder comment = new StringBuilder();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        JSONArray sortedServerData = new JSONArray();
        JSONArray sortedClientData = new JSONArray();

        for(int i = 0; i < serverData.length(); i++)
        {
            String serverFormula = serverData.getJSONObject(i).getString(comparingField);
            String clientFormula = clientData.getJSONObject(i).getString(comparingField);

            sortedClientData.put(i, new JSONObject().put(comparingField, clientFormula));
            sortedServerData.put(i, new JSONObject().put(comparingField, sortedServerData));
        }

        sortedClientData = sortJsonArrays(sortedClientData.toString(), comparingField);
        sortedServerData = sortJsonArrays(sortedServerData.toString(), comparingField);

        return sortedClientData.toString().equals(sortedServerData.toString());
    }

    private double getSigmoidValue(double x)
    {
        return (1 / (1 + Math.exp(-x)));
    }

    private double getHiperbolicTangensValue(double x)
    {
        return (2 / (1 + Math.exp(-2 * x)));
    }

    //todo сделать для формул генерацию правильного ответа
    private JSONArray generateRightAnswer(JSONArray nodes, JSONArray edges, JSONArray nodesValue, JSONArray edgeWeight)
    {

        JSONArray checkNeuronsTableData = new JSONArray();
//        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        //нашли значение всех выходных сигналов нейронов
        for(int i = Consts.inputNeuronsAmount; i < nodes.length(); i++)
        {
            double currentNodeValue = 0;
            String nodeId = "n" + Integer.toString(i);
            StringBuilder nodeFormula = new StringBuilder();
            double nodeValue;
            JSONObject checkNeuronsTableDataRow = new JSONObject();
            JSONArray nodeSection = new JSONArray();

            for(int j = 0; j < edges.getJSONArray(i).length(); j++)
            {
                if(edges.getJSONArray(j).getInt(i) == 1)
                {
                    if(nodeFormula.length() == 0)
                    {
                        nodeFormula.append(Double.toString(nodesValue.getDouble(j))).append("*").append(Double.toString(edgeWeight.getJSONArray(j).getDouble(i)));
                    }
                    else
                    {
                        nodeFormula.append("+").append(Double.toString(nodesValue.getDouble(j))).append("*").append(Double.toString(edgeWeight.getJSONArray(j).getDouble(i)));
                    }

                    nodeSection.put(nodeSection.length(), "n" + Integer.toString(j));
                    currentNodeValue += nodesValue.getDouble(j) * edgeWeight.getJSONArray(j).getDouble(i);
                }
            }

            checkNeuronsTableDataRow.put("neuronInputSignalValue", (double) Math.round(currentNodeValue*100) / 100);

            currentNodeValue = getSigmoidValue(currentNodeValue);
            currentNodeValue = (double) Math.round(currentNodeValue * 100) / 100;
            nodesValue.put(i, currentNodeValue);

            checkNeuronsTableDataRow.put("neuronOutputSignalValue", currentNodeValue);
            checkNeuronsTableDataRow.put("nodeId", nodeId);
            checkNeuronsTableDataRow.put("neuronInputSignalFormula", nodeFormula.toString());
            checkNeuronsTableDataRow.put("nodeSection", nodeSection);

//            try {
//                checkNeuronsTableDataRow.put("test", engine.eval(nodeFormula.toString()));
//            } catch (ScriptException e) {
//                e.printStackTrace();
//            }

            checkNeuronsTableData.put(checkNeuronsTableData.length(), checkNeuronsTableDataRow);
        }

        return checkNeuronsTableData;
    }

    @Override
    public void setPreCheckResult(PreCheckResult<String> preCheckResult) {}
}
