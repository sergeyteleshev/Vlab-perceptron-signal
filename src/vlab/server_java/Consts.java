package vlab.server_java;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.ThreadLocalRandom;

public class Consts {
    public static final int minInputNeuronValue = 0;
    public static final int maxInputNeuronValue = 1;
    public static final double minEdgeValue = 0;
    public static final double maxEdgeValue = 1;
    public static final int inputNeuronsAmount = 2;
    public static final int outputNeuronsAmount = 2;
    public static final int amountOfHiddenLayers = 1;
    public static final int amountOfNodesInHiddenLayer = 2;
    public static final double errorPoints = 0.1;
    public static final double tablePoints = 0.9;
    public static final double neuronOutputSignalValueEpsilon = 0.05;
    public static final double neuronInputSignalValueEpsilon = 0.05;
    public static final String[] activationFunctions = {"сигмовидная", "линейная", "гиперболический тангенс"};
    public static final double meanSquaredErrorEpsilon = 0.05;
    public static final char[] charsForEquations = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};

    public static double doubleToTwoDecimal(double number)
    {
        return (double) Math.round(number * 100)  / 100;
    }

    public static int generateRandomIntRange(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double generateRandomDoubleRange(double min, double max)
    {
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static double roundDoubleToNDecimals(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    private static double[][] convertJSONArrayToTwoDimensionalDoubleArray(JSONArray arr)
    {
        double[][] newArr = new double[arr.length()][arr.length()];

        for (int i = 0; i < arr.length(); i++)
        {
            for(int j = 0; j < arr.getJSONArray(i).length(); j++)
            {
                newArr[i][j] = arr.getJSONArray(i).getDouble(j);
            }
        }

        return newArr;
    }

    public static double[][] twoDimensionalJsonArrayToDouble(JSONArray arr)
    {
        double[][] result = new double[arr.length()][arr.getJSONArray(0).length()];

        for(int i = 0; i < arr.length(); i++)
        {
            for(int j = 0; j < arr.getJSONArray(i).length(); j++)
            {
                result[i][j] = arr.getJSONArray(i).getDouble(j);
            }
        }

        return result;
    }

    public static double[] jsonArrayToDouble(JSONArray arr)
    {
        double[] result = new double[arr.length()];

        for(int i = 0; i < arr.length(); i++)
        {
            result[i] = arr.getDouble(i);
        }

        return result;
    }
}
