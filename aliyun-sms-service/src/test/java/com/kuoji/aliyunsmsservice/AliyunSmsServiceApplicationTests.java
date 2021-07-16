package com.kuoji.aliyunsmsservice;

import com.aliyuncs.CommonRequest;
import com.aliyuncs.CommonResponse;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.http.MethodType;
import com.aliyuncs.profile.DefaultProfile;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

@SpringBootTest
class AliyunSmsServiceApplicationTests {

    @Test
    void contextLoads() {
        // 连接阿里云
        DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", "LTAI4G7baoM2N14S7QgaKRhr", "XXX");
        IAcsClient client = new DefaultAcsClient(profile);

        // 构建请求
        CommonRequest request = new CommonRequest();

        request.setSysMethod(MethodType.POST);
        request.setSysDomain("dysmsapi.aliyuncs.com");
        request.setSysVersion("2017-05-25");
        request.setSysAction("SendSms");

        // 自定义的参数 (手机号，验证码，签名，模板)
        request.putQueryParameter("PhoneNumbers", "176XXXXXXXX");
        request.putQueryParameter("SignName", "kk的在线教育网站");
        request.putQueryParameter("TemplateCode", "XXX");
        request.putQueryParameter("TemplateParam", "{\"code\":\"1111\"}");
        try {
            CommonResponse response = client.getCommonResponse(request);
            System.out.println(response.getData());
        } catch (ServerException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    @Test
    void testHasLength(){
        System.out.println(StringUtils.hasLength(""));
        System.out.println(StringUtils.hasLength(null));
        System.out.println(StringUtils.hasLength("  "));
        System.out.println(StringUtils.hasLength("1"));
        System.out.println("============================");
        System.out.println(StringUtils.hasText(""));
        System.out.println(StringUtils.hasText(" "));
        System.out.println(StringUtils.hasText(null));
        System.out.println(StringUtils.hasText("12313"));
    }

}
