package com.heima.model.behavior.pojos;

import lombok.Data;

@Data
public class Behavior {

    private Boolean isLike = false;

    private Boolean isUnlike = false;

    private Boolean isCollection = false;

    private Boolean isFollow = false;
}
