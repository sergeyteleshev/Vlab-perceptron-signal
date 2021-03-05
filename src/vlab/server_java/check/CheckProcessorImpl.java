package vlab.server_java.check;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import rlcp.check.ConditionForChecking;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.check.PreCheckProcessor.PreCheckResult;
import rlcp.server.processor.check.PreCheckResultAwareCheckProcessor;
import vlab.server_java.Consts;
import vlab.server_java.generate.GenerateProcessorImpl;

import java.math.BigDecimal;
import java.util.*;

import static vlab.server_java.Consts.*;
import static vlab.server_java.generate.GenerateProcessorImpl. getInputSignalWithNewEdges;
import static vlab.server_java.generate.GenerateProcessorImpl.getSignalWithNewEdges;

/**
 * Simple CheckProcessor implementation. Supposed to be changed as needed to provide
 * necessary Check method support.
 */

public class CheckProcessorImpl implements PreCheckResultAwareCheckProcessor<String> {
    @Override
    public CheckingSingleConditionResult checkSingleCondition(ConditionForChecking condition, String instructions, GeneratingResult generatingResult) throws Exception {
        //do check logic here

        double points = 0;
        String comment = "";

        try
        {
            String code = generatingResult.getCode();

            JSONObject jsonCode = new JSONObject(code);
            JSONObject jsonInstructions = new JSONObject(instructions);

            JSONArray edgeWeight = jsonCode.getJSONArray("edgeWeight");
            JSONArray edges = jsonCode.getJSONArray("edges");
            JSONArray nodes = jsonCode.getJSONArray("nodes");
            JSONArray nodesValue = jsonCode.getJSONArray("nodesValue");
            String activationFunction = jsonCode.getString("currentActivationFunction");

            double error = jsonInstructions.getDouble("error");
            JSONObject serverAnswerObject = generateRightAnswer(nodes, edges, nodesValue, edgeWeight, activationFunction);
            JSONArray serverAnswer = jsonObjectToJsonArray(serverAnswerObject);
            JSONArray clientAnswer = jsonInstructions.getJSONArray("neuronsTableData");

            double checkError = doubleToTwoDecimal(countMSE(serverAnswer));

            JSONObject compareResult = compareAnswers(serverAnswer, clientAnswer, Consts.tablePoints);

            double comparePoints = compareResult.getDouble("points");

            String compareComment = compareResult.getString("comment");
            comment += compareComment;

            points += comparePoints;

            if(checkError - meanSquaredErrorEpsilon <= error && checkError + meanSquaredErrorEpsilon >= error)
            {
                points += Consts.errorPoints;
            }
            else
                comment += "Неверно посчитано MSE. MSE = " + checkError;

            points = doubleToTwoDecimal(points);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new CheckingSingleConditionResult(BigDecimal.valueOf(points), comment);
    }

    private static JSONArray jsonObjectToJsonArray(JSONObject jsonObject)
    {
        JSONArray result = new JSONArray();
        Iterator x = jsonObject.keys();
        int arraySize = 0;
        String[] keys = new String[jsonObject.length()];
        int j = 0;

        if(x.hasNext())
        {
            keys[j++] = (String) x.next();
            arraySize = jsonObject.getJSONArray(keys[0]).length();
        }
        else
            return result;

        while (x.hasNext()) {
            String key = (String) x.next();
            keys[j++] = key;
        }

        for(int i = 0; i < arraySize; i++) {
            JSONObject currentJsonObject = new JSONObject();

            for(int m = 0; m < keys.length; m++)
            {
                currentJsonObject.put(keys[m], jsonObject.getJSONArray(keys[m]).get(i));
            }

            result.put(i, currentJsonObject);
        }

        return result;
    }

    private static JSONObject compareAnswers(JSONArray serverAnswer, JSONArray clientAnswer, double pointPercent)
    {
        double pointDelta = pointPercent / serverAnswer.length();
        double points = 0;
        JSONObject result = new JSONObject();
        StringBuilder comment = new StringBuilder();

        JSONArray sortedServerAnswer = sortJsonArrays(serverAnswer.toString(), "nodeId");
        JSONArray sortedClientAnswer = sortJsonArrays(clientAnswer.toString(), "nodeId");

        for(int i = 0; i < sortedClientAnswer.length(); i++)
        {
            boolean isNeuronInputSignalValueCorrect = false;
            boolean isNeuronOutputSignalValueCorrect = false;
            boolean isNeuronNodeSectionCorrect = false;

            //равны входные значения сигнала на конкретный нейрон в рамках окрестности
            if(sortedClientAnswer.getJSONObject(i).getDouble("neuronInputSignalValue") >= sortedServerAnswer.getJSONObject(i).getDouble("neuronInputSignalValue") - neuronInputSignalValueEpsilon
                    &&
                sortedClientAnswer.getJSONObject(i).getDouble("neuronInputSignalValue") <= sortedServerAnswer.getJSONObject(i).getDouble("neuronInputSignalValue") + neuronInputSignalValueEpsilon
            )
            {
                isNeuronInputSignalValueCorrect = true;
            }
            else
            {
                comment.append("Неверное значение входного сигнала нейрона ").append(sortedClientAnswer.getJSONObject(i).getString("nodeId")).append(". ");
            }

            //равны выходные значения сигнала на конкретный нейрон в рамках окрестности
            if(sortedClientAnswer.getJSONObject(i).getDouble("neuronOutputSignalValue") >=
                    sortedServerAnswer.getJSONObject(i).getDouble("neuronOutputSignalValue") - neuronOutputSignalValueEpsilon
                    &&
                    sortedClientAnswer.getJSONObject(i).getDouble("neuronOutputSignalValue") <= sortedServerAnswer.getJSONObject(i).getDouble("neuronOutputSignalValue") + neuronOutputSignalValueEpsilon
            )
            {
                isNeuronOutputSignalValueCorrect = true;
            }
            else
            {
                comment.append("Неверное значение выходного сигнала нейрона ").append(sortedClientAnswer.getJSONObject(i).getString("nodeId")).append(". ");
            }

            //если правильно в графе выделил нейроны, из которых сигнал течёт в текущий нейрон по таблице
            if(compareArrays(sortedClientAnswer.getJSONObject(i).getJSONArray("nodeSection"), sortedServerAnswer.getJSONObject(i).getJSONArray("nodeSection")))
            {
                isNeuronNodeSectionCorrect = true;
            }
            else
            {
                comment.append("Неверно выделены нейроны из которых течёт сигнал в нейрон ").append(sortedClientAnswer.getJSONObject(i).getString("nodeId")).append(". ");
            }

            if(isNeuronInputSignalValueCorrect)
                points += pointDelta / 3;

            if(isNeuronOutputSignalValueCorrect)
                points += pointDelta / 3;

            if(isNeuronNodeSectionCorrect)
                points += pointDelta / 3;
        }

        int rowsDiff = serverAnswer.length() - clientAnswer.length();
        if(rowsDiff > 0)
        {
            comment.append("В таблице не хватает ").append(String.valueOf(rowsDiff)).append(" строк. ");
        }

        result.put("points", points);
        result.put("comment", comment.toString());

        return result;
    }

    //по дефолту у нас верные значение выходных нейронов это всегда единицы (могут быть и другие на самом деле)
    private static double countMSE(JSONArray serverAnswer)
    {
        double sum = 0;
        double mse;
        int[] idealOutputNeuronValues = new int[outputNeuronsAmount];

        for (int i = 1; i < outputNeuronsAmount + 1; i++)
        {
            double currentOutputNeuronValue = serverAnswer.getJSONObject(serverAnswer.length() - i).getDouble("neuronOutputSignalValue");
            if(currentOutputNeuronValue > classBorderline)
                idealOutputNeuronValues[i - 1] = 1;
            else
                idealOutputNeuronValues[i - 1] = 0;
        }

        for (int i = 1; i < outputNeuronsAmount + 1; i++)
        {
            double currentOutputNeuronValue = serverAnswer.getJSONObject(serverAnswer.length() - i).getDouble("neuronOutputSignalValue");
            sum += Math.pow((idealOutputNeuronValues[i - 1] - currentOutputNeuronValue), 2);
        }

        mse = sum / outputNeuronsAmount;

        return mse;
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

    private static boolean compareArrays(JSONArray arr1, JSONArray arr2) {
        Object[] normalArr1 = new Object[arr1.length()];
        Object[] normalArr2 = new Object[arr2.length()];

        for(int i = 0; i < arr1.length(); i++)
        {
            normalArr1[i] = arr1.get(i);
        }

        for(int i = 0; i < arr2.length(); i++)
        {
            normalArr2[i] = arr2.get(i);
        }

        Arrays.sort(normalArr1);
        Arrays.sort(normalArr2);
        return Arrays.equals(normalArr1, normalArr2);
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

    public JSONObject generateRightAnswer(JSONArray nodes, JSONArray edges, JSONArray nodesValue, JSONArray edgeWeight, String activationFunction)
    {
        JSONArray jsonNodeId = new JSONArray();
        JSONArray jsonNodeSection = new JSONArray();
        JSONObject serverAnswer = new JSONObject();

        double[] newNodesValues = getSignalWithNewEdges(jsonArrayToInt(nodes), twoDimensionalJsonArrayToInt(edges), twoDimensionalJsonArrayToDouble(edgeWeight), jsonArrayToDouble(nodesValue), activationFunction);
        double[] inputSignals = getInputSignalWithNewEdges(jsonArrayToInt(nodes), twoDimensionalJsonArrayToInt(edges), twoDimensionalJsonArrayToDouble(edgeWeight), jsonArrayToDouble(nodesValue), activationFunction);

        //нашли значение всех выходных сигналов нейронов
        for(int i = 0; i < nodes.length(); i++)
        {
            String nodeId = "n" + Integer.toString(i);
            JSONArray nodeSection = new JSONArray();

            for(int j = 0; j < edges.getJSONArray(i).length(); j++)
            {
                if(edges.getJSONArray(j).getInt(i) == 1)
                {
                    nodeSection.put(nodeSection.length(), "n" + Integer.toString(j));
                }
            }

            jsonNodeId.put(i, nodeId);
            jsonNodeSection.put(i, nodeSection);
        }

        serverAnswer.put("neuronInputSignalValue", new JSONArray(inputSignals));
        serverAnswer.put("neuronOutputSignalValue", new JSONArray(newNodesValues));
        serverAnswer.put("nodeId", jsonNodeId);
        serverAnswer.put("nodeSection", jsonNodeSection);

        return serverAnswer;
    }

    @Override
    public void setPreCheckResult(PreCheckResult<String> preCheckResult) {}
}
