package com.example.onlinejudge.entity;

import lombok.Data;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@Data
@ApiModel(description = "管理员实体")
public class Admin extends Account {
    @ApiModelProperty("管理员ID")
    private Integer id;
}
