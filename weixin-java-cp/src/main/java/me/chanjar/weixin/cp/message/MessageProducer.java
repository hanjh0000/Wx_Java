package me.chanjar.weixin.cp.message;


import me.chanjar.weixin.cp.bean.message.WxCpXmlMessage;

public interface MessageProducer {
    void push(String topic,WxCpXmlMessage message);
}
