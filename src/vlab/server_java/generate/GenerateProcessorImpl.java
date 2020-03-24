package vlab.server_java.generate;

import org.json.JSONObject;
import rlcp.generate.GeneratingResult;
import rlcp.server.processor.generate.GenerateProcessor;

import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.SocketHandler;

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

        int maxInputNeurons = 2;
        int minInputNeurons = 1;
        int maxOutputNeurons = 2;
        int minOutputNeurons = 1;
        int amountOfHiddenLayers = 2;
        int minAmountOfNeuronsInHiddenLayer = 3;
        int maxAmountOfNeuronsInHiddenLayer = 4;
        int minInputNeuronValue = 0;
        int maxInputNeuronValue = 1;

        final Random random = new Random();

        int inputNeuronsAmount = minInputNeurons + (int)(Math.random() * ((maxInputNeurons - minInputNeurons) + 1));
        int outputNeuronsAmount = minOutputNeurons + (int)(Math.random() * ((maxOutputNeurons - minOutputNeurons) + 1));
        int hiddenLayersAmount = minAmountOfNeuronsInHiddenLayer + (int)(Math.random() * ((maxAmountOfNeuronsInHiddenLayer - minAmountOfNeuronsInHiddenLayer) + 1));

        int nodesAmount = inputNeuronsAmount + outputNeuronsAmount + hiddenLayersAmount ; //всего вершин в графе
        int[][] edges = new int[nodesAmount][nodesAmount];

        int[] nodes = new int[nodesAmount];
        float[] nodesValue = new float[nodesAmount];
        int[] nodesLevel = new int[nodesAmount];

        JSONObject graph = new JSONObject();

        for(int i = 0; i < nodesAmount; i++)
        {
            nodes[i] = i;
            //рецепторы
            if (
                    i < inputNeuronsAmount
                    && i < inputNeuronsAmount + outputNeuronsAmount
                    && i < inputNeuronsAmount + outputNeuronsAmount +hiddenLayersAmount)
            {

            }
            //скрытые слои
            else if (i > inputNeuronsAmount && i < inputNeuronsAmount + outputNeuronsAmount + hiddenLayersAmount)
            {

            }
            //выходные нейроны
            else if (i > inputNeuronsAmount + outputNeuronsAmount)
            {

            }
        }

        for(int i = 0; i < inputNeuronsAmount; i++)
        {
            nodesValue[i] = minInputNeuronValue + (float)(Math.random() * ((maxInputNeuronValue - minInputNeuronValue) + 1));
        }


        graph.put("nodesValue", nodesValue);
        graph.put("edges", edges);

        code = graph.toString();
        text = "Найдите максимальный поток из вершины " + Integer.toString(nodes[0]) + " в вершину  " + Integer.toString(nodes.length - 1);

        return new GeneratingResult(text, code, instructions);
    }
}
