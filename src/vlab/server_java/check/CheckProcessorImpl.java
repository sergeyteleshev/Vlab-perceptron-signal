package vlab.server_java.check;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import javax.script.ScriptException;
import java.math.BigDecimal;
import java.util.*;

import static vlab.server_java.Consts.doubleToTwoDecimal;
import static vlab.server_java.Consts.outputNeuronsAmount;

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

        Object error = jsonInstructions.get("error");
        JSONArray edgeWeight = jsonCode.getJSONArray("edgeWeight");
        JSONArray nodesLevel = jsonCode.getJSONArray("nodesLevel");
        JSONArray nodesValue = jsonCode.getJSONArray("nodesValue");
        JSONArray neuronsTableData = jsonInstructions.getJSONArray("neuronsTableData");

        JSONObject serverAnswer = generateRightAnswer(nodes, edges, nodesValue, edgeWeight);
        JSONObject clientAnswer = new JSONObject(instructions);

        double checkError = doubleToTwoDecimal(countError(serverAnswer.getJSONArray("neuronOutputSignalValue").getDouble(serverAnswer.getJSONArray("neuronOutputSignalValue").length() - 1)));

        JSONArray test = new JSONArray();

        return new CheckingSingleConditionResult(points, comment);
    }

    private static JSONArray compareAnswers(JSONArray serverAnswer, JSONArray clientAnswer, double pointPercent)
    {
        double pointDelta = pointPercent / serverAnswer.length();

        JSONArray sortedServerAnswer = sortJsonArrays(serverAnswer.toString(), "nodeId");
        JSONArray sortedClientAnswer = sortJsonArrays(clientAnswer.toString(), "nodeId");
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        //todo сравнить nodeSection (отсортировать массивы строк) Arrays.sort
        JSONArray sortedServerNodeSection = getJSONArrayByKey(serverAnswer, "nodeSection");
        JSONArray sortedClientNodeSection = getJSONArrayByKey(clientAnswer, "nodeSection");

        //todo убрать?
        for(int i = 0; i < sortedServerNodeSection.length(); i++)
        {
//            sortedServerNodeSection.put(i, sortJsonArrayWithoutKey(sortedServerNodeSection.getJSONObject(i).toString()));
//            sortedClientNodeSection.put(i, sortJsonArrayWithoutKey(sortedClientNodeSection.getJSONObject(i).toString()));
        }

        for(int i = 0; i < sortedClientAnswer.length(); i++)
        {
            boolean isNeuronInputSignalValueCorrect = false;
            boolean isNeuronOutputSignalValueCorrect = false;
            boolean isNeuronInputSignalFormulaCorrect = false;

            //равны входные значения сигнала на конкретный нейрон
            if(sortedClientAnswer.getJSONObject(i).getDouble("neuronInputSignalValue") !=
                    sortedServerAnswer.getJSONObject(i).getDouble("neuronInputSignalValue"))
            {
                isNeuronInputSignalValueCorrect = true;
            }

            //равны выходные значения сигнала на конкретный нейрон
            if(sortedClientAnswer.getJSONObject(i).getDouble("neuronOutputSignalValue") ==
                    sortedServerAnswer.getJSONObject(i).getDouble("neuronOutputSignalValue"))
            {
                isNeuronOutputSignalValueCorrect = true;
            }

            //если рассчётные значения в поле инпута формулы равны
            //todo добавить сравнение значение с outputNeuronValue
            try {
                if(engine.eval(sortedClientAnswer.getJSONObject(i).getString("neuronInputSignalFormula")) ==
                        engine.eval(sortedServerAnswer.getJSONObject(i).getString("neuronInputSignalFormula")))
                {
                    isNeuronInputSignalFormulaCorrect = true;
                }
            } catch (ScriptException e) {
                e.printStackTrace();
            }



//            sortJsonArrays(sortedClientAnswer.getJSONObject("nodeSection").toString(), "ID");
        }

        return sortedClientNodeSection;
    }


    //считает только для выходного нейрона. метод подсчёта NSE
    private static double countError(double outputNeuronValue)
    {
        return Math.pow(1 - outputNeuronValue, 2) / 1;
    }

    private static JSONArray getJSONArrayByKey(JSONArray arr, String key)
    {
        JSONArray result = new JSONArray();

        for(int i = 0; i < arr.length(); i++)
        {
            result.put(i, arr.getJSONObject(i).getJSONArray(key));
        }

        return result;
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

    private static boolean compareArrays(Object[] arr1, Object[] arr2) {
        if (arr1.length != arr2.length)
        {
            return false;
        }

        Arrays.sort(arr1);
        Arrays.sort(arr2);
        return Arrays.equals(arr1, arr2);
    }

    private double getSigmoidValue(double x)
    {
        return (1 / (1 + Math.exp(-x)));
    }

    private double getHiperbolicTangensValue(double x)
    {
        return (2 / (1 + Math.exp(-2 * x)));
    }

    private static JSONArray sortJsonArrayWithoutKey(JSONArray jsonArr)
    {
        String[] stringArr = new String[jsonArr.length()];

        for(int i = 0; i < jsonArr.length(); i++)
        {
            stringArr[i] = jsonArr.getString(i);
        }

        Arrays.sort(stringArr);
        Gson gson=new GsonBuilder().create();
        String jsonArray = gson.toJson(stringArr);

        return new JSONArray(jsonArray);
    }

    //todo сделать для формул генерацию правильного ответа
    private JSONObject generateRightAnswer(JSONArray nodes, JSONArray edges, JSONArray nodesValue, JSONArray edgeWeight)
    {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");
        double error = 0;

        JSONArray jsonNeuronInputSignalValue = new JSONArray();
        JSONArray jsonNeuronOutputSignalValue = new JSONArray();
        JSONArray jsonNodeId = new JSONArray();
        JSONArray jsonNeuronInputSignalFormula = new JSONArray();
        JSONArray jsonNodeSection = new JSONArray();
        JSONArray jsonCountedFormula = new JSONArray();
        JSONObject serverAnswer = new JSONObject();

        //нашли значение всех выходных сигналов нейронов
        for(int i = Consts.inputNeuronsAmount; i < nodes.length(); i++)
        {
            double currentNodeValue = 0;
            String nodeId = "n" + Integer.toString(i);
            StringBuilder nodeFormula = new StringBuilder();
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

            jsonNeuronInputSignalValue.put(i, doubleToTwoDecimal(currentNodeValue));

            currentNodeValue = getSigmoidValue(currentNodeValue);
            currentNodeValue = doubleToTwoDecimal(currentNodeValue);
            nodesValue.put(i, currentNodeValue);

            jsonNeuronOutputSignalValue.put(i, currentNodeValue);
            jsonNodeId.put(i, nodeId);
            jsonNeuronInputSignalFormula.put(i, nodeFormula.toString());
            jsonNodeSection.put(i, nodeSection);

            Object countedFormula = null;

            try {
                countedFormula = engine.eval(nodeFormula.toString());
                countedFormula = doubleToTwoDecimal(new Double(countedFormula.toString()));

            } catch (ScriptException e) {
                e.printStackTrace();
            }

            jsonCountedFormula.put(i, countedFormula);
        }

        //избавляемся от ненужных null для входных нейронов, которые уже изначально даны по условию, чтобы у нас был одинаково похожий объект с clientData
        for(int i = 0; i < Consts.inputNeuronsAmount; i++)
        {
            jsonNeuronInputSignalValue.remove(0);
            jsonNeuronOutputSignalValue.remove(0);
            jsonNodeId.remove(0);
            jsonNeuronInputSignalFormula.remove(0);
            jsonNodeSection.remove(0);
            jsonCountedFormula.remove(0);
        }

        serverAnswer.put("neuronInputSignalValue", jsonNeuronInputSignalValue);
        serverAnswer.put("neuronOutputSignalValue", jsonNeuronOutputSignalValue);
        serverAnswer.put("nodeId", jsonNodeId);
        serverAnswer.put("neuronInputSignalFormula", jsonNeuronInputSignalFormula);
        serverAnswer.put("nodeSection", jsonNodeSection);
        serverAnswer.put("countedFormula", jsonCountedFormula);

        return serverAnswer;
    }

    @Override
    public void setPreCheckResult(PreCheckResult<String> preCheckResult) {}
}
