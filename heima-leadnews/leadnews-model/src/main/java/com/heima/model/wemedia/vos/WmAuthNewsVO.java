package com.heima.model.wemedia.vos;

import com.heima.model.wemedia.pojos.WmNews;
import lombok.Data;

@Data
public class WmAuthNewsVO extends WmNews {

    /**
     * 审核端需要展示作者名
     */
    private String authorName;
}
