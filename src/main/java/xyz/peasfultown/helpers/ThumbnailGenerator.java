package xyz.peasfultown.helpers;

import java.io.File;
import java.nio.file.Path;
import org.im4java.core.IMOperation;
import org.im4java.core.ConvertCmd;

public class ThumbnailGenerator {
    public static void generatePDFThumbnail(File pdfFile, Path target) throws Exception {
        try {
            IMOperation op = new IMOperation();
            op.addImage(pdfFile.getAbsolutePath().toString());
            op.thumbnail(480);
            op.background("white");
            op.alpha("remove");
            op.addImage(target.toString());

            ConvertCmd cmd = new ConvertCmd();
            // TODO: prompt for search path?
            // cmd.setSearchPath("/usr/bin");
            cmd.run(op);
        } catch (Exception e) {
            throw new ThumbnailGeneratorException("Exception while trying to generate PDF thumbnail.", e);
        }
    }
}
