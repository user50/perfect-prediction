package oleogin.lerning;

import oleogin.common.FunctionBuilder;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.util.ModelSerializer;
import org.deeplearning4j.zoo.PretrainedType;
import org.deeplearning4j.zoo.model.ResNet50;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import static org.nd4j.linalg.indexing.NDArrayIndex.all;
import static org.nd4j.linalg.indexing.NDArrayIndex.point;

public class AttentedRegionsFunProvider {

    public Function<String, byte[]> get(Function<String, INDArray> act49, ComputationGraph graph, Function<String,byte[]> imgRepo){

        return new FunctionBuilder<>(act49)
                .<String,byte[]>wrap(actinvation49 -> {
                        INDArray attentionVector = graph.paramTable().get("self_attended_Q").reshape(1, 2048);

                        return bytes -> ImgUtil.dotProdShow(attentionVector, actinvation49.apply(bytes), imgRepo.apply(bytes));
                } )
                .build();


    }

}
