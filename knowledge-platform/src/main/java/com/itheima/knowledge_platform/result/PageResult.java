package com.itheima.knowledge_platform.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResult<T> implements Serializable {

    private Long total; //总记录数

    private Integer pageNum;//页码

    private Integer pageSize;//单页存放数据数目

    private List<T> records; //当前页数据集合

}