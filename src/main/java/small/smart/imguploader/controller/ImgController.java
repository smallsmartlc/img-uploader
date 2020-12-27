package small.smart.imguploader.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import small.smart.imguploader.message.ResponseMessage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLEncoder;
import java.util.UUID;

/**
 * @author LuCong
 * @since 2020-12-09
 **/
@RestController
@RequestMapping("file")
public class ImgController {

    /**
     * 默认大小 50M
     */
    public static final long DEFAULT_MAX_SIZE = 2 * 1024 * 1024;

    @Value("${web.upload-path}")
    private String mImagesPath;

    @GetMapping("img/{fileName}")
    public void downImg(@PathVariable("fileName") String fileName,
                        HttpServletResponse response) throws Exception {
        String path = "E:/";
        response.reset();
        response.setCharacterEncoding("UTF-8");
        response.setContentType("image/jpeg");
        //设置响应头
        response.setHeader("Content-Dispostion",
                "attachment;fileName=" + URLEncoder.encode(fileName, "UTF-8"));
        File file = new File(path, fileName);
        InputStream is = new FileInputStream(file);
        OutputStream os = response.getOutputStream();

        byte[] buffer = new byte[1024];
        int index = 0;
        while ((index = is.read(buffer)) != -1) {
            os.write(buffer, 0, index);
            os.flush();
        }
        os.close();
        is.close();
    }


    @PostMapping("img/{project}/{folder}/{key}")
    public ResponseMessage uploadImg(@RequestParam("file") MultipartFile file,
                                     @PathVariable("project") String project,
                                     @PathVariable("folder") String folder,
                                     @PathVariable("key") String key) throws FileNotFoundException {
//        if(!key.equals(uploadKey)){
//            return new ResponseMessage("0000000009","没有权限上传文件",null);
//        }
        String targetpath = "images/" + project + "/" + folder;
        String pathname = mImagesPath + targetpath;
        File filePath = new File(pathname);
        if (!filePath.exists() && !filePath.isDirectory()) {
            filePath.mkdirs();
        }
        if (file.isEmpty()) {
            return new ResponseMessage("400", "文件为空", null);
        }
        //判断文件是否为空文件
        if (file.getSize() <= 0) {
            return new ResponseMessage("400", "文件为空", null);
        }
        // 判断文件大小不能大于50M
        if (DEFAULT_MAX_SIZE != -1 && file.getSize() > DEFAULT_MAX_SIZE) {
            return new ResponseMessage("400", "上传的文件不能大于2M", false);
        }
        String fileName = file.getOriginalFilename();
        String fileExtension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();
        String newName = UUID.randomUUID().toString().replaceAll("-", "") + fileExtension;
        File targetFile = new File(filePath, newName);

        try {
            file.transferTo(targetFile);
        } catch (IOException e) {
            System.err.println(e);
            return new ResponseMessage("400", "文件上传失败", null);
        }
        return new ResponseMessage(targetpath + newName);
    }
}
