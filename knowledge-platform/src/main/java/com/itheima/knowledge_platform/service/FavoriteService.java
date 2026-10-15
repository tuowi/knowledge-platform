package com.itheima.knowledge_platform.service;

import com.itheima.knowledge_platform.pojo.vo.FavoriteDocumentVO;
import com.itheima.knowledge_platform.result.PageResult;

public interface FavoriteService {

    /**
     * 收藏文档
     * @param documentId
     */
    void favorite(Long documentId);

    /**
     * 取消收藏
     * @param documentId
     */
    void cancel(Long documentId);

    /**
     * 获取我的收藏列表
     * @param page
     * @param size
     * @return
     */
    PageResult<FavoriteDocumentVO> page(Integer page, Integer size);
}
