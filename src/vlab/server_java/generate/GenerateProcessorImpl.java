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
        int maxNodes = 6 ; //всего вершин в графе
        int maxEdgeValue = 15; //максимально возможный вес ребра
        int minEdgesNumberFromNode = 2; //сколько минимум из одной вершины должно выходить рёбер
        int maxEdgesNumberFromNode = 4; //сколько максимум из ондйо вершины должно выходить рёбер
        int[][] edges = new int[maxNodes][maxNodes];
        int[][] edgesBack = new int[maxNodes][maxNodes];
        int[] nodes = new int[maxNodes];
        final Random random = new Random();
        JSONObject graph = new JSONObject();

        for (int i = 0; i < nodes.length; i++)
        {
            nodes[i] = i;
        }

        for (int i = 0; i < edges.length; i++)
        {
            int currentEdgesNumberFromNode = minEdgesNumberFromNode + (int)(Math.random() * ((maxEdgesNumberFromNode - minEdgesNumberFromNode) + 1));

            if(currentEdgesNumberFromNode >= edges[i].length - i - 1)
            {
                currentEdgesNumberFromNode = edges[i].length - i - 1;
            }

            while(currentEdgesNumberFromNode > 0)
            {
                for (int j = i+1; j < edges[i].length; j++)
                {
                    if(edges[i][j] == 0)
                    {
                        if(random.nextBoolean())
                        {
                            edges[i][j] = random.nextInt(maxEdgeValue);
                            currentEdgesNumberFromNode--;
                        }
                    }
                }
            }
        }

        graph.put("nodes", nodes);
        graph.put("edges", edges);
        graph.put("edgesBack", edgesBack);

        code = graph.toString();
        text = "Найдите максимальный поток из вершины " + Integer.toString(nodes[0]) + " в вершину  " + Integer.toString(nodes.length - 1);

        return new GeneratingResult(text, code, instructions);
    }
}
