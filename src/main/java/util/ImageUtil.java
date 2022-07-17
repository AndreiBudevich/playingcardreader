package util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

public class ImageUtil {

    private static final int[][] ctm = {
            {1, 0, 1, 0, 0, 0, 1, 0}, //left
            {0, 1, 0, 1, 0, 0, 0, 1}, //up
            {0, 0, 1, 0, 1, 0, 1, 0}, //right
            {0, 0, 0, 1, 0, 1, 0, 1}, //down
            {1, 1, 1, 1, 0, 0, 1, 1}, //up left
            {0, 1, 1, 1, 1, 0, 1, 1}, //up right
            {1, 0, 1, 1, 0, 1, 1, 1}, //down left
            {0, 0, 1, 1, 1, 1, 1, 1}};//down right

    private ImageUtil() {
    }

    public static BufferedImage getFromFile(File file) {
        BufferedImage bufferedImage = null;
        try {
            bufferedImage = ImageIO.read(file);
        } catch (IOException e) {
            System.err.println("Could not read file");
        }
        return bufferedImage;
    }

    public static BufferedImage[][] getArraySubImage(BufferedImage bufferedImage) {
        BufferedImage[][] arraySubImage = new BufferedImage[5][2];
        for (int i = 0; i < 5; i++) {
            arraySubImage[i][0] = bufferedImage.getSubimage(170 + i * (42 + 29), 634, 29, 32);
            arraySubImage[i][1] = bufferedImage.getSubimage(148 + i * (42 + 29), 591, 29, 23);
        }
        return arraySubImage;
    }

    public static BufferedImage convertToGray(BufferedImage sourceImg) {
        BufferedImage image = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics g = image.getGraphics();
        g.drawImage(sourceImg, 0, 0, null);
        g.dispose();
        return image;
    }

    public static BufferedImage doThreshold(BufferedImage image) {

        final int[] histData = new int[256];
        int maxLevelValue;
        int threshold;

        int width = image.getWidth();
        int height = image.getHeight();

        byte[] srcData = getDBB(image);
        byte[] monoData = new byte[srcData.length];

        int ptr = 0;
        while (ptr < histData.length) histData[ptr++] = 0;

        ptr = 0;
        maxLevelValue = 0;
        while (ptr < srcData.length) {
            int h = 0xFF & srcData[ptr];
            histData[h]++;
            if (histData[h] > maxLevelValue) maxLevelValue = histData[h];
            ptr++;
        }

        int total = srcData.length;

        float sum = 0;
        for (int t = 0; t < 256; t++) sum += t * histData[t];

        float sumB = 0;
        int wB = 0;
        int wF = 0;

        float varMax = 0;
        threshold = 0;

        for (int t = 0; t < 256; t++) {
            wB += histData[t];
            if (wB == 0) continue;

            wF = total - wB;
            if (wF == 0) break;

            sumB += (t * histData[t]);

            float mB = sumB / wB;
            float mF = (sum - sumB) / wF;

            float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);

            if (varBetween > varMax) {
                varMax = varBetween;
                threshold = t;
            }
        }

        ptr = 0;
        while (ptr < srcData.length) {
            monoData[ptr] = ((0xFF & srcData[ptr]) >= threshold) ? (byte) 255 : 0;
            ptr++;
        }
        return createFromBytes(monoData, width, height);
    }

    public static boolean compare(BufferedImage srcData, BufferedImage dstData) {
        int height = srcData.getHeight();
        int width = srcData.getWidth();
        return compareArrayByteImage(getDBB(srcData), getDBB(dstData), width, height);
    }

    private static byte[] getDBB(BufferedImage bufferedImage) {
        Raster raster = bufferedImage.getData();
        DataBuffer buffer = raster.getDataBuffer();
        DataBufferByte byteBuffer = (DataBufferByte) buffer;
        return byteBuffer.getData(0);
    }


    private static boolean compareArrayByteImage(byte[] srcBytes, byte[] dstBytes, int width, int height) {
        Float[] val = {0f, 0f, 0f, 0f, 0f};

        if (srcBytes.length != dstBytes.length) {
            System.err.println("Arrays do not match in size");
        }

        for (int i = 0; i < srcBytes.length; i++) {
            if (srcBytes[i] == dstBytes[i]) {
                val[0]++;
            }
            if (i > width && srcBytes[i] == dstBytes[i - width]) {
                val[1]++;
            }
            if (i % width != 0 && srcBytes[i] == dstBytes[i - 1]) {
                val[2]++;
            }
            if ((i + 1) % width != 0 && srcBytes[i] == dstBytes[i + 1]) {
                val[3]++;
            }
            if (i / width > height - 1 && srcBytes[i] == dstBytes[i - width]) {
                val[4]++;
            }
        }
        Arrays.sort(val, Collections.reverseOrder());
        return val[0] / srcBytes.length > 0.91;
    }

    private static BufferedImage createFromBytes(byte[] imageData, int width, int height) {
        BufferedImage outBufImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        outBufImg.setData(Raster.createRaster(outBufImg.getSampleModel(), new DataBufferByte(imageData, imageData.length), new Point()));
        return outBufImg;
    }

    public static BufferedImage[] reproduction(BufferedImage sourceImg) {
        BufferedImage[] arrayImage = new BufferedImage[9];
        int width = sourceImg.getWidth();
        int height = sourceImg.getHeight();

        arrayImage[8] = sourceImg;
        for (int i = 0; i < ctm.length; i++) {
            BufferedImage image = new BufferedImage(sourceImg.getWidth(), sourceImg.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            Graphics g = image.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0, 0, width, height);
            g.drawImage(sourceImg.getSubimage(ctm[i][0], ctm[i][1], width - ctm[i][2], height - ctm[i][3]),
                    ctm[i][4], ctm[i][5], width - ctm[i][6], height - ctm[i][7], null);
            g.dispose();
            arrayImage[i] = image;
        }
        return arrayImage;
    }
}
