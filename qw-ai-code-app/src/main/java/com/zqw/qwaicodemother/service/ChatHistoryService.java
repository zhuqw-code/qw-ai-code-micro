package com.zqw.qwaicodemother.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.zqw.qwaicodemother.model.dto.chat_history.ChatHistoryQueryRequest;
import com.zqw.qwaicodemother.model.entity.ChatHistory;
import com.zqw.qwaicodemother.model.entity.User;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 *  服务层。
 *
 * @author <a href="#">程序员zqw</a>
 */
public interface ChatHistoryService extends IService<ChatHistory> {

    /**
     * 根据应用id查询聊天记录
     * @param appId 应用id
     * @param chatMemory 聊天记录
     * @param maxCount 最大数量
     * @return 聊天记录
     */
    int loadChatHistoryToMemory(Long appId, MessageWindowChatMemory chatMemory, int maxCount);

    /**
     * 根据应用id删除聊天记录
     *
     * @param appId 应用id
     * @return {@code true} 删除成功，{@code false} 删除失败
     */
    boolean deleteAppById(Long appId);

    /**
     * 添加聊天消息。
     *
     * @param appId 应用id
     * @param message 消息
     * @param messageType 消息类型
     * @param userId 用户id
     * @return {@code true} 添加成功，{@code false} 添加失败
     */
    boolean addChatMessage(Long appId, String message, String messageType, Long userId);



    QueryWrapper getQueryWrapper(ChatHistoryQueryRequest chatHistoryQueryRequest);

    Page<ChatHistory> listAppChatHistoryByPage(Long appId, int pageSize,
                                               LocalDateTime lastCreateTime,
                                               User loginUser);
}
