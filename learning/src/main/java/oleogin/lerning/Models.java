package oleogin.lerning;

import org.deeplearning4j.api.storage.StatsStorage;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.PreprocessorVertex;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.LearnedSelfAttentionLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.ui.api.UIServer;
import org.deeplearning4j.ui.stats.StatsListener;
import org.deeplearning4j.ui.storage.InMemoryStatsStorage;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Sgd;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.File;
import java.io.IOException;

public class Models {

    public static ComputationGraph getAttentionModel() throws IOException {
        ComputationGraphConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(12345)
                .l2(0.00001)
                .weightInit(WeightInit.XAVIER)
                .updater(new Sgd(0.001))
                .graphBuilder()
                .addInputs("activation_49")
                .addLayer("self_attended", new LearnedSelfAttentionLayer.Builder()
                        .nIn(2048)
                        .nOut(2048)
                        .nQueries(1)
                        .projectInput(false)
                        .nHeads(1)
                        .build(), "activation_49")
                .addVertex("flatten_1", new PreprocessorVertex(new RnnToFFPreprocessor(2048)),
                        "self_attended")
                .addLayer("flatten_2", new DenseLayer.Builder()
                        .nIn(2048)
                        .dropOut(0.50)
                        .nOut(256)
                        .activation(Activation.RELU)
                        .build(), "flatten_1")
                .addLayer("output", new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nIn(256)
                        .dropOut(0.50)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .build(), "flatten_2")
                .setOutputs("output")
                .build();

        ComputationGraph network =   new ComputationGraph(config);
        network.init();

        File file = new File("mode.zip");

        if (file.exists())
            network.setParams(ModelSerializer.restoreComputationGraph(file).params());


        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);

        network.setListeners(new StatsListener(statsStorage, 100));

        return network;
    }

    public static MultiLayerNetwork getSimpleModel() {
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .updater(new Sgd(0.001))
                .l2(0.000001)
                .list()
                .layer(0, new DenseLayer.Builder()
                        .nIn(2048)
                        .dropOut(0.50)
                        .nOut(256)
                        .activation(Activation.RELU)
                        .build()
                )
                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.XENT)
                        .nIn(256)
                        .dropOut(0.50)
                        .nOut(1)
                        .activation(Activation.SIGMOID)
                        .build()
                )
                .build();

        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();

        UIServer uiServer = UIServer.getInstance();
        StatsStorage statsStorage = new InMemoryStatsStorage();
        uiServer.attach(statsStorage);

        network.setListeners(new StatsListener(statsStorage, 100));

        return network;
    }
}
