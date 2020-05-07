package oleogin.lerning;

import oleogin.common.FunctionBuilder;
import oleogin.rocksdb.RocksDbFunctionBuilder;
import org.datavec.image.loader.NativeImageLoader;
import org.deeplearning4j.nn.graph.ComputationGraph;
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

public class Activation49 {

    public static Function<String, INDArray> create(Function<String, byte[]> imageRepo){
        return new FunctionBuilder<>(imageRepo)
                .andThen(() -> {
                    NativeImageLoader imageLoader = new NativeImageLoader(224, 224, 3);

                    return imageBytes -> {
                        try {
                            return imageLoader.asMatrix(new ByteArrayInputStream(imageBytes));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    };
                })
                .andThen(indArray -> {
                    indArray.get(point(0), all()).get(point(0), all()).addi(-103.939);
                    indArray.get(point(0), all()).get(point(1), all()).addi(-116.779);
                    indArray.get(point(0), all()).get(point(2), all()).addi(-123.68);

                    return indArray;
                })
                .andThen(() -> {
                    ResNet50 zooModel = ResNet50.builder()
                            .build();

                    try {
                        zooModel.initPretrained(PretrainedType.IMAGENET);
                        ComputationGraph pretrainedNet = (ComputationGraph) zooModel.initPretrained();

                        return imageTensor -> pretrainedNet.output(Arrays.asList("activation_49"), false, new INDArray[]{imageTensor}, null)[0];
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toFuncBuilder(RocksDbFunctionBuilder::new)
                .addRocksdbCache("activation_49_cache", String::getBytes)
                .build();
    }

}
