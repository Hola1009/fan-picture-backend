package com.fancier.picture.backend.helper.tencentCOS;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author <a href="https://github.com/hola1009">fancier</a>
 **/
@Data
@Component
@ConfigurationProperties(prefix = "cos.client")
public class TencentCOSConfigure {
    private String host;
    private String secretId;
    private String secretKey;
    private String region;
    private String bucket;

    @Bean
    public COSClient cosClient() {

        BasicCOSCredentials cred = new BasicCOSCredentials(secretId, secretKey);

        ClientConfig clientConfig = new ClientConfig(new Region(region));
        clientConfig.setHttpProtocol(HttpProtocol.https);

        return new COSClient(cred, clientConfig);
    }

}
