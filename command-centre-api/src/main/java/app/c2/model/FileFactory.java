package app.c2.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;

public class FileFactory {

    public static final String SOURCE_TYPE_UPLOAD="Upload";
    public static final String SOURCE_TYPE_GIT="Git";
    public static final String SOURCE_TYPE_STRING ="String";

    public static File createFromFile(MultipartFile file, long projectId, String source, String sourceType) throws IOException {
        File f = new File();
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        f.setName(fileName);
        f.setFileType(file.getContentType());
        f.setProjectId(projectId);
        f.setSource(source);
        f.setSourceType(sourceType);
        byte[] bytes = file.getBytes();
        String md5 = DigestUtils.md5Hex(bytes);
        f.setFileBlob(bytes);
        f.setFileHash(md5);
        return f;
    }

}
