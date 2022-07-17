package util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static util.FileUtil.getFiles;
import static util.ImageUtil.*;

public class KnowledgeRepository {

    private static final Map<BufferedImage, String> importanceBufferedImages = new HashMap<>();
    private static final Map<BufferedImage, String> suitOfCardsBufferedImages = new HashMap<>();

    private static final List<File> importanceFiles = getFiles("d:/importanceFiles");
    private static final List<File> suitOfCardsFiles = getFiles("d:/suitOfCardsFiles");

    static {
        for (File importanceFile : importanceFiles) {
            BufferedImage fromFile = getFromFile(importanceFile);
            BufferedImage[] box = reproduction(fromFile);
            for (BufferedImage bufferedImage : box) {
                importanceBufferedImages.put(bufferedImage, importanceFile.getName().replaceAll("[^\\dA-Z]", ""));
            }
        }

        for (File suitOfCardsFile : suitOfCardsFiles) {
            BufferedImage fromFile = getFromFile(suitOfCardsFile);
            BufferedImage[] box = reproduction(fromFile);
            for (BufferedImage bufferedImage : box) {
                suitOfCardsBufferedImages.put(bufferedImage, suitOfCardsFile.getName().substring(0, 1));
            }
        }
    }

    private KnowledgeRepository() {
    }

    public static float testingFiles(List<File> testFiles) {
        float right = 0;
        for (File testFile : testFiles) {
            String expectedNameFile = testFile.getName();
            StringBuilder actualNameFile = new StringBuilder();
            BufferedImage bufImgTestFile = getFromFile(testFile);
            BufferedImage[][] arrayBufSubImageTestFile = getArraySubImage(bufImgTestFile);

            for (BufferedImage[] bufferedImages : arrayBufSubImageTestFile) {
                BufferedImage actualSuitOfCardsBufImg = ImageUtil.doThreshold(convertToGray(bufferedImages[0]));
                BufferedImage actualImportanceBufImg = ImageUtil.doThreshold(convertToGray(bufferedImages[1]));
                for (Map.Entry<BufferedImage, String> entrySuitOfCards : suitOfCardsBufferedImages.entrySet()) {
                    BufferedImage expectedSuitOfCards = entrySuitOfCards.getKey();
                    if (compare(actualSuitOfCardsBufImg, expectedSuitOfCards)) {
                        String valueSuitOfCards = entrySuitOfCards.getValue();
                        for (Map.Entry<BufferedImage, String> entryImportance : importanceBufferedImages.entrySet()) {
                            BufferedImage expected = entryImportance.getKey();
                            if (compare(actualImportanceBufImg, expected)) {
                                String valueImportance = entryImportance.getValue();
                                actualNameFile.append(valueImportance).append(valueSuitOfCards);
                                break;
                            }
                        }
                        break;
                    }
                }
            }
            String basename = expectedNameFile.substring(0, expectedNameFile.lastIndexOf("."));
            if (basename.equals(actualNameFile.toString())) {
                right++;
            }
            System.out.println(expectedNameFile + " - " + actualNameFile);
        }
        return right;
    }
}
