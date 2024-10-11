package com.rk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.web.multipart.MultipartFile;

public class FileUtil {
    public FileUtil() {
    }

    public static Map<String, Object> Image(MultipartFile attach, String path, long size) throws Exception {
        Map<String, Object> map = new HashMap();
        if (!attach.isEmpty()) {
            if (attach.getSize() >= size * 100L) {
                map.put("code", "2");
                return map;
            } else {
                String names = attach.getOriginalFilename();
                String prefixName = FilenameUtils.getExtension(names);
                SimpleDateFormat sdf = new SimpleDateFormat("MMddHHmmssSSS");
                String fileName = sdf.format(new Date()) + "." + prefixName;
                uploadFile(attach, path, fileName);
                map.put("paths", fileName);
                map.put("names", names);
                map.put("code", "0");
                return map;
            }
        } else {
            map.put("code", "1");
            return map;
        }
    }

    public static void uploadFile(MultipartFile attach, String filePath, String fileName) throws Exception {
        FileOutputStream out = null;

        try {
            File targetFile = new File(filePath);
            if (!targetFile.exists()) {
                targetFile.mkdirs();
            }

            InputStream input = attach.getInputStream();
            out = new FileOutputStream(new File(filePath + fileName));
            IOUtils.copy(input, out);
            out.close();
        } finally {
            if (out != null) {
                out.close();
            }

        }

    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        } else {
            return false;
        }
    }
}
