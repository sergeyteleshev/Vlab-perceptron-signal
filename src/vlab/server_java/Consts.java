package vlab.server_java;

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
    public static final double countedFormulaEpsilon = 0.05;
    public static final double meanSquaredErrorEpsilon = 0.05;

    public static double doubleToTwoDecimal(double number)
    {
        return (double) Math.round(number * 100)  / 100;
    }

    public static int generateRandomIntRange(int min, int max)
    {
        return ThreadLocalRandom.current().nextInt(min, max);
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
}
