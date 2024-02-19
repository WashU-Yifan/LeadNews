package com.heima.model.wemedia.dtos;

import com.heima.model.common.dtos.PageRequestDto;
import lombok.Data;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

@Data
public class WmSensitivePageReqDto extends PageRequestDto {

    /**
     * 用户名
     */
    private String name;
}
