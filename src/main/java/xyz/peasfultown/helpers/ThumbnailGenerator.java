package xyz.peasfultown.helpers;

import org.im4java.core.ConvertCmd;
import org.im4java.core.IMOperation;
import xyz.peasfultown.Application;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ThumbnailGenerator {
    private static final String COVER_FILE_NAME_PATTERN = ".*cover\\.(jpg|png)";
    public static void generatePDFThumbnail(File pdfFile, Path target) throws ThumbnailGeneratorException {
        try {
            IMOperation op = new IMOperation();
            op.addImage(pdfFile.getAbsolutePath().toString()+"[0]");
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

    public static void generateEpubThumbnail(File epubFile, Path target) throws ThumbnailGeneratorException {
        Path tmpFile;
        try (ZipFile zipFile = new ZipFile(epubFile)) {
            ZipEntry coverFile = getCoverFromZip(zipFile);
            if (coverFile == null)
                throw new ThumbnailGeneratorException("Ebook cover file not found.");
            String[] parts = epubFile.getName().split("\\.");
            tmpFile = Files.createTempFile("jebman", parts[1].equals("png") ? ".png" : ".jpg");
            Files.copy(zipFile.getInputStream(coverFile), tmpFile, StandardCopyOption.REPLACE_EXISTING);
            tmpFile.toFile().deleteOnExit();

            IMOperation op = new IMOperation();
            op.addImage(tmpFile.toAbsolutePath().toString());
            op.thumbnail(300);
            op.addImage(target.toString());

            ConvertCmd cmd = new ConvertCmd();
            // TODO: prompt for search path?
            // cmd.setSearchPath("/usr/bin");
            cmd.run(op);
        } catch (Exception e) {
            throw new ThumbnailGeneratorException("Exception occurred while trying to generate thumbnail for Epub file", e);
        }
    }

    private static ZipEntry getCoverFromZip(ZipFile zipFile) {
        Iterator iFiles = zipFile.entries().asIterator();
        while (iFiles.hasNext()) {
            ZipEntry entry = (ZipEntry) iFiles.next();
            if (Pattern.matches(COVER_FILE_NAME_PATTERN, entry.getName())) {
                System.out.println("zip entry file found");
                return entry;
            }
        }

        return null;
    }

    public static void addPlaceHolderThumbnail(Path target) throws ThumbnailGeneratorException {
        try {
            Files.copy(Path.of(Application.class.getClassLoader().getResource("nocover.png").getFile()), target);
        } catch (Exception e) {
            throw new ThumbnailGeneratorException("Exception while trying to copy over the default cover image.", e);
        }
    }
}
