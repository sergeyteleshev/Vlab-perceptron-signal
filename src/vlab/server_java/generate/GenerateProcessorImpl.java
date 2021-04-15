package vlab.server_java.generate;

import org.json.JSONObject;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.generate.GenerateProcessor;
import vlab.server_java.Consts;

import java.util.ArrayList;

import static vlab.server_java.Consts.*;
import static vlab.server_java.Consts.inputNeuronsAmount;

/**
 * Simple GenerateProcessor implementation. Supposed to be changed as needed to
 * provide necessary Generate method support.
 */
public class GenerateProcessorImpl implements GenerateProcessor {
    @Override
    public GeneratingResult generate(String condition) {
        //do Generate logic here
        StringBuilder text = new StringBuilder();
        String code = "";
        String instructions = "";
        try
        {
            JSONObject graph = new JSONObject();
            double[] inputNeuronsValues = new double[inputNeuronsAmount];

            int inputNeuronsAmount = Consts.inputNeuronsAmount;
            int outputNeuronsAmount = Consts.outputNeuronsAmount;

            int amountOfHiddenLayers = Consts.amountOfHiddenLayers;
            int amountOfNodesInHiddenLayer = Consts.amountOfNodesInHiddenLayer;
            int[] hiddenLayerNodesAmount = new int[amountOfHiddenLayers];

            final String[] activationFunctions = Consts.activationFunctions;
            int currentActivationFunctionIndex = generateRandomIntRange(0, activationFunctions.length - 1);
            String currentActivationFunction = activationFunctions[currentActivationFunctionIndex];
            JSONObject randomGraph = generateVariant(currentActivationFunction);

            double[][] edgeWeight = (double[][]) randomGraph.get("edgeWeight");
            int[][] edges = (int[][]) randomGraph.get("edges");
            int[] nodesLevel = (int[]) randomGraph.get("nodesLevel");
            int[] nodes = (int[]) randomGraph.get("nodes");
            double[] nodesValue = (double[]) randomGraph.get("nodesValue");

            for (int i = 0; i < inputNeuronsAmount; i++)
            {
                text.append("input(X").append(i).append(") = ").append(nodesValue[i]).append(". ");
            }

            graph.put("edgeWeight", edgeWeight);
            graph.put("nodes", nodes);
            graph.put("nodesLevel", nodesLevel);
            graph.put("nodesValue", nodesValue);
            graph.put("edges", edges);
            graph.put("hiddenNodesLeft", hiddenLayerNodesAmount);
            graph.put("inputNeuronsAmount", inputNeuronsAmount);
            graph.put("outputNeuronsAmount", outputNeuronsAmount);
            graph.put("amountOfHiddenLayers", amountOfHiddenLayers);
            graph.put("amountOfNodesInHiddenLayer", amountOfNodesInHiddenLayer);
            graph.put("inputNeuronsValues", inputNeuronsValues);
            graph.put("currentActivationFunction", currentActivationFunction);

            text.append("Фунция активации – ").append(currentActivationFunction).append(".");

            code = graph.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return new GeneratingResult(text.toString(), code, instructions);
    }

    public JSONObject generateRandomGraph()
    {
        int nodesAmount = inputNeuronsAmount + outputNeuronsAmount + amountOfNodesInHiddenLayer * amountOfHiddenLayers; //всего вершин в графе
        double[][] edgeWeight = new double[nodesAmount][nodesAmount];
        int currentHiddenLayer = 2;
        int[] nodesLevel = new int[nodesAmount];
        JSONObject result = new JSONObject();
        int[][] edges = new int[nodesAmount][nodesAmount];
        int[] nodes = new int[nodesAmount];
        double[] nodesValue = new double[nodesAmount];

        for(int i = 0; i < nodesValue.length; i++)
        {
            if(i < inputNeuronsAmount)
            {
                nodesValue[i] = roundDoubleToNDecimals(generateRandomDoubleRange(minInputNeuronValue, maxInputNeuronValue), roundNodesValueSign);
            }
        }

        for(int i = 0; i < nodesAmount; i++)
        {
            nodes[i] = i;
        }

        //начальные значения для рецепторов
        for(int i = 0; i < inputNeuronsAmount; i++)
        {
            nodesLevel[i] = 1;
        }

        for(int i = 1; i <= outputNeuronsAmount; i++)
        {
            nodesLevel[nodesLevel.length - i] = 1 + amountOfHiddenLayers + 1;
        }

        //уровни словёв
        int countTemp = 0;
        int amountOfNodesBeforeOutputNeurons = inputNeuronsAmount + amountOfNodesInHiddenLayer * amountOfHiddenLayers;
        for(int i = inputNeuronsAmount; i < amountOfNodesBeforeOutputNeurons; i++)
        {
            nodesLevel[i] = currentHiddenLayer;
            countTemp++;
            if(countTemp % amountOfNodesInHiddenLayer == 0 && countTemp != 0)
            {
                currentHiddenLayer++;
            }
        }

        for(int i = 0; i < nodesAmount; i++)
        {
            int currentNodeLevel = nodesLevel[i];

            for(int j = 0; j < nodesLevel.length; j++)
            {
                if(nodesLevel[j] == currentNodeLevel + 1)
                {
                    edges[i][j] = 1;
                    // от -1 до 1 с двумя знаками после запятой
                    edgeWeight[i][j] = (double) roundDoubleToNDecimals(generateRandomDoubleRange(minEdgeValue, maxEdgeValue), roundEdgeWeightSign);
                }
                else
                {
                    edges[i][j] = 0;
                    edgeWeight[i][j] = 0;
                }
            }
        }

        result.put("nodesLevel", nodesLevel);
        result.put("edgeWeight", edgeWeight);
        result.put("edges", edges);
        result.put("nodes", nodes);
        result.put("nodesValue", nodesValue);

        return result;
    }

    double[] generateNeuronIdealOutputSignalValues(String activationFunctionType)
    {
        double[] idealOutputSignalValues = new double[outputNeuronsAmount];
        int amountOfZerosInOutputNeurons = outputNeuronsAmount;

        if(activationFunctionType.equals(sigmoidFunction))
        {
            for(int i = 0; i < outputNeuronsAmount; i++)
            {
                idealOutputSignalValues[i] = generateRandomIntRange(0, 1);
                if(idealOutputSignalValues[i] != 0)
                {
                    amountOfZerosInOutputNeurons--;
                }
            }

            while (amountOfZerosInOutputNeurons == outputNeuronsAmount)
            {
                for(int i = 0; i < outputNeuronsAmount; i++)
                {
                    idealOutputSignalValues[i] = generateRandomIntRange(0, 1);
                    if(idealOutputSignalValues[i] != 0)
                    {
                        amountOfZerosInOutputNeurons--;
                    }
                }
            }
        }

        return idealOutputSignalValues;
    }

    public JSONObject generateVariant(String activationFunction)
    {
        JSONObject randomGraph = new JSONObject();
        int nodesAmountWithoutOutput = inputNeuronsAmount + amountOfHiddenLayers * amountOfNodesInHiddenLayer;
        final int amountOfZeroClassOutputNeurons = generateRandomIntRange(1, outputNeuronsAmount);
        int currentAmountOfZeroClassOutputNeurons = 0;

        while(currentAmountOfZeroClassOutputNeurons != amountOfZeroClassOutputNeurons)
        {
            randomGraph = generateRandomGraph();
            currentAmountOfZeroClassOutputNeurons = 0;
            double[][] edgeWeight = (double[][]) randomGraph.get("edgeWeight");
            int[][] edges = (int[][]) randomGraph.get("edges");
            int[] nodes = (int[]) randomGraph.get("nodes");
            double[] nodesValue = (double[]) randomGraph.get("nodesValue");
            double[] currentNodesValue;

            currentNodesValue = getSignalWithNewEdges(nodes, edges, edgeWeight, nodesValue, activationFunction);
            for(int i = nodesAmountWithoutOutput; i < nodesValue.length; i++)
            {
                if(currentNodesValue[i] <= classBorderline)
                    currentAmountOfZeroClassOutputNeurons++;
            }
        }

        return randomGraph;
    }

    private static ArrayList<Integer> findEdgesToNeuron(double[][] edges, int neuronIndex)
    {
        ArrayList<Integer> result = new ArrayList<>();

        for(int i = 0; i < edges.length; i++)
        {
            if(edges[neuronIndex][i] != 0)
            {
                result.add(i);
            }
        }

        return result;
    }

    public static double[][] backpropagation(double[] neuronOutputSignalValue, double[] idealNeuronOutputSignalValue, double[][] edgesWeight)
    {
        JSONObject result = new JSONObject();
        double[] delta = new double[neuronOutputSignalValue.length];
        double[][] grad = new double[edgesWeight.length][edgesWeight.length];
        double[][] deltaW = new double[edgesWeight.length][edgesWeight.length];
        double E = Consts.E;
        double A = Consts.A;

        double[] idealNeuronOutputSignalValueFull = new double[edgesWeight.length];

        for (int i = 0; i < idealNeuronOutputSignalValue.length; i++)
        {
            idealNeuronOutputSignalValueFull[idealNeuronOutputSignalValueFull.length - 1 - i] = idealNeuronOutputSignalValue[idealNeuronOutputSignalValue.length - 1 - i];
        }

        for(int i = neuronOutputSignalValue.length - 1; i >= 0; i--)
        {
            ArrayList<Integer> connectedNeurons = findEdgesToNeuron(edgesWeight, i);

            //esli eto posledniy sloy neyronov, to odna formula
            if(i + Consts.outputNeuronsAmount > neuronOutputSignalValue.length - 1)
            {
                delta[i] = (idealNeuronOutputSignalValueFull[i] - neuronOutputSignalValue[i]) * ((idealNeuronOutputSignalValueFull[i] - neuronOutputSignalValue[i]) * neuronOutputSignalValue[i]);
            }
            else if (i < Consts.inputNeuronsAmount)
            {
                for(int j = 0; j < connectedNeurons.size(); j++)
                {
                    grad[i][connectedNeurons.get(j)] = neuronOutputSignalValue[i] * delta[connectedNeurons.get(j)];
                    deltaW[i][connectedNeurons.get(j)] = E * grad[i][connectedNeurons.get(j)];
                    edgesWeight[i][connectedNeurons.get(j)] += deltaW[i][connectedNeurons.get(j)];
                }
            }
            else
            {
                double deltaSumEdgeMultipliedToDelta = 0;
                for(int j = 0; j < connectedNeurons.size(); j++)
                {
                    deltaSumEdgeMultipliedToDelta += edgesWeight[i][connectedNeurons.get(j)] * delta[connectedNeurons.get(j)];
                }

                for(int j = 0; j < connectedNeurons.size(); j++)
                {
                    grad[i][connectedNeurons.get(j)] = neuronOutputSignalValue[i] * delta[connectedNeurons.get(j)];
                    deltaW[i][connectedNeurons.get(j)] = E * grad[i][connectedNeurons.get(j)];
                    edgesWeight[i][connectedNeurons.get(j)] += deltaW[i][connectedNeurons.get(j)];
                }

                delta[i] = ((1 - neuronOutputSignalValue[i]) * neuronOutputSignalValue[i]) * deltaSumEdgeMultipliedToDelta;
            }
        }

        return edgesWeight;
    }

    String makeLinearEquation(int neuronIndex, int[] foundEdgesFromNeuronToNeuron, double[][] edgeWeight, double equalTo)
    {
        String equation = "";

        for(int i = 0; i < foundEdgesFromNeuronToNeuron.length; i++)
        {
            char currentEquationChar = charsForEquations[i];
            if(i == 0)
                equation += edgeWeight[foundEdgesFromNeuronToNeuron[i]][neuronIndex] + "*" + currentEquationChar;
            else
                equation += "+" + edgeWeight[foundEdgesFromNeuronToNeuron[i]][neuronIndex] + "*" + currentEquationChar;
        }

        equation += "=" + equalTo;

        return equation;
    }

    public static double[] getSignalWithNewEdges(int[] nodes, int[][] edges, double[][] edgesWeight, double[] nodesValue, String activationFunction)
    {
        double[] currentNodesValue = new double[nodesValue.length];
        for(int i = 0; i < inputNeuronsAmount; i++)
        {
            currentNodesValue[i] = nodesValue[i];
        }

        for(int i = Consts.inputNeuronsAmount; i < nodes.length; i++)
        {
            double nodeInputSignal = 0;
            double nodeOutputSignal = 0;

            for(int j = 0; j < i; j++)
            {
                if(edges[j][i] == 1)
                {
                    nodeInputSignal += currentNodesValue[j] * edgesWeight[j][i];
                }
            }

            nodeInputSignal = roundDoubleToNDecimals(nodeInputSignal, roundNodesValueSign);

            switch (activationFunction) {
                case sigmoidFunction:
                    nodeOutputSignal = sigmoid(nodeInputSignal);
                    break;
                case linearFunction:
                    nodeOutputSignal = linear(nodeInputSignal);
                    break;
                case tgFunction:
                    nodeOutputSignal = tg(nodeInputSignal);
                    break;
            }

            nodeOutputSignal = roundDoubleToNDecimals(nodeOutputSignal, roundNodesValueSign);

            currentNodesValue[i] = nodeOutputSignal;
        }

        return currentNodesValue;
    }

    public static double[] getInputSignalWithNewEdges(int[] nodes, int[][] edges, double[][] edgesWeight, double[] nodesValue, String activationFunction)
    {
        double[] currentNodesValue = new double[nodesValue.length];
        double[] currentInputSignalValues = new double[nodesValue.length];

        for(int i = 0; i < inputNeuronsAmount; i++)
        {
            currentNodesValue[i] = nodesValue[i];
            currentInputSignalValues[i] = nodesValue[i];
        }

        for(int i = Consts.inputNeuronsAmount; i < nodes.length; i++)
        {
            double nodeInputSignal = 0;
            double nodeOutputSignal = 0;

            for(int j = 0; j < i; j++)
            {
                if(edges[j][i] == 1)
                {
                    nodeInputSignal += currentNodesValue[j] * edgesWeight[j][i];
                }
            }

            nodeInputSignal = roundDoubleToNDecimals(nodeInputSignal, roundNodesValueSign);
            currentInputSignalValues[i] = nodeInputSignal;

            switch (activationFunction) {
                case sigmoidFunction:
                    nodeOutputSignal = sigmoid(nodeInputSignal);
                    break;
                case linearFunction:
                    nodeOutputSignal = linear(nodeInputSignal);
                    break;
                case tgFunction:
                    nodeOutputSignal = tg(nodeInputSignal);
                    break;
            }

            nodeOutputSignal = roundDoubleToNDecimals(nodeOutputSignal, roundNodesValueSign);

            currentNodesValue[i] = nodeOutputSignal;
        }

        return currentInputSignalValues;
    }

    int[] findEdgesFromNeuronToNeuron(int nodeIndex, int[][] edges)
    {
        ArrayList<Integer> foundEdges = new ArrayList<Integer>();

        for(int i = 0; i < edges[nodeIndex].length; i++)
        {
            if(edges[nodeIndex][i] ==1)
                foundEdges.add(i);
        }

        return foundEdges.stream().mapToInt(i -> i).toArray();
    }

    int[] findEBackEdgesFromNeuronToNeuron(int nodeIndex, int[][] edges)
    {
        ArrayList<Integer> foundEdges = new ArrayList<Integer>();

        for(int i = 0; i < edges[nodeIndex].length; i++)
        {
            if(edges[i][nodeIndex] == 1)
                foundEdges.add(i);
        }

        return foundEdges.stream().mapToInt(i -> i).toArray();
    }
}
