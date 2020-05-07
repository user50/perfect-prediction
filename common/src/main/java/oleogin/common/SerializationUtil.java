package oleogin.common;



import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class SerializationUtil {

    public static Collection<Float> bytes2floatList(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        List<Float> vector = new ArrayList<>();

        for (int i = 0; i < bytes.length/4; i++)
            vector.add(buffer.getFloat());

        return vector;
    }

    public static <T> Optional<T> read(InputStream is, Function<byte[],T> deserializer){
        Optional<Integer> integer = SerializationUtil.readInt(is);

        if (!integer.isPresent())
            return Optional.empty();

        byte[] bytes = new byte[integer.get()];
        try {
            int read = is.read(bytes);

            if (read != bytes.length)
                throw new RuntimeException("It seems that the stream is corrupted");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Optional.of(deserializer.apply(bytes));
    }

    public static float[] bytes2floatArray(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        float[] vector = new float[bytes.length/4];

        for (int i = 0; i < bytes.length/4; i++)
            vector[i] = buffer.getFloat();

        return vector;
    }

    public static float[] bytes2floatArray(byte[] bytes, ByteOrder byteOrder){
        ByteBuffer buffer = ByteBuffer.wrap(bytes).order(byteOrder);

        float[] vector = new float[bytes.length/4];

        for (int i = 0; i < bytes.length/4; i++)
            vector[i] = buffer.getFloat();

        return vector;
    }

    public static byte[] floatList2bytes(float[] values){
        List<Byte> bytes = new ArrayList<>();

        for (Float value : values) {
            for (byte b : ByteBuffer.allocate(4).putFloat(value).array()) {
                bytes.add(b);
            }
        }

        byte[] byteArray = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            byteArray[i] = bytes.get(i);

        return byteArray;
    }

    public static Collection<Integer> bytes2intList(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        List<Integer> vector = new ArrayList<>();

        for (int i = 0; i < bytes.length/4; i++)
            vector.add(buffer.getInt());

        return vector;
    }

    public static int bytes2int(byte[] bytes){
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte[] intList2bytes(Collection<Integer> values){
        List<Byte> bytes = new ArrayList<>();

        for (Integer value : values) {
            for (byte b : ByteBuffer.allocate(4).putInt(value).array()) {
                bytes.add(b);
            }
        }

        byte[] byteArray = new byte[bytes.size()];

        for (int i = 0; i < bytes.size(); i++)
            byteArray[i] = bytes.get(i);

        return byteArray;
    }

    public static byte[] int2bytes(int value){
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte[] float2bytes(float value){
        return ByteBuffer.allocate(4).putFloat(value).array();
    }

    public static byte[] serializeInt2IntMap(Map<Integer,Integer> map){
        byte[] bytes = new byte[map.size()*8];

        int index = 0;
        for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
            for (byte b : int2bytes(entry.getKey()))
                bytes[index++]=b;

            for (byte b : int2bytes(entry.getValue()))
                bytes[index++]=b;
        }

        return bytes;
    }

    public static byte[] serializeInt2FloatMap(Map<Integer,Float> map){
        byte[] bytes = new byte[map.size()*8];

        int index = 0;
        for (Map.Entry<Integer, Float> entry : map.entrySet()) {
            for (byte b : int2bytes(entry.getKey()))
                bytes[index++]=b;

            for (byte b : float2bytes(entry.getValue()))
                bytes[index++]=b;
        }

        return bytes;
    }

    public static Map<Integer,Integer> deserializeInt2IntMap(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Map<Integer,Integer> result = new HashMap<>();
        for (int i = 0; i < bytes.length / 8; i++)
            result.put(buffer.getInt(), buffer.getInt());

        return result;
    }


    public static Map<Integer,Float> deserializeInt2FloatMap(byte[] bytes){
        ByteBuffer buffer = ByteBuffer.wrap(bytes);

        Map<Integer,Float> result = new HashMap<>();
        for (int i = 0; i < bytes.length / 8; i++)
            result.put(buffer.getInt(), buffer.getFloat());

        return result;
    }

    public static byte[] concat( byte[] ... arrays){
        int size = Arrays.stream(arrays).mapToInt(v -> v.length).sum();

        byte[] array = new byte[size];
        int index = 0;

        for (byte[] bytes : arrays)
            for (byte aByte : bytes)
                array[index++] = aByte;

        return array;
    }

    public static byte[] serialize(Serializable obj){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try(ObjectOutputStream objectOutputStream = new ObjectOutputStream(new BufferedOutputStream(outputStream))){
            objectOutputStream.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return outputStream.toByteArray();
    }

    public static <T> T deserialize(byte[] objBytes){
        try(ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(objBytes))){
            return (T)objectInputStream.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] gzip(byte[] bytes){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try(OutputStream gzipOs = new GZIPOutputStream(os)){
            gzipOs.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return os.toByteArray();
    }

    public static Optional<Integer> readInt(InputStream is)  {
        byte[] bytes = new byte[4];

        try {
            int read = is.read(bytes);

            if (read <= 0)
                return Optional.empty();

            if (read == 4 )
                return Optional.of(bytes2int(bytes));

            throw new RuntimeException("red "+read+" bytes, but need 4");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> T readObj(InputStream is){
        try {
            return (T)new ObjectInputStream(is).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> T readGzipedObj(byte[] bytes){
        try {
            return (T)new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes))).readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> T readObj(byte[] bytes){

        try(InputStream is = new ByteArrayInputStream(bytes)){
            return readObj(is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] writeObj(Serializable obj){
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        try(ObjectOutputStream oos = new ObjectOutputStream(os)){
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return os.toByteArray();
    }

    public static byte[] writeObjGzip(Serializable obj){
        return gzip(writeObj(obj));
    }

    public static void writeObj(Serializable obj, String fileName){
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))){
            oos.writeObject(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] writeObjSizeAndObj(Serializable obj){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] bytes = writeObj(obj);
        try {
            os.write(int2bytes(bytes.length));
            os.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        return os.toByteArray();
    }

    public static byte[] writeStringAndSize(String s, String charset){
        try {
            return writeBytesAndSize(s.getBytes(charset));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

    }

    public static byte[] writeBytesAndSize(byte[] bytes){
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            os.write(int2bytes(bytes.length));
            os.write(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return os.toByteArray();
    }

    public static byte[] ungzip(byte[] bytes){
        try(InputStream is = new GZIPInputStream(new ByteArrayInputStream(bytes));
            ByteArrayOutputStream buffer = new ByteArrayOutputStream()
        ) {

            int nRead;
            byte[] data = new byte[16384];

            while ((nRead = is.read(data, 0, data.length)) != -1)
                buffer.write(data, 0, nRead);

            return buffer.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static  <T> Optional<T> readOneObject(InputStream is){
        Optional<Integer> integer = readInt(is);

        if (!integer.isPresent()) {
            return Optional.empty();
        }

        byte[] objBytes = new byte[integer.get()];
        try {
            int read = is.read(objBytes);

            if (read != integer.get())
                throw new RuntimeException("the stream is corrupted");

            return Optional.of(readObj(new ByteArrayInputStream(objBytes)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<byte[]> readBytes(InputStream is, int count){
        byte[] bytes = new byte[count];

        try {
            int read = is.read(bytes);

            if (read <= 0)
                return Optional.empty();

            if (read != count)
                throw new RuntimeException("it seems that the data is corrupted");

            return Optional.of(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Optional<String> readOneLine(InputStream is, String charset){
        Optional<Integer> integer = readInt(is);

        if (!integer.isPresent()) {
            return Optional.empty();
        }

        byte[] objBytes = new byte[integer.get()];
        try {
            int read = is.read(objBytes);

            if (read != integer.get())
                throw new RuntimeException("the stream is corrupted");

            return Optional.of(new String(objBytes, charset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <A> Optional<A> readElement(InputStream is, Function<byte[],A> deserializer) {
        Optional<Integer> bytesCount = readInt(is);

        if (!bytesCount.isPresent())
            return Optional.empty();

        byte[] elementBytes = new byte[bytesCount.get()];

        if (bytesCount.get() == 0)
            return Optional.of(deserializer.apply(elementBytes));

        try {
            int read = is.read(elementBytes);

            if (read != bytesCount.get())
                throw new RuntimeException("It seems that the data is corrupted");

            return Optional.of(deserializer.apply(elementBytes));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <A> Optional<A> readElement(byte[] bytes, Function<byte[],A> deserializer) {
        try(InputStream is = new ByteArrayInputStream(bytes)){
            return readElement(is, deserializer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <A extends Serializable> A getLazyCachedObject(Supplier<A> objSupplier, String objFileName){
        File file = new File(objFileName);

        if (!file.exists()){
            writeObj(objSupplier.get(), objFileName);
        }

        try {
            return readObj(new FileInputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}

