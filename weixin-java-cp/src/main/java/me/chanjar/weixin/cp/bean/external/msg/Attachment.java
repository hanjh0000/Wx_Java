package me.chanjar.weixin.cp.bean.external.msg;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Description: 消息附件
 * @Author: mayingjie@sensorsdata.com
 * @Date: 2021/4/6
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {

  private String msgtype;

  private Image image;

  private Link link;

  private MiniProgram miniprogram;

}
