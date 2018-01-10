package tech.seedhk.buffer;

import java.io.Serializable;
import java.nio.ByteBuffer;

/**
 * 包含内容：
 * 	1.命令
 *  2.命令内容(Object对象，可能是一段文本，可能是一个对象)
 *  3.附件内容
 *  4.是否有文件及文件名+后缀
 * @author Seed
 * 2017年12月27日 下午2:00:36
 */
public class BodyBuffer implements Serializable{

	private static final long serialVersionUID = 1L;

	
	private String cmd;
	private Object content;
	private byte[] attach;
	private boolean hasAttach;//是否有附件
	private String attachName;
	private boolean isReady;	//该body是否已经准备好进行传输了

	
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public Object getContent() {
		return content;
	}
	public void setContent(Object content) {
		this.content = content;
	}
	public ByteBuffer getAttach() {
		return ByteBuffer.wrap(attach);
	}
	public void setAttach(ByteBuffer attach) {
		byte[] b=attach.array();
		this.attach = b;
	}
	
	public void setAttach(byte[] b) {
		this.attach = b;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public boolean isHasAttach() {
		return hasAttach;
	}
	public void setHasAttach(boolean hasAttach) {
		this.hasAttach = hasAttach;
	}
	public String getAttachName() {
		return attachName;
	}
	public void setAttachName(String attachName) {
		this.attachName = attachName;
	}
	public boolean isReady() {
		return isReady;
	}
	public void setReady(boolean isReady) {
		this.isReady = isReady;
	}
	
}
