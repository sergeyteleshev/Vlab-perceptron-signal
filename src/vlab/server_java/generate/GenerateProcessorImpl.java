package vlab.server_java.generate;

import org.json.JSONObject;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.generate.GenerateProcessor;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.SocketHandler;
import java.util.Arrays;

/**
 * Simple GenerateProcessor implementation. Supposed to be changed as needed to
 * provide necessary Generate method support.
 */
public class GenerateProcessorImpl implements GenerateProcessor {
    @Override
    public GeneratingResult generate(String condition) {
        //do Generate logic here
        String text;
        String code;
        String instructions = "instructions";
        final Random random = new Random();
        JSONObject graph = new JSONObject();

        int maxInputNeurons = 2;
        int minInputNeurons = 1;

        int maxOutputNeurons = 2;
        int minOutputNeurons = 1;

        int minInputNeuronValue = 0;
        int maxInputNeuronValue = 1;
        int inputNeuronsAmount = minInputNeurons + (int)(Math.random() * ((maxInputNeurons - minInputNeurons) + 1));
        int outputNeuronsAmount = minOutputNeurons + (int)(Math.random() * ((maxOutputNeurons - minOutputNeurons) + 1));

        int amountOfHiddenLayers = 4;
        int amountOfNodesInHiddenLayer = 8;
        int[] hiddenLayerNodesAmount = new int[amountOfHiddenLayers];
        int nodesPerHiddenLayer = (int) Math.round(amountOfNodesInHiddenLayer / amountOfHiddenLayers);
        int currentHiddenLayer = 2;

        int nodesAmount = inputNeuronsAmount + outputNeuronsAmount + amountOfNodesInHiddenLayer ; //всего вершин в графе

        int[][] edges = new int[nodesAmount][nodesAmount];
        int[] nodes = new int[nodesAmount];
        float[] nodesValue = new float[nodesAmount];
        int[] nodesLevel = new int[nodesAmount];

        for(int i = inputNeuronsAmount; i < inputNeuronsAmount + amountOfNodesInHiddenLayer; i++)
        {
            nodesLevel[i] = currentHiddenLayer;
            if(i % nodesPerHiddenLayer == 0)
            {
                currentHiddenLayer++;
            }
        }

        for(int i = 0; i < nodesAmount; i++)
        {
            nodes[i] = i;
            //рецепторы
            if (
                    i < inputNeuronsAmount
                    && i < inputNeuronsAmount + outputNeuronsAmount
                    && i < inputNeuronsAmount + outputNeuronsAmount + amountOfNodesInHiddenLayer)
            {
                //начальные значения для рецепторов
                for(int r = 0; r < inputNeuronsAmount; r++)
                {
                    nodesLevel[r] = 1;
                    nodesValue[r] = minInputNeuronValue + (float)(Math.random() * ((maxInputNeuronValue - minInputNeuronValue) + 1));
                }

                for(int e = 0; e < nodesAmount; e++)
                {
                    if(e > inputNeuronsAmount && e < inputNeuronsAmount + outputNeuronsAmount)
                        edges[i][e] = random.nextInt(2);
                }
            }
            //скрытые слои
            else if (i >= inputNeuronsAmount && i < inputNeuronsAmount + outputNeuronsAmount)
            {

            }
            //выходные нейроны
            else if (i >= inputNeuronsAmount + amountOfNodesInHiddenLayer)
            {
                nodesLevel[i] = 1 + amountOfHiddenLayers + 1;
            }
        }

        for(int i = 0; i < nodesAmount; i++)
        {
            int currentNodeLevel = nodesLevel[i];
            ArrayList<Integer> nextLayerIndexes = this.findIndexesByValueInArray(nodesLevel, currentNodeLevel+1);

            for(int j = 0; i < nextLayerIndexes.size(); j++)
            {
                edges[i][nextLayerIndexes.get(j)] = 1;
            }
        }

        graph.put("nodes", nodes);
        graph.put("nodesLevel", nodesLevel);
        graph.put("nodesValue", nodesValue);
        graph.put("edges", edges);
        graph.put("hiddenNodesLeft", hiddenLayerNodesAmount);

        code = graph.toString();
        text = "Найдите максимальный поток из вершины " + Integer.toString(nodes[0]) + " в вершину  " + Integer.toString(nodes.length - 1);

        return new GeneratingResult(text, code, instructions);
    }

    private ArrayList<Integer> findIndexesByValueInArray(int[] nodesLevel, int value) {
        ArrayList<Integer> result = new ArrayList<>();
        for(int i=0; i < nodesLevel.length; i++)
            if(nodesLevel[i] == value)
                result.add(i);

        return result;
    }
}
