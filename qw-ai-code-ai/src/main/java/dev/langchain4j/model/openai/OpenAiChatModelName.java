// 手动创建枚举类，放在 dev.langchain4j.model.openai 包下
package dev.langchain4j.model.openai;

/**
 * 手动定义 OpenAI 聊天模型名称枚举（适配原代码逻辑）
 */
public enum OpenAiChatModelName {
    GPT_4O_MINI("gpt-4o-mini"),
    GPT_4O("gpt-4o"),
    GPT_4_TURBO("gpt-4-turbo"),
    GPT_3_5_TURBO("gpt-3.5-turbo");

    private final String value;

    OpenAiChatModelName(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}