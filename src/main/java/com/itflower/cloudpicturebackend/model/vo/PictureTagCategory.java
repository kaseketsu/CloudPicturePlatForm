package com.itflower.cloudpicturebackend.model.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PictureTagCategory implements Serializable {

    private static final long serialVersionUID = -4031737933517424721L;

    private List<String> tagList;

    private List<String> categoryList;
}
