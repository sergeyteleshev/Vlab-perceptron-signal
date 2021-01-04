package vlab.server_java.check;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
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

import static vlab.server_java.Consts.*;

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

        String code = generatingResult.getCode();

        JSONObject jsonCode = new JSONObject(code);
        JSONObject jsonInstructions = new JSONObject(instructions);

        JSONArray nodes = jsonCode.getJSONArray("nodes");
        JSONArray edges = jsonCode.getJSONArray("edges");

        double error = jsonInstructions.getDouble("error");
        JSONArray edgeWeight = jsonCode.getJSONArray("edgeWeight");
        JSONArray nodesValue = jsonCode.getJSONArray("nodesValue");

        JSONArray serverAnswer = jsonObjectToJsonArray(generateRightAnswer(nodes, edges, nodesValue, edgeWeight));
        JSONArray clientAnswer = jsonInstructions.getJSONArray("neuronsTableData");

        double checkError = countMSE(serverAnswer);
        checkError = (double) Math.round(checkError * 100) / 100;

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
            comment += "Неверно посчитано MSE. MSE = " + Double.toString(checkError);

        points = doubleToTwoDecimal(points);

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

            for(int m = 0; m < keys.length - 1; m++)
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
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("JavaScript");

        for(int i = 0; i < sortedClientAnswer.length(); i++)
        {
            boolean isNeuronInputSignalValueCorrect = false;
            boolean isNeuronOutputSignalValueCorrect = false;
            boolean isNeuronInputSignalFormulaCorrect = false;
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

            //если рассчётные значения в поле инпута формулы равны
            try {
                double clientCountedFormula = new Double(engine.eval(sortedClientAnswer.getJSONObject(i).getString("neuronInputSignalFormula")).toString());
                clientCountedFormula = (double) Math.round(clientCountedFormula * 100) / 100;
                if(clientCountedFormula >= sortedServerAnswer.getJSONObject(i).getDouble("countedFormula") - countedFormulaEpsilon
                    &&
                    clientCountedFormula <= sortedServerAnswer.getJSONObject(i).getDouble("countedFormula") + countedFormulaEpsilon
                )
                {
                    isNeuronInputSignalFormulaCorrect = true;
                }
                else
                {
                    comment.append(" Неверная формула входного сигнала нейрона ").append(sortedClientAnswer.getJSONObject(i).getString("nodeId")).append(". ");
                }
            } catch (ScriptException e) {
                comment.append(" Некорректная формула расчёта входного сигнала нейрона ").append(sortedClientAnswer.getJSONObject(i).getString("nodeId")).append(". ");;
                continue;
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

            if(isNeuronInputSignalFormulaCorrect)
                points += pointDelta / 4;

            if(isNeuronInputSignalValueCorrect)
                points += pointDelta / 4;

            if(isNeuronOutputSignalValueCorrect)
                points += pointDelta / 4;

            if(isNeuronNodeSectionCorrect)
                points += pointDelta / 4;
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

        for (int i = 1; i < outputNeuronsAmount + 1; i++)
        {
            double currentOutputNeuronValue = serverAnswer.getJSONObject(serverAnswer.length() - i).getDouble("neuronOutputSignalValue");
            sum += Math.pow((1 - currentOutputNeuronValue), 2);
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

    public JSONObject generateRightAnswer(JSONArray nodes, JSONArray edges, JSONArray nodesValue, JSONArray edgeWeight)
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
