import java.io.File;
import java.util.Date;
import java.util.List;

import static util.FileUtil.getFiles;
import static util.KnowledgeRepository.testingFiles;

public class Main {

    public static void main(String[] args) {
        Date start = new Date();
        List<File> testFiles = getFiles(args[0]);

        float right = testingFiles(testFiles);

        Date finish = new Date();
        System.out.println(finish.getTime() - start.getTime() + " ms");
        System.out.println("Match percentage " + right / testFiles.size() * 100);
    }
}
