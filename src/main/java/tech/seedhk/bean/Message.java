package tech.seedhk.bean;

import java.io.Serializable;

/**
 * @author zcy
 * @date 2019年3月23日 下午2:53:40
*/
public class Message implements Serializable{
	
	private static final long serialVersionUID = -3037045590792423709L;
	
	public static final Integer LOGIN=1;//登录
	public static final Integer LOGOUT=-1;//登出
	public static final Integer REGSERVER=100;//注册成为一个服务端
	public static final Integer REGCLIENT=200;//注册成为一个客户端
	public static final Integer SUCCESS=0;//成功
	
	
	public Message() {
	}
	public Message(Integer messageType) {
		this.messageType = messageType;
	}
	
	private Integer messageType;//消息类型
	
	public Integer getMessageType() {
		return messageType;
	}
	public void setMessageType(Integer messageType) {
		this.messageType = messageType;
	}

}
