package com.heima.wemedia.test;

import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.wemedia.WemediaApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

@SpringBootTest(classes = WemediaApplication.class)
@RunWith(SpringRunner.class)
public class AliyunTest {

    @Autowired
    private GreenTextScan greenTextScan;

    @Autowired
    private GreenImageScan greenImageScan;

    /**
     * 测试文本内容审核
     */
    @Test
    public void testScanText() throws Exception {
        Map map = greenTextScan.greeTextScan("This is a test message");
        System.out.println(map);
    }

    /**
     *测试图片审核
     */
    @Test
    public void testScanImage() {

    }
}
