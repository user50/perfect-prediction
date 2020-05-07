package oleogin.lerning;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.NDArrayIndex;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImgUtil {

    public static void dotProdShow(INDArray vector, INDArray act49, File img)  {
        long[] shape = act49.shape();
        try {
            BufferedImage read = ImageIO.read(new FileInputStream(img));

            int w = read.getWidth() / (int) shape[2];
            int h = read.getHeight() / (int) shape[3];

            double maxValue = 0;

            for (int i = 0; i < (int) shape[2]; i++) {
                for (int j = 0; j < (int) shape[3]; j++) {
                    INDArray array = act49.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j), NDArrayIndex.point(i)).reshape(new int[]{1, 2048});
                    double v = Math.exp(array.mmul(vector.transpose()).getDouble(0)/Math.sqrt(2048));
                    maxValue = v > maxValue ? v : maxValue;
                }
            }

            for (int i = 0; i < (int) shape[2]; i++) {
                for (int j = 0; j < (int) shape[3]; j++) {
                    INDArray array = act49.get(NDArrayIndex.point(0), NDArrayIndex.all(), NDArrayIndex.point(j), NDArrayIndex.point(i)).reshape(new int[]{1, 2048});
                    double v = Math.exp(array.mmul(vector.transpose()).getDouble(0)/Math.sqrt(2048));

                    float scale = (float)(v/maxValue);
                    changeBightnessOfImagePart(read, scale, i * w, j * h, w, h);
                }
            }

            ImageIO.write(read, "jpg", new File("d:/attended/"+img.getName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void changeBightnessOfImagePart(BufferedImage src, float scale, int x, int y, int w, int h){

        RescaleOp rescaleOp = new RescaleOp(scale, 0f, null);

        BufferedImage image = rescaleOp.filter(src.getSubimage(x,y,w,h), null );

        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                src.setRGB(i+x,j+y,image.getRGB(i,j));
            }
        }
    }
}
