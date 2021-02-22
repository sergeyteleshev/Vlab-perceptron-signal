package vlab.server_java.generate;

import org.json.JSONArray;
import org.json.JSONObject;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.generate.GenerateProcessor;
import vlab.server_java.Consts;

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

            int minInputNeuronValue = Consts.minInputNeuronValue;
            int maxInputNeuronValue = Consts.maxInputNeuronValue;
            int inputNeuronsAmount = Consts.inputNeuronsAmount;
            int outputNeuronsAmount = Consts.outputNeuronsAmount;

            int amountOfHiddenLayers = Consts.amountOfHiddenLayers;
            int amountOfNodesInHiddenLayer = Consts.amountOfNodesInHiddenLayer;
            int[] hiddenLayerNodesAmount = new int[amountOfHiddenLayers];
            int currentHiddenLayer = 2;

            int nodesAmount = inputNeuronsAmount + outputNeuronsAmount + amountOfNodesInHiddenLayer * amountOfHiddenLayers; //всего вершин в графе

            int[][] edges = new int[nodesAmount][nodesAmount];
            int[] nodes = new int[nodesAmount];
            Object[] nodesValue = new Object[nodesAmount];
            float[][] edgeWeight = new float[nodesAmount][nodesAmount];
            int[] nodesLevel = new int[nodesAmount];

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
                nodes[i] = i;
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
                        edgeWeight[i][j] = (float) roundDoubleToNDecimals(generateRandomDoubleRange(minEdgeValue, maxEdgeValue), 1);
                    }
                    else
                    {
                        edges[i][j] = 0;
                        edgeWeight[i][j] = 0;
                    }
                }
            }

            for (int i = 0; i < inputNeuronsAmount; i++)
            {
                inputNeuronsValues[i] = roundDoubleToNDecimals(generateRandomDoubleRange(minInputNeuronValue, maxInputNeuronValue), 2);
                text.append("input(X").append(i).append(") = ").append(inputNeuronsValues[i]).append(". ");
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

            code = graph.toString();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        text.append("Фунция активации - сигмоид.");

        return new GeneratingResult(text.toString(), code, instructions);
    }
}
