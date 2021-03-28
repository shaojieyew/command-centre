package c2.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;

public class FileFactory {

    public static final String SOURCE_TYPE_UPLOAD="Upload";
    public static final String SOURCE_TYPE_GIT="Git";

    public static File createFromMultipartFile(MultipartFile file, long projectId, String source, String sourceType) throws IOException {
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

    public static File createNioFile(java.io.File file, long projectId, String source, String sourceType, String url) throws IOException {
        String mimeType = URLConnection.guessContentTypeFromName(file.getName());
        File f = new File();
        f.setName(file.getName());
        f.setFileType(mimeType);
        f.setProjectId(projectId);
        f.setSource(source);
        f.setUrl(url);
        f.setSourceType(sourceType);
        byte[] bytes = Files.readAllBytes(file.toPath());
        String md5 = DigestUtils.md5Hex(bytes);
        f.setFileBlob(bytes);
        f.setFileHash(md5);
        return f;
    }
    public static File createFromString(String content, String mimeType, String fileName, long projectId, String source, String sourceType, String url) throws IOException {
        File f = new File();
        f.setName(fileName);
        f.setFileType(mimeType);
        f.setProjectId(projectId);
        f.setSource(source);
        f.setUrl(url);
        f.setSourceType(sourceType);
        byte[] bytes = content.getBytes();
        String md5 = DigestUtils.md5Hex(bytes);
        f.setFileBlob(bytes);
        f.setFileHash(md5);
        return f;
    }

}
