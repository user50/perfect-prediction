package oleogin.lerning;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.preprocessor.BaseInputPreProcessor;
import org.deeplearning4j.nn.workspace.ArrayType;
import org.deeplearning4j.nn.workspace.LayerWorkspaceMgr;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Objects;

public class RnnToFFPreprocessor extends BaseInputPreProcessor {

    private int size;

    public RnnToFFPreprocessor(int size) {
        this.size = size;
    }

    public RnnToFFPreprocessor() {
    }

    @Override
    public INDArray preProcess(INDArray input, int miniBatchSize, LayerWorkspaceMgr workspaceMgr) {
        long[] shape = input.shape();

        if (shape.length != 3)
            throw new RuntimeException("It seems that input shape is not Rnn");

        long[] newShape = new long[]{shape[0], shape[1]};

        return workspaceMgr.leverageTo(ArrayType.ACTIVATIONS, input.reshape(newShape));
    }

    @Override
    public INDArray backprop(INDArray output, int miniBatchSize, LayerWorkspaceMgr workspaceMgr) {
        long[] shape = output.shape();

        if (shape.length != 2)
            throw new RuntimeException("It seems that input shape is not rnn");

        long[] newShape = new long[]{shape[0], shape[1], 1};

        return workspaceMgr.leverageTo(ArrayType.ACTIVATION_GRAD, output.reshape(newShape));
    }

    @Override
    public InputType getOutputType(InputType inputType) {
        return InputType.feedForward(size);
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RnnToFFPreprocessor that = (RnnToFFPreprocessor) o;
        return size == that.size;
    }

    @Override
    public int hashCode() {
        return Objects.hash(size);
    }
}
