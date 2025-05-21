package com.ruoyi.common.utils;

import com.google.gson.Gson;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.ruoyi.common.constant.BaseConstants;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

@Data
@AllArgsConstructor
@Slf4j
public class QiniuOssUtil {

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String url;

    private UploadManager uploadManager;    //七牛文件上传管理器
    private String token;   //上传的token
    private Auth auth;  //七牛认证管理
    private BucketManager bucketManager;


    public QiniuOssUtil(String accessKey, String secretKey, String bucket, String url) {

        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucket;
        this.url = url;

        // 华南:zone2,华北:zone1
        uploadManager = new UploadManager(new Configuration(Zone.zone1()));
        auth = Auth.create(accessKey, secretKey);
        // 根据命名空间生成的上传token
        bucketManager = new BucketManager(auth, new Configuration(Zone.zone1()));
        token = auth.uploadToken(bucket);
    }

    /**
     * 文件上传
     *
     * @return
     */
    public String upload(String objectName, MultipartFile file) {

        try {
            //判断是否为恶意程序
            //通过流的方式把文件转换为BufferedImage对象,获取宽和高,只有图片才具有宽高属性
            BufferedImage bufferedImage = ImageIO.read(file.getInputStream());
            if (bufferedImage == null || bufferedImage.getHeight() == 0 || bufferedImage.getWidth() == 0) {
                return BaseConstants.UPLOAD_FAILED;
            }
            // 上传图片文件
            Response res = uploadManager.put(file.getInputStream(), objectName, token, null, null);
            if (!res.isOK()) {
                throw new RuntimeException(BaseConstants.UPLOAD_FAILED + res);
            }
            // 解析上传成功的结果
            DefaultPutRet putRet = new Gson().fromJson(res.bodyString(), DefaultPutRet.class);
            // 直接返回外链地址
            return getPrivateFile(objectName);
        } catch (QiniuException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return BaseConstants.UPLOAD_FAILED;
    }

    /**
     * 获取私有空间文件
     *
     * @param fileKey
     * @return
     */
    public String getPrivateFile(String fileKey) {
        String encodedFileName = null;
        String finalUrl = null;
        try {
            encodedFileName = URLEncoder.encode(fileKey, "utf-8").replace("+", "%20");
            String publicUrl = String.format("%s/%s", url, encodedFileName);
            //1小时，可以自定义链接过期时间
            long expireInSeconds = 3600;
            finalUrl = auth.privateDownloadUrl(publicUrl, expireInSeconds);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        log.info("图片地址：{}", finalUrl);
        return finalUrl;
    }


    /**
     * 根据空间名、文件名删除文件
     *
     * @param bucketName
     * @param fileKey
     * @return
     */
    public boolean removeFile(String bucketName, String fileKey) {
        try {
            bucketManager.delete(bucketName, fileKey);
        } catch (QiniuException e) {
            e.printStackTrace();
        }
        return true;
    }


}
